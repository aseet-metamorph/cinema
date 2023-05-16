package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.stream.IntStream;


public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;

    public TicketServiceImpl(final TicketPaymentService paymentService, final SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    private static IntStream apply(final TicketRequest ticketRequest) {
        return IntStream.of(ticketRequest.getNoOfTickets());
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(final TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
        final TicketRequest[] ticketTypeRequests = ticketPurchaseRequest.getTicketTypeRequests();
        final long accountId = ticketPurchaseRequest.getAccountId();
        final int numberOfAdults = Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> TicketRequest.Type.ADULT.equals(ticketTypeRequest.getTicketType()))
                .flatMapToInt(TicketServiceImpl::apply)
                .sum();

        final int numberOfChildren = Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> TicketRequest.Type.CHILD.equals(ticketTypeRequest.getTicketType()))
                .flatMapToInt(TicketServiceImpl::apply)
                .sum();

        final int numberOfInfant = Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> TicketRequest.Type.INFANT.equals(ticketTypeRequest.getTicketType()))
                .flatMapToInt(TicketServiceImpl::apply)
                .sum();
        final long totalNumOfTickets = numberOfAdults + numberOfChildren + numberOfInfant;

        validateTicketPurchaseRequest(numberOfAdults, totalNumOfTickets);

        final int totalPrice = numberOfAdults * 20 + numberOfChildren * 10;

        paymentService.makePayment(accountId, totalPrice);
        reservationService.reserveSeat(accountId, numberOfAdults + numberOfChildren);
    }

    private void validateTicketPurchaseRequest(final long numberOfAdults, final long totalNumOfTickets) {
        if (totalNumOfTickets > 20) {
            throw new InvalidPurchaseException("Maximum 20 number of tickets can be booked at a time.");
        } else if (numberOfAdults <= 0) {
            throw new InvalidPurchaseException("Ticket purchased not allowed without an adult.");
        }
    }
}

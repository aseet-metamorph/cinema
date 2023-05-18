package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;

    public TicketServiceImpl(final TicketPaymentService paymentService, final SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(final TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
        final TicketRequest[] ticketTypeRequests = ticketPurchaseRequest.getTicketTypeRequests();
        final long accountId = ticketPurchaseRequest.getAccountId();

        final Map<TicketRequest.Type, Integer> ticketTypeNumberOfTicketsMap = Arrays.stream(ticketTypeRequests)
                .collect(Collectors.groupingBy(TicketRequest::getTicketType, Collectors.summingInt(TicketRequest::getNoOfTickets)));

        final int numberOfAdults = Optional.ofNullable(ticketTypeNumberOfTicketsMap.get(TicketRequest.Type.ADULT))
                .orElse(0);
        final int numberOfChildren = Optional.ofNullable(ticketTypeNumberOfTicketsMap.get(TicketRequest.Type.CHILD))
                .orElse(0);
        final int numberOfInfant = Optional.ofNullable(ticketTypeNumberOfTicketsMap.get(TicketRequest.Type.INFANT))
                .orElse(0);

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

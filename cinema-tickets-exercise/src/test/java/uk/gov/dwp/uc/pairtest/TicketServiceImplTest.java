package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {
    @Mock
    private TicketPaymentService paymentService;
    @Mock
    private SeatReservationService reservationService;
    private TicketService ticketService;

    private TicketPurchaseRequest purchaseRequest;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(paymentService, reservationService);
    }

    @Test
    void purchaseMoreThan20Tickets() {
        final TicketRequest[] ticketRequests = new TicketRequest[3];
        ticketRequests[0] = new TicketRequest(TicketRequest.Type.ADULT, 17);
        ticketRequests[1] = new TicketRequest(TicketRequest.Type.CHILD, 3);
        ticketRequests[2] = new TicketRequest(TicketRequest.Type.INFANT, 5);

        purchaseRequest = new TicketPurchaseRequest(1, ticketRequests);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(purchaseRequest));
    }

    @Test
    void bookTicketsWithoutAdult() {
        final TicketRequest[] ticketRequests = new TicketRequest[2];
        ticketRequests[0] = new TicketRequest(TicketRequest.Type.CHILD, 17);
        ticketRequests[1] = new TicketRequest(TicketRequest.Type.CHILD, 2);
        purchaseRequest = new TicketPurchaseRequest(1, ticketRequests);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(purchaseRequest));
    }

    @Test
    void bookTicketsWithInvalidInput() {
        final TicketRequest[] ticketRequests = new TicketRequest[2];
        ticketRequests[0] = new TicketRequest(TicketRequest.Type.ADULT, 0);
        ticketRequests[1] = new TicketRequest(TicketRequest.Type.CHILD, 0);
        purchaseRequest = new TicketPurchaseRequest(1, ticketRequests);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(purchaseRequest));
    }

    @Test
    void bookTicketsSuccess() {
        final TicketRequest[] ticketRequests = new TicketRequest[3];
        ticketRequests[0] = new TicketRequest(TicketRequest.Type.ADULT, 2);
        ticketRequests[1] = new TicketRequest(TicketRequest.Type.CHILD, 2);
        ticketRequests[2] = new TicketRequest(TicketRequest.Type.INFANT, 1);
        final int totalAmountPay = 2 * 20 + 2 * 10;
        ticketService.purchaseTickets(new TicketPurchaseRequest(1, ticketRequests));
        Mockito.verify(paymentService).makePayment(1, totalAmountPay);
        Mockito.verify(reservationService).reserveSeat(1, 4);
    }

    @Test
    void testImmutabilityOfTicketPurchaseRequest() {
        final TicketRequest[] ticketRequests = new TicketRequest[3];
        ticketRequests[0] = new TicketRequest(TicketRequest.Type.ADULT, 2);
        ticketRequests[1] = new TicketRequest(TicketRequest.Type.CHILD, 2);
        ticketRequests[2] = new TicketRequest(TicketRequest.Type.INFANT, 1);
        final TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(1, ticketRequests);
        ticketService.purchaseTickets(ticketPurchaseRequest);
        TicketRequest[] existingTicketRequest = ticketPurchaseRequest.getTicketTypeRequests();
        existingTicketRequest[0] = new TicketRequest(TicketRequest.Type.CHILD, 3);
        Assertions.assertEquals(ticketRequests[0], ticketPurchaseRequest.getTicketTypeRequests()[0]);
        assertEquals(ticketPurchaseRequest.getAccountId(), 1);
    }
}
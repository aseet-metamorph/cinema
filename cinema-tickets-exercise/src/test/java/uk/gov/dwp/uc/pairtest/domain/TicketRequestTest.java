package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketRequestTest {

    private TicketRequest ticketRequest;

    @Test
    void testImmutable() throws NoSuchFieldException {

        ticketRequest = new TicketRequest(TicketRequest.Type.ADULT, 5);

        assertEquals(TicketRequest.Type.ADULT, ticketRequest.getTicketType());
        Field fieldType = ticketRequest.getTicketType().getClass().getDeclaredField("ADULT");
        fieldType.setAccessible(true);
        assertThrows(IllegalAccessException.class, () -> fieldType.set(ticketRequest, TicketRequest.Type.CHILD));

    }

}
package uk.gov.dwp.uc.pairtest.domain;

import java.util.Arrays;

/**
 * Should be an Immutable Object
 */

public final class TicketPurchaseRequest {

    private final long accountId;

    private final TicketRequest[] ticketRequests;

    public TicketPurchaseRequest(final long accountId, final TicketRequest[] ticketRequests) {
        this.accountId = accountId;
        this.ticketRequests = Arrays.copyOf(ticketRequests, ticketRequests.length);
    }

    public long getAccountId() {
        return accountId;
    }

    public TicketRequest[] getTicketTypeRequests() {
        return Arrays.copyOf(ticketRequests, ticketRequests.length);
    }

}

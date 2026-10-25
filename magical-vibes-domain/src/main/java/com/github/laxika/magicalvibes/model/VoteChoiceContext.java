package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Context of a prompt in which one player casts a vote (CR 701.38a). Its queue of voters still to vote
 * lets a vote answer be replayed with the same voter voting again first, so "you may vote an additional
 * time" works the same way for every vote.
 */
public interface VoteChoiceContext {

    /** Players still to vote after this answer, in voting order. */
    List<UUID> remainingVoterIds();

    /** This context with a different queue of voters still to vote. */
    VoteChoiceContext withRemainingVoterIds(List<UUID> remainingVoterIds);

    String sourceName();
}

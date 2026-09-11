package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;
import java.util.UUID;

/**
 * Delayed trigger: "Whenever a creature attacks this turn, it gains the configured keywords
 * until end of turn." Fires once for each attacking creature and is cleared at turn cleanup.
 */
public record DelayedAttackerKeywordGrant(UUID controllerId, Set<Keyword> keywords, Card sourceCard)
        implements DelayedAction {

    public DelayedAttackerKeywordGrant {
        keywords = Set.copyOf(keywords);
    }
}

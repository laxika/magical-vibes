package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Offers to reveal the top card of the controller's library. A revealed matching card may be put
 * onto the battlefield; otherwise, the revealed card is put on the bottom of that library.
 */
public record RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(
        CardPredicate predicate,
        Stage stage
) implements CardEffect {

    public enum Stage {
        MAY_REVEAL,
        MAY_PUT
    }

    public RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(CardPredicate predicate) {
        this(predicate, Stage.MAY_REVEAL);
    }

    public RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect withStage(Stage stage) {
        return new RevealTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(predicate, stage);
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Privately looks at the top card of the controller's library. A matching card may be put onto
 * the battlefield; if it is not, the controller may put it on the bottom of their library.
 */
public record LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(
        CardPredicate predicate,
        boolean enterTapped,
        Stage stage
) implements CardEffect {

    public enum Stage {
        MAY_PUT,
        MAY_BOTTOM
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(CardPredicate predicate,
                                                                      boolean enterTapped) {
        this(predicate, enterTapped, Stage.MAY_PUT);
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(CardPredicate predicate) {
        this(predicate, false);
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect withStage(Stage stage) {
        return new LookAtTopCardMayPutMatchingOntoBattlefieldOrBottomEffect(predicate, enterTapped, stage);
    }
}

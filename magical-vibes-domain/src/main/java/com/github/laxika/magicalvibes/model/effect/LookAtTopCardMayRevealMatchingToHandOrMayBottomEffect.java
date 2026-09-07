package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top card of the controller's library. If it matches {@code predicate}, the
 * controller may reveal it and put it into their hand. If it does not end up in hand, the
 * controller may put it on the bottom of their library.
 */
public record LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect(
        CardPredicate predicate,
        Stage stage
) implements CardEffect {

    public enum Stage {
        LOOK,
        MAY_HAND,
        MAY_BOTTOM
    }

    public LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect(CardPredicate predicate) {
        this(predicate, Stage.LOOK);
    }

    public LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect withStage(Stage stage) {
        return new LookAtTopCardMayRevealMatchingToHandOrMayBottomEffect(predicate, stage);
    }
}

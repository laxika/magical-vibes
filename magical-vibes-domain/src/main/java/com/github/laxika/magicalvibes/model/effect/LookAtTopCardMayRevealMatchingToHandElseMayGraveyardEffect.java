package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Look at the top card of the controller's library. If it matches {@code predicate}, the controller
 * may reveal it and put it into their hand. If they don't put the card into their hand (either
 * because it does not match, or they decline), they may put it into their graveyard; otherwise it
 * stays on top.
 *
 * <p>Used by Archghoul of Thraben ({@code CardSubtypePredicate(ZOMBIE)}). The {@link Stage} field
 * distinguishes the initial look (stack resolution) from the two may-ability follow-ups.
 */
public record LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect(
        CardPredicate predicate,
        Stage stage
) implements CardEffect {

    public enum Stage {
        /** Stack resolution: private look, then queue the appropriate may. */
        LOOK,
        /** May: reveal matching top card and put it into hand. */
        MAY_HAND,
        /** May: put the top card into the graveyard. */
        MAY_GRAVEYARD
    }

    public LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect(CardPredicate predicate) {
        this(predicate, Stage.LOOK);
    }

    public LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect withStage(Stage stage) {
        return new LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect(predicate, stage);
    }
}

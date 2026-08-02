package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Look at the top card of the controller's library. If it matches {@code predicate}, the controller
 * may reveal it and put it into their hand. When {@code mayGraveyardOtherwise} is set, a card that
 * does not end up in hand (either because it does not match, or because the reveal was declined)
 * may instead be put into the graveyard; otherwise it simply stays on top of the library.
 *
 * <p>Domri Rade's +1 uses {@code CardTypePredicate(CREATURE)} without the graveyard fallback;
 * Archghoul of Thraben uses {@code CardSubtypePredicate(ZOMBIE)} with it. The {@link Stage} field
 * distinguishes the initial look (stack resolution) from the may-ability follow-ups.
 */
public record LookAtTopCardMayRevealMatchingToHandEffect(
        CardPredicate predicate,
        boolean mayGraveyardOtherwise,
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

    public LookAtTopCardMayRevealMatchingToHandEffect(CardPredicate predicate, boolean mayGraveyardOtherwise) {
        this(predicate, mayGraveyardOtherwise, Stage.LOOK);
    }

    public LookAtTopCardMayRevealMatchingToHandEffect withStage(Stage stage) {
        return new LookAtTopCardMayRevealMatchingToHandEffect(predicate, mayGraveyardOtherwise, stage);
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Look at the top card of the controller's library. If it matches {@code predicate}, the controller
 * may reveal it and put it into their hand. A card that does not end up in hand (either because it
 * does not match, or because the reveal was declined) is handled according to
 * {@code otherwiseDestination}.
 *
 * <p>Domri Rade's +1 uses {@link OtherwiseDestination#TOP}; Archghoul of Thraben uses
 * {@link OtherwiseDestination#GRAVEYARD}; Vivien's Grizzly uses {@link OtherwiseDestination#BOTTOM}.
 * The {@link Stage} field distinguishes the initial look (stack resolution) from the may-ability
 * follow-ups.
 */
public record LookAtTopCardMayRevealMatchingToHandEffect(
        CardPredicate predicate,
        OtherwiseDestination otherwiseDestination,
        Stage stage,
        List<CardEffect> effectsIfCardPutIntoHand
) implements CardEffect {

    public LookAtTopCardMayRevealMatchingToHandEffect {
        effectsIfCardPutIntoHand = effectsIfCardPutIntoHand == null
                ? List.of() : List.copyOf(effectsIfCardPutIntoHand);
    }

    public enum OtherwiseDestination {
        TOP,
        GRAVEYARD,
        BOTTOM
    }

    public enum Stage {
        /** Stack resolution: private look, then queue the appropriate may. */
        LOOK,
        /** May: reveal matching top card and put it into hand. */
        MAY_HAND,
        /** May: put the top card into the graveyard. */
        MAY_GRAVEYARD
    }

    public LookAtTopCardMayRevealMatchingToHandEffect(CardPredicate predicate, boolean mayGraveyardOtherwise) {
        this(predicate,
                mayGraveyardOtherwise ? OtherwiseDestination.GRAVEYARD : OtherwiseDestination.TOP,
                Stage.LOOK,
                List.of());
    }

    public LookAtTopCardMayRevealMatchingToHandEffect(
            CardPredicate predicate, boolean mayGraveyardOtherwise,
            List<CardEffect> effectsIfCardPutIntoHand) {
        this(predicate,
                mayGraveyardOtherwise ? OtherwiseDestination.GRAVEYARD : OtherwiseDestination.TOP,
                Stage.LOOK,
                effectsIfCardPutIntoHand);
    }

    public LookAtTopCardMayRevealMatchingToHandEffect(
            CardPredicate predicate, OtherwiseDestination otherwiseDestination) {
        this(predicate, otherwiseDestination, Stage.LOOK, List.of());
    }

    public LookAtTopCardMayRevealMatchingToHandEffect(
            CardPredicate predicate, OtherwiseDestination otherwiseDestination, Stage stage) {
        this(predicate, otherwiseDestination, stage, List.of());
    }

    public LookAtTopCardMayRevealMatchingToHandEffect withStage(Stage stage) {
        return new LookAtTopCardMayRevealMatchingToHandEffect(
                predicate, otherwiseDestination, stage, effectsIfCardPutIntoHand);
    }
}

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
 * {@link OtherwiseDestination#GRAVEYARD}; Vivien's Grizzly uses {@link OtherwiseDestination#BOTTOM};
 * Bucolic Ranch uses {@link OtherwiseDestination#MAY_BOTTOM}. "If it shares a card type with that
 * permanent" is {@code CardSharesCardTypeWithPermanentsPredicate(Subjects.triggering())} (Amareth): the
 * predicate's subjects are bound to the resolving entry as the card is looked at. "You may reveal that card
 * and put it into your hand" with no condition is {@code CardTruePredicate}.
 *
 * <p>A {@code StepKind.REVEAL} step: the card revealed and put into hand (its {@code MovedCard} naming the hand),
 * or nothing. "If you do, you lose life equal to its mana value" (Sorin the Mirthless) is
 * {@code effectsIfCardPutIntoHand} = {@code LoseLifeEffect(AmountThisWay.revealedManaValue(), you())}, run after
 * the card is in the hand. The {@link Stage} field distinguishes the initial look (stack resolution) from the
 * may-ability follow-ups.
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
        /** The card stays on top of the library. */
        TOP,
        /** The controller may put the card into their graveyard. */
        GRAVEYARD,
        /** The card is put on the bottom of the library. */
        BOTTOM,
        /** The controller may put the card on the bottom of the library ("you may put it on the bottom"). */
        MAY_BOTTOM
    }

    public enum Stage {
        /** Stack resolution: private look, then queue the appropriate may. */
        LOOK,
        /** May: reveal matching top card and put it into hand. */
        MAY_HAND,
        /** May: put the top card into the graveyard. */
        MAY_GRAVEYARD,
        /** May: put the top card on the bottom of the library. */
        MAY_BOTTOM
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
            CardPredicate predicate, OtherwiseDestination otherwiseDestination,
            List<CardEffect> effectsIfCardPutIntoHand) {
        this(predicate, otherwiseDestination, Stage.LOOK, effectsIfCardPutIntoHand);
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

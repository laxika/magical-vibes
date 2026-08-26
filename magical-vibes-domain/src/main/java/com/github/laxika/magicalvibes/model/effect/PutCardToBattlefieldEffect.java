package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.Set;

/**
 * Lets the controller put a card from their hand onto the battlefield.
 * <p>
 * The {@code predicate} filters which cards in hand are valid choices, and
 * the {@code label} is used in the prompt shown to the player (e.g. "creature", "land",
 * "historic permanent").
 * <p>
 * Typically wrapped in a {@link MayEffect} for "you may put" wording. When {@code drawAndRepeat}
 * is true the choice itself is the "you may" (declinable HandCardChoice): putting a card draws one
 * and re-offers the choice until declined or no matching cards remain (Cultivator Colossus).
 * When {@code putAnyNumber} is true, putting re-offers without drawing until declined
 * (Wrenn and Seven's "put any number of land cards … tapped").
 *
 * @param predicate               filter for eligible cards in hand (e.g. {@code CardTypePredicate(CREATURE)},
 *                                {@code CardAllOfPredicate(CardIsHistoricPredicate, CardIsPermanentPredicate)})
 * @param label                   human-readable description of the card type for prompts (e.g. "creature", "historic permanent")
 * @param enterTapped             if {@code true}, the chosen card enters the battlefield tapped (e.g. Embrace the Paradox)
 * @param maxManaValueBoundedByX  if {@code true}, only cards whose mana value is at most the spell's X value
 *                                are eligible (e.g. Mind into Matter's "mana value X or less")
 * @param grantHaste              if {@code true}, the chosen card gains haste until end of turn (e.g. Incandescent Soulstoke)
 * @param sacrificeAtEndStep      if {@code true}, the chosen card is sacrificed at the beginning of the next end step
 *                                (e.g. Incandescent Soulstoke)
 * @param attachSourceEquipment   if {@code true}, the source Equipment is attached to the chosen card after it enters
 *                                (e.g. Deathrender's "put a creature card from your hand onto the battlefield and
 *                                attach this Equipment to it")
 * @param enterAttacking          if {@code true}, the chosen creature enters the battlefield attacking
 *                                (e.g. Preeminent Captain's "onto the battlefield tapped and attacking")
 * @param drawAndRepeat           if {@code true}, after putting a card the controller draws a card and the process
 *                                repeats until they decline or have no matching cards (Cultivator Colossus)
 * @param putAnyNumber            if {@code true}, after putting a card the process re-offers until decline / no matches
 *                                without drawing (Wrenn and Seven)
 * @param cloaked                 if {@code true}, the chosen card enters the battlefield cloaked and the hand-card
 *                                choice is mandatory
 * @param returnToHandAtEndStep   if {@code true}, the chosen permanent is returned to its owner's hand at the
 *                                beginning of the next end step, if it is still on the battlefield (Surprise Deployment)
 * @param enterTappedAndAttackingIf if non-null, a chosen card matching this predicate enters the battlefield
 *                                  tapped and attacking; other chosen cards enter normally
 */
public record PutCardToBattlefieldEffect(CardPredicate predicate, String label,
                                         boolean enterTapped, boolean maxManaValueBoundedByX,
                                         boolean grantHaste, boolean sacrificeAtEndStep,
                                         boolean attachSourceEquipment, boolean enterAttacking,
                                         boolean drawAndRepeat, boolean putAnyNumber,
                                         boolean faceDown, int faceDownPower, int faceDownToughness,
                                         Set<CardType> faceDownCardTypes,
                                         boolean cloaked,
                                         boolean returnExiledSourceIfSacrificed,
                                         boolean returnToHandAtEndStep,
                                         CardPredicate enterTappedAndAttackingIf) implements CardEffect {

    public PutCardToBattlefieldEffect {
        faceDownCardTypes = Set.copyOf(faceDownCardTypes);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label,
                                      boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep,
                                      boolean attachSourceEquipment, boolean enterAttacking,
                                      boolean drawAndRepeat, boolean putAnyNumber,
                                      boolean faceDown, int faceDownPower, int faceDownToughness,
                                      Set<CardType> faceDownCardTypes) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep,
                attachSourceEquipment, enterAttacking, drawAndRepeat, putAnyNumber,
                faceDown, faceDownPower, faceDownToughness, faceDownCardTypes, false, false, false, null);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label,
                                      boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep,
                                      boolean attachSourceEquipment, boolean enterAttacking,
                                      boolean drawAndRepeat, boolean putAnyNumber,
                                      boolean faceDown, int faceDownPower, int faceDownToughness,
                                      Set<CardType> faceDownCardTypes, boolean cloaked) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep,
                attachSourceEquipment, enterAttacking, drawAndRepeat, putAnyNumber,
                faceDown, faceDownPower, faceDownToughness, faceDownCardTypes, cloaked, false, false, null);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label,
                                      boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep,
                                      boolean attachSourceEquipment, boolean enterAttacking,
                                      boolean drawAndRepeat, boolean putAnyNumber) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep,
                attachSourceEquipment, enterAttacking, drawAndRepeat, putAnyNumber, false, 0, 0, Set.of());
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label) {
        this(predicate, label, false, false, false, false, false, false, false, false, false, 0, 0, Set.of());
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped) {
        this(predicate, label, enterTapped, false, false, false, false, false, false, false, false, 0, 0, Set.of());
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, false, false, false, false, false, false, false, 0, 0, Set.of());
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep, false, false, false, false, false, 0, 0, Set.of());
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep, boolean attachSourceEquipment) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep, attachSourceEquipment, false, false, false, false, 0, 0, Set.of());
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep, boolean attachSourceEquipment,
                                      boolean enterAttacking) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep, attachSourceEquipment,
                enterAttacking, false, false, false, 0, 0, Set.of());
    }

    /** Compact ctor used by the Cultivator Colossus re-offer path (drawAndRepeat only). */
    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep, boolean attachSourceEquipment,
                                      boolean enterAttacking, boolean drawAndRepeat) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep, attachSourceEquipment,
                enterAttacking, drawAndRepeat, false, false, 0, 0, Set.of());
    }

    /**
     * "Put a card from your hand onto the battlefield tapped and attacking" (e.g. Preeminent Captain).
     */
    public static PutCardToBattlefieldEffect tappedAndAttacking(CardPredicate predicate, String label) {
        return new PutCardToBattlefieldEffect(predicate, label, true, false, false, false, false, true, false, false);
    }

    /**
     * Cultivator Colossus: "you may put a land card from your hand onto the battlefield tapped.
     * If you do, draw a card and repeat this process."
     */
    public static PutCardToBattlefieldEffect tappedDrawAndRepeat(CardPredicate predicate, String label) {
        return new PutCardToBattlefieldEffect(predicate, label, true, false, false, false, false, false, true, false);
    }

    /**
     * Myojin of Life's Web: "Put any number of creature cards from your hand onto the battlefield."
     * Declinable HandCardChoice; each put re-offers until decline / no matches, entering untapped.
     */
    public static PutCardToBattlefieldEffect anyNumber(CardPredicate predicate, String label) {
        return new PutCardToBattlefieldEffect(predicate, label, false, false, false, false, false, false, false, true);
    }

    /**
     * Wrenn and Seven: "Put any number of land cards from your hand onto the battlefield tapped."
     * Declinable HandCardChoice; each put re-offers until decline / no matches (no draw).
     */
    public static PutCardToBattlefieldEffect tappedAnyNumber(CardPredicate predicate, String label) {
        return new PutCardToBattlefieldEffect(predicate, label, true, false, false, false, false, false, false, true);
    }

    public static PutCardToBattlefieldEffect anyNumberFaceDown(int power, int toughness,
                                                                 Set<CardType> cardTypes) {
        return new PutCardToBattlefieldEffect(new CardTruePredicate(),
                "card", false, false, false, false, false, false, false, true,
                true, power, toughness, cardTypes);
    }

    /** Puts a card from hand onto the battlefield cloaked; the card selection is mandatory. */
    public static PutCardToBattlefieldEffect cloakedFromHand() {
        return new PutCardToBattlefieldEffect(new CardTruePredicate(), "card",
                false, false, false, false, false, false, false, false,
                true, 2, 2, Set.of(CardType.CREATURE), true, false, false, null);
    }

    /** Shifty Doppelganger: return its exiled source card if the entered creature is sacrificed. */
    public PutCardToBattlefieldEffect returningExiledSourceIfSacrificed() {
        return new PutCardToBattlefieldEffect(predicate, label, enterTapped, maxManaValueBoundedByX,
                grantHaste, sacrificeAtEndStep, attachSourceEquipment, enterAttacking, drawAndRepeat,
                putAnyNumber, faceDown, faceDownPower, faceDownToughness, faceDownCardTypes, cloaked, true,
                returnToHandAtEndStep, enterTappedAndAttackingIf);
    }

    /** Surprise Deployment: return the chosen permanent to its owner's hand at the next end step. */
    public PutCardToBattlefieldEffect returningToHandAtEndStep() {
        return new PutCardToBattlefieldEffect(predicate, label, enterTapped, maxManaValueBoundedByX,
                grantHaste, sacrificeAtEndStep, attachSourceEquipment, enterAttacking, drawAndRepeat,
                putAnyNumber, faceDown, faceDownPower, faceDownToughness, faceDownCardTypes,
                cloaked, returnExiledSourceIfSacrificed, true, enterTappedAndAttackingIf);
    }

    public PutCardToBattlefieldEffect withEnterTappedAndAttackingIf(CardPredicate predicate) {
        return new PutCardToBattlefieldEffect(this.predicate, label, enterTapped, maxManaValueBoundedByX,
                grantHaste, sacrificeAtEndStep, attachSourceEquipment, enterAttacking, drawAndRepeat,
                putAnyNumber, faceDown, faceDownPower, faceDownToughness, faceDownCardTypes,
                cloaked, returnExiledSourceIfSacrificed, returnToHandAtEndStep, predicate);
    }
}

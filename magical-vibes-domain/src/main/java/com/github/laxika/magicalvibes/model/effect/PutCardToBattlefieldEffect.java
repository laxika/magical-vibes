package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

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
 */
public record PutCardToBattlefieldEffect(CardPredicate predicate, String label,
                                         boolean enterTapped, boolean maxManaValueBoundedByX,
                                         boolean grantHaste, boolean sacrificeAtEndStep,
                                         boolean attachSourceEquipment, boolean enterAttacking,
                                         boolean drawAndRepeat, boolean putAnyNumber) implements CardEffect {

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label) {
        this(predicate, label, false, false, false, false, false, false, false, false);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped) {
        this(predicate, label, enterTapped, false, false, false, false, false, false, false);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, false, false, false, false, false, false);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep, false, false, false, false);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep, boolean attachSourceEquipment) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep, attachSourceEquipment, false, false, false);
    }

    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep, boolean attachSourceEquipment,
                                      boolean enterAttacking) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep, attachSourceEquipment,
                enterAttacking, false, false);
    }

    /** Compact ctor used by the Cultivator Colossus re-offer path (drawAndRepeat only). */
    public PutCardToBattlefieldEffect(CardPredicate predicate, String label, boolean enterTapped, boolean maxManaValueBoundedByX,
                                      boolean grantHaste, boolean sacrificeAtEndStep, boolean attachSourceEquipment,
                                      boolean enterAttacking, boolean drawAndRepeat) {
        this(predicate, label, enterTapped, maxManaValueBoundedByX, grantHaste, sacrificeAtEndStep, attachSourceEquipment,
                enterAttacking, drawAndRepeat, false);
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
     * Wrenn and Seven: "Put any number of land cards from your hand onto the battlefield tapped."
     * Declinable HandCardChoice; each put re-offers until decline / no matches (no draw).
     */
    public static PutCardToBattlefieldEffect tappedAnyNumber(CardPredicate predicate, String label) {
        return new PutCardToBattlefieldEffect(predicate, label, true, false, false, false, false, false, false, true);
    }
}

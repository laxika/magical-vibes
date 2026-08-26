package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile up to {@code maxTargets} target cards from graveyard(s) and gain life for it. The targets
 * are chosen as the spell/ability goes on the stack; choosing zero cards covers a "you may" clause.
 *
 * @param maxTargets            how many graveyard cards may be chosen ("up to N target cards");
 *                              {@code 0} means the limit comes from the ability's X value
 * @param lifeGain              life gained — a flat amount (Rooftop Percher's 3) unless
 *                              {@code lifeGainPerExiledCard} is set; 0 = no life gain
 * @param lifeGainPerExiledCard when {@code true}, {@code lifeGain} is granted once per card actually
 *                              exiled this way ("you gain 1 life for each card exiled this way"),
 *                              so exiling nothing gains nothing (Rysorian Badger)
 * @param filter                an optional restriction on the choosable cards ("creature cards");
 *                              {@code null} = any card
 * @param assignNoCombatDamage  when {@code true} and at least one card was exiled, the ability's
 *                              source permanent assigns no combat damage this turn — the "if you do"
 *                              rider of an unblocked-attack trigger (Rysorian Badger)
 * @param conditionalFilter optional filter for a conditional life rider
 * @param conditionalLifeLossEachOpponent life lost by each opponent when the rider applies
 * @param conditionalLifeGain life gained by the controller when the rider applies
 * @param conditionalLifePerMatchingCard when {@code true}, the conditional life rider applies
 *                                      once for each exiled card matching {@code conditionalFilter}
 * @param singleGraveyard whether all selected cards must come from one graveyard
 * @param trackWithSource whether the exiled cards are tracked with the source permanent
 * @param ownGraveyardOnly whether ETB targeting is restricted to the controller's graveyard
 */
public record ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean lifeGainPerExiledCard,
                                            CardPredicate filter, boolean assignNoCombatDamage,
                                            CardPredicate conditionalFilter,
                                            int conditionalLifeLossEachOpponent,
                                            int conditionalLifeGain,
                                            boolean singleGraveyard,
                                            boolean conditionalLifePerMatchingCard,
                                            boolean trackWithSource,
                                            boolean ownGraveyardOnly)
        implements GraveyardCardChoosingEffect {

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain) {
        this(maxTargets, lifeGain, false, null, false, null, 0, 0, false, false, false, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean singleGraveyard) {
        this(maxTargets, lifeGain, false, null, false, null, 0, 0, singleGraveyard, false, false, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean lifeGainPerExiledCard,
                                         CardPredicate filter, boolean assignNoCombatDamage) {
        this(maxTargets, lifeGain, lifeGainPerExiledCard, filter, assignNoCombatDamage,
                null, 0, 0, false, false, false, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, CardPredicate conditionalFilter,
                                         int conditionalLifeLossEachOpponent, int conditionalLifeGain,
                                         boolean singleGraveyard) {
        this(maxTargets, 0, false, null, false, conditionalFilter,
                conditionalLifeLossEachOpponent, conditionalLifeGain, singleGraveyard, false, false, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, CardPredicate conditionalFilter,
                                         int conditionalLifeLossEachOpponent, int conditionalLifeGain,
                                         boolean singleGraveyard, boolean conditionalLifePerMatchingCard) {
        this(maxTargets, 0, false, null, false, conditionalFilter,
                conditionalLifeLossEachOpponent, conditionalLifeGain, singleGraveyard,
                conditionalLifePerMatchingCard, false, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, boolean singleGraveyard,
                                         boolean trackWithSource) {
        this(maxTargets, 0, false, null, false, null, 0, 0, singleGraveyard, false, trackWithSource, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, CardPredicate filter,
                                         boolean singleGraveyard, boolean trackWithSource,
                                         boolean ownGraveyardOnly) {
        this(maxTargets, 0, false, filter, false, null, 0, 0, singleGraveyard, false,
                trackWithSource, ownGraveyardOnly);
    }

    /** Whether the maximum target count is supplied by the ability's X value. */
    public boolean xScaled() {
        return maxTargets == 0;
    }

    @Override
    public int graveyardChoiceMaxTargets() {
        return maxTargets;
    }

    @Override
    public CardPredicate graveyardChoiceFilter() {
        return filter;
    }

    @Override
    public boolean singleGraveyard() {
        return singleGraveyard;
    }
}

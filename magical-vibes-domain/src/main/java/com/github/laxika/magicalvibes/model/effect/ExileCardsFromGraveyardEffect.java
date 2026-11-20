package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile up to {@code maxTargets} target cards from graveyard(s) and gain life for it. The targets
 * are chosen as the spell/ability goes on the stack; choosing zero cards covers a "you may" clause.
 *
 * <p>Records the {@code StepKind.EXILE} step: the cards exiled and the graveyards' owners, so "for each card exiled this
 * way" ({@code AmountThisWay}) and "a copy of that card" ({@code Subjects.cardsThisWay(StepKind.EXILE)}) can follow
 * it as later steps. A target that is no longer in a graveyard of the effect's scope or no longer matches
 * {@code filter} is not exiled (CR 608.2b).
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
 * @param ownGraveyardOnly whether the targets come from the controller's graveyard only (an ability's, and a spell's
 *                         exact choice: {@link #exactlyXFromOwnGraveyard})
 * @param exactTargets whether a spell-cast selection must contain exactly {@code maxTargets}
 *                     cards instead of allowing any number up to that limit
 * @param xTargetMultiplier multiplier applied to the paid X value when {@code maxTargets} is zero
 * @param allGraveyardsForSpell whether a spell-cast selection may span multiple graveyards (up to N, or with
 *                              {@code exactTargets} exactly N: {@link #exactlyXFromGraveyards})
 * @param maxTargetsFromMultikicker whether an ETB target limit is the number of multikicker
 *                                  payments made for the entering creature
 * @param putKickCountersOnExiledCards whether successfully exiled cards receive kick counters
 */
public record ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean lifeGainPerExiledCard,
                                            CardPredicate filter, boolean assignNoCombatDamage,
                                            CardPredicate conditionalFilter,
                                            int conditionalLifeLossEachOpponent,
                                            int conditionalLifeGain,
                                            boolean singleGraveyard,
                                            boolean conditionalLifePerMatchingCard,
                                            boolean trackWithSource,
                                            boolean ownGraveyardOnly,
                                            boolean exactTargets,
                                            int xTargetMultiplier,
                                            boolean allGraveyardsForSpell,
                                            boolean maxTargetsFromMultikicker,
                                            boolean putKickCountersOnExiledCards)
        implements GraveyardCardChoosingEffect {

    public ExileCardsFromGraveyardEffect {
        if (xTargetMultiplier < 1) {
            throw new IllegalArgumentException("xTargetMultiplier must be positive");
        }
    }

    /** Backward-compatible constructor for ordinary graveyard exile effects. */
    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean lifeGainPerExiledCard,
                                         CardPredicate filter, boolean assignNoCombatDamage,
                                         CardPredicate conditionalFilter,
                                         int conditionalLifeLossEachOpponent,
                                         int conditionalLifeGain,
                                         boolean singleGraveyard,
                                         boolean conditionalLifePerMatchingCard,
                                         boolean trackWithSource,
                                         boolean ownGraveyardOnly,
                                         boolean exactTargets,
                                         int xTargetMultiplier,
                                         boolean allGraveyardsForSpell) {
        this(maxTargets, lifeGain, lifeGainPerExiledCard, filter, assignNoCombatDamage,
                conditionalFilter, conditionalLifeLossEachOpponent, conditionalLifeGain,
                singleGraveyard, conditionalLifePerMatchingCard, trackWithSource, ownGraveyardOnly,
                exactTargets, xTargetMultiplier, allGraveyardsForSpell, false, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain) {
        this(maxTargets, lifeGain, false, null, false, null, 0, 0,
                false, false, false, false, false, 1, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean singleGraveyard) {
        this(maxTargets, lifeGain, false, null, false, null, 0, 0,
                singleGraveyard, false, false, false, false, 1, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean singleGraveyard,
                                         boolean exactTargets) {
        this(maxTargets, lifeGain, false, null, false, null, 0, 0,
                singleGraveyard, false, false, false, exactTargets, 1, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean lifeGainPerExiledCard,
                                         CardPredicate filter, boolean assignNoCombatDamage) {
        this(maxTargets, lifeGain, lifeGainPerExiledCard, filter, assignNoCombatDamage,
                null, 0, 0, false, false, false, false, false, 1, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, CardPredicate conditionalFilter,
                                         int conditionalLifeLossEachOpponent, int conditionalLifeGain,
                                         boolean singleGraveyard) {
        this(maxTargets, 0, false, null, false, conditionalFilter,
                conditionalLifeLossEachOpponent, conditionalLifeGain, singleGraveyard,
                false, false, false, false, 1, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, CardPredicate conditionalFilter,
                                         int conditionalLifeLossEachOpponent, int conditionalLifeGain,
                                         boolean singleGraveyard, boolean conditionalLifePerMatchingCard) {
        this(maxTargets, 0, false, null, false, conditionalFilter,
                conditionalLifeLossEachOpponent, conditionalLifeGain, singleGraveyard,
                conditionalLifePerMatchingCard, false, false, false, 1, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, boolean singleGraveyard,
                                         boolean trackWithSource) {
        this(maxTargets, 0, false, null, false, null, 0, 0,
                singleGraveyard, false, trackWithSource, false, false, 1, false);
    }

    /** Creates a spell effect whose up-to target limit is the paid X multiplied by the factor. */
    public static ExileCardsFromGraveyardEffect upToXTimesCardsFromAllGraveyards(int xTargetMultiplier) {
        return new ExileCardsFromGraveyardEffect(
                0, 0, false, null, false, null, 0, 0, false, false, false, false,
                false, xTargetMultiplier, true);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, CardPredicate filter,
                                         boolean singleGraveyard, boolean trackWithSource,
                                         boolean ownGraveyardOnly) {
        this(maxTargets, 0, false, filter, false, null, 0, 0, singleGraveyard, false,
                trackWithSource, ownGraveyardOnly, false, 1, false);
    }

    public static ExileCardsFromGraveyardEffect exactFromSingleGraveyard(
            int count, CardPredicate filter, boolean trackWithSource) {
        return new ExileCardsFromGraveyardEffect(count, 0, false, filter, false, null,
                0, 0, true, false, trackWithSource, false, true, 1, false);
    }

    /** "Exile X target [filter] cards from your graveyard" on a spell (Hour of Eternity, Midnight Ritual). */
    public static ExileCardsFromGraveyardEffect exactlyXFromOwnGraveyard(CardPredicate filter) {
        return new ExileCardsFromGraveyardEffect(0, 0, false, filter, false, null, 0, 0,
                false, false, false, true, true, 1, false);
    }

    /** "Exile X target [filter] cards from graveyards" on a spell, from any graveyards together (Foggy Swamp Visions). */
    public static ExileCardsFromGraveyardEffect exactlyXFromGraveyards(CardPredicate filter) {
        return new ExileCardsFromGraveyardEffect(0, 0, false, filter, false, null, 0, 0,
                false, false, false, false, true, 1, true);
    }

    /**
     * The graveyards a spell's exact choice ({@code exactTargets}) takes its cards from: the controller's
     * ({@code ownGraveyardOnly}), every graveyard together ({@code allGraveyardsForSpell}), or {@code null} for
     * "from a single graveyard" and for an up-to choice.
     */
    public GraveyardSearchScope exactSpellGraveyardScope() {
        if (!exactTargets) {
            return null;
        }
        if (ownGraveyardOnly) {
            return GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
        }
        return allGraveyardsForSpell ? GraveyardSearchScope.ALL_GRAVEYARDS : null;
    }

    @Override
    public boolean choosesFromControllersGraveyardOnly() {
        return ownGraveyardOnly;
    }

    /** Whether the maximum target count is supplied by the ability's X value. */
    public boolean xScaled() {
        return maxTargets == 0;
    }

    /** Returns the cast-time maximum target count for the supplied paid X value. */
    public int maxTargetsForX(int xValue) {
        if (!xScaled()) {
            return maxTargets;
        }
        long scaled = Math.max(0L, (long) xValue * xTargetMultiplier);
        return (int) Math.min(Integer.MAX_VALUE, scaled);
    }

    @Override
    public TargetSpec targetSpec() {
        // Spell casting gathers these targets through a MultiGraveyardChoice before the
        // spell is put on the stack. The ordinary single-target validator runs before that
        // choice and would incorrectly require a graveyard card ID in the initial cast call.
        return TargetSpec.NONE;
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

    @Override
    public boolean graveyardChoiceExactTargets() {
        return exactTargets;
    }
}

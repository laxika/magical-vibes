package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exile up to {@code maxTargets} target cards from graveyard(s) and gain life for it. The targets
 * are chosen as the spell/ability goes on the stack; choosing zero cards covers a "you may" clause.
 *
 * @param maxTargets            how many graveyard cards may be chosen ("up to N target cards")
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
 */
public record ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean lifeGainPerExiledCard,
                                            CardPredicate filter, boolean assignNoCombatDamage,
                                            boolean singleGraveyard)
        implements GraveyardCardChoosingEffect {

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain) {
        this(maxTargets, lifeGain, false, null, false, false);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean singleGraveyard) {
        this(maxTargets, lifeGain, false, null, false, singleGraveyard);
    }

    public ExileCardsFromGraveyardEffect(int maxTargets, int lifeGain, boolean lifeGainPerExiledCard,
                                         CardPredicate filter, boolean assignNoCombatDamage) {
        this(maxTargets, lifeGain, lifeGainPerExiledCard, filter, assignNoCombatDamage, false);
    }

    @Override
    public int graveyardChoiceMaxTargets() {
        return maxTargets;
    }

    @Override
    public CardPredicate graveyardChoiceFilter() {
        return filter;
    }
}

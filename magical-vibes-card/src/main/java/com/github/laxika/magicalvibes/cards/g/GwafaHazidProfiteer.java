package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "110")
public class GwafaHazidProfiteer extends Card {

    public GwafaHazidProfiteer() {
        // {W}{U}, {T}: Put a bribery counter on target creature you don't control. Its controller draws a card.
        // The counter effect owns the single permanent target; the draw effect piggybacks on it, making
        // that same target creature's controller (never you, per the filter) draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{U}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.BRIBERY, 1),
                        new TargetPermanentControllerDrawsCardEffect()),
                "{W}{U}, {T}: Put a bribery counter on target creature you don't control. Its controller draws a card.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                        "Target must be a creature you don't control")));

        // Creatures with bribery counters on them can't attack or block.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantAttackOrBlockEffect(
                new PermanentHasCountersPredicate(CounterType.BRIBERY),
                "Creatures with bribery counters on them can't attack or block"));
    }
}

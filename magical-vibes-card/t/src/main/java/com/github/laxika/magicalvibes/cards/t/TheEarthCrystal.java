package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "184")
@CardRegistration(set = "FIN", collectorNumber = "342")
public class TheEarthCrystal extends Card {

    public TheEarthCrystal() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardColorPredicate(CardColor.GREEN), 1, CostModificationScope.SELF));
        addEffect(EffectSlot.STATIC, new DoublePlusOnePlusOneCountersEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{G}{G}",
                List.of(DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                        CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "{4}{G}{G}, {T}: Distribute two +1/+1 counters among one or two target creatures you control.",
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureYouControl()),
                1,
                2));
    }
}

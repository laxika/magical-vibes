package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "215")
public class HulkGammaGoliath extends Card {

    public HulkGammaGoliath() {
        addEffect(EffectSlot.STATIC, new ReduceActivatedAbilityCostEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                3,
                true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{R}{G}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 5)),
                "Power-up — {6}{R}{G}: Put five +1/+1 counters on Hulk. Activate each power-up "
                        + "ability only once. Reduce the cost by his mana cost if he entered this turn."
        ).withPowerUp());
    }
}

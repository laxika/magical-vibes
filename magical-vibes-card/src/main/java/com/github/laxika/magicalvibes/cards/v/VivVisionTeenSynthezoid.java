package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "256")
public class VivVisionTeenSynthezoid extends Card {

    public VivVisionTeenSynthezoid() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourcePowerAtLeast(4),
                new DrawCardEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "Power-up — {7}: Put two +1/+1 counters on Viv Vision. Activate each power-up ability only once. "
                        + "Reduce the cost by her mana cost if she entered this turn."
        ).withPowerUp());
    }
}

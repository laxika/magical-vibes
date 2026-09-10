package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "76")
public class StatureSizeShifter extends Card {

    public StatureSizeShifter() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourcePowerAtLeast(2)),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{U}{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue())),
                "Power-up — {X}{U}{U}: Put X +1/+1 counters on Stature. Activate each power-up ability only "
                        + "once. Reduce the cost by her mana cost if she entered this turn."
        ).withPowerUp());
    }
}

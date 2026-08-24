package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.amount.CountersOnTargetPermanent;

@CardRegistration(set = "SPM", collectorNumber = "67")
public class SpiderManNoir extends Card {

    public SpiderManNoir() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new ConditionalEffect(
                new AttacksAlone(),
                SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new SurveilEffect(new CountersOnTargetPermanent(CounterType.PLUS_ONE_PLUS_ONE))
                )));
    }
}

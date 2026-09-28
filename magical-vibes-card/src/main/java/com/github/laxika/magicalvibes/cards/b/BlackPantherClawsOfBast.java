package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

@CardRegistration(set = "MSC", collectorNumber = "829")
public class BlackPantherClawsOfBast extends Card {

    public BlackPantherClawsOfBast() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ULG", collectorNumber = "41")
public class SecondChance extends Card {

    public SecondChance() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new ControllerLifeAtMost(5),
                        SequenceEffect.of(new SacrificeSelfEffect(), new ControllerExtraTurnEffect(1))));
    }
}

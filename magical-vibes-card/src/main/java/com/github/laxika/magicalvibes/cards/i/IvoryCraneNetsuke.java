package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "SOK", collectorNumber = "155")
public class IvoryCraneNetsuke extends Card {

    public IvoryCraneNetsuke() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new CardsInHandAtLeast(7), new GainLifeEffect(4)));
    }
}

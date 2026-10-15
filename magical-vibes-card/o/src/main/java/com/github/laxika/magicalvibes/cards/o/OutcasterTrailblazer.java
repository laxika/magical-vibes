package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;

@CardRegistration(set = "OTJ", collectorNumber = "173")
public class OutcasterTrailblazer extends Card {

    public OutcasterTrailblazer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardAnyColorManaEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(4, new DrawCardEffect(1)));
    }
}

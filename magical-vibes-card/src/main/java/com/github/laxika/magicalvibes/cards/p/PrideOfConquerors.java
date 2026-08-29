package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "RIX", collectorNumber = "17")
public class PrideOfConquerors extends Card {

    public PrideOfConquerors() {
        addEffect(EffectSlot.SPELL, new AscendEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerHasCityBlessing(),
                new BoostAllOwnCreaturesEffect(2, 2)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(new ControllerHasCityBlessing()),
                new BoostAllOwnCreaturesEffect(1, 1)));
    }
}

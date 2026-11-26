package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "RIX", collectorNumber = "17")
public class PrideOfConquerors extends Card {

    public PrideOfConquerors() {
        addSpellEffect(new AscendEffect());
        addSpellEffect(new ConditionalEffect(new ControllerHasCityBlessing(),
                BoostEffect.creaturesYouControl(2, 2)));
        addSpellEffect(new ConditionalEffect(new NotCondition(new ControllerHasCityBlessing()),
                BoostEffect.creaturesYouControl(1, 1)));
    }
}

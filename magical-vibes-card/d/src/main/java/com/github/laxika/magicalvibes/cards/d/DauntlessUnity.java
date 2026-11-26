package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "ZNR", collectorNumber = "9")
public class DauntlessUnity extends Card {

    public DauntlessUnity() {
        addStaticEffect(new KickerEffect("{1}{W}"));
        addSpellEffect(new ConditionalReplacementEffect(new Kicked(),
                BoostEffect.creaturesYouControl(1, 1),
                BoostEffect.creaturesYouControl(2, 1)));
    }
}

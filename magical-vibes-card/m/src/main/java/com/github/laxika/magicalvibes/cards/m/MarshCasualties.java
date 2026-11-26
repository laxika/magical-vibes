package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "ZEN", collectorNumber = "101")
@CardRegistration(set = "DDM", collectorNumber = "69")
@CardRegistration(set = "DDP", collectorNumber = "54")
public class MarshCasualties extends Card {

    public MarshCasualties() {
        addStaticEffect(new KickerEffect("{3}"));
        addSpellEffect(new ConditionalReplacementEffect(new Kicked(),
                BoostEffect.creaturesTargetPlayerControls(-1, -1),
                BoostEffect.creaturesTargetPlayerControls(-2, -2)));
    }
}

package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "ZEN", collectorNumber = "119")
@CardRegistration(set = "FDN", collectorNumber = "192")
@CardRegistration(set = "MM2", collectorNumber = "109")
@CardRegistration(set = "SOA", collectorNumber = "41")
public class BurstLightning extends Card {

    public BurstLightning() {
        addStaticEffect(new KickerEffect("{4}"));
        addSpellEffect(new ConditionalReplacementEffect(new Kicked(),
                DealDamageEffect.toAnyTarget(2),
                DealDamageEffect.toAnyTarget(4)));
    }
}

package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TempleOfPower;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OjerAxonilDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;

@CardRegistration(set = "LCI", collectorNumber = "158")
public class OjerAxonil extends Card {

    public OjerAxonil() {
        setBackFaceCard(new TempleOfPower());
        addEffect(EffectSlot.STATIC, new OjerAxonilDamageReplacementEffect());
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceTransformedFromGraveyardEffect(true, true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "TempleOfPower";
    }
}

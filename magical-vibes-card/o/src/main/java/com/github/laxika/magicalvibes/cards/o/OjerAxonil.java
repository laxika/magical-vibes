package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TempleOfPower;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OjerAxonilDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "158")
@CardRegistration(set = "LCI", collectorNumber = "317")
public class OjerAxonil extends Card {

    public OjerAxonil() {
        setBackFaceCard(new TempleOfPower());
        addStaticEffect(new OjerAxonilDamageReplacementEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceTransformedFromGraveyardEffect(true, true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "TempleOfPower";
    }
}

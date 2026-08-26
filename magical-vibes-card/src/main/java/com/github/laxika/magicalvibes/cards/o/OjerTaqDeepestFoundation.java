package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TempleOfCivilization;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;

@CardRegistration(set = "LCI", collectorNumber = "26")
public class OjerTaqDeepestFoundation extends Card {

    public OjerTaqDeepestFoundation() {
        setBackFaceCard(new TempleOfCivilization());

        addEffect(EffectSlot.STATIC, new MultiplyTokenCreationEffect(3, true));
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceTransformedFromGraveyardEffect(true, true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "TempleOfCivilization";
    }
}

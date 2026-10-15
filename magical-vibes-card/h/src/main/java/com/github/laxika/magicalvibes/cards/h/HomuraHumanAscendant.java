package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;

@CardRegistration(set = "SOK", collectorNumber = "103")
public class HomuraHumanAscendant extends Card {

    public HomuraHumanAscendant() {
        setBackFaceCard(new HomurasEssence());

        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceTransformedFromGraveyardEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "HomurasEssence";
    }
}

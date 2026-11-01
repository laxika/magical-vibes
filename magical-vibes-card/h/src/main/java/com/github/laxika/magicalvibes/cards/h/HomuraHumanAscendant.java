package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "103")
public class HomuraHumanAscendant extends Card {

    public HomuraHumanAscendant() {
        setBackFaceCard(new HomurasEssence());

        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceTransformedFromGraveyardEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "HomurasEssence";
    }
}

package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ScroungedScythe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "265")
@CardRegistration(set = "SOI", collectorNumber = "256")
@CardRegistration(set = "SIR", collectorNumber = "252")
public class HarvestHand extends Card {

    public HarvestHand() {
        setBackFaceCard(new ScroungedScythe());

        // When this creature dies, return it to the battlefield transformed under your control.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceTransformedFromGraveyardEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ScroungedScythe";
    }
}

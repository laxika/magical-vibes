package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KenzoTheHardhearted;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "2")
public class BushiTenderfoot extends Card {

    public BushiTenderfoot() {
        setBackFaceCard(new KenzoTheHardhearted());
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), new TransformToBackFaceEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "KenzoTheHardhearted";
    }
}

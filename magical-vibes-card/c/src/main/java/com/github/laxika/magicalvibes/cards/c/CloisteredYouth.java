package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.u.UnholyFiend;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "8")
public class CloisteredYouth extends Card {

    public CloisteredYouth() {
        setBackFaceCard(new UnholyFiend());

        // At the beginning of your upkeep, you may transform Cloistered Youth.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new TransformSelfEffect(),
                "Transform Cloistered Youth?"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "UnholyFiend";
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TokTokVolcanoBorn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "153")
public class AkkiLavarunner extends Card {

    public AkkiLavarunner() {
        setBackFaceCard(new TokTokVolcanoBorn());

        // Includes combat and noncombat damage, but only to an opponent.
        addTrigger(Triggers.dealsDamageToOpponent(), new TransformToBackFaceEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "TokTokVolcanoBorn";
    }
}

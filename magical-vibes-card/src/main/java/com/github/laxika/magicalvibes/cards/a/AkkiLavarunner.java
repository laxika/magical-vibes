package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TokTokVolcanoBorn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;

@CardRegistration(set = "CHK", collectorNumber = "153")
public class AkkiLavarunner extends Card {

    public AkkiLavarunner() {
        setBackFaceCard(new TokTokVolcanoBorn());

        // Includes combat and noncombat damage, but only to an opponent.
        addEffect(EffectSlot.ON_DAMAGE_TO_OPPONENT, new TransformToBackFaceEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "TokTokVolcanoBorn";
    }
}

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

        // "Whenever this creature deals damage to an opponent, flip it." - ON_DAMAGE_TO_PLAYER covers
        // combat and noncombat damage alike.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new TransformToBackFaceEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "TokTokVolcanoBorn";
    }
}

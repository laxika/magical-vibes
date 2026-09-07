package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BoulderRush;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "ELD", collectorNumber = "137")
public class RimrockKnight extends Card {

    public RimrockKnight() {
        setBackFaceCard(new BoulderRush());
        addCastingOption(new AdventureCast("{R}"));
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "BoulderRush";
    }
}

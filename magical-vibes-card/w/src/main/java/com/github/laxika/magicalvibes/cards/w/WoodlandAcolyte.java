package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MendTheWilds;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "WOE", collectorNumber = "241")
public class WoodlandAcolyte extends Card {

    public WoodlandAcolyte() {
        setBackFaceCard(new MendTheWilds());
        addCastingOption(new AdventureCast("{G}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "MendTheWilds";
    }
}

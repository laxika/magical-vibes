package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "VOW", collectorNumber = "233")
public class BrineComber extends Card {

    public BrineComber() {
        setBackFaceCard(new BrineboundGift());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSpirit(1));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_AURA_SPELL, CreateTokenEffect.whiteSpirit(1));
        addCastingOption(new DisturbCast("{W}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BrineboundGift";
    }
}

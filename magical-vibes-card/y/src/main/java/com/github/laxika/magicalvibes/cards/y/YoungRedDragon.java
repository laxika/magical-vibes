package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BatheInGold;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "HBG", collectorNumber = "197")
public class YoungRedDragon extends Card {

    public YoungRedDragon() {
        setBackFaceCard(new BatheInGold());
        addCastingOption(new AdventureCast("{1}{R}"));
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "BatheInGold";
    }
}

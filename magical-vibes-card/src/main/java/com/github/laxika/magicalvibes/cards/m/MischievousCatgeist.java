package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CatlikeCuriosity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "VOW", collectorNumber = "69")
public class MischievousCatgeist extends Card {

    public MischievousCatgeist() {
        setBackFaceCard(new CatlikeCuriosity());

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1));
        addCastingOption(new DisturbCast("{2}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "CatlikeCuriosity";
    }
}

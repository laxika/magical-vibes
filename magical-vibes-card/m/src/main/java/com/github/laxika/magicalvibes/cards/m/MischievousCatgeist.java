package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CatlikeCuriosity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "69")
@CardRegistration(set = "DBL", collectorNumber = "336")
public class MischievousCatgeist extends Card {

    public MischievousCatgeist() {
        setBackFaceCard(new CatlikeCuriosity());

        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1));
        addCastingOption(new DisturbCast("{2}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "CatlikeCuriosity";
    }
}

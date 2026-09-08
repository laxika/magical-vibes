package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "26")
public class BringerOfTheBlueDawn extends Card {

    public BringerOfTheBlueDawn() {
        // You may pay {W}{U}{B}{R}{G} rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}{U}{B}{R}{G}"))));

        // At the beginning of your upkeep, you may draw two cards.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(new DrawCardEffect(2), "Draw two cards?"));
    }
}

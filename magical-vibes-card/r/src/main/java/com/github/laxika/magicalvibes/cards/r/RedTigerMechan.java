package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "154")
public class RedTigerMechan extends Card {

    public RedTigerMechan() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{R}"))));
    }
}

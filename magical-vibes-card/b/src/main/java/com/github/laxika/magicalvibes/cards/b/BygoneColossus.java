package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "235")
public class BygoneColossus extends Card {

    public BygoneColossus() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}"))));
    }
}

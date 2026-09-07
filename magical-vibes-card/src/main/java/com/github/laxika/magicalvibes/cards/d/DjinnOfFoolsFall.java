package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "43")
public class DjinnOfFoolsFall extends Card {

    public DjinnOfFoolsFall() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{U}"))));
    }
}

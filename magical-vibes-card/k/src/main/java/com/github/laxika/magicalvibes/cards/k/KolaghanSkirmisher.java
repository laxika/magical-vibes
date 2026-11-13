package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "107")
public class KolaghanSkirmisher extends Card {

    public KolaghanSkirmisher() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{B}"))));
    }
}

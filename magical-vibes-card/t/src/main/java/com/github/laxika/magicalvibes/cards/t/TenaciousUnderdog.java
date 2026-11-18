package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "97")
public class TenaciousUnderdog extends Card {

    public TenaciousUnderdog() {
        addCastingOption(AlternateHandCast.blitz(List.of(
                new ManaCastingCost("{2}{B}{B}"), new LifeCastingCost(2))));
        addCastingOption(new GraveyardCast(null, "{2}{B}{B}", List.of(new LifeCastingCost(2)), null));
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "121")
public class TimelineCuller extends Card {

    public TimelineCuller() {
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{B}"),
                new LifeCastingCost(2))));
        addCastingOption(new GraveyardCast(null, "{B}", List.of(new LifeCastingCost(2)), null));
    }
}

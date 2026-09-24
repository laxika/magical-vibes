package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "76")
@CardRegistration(set = "GNT", collectorNumber = "26")
@CardRegistration(set = "CMM", collectorNumber = "136")
public class ZahidDjinnOfTheLamp extends Card {

    public ZahidDjinnOfTheLamp() {
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{3}{U}"),
                new TapUntappedPermanentsCost(1, new PermanentIsArtifactPredicate())
        )));
    }
}

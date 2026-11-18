package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "57")
@CardRegistration(set = "FIC", collectorNumber = "154")
public class SabinMasterMonk extends Card {

    public SabinMasterMonk() {
        List<CastingCost> blitzCosts = List.of(
                new ManaCastingCost("{2}{R}{R}"), new DiscardCardCastingCost());
        addCastingOption(AlternateHandCast.blitz(blitzCosts));
        addCastingOption(new GraveyardCast(null, "{2}{R}{R}",
                List.of(new DiscardCardCastingCost()), null));
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MID", collectorNumber = "135")
@CardRegistration(set = "DBL", collectorNumber = "135")
@CardRegistration(set = "OTC", collectorNumber = "163")
public class ElectricRevelation extends Card {

    public ElectricRevelation() {
        addCastingCost(new DiscardCardTypeCost(null, null));
        addSpellEffect(new DrawCardEffect(2));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}

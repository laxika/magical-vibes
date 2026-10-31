package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "FIN", collectorNumber = "143")
@CardRegistration(set = "FIN", collectorNumber = "585")
public class LaughingMad extends Card {

    public LaughingMad() {
        addCastingCost(new DiscardCardTypeCost(null, null));
        addSpellEffect(new DrawCardEffect(2));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}

package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "RIX", collectorNumber = "109")
@CardRegistration(set = "2X2", collectorNumber = "120")
public class PiratesPillage extends Card {

    public PiratesPillage() {
        addCastingCost(new DiscardCardTypeCost(null, null));
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(CreateTokenEffect.ofTreasureToken(2));
    }
}

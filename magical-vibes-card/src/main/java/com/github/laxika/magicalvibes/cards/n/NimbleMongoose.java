package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "ODY", collectorNumber = "258")
public class NimbleMongoose extends Card {

    public NimbleMongoose() {
        // Threshold — This creature gets +2/+2 as long as there are seven or more cards in your graveyard.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new GraveyardCardThreshold(7, null),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}

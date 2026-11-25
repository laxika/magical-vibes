package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "WOE", collectorNumber = "128")
public class FlickACoin extends Card {

    public FlickACoin() {
        addSpellEffect(DealDamageEffect.toAnyTarget(1));
        addSpellEffect(CreateTokenEffect.ofTreasureToken(1));
        addSpellEffect(new DrawCardEffect(1));
    }
}

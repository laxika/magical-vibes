package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "ODY", collectorNumber = "138")
public class Frightcrawler extends Card {

    public Frightcrawler() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addStaticEffect(new ConditionalEffect(threshold, new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addStaticEffect(new CantBlockUnlessEffect(new NotCondition(threshold), "fewer than seven cards in your graveyard"));
    }
}

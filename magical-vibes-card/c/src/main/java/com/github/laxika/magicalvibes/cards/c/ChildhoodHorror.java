package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "ODY", collectorNumber = "123")
public class ChildhoodHorror extends Card {

    public ChildhoodHorror() {
        var threshold = new GraveyardCardThreshold(7, null);
        addStaticEffect(new ConditionalEffect(threshold,
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addStaticEffect(new CantBlockUnlessEffect(
                new NotCondition(threshold), "there are fewer than seven cards in your graveyard"));
    }
}

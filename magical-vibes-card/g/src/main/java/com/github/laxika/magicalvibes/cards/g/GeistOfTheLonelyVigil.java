package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "EMN", collectorNumber = "27")
public class GeistOfTheLonelyVigil extends Card {

    public GeistOfTheLonelyVigil() {
        // Delirium — This creature can attack as though it didn't have defender.
        addStaticEffect(new ConditionalEffect(
                new Delirium(), new CanAttackAsThoughNoDefenderEffect()));
    }
}

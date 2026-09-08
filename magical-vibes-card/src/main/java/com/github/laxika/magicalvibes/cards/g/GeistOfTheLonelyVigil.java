package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "EMN", collectorNumber = "27")
public class GeistOfTheLonelyVigil extends Card {

    public GeistOfTheLonelyVigil() {
        // Delirium — This creature can attack as though it didn't have defender.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(), new CanAttackAsThoughNoDefenderEffect()));
    }
}

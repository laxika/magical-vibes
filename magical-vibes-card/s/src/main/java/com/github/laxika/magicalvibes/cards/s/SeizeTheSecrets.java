package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CommittedCrimeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "OTJ", collectorNumber = "64")
public class SeizeTheSecrets extends Card {

    public SeizeTheSecrets() {
        addStaticEffect(new ConditionalEffect(
                new CommittedCrimeThisTurn(), new ReduceOwnCastCostEffect(new Fixed(1))));
        addSpellEffect(new DrawCardEffect(2));
    }
}

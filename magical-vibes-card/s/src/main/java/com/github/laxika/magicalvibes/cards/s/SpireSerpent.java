package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MBS", collectorNumber = "32")
@CardRegistration(set = "TD2", collectorNumber = "15")
public class SpireSerpent extends Card {

    public SpireSerpent() {
        addStaticEffect(new ConditionalEffect(new Metalcraft(), new StaticBoostEffect(2, 2, Subjects.source())));
        addStaticEffect(new ConditionalEffect(new Metalcraft(), new CanAttackAsThoughNoDefenderEffect()));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "218")
public class Scuttlegator extends Card {

    public Scuttlegator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{G/U}{G/U}",
                List.of(new AdaptEffect(3)),
                "{6}{G/U}{G/U}: Adapt 3."
        ));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE),
                new CanAttackAsThoughNoDefenderEffect()));
    }
}

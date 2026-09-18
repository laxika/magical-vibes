package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "93")
public class EvolvedSleeper extends Card {

    public EvolvedSleeper() {
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new BecomeCreatureTypeWithBasePowerToughnessEffect(2, 2, CardSubtype.CLERIC)),
                "{B}: This creature becomes a Human Cleric with base power and toughness 2/2."));

        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(
                new ConditionalEffect(new SourceHasSubtype(CardSubtype.CLERIC), SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.DEATHTOUCH),
                        new BecomeCreatureTypeWithBasePowerToughnessEffect(3, 3, CardSubtype.PHYREXIAN,
                                CardSubtype.CLERIC)))),
                "{1}{B}: If this creature is a Cleric, put a deathtouch counter on it and it becomes a Phyrexian Human Cleric with base power and toughness 3/3."));

        addActivatedAbility(new ActivatedAbility(false, "{1}{B}{B}", List.of(
                new ConditionalEffect(new SourceHasSubtype(CardSubtype.PHYREXIAN), SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1),
                        new LoseLifeEffect(1)))),
                "{1}{B}{B}: If this creature is a Phyrexian, put a +1/+1 counter on it, then you draw a card and you lose 1 life."));
    }
}

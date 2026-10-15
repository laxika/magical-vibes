package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureTypeWithBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "143")
public class WardenOfTheFirstTree extends Card {

    public WardenOfTheFirstTree() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W/B}",
                List.of(new BecomeCreatureTypeWithBasePowerToughnessEffect(3, 3, CardSubtype.WARRIOR,
                        null, true)),
                "{1}{W/B}: This creature becomes a Human Warrior with base power and toughness 3/3."));

        addActivatedAbility(new ActivatedAbility(false, "{2}{W/B}{W/B}",
                List.of(new ConditionalEffect(new SourceHasSubtype(CardSubtype.WARRIOR),
                        SequenceEffect.of(
                                new BecomeCreatureTypeWithBasePowerToughnessEffect(3, 3, CardSubtype.SPIRIT,
                                        CardSubtype.WARRIOR),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF, GrantDuration.INDEFINITE),
                                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF, GrantDuration.INDEFINITE)))),
                "{2}{W/B}{W/B}: If this creature is a Warrior, it becomes a Human Spirit Warrior with trample and lifelink."));

        addActivatedAbility(new ActivatedAbility(false, "{3}{W/B}{W/B}{W/B}",
                List.of(new ConditionalEffect(new SourceHasSubtype(CardSubtype.SPIRIT),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 5))),
                "{3}{W/B}{W/B}{W/B}: If this creature is a Spirit, put five +1/+1 counters on it."));
    }
}

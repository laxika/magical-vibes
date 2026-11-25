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
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
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
                                new BecomeCreatureTypeWithBasePowerToughnessEffect(null, null, CardSubtype.SPIRIT,
                                        CardSubtype.WARRIOR, false, false, null),
                                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.source(), Duration.PERMANENT),
                                new GrantKeywordEffect(Keyword.LIFELINK, Subjects.source(), Duration.PERMANENT)))),
                "{2}{W/B}{W/B}: If this creature is a Warrior, it becomes a Human Spirit Warrior with trample and lifelink."));

        addActivatedAbility(new ActivatedAbility(false, "{3}{W/B}{W/B}{W/B}",
                List.of(new ConditionalEffect(new SourceHasSubtype(CardSubtype.SPIRIT),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 5))),
                "{3}{W/B}{W/B}{W/B}: If this creature is a Spirit, put five +1/+1 counters on it."));
    }
}

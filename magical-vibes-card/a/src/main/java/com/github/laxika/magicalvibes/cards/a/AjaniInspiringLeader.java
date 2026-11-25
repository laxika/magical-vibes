package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "282")
public class AjaniInspiringLeader extends Card {

    public AjaniInspiringLeader() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new GainLifeEffect(2),
                        new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)
                ),
                "+2: You gain 2 life. Put two +1/+1 counters on up to one target creature.",
                TargetFilters.creature(),
                +2, null, null,
                List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(SequenceEffect.of(
                        new ExileTargetPermanentEffect(),
                        new GainLifeEffect(2, Subjects.controllerOf(Subjects.target())))),
                "\u22123: Exile target creature. Its controller gains 2 life.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.FLYING, Keyword.DOUBLE_STRIKE), Subjects.creaturesYouControl())),
                "\u221210: Creatures you control gain flying and double strike until end of turn."
        ));
    }
}

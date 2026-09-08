package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
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
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)
                ),
                "+2: You gain 2 life. Put two +1/+1 counters on up to one target creature.",
                TargetFilters.creature(),
                +2, null, null,
                List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new ExileTargetPermanentThenEffect(
                        new GainLifeEffect(2), ThenEffectRecipient.TARGET_CONTROLLER)),
                "\u22123: Exile target creature. Its controller gains 2 life.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.FLYING, Keyword.DOUBLE_STRIKE), GrantScope.OWN_CREATURES)),
                "\u221210: Creatures you control gain flying and double strike until end of turn."
        ));
    }
}

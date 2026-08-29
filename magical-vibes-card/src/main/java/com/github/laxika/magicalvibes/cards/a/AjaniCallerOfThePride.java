package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "1")
@CardRegistration(set = "M14", collectorNumber = "1")
@CardRegistration(set = "FDN", collectorNumber = "134")
public class AjaniCallerOfThePride extends Card {

    public AjaniCallerOfThePride() {
        // +1: Put a +1/+1 counter on up to one target creature.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "+1: Put a +1/+1 counter on up to one target creature.",
                TargetFilters.creature(),
                +1, null, null,
                List.of(), 0, 1
        ));

        // −3: Target creature gains flying and double strike until end of turn.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.DOUBLE_STRIKE), GrantScope.TARGET)),
                "−3: Target creature gains flying and double strike until end of turn.",
                TargetFilters.creature()
        ));

        // −8: Create X 2/2 white Cat creature tokens, where X is your life total.
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateTokenEffect(
                        new ControllerLifeTotal(), "Cat", 2, 2,
                        CardColor.WHITE, List.of(CardSubtype.CAT),
                        Set.of(), Set.of()
                )),
                "−8: Create X 2/2 white Cat creature tokens, where X is your life total."
        ));
    }
}

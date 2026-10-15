package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "171")
public class HerculesPrinceOfPower extends Card {

    public HerculesPrinceOfPower() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(
                                Set.of(Keyword.VIGILANCE, Keyword.INDESTRUCTIBLE, Keyword.HASTE),
                                GrantScope.SELF)
                ),
                "Power-up — {4}{G}: Put a +1/+1 counter on Hercules. He gains vigilance, indestructible, and haste until end of turn. "
                        + "(Activate each power-up ability only once. Reduce the cost by his mana cost if he entered this turn.)"
        ).withPowerUp());
    }
}

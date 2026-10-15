package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "592")
public class DonaldBlakeGuiseOfThor extends Card {

    public DonaldBlakeGuiseOfThor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}{W}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new PutCountersOnSelfEffect(CounterType.FLYING),
                        new GrantSubtypeEffect(CardSubtype.GOD, GrantScope.SELF, true),
                        new GrantSubtypeEffect(CardSubtype.WARRIOR, GrantScope.SELF),
                        new GrantSubtypeEffect(CardSubtype.HERO, GrantScope.SELF)
                ),
                "Power-up — {4}{W}{W}: Put two +1/+1 counters and a flying counter on Donald Blake. "
                        + "He becomes a God Warrior Hero. (He loses all other creature types. Activate each "
                        + "power-up ability only once. Reduce the cost by his mana cost if he entered this turn.)"
        ).withPowerUp());
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "185")
public class GwennaEyesOfGaea extends Card {

    public GwennaEyesOfGaea() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2, ManaSpendRestriction.CREATURE_SPELLS_OR_ABILITIES)),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast creature spells or activate abilities of creature sources."
        ));

        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtLeastPredicate(5)))),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}

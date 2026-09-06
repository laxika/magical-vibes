package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "205")
public class BaylenTheHaymaker extends Card {

    public BaylenTheHaymaker() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsTokenPredicate()),
                        new AwardAnyColorManaEffect()
                ),
                "Tap two untapped tokens you control: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(3, new PermanentIsTokenPredicate()),
                        new DrawCardEffect()
                ),
                "Tap three untapped tokens you control: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(4, new PermanentIsTokenPredicate()),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)
                ),
                "Tap four untapped tokens you control: Put three +1/+1 counters on Baylen. It gains trample until end of turn."
        ));
    }
}

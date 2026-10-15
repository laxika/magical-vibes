package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "262")
public class CrawlingBarrens extends Card {

    public CrawlingBarrens() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new MayEffect(
                                new AnimatePermanentsEffect(
                                        0, 0, List.of(CardSubtype.ELEMENTAL), Set.of(), null,
                                        Set.of(), GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN),
                                "Have Crawling Barrens become a 0/0 Elemental creature?"
                        )
                ),
                "{4}: Put two +1/+1 counters on this land. Then you may have it become a 0/0 "
                        + "Elemental creature until end of turn. It's still a land."
        ));
    }
}

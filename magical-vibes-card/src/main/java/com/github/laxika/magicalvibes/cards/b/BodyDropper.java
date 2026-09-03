package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "168")
public class BodyDropper extends Card {

    public BodyDropper() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{R}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
                ),
                "{B}{R}, Sacrifice another creature: This creature gains menace until end of turn."
        ));
    }
}

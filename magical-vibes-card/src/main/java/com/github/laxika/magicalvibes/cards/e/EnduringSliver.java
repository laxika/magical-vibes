package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "6")
public class EnduringSliver extends Card {

    public EnduringSliver() {
        ActivatedAbility outlastAbility = new ActivatedAbility(
                true,
                "{2}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{2}, {T}: Put a +1/+1 counter on this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        );

        addActivatedAbility(outlastAbility);
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                outlastAbility,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)
        ));
    }
}

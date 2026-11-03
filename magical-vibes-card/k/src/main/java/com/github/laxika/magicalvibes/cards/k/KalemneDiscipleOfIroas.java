package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C15", collectorNumber = "45")
@CardRegistration(set = "CM2", collectorNumber = "7")
public class KalemneDiscipleOfIroas extends Card {

    public KalemneDiscipleOfIroas() {
        CardAllOfPredicate qualifyingCreatureSpell = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMinManaValuePredicate(5, true)));

        addTrigger(Triggers.youCastSpell().withFilter(qualifyingCreatureSpell), new ExperienceCountersEffect(1));
        addStaticEffect(new DynamicStaticBoostEffect(
                new ControllerExperienceCounters(),
                new ControllerExperienceCounters(),
                GrantScope.SELF));
    }
}

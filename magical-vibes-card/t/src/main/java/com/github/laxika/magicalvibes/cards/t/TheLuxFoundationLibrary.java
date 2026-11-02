package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "588")
public class TheLuxFoundationLibrary extends Card {

    public TheLuxFoundationLibrary() {
        addStaticEffect(new PlayersHaveNoMaximumHandSizeEffect());
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(null,
                        new MayEffect(new DrawCardEffect(1), "Draw a card?")));
        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(),
                new PutCounterOnTargetPermanentEffect(CounterType.SHADOW));
    }
}

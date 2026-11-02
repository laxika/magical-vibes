package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "192")
public class HollowmurkSiege extends Card {

    public HollowmurkSiege() {
        addStaticEffect(new ChooseModeOnEnterEffect(List.of("Sultai", "Abzan")));

        addEffect(EffectSlot.ON_ALLY_COUNTER_PUT_ON_CREATURE, new ConditionalEffect(
                new SourceHasChosenMode("Sultai"),
                new OncePerTurnTriggerEffect(new DrawCardEffect(1))));

        target(TargetFilters.attackingCreature())
                .addTrigger(Triggers.youAttack(), new ConditionalEffect(
                        new SourceHasChosenMode("Abzan"),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)))
                .addTrigger(Triggers.youAttack(), new ConditionalEffect(
                        new SourceHasChosenMode("Abzan"),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET)));
    }
}

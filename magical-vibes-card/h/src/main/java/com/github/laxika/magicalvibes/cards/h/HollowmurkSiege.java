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

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "192")
public class HollowmurkSiege extends Card {

    public HollowmurkSiege() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of("Sultai", "Abzan")));

        addEffect(EffectSlot.ON_ALLY_COUNTER_PUT_ON_CREATURE, new ConditionalEffect(
                new SourceHasChosenMode("Sultai"),
                new OncePerTurnTriggerEffect(new DrawCardEffect(1))));

        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                        new SourceHasChosenMode("Abzan"),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                        new SourceHasChosenMode("Abzan"),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET)));
    }
}

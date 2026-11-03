package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "187")
public class FrostcliffSiege extends Card {

    public FrostcliffSiege() {
        addStaticEffect(new ChooseModeOnEnterEffect(List.of("Jeskai", "Temur")));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCreaturePredicate()),
                TriggerOptions.ONE_OR_MORE.withInterveningIf(new SourceHasChosenMode("Jeskai")),
                new DrawCardEffect(1));

        addStaticEffect(new ConditionalEffect(
                new SourceHasChosenMode("Temur"),
                new StaticBoostEffect(1, 0, Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.OWN_CREATURES)));
    }
}

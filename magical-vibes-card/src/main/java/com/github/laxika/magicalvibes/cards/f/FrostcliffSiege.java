package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "187")
public class FrostcliffSiege extends Card {

    public FrostcliffSiege() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of("Jeskai", "Temur")));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new ConditionalEffect(new SourceHasChosenMode("Jeskai"), new DrawCardEffect(1)),
                        false,
                        true));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceHasChosenMode("Temur"),
                new StaticBoostEffect(1, 0, Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.OWN_CREATURES)));
    }
}

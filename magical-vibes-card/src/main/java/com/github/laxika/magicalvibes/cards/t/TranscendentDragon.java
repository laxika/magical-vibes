package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileThenGrantFreeCastPermissionEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "TDC", collectorNumber = "22")
@CardRegistration(set = "TDC", collectorNumber = "62")
public class TranscendentDragon extends Card {

    public TranscendentDragon() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.CREATURE_SPELL,
                        StackEntryType.ENCHANTMENT_SPELL,
                        StackEntryType.SORCERY_SPELL,
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.ARTIFACT_SPELL,
                        StackEntryType.PLANESWALKER_SPELL,
                        StackEntryType.BATTLE_SPELL
                )),
                "Target must be a spell."
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new WasCast(),
                        new CounterSpellAndExileThenGrantFreeCastPermissionEffect()));
    }
}

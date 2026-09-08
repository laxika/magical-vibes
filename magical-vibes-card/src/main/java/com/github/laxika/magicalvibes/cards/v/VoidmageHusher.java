package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "92")
public class VoidmageHusher extends Card {

    public VoidmageHusher() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryHasTargetPredicate(),
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.ACTIVATED_ABILITY)))),
                "Target must be an activated ability."
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CounterSpellEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new MayEffect(new SpellCastTriggerEffect(null, List.of(ReturnToHandEffect.self())),
                        "Return Voidmage Husher to its owner's hand?"));
    }
}

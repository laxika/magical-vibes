package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "40")
public class CeremoniousRejection extends Card {

    public CeremoniousRejection() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryColorInPredicate(Set.of(
                        CardColor.WHITE,
                        CardColor.BLUE,
                        CardColor.BLACK,
                        CardColor.RED,
                        CardColor.GREEN
                ))),
                "Target spell must be colorless."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}

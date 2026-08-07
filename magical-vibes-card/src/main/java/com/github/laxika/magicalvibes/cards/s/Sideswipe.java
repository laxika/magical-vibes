package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;

import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "187")
public class Sideswipe extends Card {

    public Sideswipe() {
        // "You may change any targets of target Arcane spell."
        target(new StackEntryPredicateTargetFilter(
                new StackEntrySubtypeInPredicate(Set.of(CardSubtype.ARCANE)),
                "Target must be an Arcane spell."
        )).addEffect(EffectSlot.SPELL, new MayEffect(new ChooseNewTargetsForTargetSpellEffect(),
                "Change the targets of the spell?"));
    }
}

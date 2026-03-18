package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "155")
public class Reverberate extends Card {

    public Reverberate() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."
        )).addEffect(EffectSlot.SPELL, new CopySpellEffect());
    }
}

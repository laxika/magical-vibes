package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetSpellControllerUntilInstantOrSorceryAndCastEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "47")
public class Spellshift extends Card {

    public Spellshift() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."));
        addEffect(EffectSlot.SPELL,
                new RevealTopCardsOfTargetSpellControllerUntilInstantOrSorceryAndCastEffect());
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}

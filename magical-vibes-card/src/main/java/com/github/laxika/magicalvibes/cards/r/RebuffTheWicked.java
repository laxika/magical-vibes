package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;

@CardRegistration(set = "PLC", collectorNumber = "12")
public class RebuffTheWicked extends Card {

    public RebuffTheWicked() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTargetsYourPermanentPredicate(),
                "Target spell must target a permanent you control."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}

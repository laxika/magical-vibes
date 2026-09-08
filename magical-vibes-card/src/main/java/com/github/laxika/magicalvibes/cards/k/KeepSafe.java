package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;

@CardRegistration(set = "IKO", collectorNumber = "56")
public class KeepSafe extends Card {

    public KeepSafe() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTargetsYourPermanentPredicate(),
                "Target spell must target a permanent you control."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}

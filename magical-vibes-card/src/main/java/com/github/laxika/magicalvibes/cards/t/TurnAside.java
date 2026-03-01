package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;

@CardRegistration(set = "SOM", collectorNumber = "49")
public class TurnAside extends Card {

    public TurnAside() {
        setTargetFilter(new StackEntryPredicateTargetFilter(
                new StackEntryTargetsYourPermanentPredicate(),
                "Target spell must target a permanent you control."
        ));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}

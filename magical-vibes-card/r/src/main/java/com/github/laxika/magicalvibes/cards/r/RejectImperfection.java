package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;

@CardRegistration(set = "ONE", collectorNumber = "67")
public class RejectImperfection extends Card {

    public RejectImperfection() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetSpellMatches(new StackEntryMaxManaValuePredicate(3)),
                new ProliferateEffect()));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}

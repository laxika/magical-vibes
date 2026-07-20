package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "AKH", collectorNumber = "185")
public class ShedWeakness extends Card {

    public ShedWeakness() {
        // "Target creature gets +2/+2 until end of turn. You may remove a -1/-1 counter from it."
        // Both effects share the single spell target; the may-remove uses the spell's target id ("it").
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new RemoveCounterFromTargetPermanentEffect(
                        CounterType.MINUS_ONE_MINUS_ONE, new PermanentIsCreaturePredicate()),
                "Remove a -1/-1 counter from it?"));
    }
}

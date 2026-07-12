package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "9")
@CardRegistration(set = "8ED", collectorNumber = "9")
public class Chastise extends Card {

    public Chastise() {
        // Gain life first so the target's power is read before it is destroyed.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsAttackingPredicate(),
                "Target must be an attacking creature"
        ))
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(new TargetPower()))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(false));
    }
}

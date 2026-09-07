package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "PLC", collectorNumber = "72")
public class ImpsMischief extends Card {

    public ImpsMischief() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryIsSingleTargetPredicate(),
                "Target spell must have a single target."
        ))
                .addEffect(EffectSlot.SPELL, new ChangeTargetOfTargetSpellWithSingleTargetEffect())
                .addEffect(EffectSlot.SPELL,
                        new LoseLifeEffect(new TargetSpellManaValue(), LoseLifeRecipient.CONTROLLER));
    }
}

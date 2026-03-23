package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "XLN", collectorNumber = "287")
public class HuatlisSpurring extends Card {

    public HuatlisSpurring() {
        // Target creature gets +2/+0 until end of turn.
        // If you control a Huatli planeswalker, that creature gets +4/+0 until end of turn instead.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new ControlsSubtypeReplacementEffect(
                CardSubtype.HUATLI,
                new BoostTargetCreatureEffect(2, 0),
                new BoostTargetCreatureEffect(4, 0)
        ));
    }
}

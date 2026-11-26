package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "XLN", collectorNumber = "287")
public class HuatlisSpurring extends Card {

    public HuatlisSpurring() {
        // Target creature gets +2/+0 until end of turn.
        // If you control a Huatli planeswalker, that creature gets +4/+0 until end of turn instead.
        target(TargetFilters.creature()).addSpellEffect(new ConditionalReplacementEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.HUATLI)), BoostEffect.target(2, 0), BoostEffect.target(4, 0)));
    }
}

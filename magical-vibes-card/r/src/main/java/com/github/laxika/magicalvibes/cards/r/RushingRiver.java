package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "PLS", collectorNumber = "30")
public class RushingRiver extends Card {

    public RushingRiver() {
        addEffect(EffectSlot.STATIC, new KickerEffect(new PermanentIsLandPredicate(), "a land"));
        targetWhenKicked(
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        "Target must be a nonland permanent"),
                1, 1, 2, 2
        ).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}

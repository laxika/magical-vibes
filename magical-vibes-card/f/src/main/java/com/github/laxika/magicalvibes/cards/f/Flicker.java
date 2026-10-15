package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "UDS", collectorNumber = "9")
public class Flicker extends Card {

    public Flicker() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                "Target must be a nontoken permanent"
        )).addEffect(EffectSlot.SPELL, FlickerEffect.flickerTarget());
    }
}

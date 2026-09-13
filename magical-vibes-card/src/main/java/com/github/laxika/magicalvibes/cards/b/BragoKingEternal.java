package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "VMA", collectorNumber = "246")
public class BragoKingEternal extends Card {

    public BragoKingEternal() {
        target(new ControlledPermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        "Target must be a nonland permanent you control"), 0, 99)
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, FlickerEffect.flickerTarget());
    }
}

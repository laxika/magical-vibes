package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.TrackedLandsBecomeBasicLandTypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "24")
public class GracefulAntelope extends Card {

    public GracefulAntelope() {
        target(TargetFilters.land()).addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect(CardSubtype.PLAINS),
                        "Have target land become a Plains?"
                ));
        addEffect(EffectSlot.STATIC, new TrackedLandsBecomeBasicLandTypeEffect(CardSubtype.PLAINS));
    }
}

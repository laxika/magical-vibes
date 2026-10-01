package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "16")
@CardRegistration(set = "WHO", collectorNumber = "336")
@CardRegistration(set = "WHO", collectorNumber = "621")
@CardRegistration(set = "WHO", collectorNumber = "927")
public class CrackInTime extends Card {

    public CrackInTime() {
        ExileTargetPermanentUntilSourceLeavesEffect exileEffect =
                new ExileTargetPermanentUntilSourceLeavesEffect();
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, exileEffect)
                .addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, exileEffect);
    }
}

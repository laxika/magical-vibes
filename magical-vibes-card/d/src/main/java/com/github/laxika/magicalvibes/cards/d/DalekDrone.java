package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "64")
@CardRegistration(set = "WHO", collectorNumber = "669")
@CardRegistration(set = "WHO", collectorNumber = "371")
@CardRegistration(set = "WHO", collectorNumber = "962")
public class DalekDrone extends Card {

    public DalekDrone() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}

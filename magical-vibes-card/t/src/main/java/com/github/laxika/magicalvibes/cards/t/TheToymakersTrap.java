package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ToymakersTrapEffect;

@CardRegistration(set = "WHO", collectorNumber = "72")
@CardRegistration(set = "WHO", collectorNumber = "375")
@CardRegistration(set = "WHO", collectorNumber = "677")
@CardRegistration(set = "WHO", collectorNumber = "966")
public class TheToymakersTrap extends Card {

    public TheToymakersTrap() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ToymakersTrapEffect());
    }
}

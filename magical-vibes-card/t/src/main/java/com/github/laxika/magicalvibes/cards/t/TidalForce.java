package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;

@CardRegistration(set = "C13", collectorNumber = "62")
public class TidalForce extends Card {

    public TidalForce() {
        // At the beginning of each upkeep, you may tap or untap target permanent.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MayEffect(
                new TapOrUntapTargetPermanentEffect(),
                "Tap or untap target permanent?"));
    }
}

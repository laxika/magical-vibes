package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;

@CardRegistration(set = "CHK", collectorNumber = "212")
public class HeartbeatOfSpring extends Card {

    public HeartbeatOfSpring() {
        // Symmetric: any player who taps a land for mana adds one extra mana of a type it produced.
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddOneOfEachManaTypeProducedByLandEffect(false));
    }
}

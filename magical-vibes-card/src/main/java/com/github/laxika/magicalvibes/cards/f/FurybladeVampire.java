package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "INR", collectorNumber = "155")
public class FurybladeVampire extends Card {

    public FurybladeVampire() {
        // At the beginning of combat on your turn, you may discard a card. If you do, this creature
        // gets +3/+0 until end of turn.
        // (Trample is loaded from Scryfall keywords.)
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                new DiscardCardAndBoostSelfEffect(3, 0),
                "Discard a card to give this creature +3/+0 until end of turn?"
        ));
    }
}

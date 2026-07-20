package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AKH", collectorNumber = "90")
public class FaithOfTheDevoted extends Card {

    public FaithOfTheDevoted() {
        // Whenever you cycle or discard a card, you may pay {1}. If you do, each opponent loses 2 life
        // and you gain 2 life. Cycling is a discard (CR 702.29e), so the single "controller discards"
        // trigger covers both wordings. The gain is a fixed 2 (multiplayer: you gain 2 total no matter
        // how many opponents lose life), so a separate GainLifeEffect(2) rather than the drain flag.
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new MayPayManaEffect("{1}",
                        SequenceEffect.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(2)),
                        "Pay {1} so each opponent loses 2 life and you gain 2 life?"));
    }
}

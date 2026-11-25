package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "90")
@CardRegistration(set = "HA4", collectorNumber = "10")
@CardRegistration(set = "C19", collectorNumber = "112")
public class FaithOfTheDevoted extends Card {

    public FaithOfTheDevoted() {
        // Whenever you cycle or discard a card, you may pay {1}. If you do, each opponent loses 2 life
        // and you gain 2 life. Cycling is a discard (CR 702.29e), so the single "controller discards"
        // trigger covers both wordings. The gain is a fixed 2 (multiplayer: you gain 2 total no matter
        // how many opponents lose life), so a separate GainLifeEffect(2) rather than the drain flag.
        addTrigger(Triggers.youDiscardCard(),
                UnlessPaysEffect.youMayPay("{1}", SequenceEffect.of(
                                new LoseLifeEffect(2, Subjects.eachOpponent()),
                                new GainLifeEffect(2))).withPrompt("Pay {1} so each opponent loses 2 life and you gain 2 life?"));
    }
}

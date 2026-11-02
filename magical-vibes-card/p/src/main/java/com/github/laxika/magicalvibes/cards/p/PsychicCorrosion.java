package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Psychic Corrosion — Enchantment.
 * Whenever you draw a card, each opponent mills two cards.
 */
@CardRegistration(set = "M19", collectorNumber = "68")
@CardRegistration(set = "SLD", collectorNumber = "1669")
public class PsychicCorrosion extends Card {

    public PsychicCorrosion() {
        // Whenever you draw a card, each opponent mills two cards.
        addTrigger(Triggers.youDrawCard(), new MillEffect(2, MillRecipient.EACH_OPPONENT));
    }
}

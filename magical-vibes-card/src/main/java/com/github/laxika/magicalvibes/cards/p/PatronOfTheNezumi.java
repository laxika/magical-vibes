package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

/**
 * Rat offering is not modelled — the engine has no alternative-cost casting for the Offering
 * keyword, so the card can only be cast for its printed mana cost.
 */
@CardRegistration(set = "BOK", collectorNumber = "77")
public class PatronOfTheNezumi extends Card {

    public PatronOfTheNezumi() {
        // Whenever a permanent is put into an opponent's graveyard, that player loses 1 life.
        // The slot bakes the graveyard's owner as the entry's target, so TARGET_PLAYER is "that player".
        addEffect(EffectSlot.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PlayedCardNameMatchesCardExiledWithSourceTriggerEffect;

/**
 * Search the City — {4}{U} enchantment.
 *
 * <p>"When this enchantment enters, exile the top five cards of your library. Whenever you play a
 * card with the same name as one of the exiled cards, you may put one of those cards with that name
 * into its owner's hand. Then if there are no cards exiled with this enchantment, sacrifice it. If
 * you do, take an extra turn after this one."
 *
 * <p>"Play a card" is both halves of playing, so the name-match trigger sits on the spell-cast slot
 * and on the land-play slot. The collector builds the whole ability (optional return, then the
 * intervening-if sacrifice + extra turn).
 */
@CardRegistration(set = "RTR", collectorNumber = "49")
public class SearchTheCity extends Card {

    public SearchTheCity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardsToSourceEffect(5, false));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PlayedCardNameMatchesCardExiledWithSourceTriggerEffect());
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new PlayedCardNameMatchesCardExiledWithSourceTriggerEffect());
    }
}

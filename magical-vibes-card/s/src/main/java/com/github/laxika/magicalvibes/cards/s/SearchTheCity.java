package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PlayedCardNameMatchesCardExiledWithSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardsToSourceEffect(5, false));
        addTrigger(Triggers.youCastSpell(),
                new PlayedCardNameMatchesCardExiledWithSourceTriggerEffect());
        addTrigger(Triggers.youPlayLand(),
                new PlayedCardNameMatchesCardExiledWithSourceTriggerEffect());
    }
}

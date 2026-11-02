package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * The Mending of Dominaria — {3}{G}{G} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I, II — Mill two cards, then you may return a creature card from your graveyard to your hand.
 * III — Return all land cards from your graveyard to the battlefield, then shuffle your
 *        graveyard into your library.
 */
@CardRegistration(set = "DOM", collectorNumber = "173")
@CardRegistration(set = "OTC", collectorNumber = "197")
@CardRegistration(set = "ZNC", collectorNumber = "74")
public class TheMendingOfDominaria extends Card {

    public TheMendingOfDominaria() {
        // Chapter I: Mill two cards, then you may return a creature card from your graveyard to your hand
        addTrigger(Triggers.chapter(1), new MillEffect(2, MillRecipient.CONTROLLER));
        addTrigger(Triggers.chapter(1), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .build());

        // Chapter II: Same as chapter I
        addTrigger(Triggers.chapter(2), new MillEffect(2, MillRecipient.CONTROLLER));
        addTrigger(Triggers.chapter(2), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .build());

        // Chapter III: Return all land cards from your graveyard to the battlefield,
        // then shuffle your graveyard into your library
        addTrigger(Triggers.chapter(3), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.LAND))
                .returnAll(true)
                .build());
        addTrigger(Triggers.chapter(3), new ShuffleGraveyardIntoLibraryEffect(false));
    }
}

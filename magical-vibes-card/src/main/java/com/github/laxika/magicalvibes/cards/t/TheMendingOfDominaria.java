package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * The Mending of Dominaria — {3}{G}{G} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I, II — Mill two cards, then you may return a creature card from your graveyard to your hand.
 * III — Return all land cards from your graveyard to the battlefield, then shuffle your
 *        graveyard into your library.
 */
@CardRegistration(set = "DOM", collectorNumber = "173")
public class TheMendingOfDominaria extends Card {

    public TheMendingOfDominaria() {
        // Chapter I: Mill two cards, then you may return a creature card from your graveyard to your hand
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MillControllerEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_I, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .build());

        // Chapter II: Same as chapter I
        addEffect(EffectSlot.SAGA_CHAPTER_II, new MillControllerEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_II, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .build());

        // Chapter III: Return all land cards from your graveyard to the battlefield,
        // then shuffle your graveyard into your library
        addEffect(EffectSlot.SAGA_CHAPTER_III, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.LAND))
                .returnAll(true)
                .build());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ShuffleGraveyardIntoLibraryEffect());
    }
}

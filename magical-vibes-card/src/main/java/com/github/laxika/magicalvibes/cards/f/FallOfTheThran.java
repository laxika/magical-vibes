package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

/**
 * Fall of the Thran — {5}{W} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I — Destroy all lands.
 * II, III — Each player returns up to two land cards from their graveyard to the battlefield.
 */
@CardRegistration(set = "DOM", collectorNumber = "18")
public class FallOfTheThran extends Card {

    public FallOfTheThran() {
        // Chapter I: Destroy all lands
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DestroyAllPermanentsEffect(new PermanentIsLandPredicate()));

        // Chapter II: Each player returns up to two land cards from their graveyard to the battlefield
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(2, new CardTypePredicate(CardType.LAND)));

        // Chapter III: Same as chapter II
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(2, new CardTypePredicate(CardType.LAND)));
    }
}

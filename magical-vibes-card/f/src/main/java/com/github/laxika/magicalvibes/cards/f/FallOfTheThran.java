package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Fall of the Thran — {5}{W} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I — Destroy all lands.
 * II, III — Each player returns two land cards from their graveyard to the battlefield.
 */
@CardRegistration(set = "DOM", collectorNumber = "18")
public class FallOfTheThran extends Card {

    public FallOfTheThran() {
        // Chapter I: Destroy all lands
        addTrigger(Triggers.chapter(1), new DestroyAllPermanentsEffect(new PermanentIsLandPredicate()));

        // Chapter II: Each player returns two land cards from their graveyard to the battlefield
        addTrigger(Triggers.chapter(2),
                EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.mandatory(2, new CardTypePredicate(CardType.LAND)));

        // Chapter III: Same as chapter II
        addTrigger(Triggers.chapter(3),
                EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.mandatory(2, new CardTypePredicate(CardType.LAND)));
    }
}

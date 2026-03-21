package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

/**
 * The Eldest Reborn — {4}{B} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I — Each opponent sacrifices a creature or planeswalker.
 * II — Each opponent discards a card.
 * III — Put target creature or planeswalker card from a graveyard onto the battlefield
 *       under your control.
 */
@CardRegistration(set = "DOM", collectorNumber = "90")
public class TheEldestReborn extends Card {

    public TheEldestReborn() {
        // Chapter I: Each opponent sacrifices a creature or planeswalker
        addEffect(EffectSlot.SAGA_CHAPTER_I, new EachOpponentSacrificesPermanentsEffect(
                1, new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                ))
        ));

        // Chapter II: Each opponent discards a card
        addEffect(EffectSlot.SAGA_CHAPTER_II, new EachOpponentDiscardsEffect());

        // Chapter III: Put target creature or planeswalker card from a graveyard
        // onto the battlefield under your control
        addEffect(EffectSlot.SAGA_CHAPTER_III, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.PLANESWALKER)
                )))
                .build());
    }
}

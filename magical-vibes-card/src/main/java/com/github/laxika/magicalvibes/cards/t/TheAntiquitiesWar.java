package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/**
 * The Antiquities War — {3}{U} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I, II — Look at the top five cards of your library. You may reveal an artifact card
 *          from among them and put it into your hand. Put the rest on the bottom of your
 *          library in a random order.
 * III — Artifacts you control become artifact creatures with base power and toughness
 *       5/5 until end of turn.
 */
@CardRegistration(set = "DOM", collectorNumber = "42")
public class TheAntiquitiesWar extends Card {

    public TheAntiquitiesWar() {
        // Chapter I: Look at top 5 cards, may reveal an artifact to hand, rest on bottom
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(
                        5, new CardTypePredicate(CardType.ARTIFACT)));

        // Chapter II: Same as chapter I
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(
                        5, new CardTypePredicate(CardType.ARTIFACT)));

        // Chapter III: Artifacts you control become artifact creatures with base P/T 5/5 until end of turn
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new AnimateControlledPermanentsEffect(5, 5, new PermanentIsArtifactPredicate()));
    }
}

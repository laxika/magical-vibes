package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostColorSourceDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

/**
 * The Flame of Keld — {1}{R} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I — Discard your hand.
 * II — Draw two cards.
 * III — If a red source you control would deal damage to a permanent or player this turn,
 *        it deals that much damage plus 2 to that permanent or player instead.
 */
@CardRegistration(set = "DOM", collectorNumber = "123")
public class TheFlameOfKeld extends Card {

    public TheFlameOfKeld() {
        // Chapter I: Discard your hand
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DiscardOwnHandEffect());

        // Chapter II: Draw two cards
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DrawCardEffect(2));

        // Chapter III: Red sources you control deal +2 damage this turn
        addEffect(EffectSlot.SAGA_CHAPTER_III, new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 2));
    }
}

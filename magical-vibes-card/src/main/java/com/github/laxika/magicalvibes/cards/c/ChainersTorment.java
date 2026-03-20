package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenFromHalfLifeTotalAndDealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

/**
 * Chainer's Torment — {3}{B} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I, II — Chainer's Torment deals 2 damage to each opponent and you gain 2 life.
 * III — Create an X/X black Nightmare Horror creature token, where X is half your life
 *       total, rounded up. It deals X damage to you.
 */
@CardRegistration(set = "DOM", collectorNumber = "82")
public class ChainersTorment extends Card {

    public ChainersTorment() {
        // Chapter I: deal 2 damage to each opponent, gain 2 life
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToEachOpponentEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new GainLifeEffect(2));

        // Chapter II: same as chapter I
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DealDamageToEachOpponentEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(2));

        // Chapter III: create X/X black Nightmare Horror token, it deals X damage to you
        addEffect(EffectSlot.SAGA_CHAPTER_III, new CreateTokenFromHalfLifeTotalAndDealDamageEffect(
                "Nightmare Horror",
                CardColor.BLACK,
                List.of(CardSubtype.NIGHTMARE, CardSubtype.HORROR)
        ));
    }
}

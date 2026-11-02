package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenFromHalfLifeTotalAndDealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.chapter(1), new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(1), new GainLifeEffect(2));

        // Chapter II: same as chapter I
        addTrigger(Triggers.chapter(2), new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(2), new GainLifeEffect(2));

        // Chapter III: create X/X black Nightmare Horror token, it deals X damage to you
        addTrigger(Triggers.chapter(3), new CreateTokenFromHalfLifeTotalAndDealDamageEffect(
                "Nightmare Horror",
                CardColor.BLACK,
                List.of(CardSubtype.NIGHTMARE, CardSubtype.HORROR)
        ));
    }
}

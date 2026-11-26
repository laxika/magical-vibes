package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DeterminedOnce;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

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
        addTrigger(Triggers.chapter(1),
                DealDamageEffect.to(Subjects.eachOpponent(), 2),
                new GainLifeEffect(2));

        // Chapter II: same as chapter I
        addTrigger(Triggers.chapter(2),
                DealDamageEffect.to(Subjects.eachOpponent(), 2),
                new GainLifeEffect(2));

        // Chapter III: create X/X black Nightmare Horror token, it deals X damage to you
        var halfYourLife = new DeterminedOnce(new HalfControllerLifeRoundedUp());
        addTrigger(Triggers.chapter(3),
                new CreateTokenEffect("Nightmare Horror", halfYourLife, halfYourLife, CardColor.BLACK,
                        List.of(CardSubtype.NIGHTMARE, CardSubtype.HORROR), Set.of(), Set.of()),
                DealDamageEffect.to(Subjects.you(), halfYourLife).dealtBy(Subjects.createdThisResolution()));
    }
}

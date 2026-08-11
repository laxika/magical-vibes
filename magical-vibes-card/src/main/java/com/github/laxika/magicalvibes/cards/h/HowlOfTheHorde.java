package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;

@CardRegistration(set = "KTK", collectorNumber = "112")
public class HowlOfTheHorde extends Card {

    public HowlOfTheHorde() {
        addEffect(EffectSlot.SPELL, new CopyNextInstantOrSorceryCastThisTurnEffect());
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new Raid(), new CopyNextInstantOrSorceryCastThisTurnEffect()));
    }
}

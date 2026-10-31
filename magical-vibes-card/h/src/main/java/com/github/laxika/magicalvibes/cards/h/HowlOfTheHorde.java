package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;

@CardRegistration(set = "KTK", collectorNumber = "112")
public class HowlOfTheHorde extends Card {

    public HowlOfTheHorde() {
        addSpellEffect(new CopyNextInstantOrSorceryCastThisTurnEffect());
        addSpellEffect(new ConditionalEffect(new Raid(), new CopyNextInstantOrSorceryCastThisTurnEffect()));
    }
}

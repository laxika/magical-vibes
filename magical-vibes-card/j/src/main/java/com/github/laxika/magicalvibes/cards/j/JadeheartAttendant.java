package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

public class JadeheartAttendant extends Card {

    public JadeheartAttendant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GainLifeEffect(new TotalManaValueOfCardsExiledWithSource()));
    }
}

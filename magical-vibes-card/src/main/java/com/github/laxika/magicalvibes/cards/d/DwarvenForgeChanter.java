package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;

@CardRegistration(set = "BRO", collectorNumber = "131")
public class DwarvenForgeChanter extends Card {

    public DwarvenForgeChanter() {
        // Ward—Pay 2 life. (Whenever this creature becomes the target of a spell or ability an
        // opponent controls, counter it unless that player pays 2 life.)
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysLifeEffect(new Fixed(2)));

    }
}

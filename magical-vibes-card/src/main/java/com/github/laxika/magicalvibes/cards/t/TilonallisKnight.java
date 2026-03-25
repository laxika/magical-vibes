package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;

@CardRegistration(set = "XLN", collectorNumber = "169")
public class TilonallisKnight extends Card {

    public TilonallisKnight() {
        // Whenever this creature attacks, if you control a Dinosaur,
        // this creature gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ATTACK,
                new ControlsSubtypeConditionalEffect(CardSubtype.DINOSAUR,
                        new BoostSelfEffect(1, 1)));
    }
}

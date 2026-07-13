package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect;

@CardRegistration(set = "8ED", collectorNumber = "206")
@CardRegistration(set = "7ED", collectorNumber = "204")
public class Okk extends Card {

    public Okk() {
        // Can't attack unless a creature with greater power also attacks;
        // can't block unless a creature with greater power also blocks.
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect());
    }
}

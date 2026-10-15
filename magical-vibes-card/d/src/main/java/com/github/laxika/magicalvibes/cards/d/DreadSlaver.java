package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingDamagedCreatureUnderControlEffect;

@CardRegistration(set = "AVR", collectorNumber = "98")
public class DreadSlaver extends Card {

    public DreadSlaver() {
        // "Whenever a creature dealt damage by this creature this turn dies, return it to the
        // battlefield under your control. That creature is a black Zombie in addition to its
        // other colors and types."
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES,
                new ReturnDyingDamagedCreatureUnderControlEffect(CardColor.BLACK, CardSubtype.ZOMBIE));
    }
}

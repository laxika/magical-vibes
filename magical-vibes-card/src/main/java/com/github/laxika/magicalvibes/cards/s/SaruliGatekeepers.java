package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DGM", collectorNumber = "48")
public class SaruliGatekeepers extends Card {

    public SaruliGatekeepers() {
        // When this creature enters, if you control two or more Gates, you gain 7 life.
        // Intervening-if gate (CR 603.4): checked as the trigger goes on the stack and again at resolution.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.GATE)),
                new GainLifeEffect(7)));
    }
}

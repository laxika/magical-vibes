package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;

@CardRegistration(set = "YNEO", collectorNumber = "1")
public class DragonflyPilot extends Card {

    public DragonflyPilot() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardNamedIntoHandEffect("Dragonfly Suit", false));
        addEffect(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2, true));
    }
}

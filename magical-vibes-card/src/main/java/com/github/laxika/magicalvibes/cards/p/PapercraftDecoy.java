package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "NEO", collectorNumber = "253")
public class PapercraftDecoy extends Card {

    public PapercraftDecoy() {
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay {2} to draw a card?"));
    }
}

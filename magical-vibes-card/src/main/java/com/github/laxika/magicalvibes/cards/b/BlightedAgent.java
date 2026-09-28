package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "29")
@CardRegistration(set = "SLD", collectorNumber = "1203")
public class BlightedAgent extends Card {

    public BlightedAgent() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}

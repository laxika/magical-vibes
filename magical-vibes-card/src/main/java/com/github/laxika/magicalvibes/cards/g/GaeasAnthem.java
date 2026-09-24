package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "PLC", collectorNumber = "147")
@CardRegistration(set = "MM3", collectorNumber = "127")
@CardRegistration(set = "TSR", collectorNumber = "204")
public class GaeasAnthem extends Card {

    public GaeasAnthem() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));
    }
}

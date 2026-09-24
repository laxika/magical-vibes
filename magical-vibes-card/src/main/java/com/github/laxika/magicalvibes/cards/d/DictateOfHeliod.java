package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "JOU", collectorNumber = "8")
@CardRegistration(set = "DDO", collectorNumber = "8")
@CardRegistration(set = "PIO", collectorNumber = "11")
@CardRegistration(set = "C15", collectorNumber = "68")
public class DictateOfHeliod extends Card {

    public DictateOfHeliod() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES));
    }
}

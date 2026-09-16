package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "9")
@CardRegistration(set = "MM2", collectorNumber = "16")
@CardRegistration(set = "IMA", collectorNumber = "18")
@CardRegistration(set = "HA5", collectorNumber = "2")
@CardRegistration(set = "MUL", collectorNumber = "3")
@CardRegistration(set = "MUL", collectorNumber = "68")
@CardRegistration(set = "MUL", collectorNumber = "133")
public class EleshNornGrandCenobite extends Card {

    public EleshNornGrandCenobite() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, -2, GrantScope.OPPONENT_CREATURES));
    }
}

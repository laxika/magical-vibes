package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllDamageDealtWithWitherEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "186")
@CardRegistration(set = "ECC", collectorNumber = "121")
@CardRegistration(set = "C16", collectorNumber = "233")
public class EverlastingTorment extends Card {

    public EverlastingTorment() {
        addStaticEffect(new PlayersCantGainLifeEffect());
        addStaticEffect(new DamageCantBePreventedEffect());
        addStaticEffect(new AllDamageDealtWithWitherEffect());
    }
}

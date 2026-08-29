package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "203")
@CardRegistration(set = "BRB", collectorNumber = "97")
@CardRegistration(set = "TSB", collectorNumber = "72")
public class WildfireEmissary extends Card {

    public WildfireEmissary() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));

        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new BoostSelfEffect(1, 0)),
                "{1}{R}: Wildfire Emissary gets +1/+0 until end of turn."));
    }
}

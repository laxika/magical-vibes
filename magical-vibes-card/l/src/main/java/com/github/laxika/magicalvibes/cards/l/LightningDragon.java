package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "202")
@CardRegistration(set = "VMA", collectorNumber = "177")
public class LightningDragon extends Card {

    public LightningDragon() {
        addStaticEffect(new EchoEffect("{2}{R}{R}"));
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));
    }
}

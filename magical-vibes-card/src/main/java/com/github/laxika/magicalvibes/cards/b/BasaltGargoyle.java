package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "145")
public class BasaltGargoyle extends Card {

    public BasaltGargoyle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{2}{R}"));
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(0, 1)),
                "{R}: This creature gets +0/+1 until end of turn."));
    }
}

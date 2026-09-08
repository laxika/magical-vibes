package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "117")
public class HenchfiendOfUkor extends Card {

    public HenchfiendOfUkor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{1}{B}"));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B/R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{B/R}: Henchfiend of Ukor gets +1/+0 until end of turn."
        ));
    }
}

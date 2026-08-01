package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "206")
public class TrostaniSelesnyasVoice extends Card {

    public TrostaniSelesnyasVoice() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEqualToToughnessEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}{W}",
                List.of(new PopulateEffect()),
                "{1}{G}{W}, {T}: Populate."
        ));
    }
}

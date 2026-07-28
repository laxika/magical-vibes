package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "339")
public class SoldeviSimulacrum extends Card {

    public SoldeviSimulacrum() {
        // Cumulative upkeep {1}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new BoostSelfEffect(1, 0)), "{1}: This creature gets +1/+0 until end of turn."));
    }
}

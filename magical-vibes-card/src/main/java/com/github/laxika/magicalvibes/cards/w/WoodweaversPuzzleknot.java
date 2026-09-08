package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "240")
public class WoodweaversPuzzleknot extends Card {

    public WoodweaversPuzzleknot() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(3));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3), new EnergyCountersEffect(3)),
                "{2}{G}, Sacrifice this artifact: You gain 3 life and get {E}{E}{E}."
        ));
    }
}

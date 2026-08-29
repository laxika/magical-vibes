package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "217")
public class GlassblowersPuzzleknot extends Card {

    public GlassblowersPuzzleknot() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new SacrificeSelfCost(), new ScryEffect(2), new EnergyCountersEffect(2)),
                "{2}{U}, Sacrifice this artifact: Scry 2, then you get {E}{E}."
        ));
    }
}

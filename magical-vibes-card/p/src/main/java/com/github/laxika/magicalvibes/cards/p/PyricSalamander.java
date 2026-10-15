package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "187")
public class PyricSalamander extends Card {

    public PyricSalamander() {
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new BoostSelfEffect(1, 0), new SacrificeSelfAtEndStepEffect()),
                "{R}: Pyric Salamander gets +1/+0 until end of turn. Sacrifice Pyric Salamander at the beginning of the next end step."));
    }
}

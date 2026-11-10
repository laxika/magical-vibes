package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedChooseOpponentGainsControlOfSourceEffect;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "99")
@CardRegistration(set = "FEM", collectorNumber = "142")
@CardRegistration(set = "ME1", collectorNumber = "179")
public class RainbowVale extends Card {

    public RainbowVale() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(),
                        new RegisterDelayedChooseOpponentGainsControlOfSourceEffect()),
                "{T}: Add one mana of any color. An opponent gains control of this land at the beginning of the "
                        + "next end step."));
    }
}

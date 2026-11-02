package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FEM", collectorNumber = "99")
@CardRegistration(set = "FEM", collectorNumber = "142")
@CardRegistration(set = "ME1", collectorNumber = "179")
public class RainbowVale extends Card {

    public RainbowVale() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addTrigger(Triggers.playerTapsLandForMana(),
                new RegisterDelayedChooseOpponentGainsControlOfSourceEffect());
    }
}

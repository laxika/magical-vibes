package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "8")
public class DauntlessVeteran extends Card {

    public DauntlessVeteran() {
        // Whenever this creature attacks, creatures you control get +1/+1 until end of turn.
        addTrigger(Triggers.attacks(), new BoostAllOwnCreaturesEffect(1, 1));
    }
}

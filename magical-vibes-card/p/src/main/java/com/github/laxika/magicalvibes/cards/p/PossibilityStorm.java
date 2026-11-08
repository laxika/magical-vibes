package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PossibilityStormExileAndCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DGM", collectorNumber = "34")
public class PossibilityStorm extends Card {

    public PossibilityStorm() {
        addTrigger(Triggers.playerCastsSpell().castFrom(Zone.HAND), new PossibilityStormExileAndCastEffect());
    }
}

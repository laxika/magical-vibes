package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleAllOwnCreaturesPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "223")
@CardRegistration(set = "MID", collectorNumber = "206")
@CardRegistration(set = "SLD", collectorNumber = "865")
@CardRegistration(set = "WOT", collectorNumber = "62")
@CardRegistration(set = "MAR", collectorNumber = "84")
@CardRegistration(set = "DBL", collectorNumber = "206")
@CardRegistration(set = "BLC", collectorNumber = "245")
@CardRegistration(set = "FDC", collectorNumber = "238")
public class UnnaturalGrowth extends Card {

    public UnnaturalGrowth() {
        // At the beginning of each combat, double the power and toughness of each creature you
        // control until end of turn.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                new DoubleAllOwnCreaturesPowerToughnessEffect());
    }
}

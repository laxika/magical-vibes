package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPlaysAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "192")
@CardRegistration(set = "FUT", collectorNumber = "137")
@CardRegistration(set = "SLD", collectorNumber = "2074")
@CardRegistration(set = "TLE", collectorNumber = "45")
@CardRegistration(set = "BLC", collectorNumber = "122")
@CardRegistration(set = "C16", collectorNumber = "163")
@CardRegistration(set = "ZNC", collectorNumber = "80")
public class RitesOfFlourishing extends Card {

    public RitesOfFlourishing() {
        // At the beginning of each player's draw step, that player draws an additional card.
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), new DrawCardForTargetPlayerEffect(1));

        // Each player may play an additional land on each of their turns.
        addStaticEffect(new EachPlayerPlaysAdditionalLandEffect());
    }
}

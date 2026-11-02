package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "74")
@CardRegistration(set = "LEG", collectorNumber = "224")
@CardRegistration(set = "ME3", collectorNumber = "147")
@CardRegistration(set = "SLD", collectorNumber = "1068")
public class Chromium extends Card {

    public Chromium() {
        // At the beginning of your upkeep, sacrifice Chromium unless you pay {W}{U}{B}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{W}{U}{B}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Rampage 2: whenever Chromium becomes blocked, it gets +2/+2 until end of turn for
        // each creature blocking it beyond the first, i.e. 2 * (blockers - 1).
        addTrigger(Triggers.becomesBlocked(), new RampageEffect(2));
    }
}

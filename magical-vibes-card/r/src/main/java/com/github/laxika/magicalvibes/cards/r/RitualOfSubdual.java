package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "261")
@CardRegistration(set = "ME2", collectorNumber = "174")
public class RitualOfSubdual extends Card {

    public RitualOfSubdual() {
        // Cumulative upkeep {2}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{2}"));

        // If a land is tapped for mana, it produces colorless mana instead of any other type.
        addStaticEffect(new ReplaceLandManaWithColorEffect(ManaColor.COLORLESS));
    }
}

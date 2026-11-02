package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "135")
@CardRegistration(set = "ME2", collectorNumber = "97")
public class InfernalDarkness extends Card {

    public InfernalDarkness() {
        // Cumulative upkeep—Pay {B} and 1 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{B}", 1));

        // If a land is tapped for mana, it produces {B} instead of any other type.
        addStaticEffect(new ReplaceLandManaWithColorEffect(ManaColor.BLACK));
    }
}

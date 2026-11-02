package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddProducedManaWhenSnowLandTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TappedSnowLandDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "114")
@CardRegistration(set = "ME2", collectorNumber = "200")
public class WintersNight extends Card {

    public WintersNight() {
        addTrigger(Triggers.playerTapsLandForMana(), new AddProducedManaWhenSnowLandTappedEffect());
        addTrigger(Triggers.playerTapsLandForMana(), new TappedSnowLandDoesntUntapEffect());
    }
}

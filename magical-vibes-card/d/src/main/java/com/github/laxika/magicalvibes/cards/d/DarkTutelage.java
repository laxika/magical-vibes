package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "90")
@CardRegistration(set = "WOT", collectorNumber = "28")
public class DarkTutelage extends Card {

    public DarkTutelage() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new RevealTopCardPutIntoHandAndChangeLifeEffect(false));
    }
}

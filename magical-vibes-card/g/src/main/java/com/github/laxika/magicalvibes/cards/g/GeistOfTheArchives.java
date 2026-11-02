package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "62")
@CardRegistration(set = "SIR", collectorNumber = "72")
public class GeistOfTheArchives extends Card {

    public GeistOfTheArchives() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ScryEffect(1));
    }
}

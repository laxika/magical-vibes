package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C13", collectorNumber = "62")
public class TidalForce extends Card {

    public TidalForce() {
        // At the beginning of each upkeep, you may tap or untap target permanent.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new MayEffect(
                new TapOrUntapTargetPermanentEffect(),
                "Tap or untap target permanent?"));
    }
}

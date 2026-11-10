package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.APlayerHasMoreLifeThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostLifeGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "284")
@CardRegistration(set = "DMR", collectorNumber = "183")
public class WildDogs extends Card {

    public WildDogs() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new APlayerHasMoreLifeThanEachOtherPlayer()),
                new PlayerWithMostLifeGainsControlOfSourceCreatureEffect());
        addCycling("{2}");
    }
}

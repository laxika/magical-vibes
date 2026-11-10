package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.APlayerHasMoreLifeThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostLifeGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "298")
@CardRegistration(set = "CHR", collectorNumber = "65")
@CardRegistration(set = "ME1", collectorNumber = "120")
@CardRegistration(set = "ARN", collectorNumber = "49")
public class GhazbNOgre extends Card {

    public GhazbNOgre() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new APlayerHasMoreLifeThanEachOtherPlayer()),
                new PlayerWithMostLifeGainsControlOfSourceCreatureEffect());
    }
}

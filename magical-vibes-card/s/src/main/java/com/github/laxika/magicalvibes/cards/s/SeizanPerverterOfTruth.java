package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "143")
@CardRegistration(set = "MUL", collectorNumber = "15")
@CardRegistration(set = "MUL", collectorNumber = "80")
@CardRegistration(set = "MUL", collectorNumber = "145")
public class SeizanPerverterOfTruth extends Card {

    public SeizanPerverterOfTruth() {
        // At the beginning of each player's upkeep, that player loses 2 life and draws two cards.
        // Triggers.upkeep(ActivePlayer.ANY) bakes the active player onto the entry's targetId, so both steps
        // route to that player. One SequenceEffect keeps the ability a single atomic stack entry
        // with the life loss resolving before the draw.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.ACTIVE_PLAYER),
                new DrawCardForTargetPlayerEffect(2)));
    }
}

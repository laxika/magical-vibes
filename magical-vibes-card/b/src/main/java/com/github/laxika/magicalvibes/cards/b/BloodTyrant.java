package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CON", collectorNumber = "99")
@CardRegistration(set = "C16", collectorNumber = "183")
@CardRegistration(set = "E01", collectorNumber = "81")
public class BloodTyrant extends Card {

    public BloodTyrant() {
        // At the beginning of your upkeep, each player loses 1 life. Put a +1/+1 counter on this
        // creature for each 1 life lost this way.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new LoseLifeEffect(1, Subjects.eachPlayer()),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new AmountThisWay(StepKind.LOSE_LIFE))));

        // Whenever a player loses the game, put five +1/+1 counters on this creature. (2-player engine:
        // the game ends before this trigger resolves — see WithengarUnbound.)
        addTrigger(Triggers.playerLosesGame(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 5));
    }
}

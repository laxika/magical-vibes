package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.ArtifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "112")
public class IchorShade extends Card {

    public IchorShade() {
        // At the beginning of your end step, if an artifact or creature was put into a graveyard
        // from the battlefield this turn, put a +1/+1 counter on this creature.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ArtifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn()),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}

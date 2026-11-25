package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.PermanentPutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Back face of Invasion of Azgol. */
public class AshenReaper extends Card {

    public AshenReaper() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new PermanentPutIntoGraveyardFromBattlefieldThisTurn(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}

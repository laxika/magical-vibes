package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "112")
public class AdrenalineJockey extends Card {

    public AdrenalineJockey() {
        // Whenever a player casts a spell, if it's not their turn, this creature deals 4 damage to them.
        addTrigger(Triggers.playerCastsSpell().notDuringCastersTurn(),
                new DealDamageToPlayersEffect(4, Subjects.triggeringPlayer()));
        addTrigger(Triggers.youActivateExhaustAbility(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}

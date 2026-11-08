package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "112")
public class AdrenalineJockey extends Card {

    public AdrenalineJockey() {
        // Whenever a player casts a spell, if it's not their turn, this creature deals 4 damage to them.
        addTrigger(Triggers.playerCastsSpell().notDuringCastersTurn(),
                new DealDamageToPlayersEffect(4, DamageRecipient.TRIGGERING_PLAYER));
        addTrigger(Triggers.youActivateExhaustAbility(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}

package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PutCounterOnCreatureThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "149")
public class LastingTarfire extends Card {

    public LastingTarfire() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new PutCounterOnCreatureThisTurn(),
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)));
    }
}

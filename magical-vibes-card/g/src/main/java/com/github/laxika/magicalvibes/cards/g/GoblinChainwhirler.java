package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "129")
public class GoblinChainwhirler extends Card {

    public GoblinChainwhirler() {
        // When Goblin Chainwhirler enters the battlefield, it deals 1 damage to each opponent
        // and each creature and planeswalker they control.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                new DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect(1));
    }
}

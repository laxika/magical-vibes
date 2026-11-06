package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "37")
@CardRegistration(set = "8ED", collectorNumber = "39")
@CardRegistration(set = "7ED", collectorNumber = "36")
@CardRegistration(set = "STH", collectorNumber = "12")
public class SacredGround extends Card {

    public SacredGround() {
        // Whenever a spell or ability an opponent controls causes a land to be put into your
        // graveyard from the battlefield, return that card to the battlefield. "That card" is the stack entry's
        // triggering card.
        addTrigger(Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN).causedBy(PlayerRelation.OPPONENT),
                new ReturnTriggeringLandFromGraveyardToBattlefieldEffect());
    }
}

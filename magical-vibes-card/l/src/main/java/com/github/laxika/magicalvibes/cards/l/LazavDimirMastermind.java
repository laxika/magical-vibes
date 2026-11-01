package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCreatureCardInOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "174")
@CardRegistration(set = "GK1", collectorNumber = "16")
@CardRegistration(set = "ZNC", collectorNumber = "92")
public class LazavDimirMastermind extends Card {

    public LazavDimirMastermind() {
        // Whenever a creature card is put into an opponent's graveyard from anywhere, you may have
        // Lazav become a copy of that card, except its name is Lazav, Dimir Mastermind, it's legendary
        // in addition to its other types, and it has hexproof and this ability.
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS),
                new MayEffect(new BecomeCopyOfCreatureCardInOpponentGraveyardEffect(),
                        "Become a copy of that creature card?"));
    }
}

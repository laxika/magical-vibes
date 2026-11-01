package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "182")
@CardRegistration(set = "SLD", collectorNumber = "1549")
public class PrinceOfThralls extends Card {

    public PrinceOfThralls() {
        // Whenever a permanent an opponent controls is put into a graveyard, put that card onto the
        // battlefield under your control unless that opponent pays 3 life.
        addTrigger(Triggers.dies(null, ObjectRelation.OPPONENT_CONTROLS),
                new StealDyingOpponentPermanentUnlessPaysLifeEffect(3));
    }
}

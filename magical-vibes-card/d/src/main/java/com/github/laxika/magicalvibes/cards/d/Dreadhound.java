package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "97")
@CardRegistration(set = "PIO", collectorNumber = "92")
@CardRegistration(set = "DBL", collectorNumber = "97")
public class Dreadhound extends Card {

    public Dreadhound() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(3, MillRecipient.CONTROLLER));
        // "Whenever a creature dies" includes this creature's own death (it looks back, CR 603.10a).
        LoseLifeEffect loseLife = new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT);
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), loseLife);
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, ObjectRelation.ANY),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}

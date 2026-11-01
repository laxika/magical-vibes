package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "206")
@CardRegistration(set = "MOC", collectorNumber = "321")
@CardRegistration(set = "DRC", collectorNumber = "50")
public class CorpseKnight extends Card {

    public CorpseKnight() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}

package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "210")
public class ObyraDreamingDuelist extends Card {

    public ObyraDreamingDuelist() {
        addTrigger(Triggers.enters(
                        new PermanentHasSubtypePredicate(CardSubtype.FAERIE),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}

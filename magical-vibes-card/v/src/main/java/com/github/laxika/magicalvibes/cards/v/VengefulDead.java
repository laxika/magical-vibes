package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "80")
@CardRegistration(set = "SCD", collectorNumber = "117")
@CardRegistration(set = "FDC", collectorNumber = "132")
public class VengefulDead extends Card {

    public VengefulDead() {
        // Whenever this creature or another Zombie dies, each opponent loses 1 life.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ZOMBIE),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}

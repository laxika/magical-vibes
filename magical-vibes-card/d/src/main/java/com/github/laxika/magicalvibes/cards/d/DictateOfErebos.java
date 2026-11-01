package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "65")
@CardRegistration(set = "SLD", collectorNumber = "1713")
@CardRegistration(set = "SLD", collectorNumber = "2204")
@CardRegistration(set = "PIO", collectorNumber = "91")
public class DictateOfErebos extends Card {

    public DictateOfErebos() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT));
    }
}

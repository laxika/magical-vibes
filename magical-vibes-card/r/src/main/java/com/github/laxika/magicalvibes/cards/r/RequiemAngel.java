package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "18")
@CardRegistration(set = "SIS", collectorNumber = "11")
@CardRegistration(set = "C14", collectorNumber = "84")
public class RequiemAngel extends Card {

    public RequiemAngel() {
        addTrigger(Triggers.creatureDies(
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                CreateTokenEffect.whiteSpirit(1));
    }
}

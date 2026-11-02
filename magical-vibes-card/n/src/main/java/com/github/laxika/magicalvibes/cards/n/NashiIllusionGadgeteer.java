package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromGraveyardAndConjureDuplicateIntoHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YOTJ", collectorNumber = "24")
public class NashiIllusionGadgeteer extends Card {

    public NashiIllusionGadgeteer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseCardFromGraveyardAndConjureDuplicateIntoHandEffect());
    }
}

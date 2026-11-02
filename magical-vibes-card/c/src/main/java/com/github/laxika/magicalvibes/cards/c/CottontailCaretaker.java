package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantOffspringToWhiteCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "2")
public class CottontailCaretaker extends Card {

    public CottontailCaretaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GrantOffspringToWhiteCreatureCardInHandEffect("{1}"));
    }
}

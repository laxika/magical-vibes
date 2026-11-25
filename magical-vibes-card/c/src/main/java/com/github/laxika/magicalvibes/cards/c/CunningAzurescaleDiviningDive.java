package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DiviningDive;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YTDM", collectorNumber = "4")
public class CunningAzurescaleDiviningDive extends Card {

    public CunningAzurescaleDiviningDive() {
        setBackFaceCard(new DiviningDive());
        addCastingOption(new OmenCast());
        addTrigger(Triggers.enters(ObjectRelation.THIS), DiviningDive.seekCardsOfSecretlyChosenKind(2));
    }

    @Override
    public String getBackFaceClassName() {
        return "DiviningDive";
    }
}

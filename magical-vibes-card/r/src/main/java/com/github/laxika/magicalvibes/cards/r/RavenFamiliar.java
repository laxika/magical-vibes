package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "39")
@CardRegistration(set = "C13", collectorNumber = "55")
public class RavenFamiliar extends Card {

    public RavenFamiliar() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{2}{U}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
    }
}

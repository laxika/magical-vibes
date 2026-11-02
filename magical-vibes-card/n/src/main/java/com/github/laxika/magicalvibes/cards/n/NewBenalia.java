package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "172")
@CardRegistration(set = "DDI", collectorNumber = "35")
@CardRegistration(set = "DDL", collectorNumber = "34")
@CardRegistration(set = "C13", collectorNumber = "309")
@CardRegistration(set = "C15", collectorNumber = "295")
@CardRegistration(set = "C18", collectorNumber = "270")
public class NewBenalia extends Card {

    public NewBenalia() {
        addStaticEffect(new EntersTappedEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(1));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "227")
@CardRegistration(set = "M20", collectorNumber = "256")
@CardRegistration(set = "M21", collectorNumber = "255")
@CardRegistration(set = "M21", collectorNumber = "390")
@CardRegistration(set = "PIP", collectorNumber = "310")
@CardRegistration(set = "PIP", collectorNumber = "524")
@CardRegistration(set = "PIP", collectorNumber = "838")
@CardRegistration(set = "PIP", collectorNumber = "1052")
@CardRegistration(set = "SOC", collectorNumber = "415")
@CardRegistration(set = "C21", collectorNumber = "325")
@CardRegistration(set = "DSC", collectorNumber = "312")
@CardRegistration(set = "DRC", collectorNumber = "178")
@CardRegistration(set = "BRC", collectorNumber = "209")
@CardRegistration(set = "ONC", collectorNumber = "171")
public class TempleOfSilence extends Card {

    public TempleOfSilence() {
        addStaticEffect(new EntersTappedEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(1));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}

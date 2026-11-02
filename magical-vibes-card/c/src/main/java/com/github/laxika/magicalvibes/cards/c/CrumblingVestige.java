package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "170")
public class CrumblingVestige extends Card {

    public CrumblingVestige() {
        addStaticEffect(new EntersTappedEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AwardAnyColorManaEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}

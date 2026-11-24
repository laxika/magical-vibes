package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.CloakTopCardOfControllerLibraryEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "50")
@CardRegistration(set = "MKM", collectorNumber = "394")
public class CrypticCoat extends Card {

    public CrypticCoat() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new CloakTopCardOfControllerLibraryEffect(),
                new AttachSourceEquipmentToTargetCreatureEffect(Subjects.source(), Subjects.thisWay(StepKind.MANIFEST))));
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addStaticEffect(new CantBeBlockedEffect());
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(ReturnToHandEffect.self()),
                "{1}{U}: Return this Equipment to its owner's hand."));
    }
}

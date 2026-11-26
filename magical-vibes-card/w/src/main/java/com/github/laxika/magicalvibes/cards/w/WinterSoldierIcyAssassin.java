package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "239")
public class WinterSoldierIcyAssassin extends Card {

    public WinterSoldierIcyAssassin() {
        addStaticEffect(BoostEffect.self(new Scaled(new AttachmentsOnSource(false, true), 2), new Fixed(0)));

        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{3}{W}{B}",
                List.of(
                        new PutCardsOntoBattlefieldEffect(Subjects.sourceObjectCard())
                                .withCounters(CounterType.FINALITY, 1),
                        new MayEffect(
                                SequenceEffect.of(
                                        ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(),
                                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)), "an Equipment you control")
                                        .thatCanBeAttachedTo(Subjects.returnedThisResolution()),
                                        new AttachSourceEquipmentToTargetCreatureEffect(Subjects.thisWay(StepKind.CHOOSE),
                                                Subjects.returnedThisResolution())),
                                "Attach an Equipment you control to Winter Soldier?")),
                "{3}{W}{B}: Return this card from your graveyard to the battlefield with a finality counter on it. "
                        + "Then you may attach an Equipment you control to it."
        ));
    }
}

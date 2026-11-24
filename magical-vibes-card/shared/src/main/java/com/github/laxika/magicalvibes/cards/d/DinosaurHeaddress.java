package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AsThisBecomesAttachedEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class DinosaurHeaddress extends Card {

    public DinosaurHeaddress() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new AttachSourceEquipmentToTargetCreatureEffect());
        addStaticEffect(new AsThisBecomesAttachedEffect(SequenceEffect.of(
                ChooseCardEffect.oneOf(Subjects.exiledWithSourceMatching(new CardTypePredicate(CardType.CREATURE)),
                        "an exiled creature card used to craft this Equipment"),
                BecomeCopyOfSubjectEffect.of(Subjects.cardsThisWay(StepKind.CHOOSE)).by(Subjects.triggering())
                        .lasting(Duration.WHILE_ATTACHED))));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}

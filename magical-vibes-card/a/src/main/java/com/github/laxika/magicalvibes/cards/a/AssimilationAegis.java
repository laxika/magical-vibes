package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "192")
public class AssimilationAegis extends Card {

    public AssimilationAegis() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetPermanentUntilSourceLeavesEffect());
        addTrigger(Triggers.becomesAttachedToCreature(), SequenceEffect.of(
                ChooseCardEffect.oneOf(Subjects.exiledWithSourceMatching(new CardTypePredicate(CardType.CREATURE)),
                        "a creature card exiled with this Equipment"),
                BecomeCopyOfSubjectEffect.of(Subjects.cardsThisWay(StepKind.CHOOSE)).by(Subjects.triggering())
                        .lasting(Duration.WHILE_REMAINS_ATTACHED)));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}

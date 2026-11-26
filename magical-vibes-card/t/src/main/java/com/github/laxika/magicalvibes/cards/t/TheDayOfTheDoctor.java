package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilCardPredicateMayPlayWhileSourceControlledEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "121")
@CardRegistration(set = "WHO", collectorNumber = "726")
public class TheDayOfTheDoctor extends Card {

    public TheDayOfTheDoctor() {
        ExileUntilCardPredicateMayPlayWhileSourceControlledEffect search =
                new ExileUntilCardPredicateMayPlayWhileSourceControlledEffect(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY));
        addTrigger(Triggers.chapter(1), search);
        addTrigger(Triggers.chapter(2), search);
        addTrigger(Triggers.chapter(3), search);
        addTrigger(Triggers.chapter(4),
                ChoosePermanentsEffect.upTo(3,
                        Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.DOCTOR)))),
                        "up to three Doctors"),
                new MayEffect(
                        SequenceEffect.of(
                                new ExileTargetPermanentEffect(
                                        Subjects.except(Subjects.allCreatures(), Subjects.thisWay(StepKind.CHOOSE))),
                                DealDamageEffect.to(Subjects.you(), 13)),
                        "Exile all other creatures? If you do, The Day of the Doctor deals 13 damage to you."));
    }
}

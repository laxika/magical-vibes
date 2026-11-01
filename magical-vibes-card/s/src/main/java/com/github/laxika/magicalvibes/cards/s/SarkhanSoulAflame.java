package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MAT", collectorNumber = "46")
@CardRegistration(set = "MAT", collectorNumber = "96")
@CardRegistration(set = "MAT", collectorNumber = "146")
@CardRegistration(set = "MAT", collectorNumber = "181")
@CardRegistration(set = "MAT", collectorNumber = "225")
@CardRegistration(set = "TDC", collectorNumber = "301")
public class SarkhanSoulAflame extends Card {

    public SarkhanSoulAflame() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.DRAGON), 1, CostModificationScope.SELF));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DRAGON),
                        new BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect(
                                "Sarkhan, Soul Aflame", Set.of(CardSupertype.LEGENDARY))));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
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
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
                        "Sarkhan, Soul Aflame", Set.of(CardSupertype.LEGENDARY)).ofTriggeringPermanent(),
                        "Have Sarkhan become a copy of it until end of turn?"));
    }
}

package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "106")
public class LegionsEnd extends Card {

    public LegionsEnd() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentMaxManaValuePredicate(2))),
                "Target must be a creature an opponent controls with mana value 2 or less"))
                .addSpellEffect(new ExileTargetPermanentEffect(Subjects.allOf(
                        Subjects.target(),
                        Subjects.othersItsControllerControlsSharing(Subjects.target(),
                                OtherPermanentsSharing.SharedQuality.NAME, new PermanentIsCreaturePredicate()))))
                .addSpellEffect(ExileCardsWithSameNameEffect.every(Subjects.target(),
                        Subjects.controllerOf(Subjects.target()), Zone.HAND, Zone.GRAVEYARD));
    }
}

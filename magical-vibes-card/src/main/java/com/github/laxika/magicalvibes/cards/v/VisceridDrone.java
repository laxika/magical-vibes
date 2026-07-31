package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "42")
public class VisceridDrone extends Card {

    public VisceridDrone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(new PermanentIsCreaturePredicate(), swamp()),
                                List.of("a creature", "a Swamp")),
                        new DestroyTargetPermanentEffect(true)
                ),
                "{T}, Sacrifice a creature and a Swamp: Destroy target nonartifact creature. "
                        + "It can't be regenerated.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsArtifactPredicate())
                        )),
                        "Target must be a nonartifact creature")
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(new PermanentIsCreaturePredicate(), snowSwamp()),
                                List.of("a creature", "a snow Swamp")),
                        new DestroyTargetPermanentEffect(true)
                ),
                "{T}, Sacrifice a creature and a snow Swamp: Destroy target creature. "
                        + "It can't be regenerated.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature")
        ));
    }

    private static PermanentPredicate swamp() {
        return new PermanentHasSubtypePredicate(CardSubtype.SWAMP);
    }

    private static PermanentPredicate snowSwamp() {
        return new PermanentAllOfPredicate(List.of(
                swamp(), new PermanentHasSupertypePredicate(CardSupertype.SNOW)));
    }
}

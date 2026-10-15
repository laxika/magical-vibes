package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BRC", collectorNumber = "1")
@CardRegistration(set = "BRC", collectorNumber = "39")
@CardRegistration(set = "BRC", collectorNumber = "48")
public class MishraEminentOne extends Card {

    public MishraEminentOne() {
        PermanentPredicate noncreatureArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
        ));

        target(new ControlledPermanentPredicateTargetFilter(
                noncreatureArtifact, "Target must be a noncreature artifact you control"))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(CardSubtype.CONSTRUCT),
                                Set.of(CardType.CREATURE),
                                4,
                                4,
                                Map.of(),
                                true,
                                false,
                                true,
                                false,
                                false,
                                false,
                                null,
                                Set.of(),
                                false,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                new Fixed(1),
                                false,
                                Set.of(),
                                false,
                                List.of(),
                                "Mishra's Warform"));
    }
}

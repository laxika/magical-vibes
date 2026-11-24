package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOM", collectorNumber = "43")
public class ShapeAnew extends Card {

    public ShapeAnew() {
        // The reveal doesn't depend on the sacrifice (ruling): "that player" is the artifact's controller, as it is
        // or last was.
        target(TargetFilters.artifact())
                .addSpellEffect(new SacrificeSelfEffect(Subjects.target(), Subjects.controllerOf(Subjects.target()),
                        new PermanentIsArtifactPredicate()))
                .addSpellEffect(RevealUntilCardPredicateRestOnBottomRandomEffect.ontoBattlefield(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new RevealUntilCardPredicateRestOnBottomRandomEffect.Players(Subjects.controllerOf(Subjects.target())))
                        .shufflingRest());
    }
}

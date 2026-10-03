package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetPlayerPermanentsUntilEndOfNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "324")
public class TeferiTimelessVoyager extends Card {

    public TeferiTimelessVoyager() {
        // +1: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1)),
                "+1: Draw a card."
        ));

        // −3: Put target creature on top of its owner's library.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new PutTargetOnTopOfLibraryEffect()),
                "−3: Put target creature on top of its owner's library.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // −8: Each creature target opponent controls phases out and cannot phase in until the end
        // of your next turn.
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new PhaseOutTargetPlayerPermanentsUntilEndOfNextTurnEffect(
                        new PermanentIsCreaturePredicate())),
                "−8: Each creature target opponent controls phases out. Until the end of your next turn, "
                        + "they can't phase in.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}

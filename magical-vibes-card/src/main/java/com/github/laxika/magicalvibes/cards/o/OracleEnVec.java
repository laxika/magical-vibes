package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCreaturesToAttackNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "31")
public class OracleEnVec extends Card {

    public OracleEnVec() {
        // {T}: Target opponent chooses any number of creatures they control. During that player's
        // next turn, the chosen creatures attack if able, and other creatures can't attack. At the
        // beginning of that turn's end step, destroy each of the chosen creatures that didn't attack
        // this turn. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TargetPlayerChoosesCreaturesToAttackNextTurnEffect()),
                "{T}: Target opponent chooses any number of creatures they control. During that player's next turn, "
                        + "the chosen creatures attack if able, and other creatures can't attack. At the beginning of "
                        + "that turn's end step, destroy each of the chosen creatures that didn't attack this turn. "
                        + "Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN));
    }
}

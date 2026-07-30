package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPlayerLifeToSpecificValueEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "109")
public class SorinMarkov extends Card {

    public SorinMarkov() {
        // +2: Sorin Markov deals 2 damage to any target and you gain 2 life.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DealDamageToAnyTargetEffect(2), new GainLifeEffect(2)),
                "+2: Sorin Markov deals 2 damage to any target and you gain 2 life."
        ));

        // −3: Target opponent's life total becomes 10.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new SetTargetPlayerLifeToSpecificValueEffect(10)),
                "−3: Target opponent's life total becomes 10.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Must target an opponent"
                )
        ));

        // −7: You control target player during that player's next turn.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new ControlTargetPlayerNextTurnEffect()),
                "−7: You control target player during that player's next turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Must target a player"
                )
        ));
    }
}

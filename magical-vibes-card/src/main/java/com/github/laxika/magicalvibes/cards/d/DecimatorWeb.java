package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "105")
public class DecimatorWeb extends Card {

    public DecimatorWeb() {
        // {4}, {T}: Target opponent loses 2 life, gets a poison counter, then mills six cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new TargetPlayerLosesLifeEffect(2),
                        new GiveTargetPlayerPoisonCountersEffect(1),
                        new MillTargetPlayerEffect(6)
                ),
                "{4}, {T}: Target opponent loses 2 life, gets a poison counter, then mills six cards.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}

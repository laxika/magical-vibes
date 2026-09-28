package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1538")
public class Fluttershy extends Card {

    public Fluttershy() {
        // The engine has no representation for art-based physical characteristics, so the
        // counter effect uses the creature predicate for the chosen player's battlefield.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect(),
                        new CantAttackThisTurnEffect(TapUntapScope.TARGET),
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET)
                ),
                "{1}, {T}: Put a +1/+1 counter on each creature with a tail target player controls. "
                        + "Stare down up to one target creature until end of turn.",
                List.of(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player"),
                        TargetFilters.creature()
                ),
                1,
                2
        ));
    }
}

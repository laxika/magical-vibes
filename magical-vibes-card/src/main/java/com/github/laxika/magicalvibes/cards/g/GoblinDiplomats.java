package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

/**
 * Goblin Diplomats — {1}{R} Creature — Goblin 2/1.
 * {T}: Each creature attacks this turn if able.
 */
@CardRegistration(set = "M14", collectorNumber = "141")
public class GoblinDiplomats extends Card {

    public GoblinDiplomats() {
        // {T}: Each creature attacks this turn if able.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MatchingCreaturesMustAttackThisTurnEffect(new PermanentTruePredicate())),
                "{T}: Each creature attacks this turn if able."
        ));
    }
}

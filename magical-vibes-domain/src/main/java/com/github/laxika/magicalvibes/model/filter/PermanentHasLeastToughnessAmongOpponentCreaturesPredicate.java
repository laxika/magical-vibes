package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.amount.CountScope;

/**
 * Matches a creature with the least effective toughness among the creatures of a scope, judged from the source's
 * controller; several creatures can match when tied:
 * <ul>
 *   <li>{@code OPPONENTS} (the default): an opponent's creature with the least toughness among the creatures the
 *       opponents control ("the creature with the least toughness among creatures your opponents control");</li>
 *   <li>{@code CONTROLLER}: a creature the source's controller controls with the least toughness among them ("a
 *       creature with the least toughness among creatures you control", The Dining Car);</li>
 *   <li>{@code ANY_PLAYER}: the creature with the least toughness among every creature on the battlefield ("the
 *       creature with the least toughness", Purging Scythe).</li>
 * </ul>
 * The least toughness is found among every creature of the scope, whether or not it can be chosen, sacrificed or
 * dealt damage; a step choosing among the matches leaves those out of its choice (CR 608.2d).
 *
 * @param scope {@code OPPONENTS}, {@code CONTROLLER} or {@code ANY_PLAYER}
 */
public record PermanentHasLeastToughnessAmongOpponentCreaturesPredicate(CountScope scope) implements PermanentPredicate {

    public PermanentHasLeastToughnessAmongOpponentCreaturesPredicate {
        if (scope != CountScope.OPPONENTS && scope != CountScope.CONTROLLER && scope != CountScope.ANY_PLAYER) {
            throw new IllegalArgumentException("Unsupported scope: " + scope);
        }
    }

    public PermanentHasLeastToughnessAmongOpponentCreaturesPredicate() {
        this(CountScope.OPPONENTS);
    }
}

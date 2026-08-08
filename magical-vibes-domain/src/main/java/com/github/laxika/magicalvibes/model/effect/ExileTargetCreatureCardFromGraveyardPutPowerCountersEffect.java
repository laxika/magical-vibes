package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * "Exile target creature card from a graveyard. Put X +1/+1 counters on target creature, where X is
 * the power of the card you exiled." — Flesh (the left half of Flesh // Blood).
 *
 * <p>The effect reads two independent targets, so the groups live in DATA:
 * {@code graveyardTargetGroup} is the index of the {@code GraveyardCardPredicateTargetFilter} group
 * and {@code creatureTargetGroup} the index of the battlefield-creature group. The two halves are
 * applied independently, so an illegal graveyard target simply makes X zero and an illegal creature
 * target still leaves the card exiled (CR 608.2b).</p>
 */
public record ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect(int graveyardTargetGroup,
                                                                        int creatureTargetGroup)
        implements CardEffect {

    /** Graveyard card in group 0, the boosted creature in group 1. */
    public ExileTargetCreatureCardFromGraveyardPutPowerCountersEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}

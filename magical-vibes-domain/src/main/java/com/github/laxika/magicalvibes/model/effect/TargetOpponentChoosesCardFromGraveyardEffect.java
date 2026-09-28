package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * The targeted opponent chooses a card matching {@link #filter()} from their graveyard and puts
 * it onto the battlefield under the spell controller's control. Optional continuous effects can
 * be retained on that permanent through {@link #battlefieldEffectGrants()}.
 */
public record TargetOpponentChoosesCardFromGraveyardEffect(
        CardPredicate filter,
        List<CardEffect> battlefieldEffectGrants
) implements CardEffect {

    public TargetOpponentChoosesCardFromGraveyardEffect(CardPredicate filter) {
        this(filter, List.of());
    }

    public TargetOpponentChoosesCardFromGraveyardEffect {
        battlefieldEffectGrants = battlefieldEffectGrants == null
                ? List.of()
                : List.copyOf(battlefieldEffectGrants);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}

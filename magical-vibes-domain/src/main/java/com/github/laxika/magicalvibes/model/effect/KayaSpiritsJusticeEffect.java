package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.UUID;

/**
 * Resolves Kaya, Spirits' Justice's exile trigger: the controller may choose a creature card from
 * among {@code creatureCardIds} (the creature cards that were put into exile), and the targeted
 * token becomes a copy of it until end of turn, except it has flying.
 */
public record KayaSpiritsJusticeEffect(List<UUID> creatureCardIds) implements CardEffect {

    public KayaSpiritsJusticeEffect {
        creatureCardIds = creatureCardIds == null ? List.of() : List.copyOf(creatureCardIds);
    }

    public KayaSpiritsJusticeEffect() {
        this(List.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), new PermanentIsTokenPredicate());
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Adds one mana of any color that a land in {@code scope} matching {@code landPredicate} could
 * produce (the controller chooses among the available colors). If only one color is available it
 * is added automatically; if no matching land could produce colored mana, no mana is produced.
 *
 * <p>Colorless is offered only when {@code includeColorless} is set, for "any type that a land could
 * produce" wording (Naga Vitalist); "any color" wording excludes it.
 *
 * <p>Used by Fellwar Stone ({@code OPPONENTS} + any land) and Star Compass ({@code CONTROLLER} +
 * basic land).
 */
public record AwardManaOfColorsLandsCouldProduceEffect(ManaColorLandScope scope,
                                                       PermanentPredicate landPredicate,
                                                       boolean includeColorless)
        implements ManaProducingEffect {

    public AwardManaOfColorsLandsCouldProduceEffect(ManaColorLandScope scope, PermanentPredicate landPredicate) {
        this(scope, landPredicate, false);
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * The creature identified by {@code scope} (typically the enchanted/equipped creature of an Aura
 * or Equipment) gets +X/+Y as a continuous static bonus, where the amounts are {@link DynamicAmount}s.
 *
 * <p>This is the attached-scope sibling of {@link BoostSelfEffect}: "equipped creature gets +1/+1
 * for each Swamp you control", "+X/+X where X is the number of creature cards in all graveyards",
 * and "+X/+0 for each instant/sorcery in your graveyard" are all the same effect with different
 * amount parameters, collapsing the former {@code BoostCreaturePer*} family.
 *
 * <p>On an Aura or Equipment, {@code you}/{@code you control} refers to the Aura/Equipment's
 * controller (CR 109.5). The {@link com.github.laxika.magicalvibes.model.amount.CountScope#CONTROLLER}
 * scope on the amounts resolves to that controller, since the static handler evaluates amounts
 * with the source (the Aura/Equipment) as the amount source.
 */
public record AttachedBoostEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        GrantScope scope
) implements CardEffect {
}

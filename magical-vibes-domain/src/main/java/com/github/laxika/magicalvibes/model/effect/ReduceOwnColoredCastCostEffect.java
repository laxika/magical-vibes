package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Reduces this spell's own cost by {@code unit} mana, {@code amount} times: coloured symbols of the unit reduce the
 * matching coloured components, and what a coloured symbol can't reduce reduces the generic component instead
 * (CR 118.7c); the unit's generic mana reduces only the generic component (CR 118.7a).
 *
 * <ul>
 *   <li>"This spell costs {U} less to cast for each instant and sorcery you've cast this turn" (Demilich):
 *   {@code (ManaColor.BLUE, new SpellsCastThisTurn(...))}.</li>
 *   <li>"This spell costs {2}{U}{U} less to cast if it's your turn" (Discontinuity): {@code ("{2}{U}{U}")} inside a
 *   {@link ConditionalEffect}.</li>
 * </ul>
 */
public record ReduceOwnColoredCastCostEffect(ManaCost unit, DynamicAmount amount) implements CardEffect {

    /** "Costs {C} less for each …": one symbol of {@code color} per counted object. */
    public ReduceOwnColoredCastCostEffect(ManaColor color, DynamicAmount amount) {
        this(new ManaCost("{" + color.getCode() + "}"), amount);
    }

    /** "Costs {2}{U} less": the whole {@code reduction} once. */
    public ReduceOwnColoredCastCostEffect(String reduction) {
        this(new ManaCost(reduction), new Fixed(1));
    }
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * How a {@link DealDividedDamageEffect} splits its total damage across its targets.
 *
 * <ul>
 *   <li>{@code CHOSEN} — the controller announces the division as the spell/ability is put on
 *       the stack (CR 601.2b). Per-target amounts are read from {@code StackEntry.damageAssignments}
 *       (or {@code GameData.pendingETBDamageAssignments} when {@code etbAssignments} is set).</li>
 *   <li>{@code EVEN} — the total is divided evenly (rounded down) among the targets at resolution
 *       ({@code floor(total / targetCount)} each). Reads {@code StackEntry.targetIds}. Fireball.</li>
 *   <li>{@code ORDERED} — fixed per-target amounts assigned by target order (the i-th target
 *       receives {@code orderedAmounts.get(i)}). Reads {@code StackEntry.targetIds}. Cone of Flame,
 *       Arc Trail.</li>
 * </ul>
 */
public enum DivisionMode {
    CHOSEN,
    EVEN,
    ORDERED
}

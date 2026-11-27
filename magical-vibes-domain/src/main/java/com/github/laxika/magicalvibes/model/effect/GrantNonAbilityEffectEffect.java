package com.github.laxika.magicalvibes.model.effect;

/**
 * A rules restriction or rules change that applies to a permanent but is not an ability of it, applied in layer 6
 * with the abilities (CR 613.1f) and, unlike {@link GrantEffectEffect}, never removed by a "loses all abilities":
 * the permanent is not the object that has the ability. Two forms:
 * <ul>
 *   <li>the payload of a resolution's floating effect, {@code subject} {@link Subjects#target()}, the affected
 *       permanents being the floating effect's own (Display of Dominance's "permanents you control can't be the
 *       targets of blue or black spells your opponents control", Vines of Vastwood, Rikku's "can't be blocked by
 *       creatures your opponents control", {@link ApplyRuleToPermanentEffect});</li>
 *   <li>a static ability of a source over the set {@code subject} names (Zilortha's "lethal damage dealt to
 *       creatures you control is determined by their power", Stoic Star-Captain's crew bonus): it lasts while the
 *       source has the ability, whatever the affected creatures lose.</li>
 * </ul>
 *
 * @param effect  the restriction to apply (read by its readers like any granted effect)
 * @param subject {@link Subjects#target()} as a floating payload; the permanents a static ability names otherwise
 */
public record GrantNonAbilityEffectEffect(CardEffect effect, PermanentSubject subject) implements CardEffect {
}

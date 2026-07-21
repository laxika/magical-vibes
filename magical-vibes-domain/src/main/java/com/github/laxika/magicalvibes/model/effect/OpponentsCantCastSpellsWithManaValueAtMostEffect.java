package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: opponents of the source's controller can't cast spells with mana value
 * {@code maxManaValue} or less. The controller is unrestricted.
 *
 * <p>Brisela, Voice of Nightmares uses {@code (3)}. For spells with {@code {X}} in their cost,
 * the chosen value of X is included in mana value (CR 202.3c); playability still offers X spells
 * because a high enough X is always legal.
 *
 * <p>Enforced via {@code CastingPermissionService.isOpponentsManaValueSpellCastRestricted}.
 */
public record OpponentsCantCastSpellsWithManaValueAtMostEffect(int maxManaValue) implements CardEffect {
}

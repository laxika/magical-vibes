package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher effect for auras: "destroy [the enchanted permanent] unless that player pays {manaCost}
 * or {lifeCost} life." Placed in the {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED} slot;
 * the paying player is the enchanted permanent's controller (baked onto the trigger's {@code targetId}),
 * and the permanent that is destroyed is the one the source Aura is attached to
 * ({@code sourcePermanentId}). Used by Erosion ({@code ("{1}", 1)}).
 *
 * <p>The affected player chooses whether to save the permanent; if they do, the engine spends the
 * cheaper resource available (floating mana that would empty at end of step, otherwise life). A
 * player who can pay neither loses the permanent automatically.
 *
 * @param manaCost the mana cost (e.g. {@code "{1}"}) the player may pay to save the permanent
 * @param lifeCost the amount of life the player may pay instead to save the permanent
 */
public record DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect(String manaCost, int lifeCost) implements CardEffect {
}

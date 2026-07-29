package com.github.laxika.magicalvibes.model.effect;

/**
 * "That player may pay {M} or N life. If the player does, [wrapped]."
 *
 * <p>The paying player is the one carried on the stack entry's {@code targetId} — for an
 * {@code EACH_UPKEEP_TRIGGERED} trigger that is the active player, so the prompt goes to the
 * player whose upkeep it is rather than to the source's controller (Emberwilde Djinn).
 *
 * <p>Only a yes/no choice is offered: accepting spends floating mana when the mana cost can be
 * paid, otherwise life. This mirrors {@link DestroyEnchantedPermanentUnlessPaysManaOrLifeEffect}
 * — mana that would empty at end of step is strictly the cheaper resource, so the engine never
 * has to ask.
 */
public record TargetPlayerMayPayManaOrLifeEffect(String manaCost, int lifeCost, CardEffect wrapped,
                                                 String prompt) implements CardEffect {
}

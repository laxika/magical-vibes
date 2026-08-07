package com.github.laxika.magicalvibes.model.effect;

/**
 * Which combat action an {@link EnchantedCreatureCombatTaxEffect} taxes, and therefore which player
 * pays and how often the amount is charged.
 *
 * <p>None of these make the action illegal — the creature may still be declared, and per CR 508.1h /
 * CR 509.1d the amount simply joins the total cost to attack or block, which the declaring player
 * must be able to pay.
 */
public enum CombatTaxKind {

    /**
     * "Enchanted creature can't attack unless its controller pays {N}" (Brainwash) — charged once per
     * declaration of the enchanted creature as an attacker, paid by its controller.
     */
    ATTACK,

    /**
     * "Enchanted creature can't block unless its controller pays {N}" (Oppressive Rays) — charged once
     * per block declared by the enchanted creature, paid by its controller.
     */
    BLOCK_WITH,

    /**
     * "Enchanted creature can't be blocked unless defending player pays {N} for each creature they
     * control that's blocking it" (Awesome Presence) — charged per blocker assigned to the enchanted
     * creature, paid by the defending player rather than the Aura's or the creature's controller.
     */
    BE_BLOCKED_BY
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Like {@link MayEffect}, but the player must pay a mana cost to get the effect.
 * Used for "you may pay {X}. If you do, [effect]" patterns (e.g. Spellbomb cycle).
 *
 * <p>{@code payerIsEnchantedController} redirects the "you may pay" prompt from the ability's
 * controller to the enchanted permanent's controller — the player carried on the stack entry's
 * {@code targetId} by an {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED} trigger. This
 * models "that player may pay {X}" where "that player" is the enchanted creature's controller,
 * not the Aura's controller (e.g. Paralyze).
 */
public record MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt,
                               boolean payerIsEnchantedController) implements CardEffect {

    public MayPayManaEffect(String manaCost, CardEffect wrapped, String prompt) {
        this(manaCost, wrapped, prompt, false);
    }
}

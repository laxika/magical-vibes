package com.github.laxika.magicalvibes.model.effect;

/**
 * "That player chooses artifact, creature, land, or non-Aura enchantment. All nontoken permanents of
 * that type phase out." (Teferi's Realm). Resolution pauses for the chooser — the stack entry's
 * {@code targetId} when set by {@code EACH_UPKEEP_TRIGGERED} (the active player), otherwise the
 * source controller — then every matching nontoken permanent on every battlefield phases out via
 * {@code PhasingService}.
 */
public record PhaseOutChosenTypeNontokenPermanentsEffect() implements CardEffect {
}

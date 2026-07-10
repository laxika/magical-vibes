package com.github.laxika.magicalvibes.model.amount;

import java.util.List;

/**
 * Evaluates to {@code amount} when the controller controls at least one permanent for every
 * name in {@code requiredNames}, and to {@code otherwise} when any required name is missing.
 *
 * <p>Models the Urza land ("Tron") mana boost — e.g. Urza's Mine's "{T}: Add {C}. If you control
 * an Urza's Power-Plant and an Urza's Tower, add {C}{C} instead." Feeding this straight to
 * {@code AwardManaEffect} keeps the whole ability on the mana-ability resolution path (which only
 * reads {@code AwardManaEffect} amounts) rather than a stack-resolved {@code ConditionalEffect}.
 */
public record FixedIfControlsAllNamed(List<String> requiredNames, int amount, int otherwise)
        implements DynamicAmount {
}

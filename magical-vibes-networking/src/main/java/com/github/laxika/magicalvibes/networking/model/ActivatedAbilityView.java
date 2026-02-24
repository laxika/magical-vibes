package com.github.laxika.magicalvibes.networking.model;

public record ActivatedAbilityView(String description, boolean requiresTap, boolean needsTarget, boolean needsSpellTarget, String manaCost, Integer loyaltyCost) {
}

package com.github.laxika.magicalvibes.networking.model;

import java.util.List;

public record ActivatedAbilityView(String description, boolean requiresTap, boolean needsTarget, boolean targetsPlayer, List<String> allowedTargetTypes, String manaCost) {
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

import java.util.List;

public record GrantSourceActivatedAbilitiesUntilEndOfTurnEffect(
        List<ActivatedAbility> abilities,
        String copiedFromCardName
) implements CardEffect {
}

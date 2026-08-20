package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that makes each creature controlled by the source's controller every creature
 * type. The same grant applies to that controller's creature spells and creature cards they own
 * outside the battlefield.
 */
public record GrantAllCreatureTypesToOwnCreaturesEffect() implements CardEffect {
}

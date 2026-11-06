package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the permanent that granted the triggered ability this effect is part of ("transform Optimus Prime" in the
 * ability Optimus Prime gives the chosen creature), the stack entry's granting permanent
 * ({@code StackEntry.getGrantingPermanentId()}).
 */
public record TransformGrantingPermanentEffect() implements CardEffect {
}

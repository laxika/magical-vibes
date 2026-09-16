package com.github.laxika.magicalvibes.model.effect;

/** Static restriction: this creature can't attack a player it has already attacked this turn. */
public record CantAttackPlayerAlreadyAttackedThisTurnEffect()
        implements PreviouslyAttackedPlayerRestrictionEffect {
}

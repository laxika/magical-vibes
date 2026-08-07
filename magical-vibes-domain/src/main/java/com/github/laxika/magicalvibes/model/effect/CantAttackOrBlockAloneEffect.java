package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature can't attack alone, and — when {@code restrictsBlocking} is
 * {@code true} — can't block alone either.
 * <p>
 * Per CR 508.1c, a creature with this restriction can't be the only creature declared as an
 * attacker. When it also restricts blocking, per CR 509.1a it can't be the only creature
 * declared as a blocker. Two or more creatures with the restriction can attack or block
 * together.
 *
 * @param restrictsBlocking whether the blocking half of the restriction applies; cards such as
 *                          Bonded Construct that only say "can't attack alone" pass {@code false}
 */
public record CantAttackOrBlockAloneEffect(boolean restrictsBlocking) implements CardEffect {

    public CantAttackOrBlockAloneEffect() {
        this(true);
    }
}

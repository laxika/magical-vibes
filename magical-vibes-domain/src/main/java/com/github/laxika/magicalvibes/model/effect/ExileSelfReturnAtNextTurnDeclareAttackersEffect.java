package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the source permanent and returns it under its controller's control, tapped and attacking,
 * at the beginning of that controller's next declare-attackers step. The returned permanent may
 * optionally be unable to be blocked for that combat.
 */
public record ExileSelfReturnAtNextTurnDeclareAttackersEffect(boolean returnCantBeBlocked) implements CardEffect {

    public ExileSelfReturnAtNextTurnDeclareAttackersEffect() {
        this(false);
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates the wrapped token under the control of the player whose action caused the trigger.
 *
 * <p>The player is read from what the trigger's matcher recorded on the stack entry: the triggering
 * permanent's controller when one was recorded, else the entry's non-targeting player ("that player",
 * such as the caster of a spell-cast trigger), else the entry's controller. A token blueprint sized by
 * the triggering spell reads {@code TargetSpellManaValue} (Metallurgic Summonings, Pure Reflection).
 *
 * @param token token blueprint to create
 * @param attacking whether created attacking creatures attack the trigger's captured target
 * @param resolveCurrentController use the triggering permanent's current controller, or its last known controller if absent
 */
public record CreateTokenForTriggeringPlayerEffect(CreateTokenEffect token, boolean attacking,
                                                  boolean resolveCurrentController)
        implements TokenCreatingEffect {

    public CreateTokenForTriggeringPlayerEffect(CreateTokenEffect token, boolean attacking) {
        this(token, attacking, false);
    }

    public CreateTokenForTriggeringPlayerEffect(CreateTokenEffect token) {
        this(token, false);
    }

    @Override
    public DynamicAmount tokenAmount() {
        return token.amount();
    }

    @Override
    public CardType tokenType() {
        return token.primaryType();
    }

    @Override
    public int tokenPower() {
        return token.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return token.tokenToughness();
    }
}

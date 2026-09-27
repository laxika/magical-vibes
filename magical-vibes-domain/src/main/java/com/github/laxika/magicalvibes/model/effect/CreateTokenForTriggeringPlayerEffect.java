package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates the wrapped token under the control of the player whose action caused the trigger.
 *
 * <p>The trigger collector carries that player on the stack entry. Permanent-event triggers use
 * the triggering permanent's controller field; spell-cast and upkeep triggers use the target or
 * active-player context as appropriate. Direct attack-player triggers use the stack entry's
 * controller as the player whose attack caused the trigger. In a spell-cast trigger, the collector
 * also stores the triggering spell's mana value in the entry event value, so the token blueprint
 * may use {@code EventValue} for its power and toughness.
 *
 * @param token token blueprint to create
 * @param attacking whether created attacking creatures attack the trigger's captured target
 */
public record CreateTokenForTriggeringPlayerEffect(CreateTokenEffect token, boolean attacking)
        implements TokenCreatingEffect, TriggeringSpellManaValueEffect {

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

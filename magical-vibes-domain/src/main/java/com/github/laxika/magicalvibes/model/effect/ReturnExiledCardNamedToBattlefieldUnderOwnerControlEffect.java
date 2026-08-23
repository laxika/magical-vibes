package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns one face-up exiled card with the specified name to the battlefield under its owner's
 * control. If multiple matching cards exist, the resolving ability's controller chooses one.
 */
public record ReturnExiledCardNamedToBattlefieldUnderOwnerControlEffect(String cardName)
        implements CardEffect {
}

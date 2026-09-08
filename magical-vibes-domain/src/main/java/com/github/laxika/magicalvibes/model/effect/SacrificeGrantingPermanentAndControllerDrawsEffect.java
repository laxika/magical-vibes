package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * The controller of the permanent that granted this ability sacrifices that permanent, then
 * draws cards. The granting permanent is bound when the ability is activated.
 */
public record SacrificeGrantingPermanentAndControllerDrawsEffect(int cards, UUID grantingPermanentId)
        implements CardEffect {

    public SacrificeGrantingPermanentAndControllerDrawsEffect(int cards) {
        this(cards, null);
    }
}

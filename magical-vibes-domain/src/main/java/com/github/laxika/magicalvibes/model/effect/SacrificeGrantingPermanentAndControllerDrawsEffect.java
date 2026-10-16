package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * The controller of the permanent that granted this ability sacrifices that permanent, then
 * draws cards. The granting permanent and its controller are bound when the ability is activated,
 * so the draw still goes to that controller if the granting permanent has already left.
 */
public record SacrificeGrantingPermanentAndControllerDrawsEffect(int cards, UUID grantingPermanentId,
                                                                 UUID grantingControllerId)
        implements CardEffect {

    public SacrificeGrantingPermanentAndControllerDrawsEffect(int cards) {
        this(cards, null, null);
    }

    public SacrificeGrantingPermanentAndControllerDrawsEffect(int cards, UUID grantingPermanentId) {
        this(cards, grantingPermanentId, null);
    }
}

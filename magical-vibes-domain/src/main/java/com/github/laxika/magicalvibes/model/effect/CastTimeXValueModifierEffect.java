package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * A static effect that modifies a spell's announced X while it is being cast.
 *
 * <p>Unlike {@link CastTimeXValueEffect}, which supplies a spell's own cast-time X value, this
 * interface is used by battlefield effects and applies its modification to the value produced by
 * earlier cast-time processing.</p>
 */
public interface CastTimeXValueModifierEffect extends CardEffect {

    boolean appliesTo(Card spell, UUID spellControllerId, UUID sourceControllerId);

    int modifyCastTimeXValue(int xValue);
}

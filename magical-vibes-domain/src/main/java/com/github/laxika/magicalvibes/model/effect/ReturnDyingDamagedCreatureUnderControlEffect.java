package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * {@code ON_DAMAGED_CREATURE_DIES} effect for Dread Slaver: immediately return the creature that
 * died to the battlefield under the ability controller's control, optionally granting it a colour
 * and a subtype "in addition to its other colors and types" (persistent grants, so they survive
 * turn resets). Unlike Seraph's delayed return there is no control-loss sacrifice linkage — Dread
 * Slaver keeps the creature even if it later leaves.
 *
 * @param grantColor   colour added to the returned creature, or {@code null} for none
 * @param grantSubtype subtype added to the returned creature, or {@code null} for none
 */
public record ReturnDyingDamagedCreatureUnderControlEffect(CardColor grantColor, CardSubtype grantSubtype)
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}

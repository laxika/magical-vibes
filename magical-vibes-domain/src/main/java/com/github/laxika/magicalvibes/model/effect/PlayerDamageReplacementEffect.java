package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/**
 * Capability for a static replacement effect that can modify damage dealt to its controller.
 * The engine applies the capability only while the affected player is the permanent's controller.
 */
public interface PlayerDamageReplacementEffect extends ReplacementEffect {

    int replaceDamage(StackEntry entry, int damage);
}

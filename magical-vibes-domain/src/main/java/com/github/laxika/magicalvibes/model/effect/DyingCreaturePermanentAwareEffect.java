package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/** Binds a last-known snapshot of a creature that caused a death trigger. */
public interface DyingCreaturePermanentAwareEffect {

    CardEffect boundToDyingCreature(Permanent dyingCreature);
}

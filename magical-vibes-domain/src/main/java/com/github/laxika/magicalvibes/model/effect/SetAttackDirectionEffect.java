package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AttackDirection;

/** Records the direction chosen for the source permanent's directional attack restriction. */
public record SetAttackDirectionEffect(AttackDirection direction) implements CardEffect {
}

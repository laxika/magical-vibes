package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static replacement effect that increases damage from noncreature sources
 * against creatures, battles, or opponents.
 */
public interface NoncreatureSourceDamageBonusEffect extends CardEffect {

    int amount();
}

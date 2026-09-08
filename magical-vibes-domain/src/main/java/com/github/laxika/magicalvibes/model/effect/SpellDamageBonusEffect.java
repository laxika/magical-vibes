package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Capability for a static effect that adds damage to matching colored spells,
 * regardless of which player controls the spell or the static effect.
 */
public interface SpellDamageBonusEffect extends CardEffect {

    Set<CardColor> colors();

    int amount();
}

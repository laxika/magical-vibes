package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/** Capability for a static effect that adds damage from matching sources to opponents or their permanents. */
public interface SourceOpponentDamageBonusEffect extends CardEffect {

    int amount();

    boolean appliesTo(Set<CardColor> sourceColors, boolean artifactSource);
}

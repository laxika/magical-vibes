package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Collections;
import java.util.Set;

/** Static replacement effect for matching-color sources controlled by this permanent's controller. */
public record AdditionalDamageToOpponentsFromColorSourcesEffect(int amount, Set<CardColor> colors)
        implements SourceOpponentDamageBonusEffect {

    public AdditionalDamageToOpponentsFromColorSourcesEffect(int amount, CardColor color) {
        this(amount, Set.of(color));
    }

    @Override
    public boolean appliesTo(Set<CardColor> sourceColors, boolean artifactSource) {
        return !Collections.disjoint(colors, sourceColors);
    }
}

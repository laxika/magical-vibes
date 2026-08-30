package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Static replacement effect: if a red or artifact source controlled by this permanent's
 * controller would deal damage to an opponent or a permanent an opponent controls, it deals that
 * much damage plus {@code amount} instead.
 */
public record AdditionalDamageToOpponentsFromRedOrArtifactSourcesEffect(int amount)
        implements SourceOpponentDamageBonusEffect {

    @Override
    public boolean appliesTo(Set<CardColor> sourceColors, boolean artifactSource) {
        return artifactSource || sourceColors.contains(CardColor.RED);
    }
}

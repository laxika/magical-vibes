package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

/** Loading Zone's replacement effect for counters put on controlled creatures, Spacecraft, or Planets. */
public record DoubleCountersOnControlledCreaturesSpacecraftsAndPlanetsEffect()
        implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count * 2 : count;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                             boolean affectedPermanentIsArtifact) {
        return affectedPermanentIsCreature;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                             boolean affectedPermanentIsArtifact, Permanent sourcePermanent,
                             Permanent affectedPermanent) {
        if (affectedPermanent == null) {
            return affectedPermanentIsCreature;
        }
        return affectedPermanentIsCreature
                || affectedPermanent.getCard().getSubtypes().contains(CardSubtype.SPACECRAFT)
                || affectedPermanent.getCard().getSubtypes().contains(CardSubtype.PLANET);
    }
}

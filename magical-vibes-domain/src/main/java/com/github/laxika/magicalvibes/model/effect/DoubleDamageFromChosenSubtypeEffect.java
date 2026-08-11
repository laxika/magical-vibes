package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that doubles damage from sources carrying the subtype chosen for its source
 * permanent.
 */
public record DoubleDamageFromChosenSubtypeEffect() implements SourceDamageMultiplyingEffect {

    private static final PermanentPredicate SOURCE_FILTER = new PermanentHasSourceChosenSubtypePredicate();

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        return SOURCE_FILTER;
    }

    @Override
    public boolean matchesStackEntrySource(StackEntry entry, Permanent effectSource) {
        CardSubtype chosenSubtype = effectSource.getChosenSubtype();
        return chosenSubtype != null && (entry.getEffectiveDamageSourceCard().getSubtypes().contains(chosenSubtype)
                || entry.getEffectiveDamageSourceCard().getKeywords().contains(Keyword.CHANGELING));
    }
}

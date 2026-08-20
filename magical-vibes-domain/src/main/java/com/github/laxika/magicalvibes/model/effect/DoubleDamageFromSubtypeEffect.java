package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that doubles damage from sources carrying a specified subtype.
 */
public record DoubleDamageFromSubtypeEffect(CardSubtype subtype) implements SourceDamageMultiplyingEffect {

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        return new PermanentHasSubtypePredicate(subtype);
    }

    @Override
    public boolean matchesStackEntrySource(StackEntry entry, Permanent effectSource) {
        return entry.getEffectiveDamageSourceCard().getSubtypes().contains(subtype)
                || entry.getEffectiveDamageSourceCard().getKeywords().contains(Keyword.CHANGELING);
    }
}

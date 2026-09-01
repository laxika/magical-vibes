package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;

/**
 * Static replacement effect that changes qualifying damage from an instant or sorcery spell to
 * its controller.
 */
public record ReplaceInstantOrSorceryDamageToControllerEffect(int threshold, int replacementDamage)
        implements PlayerDamageReplacementEffect {

    @Override
    public int replaceDamage(StackEntry entry, int damage) {
        if (entry == null || entry.getSourcePermanentId() != null || damage < threshold
                || (entry.getEntryType() != StackEntryType.INSTANT_SPELL
                && entry.getEntryType() != StackEntryType.SORCERY_SPELL)) {
            return damage;
        }
        return replacementDamage;
    }
}

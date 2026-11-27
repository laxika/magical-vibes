package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;

/**
 * Static replacement effect that changes qualifying damage from an instant or sorcery source to
 * its controller: an instant or sorcery spell, or an ability whose source is an instant or sorcery
 * card ("when you cycle this card, it deals 6 damage"), since the card is that ability's source.
 */
public record ReplaceInstantOrSorceryDamageToControllerEffect(int threshold, int replacementDamage)
        implements PlayerDamageReplacementEffect {

    @Override
    public int replaceDamage(StackEntry entry, int damage) {
        if (entry == null || entry.getSourcePermanentId() != null || damage < threshold
                || !instantOrSorcerySource(entry)) {
            return damage;
        }
        return replacementDamage;
    }

    private static boolean instantOrSorcerySource(StackEntry entry) {
        if (entry.getEntryType() == StackEntryType.INSTANT_SPELL || entry.getEntryType() == StackEntryType.SORCERY_SPELL) {
            return true;
        }
        return entry.getEntryType().isAbility() && entry.getCard() != null
                && (entry.getCard().hasType(CardType.INSTANT) || entry.getCard().hasType(CardType.SORCERY));
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Static effect that doubles damage from sources with the specified mana-value parity. */
public record DoubleDamageFromManaValueParityEffect(ManaValueParity parity)
        implements SourceDamageMultiplyingEffect {

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        return new PermanentManaValueParityPredicate(parity);
    }

    @Override
    public boolean matchesStackEntrySource(StackEntry entry, Permanent effectSource) {
        Card sourceCard = entry.getEffectiveDamageSourceCard();
        int manaValue = sourceCard.getManaValue();
        if (isSpellEntry(entry.getEntryType())) {
            manaValue += entry.getXValue();
        }
        return parity.matches(manaValue);
    }

    private static boolean isSpellEntry(StackEntryType entryType) {
        return switch (entryType) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, SORCERY_SPELL, INSTANT_SPELL,
                    ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case TRIGGERED_ABILITY, ACTIVATED_ABILITY -> false;
        };
    }
}

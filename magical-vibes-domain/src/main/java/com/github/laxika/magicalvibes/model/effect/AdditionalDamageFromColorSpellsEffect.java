package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Static replacement effect: if a spell of one of {@code colors} would deal damage to a permanent
 * or player, it deals that much damage plus {@code amount} instead.
 */
public record AdditionalDamageFromColorSpellsEffect(Set<CardColor> colors, int amount)
        implements SpellDamageBonusEffect {
}

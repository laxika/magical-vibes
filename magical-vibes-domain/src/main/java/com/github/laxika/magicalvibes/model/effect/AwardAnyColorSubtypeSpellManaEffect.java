package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Produces {@code amount} mana of any one color that can only be spent to cast spells of the given
 * creature {@code subtype} (e.g. Sliver Hive: "{T}: Add one mana of any color. Spend this mana only
 * to cast a Sliver spell.").
 *
 * <p>Routed into {@link com.github.laxika.magicalvibes.model.ManaPool}'s per-subtype spell-only
 * bucket, so — unlike {@link AwardAnyColorManaEffect} with
 * {@link ManaSpendRestriction#SUBTYPE_SPELL_OR_ABILITY} (Smokebraider) — it cannot pay for activated
 * abilities. Distinct from {@link ManaSpendRestriction#CHOSEN_SUBTYPE_CREATURE} (Cavern of Souls),
 * whose subtype is the one chosen as the source entered rather than a fixed one printed on the card.
 */
public record AwardAnyColorSubtypeSpellManaEffect(int amount, CardSubtype subtype) implements ManaProducingEffect {

    public AwardAnyColorSubtypeSpellManaEffect(CardSubtype subtype) {
        this(1, subtype);
    }
}

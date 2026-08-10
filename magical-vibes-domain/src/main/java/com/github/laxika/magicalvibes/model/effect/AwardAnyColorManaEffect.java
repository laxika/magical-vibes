package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "Add {@code amount} mana of any color", with {@code restriction} carrying whatever CR 106.6 rider
 * the printed ability puts on that mana (spend restriction, additional effect, or delayed trigger).
 * The restriction also decides whether the controller picks one colour for the whole batch or one
 * per mana — see {@link ManaSpendRestriction}.
 *
 * <p>{@code subtype} is the type printed on the card for the {@link ManaSpendRestriction#SUBTYPE_SPELL}
 * and {@link ManaSpendRestriction#SUBTYPE_SPELL_OR_ABILITY} restrictions (Sliver Hive's Sliver,
 * Smokebraider's Elemental) and is {@code null} for every other restriction. The
 * {@code CHOSEN_SUBTYPE_*} forms read their type off the source permanent instead, since it is
 * chosen as the permanent enters.
 */
public record AwardAnyColorManaEffect(DynamicAmount amount,
                                      ManaSpendRestriction restriction,
                                      CardSubtype subtype) implements ManaProducingEffect {

    public AwardAnyColorManaEffect() {
        this(1);
    }

    public AwardAnyColorManaEffect(int amount) {
        this(new Fixed(amount), ManaSpendRestriction.NONE, null);
    }

    /** "Add X mana of any one color", X coming from the ability's xValue (Springjack Pasture). */
    public AwardAnyColorManaEffect(DynamicAmount amount) {
        this(amount, ManaSpendRestriction.NONE, null);
    }

    public AwardAnyColorManaEffect(int amount, ManaSpendRestriction restriction) {
        this(new Fixed(amount), restriction, null);
    }

    public AwardAnyColorManaEffect(int amount, ManaSpendRestriction restriction, CardSubtype subtype) {
        this(new Fixed(amount), restriction, subtype);
    }

    /**
     * Only the unrestricted form counts as full colour coverage; every restriction pays into a
     * bucket an ordinary cost cannot draw from. An X-scaled amount is excluded too — the estimator
     * has no static quantity to model, which is what the pre-merge X variant reported.
     */
    @Override
    public boolean estimatedCountsAllColors() {
        return restriction == ManaSpendRestriction.NONE && amount instanceof Fixed;
    }

    @Override
    public int estimatedWildcardMana() {
        return switch (restriction) {
            case NONE, CREATURE_SPELL_ONLY, CHOSEN_SUBTYPE_CREATURE, CHOSEN_SUBTYPE_CREATURE_UNCOUNTERABLE ->
                    amount instanceof Fixed fixed ? fixed.value() : 0;
            case IMPRINTED_CARD_COLORS, INSTANT_SORCERY_COPY, INSTANT_SORCERY_ONLY, FLASHBACK_ONLY,
                 SUBTYPE_SPELL, SUBTYPE_SPELL_OR_ABILITY -> 0;
        };
    }
}

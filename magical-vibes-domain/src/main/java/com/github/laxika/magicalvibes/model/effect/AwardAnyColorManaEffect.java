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
 *
 * <p>{@code targetsPlayer} declares that a player target is required. The separate
 * {@code manaRecipientIsTargetPlayer} flag controls whether that player, rather than the effect's
 * controller, receives the mana; a target may instead exist only to determine a dynamic amount.
 */
public record AwardAnyColorManaEffect(DynamicAmount amount,
                                      ManaSpendRestriction restriction,
                                      CardSubtype subtype,
                                      boolean sourceBecomesProducedColorUntilEndOfTurn,
                                      boolean targetsPlayer,
                                      boolean manaRecipientIsTargetPlayer,
                                      boolean markSourceAsHavingAddedManaThisTurn,
                                      boolean anyColorCombination,
                                      boolean grantsAdditionalPlusOneCounter) implements ManaProducingEffect {

    public AwardAnyColorManaEffect() {
        this(1);
    }

    public AwardAnyColorManaEffect(int amount) {
        this(new Fixed(amount), ManaSpendRestriction.NONE, null, false, false, false, false, false, false);
    }

    /** "Add N mana in any combination of colors." */
    public AwardAnyColorManaEffect(int amount, boolean anyColorCombination) {
        this(new Fixed(amount), ManaSpendRestriction.NONE, null, false,
                false, false, false, anyColorCombination, false);
    }

    /** "Add mana of any color. If that mana is spent on a multicolored creature spell, it enters with an additional +1/+1 counter." */
    public static AwardAnyColorManaEffect forMulticoloredCreatureCounter(int amount) {
        return new AwardAnyColorManaEffect(new Fixed(amount), ManaSpendRestriction.NONE, null,
                false, false, false, false, false, true);
    }

    /** "Add one mana of any color. This creature becomes that color until end of turn." */
    public AwardAnyColorManaEffect(boolean sourceBecomesProducedColorUntilEndOfTurn) {
        this(new Fixed(1), ManaSpendRestriction.NONE, null, sourceBecomesProducedColorUntilEndOfTurn,
                false, false, false, false, false);
    }

    /** "Add X mana of any one color", X coming from the ability's xValue (Springjack Pasture). */
    public AwardAnyColorManaEffect(DynamicAmount amount) {
        this(amount, ManaSpendRestriction.NONE, null, false, false, false, false, false, false);
    }

    public AwardAnyColorManaEffect(int amount, ManaSpendRestriction restriction) {
        this(new Fixed(amount), restriction, null, false, false, false, false, false, false);
    }

    public AwardAnyColorManaEffect(int amount, ManaSpendRestriction restriction, CardSubtype subtype) {
        this(new Fixed(amount), restriction, subtype, false, false, false, false, false, false);
    }

    public AwardAnyColorManaEffect(int amount, ManaSpendRestriction restriction, CardSubtype subtype,
                                   boolean sourceBecomesProducedColorUntilEndOfTurn) {
        this(new Fixed(amount), restriction, subtype, sourceBecomesProducedColorUntilEndOfTurn,
                false, false, false, false, false);
    }

    public AwardAnyColorManaEffect(DynamicAmount amount, ManaSpendRestriction restriction, CardSubtype subtype,
                                   boolean sourceBecomesProducedColorUntilEndOfTurn) {
        this(amount, restriction, subtype, sourceBecomesProducedColorUntilEndOfTurn,
                false, false, false, false, false);
    }

    /** Targeted "target player adds mana" form with a dynamic amount. */
    public AwardAnyColorManaEffect(DynamicAmount amount, boolean targetsPlayer,
                                   boolean markSourceAsHavingAddedManaThisTurn) {
        this(amount, ManaSpendRestriction.NONE, null, false, targetsPlayer,
                targetsPlayer, markSourceAsHavingAddedManaThisTurn, false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
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
            case ABILITIES, IMPRINTED_CARD_COLORS, INSTANT_SORCERY_COPY, INSTANT_SORCERY_ONLY,
                 ARTIFACT_SPELLS_OR_ABILITIES, FLASHBACK_ONLY,
                 CHOSEN_SUBTYPE_SPELL_OR_ABILITY, SUBTYPE_SPELL, SUBTYPE_SPELL_OR_ABILITY,
                 CHOSEN_SUBTYPE_CREATURE_SOURCE_SPELL_OR_ABILITY,
                 CREATURE_SPELLS_OR_ABILITIES, MANA_VALUE_AT_LEAST_FOUR,
                 CREATURE_SPELL_MANA_VALUE_AT_LEAST_FOUR_OR_X,
                 PARTY_SPELL_OR_ABILITY -> 0;
        };
    }
}

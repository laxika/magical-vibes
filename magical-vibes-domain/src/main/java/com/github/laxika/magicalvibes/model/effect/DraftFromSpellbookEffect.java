package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

import java.util.List;
import java.util.Objects;

/**
 * Offers a random subset of a digital spellbook and lets a player draft one card.
 *
 * <p>With {@code conjure} set ("conjure a card of your choice from … spellbook") the chosen card counts
 * as conjured, so "whenever you conjure" abilities trigger; see {@link #conjureIntoHand} and
 * {@link #conjureOntoBattlefield}.
 *
 * <p>The cards come from one of two lists of printings. {@code spellbook} offers its cards as cards to choose
 * from, handled by {@code mode}. {@code namedOptions} offers them by name as options: the chosen card goes onto the
 * battlefield, unless the mode is {@link DraftMode#CONJURE_TO_HAND} (see {@link #draftIntoHand}), which puts it into
 * hand.
 */
public record DraftFromSpellbookEffect(
        List<SpellbookCard> spellbook,
        List<SpellbookCard> namedOptions,
        int offeredCardCount,
        DraftMode mode,
        boolean conjure,
        CounterType entryCounterType,
        int entryCounterCount) implements CardEffect {

    public enum DraftMode {
        PERPETUALLY_BECOMES_ENCHANTMENT,
        MAY_CAST_WITHOUT_PAYING_MANA_COST,
        MAY_CAST_WITHOUT_PAYING_MANA_COST_TO_CONTROLLER,
        CONJURE_TO_HAND
    }

    public DraftFromSpellbookEffect(
            List<SpellbookCard> spellbook,
            List<SpellbookCard> namedOptions,
            int offeredCardCount,
            DraftMode mode,
            boolean conjure) {
        this(spellbook, namedOptions, offeredCardCount, mode, conjure, null, 0);
    }

    public DraftFromSpellbookEffect {
        spellbook = List.copyOf(Objects.requireNonNull(spellbook, "Spellbook cannot be null"));
        namedOptions = List.copyOf(Objects.requireNonNull(namedOptions, "Named options cannot be null"));
        Objects.requireNonNull(mode, "Draft mode cannot be null");
        if (spellbook.isEmpty() == namedOptions.isEmpty()) {
            throw new IllegalArgumentException("Exactly one spellbook source is required");
        }
        int available = spellbook.isEmpty() ? namedOptions.size() : spellbook.size();
        if (offeredCardCount < 1 || offeredCardCount > available) {
            throw new IllegalArgumentException("Invalid number of offered spellbook cards");
        }
    }

    public DraftFromSpellbookEffect(List<SpellbookCard> spellbook, DraftMode mode) {
        this(spellbook, List.of(), Math.min(3, spellbook.size()), mode, false);
    }

    /** "Draft a card from this spellbook and put it onto the battlefield": three of {@code namedOptions} offered. */
    public static DraftFromSpellbookEffect draftOntoBattlefield(List<SpellbookCard> namedOptions) {
        return new DraftFromSpellbookEffect(List.of(), namedOptions, Math.min(3, namedOptions.size()),
                DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT, false);
    }

    /** "Draft a card from this spellbook": the chosen card goes into the controller's hand, not conjured. */
    public static DraftFromSpellbookEffect draftIntoHand(List<SpellbookCard> namedOptions) {
        return new DraftFromSpellbookEffect(List.of(), namedOptions, Math.min(3, namedOptions.size()),
                DraftMode.CONJURE_TO_HAND, false);
    }

    /** "Conjure a card of your choice from … spellbook onto the battlefield." */
    public static DraftFromSpellbookEffect conjureOntoBattlefield(List<SpellbookCard> namedOptions) {
        return new DraftFromSpellbookEffect(List.of(), namedOptions, namedOptions.size(),
                DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT, true);
    }

    /**
     * "Conjure a card of your choice from … spellbook onto the battlefield with {@code count}
     * {@code counterType} counters on it": the counters are placed as the card enters, so they belong
     * to this entry only and are not part of the card if it is later played again.
     */
    public static DraftFromSpellbookEffect conjureOntoBattlefieldWithCounters(
            List<SpellbookCard> namedOptions, CounterType counterType, int count) {
        return new DraftFromSpellbookEffect(List.of(), namedOptions, namedOptions.size(),
                DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT, true, counterType, count);
    }

    /** "Conjure a card of your choice from … spellbook into your hand", handled by {@code mode}. */
    public static DraftFromSpellbookEffect conjureIntoHand(List<SpellbookCard> spellbook, DraftMode mode) {
        if (mode == DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT) {
            throw new IllegalArgumentException("Conjured spellbook cards are not changed perpetually");
        }
        return new DraftFromSpellbookEffect(spellbook, List.of(), spellbook.size(), mode, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return mode == DraftMode.MAY_CAST_WITHOUT_PAYING_MANA_COST
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return mode == DraftMode.MAY_CAST_WITHOUT_PAYING_MANA_COST
                ? PlayerRelation.OPPONENT : PlayerRelation.ANY;
    }

    public record SpellbookCard(String setCode, String collectorNumber) {}
}

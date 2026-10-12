package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Offers a random subset of a digital spellbook and lets a player draft one card.
 *
 * <p>With {@code conjure} set ("conjure a card of your choice from … spellbook") the chosen card counts
 * as conjured, so "whenever you conjure" abilities trigger; see {@link #conjureIntoHand} and
 * {@link #conjureOntoBattlefield}. Card factories put the chosen card onto the battlefield.
 */
public record DraftFromSpellbookEffect(
        List<SpellbookCard> spellbook,
        List<Supplier<? extends Card>> cardFactories,
        int offeredCardCount,
        DraftMode mode,
        boolean conjure) implements CardEffect {

    public enum DraftMode {
        PERPETUALLY_BECOMES_ENCHANTMENT,
        MAY_CAST_WITHOUT_PAYING_MANA_COST,
        MAY_CAST_WITHOUT_PAYING_MANA_COST_TO_CONTROLLER,
        CONJURE_TO_HAND
    }

    public DraftFromSpellbookEffect {
        spellbook = List.copyOf(Objects.requireNonNull(spellbook, "Spellbook cannot be null"));
        cardFactories = List.copyOf(Objects.requireNonNull(cardFactories, "Card factories cannot be null"));
        Objects.requireNonNull(mode, "Draft mode cannot be null");
        if (spellbook.isEmpty() == cardFactories.isEmpty()) {
            throw new IllegalArgumentException("Exactly one spellbook source is required");
        }
        int available = spellbook.isEmpty() ? cardFactories.size() : spellbook.size();
        if (offeredCardCount < 1 || offeredCardCount > available) {
            throw new IllegalArgumentException("Invalid number of offered spellbook cards");
        }
    }

    public DraftFromSpellbookEffect(List<SpellbookCard> spellbook, DraftMode mode) {
        this(spellbook, List.of(), Math.min(3, spellbook.size()), mode, false);
    }

    public DraftFromSpellbookEffect(List<Supplier<? extends Card>> cardFactories, int offeredCardCount) {
        this(List.of(), cardFactories, offeredCardCount, DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT, false);
    }

    public DraftFromSpellbookEffect(List<Supplier<? extends Card>> cardFactories) {
        this(cardFactories, 3);
    }

    /** "Conjure a card of your choice from … spellbook onto the battlefield." */
    public static DraftFromSpellbookEffect conjureOntoBattlefield(
            List<Supplier<? extends Card>> cardFactories) {
        return new DraftFromSpellbookEffect(List.of(), cardFactories, cardFactories.size(),
                DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT, true);
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

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Offers a random subset of a digital spellbook and lets a player draft one card. */
public record DraftFromSpellbookEffect(
        List<SpellbookCard> spellbook,
        List<Supplier<? extends Card>> cardFactories,
        int offeredCardCount,
        DraftMode mode) implements CardEffect {

    public enum DraftMode {
        PERPETUALLY_BECOMES_ENCHANTMENT,
        MAY_CAST_WITHOUT_PAYING_MANA_COST
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
        this(spellbook, List.of(), Math.min(3, spellbook.size()), mode);
    }

    public DraftFromSpellbookEffect(List<Supplier<? extends Card>> cardFactories, int offeredCardCount) {
        this(List.of(), cardFactories, offeredCardCount, DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT);
    }

    public DraftFromSpellbookEffect(List<Supplier<? extends Card>> cardFactories) {
        this(cardFactories, 3);
    }

    @Override
    public TargetSpec targetSpec() {
        return mode == DraftMode.MAY_CAST_WITHOUT_PAYING_MANA_COST
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }

    public record SpellbookCard(String setCode, String collectorNumber) {}
}

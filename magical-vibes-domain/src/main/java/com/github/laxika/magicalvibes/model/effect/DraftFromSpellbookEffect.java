package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

import java.util.List;

/** Offers a random selection of cards from a fixed digital spellbook and drafts one into hand. */
public record DraftFromSpellbookEffect(List<SpellbookCard> spellbook, DraftMode mode) implements CardEffect {

    public enum DraftMode {
        PERPETUALLY_BECOMES_ENCHANTMENT,
        MAY_CAST_WITHOUT_PAYING_MANA_COST
    }

    public DraftFromSpellbookEffect(List<SpellbookCard> spellbook) {
        this(spellbook, DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT);
    }

    public DraftFromSpellbookEffect {
        spellbook = List.copyOf(spellbook);
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

    public record SpellbookCard(String setCode, String collectorNumber) {
    }
}

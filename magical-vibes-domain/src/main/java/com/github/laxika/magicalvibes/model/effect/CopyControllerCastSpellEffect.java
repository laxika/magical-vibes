package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.Set;
import java.util.UUID;

/**
 * Resolution-time effect that creates a single copy of a spell for its controller.
 * The copy's controller may choose new targets.
 *
 * <p>Populated at trigger time by {@code TriggerCollectionService} when a player
 * in {@code GameData.playersWithSpellCopyUntilEndOfTurn} casts an instant or sorcery.
 * The snapshot preserves the spell's state at cast time.</p>
 *
 * <p>Used by The Mirari Conjecture chapter III's delayed triggered ability.</p>
 *
 * @param spellSnapshot snapshot of the spell on the stack at trigger time
 * @param castingPlayerId the player who cast the spell (and controls the copy)
 * @param additionalTypes types added to the copied card
 * @param tokenCopy whether the copied creature spell becomes a token as it resolves
 * @param mayChooseNewTargets whether the copy gets a choose-new-targets prompt
 */
public record CopyControllerCastSpellEffect(
        StackEntry spellSnapshot,
        UUID castingPlayerId,
        Set<Keyword> grantedKeywords,
        Set<CardType> additionalTypes,
        boolean tokenCopy,
        boolean mayChooseNewTargets
) implements CardEffect {

    public CopyControllerCastSpellEffect(StackEntry spellSnapshot, UUID castingPlayerId) {
        this(spellSnapshot, castingPlayerId, Set.of(), Set.of(), false, true);
    }

    public CopyControllerCastSpellEffect(StackEntry spellSnapshot, UUID castingPlayerId,
            Set<Keyword> grantedKeywords) {
        this(spellSnapshot, castingPlayerId, grantedKeywords, Set.of(), false, true);
    }

    public CopyControllerCastSpellEffect(StackEntry spellSnapshot, UUID castingPlayerId,
            Set<Keyword> grantedKeywords, Set<CardType> additionalTypes, boolean tokenCopy) {
        this(spellSnapshot, castingPlayerId, grantedKeywords, additionalTypes, tokenCopy, true);
    }

    public CopyControllerCastSpellEffect {
        grantedKeywords = grantedKeywords == null ? Set.of() : Set.copyOf(grantedKeywords);
        additionalTypes = additionalTypes == null ? Set.of() : Set.copyOf(additionalTypes);
    }
}

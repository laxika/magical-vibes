package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared target-selection helpers for spells cast from exile "without paying their mana cost"
 * (Paradigm copies re-cast each precombat main, Improvisation Capstone's batch cast).
 *
 * <p>Single-target casts keep their historical flattened candidate list ({@link
 * #flatSingleTargetCandidates}) so their behavior is unchanged. Multi-target casts reuse the normal
 * cast path's per-slot validator ({@link ValidTargetService#computeValidTargetsForSpell}) so each
 * target slot honors its own filter (e.g. "target player" then "creature you control") and targets
 * are collected in the card's declared order.
 */
@Component
@RequiredArgsConstructor
public class ExileCastTargetSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetLegalityService targetLegalityService;
    private final ValidTargetService validTargetService;

    public StackEntryType mapCardTypeToSpellType(Card card) {
        return switch (card.getType()) {
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    }

    /**
     * Candidates for the first target the player must choose: the per-slot list for multi-target
     * spells, or the historical flattened list for single-target spells.
     */
    public List<UUID> firstSlotCandidates(GameData gameData, Card card, UUID controllerId) {
        return card.getMaxTargets() > 1
                ? nextSlotCandidates(gameData, card, controllerId, List.of())
                : flatSingleTargetCandidates(gameData, card, controllerId);
    }

    /** Candidates for a prepared modal spell, filtered against its already-unwrapped effects. */
    public List<UUID> firstSlotCandidates(GameData gameData, Card card, List<CardEffect> spellEffects,
                                          UUID controllerId) {
        if (card.getMaxTargets() > 1) {
            return nextSlotCandidates(gameData, card, controllerId, List.of());
        }

        Set<UUID> candidates = new LinkedHashSet<>(flatSingleTargetCandidates(gameData, card, controllerId));
        candidates.addAll(gameData.orderedPlayerIds);
        gameData.forEachPermanent((ignored, permanent) -> candidates.add(permanent.getId()));
        Set<TargetType> preparedTargetTypes = EffectResolution.computeAllowedTargets(
                spellEffects, List.of(), card.isAura(), card.isEnchantPlayer());
        if (preparedTargetTypes.contains(TargetType.GRAVEYARD)) {
            gameData.playerGraveyards.values().forEach(graveyard ->
                    graveyard.forEach(graveyardCard -> candidates.add(graveyardCard.getId())));
        }

        List<UUID> legal = new ArrayList<>();
        for (UUID candidate : candidates) {
            try {
                if (gameQueryService.findCardInGraveyardById(gameData, candidate) != null) {
                    if (!preparedTargetTypes.contains(TargetType.GRAVEYARD)) {
                        continue;
                    }
                    targetLegalityService.validateEffectTargetInZone(
                            gameData, card, spellEffects, candidate, Zone.GRAVEYARD, 0, controllerId);
                } else if (gameQueryService.findCardInExileById(gameData, candidate) != null) {
                    if (!preparedTargetTypes.contains(TargetType.EXILE)) {
                        continue;
                    }
                    targetLegalityService.validateEffectTargetInZone(
                            gameData, card, spellEffects, candidate, Zone.EXILE, 0, controllerId);
                } else {
                    targetLegalityService.validateSpellTargeting(
                            gameData, card, spellEffects, candidate, null, controllerId, true, 0);
                }
                legal.add(candidate);
            } catch (IllegalStateException ignored) {
                // This candidate does not satisfy the selected mode's target declaration.
            }
        }
        return legal;
    }

    /**
     * Legal candidates for the next target slot (position = {@code chosenTargets.size()}) of a
     * multi-target spell, reusing the normal cast path's per-slot validation. Permanent, player,
     * graveyard, and exile candidates are concatenated; already-chosen targets are excluded by the
     * underlying validator.
     */
    public List<UUID> nextSlotCandidates(GameData gameData, Card card, UUID controllerId, List<UUID> chosenTargets) {
        ValidTargetsResponse response =
                validTargetService.computeValidTargetsForSpell(gameData, card, controllerId, chosenTargets);
        List<UUID> candidates = new ArrayList<>();
        candidates.addAll(response.validPermanentIds());
        candidates.addAll(response.validPlayerIds());
        candidates.addAll(response.validGraveyardCardIds());
        candidates.addAll(response.validExiledCardIds());
        return candidates;
    }

    /**
     * Returns true if a full legal set of targets exists for a multi-target spell (CR 601.2c) — i.e.
     * every target slot can be filled with a distinct legal target. Checked before any prompt so a
     * spell that can never be legally cast fizzles up front instead of prompting for a doomed target.
     * Greedy per-slot assignment is exact for the disjoint-filter spells cast through this path.
     */
    public boolean hasLegalTargetSet(GameData gameData, Card card, UUID controllerId) {
        List<UUID> chosen = new ArrayList<>();
        int totalSlots = card.getMaxTargets();
        for (int slot = 0; slot < totalSlots; slot++) {
            List<UUID> candidates = nextSlotCandidates(gameData, card, controllerId, chosen);
            if (candidates.isEmpty()) {
                return false;
            }
            chosen.add(candidates.getFirst());
        }
        return true;
    }

    /**
     * Historical single-target candidate list (permanents matching the card's target filter, or all
     * creatures when unfiltered, plus every player when the spell can target players, plus the legal
     * graveyard cards when the spell targets a card in a graveyard — cipher copies of cards such as
     * Midnight Recovery would otherwise never find a target).
     */
    public List<UUID> flatSingleTargetCandidates(GameData gameData, Card card, UUID controllerId) {
        Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(card);
        List<UUID> validTargets = new ArrayList<>();

        if (allowedTargets.contains(TargetType.PERMANENT)) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent p : battlefield) {
                    if (card.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                        if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                            validTargets.add(p.getId());
                        }
                    } else if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            validTargets.addAll(gameData.orderedPlayerIds);
        }

        if (allowedTargets.contains(TargetType.SPELL_ON_STACK)) {
            for (var stackEntry : gameData.stack) {
                UUID targetId = stackEntry.getCard().getId();
                if (targetLegalityService.checkSpellTargetOnStack(
                        gameData, targetId, card.getTargetFilter(), controllerId).isEmpty()) {
                    validTargets.add(targetId);
                }
            }
        }

        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            validTargets.addAll(validTargetService
                    .computeValidTargetsForSpell(gameData, card, controllerId, List.of())
                    .validGraveyardCardIds());
        }

        if (allowedTargets.contains(TargetType.EXILE)) {
            validTargets.addAll(validTargetService
                    .computeValidTargetsForSpell(gameData, card, controllerId, List.of())
                    .validExiledCardIds());
        }

        return validTargets;
    }
}

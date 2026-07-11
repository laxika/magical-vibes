package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
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
    private final ValidTargetService validTargetService;

    public StackEntryType mapCardTypeToSpellType(Card card) {
        return switch (card.getType()) {
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
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
                : flatSingleTargetCandidates(gameData, card);
    }

    /**
     * Legal candidates for the next target slot (position = {@code chosenTargets.size()}) of a
     * multi-target spell, reusing the normal cast path's per-slot validation. Permanent, player, and
     * graveyard candidates are concatenated; already-chosen targets are excluded by the underlying
     * validator.
     */
    public List<UUID> nextSlotCandidates(GameData gameData, Card card, UUID controllerId, List<UUID> chosenTargets) {
        ValidTargetsResponse response =
                validTargetService.computeValidTargetsForSpell(gameData, card, controllerId, chosenTargets);
        List<UUID> candidates = new ArrayList<>();
        candidates.addAll(response.validPermanentIds());
        candidates.addAll(response.validPlayerIds());
        candidates.addAll(response.validGraveyardCardIds());
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
     * creatures when unfiltered, plus every player when the spell can target players). Kept verbatim
     * so single-target exile casts (Knowledge Pool, Omen Machine, single-target Paradigm/Capstone)
     * behave exactly as before.
     */
    public List<UUID> flatSingleTargetCandidates(GameData gameData, Card card) {
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

        return validTargets;
    }
}

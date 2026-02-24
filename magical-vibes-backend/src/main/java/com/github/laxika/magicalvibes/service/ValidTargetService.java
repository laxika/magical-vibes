package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidTargetService {

    private final GameQueryService gameQueryService;

    public ValidTargetsResponse computeValidTargetsForSpell(GameData gameData, Card card, UUID controllerId, List<UUID> alreadySelectedIds) {
        boolean isMultiTarget = card.getMaxTargets() > 1;
        Set<TargetType> allowedTargets = card.getAllowedTargets();

        List<UUID> validPermanentIds = new ArrayList<>();
        List<UUID> validPlayerIds = new ArrayList<>();
        Set<UUID> excludeIds = alreadySelectedIds != null ? Set.copyOf(alreadySelectedIds) : Set.of();

        int positionIndex = alreadySelectedIds != null ? alreadySelectedIds.size() : 0;

        if (allowedTargets.contains(TargetType.PERMANENT)) {
            // Determine per-position filter for multi-target spells
            TargetFilter positionFilter = null;
            if (isMultiTarget && positionIndex < card.getMultiTargetFilters().size()) {
                positionFilter = card.getMultiTargetFilters().get(positionIndex);
            }

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent perm : battlefield) {
                    if (excludeIds.contains(perm.getId())) continue;
                    if (isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, positionFilter)) {
                        validPermanentIds.add(perm.getId());
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            boolean multiTargetAllowsPlayers = isMultiTarget && card.getEffects(EffectSlot.SPELL).stream()
                    .anyMatch(e -> e instanceof DealOrderedDamageToAnyTargetsEffect);
            boolean singleTargetAllowsPlayers = !isMultiTarget;

            if (singleTargetAllowsPlayers || multiTargetAllowsPlayers) {
                for (UUID playerId : gameData.playerIds) {
                    if (excludeIds.contains(playerId)) continue;
                    if (isValidPlayerTarget(gameData, card, playerId, controllerId)) {
                        validPlayerIds.add(playerId);
                    }
                }
            }
        }

        String prompt = "Select a target for " + card.getName();
        if (isMultiTarget) {
            prompt = "Select targets for " + card.getName();
        }

        return new ValidTargetsResponse(validPermanentIds, validPlayerIds, card.getMinTargets(), card.getMaxTargets(), prompt);
    }

    public ValidTargetsResponse computeValidTargetsForAbility(GameData gameData, Card sourceCard, ActivatedAbility ability, UUID controllerId, int permanentIndex) {
        List<UUID> validPermanentIds = new ArrayList<>();
        List<UUID> validPlayerIds = new ArrayList<>();

        boolean targetsPlayer = ability.getEffects().stream().anyMatch(CardEffect::canTargetPlayer);
        boolean targetsPermanent = ability.getEffects().stream().anyMatch(CardEffect::canTargetPermanent);
        boolean targetsBlockingThis = ability.getEffects().stream()
                .anyMatch(e -> e instanceof DestroyCreatureBlockingThisEffect);

        if (targetsPermanent) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent perm : battlefield) {
                    if (isValidAbilityPermanentTarget(gameData, sourceCard, ability, perm, controllerId, targetsBlockingThis, permanentIndex)) {
                        validPermanentIds.add(perm.getId());
                    }
                }
            }
        }

        if (targetsPlayer) {
            for (UUID playerId : gameData.playerIds) {
                if (isValidAbilityPlayerTarget(gameData, playerId, controllerId)) {
                    validPlayerIds.add(playerId);
                }
            }
        }

        String prompt = "Select a target for " + sourceCard.getName() + " ability";

        return new ValidTargetsResponse(validPermanentIds, validPlayerIds, 1, 1, prompt);
    }

    private boolean isValidPermanentTarget(GameData gameData, Card card, Permanent perm, UUID controllerId,
                                            boolean isMultiTarget, TargetFilter positionFilter) {
        CardColor sourceColor = card.getColor();

        // Protection from source color
        if (gameQueryService.hasProtectionFrom(gameData, perm, sourceColor)) {
            return false;
        }

        // Shroud
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)) {
            return false;
        }

        // Hexproof (only blocks if target is opponent's)
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return false;
            }
        }

        // CantBeTargetOfSpellsOrAbilitiesEffect (granted hexproof-like)
        if (gameQueryService.hasGrantedEffect(gameData, perm, CantBeTargetOfSpellsOrAbilitiesEffect.class)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return false;
            }
        }

        // Can't be targeted by spell color
        if (gameQueryService.cantBeTargetedBySpellColor(gameData, perm, sourceColor)) {
            return false;
        }

        // Card's TargetFilter
        if (card.getTargetFilter() != null) {
            try {
                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceCardId(card.getId())
                        .withSourceControllerId(controllerId);
                gameQueryService.validateTargetFilter(card.getTargetFilter(), perm, filterContext);
            } catch (IllegalStateException e) {
                return false;
            }
        }

        // Per-position filter for multi-target spells
        if (positionFilter != null) {
            try {
                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceCardId(card.getId())
                        .withSourceControllerId(controllerId);
                gameQueryService.validateTargetFilter(positionFilter, perm, filterContext);
            } catch (IllegalStateException e) {
                return false;
            }
        }

        // For multi-target spells that don't allow players, target must be a creature
        if (isMultiTarget) {
            boolean multiTargetAllowsPlayers = card.getEffects(EffectSlot.SPELL).stream()
                    .anyMatch(e -> e instanceof DealOrderedDamageToAnyTargetsEffect);
            if (!multiTargetAllowsPlayers && !gameQueryService.isCreature(gameData, perm)) {
                return false;
            }
        }

        // Effect-level validation: use the TargetValidationService logic inline
        // For spells with no TargetFilter and no multi-target, the TargetValidationService checks
        // creature/player type constraints. We replicate the essential checks here.
        // The TargetFilter already handles the filter predicate constraints (e.g. "nonblack creature").
        // Effect-specific constraints (e.g. "must be attacking") are handled by effect validators.
        // We rely on the TargetFilter being the canonical source of truth.

        return true;
    }

    private boolean isValidPlayerTarget(GameData gameData, Card card, UUID playerId, UUID controllerId) {
        // Player shroud
        if (gameQueryService.playerHasShroud(gameData, playerId)) {
            return false;
        }

        // PlayerPredicateTargetFilter (e.g. "target opponent")
        if (card.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter) {
            if (playerFilter.predicate() instanceof PlayerRelationPredicate rel) {
                if (rel.relation() == PlayerRelation.OPPONENT && controllerId.equals(playerId)) {
                    return false;
                }
                if (rel.relation() == PlayerRelation.SELF && !controllerId.equals(playerId)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isValidAbilityPermanentTarget(GameData gameData, Card sourceCard, ActivatedAbility ability,
                                                   Permanent perm, UUID controllerId,
                                                   boolean targetsBlockingThis, int sourcePermanentIndex) {
        // Special case: targeting blocking creature
        if (targetsBlockingThis) {
            if (!gameQueryService.isCreature(gameData, perm) || !perm.isBlocking()) {
                return false;
            }
            if (!perm.getBlockingTargets().contains(sourcePermanentIndex)) {
                return false;
            }
        }

        // Shroud
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)) {
            return false;
        }

        // Hexproof (only blocks if target is opponent's)
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return false;
            }
        }

        // CantBeTargetOfSpellsOrAbilitiesEffect
        if (gameQueryService.hasGrantedEffect(gameData, perm, CantBeTargetOfSpellsOrAbilitiesEffect.class)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return false;
            }
        }

        // TargetFilter from ability
        if (ability.getTargetFilter() != null) {
            try {
                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceCardId(sourceCard.getId())
                        .withSourceControllerId(controllerId);
                gameQueryService.validateTargetFilter(ability.getTargetFilter(), perm, filterContext);
            } catch (IllegalStateException e) {
                return false;
            }
        }

        // Protection from source color (for abilities that deal damage)
        if (sourceCard.getColor() != null) {
            boolean dealsDamage = ability.getEffects().stream().anyMatch(e ->
                    e.canTargetPermanent() && (e.getClass().getSimpleName().contains("DealDamage")
                            || e.getClass().getSimpleName().contains("Destroy")));
            if (dealsDamage && gameQueryService.hasProtectionFrom(gameData, perm, sourceCard.getColor())) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidAbilityPlayerTarget(GameData gameData, UUID playerId, UUID controllerId) {
        if (gameQueryService.playerHasShroud(gameData, playerId)) {
            return false;
        }
        return true;
    }
}

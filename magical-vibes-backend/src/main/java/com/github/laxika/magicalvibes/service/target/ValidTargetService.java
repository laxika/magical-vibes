package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
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
            TargetFilter positionFilter = isMultiTarget && positionIndex < card.getMultiTargetFilters().size()
                    ? card.getMultiTargetFilters().get(positionIndex)
                    : null;

            gameData.forEachPermanent((playerId, perm) -> {
                if (excludeIds.contains(perm.getId())) return;
                if (isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, positionFilter)) {
                    validPermanentIds.add(perm.getId());
                }
            });
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            boolean multiTargetAllowsPlayers = isMultiTarget && card.getEffects(EffectSlot.SPELL).stream()
                    .anyMatch(CardEffect::canTargetPlayer);
            boolean singleTargetAllowsPlayers = !isMultiTarget;

            if (singleTargetAllowsPlayers || multiTargetAllowsPlayers) {
                for (UUID playerId : gameData.playerIds) {
                    if (excludeIds.contains(playerId)) continue;
                    if (isValidPlayerTarget(gameData, card.getTargetFilter(), playerId, controllerId)) {
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
        return computeValidTargetsForAbility(gameData, sourceCard, ability, controllerId, permanentIndex, List.of());
    }

    public ValidTargetsResponse computeValidTargetsForAbility(GameData gameData, Card sourceCard, ActivatedAbility ability, UUID controllerId, int permanentIndex, List<UUID> alreadySelectedIds) {
        List<UUID> validPermanentIds = new ArrayList<>();
        List<UUID> validPlayerIds = new ArrayList<>();
        Set<UUID> excludeIds = alreadySelectedIds != null && !alreadySelectedIds.isEmpty() ? Set.copyOf(alreadySelectedIds) : Set.of();

        if (ability.isMultiTarget()) {
            // Multi-target ability: use per-position filter
            int positionIndex = alreadySelectedIds != null ? alreadySelectedIds.size() : 0;
            TargetFilter positionFilter = positionIndex < ability.getMultiTargetFilters().size()
                    ? ability.getMultiTargetFilters().get(positionIndex)
                    : null;

            if (positionFilter instanceof PlayerPredicateTargetFilter) {
                // Player-targeting position: add valid players
                for (UUID playerId : gameData.playerIds) {
                    if (excludeIds.contains(playerId)) continue;
                    if (isValidPlayerTarget(gameData, ability.getTargetFilter(), playerId, controllerId)) {
                        validPlayerIds.add(playerId);
                    }
                }
            } else {
                gameData.forEachPermanent((playerId, perm) -> {
                    if (excludeIds.contains(perm.getId())) return;
                    if (isValidAbilityPermanentTarget(gameData, sourceCard, ability, perm, controllerId, false, permanentIndex, positionFilter)) {
                        validPermanentIds.add(perm.getId());
                    }
                });
            }

            String prompt = "Select targets for " + sourceCard.getName() + " ability";
            return new ValidTargetsResponse(validPermanentIds, validPlayerIds, ability.getMinTargets(), ability.getMaxTargets(), prompt);
        }

        boolean targetsPlayer = ability.getEffects().stream().anyMatch(CardEffect::canTargetPlayer);
        boolean targetsPermanent = ability.getEffects().stream().anyMatch(CardEffect::canTargetPermanent);
        boolean targetsBlockingThis = ability.getEffects().stream()
                .anyMatch(e -> e instanceof DestroyCreatureBlockingThisEffect);

        if (targetsPermanent) {
            gameData.forEachPermanent((playerId, perm) -> {
                if (isValidAbilityPermanentTarget(gameData, sourceCard, ability, perm, controllerId, targetsBlockingThis, permanentIndex, null)) {
                    validPermanentIds.add(perm.getId());
                }
            });
        }

        if (targetsPlayer) {
            for (UUID playerId : gameData.playerIds) {
                if (isValidPlayerTarget(gameData, ability.getTargetFilter(), playerId, controllerId)) {
                    validPlayerIds.add(playerId);
                }
            }
        }

        String prompt = "Select a target for " + sourceCard.getName() + " ability";

        return new ValidTargetsResponse(validPermanentIds, validPlayerIds, 1, 1, prompt);
    }

    /**
     * Checks whether a permanent can legally be targeted by a spell cast by the given controller.
     * Evaluates shroud, hexproof, CantBeTargetOfSpellsOrAbilities, protection from color,
     * protection from card types, cant-be-targeted-by-spell-color, and the spell's TargetFilter.
     */
    public boolean canPermanentBeTargetedBySpell(GameData gameData, Permanent perm, Card spellCard, UUID castingPlayerId) {
        CardColor sourceColor = spellCard.getColor();

        // Protection from source color
        if (gameQueryService.hasProtectionFrom(gameData, perm, sourceColor)) {
            return false;
        }
        // Protection from source card type
        if (gameQueryService.hasProtectionFromSourceCardTypes(perm, spellCard)) {
            return false;
        }
        // Protection from source subtype
        if (gameQueryService.hasProtectionFromSourceSubtypes(perm, spellCard)) {
            return false;
        }

        if (isBlockedByHexproofOrGrantedEffect(gameData, perm, castingPlayerId)) {
            return false;
        }

        // Can't be targeted by spell color
        if (gameQueryService.cantBeTargetedBySpellColor(gameData, perm, sourceColor)) {
            return false;
        }

        // Can't be targeted by non-color sources (e.g. Gaea's Revenge)
        if (gameQueryService.cantBeTargetedByNonColorSources(gameData, perm, spellCard)) {
            return false;
        }

        // Card's TargetFilter
        if (!passesTargetFilter(gameData, spellCard.getTargetFilter(), perm, spellCard.getId(), castingPlayerId)) {
            return false;
        }

        return true;
    }

    private boolean isValidPermanentTarget(GameData gameData, Card card, Permanent perm, UUID controllerId,
                                            boolean isMultiTarget, TargetFilter positionFilter) {
        if (!canPermanentBeTargetedBySpell(gameData, perm, card, controllerId)) {
            return false;
        }

        // Per-position filter for multi-target spells
        if (!passesTargetFilter(gameData, positionFilter, perm, card.getId(), controllerId)) {
            return false;
        }

        // "Any target" in MTG means creature, planeswalker, or player — not all permanents.
        // When all permanent-targeting spell effects also target players (i.e. "any target"),
        // restrict valid permanent targets to creatures and planeswalkers.
        if (card.getTargetFilter() == null) {
            List<CardEffect> permanentEffects = card.getEffects(EffectSlot.SPELL).stream()
                    .filter(CardEffect::canTargetPermanent)
                    .toList();
            boolean allAnyTarget = !permanentEffects.isEmpty()
                    && permanentEffects.stream().allMatch(CardEffect::canTargetPlayer);
            if (allAnyTarget) {
                if (!gameQueryService.isCreature(gameData, perm) && !isPlaneswalker(perm)) {
                    return false;
                }
            } else if (isMultiTarget && !gameQueryService.isCreature(gameData, perm)) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidPlayerTarget(GameData gameData, TargetFilter targetFilter, UUID playerId, UUID controllerId) {
        // Player shroud
        if (gameQueryService.playerHasShroud(gameData, playerId)) {
            return false;
        }

        // Player hexproof (only blocks opponents)
        if (!controllerId.equals(playerId) && gameQueryService.playerHasHexproof(gameData, playerId)) {
            return false;
        }

        // PlayerPredicateTargetFilter (e.g. "target opponent")
        if (targetFilter instanceof PlayerPredicateTargetFilter playerFilter) {
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
                                                   boolean targetsBlockingThis, int sourcePermanentIndex,
                                                   TargetFilter positionFilter) {
        // Special case: targeting blocking creature
        if (targetsBlockingThis) {
            if (!gameQueryService.isCreature(gameData, perm) || !perm.isBlocking()) {
                return false;
            }
            if (!perm.getBlockingTargets().contains(sourcePermanentIndex)) {
                return false;
            }
        }

        if (isBlockedByHexproofOrGrantedEffect(gameData, perm, controllerId)) {
            return false;
        }

        // Can't be targeted by non-color sources (e.g. Gaea's Revenge)
        if (gameQueryService.cantBeTargetedByNonColorSources(gameData, perm, sourceCard)) {
            return false;
        }

        // TargetFilter from ability
        if (!passesTargetFilter(gameData, ability.getTargetFilter(), perm, sourceCard.getId(), controllerId)) {
            return false;
        }

        // Protection from source color/type/subtype (for abilities that deal damage or destroy)
        boolean dealsDamageOrDestroys = ability.getEffects().stream().anyMatch(e ->
                e.canTargetPermanent() && e.isDamageOrDestruction());
        if (dealsDamageOrDestroys) {
            if (sourceCard.getColor() != null && gameQueryService.hasProtectionFrom(gameData, perm, sourceCard.getColor())) {
                return false;
            }
            if (gameQueryService.hasProtectionFromSourceCardTypes(perm, sourceCard)) {
                return false;
            }
            if (gameQueryService.hasProtectionFromSourceSubtypes(perm, sourceCard)) {
                return false;
            }
        }

        // Per-position filter for multi-target abilities
        if (!passesTargetFilter(gameData, positionFilter, perm, sourceCard.getId(), controllerId)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if at least one legal target exists for the given spell.
     * Per MTG rule 601.2c, a spell can't be cast unless a legal set of targets can be chosen for it.
     */
    public boolean hasValidTargetsForSpell(GameData gameData, Card card, UUID controllerId) {
        Set<TargetType> allowedTargets = card.getAllowedTargets();
        boolean isMultiTarget = card.getMaxTargets() > 1;

        if (allowedTargets.contains(TargetType.PERMANENT)) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent perm : battlefield) {
                    if (isValidPermanentTarget(gameData, card, perm, controllerId, isMultiTarget, null)) {
                        return true;
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            boolean multiTargetAllowsPlayers = isMultiTarget && card.getEffects(EffectSlot.SPELL).stream()
                    .anyMatch(CardEffect::canTargetPlayer);
            boolean singleTargetAllowsPlayers = !isMultiTarget;

            if (singleTargetAllowsPlayers || multiTargetAllowsPlayers) {
                for (UUID playerId : gameData.playerIds) {
                    if (isValidPlayerTarget(gameData, card.getTargetFilter(), playerId, controllerId)) {
                        return true;
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            return true;
        }

        if (allowedTargets.contains(TargetType.EXILE)) {
            return true;
        }

        return false;
    }

    /**
     * Checks shroud, hexproof, and CantBeTargetOfSpellsOrAbilitiesEffect on a permanent.
     * Returns true if the permanent is blocked from being targeted by the given controller.
     */
    private boolean isBlockedByHexproofOrGrantedEffect(GameData gameData, Permanent perm, UUID controllerId) {
        // Shroud
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)) {
            return true;
        }

        // Hexproof (only blocks if target is opponent's)
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return true;
            }
        }

        // CantBeTargetOfSpellsOrAbilitiesEffect (granted hexproof-like)
        if (gameQueryService.hasGrantedEffect(gameData, perm, CantBeTargetOfSpellsOrAbilitiesEffect.class)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, perm.getId());
            if (targetController != null && !targetController.equals(controllerId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validates a target filter against a permanent. Returns true if the filter is null or the permanent passes.
     */
    private boolean passesTargetFilter(GameData gameData, TargetFilter filter, Permanent perm, UUID sourceCardId, UUID controllerId) {
        if (filter == null) {
            return true;
        }
        try {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(sourceCardId)
                    .withSourceControllerId(controllerId);
            gameQueryService.validateTargetFilter(filter, perm, filterContext);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private boolean isPlaneswalker(Permanent perm) {
        return perm.getCard().hasType(CardType.PLANESWALKER);
    }
}

package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TargetLegalityService {

    private final GameQueryService gameQueryService;
    private final TargetValidationService targetValidationService;

    public void validateSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter) {
        if (targetId == null) {
            throw new IllegalStateException("Must target a spell on the stack");
        }

        StackEntry targetSpell = findSpellOnStack(gameData, targetId);
        if (targetSpell == null) {
            throw new IllegalStateException("Target must be a spell on the stack");
        }

        if (targetFilter instanceof StackEntryPredicateTargetFilter filter
                && !matchesStackEntryPredicate(targetSpell, filter.predicate())) {
            throw new IllegalStateException(filter.errorMessage());
        }
    }

    public void validateActivatedAbilityTargeting(GameData gameData,
                                                  UUID playerId,
                                                  ActivatedAbility ability,
                                                  List<CardEffect> abilityEffects,
                                                  UUID targetPermanentId,
                                                  Zone targetZone,
                                                  Card sourceCard) {
        targetValidationService.validateEffectTargets(abilityEffects,
                new TargetValidationContext(gameData, targetPermanentId, targetZone, sourceCard));

        if (ability.getTargetFilter() != null && targetPermanentId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
            if (target != null) {
                gameQueryService.validateTargetFilter(ability.getTargetFilter(),
                        target,
                        FilterContext.of(gameData)
                                .withSourceCardId(sourceCard.getId())
                                .withSourceControllerId(playerId));
            }
        }

        validateShroudTargeting(gameData, targetPermanentId);
    }

    public void validateSpellTargeting(GameData gameData, Card card, UUID targetPermanentId, Zone targetZone, UUID controllerId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null && !gameData.playerIds.contains(targetPermanentId)) {
            throw new IllegalStateException("Invalid target");
        }

        if (target != null && card.isNeedsTarget() && gameQueryService.hasProtectionFrom(gameData, target, card.getColor())) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from " + card.getColor().name().toLowerCase());
        }

        if (target != null && card.isNeedsTarget() && gameQueryService.hasKeyword(gameData, target, Keyword.SHROUD)) {
            throw new IllegalStateException(target.getCard().getName() + " has shroud and can't be targeted");
        }

        if (target == null && card.isNeedsTarget() && gameData.playerIds.contains(targetPermanentId)
                && gameQueryService.playerHasShroud(gameData, targetPermanentId)) {
            throw new IllegalStateException(gameData.playerIdToName.get(targetPermanentId) + " has shroud and can't be targeted");
        }

        if (target == null
                && card.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter
                && !matchesPlayerPredicate(controllerId, targetPermanentId, playerFilter.predicate())) {
            throw new IllegalStateException(playerFilter.errorMessage());
        }

        if (card.getTargetFilter() != null && target != null) {
            gameQueryService.validateTargetFilter(card.getTargetFilter(),
                    target,
                    FilterContext.of(gameData)
                            .withSourceCardId(card.getId())
                            .withSourceControllerId(controllerId));
        }

        targetValidationService.validateEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetPermanentId, targetZone, card));
    }

    public void validateEffectTargetInZone(GameData gameData, Card card, UUID targetId, Zone targetZone) {
        targetValidationService.validateEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetId, targetZone, card));
    }

    public void validateMultiSpellTargets(GameData gameData, Card card, List<UUID> targetPermanentIds) {
        if (targetPermanentIds.size() < card.getMinTargets() || targetPermanentIds.size() > card.getMaxTargets()) {
            throw new IllegalStateException("Must target between " + card.getMinTargets() + " and " + card.getMaxTargets() + " targets");
        }
        if (new java.util.HashSet<>(targetPermanentIds).size() != targetPermanentIds.size()) {
            throw new IllegalStateException("All targets must be different");
        }

        boolean multiTargetAllowsPlayers = card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e instanceof DealOrderedDamageToAnyTargetsEffect);
        for (UUID targetId : targetPermanentIds) {
            boolean isPlayerTarget = gameData.playerIds.contains(targetId);
            Permanent target = isPlayerTarget ? null : gameQueryService.findPermanentById(gameData, targetId);
            if (!isPlayerTarget && target == null) {
                throw new IllegalStateException("Invalid target");
            }
            if (isPlayerTarget && !multiTargetAllowsPlayers) {
                throw new IllegalStateException("This spell cannot target players");
            }
            if (!isPlayerTarget) {
                if (!gameQueryService.isCreature(gameData, target)) {
                    throw new IllegalStateException(target.getCard().getName() + " is not a creature");
                }
                if (card.isNeedsTarget() && gameQueryService.hasProtectionFrom(gameData, target, card.getColor())) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from " + card.getColor().name().toLowerCase());
                }
                if (card.isNeedsTarget() && gameQueryService.hasKeyword(gameData, target, Keyword.SHROUD)) {
                    throw new IllegalStateException(target.getCard().getName() + " has shroud and can't be targeted");
                }
            } else if (card.isNeedsTarget() && gameQueryService.playerHasShroud(gameData, targetId)) {
                throw new IllegalStateException(gameData.playerIdToName.get(targetId) + " has shroud and can't be targeted");
            }
        }
    }

    public boolean isTargetIllegalOnResolution(GameData gameData, StackEntry entry) {
        if (entry.isNonTargeting()) {
            return false;
        }

        boolean targetFizzled = false;
        if (entry.getTargetPermanentId() != null) {
            if (entry.getTargetZone() == Zone.GRAVEYARD) {
                targetFizzled = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId()) == null;
            } else if (entry.getTargetZone() == Zone.STACK) {
                targetFizzled = gameData.stack.stream().noneMatch(se -> se.getCard().getId().equals(entry.getTargetPermanentId()));
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
                if (targetPerm == null && !gameData.playerIds.contains(entry.getTargetPermanentId())) {
                    targetFizzled = true;
                } else if (targetPerm != null) {
                    TargetFilter effectiveTargetFilter =
                            entry.getTargetFilter() != null
                                    ? entry.getTargetFilter()
                                    : entry.getCard() != null ? entry.getCard().getTargetFilter() : null;
                    if (effectiveTargetFilter != null) {
                        try {
                            FilterContext filterContext = FilterContext.of(gameData)
                                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                                    .withSourceControllerId(entry.getControllerId());
                            gameQueryService.validateTargetFilter(effectiveTargetFilter, targetPerm, filterContext);
                        } catch (IllegalStateException e) {
                            targetFizzled = true;
                        }
                    }
                }
            }
        }

        if (!targetFizzled && entry.getTargetPermanentIds() != null && !entry.getTargetPermanentIds().isEmpty()) {
            boolean allGone = true;
            for (UUID permId : entry.getTargetPermanentIds()) {
                if (gameQueryService.findPermanentById(gameData, permId) != null || gameData.playerIds.contains(permId)) {
                    allGone = false;
                    break;
                }
            }
            if (allGone) {
                targetFizzled = true;
            }
        }

        if (!targetFizzled && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
            boolean allGone = true;
            for (UUID cardId : entry.getTargetCardIds()) {
                if (gameQueryService.findCardInGraveyardById(gameData, cardId) != null) {
                    allGone = false;
                    break;
                }
            }
            if (allGone) {
                targetFizzled = true;
            }
        }

        return targetFizzled;
    }

    private void validateShroudTargeting(GameData gameData, UUID targetPermanentId) {
        if (targetPermanentId != null) {
            Permanent shroudTarget = gameQueryService.findPermanentById(gameData, targetPermanentId);
            if (shroudTarget != null && gameQueryService.hasKeyword(gameData, shroudTarget, Keyword.SHROUD)) {
                throw new IllegalStateException(shroudTarget.getCard().getName() + " has shroud and can't be targeted");
            }
        }
        if (targetPermanentId != null && gameData.playerIds.contains(targetPermanentId)
                && gameQueryService.playerHasShroud(gameData, targetPermanentId)) {
            throw new IllegalStateException(gameData.playerIdToName.get(targetPermanentId) + " has shroud and can't be targeted");
        }
    }

    private StackEntry findSpellOnStack(GameData gameData, UUID targetId) {
        return gameData.stack.stream()
                .filter(se -> se.getCard().getId().equals(targetId)
                        && se.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                        && se.getEntryType() != StackEntryType.ACTIVATED_ABILITY)
                .findFirst()
                .orElse(null);
    }

    private boolean isSingleTargetSpell(StackEntry stackEntry) {
        return stackEntry.getTargetPermanentId() != null
                && stackEntry.getTargetPermanentIds().isEmpty()
                && stackEntry.getTargetCardIds().isEmpty();
    }

    public boolean matchesStackEntryPredicate(StackEntry stackEntry, StackEntryPredicate predicate) {
        if (predicate instanceof StackEntryTypeInPredicate typeInPredicate) {
            return typeInPredicate.spellTypes().contains(stackEntry.getEntryType());
        }
        if (predicate instanceof StackEntryColorInPredicate colorInPredicate) {
            return colorInPredicate.colors().contains(stackEntry.getCard().getColor());
        }
        if (predicate instanceof StackEntryIsSingleTargetPredicate) {
            return isSingleTargetSpell(stackEntry);
        }
        if (predicate instanceof StackEntryAnyOfPredicate anyOfPredicate) {
            for (StackEntryPredicate nested : anyOfPredicate.predicates()) {
                if (matchesStackEntryPredicate(stackEntry, nested)) {
                    return true;
                }
            }
            return false;
        }
        if (predicate instanceof StackEntryAllOfPredicate allOfPredicate) {
            for (StackEntryPredicate nested : allOfPredicate.predicates()) {
                if (!matchesStackEntryPredicate(stackEntry, nested)) {
                    return false;
                }
            }
            return true;
        }
        if (predicate instanceof StackEntryNotPredicate notPredicate) {
            return !matchesStackEntryPredicate(stackEntry, notPredicate.predicate());
        }
        return false;
    }

    private boolean matchesPlayerPredicate(UUID controllerId, UUID targetPlayerId, PlayerPredicate predicate) {
        if (predicate instanceof PlayerRelationPredicate relationPredicate) {
            if (relationPredicate.relation() == PlayerRelation.SELF) {
                return controllerId != null && controllerId.equals(targetPlayerId);
            }
            if (relationPredicate.relation() == PlayerRelation.OPPONENT) {
                return controllerId != null && !controllerId.equals(targetPlayerId);
            }
        }
        return false;
    }
}


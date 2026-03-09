package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TargetLegalityService {

    private final GameQueryService gameQueryService;
    private final TargetValidationService targetValidationService;

    public void validateSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter, UUID controllerId) {
        if (targetId == null) {
            throw new IllegalStateException("Must target a spell on the stack");
        }

        boolean includeAbilities = containsHasTargetPredicate(targetFilter);
        StackEntry targetSpell = includeAbilities
                ? findAnyEntryOnStack(gameData, targetId)
                : findSpellOnStack(gameData, targetId);
        if (targetSpell == null) {
            throw new IllegalStateException(includeAbilities
                    ? "Target must be a spell or ability on the stack"
                    : "Target must be a spell on the stack");
        }

        if (targetFilter instanceof StackEntryPredicateTargetFilter filter
                && !matchesStackEntryPredicate(gameData, targetSpell, filter.predicate(), controllerId)) {
            throw new IllegalStateException(filter.errorMessage());
        }
    }

    public void validateMultiTargetAbility(GameData gameData, UUID playerId, ActivatedAbility ability, List<UUID> targetPermanentIds, Card sourceCard) {
        validateMultiTargetCount(targetPermanentIds, ability.getMinTargets(), ability.getMaxTargets());

        List<TargetFilter> perPositionFilters = ability.getMultiTargetFilters();
        for (int i = 0; i < targetPermanentIds.size(); i++) {
            UUID targetId = targetPermanentIds.get(i);
            TargetFilter positionFilter = i < perPositionFilters.size() ? perPositionFilters.get(i) : null;

            // Player-targeting position
            if (positionFilter instanceof PlayerPredicateTargetFilter playerFilter) {
                if (!gameData.playerIds.contains(targetId)) {
                    throw new IllegalStateException("Invalid player target");
                }
                validatePlayerTargetable(gameData, targetId);
                validatePlayerPredicate(playerId, targetId, playerFilter.predicate(), playerFilter.errorMessage());
                continue;
            }

            // Permanent-targeting position
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                throw new IllegalStateException("Invalid target");
            }

            validatePermanentTargetable(gameData, target, playerId);

            // Per-position filter
            if (positionFilter != null) {
                gameQueryService.validateTargetFilter(positionFilter, target,
                        FilterContext.of(gameData)
                                .withSourceCardId(sourceCard.getId())
                                .withSourceControllerId(playerId));
            }
        }
    }

    public void validateActivatedAbilityTargeting(GameData gameData,
                                                  UUID playerId,
                                                  ActivatedAbility ability,
                                                  List<CardEffect> abilityEffects,
                                                  UUID targetPermanentId,
                                                  Zone targetZone,
                                                  Card sourceCard,
                                                  int xValue) {
        targetValidationService.validateEffectTargets(abilityEffects,
                new TargetValidationContext(gameData, targetPermanentId, targetZone, sourceCard, xValue));

        if (ability.getTargetFilter() != null && targetPermanentId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
            if (target != null) {
                gameQueryService.validateTargetFilter(ability.getTargetFilter(),
                        target,
                        FilterContext.of(gameData)
                                .withSourceCardId(sourceCard.getId())
                                .withSourceControllerId(playerId));
            } else if (gameData.playerIds.contains(targetPermanentId)
                    && ability.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter) {
                validatePlayerPredicate(playerId, targetPermanentId, playerFilter.predicate(), playerFilter.errorMessage());
            }
        }

        validateTargetable(gameData, targetPermanentId, playerId);
    }

    public void validateSpellTargeting(GameData gameData, Card card, UUID targetPermanentId, Zone targetZone, UUID controllerId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null && !gameData.playerIds.contains(targetPermanentId)) {
            throw new IllegalStateException("Invalid target");
        }

        if (target != null && card.isNeedsTarget()) {
            validateSpellProtections(gameData, target, card);
            validatePermanentTargetable(gameData, target, controllerId);
        }

        if (target == null && card.isNeedsTarget() && gameData.playerIds.contains(targetPermanentId)) {
            validatePlayerTargetable(gameData, targetPermanentId);
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

    public void validateEffectTargetInZone(GameData gameData, Card card, UUID targetId, Zone targetZone, int xValue) {
        targetValidationService.validateEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetId, targetZone, card, xValue));
    }

    public void validateMultiSpellTargets(GameData gameData, Card card, List<UUID> targetPermanentIds, UUID controllerId) {
        validateMultiTargetCount(targetPermanentIds, card.getMinTargets(), card.getMaxTargets());

        boolean multiTargetAllowsPlayers = card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e.canTargetPlayer() && e.canTargetPermanent());
        List<TargetFilter> perPositionFilters = card.getMultiTargetFilters();
        for (int i = 0; i < targetPermanentIds.size(); i++) {
            UUID targetId = targetPermanentIds.get(i);
            boolean isPlayerTarget = gameData.playerIds.contains(targetId);
            Permanent target = isPlayerTarget ? null : gameQueryService.findPermanentById(gameData, targetId);
            if (!isPlayerTarget && target == null) {
                throw new IllegalStateException("Invalid target");
            }
            if (isPlayerTarget && !multiTargetAllowsPlayers) {
                throw new IllegalStateException("This spell cannot target players");
            }
            if (!isPlayerTarget) {
                // When the card has a targetFilter, let it handle type validation;
                // otherwise fall back to requiring a creature target.
                if (card.getTargetFilter() != null) {
                    gameQueryService.validateTargetFilter(card.getTargetFilter(), target,
                            FilterContext.of(gameData)
                                    .withSourceCardId(card.getId())
                                    .withSourceControllerId(controllerId));
                } else if (!gameQueryService.isCreature(gameData, target)) {
                    throw new IllegalStateException(target.getCard().getName() + " is not a creature");
                }
                if (card.isNeedsTarget()) {
                    validateSpellProtections(gameData, target, card);
                    validatePermanentTargetable(gameData, target, controllerId);
                }
                // Apply per-position target filter if available
                if (i < perPositionFilters.size() && perPositionFilters.get(i) != null) {
                    gameQueryService.validateTargetFilter(perPositionFilters.get(i), target,
                            FilterContext.of(gameData)
                                    .withSourceCardId(card.getId())
                                    .withSourceControllerId(controllerId));
                }
            } else if (card.isNeedsTarget()) {
                validatePlayerTargetable(gameData, targetId);
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
                    targetFizzled = isPermanentUntargetableBy(gameData, targetPerm, entry.getControllerId());
                    if (!targetFizzled) {
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

    private boolean isPermanentUntargetableBy(GameData gameData, Permanent target, UUID sourcePlayerId) {
        if (gameQueryService.hasKeyword(gameData, target, Keyword.SHROUD)) {
            return true;
        }
        UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetController != null && !targetController.equals(sourcePlayerId)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.HEXPROOF)) {
                return true;
            }
            if (gameQueryService.hasGrantedEffect(gameData, target, CantBeTargetOfSpellsOrAbilitiesEffect.class)) {
                return true;
            }
        }
        return false;
    }

    private void validatePermanentTargetable(GameData gameData, Permanent target, UUID sourcePlayerId) {
        if (gameQueryService.hasKeyword(gameData, target, Keyword.SHROUD)) {
            throw new IllegalStateException(target.getCard().getName() + " has shroud and can't be targeted");
        }
        UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetController != null && !targetController.equals(sourcePlayerId)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.HEXPROOF)
                    || gameQueryService.hasGrantedEffect(gameData, target, CantBeTargetOfSpellsOrAbilitiesEffect.class)) {
                throw new IllegalStateException(target.getCard().getName() + " has hexproof and can't be targeted");
            }
        }
    }

    private void validatePlayerTargetable(GameData gameData, UUID targetPlayerId) {
        if (gameQueryService.playerHasShroud(gameData, targetPlayerId)) {
            throw new IllegalStateException(gameData.playerIdToName.get(targetPlayerId) + " has shroud and can't be targeted");
        }
    }

    private void validateTargetable(GameData gameData, UUID targetId, UUID sourcePlayerId) {
        if (targetId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            validatePermanentTargetable(gameData, target, sourcePlayerId);
        } else if (gameData.playerIds.contains(targetId)) {
            validatePlayerTargetable(gameData, targetId);
        }
    }

    private void validateSpellProtections(GameData gameData, Permanent target, Card card) {
        if (gameQueryService.hasProtectionFrom(gameData, target, card.getColor())) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from " + card.getColor().name().toLowerCase());
        }
        if (gameQueryService.hasProtectionFromSourceCardTypes(target, card)) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from " + card.getType().getDisplayName().toLowerCase() + "s");
        }
        if (gameQueryService.cantBeTargetedBySpellColor(gameData, target, card.getColor())) {
            throw new IllegalStateException(target.getCard().getName() + " can't be the target of " + card.getColor().name().toLowerCase() + " spells");
        }
    }

    private void validateMultiTargetCount(List<UUID> targetIds, int min, int max) {
        if (targetIds == null || targetIds.size() < min || targetIds.size() > max) {
            throw new IllegalStateException("Must target between " + min + " and " + max + " targets");
        }
        if (new HashSet<>(targetIds).size() != targetIds.size()) {
            throw new IllegalStateException("All targets must be different");
        }
    }

    private void validatePlayerPredicate(UUID controllerId, UUID targetPlayerId, PlayerPredicate predicate, String errorMessage) {
        if (!matchesPlayerPredicate(controllerId, targetPlayerId, predicate)) {
            throw new IllegalStateException(errorMessage);
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

    StackEntry findAnyEntryOnStack(GameData gameData, UUID targetId) {
        return gameData.stack.stream()
                .filter(se -> se.getCard().getId().equals(targetId))
                .findFirst()
                .orElse(null);
    }

    private boolean containsHasTargetPredicate(TargetFilter targetFilter) {
        if (!(targetFilter instanceof StackEntryPredicateTargetFilter filter)) {
            return false;
        }
        return predicateContainsHasTarget(filter.predicate());
    }

    private boolean predicateContainsHasTarget(StackEntryPredicate predicate) {
        if (predicate instanceof StackEntryHasTargetPredicate) {
            return true;
        }
        if (predicate instanceof StackEntryAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(this::predicateContainsHasTarget);
        }
        if (predicate instanceof StackEntryAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(this::predicateContainsHasTarget);
        }
        if (predicate instanceof StackEntryNotPredicate not) {
            return predicateContainsHasTarget(not.predicate());
        }
        return false;
    }

    private boolean isSingleTargetSpell(StackEntry stackEntry) {
        return stackEntry.getTargetPermanentId() != null
                && stackEntry.getTargetPermanentIds().isEmpty()
                && stackEntry.getTargetCardIds().isEmpty();
    }

    private boolean hasAnyTarget(StackEntry stackEntry) {
        return stackEntry.getTargetPermanentId() != null
                || !stackEntry.getTargetPermanentIds().isEmpty()
                || !stackEntry.getTargetCardIds().isEmpty();
    }

    public boolean matchesStackEntryPredicate(GameData gameData, StackEntry stackEntry, StackEntryPredicate predicate, UUID controllerId) {
        if (predicate instanceof StackEntryTypeInPredicate typeInPredicate) {
            return typeInPredicate.spellTypes().contains(stackEntry.getEntryType());
        }
        if (predicate instanceof StackEntryColorInPredicate colorInPredicate) {
            return colorInPredicate.colors().contains(stackEntry.getCard().getColor());
        }
        if (predicate instanceof StackEntryIsSingleTargetPredicate) {
            return isSingleTargetSpell(stackEntry);
        }
        if (predicate instanceof StackEntryHasTargetPredicate) {
            // Matches any spell or ability — per rules (e.g. Spellskite), activation is legal
            // even if the targeted spell/ability has no targets; resolution handles that case.
            return true;
        }
        if (predicate instanceof StackEntryManaValuePredicate manaValuePredicate) {
            return stackEntry.getCard().getManaValue() == manaValuePredicate.manaValue();
        }
        if (predicate instanceof StackEntryTargetsYourPermanentPredicate) {
            return targetsAPermanentControlledBy(gameData, stackEntry, controllerId);
        }
        if (predicate instanceof StackEntryAnyOfPredicate anyOfPredicate) {
            for (StackEntryPredicate nested : anyOfPredicate.predicates()) {
                if (matchesStackEntryPredicate(gameData, stackEntry, nested, controllerId)) {
                    return true;
                }
            }
            return false;
        }
        if (predicate instanceof StackEntryAllOfPredicate allOfPredicate) {
            for (StackEntryPredicate nested : allOfPredicate.predicates()) {
                if (!matchesStackEntryPredicate(gameData, stackEntry, nested, controllerId)) {
                    return false;
                }
            }
            return true;
        }
        if (predicate instanceof StackEntryNotPredicate notPredicate) {
            return !matchesStackEntryPredicate(gameData, stackEntry, notPredicate.predicate(), controllerId);
        }
        return false;
    }

    private boolean targetsAPermanentControlledBy(GameData gameData, StackEntry stackEntry, UUID controllerId) {
        // Check single target
        if (stackEntry.getTargetPermanentId() != null) {
            UUID targetController = gameQueryService.findPermanentController(gameData, stackEntry.getTargetPermanentId());
            if (controllerId.equals(targetController)) {
                return true;
            }
        }
        // Check multiple targets
        if (stackEntry.getTargetPermanentIds() != null) {
            for (UUID targetId : stackEntry.getTargetPermanentIds()) {
                UUID targetController = gameQueryService.findPermanentController(gameData, targetId);
                if (controllerId.equals(targetController)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesPlayerPredicate(UUID controllerId, UUID targetPlayerId, PlayerPredicate predicate) {
        if (predicate instanceof PlayerRelationPredicate relationPredicate) {
            if (relationPredicate.relation() == PlayerRelation.ANY) {
                return true;
            }
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

package com.github.laxika.magicalvibes.service.target;

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
import java.util.function.Predicate;

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
            TargetFilter positionFilter = getPositionFilter(perPositionFilters, i);

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
                        filterContext(gameData, sourceCard.getId(), playerId));
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
                        filterContext(gameData, sourceCard.getId(), playerId));
            } else if (gameData.playerIds.contains(targetPermanentId)
                    && ability.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter) {
                validatePlayerPredicate(playerId, targetPermanentId, playerFilter.predicate(), playerFilter.errorMessage());
            }
        }

        validateTargetable(gameData, targetPermanentId, playerId);
    }

    public void validateSpellTargeting(GameData gameData, Card card, UUID targetPermanentId, Zone targetZone, UUID controllerId) {
        validateSpellTargeting(gameData, card, targetPermanentId, targetZone, controllerId, card.isNeedsTarget());
    }

    public void validateSpellTargeting(GameData gameData, Card card, UUID targetPermanentId, Zone targetZone, UUID controllerId, boolean needsTarget) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null && !gameData.playerIds.contains(targetPermanentId)) {
            throw new IllegalStateException("Invalid target");
        }

        if (target != null && needsTarget) {
            validateSpellProtections(gameData, target, card);
            validatePermanentTargetable(gameData, target, controllerId);
        }

        if (target == null && needsTarget && gameData.playerIds.contains(targetPermanentId)) {
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
                    filterContext(gameData, card.getId(), controllerId));
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

        boolean allowsPlayers = card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e.canTargetPlayer() && e.canTargetPermanent());
        List<TargetFilter> perPositionFilters = card.getMultiTargetFilters();
        for (int i = 0; i < targetPermanentIds.size(); i++) {
            UUID targetId = targetPermanentIds.get(i);

            // Player-targeting position
            if (gameData.playerIds.contains(targetId)) {
                if (!allowsPlayers) {
                    throw new IllegalStateException("This spell cannot target players");
                }
                if (card.isNeedsTarget()) {
                    validatePlayerTargetable(gameData, targetId);
                }
                continue;
            }

            // Permanent-targeting position
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                throw new IllegalStateException("Invalid target");
            }

            // When the card has a targetFilter, let it handle type validation;
            // otherwise fall back to requiring a creature target.
            if (card.getTargetFilter() != null) {
                gameQueryService.validateTargetFilter(card.getTargetFilter(), target,
                        filterContext(gameData, card.getId(), controllerId));
            } else if (!gameQueryService.isCreature(gameData, target)) {
                throw new IllegalStateException(target.getCard().getName() + " is not a creature");
            }

            if (card.isNeedsTarget()) {
                validateSpellProtections(gameData, target, card);
                validatePermanentTargetable(gameData, target, controllerId);
            }

            // Apply per-position target filter if available
            TargetFilter positionFilter = getPositionFilter(perPositionFilters, i);
            if (positionFilter != null) {
                gameQueryService.validateTargetFilter(positionFilter, target,
                        filterContext(gameData, card.getId(), controllerId));
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
                    targetFizzled = untargetableReason(gameData, targetPerm, entry.getControllerId()) != null;
                    if (!targetFizzled) {
                        targetFizzled = isSpellProtected(gameData, targetPerm, entry);
                    }
                    if (!targetFizzled) {
                        TargetFilter effectiveTargetFilter =
                                entry.getTargetFilter() != null
                                        ? entry.getTargetFilter()
                                        : entry.getCard() != null ? entry.getCard().getTargetFilter() : null;
                        if (effectiveTargetFilter != null) {
                            try {
                                gameQueryService.validateTargetFilter(effectiveTargetFilter, targetPerm,
                                        filterContext(gameData,
                                                entry.getCard() != null ? entry.getCard().getId() : null,
                                                entry.getControllerId()));
                            } catch (IllegalStateException e) {
                                targetFizzled = true;
                            }
                        }
                    }
                }
            }
        }

        if (!targetFizzled) {
            targetFizzled = allTargetsGone(entry.getTargetPermanentIds(),
                    id -> gameQueryService.findPermanentById(gameData, id) != null || gameData.playerIds.contains(id));
        }

        if (!targetFizzled) {
            targetFizzled = allTargetsGone(entry.getTargetCardIds(),
                    id -> gameQueryService.findCardInGraveyardById(gameData, id) != null);
        }

        return targetFizzled;
    }

    /**
     * Checks if the target permanent is protected from the resolving spell's color
     * (e.g. via Autumn's Veil or static CantBeTargetedBySpellColorsEffect).
     * Only applies when the entry is a spell (not a triggered/activated ability).
     */
    private boolean isSpellProtected(GameData gameData, Permanent targetPerm, StackEntry entry) {
        if (entry.getCard() == null) return false;
        StackEntryType entryType = entry.getEntryType();
        if (entryType == StackEntryType.TRIGGERED_ABILITY || entryType == StackEntryType.ACTIVATED_ABILITY) {
            return false;
        }
        return gameQueryService.cantBeTargetedBySpellColor(gameData, targetPerm, entry.getCard().getColor());
    }

    private String untargetableReason(GameData gameData, Permanent target, UUID sourcePlayerId) {
        if (gameQueryService.hasKeyword(gameData, target, Keyword.SHROUD)) {
            return target.getCard().getName() + " has shroud and can't be targeted";
        }
        UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetController != null && !targetController.equals(sourcePlayerId)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.HEXPROOF)
                    || gameQueryService.hasGrantedEffect(gameData, target, CantBeTargetOfSpellsOrAbilitiesEffect.class)) {
                return target.getCard().getName() + " has hexproof and can't be targeted";
            }
        }
        return null;
    }

    private void validatePermanentTargetable(GameData gameData, Permanent target, UUID sourcePlayerId) {
        String reason = untargetableReason(gameData, target, sourcePlayerId);
        if (reason != null) {
            throw new IllegalStateException(reason);
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

    private FilterContext filterContext(GameData gameData, UUID sourceCardId, UUID controllerId) {
        return FilterContext.of(gameData)
                .withSourceCardId(sourceCardId)
                .withSourceControllerId(controllerId);
    }

    private TargetFilter getPositionFilter(List<TargetFilter> filters, int index) {
        return index < filters.size() ? filters.get(index) : null;
    }

    private boolean allTargetsGone(List<UUID> ids, Predicate<UUID> existsCheck) {
        return ids != null && !ids.isEmpty() && ids.stream().noneMatch(existsCheck);
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
            return switch (relationPredicate.relation()) {
                case ANY -> true;
                case SELF -> controllerId != null && controllerId.equals(targetPlayerId);
                case OPPONENT -> controllerId != null && !controllerId.equals(targetPlayerId);
            };
        }
        return false;
    }
}

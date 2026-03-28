package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetedByNonColorSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardsFromOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouOrCreatureYouControlPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class TargetLegalityService {

    private final GameQueryService gameQueryService;
    private final TargetValidationService targetValidationService;

    public Optional<String> checkSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter, UUID controllerId) {
        if (targetId == null) {
            return Optional.of("Must target a spell on the stack");
        }

        boolean includeAbilities = containsHasTargetPredicate(targetFilter);
        StackEntry targetSpell = includeAbilities
                ? findAnyEntryOnStack(gameData, targetId)
                : findSpellOnStack(gameData, targetId);
        if (targetSpell == null) {
            return Optional.of(includeAbilities
                    ? "Target must be a spell or ability on the stack"
                    : "Target must be a spell on the stack");
        }

        if (targetFilter instanceof StackEntryPredicateTargetFilter filter
                && !matchesStackEntryPredicate(gameData, targetSpell, filter.predicate(), controllerId)) {
            return Optional.of(filter.errorMessage());
        }

        return Optional.empty();
    }

    public void validateSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter, UUID controllerId) {
        checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    /**
     * Validates that the given graveyard card IDs are legal targets for a multi-target graveyard ability.
     * Each card must exist in an opponent's graveyard (not the controller's).
     */
    public void validateMultiTargetGraveyardAbility(GameData gameData, UUID playerId,
                                                     List<CardEffect> effects, List<UUID> targetCardIds) {
        if (targetCardIds == null || targetCardIds.isEmpty()) {
            throw new IllegalStateException("Must select graveyard targets");
        }
        for (CardEffect effect : effects) {
            if (effect instanceof ExileTargetCardsFromOpponentGraveyardEffect graveyardEffect) {
                if (targetCardIds.size() != graveyardEffect.count()) {
                    throw new IllegalStateException("Must select exactly " + graveyardEffect.count() + " target cards");
                }
                for (UUID cardId : targetCardIds) {
                    Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                    if (card == null) {
                        throw new IllegalStateException("Target card not found in any graveyard");
                    }
                    UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                    if (graveyardOwnerId != null && graveyardOwnerId.equals(playerId)) {
                        throw new IllegalStateException("Target must be in an opponent's graveyard");
                    }
                }
                break;
            }
        }
    }

    public void validateMultiTargetAbility(GameData gameData, UUID playerId, ActivatedAbility ability, List<UUID> targetIds, Card sourceCard) {
        validateMultiTargetCount(targetIds, ability.getMinTargets(), ability.getMaxTargets());

        List<TargetFilter> perPositionFilters = ability.getMultiTargetFilters();
        for (int i = 0; i < targetIds.size(); i++) {
            UUID targetId = targetIds.get(i);
            TargetFilter positionFilter = getPositionFilter(perPositionFilters, i);

            // Player-targeting position
            if (positionFilter instanceof PlayerPredicateTargetFilter playerFilter) {
                if (!gameData.playerIds.contains(targetId)) {
                    throw new IllegalStateException("Invalid player target");
                }
                validatePlayerTargetable(gameData, targetId, playerId);
                validatePlayerPredicate(playerId, targetId, playerFilter.predicate(), playerFilter.errorMessage());
                continue;
            }

            // Permanent-targeting position
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                throw new IllegalStateException("Invalid target");
            }

            validatePermanentTargetable(gameData, target, playerId);
            validateHexproofFromColor(gameData, target, sourceCard, playerId);

            // Can't be targeted by non-color sources (e.g. Gaea's Revenge)
            if (gameQueryService.cantBeTargetedByNonColorSources(gameData, target, sourceCard)) {
                throw new IllegalStateException(nonColorSourceRestrictionMessage(target));
            }

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
                                                  UUID targetId,
                                                  Zone targetZone,
                                                  Card sourceCard,
                                                  int xValue) {
        // "Up to N" abilities (minTargets=0) allow choosing zero targets (CR 115.1d)
        if (ability.getMinTargets() == 0 && targetId == null) {
            return;
        }

        targetValidationService.validateEffectTargets(abilityEffects,
                new TargetValidationContext(gameData, targetId, targetZone, sourceCard, xValue));

        if (ability.getTargetFilter() != null && targetId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                gameQueryService.validateTargetFilter(ability.getTargetFilter(),
                        target,
                        filterContext(gameData, sourceCard.getId(), playerId).withXValue(xValue));
            } else if (gameData.playerIds.contains(targetId)
                    && ability.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter) {
                validatePlayerPredicate(playerId, targetId, playerFilter.predicate(), playerFilter.errorMessage());
            }
        }

        validateTargetable(gameData, targetId, playerId);

        // Can't be the target of opponents' abilities (e.g. Shanna, Sisay's Legacy)
        if (targetId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null && gameQueryService.cantBeTargetOfOpponentAbilities(gameData, target)) {
                UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
                if (targetController != null && !targetController.equals(playerId)) {
                    throw new IllegalStateException(target.getCard().getName()
                            + " can't be the target of abilities opponents control");
                }
            }
        }

        // Hexproof from color (blocks opponent's abilities of the specified color)
        if (targetId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                validateHexproofFromColor(gameData, target, sourceCard, playerId);
            }
        }

        // Can't be targeted by non-color sources (e.g. Gaea's Revenge)
        if (targetId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null && gameQueryService.cantBeTargetedByNonColorSources(gameData, target, sourceCard)) {
                throw new IllegalStateException(nonColorSourceRestrictionMessage(target));
            }
        }
    }

    public void validateSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone, UUID controllerId) {
        checkSpellTargeting(gameData, card, targetId, targetZone, controllerId, EffectResolution.needsTarget(card))
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public void validateSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone, UUID controllerId, boolean needsTarget) {
        checkSpellTargeting(gameData, card, targetId, targetZone, controllerId, needsTarget, 0)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public void validateSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone, UUID controllerId, boolean needsTarget, int xValue) {
        checkSpellTargeting(gameData, card, targetId, targetZone, controllerId, needsTarget, xValue)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public Optional<String> checkSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone, UUID controllerId) {
        return checkSpellTargeting(gameData, card, targetId, targetZone, controllerId, EffectResolution.needsTarget(card), 0);
    }

    private Optional<String> checkSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone, UUID controllerId, boolean needsTarget) {
        return checkSpellTargeting(gameData, card, targetId, targetZone, controllerId, needsTarget, 0);
    }

    private Optional<String> checkSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone, UUID controllerId, boolean needsTarget, int xValue) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null && !gameData.playerIds.contains(targetId)) {
            return Optional.of("Invalid target");
        }

        if (needsTarget) {
            Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(card);

            if (target != null && !allowedTargets.contains(TargetType.PERMANENT)) {
                return Optional.of("This spell can only target players");
            }
            if (target == null && gameData.playerIds.contains(targetId) && !allowedTargets.contains(TargetType.PLAYER)) {
                return Optional.of("This spell cannot target players");
            }
        }

        if (target != null && needsTarget) {
            String protectionReason = checkSpellProtection(gameData, target, card, controllerId);
            if (protectionReason != null) return Optional.of(protectionReason);

            String untargetable = untargetableReason(gameData, target, controllerId);
            if (untargetable != null) return Optional.of(untargetable);
        }

        if (target == null && needsTarget && gameData.playerIds.contains(targetId)) {
            String playerReason = checkPlayerUntargetableReason(gameData, targetId, controllerId);
            if (playerReason != null) return Optional.of(playerReason);
        }

        if (target == null
                && card.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter
                && !matchesPlayerPredicate(controllerId, targetId, playerFilter.predicate())) {
            return Optional.of(playerFilter.errorMessage());
        }

        if (card.getTargetFilter() != null && target != null) {
            Optional<String> filterReason = gameQueryService.checkTargetFilter(card.getTargetFilter(),
                    target,
                    filterContext(gameData, card.getId(), controllerId).withXValue(xValue));
            if (filterReason.isPresent()) return filterReason;
        }

        Optional<String> effectReason = targetValidationService.checkEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetId, targetZone, card));
        if (effectReason.isPresent()) return effectReason;

        return Optional.empty();
    }

    public void validateEffectTargetInZone(GameData gameData, Card card, UUID targetId, Zone targetZone) {
        targetValidationService.validateEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetId, targetZone, card));
    }

    public void validateEffectTargetInZone(GameData gameData, Card card, UUID targetId, Zone targetZone, int xValue) {
        targetValidationService.validateEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetId, targetZone, card, xValue));
    }

    /**
     * Validates only the graveyard-targeting effects of a spell, ignoring permanent-targeting effects.
     * Used for spells with mixed graveyard + permanent targets (e.g. Yawgmoth's Vile Offering)
     * where each target type is validated separately.
     */
    public void validateGraveyardEffectTargetOnly(GameData gameData, Card card, UUID targetId) {
        List<CardEffect> graveyardEffects = card.getEffects(EffectSlot.SPELL).stream()
                .filter(CardEffect::canTargetGraveyard)
                .toList();
        targetValidationService.validateEffectTargets(graveyardEffects,
                new TargetValidationContext(gameData, targetId, Zone.GRAVEYARD, card));
    }

    public void validateMultiSpellTargets(GameData gameData, Card card, List<UUID> targetIds, UUID controllerId) {
        validateMultiTargetCount(targetIds, card.getMinTargets(), card.getMaxTargets(), card.getSpellTargets());

        List<TargetFilter> perPositionFilters = card.getMultiTargetFilters();
        for (int i = 0; i < targetIds.size(); i++) {
            UUID targetId = targetIds.get(i);

            // Player-targeting position
            if (gameData.playerIds.contains(targetId)) {
                if (!card.doesPositionAllowPlayerTargets(i)) {
                    throw new IllegalStateException("This spell cannot target players");
                }
                if (EffectResolution.needsTarget(card)) {
                    validatePlayerTargetable(gameData, targetId, controllerId);
                }
                continue;
            }

            // Permanent-targeting position
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                throw new IllegalStateException("Invalid target");
            }

            // Apply per-position target filter if available; otherwise fall back to
            // the card-level targetFilter, or require a creature target as default.
            TargetFilter positionFilter = getPositionFilter(perPositionFilters, i);
            if (positionFilter != null) {
                gameQueryService.validateTargetFilter(positionFilter, target,
                        filterContext(gameData, card.getId(), controllerId));
            } else if (card.getTargetFilter() != null) {
                gameQueryService.validateTargetFilter(card.getTargetFilter(), target,
                        filterContext(gameData, card.getId(), controllerId));
            } else if (!gameQueryService.isCreature(gameData, target)) {
                throw new IllegalStateException(target.getCard().getName() + " is not a creature");
            }

            if (EffectResolution.needsTarget(card)) {
                validateSpellProtections(gameData, target, card);
                validateHexproofFromColor(gameData, target, card, controllerId);
                validatePermanentTargetable(gameData, target, controllerId);
            }
        }
    }

    public boolean isTargetIllegalOnResolution(GameData gameData, StackEntry entry) {
        if (entry.isNonTargeting()) {
            return false;
        }

        // Multi-zone targeting: spell targets both a spell on the stack and permanent(s)
        // (e.g. Lost in the Mist). Per MTG CR 608.2b: fizzles only when ALL targets become illegal.
        if (entry.getTargetId() != null && entry.getTargetZone() == Zone.STACK
                && !entry.getTargetIds().isEmpty()) {
            boolean spellTargetLegal = gameData.stack.stream()
                    .anyMatch(se -> se.getCard().getId().equals(entry.getTargetId()));
            boolean anyPermanentTargetLegal = entry.getTargetIds().stream()
                    .anyMatch(id -> gameQueryService.findPermanentById(gameData, id) != null
                            || gameData.playerIds.contains(id));
            return !spellTargetLegal && !anyPermanentTargetLegal;
        }

        boolean targetFizzled = false;
        if (entry.getTargetId() != null) {
            if (entry.getTargetZone() == Zone.EXILE) {
                targetFizzled = gameQueryService.findCardInExileById(gameData, entry.getTargetId()) == null;
            } else if (entry.getTargetZone() == Zone.GRAVEYARD) {
                targetFizzled = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId()) == null;
            } else if (entry.getTargetZone() == Zone.STACK) {
                targetFizzled = gameData.stack.stream().noneMatch(se -> se.getCard().getId().equals(entry.getTargetId()));
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (targetPerm == null && !gameData.playerIds.contains(entry.getTargetId())) {
                    targetFizzled = true;
                } else if (targetPerm == null && gameData.playerIds.contains(entry.getTargetId())) {
                    // Player target: check hexproof/shroud at resolution time
                    String playerReason = checkPlayerUntargetableReason(gameData, entry.getTargetId(), entry.getControllerId());
                    if (playerReason != null) {
                        targetFizzled = true;
                    }
                } else if (targetPerm != null) {
                    targetFizzled = untargetableReason(gameData, targetPerm, entry.getControllerId()) != null;
                    if (!targetFizzled
                            && (entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)) {
                        targetFizzled = isBlockedByOpponentAbilityRestriction(gameData, targetPerm, entry.getControllerId());
                    }
                    if (!targetFizzled) {
                        targetFizzled = isSpellProtected(gameData, targetPerm, entry);
                    }
                    if (!targetFizzled) {
                        targetFizzled = isHexproofFromColorBlocked(gameData, targetPerm, entry);
                    }
                    if (!targetFizzled) {
                        targetFizzled = isNonColorSourceRestricted(gameData, targetPerm, entry);
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
                                                entry.getControllerId()).withXValue(entry.getXValue()));
                            } catch (IllegalStateException e) {
                                targetFizzled = true;
                            }
                        }
                    }
                }
            }
        }

        // Multi-target spell with both targetId and targetIds (e.g., kicked spells with additional targets).
        // Per MTG CR 608.2b: fizzle only when ALL targets become illegal.
        if (targetFizzled && entry.getTargetId() != null && !entry.getTargetIds().isEmpty()) {
            boolean anySecondaryTargetLegal = entry.getTargetIds().stream()
                    .anyMatch(id -> gameQueryService.findPermanentById(gameData, id) != null
                            || gameData.playerIds.contains(id));
            if (anySecondaryTargetLegal) {
                targetFizzled = false;
            }
        }

        if (!targetFizzled) {
            boolean allSecondaryGone = allTargetsGone(entry.getTargetIds(),
                    id -> gameQueryService.findPermanentById(gameData, id) != null || gameData.playerIds.contains(id));
            // If targetId is still valid, don't fizzle just because targetIds are gone
            if (allSecondaryGone && entry.getTargetId() != null) {
                Permanent primaryTarget = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                boolean primaryStillLegal = primaryTarget != null || gameData.playerIds.contains(entry.getTargetId());
                // Also check graveyard for graveyard-zone primary targets (e.g. Yawgmoth's Vile Offering)
                if (!primaryStillLegal && entry.getTargetZone() == Zone.GRAVEYARD) {
                    primaryStillLegal = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId()) != null;
                }
                targetFizzled = !primaryStillLegal;
            } else {
                targetFizzled = allSecondaryGone;
            }
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

    private boolean isNonColorSourceRestricted(GameData gameData, Permanent targetPerm, StackEntry entry) {
        if (entry.getCard() == null) return false;
        return gameQueryService.cantBeTargetedByNonColorSources(gameData, targetPerm, entry.getCard());
    }

    private String nonColorSourceRestrictionMessage(Permanent target) {
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeTargetedByNonColorSourcesEffect r) {
                return target.getCard().getName() + " can't be the target of non-"
                        + r.allowedColor().name().toLowerCase() + " spells or abilities";
            }
        }
        return target.getCard().getName() + " can't be targeted by this source";
    }

    private boolean isHexproofFromColorBlocked(GameData gameData, Permanent targetPerm, StackEntry entry) {
        if (entry.getCard() == null) return false;
        var sourceColor = entry.getCard().getColor();
        if (sourceColor == null) return false;
        if (!gameQueryService.hasHexproofFromColor(gameData, targetPerm, sourceColor)) return false;
        UUID targetController = gameQueryService.findPermanentController(gameData, targetPerm.getId());
        return targetController != null && !targetController.equals(entry.getControllerId());
    }

    private void validateHexproofFromColor(GameData gameData, Permanent target, Card sourceCard, UUID sourcePlayerId) {
        if (sourceCard == null) return;
        var sourceColor = sourceCard.getColor();
        if (sourceColor == null) return;
        if (gameQueryService.hasHexproofFromColor(gameData, target, sourceColor)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
            if (targetController != null && !targetController.equals(sourcePlayerId)) {
                throw new IllegalStateException(target.getCard().getName()
                        + " has hexproof from " + sourceColor.name().toLowerCase());
            }
        }
    }

    private String hexproofFromColorReason(GameData gameData, Permanent target, Card sourceCard, UUID sourcePlayerId) {
        if (sourceCard == null) return null;
        var sourceColor = sourceCard.getColor();
        if (sourceColor == null) return null;
        if (gameQueryService.hasHexproofFromColor(gameData, target, sourceColor)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
            if (targetController != null && !targetController.equals(sourcePlayerId)) {
                return target.getCard().getName() + " has hexproof from " + sourceColor.name().toLowerCase();
            }
        }
        return null;
    }

    private boolean isBlockedByOpponentAbilityRestriction(GameData gameData, Permanent target, UUID sourcePlayerId) {
        if (gameQueryService.cantBeTargetOfOpponentAbilities(gameData, target)) {
            UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
            return targetController != null && !targetController.equals(sourcePlayerId);
        }
        return false;
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

    private String checkPlayerUntargetableReason(GameData gameData, UUID targetPlayerId, UUID sourcePlayerId) {
        if (gameQueryService.playerHasShroud(gameData, targetPlayerId)) {
            return gameData.playerIdToName.get(targetPlayerId) + " has shroud and can't be targeted";
        }
        if (sourcePlayerId != null && !sourcePlayerId.equals(targetPlayerId)
                && gameQueryService.playerHasHexproof(gameData, targetPlayerId)) {
            return gameData.playerIdToName.get(targetPlayerId) + " has hexproof and can't be targeted";
        }
        return null;
    }

    private void validatePlayerTargetable(GameData gameData, UUID targetPlayerId, UUID sourcePlayerId) {
        String reason = checkPlayerUntargetableReason(gameData, targetPlayerId, sourcePlayerId);
        if (reason != null) {
            throw new IllegalStateException(reason);
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
            validatePlayerTargetable(gameData, targetId, sourcePlayerId);
        }
    }

    private String checkSpellProtection(GameData gameData, Permanent target, Card card) {
        return checkSpellProtection(gameData, target, card, null);
    }

    private String checkSpellProtection(GameData gameData, Permanent target, Card card, UUID sourcePlayerId) {
        if (gameQueryService.hasProtectionFrom(gameData, target, card.getColor())) {
            return target.getCard().getName() + " has protection from " + card.getColor().name().toLowerCase();
        }
        if (gameQueryService.hasProtectionFromSourceCardTypes(target, card)) {
            return target.getCard().getName() + " has protection from " + card.getType().getDisplayName().toLowerCase() + "s";
        }
        if (gameQueryService.hasProtectionFromSourceSubtypes(target, card)) {
            return target.getCard().getName() + " has protection from source's subtype";
        }
        if (gameQueryService.cantBeTargetedBySpellColor(gameData, target, card.getColor())) {
            return target.getCard().getName() + " can't be the target of " + card.getColor().name().toLowerCase() + " spells";
        }
        if (gameQueryService.cantBeTargetedByNonColorSources(gameData, target, card)) {
            return nonColorSourceRestrictionMessage(target);
        }
        if (sourcePlayerId != null) {
            String hexReason = hexproofFromColorReason(gameData, target, card, sourcePlayerId);
            if (hexReason != null) return hexReason;
        }
        return null;
    }

    private void validateSpellProtections(GameData gameData, Permanent target, Card card) {
        String reason = checkSpellProtection(gameData, target, card);
        if (reason != null) {
            throw new IllegalStateException(reason);
        }
    }

    private void validateMultiTargetCount(List<UUID> targetIds, int min, int max) {
        validateMultiTargetCount(targetIds, min, max, null);
    }

    /**
     * Validates target count and uniqueness. Per MTG rule 114.6c, the same permanent
     * can be chosen for different targeting instances (target groups), but targets
     * within the same group must be unique.
     */
    private void validateMultiTargetCount(List<UUID> targetIds, int min, int max, List<SpellTarget> spellTargets) {
        if (targetIds == null || targetIds.size() < min || targetIds.size() > max) {
            throw new IllegalStateException("Must target between " + min + " and " + max + " targets");
        }
        if (spellTargets != null && spellTargets.size() > 1) {
            // Multi-group: check uniqueness within each group, not across groups (CR 114.6c)
            int consumed = 0;
            for (SpellTarget group : spellTargets) {
                int groupSize = Math.min(group.getMaxTargets(), targetIds.size() - consumed);
                List<UUID> groupTargets = targetIds.subList(consumed, consumed + groupSize);
                if (new HashSet<>(groupTargets).size() != groupTargets.size()) {
                    throw new IllegalStateException("All targets must be different");
                }
                consumed += groupSize;
            }
        } else {
            // Single group or no group info: check global uniqueness
            if (new HashSet<>(targetIds).size() != targetIds.size()) {
                throw new IllegalStateException("All targets must be different");
            }
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

    public Optional<String> checkGraveyardRetargetCandidate(GameData gameData, Card spellCard, UUID candidateTargetId, UUID spellControllerId) {
        if (gameQueryService.findCardInGraveyardById(gameData, candidateTargetId) == null) {
            return Optional.of("Target card is not in any graveyard");
        }
        ReturnCardFromGraveyardEffect graveyardEffect = spellCard.getEffects(EffectSlot.SPELL)
                .stream()
                .filter(e -> e instanceof ReturnCardFromGraveyardEffect)
                .findFirst()
                .map(e -> (ReturnCardFromGraveyardEffect) e)
                .orElse(null);
        if (graveyardEffect != null && graveyardEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
            boolean inControllersGraveyard = gameData.playerGraveyards
                    .getOrDefault(spellControllerId, List.of())
                    .stream()
                    .anyMatch(c -> c.getId().equals(candidateTargetId));
            if (!inControllersGraveyard) {
                return Optional.of("Target card is not in controller's graveyard");
            }
        }
        Optional<String> effectReason = targetValidationService.checkEffectTargets(spellCard.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, candidateTargetId, Zone.GRAVEYARD, spellCard));
        if (effectReason.isPresent()) return effectReason;

        return Optional.empty();
    }

    public void validateGraveyardRetargetCandidate(GameData gameData, Card spellCard, UUID candidateTargetId, UUID spellControllerId) {
        checkGraveyardRetargetCandidate(gameData, spellCard, candidateTargetId, spellControllerId)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public boolean matchesStackEntryPredicate(GameData gameData, StackEntry stackEntry, StackEntryPredicate predicate, UUID controllerId) {
        if (predicate instanceof StackEntryTypeInPredicate typeInPredicate) {
            return typeInPredicate.spellTypes().contains(stackEntry.getEntryType());
        }
        if (predicate instanceof StackEntryColorInPredicate colorInPredicate) {
            return colorInPredicate.colors().contains(stackEntry.getCard().getColor());
        }
        if (predicate instanceof StackEntryIsSingleTargetPredicate) {
            return stackEntry.isSingleTarget();
        }
        if (predicate instanceof StackEntryHasTargetPredicate) {
            // Matches any spell or ability — per rules (e.g. Spellskite), activation is legal
            // even if the targeted spell/ability has no targets; resolution handles that case.
            return true;
        }
        if (predicate instanceof StackEntryManaValuePredicate manaValuePredicate) {
            return stackEntry.getCard().getManaValue() == manaValuePredicate.manaValue();
        }
        if (predicate instanceof StackEntryControlledByPredicate) {
            return stackEntry.getControllerId().equals(controllerId);
        }
        if (predicate instanceof StackEntryTargetsYourPermanentPredicate) {
            return targetsAPermanentControlledBy(gameData, stackEntry, controllerId);
        }
        if (predicate instanceof StackEntryTargetsYouOrCreatureYouControlPredicate) {
            return targetsPlayerOrCreatureControlledBy(gameData, stackEntry, controllerId);
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
        if (stackEntry.getTargetId() != null) {
            UUID targetController = gameQueryService.findPermanentController(gameData, stackEntry.getTargetId());
            if (controllerId.equals(targetController)) {
                return true;
            }
        }
        // Check multiple targets
        if (stackEntry.getTargetIds() != null) {
            for (UUID targetId : stackEntry.getTargetIds()) {
                UUID targetController = gameQueryService.findPermanentController(gameData, targetId);
                if (controllerId.equals(targetController)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean targetsPlayerOrCreatureControlledBy(GameData gameData, StackEntry stackEntry, UUID controllerId) {
        // Check single target
        if (stackEntry.getTargetId() != null) {
            if (targetsPlayerOrCreature(gameData, stackEntry.getTargetId(), controllerId)) {
                return true;
            }
        }
        // Check multiple targets
        if (stackEntry.getTargetIds() != null) {
            for (UUID targetId : stackEntry.getTargetIds()) {
                if (targetsPlayerOrCreature(gameData, targetId, controllerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean targetsPlayerOrCreature(GameData gameData, UUID targetId, UUID controllerId) {
        // "targets you" — the target is the player themselves
        if (targetId.equals(controllerId)) {
            return true;
        }
        // "targets a creature you control"
        UUID targetController = gameQueryService.findPermanentController(gameData, targetId);
        if (controllerId.equals(targetController)) {
            Permanent perm = gameQueryService.findPermanentById(gameData, targetId);
            if (perm != null && gameQueryService.isCreature(gameData, perm)) {
                return true;
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

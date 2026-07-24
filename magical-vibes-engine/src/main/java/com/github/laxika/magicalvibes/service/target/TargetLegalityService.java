package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.TargetColorMode;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.AttackCounterMoveEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesChosenNameWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouOrCreatureYouControlPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetValidationService targetValidationService;

    public Optional<String> checkSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter, UUID controllerId) {
        return checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, null, null);
    }

    /**
     * Same as {@link #checkSpellTargetOnStack(GameData, UUID, TargetFilter, UUID)} but with the source
     * permanent supplied, which source-dependent predicates (e.g. "with the chosen name") require.
     */
    public Optional<String> checkSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter,
                                                    UUID controllerId, Permanent source) {
        return checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, source, null);
    }

    /**
     * Same as above but with the casting spell's chosen X, which X-relative predicates
     * (e.g. "counter target spell with mana value X" — Spell Blast) require. A {@code null}
     * {@code xValue} means X is not yet known (target enumeration) and such predicates match
     * permissively.
     */
    public Optional<String> checkSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter,
                                                    UUID controllerId, Permanent source, Integer xValue) {
        if (targetId == null) {
            return Optional.of("Must target a spell on the stack");
        }

        boolean includeAbilities = filterAdmitsAbilityTarget(targetFilter);
        StackEntry targetSpell = includeAbilities
                ? findAnyEntryOnStack(gameData, targetId)
                : findSpellOnStack(gameData, targetId);
        if (targetSpell == null) {
            return Optional.of(includeAbilities
                    ? "Target must be a spell or ability on the stack"
                    : "Target must be a spell on the stack");
        }

        if (targetFilter instanceof StackEntryPredicateTargetFilter filter
                && !matchesStackEntryPredicate(gameData, targetSpell, filter.predicate(), controllerId, source, xValue)) {
            return Optional.of(filter.errorMessage());
        }

        return Optional.empty();
    }

    public void validateSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter, UUID controllerId) {
        validateSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, null);
    }

    public void validateSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter, UUID controllerId, Permanent source) {
        checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, source)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public void validateSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter, UUID controllerId, int xValue) {
        checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, null, xValue)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    /**
     * Validates a spell that targets multiple distinct spells on the stack, each with its own
     * per-position filter (e.g. Choreographed Sparks' "both" mode: one instant/sorcery spell and
     * one creature spell). Targets must be distinct and each must satisfy its position's filter.
     *
     * <p>The chosen count must fall within {@code [card.getMinTargets(), perPositionFilters.size()]}.
     * A modal "both" mode (two required 1..1 groups) has {@code min == max}, so it still demands exactly
     * that many; a non-modal "counter up to N target spells" (Double Negative: one 0..N group) has
     * {@code min == 0}, so 0..N targets are legal.</p>
     */
    public void validateMultiSpellTargetsOnStack(GameData gameData, Card card, List<UUID> targetIds, UUID controllerId) {
        List<TargetFilter> perPositionFilters = card.getMultiTargetFilters();
        int maxTargets = perPositionFilters.size();
        int minTargets = card.getMinTargets();
        if (targetIds == null || targetIds.size() < minTargets || targetIds.size() > maxTargets) {
            throw new IllegalStateException(minTargets == maxTargets
                    ? "Must choose " + maxTargets + " target spells"
                    : "Must choose up to " + maxTargets + " target spells");
        }
        for (int i = 0; i < targetIds.size(); i++) {
            for (int j = i + 1; j < targetIds.size(); j++) {
                if (targetIds.get(i).equals(targetIds.get(j))) {
                    throw new IllegalStateException("Targets must be different spells");
                }
            }
        }
        for (int i = 0; i < targetIds.size(); i++) {
            validateSpellTargetOnStack(gameData, targetIds.get(i), perPositionFilters.get(i), controllerId);
        }
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
            if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
                for (UUID cardId : targetCardIds) {
                    Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                    if (card == null) {
                        throw new IllegalStateException("Target card not found in any graveyard");
                    }
                    if (returnEffect.filter() != null
                            && !predicateEvaluationService.matchesCardPredicate(card, returnEffect.filter(), null)) {
                        throw new IllegalStateException("Target card must be a "
                                + CardPredicateUtils.describeFilter(returnEffect.filter()));
                    }
                    if (returnEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
                        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                        if (graveyardOwnerId != null && !graveyardOwnerId.equals(playerId)) {
                            throw new IllegalStateException("Target must be in your graveyard");
                        }
                    }
                }
                break;
            }
            if (effect instanceof ExileGraveyardCardsEffect graveyardEffect
                    && graveyardEffect.scope() == GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD) {
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
            if (effect instanceof ExileCardsFromGraveyardEffect exileEffect) {
                // "Exile up to N target cards from graveyards" (e.g. Faerie Macabre) — any graveyard,
                // no more than N distinct targets, each still present in a graveyard.
                if (targetCardIds.size() > exileEffect.maxTargets()) {
                    throw new IllegalStateException("Cannot target more than " + exileEffect.maxTargets() + " cards");
                }
                if (new HashSet<>(targetCardIds).size() != targetCardIds.size()) {
                    throw new IllegalStateException("Cannot target the same card twice");
                }
                for (UUID cardId : targetCardIds) {
                    if (gameQueryService.findCardInGraveyardById(gameData, cardId) == null) {
                        throw new IllegalStateException("Target card not found in any graveyard");
                    }
                }
                break;
            }
            if (effect instanceof ExileTargetCardFromGraveyardAndCreateTokenCopyEffect exileCopy) {
                if (targetCardIds.size() != 1) {
                    throw new IllegalStateException("Must select exactly 1 target card");
                }
                UUID cardId = targetCardIds.getFirst();
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card == null) {
                    throw new IllegalStateException("Target card not found in any graveyard");
                }
                if (exileCopy.filter() != null
                        && !predicateEvaluationService.matchesCardPredicate(card, exileCopy.filter(), null)) {
                    throw new IllegalStateException("Target card must be a "
                            + CardPredicateUtils.describeFilter(exileCopy.filter()));
                }
                if (exileCopy.ownGraveyardOnly()) {
                    UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                    if (graveyardOwnerId != null && !graveyardOwnerId.equals(playerId)) {
                        throw new IllegalStateException("Target must be in your graveyard");
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
            if (positionFilter == null) {
                positionFilter = ability.getTargetFilter();
            }

            // Player-targeting position
            if (positionFilter instanceof PlayerPredicateTargetFilter playerFilter) {
                if (!gameData.playerIds.contains(targetId)) {
                    throw new IllegalStateException("Invalid player target");
                }
                validatePlayerTargetable(gameData, targetId, playerId);
                validatePlayerPredicate(gameData, playerId, targetId, playerFilter.predicate(), playerFilter.errorMessage());
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
                predicateEvaluationService.validateTargetFilter(positionFilter, target,
                        filterContext(gameData, sourceCard.getId(), playerId));
            }
        }

        validateMultiTargetConstraint(gameData, ability.getMultiTargetConstraint(), targetIds);
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
                predicateEvaluationService.validateTargetFilter(ability.getTargetFilter(),
                        target,
                        filterContext(gameData, sourceCard.getId(), playerId).withXValue(xValue));
            } else if (gameData.playerIds.contains(targetId)
                    && ability.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter) {
                validatePlayerPredicate(gameData, playerId, targetId, playerFilter.predicate(), playerFilter.errorMessage());
            } else if (gameData.playerIds.contains(targetId)
                    && ability.getTargetFilter() instanceof AnyTargetPredicateTargetFilter anyFilter) {
                validatePlayerPredicate(gameData, playerId, targetId, anyFilter.playerPredicate(), anyFilter.errorMessage());
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
            // Skip target-type validation for modal spells: their modes have already been
            // unwrapped by SpellCastingService and the mode-specific effects/filters handle
            // validation downstream.  computeAllowedTargets(card) uses the raw (unresolved)
            // ChooseOneEffect which doesn't expose inner target types.
            boolean isModal = card.getEffects(EffectSlot.SPELL).stream()
                    .anyMatch(ChooseOneEffect.class::isInstance);
            if (!isModal) {
                Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(card);

                if (target != null && !allowedTargets.contains(TargetType.PERMANENT)) {
                    return Optional.of("This spell can only target players");
                }
                if (target == null && gameData.playerIds.contains(targetId) && !allowedTargets.contains(TargetType.PLAYER)) {
                    return Optional.of("This spell cannot target players");
                }
            }
        }

        if (target != null && needsTarget) {
            Optional<String> structuralReason = checkSpellPermanentTargetableReason(gameData, target, card, controllerId);
            if (structuralReason.isPresent()) return structuralReason;
        }

        if (target == null && needsTarget && gameData.playerIds.contains(targetId)) {
            String playerReason = checkPlayerUntargetableReason(gameData, targetId, controllerId);
            if (playerReason != null) return Optional.of(playerReason);
            if (card != null && card.getColor() != null
                    && gameQueryService.playerHasProtectionFromColor(gameData, targetId, card.getColor())) {
                return Optional.of(gameData.playerIdToName.get(targetId)
                        + " has protection from " + card.getColor().name().toLowerCase());
            }
            if (card != null
                    && gameQueryService.playerHasProtectionFromChosenName(gameData, targetId, card.getName())) {
                return Optional.of(gameData.playerIdToName.get(targetId)
                        + " has protection from " + card.getName());
            }
        }

        if (target == null
                && card.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter
                && !matchesPlayerPredicate(gameData, controllerId, targetId, playerFilter.predicate())) {
            return Optional.of(playerFilter.errorMessage());
        }

        if (target == null
                && card.getTargetFilter() instanceof AnyTargetPredicateTargetFilter anyFilter
                && !matchesPlayerPredicate(gameData, controllerId, targetId, anyFilter.playerPredicate())) {
            return Optional.of(anyFilter.errorMessage());
        }

        if (card.getTargetFilter() != null && target != null) {
            Optional<String> filterReason = predicateEvaluationService.checkTargetFilter(card.getTargetFilter(),
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
     * Validates a graveyard/exile target against an explicit effect list rather than the card's raw
     * SPELL slot. Used by modal spells, whose raw SPELL slot holds only the {@code ChooseOneEffect};
     * the chosen mode's unwrapped effects (and their target filters) must be validated instead.
     */
    public void validateEffectTargetInZone(GameData gameData, Card card, List<CardEffect> effects,
                                           UUID targetId, Zone targetZone) {
        targetValidationService.validateEffectTargets(effects,
                new TargetValidationContext(gameData, targetId, targetZone, card));
    }

    public void validateEffectTargetInZone(GameData gameData, Card card, List<CardEffect> effects,
                                           UUID targetId, Zone targetZone, int xValue) {
        targetValidationService.validateEffectTargets(effects,
                new TargetValidationContext(gameData, targetId, targetZone, card, xValue));
    }

    /**
     * Validates only the graveyard-targeting effects of a spell, ignoring permanent-targeting effects.
     * Used for spells with mixed graveyard + permanent targets (e.g. Yawgmoth's Vile Offering)
     * where each target type is validated separately.
     */
    public void validateGraveyardEffectTargetOnly(GameData gameData, Card card, UUID targetId) {
        List<CardEffect> graveyardEffects = card.getEffects(EffectSlot.SPELL).stream()
                .filter(e -> e.targetSpec().category().isGraveyard())
                // Unwrap conditional reanimation (e.g. Torrent of Souls' "if {B} was spent") so the
                // inner effect's card-type filter is enforced when the graveyard target is chosen.
                .map(e -> e instanceof ConditionalEffect conditional ? conditional.wrapped() : e)
                .toList();
        targetValidationService.validateEffectTargets(graveyardEffects,
                new TargetValidationContext(gameData, targetId, Zone.GRAVEYARD, card));
    }

    public void validateMultiSpellTargets(GameData gameData, Card card, List<UUID> targetIds, UUID controllerId) {
        validateMultiSpellTargets(gameData, card, targetIds, controllerId, 0);
    }

    public void validateMultiSpellTargets(GameData gameData, Card card, List<UUID> targetIds, UUID controllerId, int xValue) {
        validateMultiTargetCount(targetIds, card.getEffectiveMinTargets(xValue), card.getEffectiveMaxTargets(xValue),
                card.getSpellTargets(), card.isAllowSharedTargets());

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
                predicateEvaluationService.validateTargetFilter(positionFilter, target,
                        filterContext(gameData, card.getId(), controllerId));
            } else if (card.getTargetFilter() != null) {
                predicateEvaluationService.validateTargetFilter(card.getTargetFilter(), target,
                        filterContext(gameData, card.getId(), controllerId));
            } else if (!gameQueryService.isCreature(gameData, target)) {
                throw new IllegalStateException(target.getCard().getName() + " is not a creature");
            }

            if (EffectResolution.needsTarget(card)) {
                checkSpellPermanentTargetableReason(gameData, target, card, controllerId)
                        .ifPresent(reason -> { throw new IllegalStateException(reason); });
            }
        }

        validateMultiTargetConstraint(gameData, card.getMultiTargetConstraint(), targetIds);
    }

    /**
     * Enforces a spell's cross-target restriction on the whole chosen set (CR 601.2c), beyond the
     * per-position filters. Currently only "share no creature types" (Rivals' Duel).
     */
    private void validateMultiTargetConstraint(GameData gameData, MultiTargetConstraint constraint, List<UUID> targetIds) {
        if (constraint == null) {
            return;
        }
        List<Permanent> targets = targetIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .toList();
        for (int i = 0; i < targets.size(); i++) {
            for (int j = i + 1; j < targets.size(); j++) {
                Permanent a = targets.get(i);
                Permanent b = targets.get(j);
                switch (constraint) {
                    case SHARE_NO_CREATURE_TYPES -> {
                        if (gameQueryService.shareCreatureType(gameData, a, b)) {
                            throw new IllegalStateException("Chosen creatures must share no creature types");
                        }
                    }
                    case SHARE_ARTIFACT_CREATURE_OR_LAND_TYPE -> {
                        if (!gameQueryService.sharesArtifactCreatureOrLandType(a, b)) {
                            throw new IllegalStateException(
                                    "Chosen permanents must share an artifact, creature, or land type");
                        }
                    }
                }
            }
        }
    }

    public boolean isTargetIllegalOnResolution(GameData gameData, StackEntry entry) {
        if (entry.isNonTargeting()) {
            return false;
        }

        if (entry.getEffectsToResolve().stream().anyMatch(AttackCounterMoveEffect.class::isInstance)) {
            List<UUID> targetIds = entry.getDeclaredTargetIds();
            boolean anyLegalTarget = false;
            for (int i = 0; i < targetIds.size(); i++) {
                if (gameQueryService.findPermanentById(gameData, targetIds.get(i)) != null) {
                    anyLegalTarget = true;
                } else {
                    entry.markTargetIllegal(i);
                }
            }
            return !anyLegalTarget;
        }

        // CR 608.2b requires every target occurrence to be checked again. Keep illegal flat-list
        // positions masked on the entry so a spell with at least one legal target can resolve
        // without its handlers affecting the illegal targets or shifting later target groups.
        List<UUID> declaredTargetIds = entry.getDeclaredTargetIds();
        if (!declaredTargetIds.isEmpty()) {
            List<TargetFilter> targetFilters = targetFiltersForDeclaredPositions(entry, declaredTargetIds.size());
            UUID primaryTargetId = entry.getTargetId();
            boolean hasPrimaryTarget = primaryTargetId != null;
            boolean primaryTargetLegal = hasPrimaryTarget
                    && isPrimaryTargetLegalOnResolution(gameData, entry, primaryTargetId);
            if (hasPrimaryTarget && !primaryTargetLegal) {
                entry.setTargetId(null);
            }

            boolean secondaryTargetsAreOnStack = entry.getTargetZone() == Zone.STACK && !hasPrimaryTarget;
            boolean anySecondaryTargetLegal = false;
            for (int i = 0; i < declaredTargetIds.size(); i++) {
                UUID targetId = declaredTargetIds.get(i);
                TargetFilter targetFilter = targetFilters.get(i);
                boolean legal = secondaryTargetsAreOnStack
                        ? checkSpellTargetOnStack(gameData, targetId, targetFilter, entry.getControllerId(),
                                entry.getSourcePermanentSnapshot(), entry.getXValue()).isEmpty()
                        : isBattlefieldTargetLegalOnResolution(gameData, entry, targetId, targetFilter);
                if (legal) {
                    anySecondaryTargetLegal = true;
                } else {
                    entry.markTargetIllegal(i);
                }
            }

            boolean anyGraveyardCardTargetLegal = entry.getTargetCardIds().stream()
                    .anyMatch(id -> gameQueryService.findCardInGraveyardById(gameData, id) != null);
            return !primaryTargetLegal && !anySecondaryTargetLegal && !anyGraveyardCardTargetLegal;
        }

        // Multi-spell targeting: spell targets multiple distinct spells on the stack (e.g.
        // Choreographed Sparks' "both" mode). Per MTG CR 608.2b: fizzles only when ALL of the
        // targeted spells have left the stack; each still-legal target is handled per-effect.
        if (entry.getTargetId() == null && entry.getTargetZone() == Zone.STACK
                && !entry.getTargetIds().isEmpty()) {
            boolean anyStillOnStack = entry.getTargetIds().stream()
                    .anyMatch(id -> gameData.stack.stream().anyMatch(se -> se.getCard().getId().equals(id)));
            return !anyStillOnStack;
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
                    } else if (entry.getCard() != null && entry.getCard().getColor() != null
                            && gameQueryService.playerHasProtectionFromColor(gameData, entry.getTargetId(),
                                    entry.getCard().getColor())) {
                        targetFizzled = true;
                    } else if (entry.getCard() != null
                            && gameQueryService.playerHasProtectionFromChosenName(gameData, entry.getTargetId(),
                                    entry.getCard().getName())) {
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
                        // A triggered ability falls back to the card-level target filter only when one
                        // of its effects is actually bound to that declared target group. Otherwise the
                        // filter belongs to a different ability of the same card (e.g. Soulstinger's
                        // ETB "target creature you control") and must not fizzle this trigger, whose
                        // own effect targets any creature. Cast spells / activated abilities are
                        // unaffected — their effects are always bound to their target group.
                        TargetFilter cardFilter = entry.getCard() != null ? entry.getCard().getTargetFilter() : null;
                        if (cardFilter != null && entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                                && entry.getEffectsToResolve().stream()
                                        .noneMatch(e -> entry.getCard().getEffectTargetIndex(e) >= 0)) {
                            cardFilter = null;
                        }
                        TargetFilter effectiveTargetFilter =
                                entry.getTargetFilter() != null ? entry.getTargetFilter() : cardFilter;
                        if (effectiveTargetFilter != null) {
                            try {
                                predicateEvaluationService.validateTargetFilter(effectiveTargetFilter, targetPerm,
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

    private boolean isPrimaryTargetLegalOnResolution(GameData gameData, StackEntry entry, UUID targetId) {
        if (entry.getTargetZone() == Zone.EXILE) {
            return gameQueryService.findCardInExileById(gameData, targetId) != null;
        }
        if (entry.getTargetZone() == Zone.GRAVEYARD) {
            return gameQueryService.findCardInGraveyardById(gameData, targetId) != null;
        }
        if (entry.getTargetZone() == Zone.STACK) {
            return checkSpellTargetOnStack(gameData, targetId, entry.getTargetFilter(), entry.getControllerId(),
                    entry.getSourcePermanentSnapshot(), entry.getXValue()).isEmpty();
        }
        return isBattlefieldTargetLegalOnResolution(gameData, entry, targetId, primaryTargetFilter(entry));
    }

    private boolean isBattlefieldTargetLegalOnResolution(GameData gameData, StackEntry entry, UUID targetId,
                                                          TargetFilter targetFilter) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            if (!gameData.playerIds.contains(targetId)) {
                return false;
            }
            if (checkPlayerUntargetableReason(gameData, targetId, entry.getControllerId()) != null) {
                return false;
            }
            Card sourceCard = entry.getCard();
            if (sourceCard != null && sourceCard.getColor() != null
                    && gameQueryService.playerHasProtectionFromColor(gameData, targetId, sourceCard.getColor())) {
                return false;
            }
            if (sourceCard != null
                    && gameQueryService.playerHasProtectionFromChosenName(gameData, targetId, sourceCard.getName())) {
                return false;
            }
            if (targetFilter instanceof PlayerPredicateTargetFilter playerFilter) {
                return matchesPlayerPredicate(gameData, entry.getControllerId(), targetId, playerFilter.predicate());
            }
            if (targetFilter instanceof AnyTargetPredicateTargetFilter anyFilter) {
                return matchesPlayerPredicate(gameData, entry.getControllerId(), targetId, anyFilter.playerPredicate());
            }
            return true;
        }

        if (untargetableReason(gameData, target, entry.getControllerId()) != null) {
            return false;
        }
        if ((entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                && isBlockedByOpponentAbilityRestriction(gameData, target, entry.getControllerId())) {
            return false;
        }
        if (isProtectedFromSource(gameData, target, entry) || isSpellProtected(gameData, target, entry)
                || isHexproofFromColorBlocked(gameData, target, entry)
                || isNonColorSourceRestricted(gameData, target, entry)) {
            return false;
        }
        if (targetFilter != null) {
            try {
                predicateEvaluationService.validateTargetFilter(targetFilter, target,
                        filterContext(gameData, entry.getCard() != null ? entry.getCard().getId() : null,
                                entry.getControllerId()).withXValue(entry.getXValue()));
            } catch (IllegalStateException e) {
                return false;
            }
        }
        return true;
    }

    private boolean isProtectedFromSource(GameData gameData, Permanent target, StackEntry entry) {
        Card sourceCard = entry.getCard();
        if (sourceCard == null) {
            return false;
        }
        return (sourceCard.getColor() != null
                    && gameQueryService.hasProtectionFrom(gameData, target, sourceCard.getColor()))
                || gameQueryService.hasProtectionFromSourceCardTypes(target, sourceCard)
                || gameQueryService.hasProtectionFromSourceSubtypes(target, sourceCard);
    }

    private TargetFilter primaryTargetFilter(StackEntry entry) {
        if (entry.getTargetFilter() != null) {
            return entry.getTargetFilter();
        }
        if (entry.getCard() == null) {
            return null;
        }
        TargetFilter cardFilter = entry.getCard().getTargetFilter();
        if (cardFilter != null && entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getEffectsToResolve().stream()
                        .noneMatch(e -> entry.getCard().getEffectTargetIndex(e) >= 0)) {
            return null;
        }
        return cardFilter;
    }

    private List<TargetFilter> targetFiltersForDeclaredPositions(StackEntry entry, int targetCount) {
        List<TargetFilter> filters = new ArrayList<>(targetCount);
        if (entry.getTargetFilter() != null) {
            for (int i = 0; i < targetCount; i++) {
                filters.add(entry.getTargetFilter());
            }
            return filters;
        }

        Card card = entry.getCard();
        if (card != null) {
            int firstFlatGroup = card.isAura() && entry.getTargetId() != null ? 1 : 0;
            int remaining = targetCount;
            for (SpellTarget group : card.getSpellTargets()) {
                if (group.getIndex() < firstFlatGroup || !entry.isTargetGroupActive(group.getIndex())) {
                    continue;
                }
                int size = Math.min(Math.max(group.getMaxTargets(), 0), remaining);
                for (int i = 0; i < size; i++) {
                    filters.add(group.getFilter());
                }
                remaining -= size;
                if (remaining == 0) {
                    break;
                }
            }
        }

        TargetFilter fallback = card != null ? card.getTargetFilter() : null;
        while (filters.size() < targetCount) {
            filters.add(fallback);
        }
        return filters;
    }

    /**
     * Checks if the target permanent is protected from the resolving spell's color
     * (e.g. via Autumn's Veil or a static TargetingRestrictionEffect blocking that spell color).
     * Only applies when the entry is a spell (not a triggered/activated ability).
     */
    private boolean isSpellProtected(GameData gameData, Permanent targetPerm, StackEntry entry) {
        if (entry.getCard() == null) return false;
        StackEntryType entryType = entry.getEntryType();
        if (entryType == StackEntryType.TRIGGERED_ABILITY || entryType == StackEntryType.ACTIVATED_ABILITY) {
            return false;
        }
        return gameQueryService.cantBeTargetedBySpellColor(gameData, targetPerm, entry.getCard().getColor())
                || gameQueryService.cantBeTargetedByAnySpell(gameData, targetPerm);
    }

    private boolean isNonColorSourceRestricted(GameData gameData, Permanent targetPerm, StackEntry entry) {
        if (entry.getCard() == null) return false;
        return gameQueryService.cantBeTargetedByNonColorSources(gameData, targetPerm, entry.getCard());
    }

    private String nonColorSourceRestrictionMessage(Permanent target) {
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof TargetingRestrictionEffect r && r.mode() == TargetColorMode.ALLOWED_COLORS_ONLY) {
                String colorName = r.colors().stream()
                        .map(c -> c.name().toLowerCase())
                        .reduce((a, b) -> a + "/" + b)
                        .orElse("");
                return target.getCard().getName() + " can't be the target of non-"
                        + colorName + " spells or abilities";
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
                    || gameQueryService.cantBeTargetedBySpellsOrAbilities(gameData, target)) {
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

    /**
     * The single per-permanent structural targeting core for a spell, shared by all three
     * spell-target validation paths: UI/AI enumeration ({@link ValidTargetService}), the
     * multi-target cast path ({@link #validateMultiSpellTargets}) and the single-target cast path
     * ({@link #checkSpellTargeting}). Checks protection (color/type/subtype), can't-be-targeted
     * (spell color / any spell / non-color sources), shroud, hexproof, granted hexproof, and
     * hexproof-from-color, returning the reason a permanent can't legally be targeted or empty if
     * it is structurally legal. It deliberately does NOT apply the card's {@code TargetFilter} or
     * the per-effect {@code @ValidatesTarget} validators — those are layered on by each entry point
     * (filters via list/position bookkeeping; validators as the shared type-narrowing mechanism).
     */
    public Optional<String> checkSpellPermanentTargetableReason(GameData gameData, Permanent target, Card card, UUID controllerId) {
        String protectionReason = checkSpellProtection(gameData, target, card, controllerId);
        if (protectionReason != null) {
            return Optional.of(protectionReason);
        }
        String untargetable = untargetableReason(gameData, target, controllerId);
        if (untargetable != null) {
            return Optional.of(untargetable);
        }
        return Optional.empty();
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
        if (gameQueryService.cantBeTargetedByAnySpell(gameData, target)) {
            return target.getCard().getName() + " can't be the target of spells";
        }
        if (gameQueryService.cantBeTargetedByNonColorSources(gameData, target, card)) {
            return nonColorSourceRestrictionMessage(target);
        }
        if (card.isAura() && gameQueryService.cantBeEnchantedByOtherAuras(gameData, target)) {
            return target.getCard().getName() + " can't be enchanted by other Auras";
        }
        if (sourcePlayerId != null) {
            String hexReason = hexproofFromColorReason(gameData, target, card, sourcePlayerId);
            if (hexReason != null) return hexReason;
        }
        return null;
    }

    private void validateMultiTargetCount(List<UUID> targetIds, int min, int max) {
        validateMultiTargetCount(targetIds, min, max, null, false);
    }

    /**
     * Validates target count and uniqueness.
     * <p>
     * By default, all targets must be globally unique across all groups — this matches the
     * common MTG pattern where separate "target" words imply distinct objects. Cards whose
     * oracle text does NOT use "another" and whose target filters can overlap must set
     * {@code allowSharedTargets = true} to opt in to the CR 114.6c rule that allows the same
     * permanent across different target groups (within-group uniqueness is still enforced).
     */
    private void validateMultiTargetCount(List<UUID> targetIds, int min, int max,
                                          List<SpellTarget> spellTargets, boolean allowSharedTargets) {
        if (targetIds == null || targetIds.size() < min || targetIds.size() > max) {
            throw new IllegalStateException("Must target between " + min + " and " + max + " targets");
        }
        if (allowSharedTargets && spellTargets != null && spellTargets.size() > 1) {
            // CR 114.6c: same permanent allowed across groups; enforce within-group uniqueness only
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
            // Default: global uniqueness across all targets
            if (new HashSet<>(targetIds).size() != targetIds.size()) {
                throw new IllegalStateException("All targets must be different");
            }
        }
    }

    private void validatePlayerPredicate(GameData gameData, UUID controllerId, UUID targetPlayerId, PlayerPredicate predicate, String errorMessage) {
        if (!matchesPlayerPredicate(gameData, controllerId, targetPlayerId, predicate)) {
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

    /**
     * True when {@code targetId} is the card id of a (non-ability) spell currently on the stack.
     * Used to route a "spell or permanent" single target (e.g. Glamerdye) to the correct zone.
     */
    public boolean isSpellOnStack(GameData gameData, UUID targetId) {
        return targetId != null && findSpellOnStack(gameData, targetId) != null;
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

    /**
     * Whether {@code targetFilter} can legally match an activated or triggered ability on the stack
     * (not just a spell), which decides whether the candidate lookup includes ability entries
     * ({@link #findAnyEntryOnStack}) or only spells ({@link #findSpellOnStack}). True when the filter
     * explicitly admits abilities: a {@link StackEntryHasTargetPredicate} (an ability that targets,
     * e.g. Siren Stormtamer) or a {@link StackEntryTypeInPredicate} naming an ability type ("counter
     * target activated or triggered ability", Nimble Obstructionist).
     */
    private boolean filterAdmitsAbilityTarget(TargetFilter targetFilter) {
        if (!(targetFilter instanceof StackEntryPredicateTargetFilter filter)) {
            return false;
        }
        return predicateAdmitsAbilityTarget(filter.predicate());
    }

    private boolean predicateAdmitsAbilityTarget(StackEntryPredicate predicate) {
        if (predicate instanceof StackEntryHasTargetPredicate) {
            return true;
        }
        if (predicate instanceof StackEntryTypeInPredicate typeIn) {
            return typeIn.spellTypes().contains(StackEntryType.ACTIVATED_ABILITY)
                    || typeIn.spellTypes().contains(StackEntryType.TRIGGERED_ABILITY);
        }
        if (predicate instanceof StackEntryAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(this::predicateAdmitsAbilityTarget);
        }
        if (predicate instanceof StackEntryAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(this::predicateAdmitsAbilityTarget);
        }
        if (predicate instanceof StackEntryNotPredicate not) {
            return predicateAdmitsAbilityTarget(not.predicate());
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
        return matchesStackEntryPredicate(gameData, stackEntry, predicate, controllerId, null);
    }

    public boolean matchesStackEntryPredicate(GameData gameData, StackEntry stackEntry, StackEntryPredicate predicate,
                                              UUID controllerId, Permanent source) {
        return matchesStackEntryPredicate(gameData, stackEntry, predicate, controllerId, source, null);
    }

    public boolean matchesStackEntryPredicate(GameData gameData, StackEntry stackEntry, StackEntryPredicate predicate,
                                              UUID controllerId, Permanent source, Integer xValue) {
        if (predicate instanceof StackEntrySharesChosenNameWithSourcePredicate) {
            return source != null && source.getChosenName() != null
                    && source.getChosenName().equals(stackEntry.getCard().getName());
        }
        if (predicate instanceof StackEntryTypeInPredicate typeInPredicate) {
            return typeInPredicate.spellTypes().contains(stackEntry.getEntryType());
        }
        if (predicate instanceof StackEntryColorInPredicate colorInPredicate) {
            return colorInPredicate.colors().contains(stackEntry.getCard().getColor());
        }
        if (predicate instanceof StackEntrySubtypeInPredicate subtypeInPredicate) {
            return stackEntry.getCard().getSubtypes().stream()
                    .anyMatch(subtypeInPredicate.subtypes()::contains);
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
        if (predicate instanceof StackEntryManaValueEqualsXPredicate) {
            // When X is unknown (target enumeration before X is chosen), match permissively —
            // any spell is potentially a legal target since X can be any non-negative integer.
            return xValue == null || stackEntry.getCard().getManaValue() == xValue;
        }
        if (predicate instanceof StackEntryManaValueEqualsSourceCountersPredicate equalsCounters) {
            if (source == null) {
                return false;
            }
            int manaValue = stackEntry.getCard().getManaValue() + stackEntry.getXValue();
            return manaValue == source.getCounterCount(equalsCounters.counterType());
        }
        if (predicate instanceof StackEntryManaValueAtMostControlledCountPredicate atMostPredicate) {
            int count = countControlledMatching(gameData, controllerId, atMostPredicate.countFilter());
            return stackEntry.getCard().getManaValue() <= count;
        }
        if (predicate instanceof StackEntryControlledByPredicate) {
            return stackEntry.getControllerId().equals(controllerId);
        }
        if (predicate instanceof StackEntryCastFromZonePredicate castFrom) {
            return stackEntry.getSourceZone() == castFrom.sourceZone();
        }
        if (predicate instanceof StackEntryTargetsYourPermanentPredicate) {
            return targetsAPermanentControlledBy(gameData, stackEntry, controllerId);
        }
        if (predicate instanceof StackEntryTargetsYouOrCreatureYouControlPredicate) {
            return targetsPlayerOrCreatureControlledBy(gameData, stackEntry, controllerId);
        }
        if (predicate instanceof StackEntryTargetsYouPredicate) {
            return targetsPlayer(stackEntry, controllerId);
        }
        if (predicate instanceof StackEntryTargetsSourcePredicate) {
            return source != null && targetsPermanent(stackEntry, source.getId());
        }
        if (predicate instanceof StackEntryTargetsPermanentPredicate targetsPermanent) {
            return targetsAnyMatchingPermanent(gameData, stackEntry, targetsPermanent.filter(), controllerId);
        }
        if (predicate instanceof StackEntryAnyOfPredicate anyOfPredicate) {
            for (StackEntryPredicate nested : anyOfPredicate.predicates()) {
                if (matchesStackEntryPredicate(gameData, stackEntry, nested, controllerId, source, xValue)) {
                    return true;
                }
            }
            return false;
        }
        if (predicate instanceof StackEntryAllOfPredicate allOfPredicate) {
            for (StackEntryPredicate nested : allOfPredicate.predicates()) {
                if (!matchesStackEntryPredicate(gameData, stackEntry, nested, controllerId, source, xValue)) {
                    return false;
                }
            }
            return true;
        }
        if (predicate instanceof StackEntryNotPredicate notPredicate) {
            return !matchesStackEntryPredicate(gameData, stackEntry, notPredicate.predicate(), controllerId, source, xValue);
        }
        return false;
    }

    private int countControlledMatching(GameData gameData, UUID controllerId, PermanentPredicate filter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;
        FilterContext ctx = FilterContext.of(gameData).withSourceControllerId(controllerId);
        int count = 0;
        for (Permanent p : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(p, filter, ctx)) {
                count++;
            }
        }
        return count;
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

    private boolean targetsAnyMatchingPermanent(GameData gameData, StackEntry stackEntry,
                                                 PermanentPredicate filter, UUID controllerId) {
        FilterContext ctx = FilterContext.of(gameData).withSourceControllerId(controllerId);
        if (stackEntry.getTargetId() != null && matchesTarget(gameData, stackEntry.getTargetId(), filter, ctx)) {
            return true;
        }
        if (stackEntry.getTargetIds() != null) {
            for (UUID targetId : stackEntry.getTargetIds()) {
                if (matchesTarget(gameData, targetId, filter, ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesTarget(GameData gameData, UUID targetId, PermanentPredicate filter, FilterContext ctx) {
        Permanent perm = gameQueryService.findPermanentById(gameData, targetId);
        return perm != null && predicateEvaluationService.matchesPermanentPredicate(perm, filter, ctx);
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

    private boolean targetsPlayer(StackEntry stackEntry, UUID controllerId) {
        if (controllerId.equals(stackEntry.getTargetId())) {
            return true;
        }
        if (stackEntry.getTargetIds() != null) {
            for (UUID targetId : stackEntry.getTargetIds()) {
                if (controllerId.equals(targetId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean targetsPermanent(StackEntry stackEntry, UUID permanentId) {
        if (permanentId.equals(stackEntry.getTargetId())) {
            return true;
        }
        if (stackEntry.getTargetIds() != null) {
            for (UUID targetId : stackEntry.getTargetIds()) {
                if (permanentId.equals(targetId)) {
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

    private boolean matchesPlayerPredicate(GameData gameData, UUID controllerId, UUID targetPlayerId, PlayerPredicate predicate) {
        return switch (predicate) {
            case PlayerRelationPredicate relationPredicate -> switch (relationPredicate.relation()) {
                case ANY -> true;
                case SELF -> controllerId != null && controllerId.equals(targetPlayerId);
                case OPPONENT -> controllerId != null && !controllerId.equals(targetPlayerId);
            };
            case PlayerDealtDamageThisTurnPredicate ignored ->
                    gameData.playersDealtDamageThisTurn.contains(targetPlayerId);
        };
    }
}

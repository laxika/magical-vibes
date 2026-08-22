package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.AttackCounterMoveEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.TargetedGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAndReturnTargetCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerDamagedBySourceThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerDamagedBySourceCombatThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerLostLifeThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMoreCreaturesThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMoreLandsThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerHasMoreCardsInHandThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerHasMoreLifeThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesChosenNameWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesNameWithCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySupertypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsNthSpellCastThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryKickedPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesColorOrManaValueWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotTargetedByNamedCreatureAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouOrCreatureYouControlPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsAnyPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
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
    private final AmountEvaluationService amountEvaluationService;
    private final TargetGroupAssignmentService targetGroupAssignmentService;

    public Optional<String> checkSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter, UUID controllerId) {
        return checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, null, null, null);
    }

    /**
     * Same as {@link #checkSpellTargetOnStack(GameData, UUID, TargetFilter, UUID)} but with the source
     * permanent supplied, which source-dependent predicates (e.g. "with the chosen name") require.
     */
    public Optional<String> checkSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter,
                                                    UUID controllerId, Permanent source) {
        return checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, source, null, null);
    }

    /**
     * Same as above but with the casting spell's chosen X, which X-relative predicates
     * (e.g. "counter target spell with mana value X" — Spell Blast) require. A {@code null}
     * {@code xValue} means X is not yet known (target enumeration) and such predicates match
     * permissively.
     */
    public Optional<String> checkSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter,
                                                    UUID controllerId, Permanent source, Integer xValue) {
        return checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, source, xValue, null);
    }

    public Optional<String> checkSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter,
                                                    UUID controllerId, Permanent source, Integer xValue, Boolean kicked) {
        if (targetId == null) {
            return Optional.of("Must target a spell on the stack");
        }

        boolean includeAbilities = filterAdmitsAbilityTarget(targetFilter, kicked);
        StackEntry targetSpell = includeAbilities
                ? findAnyEntryOnStack(gameData, targetId)
                : findSpellOnStack(gameData, targetId);
        if (targetSpell == null) {
            return Optional.of(includeAbilities
                    ? "Target must be a spell or ability on the stack"
                    : "Target must be a spell on the stack");
        }

        if (targetFilter instanceof StackEntryPredicateTargetFilter filter
                && !matchesStackEntryPredicate(gameData, targetSpell, filter.predicateFor(Boolean.TRUE.equals(kicked)),
                controllerId, source, xValue)) {
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

    public void validateSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter,
                                           UUID controllerId, Permanent source, int xValue) {
        checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, source, xValue)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public void validateSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter, UUID controllerId, int xValue) {
        checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, null, xValue)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public void validateSpellTargetOnStack(GameData gameData, UUID targetId, TargetFilter targetFilter,
                                           UUID controllerId, int xValue, boolean kicked) {
        checkSpellTargetOnStack(gameData, targetId, targetFilter, controllerId, null, xValue, kicked)
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
        validateMultiSpellTargetsOnStack(gameData, card, targetIds, controllerId, false);
    }

    public void validateMultiSpellTargetsOnStack(GameData gameData, Card card, List<UUID> targetIds,
                                                 UUID controllerId, boolean kicked) {
        List<TargetFilter> perPositionFilters = card.getMultiTargetFilters();
        int maxTargets = perPositionFilters.size();
        int minTargets = card.getEffectiveMinTargets(0, kicked);
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
            validateSpellTargetOnStack(gameData, targetIds.get(i), perPositionFilters.get(i), controllerId,
                    0, kicked);
        }
    }

    /**
     * Validates that the given graveyard card IDs are legal targets for a multi-target graveyard ability.
     * Each card must exist in an opponent's graveyard (not the controller's).
     */
    public void validateMultiTargetGraveyardAbility(GameData gameData, UUID playerId,
                                                     List<CardEffect> effects, List<UUID> targetCardIds) {
        validateMultiTargetGraveyardAbility(gameData, playerId, effects, targetCardIds, null);
    }

    public void validateMultiTargetGraveyardAbility(GameData gameData, UUID playerId,
                                                     List<CardEffect> effects, List<UUID> targetCardIds,
                                                     UUID sourceCardId) {
        validateMultiTargetGraveyardAbility(gameData, playerId, effects, targetCardIds, sourceCardId, null);
    }

    public void validateMultiTargetGraveyardAbility(GameData gameData, UUID playerId,
                                                     List<CardEffect> effects, List<UUID> targetCardIds,
                                                     UUID sourceCardId, Integer xValue,
                                                     MultiTargetConstraint constraint) {
        validateMultiTargetGraveyardAbility(gameData, playerId, effects, targetCardIds,
                sourceCardId, xValue);
        validateMultiTargetConstraint(gameData, constraint, targetCardIds);
    }

    public void validateMultiTargetGraveyardAbility(GameData gameData, UUID playerId,
                                                     List<CardEffect> effects, List<UUID> targetCardIds,
                                                     UUID sourceCardId, Integer xValue) {
        if (targetCardIds == null) {
            throw new IllegalStateException("Must select graveyard targets");
        }
        for (CardEffect effect : effects) {
            if (effect instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect
                    && returnEffect.hasTotalManaValueCap()) {
                validateTotalManaValueGraveyardTargets(gameData, playerId, returnEffect, targetCardIds,
                        sourceCardId);
                return;
            }
        }
        if (targetCardIds.isEmpty()) {
            boolean zeroTargetsAllowed = effects.stream().anyMatch(effect ->
                    effect instanceof TargetedGraveyardCardsEffect
                            || effect instanceof ExileCardsFromGraveyardEffect
                            || effect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnEffect
                            && returnEffect.minTargets() == 0)
                    || effects.stream().anyMatch(effect ->
                    effect instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect
                            && returnEffect.source() == GraveyardSearchScope.ALL_GRAVEYARDS)
                    || xValue != null && xValue == 0 && effects.stream()
                    .anyMatch(effect -> effect instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect
                            && ((ReturnTargetCardsFromGraveyardToBattlefieldEffect) effect).xScaled());
            if (!zeroTargetsAllowed) {
                throw new IllegalStateException("Must select graveyard targets");
            }
        }
        for (CardEffect effect : effects) {
            if (effect instanceof TargetedGraveyardCardsEffect libraryEffect) {
                validateTargetedGraveyardCardLibraryEffect(gameData, playerId, libraryEffect, targetCardIds);
                break;
            }
            if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
                for (UUID cardId : targetCardIds) {
                    Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                    if (card == null) {
                        throw new IllegalStateException("Target card not found in any graveyard");
                    }
                    if (!matchesReturnCardFilter(gameData, returnEffect, card, sourceCardId)) {
                        throw new IllegalStateException("Target card must be a "
                                + (returnEffect.filter() == null
                                ? "card of the chosen type"
                                : CardPredicateUtils.describeFilter(returnEffect.filter())));
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
            if (effect instanceof ExileGraveyardCardsEffect anyGraveyardEffect
                    && anyGraveyardEffect.scope() == GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD) {
                // "Exile up to N target cards from a single graveyard" (Rag Dealer) — any player's
                // graveyard, at most N distinct cards, and all of them in the same graveyard.
                if (anyGraveyardEffect.exactTargetCount()
                        && targetCardIds.size() != anyGraveyardEffect.count()) {
                    throw new IllegalStateException("Must select exactly " + anyGraveyardEffect.count()
                            + " target cards");
                }
                if (targetCardIds.size() > anyGraveyardEffect.count()) {
                    throw new IllegalStateException("Cannot target more than " + anyGraveyardEffect.count() + " cards");
                }
                if (new HashSet<>(targetCardIds).size() != targetCardIds.size()) {
                    throw new IllegalStateException("Cannot target the same card twice");
                }
                UUID sharedGraveyardOwnerId = null;
                for (UUID cardId : targetCardIds) {
                    Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                    if (card == null) {
                        throw new IllegalStateException("Target card not found in any graveyard");
                    }
                    if (anyGraveyardEffect.filter() != null
                            && !predicateEvaluationService.matchesCardPredicate(card, anyGraveyardEffect.filter(), null)) {
                        throw new IllegalStateException("Target card must be a "
                                + CardPredicateUtils.describeFilter(anyGraveyardEffect.filter()));
                    }
                    UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                    if (sharedGraveyardOwnerId == null) {
                        sharedGraveyardOwnerId = graveyardOwnerId;
                    } else if (!sharedGraveyardOwnerId.equals(graveyardOwnerId)) {
                        throw new IllegalStateException("All targets must be in a single graveyard");
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
                if (!exileEffect.xScaled() && targetCardIds.size() > exileEffect.maxTargets()) {
                    throw new IllegalStateException("Cannot target more than " + exileEffect.maxTargets() + " cards");
                }
                if (exileEffect.xScaled() && xValue != null && targetCardIds.size() > xValue) {
                    throw new IllegalStateException("Cannot target more than " + xValue + " cards");
                }
                if (new HashSet<>(targetCardIds).size() != targetCardIds.size()) {
                    throw new IllegalStateException("Cannot target the same card twice");
                }
                UUID sharedGraveyardOwnerId = null;
                for (UUID cardId : targetCardIds) {
                    if (gameQueryService.findCardInGraveyardById(gameData, cardId) == null) {
                        throw new IllegalStateException("Target card not found in any graveyard");
                    }
                    if (exileEffect.singleGraveyard()) {
                        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                        if (sharedGraveyardOwnerId == null) {
                            sharedGraveyardOwnerId = graveyardOwnerId;
                        } else if (!sharedGraveyardOwnerId.equals(graveyardOwnerId)) {
                            throw new IllegalStateException("All targets must be in a single graveyard");
                        }
                    }
                }
                break;
            }
            if (effect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnCardsEffect) {
                // "Return up to N target [type] cards from your graveyard to your hand" (Soul of
                // Innistrad) — no more than N distinct cards, each in the controller's own graveyard
                // and matching the filter.
                // A dynamic cap (Reap) is computed and enforced at cast time by the multi-graveyard
                // choice itself — there is no fixed number to check against here.
                if (returnCardsEffect.dynamicMaxTargets() == null
                        && targetCardIds.size() > returnCardsEffect.maxTargets()) {
                    throw new IllegalStateException("Cannot target more than "
                            + returnCardsEffect.maxTargets() + " cards");
                }
                if (returnCardsEffect.exactTargets()
                        && targetCardIds.size() != returnCardsEffect.maxTargets()) {
                    throw new IllegalStateException("Must target exactly "
                            + returnCardsEffect.maxTargets() + " cards");
                }
                if (new HashSet<>(targetCardIds).size() != targetCardIds.size()) {
                    throw new IllegalStateException("Cannot target the same card twice");
                }
                for (UUID cardId : targetCardIds) {
                    Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                    if (card == null) {
                        throw new IllegalStateException("Target card not found in any graveyard");
                    }
                    if (returnCardsEffect.filter() != null
                            && !predicateEvaluationService.matchesCardPredicate(
                            card, returnCardsEffect.filter(), sourceCardId)) {
                        throw new IllegalStateException("Target card must be a "
                                + CardPredicateUtils.describeFilter(returnCardsEffect.filter()));
                    }
                }
                for (CardType cardType : returnCardsEffect.maxOnePerCardType()) {
                    long matchingTargets = targetCardIds.stream()
                            .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                            .filter(java.util.Objects::nonNull)
                            .filter(card -> card.hasType(cardType))
                            .count();
                    if (matchingTargets > 1) {
                        throw new IllegalStateException("Cannot target more than one "
                                + cardType.name().toLowerCase() + " card");
                    }
                }
                for (UUID cardId : targetCardIds) {
                    UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                    if (graveyardOwnerId != null && !graveyardOwnerId.equals(playerId)) {
                        throw new IllegalStateException("Target must be in your graveyard");
                    }
                }
                break;
            }
            if (effect instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect returnCardsEffect) {
                if (!returnCardsEffect.xScaled() && targetCardIds.size() > returnCardsEffect.maxTargets()) {
                    throw new IllegalStateException("Cannot target more than "
                            + returnCardsEffect.maxTargets() + " cards");
                }
                if (returnCardsEffect.xScaled() && xValue != null) {
                    if (targetCardIds.size() > xValue) {
                        throw new IllegalStateException("Cannot target more than " + xValue + " cards");
                    }
                    long matchingCards = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                            .filter(card -> predicateEvaluationService.matchesCardPredicate(
                                    card, returnCardsEffect.filter(), sourceCardId))
                            .count();
                    int requiredTargets = Math.min(xValue, (int) matchingCards);
                    if (targetCardIds.size() != requiredTargets) {
                        throw new IllegalStateException("Must target exactly " + requiredTargets + " cards");
                    }
                }
                if (new HashSet<>(targetCardIds).size() != targetCardIds.size()) {
                    throw new IllegalStateException("Cannot target the same card twice");
                }
                for (UUID cardId : targetCardIds) {
                    Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                    if (card == null) {
                        throw new IllegalStateException("Target card not found in any graveyard");
                    }
                    if (!predicateEvaluationService.matchesCardPredicate(
                            card, returnCardsEffect.filter(), sourceCardId)) {
                        throw new IllegalStateException("Target card must be a creature card");
                    }
                    UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                    if (returnCardsEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD
                            && graveyardOwnerId != null && !graveyardOwnerId.equals(playerId)) {
                        throw new IllegalStateException("Target must be in your graveyard");
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
                        && !predicateEvaluationService.matchesCardPredicate(
                        card, exileCopy.filter(), sourceCardId, gameData, playerId)) {
                    throw new IllegalStateException("Target card must be a "
                            + CardPredicateUtils.describeFilter(exileCopy.filter()));
                }
                if (exileCopy.ownGraveyardOnly()) {
                    UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                    if (graveyardOwnerId != null && !graveyardOwnerId.equals(playerId)) {
                        throw new IllegalStateException("Target must be in your graveyard");
                    }
                }
                if (exileCopy.targetPutIntoGraveyardFromAnywhereThisTurn()) {
                    UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                    boolean tracked = graveyardOwnerId != null
                            && gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                                    .getOrDefault(graveyardOwnerId, Set.of()).contains(cardId);
                    if (!tracked) {
                        throw new IllegalStateException(
                                "Target must be a creature card put into a graveyard this turn");
                    }
                }
                break;
            }
        }
        validateDeclarativeGraveyardTargets(
                gameData, playerId, effects, targetCardIds, sourceCardId, xValue);
    }

    private void validateDeclarativeGraveyardTargets(GameData gameData, UUID playerId,
                                                      List<CardEffect> effects, List<UUID> targetCardIds,
                                                      UUID sourceCardId, Integer xValue) {
        List<CardEffect> declarativeEffects = effects.stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                .toList();
        if (declarativeEffects.isEmpty()) {
            return;
        }

        Permanent sourcePermanent = sourceCardId == null ? null : gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> sourceCardId.equals(permanent.getCard().getId())
                        || sourceCardId.equals(permanent.getOriginalCard().getId()))
                .findFirst()
                .orElse(null);
        Card sourceCard = sourcePermanent == null ? null : sourcePermanent.getCard();
        int effectiveXValue = xValue == null ? 0 : xValue;
        for (UUID targetCardId : targetCardIds) {
            String rejection = null;
            boolean legalForAnEffect = false;
            for (CardEffect effect : declarativeEffects) {
                var reason = targetValidationService.checkEffectTargets(
                        List.of(effect),
                        new TargetValidationContext(gameData, targetCardId, Zone.GRAVEYARD,
                                sourceCard, effectiveXValue, playerId, sourcePermanent));
                if (reason.isEmpty()) {
                    legalForAnEffect = true;
                    break;
                }
                if (rejection == null) {
                    rejection = reason.get();
                }
            }
            if (!legalForAnEffect) {
                throw new IllegalStateException(rejection != null ? rejection : "Invalid graveyard target");
            }
        }
    }

    private void validateTotalManaValueGraveyardTargets(
            GameData gameData, UUID playerId,
            ReturnTargetCardsFromGraveyardToBattlefieldEffect effect,
            List<UUID> targetCardIds, UUID sourceCardId) {
        if (!targetCardIds.isEmpty() && !gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            throw new IllegalStateException("Cards in graveyards can't be the targets of spells or abilities");
        }
        if (new HashSet<>(targetCardIds).size() != targetCardIds.size()) {
            throw new IllegalStateException("Cannot target the same card twice");
        }

        int totalManaValue = 0;
        for (UUID cardId : targetCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card == null) {
                throw new IllegalStateException("Target card not found in any graveyard");
            }
            if (gameQueryService.isLandCardTargetRestricted(gameData, card, playerId)) {
                throw new IllegalStateException(
                        "Land cards in graveyards can't be the targets of spells or abilities opponents control");
            }
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            if (graveyardOwnerId == null || !graveyardOwnerId.equals(playerId)) {
                throw new IllegalStateException("Target must be in your graveyard");
            }
            if (effect.filter() != null
                    && !predicateEvaluationService.matchesCardPredicate(card, effect.filter(), sourceCardId)) {
                throw new IllegalStateException("Target card must be a "
                        + CardPredicateUtils.describeFilter(effect.filter()));
            }
            totalManaValue += card.getManaValue();
            if (totalManaValue > effect.maxTotalManaValue()) {
                throw new IllegalStateException("Target cards' total mana value cannot exceed "
                        + effect.maxTotalManaValue());
            }
        }
    }

    private boolean matchesReturnCardFilter(GameData gameData, ReturnCardFromGraveyardEffect effect,
                                             Card card, UUID sourceCardId) {
        if (effect.sourceChosenSubtype()) {
            CardSubtype chosenSubtype = findSourceChosenSubtype(gameData, sourceCardId);
            UUID cardOwnerId = card.getOwnerId() != null
                    ? card.getOwnerId()
                    : gameQueryService.findGraveyardOwnerById(gameData, card.getId());
            return chosenSubtype != null
                    && (card.getKeywords().contains(Keyword.CHANGELING)
                    || gameQueryService.cardHasSubtype(card, chosenSubtype, gameData, cardOwnerId));
        }
        return effect.filter() == null
                || predicateEvaluationService.matchesCardPredicate(card, effect.filter(), sourceCardId);
    }

    private CardSubtype findSourceChosenSubtype(GameData gameData, UUID sourceCardId) {
        if (sourceCardId == null) {
            return null;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(sourceCardId)
                        || permanent.getOriginalCard().getId().equals(sourceCardId)) {
                    return permanent.getChosenSubtype();
                }
            }
        }
        return null;
    }

    /**
     * Validates an activated effect that moves up to N target cards from one graveyard into a
     * library. No more than N distinct cards may be chosen, each still in a graveyard and matching
     * the filter, and — for the non-controller scopes — all in the same graveyard.
     */
    private void validateTargetedGraveyardCardLibraryEffect(GameData gameData, UUID playerId,
                                                            TargetedGraveyardCardsEffect effect,
                                                            List<UUID> targetCardIds) {
        if (effect.maxTargets() != 0
                && targetCardIds.size() > effect.maxTargets()) {
            throw new IllegalStateException("Cannot target more than " + effect.maxTargets() + " cards");
        }
        if (new HashSet<>(targetCardIds).size() != targetCardIds.size()) {
            throw new IllegalStateException("Cannot target the same card twice");
        }

        UUID sharedOwnerId = null;
        for (UUID cardId : targetCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card == null) {
                throw new IllegalStateException("Target card not found in any graveyard");
            }
            if (effect.filter() != null
                    && !predicateEvaluationService.matchesCardPredicate(card, effect.filter(), null)) {
                throw new IllegalStateException("Target card must be a "
                        + CardPredicateUtils.describeFilter(effect.filter()));
            }
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            if (!effect.fromOtherGraveyards()) {
                if (ownerId != null && !ownerId.equals(playerId)) {
                    throw new IllegalStateException("Target must be in your graveyard");
                }
                continue;
            }
            if (effect.source() == GraveyardSearchScope.OPPONENT_GRAVEYARD
                    && ownerId != null && ownerId.equals(playerId)) {
                throw new IllegalStateException("Target must be in an opponent's graveyard");
            }
            if (sharedOwnerId == null) {
                sharedOwnerId = ownerId;
            } else if (!sharedOwnerId.equals(ownerId)) {
                throw new IllegalStateException("All targets must be in a single graveyard");
            }
        }
    }

    public void validateMultiTargetAbility(GameData gameData, UUID playerId, ActivatedAbility ability, List<UUID> targetIds, Card sourceCard) {
        validateMultiTargetAbility(gameData, playerId, ability, targetIds, sourceCard, 0);
    }

    /**
     * Validates a multi-target ability's chosen targets. {@code xValue} is the X paid for the
     * ability's {@code {X}} cost and bounds the target count for X-scaled abilities (Runed Arch).
     */
    public void validateMultiTargetAbility(GameData gameData, UUID playerId, ActivatedAbility ability, List<UUID> targetIds, Card sourceCard, int xValue) {
        validateMultiTargetAbility(gameData, playerId, ability, targetIds, sourceCard, xValue, ability.getEffects());
    }

    public void validateMultiTargetAbility(GameData gameData, UUID playerId, ActivatedAbility ability,
                                           List<UUID> targetIds, Card sourceCard, int xValue,
                                           List<CardEffect> abilityEffects) {
        validateMultiTargetCount(targetIds, ability.getEffectiveMinTargets(xValue), ability.getEffectiveMaxTargets(xValue),
                null, ability.isAllowSharedTargets());

        List<TargetFilter> perPositionFilters = ability.getMultiTargetFilters();
        // An unfiltered position is restricted by what the ability's own effects declare, exactly
        // as target enumeration reads it (ValidTargetService.computeValidTargetsForAbility).
        boolean allowsPlayers = ability.getTargetFilter() == null
                && EffectResolution.allowsPlayerTargets(abilityEffects);
        PermanentPredicate declaredRestriction = ability.getTargetFilter() == null
                ? EffectResolution.declaredPermanentRestriction(abilityEffects).orElse(null)
                : null;
        for (int i = 0; i < targetIds.size(); i++) {
            UUID targetId = targetIds.get(i);
            TargetFilter positionFilter = getPositionFilter(perPositionFilters, i);
            if (positionFilter == null) {
                positionFilter = ability.getTargetFilter();
            }

            // "Any target" position (Chandra, the Firebrand −6): players are legal alongside
            // creatures and planeswalkers, and no other permanent type is.
            if (positionFilter == null && allowsPlayers && gameData.playerIds.contains(targetId)) {
                String peaceTalks = peaceTalksUntargetableReason(gameData);
                if (peaceTalks != null) {
                    throw new IllegalStateException(peaceTalks);
                }
                validatePlayerTargetable(gameData, targetId, playerId, sourceCard);
                continue;
            }

            // Player-targeting position
            if (positionFilter instanceof PlayerPredicateTargetFilter playerFilter) {
                if (!gameData.playerIds.contains(targetId)) {
                    throw new IllegalStateException("Invalid player target");
                }
                String peaceTalks = peaceTalksUntargetableReason(gameData);
                if (peaceTalks != null) {
                    throw new IllegalStateException(peaceTalks);
                }
                validatePlayerTargetable(gameData, targetId, playerId, sourceCard);
                validatePlayerPredicate(gameData, playerId, targetId, playerFilter.predicate(), playerFilter.errorMessage());
                continue;
            }

            // "Target player or planeswalker" position (Chandra, Pyromaster +1): the player side is
            // checked against the filter's player predicate, the permanent side falls through below.
            if (positionFilter instanceof AnyTargetPredicateTargetFilter anyFilter
                    && gameData.playerIds.contains(targetId)) {
                validatePlayerTargetable(gameData, targetId, playerId, sourceCard);
                validatePlayerPredicate(gameData, playerId, targetId, anyFilter.playerPredicate(), anyFilter.errorMessage());
                continue;
            }

            if (positionFilter instanceof GraveyardCardPredicateTargetFilter graveyardFilter) {
                validateGraveyardCardTarget(gameData, sourceCard, graveyardFilter, targetId, playerId, xValue);
                continue;
            }

            // Permanent-targeting position
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                throw new IllegalStateException("Invalid target");
            }

            String peaceTalks = peaceTalksUntargetableReason(gameData);
            if (peaceTalks != null) {
                throw new IllegalStateException(peaceTalks);
            }
            validatePermanentTargetable(gameData, target, playerId);
            if (positionFilter == null && declaredRestriction != null
                    && !predicateEvaluationService.matchesPermanentPredicate(target, declaredRestriction,
                            filterContext(gameData, sourceCard.getId(), playerId))) {
                throw new IllegalStateException(target.getCard().getName() + " is not a legal target");
            }
            validateHexproofFromColor(gameData, target, sourceCard, playerId);

            // Can't be targeted by non-color sources (e.g. Gaea's Revenge)
            if (gameQueryService.cantBeTargetedByNonColorSources(gameData, target, sourceCard, playerId)) {
                throw new IllegalStateException(nonColorSourceRestrictionMessage(target));
            }

            if (gameQueryService.cantBeTargetedByWallOnlySources(gameData, target)
                    && sourceCanTargetOnlyWalls(sourceCard, abilityEffects, ability.getTargetFilter(), perPositionFilters)) {
                throw new IllegalStateException(target.getCard().getName()
                        + " can't be targeted by spells or abilities that can target only Walls");
            }

            // Per-position filter
            if (positionFilter != null) {
                predicateEvaluationService.validateTargetFilter(positionFilter, target,
                        filterContext(gameData, sourceCard.getId(), playerId));
            }
        }

        validateMultiTargetConstraint(gameData, ability.getMultiTargetConstraint(), targetIds);
        validateFlagbearerTargetChoiceForMultiAbility(gameData, playerId, ability, abilityEffects,
                targetIds, sourceCard, xValue);
    }

    public void validateActivatedAbilityTargeting(GameData gameData,
                                                  UUID playerId,
                                                  ActivatedAbility ability,
                                                  List<CardEffect> abilityEffects,
                                                  UUID targetId,
                                                  Zone targetZone,
                                                  Card sourceCard,
                                                  int xValue) {
        validateActivatedAbilityTargeting(gameData, playerId, ability, abilityEffects, targetId, targetZone,
                sourceCard, xValue, hasCostDerivedManaValueTarget(abilityEffects));
    }

    public void validateActivatedAbilityTargetingAfterCostSelection(GameData gameData,
                                                                     UUID playerId,
                                                                     ActivatedAbility ability,
                                                                     List<CardEffect> abilityEffects,
                                                                     UUID targetId,
                                                                     Zone targetZone,
                                                                     Card sourceCard,
                                                                     int xValue) {
        validateActivatedAbilityTargeting(gameData, playerId, ability, abilityEffects, targetId, targetZone,
                sourceCard, xValue, false);
    }

    private void validateActivatedAbilityTargeting(GameData gameData,
                                                   UUID playerId,
                                                   ActivatedAbility ability,
                                                   List<CardEffect> abilityEffects,
                                                   UUID targetId,
                                                   Zone targetZone,
                                                   Card sourceCard,
                                                   int xValue,
                                                   boolean deferCostDerivedXValueChecks) {
        // "Up to N" abilities (minTargets=0) allow choosing zero targets (CR 115.1d)
        if (ability.getMinTargets() == 0 && targetId == null) {
            return;
        }
        boolean hasLegacyTargetFilter = ability.getTargetFilter() != null
                || !ability.getMultiTargetFilters().isEmpty();
        if (targetId == null && hasLegacyTargetFilter) {
            throw new IllegalStateException("A target is required");
        }

        targetValidationService.validateEffectTargets(abilityEffects,
                new TargetValidationContext(gameData, targetId, targetZone, sourceCard, xValue,
                        playerId, null, deferCostDerivedXValueChecks));

        if (ability.getTargetFilter() != null && targetId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                predicateEvaluationService.validateTargetFilter(ability.getTargetFilter(),
                        target,
                        filterContext(gameData, sourceCard.getId(), playerId).withXValue(xValue));
            } else if (gameData.playerIds.contains(targetId)
                    && ability.getTargetFilter() instanceof PlayerPredicateTargetFilter playerFilter) {
                validatePlayerPredicate(gameData, playerId, targetId, playerFilter.predicate(), playerFilter.errorMessage(),
                        findSourcePermanentIdByCardId(gameData, sourceCard.getId()));
            } else if (gameData.playerIds.contains(targetId)
                    && ability.getTargetFilter() instanceof AnyTargetPredicateTargetFilter anyFilter) {
                validatePlayerPredicate(gameData, playerId, targetId, anyFilter.playerPredicate(), anyFilter.errorMessage(),
                        findSourcePermanentIdByCardId(gameData, sourceCard.getId()));
            }
        } else if (!ability.getMultiTargetFilters().isEmpty()) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                predicateEvaluationService.validateTargetFilter(ability.getMultiTargetFilters().getFirst(),
                        target,
                        filterContext(gameData, sourceCard.getId(), playerId).withXValue(xValue));
            }
        }

        validateTargetable(gameData, targetId, playerId);

        Permanent protectedTarget = gameQueryService.findPermanentById(gameData, targetId);
        if (protectedTarget != null) {
            for (CardColor color : effectiveSourceColors(gameData, sourceCard)) {
                if (gameQueryService.hasProtectionFrom(gameData, protectedTarget, color)) {
                    throw new IllegalStateException(protectedTarget.getCard().getName()
                            + " has protection from " + color.name().toLowerCase());
                }
            }
            if (gameQueryService.hasProtectionFromSource(gameData, protectedTarget, sourceCard, playerId)) {
                throw new IllegalStateException(protectedTarget.getCard().getName()
                        + " has protection from this source");
            }
        }

        if (targetId != null && (gameQueryService.findPermanentById(gameData, targetId) != null
                || gameData.playerIds.contains(targetId))) {
            String peaceTalks = peaceTalksUntargetableReason(gameData);
            if (peaceTalks != null) {
                throw new IllegalStateException(peaceTalks);
            }
        }

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
            if (target != null && gameQueryService.cantBeTargetedByNonColorSources(
                    gameData, target, sourceCard, playerId)) {
                throw new IllegalStateException(nonColorSourceRestrictionMessage(target));
            }
            if (target != null && gameQueryService.cantBeTargetedByWallOnlySources(gameData, target)
                    && sourceCanTargetOnlyWalls(sourceCard, abilityEffects, ability.getTargetFilter())) {
                throw new IllegalStateException(target.getCard().getName()
                        + " can't be targeted by spells or abilities that can target only Walls");
            }
        }

        if (violatesFlagbearerTargetChoiceForAbility(gameData, playerId, ability, abilityEffects,
                targetId, sourceCard, xValue)) {
            throw new IllegalStateException("Must target a Flagbearer if able");
        }

    }

    private boolean hasCostDerivedManaValueTarget(List<CardEffect> effects) {
        boolean tracksSacrificedManaValue = effects.stream().anyMatch(effect ->
                effect instanceof SacrificeCreatureCost creatureCost && creatureCost.trackSacrificedManaValue()
                        || effect instanceof SacrificePermanentCost permanentCost && permanentCost.trackSacrificedManaValue());
        return tracksSacrificedManaValue && effects.stream().anyMatch(effect ->
                effect instanceof ReturnCardFromGraveyardEffect returnEffect
                        && (returnEffect.requiresManaValueEqualsX() || returnEffect.requiresManaValueAtMostX()));
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

    public void validateSpellTargeting(GameData gameData, Card card, List<CardEffect> spellEffects,
                                       UUID targetId, Zone targetZone, UUID controllerId,
                                       boolean needsTarget, int xValue) {
        checkSpellTargeting(gameData, card, spellEffects, targetId, targetZone, controllerId, needsTarget, xValue)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public void validateSpellTargeting(GameData gameData, Card card, List<CardEffect> spellEffects,
                                       UUID targetId, Zone targetZone, UUID controllerId,
                                       boolean needsTarget, int xValue, boolean kicked) {
        checkSpellTargeting(gameData, card, spellEffects, targetId, targetZone, controllerId,
                needsTarget, xValue, kicked)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public void validateSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone,
                                       UUID controllerId, boolean needsTarget, int xValue, boolean kicked) {
        checkSpellTargeting(gameData, card, targetId, targetZone, controllerId, needsTarget, xValue, kicked)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public Optional<String> checkSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone,
                                                UUID controllerId, boolean needsTarget, int xValue,
                                                boolean kicked, boolean castForMadnessCost) {
        return checkSpellTargeting(gameData, card, card.getEffects(EffectSlot.SPELL), targetId, targetZone,
                controllerId, needsTarget, xValue, kicked, castForMadnessCost);
    }

    public Optional<String> checkSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone, UUID controllerId) {
        return checkSpellTargeting(gameData, card, targetId, targetZone, controllerId, EffectResolution.needsTarget(card), 0);
    }

    private Optional<String> checkSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone, UUID controllerId, boolean needsTarget) {
        return checkSpellTargeting(gameData, card, targetId, targetZone, controllerId, needsTarget, 0);
    }

    private Optional<String> checkSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone, UUID controllerId, boolean needsTarget, int xValue) {
        return checkSpellTargeting(gameData, card, card.getEffects(EffectSlot.SPELL), targetId, targetZone,
                controllerId, needsTarget, xValue);
    }

    private Optional<String> checkSpellTargeting(GameData gameData, Card card, List<CardEffect> spellEffects,
                                                 UUID targetId, Zone targetZone, UUID controllerId,
                                                 boolean needsTarget, int xValue) {
        return checkSpellTargeting(gameData, card, spellEffects, targetId, targetZone, controllerId,
                needsTarget, xValue, false, false);
    }

    private Optional<String> checkSpellTargeting(GameData gameData, Card card, UUID targetId, Zone targetZone,
                                                 UUID controllerId, boolean needsTarget, int xValue, boolean kicked) {
        return checkSpellTargeting(gameData, card, card.getEffects(EffectSlot.SPELL), targetId, targetZone,
                controllerId, needsTarget, xValue, kicked, false);
    }

    private Optional<String> checkSpellTargeting(GameData gameData, Card card, List<CardEffect> spellEffects,
                                                 UUID targetId, Zone targetZone, UUID controllerId,
                                                 boolean needsTarget, int xValue, boolean kicked) {
        return checkSpellTargeting(gameData, card, spellEffects, targetId, targetZone, controllerId,
                needsTarget, xValue, kicked, false);
    }

    private Optional<String> checkSpellTargeting(GameData gameData, Card card, List<CardEffect> spellEffects,
                                                 UUID targetId, Zone targetZone, UUID controllerId,
                                                 boolean needsTarget, int xValue, boolean kicked,
                                                 boolean castForMadnessCost) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null && !gameData.playerIds.contains(targetId)) {
            return Optional.of("Invalid target");
        }

        TargetFilter effectiveTargetFilter = targetFilterForKickedCast(card.getTargetFilter(), kicked);

        if (target != null && effectiveTargetFilter instanceof PlayerPredicateTargetFilter) {
            return Optional.of("This spell can only target players");
        }

        if (needsTarget) {
            // Skip target-type validation for modal spells: their modes have already been
            // unwrapped by SpellCastingService and the mode-specific effects/filters handle
            // validation downstream.  computeAllowedTargets(card) uses the raw (unresolved)
            // ChooseOneEffect which doesn't expose inner target types.
            boolean isModal = spellEffects.stream()
                    .anyMatch(ChooseOneEffect.class::isInstance);
            if (!isModal) {
                Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(
                        spellEffects, card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD), card.isAura(), card.isEnchantPlayer());

                if (target != null && !allowedTargets.contains(TargetType.PERMANENT)
                        && !targetFilterAllowsPermanent(effectiveTargetFilter)) {
                    return Optional.of("This spell can only target players");
                }
                if (target == null && gameData.playerIds.contains(targetId)
                        && !allowedTargets.contains(TargetType.PLAYER)
                        && !targetFilterAllowsPlayer(effectiveTargetFilter)) {
                    return Optional.of("This spell cannot target players");
                }
            }
        }

        boolean declaresPermanentTarget = needsTarget
                || effectiveTargetFilter != null
                || !card.getSpellTargets().isEmpty();
        if (target != null && declaresPermanentTarget) {
            Optional<String> structuralReason = checkSpellPermanentTargetableReason(
                    gameData, target, card, controllerId, spellEffects, effectiveTargetFilter);
            if (structuralReason.isPresent()) return structuralReason;
        }

        if (target == null && needsTarget && gameData.playerIds.contains(targetId)) {
            String peaceTalks = peaceTalksUntargetableReason(gameData);
            if (peaceTalks != null) return Optional.of(peaceTalks);
            String playerReason = checkPlayerUntargetableReason(gameData, targetId, controllerId, card);
            if (playerReason != null) return Optional.of(playerReason);
            CardColor effectiveColor = gameQueryService.getEffectiveCardColor(gameData, card);
            if (effectiveColor != null
                    && gameQueryService.playerHasProtectionFromColor(gameData, targetId, effectiveColor)) {
                return Optional.of(gameData.playerIdToName.get(targetId)
                        + " has protection from " + effectiveColor.name().toLowerCase());
            }
            if (card != null
                    && gameQueryService.playerHasProtectionFromChosenName(gameData, targetId, card.getName())) {
                return Optional.of(gameData.playerIdToName.get(targetId)
                        + " has protection from " + card.getName());
            }
        }

        if (target == null
                && effectiveTargetFilter instanceof PlayerPredicateTargetFilter playerFilter
                && !matchesPlayerPredicate(gameData, controllerId, targetId, playerFilter.predicate())) {
            return Optional.of(playerFilter.errorMessage());
        }

        if (target == null
                && effectiveTargetFilter instanceof AnyTargetPredicateTargetFilter anyFilter
                && !matchesPlayerPredicate(gameData, controllerId, targetId, anyFilter.playerPredicate())) {
            return Optional.of(anyFilter.errorMessage());
        }

        if (effectiveTargetFilter != null && target != null) {
            Optional<String> filterReason = predicateEvaluationService.checkTargetFilter(effectiveTargetFilter,
                    target,
                    filterContext(gameData, card.getId(), controllerId).withXValue(xValue)
                            .withMadness(castForMadnessCost));
            if (filterReason.isPresent()) return filterReason;
        }

        Optional<String> effectReason = targetValidationService.checkEffectTargets(spellEffects,
                new TargetValidationContext(gameData, targetId, targetZone, card, xValue, controllerId, null));
        if (effectReason.isPresent()) return effectReason;

        return Optional.empty();
    }

    private static boolean targetFilterAllowsPlayer(TargetFilter targetFilter) {
        return targetFilter instanceof AnyTargetPredicateTargetFilter
                || targetFilter instanceof PlayerPredicateTargetFilter;
    }

    private static boolean targetFilterAllowsPermanent(TargetFilter targetFilter) {
        return targetFilter instanceof AnyTargetPredicateTargetFilter
                || targetFilter instanceof ControlledPermanentPredicateTargetFilter
                || targetFilter instanceof OwnedPermanentPredicateTargetFilter
                || targetFilter instanceof PermanentPredicateTargetFilter;
    }

    public void validateFlagbearerTargetChoiceForSpellCast(GameData gameData, Card card,
                                                            List<CardEffect> spellEffects,
                                                            UUID targetId, List<UUID> targetIds,
                                                            UUID controllerId, int xValue,
                                                            boolean kicked) {
        if (!gameQueryService.hasFlagbearerControlledByOpponent(gameData, controllerId)) {
            return;
        }
        if (containsFlagbearer(gameData, targetId, targetIds)) {
            return;
        }
        if (targetId == null && targetIds.isEmpty()) {
            return;
        }

        if (targetId != null && targetIds.isEmpty()) {
            TargetFilter targetFilter = targetFilterForKickedCast(card.getTargetFilter(), kicked);
            if (hasLegalFlagbearerSpellTarget(gameData, card, spellEffects, controllerId, xValue, targetFilter)) {
                throw new IllegalStateException("Must target a Flagbearer if able");
            }
            return;
        }

        int firstGroupIndex = targetId == null || card.getSpellTargets().isEmpty() ? 0 : 1;
        if (targetId != null) {
            TargetFilter targetFilter = card.getSpellTargets().isEmpty()
                    ? card.getTargetFilter() : card.getSpellTargets().getFirst().getFilter();
            if (hasLegalFlagbearerSpellTarget(gameData, card, spellEffects, controllerId, xValue,
                    targetFilterForKickedCast(targetFilter, kicked))) {
                throw new IllegalStateException("Must target a Flagbearer if able");
            }
        }
        validateFlagbearerTargetChoiceForMultiSpell(gameData, card, spellEffects, targetIds,
                controllerId, xValue, kicked, firstGroupIndex);
    }

    private boolean containsFlagbearer(GameData gameData, UUID targetId, List<UUID> targetIds) {
        return java.util.stream.Stream.concat(
                        targetId == null ? java.util.stream.Stream.empty() : java.util.stream.Stream.of(targetId),
                        targetIds.stream())
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .anyMatch(permanent -> permanent != null && gameQueryService.isFlagbearer(gameData, permanent));
    }

    private boolean hasLegalFlagbearerSpellTarget(GameData gameData, Card card,
                                                   List<CardEffect> spellEffects, UUID controllerId,
                                                   int xValue, TargetFilter targetFilter) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> gameQueryService.isFlagbearer(gameData, permanent))
                .anyMatch(permanent -> isLegalFlagbearerSpellTarget(
                        gameData, card, spellEffects, permanent, controllerId, xValue, targetFilter));
    }

    private boolean isLegalFlagbearerSpellTarget(GameData gameData, Card card,
                                                  List<CardEffect> spellEffects, Permanent candidate,
                                                  UUID controllerId, int xValue, TargetFilter targetFilter) {
        if (!EffectResolution.computeAllowedTargets(
                spellEffects, card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD),
                card.isAura(), card.isEnchantPlayer()).contains(TargetType.PERMANENT)) {
            return false;
        }
        if (checkSpellPermanentTargetableReason(
                gameData, candidate, card, controllerId, spellEffects, targetFilter).isPresent()) {
            return false;
        }
        if (targetFilter != null && (targetFilter instanceof PlayerPredicateTargetFilter
                || targetFilter instanceof GraveyardCardPredicateTargetFilter
                || targetFilter instanceof StackEntryPredicateTargetFilter)) {
            return false;
        }
        if (targetFilter != null
                && predicateEvaluationService.checkTargetFilter(
                        targetFilter, candidate,
                        filterContext(gameData, card.getId(), controllerId).withXValue(xValue)).isPresent()) {
            return false;
        }
        if (targetFilter == null) {
            PermanentPredicate declaredRestriction = EffectResolution
                    .declaredPermanentRestriction(spellEffects).orElse(null);
            if (declaredRestriction != null
                    && !predicateEvaluationService.matchesPermanentPredicate(
                            candidate, declaredRestriction, filterContext(gameData, card.getId(), controllerId))) {
                return false;
            }
        }
        return targetValidationService.checkEffectTargets(spellEffects,
                new TargetValidationContext(gameData, candidate.getId(), Zone.BATTLEFIELD,
                        card, xValue, controllerId, null)).isEmpty();
    }

    private void validateFlagbearerTargetChoiceForMultiSpell(GameData gameData, Card card,
                                                              List<CardEffect> spellEffects,
                                                              List<UUID> targetIds, UUID controllerId,
                                                              int xValue, boolean kicked,
                                                              int firstGroupIndex) {
        if (!gameQueryService.hasFlagbearerControlledByOpponent(gameData, controllerId)
                || targetIds.stream().map(id -> gameQueryService.findPermanentById(gameData, id))
                .anyMatch(permanent -> permanent != null && gameQueryService.isFlagbearer(gameData, permanent))) {
            return;
        }
        List<TargetFilter> positionFilters = card.getSpellTargets().stream()
                .filter(group -> group.getIndex() >= firstGroupIndex)
                .flatMap(group -> java.util.stream.IntStream.range(0, group.getMaxTargets())
                        .mapToObj(ignored -> group.getFilter()))
                .toList();
        List<TargetFilter> selectedPositionFilters = positionFilters.stream()
                .limit(targetIds.size())
                .toList();
        boolean legalFlagbearer = gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> gameQueryService.isFlagbearer(gameData, permanent))
                .anyMatch(permanent -> selectedPositionFilters.stream().anyMatch(positionFilter ->
                        isLegalFlagbearerSpellTarget(gameData, card, spellEffects, permanent,
                                controllerId, xValue,
                                positionFilter != null
                                        ? targetFilterForKickedCast(positionFilter, kicked)
                                        : targetFilterForKickedCast(card.getTargetFilter(), kicked))));
        if (legalFlagbearer) {
            throw new IllegalStateException("Must target a Flagbearer if able");
        }
    }

    private boolean violatesFlagbearerTargetChoiceForAbility(GameData gameData, UUID playerId,
                                                              ActivatedAbility ability,
                                                              List<CardEffect> abilityEffects,
                                                              UUID targetId, Card sourceCard, int xValue) {
        if (targetId == null || !gameQueryService.hasFlagbearerControlledByOpponent(gameData, playerId)) {
            return false;
        }
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, targetId);
        if (chosenPermanent != null && gameQueryService.isFlagbearer(gameData, chosenPermanent)) {
            return false;
        }
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> gameQueryService.isFlagbearer(gameData, permanent))
                .anyMatch(permanent -> isLegalFlagbearerAbilityTarget(
                        gameData, playerId, ability, abilityEffects, permanent, sourceCard, xValue,
                        ability.getTargetFilter()));
    }

    private boolean isLegalFlagbearerAbilityTarget(GameData gameData, UUID playerId,
                                                    ActivatedAbility ability,
                                                    List<CardEffect> abilityEffects,
                                                    Permanent candidate, Card sourceCard, int xValue,
                                                    TargetFilter targetFilter) {
        if (abilityEffects.stream().noneMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))) {
            return false;
        }
        if (peaceTalksUntargetableReason(gameData) != null || untargetableReason(gameData, candidate, playerId) != null) {
            return false;
        }
        UUID candidateController = gameQueryService.findPermanentController(gameData, candidate.getId());
        if (gameQueryService.cantBeTargetOfOpponentAbilities(gameData, candidate)
                && candidateController != null && !candidateController.equals(playerId)) {
            return false;
        }
        if (hexproofFromColorReason(gameData, candidate, sourceCard, playerId) != null
                || gameQueryService.cantBeTargetedByNonColorSources(gameData, candidate, sourceCard)
                || gameQueryService.cantBeTargetedByWallOnlySources(gameData, candidate)
                && sourceCanTargetOnlyWalls(sourceCard, abilityEffects, ability.getTargetFilter())) {
            return false;
        }
        if (targetFilter instanceof PlayerPredicateTargetFilter
                || targetFilter instanceof GraveyardCardPredicateTargetFilter
                || targetFilter instanceof StackEntryPredicateTargetFilter) {
            return false;
        }
        if (targetFilter != null
                && predicateEvaluationService.checkTargetFilter(
                        targetFilter, candidate,
                        filterContext(gameData, sourceCard.getId(), playerId).withXValue(xValue)).isPresent()) {
            return false;
        }
        if (targetFilter == null) {
            PermanentPredicate declaredRestriction = EffectResolution
                    .declaredPermanentRestriction(abilityEffects).orElse(null);
            if (declaredRestriction != null
                    && !predicateEvaluationService.matchesPermanentPredicate(
                            candidate, declaredRestriction, filterContext(gameData, sourceCard.getId(), playerId))) {
                return false;
            }
        }
        return targetValidationService.checkEffectTargets(abilityEffects,
                new TargetValidationContext(gameData, candidate.getId(), Zone.BATTLEFIELD,
                        sourceCard, xValue, playerId, null)).isEmpty();
    }

    private void validateFlagbearerTargetChoiceForMultiAbility(GameData gameData, UUID playerId,
                                                               ActivatedAbility ability,
                                                               List<CardEffect> abilityEffects,
                                                               List<UUID> targetIds, Card sourceCard,
                                                               int xValue) {
        if (!gameQueryService.hasFlagbearerControlledByOpponent(gameData, playerId)
                || targetIds.stream().map(id -> gameQueryService.findPermanentById(gameData, id))
                .anyMatch(permanent -> permanent != null && gameQueryService.isFlagbearer(gameData, permanent))) {
            return;
        }
        int positionCount = targetIds.size();
        boolean legalFlagbearer = gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> gameQueryService.isFlagbearer(gameData, permanent))
                .anyMatch(permanent -> java.util.stream.IntStream.range(0, positionCount)
                        .mapToObj(index -> index < ability.getMultiTargetFilters().size()
                                ? ability.getMultiTargetFilters().get(index) : ability.getTargetFilter())
                        .anyMatch(positionFilter -> isLegalFlagbearerAbilityTarget(
                                gameData, playerId, ability, abilityEffects, permanent, sourceCard, xValue,
                                positionFilter)));
        if (legalFlagbearer) {
            throw new IllegalStateException("Must target a Flagbearer if able");
        }
    }

    public void validateEffectTargetInZone(GameData gameData, Card card, UUID targetId, Zone targetZone) {
        targetValidationService.validateEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetId, targetZone, card));
    }

    public void validateEffectTargetInZone(GameData gameData, Card card, UUID targetId, Zone targetZone,
                                           UUID sourceControllerId) {
        targetValidationService.validateEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetId, targetZone, card, 0, sourceControllerId, null));
    }

    public void validateEffectTargetInZone(GameData gameData, Card card, UUID targetId, Zone targetZone, int xValue) {
        targetValidationService.validateEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetId, targetZone, card, xValue));
    }

    public void validateEffectTargetInZone(GameData gameData, Card card, UUID targetId, Zone targetZone,
                                           int xValue, UUID sourceControllerId) {
        targetValidationService.validateEffectTargets(card.getEffects(EffectSlot.SPELL),
                new TargetValidationContext(gameData, targetId, targetZone, card, xValue,
                        sourceControllerId, null));
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

    public void validateEffectTargetInZone(GameData gameData, Card card, List<CardEffect> effects,
                                           UUID targetId, Zone targetZone, int xValue,
                                           UUID sourceControllerId) {
        targetValidationService.validateEffectTargets(effects,
                new TargetValidationContext(gameData, targetId, targetZone, card, xValue,
                        sourceControllerId, null));
    }

    /**
     * Validates only the graveyard-targeting effects of a spell, ignoring permanent-targeting effects.
     * Used for spells with mixed graveyard + permanent targets (e.g. Yawgmoth's Vile Offering)
     * where each target type is validated separately.
     */
    public void validateGraveyardEffectTargetOnly(GameData gameData, Card card, UUID targetId) {
        validateGraveyardEffectTargetOnly(gameData, card, card.getEffects(EffectSlot.SPELL), targetId, 0);
    }

    /**
     * Mixed graveyard + permanent validation against an explicit effect list (modal unwrap) and
     * the announced X (Profane Command's MV ≤ X reanimate mode).
     */
    public void validateGraveyardEffectTargetOnly(GameData gameData, Card card, List<CardEffect> effects,
                                                  UUID targetId, int xValue) {
        validateGraveyardEffectTargetOnly(gameData, card, effects, targetId, xValue, null);
    }

    public void validateGraveyardEffectTargetOnly(GameData gameData, Card card, List<CardEffect> effects,
                                                  UUID targetId, int xValue, UUID sourceControllerId) {
        List<CardEffect> graveyardEffects = effects.stream()
                .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                // Unwrap conditional reanimation (e.g. Torrent of Souls' "if {B} was spent") so the
                // inner effect's card-type filter is enforced when the graveyard target is chosen.
                .map(e -> e instanceof ConditionalEffect conditional ? conditional.wrapped() : e)
                .toList();
        targetValidationService.validateEffectTargets(graveyardEffects,
                new TargetValidationContext(gameData, targetId, Zone.GRAVEYARD, card, xValue,
                        sourceControllerId, null));
    }

    public void validateMultiSpellTargets(GameData gameData, Card card, List<UUID> targetIds, UUID controllerId) {
        validateMultiSpellTargets(gameData, card, targetIds, controllerId, 0, false);
    }

    public void validateMultiSpellTargets(GameData gameData, Card card, List<UUID> targetIds, UUID controllerId, int xValue) {
        validateMultiSpellTargets(gameData, card, targetIds, controllerId, xValue, false);
    }

    public void validateMultiSpellTargets(GameData gameData, Card card, List<UUID> targetIds,
                                          UUID controllerId, int xValue, boolean kicked) {
        validateMultiSpellTargets(gameData, card, targetIds, controllerId, xValue, kicked, 0);
    }

    /**
     * Validates the targets chosen for one spell target group when the number of targets in an
     * earlier group is supplied separately, such as a divided-damage assignment map.
     */
    public void validateSpellTargetGroup(GameData gameData, Card card, int groupIndex,
                                         List<UUID> targetIds, UUID controllerId,
                                         int xValue, boolean kicked) {
        SpellTarget group = card.getSpellTargets().stream()
                .filter(candidate -> candidate.getIndex() == groupIndex)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown spell target group"));
        int minTargets = kicked ? group.getKickedMinTargets() : group.getMinTargets();
        if (group.isXScaled()) {
            minTargets = Math.min(xValue, minTargets);
        }
        int maxTargets = effectiveGroupMaxTargets(gameData, controllerId, null, group, xValue, kicked);
        validateMultiTargetCount(targetIds, minTargets, maxTargets);

        int positionOffset = card.getSpellTargets().stream()
                .filter(candidate -> candidate.getIndex() < groupIndex)
                .mapToInt(candidate -> effectiveGroupMaxTargets(
                        gameData, controllerId, null, candidate, xValue, kicked))
                .sum();
        PermanentPredicate declaredRestriction = EffectResolution
                .declaredPermanentRestriction(card.getEffects(EffectSlot.SPELL)).orElse(null);
        for (int i = 0; i < targetIds.size(); i++) {
            validateMultiSpellTargetPosition(gameData, card, targetIds.get(i), controllerId,
                    positionOffset + i, groupIndex, group.getFilter(), declaredRestriction, kicked);
        }
    }

    /**
     * Validates the permanent target groups that follow a separately stored primary target.
     */
    public void validateMixedSpellAndPermanentTargets(GameData gameData, Card card, List<UUID> targetIds,
                                                       UUID controllerId, int xValue) {
        if (card.getSpellTargets().size() <= 1) {
            for (UUID targetId : targetIds) {
                validateSpellTargeting(gameData, card, targetId, null, controllerId, true, xValue);
            }
            return;
        }
        validateMultiSpellTargets(gameData, card, targetIds, controllerId, xValue, false, 1);
    }

    public void validateSpellTargetGroupsAfterPrimary(GameData gameData, Card card,
                                                       List<UUID> targetIds, UUID controllerId,
                                                       int xValue, boolean kicked) {
        validateMultiSpellTargets(gameData, card, targetIds, controllerId, xValue, kicked, 1);
    }

    private void validateMultiSpellTargets(GameData gameData, Card card, List<UUID> targetIds,
                                           UUID controllerId, int xValue, boolean kicked, int firstGroupIndex) {
        List<SpellTarget> targetGroups = card.getSpellTargets().stream()
                .filter(group -> group.getIndex() >= firstGroupIndex)
                .toList();
        int minTargets = targetGroups.stream()
                .mapToInt(group -> {
                    int min = kicked ? group.getKickedMinTargets() : group.getMinTargets();
                    return group.isXScaled() ? Math.min(xValue, min) : min;
                })
                .sum();
        int maxTargets = targetGroups.stream()
                .mapToInt(group -> effectiveGroupMaxTargets(
                        gameData, controllerId, null, group, xValue, kicked))
                .sum();
        validateMultiTargetCount(targetIds, minTargets, maxTargets, targetGroups, card.isAllowSharedTargets());

        if (card.getMultiTargetConstraint() == MultiTargetConstraint.AT_MOST_ONE_PER_COLOR) {
            GraveyardCardPredicateTargetFilter ownGraveyardCards =
                    new GraveyardCardPredicateTargetFilter(null,
                            com.github.laxika.magicalvibes.model.GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
            for (UUID targetId : targetIds) {
                validateGraveyardCardTarget(gameData, card, ownGraveyardCards, targetId, controllerId, xValue);
            }
            if (targetGroupAssignmentService.assignDistinctColors(gameData, targetIds).isEmpty()) {
                throw new IllegalStateException("Must choose at most one card for each color");
            }
            return;
        }

        List<TargetFilter> perPositionFilters = targetGroups.stream()
                .flatMap(group -> java.util.stream.IntStream.range(0,
                                Math.max(group.getMaxTargets(), group.getKickedMaxTargets()))
                        .mapToObj(ignored -> group.getFilter()))
                .toList();
        int positionOffset = card.getSpellTargets().stream()
                .filter(group -> group.getIndex() < firstGroupIndex)
                .mapToInt(SpellTarget::getMaxTargets)
                .sum();
        // The restriction an unfiltered position inherits from the spell's own effects, read the
        // same way target enumeration reads it (ValidTargetService.isValidPermanentTarget).
        PermanentPredicate declaredRestriction = EffectResolution
                .declaredPermanentRestriction(card.getEffects(EffectSlot.SPELL)).orElse(null);
        for (int i = 0; i < targetIds.size(); i++) {
            UUID targetId = targetIds.get(i);

            // Player-targeting position
            if (gameData.playerIds.contains(targetId)) {
                if (!card.doesPositionAllowPlayerTargets(positionOffset + i)) {
                    throw new IllegalStateException("This spell cannot target players");
                }
                TargetFilter playerSlotFilter = getPositionFilter(perPositionFilters, i);
                if (playerSlotFilter instanceof AnyTargetPredicateTargetFilter anyFilter) {
                    validatePlayerTargetable(gameData, targetId, controllerId, card);
                    validatePlayerPredicate(gameData, controllerId, targetId, anyFilter.playerPredicate(),
                            anyFilter.errorMessage());
                    continue;
                }
                if (playerSlotFilter instanceof PlayerPredicateTargetFilter playerFilter) {
                    validatePlayerTargetable(gameData, targetId, controllerId, card);
                    validatePlayerPredicate(gameData, controllerId, targetId, playerFilter.predicate(),
                            playerFilter.errorMessage());
                    continue;
                }
                if (EffectResolution.needsTarget(card)) {
                    validatePlayerTargetable(gameData, targetId, controllerId, card);
                }
                continue;
            }

            // Graveyard-targeting position (the group declares which graveyards and which cards)
            TargetFilter slotFilter = getPositionFilter(perPositionFilters, i);
            if (slotFilter instanceof GraveyardCardPredicateTargetFilter graveyardFilter) {
                validateGraveyardCardTarget(gameData, card, graveyardFilter, targetId, controllerId, xValue);
                continue;
            }

            // Permanent-targeting position
            if (isSpellOnStack(gameData, targetId)) {
                validateSpellTargetOnStack(gameData, targetId, slotFilter, controllerId);
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                throw new IllegalStateException("Invalid target");
            }

            // Apply per-position target filter if available; otherwise fall back to the card-level
            // targetFilter, then to what the spell's own effects declare — "any target" (CR 115.4)
            // makes a planeswalker legal here, which the creature-only default below rejected even
            // though enumeration offered it. That default now applies only to a slot no effect
            // restricts.
            TargetFilter positionFilter = slotFilter;
            if (positionFilter != null) {
                predicateEvaluationService.validateTargetFilter(targetFilterForKickedCast(positionFilter, kicked), target,
                        filterContext(gameData, card.getId(), controllerId));
            } else if (card.getTargetFilter() != null) {
                predicateEvaluationService.validateTargetFilter(targetFilterForKickedCast(card.getTargetFilter(), kicked), target,
                        filterContext(gameData, card.getId(), controllerId));
            } else if (declaredRestriction != null) {
                if (!predicateEvaluationService.matchesPermanentPredicate(target, declaredRestriction,
                        filterContext(gameData, card.getId(), controllerId))) {
                    throw new IllegalStateException(target.getCard().getName() + " is not a legal target");
                }
            } else if (!gameQueryService.isCreature(gameData, target)) {
                throw new IllegalStateException(target.getCard().getName() + " is not a creature");
            }

            if (EffectResolution.needsTarget(card)) {
                checkSpellPermanentTargetableReason(gameData, target, card, controllerId,
                        card.getEffects(EffectSlot.SPELL), positionFilter)
                        .ifPresent(reason -> { throw new IllegalStateException(reason); });
            }
        }

        validateMultiTargetConstraint(gameData, card.getMultiTargetConstraint(), targetIds);
    }

    private void validateMultiSpellTargetPosition(GameData gameData, Card card, UUID targetId,
                                                   UUID controllerId, int positionIndex,
                                                   int groupIndex, TargetFilter positionFilter,
                                                   PermanentPredicate declaredRestriction,
                                                   boolean kicked) {
        if (gameData.playerIds.contains(targetId)) {
            if (!card.doesPositionAllowPlayerTargets(positionIndex)) {
                throw new IllegalStateException("This spell cannot target players");
            }
            if (positionFilter instanceof AnyTargetPredicateTargetFilter anyFilter) {
                validatePlayerTargetable(gameData, targetId, controllerId, card);
                validatePlayerPredicate(gameData, controllerId, targetId, anyFilter.playerPredicate(),
                        anyFilter.errorMessage());
                return;
            }
            if (positionFilter instanceof PlayerPredicateTargetFilter playerFilter) {
                validatePlayerTargetable(gameData, targetId, controllerId, card);
                validatePlayerPredicate(gameData, controllerId, targetId, playerFilter.predicate(),
                        playerFilter.errorMessage());
                return;
            }
            if (EffectResolution.needsTarget(card)) {
                validatePlayerTargetable(gameData, targetId, controllerId, card);
            }
            return;
        }

        if (positionFilter instanceof GraveyardCardPredicateTargetFilter graveyardFilter) {
            validateGraveyardCardTarget(gameData, card, graveyardFilter, targetId, controllerId);
            return;
        }
        if (isSpellOnStack(gameData, targetId)) {
            validateSpellTargetOnStack(gameData, targetId, positionFilter, controllerId);
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            throw new IllegalStateException("Invalid target");
        }

        if (positionFilter != null) {
            predicateEvaluationService.validateTargetFilter(targetFilterForKickedCast(positionFilter, kicked), target,
                    filterContext(gameData, card.getId(), controllerId));
        } else if (card.getTargetFilter() != null) {
            predicateEvaluationService.validateTargetFilter(targetFilterForKickedCast(card.getTargetFilter(), kicked), target,
                    filterContext(gameData, card.getId(), controllerId));
        } else if (declaredRestriction != null) {
            if (!predicateEvaluationService.matchesPermanentPredicate(target, declaredRestriction,
                    filterContext(gameData, card.getId(), controllerId))) {
                throw new IllegalStateException(target.getCard().getName() + " is not a legal target");
            }
        } else if (!gameQueryService.isCreature(gameData, target)
                && !targetGroupAllowsPlaneswalkers(card, groupIndex)) {
            throw new IllegalStateException(target.getCard().getName() + " is not a creature");
        }

        if (EffectResolution.needsTarget(card)) {
            checkSpellPermanentTargetableReason(gameData, target, card, controllerId,
                    card.getEffects(EffectSlot.SPELL), positionFilter)
                    .ifPresent(reason -> { throw new IllegalStateException(reason); });
        }
    }

    private boolean targetGroupAllowsPlaneswalkers(Card card, int groupIndex) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(effect -> card.getEffectTargetIndex(effect) == groupIndex)
                .anyMatch(effect -> effect.targetSpec().declares(TargetPredicates.anyTarget())
                        || effect.targetSpec().declares(TargetPredicates.creatureOrPlaneswalker())
                        || effect.targetSpec().declares(TargetPredicates.playerOrPlaneswalker()));
    }

    /**
     * Returns the effective target cap after applying X-scaled and dynamic target-group limits.
     * A source permanent is supplied for deferred triggered-ability target selection; a null
     * source models cast-time targeting.
     */
    public int getEffectiveMaxTargets(GameData gameData, Card card, UUID controllerId, int xValue) {
        return getEffectiveMaxTargets(gameData, card, controllerId, null, xValue, false);
    }

    public int getEffectiveMaxTargets(GameData gameData, Card card, UUID controllerId,
                                      int xValue, boolean kicked) {
        return getEffectiveMaxTargets(gameData, card, controllerId, null, xValue, kicked);
    }

    public int getEffectiveMaxTargets(GameData gameData, Card card, UUID controllerId,
                                      Permanent sourcePermanent) {
        return getEffectiveMaxTargets(gameData, card, controllerId, sourcePermanent, 0, false);
    }

    public int getEffectiveMaxTargetsForGroup(GameData gameData, Card card, UUID controllerId,
                                              Permanent sourcePermanent, SpellTarget group) {
        return effectiveGroupMaxTargets(gameData, controllerId, sourcePermanent, group, 0);
    }

    private int getEffectiveMaxTargets(GameData gameData, Card card, UUID controllerId,
                                       Permanent sourcePermanent, int xValue, boolean kicked) {
        return card.getSpellTargets().stream()
                .mapToInt(group -> effectiveGroupMaxTargets(gameData, controllerId, sourcePermanent, group, xValue, kicked))
                .sum();
    }

    private int effectiveGroupMaxTargets(GameData gameData, UUID controllerId, Permanent sourcePermanent,
                                         SpellTarget group, int xValue) {
        return effectiveGroupMaxTargets(gameData, controllerId, sourcePermanent, group, xValue, false);
    }

    private int effectiveGroupMaxTargets(GameData gameData, UUID controllerId, Permanent sourcePermanent,
                                         SpellTarget group, int xValue, boolean kicked) {
        int declaredMax = kicked ? group.getKickedMaxTargets() : group.getMaxTargets();
        int max = group.isXScaled() ? Math.min(xValue, declaredMax) : declaredMax;
        if (group.getDynamicMaxTargets() == null) {
            return max;
        }
        int dynamicMax = amountEvaluationService.evaluate(gameData, group.getDynamicMaxTargets(),
                new AmountContext(controllerId, sourcePermanent, null, xValue, 0));
        return Math.min(max, Math.max(0, dynamicMax));
    }

    /**
     * Validates one target of a graveyard target group: the card must still be in a graveyard the
     * group's scope allows and match the group's card filter.
     */
    private void validateGraveyardCardTarget(GameData gameData, Card card,
                                             GraveyardCardPredicateTargetFilter filter,
                                             UUID targetId, UUID controllerId) {
        validateGraveyardCardTarget(gameData, card, filter, targetId, controllerId, null);
    }

    private void validateGraveyardCardTarget(GameData gameData, Card card,
                                             GraveyardCardPredicateTargetFilter filter,
                                             UUID targetId, UUID controllerId, Integer xValue) {
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
        UUID graveyardOwnerId = graveyardCard == null
                ? null
                : gameQueryService.findGraveyardOwnerById(gameData, targetId);
        if (graveyardOwnerId == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        switch (filter.scope()) {
            case CONTROLLERS_GRAVEYARD -> {
                if (!graveyardOwnerId.equals(controllerId)) {
                    throw new IllegalStateException("Target must be in your graveyard");
                }
            }
            case OPPONENT_GRAVEYARD -> {
                if (graveyardOwnerId.equals(controllerId)) {
                    throw new IllegalStateException("Target must be in an opponent's graveyard");
                }
            }
            case ALL_GRAVEYARDS -> { }
        }
        if (filter.predicate() != null
                && !predicateEvaluationService.matchesCardPredicate(graveyardCard, filter.predicate(), card.getId(),
                gameData, graveyardOwnerId, null, null, xValue)) {
            throw new IllegalStateException("Target must be a "
                    + CardPredicateUtils.describeFilter(filter.predicate()));
        }
    }

    /**
     * Enforces a spell's cross-target restriction on the whole chosen set beyond the per-position
     * filters.
     */
    private void validateMultiTargetConstraint(GameData gameData, MultiTargetConstraint constraint, List<UUID> targetIds) {
        if (constraint == null) {
            return;
        }
        if (constraint == MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET) {
            validateControlledByFirstTarget(gameData, targetIds);
            return;
        }
        if (constraint == MultiTargetConstraint.ATTACHED_TO_FIRST_TARGET) {
            validateAttachedToFirstTarget(gameData, targetIds);
            return;
        }
        if (constraint == MultiTargetConstraint.AT_MOST_TWO_CREATURES_AND_TWO_LANDS) {
            validateAtMostTwoCreaturesAndTwoLands(gameData, targetIds);
            return;
        }
        if (constraint == MultiTargetConstraint.AT_MOST_ONE_ARTIFACT_ONE_CREATURE_AND_ONE_LAND) {
            validateAtMostOneArtifactOneCreatureAndOneLand(gameData, targetIds);
            return;
        }
        if (constraint == MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER
                || constraint == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE) {
            validateAtMostOnePerController(gameData, targetIds);
            return;
        }
        if (constraint == MultiTargetConstraint.AT_MOST_ONE_INSTANT_AND_ONE_SORCERY) {
            validateAtMostOneInstantAndOneSorcery(gameData, targetIds);
            return;
        }
        if (constraint == MultiTargetConstraint.AT_MOST_ONE_CREATURE_AND_ONE_LAND) {
            validateAtMostOneCreatureAndOneLand(gameData, targetIds);
            return;
        }
        if (constraint == MultiTargetConstraint.SAME_CREATURE_OR_LAND_TYPE_AS_FIRST_AURA_HOST) {
            validateSameCreatureOrLandTypeAsFirstAuraHost(gameData, targetIds);
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
                    case SHARE_ARTIFACT_OR_CREATURE_TYPE -> {
                        if (!gameQueryService.sharesArtifactOrCreatureType(a, b)) {
                            throw new IllegalStateException(
                                    "Chosen permanents must share an artifact or creature type");
                        }
                    }
                    case SHARE_CARD_TYPE -> {
                        if (!gameQueryService.sharesCardType(a, b)) {
                            throw new IllegalStateException("Chosen permanents must share a card type");
                        }
                    }
                    case CONTROLLED_BY_FIRST_TARGET, AT_MOST_TWO_CREATURES_AND_TWO_LANDS,
                         AT_MOST_ONE_ARTIFACT_ONE_CREATURE_AND_ONE_LAND,
                         AT_MOST_ONE_PER_CONTROLLER, ONE_PER_CONTROLLER_IF_ABLE,
                         AT_MOST_ONE_INSTANT_AND_ONE_SORCERY, AT_MOST_ONE_CREATURE_AND_ONE_LAND -> {
                        // Handled by early returns above.
                    }
                }
            }
        }
    }

    private void validateAtMostOneInstantAndOneSorcery(GameData gameData, List<UUID> targetIds) {
        int instantCount = 0;
        int sorceryCount = 0;
        Set<UUID> countedIds = new HashSet<>();
        for (UUID targetId : targetIds) {
            if (!countedIds.add(targetId)) {
                continue;
            }
            Card card = gameQueryService.findCardInGraveyardById(gameData, targetId);
            if (card == null) {
                continue;
            }
            if (card.hasType(CardType.INSTANT) && ++instantCount > 1) {
                throw new IllegalStateException("You cannot choose more than one instant card");
            }
            if (card.hasType(CardType.SORCERY) && ++sorceryCount > 1) {
                throw new IllegalStateException("You cannot choose more than one sorcery card");
            }
        }
    }

    private void validateAtMostOneCreatureAndOneLand(GameData gameData, List<UUID> targetIds) {
        if (!fitsAtMostOneCreatureAndOneLand(gameData, targetIds)) {
            throw new IllegalStateException("Must target at most one creature and at most one land");
        }
    }

    public boolean fitsAtMostOneCreatureAndOneLand(GameData gameData, List<UUID> targetIds) {
        int pureCreatures = 0;
        int pureLands = 0;
        int duals = 0;
        for (UUID id : targetIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, id);
            if (card == null) {
                continue;
            }
            boolean creature = card.hasType(CardType.CREATURE);
            boolean land = card.hasType(CardType.LAND);
            if (creature && land) {
                duals++;
            } else if (creature) {
                pureCreatures++;
            } else if (land) {
                pureLands++;
            } else {
                return false;
            }
        }
        if (pureCreatures > 1 || pureLands > 1) {
            return false;
        }
        return duals <= (1 - pureCreatures) + (1 - pureLands);
    }

    private void validateSameCreatureOrLandTypeAsFirstAuraHost(GameData gameData, List<UUID> targetIds) {
        if (targetIds.size() < 2) {
            throw new IllegalStateException("Two targets are required");
        }
        Permanent aura = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        Permanent destination = gameQueryService.findPermanentById(gameData, targetIds.get(1));
        if (aura == null || destination == null || !aura.isAttached()) {
            throw new IllegalStateException("The Aura must still be attached to a permanent");
        }
        Permanent host = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (host == null) {
            throw new IllegalStateException("The Aura's attached permanent no longer exists");
        }
        if (destination.getId().equals(host.getId())) {
            throw new IllegalStateException("The destination must be another permanent");
        }
        boolean sameCreatureType = gameQueryService.isCreature(gameData, host)
                && gameQueryService.isCreature(gameData, destination);
        boolean sameLandType = gameQueryService.isLand(gameData, host)
                && gameQueryService.isLand(gameData, destination);
        if (!sameCreatureType && !sameLandType) {
            throw new IllegalStateException("The destination must be another permanent of the Aura's host type");
        }
    }

    private void validateAtMostOnePerController(GameData gameData, List<UUID> targetIds) {
        Set<UUID> controllers = new HashSet<>();
        for (UUID targetId : targetIds) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId == null) {
                controllerId = gameQueryService.findGraveyardOwnerById(gameData, targetId);
            }
            if (controllerId != null && !controllers.add(controllerId)) {
                throw new IllegalStateException("May target at most one permanent per controller");
            }
        }
    }

    /**
     * "Up to two target creatures and up to two target lands" — each permanent consumes one target
     * slot; dual-typed permanents may be assigned to either quota.
     */
    private void validateAtMostTwoCreaturesAndTwoLands(GameData gameData, List<UUID> targetIds) {
        if (!fitsAtMostTwoCreaturesAndTwoLands(gameData, targetIds)) {
            throw new IllegalStateException(
                    "Must target at most two creatures and at most two lands");
        }
    }

    public boolean fitsAtMostTwoCreaturesAndTwoLands(GameData gameData, List<UUID> targetIds) {
        int pureCreatures = 0;
        int pureLands = 0;
        int duals = 0;
        for (UUID id : targetIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent == null) {
                continue;
            }
            boolean creature = gameQueryService.isCreature(gameData, permanent);
            boolean land = gameQueryService.isLand(gameData, permanent);
            if (creature && land) {
                duals++;
            } else if (creature) {
                pureCreatures++;
            } else if (land) {
                pureLands++;
            } else {
                return false;
            }
        }
        if (pureCreatures > 2 || pureLands > 2) {
            return false;
        }
        return duals <= (2 - pureCreatures) + (2 - pureLands);
    }

    private void validateAtMostOneArtifactOneCreatureAndOneLand(GameData gameData, List<UUID> targetIds) {
        if (!fitsAtMostOneArtifactOneCreatureAndOneLand(gameData, targetIds)) {
            throw new IllegalStateException(
                    "Must target at most one artifact, at most one creature, and at most one land");
        }
    }

    public boolean fitsAtMostOneArtifactOneCreatureAndOneLand(GameData gameData, List<UUID> targetIds) {
        if (targetIds == null || targetIds.size() > 3
                || targetIds.stream().distinct().count() != targetIds.size()) {
            return false;
        }
        List<Permanent> targets = new ArrayList<>(targetIds.size());
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                return false;
            }
            targets.add(target);
        }
        return canAssignArtifactCreatureAndLandTargets(gameData, targets, 0, 0);
    }

    private boolean canAssignArtifactCreatureAndLandTargets(GameData gameData, List<Permanent> targets,
                                                            int targetIndex, int usedCategories) {
        if (targetIndex == targets.size()) {
            return true;
        }
        Permanent target = targets.get(targetIndex);
        boolean[] matches = {
                gameQueryService.isArtifact(gameData, target),
                gameQueryService.isCreature(gameData, target),
                gameQueryService.isLand(gameData, target)
        };
        for (int category = 0; category < matches.length; category++) {
            int categoryBit = 1 << category;
            if (matches[category] && (usedCategories & categoryBit) == 0
                    && canAssignArtifactCreatureAndLandTargets(
                    gameData, targets, targetIndex + 1, usedCategories | categoryBit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Every target after the first must be a permanent controlled by the first target — the first
     * target itself when it is a player, otherwise the controller of that permanent
     * (Chandra, Pyromaster +1).
     */
    private void validateControlledByFirstTarget(GameData gameData, List<UUID> targetIds) {
        if (targetIds.size() < 2) {
            return;
        }
        UUID firstTargetId = targetIds.getFirst();
        UUID requiredControllerId = gameData.playerIds.contains(firstTargetId)
                ? firstTargetId
                : gameQueryService.findPermanentController(gameData, firstTargetId);
        for (int i = 1; i < targetIds.size(); i++) {
            UUID controllerOfTarget = gameQueryService.findPermanentController(gameData, targetIds.get(i));
            if (controllerOfTarget == null) {
                controllerOfTarget = gameQueryService.findGraveyardOwnerById(gameData, targetIds.get(i));
            }
            if (!java.util.Objects.equals(requiredControllerId, controllerOfTarget)) {
                throw new IllegalStateException(
                        "Target must be controlled by the player or planeswalker's controller you targeted");
            }
        }
    }

    private void validateAttachedToFirstTarget(GameData gameData, List<UUID> targetIds) {
        if (targetIds.size() < 2) {
            return;
        }
        Permanent firstTarget = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        for (int i = 1; i < targetIds.size(); i++) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetIds.get(i));
            if (firstTarget == null || target == null
                    || !firstTarget.getId().equals(target.getAttachedTo())) {
                throw new IllegalStateException("Target must be attached to the first target");
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

        if (entry.getTargetId() == null && entry.getTargetIds().isEmpty()
                && !entry.getTargetCardIds().isEmpty()) {
            return entry.getTargetCardIds().stream()
                    .noneMatch(id -> isTargetCardLegalOnResolution(gameData, entry, id));
        }

        // CR 608.2b requires every target occurrence to be checked again. Keep illegal flat-list
        // positions masked on the entry so a spell with at least one legal target can resolve
        // without its handlers affecting the illegal targets or shifting later target groups.
        List<UUID> declaredTargetIds = entry.getDeclaredTargetIds();
        if (!declaredTargetIds.isEmpty()) {
            List<TargetFilter> targetFilters = targetFiltersForDeclaredPositions(
                    gameData, entry, declaredTargetIds.size());
            boolean[] targetLegal = new boolean[declaredTargetIds.size()];
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
                boolean legal;
                if (targetFilter instanceof GraveyardCardPredicateTargetFilter graveyardFilter) {
                    // A graveyard target group's positions live in a graveyard, not on the battlefield
                    // (Spelltwine). It stays legal as long as the card is still in a graveyard.
                    Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
                    UUID graveyardOwnerId = graveyardCard == null
                            ? null : gameQueryService.findGraveyardOwnerById(gameData, targetId);
                    legal = graveyardCard != null && switch (graveyardFilter.scope()) {
                        case CONTROLLERS_GRAVEYARD -> entry.getControllerId().equals(graveyardOwnerId);
                        case OPPONENT_GRAVEYARD -> !entry.getControllerId().equals(graveyardOwnerId);
                        case ALL_GRAVEYARDS -> true;
                    };
                    if (legal && graveyardFilter.predicate() != null) {
                        if (graveyardFilter.predicate() instanceof CardColorPredicate colorPredicate) {
                            legal = gameQueryService.getEffectiveCardColors(gameData, graveyardCard)
                                    .contains(colorPredicate.color());
                        } else {
                            legal = predicateEvaluationService.matchesCardPredicate(
                                    graveyardCard, graveyardFilter.predicate(), entry.getCard().getId());
                        }
                    }
                } else if (secondaryTargetsAreOnStack) {
                    legal = checkSpellTargetOnStack(gameData, targetId, targetFilter, entry.getControllerId(),
                            entry.getSourcePermanentSnapshot(), entry.getXValue(), entry.isKicked()).isEmpty();
                } else {
                    legal = isBattlefieldTargetLegalOnResolution(gameData, entry, targetId, targetFilter);
                }
                if (legal) {
                    targetLegal[i] = true;
                } else {
                    entry.markTargetIllegal(i);
                }
            }

            if (entry.getCard() != null
                    && entry.getCard().getMultiTargetConstraint() == MultiTargetConstraint.ATTACHED_TO_FIRST_TARGET) {
                Permanent firstTarget = declaredTargetIds.isEmpty()
                        ? null
                        : gameQueryService.findPermanentById(gameData, declaredTargetIds.getFirst());
                for (int i = 1; i < declaredTargetIds.size(); i++) {
                    Permanent target = gameQueryService.findPermanentById(gameData, declaredTargetIds.get(i));
                    if (targetLegal[i] && (!targetLegal[0] || firstTarget == null
                            || !firstTarget.getId().equals(target.getAttachedTo()))) {
                        targetLegal[i] = false;
                        entry.markTargetIllegal(i);
                    }
                }
            }
            for (boolean legal : targetLegal) {
                anySecondaryTargetLegal |= legal;
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
                if (!targetFizzled) {
                    List<CardEffect> exileTargetEffects = entry.getEffectsToResolve().stream()
                            .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD))
                            .toList();
                    targetFizzled = targetValidationService.checkEffectTargets(
                            exileTargetEffects,
                            new TargetValidationContext(gameData, entry.getTargetId(), Zone.EXILE,
                                    entry.getCard(), entry.getXValue(), entry.getControllerId(),
                                    entry.getSourcePermanentSnapshot())).isPresent();
                }
            } else if (entry.getTargetZone() == Zone.GRAVEYARD) {
                targetFizzled = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId()) == null;
                if (!targetFizzled) {
                    List<CardEffect> graveyardTargetEffects = entry.getEffectsToResolve().stream()
                            .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                            .toList();
                    targetFizzled = targetValidationService.checkEffectTargets(
                            graveyardTargetEffects,
                            new TargetValidationContext(gameData, entry.getTargetId(), Zone.GRAVEYARD,
                                    entry.getCard(), entry.getXValue(), entry.getControllerId(),
                                    entry.getSourcePermanentSnapshot(), entry.getSourcePermanentId(),
                                    entry.getTriggeringPermanentPowerAtTrigger())).isPresent();
                }
            } else if (entry.getTargetZone() == Zone.STACK) {
                targetFizzled = gameData.stack.stream().noneMatch(se -> se.getCard().getId().equals(entry.getTargetId()));
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (targetPerm == null && !gameData.playerIds.contains(entry.getTargetId())) {
                    targetFizzled = true;
                } else if (targetPerm == null && gameData.playerIds.contains(entry.getTargetId())) {
                    // Player target: check hexproof/shroud at resolution time
                    if (isBlockedByPeaceTalksForEntry(gameData, entry)) {
                        targetFizzled = true;
                    } else {
                    String playerReason = checkPlayerUntargetableReason(
                            gameData, entry.getTargetId(), entry.getControllerId(), entry.getCard());
                    if (playerReason != null) {
                        targetFizzled = true;
                    } else if (effectiveSpellColors(gameData, entry).stream()
                            .anyMatch(color -> gameQueryService.playerHasProtectionFromColor(
                                    gameData, entry.getTargetId(), color))) {
                        targetFizzled = true;
                    } else if (entry.getCard() != null
                            && gameQueryService.playerHasProtectionFromChosenName(gameData, entry.getTargetId(),
                                    entry.getCard().getName())) {
                        targetFizzled = true;
                    }
                    if (!targetFizzled) {
                        targetFizzled = !isBattlefieldTargetLegalOnResolution(gameData, entry,
                                entry.getTargetId(), primaryTargetFilter(entry));
                    }
                    }
                } else if (targetPerm != null) {
                    targetFizzled = untargetableReason(gameData, targetPerm, entry.getControllerId()) != null;
                    if (!targetFizzled && isBlockedByPeaceTalksForEntry(gameData, entry)) {
                        targetFizzled = true;
                    }
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
                        TargetFilter effectiveTargetFilter = primaryTargetFilter(entry);
                        effectiveTargetFilter = targetFilterForKickedCast(effectiveTargetFilter, entry.isKicked());
                        if (effectiveTargetFilter != null) {
                            try {
                                predicateEvaluationService.validateTargetFilter(effectiveTargetFilter, targetPerm,
                                filterContext(gameData,
                                                entry.getCard() != null ? entry.getCard().getId() : null,
                                                entry.getControllerId(), defendingPlayerId(gameData, entry))
                                                .withXValue(entry.getXValue())
                                                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                                                .withMadness(entry.isMadness()));
                            } catch (IllegalStateException e) {
                                targetFizzled = true;
                            }
                        }
                        if (!targetFizzled && targetValidationService.checkEffectTargets(
                                entry.getEffectsToResolve(),
                                new TargetValidationContext(gameData, entry.getTargetId(), entry.getTargetZone(),
                                        entry.getCard(), entry.getXValue(), entry.getControllerId(),
                                        entry.getSourcePermanentSnapshot(), entry.getSourcePermanentId(),
                                        entry.getTriggeringPermanentPowerAtTrigger())).isPresent()) {
                            targetFizzled = true;
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
            } else if (entry.getTargetCardIds().isEmpty()) {
                targetFizzled = allSecondaryGone;
            }
        }

        if (!targetFizzled) {
            targetFizzled = allTargetsGone(entry.getTargetCardIds(),
                    id -> isTargetCardLegalOnResolution(gameData, entry, id));
        }

        return targetFizzled;
    }

    private boolean isTargetCardLegalOnResolution(GameData gameData, StackEntry entry, UUID cardId) {
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (card == null) {
            return false;
        }

        List<CardEffect> declarativeGraveyardEffects = entry.getEffectsToResolve().stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                .toList();
        if (!declarativeGraveyardEffects.isEmpty()) {
            return declarativeGraveyardEffects.stream().anyMatch(effect ->
                    targetValidationService.checkEffectTargets(
                            List.of(effect),
                            new TargetValidationContext(gameData, cardId, Zone.GRAVEYARD,
                                    entry.getCard(), entry.getXValue(), entry.getControllerId(),
                                    entry.getSourcePermanentSnapshot(), entry.getSourcePermanentId(),
                                    entry.getTriggeringPermanentPowerAtTrigger()))
                            .isEmpty());
        }

        SacrificePermanentAndReturnTargetCardsFromGraveyardEffect effect = entry.getEffectsToResolve().stream()
                .filter(SacrificePermanentAndReturnTargetCardsFromGraveyardEffect.class::isInstance)
                .map(SacrificePermanentAndReturnTargetCardsFromGraveyardEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return true;
        }

        return gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of()).stream()
                .anyMatch(graveyardCard -> graveyardCard.getId().equals(cardId))
                && predicateEvaluationService.matchesCardPredicate(
                        card, effect.returnFilter(), entry.getCard().getId());
    }

    public boolean isPrimaryTargetLegalOnResolution(GameData gameData, StackEntry entry, UUID targetId) {
        if (entry.getTargetZone() == Zone.EXILE) {
            return gameQueryService.findCardInExileById(gameData, targetId) != null;
        }
        if (entry.getTargetZone() == Zone.GRAVEYARD) {
            if (gameQueryService.findCardInGraveyardById(gameData, targetId) == null) {
                return false;
            }
            List<CardEffect> graveyardTargetEffects = entry.getEffectsToResolve().stream()
                    .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                    .toList();
            return targetValidationService.checkEffectTargets(
                    graveyardTargetEffects,
                    new TargetValidationContext(gameData, targetId, Zone.GRAVEYARD,
                            entry.getCard(), entry.getXValue(), entry.getControllerId(),
                            entry.getSourcePermanentSnapshot(), entry.getSourcePermanentId(),
                            entry.getTriggeringPermanentPowerAtTrigger())).isEmpty();
        }
        if (entry.getTargetZone() == Zone.STACK) {
            return checkSpellTargetOnStack(gameData, targetId, primaryTargetFilter(entry), entry.getControllerId(),
                    entry.getSourcePermanentSnapshot(), entry.getXValue(), entry.isKicked()).isEmpty();
        }
        return isBattlefieldTargetLegalOnResolution(gameData, entry, targetId, primaryTargetFilter(entry));
    }

    private boolean isBattlefieldTargetLegalOnResolution(GameData gameData, StackEntry entry, UUID targetId,
                                                          TargetFilter targetFilter) {
        targetFilter = targetFilterForKickedCast(targetFilter, entry.isKicked());
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            if (!gameData.playerIds.contains(targetId)) {
                return false;
            }
            if (isBlockedByPeaceTalksForEntry(gameData, entry)) {
                return false;
            }
            if (checkPlayerUntargetableReason(gameData, targetId, entry.getControllerId(), entry.getCard()) != null) {
                return false;
            }
            Card sourceCard = entry.getCard();
            CardColor effectiveColor = gameQueryService.getEffectiveCardColor(gameData, sourceCard);
            if (effectiveColor != null
                    && gameQueryService.playerHasProtectionFromColor(gameData, targetId, effectiveColor)) {
                return false;
            }
            if (sourceCard != null
                    && gameQueryService.playerHasProtectionFromChosenName(gameData, targetId, sourceCard.getName())) {
                return false;
            }
            if (targetFilter instanceof PlayerPredicateTargetFilter playerFilter) {
                return matchesPlayerPredicateAtResolution(gameData, targetPredicateController(entry), targetId, playerFilter.predicate(),
                        entry.getSourcePermanentId());
            }
            if (targetFilter instanceof AnyTargetPredicateTargetFilter anyFilter) {
                return matchesPlayerPredicateAtResolution(gameData, targetPredicateController(entry), targetId, anyFilter.playerPredicate(),
                        entry.getSourcePermanentId());
            }
            return true;
        }

        if (untargetableReason(gameData, target, entry.getControllerId()) != null) {
            return false;
        }
        if (isBlockedByPeaceTalksForEntry(gameData, entry)) {
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
        if (entry.getCard() != null
                && gameQueryService.cantBeTargetedByWallOnlySources(gameData, target)
                && sourceCanTargetOnlyWalls(entry.getCard(), entry.getEffectsToResolve(), targetFilter)) {
            return false;
        }
        if (targetFilter != null) {
            try {
                predicateEvaluationService.validateTargetFilter(targetFilter, target,
                        filterContext(gameData, entry.getCard() != null ? entry.getCard().getId() : null,
                                entry.getControllerId(), defendingPlayerId(gameData, entry)).withXValue(entry.getXValue())
                                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                                .withMadness(entry.isMadness()));
            } catch (IllegalStateException e) {
                return false;
            }
        }
        if (entry.getTargetIds().isEmpty()
                && targetValidationService.checkEffectTargets(entry.getEffectsToResolve(),
                new TargetValidationContext(gameData, targetId, entry.getTargetZone(), entry.getCard(), entry.getXValue(),
                        entry.getControllerId(), entry.getSourcePermanentSnapshot(), entry.getSourcePermanentId(),
                        entry.getTriggeringPermanentPowerAtTrigger()))
                .isPresent()) {
            return false;
        }
        return true;
    }

    private TargetFilter targetFilterForKickedCast(TargetFilter targetFilter, boolean kicked) {
        if (targetFilter instanceof PermanentPredicateTargetFilter filter) {
            return new PermanentPredicateTargetFilter(filter.predicateFor(kicked), filter.errorMessage());
        }
        return targetFilter;
    }

    private boolean isProtectedFromSource(GameData gameData, Permanent target, StackEntry entry) {
        Card sourceCard = entry.getCard();
        if (sourceCard == null) {
            return false;
        }
        return gameQueryService.hasProtectionFromSource(gameData, target, sourceCard, entry.getControllerId());
    }

    private UUID targetPredicateController(StackEntry entry) {
        return entry.getActivePlayerId() != null ? entry.getActivePlayerId() : entry.getControllerId();
    }

    private TargetFilter primaryTargetFilter(StackEntry entry) {
        if (entry.getTargetFilter() != null) {
            return entry.getTargetFilter();
        }
        Card targetingCard = entry.getTargetingCard();
        if (targetingCard == null) {
            return null;
        }
        if (entry.isPrimaryTargetStoredSeparately() && !targetingCard.getSpellTargets().isEmpty()) {
            return targetingCard.getSpellTargets().getFirst().getFilter();
        }
        if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            for (CardEffect effect : entry.getEffectsToResolve()) {
                int groupIndex = targetingCard.getEffectTargetIndex(effect);
                if (groupIndex >= 0 && groupIndex < targetingCard.getSpellTargets().size()) {
                    return targetingCard.getSpellTargets().get(groupIndex).getFilter();
                }
            }
            return null;
        }
        return targetingCard.getTargetFilter();
    }

    private List<TargetFilter> targetFiltersForDeclaredPositions(GameData gameData, StackEntry entry,
                                                                  int targetCount) {
        List<TargetFilter> filters = new ArrayList<>(targetCount);
        if (entry.getTargetFilter() != null) {
            for (int i = 0; i < targetCount; i++) {
                filters.add(entry.getTargetFilter());
            }
            return filters;
        }

        // An amount-assignment entry derives its flat target list from the assignment keys, not from
        // the card's declared target groups. Those groups describe the separate primary target that
        // rides alongside the assignments (Fiery Justice's "target opponent gains 5 life"), so they
        // must not be applied to the assignment positions.
        if (entry.isTargetIdsFromAssignments()) {
            for (int i = 0; i < targetCount; i++) {
                filters.add(null);
            }
            return filters;
        }

        Card card = entry.getTargetingCard();
        if (card != null) {
            int firstFlatGroup = entry.isPrimaryTargetStoredSeparately() ? 1 : 0;
            int remaining = targetCount;
            for (SpellTarget group : card.getSpellTargets()) {
                if (group.getIndex() < firstFlatGroup || !entry.isTargetGroupActive(group.getIndex())) {
                    continue;
                }
                int declaredSize = group.getIndex() < entry.getTargetGroupSizes().size()
                        ? entry.getTargetGroupSizes().get(group.getIndex())
                        : effectiveGroupMaxTargets(gameData, entry.getControllerId(),
                                entry.getSourcePermanentSnapshot(), group, entry.getXValue(), entry.isKicked());
                int size = Math.min(Math.max(declaredSize, 0), remaining);
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
        Set<CardColor> spellColors = effectiveSpellColors(gameData, entry);
        return spellColors.stream().anyMatch(color ->
                gameQueryService.hasProtectionFrom(gameData, targetPerm, color)
                        || gameQueryService.cantBeTargetedBySpellColor(gameData, targetPerm, color,
                        entry.getControllerId()))
                || (entry.getCard().hasType(CardType.INSTANT)
                && gameQueryService.hasHexproofFromCardType(gameData, targetPerm, CardType.INSTANT,
                entry.getControllerId()))
                || gameQueryService.cantBeTargetedByAnySpell(gameData, targetPerm);
    }

    private Set<CardColor> effectiveSpellColors(GameData gameData, StackEntry entry) {
        return effectiveSourceColors(gameData, entry);
    }

    Set<CardColor> effectiveSourceColors(GameData gameData, Card sourceCard) {
        if (sourceCard == null) {
            return Set.of();
        }
        UUID sourcePermanentId = findSourcePermanentIdByCardId(gameData, sourceCard.getId());
        if (sourcePermanentId != null) {
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            if (sourcePermanent != null) {
                return Set.copyOf(gameQueryService.getEffectiveColors(gameData, sourcePermanent));
            }
        }
        return gameQueryService.getEffectiveCardColors(gameData, sourceCard);
    }

    private Set<CardColor> effectiveSourceColors(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (sourcePermanent != null) {
                return Set.copyOf(gameQueryService.getEffectiveColors(gameData, sourcePermanent));
            }
        }
        return gameQueryService.getEffectiveCardColors(gameData, entry.getCard());
    }

    private boolean isNonColorSourceRestricted(GameData gameData, Permanent targetPerm, StackEntry entry) {
        if (entry.getCard() == null) return false;
        return gameQueryService.cantBeTargetedByNonColorSources(
                gameData, targetPerm, entry.getCard(), entry.getControllerId());
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
        Set<CardColor> sourceColors = effectiveSourceColors(gameData, entry);
        if (sourceColors.size() == 1 && gameQueryService.hasHexproofFromMonocolored(gameData, targetPerm)
                && !(gameQueryService.isCreature(gameData, targetPerm)
                && gameQueryService.ignoresOpponentCreatureHexproof(gameData, entry.getControllerId()))) {
            UUID targetController = gameQueryService.findPermanentController(gameData, targetPerm.getId());
            if (targetController != null && !targetController.equals(entry.getControllerId())) {
                return true;
            }
        }
        var sourceColor = entry.getCard().getColor();
        if (sourceColor == null) return false;
        if (gameQueryService.cantBeTargetedByColorSources(gameData, targetPerm, sourceColor)) return true;
        if (!gameQueryService.hasHexproofFromColor(gameData, targetPerm, sourceColor)) return false;
        UUID targetController = gameQueryService.findPermanentController(gameData, targetPerm.getId());
        return targetController != null && !targetController.equals(entry.getControllerId());
    }

    private void validateHexproofFromColor(GameData gameData, Permanent target, Card sourceCard, UUID sourcePlayerId) {
        if (sourceCard == null) return;
        if (effectiveSourceColors(gameData, sourceCard).size() == 1
                && gameQueryService.hasHexproofFromMonocolored(gameData, target)
                && !(gameQueryService.isCreature(gameData, target)
                && gameQueryService.ignoresOpponentCreatureHexproof(gameData, sourcePlayerId))) {
            UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
            if (targetController != null && !targetController.equals(sourcePlayerId)) {
                throw new IllegalStateException(target.getCard().getName()
                        + " has hexproof from monocolored");
            }
        }
        var sourceColor = sourceCard.getColor();
        if (sourceColor == null) return;
        if (gameQueryService.cantBeTargetedByColorSources(gameData, target, sourceColor)) {
            throw new IllegalStateException(target.getCard().getName()
                    + " can't be the target of " + sourceColor.name().toLowerCase()
                    + " spells or abilities from " + sourceColor.name().toLowerCase() + " sources");
        }
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
        if (effectiveSourceColors(gameData, sourceCard).size() == 1
                && gameQueryService.hasHexproofFromMonocolored(gameData, target)
                && !(gameQueryService.isCreature(gameData, target)
                && gameQueryService.ignoresOpponentCreatureHexproof(gameData, sourcePlayerId))) {
            UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
            if (targetController != null && !targetController.equals(sourcePlayerId)) {
                return target.getCard().getName() + " has hexproof from monocolored";
            }
        }
        var sourceColor = sourceCard.getColor();
        if (sourceColor == null) return null;
        if (gameQueryService.cantBeTargetedByColorSources(gameData, target, sourceColor)) {
            return target.getCard().getName() + " can't be the target of "
                    + sourceColor.name().toLowerCase() + " spells or abilities from "
                    + sourceColor.name().toLowerCase() + " sources";
        }
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
        if (gameQueryService.isLandTargetRestricted(gameData, target, sourcePlayerId)) {
            return target.getCard().getName()
                    + " can't be the target of spells or abilities opponents control";
        }
        if (gameQueryService.hasKeyword(gameData, target, Keyword.SHROUD)
                && !target.ignoresShroudFor(sourcePlayerId)) {
            return target.getCard().getName() + " has shroud and can't be targeted";
        }
        UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
        // Glaring Spotlight: opponents' hexproof creatures are targetable as though they had none.
        boolean hexproofLifted = gameQueryService.ignoresOpponentPermanentHexproof(gameData, sourcePlayerId)
                || (gameQueryService.isCreature(gameData, target)
                && gameQueryService.ignoresOpponentCreatureHexproof(gameData, sourcePlayerId));
        if (targetController != null && !targetController.equals(sourcePlayerId)) {
            if ((!hexproofLifted && gameQueryService.hasKeyword(gameData, target, Keyword.HEXPROOF))
                    || gameQueryService.cantBeTargetedByOpponentSpellsOrAbilities(gameData, target, sourcePlayerId)) {
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
        return checkPlayerUntargetableReason(gameData, targetPlayerId, sourcePlayerId, null);
    }

    private String checkPlayerUntargetableReason(GameData gameData, UUID targetPlayerId, UUID sourcePlayerId,
                                                 Card sourceCard) {
        if (gameQueryService.playerHasShroud(gameData, targetPlayerId)) {
            return gameData.playerIdToName.get(targetPlayerId) + " has shroud and can't be targeted";
        }
        if (gameQueryService.playerHasProtectionFromEverything(gameData, targetPlayerId)) {
            return gameData.playerIdToName.get(targetPlayerId) + " has protection from everything and can't be targeted";
        }
        if (sourcePlayerId != null && !sourcePlayerId.equals(targetPlayerId)
                && gameQueryService.playerHasHexproof(gameData, targetPlayerId)
                && !gameQueryService.ignoresOpponentPlayerHexproof(gameData, sourcePlayerId)) {
            return gameData.playerIdToName.get(targetPlayerId) + " has hexproof and can't be targeted";
        }
        CardColor effectiveColor = gameQueryService.getEffectiveCardColor(gameData, sourceCard);
        if (sourcePlayerId != null && !sourcePlayerId.equals(targetPlayerId)
                && effectiveColor != null
                && gameQueryService.playerHasHexproofFromColor(gameData, targetPlayerId, effectiveColor)) {
            return gameData.playerIdToName.get(targetPlayerId) + " has hexproof from "
                    + effectiveColor.name().toLowerCase();
        }
        return null;
    }

    private void validatePlayerTargetable(GameData gameData, UUID targetPlayerId, UUID sourcePlayerId) {
        String reason = checkPlayerUntargetableReason(gameData, targetPlayerId, sourcePlayerId);
        if (reason != null) {
            throw new IllegalStateException(reason);
        }
    }

    private void validatePlayerTargetable(GameData gameData, UUID targetPlayerId, UUID sourcePlayerId,
                                          Card sourceCard) {
        String reason = checkPlayerUntargetableReason(gameData, targetPlayerId, sourcePlayerId, sourceCard);
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
        return checkSpellPermanentTargetableReason(gameData, target, card, controllerId,
                card.getEffects(EffectSlot.SPELL), card.getTargetFilter());
    }

    public Optional<String> checkSpellPermanentTargetableReason(GameData gameData, Permanent target, Card card,
                                                                UUID controllerId, List<CardEffect> sourceEffects,
                                                                TargetFilter targetFilter) {
        String peaceTalks = peaceTalksUntargetableReason(gameData);
        if (peaceTalks != null) {
            return Optional.of(peaceTalks);
        }
        String protectionReason = checkSpellProtection(gameData, target, card, controllerId, true);
        if (protectionReason != null) {
            return Optional.of(protectionReason);
        }
        String untargetable = untargetableReason(gameData, target, controllerId);
        if (untargetable != null) {
            return Optional.of(untargetable);
        }
        if (card != null && gameQueryService.cantBeTargetedByWallOnlySources(gameData, target)
                && sourceCanTargetOnlyWalls(card, sourceEffects, targetFilter)) {
            return Optional.of(target.getCard().getName()
                    + " can't be targeted by spells or abilities that can target only Walls");
        }
        return Optional.empty();
    }

    public boolean sourceCanTargetOnlyWalls(Card sourceCard, List<CardEffect> sourceEffects,
                                            TargetFilter targetFilter) {
        return sourceCanTargetOnlyWalls(sourceCard, sourceEffects, targetFilter, List.of());
    }

    public boolean sourceCanTargetOnlyWalls(Card sourceCard, List<CardEffect> sourceEffects,
                                            TargetFilter targetFilter, List<TargetFilter> targetFilters) {
        ChooseOneEffect modal = sourceEffects == null ? null : sourceEffects.stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst().orElse(null);
        if (modal == null && sourceCard != null) {
            modal = sourceCard.getEffects(EffectSlot.SPELL).stream()
                    .filter(ChooseOneEffect.class::isInstance)
                    .map(ChooseOneEffect.class::cast)
                    .findFirst().orElse(null);
        }
        if (modal != null) {
            return modal.options().stream().allMatch(this::modeCanTargetOnlyWalls);
        }

        List<TargetFilter> allTargetFilters = targetFilters != null && !targetFilters.isEmpty()
                ? targetFilters
                : sourceCard == null ? List.of() : sourceCard.getMultiTargetFilters();
        if (!allTargetFilters.isEmpty()) {
            return allTargetFilters.stream().allMatch(this::targetFilterCanTargetOnlyWalls);
        }
        if (targetFilter != null) {
            return targetFilterCanTargetOnlyWalls(targetFilter);
        }
        if (sourceEffects == null || EffectResolution.allowsPlayerTargets(sourceEffects)) {
            return false;
        }
        return EffectResolution.declaredPermanentRestriction(sourceEffects)
                .map(this::permanentPredicateAllowsOnlyWalls)
                .orElse(false);
    }

    private boolean modeCanTargetOnlyWalls(ChooseOneEffect.ChooseOneOption mode) {
        if (mode.targetFilters() != null && !mode.targetFilters().isEmpty()) {
            return mode.targetFilters().stream().allMatch(this::targetFilterCanTargetOnlyWalls);
        }
        if (mode.targetFilter() != null) {
            return targetFilterCanTargetOnlyWalls(mode.targetFilter());
        }
        if (EffectResolution.allowsPlayerTargets(mode.effects())) {
            return false;
        }
        return EffectResolution.declaredPermanentRestriction(mode.effects())
                .map(this::permanentPredicateAllowsOnlyWalls)
                .orElse(false);
    }

    private boolean targetFilterCanTargetOnlyWalls(TargetFilter targetFilter) {
        if (targetFilter instanceof PermanentPredicateTargetFilter filter) {
            return permanentPredicateAllowsOnlyWalls(filter.predicate());
        }
        if (targetFilter instanceof ControlledPermanentPredicateTargetFilter filter) {
            return permanentPredicateAllowsOnlyWalls(filter.predicate());
        }
        return false;
    }

    private boolean permanentPredicateAllowsOnlyWalls(PermanentPredicate predicate) {
        if (predicate instanceof PermanentHasSubtypePredicate subtype) {
            return subtype.subtype() == CardSubtype.WALL;
        }
        if (predicate instanceof PermanentHasAnySubtypePredicate subtypes) {
            return subtypes.subtypes().size() == 1 && subtypes.subtypes().contains(CardSubtype.WALL);
        }
        if (predicate instanceof PermanentAllOfPredicate allOf) {
            return !allOf.predicates().isEmpty()
                    && allOf.predicates().stream().anyMatch(this::permanentPredicateAllowsOnlyWalls);
        }
        if (predicate instanceof PermanentAnyOfPredicate anyOf) {
            return !anyOf.predicates().isEmpty()
                    && anyOf.predicates().stream().allMatch(this::permanentPredicateAllowsOnlyWalls);
        }
        return false;
    }

    /**
     * Permanent-target structural check for triggered abilities — same as
     * {@link #checkSpellPermanentTargetableReason} but ignores Peace Talks, which only blocks
     * spells and activated abilities.
     */
    public Optional<String> checkTriggeredPermanentTargetableReason(GameData gameData, Permanent target, Card card, UUID controllerId) {
        String protectionReason = checkSpellProtection(gameData, target, card, controllerId, false);
        if (protectionReason != null) {
            return Optional.of(protectionReason);
        }
        String untargetable = untargetableReason(gameData, target, controllerId);
        if (untargetable != null) {
            return Optional.of(untargetable);
        }
        return Optional.empty();
    }

    private String peaceTalksUntargetableReason(GameData gameData) {
        if (gameQueryService.isPeaceTalksActive(gameData)) {
            return "Players and permanents can't be the targets of spells or activated abilities";
        }
        return null;
    }

    /** Peace Talks only blocks spells and activated abilities — not triggered abilities. */
    private boolean isBlockedByPeaceTalksForEntry(GameData gameData, StackEntry entry) {
        if (!gameQueryService.isPeaceTalksActive(gameData)) {
            return false;
        }
        StackEntryType type = entry.getEntryType();
        return type != StackEntryType.TRIGGERED_ABILITY;
    }

    private String checkSpellProtection(GameData gameData, Permanent target, Card card, UUID sourcePlayerId,
                                        boolean sourceIsSpell) {
        if (card.hasType(CardType.INSTANT)
                && gameQueryService.hasHexproofFromCardType(gameData, target, CardType.INSTANT, sourcePlayerId)) {
            return target.getCard().getName() + " has hexproof from instants";
        }
        if (gameQueryService.hasProtectionFromOpponents(gameData, target, sourcePlayerId)) {
            return target.getCard().getName() + " has protection from the source's controller";
        }
        Set<CardColor> effectiveColors = gameQueryService.getEffectiveCardColors(gameData, card);
        if (!effectiveColors.isEmpty()
                && ((sourceIsSpell && gameQueryService.hasProtectionFromColoredSpells(gameData, target))
                || gameQueryService.hasProtectionFromColoredSpellSource(gameData, target, card))) {
            return target.getCard().getName() + " has protection from colored spells";
        }
        for (CardColor color : effectiveColors) {
            if (gameQueryService.hasProtectionFrom(gameData, target, color)) {
                return target.getCard().getName() + " has protection from " + color.name().toLowerCase();
            }
        }
        if (gameQueryService.hasProtectionFromSourceCardTypes(gameData, target, card)) {
            return target.getCard().getName() + " has protection from " + card.getType().getDisplayName().toLowerCase() + "s";
        }
        if (gameQueryService.hasProtectionFromSourceSubtypes(target, card)) {
            return target.getCard().getName() + " has protection from source's subtype";
        }
        if (sourcePlayerId != null) {
            String hexReason = hexproofFromColorReason(gameData, target, card, sourcePlayerId);
            if (hexReason != null) return hexReason;
        }
        for (CardColor color : effectiveColors) {
            if (gameQueryService.cantBeTargetedBySpellColor(gameData, target, color, sourcePlayerId)) {
                return target.getCard().getName() + " can't be the target of "
                        + color.name().toLowerCase() + " spells";
            }
        }
        if (gameQueryService.cantBeTargetedByAnySpell(gameData, target)) {
            return target.getCard().getName() + " can't be the target of spells";
        }
        if (gameQueryService.cantBeTargetedByNonColorSources(gameData, target, card, sourcePlayerId)) {
            return nonColorSourceRestrictionMessage(target);
        }
        if (card.isAura() && gameQueryService.cantBeEnchantedByOtherAuras(gameData, target)) {
            return target.getCard().getName() + " can't be enchanted by other Auras";
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
        if (allowSharedTargets && spellTargets == null) {
            return;
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
        validatePlayerPredicate(gameData, controllerId, targetPlayerId, predicate, errorMessage, null);
    }

    private void validatePlayerPredicate(GameData gameData, UUID controllerId, UUID targetPlayerId,
                                         PlayerPredicate predicate, String errorMessage, UUID sourcePermanentId) {
        if (!matchesPlayerPredicate(gameData, controllerId, targetPlayerId, predicate, sourcePermanentId)) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private FilterContext filterContext(GameData gameData, UUID sourceCardId, UUID controllerId) {
        return FilterContext.of(gameData)
                .withSourceCardId(sourceCardId)
                .withSourceControllerId(controllerId);
    }

    private FilterContext filterContext(GameData gameData, UUID sourceCardId, UUID controllerId,
                                        UUID defendingPlayerId) {
        return filterContext(gameData, sourceCardId, controllerId)
                .withDefendingPlayerId(defendingPlayerId);
    }

    private UUID defendingPlayerId(GameData gameData, StackEntry entry) {
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
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
    private boolean filterAdmitsAbilityTarget(TargetFilter targetFilter, Boolean kicked) {
        if (!(targetFilter instanceof StackEntryPredicateTargetFilter filter)) {
            return false;
        }
        return predicateAdmitsAbilityTarget(filter.predicateFor(Boolean.TRUE.equals(kicked)));
    }

    private boolean predicateAdmitsAbilityTarget(StackEntryPredicate predicate) {
        if (predicate instanceof StackEntryHasTargetPredicate) {
            return true;
        }
        if (predicate instanceof StackEntryTargetsPermanentPredicate) {
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
        if (predicate instanceof StackEntryTruePredicate) {
            return true;
        }
        if (predicate instanceof StackEntrySharesChosenNameWithSourcePredicate) {
            return source != null && source.getChosenName() != null
                    && source.getChosenName().equals(stackEntry.getCard().getName());
        }
        if (predicate instanceof StackEntrySharesNameWithCardExiledWithSourcePredicate) {
            return source != null
                    && gameData.getCardsExiledByPermanent(source.getId()).stream()
                    .anyMatch(card -> card.getName().equals(stackEntry.getCard().getName()));
        }
        if (predicate instanceof StackEntryTypeInPredicate typeInPredicate) {
            return typeInPredicate.spellTypes().contains(stackEntry.getEntryType());
        }
        if (predicate instanceof StackEntryColorInPredicate colorInPredicate) {
            return gameQueryService.getEffectiveCardColors(gameData, stackEntry.getCard()).stream()
                    .anyMatch(colorInPredicate.colors()::contains);
        }
        if (predicate instanceof StackEntryCardTypeInPredicate cardTypeInPredicate) {
            return cardTypeInPredicate.cardTypes().stream().anyMatch(stackEntry.getCard()::hasType);
        }
        if (predicate instanceof StackEntrySubtypeInPredicate subtypeInPredicate) {
            return stackEntry.getCard().getSubtypes().stream()
                    .anyMatch(subtypeInPredicate.subtypes()::contains);
        }
        if (predicate instanceof StackEntrySupertypeInPredicate supertypeInPredicate) {
            return stackEntry.getCard().getSupertypes().stream()
                    .anyMatch(supertypeInPredicate.supertypes()::contains);
        }
        if (predicate instanceof StackEntryIsSingleTargetPredicate) {
            return stackEntry.isSingleTarget();
        }
        if (predicate instanceof StackEntryHasTargetPredicate) {
            // Matches any spell or ability — per rules (e.g. Spellskite), activation is legal
            // even if the targeted spell/ability has no targets; resolution handles that case.
            return true;
        }
        if (predicate instanceof StackEntryHasXInManaCostPredicate) {
            return stackEntry.getCard().getParsedManaCost() != null
                    && stackEntry.getCard().getParsedManaCost().hasX();
        }
        if (predicate instanceof StackEntryIsNthSpellCastThisTurnPredicate nthSpell) {
            return gameData.getSpellCastOrdinalThisTurn(stackEntry.getCard().getId()) == nthSpell.spellNumber();
        }
        if (predicate instanceof StackEntryManaValuePredicate manaValuePredicate) {
            return stackEntry.getCard().getManaValue() == manaValuePredicate.manaValue();
        }
        if (predicate instanceof StackEntryMaxManaValuePredicate maxManaValuePredicate) {
            int manaValue = stackEntry.getCard().getManaValue() + stackEntry.getXValue();
            return manaValue <= maxManaValuePredicate.maxManaValue();
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
        if (predicate instanceof StackEntrySharesColorOrManaValueWithImprintedCardPredicate) {
            if (source == null) {
                return false;
            }
            Card imprintedCard = gameData.getImprintedCard(source.getCard());
            if (imprintedCard == null) {
                return false;
            }
            boolean sharesColor = imprintedCard.getColors().stream()
                    .anyMatch(stackEntry.getCard().getColors()::contains);
            int spellManaValue = stackEntry.getCard().getManaValue() + stackEntry.getXValue();
            return sharesColor || spellManaValue == imprintedCard.getManaValue();
        }
        if (predicate instanceof StackEntryControlledByPredicate) {
            return stackEntry.getControllerId().equals(controllerId);
        }
        if (predicate instanceof StackEntryNotTargetedByNamedCreatureAbilityPredicate notTargeted) {
            if (source == null || stackEntry.getCard() == null) {
                return false;
            }
            UUID candidateCardId = stackEntry.getCard().getId();
            for (StackEntry ability : gameData.stack) {
                if ((ability.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                        && ability.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                        || ability.getCard() == null
                        || !ability.getCard().hasType(CardType.CREATURE)
                        || !notTargeted.creatureName().equals(ability.getCard().getName())
                        || source.getId().equals(ability.getSourcePermanentId())) {
                    continue;
                }
                if (candidateCardId.equals(ability.getTargetId())
                        || ability.getDeclaredTargetIds().contains(candidateCardId)) {
                    return false;
                }
            }
            return true;
        }
        if (predicate instanceof StackEntryCastFromZonePredicate castFrom) {
            return stackEntry.getSourceZone() == castFrom.sourceZone();
        }
        if (predicate instanceof StackEntryKickedPredicate) {
            return stackEntry.wasKicked();
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
        if (predicate instanceof StackEntryTargetsAnyPlayerPredicate) {
            return targetsAnyPlayer(gameData, stackEntry);
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

    private boolean targetsAnyPlayer(GameData gameData, StackEntry stackEntry) {
        if (stackEntry.getTargetId() != null
                && gameData.orderedPlayerIds.contains(stackEntry.getTargetId())) {
            return true;
        }
        if (stackEntry.getTargetIds() != null) {
            for (UUID targetId : stackEntry.getTargetIds()) {
                if (gameData.orderedPlayerIds.contains(targetId)) {
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

    /**
     * Validates a player target that rides alongside a divided-damage cast, where the divided
     * branch owns the cast and the regular single-target validation path never runs (Fiery
     * Justice's "Target opponent gains 5 life" next to its 5 divided damage). Checks that the
     * target is a player matching the card's {@link PlayerPredicateTargetFilter} and that it is
     * targetable (shroud / hexproof).
     */
    public void validateSpellPlayerTarget(GameData gameData, UUID targetPlayerId, UUID controllerId,
                                          PlayerPredicateTargetFilter filter) {
        validateSpellPlayerTarget(gameData, targetPlayerId, controllerId, null, filter);
    }

    public void validateSpellPlayerTarget(GameData gameData, UUID targetPlayerId, UUID controllerId,
                                          Card sourceCard, PlayerPredicateTargetFilter filter) {
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || !matchesPlayerPredicate(gameData, controllerId, targetPlayerId, filter.predicate())) {
            throw new IllegalStateException(filter.errorMessage());
        }
        validatePlayerTargetable(gameData, targetPlayerId, controllerId, sourceCard);
    }

    /**
     * Whether {@code targetPlayerId} satisfies {@code predicate} relative to {@code controllerId}.
     * This service owns player-predicate evaluation on the targeting path; other targeting code
     * (including {@code TargetPredicateEvaluationService}) delegates here rather than re-deriving
     * the relation. A {@code null} controller matches no relation, which is what a source with no
     * known controller should do.
     */
    public boolean matchesPlayerPredicate(GameData gameData, UUID controllerId, UUID targetPlayerId, PlayerPredicate predicate) {
        return matchesPlayerPredicate(gameData, controllerId, targetPlayerId, predicate, null);
    }

    /**
     * Source-aware overload. {@code sourcePermanentId} is the permanent the spell or ability came
     * from, needed by source-relative predicates such as
     * {@link PlayerDamagedBySourceThisTurnPredicate}; a {@code null} source matches no player for
     * those, so a targeting path that cannot supply one rejects rather than over-allows.
     */
    public boolean matchesPlayerPredicate(GameData gameData, UUID controllerId, UUID targetPlayerId,
                                          PlayerPredicate predicate, UUID sourcePermanentId) {
        return switch (predicate) {
            case PlayerRelationPredicate relationPredicate -> switch (relationPredicate.relation()) {
                case ANY -> true;
                case SELF -> controllerId != null && controllerId.equals(targetPlayerId);
                case OPPONENT -> controllerId != null && !controllerId.equals(targetPlayerId);
            };
            case PlayerAttackedThisTurnPredicate ignored ->
                    gameData.playersDeclaredAttackersThisTurn.contains(targetPlayerId);
            case PlayerDealtDamageThisTurnPredicate ignored ->
                    gameData.playersDealtDamageThisTurn.contains(targetPlayerId);
            case PlayerLostLifeThisTurnPredicate ignored ->
                    gameData.lifeLostThisTurn.getOrDefault(targetPlayerId, 0) > 0;
            case PlayerDamagedBySourceThisTurnPredicate ignored ->
                    wasDamagedBySourceThisTurn(gameData, sourcePermanentId, targetPlayerId);
            case PlayerDamagedBySourceCombatThisTurnPredicate ignored ->
                    wasDamagedBySourceCombatThisTurn(gameData, sourcePermanentId, targetPlayerId);
            case PlayerControlsMoreCreaturesThanControllerPredicate ignored ->
                    controllerId != null
                            && !controllerId.equals(targetPlayerId)
                            && gameQueryService.controlsMoreCreaturesThan(gameData, targetPlayerId, controllerId);
            case PlayerControlsMoreLandsThanControllerPredicate ignored ->
                    controllerId != null
                            && !controllerId.equals(targetPlayerId)
                            && gameQueryService.controlsMoreLandsThan(gameData, targetPlayerId, controllerId);
            case PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate graveyardPredicate ->
                    controllerId != null
                            && targetPlayerId != null
                            && !controllerId.equals(targetPlayerId)
                            && countCreatureCardsInGraveyard(gameData, controllerId)
                            >= countCreatureCardsInGraveyard(gameData, targetPlayerId)
                            + graveyardPredicate.minimumDifference();
            case PlayerHasMoreLifeThanControllerPredicate ignored ->
                    controllerId != null
                            && !controllerId.equals(targetPlayerId)
                            && gameData.getLife(targetPlayerId) > gameData.getLife(controllerId);
            case PlayerHasMoreCardsInHandThanControllerPredicate handPredicate ->
                    controllerId != null
                            && targetPlayerId != null
                            && !controllerId.equals(targetPlayerId)
                            && gameData.playerHands.getOrDefault(targetPlayerId, List.of()).size()
                            >= gameData.playerHands.getOrDefault(controllerId, List.of()).size()
                            + handPredicate.minimumDifference();
        };
    }

    private boolean matchesPlayerPredicateAtResolution(GameData gameData, UUID controllerId,
                                                        UUID targetPlayerId, PlayerPredicate predicate,
                                                        UUID sourcePermanentId) {
        if (predicate instanceof PlayerHasMoreLifeThanControllerPredicate) {
            return controllerId != null && !controllerId.equals(targetPlayerId);
        }
        if (predicate instanceof PlayerControlsMoreCreaturesThanControllerPredicate) {
            return controllerId != null && !controllerId.equals(targetPlayerId);
        }
        if (predicate instanceof PlayerHasMoreCardsInHandThanControllerPredicate handPredicate) {
            if (controllerId == null || controllerId.equals(targetPlayerId)) {
                return false;
            }
            return !handPredicate.recheckAtResolution()
                    || gameData.playerHands.getOrDefault(targetPlayerId, List.of()).size()
                    >= gameData.playerHands.getOrDefault(controllerId, List.of()).size()
                    + handPredicate.minimumDifference();
        }
        if (predicate instanceof PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate graveyardPredicate) {
            if (controllerId == null || controllerId.equals(targetPlayerId)) {
                return false;
            }
            return !graveyardPredicate.recheckAtResolution()
                    || countCreatureCardsInGraveyard(gameData, controllerId)
                    >= countCreatureCardsInGraveyard(gameData, targetPlayerId)
                    + graveyardPredicate.minimumDifference();
        }
        return matchesPlayerPredicate(gameData, controllerId, targetPlayerId, predicate, sourcePermanentId);
    }

    private int countCreatureCardsInGraveyard(GameData gameData, UUID playerId) {
        return (int) gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                .filter(card -> card.hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE))
                .count();
    }

    private boolean wasDamagedBySourceThisTurn(GameData gameData, UUID sourcePermanentId, UUID targetPlayerId) {
        if (sourcePermanentId == null) {
            return false;
        }
        Set<UUID> combatVictims = gameData.combatDamageToPlayersThisTurn.get(sourcePermanentId);
        Set<UUID> noncombatVictims = gameData.noncombatDamageToPlayersThisTurn.get(sourcePermanentId);
        return (combatVictims != null && combatVictims.contains(targetPlayerId))
                || (noncombatVictims != null && noncombatVictims.contains(targetPlayerId));
    }

    private boolean wasDamagedBySourceCombatThisTurn(GameData gameData, UUID sourcePermanentId,
                                                     UUID targetPlayerId) {
        if (sourcePermanentId == null) {
            return false;
        }
        Set<UUID> combatVictims = gameData.combatDamageToPlayersThisTurn.get(sourcePermanentId);
        return combatVictims != null && combatVictims.contains(targetPlayerId);
    }

    /**
     * The battlefield permanent whose original card is {@code sourceCardId}, or {@code null} when
     * the source is not (or no longer) on the battlefield. Source-relative player predicates key
     * the per-turn damage records by permanent id, not card id.
     */
    UUID findSourcePermanentIdByCardId(GameData gameData, UUID sourceCardId) {
        if (sourceCardId == null) {
            return null;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getOriginalCard().getId().equals(sourceCardId)
                        || permanent.getCard().getId().equals(sourceCardId)) {
                    return permanent.getId();
                }
            }
        }
        return null;
    }
}

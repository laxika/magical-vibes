package com.github.laxika.magicalvibes.service.filter;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasFlashbackPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControlledCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledCreatureCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PhyrexianManaPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouOrCreatureYouControlPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The single evaluation point for the predicate and target-filter hierarchies
 * ({@link CardPredicate}, {@link PermanentPredicate}, {@link StackEntryPredicate},
 * {@link TargetFilter}).
 *
 * <p>Each family is dispatched with a switch that is exhaustive over its sealed hierarchy —
 * adding a predicate without an evaluation is a compile error, never a silent {@code false}.
 * Predicates are pure data in the domain; anything that needs engine-computed state (effective
 * power/toughness with static bonuses, changeling-aware subtype checks, animation-aware
 * creature checks) delegates to {@link GameQueryService}.</p>
 *
 * <p>Several evaluations have an explicit {@code gameData == null} fallback that uses only the
 * permanent's intrinsic values — these are relied on by callers that match predicates outside
 * a full game context and must be preserved.</p>
 */
@Service
@RequiredArgsConstructor
public class PredicateEvaluationService {

    private final GameQueryService gameQueryService;

    // --- Card predicate matching ---

    /**
     * Tests whether a card satisfies the given {@link CardPredicate}. Supports composite predicates
     * ({@link CardAllOfPredicate}, {@link CardAnyOfPredicate}, {@link CardNotPredicate}) as well as
     * leaf predicates for type, subtype, keyword, color, aura status, and self-identity.
     *
     * @param card         the card to test
     * @param predicate    the predicate to evaluate, or {@code null} (always matches)
     * @param sourceCardId the ID of the source card, used by {@link CardIsSelfPredicate}
     * @return {@code true} if the card matches the predicate
     */
    public boolean matchesCardPredicate(Card card, CardPredicate predicate, UUID sourceCardId) {
        return matchesCardPredicate(card, predicate, sourceCardId, null, null);
    }

    /**
     * Overload that accounts for Arcane Adaptation-style effects: when a {@link GameData} and
     * card owner are provided, {@link CardSubtypePredicate} checks also include subtypes
     * granted by all-zone subtype grants (see {@link GameQueryService#cardHasSubtype}).
     */
    public boolean matchesCardPredicate(Card card, CardPredicate predicate, UUID sourceCardId,
                                        GameData gameData, UUID cardOwnerId) {
        if (predicate == null) return true;

        return switch (predicate) {
            case CardTypePredicate p ->
                    card.hasType(p.cardType());
            case CardSubtypePredicate p ->
                    gameQueryService.cardHasSubtype(card, p.subtype(), gameData, cardOwnerId);
            case CardKeywordPredicate p ->
                    card.getKeywords().contains(p.keyword());
            case CardIsSelfPredicate ignored ->
                    sourceCardId != null && card.getId().equals(sourceCardId);
            case CardColorPredicate p ->
                    card.getColor() != null && card.getColor() == p.color();
            case PhyrexianManaPredicate ignored ->
                    card.getManaCost() != null && new ManaCost(card.getManaCost()).hasPhyrexianMana();
            case CardIsAuraPredicate ignored ->
                    card.isAura();
            case CardHasFlashbackPredicate ignored ->
                    card.getCastingOption(FlashbackCast.class).isPresent();
            case CardIsPermanentPredicate ignored ->
                    card.getType().isPermanentType();
            case CardIsHistoricPredicate ignored ->
                    card.hasType(CardType.ARTIFACT)
                            || card.getSupertypes().contains(CardSupertype.LEGENDARY)
                            || card.getSubtypes().contains(CardSubtype.SAGA);
            case CardSupertypePredicate p ->
                    card.getSupertypes().contains(p.supertype());
            case CardMaxManaValuePredicate p ->
                    card.getManaValue() <= p.maxManaValue();
            case CardMinManaValuePredicate p ->
                    card.getManaValue() >= p.minManaValue();
            case CardNamedPredicate p ->
                    p.cardName().equals(card.getName());
            case CardNotPredicate p ->
                    !matchesCardPredicate(card, p.predicate(), sourceCardId, gameData, cardOwnerId);
            case CardAllOfPredicate p ->
                    p.predicates().stream().allMatch(sub -> matchesCardPredicate(card, sub, sourceCardId, gameData, cardOwnerId));
            case CardAnyOfPredicate p ->
                    p.predicates().stream().anyMatch(sub -> matchesCardPredicate(card, sub, sourceCardId, gameData, cardOwnerId));
        };
    }

    // --- Permanent predicate matching ---

    /**
     * Tests whether a permanent satisfies the given {@link PermanentPredicate},
     * using game data for keyword/stat resolution.
     *
     * @see #matchesPermanentPredicate(Permanent, PermanentPredicate, FilterContext)
     */
    public boolean matchesPermanentPredicate(GameData gameData, Permanent permanent, PermanentPredicate predicate) {
        return matchesPermanentPredicate(permanent, predicate, FilterContext.of(gameData));
    }

    /**
     * Tests whether a permanent satisfies the given {@link PermanentPredicate}. Supports
     * composite predicates (all-of, any-of, not) and leaf predicates for card type, subtype,
     * keyword, color, tapped/attacking/blocking status, token status, power threshold, and
     * source identity. When a {@link FilterContext} is provided, keyword and power checks
     * include static bonuses; otherwise they use only intrinsic values.
     *
     * @param permanent     the permanent to test
     * @param predicate     the predicate to evaluate, or {@code null} (never matches)
     * @param filterContext context providing game data, source card ID, and source controller ID
     * @return {@code true} if the permanent matches the predicate
     */
    public boolean matchesPermanentPredicate(Permanent permanent,
                                             PermanentPredicate predicate,
                                             FilterContext filterContext) {
        if (predicate == null) return false;
        GameData gameData = filterContext != null ? filterContext.gameData() : null;
        UUID sourceCardId = filterContext != null ? filterContext.sourceCardId() : null;
        UUID sourceControllerId = filterContext != null ? filterContext.sourceControllerId() : null;

        return switch (predicate) {
            case PermanentHasKeywordPredicate hasKeywordPredicate -> {
                if (gameData == null) {
                    yield permanent.hasKeyword(hasKeywordPredicate.keyword());
                }
                yield gameQueryService.hasKeyword(gameData, permanent, hasKeywordPredicate.keyword());
            }
            case PermanentHasSubtypePredicate hasSubtypePredicate -> {
                boolean creatureSubtype = gameQueryService.isCreatureSubtype(hasSubtypePredicate.subtype());
                yield permanent.getCard().getSubtypes().contains(hasSubtypePredicate.subtype())
                        || permanent.getTransientSubtypes().contains(hasSubtypePredicate.subtype())
                        || permanent.getGrantedSubtypes().contains(hasSubtypePredicate.subtype())
                        || (creatureSubtype && (gameData == null
                        ? permanent.hasKeyword(Keyword.CHANGELING)
                        : gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING)));
            }
            case PermanentHasAnySubtypePredicate hasAnySubtypePredicate -> {
                boolean hasSubtype = permanent.getCard().getSubtypes().stream().anyMatch(hasAnySubtypePredicate.subtypes()::contains)
                        || permanent.getTransientSubtypes().stream().anyMatch(hasAnySubtypePredicate.subtypes()::contains)
                        || permanent.getGrantedSubtypes().stream().anyMatch(hasAnySubtypePredicate.subtypes()::contains);
                boolean canUseChangeling = hasAnySubtypePredicate.subtypes().stream().anyMatch(gameQueryService::isCreatureSubtype);
                yield hasSubtype || (canUseChangeling && (gameData == null
                        ? permanent.hasKeyword(Keyword.CHANGELING)
                        : gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING)));
            }
            case PermanentIsCreaturePredicate ignored -> {
                if (gameData == null) {
                    yield permanent.getCard().hasType(CardType.CREATURE)
                            || permanent.isAnimatedUntilEndOfTurn()
                            || permanent.isAnimatedUntilNextTurn()
                            || permanent.isPermanentlyAnimated()
                            || permanent.getCounterCount(CounterType.AWAKENING) > 0;
                }
                yield gameQueryService.isCreature(gameData, permanent);
            }
            case PermanentIsLandPredicate ignored ->
                    permanent.getCard().hasType(CardType.LAND);
            case PermanentIsArtifactPredicate ignored -> {
                if (gameData == null) {
                    yield gameQueryService.isArtifact(permanent);
                }
                yield gameQueryService.isArtifact(gameData, permanent);
            }
            case PermanentIsHistoricPredicate ignored -> {
                boolean artifact = gameData == null
                        ? gameQueryService.isArtifact(permanent)
                        : gameQueryService.isArtifact(gameData, permanent);
                yield artifact
                        || permanent.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)
                        || permanent.getCard().getSubtypes().contains(CardSubtype.SAGA)
                        || permanent.getTransientSubtypes().contains(CardSubtype.SAGA);
            }
            case PermanentIsEnchantmentPredicate ignored ->
                    permanent.getCard().hasType(CardType.ENCHANTMENT);
            case PermanentIsPlaneswalkerPredicate ignored ->
                    permanent.getCard().hasType(CardType.PLANESWALKER);
            case PermanentIsTappedPredicate ignored ->
                    permanent.isTapped();
            case PermanentIsTokenPredicate ignored ->
                    permanent.getCard().isToken();
            case PermanentIsAttackingPredicate ignored ->
                    permanent.isAttacking();
            case PermanentIsBlockingPredicate ignored ->
                    permanent.isBlocking();
            case PermanentPowerAtMostPredicate powerAtMostPredicate -> {
                if (gameData == null) {
                    yield permanent.getEffectivePower() <= powerAtMostPredicate.maxPower();
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) <= powerAtMostPredicate.maxPower();
            }
            case PermanentPowerAtMostXPredicate ignored -> {
                int xVal = filterContext != null && filterContext.xValue() != null ? filterContext.xValue() : 0;
                if (gameData == null) {
                    yield permanent.getEffectivePower() <= xVal;
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) <= xVal;
            }
            case PermanentPowerAtMostControlledCreatureCountPredicate ignored -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                int creatureCount = 0;
                if (controllerBattlefield != null) {
                    for (Permanent p : controllerBattlefield) {
                        if (gameQueryService.isCreature(gameData, p)) {
                            creatureCount++;
                        }
                    }
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) <= creatureCount;
            }
            case PermanentManaValueEqualsXPredicate ignored -> {
                // When xValue is null (e.g. during valid-target checks before X is chosen),
                // any creature is potentially valid since X can be any non-negative integer.
                if (filterContext == null || filterContext.xValue() == null) {
                    yield true;
                }
                yield permanent.getCard().getManaValue() == filterContext.xValue();
            }
            case PermanentPowerAtLeastPredicate powerAtLeastPredicate -> {
                if (gameData == null) {
                    yield permanent.getEffectivePower() >= powerAtLeastPredicate.minPower();
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) >= powerAtLeastPredicate.minPower();
            }
            case PermanentToughnessAtMostPredicate toughnessAtMostPredicate -> {
                if (gameData == null) {
                    yield permanent.getEffectiveToughness() <= toughnessAtMostPredicate.maxToughness();
                }
                yield gameQueryService.getEffectiveToughness(gameData, permanent) <= toughnessAtMostPredicate.maxToughness();
            }
            case PermanentHasSupertypePredicate hasSupertypePredicate ->
                    permanent.getCard().getSupertypes().contains(hasSupertypePredicate.supertype());
            case PermanentColorInPredicate colorInPredicate -> {
                if (permanent.isColorOverridden()) {
                    yield permanent.getTransientColors().stream().anyMatch(colorInPredicate.colors()::contains);
                }
                CardColor effectiveColor = permanent.getEffectiveColor();
                yield (effectiveColor != null && colorInPredicate.colors().contains(effectiveColor))
                        || permanent.getTransientColors().stream().anyMatch(colorInPredicate.colors()::contains)
                        || permanent.getGrantedColors().stream().anyMatch(colorInPredicate.colors()::contains);
            }
            case PermanentAnyOfPredicate anyOfPredicate -> {
                for (PermanentPredicate nested : anyOfPredicate.predicates()) {
                    if (matchesPermanentPredicate(permanent, nested, filterContext)) {
                        yield true;
                    }
                }
                yield false;
            }
            case PermanentAllOfPredicate allOfPredicate -> {
                for (PermanentPredicate nested : allOfPredicate.predicates()) {
                    if (!matchesPermanentPredicate(permanent, nested, filterContext)) {
                        yield false;
                    }
                }
                yield true;
            }
            case PermanentNotPredicate notPredicate ->
                    !matchesPermanentPredicate(permanent, notPredicate.predicate(), filterContext);
            case PermanentIsSourceCardPredicate ignored ->
                    sourceCardId != null && permanent.getOriginalCard().getId().equals(sourceCardId);
            case PermanentControlledBySourceControllerPredicate ignored -> {
                if (sourceControllerId == null || gameData == null) {
                    yield false;
                }
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                yield controllerBattlefield != null && controllerBattlefield.contains(permanent);
            }
            case PermanentAttachedToSourceControllerPredicate ignored ->
                    sourceControllerId != null && permanent.isAttached()
                            && sourceControllerId.equals(permanent.getAttachedTo());
            case PermanentToughnessLessThanSourcePowerPredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (sourcePermanent == null) {
                    yield false;
                }
                int sourcePower = gameQueryService.getEffectivePower(gameData, sourcePermanent);
                int targetToughness = gameQueryService.getEffectiveToughness(gameData, permanent);
                yield targetToughness < sourcePower;
            }
            case PermanentInCombatWithSourcePredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (sourcePermanent == null) {
                    yield false;
                }
                // Target is blocking source
                if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(sourcePermanent.getId())) {
                    yield true;
                }
                // Source is blocking target
                yield sourcePermanent.isBlocking() && sourcePermanent.getBlockingTargetIds().contains(permanent.getId());
            }
            case PermanentHasSameNameAsSourcePredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                // Find the source permanent by its current card ID (important for clones
                // where card differs from originalCard)
                Permanent sourcePermanent = findPermanentByCurrentCardId(gameData, sourceCardId);
                if (sourcePermanent == null) {
                    yield false;
                }
                yield permanent.getCard().getName().equals(sourcePermanent.getCard().getName());
            }
            case PermanentHasCountersPredicate hasCountersPredicate ->
                    switch (hasCountersPredicate.counterType()) {
                        case PLUS_ONE_PLUS_ONE -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0;
                        case MINUS_ONE_MINUS_ONE -> permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0;
                        case CHARGE -> permanent.getCounterCount(CounterType.CHARGE) > 0;
                        case LOYALTY -> permanent.getCounterCount(CounterType.LOYALTY) > 0;
                        case HATCHLING -> permanent.getCounterCount(CounterType.HATCHLING) > 0;
                        case SLIME -> permanent.getCounterCount(CounterType.SLIME) > 0;
                        case STUDY -> permanent.getCounterCount(CounterType.STUDY) > 0;
                        case WISH -> permanent.getCounterCount(CounterType.WISH) > 0;
                        case LORE -> permanent.getCounterCount(CounterType.LORE) > 0;
                        case AIM -> permanent.getCounterCount(CounterType.AIM) > 0;
                        case ANY -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0
                                || permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0
                                || permanent.getCounterCount(CounterType.CHARGE) > 0
                                || permanent.getCounterCount(CounterType.LOYALTY) > 0
                                || permanent.getCounterCount(CounterType.HATCHLING) > 0
                                || permanent.getCounterCount(CounterType.SLIME) > 0
                                || permanent.getCounterCount(CounterType.STUDY) > 0
                                || permanent.getCounterCount(CounterType.WISH) > 0
                                || permanent.getCounterCount(CounterType.LORE) > 0
                                || permanent.getCounterCount(CounterType.AIM) > 0;
                        default -> false;
                    };
            case PermanentDealtDamageThisTurnPredicate ignored ->
                    gameData != null && gameData.permanentsDealtDamageThisTurn.contains(permanent.getId());
            case PermanentTruePredicate ignored ->
                    true;
            case PermanentHasGreatestPowerAmongControlledCreaturesPredicate ignored -> {
                if (gameData == null || sourceControllerId == null) yield false;
                List<Permanent> controllerBf = gameData.playerBattlefields.get(sourceControllerId);
                if (controllerBf == null || !controllerBf.contains(permanent)) yield false;
                if (!gameQueryService.isCreature(gameData, permanent)) yield false;
                int maxPower = controllerBf.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .mapToInt(p -> gameQueryService.getEffectivePower(gameData, p))
                        .max().orElse(0);
                yield gameQueryService.getEffectivePower(gameData, permanent) == maxPower;
            }
        };
    }

    private Permanent findPermanentByOriginalCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getOriginalCard().getId().equals(cardId)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    private Permanent findPermanentByCurrentCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard().getId().equals(cardId)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    // --- Stack entry predicate matching ---

    /**
     * Evaluates a {@link StackEntryPredicate} against a stack entry, supporting predicates that
     * reference the "enchanted player" — the player the evaluating source permanent is attached to.
     *
     * <p>This is the static-effect-context evaluation (e.g. damage multipliers). Targeting-context
     * predicates (single-target, has-target, mana-value, controlled-by, targets-your-permanent,
     * targets-you-or-creature-you-control) are evaluated by
     * {@code TargetLegalityService#matchesStackEntryPredicate} instead and never match here.</p>
     *
     * @param enchantedPlayerId the player the source aura is attached to, or {@code null} when no
     *                          such context applies (e.g. damage-multiplier evaluation)
     */
    public boolean matchesStackEntryPredicate(StackEntry entry, StackEntryPredicate predicate, UUID enchantedPlayerId) {
        if (predicate == null) return false;
        return switch (predicate) {
            case StackEntryControlledByEnchantedPlayerPredicate ignored ->
                    enchantedPlayerId != null && enchantedPlayerId.equals(entry.getControllerId());
            case StackEntryTypeInPredicate typeIn ->
                    typeIn.spellTypes().contains(entry.getEntryType());
            case StackEntryColorInPredicate colorIn -> {
                List<CardColor> colors = entry.getCard().getColors();
                if (colors == null) yield false;
                for (CardColor color : colors) {
                    if (colorIn.colors().contains(color)) yield true;
                }
                yield false;
            }
            case StackEntryAllOfPredicate allOf -> {
                for (StackEntryPredicate nested : allOf.predicates()) {
                    if (!matchesStackEntryPredicate(entry, nested, enchantedPlayerId)) yield false;
                }
                yield true;
            }
            case StackEntryAnyOfPredicate anyOf -> {
                for (StackEntryPredicate nested : anyOf.predicates()) {
                    if (matchesStackEntryPredicate(entry, nested, enchantedPlayerId)) yield true;
                }
                yield false;
            }
            case StackEntryNotPredicate not ->
                    !matchesStackEntryPredicate(entry, not.predicate(), enchantedPlayerId);
            // Targeting-only predicates: evaluated by TargetLegalityService, never in this context.
            case StackEntryIsSingleTargetPredicate ignored -> false;
            case StackEntryHasTargetPredicate ignored -> false;
            case StackEntryManaValuePredicate ignored -> false;
            case StackEntryControlledByPredicate ignored -> false;
            case StackEntryTargetsYourPermanentPredicate ignored -> false;
            case StackEntryTargetsYouOrCreatureYouControlPredicate ignored -> false;
            case StackEntryTargetsPermanentPredicate ignored -> false;
        };
    }

    // --- Target filtering & validation ---

    /**
     * Returns {@code true} if the permanent passes all of the given target filters.
     *
     * @see #matchesFilters(Permanent, Set, FilterContext)
     */
    public boolean matchesFilters(GameData gameData, Permanent permanent, Set<TargetFilter> filters) {
        return matchesFilters(permanent, filters, FilterContext.of(gameData));
    }

    /**
     * Returns {@code true} if the permanent passes all of the given target filters,
     * using the provided {@link FilterContext} for source-aware checks (e.g. "controlled
     * by source's controller" or "owned by source's controller").
     */
    public boolean matchesFilters(Permanent permanent,
                                  Set<TargetFilter> filters,
                                  FilterContext filterContext) {
        for (TargetFilter filter : filters) {
            if (!matchesSingleFilter(filter, permanent, filterContext)) return false;
        }
        return true;
    }

    /**
     * Validates that the target permanent passes the given filter.
     * Returns the filter's error message if it does not.
     */
    public Optional<String> checkTargetFilter(TargetFilter filter, Permanent target) {
        return checkTargetFilter(filter, target, FilterContext.empty());
    }

    public Optional<String> checkTargetFilter(TargetFilter filter,
                                              Permanent target,
                                              FilterContext filterContext) {
        if (!matchesSingleFilter(filter, target, filterContext)) {
            return Optional.of(getFilterErrorMessage(filter));
        }
        return Optional.empty();
    }

    public void validateTargetFilter(TargetFilter filter, Permanent target) {
        checkTargetFilter(filter, target).ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    /**
     * Validates that the target permanent passes the given filter, using game data
     * for source-aware checks. Throws {@link IllegalStateException} if it does not.
     */
    public void validateTargetFilter(GameData gameData, TargetFilter filter, Permanent target) {
        checkTargetFilter(filter, target, FilterContext.of(gameData))
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    /**
     * Validates that the target permanent passes the given filter, using the provided
     * {@link FilterContext}. Throws {@link IllegalStateException} with the filter's
     * error message if it does not.
     */
    public void validateTargetFilter(TargetFilter filter,
                                     Permanent target,
                                     FilterContext filterContext) {
        checkTargetFilter(filter, target, filterContext)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    private boolean matchesSingleFilter(TargetFilter filter, Permanent target, FilterContext filterContext) {
        if (filter == null) return true;
        GameData gameData = filterContext != null ? filterContext.gameData() : null;
        UUID sourceControllerId = filterContext != null ? filterContext.sourceControllerId() : null;

        return switch (filter) {
            case ControlledPermanentPredicateTargetFilter controlledFilter -> {
                if (sourceControllerId == null || gameData == null) yield false;
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                if (controllerBattlefield == null || !controllerBattlefield.contains(target)) yield false;
                yield matchesPermanentPredicate(target, controlledFilter.predicate(), filterContext);
            }
            case OwnedPermanentPredicateTargetFilter ownedFilter -> {
                if (gameData == null || sourceControllerId == null) yield false;
                boolean ownedByController = false;
                for (UUID playerId : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield != null && battlefield.contains(target)) {
                        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                        ownedByController = ownerId.equals(sourceControllerId);
                        break;
                    }
                }
                if (!ownedByController) yield false;
                yield matchesPermanentPredicate(target, ownedFilter.predicate(), filterContext);
            }
            case PermanentPredicateTargetFilter f ->
                    matchesPermanentPredicate(target, f.predicate(), filterContext);
            case PlayerPredicateTargetFilter ignored -> false;
            // Stack-entry filters never restrict a permanent target.
            case StackEntryPredicateTargetFilter ignored -> true;
        };
    }

    private String getFilterErrorMessage(TargetFilter filter) {
        return switch (filter) {
            case ControlledPermanentPredicateTargetFilter f -> f.errorMessage();
            case OwnedPermanentPredicateTargetFilter f -> f.errorMessage();
            case PermanentPredicateTargetFilter f -> f.errorMessage();
            case PlayerPredicateTargetFilter f -> f.errorMessage();
            case StackEntryPredicateTargetFilter f -> f.errorMessage();
        };
    }
}

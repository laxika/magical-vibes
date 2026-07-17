package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.AllowExtraLoyaltyActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AttackOrBlockRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.BlockabilityRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.BlockingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.NoDefenderAttackPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEquippedEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTransformEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetColorMode;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingSourceKind;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantGetPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.AllDamageDealtWithWitherEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantReduceLifeBelowOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealtAsInfectBelowZeroLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerHasProtectionFromChosenNameEffect;
import com.github.laxika.magicalvibes.model.effect.ActivateCreatureAbilitiesAsThoughHasteEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantActivateAbilitiesOfGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleEquippedCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerShroudEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.ManaReflectionEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventColorDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.battlefield.BlockLegalityContext.BlockDenial;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.layer.ModifierLine;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Read-only query service for inspecting game state. Provides methods for looking up permanents
 * and cards, evaluating predicates and filters, computing effective stats (including static bonuses
 * from other permanents, auras, emblems, and granted effects), and checking protection, keywords,
 * evasion, and other derived properties.
 *
 * <p>This service never mutates game state. All methods are safe to call from validation,
 * combat resolution, AI evaluation, and view-building code.
 */
@Component
@RequiredArgsConstructor
public class GameQueryService {

    public static final List<String> TEXT_CHANGE_COLOR_WORDS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
    public static final List<String> TEXT_CHANGE_LAND_TYPES = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP,
            CardSubtype.AURA,
            CardSubtype.EQUIPMENT,
            CardSubtype.AJANI
    );

    private final StaticEffectHandlerRegistry staticEffectRegistry;

    /**
     * The CR 613 layered engine: computes the whole-battlefield layer-4 (type-changing) pass
     * whose {@code CharacteristicState}s the legacy layer 5-7 accumulator below reads its
     * type/subtype filter answers from. See {@code agent-docs/LAYER_SYSTEM.md}. Injected
     * lazily like the other collaborators because it evaluates predicates, which query game
     * state through this service.
     */
    @Autowired
    @Lazy
    private LayerSystemService layerSystemService;

    /**
     * Evaluates conditional static effects (e.g. metalcraft-animate checks). Injected lazily
     * because the evaluation service itself queries game state through this service.
     */
    @Autowired
    @Lazy
    private ConditionEvaluationService conditionEvaluationService;

    /**
     * Evaluates card/permanent/stack-entry predicates and target filters. Injected lazily
     * because the evaluation service itself queries game state through this service.
     */
    @Autowired
    @Lazy
    private PredicateEvaluationService predicateEvaluationService;

    /**
     * Aggregated static bonuses from other permanents, auras, emblems, and self-referencing
     * effects for a single permanent. Computed on-the-fly by {@link #computeStaticBonus} and
     * never stored on the permanent itself.
     *
     * @param power                     total power modifier from static effects
     * @param toughness                 total toughness modifier from static effects
     * @param keywords                  keywords granted by static effects
     * @param protectionColors          protection colors granted by static effects
     * @param animatedCreature          whether the permanent is animated into a creature
     * @param grantedActivatedAbilities activated abilities granted by static effects
     * @param grantedEffects            card effects granted by static effects
     * @param grantedColors             colors granted by static effects
     * @param grantedSubtypes           subtypes granted by static effects
     * @param grantedCardTypes          card types granted by static effects
     * @param colorOverriding           whether granted colors replace the permanent's natural color
     * @param subtypeOverriding         whether granted subtypes replace the permanent's natural subtypes
     */
    public record StaticBonus(int power, int toughness, Set<Keyword> keywords, Set<CardColor> protectionColors, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, List<CardEffect> grantedEffects, Set<CardColor> grantedColors, List<CardSubtype> grantedSubtypes, Set<CardType> grantedCardTypes, Set<CardSupertype> grantedSupertypes, boolean colorOverriding, boolean subtypeOverriding, boolean landSubtypeOverriding, Set<Keyword> removedKeywords, boolean basePTOverridden, int basePowerOverride, int baseToughnessOverride, boolean losesAllAbilities, boolean ptSwitched) {
        static final StaticBonus NONE = new StaticBonus(0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(), Set.of(), false, false, false, Set.of(), false, 0, 0, false, false);
    }

    // --- Lookup helpers ---

    private <T> T findInBattlefields(GameData gameData, UUID id, BiFunction<UUID, Permanent, T> mapper) {
        if (id == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(id)) return mapper.apply(playerId, p);
            }
        }
        return null;
    }

    private <T> T findInGraveyards(GameData gameData, UUID id, BiFunction<UUID, Card, T> mapper) {
        if (id == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> gy = gameData.playerGraveyards.get(playerId);
            if (gy == null) continue;
            for (Card c : gy) {
                if (c.getId().equals(id)) return mapper.apply(playerId, c);
            }
        }
        return null;
    }

    private boolean hasCardType(Card card, CardType type) {
        return card.hasType(type);
    }

    /**
     * Returns true if the given player cast at least one historic spell (artifact, legendary, or Saga) this turn.
     */
    public boolean playerCastHistoricSpellThisTurn(GameData gameData, UUID playerId) {
        return gameData.getSpellsCastThisTurn(playerId).stream()
                .anyMatch(card -> predicateEvaluationService.matchesCardPredicate(card, new CardIsHistoricPredicate(), card.getId()));
    }

    private boolean hasCardType(Permanent permanent, CardType type) {
        return hasCardType(permanent.getCard(), type);
    }

    private boolean playerBattlefieldHasStaticEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectType) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            if (perm.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(effectType::isInstance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given permanent can't transform because its controller controls a
     * permanent with a {@link PreventTransformEffect} whose filter matches it (e.g. Immerwolf's
     * "Non-Human Werewolves you control can't transform"). The filter is evaluated against the
     * permanent's current face.
     */
    public boolean isTransformPrevented(GameData gameData, Permanent permanent) {
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent source : battlefield) {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PreventTransformEffect prevent
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, prevent.filter())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean anyBattlefieldHasStaticEffect(GameData gameData, Class<? extends CardEffect> effectType) {
        return gameData.anyPermanentMatches(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(effectType::isInstance));
    }

    // --- Permanent / Card lookups ---

    /**
     * Finds a permanent on any player's battlefield by its unique ID.
     *
     * @return the permanent, or {@code null} if not found
     */
    public Permanent findPermanentById(GameData gameData, UUID permanentId) {
        return findInBattlefields(gameData, permanentId, (playerId, p) -> p);
    }

    /**
     * Finds the controller (player ID) of a permanent by the permanent's unique ID.
     *
     * @return the controlling player's ID, or {@code null} if the permanent is not on any battlefield
     */
    public UUID findPermanentController(GameData gameData, UUID permanentId) {
        return findInBattlefields(gameData, permanentId, (playerId, p) -> playerId);
    }

    public void setImprintedCardOnPermanent(GameData gameData, UUID sourcePermanentId, Card card) {
        Permanent perm = findPermanentById(gameData, sourcePermanentId);
        if (perm != null) {
            gameData.setImprintedCard(perm.getCard(), card);
        }
    }

    /**
     * Finds a card in any player's graveyard by its unique ID.
     *
     * @return the card, or {@code null} if not found
     */
    public Card findCardInGraveyardById(GameData gameData, UUID cardId) {
        return findInGraveyards(gameData, cardId, (playerId, c) -> c);
    }

    /**
     * Finds the owner (player ID) of a card in a graveyard by the card's unique ID.
     *
     * @return the owning player's ID, or {@code null} if the card is not in any graveyard
     */
    public UUID findGraveyardOwnerById(GameData gameData, UUID cardId) {
        return findInGraveyards(gameData, cardId, (playerId, c) -> playerId);
    }

    /**
     * Finds a card in any player's exile zone by its unique ID.
     *
     * @return the card, or {@code null} if not found
     */
    public Card findCardInExileById(GameData gameData, UUID cardId) {
        return findInExile(gameData, cardId, (playerId, c) -> c);
    }

    /**
     * Finds the owner (player ID) of a card in exile by the card's unique ID.
     *
     * @return the owning player's ID, or {@code null} if the card is not in any exile zone
     */
    public UUID findExileOwnerById(GameData gameData, UUID cardId) {
        return findInExile(gameData, cardId, (playerId, c) -> playerId);
    }

    public StackEntry findStackEntryByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) return null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(cardId)) {
                return se;
            }
        }
        return null;
    }

    private <T> T findInExile(GameData gameData, UUID id, BiFunction<UUID, Card, T> mapper) {
        if (id == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> exile = gameData.getPlayerExiledCards(playerId);
            for (Card c : exile) {
                if (c.getId().equals(id)) return mapper.apply(playerId, c);
            }
        }
        return null;
    }

    // --- Arcane Adaptation / all-zone subtype grants ---

    /**
     * Returns {@code true} if the card has the given subtype, considering both its natural
     * subtypes and any subtypes granted by "affects all zones" static effects (e.g. Arcane
     * Adaptation). Only creature cards receive granted subtypes.
     *
     * <p>When {@code gameData} or {@code cardOwnerId} is {@code null}, falls back to checking
     * only the card's natural subtypes.
     */
    public boolean cardHasSubtype(Card card, CardSubtype subtype, GameData gameData, UUID cardOwnerId) {
        if (card.getSubtypes().contains(subtype)) return true;
        if (gameData == null || cardOwnerId == null) return false;
        if (!card.hasType(CardType.CREATURE)) return false;
        return computeGrantedSubtypesForOwnedCreatureCard(gameData, cardOwnerId).contains(subtype);
    }

    private static final Set<CardSubtype> BASIC_LAND_SUBTYPES = EnumSet.of(
            CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
            CardSubtype.MOUNTAIN, CardSubtype.FOREST);

    /**
     * The effective basic land types (Plains/Island/Swamp/Mountain/Forest) of a permanent,
     * respecting CR 305.7 land-type overrides (Blood Moon, Tideshaper Mystic) — when a land-type
     * setter is active the printed and one-shot-granted types are replaced by the setter's types.
     * Used for Domain counting.
     */
    public Set<CardSubtype> effectiveBasicLandTypes(GameData gameData, Permanent permanent) {
        Set<CardSubtype> result = EnumSet.noneOf(CardSubtype.class);
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (!bonus.landSubtypeOverriding()) {
            for (CardSubtype st : permanent.getCard().getSubtypes()) {
                if (BASIC_LAND_SUBTYPES.contains(st)) result.add(st);
            }
            for (CardSubtype st : permanent.getGrantedSubtypes()) {
                if (BASIC_LAND_SUBTYPES.contains(st)) result.add(st);
            }
        }
        for (CardSubtype st : bonus.grantedSubtypes()) {
            if (BASIC_LAND_SUBTYPES.contains(st)) result.add(st);
        }
        for (CardSubtype st : permanent.getTransientSubtypes()) {
            if (BASIC_LAND_SUBTYPES.contains(st)) result.add(st);
        }
        return result;
    }

    /**
     * Returns all subtypes of a creature card, including those granted by Arcane Adaptation-style effects.
     */
    public Set<CardSubtype> getCardSubtypes(Card card, GameData gameData, UUID cardOwnerId) {
        Set<CardSubtype> subtypes = new java.util.HashSet<>(card.getSubtypes());
        if (gameData != null && cardOwnerId != null && card.hasType(CardType.CREATURE)) {
            subtypes.addAll(computeGrantedSubtypesForOwnedCreatureCard(gameData, cardOwnerId));
        }
        return subtypes;
    }

    /**
     * Computes the list of subtypes granted to creature cards owned by the given player in
     * non-battlefield zones (hand, graveyard, library, exile) and creature spells they control
     * on the stack. Scans the owner's battlefield for permanents with
     * {@link GrantChosenSubtypeToOwnCreaturesEffect#affectsAllZones()} == {@code true}.
     */
    public List<CardSubtype> computeGrantedSubtypesForOwnedCreatureCard(GameData gameData, UUID ownerId) {
        List<CardSubtype> result = new ArrayList<>();
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf == null) return result;
        for (Permanent perm : bf) {
            CardSubtype chosen = perm.getChosenSubtype();
            if (chosen == null) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantChosenSubtypeToOwnCreaturesEffect g && g.affectsAllZones()) {
                    if (!result.contains(chosen)) {
                        result.add(chosen);
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Computes the graveyard-activated abilities granted to creature cards owned by the given player
     * by static effects on that player's battlefield (e.g. Sedris, the Traitor King grants unearth
     * {2}{B} to each creature card in its controller's graveyard). Scans the owner's battlefield for
     * permanents carrying {@link GrantGraveyardAbilityToCreatureCardsEffect}.
     */
    public List<ActivatedAbility> computeGrantedGraveyardAbilitiesForOwnedCreatureCard(GameData gameData, UUID ownerId) {
        List<ActivatedAbility> result = new ArrayList<>();
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf == null) return result;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GraveyardAbilityGrantingEffect g) {
                    result.add(g.grantedGraveyardAbility());
                }
            }
        }
        return result;
    }

    // --- Player queries ---

    /**
     * Returns the opponent's player ID in a two-player game.
     */
    public UUID getOpponentId(GameData gameData, UUID playerId) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        return ids.get(0).equals(playerId) ? ids.get(1) : ids.get(0);
    }

    /**
     * Returns the player ID of whoever currently has priority, following APNAP
     * (Active Player, Non-Active Player) order. Returns {@code null} if both players
     * have already passed priority.
     */
    public UUID getPriorityPlayerId(GameData data) {
        if (data.activePlayerId == null) {
            return null;
        }
        if (!data.priorityPassedBy.contains(data.activePlayerId)) {
            return data.activePlayerId;
        }
        List<UUID> ids = new ArrayList<>(data.orderedPlayerIds);
        UUID nonActive = ids.get(0).equals(data.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!data.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }

    /**
     * Returns {@code true} if the player's life total is allowed to change (i.e. no
     * {@link LifeTotalCantChangeEffect} is present on their battlefield).
     */
    public boolean canPlayerLifeChange(GameData gameData, UUID playerId) {
        return !playerBattlefieldHasStaticEffect(gameData, playerId, LifeTotalCantChangeEffect.class);
    }

    /**
     * Returns {@code true} if the player is able to gain life (i.e. no
     * {@link PlayersCantGainLifeEffect} is present on any battlefield and
     * no {@link LifeTotalCantChangeEffect} prevents life changes).
     */
    public boolean canPlayerGainLife(GameData gameData, UUID playerId) {
        if (!canPlayerLifeChange(gameData, playerId)) return false;
        if (gameData.playersWhoCantGainLifeRestOfGame.contains(playerId)) return false;
        return !anyBattlefieldHasStaticEffect(gameData, PlayersCantGainLifeEffect.class);
    }

    /**
     * Returns the multiplier applied to life the given player gains, per any
     * {@link DoubleLifeGainEffect} static effects they control (e.g. Boon Reflection). Each such
     * effect doubles the life gained, and multiple stack multiplicatively (2^count), matching the
     * Rhox Faithmender / Alhammarret's Archive ruling. Returns 1 when the player controls none.
     */
    public int lifeGainMultiplier(GameData gameData, UUID playerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return 1;
        int doublers = 0;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof DoubleLifeGainEffect) doublers++;
            }
        }
        return 1 << doublers;
    }

    /**
     * Returns the multiplier applied to mana the given player produces by tapping a permanent for
     * mana, per any {@link ManaReflectionEffect} static effects they control (Mana Reflection). Each
     * such effect doubles the mana produced, and multiple stack multiplicatively (2^count). Returns
     * 1 when the player controls none.
     */
    public int manaProductionMultiplier(GameData gameData, UUID playerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return 1;
        int reflections = 0;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ManaReflectionEffect) reflections++;
            }
        }
        return 1 << reflections;
    }

    /**
     * Returns {@code true} if players are allowed to cast spells from the given zone.
     * Returns {@code false} when a {@link PlayersCantCastSpellsFromZonesEffect} whose
     * {@code zones} contains {@code zone} is on any battlefield (e.g. Ashes of the Abhorrent
     * for graveyards, Grafdigger's Cage for graveyards and libraries).
     */
    public boolean canPlayersCastSpellsFromZone(GameData gameData, Zone zone) {
        return !gameData.anyPermanentMatches(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(PlayersCantCastSpellsFromZonesEffect.class::isInstance)
                        .map(PlayersCantCastSpellsFromZonesEffect.class::cast)
                        .anyMatch(e -> e.zones().contains(zone)));
    }

    /**
     * Returns {@code true} if the given card is barred from entering the battlefield from
     * {@code zone} by a {@link CardsCantEnterBattlefieldFromZonesEffect} on any battlefield
     * (e.g. Grafdigger's Cage). The card is tested against each such effect's filter, and the
     * effect must list {@code zone} in its {@code zones}, so only matching cards (e.g. creature
     * cards) entering from a blocked zone are stopped.
     */
    public boolean isCardBlockedFromEnteringFromZone(GameData gameData, Card card, Zone zone) {
        return gameData.anyPermanentMatches(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(CardsCantEnterBattlefieldFromZonesEffect.class::isInstance)
                        .map(CardsCantEnterBattlefieldFromZonesEffect.class::cast)
                        .anyMatch(e -> e.zones().contains(zone) && predicateEvaluationService.matchesCardPredicate(card, e.filter(), null)));
    }

    /**
     * Returns {@code true} if players are allowed to activate abilities of cards in graveyards.
     * Returns {@code false} when a {@link PlayersCantActivateAbilitiesOfGraveyardCardsEffect}
     * is on any battlefield (e.g. Ashes of the Abhorrent).
     */
    public boolean canPlayersActivateGraveyardAbilities(GameData gameData) {
        return !anyBattlefieldHasStaticEffect(gameData, PlayersCantActivateAbilitiesOfGraveyardCardsEffect.class);
    }

    /**
     * Returns {@code true} if the given player may activate abilities of creatures they control as
     * though those creatures had haste (i.e. they control a permanent with
     * {@link ActivateCreatureAbilitiesAsThoughHasteEffect}, e.g. Thousand-Year Elixir). This only
     * lifts the summoning sickness restriction on ability activation — it does not grant haste.
     */
    public boolean canActivateCreatureAbilitiesAsThoughHaste(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, ActivateCreatureAbilitiesAsThoughHasteEffect.class);
    }

    /**
     * Returns {@code true} if damage can be prevented. Returns {@code false}
     * when a {@link DamageCantBePreventedEffect} is on any battlefield
     * (e.g. Leyline of Punishment).
     */
    public boolean isDamagePreventable(GameData gameData) {
        return !gameData.damageCantBePreventedThisTurn
                && !anyBattlefieldHasStaticEffect(gameData, DamageCantBePreventedEffect.class);
    }

    /**
     * Returns {@code true} if the player is able to lose the game (i.e. no
     * {@link CantLoseGameEffect} is present on their battlefield).
     */
    public boolean canPlayerLoseGame(GameData gameData, UUID playerId) {
        return !playerBattlefieldHasStaticEffect(gameData, playerId, CantLoseGameEffect.class);
    }

    /**
     * Returns {@code true} if the player can lose the game from having 0 or less life
     * (i.e. no {@link CantLoseGameFromLifeEffect} is present on their battlefield).
     */
    public boolean canPlayerLoseFromLife(GameData gameData, UUID playerId) {
        return !playerBattlefieldHasStaticEffect(gameData, playerId, CantLoseGameFromLifeEffect.class);
    }

    /**
     * Returns {@code true} if damage dealt to this player should be dealt as though
     * its source had infect. This is true when the player controls a permanent with
     * {@link DamageDealtAsInfectBelowZeroLifeEffect} and has 0 or less life.
     */
    public boolean shouldDamageBeDealtAsInfect(GameData gameData, UUID playerId) {
        if (!playerBattlefieldHasStaticEffect(gameData, playerId, DamageDealtAsInfectBelowZeroLifeEffect.class)) {
            return false;
        }
        int life = gameData.getLife(playerId);
        return life <= 0;
    }

    /**
     * Returns {@code true} if damage dealt to this player can't reduce their life total below 1
     * (Worship). This is true only when the player controls a permanent with
     * {@link DamageCantReduceLifeBelowOneEffect} AND currently controls a creature (Worship's
     * "If you control a creature" clause).
     */
    public boolean damageCantReduceLifeBelowOne(GameData gameData, UUID playerId) {
        if (!playerBattlefieldHasStaticEffect(gameData, playerId, DamageCantReduceLifeBelowOneEffect.class)) {
            return false;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        return bf.stream().anyMatch(p -> isCreature(gameData, p));
    }

    // --- Creature / type classification ---

    /**
     * Returns {@code true} if the permanent is currently a creature. This accounts for
     * the permanent's natural card type, temporary animation effects, awakening counters,
     * global artifact animation, and metalcraft-conditional self-animation.
     */
    public boolean isCreature(GameData gameData, Permanent permanent) {
        if (hasCardType(permanent, CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        if (permanent.isAnimatedUntilEndOfCombat()) return true;
        if (permanent.isAnimatedUntilNextTurn()) return true;
        if (permanent.isPermanentlyAnimated()) return true;
        if (permanent.getCounterCount(CounterType.AWAKENING) > 0) return true;
        if (isArtifact(permanent) && hasAnimateArtifactEffect(gameData)) return true;
        if (hasCardType(permanent, CardType.LAND) && matchesAnimateLand(gameData, permanent)) return true;
        if (hasAuraBecomeCreatureEffect(gameData, permanent)) return true;
        return hasSelfBecomeCreatureEffect(gameData, permanent);
    }

    /**
     * Returns {@code true} if an aura attached to the given permanent carries an
     * {@link EnchantedPermanentBecomesCreatureEffect} (e.g. Living Terrain), which continuously
     * makes the enchanted permanent a creature.
     */
    public boolean hasAuraBecomeCreatureEffect(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent source : battlefield) {
                if (source.isAttached() && permanent.getId().equals(source.getAttachedTo())) {
                    for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof EnchantedPermanentBecomesCreatureEffect) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the permanent has a conditional self-scope
     * {@link AnimatePermanentsEffect} and its condition is currently met.
     */
    public boolean hasSelfBecomeCreatureEffect(GameData gameData, Permanent permanent) {
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof AnimatePermanentsEffect animate
                    && animate.scope() == GrantScope.SELF) {
                UUID controllerId = findPermanentController(gameData, permanent.getId());
                if (controllerId != null && conditionEvaluationService.isMet(gameData,
                        conditional.condition(), ConditionContext.forPermanent(permanent, controllerId))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given player controls three or more artifacts (metalcraft).
     */
    public boolean isMetalcraftMet(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        long artifactCount = battlefield.stream()
                .filter(p -> isArtifact(gameData, p))
                .count();
        return artifactCount >= 3;
    }

    /**
     * Returns {@code true} if any opponent controls strictly more lands than the given player
     * (e.g. Gift of Estates, Weathered Wayfarer's "an opponent controls more lands than you").
     */
    public boolean anyOpponentControlsMoreLands(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        int yourLands = countLandsControlled(gameData, controllerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (countLandsControlled(gameData, candidateOpponentId) > yourLands) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if any opponent of the given player controls a creature with flying
     * (Groundling Pouncer's activation restriction).
     */
    public boolean anyOpponentControlsFlyingCreature(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(candidateOpponentId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (isCreature(gameData, permanent) && hasKeyword(gameData, permanent, Keyword.FLYING)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int countLandsControlled(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().hasType(CardType.LAND)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns {@code true} if the given player controls at least one permanent matching
     * the predicate besides the given source card.
     */
    public boolean controlsAnotherPermanent(GameData gameData, UUID controllerId, Card sourceCard, PermanentPredicate predicate) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .anyMatch(p -> p.getCard() != sourceCard && predicateEvaluationService.matchesPermanentPredicate(gameData, p, predicate));
    }

    /**
     * Returns {@code true} if a creature died this turn (morbid condition).
     * Checks all players' death counts since morbid is not controller-specific.
     */
    public boolean isMorbidMet(GameData gameData) {
        return gameData.creatureDeathCountThisTurn.values().stream()
                .anyMatch(count -> count > 0);
    }

    /**
     * Returns {@code true} if the given player has cast another spell matching {@code filter}
     * this turn, excluding {@code excludeSpell} (typically the spell currently resolving).
     */
    public boolean hasControllerCastAnotherSpellThisTurn(
            GameData gameData, UUID controllerId, Card excludeSpell, CardPredicate filter) {
        for (Card spell : gameData.getSpellsCastThisTurn(controllerId)) {
            if (spell == excludeSpell) {
                continue;
            }
            if (predicateEvaluationService.matchesCardPredicate(spell, filter, spell.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given creature can attack despite having defender.
     * Checks for {@link CanAttackAsThoughNoDefenderEffect} in static effects, including
     * those wrapped in a {@link ConditionalEffect} (e.g. metalcraft).
     */
    public boolean canAttackDespiteDefender(GameData gameData, Permanent creature) {
        UUID controllerId = findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof NoDefenderAttackPermissionEffect permission
                    && permission.grantsCarrierAttackAsThoughNoDefender()) {
                return true;
            }
            if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof NoDefenderAttackPermissionEffect permission
                    && permission.grantsCarrierAttackAsThoughNoDefender()) {
                if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(creature, controllerId))) {
                    return true;
                }
            }
        }
        // An Aura attached to this creature that grants the permission (e.g. Animate Wall).
        if (hasAuraWithEffect(gameData, creature, CanAttackAsThoughNoDefenderEffect.class)) {
            return true;
        }
        // Until-end-of-turn grants from a resolved activated ability (e.g. Wall of Wonder),
        // stored as floating effects affecting this creature.
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (floating.effect() instanceof NoDefenderAttackPermissionEffect permission
                        && permission.grantsCarrierAttackAsThoughNoDefender()
                        && creature.getId().equals(floating.affectedPermanentId())) {
                    return true;
                }
            }
        }
        // Global grants: any permanent (any controller) whose STATIC effects let matching
        // creatures attack despite defender (e.g. Rolling Stones for Wall creatures).
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent grantor : bf) {
                for (CardEffect effect : grantor.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof NoDefenderAttackPermissionEffect grant
                            && grant.noDefenderAttackMatcher() != null
                            && predicateEvaluationService.matchesPermanentPredicate(gameData, creature, grant.noDefenderAttackMatcher())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the permanent is an artifact, either by its natural card type,
     * a transient granted card type (until end of turn), or a persistent granted card type (permanent).
     */
    public boolean isArtifact(Permanent permanent) {
        return hasCardType(permanent, CardType.ARTIFACT)
                || permanent.getGrantedCardTypes().contains(CardType.ARTIFACT)
                || permanent.getPersistentGrantedCardTypes().contains(CardType.ARTIFACT);
    }

    /**
     * Returns {@code true} if the permanent is an artifact, checking natural card type,
     * temporary granted card types, and static card type grants (e.g. from equipment).
     */
    public boolean isArtifact(GameData gameData, Permanent permanent) {
        return isArtifact(permanent)
                || computeStaticBonus(gameData, permanent).grantedCardTypes().contains(CardType.ARTIFACT);
    }

    /**
     * Returns {@code true} if the permanent is an enchantment, either by its natural card type,
     * a transient granted card type (until end of turn), or a persistent granted card type (permanent).
     */
    public boolean isEnchantment(Permanent permanent) {
        return hasCardType(permanent, CardType.ENCHANTMENT)
                || permanent.getGrantedCardTypes().contains(CardType.ENCHANTMENT)
                || permanent.getPersistentGrantedCardTypes().contains(CardType.ENCHANTMENT);
    }

    /**
     * Returns {@code true} if the permanent is an enchantment, checking natural card type,
     * temporary granted card types, and static card type grants (e.g. from Enchanted Evening).
     */
    public boolean isEnchantment(GameData gameData, Permanent permanent) {
        return isEnchantment(permanent)
                || computeStaticBonus(gameData, permanent).grantedCardTypes().contains(CardType.ENCHANTMENT);
    }

    // --- Keyword & effect checking ---

    /**
     * Returns {@code true} if the permanent has the given keyword after the CR 613 layered
     * computation: {@code bonus.keywords()} is the complete final keyword set (printed keywords
     * included, layer-6 grants added and removals/ability loss applied in timestamp order).
     * {@link StaticBonus#NONE} means no continuous effect touched the permanent — the intrinsic
     * answer stands.
     */
    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return hasKeyword(permanent, computeStaticBonus(gameData, permanent), keyword);
    }

    /**
     * Keyword check against a pre-computed static bonus, for callers that read many keywords
     * off the same permanent (mirrors {@link #getEffectivePower(Permanent, StaticBonus)}).
     */
    public boolean hasKeyword(Permanent permanent, StaticBonus bonus, Keyword keyword) {
        if (bonus == StaticBonus.NONE) {
            return permanent.hasKeyword(keyword);
        }
        if (bonus.removedKeywords().contains(keyword)) return false;
        return bonus.keywords().contains(keyword);
    }

    /**
     * Returns the permanent's colors after the CR 613 layer-5 computation: the natural color
     * plus additive grants, or the replacement set when a color-setting effect ("becomes red")
     * applied. Prefer this over {@link Permanent#getEffectiveColor()} in engine code — the
     * legacy accessor does not see layered color changes.
     */
    public Set<CardColor> getEffectiveColors(GameData gameData, Permanent permanent) {
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.colorOverriding()) {
            return bonus.grantedColors();
        }
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        colors.addAll(permanent.getEffectiveColors());
        colors.addAll(permanent.getGrantedColors());
        colors.addAll(bonus.grantedColors());
        return colors;
    }

    /** Returns {@code true} if the permanent currently has the given color (layer-5 aware). */
    public boolean hasColor(GameData gameData, Permanent permanent, CardColor color) {
        return color != null && getEffectiveColors(gameData, permanent).contains(color);
    }

    /**
     * Layer-5 aware replacement for {@link Permanent#getEffectiveColor()} at call sites that
     * need a single color (legacy single-color APIs like the color damage-prevention counters):
     * a color-setting effect's replacement color when one applies, otherwise the intrinsic
     * answer. Multicolor-sensitive checks should iterate {@link #getEffectiveColors} instead.
     */
    public CardColor getEffectiveColor(GameData gameData, Permanent permanent) {
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.colorOverriding() && !bonus.grantedColors().isEmpty()) {
            return bonus.grantedColors().iterator().next();
        }
        return permanent.getEffectiveColor();
    }

    /**
     * Returns {@code true} if the permanent has been granted the given effect type
     * by static effects from other permanents (e.g. via {@code GrantEffectEffect}).
     */
    public boolean hasGrantedEffect(GameData gameData, Permanent permanent, Class<? extends CardEffect> effectType) {
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(effectType::isInstance);
    }

    /**
     * Returns {@code true} if the permanent cannot have counters placed on it,
     * either from its own static effects or from effects granted by other permanents.
     */
    public boolean cantHaveCounters(GameData gameData, Permanent permanent) {
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantHaveCountersEffect.class::isInstance)) {
            return true;
        }
        return hasGrantedEffect(gameData, permanent, CantHaveCountersEffect.class);
    }

    /**
     * Returns {@code true} if the permanent cannot be the target of opponents' abilities,
     * either from its own static effects or from effects granted by other permanents.
     * This does NOT block spells — only activated and triggered abilities.
     */
    public boolean cantBeTargetOfOpponentAbilities(GameData gameData, Permanent permanent) {
        if (!permanent.isLosesAllAbilitiesUntilEndOfTurn()
                && !computeStaticBonus(gameData, permanent).losesAllAbilities()) {
            if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(GameQueryService::isOpponentAbilityRestriction)) {
                return true;
            }
        }
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(GameQueryService::isOpponentAbilityRestriction);
    }

    private static boolean isOpponentAbilityRestriction(CardEffect effect) {
        return effect instanceof TargetingRestrictionEffect r
                && r.kind() == TargetingSourceKind.ABILITIES
                && r.mode() == TargetColorMode.ANY;
    }

    /**
     * Returns {@code true} if the permanent has been granted hexproof (opponents' spells and
     * abilities can't target it), e.g. by Asceticism. Only checks effects granted by other
     * permanents, matching the historical behavior of this shroud/hexproof-like marker.
     */
    public boolean cantBeTargetedBySpellsOrAbilities(GameData gameData, Permanent permanent) {
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(e -> e instanceof TargetingRestrictionEffect r
                        && r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                        && r.mode() == TargetColorMode.ANY);
    }

    /**
     * Returns {@code true} if the permanent cannot have -1/-1 counters placed on it,
     * from effects granted by other permanents (e.g. Melira, Sylvok Outcast).
     */
    public boolean cantHaveMinusOneMinusOneCounters(GameData gameData, Permanent permanent) {
        return hasGrantedEffect(gameData, permanent, CantHaveMinusOneMinusOneCountersEffect.class);
    }

    /**
     * Returns {@code true} if the player cannot get poison counters,
     * because they control a permanent with {@link PlayerCantGetPoisonCountersEffect}.
     */
    public boolean canPlayerGetPoisonCounters(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return true;
        for (Permanent p : battlefield) {
            if (p.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(PlayerCantGetPoisonCountersEffect.class::isInstance)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the creature cannot be blocked. Checks the permanent's
     * transient flag, its own static effects, attached auras/equipment, and granted effects.
     */
    public boolean hasCantBeBlocked(GameData gameData, Permanent creature) {
        if (creature.isCantBeBlocked()) return true;
        if (creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof BlockabilityRestrictionEffect r && r.cantBeBlocked())) return true;
        if (hasAuraWithEffect(gameData, creature, CantBeBlockedEffect.class)) return true;
        return hasGrantedEffect(gameData, creature, CantBeBlockedEffect.class);
    }

    /**
     * True if any attacking creature is attacking the given player directly or one of the planeswalkers
     * they control (i.e. the player "has been attacked" this combat). Defiant Stand, Kongming's Contraptions.
     */
    public boolean isPlayerBeingAttacked(GameData gameData, UUID playerId) {
        List<Permanent> playerBattlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (!perm.isAttacking()) continue;
                UUID target = perm.getAttackTarget();
                if (target == null) continue;
                if (target.equals(playerId)) return true;
                if (playerBattlefield.stream().anyMatch(p -> p.getId().equals(target))) return true;
            }
        }
        return false;
    }

    /** True if the given attacker is the only creature its controller declared as an attacker (CR 509.1). */
    private boolean isAttackingAlone(GameData gameData, Permanent attacker) {
        UUID controllerId = findPermanentController(gameData, attacker.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream().filter(Permanent::isAttacking).count() == 1;
    }

    // --- Stats calculation ---

    /**
     * Returns the permanent's effective power, including its base/modified power
     * plus any static bonuses from other permanents on the battlefield.
     */
    public int getEffectivePower(GameData gameData, Permanent permanent) {
        return getEffectivePower(permanent, computeStaticBonus(gameData, permanent));
    }

    /**
     * Returns the permanent's effective power using a pre-computed static bonus.
     * {@code bonus.basePTOverridden()} carries the CR 613 layer 7a/7b result (the object's CDA
     * overridden by the timestamp-resolved base-P/T setter winner from the layered pass) — it
     * takes precedence over every legacy {@code Permanent} base field; modifiers (7c) and the
     * bonus sum apply on top. {@code bonus.ptSwitched()} carries the layer-7d switch parity:
     * a switch swaps the values calculated through 7c (CR 613.4d), so a switched permanent's
     * power is the fully-resolved pre-switch toughness — 7b setters and 7c boosts resolving
     * after the switch still apply before the swap, because layers order them, not timestamps.
     */
    public int getEffectivePower(Permanent permanent, StaticBonus bonus) {
        return bonus.ptSwitched()
                ? preSwitchToughness(permanent, bonus)
                : preSwitchPower(permanent, bonus);
    }

    /**
     * Returns the permanent's effective toughness, including its base/modified toughness
     * plus any static bonuses from other permanents on the battlefield.
     */
    public int getEffectiveToughness(GameData gameData, Permanent permanent) {
        return getEffectiveToughness(permanent, computeStaticBonus(gameData, permanent));
    }

    /**
     * Returns the permanent's effective toughness using a pre-computed static bonus.
     * See {@link #getEffectivePower(Permanent, StaticBonus)} — the layered 7a/7b base wins
     * and the layer-7d switch parity swaps the finished values.
     */
    public int getEffectiveToughness(Permanent permanent, StaticBonus bonus) {
        return bonus.ptSwitched()
                ? preSwitchPower(permanent, bonus)
                : preSwitchToughness(permanent, bonus);
    }

    /** The permanent's power through layer 7c (base + modifiers + static bonuses), before 7d. */
    private int preSwitchPower(Permanent permanent, StaticBonus bonus) {
        if (bonus.basePTOverridden()) {
            return bonus.basePowerOverride() + permanent.getPowerModifiers() + bonus.power();
        }
        return permanent.getEffectivePower() + bonus.power();
    }

    /** The permanent's toughness through layer 7c (base + modifiers + static bonuses), before 7d. */
    private int preSwitchToughness(Permanent permanent, StaticBonus bonus) {
        if (bonus.basePTOverridden()) {
            return bonus.baseToughnessOverride() + permanent.getToughnessModifiers() + bonus.toughness();
        }
        return permanent.getEffectiveToughness() + bonus.toughness();
    }

    /**
     * Returns the amount of combat damage this creature assigns.
     * Normally equal to effective power, but some effects cause a creature to assign
     * damage equal to its toughness instead:
     * <ul>
     *   <li>Equipment/aura-scoped (e.g. Bark of Doran): only when toughness &gt; power.</li>
     *   <li>Controller-scoped (e.g. Belligerent Brontodon): always uses toughness.</li>
     * </ul>
     */
    public int getEffectiveCombatDamage(GameData gameData, Permanent creature) {
        // Multiple layered reads for one answer — share a single pass across them.
        return withQueryScope(gameData, () -> {
            int power = getEffectivePower(gameData, creature);
            int toughness = getEffectiveToughness(gameData, creature);

            // Global-scoped: every creature uses toughness (e.g. Doran, the Siege Tower)
            if (hasGlobalToughnessAssignEffect(gameData)) {
                return Math.max(0, toughness);
            }

            // Controller-scoped: always use toughness (e.g. Belligerent Brontodon)
            if (hasControllerToughnessAssignEffect(gameData, creature)) {
                return Math.max(0, toughness);
            }

            // Equipment/aura-scoped: use toughness only when toughness > power
            if (toughness > power && hasAuraWithEffect(gameData, creature, AssignCombatDamageWithToughnessEffect.class)) {
                return Math.max(0, toughness);
            }

            // CR 510.1a: a creature assigns combat damage equal to its power. A creature with
            // 0 or negative power assigns 0 combat damage.
            return Math.max(0, power);
        });
    }

    /**
     * Returns the amount of damage a creature deals for non-combat effects that deal damage
     * equal to the creature's power (fight, bite, Arc-Lightning-style, Hunters, Berserker,
     * planeswalker power-damage, etc.). Equivalent to {@code Math.max(0, getEffectivePower(...))}.
     * <p>
     * A creature with 0 or negative power deals 0 damage.
     * <p>
     * Do NOT use this for combat damage assignment — use {@link #getEffectiveCombatDamage} instead,
     * which also handles Belligerent-Brontodon / Bark-of-Doran "assign damage equal to toughness"
     * effects that apply only in combat.
     */
    public int getPowerBasedDamage(GameData gameData, Permanent creature) {
        return Math.max(0, getEffectivePower(gameData, creature));
    }

    /**
     * Returns {@code true} if the creature's controller has a permanent on the battlefield
     * with an {@link AssignCombatDamageWithToughnessEffect} whose scope covers this creature
     * ({@link GrantScope#OWN_CREATURES} or {@link GrantScope#ALL_OWN_CREATURES}).
     */
    /**
     * Returns {@code true} if any player controls a permanent on the battlefield with an
     * {@link AssignCombatDamageWithToughnessEffect} scoped to {@link GrantScope#ALL_CREATURES}
     * (e.g. Doran, the Siege Tower — "each creature assigns combat damage equal to its toughness").
     */
    private boolean hasGlobalToughnessAssignEffect(GameData gameData) {
        for (List<Permanent> bf : gameData.playerBattlefields.values()) {
            for (Permanent p : bf) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AssignCombatDamageWithToughnessEffect acdt
                            && acdt.scope() == GrantScope.ALL_CREATURES) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasControllerToughnessAssignEffect(GameData gameData, Permanent creature) {
        UUID controllerId = findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;

        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf == null) return false;

        for (Permanent p : bf) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AssignCombatDamageWithToughnessEffect acdt) {
                    GrantScope scope = acdt.scope();
                    if (scope == GrantScope.ALL_OWN_CREATURES) {
                        return true;
                    }
                    if (scope == GrantScope.OWN_CREATURES && !p.getId().equals(creature.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Computes the aggregate static bonus for a permanent from all other permanents on the
     * battlefield, emblems, and self-referencing characteristic-defining abilities. This includes
     * power/toughness modifiers, granted keywords, protection colors, animation state, granted
     * abilities/effects/colors/subtypes, and color/subtype overriding flags.
     *
     * <p>Returns {@link StaticBonus#NONE} as an early-out when no bonuses apply to a non-creature
     * permanent.
     */
    /**
     * Runs a batch of read-only queries under one shared layered pass: the board fingerprint
     * is checked once and every {@link #computeStaticBonus} call inside the scope hits the
     * pass-level bonus memo instead of re-assembling per query. Reuses an already-active pass
     * when nested.
     *
     * <p>The queries must not mutate game state — the memoized bonuses would go stale. Same
     * contract the nested-pass memo already relies on.
     */
    public <T> T withQueryScope(GameData gameData, Supplier<T> queries) {
        if (layerSystemService.activePass(gameData) != null) {
            return queries.get();
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            return queries.get();
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    public StaticBonus computeStaticBonus(GameData gameData, Permanent target) {
        // One layered pass per external query: the layer-4 board state is computed once and
        // shared (via the thread-local pass) with every nested computeStaticBonus call made by
        // handlers while assembling bonuses, together with a per-pass bonus memo.
        LayerSystemService.Pass active = layerSystemService.activePass(gameData);
        if (active != null) {
            // The memo is only valid once the layered board is finished: nested calls made by
            // handlers WHILE the layer 5/6 passes are still applying see partial states and
            // must not cache their answers.
            if (!active.isBoardReady()) {
                return assembleStaticBonus(gameData, active.board(), target);
            }
            StaticBonus memoized = active.bonusMemo().get(target.getId());
            if (memoized != null) {
                return memoized;
            }
            StaticBonus bonus = assembleStaticBonus(gameData, active.board(), target);
            active.bonusMemo().put(target.getId(), bonus);
            return bonus;
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            return assembleStaticBonus(gameData, pass.board(), target);
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /**
     * A {@link StaticBonus} plus its per-source display attribution: one {@link ModifierLine}
     * per contributing source (7c boosts and unmanaged keyword grants diffed during assembly,
     * layer-6/7b/7d contributions read from the board's provenance). Display-only — computed
     * by the view-building path, never consulted by rules code.
     */
    public record ExplainedBonus(StaticBonus bonus, List<ModifierLine> lines) {
    }

    /** {@link #computeStaticBonus} plus the per-source attribution lines for the client's
     *  hover breakdown. Runs its own assembly (bypassing the per-pass bonus memo) so the
     *  accumulator diffs can be observed; the returned bonus is identical to
     *  {@code computeStaticBonus}'s. */
    public ExplainedBonus explainStaticBonus(GameData gameData, Permanent target) {
        LayerSystemService.Pass active = layerSystemService.activePass(gameData);
        if (active != null) {
            return explainAgainstBoard(gameData, active.board(), target);
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            return explainAgainstBoard(gameData, pass.board(), target);
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    private ExplainedBonus explainAgainstBoard(GameData gameData, LayerSystemService.LayeredBoardState board, Permanent target) {
        List<ModifierLine> lines = new ArrayList<>();
        StaticBonus bonus = assembleStaticBonus(gameData, board, target, lines);
        // Board-recorded lines (layer 6 keyword grants/removals, 7b base setters in resolved
        // order, 7d switches) follow the assembly lines: base lines must fold AFTER the 7a CDA
        // line the assembly may have emitted, mirroring the merge in assembleStaticBonus.
        List<ModifierLine> recorded = board.provenance().get(target.getId());
        if (recorded != null) {
            lines.addAll(recorded);
        }
        if (bonus == StaticBonus.NONE) {
            return new ExplainedBonus(bonus, List.of());
        }
        return new ExplainedBonus(bonus, mergeModifierLines(lines));
    }

    /** Merges the additive/keyword lines of one source into a single display line; base-setting
     *  and switch lines keep their identity and relative order (folding is order-sensitive). */
    private static List<ModifierLine> mergeModifierLines(List<ModifierLine> lines) {
        Map<String, ModifierLine> merged = new LinkedHashMap<>();
        List<ModifierLine> orderSensitive = new ArrayList<>();
        for (ModifierLine line : lines) {
            if (line.basePower() != null || line.baseToughness() != null || line.switchesPt()) {
                orderSensitive.add(line);
                continue;
            }
            merged.merge(line.source(), line, (a, b) -> {
                Set<Keyword> gained = new HashSet<>(a.gainedKeywords());
                gained.addAll(b.gainedKeywords());
                Set<Keyword> removed = new HashSet<>(a.removedKeywords());
                removed.addAll(b.removedKeywords());
                return new ModifierLine(a.source(), a.power() + b.power(), a.toughness() + b.toughness(),
                        null, null, gained, removed, a.losesAllAbilities() || b.losesAllAbilities(), false);
            });
        }
        List<ModifierLine> result = new ArrayList<>(merged.values());
        result.addAll(orderSensitive);
        return result;
    }

    /**
     * The explain diff baseline: the accumulator's display-relevant state before one source's
     * handlers ran. {@link #diff} turns the delta into that source's attribution line (base
     * overrides are deliberately NOT diffed here — every 7b setter is already recorded with
     * attribution by the layered pass; only the self-CDA section diffs the base).
     */
    private record AccumulatorSnapshot(int power, int toughness, Set<Keyword> keywords,
                                       Set<Keyword> removedKeywords, boolean losesAllAbilities,
                                       boolean basePTOverridden, int basePowerOverride, int baseToughnessOverride) {
        static AccumulatorSnapshot of(StaticBonusAccumulator accumulator) {
            return new AccumulatorSnapshot(accumulator.getPower(), accumulator.getToughness(),
                    Set.copyOf(accumulator.getKeywords()), Set.copyOf(accumulator.getRemovedKeywords()),
                    accumulator.isLosesAllAbilities(), accumulator.isBasePTOverridden(),
                    accumulator.getBasePowerOverride(), accumulator.getBaseToughnessOverride());
        }

        ModifierLine diff(String source, StaticBonusAccumulator accumulator, boolean includeBase) {
            Set<Keyword> gained = new HashSet<>(accumulator.getKeywords());
            gained.removeAll(keywords);
            Set<Keyword> removed = new HashSet<>(accumulator.getRemovedKeywords());
            removed.removeAll(removedKeywords);
            boolean baseChanged = includeBase && accumulator.isBasePTOverridden()
                    && (!basePTOverridden
                        || accumulator.getBasePowerOverride() != basePowerOverride
                        || accumulator.getBaseToughnessOverride() != baseToughnessOverride);
            return new ModifierLine(source,
                    accumulator.getPower() - power, accumulator.getToughness() - toughness,
                    baseChanged ? accumulator.getBasePowerOverride() : null,
                    baseChanged ? accumulator.getBaseToughnessOverride() : null,
                    gained, removed,
                    !losesAllAbilities && accumulator.isLosesAllAbilities(), false);
        }
    }

    /** A static-effect source with the CR 613.7 ordering key used by {@link #assembleStaticBonus}. */
    private record StaticSource(Permanent permanent, boolean sameBattlefieldAsTarget, long timestamp, int position) {
    }

    /**
     * Legacy layer 7 accumulator assembly for one permanent, running against the layered board
     * state: sources apply in timestamp order (battlefield position for equal timestamps),
     * subtype/type filters are answered from the L4-corrected {@code CharacteristicState}s via
     * the active pass, and the sublayer-7b base P/T (every setter — static, floating one-shot,
     * animation, exchange, March MV — resolved in timestamp order by
     * {@code LayerSystemService.applyLayer7b}) is merged over the 7a CDA / intrinsic base.
     */
    private StaticBonus assembleStaticBonus(GameData gameData, LayerSystemService.LayeredBoardState board, Permanent target) {
        return assembleStaticBonus(gameData, board, target, null);
    }

    /** With a non-null {@code explain} list, additionally records one attribution line per
     *  contributing source by diffing the accumulator around each source's handlers. Only the
     *  view-building path passes a recorder; rules-code callers pay no diffing cost. */
    private StaticBonus assembleStaticBonus(GameData gameData, LayerSystemService.LayeredBoardState board, Permanent target, List<ModifierLine> explain) {
        boolean isNaturalCreature = hasCardType(target, CardType.CREATURE);
        StaticBonusAccumulator accumulator = new StaticBonusAccumulator();
        List<StaticSource> sources = new ArrayList<>();
        int position = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            boolean targetOnSameBattlefield = bf.contains(target);
            for (Permanent source : bf) {
                sources.add(new StaticSource(source, targetOnSameBattlefield, source.getTimestamp(), position++));
            }
        }
        sources.sort(Comparator.comparingLong(StaticSource::timestamp).thenComparingInt(StaticSource::position));
        for (StaticSource sourceSlot : sources) {
            Permanent source = sourceSlot.permanent();
            if (source == target) continue;
            // CR 613.8a(1)/CR 305.7: a source whose abilities are gone — "loses all abilities"
            // applied in layer 6, or a land whose type was set (removing its printed abilities
            // in layer 4) — contributes nothing in layer 7 either: a lose-all'd lord grants
            // neither its keyword (suppressed by the pass) nor its 7c boost. Managed layer-4
            // replays below still run: the pass already decided existence with correct
            // ordering, and a skipped instance simply recorded no contributions.
            CharacteristicState sourceState = board.states().get(source.getId());
            boolean sourceAbilitiesGone = sourceState != null
                    && (sourceState.isLosesAllAbilities() || sourceState.isPrintedAbilitiesRemoved());
            StaticEffectContext context = new StaticEffectContext(source, target, sourceSlot.sameBattlefieldAsTarget(), gameData);
            AccumulatorSnapshot beforeSource = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                // Purely type-changing effects were applied by the layer-4 pass (with filters
                // evaluated as of each effect's own application); replay its recorded decision
                // instead of re-running the handler against the finished states, which would
                // let self-referencing filters negate their own output (Bludgeon Brawl).
                if (board.isManagedL4(effect)) {
                    board.replayL4Contribution(effect, target.getId(), accumulator);
                    continue;
                }
                if (sourceAbilitiesGone) {
                    continue;
                }
                StaticEffectHandler handler = staticEffectRegistry.getHandler(effect);
                if (handler != null) {
                    // Layer 5/6 outputs of pass-managed instances were applied in timestamp
                    // order by the layered pass and merged in below; the handler still runs for
                    // its other-layer outputs (a lord's 7c boost) with those adds discarded.
                    // The handler sees the layer-3 view of the ability — the source's text
                    // changes applied — matching what the layered pass applied.
                    boolean layeredManaged = board.isManagedL56(effect);
                    if (layeredManaged) {
                        accumulator.setLayeredOutputsSuppressed(true);
                    }
                    try {
                        handler.apply(context,
                                TextChangeTransformer.transform(effect, source.getTextReplacements()),
                                accumulator);
                    } finally {
                        if (layeredManaged) {
                            accumulator.setLayeredOutputsSuppressed(false);
                        }
                    }
                }
            }
            if (beforeSource != null) {
                ModifierLine line = beforeSource.diff(source.getCard().getName(), accumulator, false);
                if (!line.isEmpty()) {
                    explain.add(line);
                }
            }
        }
        // Process emblem static effects
        for (Emblem emblem : gameData.emblems) {
            List<Permanent> ownerBf = gameData.playerBattlefields.get(emblem.controllerId());
            if (ownerBf == null || !ownerBf.contains(target)) continue;
            AccumulatorSnapshot beforeEmblem = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof GrantActivatedAbilityEffect grant
                        && grant.scope() == GrantScope.OWN_PERMANENTS
                        && (grant.filter() == null || predicateEvaluationService.matchesPermanentPredicate(gameData, target, grant.filter()))) {
                    accumulator.addActivatedAbility(grant.ability());
                } else if (effect instanceof StaticBoostEffect boost
                        && (boost.scope() == GrantScope.OWN_CREATURES || boost.scope() == GrantScope.ALL_OWN_CREATURES)
                        && isCreature(gameData, target)
                        && (boost.filter() == null || predicateEvaluationService.matchesPermanentPredicate(gameData, target, boost.filter()))) {
                    accumulator.addPower(boost.powerBoost());
                    accumulator.addToughness(boost.toughnessBoost());
                    accumulator.addKeywords(boost.grantedKeywords());
                } else if (effect instanceof GrantKeywordEffect grant
                        && grant.scope() == GrantScope.OWN_PERMANENTS
                        // Evaluate the filter with a null GameData so type predicates read the
                        // permanent's printed/granted types directly instead of re-entering
                        // computeStaticBonus for this same target (which would recurse forever).
                        && (grant.filter() == null || predicateEvaluationService.matchesPermanentPredicate(null, target, grant.filter()))) {
                    accumulator.addKeywords(grant.keywords());
                }
            }
            if (beforeEmblem != null) {
                String emblemName = emblem.sourceCard() != null ? emblem.sourceCard().getName() : "Emblem";
                ModifierLine line = beforeEmblem.diff(emblemName, accumulator, false);
                if (!line.isEmpty()) {
                    explain.add(line);
                }
            }
        }

        // Indefinite target buffs (Riding the Dilu Horse): a resolved spell recorded a PERMANENT
        // floating BuffTargetCreatureIndefinitelyEffect on this permanent. It has no static-slot
        // source, so it is read here rather than through a handler — the additive +P/+Y (7c) and
        // granted keywords (layer 6) apply for as long as the permanent exists; multiple copies
        // stack additively.
        AccumulatorSnapshot beforeIndefinite = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (floating.effect() instanceof BuffTargetCreatureIndefinitelyEffect buff
                        && target.getId().equals(floating.affectedPermanentId())) {
                    accumulator.addPower(buff.powerBoost());
                    accumulator.addToughness(buff.toughnessBoost());
                    accumulator.addKeywords(buff.keywords());
                }
            }
        }
        if (beforeIndefinite != null) {
            ModifierLine line = beforeIndefinite.diff("Indefinite buff", accumulator, false);
            if (!line.isEmpty()) {
                explain.add(line);
            }
        }

        // Handle characteristic-defining abilities (self-referencing static effects like */* P/T)
        CharacteristicState state = board.states().get(target.getId());
        AccumulatorSnapshot beforeSelf = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            StaticEffectHandler selfHandler = staticEffectRegistry.getSelfHandler(effect);
            if (selfHandler != null) {
                // CR 613.4a: an object whose own static abilities were removed in layer 6
                // ("loses all abilities") contributes no characteristic-defining P/T in 7a —
                // Maro under Merfolk Trickster is 0/0. The removal is NOT retroactive on
                // layers 2-5: type/color contributions the removed abilities already made in
                // earlier layers stay applied (CR 613 layers apply in order; see
                // agent-docs/LAYER_SYSTEM.md §3).
                if (effect instanceof SetPowerToughnessToAmountEffect
                        && state != null && state.isLosesAllAbilities()) {
                    continue;
                }
                boolean layeredManaged = board.isManagedL56(effect);
                if (layeredManaged) {
                    accumulator.setLayeredOutputsSuppressed(true);
                }
                try {
                    StaticEffectContext selfContext = new StaticEffectContext(target, target, true, gameData);
                    selfHandler.apply(selfContext,
                            TextChangeTransformer.transform(effect, target.getTextReplacements()),
                            accumulator);
                } finally {
                    if (layeredManaged) {
                        accumulator.setLayeredOutputsSuppressed(false);
                    }
                }
            }
        }
        if (beforeSelf != null) {
            // The 7a CDA base line — emitted before the board's 7b lines fold over it.
            ModifierLine line = beforeSelf.diff(target.getCard().getName(), accumulator, true);
            if (!line.isEmpty()) {
                explain.add(line);
            }
        }

        // Sublayer 7b: the timestamp-resolved base P/T from the layered pass overrides the base
        // decided so far (the 7a CDA applied just above, or the intrinsic base). CR 613.4:
        // 7a applies before 7b, so the latest-timestamp setter beats the CDA regardless of when
        // either arrived; a component no 7b entry set (power-only exchange) keeps the 7a/ladder
        // value. Precedence between setters lives entirely in LayerSystemService.applyLayer7b.
        LayerSystemService.BasePt basePt7b = board.basePt7b().get(target.getId());
        if (basePt7b != null) {
            int basePower = basePt7b.power() != null ? basePt7b.power()
                    : accumulator.isBasePTOverridden() ? accumulator.getBasePowerOverride()
                    : target.getBasePower();
            int baseToughness = basePt7b.toughness() != null ? basePt7b.toughness()
                    : accumulator.isBasePTOverridden() ? accumulator.getBaseToughnessOverride()
                    : target.getBaseToughness();
            accumulator.setBasePTOverride(basePower, baseToughness);
        }

        // "Becomes the basic land type" override (Tideshaper Mystic until end of turn, Orcish Farmer
        // until controller's next untap step): replaces the land's subtypes and mana ability (rule 305.7).
        if (target.getEffectiveLandTypeOverride() != null) {
            accumulator.addGrantedSubtype(target.getEffectiveLandTypeOverride());
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }

        // Sublayer 7d: the parity of the active floating switch effects, resolved by the
        // layered pass; the effective-P/T queries swap the finished 7a-7c values when set.
        boolean ptSwitched = board.switchedPt7d().contains(target.getId());

        boolean layeredTouched = state != null
                && (board.l56Touched().contains(target.getId()) || basePt7b != null || ptSwitched);
        boolean isSelfAnimated = target.isAnimatedUntilEndOfTurn() || target.isAnimatedUntilEndOfCombat() || target.isAnimatedUntilNextTurn() || target.getCounterCount(CounterType.AWAKENING) > 0 || accumulator.isSelfBecomeCreature();
        if (!isNaturalCreature
                && !accumulator.isAnimatedCreature()
                && !isSelfAnimated
                && !layeredTouched
                && accumulator.getKeywords().isEmpty()
                && accumulator.getGrantedActivatedAbilities().isEmpty()
                && accumulator.getProtectionColors().isEmpty()
                && accumulator.getGrantedColors().isEmpty()
                && accumulator.getGrantedSubtypes().isEmpty()
                && accumulator.getGrantedCardTypes().isEmpty()
                && accumulator.getGrantedSupertypes().isEmpty()) {
            return StaticBonus.NONE;
        }

        // Merge the layered layer 5/6 results (applied in CR 613.7 timestamp order by the pass)
        // with the unmanaged legacy accumulator outputs (emblems, conditional wrappers), which
        // stay additive outside timestamp order. bonus.keywords() becomes the COMPLETE keyword
        // set (printed included); bonus.removedKeywords() reports the seeded keywords the
        // layered pass removed, so consumers and views see removals and re-grants correctly.
        Set<Keyword> keywords = accumulator.getKeywords();
        Set<Keyword> removedKeywords = accumulator.getRemovedKeywords();
        Set<CardColor> grantedColors = accumulator.getGrantedColors();
        boolean colorOverriding = accumulator.isColorOverriding();
        Set<CardColor> protectionColors = accumulator.getProtectionColors();
        List<ActivatedAbility> grantedActivatedAbilities = accumulator.getGrantedActivatedAbilities();
        List<CardEffect> grantedEffects = accumulator.getGrantedEffects();
        boolean losesAllAbilities = accumulator.isLosesAllAbilities();
        if (state != null) {
            Set<Keyword> mergedKeywords = new HashSet<>(state.getKeywords());
            mergedKeywords.addAll(accumulator.getKeywords());
            mergedKeywords.removeAll(accumulator.getRemovedKeywords());
            keywords = mergedKeywords;
            Set<Keyword> mergedRemoved = new HashSet<>(accumulator.getRemovedKeywords());
            for (Keyword seeded : state.getSeededKeywords()) {
                if (!mergedKeywords.contains(seeded)) {
                    mergedRemoved.add(seeded);
                }
            }
            removedKeywords = mergedRemoved;
            Set<CardColor> mergedColors = EnumSet.noneOf(CardColor.class);
            mergedColors.addAll(state.getColors());
            if (state.isColorsOverridden()) {
                colorOverriding = true;
            } else {
                mergedColors.removeAll(state.getSeededColors());
            }
            mergedColors.addAll(accumulator.getGrantedColors());
            grantedColors = mergedColors;
            Set<CardColor> mergedProtection = EnumSet.noneOf(CardColor.class);
            mergedProtection.addAll(state.getProtectionColors());
            mergedProtection.addAll(accumulator.getProtectionColors());
            protectionColors = mergedProtection;
            List<ActivatedAbility> mergedAbilities = new ArrayList<>(state.getGrantedActivatedAbilities());
            mergedAbilities.addAll(accumulator.getGrantedActivatedAbilities());
            grantedActivatedAbilities = mergedAbilities;
            List<CardEffect> mergedEffects = new ArrayList<>(state.getGrantedStaticEffects());
            mergedEffects.addAll(accumulator.getGrantedEffects());
            grantedEffects = mergedEffects;
            losesAllAbilities = state.isLosesAllAbilities() || accumulator.isLosesAllAbilities();
        } else {
            // The target is not on a battlefield (AI hypothetical evaluation of an uncast
            // permanent), so the layered pass carries no state for it. bonus.keywords() must
            // still be the complete set — reconstruct it with the legacy Permanent.hasKeyword
            // semantics plus the accumulator's grants.
            Set<Keyword> mergedKeywords = new HashSet<>(accumulator.getKeywords());
            if (!target.isLosesAllAbilitiesUntilEndOfTurn()) {
                mergedKeywords.addAll(target.getCard().getKeywords());
                mergedKeywords.addAll(target.getGrantedKeywords());
                mergedKeywords.addAll(target.getPersistentGrantedKeywords());
                mergedKeywords.addAll(target.getUntilNextTurnKeywords());
                mergedKeywords.removeAll(target.getRemovedKeywords());
                if (target.isLosesAllCreatureTypesUntilEndOfTurn()) {
                    mergedKeywords.remove(Keyword.CHANGELING);
                }
            }
            mergedKeywords.removeAll(accumulator.getRemovedKeywords());
            keywords = mergedKeywords;
        }

        return new StaticBonus(accumulator.getPower(), accumulator.getToughness(), keywords,
                protectionColors, accumulator.isAnimatedCreature() || isSelfAnimated,
                grantedActivatedAbilities, grantedEffects, grantedColors,
                accumulator.getGrantedSubtypes(), accumulator.getGrantedCardTypes(),
                accumulator.getGrantedSupertypes(), colorOverriding,
                accumulator.isSubtypeOverriding(), accumulator.isLandSubtypeOverriding(),
                removedKeywords, accumulator.isBasePTOverridden(),
                accumulator.getBasePowerOverride(), accumulator.getBaseToughnessOverride(),
                losesAllAbilities, ptSwitched);
    }

    // --- CR 614.12 replacement-effect lookahead ---

    /**
     * CR 614.12 lookahead: determines whether a permanent <em>about to enter the battlefield</em>
     * would have the given subtype once all static effects are applied. This considers:
     * <ol>
     *   <li>The permanent's natural subtypes (from its card).</li>
     *   <li>Transient and granted subtypes already on the permanent.</li>
     *   <li>The Changeling keyword (natural or granted by static effects).</li>
     *   <li>Static effects from permanents already on the battlefield (e.g., Xenograft,
     *       Conspiracy, lord effects that grant subtypes).</li>
     * </ol>
     *
     * <p>Per CR 614.12, when multiple permanents enter simultaneously they <em>cannot</em>
     * see each other. The {@code simultaneouslyEntered} parameter lists permanents that were
     * already placed on the battlefield as part of the same simultaneous batch and must be
     * <em>excluded</em> from the lookahead.
     *
     * <p>Implementation: temporarily adds the entering permanent to the controller's battlefield
     * (and removes any {@code simultaneouslyEntered} permanents), runs {@link #computeStaticBonus},
     * then restores the original state.
     *
     * @param gameData               current game state
     * @param entering               the permanent about to enter the battlefield
     * @param controllerId           the controller under whose control it will enter
     * @param simultaneouslyEntered  permanents already on the battlefield from this simultaneous
     *                               batch that should be excluded from the lookahead; may be empty
     * @param subtype                the subtype to check for
     * @return {@code true} if the permanent would have the subtype on the battlefield
     */
    public boolean permanentWouldHaveSubtype(GameData gameData, Permanent entering, UUID controllerId,
                                              List<Permanent> simultaneouslyEntered, CardSubtype subtype) {
        // Quick path: check natural/transient/granted subtypes
        if (entering.getCard().getSubtypes().contains(subtype)) return true;
        if (entering.getTransientSubtypes().contains(subtype)) return true;
        if (entering.getGrantedSubtypes().contains(subtype)) return true;

        // Quick path: Changeling means all creature subtypes
        if (isCreatureSubtype(subtype) && entering.getCard().getKeywords().contains(Keyword.CHANGELING)) return true;

        // Full lookahead: temporarily add entering permanent and remove simultaneously-entered
        // permanents, compute static bonus, then restore original state.
        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        bf.add(entering);
        bf.removeAll(simultaneouslyEntered);
        try {
            StaticBonus bonus = computeStaticBonus(gameData, entering);
            if (bonus.grantedSubtypes().contains(subtype)) return true;
            // A static effect might grant Changeling
            return isCreatureSubtype(subtype) && bonus.keywords().contains(Keyword.CHANGELING);
        } finally {
            bf.remove(entering);
            bf.addAll(simultaneouslyEntered);
        }
    }

    // --- Protection & evasion ---

    /**
     * Returns {@code true} if the target permanent has protection from the given color.
     * Checks the permanent's own {@link ProtectionGrantingEffect}, static bonuses from
     * other permanents, and the permanent's chosen color (e.g. from a "choose a color" effect).
     */
    public boolean hasProtectionFrom(GameData gameData, Permanent target, CardColor sourceColor) {
        if (sourceColor == null) return false;
        StaticBonus bonus = computeStaticBonus(gameData, target);
        if (bonus == StaticBonus.NONE) {
            // No continuous effect touched the permanent: its own printed protection stands.
            for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ProtectionGrantingEffect protection
                        && protection.protectionScope() == null
                        && protection.protectionFromColors().contains(sourceColor)) {
                    return true;
                }
            }
        }
        // Layered layer-6 protection state: own printed protection (removable by a
        // later-timestamp "loses all abilities") plus protection grants.
        if (bonus.protectionColors().contains(sourceColor)) {
            return true;
        }
        // Protection granted by another permanent's static effect (e.g. Favor of the Mighty
        // via GrantEffectEffect(ProtectionFromColorsEffect, ...)).
        for (CardEffect effect : bonus.grantedEffects()) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && protection.protectionFromColors().contains(sourceColor)) {
                return true;
            }
        }
        if (target.getChosenColor() != null && target.getChosenColor() == sourceColor) {
            return true;
        }
        if (target.getProtectionFromColorsUntilEndOfTurn().contains(sourceColor)) {
            return true;
        }
        return false;
    }

    /**
     * Prismatic Ward: returns {@code true} if {@code creature} is enchanted by an Aura carrying
     * {@link PreventColorDamageToEnchantedCreatureEffect} whose chosen colour is among the damage
     * source's colours. Only prevents damage (not blocking/targeting/enchanting), and only while
     * damage is currently preventable (respects Leyline of Punishment, etc.).
     */
    public boolean isColorDamageToEnchantedCreaturePrevented(GameData gameData, Permanent creature, Set<CardColor> sourceColors) {
        if (creature == null || sourceColors == null || sourceColors.isEmpty()) return false;
        if (!isDamagePreventable(gameData)) return false;
        return gameData.anyPermanentMatches(aura ->
                aura.isAttached() && creature.getId().equals(aura.getAttachedTo())
                        && aura.getChosenColor() != null && sourceColors.contains(aura.getChosenColor())
                        && aura.getCard().getEffects(EffectSlot.STATIC).stream()
                                .anyMatch(PreventColorDamageToEnchantedCreatureEffect.class::isInstance));
    }

    /**
     * Returns {@code true} if the given permanent is a creature with the greatest mana value
     * among all creatures on the battlefield (across every player's battlefield). Ties allowed.
     * Used by Favor of the Mighty.
     */
    public boolean hasGreatestManaValueAmongAllCreatures(GameData gameData, Permanent permanent) {
        if (gameData == null || !isCreature(gameData, permanent)) {
            return false;
        }
        int greatest = -1;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (isCreature(gameData, candidate)) {
                    greatest = Math.max(greatest, candidate.getCard().getManaValue());
                }
            }
        }
        return permanent.getCard().getManaValue() == greatest;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * permanent's card types. Accounts for artifact status (including granted) and creature
     * status (including animation).
     */
    public boolean hasProtectionFromSourceCardTypes(GameData gameData, Permanent target, Permanent source) {
        Set<CardType> protectedTypes = EnumSet.noneOf(CardType.class);
        protectedTypes.addAll(target.getProtectionFromCardTypes());
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionGrantingEffect protection) {
                protectedTypes.addAll(protection.protectionFromCardTypes());
            }
        }
        if (protectedTypes.isEmpty()) return false;
        if (protectedTypes.contains(CardType.ARTIFACT) && isArtifact(source)) return true;
        if (protectedTypes.contains(CardType.CREATURE) && isCreature(gameData, source)) return true;
        if (protectedTypes.contains(source.getCard().getType())) return true;
        for (CardType type : source.getCard().getAdditionalTypes()) {
            if (protectedTypes.contains(type)) return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * card's card types. This overload is used for spells on the stack (which are cards,
     * not permanents) and only checks the card's natural types.
     */
    public boolean hasProtectionFromSourceCardTypes(Permanent target, Card sourceCard) {
        Set<CardType> protectedTypes = EnumSet.noneOf(CardType.class);
        protectedTypes.addAll(target.getProtectionFromCardTypes());
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionGrantingEffect protection) {
                protectedTypes.addAll(protection.protectionFromCardTypes());
            }
        }
        if (protectedTypes.isEmpty()) return false;
        if (protectedTypes.contains(sourceCard.getType())) return true;
        for (CardType type : sourceCard.getAdditionalTypes()) {
            if (protectedTypes.contains(type)) return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * permanent's subtypes. Checks the permanent's own card subtypes, transient subtypes,
     * granted subtypes, and Changeling keyword (which counts as all creature subtypes).
     */
    public boolean hasProtectionFromSourceSubtypes(GameData gameData, Permanent target, Permanent source) {
        Set<CardSubtype> protectedSubtypes = EnumSet.noneOf(CardSubtype.class);
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionGrantingEffect protection) {
                protectedSubtypes.addAll(protection.protectionFromSubtypes());
            }
        }
        if (protectedSubtypes.isEmpty()) return false;
        for (CardSubtype subtype : source.getCard().getSubtypes()) {
            if (protectedSubtypes.contains(subtype)) return true;
        }
        for (CardSubtype subtype : source.getTransientSubtypes()) {
            if (protectedSubtypes.contains(subtype)) return true;
        }
        for (CardSubtype subtype : source.getGrantedSubtypes()) {
            if (protectedSubtypes.contains(subtype)) return true;
        }
        if (hasKeyword(gameData, source, Keyword.CHANGELING)
                && protectedSubtypes.stream().anyMatch(this::isCreatureSubtype)) {
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * card's subtypes. This overload is used for spells on the stack.
     */
    public boolean hasProtectionFromSourceSubtypes(Permanent target, Card sourceCard) {
        Set<CardSubtype> protectedSubtypes = EnumSet.noneOf(CardSubtype.class);
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionGrantingEffect protection) {
                protectedSubtypes.addAll(protection.protectionFromSubtypes());
            }
        }
        if (protectedSubtypes.isEmpty()) return false;
        for (CardSubtype subtype : sourceCard.getSubtypes()) {
            if (protectedSubtypes.contains(subtype)) return true;
        }
        if (sourceCard.getKeywords().contains(Keyword.CHANGELING)
                && protectedSubtypes.stream().anyMatch(this::isCreatureSubtype)) {
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from non-[subtype] creatures
     * and the source permanent is a creature that lacks that subtype.
     */
    public boolean hasProtectionFromNonSubtypeCreatures(GameData gameData, Permanent target, Permanent source) {
        Set<CardSubtype> protectedFrom = target.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn();
        if (protectedFrom.isEmpty()) return false;
        if (!isCreature(gameData, source)) return false;
        for (CardSubtype subtype : protectedFrom) {
            if (!permanentHasSubtype(source, subtype)
                    && !(isCreatureSubtype(subtype) && hasKeyword(gameData, source, Keyword.CHANGELING))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from non-[subtype] creatures
     * and the source card (on the stack) is a creature card that lacks that subtype.
     */
    public boolean hasProtectionFromNonSubtypeCreatures(Permanent target, Card sourceCard) {
        Set<CardSubtype> protectedFrom = target.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn();
        if (protectedFrom.isEmpty()) return false;
        if (sourceCard.getType() != CardType.CREATURE
                && !sourceCard.getAdditionalTypes().contains(CardType.CREATURE)) return false;
        for (CardSubtype subtype : protectedFrom) {
            if (!sourceCard.getSubtypes().contains(subtype)
                    && !(isCreatureSubtype(subtype) && sourceCard.getKeywords().contains(Keyword.CHANGELING))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from mana value N or greater
     * and the source's mana value meets that threshold (e.g. Mistmeadow Skulk).
     */
    public boolean hasProtectionFromSourceManaValue(Permanent target, Card sourceCard) {
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && protection.protectionFromManaValueAtLeast().isPresent()
                    && sourceCard.getManaValue() >= protection.protectionFromManaValueAtLeast().getAsInt()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from the source permanent,
     * checking color-based, card-type-based, subtype-based, and non-subtype-creature protection.
     */
    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Permanent source) {
        return hasProtectionFromSource(gameData, target, source, getEffectiveColors(gameData, source));
    }

    /**
     * Variant taking the source's precomputed effective colors, for sweeps that already
     * snapshotted them (see {@link BlockLegalityContext}).
     */
    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Permanent source,
                                           Set<CardColor> sourceColors) {
        // Layer-5 aware: protection applies if the source has ANY protected color.
        for (CardColor sourceColor : sourceColors) {
            if (hasProtectionFrom(gameData, target, sourceColor)) {
                return true;
            }
        }
        return hasProtectionFromSourceCardTypes(gameData, target, source)
                || hasProtectionFromSourceSubtypes(gameData, target, source)
                || hasProtectionFromNonSubtypeCreatures(gameData, target, source)
                || hasProtectionFromSourceManaValue(target, source.getCard());
    }

    /**
     * Returns {@code true} if the target permanent has protection from the source card
     * (a spell on the stack), checking color-based, card-type-based, subtype-based,
     * and non-subtype-creature protection.
     */
    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Card sourceCard) {
        return hasProtectionFrom(gameData, target, sourceCard.getColor())
                || hasProtectionFromSourceCardTypes(target, sourceCard)
                || hasProtectionFromSourceSubtypes(target, sourceCard)
                || hasProtectionFromNonSubtypeCreatures(target, sourceCard)
                || hasProtectionFromSourceManaValue(target, sourceCard);
    }

    /**
     * Returns {@code true} if the source of a stack entry has the given keyword. Uses the
     * explicit source permanent if provided; otherwise looks up the source from the entry's
     * {@code sourcePermanentId}.
     *
     * @param explicitSource an already-resolved source permanent, or {@code null} to look up
     *                       from the entry
     */
    public boolean sourceHasKeyword(GameData gameData, StackEntry entry, Permanent explicitSource, Keyword keyword) {
        Permanent source = explicitSource;
        if (source == null && entry.getSourcePermanentId() != null) {
            source = findPermanentById(gameData, entry.getSourcePermanentId());
        }
        if (source != null) {
            return hasKeyword(gameData, source, keyword);
        }
        // No permanent source: the stack entry itself is the source (e.g. an instant/sorcery
        // spell like Puncture Blast). Its printed keywords (wither, etc.) apply. CR 702.80.
        return entry.getSourcePermanentId() == null
                && entry.getCard() != null
                && entry.getCard().getKeywords().contains(keyword);
    }

    /**
     * Returns {@code true} if the permanent's damage to creatures is dealt as -1/-1 counters:
     * either infect (CR 702.90) or wither (CR 702.80). The two behave identically against
     * creatures; they differ only against players (infect gives poison, wither is normal damage).
     */
    public boolean dealsCounterDamageToCreatures(GameData gameData, Permanent permanent) {
        return allDamageDealtWithWither(gameData)
                || hasKeyword(gameData, permanent, Keyword.INFECT)
                || hasKeyword(gameData, permanent, Keyword.WITHER);
    }

    /**
     * Returns {@code true} when an {@link AllDamageDealtWithWitherEffect} is on any battlefield
     * (e.g. Everlasting Torment), making every damage source deal creature damage as -1/-1 counters.
     */
    private boolean allDamageDealtWithWither(GameData gameData) {
        return anyBattlefieldHasStaticEffect(gameData, AllDamageDealtWithWitherEffect.class);
    }

    /**
     * Stack-entry variant of {@link #dealsCounterDamageToCreatures}: whether the damage source
     * (explicit permanent or the entry's source permanent) deals creature damage as -1/-1 counters.
     */
    public boolean sourceDealsCounterDamageToCreatures(GameData gameData, StackEntry entry, Permanent explicitSource) {
        return allDamageDealtWithWither(gameData)
                || sourceHasKeyword(gameData, entry, explicitSource, Keyword.INFECT)
                || sourceHasKeyword(gameData, entry, explicitSource, Keyword.WITHER);
    }

    /**
     * Returns {@code true} if the given damage amount is lethal. Damage is lethal if it
     * meets or exceeds the effective toughness, or if the source has deathtouch and deals
     * at least 1 damage.
     */
    public boolean isLethalDamage(int damage, int effectiveToughness, boolean deathtouch) {
        return damage >= effectiveToughness || (damage >= 1 && deathtouch);
    }

    /**
     * Returns {@code true} if the target permanent cannot be targeted by spells of the
     * given color. Checks both the permanent's own static effects and effects granted by
     * other permanents.
     */
    public boolean cantBeTargetedBySpellColor(GameData gameData, Permanent target, CardColor spellColor) {
        if (spellColor == null) {
            return false;
        }
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (isSpellColorRestriction(effect, spellColor)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isSpellColorRestriction(effect, spellColor)) {
                return true;
            }
        }

        // Check per-player turn-duration protection (Autumn's Veil style)
        if (isCreature(gameData, target)) {
            UUID controllerId = findPermanentController(gameData, target.getId());
            if (controllerId != null) {
                Set<CardColor> protectedColors = gameData.playerCreaturesCantBeTargetedByColorsThisTurn.get(controllerId);
                if (protectedColors != null && protectedColors.contains(spellColor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns {@code true} if the target permanent can't be the target of any spell (regardless of
     * color or controller) — Dense Foliage's "Creatures can't be the targets of spells". Abilities are
     * unaffected. Scans both the permanent's own STATIC effects and effects granted to it.
     */
    public boolean cantBeTargetedByAnySpell(GameData gameData, Permanent target) {
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (isAnySpellRestriction(effect)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isAnySpellRestriction(effect)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnySpellRestriction(CardEffect effect) {
        return effect instanceof TargetingRestrictionEffect r
                && r.kind() == TargetingSourceKind.SPELLS
                && r.mode() == TargetColorMode.ANY;
    }

    /**
     * Returns {@code true} if the permanent can't be enchanted by other Auras (e.g. Anti-Magic Aura),
     * from its own static effects or from effects granted by other permanents.
     */
    public boolean cantBeEnchantedByOtherAuras(GameData gameData, Permanent target) {
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeEnchantedByOtherAurasEffect) {
                return true;
            }
        }
        return hasGrantedEffect(gameData, target, CantBeEnchantedByOtherAurasEffect.class);
    }

    /**
     * Matches the "can't be the target of spells of [color]" restriction (Karplusan Strider) — spells
     * only, no controller gating — for the given spell color.
     */
    private static boolean isSpellColorRestriction(CardEffect effect, CardColor spellColor) {
        return effect instanceof TargetingRestrictionEffect r
                && r.kind() == TargetingSourceKind.SPELLS
                && r.mode() == TargetColorMode.BLOCKED_COLORS
                && r.colors().contains(spellColor);
    }

    /**
     * Returns {@code true} if the target permanent has "hexproof from [color]" that matches
     * the given source color. Unlike full hexproof, this only blocks targeting from sources
     * of the specified color(s). Only blocks opponent-controlled sources.
     */
    public boolean hasHexproofFromColor(GameData gameData, Permanent target, CardColor sourceColor) {
        if (sourceColor == null) {
            return false;
        }
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (isHexproofFromColorRestriction(effect, sourceColor)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isHexproofFromColorRestriction(effect, sourceColor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHexproofFromColorRestriction(CardEffect effect, CardColor sourceColor) {
        return effect instanceof TargetingRestrictionEffect r
                && r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                && r.mode() == TargetColorMode.BLOCKED_COLORS
                && r.colors().contains(sourceColor);
    }

    /**
     * Returns {@code true} if the target permanent cannot be targeted by spells or abilities
     * from the given source card, because the target has a static effect restricting targeting
     * to only sources of a specific color (e.g. Gaea's Revenge: "can't be the target of
     * nongreen spells or abilities from nongreen sources").
     */
    public boolean cantBeTargetedByNonColorSources(GameData gameData, Permanent target, Card sourceCard) {
        if (sourceCard == null) {
            return false;
        }
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (isNonColorSourceRestriction(effect, sourceCard)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isNonColorSourceRestriction(effect, sourceCard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Matches the "can't be the target of non-[color] sources" restriction (Gaea's Revenge): the
     * source is blocked unless it has one of the allowed colors.
     */
    private boolean isNonColorSourceRestriction(CardEffect effect, Card sourceCard) {
        return effect instanceof TargetingRestrictionEffect r
                && r.mode() == TargetColorMode.ALLOWED_COLORS_ONLY
                && r.colors().stream().noneMatch(color -> sourceHasColor(sourceCard, color));
    }

    private boolean sourceHasColor(Card card, CardColor color) {
        if (card.getColor() == color) {
            return true;
        }
        return card.getColors().contains(color);
    }

    /**
     * Returns {@code true} if the given spell cannot be countered by the given counter source,
     * because the spell's controller has turn-duration protection from countering by spells
     * of the source's color (e.g. Autumn's Veil). Only applies when the counter source is
     * a spell (not a triggered or activated ability).
     */
    public boolean isProtectedFromCounterBySpellColor(GameData gameData, UUID spellControllerId, StackEntry counterSource) {
        Set<CardColor> protectedColors = gameData.playerSpellsCantBeCounteredByColorsThisTurn.get(spellControllerId);
        if (protectedColors == null || protectedColors.isEmpty()) {
            return false;
        }
        // Only protects against spells, not abilities
        StackEntryType sourceType = counterSource.getEntryType();
        if (sourceType == StackEntryType.TRIGGERED_ABILITY || sourceType == StackEntryType.ACTIVATED_ABILITY) {
            return false;
        }
        return protectedColors.contains(counterSource.getCard().getColor());
    }

    /**
     * Returns {@code true} if the given spell cannot be countered by the given source card,
     * because the spell's controller has turn-duration protection from countering by spells
     * of that color (e.g. Autumn's Veil). Only applies when the source is an instant or sorcery.
     */
    public boolean isProtectedFromCounterBySourceCard(GameData gameData, UUID spellControllerId, Card sourceCard) {
        Set<CardColor> protectedColors = gameData.playerSpellsCantBeCounteredByColorsThisTurn.get(spellControllerId);
        if (protectedColors == null || protectedColors.isEmpty()) {
            return false;
        }
        // Only protects against spells, not abilities
        CardType sourceType = sourceCard.getType();
        if (sourceType != CardType.INSTANT && sourceType != CardType.SORCERY) {
            return false;
        }
        return protectedColors.contains(sourceCard.getColor());
    }

    /**
     * Returns {@code true} if the player has shroud (cannot be the target of spells or
     * abilities), granted by a permanent they control with {@link GrantControllerShroudEffect}.
     */
    public boolean playerHasShroud(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, GrantControllerShroudEffect.class);
    }

    /**
     * Returns {@code true} if the player has hexproof (cannot be the target of spells or
     * abilities opponents control), granted by a permanent they control with
     * {@link GrantControllerHexproofEffect}.
     */
    public boolean playerHasHexproof(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, GrantControllerHexproofEffect.class);
    }

    /**
     * Returns {@code true} if the player has protection from the given color until end of turn
     * (e.g. Faith's Shield fateful hour). Such a player can't be targeted by spells or abilities
     * of that color and can't be dealt damage by sources of that color.
     */
    public boolean playerHasProtectionFromColor(GameData gameData, UUID playerId, CardColor color) {
        if (color == null) {
            return false;
        }
        Set<CardColor> colors = gameData.playerProtectionFromColorsUntilEndOfTurn.get(playerId);
        return colors != null && colors.contains(color);
    }

    /**
     * Returns {@code true} if the player controls a permanent with a
     * {@link PlayerHasProtectionFromChosenNameEffect} static effect whose chosen card name equals
     * the given name (Runed Halo). Such a player can't be targeted, dealt damage, or enchanted by
     * anything with that name.
     */
    public boolean playerHasProtectionFromChosenName(GameData gameData, UUID playerId, String cardName) {
        if (cardName == null) {
            return false;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) {
            return false;
        }
        for (Permanent perm : bf) {
            if (cardName.equals(perm.getChosenName())
                    && perm.getCard().getEffects(EffectSlot.STATIC).stream()
                            .anyMatch(PlayerHasProtectionFromChosenNameEffect.class::isInstance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the player controls a permanent with
     * {@link AllowExtraLoyaltyActivationEffect}, allowing planeswalker loyalty abilities
     * to be activated twice per turn instead of once (Oath of Teferi).
     */
    public boolean hasExtraLoyaltyActivation(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, AllowExtraLoyaltyActivationEffect.class);
    }

    /**
     * Returns {@code true} if the given card cannot be countered, either because it has
     * its own "can't be countered" ability ({@link CantBeCounteredEffect}), because it was
     * individually made uncounterable while on the stack (e.g. Vexing Shusher), or because
     * a {@link CreatureSpellsCantBeCounteredEffect} on the battlefield protects creature spells.
     */
    public boolean isUncounterable(GameData gameData, Card card) {
        if (card.getEffects(EffectSlot.STATIC).stream().anyMatch(e -> e instanceof CantBeCounteredEffect)) {
            return true;
        }
        if (gameData.spellsMadeUncounterable.contains(card.getId())) {
            return true;
        }
        if (!hasCardType(card, CardType.CREATURE)) {
            return false;
        }
        return anyBattlefieldHasStaticEffect(gameData, CreatureSpellsCantBeCounteredEffect.class);
    }

    /**
     * Returns {@code true} if any permanent on the battlefield has a
     * {@link CreatureEnteringDontCauseTriggersEffect} static effect (e.g. Torpor Orb),
     * AND the entering card is a creature.
     */
    public boolean areCreatureETBTriggersSuppressed(GameData gameData, Card enteringCard) {
        if (!hasCardType(enteringCard, CardType.CREATURE)) {
            return false;
        }
        return anyBattlefieldHasStaticEffect(gameData, CreatureEnteringDontCauseTriggersEffect.class);
    }

    /**
     * Returns the number of extra times a triggered ability should fire when a creature
     * controlled by {@code controllerId} enters the battlefield — one per
     * {@link ETBDoubleTriggerEffect} whose predicate matches the entering creature
     * (e.g. Naban, Dean of Iteration with {@code CardSubtypePredicate(WIZARD)}).
     */
    public int countETBExtraTriggers(GameData gameData, UUID controllerId, Card enteringCreature) {
        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf == null) return 0;
        int count = 0;
        for (Permanent perm : bf) {
            for (CardEffect e : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (e instanceof ETBDoubleTriggerEffect etb
                        && predicateEvaluationService.matchesCardPredicate(enteringCreature, etb.predicate(), null)) {
                    count++;
                }
            }
        }
        return count;
    }

    // --- Aura & enchantment ---

    /**
     * Returns {@code true} if any permanent on the battlefield has an
     * {@link AnimateNoncreatureArtifactsEffect}, which turns all non-creature artifacts
     * into creatures.
     */
    public boolean hasAnimateArtifactEffect(GameData gameData) {
        return anyBattlefieldHasStaticEffect(gameData, AnimateNoncreatureArtifactsEffect.class);
    }

    /**
     * Returns {@code true} if the given land is animated into a creature by an
     * {@link AllLandsAreCreaturesEffect} on any battlefield. An effect with no required subtype
     * animates every land (Nature's Revolt); an effect with a required land subtype (Living Lands:
     * Forest) animates only lands carrying that subtype.
     */
    public boolean matchesAnimateLand(GameData gameData, Permanent permanent) {
        return gameData.anyPermanentMatches(source ->
                source.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof AllLandsAreCreaturesEffect animateLands
                                && (animateLands.requiredSubtype() == null
                                        || permanent.getCard().getSubtypes().contains(animateLands.requiredSubtype()))));
    }

    /**
     * Returns {@code true} if the given permanent has an aura or equipment attached to it
     * that carries a static effect of the given type. Also unwraps
     * {@link EnchantedPermanentConditionalEffect} wrappers: if the currently active
     * inner effect (based on the enchanted permanent predicate) matches, returns {@code true}.
     */
    public boolean hasAuraWithEffect(GameData gameData, Permanent creature, Class<? extends CardEffect> effectClass) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && p.getAttachedTo().equals(creature.getId())
                        && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> isActiveEffect(gameData, creature, e, effectClass)));
    }

    /**
     * Returns the total additional generic mana the controller must pay to declare this creature as an
     * attacker, summed over every {@link EnchantedCreatureCantAttackUnlessPaysEffect} aura attached to it
     * (e.g. Brainwash — {3}).
     */
    public int getEnchantedCreatureAttackTax(GameData gameData, Permanent creature) {
        int[] total = {0};
        gameData.forEachPermanent((playerId, aura) -> {
            if (!aura.isAttached() || !aura.getAttachedTo().equals(creature.getId())) {
                return;
            }
            for (CardEffect effect : aura.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof EnchantedCreatureCantAttackUnlessPaysEffect tax) {
                    total[0] += tax.amount();
                }
            }
        });
        return total[0];
    }

    private boolean isActiveEffect(GameData gameData, Permanent creature, CardEffect effect, Class<? extends CardEffect> effectClass) {
        if (effectClass.isInstance(effect)) return true;
        if (effect instanceof EnchantedPermanentConditionalEffect cond) {
            CardEffect activeEffect = predicateEvaluationService.matchesPermanentPredicate(gameData, creature, cond.filter())
                    ? cond.ifMatch()
                    : cond.ifNotMatch();
            return effectClass.isInstance(activeEffect);
        }
        return false;
    }

    /**
     * Checks whether a permanent has a given subtype without triggering {@code computeStaticBonus}
     * (which would cause infinite recursion). Checks base subtypes, transient subtypes,
     * granted subtypes, and the intrinsic Changeling keyword.
     */
    public static boolean permanentHasSubtype(Permanent permanent, CardSubtype subtype) {
        // "Loses all creature types" (e.g. Amoeboid Changeling): every creature subtype is treated as absent.
        // hasKeyword already suppresses the Changeling grant while this flag is set.
        if (permanent.isLosesAllCreatureTypesUntilEndOfTurn() && !NON_CREATURE_SUBTYPES.contains(subtype)) {
            return false;
        }
        return permanent.getCard().getSubtypes().contains(subtype)
                || permanent.getTransientSubtypes().contains(subtype)
                || permanent.getGrantedSubtypes().contains(subtype)
                || permanent.hasKeyword(Keyword.CHANGELING);
    }

    /**
     * Returns {@code true} if the two creatures share at least one creature type, accounting for
     * granted/transient/layer subtypes, "loses all creature types", and Changeling (which counts
     * as having every creature type). Backs the "creatures that share no creature types" targeting
     * restriction (Rivals' Duel).
     */
    public boolean shareCreatureType(GameData gameData, Permanent a, Permanent b) {
        boolean aChangeling = hasKeyword(gameData, a, Keyword.CHANGELING);
        boolean bChangeling = hasKeyword(gameData, b, Keyword.CHANGELING);
        Set<CardSubtype> aTypes = effectiveCreatureSubtypes(gameData, a);
        Set<CardSubtype> bTypes = effectiveCreatureSubtypes(gameData, b);
        // A Changeling has every creature type, so it shares a type with any creature that has
        // at least one creature type (or with another Changeling).
        if (aChangeling) {
            return bChangeling || !bTypes.isEmpty();
        }
        if (bChangeling) {
            return !aTypes.isEmpty();
        }
        return aTypes.stream().anyMatch(bTypes::contains);
    }

    /**
     * Returns {@code true} if the two permanents share at least one of the card types artifact,
     * creature, or land (Gauntlets of Chaos' "shares one of those types with it"). Uses each
     * permanent's card types.
     */
    public boolean sharesArtifactCreatureOrLandType(Permanent a, Permanent b) {
        Card aCard = a.getCard();
        Card bCard = b.getCard();
        return (aCard.hasType(CardType.ARTIFACT) && bCard.hasType(CardType.ARTIFACT))
                || (aCard.hasType(CardType.CREATURE) && bCard.hasType(CardType.CREATURE))
                || (aCard.hasType(CardType.LAND) && bCard.hasType(CardType.LAND));
    }

    /** Effective creature subtypes of a permanent (named types only; Changeling handled separately). */
    private Set<CardSubtype> effectiveCreatureSubtypes(GameData gameData, Permanent permanent) {
        if (permanent.isLosesAllCreatureTypesUntilEndOfTurn()) {
            return Set.of();
        }
        Set<CardSubtype> result = new HashSet<>();
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (!bonus.subtypeOverriding()) {
            addCreatureSubtypes(result, permanent.getCard().getSubtypes());
        }
        addCreatureSubtypes(result, permanent.getTransientSubtypes());
        addCreatureSubtypes(result, permanent.getGrantedSubtypes());
        addCreatureSubtypes(result, permanent.getUntilNextTurnSubtypes());
        addCreatureSubtypes(result, bonus.grantedSubtypes());
        return result;
    }

    private void addCreatureSubtypes(Set<CardSubtype> target, List<CardSubtype> subtypes) {
        for (CardSubtype subtype : subtypes) {
            if (isCreatureSubtype(subtype)) {
                target.add(subtype);
            }
        }
    }

    /**
     * Returns {@code true} if the given permanent has at least one Equipment attached to it.
     */
    public boolean isEquipped(GameData gameData, Permanent creature) {
        return gameData.anyPermanentMatches(p ->
                p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && p.isAttached() && p.getAttachedTo().equals(creature.getId()));
    }

    /**
     * Returns {@code true} if the given creature permanent can legally be declared as a blocker.
     */
    public boolean canBlock(GameData gameData, Permanent creature) {
        return isCreature(gameData, creature)
                && !creature.isTapped()
                && !creature.isCantBlockThisTurn()
                && creature.getCard().getEffects(EffectSlot.STATIC).stream().noneMatch(CantBlockEffect.class::isInstance)
                && !hasAuraWithEffect(gameData, creature, EnchantedCreatureCantAttackOrBlockEffect.class)
                && !hasAuraWithEffect(gameData, creature, CantBlockEffect.class)
                && !(creature.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(CantAttackOrBlockUnlessEquippedEffect.class::isInstance)
                        && !isEquipped(gameData, creature))
                && !isCantBlockUnlessConditionUnmet(gameData, creature)
                && !hasGlobalCantAttackOrBlockRestriction(gameData, creature);
    }

    /**
     * Returns {@code true} if a board-wide "creatures matching X can't attack or block" restriction
     * (e.g. Kulrath Knight, Light of Day) applies to the given creature, evaluating each restriction's
     * predicate relative to the source permanent's controller. The attack side is enforced in
     * {@code CombatAttackService}.
     */
    private boolean hasGlobalCantAttackOrBlockRestriction(GameData gameData, Permanent creature) {
        boolean[] restricted = {false};
        gameData.forEachPermanent((playerId, source) -> {
            if (restricted[0]) {
                return;
            }
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AttackOrBlockRestrictionEffect restriction
                        && restriction.globallyCantAttackOrBlock() != null) {
                    FilterContext context = FilterContext.of(gameData)
                            .withSourceControllerId(playerId)
                            .withSourceCardId(source.getOriginalCard().getId());
                    if (predicateEvaluationService.matchesPermanentPredicate(creature, restriction.globallyCantAttackOrBlock(), context)) {
                        restricted[0] = true;
                    }
                }
            }
        });
        return restricted[0];
    }

    /**
     * Returns {@code true} if the creature has a "can't attack or block unless …" restriction whose
     * condition is not met (block side, mirrors the attack side in {@code CombatAttackService}).
     */
    private boolean isCantBlockUnlessConditionUnmet(GameData gameData, Permanent creature) {
        UUID controllerId = null;
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof AttackOrBlockRestrictionEffect restriction
                    && restriction.cantAttackOrBlockUnless() != null) {
                if (controllerId == null) {
                    controllerId = findPermanentController(gameData, creature.getId());
                    if (controllerId == null) return false;
                }
                if (!conditionEvaluationService.isMet(gameData, restriction.cantAttackOrBlockUnless(),
                        ConditionContext.forPermanent(creature, controllerId))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Builds a {@link BlockLegalityContext} for one declare-blockers computation: collects the
     * board-wide block restrictions and defender land types once, then caches per-creature
     * facts as pairs are queried. Use one context for a whole blocker × attacker sweep and
     * build a new one after any game-state mutation.
     */
    public BlockLegalityContext createBlockLegalityContext(GameData gameData, List<Permanent> defenderBattlefield) {
        List<MatchingCreaturesCantBlockMatchingCreaturesEffect> globalRestrictions = new ArrayList<>();
        gameData.forEachPermanent((playerId, source) -> {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof MatchingCreaturesCantBlockMatchingCreaturesEffect restriction) {
                    globalRestrictions.add(restriction);
                }
            }
        });
        Set<CardSubtype> defenderCardSubtypes = EnumSet.noneOf(CardSubtype.class);
        if (defenderBattlefield != null) {
            for (Permanent p : defenderBattlefield) {
                defenderCardSubtypes.addAll(p.getCard().getSubtypes());
            }
        }
        return new BlockLegalityContext(gameData, defenderBattlefield, globalRestrictions, defenderCardSubtypes);
    }

    /**
     * Returns {@code true} if the given blocker can legally block the given attacker,
     * considering all evasion abilities, blocking restrictions, landwalk, and protection.
     * Builds a fresh single-use {@link BlockLegalityContext}; pairwise sweeps should build
     * one context via {@link #createBlockLegalityContext} and use the context overload.
     */
    public boolean canBlockAttacker(GameData gameData, Permanent blocker, Permanent attacker,
                                    List<Permanent> defenderBattlefield) {
        return canBlockAttacker(createBlockLegalityContext(gameData, defenderBattlefield), blocker, attacker);
    }

    /** Pairwise block legality against a shared context — the allocation-free fast path. */
    public boolean canBlockAttacker(BlockLegalityContext context, Permanent blocker, Permanent attacker) {
        return findBlockDenial(context, blocker, attacker) == null;
    }

    /**
     * Returns the reason a blocker cannot legally block the given attacker, or empty if the
     * block is legal. Builds a fresh single-use {@link BlockLegalityContext}.
     */
    public Optional<String> getBlockingIllegalityReason(GameData gameData, Permanent blocker,
                                                        Permanent attacker, List<Permanent> defenderBattlefield) {
        return getBlockingIllegalityReason(createBlockLegalityContext(gameData, defenderBattlefield), blocker, attacker);
    }

    /** Message form of {@link #canBlockAttacker(BlockLegalityContext, Permanent, Permanent)}. */
    public Optional<String> getBlockingIllegalityReason(BlockLegalityContext context, Permanent blocker, Permanent attacker) {
        BlockDenial denial = findBlockDenial(context, blocker, attacker);
        return denial == null ? Optional.empty() : Optional.of(formatBlockDenial(denial, blocker, attacker));
    }

    /**
     * The single source of truth for pairwise block legality: evasion keywords, blocking
     * restrictions, landwalk, and protection. Returns the failed rule, or {@code null} when
     * the block is legal. Creature-invariant facts come from the context caches so a
     * blocker × attacker sweep evaluates each side's board scans and layered-pass lookups
     * exactly once per creature; check order matches the pre-context implementation so the
     * surfaced message is unchanged when several rules fail at once.
     */
    private BlockDenial findBlockDenial(BlockLegalityContext context, Permanent blocker, Permanent attacker) {
        GameData gameData = context.gameData;
        BlockLegalityContext.AttackerFacts atk = context.attackerFacts.computeIfAbsent(
                attacker.getId(), id -> buildAttackerFacts(context, attacker));
        if (atk.unblockable()) {
            return BlockDenial.CANT_BE_BLOCKED;
        }
        BlockLegalityContext.BlockerFacts blk = context.blockerFacts.computeIfAbsent(
                blocker.getId(), id -> buildBlockerFacts(context, blocker));
        if (atk.flying() && !blk.flying() && !blk.reach()) {
            return BlockDenial.FLYING;
        }
        if (atk.horsemanship() && !blk.horsemanship()) {
            return BlockDenial.HORSEMANSHIP;
        }
        if (atk.fear() && !blk.artifact() && !blk.colors().contains(CardColor.BLACK)) {
            return BlockDenial.FEAR;
        }
        if (atk.intimidate() && !blk.artifact()
                && Collections.disjoint(blk.colors(), atk.colors())) {
            return BlockDenial.INTIMIDATE;
        }
        for (CanBlockOnlyIfAttackerMatchesPredicateEffect restriction : blk.attackerFilterRestrictions()) {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, restriction.attackerPredicate())) {
                return new BlockDenial(BlockDenial.Reason.BLOCKER_LIMITED_TO_ATTACKERS, restriction.allowedAttackersDescription());
            }
        }
        // Board-wide "creatures matching X can't block creatures matching Y" restrictions
        // (e.g. Boldwyr Intimidator: "Cowards can't block Warriors.").
        for (MatchingCreaturesCantBlockMatchingCreaturesEffect restriction : context.globalBlockRestrictions) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, restriction.attackerPredicate())) {
                return new BlockDenial(BlockDenial.Reason.GLOBAL_RESTRICTION, restriction.description());
            }
        }
        for (CardEffect effect : atk.pairRestrictionStatics()) {
            if (effect instanceof BlockabilityRestrictionEffect restriction) {
                if (restriction.blockableOnlyBy() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockableOnlyBy())) {
                    return new BlockDenial(BlockDenial.Reason.ATTACKER_LIMITED_TO_BLOCKERS, restriction.blockableOnlyByDescription());
                }
                if (restriction.cantBeBlockedByCreaturesMatching() != null
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.cantBeBlockedByCreaturesMatching())) {
                    return BlockDenial.CANT_BE_BLOCKED_BY_MATCHING;
                }
            }
        }
        for (CanBeBlockedOnlyByFilterEffect restriction : atk.auraGrantedRestrictions()) {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return new BlockDenial(BlockDenial.Reason.ATTACKER_LIMITED_TO_BLOCKERS, restriction.allowedBlockersDescription());
            }
        }
        for (CanBeBlockedOnlyByFilterEffect restriction : attacker.getBlockRestrictionsUntilEndOfTurn()) {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return new BlockDenial(BlockDenial.Reason.ATTACKER_LIMITED_TO_BLOCKERS, restriction.allowedBlockersDescription());
            }
        }
        if (atk.landwalkDenial() != null) {
            return atk.landwalkDenial();
        }
        if (blocker.isCantBlockThisTurn()) {
            return BlockDenial.CANT_BLOCK_THIS_TURN;
        }
        if (blk.cantBlock()) {
            return BlockDenial.CANT_BLOCK;
        }
        // Ironclaw Curse: can't block attackers whose power >= this creature's own toughness.
        if (blk.cantBlockPowerAtLeastOwnToughness()
                && getEffectivePower(gameData, attacker) >= getEffectiveToughness(gameData, blocker)) {
            return BlockDenial.CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS;
        }
        // Ironclaw Orcs: can't block attackers whose power >= a fixed threshold.
        if (blk.cantBlockPowerAtLeast() != null
                && getEffectivePower(gameData, attacker) >= blk.cantBlockPowerAtLeast()) {
            return BlockDenial.CANT_BLOCK_HIGH_POWER;
        }
        if (blocker.getCantBlockIds().contains(attacker.getId())) {
            return BlockDenial.CANT_BLOCK_THAT_ATTACKER;
        }
        if (hasProtectionFromSource(gameData, attacker, blocker, blk.colors())) {
            return BlockDenial.PROTECTION;
        }
        return null;
    }

    private BlockLegalityContext.AttackerFacts buildAttackerFacts(BlockLegalityContext context, Permanent attacker) {
        GameData gameData = context.gameData;
        boolean unblockable = hasCantBeBlocked(gameData, attacker);
        List<CardEffect> pairRestrictionStatics = null;
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof BlockabilityRestrictionEffect restriction) {
                if (!unblockable) {
                    // Defender-condition unblockable (e.g. "can't be blocked if defending player controls a Forest")
                    if (restriction.unblockableIfDefenderControls() != null
                            && context.defenderBattlefield != null && context.defenderBattlefield.stream()
                                .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, restriction.unblockableIfDefenderControls()))) {
                        unblockable = true;
                    }
                    if (restriction.unblockableIfControllerCastHistoricSpellThisTurn()) {
                        UUID controllerId = findPermanentController(gameData, attacker.getId());
                        if (controllerId != null && playerCastHistoricSpellThisTurn(gameData, controllerId)) {
                            unblockable = true;
                        }
                    }
                    if (restriction.unblockableWhileAttackingAlone() && isAttackingAlone(gameData, attacker)) {
                        unblockable = true;
                    }
                }
                if (restriction.blockableOnlyBy() != null || restriction.cantBeBlockedByCreaturesMatching() != null) {
                    if (pairRestrictionStatics == null) {
                        pairRestrictionStatics = new ArrayList<>(2);
                    }
                    pairRestrictionStatics.add(effect);
                }
            }
        }
        StaticBonus bonus = computeStaticBonus(gameData, attacker);
        boolean intimidate = hasKeyword(attacker, bonus, Keyword.INTIMIDATE);
        BlockDenial landwalkDenial = null;
        for (var entry : Keyword.LANDWALK_MAP.entrySet()) {
            if (hasKeyword(attacker, bonus, entry.getKey())
                    && context.defenderCardSubtypes.contains(entry.getValue())) {
                landwalkDenial = new BlockDenial(BlockDenial.Reason.LANDWALK,
                        entry.getValue().getDisplayName().toLowerCase());
                break;
            }
        }
        return new BlockLegalityContext.AttackerFacts(
                unblockable,
                hasKeyword(attacker, bonus, Keyword.FLYING),
                hasKeyword(attacker, bonus, Keyword.HORSEMANSHIP),
                hasKeyword(attacker, bonus, Keyword.FEAR),
                intimidate,
                intimidate ? getEffectiveColors(gameData, attacker) : Set.of(),
                pairRestrictionStatics == null ? List.of() : pairRestrictionStatics,
                getAuraGrantedBlockingRestrictions(gameData, attacker),
                landwalkDenial);
    }

    private BlockLegalityContext.BlockerFacts buildBlockerFacts(BlockLegalityContext context, Permanent blocker) {
        GameData gameData = context.gameData;
        StaticBonus bonus = computeStaticBonus(gameData, blocker);
        List<CanBlockOnlyIfAttackerMatchesPredicateEffect> attackerFilterRestrictions = null;
        boolean cantBlockStatic = false;
        boolean cantBlockPowerAtLeastOwnToughnessStatic = false;
        Integer cantBlockPowerAtLeast = null;
        for (CardEffect effect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanBlockOnlyIfAttackerMatchesPredicateEffect restriction) {
                if (attackerFilterRestrictions == null) {
                    attackerFilterRestrictions = new ArrayList<>(2);
                }
                attackerFilterRestrictions.add(restriction);
            }
            if (effect instanceof BlockingRestrictionEffect restriction) {
                if (restriction.cantBlock()) {
                    cantBlockStatic = true;
                }
                if (restriction.cantBlockCreaturesWithPowerAtLeastOwnToughness()) {
                    cantBlockPowerAtLeastOwnToughnessStatic = true;
                }
                Integer threshold = restriction.cantBlockCreaturesWithPowerAtLeast();
                if (threshold != null && (cantBlockPowerAtLeast == null || threshold < cantBlockPowerAtLeast)) {
                    cantBlockPowerAtLeast = threshold;
                }
            }
        }
        return new BlockLegalityContext.BlockerFacts(
                hasKeyword(blocker, bonus, Keyword.FLYING),
                hasKeyword(blocker, bonus, Keyword.REACH),
                hasKeyword(blocker, bonus, Keyword.HORSEMANSHIP),
                isArtifact(blocker),
                getEffectiveColors(gameData, blocker),
                attackerFilterRestrictions == null ? List.of() : attackerFilterRestrictions,
                cantBlockStatic || hasAuraWithEffect(gameData, blocker, CantBlockEffect.class)
                        || hasGlobalCantAttackOrBlockRestriction(gameData, blocker),
                cantBlockPowerAtLeastOwnToughnessStatic || hasAuraWithEffect(gameData, blocker,
                        CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect.class),
                cantBlockPowerAtLeast);
    }

    /** Rebuilds the exact pre-context user-facing message for a failed block-legality check. */
    private static String formatBlockDenial(BlockDenial denial, Permanent blocker, Permanent attacker) {
        String blockerName = blocker.getCard().getName();
        String attackerName = attacker.getCard().getName();
        return switch (denial.reason()) {
            case CANT_BE_BLOCKED -> attackerName + " can't be blocked";
            case FLYING -> blockerName + " cannot block " + attackerName + " (flying)";
            case HORSEMANSHIP -> blockerName + " cannot block " + attackerName + " (horsemanship)";
            case FEAR -> blockerName + " cannot block " + attackerName + " (fear)";
            case INTIMIDATE -> blockerName + " cannot block " + attackerName + " (intimidate)";
            case BLOCKER_LIMITED_TO_ATTACKERS -> blockerName + " can only block " + denial.detail();
            case GLOBAL_RESTRICTION -> denial.detail();
            case ATTACKER_LIMITED_TO_BLOCKERS -> attackerName + " can only be blocked by " + denial.detail();
            case CANT_BE_BLOCKED_BY_MATCHING -> blockerName + " cannot block " + attackerName;
            case LANDWALK -> attackerName + " can't be blocked (" + denial.detail() + "walk)";
            case CANT_BLOCK_THIS_TURN -> blockerName + " can't block this turn";
            case CANT_BLOCK -> blockerName + " can't block";
            case CANT_BLOCK_POWER_AT_LEAST_OWN_TOUGHNESS ->
                    blockerName + " can't block " + attackerName + " (power too high)";
            case CANT_BLOCK_HIGH_POWER ->
                    blockerName + " can't block " + attackerName + " (power too high)";
            case CANT_BLOCK_THAT_ATTACKER -> blockerName + " can't block " + attackerName + " this turn";
            case PROTECTION -> blockerName + " cannot block " + attackerName + " (protection)";
        };
    }

    private List<CanBeBlockedOnlyByFilterEffect> getAuraGrantedBlockingRestrictions(GameData gameData, Permanent creature) {
        List<CanBeBlockedOnlyByFilterEffect> restrictions = new ArrayList<>();
        gameData.forEachPermanent((playerId, aura) -> {
            if (!aura.isAttached() || !aura.getAttachedTo().equals(creature.getId())) {
                return;
            }
            for (CardEffect effect : aura.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CanBeBlockedOnlyByFilterEffect restriction) {
                    restrictions.add(restriction);
                }
            }
        });
        return restrictions;
    }

    /**
     * Returns {@code true} if the given permanent has at least one aura attached to it.
     */
    public boolean isEnchanted(GameData gameData, Permanent creature) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && p.getAttachedTo().equals(creature.getId())
                        && p.getCard().isAura());
    }

    /**
     * Finds the creature that is enchanted by an aura with the given static effect type,
     * searching only the specified player's battlefield. Returns the enchanted creature,
     * or {@code null} if no matching aura is found.
     */
    public Permanent findEnchantedCreatureByAuraEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectClass) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent p : bf) {
            if (p.isAttached()) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effectClass.isInstance(effect)) {
                        return findPermanentById(gameData, p.getAttachedTo());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds a permanent on the given player's battlefield that itself carries the given static
     * effect type, returning that permanent (e.g. Empyrial Archangel redirecting damage to itself),
     * or {@code null} if none is found.
     */
    public Permanent findControlledPermanentWithStaticEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectClass) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent p : bf) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effectClass.isInstance(effect)) {
                    return p;
                }
            }
        }
        return null;
    }

    // --- Other ---

    public boolean isCreatureSubtype(CardSubtype subtype) {
        return !NON_CREATURE_SUBTYPES.contains(subtype);
    }

    /**
     * Returns the global damage multiplier based on {@link GlobalDamageMultiplyingEffect} permanents
     * on the battlefield (e.g. Furnace of Rath). Each instance multiplies by its factor, and multiple
     * instances stack multiplicatively (e.g. two Furnaces = 4x damage).
     */
    public int getDamageMultiplier(GameData gameData) {
        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GlobalDamageMultiplyingEffect multiplyingEffect) {
                    multiplier[0] *= multiplyingEffect.damageMultiplierFactor();
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Returns the damage multiplier that applies to damage dealt to a specific player based on
     * {@link DoubleDamageToEnchantedPlayerEffect} Curse Auras enchanting that player (e.g. Curse
     * of Bloodletting). Each Curse enchanting the player doubles the multiplier; multiple instances
     * stack multiplicatively. Returns {@code 1} when no such Curse enchants the player.
     */
    public int getEnchantedPlayerDamageMultiplier(GameData gameData, UUID playerId) {
        int[] multiplier = {1};
        gameData.forEachPermanent((controllerId, p) -> {
            if (!p.isAttached() || !playerId.equals(p.getAttachedTo())) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof DoubleDamageToEnchantedPlayerEffect) {
                    multiplier[0] *= 2;
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Applies the global damage multiplier to the given damage amount.
     *
     * @return the damage after applying all {@link GlobalDamageMultiplyingEffect} multipliers
     */
    public int applyDamageMultiplier(GameData gameData, int damage) {
        return damage * getDamageMultiplier(gameData);
    }

    /**
     * Returns the token creation multiplier for a specific player based on
     * {@link MultiplyTokenCreationEffect} permanents they control.
     * Unlike {@link #getDamageMultiplier} which is global, this is controller-specific:
     * only permanents controlled by the given player contribute to the multiplier.
     * Multiple instances stack multiplicatively (e.g. two Parallel Lives = 4x tokens).
     */
    public int getTokenMultiplier(GameData gameData, UUID controllerId) {
        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(controllerId)) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof MultiplyTokenCreationEffect mtce) {
                    multiplier[0] *= mtce.multiplier();
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Applies the global damage multiplier and per-controller damage multipliers
     * (e.g. {@link DoubleControllerDamageEffect}) to the given damage amount.
     *
     * @param entry the stack entry representing the damage source; used to check controller
     *              and to evaluate stack-entry predicates on controller damage effects
     * @return the damage after applying all multipliers
     */
    public int applyDamageMultiplier(GameData gameData, int damage, StackEntry entry) {
        int bonus = (damage > 0 && entry != null)
                ? getColorSourceDamageBonus(gameData, entry.getControllerId(), entry.getCard().getColors())
                : 0;
        UUID controllerId = entry != null ? entry.getControllerId() : null;
        return (damage + bonus) * getDamageMultiplier(gameData)
                * getControllerDamageMultiplier(gameData, controllerId, entry, false);
    }

    /**
     * Returns the per-controller damage multiplier based on {@link DoubleControllerDamageEffect}
     * permanents on the battlefield. Only applies when the source is controlled by the same player
     * who controls the permanent with the effect.
     *
     * <p>Each effect has a {@code stackFilter} predicate and an {@code appliesToCombatDamage} flag.
     * For stack-based damage, the effect applies if the filter is {@code null} (matches all) or if
     * the entry matches the filter. For combat damage ({@code isCombat=true}), the effect applies
     * only if {@code appliesToCombatDamage} is {@code true}.
     *
     * <p>Multiple instances stack multiplicatively.
     *
     * @param entry the stack entry, or {@code null} for combat damage
     * @param isCombat whether this is combat damage
     */
    int getControllerDamageMultiplier(GameData gameData, UUID controllerId, StackEntry entry, boolean isCombat) {
        if (controllerId == null) return 1;

        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(controllerId)) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof DoubleControllerDamageEffect dcde) {
                    if (isCombat) {
                        if (dcde.appliesToCombatDamage()) {
                            multiplier[0] *= 2;
                        }
                    } else if (entry != null) {
                        if (dcde.stackFilter() == null || predicateEvaluationService.matchesStackEntryPredicate(entry, dcde.stackFilter(), null)) {
                            multiplier[0] *= 2;
                        }
                    }
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Returns {@code true} if the given stack entry represents an instant or sorcery spell
     * that should have lifelink due to a {@link GrantLifelinkToControllerSpellsByColorEffect}
     * on the controller's battlefield. The spell's color must match the effect's required color.
     */
    public boolean shouldControllerSpellHaveLifelink(GameData gameData, StackEntry entry) {
        if (entry == null) return false;
        StackEntryType type = entry.getEntryType();
        if (type != StackEntryType.INSTANT_SPELL && type != StackEntryType.SORCERY_SPELL) return false;

        boolean[] hasLifelink = {false};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(entry.getControllerId())) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantLifelinkToControllerSpellsByColorEffect glse
                        && entry.getCard().getColors().contains(glse.color())) {
                    hasLifelink[0] = true;
                }
            }
        });
        return hasLifelink[0];
    }

    /**
     * Returns the combat damage multiplier for a creature based on
     * {@link DoubleEquippedCreatureCombatDamageEffect} on attached equipment.
     * Each such equipment doubles the multiplier.
     */
    public int getEquippedCreatureCombatDamageMultiplier(GameData gameData, Permanent creature) {
        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            if (p.isAttached() && p.getAttachedTo() != null && p.getAttachedTo().equals(creature.getId())) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof DoubleEquippedCreatureCombatDamageEffect) {
                        multiplier[0] *= 2;
                    }
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Applies the global damage multiplier and creature-specific combat damage multipliers
     * to the given combat damage amount. The source multiplier doubles damage dealt by the
     * source creature, and the target multiplier doubles damage received by the target creature.
     *
     * @param source the creature dealing combat damage
     * @param target the creature receiving combat damage, or {@code null} if damage is to a player
     * @return the damage after applying all multipliers
     */
    public int applyCombatDamageMultiplier(GameData gameData, int damage, Permanent source, Permanent target) {
        int bonus = 0;
        UUID controllerId = findPermanentController(gameData, source.getId());
        if (damage > 0) {
            if (controllerId != null) {
                bonus = getColorSourceDamageBonus(gameData, controllerId, source.getCard().getColors());
            }
        }
        int result = (damage + bonus) * getDamageMultiplier(gameData);
        result *= getControllerDamageMultiplier(gameData, controllerId, null, true);
        result *= getEquippedCreatureCombatDamageMultiplier(gameData, source);
        if (target != null) {
            result *= getEquippedCreatureCombatDamageMultiplier(gameData, target);
        }
        return result;
    }

    /**
     * Returns the additive damage bonus for sources of matching color controlled by the given
     * player this turn (e.g. The Flame of Keld Chapter III). Returns the sum of all matching
     * color bonuses. Returns 0 if no bonus applies.
     */
    int getColorSourceDamageBonus(GameData gameData, UUID controllerId, List<CardColor> sourceColors) {
        if (controllerId == null || sourceColors == null || sourceColors.isEmpty()) {
            return 0;
        }
        Map<CardColor, Integer> colorMap = gameData.colorSourceDamageBonusThisTurn.get(controllerId);
        if (colorMap == null || colorMap.isEmpty()) {
            return 0;
        }
        int bonus = 0;
        for (CardColor color : sourceColors) {
            bonus += colorMap.getOrDefault(color, 0);
        }
        return bonus;
    }

    /**
     * Returns {@code true} if the given creature is prevented from dealing damage. This
     * can be caused by an attached aura with {@link PreventAllDamageToAndByEnchantedCreatureEffect},
     * a global color-based damage prevention effect, or a per-permanent damage prevention flag.
     */
    public boolean isPreventedFromDealingDamage(GameData gameData, Permanent creature) {
        return isPreventedFromDealingDamage(gameData, creature, false);
    }

    /**
     * Returns {@code true} if the given creature is prevented from dealing damage.
     * When {@code isCombatDamage} is {@code true}, also checks for combat-specific
     * prevention effects (e.g. {@link PreventAllCombatDamageToAndByEnchantedCreatureEffect}).
     */
    public boolean isPreventedFromDealingDamage(GameData gameData, Permanent creature, boolean isCombatDamage) {
        if (!isDamagePreventable(gameData)) return false;
        if (hasAuraWithEffect(gameData, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)
                || gameData.permanentsPreventedFromDealingDamage.contains(creature.getId())) {
            return true;
        }
        for (CardColor color : getEffectiveColors(gameData, creature)) {
            if (isDamageFromSourcePrevented(gameData, color)) return true;
        }
        if (isCombatDamage && hasAuraWithEffect(gameData, creature, PreventAllCombatDamageToAndByEnchantedCreatureEffect.class)) {
            return true;
        }
        if (isCombatDamage && gameData.creaturesPreventedFromDealingCombatDamage.contains(creature.getId())) {
            return true;
        }
        if (isCombatDamage && gameData.combatDamageExemptPredicate != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, creature, gameData.combatDamageExemptPredicate)) {
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if damage from sources of the given color is currently prevented
     * (e.g. by a "prevent all damage from [color] sources" effect).
     */
    public boolean isDamageFromSourcePrevented(GameData gameData, CardColor sourceColor) {
        return sourceColor != null && gameData.preventDamageFromColors.contains(sourceColor);
    }

    /**
     * Counts the number of permanents with the given subtype controlled by the specified player.
     */
    /**
     * Counts the permanents controlled by {@code controllerId} that match {@code predicate}.
     * Used by ability activation restrictions such as Leechridden Swamp's "Activate only if you
     * control two or more black permanents".
     */
    public int countControlledPermanentsMatching(GameData gameData, UUID controllerId, PermanentPredicate predicate) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, predicate)) {
                count++;
            }
        }
        return count;
    }

    public int countControlledSubtypePermanents(GameData gameData, UUID controllerId, CardSubtype subtype) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getSubtypes().contains(subtype)
                    || permanent.getTransientSubtypes().contains(subtype)
                    || permanent.getGrantedSubtypes().contains(subtype)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns {@code true} if the permanent's mana abilities can currently be activated,
     * i.e. no static lock (Stony Silence, Pithing Needle with blocksManaAbilities, Phyrexian Revoker)
     * or aura-based lock (Arrest, Ice Cage) prevents it.
     */
    public boolean canActivateManaAbility(GameData gameData, Permanent permanent) {
        String cardName = permanent.getCard().getName();

        // Check temporary ability loss (e.g. Merfolk Trickster)
        if (permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            return false;
        }

        // Check continuous ability loss (e.g. Deep Freeze aura)
        StaticBonus staticBonus = computeStaticBonus(gameData, permanent);
        if (staticBonus.losesAllAbilities()) {
            return false;
        }

        // Check aura-based ability removal (Deep Freeze)
        if (hasAuraWithEffect(gameData, permanent, LosesAllAbilitiesEffect.class)) {
            return false;
        }

        // Check aura-based locks (Arrest, Ice Cage)
        if (hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            return false;
        }

        for (UUID pid : gameData.playerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect lock) {
                        if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, lock.predicate())) {
                            return false;
                        }
                    }
                    if (effect instanceof ActivatedAbilitiesOfChosenNameCantBeActivatedEffect lock) {
                        if (lock.blocksManaAbilities() && cardName.equals(p.getChosenName())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns the overridden mana color for a land whose land types have been set by a
     * type-changing effect (Evil Presence, Convincing Mirage, Blood Moon, Tideshaper Mystic, ...),
     * or {@code null} if no land-type-setting effect applies. Resolved by the CR 613 layer-4
     * pass, so of several setters the latest timestamp wins (CR 613.7).
     */
    public ManaColor getOverriddenLandManaColor(GameData gameData, Permanent permanent) {
        CardSubtype override = layerSystemService.landTypeOverrideFor(gameData, permanent.getId());
        return override == null ? null
                : EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(override);
    }
}

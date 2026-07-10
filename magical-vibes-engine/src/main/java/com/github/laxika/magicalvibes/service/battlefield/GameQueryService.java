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
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.AllowExtraLoyaltyActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEquippedEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTransformEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetColorMode;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingSourceKind;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantGetPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantReduceLifeBelowOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealtAsInfectBelowZeroLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantActivateAbilitiesOfGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleEquippedCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerShroudEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;

import java.util.*;
import java.util.function.BiFunction;
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
    public record StaticBonus(int power, int toughness, Set<Keyword> keywords, Set<CardColor> protectionColors, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, List<CardEffect> grantedEffects, Set<CardColor> grantedColors, List<CardSubtype> grantedSubtypes, Set<CardType> grantedCardTypes, Set<CardSupertype> grantedSupertypes, boolean colorOverriding, boolean subtypeOverriding, boolean landSubtypeOverriding, Set<Keyword> removedKeywords, boolean basePTOverridden, int basePowerOverride, int baseToughnessOverride, boolean losesAllAbilities) {
        static final StaticBonus NONE = new StaticBonus(0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(), Set.of(), false, false, false, Set.of(), false, 0, 0, false);
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
        return !anyBattlefieldHasStaticEffect(gameData, PlayersCantGainLifeEffect.class);
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
        return hasSelfBecomeCreatureEffect(gameData, permanent);
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
            if (effect instanceof CanAttackAsThoughNoDefenderEffect) {
                return true;
            }
            if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof CanAttackAsThoughNoDefenderEffect) {
                if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(creature, controllerId))) {
                    return true;
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

    // --- Keyword & effect checking ---

    /**
     * Returns {@code true} if the permanent has the given keyword, either intrinsically
     * or granted by static effects from other permanents.
     */
    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.removedKeywords().contains(keyword)) return false;
        if (bonus.losesAllAbilities() || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            // Creature has lost all its own abilities; only keywords granted by static effects apply
            return bonus.keywords().contains(keyword);
        }
        return permanent.hasKeyword(keyword) || bonus.keywords().contains(keyword);
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
                .anyMatch(e -> e instanceof CantBeBlockedEffect)) return true;
        if (hasAuraWithEffect(gameData, creature, CantBeBlockedEffect.class)) return true;
        return hasGrantedEffect(gameData, creature, CantBeBlockedEffect.class);
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
     * Handles Layer 7b base P/T overrides from continuous effects (e.g. Deep Freeze).
     */
    public int getEffectivePower(Permanent permanent, StaticBonus bonus) {
        if (bonus.basePTOverridden() && !permanent.isBasePowerToughnessOverriddenUntilEndOfTurn()) {
            int power;
            if (permanent.isPowerToughnessSwitched()) {
                power = bonus.baseToughnessOverride() + permanent.getToughnessModifiers();
            } else {
                power = bonus.basePowerOverride() + permanent.getPowerModifiers();
            }
            return power + bonus.power();
        }
        return permanent.getEffectivePower() + bonus.power();
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
     * Handles Layer 7b base P/T overrides from continuous effects (e.g. Deep Freeze).
     */
    public int getEffectiveToughness(Permanent permanent, StaticBonus bonus) {
        if (bonus.basePTOverridden() && !permanent.isBasePowerToughnessOverriddenUntilEndOfTurn()) {
            int toughness;
            if (permanent.isPowerToughnessSwitched()) {
                toughness = bonus.basePowerOverride() + permanent.getPowerModifiers();
            } else {
                toughness = bonus.baseToughnessOverride() + permanent.getToughnessModifiers();
            }
            return toughness + bonus.toughness();
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
    public StaticBonus computeStaticBonus(GameData gameData, Permanent target) {
        boolean isNaturalCreature = hasCardType(target, CardType.CREATURE);
        StaticBonusAccumulator accumulator = new StaticBonusAccumulator();
        gameData.forEachBattlefield((playerId, bf) -> {
            boolean targetOnSameBattlefield = bf.contains(target);
            for (Permanent source : bf) {
                if (source == target) continue;
                StaticEffectContext context = new StaticEffectContext(source, target, targetOnSameBattlefield, gameData);
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    StaticEffectHandler handler = staticEffectRegistry.getHandler(effect);
                    if (handler != null) {
                        handler.apply(context, effect, accumulator);
                    }
                }
            }
        });
        // Process emblem static effects
        for (Emblem emblem : gameData.emblems) {
            List<Permanent> ownerBf = gameData.playerBattlefields.get(emblem.controllerId());
            if (ownerBf == null || !ownerBf.contains(target)) continue;
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
                }
            }
        }

        // Handle characteristic-defining abilities (self-referencing static effects like */* P/T)
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            StaticEffectHandler selfHandler = staticEffectRegistry.getSelfHandler(effect);
            if (selfHandler != null) {
                StaticEffectContext selfContext = new StaticEffectContext(target, target, true, gameData);
                selfHandler.apply(selfContext, effect, accumulator);
            }
        }

        // Transient "becomes the basic land type of your choice" override (e.g. Tideshaper Mystic):
        // replaces the land's subtypes and mana ability until end of turn (rule 305.7).
        if (target.getTransientLandTypeOverride() != null) {
            accumulator.addGrantedSubtype(target.getTransientLandTypeOverride());
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }

        boolean isSelfAnimated = target.isAnimatedUntilEndOfTurn() || target.isAnimatedUntilEndOfCombat() || target.isAnimatedUntilNextTurn() || target.getCounterCount(CounterType.AWAKENING) > 0 || accumulator.isSelfBecomeCreature();
        if (!isNaturalCreature
                && !accumulator.isAnimatedCreature()
                && !isSelfAnimated
                && accumulator.getKeywords().isEmpty()
                && accumulator.getGrantedActivatedAbilities().isEmpty()
                && accumulator.getProtectionColors().isEmpty()
                && accumulator.getGrantedColors().isEmpty()
                && accumulator.getGrantedSubtypes().isEmpty()
                && accumulator.getGrantedSupertypes().isEmpty()) {
            return StaticBonus.NONE;
        }

        int power = accumulator.getPower();
        int toughness = accumulator.getToughness();
        if (accumulator.isAnimatedCreature() && !isSelfAnimated) {
            int manaValue = target.getCard().getManaValue();
            power += manaValue;
            toughness += manaValue;
        }

        return accumulator.toStaticBonus(power, toughness, accumulator.isAnimatedCreature() || isSelfAnimated);
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
     * Checks the permanent's own {@link ProtectionFromColorsEffect}, static bonuses from
     * other permanents, and the permanent's chosen color (e.g. from a "choose a color" effect).
     */
    public boolean hasProtectionFrom(GameData gameData, Permanent target, CardColor sourceColor) {
        if (sourceColor == null) return false;
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionFromColorsEffect protection
                    && protection.scope() == null
                    && protection.colors().contains(sourceColor)) {
                return true;
            }
        }
        StaticBonus bonus = computeStaticBonus(gameData, target);
        if (bonus.protectionColors().contains(sourceColor)) {
            return true;
        }
        // Protection granted by another permanent's static effect (e.g. Favor of the Mighty
        // via GrantEffectEffect(ProtectionFromColorsEffect, ...)).
        for (CardEffect effect : bonus.grantedEffects()) {
            if (effect instanceof ProtectionFromColorsEffect protection
                    && protection.colors().contains(sourceColor)) {
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
            if (effect instanceof ProtectionFromCardTypesEffect protection) {
                protectedTypes.addAll(protection.cardTypes());
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
            if (effect instanceof ProtectionFromCardTypesEffect protection) {
                protectedTypes.addAll(protection.cardTypes());
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
            if (effect instanceof ProtectionFromSubtypesEffect protection) {
                protectedSubtypes.addAll(protection.subtypes());
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
            if (effect instanceof ProtectionFromSubtypesEffect protection) {
                protectedSubtypes.addAll(protection.subtypes());
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
     * Returns {@code true} if the target permanent has protection from the source permanent,
     * checking color-based, card-type-based, subtype-based, and non-subtype-creature protection.
     */
    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Permanent source) {
        return hasProtectionFrom(gameData, target, source.getEffectiveColor())
                || hasProtectionFromSourceCardTypes(gameData, target, source)
                || hasProtectionFromSourceSubtypes(gameData, target, source)
                || hasProtectionFromNonSubtypeCreatures(gameData, target, source);
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
                || hasProtectionFromNonSubtypeCreatures(target, sourceCard);
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
        return source != null && hasKeyword(gameData, source, keyword);
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
     * Returns {@code true} if the player controls a permanent with
     * {@link AllowExtraLoyaltyActivationEffect}, allowing planeswalker loyalty abilities
     * to be activated twice per turn instead of once (Oath of Teferi).
     */
    public boolean hasExtraLoyaltyActivation(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, AllowExtraLoyaltyActivationEffect.class);
    }

    /**
     * Returns {@code true} if the given card cannot be countered, either because it has
     * its own "can't be countered" ability ({@link CantBeCounteredEffect}), or because
     * a {@link CreatureSpellsCantBeCounteredEffect} on the battlefield protects creature spells.
     */
    public boolean isUncounterable(GameData gameData, Card card) {
        if (card.getEffects(EffectSlot.STATIC).stream().anyMatch(e -> e instanceof CantBeCounteredEffect)) {
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
                && !isCantBlockUnlessConditionUnmet(gameData, creature);
    }

    /**
     * Returns {@code true} if the creature has a {@link CantAttackOrBlockUnlessEffect} whose condition
     * is not met (block side, mirrors the attack side in {@code CombatAttackService}).
     */
    private boolean isCantBlockUnlessConditionUnmet(GameData gameData, Permanent creature) {
        UUID controllerId = null;
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantAttackOrBlockUnlessEffect restriction) {
                if (controllerId == null) {
                    controllerId = findPermanentController(gameData, creature.getId());
                    if (controllerId == null) return false;
                }
                if (!conditionEvaluationService.isMet(gameData, restriction.condition(),
                        ConditionContext.forPermanent(creature, controllerId))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given blocker can legally block the given attacker,
     * considering all evasion abilities, blocking restrictions, landwalk, and protection.
     * This is the single source of truth for pairwise block legality.
     */
    public boolean canBlockAttacker(GameData gameData, Permanent blocker, Permanent attacker,
                                    List<Permanent> defenderBattlefield) {
        return getBlockingIllegalityReason(gameData, blocker, attacker, defenderBattlefield).isEmpty();
    }

    /**
     * Returns the reason a blocker cannot legally block the given attacker, or empty if the block is legal.
     * This is the single source of truth for pairwise block legality validation.
     */
    public Optional<String> getBlockingIllegalityReason(GameData gameData, Permanent blocker,
                                                        Permanent attacker, List<Permanent> defenderBattlefield) {
        if (hasCantBeBlocked(gameData, attacker)) {
            return Optional.of(attacker.getCard().getName() + " can't be blocked");
        }
        // Defender-condition unblockable (e.g. "can't be blocked if defending player controls a Forest")
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeBlockedIfDefenderControlsMatchingPermanentEffect restriction) {
                boolean defenderMatches = defenderBattlefield != null && defenderBattlefield.stream()
                        .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, restriction.defenderPermanentPredicate()));
                if (defenderMatches) {
                    return Optional.of(attacker.getCard().getName() + " can't be blocked");
                }
            }
            if (effect instanceof CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect) {
                UUID controllerId = findPermanentController(gameData, attacker.getId());
                if (controllerId != null && playerCastHistoricSpellThisTurn(gameData, controllerId)) {
                    return Optional.of(attacker.getCard().getName() + " can't be blocked");
                }
            }
            if (effect instanceof CantBeBlockedIfAttackingAloneEffect && isAttackingAlone(gameData, attacker)) {
                return Optional.of(attacker.getCard().getName() + " can't be blocked");
            }
        }
        if (hasKeyword(gameData, attacker, Keyword.FLYING)
                && !hasKeyword(gameData, blocker, Keyword.FLYING)
                && !hasKeyword(gameData, blocker, Keyword.REACH)) {
            return Optional.of(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (flying)");
        }
        if (hasKeyword(gameData, attacker, Keyword.FEAR)
                && !isArtifact(blocker)
                && blocker.getEffectiveColor() != CardColor.BLACK) {
            return Optional.of(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (fear)");
        }
        if (hasKeyword(gameData, attacker, Keyword.INTIMIDATE)
                && !isArtifact(blocker)
                && blocker.getEffectiveColor() != attacker.getEffectiveColor()) {
            return Optional.of(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (intimidate)");
        }
        for (CardEffect blockerStaticEffect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
            if (blockerStaticEffect instanceof CanBlockOnlyIfAttackerMatchesPredicateEffect restriction
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, restriction.attackerPredicate())) {
                return Optional.of(blocker.getCard().getName() + " can only block " + restriction.allowedAttackersDescription());
            }
        }
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanBeBlockedOnlyByFilterEffect restriction
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return Optional.of(attacker.getCard().getName() + " can only be blocked by " + restriction.allowedBlockersDescription());
            }
        }
        for (CanBeBlockedOnlyByFilterEffect restriction : getAuraGrantedBlockingRestrictions(gameData, attacker)) {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return Optional.of(attacker.getCard().getName() + " can only be blocked by " + restriction.allowedBlockersDescription());
            }
        }
        if (defenderBattlefield != null) {
            for (var entry : Keyword.LANDWALK_MAP.entrySet()) {
                if (hasKeyword(gameData, attacker, entry.getKey())
                        && defenderBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(entry.getValue()))) {
                    return Optional.of(attacker.getCard().getName() + " can't be blocked (" + entry.getValue().getDisplayName().toLowerCase() + "walk)");
                }
            }
        }
        if (blocker.isCantBlockThisTurn()) {
            return Optional.of(blocker.getCard().getName() + " can't block this turn");
        }
        boolean hasCantBlockStatic = blocker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantBlockEffect.class::isInstance);
        if (hasCantBlockStatic) {
            return Optional.of(blocker.getCard().getName() + " can't block");
        }
        if (hasAuraWithEffect(gameData, blocker, CantBlockEffect.class)) {
            return Optional.of(blocker.getCard().getName() + " can't block");
        }
        if (blocker.getCantBlockIds().contains(attacker.getId())) {
            return Optional.of(blocker.getCard().getName() + " can't block " + attacker.getCard().getName() + " this turn");
        }
        if (hasProtectionFromSource(gameData, attacker, blocker)) {
            return Optional.of(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (protection)");
        }
        return Optional.empty();
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

    // --- Other ---

    public boolean isCreatureSubtype(CardSubtype subtype) {
        return !NON_CREATURE_SUBTYPES.contains(subtype);
    }

    /**
     * Returns the global damage multiplier based on {@link DoubleDamageEffect} permanents on
     * the battlefield. Each instance doubles the multiplier (e.g. two Furnaces = 4x damage).
     */
    public int getDamageMultiplier(GameData gameData) {
        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof DoubleDamageEffect) {
                    multiplier[0] *= 2;
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
     * @return the damage after applying all {@link DoubleDamageEffect} multipliers
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
                || isDamageFromSourcePrevented(gameData, creature.getEffectiveColor())
                || gameData.permanentsPreventedFromDealingDamage.contains(creature.getId())) {
            return true;
        }
        if (isCombatDamage && hasAuraWithEffect(gameData, creature, PreventAllCombatDamageToAndByEnchantedCreatureEffect.class)) {
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
     * Returns the overridden mana color for a land whose type has been changed
     * by an aura (e.g. Evil Presence, Convincing Mirage), or {@code null} if
     * the land's type has not been overridden.
     */
    public ManaColor getOverriddenLandManaColor(GameData gameData, Permanent permanent) {
        // Transient self-override (e.g. Tideshaper Mystic) takes precedence over aura-based overrides.
        if (permanent.getTransientLandTypeOverride() != null) {
            return EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(permanent.getTransientLandTypeOverride());
        }
        for (UUID pid : gameData.orderedPlayerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                if (p.isAttached() && p.getAttachedTo().equals(permanent.getId())) {
                    for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof EnchantedPermanentBecomesTypeEffect landTypeEffect) {
                            return EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(
                                    landTypeEffect.subtype());
                        }
                        if (effect instanceof EnchantedPermanentBecomesChosenTypeEffect
                                && p.getChosenSubtype() != null) {
                            return EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(
                                    p.getChosenSubtype());
                        }
                    }
                }
            }
        }
        // Global "nonbasic lands are [type]s" effects (e.g. Blood Moon) affect every nonbasic land.
        if (permanent.getCard().hasType(CardType.LAND)
                && !permanent.getCard().getSupertypes().contains(CardSupertype.BASIC)) {
            for (UUID pid : gameData.orderedPlayerIds) {
                for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                    for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof NonbasicLandsBecomeTypeEffect landTypeEffect) {
                            return EnchantedPermanentBecomesTypeEffect.manaColorForLandSubtype(
                                    landTypeEffect.subtype());
                        }
                    }
                }
            }
        }
        return null;
    }
}

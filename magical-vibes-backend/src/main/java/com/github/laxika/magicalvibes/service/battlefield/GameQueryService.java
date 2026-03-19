package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEquippedEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetedByNonColorSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetedBySpellColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantGetPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealtAsInfectBelowZeroLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerSpellDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleEquippedCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerShroudEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.filter.CardHasFlashbackPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PhyrexianManaPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;

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
    public record StaticBonus(int power, int toughness, Set<Keyword> keywords, Set<CardColor> protectionColors, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, List<CardEffect> grantedEffects, Set<CardColor> grantedColors, List<CardSubtype> grantedSubtypes, Set<CardType> grantedCardTypes, boolean colorOverriding, boolean subtypeOverriding, boolean landSubtypeOverriding, Set<Keyword> removedKeywords) {
        static final StaticBonus NONE = new StaticBonus(0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(), false, false, false, Set.of());
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
            perm.getCard().setImprintedCard(card);
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

    private <T> T findInExile(GameData gameData, UUID id, BiFunction<UUID, Card, T> mapper) {
        if (id == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> exile = gameData.playerExiledCards.get(playerId);
            if (exile == null) continue;
            for (Card c : exile) {
                if (c.getId().equals(id)) return mapper.apply(playerId, c);
            }
        }
        return null;
    }

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
        if (predicate == null) return true;

        if (predicate instanceof CardTypePredicate p) {
            return hasCardType(card, p.cardType());
        }
        if (predicate instanceof CardSubtypePredicate p) {
            return card.getSubtypes().contains(p.subtype());
        }
        if (predicate instanceof CardKeywordPredicate p) {
            return card.getKeywords().contains(p.keyword());
        }
        if (predicate instanceof CardIsSelfPredicate) {
            return sourceCardId != null && card.getId().equals(sourceCardId);
        }
        if (predicate instanceof CardColorPredicate p) {
            return card.getColor() != null && card.getColor() == p.color();
        }
        if (predicate instanceof PhyrexianManaPredicate) {
            return card.getManaCost() != null && new ManaCost(card.getManaCost()).hasPhyrexianMana();
        }
        if (predicate instanceof CardIsAuraPredicate) {
            return card.isAura();
        }
        if (predicate instanceof CardHasFlashbackPredicate) {
            return card.getCastingOption(FlashbackCast.class).isPresent();
        }
        if (predicate instanceof CardIsPermanentPredicate) {
            return card.getType().isPermanentType();
        }
        if (predicate instanceof CardSupertypePredicate p) {
            return card.getSupertypes().contains(p.supertype());
        }
        if (predicate instanceof CardMaxManaValuePredicate p) {
            return card.getManaValue() <= p.maxManaValue();
        }
        if (predicate instanceof CardMinManaValuePredicate p) {
            return card.getManaValue() >= p.minManaValue();
        }
        if (predicate instanceof CardNotPredicate p) {
            return !matchesCardPredicate(card, p.predicate(), sourceCardId);
        }
        if (predicate instanceof CardAllOfPredicate p) {
            return p.predicates().stream().allMatch(sub -> matchesCardPredicate(card, sub, sourceCardId));
        }
        if (predicate instanceof CardAnyOfPredicate p) {
            return p.predicates().stream().anyMatch(sub -> matchesCardPredicate(card, sub, sourceCardId));
        }
        return false;
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
     * Returns {@code true} if damage can be prevented. Returns {@code false}
     * when a {@link DamageCantBePreventedEffect} is on any battlefield
     * (e.g. Leyline of Punishment).
     */
    public boolean isDamagePreventable(GameData gameData) {
        return !anyBattlefieldHasStaticEffect(gameData, DamageCantBePreventedEffect.class);
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

    // --- Creature / type classification ---

    /**
     * Returns {@code true} if the permanent is currently a creature. This accounts for
     * the permanent's natural card type, temporary animation effects, awakening counters,
     * global artifact animation, and metalcraft-conditional self-animation.
     */
    public boolean isCreature(GameData gameData, Permanent permanent) {
        if (hasCardType(permanent, CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        if (permanent.isPermanentlyAnimated()) return true;
        if (permanent.getAwakeningCounters() > 0) return true;
        if (isArtifact(permanent) && hasAnimateArtifactEffect(gameData)) return true;
        return hasSelfBecomeCreatureEffect(gameData, permanent);
    }

    /**
     * Returns {@code true} if the permanent has a metalcraft-conditional
     * {@link AnimateSelfWithStatsEffect} and its controller currently meets metalcraft.
     */
    public boolean hasSelfBecomeCreatureEffect(GameData gameData, Permanent permanent) {
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof MetalcraftConditionalEffect metalcraft
                    && metalcraft.wrapped() instanceof AnimateSelfWithStatsEffect) {
                for (UUID playerId : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield != null && battlefield.contains(permanent)) {
                        if (isMetalcraftMet(gameData, playerId)) return true;
                    }
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
     * Returns {@code true} if a creature died this turn (morbid condition).
     * Checks all players' death counts since morbid is not controller-specific.
     */
    public boolean isMorbidMet(GameData gameData) {
        return gameData.creatureDeathCountThisTurn.values().stream()
                .anyMatch(count -> count > 0);
    }

    /**
     * Returns {@code true} if the given creature can attack despite having defender.
     * Checks for {@link CanAttackAsThoughNoDefenderEffect} in static effects, including
     * those wrapped in {@link MetalcraftConditionalEffect}.
     */
    public boolean canAttackDespiteDefender(GameData gameData, Permanent creature) {
        UUID controllerId = findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanAttackAsThoughNoDefenderEffect) {
                return true;
            }
            if (effect instanceof MetalcraftConditionalEffect metalcraft
                    && metalcraft.wrapped() instanceof CanAttackAsThoughNoDefenderEffect) {
                if (isMetalcraftMet(gameData, controllerId)) return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the permanent is an artifact, either by its natural card type
     * or by a granted card type (temporary, from activated/triggered abilities).
     */
    public boolean isArtifact(Permanent permanent) {
        return hasCardType(permanent, CardType.ARTIFACT)
                || permanent.getGrantedCardTypes().contains(CardType.ARTIFACT);
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
     * @param predicate     the predicate to evaluate
     * @param filterContext  context providing game data, source card ID, and source controller ID
     * @return {@code true} if the permanent matches the predicate
     */
    public boolean matchesPermanentPredicate(Permanent permanent,
                                             PermanentPredicate predicate,
                                             FilterContext filterContext) {
        GameData gameData = filterContext != null ? filterContext.gameData() : null;
        UUID sourceCardId = filterContext != null ? filterContext.sourceCardId() : null;
        UUID sourceControllerId = filterContext != null ? filterContext.sourceControllerId() : null;

        if (predicate instanceof PermanentHasKeywordPredicate hasKeywordPredicate) {
            if (gameData == null) {
                return permanent.hasKeyword(hasKeywordPredicate.keyword());
            }
            return hasKeyword(gameData, permanent, hasKeywordPredicate.keyword());
        }
        if (predicate instanceof PermanentHasSubtypePredicate hasSubtypePredicate) {
            boolean creatureSubtype = isCreatureSubtype(hasSubtypePredicate.subtype());
            return permanent.getCard().getSubtypes().contains(hasSubtypePredicate.subtype())
                    || permanent.getTransientSubtypes().contains(hasSubtypePredicate.subtype())
                    || permanent.getGrantedSubtypes().contains(hasSubtypePredicate.subtype())
                    || (creatureSubtype && (gameData == null
                    ? permanent.hasKeyword(Keyword.CHANGELING)
                    : hasKeyword(gameData, permanent, Keyword.CHANGELING)));
        }
        if (predicate instanceof PermanentHasAnySubtypePredicate hasAnySubtypePredicate) {
            boolean hasSubtype = permanent.getCard().getSubtypes().stream().anyMatch(hasAnySubtypePredicate.subtypes()::contains)
                    || permanent.getTransientSubtypes().stream().anyMatch(hasAnySubtypePredicate.subtypes()::contains)
                    || permanent.getGrantedSubtypes().stream().anyMatch(hasAnySubtypePredicate.subtypes()::contains);
            boolean canUseChangeling = hasAnySubtypePredicate.subtypes().stream().anyMatch(this::isCreatureSubtype);
            return hasSubtype || (canUseChangeling && (gameData == null
                    ? permanent.hasKeyword(Keyword.CHANGELING)
                    : hasKeyword(gameData, permanent, Keyword.CHANGELING)));
        }
        if (predicate instanceof PermanentIsCreaturePredicate) {
            if (gameData == null) {
                return hasCardType(permanent, CardType.CREATURE)
                        || permanent.isAnimatedUntilEndOfTurn()
                        || permanent.isPermanentlyAnimated()
                        || permanent.getAwakeningCounters() > 0;
            }
            return isCreature(gameData, permanent);
        }
        if (predicate instanceof PermanentIsLandPredicate) {
            return hasCardType(permanent, CardType.LAND);
        }
        if (predicate instanceof PermanentIsArtifactPredicate) {
            if (gameData == null) {
                return isArtifact(permanent);
            }
            return isArtifact(gameData, permanent);
        }
        if (predicate instanceof PermanentIsEnchantmentPredicate) {
            return hasCardType(permanent, CardType.ENCHANTMENT);
        }
        if (predicate instanceof PermanentIsPlaneswalkerPredicate) {
            return hasCardType(permanent, CardType.PLANESWALKER);
        }
        if (predicate instanceof PermanentIsTappedPredicate) {
            return permanent.isTapped();
        }
        if (predicate instanceof PermanentIsTokenPredicate) {
            return permanent.getCard().isToken();
        }
        if (predicate instanceof PermanentIsAttackingPredicate) {
            return permanent.isAttacking();
        }
        if (predicate instanceof PermanentIsBlockingPredicate) {
            return permanent.isBlocking();
        }
        if (predicate instanceof PermanentPowerAtMostPredicate powerAtMostPredicate) {
            if (gameData == null) {
                return permanent.getEffectivePower() <= powerAtMostPredicate.maxPower();
            }
            return getEffectivePower(gameData, permanent) <= powerAtMostPredicate.maxPower();
        }
        if (predicate instanceof PermanentPowerAtLeastPredicate powerAtLeastPredicate) {
            if (gameData == null) {
                return permanent.getEffectivePower() >= powerAtLeastPredicate.minPower();
            }
            return getEffectivePower(gameData, permanent) >= powerAtLeastPredicate.minPower();
        }
        if (predicate instanceof PermanentColorInPredicate colorInPredicate) {
            if (permanent.isColorOverridden()) {
                return permanent.getTransientColors().stream().anyMatch(colorInPredicate.colors()::contains);
            }
            CardColor effectiveColor = permanent.getEffectiveColor();
            return (effectiveColor != null && colorInPredicate.colors().contains(effectiveColor))
                    || permanent.getTransientColors().stream().anyMatch(colorInPredicate.colors()::contains)
                    || permanent.getGrantedColors().stream().anyMatch(colorInPredicate.colors()::contains);
        }
        if (predicate instanceof PermanentAnyOfPredicate anyOfPredicate) {
            for (PermanentPredicate nested : anyOfPredicate.predicates()) {
                if (matchesPermanentPredicate(permanent, nested, filterContext)) {
                    return true;
                }
            }
            return false;
        }
        if (predicate instanceof PermanentAllOfPredicate allOfPredicate) {
            for (PermanentPredicate nested : allOfPredicate.predicates()) {
                if (!matchesPermanentPredicate(permanent, nested, filterContext)) {
                    return false;
                }
            }
            return true;
        }
        if (predicate instanceof PermanentNotPredicate notPredicate) {
            return !matchesPermanentPredicate(permanent, notPredicate.predicate(), filterContext);
        }
        if (predicate instanceof PermanentIsSourceCardPredicate) {
            return sourceCardId != null && permanent.getOriginalCard().getId().equals(sourceCardId);
        }
        if (predicate instanceof PermanentControlledBySourceControllerPredicate) {
            if (sourceControllerId == null || gameData == null) {
                return false;
            }
            List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
            return controllerBattlefield != null && controllerBattlefield.contains(permanent);
        }
        if (predicate instanceof PermanentAttachedToSourceControllerPredicate) {
            return sourceControllerId != null && permanent.isAttached()
                    && sourceControllerId.equals(permanent.getAttachedTo());
        }
        if (predicate instanceof PermanentToughnessLessThanSourcePowerPredicate) {
            if (gameData == null || sourceCardId == null) {
                return false;
            }
            // Find the source permanent by its card ID
            Permanent sourcePermanent = null;
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (p.getOriginalCard().getId().equals(sourceCardId)) {
                            sourcePermanent = p;
                            break;
                        }
                    }
                }
                if (sourcePermanent != null) break;
            }
            if (sourcePermanent == null) {
                return false;
            }
            int sourcePower = getEffectivePower(gameData, sourcePermanent);
            int targetToughness = getEffectiveToughness(gameData, permanent);
            return targetToughness < sourcePower;
        }
        if (predicate instanceof PermanentHasSameNameAsSourcePredicate) {
            if (gameData == null || sourceCardId == null) {
                return false;
            }
            // Find the source permanent by its current card ID (important for clones
            // where card differs from originalCard)
            Permanent sourcePermanent = null;
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (p.getCard().getId().equals(sourceCardId)) {
                            sourcePermanent = p;
                            break;
                        }
                    }
                }
                if (sourcePermanent != null) break;
            }
            if (sourcePermanent == null) {
                return false;
            }
            return permanent.getCard().getName().equals(sourcePermanent.getCard().getName());
        }
        if (predicate instanceof PermanentTruePredicate) {
            return true;
        }
        return false;
    }

    // --- Stats calculation ---

    /**
     * Returns the permanent's effective power, including its base/modified power
     * plus any static bonuses from other permanents on the battlefield.
     */
    public int getEffectivePower(GameData gameData, Permanent permanent) {
        return permanent.getEffectivePower() + computeStaticBonus(gameData, permanent).power();
    }

    /**
     * Returns the permanent's effective toughness, including its base/modified toughness
     * plus any static bonuses from other permanents on the battlefield.
     */
    public int getEffectiveToughness(GameData gameData, Permanent permanent) {
        return permanent.getEffectiveToughness() + computeStaticBonus(gameData, permanent).toughness();
    }

    /**
     * Returns the amount of combat damage this creature assigns.
     * Normally equal to effective power, but some effects (e.g. Bark of Doran)
     * cause a creature to assign damage equal to its toughness when toughness > power.
     */
    public int getEffectiveCombatDamage(GameData gameData, Permanent creature) {
        int power = getEffectivePower(gameData, creature);
        int toughness = getEffectiveToughness(gameData, creature);

        if (toughness > power && hasAuraWithEffect(gameData, creature, AssignCombatDamageWithToughnessEffect.class)) {
            return toughness;
        }

        return power;
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
                        && (grant.filter() == null || matchesPermanentPredicate(gameData, target, grant.filter()))) {
                    accumulator.addActivatedAbility(grant.ability());
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

        boolean isSelfAnimated = target.isAnimatedUntilEndOfTurn() || target.getAwakeningCounters() > 0 || accumulator.isSelfBecomeCreature();
        if (!isNaturalCreature
                && !accumulator.isAnimatedCreature()
                && !isSelfAnimated
                && accumulator.getKeywords().isEmpty()
                && accumulator.getGrantedActivatedAbilities().isEmpty()
                && accumulator.getProtectionColors().isEmpty()
                && accumulator.getGrantedColors().isEmpty()
                && accumulator.getGrantedSubtypes().isEmpty()) {
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
            if (effect instanceof ProtectionFromColorsEffect protection && protection.colors().contains(sourceColor)) {
                return true;
            }
        }
        if (computeStaticBonus(gameData, target).protectionColors().contains(sourceColor)) {
            return true;
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
            if (effect instanceof CantBeTargetedBySpellColorsEffect cantBeTargeted
                    && cantBeTargeted.colors().contains(spellColor)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (effect instanceof CantBeTargetedBySpellColorsEffect cantBeTargeted
                    && cantBeTargeted.colors().contains(spellColor)) {
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
            if (effect instanceof CantBeTargetedByNonColorSourcesEffect restriction
                    && !sourceHasColor(sourceCard, restriction.allowedColor())) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (effect instanceof CantBeTargetedByNonColorSourcesEffect restriction
                    && !sourceHasColor(sourceCard, restriction.allowedColor())) {
                return true;
            }
        }
        return false;
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
     * {@link EnchantedCreatureSubtypeConditionalEffect} wrappers: if the currently active
     * inner effect (based on the creature's subtype) matches, returns {@code true}.
     */
    public boolean hasAuraWithEffect(GameData gameData, Permanent creature, Class<? extends CardEffect> effectClass) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && p.getAttachedTo().equals(creature.getId())
                        && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> isActiveEffect(gameData, creature, e, effectClass)));
    }

    private boolean isActiveEffect(GameData gameData, Permanent creature, CardEffect effect, Class<? extends CardEffect> effectClass) {
        if (effectClass.isInstance(effect)) return true;
        if (effect instanceof EnchantedCreatureSubtypeConditionalEffect cond) {
            boolean hasSubtype = permanentHasSubtype(creature, cond.subtype());
            CardEffect activeEffect = hasSubtype ? cond.ifMatch() : cond.ifNotMatch();
            return effectClass.isInstance(activeEffect);
        }
        return false;
    }

    /**
     * Checks whether a permanent has a given subtype without triggering {@code computeStaticBonus}
     * (which would cause infinite recursion). Checks base subtypes, transient subtypes,
     * granted subtypes, and the intrinsic Changeling keyword.
     */
    private static boolean permanentHasSubtype(Permanent permanent, CardSubtype subtype) {
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
                        && !isEquipped(gameData, creature));
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
                        .anyMatch(p -> matchesPermanentPredicate(gameData, p, restriction.defenderPermanentPredicate()));
                if (defenderMatches) {
                    return Optional.of(attacker.getCard().getName() + " can't be blocked");
                }
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
                    && !matchesPermanentPredicate(gameData, attacker, restriction.attackerPredicate())) {
                return Optional.of(blocker.getCard().getName() + " can only block " + restriction.allowedAttackersDescription());
            }
        }
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanBeBlockedOnlyByFilterEffect restriction
                    && !matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return Optional.of(attacker.getCard().getName() + " can only be blocked by " + restriction.allowedBlockersDescription());
            }
        }
        for (CanBeBlockedOnlyByFilterEffect restriction : getAuraGrantedBlockingRestrictions(gameData, attacker)) {
            if (!matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
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
     * Throws {@link IllegalStateException} if it does not.
     */
    public void validateTargetFilter(TargetFilter filter, Permanent target) {
        validateTargetFilter(filter, target, FilterContext.empty());
    }

    /**
     * Validates that the target permanent passes the given filter, using game data
     * for source-aware checks. Throws {@link IllegalStateException} if it does not.
     */
    public void validateTargetFilter(GameData gameData, TargetFilter filter, Permanent target) {
        validateTargetFilter(filter, target, FilterContext.of(gameData));
    }

    /**
     * Validates that the target permanent passes the given filter, using the provided
     * {@link FilterContext}. Throws {@link IllegalStateException} with the filter's
     * error message if it does not.
     */
    public void validateTargetFilter(TargetFilter filter,
                                     Permanent target,
                                     FilterContext filterContext) {
        if (!matchesSingleFilter(filter, target, filterContext)) {
            throw new IllegalStateException(getFilterErrorMessage(filter));
        }
    }

    private boolean matchesSingleFilter(TargetFilter filter, Permanent target, FilterContext filterContext) {
        GameData gameData = filterContext != null ? filterContext.gameData() : null;
        UUID sourceControllerId = filterContext != null ? filterContext.sourceControllerId() : null;

        if (filter instanceof ControlledPermanentPredicateTargetFilter controlledFilter) {
            if (sourceControllerId == null || gameData == null) return false;
            List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (controllerBattlefield == null || !controllerBattlefield.contains(target)) return false;
            return matchesPermanentPredicate(target, controlledFilter.predicate(), filterContext);
        }
        if (filter instanceof OwnedPermanentPredicateTargetFilter ownedFilter) {
            if (gameData == null || sourceControllerId == null) return false;
            boolean ownedByController = false;
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield != null && battlefield.contains(target)) {
                    UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                    ownedByController = ownerId.equals(sourceControllerId);
                    break;
                }
            }
            if (!ownedByController) return false;
            return matchesPermanentPredicate(target, ownedFilter.predicate(), filterContext);
        }
        if (filter instanceof PermanentPredicateTargetFilter f) {
            return matchesPermanentPredicate(target, f.predicate(), filterContext);
        }
        return true;
    }

    private String getFilterErrorMessage(TargetFilter filter) {
        if (filter instanceof ControlledPermanentPredicateTargetFilter f) return f.errorMessage();
        if (filter instanceof OwnedPermanentPredicateTargetFilter f) return f.errorMessage();
        if (filter instanceof PermanentPredicateTargetFilter f) return f.errorMessage();
        return "Target does not match filter";
    }

    // --- Other ---

    private boolean isCreatureSubtype(CardSubtype subtype) {
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
     * Applies both the global damage multiplier and per-controller spell damage multipliers
     * (e.g. {@link DoubleControllerSpellDamageEffect}) to the given damage amount.
     *
     * @param entry the stack entry representing the damage source; used to check if the source
     *              is an instant/sorcery spell of the appropriate color for controller-specific doubling
     * @return the damage after applying all multipliers
     */
    public int applyDamageMultiplier(GameData gameData, int damage, StackEntry entry) {
        return damage * getDamageMultiplier(gameData) * getControllerSpellDamageMultiplier(gameData, entry);
    }

    /**
     * Returns the per-controller spell damage multiplier based on {@link DoubleControllerSpellDamageEffect}
     * permanents on the battlefield. Only applies when the stack entry is an instant or sorcery spell
     * whose color matches the effect's required color and whose controller matches the permanent's controller.
     */
    int getControllerSpellDamageMultiplier(GameData gameData, StackEntry entry) {
        if (entry == null) return 1;
        StackEntryType type = entry.getEntryType();
        if (type != StackEntryType.INSTANT_SPELL && type != StackEntryType.SORCERY_SPELL) return 1;

        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(entry.getControllerId())) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof DoubleControllerSpellDamageEffect dcsde
                        && entry.getCard().getColors().contains(dcsde.color())) {
                    multiplier[0] *= 2;
                }
            }
        });
        return multiplier[0];
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
        int result = damage * getDamageMultiplier(gameData);
        result *= getEquippedCreatureCombatDamageMultiplier(gameData, source);
        if (target != null) {
            result *= getEquippedCreatureCombatDamageMultiplier(gameData, target);
        }
        return result;
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
                && !matchesPermanentPredicate(gameData, creature, gameData.combatDamageExemptPredicate)) {
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
}

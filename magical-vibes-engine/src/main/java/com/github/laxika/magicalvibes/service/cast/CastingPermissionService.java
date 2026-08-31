package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithIceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryByPayingLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AnyManaTypeCastEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastAdditionalNonartifactSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CardNameRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CastPermanentSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardPermission;
import com.github.laxika.magicalvibes.model.effect.ControllerCantPlayLandsEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCantCastSpellsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCantPlayLandsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DampingEngineEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemGrantsFlashbackEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentControllerCantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantMayhemToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalLandPlayRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LimitNonPhyrexianSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueAtMostEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueGreaterThanEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantPlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlotNonlandCardsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsAndLandsWithChosenNamesCantBePlayedEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastingTimingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.WardOfBonesEffect;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Single source of truth for whether a player may cast a given spell: per-turn spell limits,
 * type/name restrictions, silence-style prevention, timing (flash grants), and permissions to
 * cast/play from non-hand zones (graveyard, exile, top of library).
 *
 * <p>Both the view side ({@code GameActionAvailabilityService} playable-card computation) and the
 * validation side ({@code SpellCastingService}) must go through this service.
 */
@Component
public class CastingPermissionService {

    private static final Set<CardType> WARD_OF_BONES_SPELL_TYPES =
            Set.of(CardType.CREATURE, CardType.ARTIFACT, CardType.ENCHANTMENT);

    private static final Set<CardType> DAMPING_ENGINE_SPELL_TYPES = WARD_OF_BONES_SPELL_TYPES;

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final StaticEffectConditionResolver staticEffectConditionResolver;

    @Autowired
    public CastingPermissionService(GameQueryService gameQueryService,
                                    PredicateEvaluationService predicateEvaluationService,
                                    ConditionEvaluationService conditionEvaluationService,
                                    AmountEvaluationService amountEvaluationService,
                                    StaticEffectConditionResolver staticEffectConditionResolver) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.amountEvaluationService = amountEvaluationService;
        this.staticEffectConditionResolver = staticEffectConditionResolver;
    }

    public CastingPermissionService(GameQueryService gameQueryService,
                                    PredicateEvaluationService predicateEvaluationService,
                                    ConditionEvaluationService conditionEvaluationService) {
        this(gameQueryService, predicateEvaluationService, conditionEvaluationService,
                new AmountEvaluationService(predicateEvaluationService, gameQueryService),
                new StaticEffectConditionResolver(conditionEvaluationService));
    }

    /**
     * Returns true if the player is allowed to cast this spell considering non-mana
     * restrictions: spell limit, type restrictions, forbidden names, silence, etc.
     */
    public boolean isSpellCastingAllowed(GameData gameData, UUID playerId, Card card) {
        if (isSplitSecondActive(gameData)) return false;
        if (isSpellLimitReached(gameData, playerId, card)) return false;
        if (isPlayerPreventedFromCasting(gameData, playerId)) return false;
        if (isSpellTypeRestricted(gameData, playerId, card)) return false;
        Set<String> forbidden = getForbiddenCardNames(gameData, playerId);
        if (forbidden.contains(card.getName())) return false;
        if (isNoncreatureSpellCastRestricted(gameData, playerId, card)) return false;
        // Aurelia's Fury etc.: per-turn "can't cast noncreature spells" restriction on a player.
        if (!card.hasType(CardType.CREATURE)
                && gameData.playersCantCastNoncreatureSpellsThisTurn.contains(playerId)) return false;
        if (!card.hasType(CardType.CREATURE)
                && isNoncreatureSpellCastRestrictedUntilNextTurn(gameData, playerId)) return false;
        if (isOpponentsChosenColorSpellCastRestricted(gameData, playerId, card)) return false;
        if (isOpponentsSpellMatchingPredicateRestricted(gameData, playerId, card)) return false;
        if (isOpponentsManaValueSpellCastRestricted(gameData, playerId, card)) return false;
        if (isAdditionalNonartifactSpellRestricted(gameData, playerId, card)) return false;
        if (isSpellCastingRestrictedByMostRecentSpell(gameData, card)) return false;
        // MTG rule 714.1: legendary sorceries require controlling a legendary creature or planeswalker
        if (card.getSupertypes().contains(CardSupertype.LEGENDARY)
                && card.hasType(CardType.SORCERY)
                && !controlsLegendaryCreatureOrPlaneswalker(gameData, playerId)) return false;
        return true;
    }

    public int getMaxSpellsPerTurn(GameData gameData, UUID playerId) {
        int limit = gameData.playersMaxSpellsThisTurn.getOrDefault(playerId, Integer.MAX_VALUE);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof LimitSpellsPerTurnEffect spellLimit)) continue;
                    if (spellLimit.spellFilter() != null) continue;
                    boolean applies = switch (spellLimit.scope()) {
                        // Rule of Law etc.: applies to every player globally.
                        case EACH_PLAYER -> true;
                        // Colfenor's Plans etc.: only applies to the permanent's controller.
                        case CONTROLLER -> pid.equals(playerId);
                        // Curse of Exhaustion etc.: only applies to the enchanted player.
                        case ENCHANTED_PLAYER -> perm.isAttached() && playerId.equals(perm.getAttachedTo());
                    };
                    if (applies) {
                        limit = Math.min(limit, spellLimit.maxSpells());
                    }
                }
            }
        }
        return limit;
    }

    /**
     * Returns whether a static spell limit already has enough matching spells cast this turn.
     * Unfiltered limits count every spell; filtered limits count only matching spells.
     */
    public boolean isSpellLimitReached(GameData gameData, UUID playerId, Card card) {
        Integer maxSpells = gameData.playersMaxSpellsThisTurn.get(playerId);
        if (maxSpells != null && gameData.getSpellsCastThisTurnCount(playerId) >= maxSpells) {
            return true;
        }

        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof LimitSpellsPerTurnEffect spellLimit)) continue;
                    boolean applies = switch (spellLimit.scope()) {
                        case EACH_PLAYER -> true;
                        case CONTROLLER -> pid.equals(playerId);
                        case ENCHANTED_PLAYER -> perm.isAttached() && playerId.equals(perm.getAttachedTo());
                    };
                    if (!applies) continue;

                    CardPredicate filter = spellLimit.spellFilter();
                    UUID sourceCardId = perm.getCard().getId();
                    if (filter != null
                            && !predicateEvaluationService.matchesCardPredicate(
                            card, filter, sourceCardId, gameData, playerId)) {
                        continue;
                    }
                    long matchingSpells = gameData.getSpellsCastThisTurn(playerId).stream()
                            .filter(cast -> filter == null
                                    || predicateEvaluationService.matchesCardPredicate(
                                    cast, filter, sourceCardId, gameData, playerId))
                            .count();
                    if (matchingSpells >= spellLimit.maxSpells()) return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the player is prevented from casting spells (e.g. Angelic Arbiter:
     * "Each opponent who attacked with a creature this turn can't cast spells").
     */
    public boolean isPlayerPreventedFromCasting(GameData gameData, UUID playerId) {
        if (gameData.playersCantCastSpellsForRestOfGame.contains(playerId)) return true;
        if (gameData.playersSilencedThisTurn.contains(playerId)) return true;

        // Grand Abolisher: during its controller's turn their opponents can't cast spells.
        if (gameQueryService.isLockedOutByOpponentsTurnRestriction(gameData, playerId)) return true;

        // City of Solitude: players can cast spells only during their own turns.
        if (gameQueryService.isLockedOutByOwnTurnOnlyRestriction(gameData, playerId)) return true;

        // Dosan the Falling Leaf: players can cast spells only during their own turns.
        if (gameQueryService.isLockedOutByOwnTurnOnlySpellRestriction(gameData, playerId)) return true;

        boolean playerAttackedThisTurn = gameData.playersDeclaredAttackersThisTurn.contains(playerId);
        if (!playerAttackedThisTurn) return false;

        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsCantCastSpellsIfAttackedThisTurnEffect restriction
                            && (!restriction.onlyIfAttackedControllerOrPlaneswalker()
                            || gameData.playersWhoAttackedPlayerOrPlaneswalkerThisTurn
                            .getOrDefault(pid, Set.of()).contains(playerId))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Set<CardType> getRestrictedSpellTypes(GameData gameData, UUID playerId) {
        Set<CardType> restricted = EnumSet.noneOf(CardType.class);
        // Moonhold / Abeyance etc.: per-turn "can't cast spells of these types" restriction on a player.
        restricted.addAll(gameData.playersCantCastSpellTypesThisTurn.getOrDefault(playerId, Set.of()));
        gameData.playersCantCastSpellTypesUntilEndOfControllerNextTurn.values()
                .forEach(types -> restricted.addAll(types.keySet()));
        // Hand to Hand: during combat no player can cast instant spells.
        if (gameQueryService.isCombatActionLockActive(gameData)) {
            restricted.add(CardType.INSTANT);
        }
        // Controller-only restrictions (Steel Golem) come from the player's own permanents;
        // symmetric restrictions (Aether Storm) apply no matter whose battlefield they sit on.
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CantCastSpellTypeEffect cantCast
                            && (cantCast.appliesToAllPlayers() || pid.equals(playerId))) {
                        restricted.addAll(cantCast.restrictedTypes());
                    }
                    // Brand of Ill Omen etc.: the enchanted permanent's controller is restricted,
                    // which may be a different player than the Aura's controller.
                    if (effect instanceof EnchantedPermanentControllerCantCastSpellTypeEffect enchantedCantCast
                            && perm.isAttached()
                            && playerId.equals(gameQueryService.findPermanentController(gameData, perm.getAttachedTo()))) {
                        restricted.addAll(enchantedCantCast.restrictedTypes());
                    }
                }
            }
        }
        addWardOfBonesRestrictedTypes(gameData, playerId, restricted);
        addDampingEngineRestrictedTypes(gameData, playerId, restricted);
        return restricted;
    }

    private void addDampingEngineRestrictedTypes(GameData gameData, UUID playerId, Set<CardType> restricted) {
        if (!controlsMorePermanentsThanEachOtherPlayer(gameData, playerId)) return;
        if (hasActiveDampingEngine(gameData)) {
            restricted.addAll(DAMPING_ENGINE_SPELL_TYPES);
        }
    }

    /**
     * Ward of Bones (EVE): each opponent who controls more creatures/artifacts/enchantments than the
     * source's controller can't cast spells of that type. Each type is compared independently, and
     * the source's own controller is never restricted ("Each opponent…").
     */
    private void addWardOfBonesRestrictedTypes(GameData gameData, UUID playerId, Set<CardType> restricted) {
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(playerId) || !controlsWardOfBones(gameData, controllerId)) continue;
            for (CardType type : WARD_OF_BONES_SPELL_TYPES) {
                if (countControlledOfType(gameData, playerId, type)
                        > countControlledOfType(gameData, controllerId, type)) {
                    restricted.add(type);
                }
            }
        }
    }

    /**
     * Returns true if a static effect stops this player from playing lands: their own
     * {@link ControllerCantPlayLandsEffect} permanent (Aggressive Mining), a global permanent-count
     * restriction (Limited Resources), or an opponent's Ward of Bones (EVE) while this player
     * controls more lands than that opponent.
     */
    public boolean isLandPlayRestricted(GameData gameData, UUID playerId) {
        if (controlsStatic(gameData, playerId, ControllerCantPlayLandsEffect.class)) return true;
        if (hasActiveGlobalLandPlayRestriction(gameData)) return true;
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(playerId) || !controlsWardOfBones(gameData, controllerId)) continue;
            if (countControlledOfType(gameData, playerId, CardType.LAND)
                    > countControlledOfType(gameData, controllerId, CardType.LAND)) {
                return true;
            }
        }
        if (controlsMorePermanentsThanEachOtherPlayer(gameData, playerId) && hasActiveDampingEngine(gameData)) {
            return true;
        }
        return false;
    }

    public boolean isLandPlayFromHandRestricted(GameData gameData, UUID playerId) {
        return controlsStatic(gameData, playerId, ControllerCantPlayLandsFromHandEffect.class);
    }

    public boolean isSpellCastingFromHandRestricted(GameData gameData, UUID playerId) {
        return controlsStatic(gameData, playerId, ControllerCantCastSpellsFromHandEffect.class);
    }

    private boolean hasActiveDampingEngine(GameData gameData) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.isDampingEngineEffectIgnoredThisTurn()) continue;
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(DampingEngineEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean controlsMorePermanentsThanEachOtherPlayer(GameData gameData, UUID playerId) {
        int count = gameData.playerBattlefields.getOrDefault(playerId, List.of()).size();
        for (UUID otherPlayerId : gameData.orderedPlayerIds) {
            if (!otherPlayerId.equals(playerId)
                    && count <= gameData.playerBattlefields.getOrDefault(otherPlayerId, List.of()).size()) {
                return false;
            }
        }
        return true;
    }

    public boolean isSpellTypeRestricted(GameData gameData, UUID playerId, Card card) {
        Set<CardType> restricted = getRestrictedSpellTypes(gameData, playerId);
        return restricted.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(restricted::contains);
    }

    private boolean hasActiveGlobalLandPlayRestriction(GameData gameData) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof GlobalLandPlayRestrictionEffect restriction
                            && countMatchingPermanents(gameData, restriction.filter())
                            >= restriction.minimumCount()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private long countMatchingPermanents(GameData gameData, PermanentPredicate filter) {
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, filter))
                .count();
    }

    private boolean controlsWardOfBones(GameData gameData, UUID playerId) {
        return controlsStatic(gameData, playerId, WardOfBonesEffect.class);
    }

    private boolean controlsStatic(GameData gameData, UUID playerId, Class<? extends CardEffect> effectType) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        return bf.stream().anyMatch(perm -> perm.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(effectType::isInstance));
    }

    private long countControlledOfType(GameData gameData, UUID playerId, CardType type) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return 0;
        return bf.stream().filter(perm -> switch (type) {
            case CREATURE -> gameQueryService.isCreature(gameData, perm);
            case ARTIFACT -> gameQueryService.isArtifact(gameData, perm);
            case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, perm);
            default -> perm.getCard().hasType(type);
        }).count();
    }

    /**
     * Returns true if an active noncreature-spell restriction on the battlefield prevents the given
     * player from casting this card. Controller-only restrictions apply only to their source's
     * controller; the two-argument overload retains the legacy global-only query semantics.
     */
    public boolean isNoncreatureSpellCastRestricted(GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.CREATURE)) return false;
        int manaValue = card.getManaValue();
        boolean hasX = card.getParsedManaCost() != null && card.getParsedManaCost().hasX();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof NoncreatureSpellsCantBeCastEffect restriction)
                            || (!restriction.appliesToAllPlayers() && !pid.equals(playerId))) continue;
                    if (manaValue >= restriction.minManaValue()) return true;
                    if (restriction.restrictXSpells() && hasX) return true;
                }
            }
        }
        return false;
    }

    public boolean isNoncreatureSpellCastRestricted(GameData gameData, Card card) {
        return isNoncreatureSpellCastRestricted(gameData, null, card);
    }

    /**
     * Brisela, Voice of Nightmares: opponents of a source controller can't cast spells with mana
     * value ≤ the effect's {@code maxManaValue}. Playability overload (no chosen X): X-cost spells
     * are never blocked because a high enough X is always legal. Cast-time overload includes the
     * chosen X in mana value.
     */
    public boolean isOpponentsManaValueSpellCastRestricted(GameData gameData, UUID castingPlayerId, Card card) {
        return isOpponentsManaValueSpellCastRestricted(gameData, castingPlayerId, card, null);
    }

    public boolean isOpponentsManaValueSpellCastRestricted(GameData gameData, UUID castingPlayerId, Card card,
                                                           Integer chosenX) {
        ManaCost cost = card.getParsedManaCost();
        boolean hasX = cost != null && cost.hasX();
        int manaValue = card.getManaValue();
        if (chosenX != null && hasX) {
            manaValue += chosenX * Math.max(1, cost.getXSymbolCount());
        }

        int tightestMax = Integer.MAX_VALUE;
        boolean restricted = false;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(castingPlayerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsCantCastSpellsWithManaValueGreaterThanEffect restriction
                            && (restriction.spellFilter() == null
                            || predicateEvaluationService.matchesCardPredicate(card,
                            restriction.spellFilter(), null, gameData, castingPlayerId))) {
                        int limit = amountEvaluationService.evaluate(gameData,
                                restriction.manaValueLimit(),
                                new AmountContext(castingPlayerId, perm, castingPlayerId, 0, 0));
                        if (manaValue > limit) return true;
                    }
                    if (effect instanceof OpponentsCantCastSpellsWithManaValueAtMostEffect restriction) {
                        restricted = true;
                        tightestMax = Math.min(tightestMax, restriction.maxManaValue());
                    }
                }
            }
        }
        if (!restricted) return false;

        if (chosenX == null) {
            // Playability: any {X} spell can pick X high enough to exceed the cap.
            if (hasX) return false;
            return card.getManaValue() <= tightestMax;
        }
        return manaValue <= tightestMax;
    }

    /**
     * Iona, Shield of Emeria: opponents of a source permanent's controller can't cast spells of
     * that permanent's chosen color. Colorless spells and permanents without a chosen color are
     * unaffected.
     */
    public boolean isOpponentsChosenColorSpellCastRestricted(GameData gameData, UUID castingPlayerId, Card card) {
        if (card.getColors() == null || card.getColors().isEmpty()) return false;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(castingPlayerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getChosenColor() != null
                        && card.getColors().contains(perm.getChosenColor())
                        && perm.getCard().getEffects(EffectSlot.STATIC).stream()
                                .anyMatch(OpponentsCantCastSpellsOfChosenColorEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Static restrictions such as Llawan's apply to spells matching a card predicate, but only
     * when the caster is an opponent of the permanent's controller.
     */
    public boolean isOpponentsSpellMatchingPredicateRestricted(GameData gameData, UUID castingPlayerId,
                                                                 Card card) {
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(castingPlayerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsCantCastSpellsMatchingPredicateEffect restriction
                            && predicateEvaluationService.matchesCardPredicate(
                            card, restriction.predicate(), perm.getCard().getId(), gameData, castingPlayerId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Ethersworn Canonist: "Each player who has cast a nonartifact spell this turn can't cast
     * additional nonartifact spells." Returns true if {@code card} is a nonartifact spell, some
     * permanent carries the effect, and {@code playerId} has already cast a nonartifact spell this
     * turn. Artifact spells and each player's first nonartifact spell are never restricted; symmetric.
     */
    public boolean isAdditionalNonartifactSpellRestricted(GameData gameData, UUID playerId, Card card) {
        if (gameQueryService.cardHasType(card, CardType.ARTIFACT, gameData, playerId)) return false;
        if (!anyPlayerControlsEtherswornCanonist(gameData)) return false;
        return gameData.getSpellsCastThisTurn(playerId).stream()
                .anyMatch(cast -> !gameQueryService.cardHasType(cast, CardType.ARTIFACT, gameData, playerId));
    }

    public boolean isAdditionalNonPhyrexianSpellRestricted(GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.LAND)) return false;
        if (gameQueryService.cardHasSubtype(card, CardSubtype.PHYREXIAN, gameData, playerId)) return false;
        if (!anyPlayerControlsPhyrexianCensor(gameData)) return false;
        return gameData.getSpellsCastThisTurn(playerId).stream()
                .anyMatch(cast -> !gameQueryService.cardHasSubtype(
                        cast, CardSubtype.PHYREXIAN, gameData, playerId));
    }

    private boolean anyPlayerControlsPhyrexianCensor(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(LimitNonPhyrexianSpellsPerTurnEffect.class::isInstance)) return true;
            }
        }
        return false;
    }

    public boolean isSpellCastingRestrictedByMostRecentSpell(GameData gameData, Card card) {
        Card mostRecentSpell = gameData.getMostRecentSpellCastThisTurn();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof SpellCastingRestrictionEffect restriction
                            && restriction.preventsCasting(perm, mostRecentSpell, card)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean anyPlayerControlsEtherswornCanonist(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CantCastAdditionalNonartifactSpellsEffect) return true;
                }
            }
        }
        return false;
    }

    public Set<String> getForbiddenCardNames(GameData gameData, UUID castingPlayerId) {
        Set<String> forbidden = new HashSet<>();
        Set<String> nontokenPermanentNames = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CantCastSpellsWithSameNameAsExiledCardEffect cantCast) {
                        // If opponentsOnly, skip if the casting player is the controller
                        if (cantCast.opponentsOnly() && pid.equals(castingPlayerId)) {
                            continue;
                        }
                        Card imprinted = gameData.getImprintedCard(perm.getCard());
                        if (imprinted != null) {
                            forbidden.add(imprinted.getName());
                        }
                        gameData.getCardsExiledByPermanent(perm.getId()).stream()
                                .map(Card::getName)
                                .forEach(forbidden::add);
                    }
                    if (effect instanceof SpellsWithChosenNameCantBeCastEffect chosenNameCast) {
                        // Gideon's Intervention restricts only opponents; the controller may still cast.
                        if (chosenNameCast.opponentsOnly() && pid.equals(castingPlayerId)) {
                            continue;
                        }
                        String chosenName = perm.getChosenName();
                        if (chosenName != null) {
                            forbidden.add(chosenName);
                        }
                    }
                    if (effect instanceof SpellsAndLandsWithChosenNamesCantBePlayedEffect) {
                        // Null Chamber: symmetric — both names are forbidden to every player.
                        if (perm.getChosenName() != null) {
                            forbidden.add(perm.getChosenName());
                        }
                        if (perm.getSecondChosenName() != null) {
                            forbidden.add(perm.getSecondChosenName());
                        }
                    }
                    if (effect instanceof CardNameRestrictionEffect restriction) {
                        if (nontokenPermanentNames == null) {
                            nontokenPermanentNames = getNontokenPermanentNames(gameData);
                        }
                        forbidden.addAll(restriction.forbiddenSpellNames(nontokenPermanentNames));
                    }
                }
            }
        }
        // Comply: until the namer's next turn, their opponents can't cast spells with the chosen name(s).
        for (var e : gameData.opponentsCantCastNamedSpellsUntilControllerNextTurn.entrySet()) {
            if (e.getKey().equals(castingPlayerId)) {
                continue;
            }
            forbidden.addAll(e.getValue());
        }
        gameData.spellsAndLandsWithChosenNameCantBePlayedUntilControllerNextTurn.values()
                .forEach(forbidden::addAll);
        return forbidden;
    }

    /**
     * Null Chamber: "lands with the chosen names can't be played". Land plays deliberately bypass the
     * spell-casting filters (they aren't spells), so the name check has its own entry point here.
     */
    public boolean isLandPlayForbiddenByChosenName(GameData gameData, Card card) {
        Set<String> nontokenPermanentNames = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof SpellsAndLandsWithChosenNamesCantBePlayedEffect
                            && (card.getName().equals(perm.getChosenName())
                                || card.getName().equals(perm.getSecondChosenName()))) {
                        return true;
                    }
                    if (effect instanceof CardNameRestrictionEffect restriction
                            && !card.getSupertypes().contains(CardSupertype.BASIC)) {
                        if (nontokenPermanentNames == null) {
                            nontokenPermanentNames = getNontokenPermanentNames(gameData);
                        }
                        if (restriction.forbiddenNonbasicLandNames(nontokenPermanentNames).contains(card.getName())) {
                            return true;
                        }
                    }
                }
            }
        }
        if (gameData.spellsAndLandsWithChosenNameCantBePlayedUntilControllerNextTurn.values().stream()
                .anyMatch(names -> names.contains(card.getName()))) {
            return true;
        }
        return false;
    }

    private Set<String> getNontokenPermanentNames(GameData gameData) {
        Set<String> names = new HashSet<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (!permanent.getCard().isToken()) {
                    names.add(gameQueryService.getEffectiveName(gameData, permanent));
                }
            }
        }
        return names;
    }

    /**
     * As {@link #isSpellRestricted(Card, Set, Set)}, plus the per-turn "this player can't cast
     * noncreature spells" lock (Aurelia's Fury), which can't be expressed as a restricted type set.
     */
    public boolean isSpellRestricted(GameData gameData, UUID playerId, Card card,
                                     Set<CardType> restrictedSpellTypes, Set<String> forbiddenCardNames) {
        if (isSplitSecondActive(gameData)) return true;
        if (isSpellCastingRestrictedByMostRecentSpell(gameData, card)) return true;
        if (!card.hasType(CardType.CREATURE)
                && gameData.playersCantCastNoncreatureSpellsThisTurn.contains(playerId)) return true;
        if (!card.hasType(CardType.CREATURE)
                && isNoncreatureSpellCastRestrictedUntilNextTurn(gameData, playerId)) return true;
        if (isOpponentsChosenColorSpellCastRestricted(gameData, playerId, card)) return true;
        if (isOpponentsSpellMatchingPredicateRestricted(gameData, playerId, card)) return true;
        return isSpellRestricted(card, restrictedSpellTypes, forbiddenCardNames);
    }

    public boolean isSplitSecondActive(GameData gameData) {
        return gameData.stack.stream()
                .anyMatch(entry -> entry.getCard() != null
                        && entry.getCard().getKeywords().contains(Keyword.SPLIT_SECOND));
    }

    public boolean isSpellRestricted(Card card, Set<CardType> restrictedSpellTypes, Set<String> forbiddenCardNames) {
        if (restrictedSpellTypes.contains(card.getType())) return true;
        for (CardType type : card.getAdditionalTypes()) {
            if (restrictedSpellTypes.contains(type)) return true;
        }
        return forbiddenCardNames.contains(card.getName());
    }

    private boolean isNoncreatureSpellCastRestrictedUntilNextTurn(GameData gameData, UUID playerId) {
        return gameData.playersCantCastNoncreatureSpellsUntilControllerNextTurn.values().stream()
                .anyMatch(targetPlayers -> targetPlayers.contains(playerId));
    }

    public boolean controlsLegendaryCreatureOrPlaneswalker(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, perm);
            boolean isLegendary = perm.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)
                    || bonus.grantedSupertypes().contains(CardSupertype.LEGENDARY);
            if (isLegendary) {
                if (gameQueryService.isCreature(gameData, perm)
                        || perm.getCard().hasType(CardType.PLANESWALKER)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canCastWithTiming(GameData gameData, UUID playerId, Card card,
                                     boolean isActivePlayer, boolean isMainPhase, boolean stackEmpty) {
        if (isSorcerySpeedOnlyForPlayer(gameData, playerId)) {
            return sorceryTimingAvailable(gameData, playerId);
        }
        boolean sorceryTiming = isActivePlayer && isMainPhase && stackEmpty;
        if (gameQueryService.isLockedOutByOpponentsSorceryTimingRestriction(gameData, playerId)) {
            return sorceryTiming;
        }

        boolean isInstantSpeed = card.hasType(CardType.INSTANT)
                || card.getKeywords().contains(Keyword.FLASH)
                || hasFlashGrantForCard(gameData, playerId, card)
                || grantsItselfFlashTiming(card)
                || hasMetFlashCastCondition(gameData, playerId, card)
                || hasAvailableFlashAlternateCast(gameData, playerId, card);
        return isInstantSpeed || sorceryTiming;
    }

    private boolean isSorcerySpeedOnlyForPlayer(GameData gameData, UUID playerId) {
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.isFaceDown()) continue;
                GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
                if (permanent.isLosesAllAbilitiesUntilEndOfTurn()
                        || staticBonus.losesAllAbilities()
                        || staticBonus.losesAllNonManaAbilities()) {
                    continue;
                }
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof SpellCastingTimingRestrictionEffect restriction
                            && restriction.appliesTo(sourceControllerId, playerId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * True when the only thing letting {@code card} be cast right now is a flash-granting
     * {@link AlternateHandCast}. The flash timing belongs to that alternate cast, so a cast that
     * pays the normal mana cost instead is illegal at this moment (Harbinger of the Tides only has
     * flash if the {2} surcharge is paid). {@link #canCastWithTiming} stays permissive — the card
     * really is castable, just not for its normal cost — and the hand-cast path rejects the
     * normal-cost cast with this check.
     */
    public boolean flashTimingRequiresAlternateCast(GameData gameData, UUID playerId, Card card) {
        if (sorceryTimingAvailable(gameData, playerId)) return false;
        if (!hasAvailableFlashAlternateCast(gameData, playerId, card)) return false;
        return !card.hasType(CardType.INSTANT)
                && !card.getKeywords().contains(Keyword.FLASH)
                && !hasFlashGrantForCard(gameData, playerId, card)
                && !grantsItselfFlashTiming(card)
                && !hasMetFlashCastCondition(gameData, playerId, card);
    }

    /**
     * Returns true when the card's own conditional flash permission is the permission being used
     * outside normal sorcery timing. An additional cost tied to that permission must be paid before
     * the cast proceeds.
     */
    public boolean isUsingCardFlashPermission(GameData gameData, UUID playerId, Card card) {
        if (sorceryTimingAvailable(gameData, playerId) || !hasMetFlashCastCondition(gameData, playerId, card)) {
            return false;
        }
        return !card.hasType(CardType.INSTANT)
                && !card.getKeywords().contains(Keyword.FLASH)
                && !hasFlashGrantForCard(gameData, playerId, card)
                && !grantsItselfFlashTiming(card)
                && !hasAvailableFlashAlternateCast(gameData, playerId, card);
    }

    /**
     * True if the card itself says "you may cast this spell as though it had flash" — the Mirage
     * flash clause. Unlike a battlefield flash grant this needs no permission source; the trade-off
     * (sacrifice at the next cleanup step) is applied when the permanent enters.
     */
    private boolean grantsItselfFlashTiming(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(FlashCastWithCleanupSacrificeEffect.class::isInstance);
    }

    /**
     * True if the card carries a condition-gated "you may cast this spell as though it had flash"
     * clause whose condition is currently met for the caster (e.g. Swift Reckoning's spell mastery).
     * The normal cost still applies — only the timing permission changes.
     */
    private boolean hasMetFlashCastCondition(GameData gameData, UUID playerId, Card card) {
        Condition condition = card.getFlashCastCondition();
        return condition != null
                && conditionEvaluationService.isMet(gameData, condition, ConditionContext.forCasting(playerId));
    }

    /**
     * Whether the player could cast a sorcery right now — active player, a main phase, empty stack.
     * Read at cast time by the Mirage flash clause to decide whether the resulting permanent must be
     * sacrificed at the next cleanup step.
     */
    public boolean sorceryTimingAvailable(GameData gameData, UUID playerId) {
        return playerId.equals(gameData.activePlayerId)
                && (gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                        || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN)
                && gameData.stack.isEmpty();
    }

    /**
     * True if the card carries an {@link AlternateHandCast} that grants flash and whose availability
     * condition is currently met (e.g. Qasali Ambusher's "you may cast this creature … as though it
     * had flash" while a creature is attacking you and you control a Forest and a Plains). This makes
     * the free flash cast castable any time the player has priority.
     */
    private boolean hasAvailableFlashAlternateCast(GameData gameData, UUID playerId, Card card) {
        AlternateHandCast altCast = card.getCastingOption(AlternateHandCast.class).orElse(null);
        if (altCast == null || !altCast.grantsFlash()) return false;
        return altCast.availabilityCondition() == null
                || conditionEvaluationService.isMet(gameData, altCast.availabilityCondition(),
                        ConditionContext.forCasting(playerId));
    }

    /**
     * Returns true if the card's card-specific spell cast timing restriction (if any) is currently
     * satisfied for the caster. Cards without such a restriction always pass. Defiant Stand.
     */
    public boolean canCastWithSpellTimingRestriction(GameData gameData, UUID playerId, Card card) {
        SpellCastTimingRestriction restriction = card.getSpellCastTimingRestriction();
        if (restriction == null) return true;
        return switch (restriction) {
            case DECLARE_ATTACKERS -> gameData.currentStep == TurnStep.DECLARE_ATTACKERS;
            case BEFORE_ATTACKERS_DECLARED -> gameData.currentStep.isBeforeAttackersDeclared()
                    && gameData.combatPhasesThisTurn <= 1;
            case DECLARE_ATTACKERS_IF_ATTACKED ->
                    gameData.currentStep == TurnStep.DECLARE_ATTACKERS
                            && gameQueryService.isPlayerBeingAttacked(gameData, playerId);
            case YOUR_END_STEP ->
                    gameData.currentStep == TurnStep.END_STEP
                            && playerId.equals(gameData.activePlayerId);
            case COMBAT_BEFORE_BLOCKERS ->
                    gameData.currentStep.isCombatPhase()
                            && gameData.currentStep.ordinal() < TurnStep.DECLARE_BLOCKERS.ordinal();
            case COMBAT -> gameData.currentStep.isCombatPhase();
            case YOUR_COMBAT ->
                    playerId.equals(gameData.activePlayerId) && gameData.currentStep.isCombatPhase();
            case YOUR_COMBAT_BEFORE_BLOCKERS ->
                    playerId.equals(gameData.activePlayerId)
                            && gameData.currentStep.isCombatPhase()
                            && gameData.currentStep.ordinal() < TurnStep.DECLARE_BLOCKERS.ordinal();
            case COMBAT_AFTER_BLOCKERS ->
                    gameData.currentStep.isCombatPhase()
                            && gameData.currentStep.ordinal() >= TurnStep.DECLARE_BLOCKERS.ordinal();
            case ONLY_DURING_COMBAT -> gameData.currentStep.isCombatPhase();
            case DECLARE_BLOCKERS ->
                    gameData.currentStep == TurnStep.DECLARE_BLOCKERS;
            case OPPONENTS_TURN_BEFORE_ATTACKERS ->
                    !playerId.equals(gameData.activePlayerId)
                            && gameData.currentStep.isBeforeAttackersDeclared();
            case OPPONENTS_TURN -> !playerId.equals(gameData.activePlayerId);
            case OPPONENTS_UPKEEP ->
                    gameData.currentStep == TurnStep.UPKEEP
                            && !playerId.equals(gameData.activePlayerId);
            case BEFORE_COMBAT_DAMAGE -> gameData.currentStep.isBeforeCombatDamage();
            case AFTER_COMBAT ->
                    gameData.currentStep.ordinal() > TurnStep.END_OF_COMBAT.ordinal();
        };
    }

    /**
     * Returns true if the card's card-specific "cast this spell only if …" condition (if any) is
     * currently satisfied for the caster. Cards without such a condition always pass. Talara's
     * Battalion ("only if you've cast another green spell this turn").
     */
    public boolean canCastWithCastCondition(GameData gameData, UUID playerId, Card card) {
        Condition condition = card.getCastCondition();
        if (condition == null) return true;
        return conditionEvaluationService.isMet(gameData, condition, ConditionContext.forCasting(playerId));
    }

    public boolean canUseFlashback(GameData gameData, UUID playerId, FlashbackCast flashbackCast) {
        Condition condition = flashbackCast.availabilityCondition();
        return condition == null
                || conditionEvaluationService.isMet(gameData, condition, ConditionContext.forCasting(playerId));
    }

    private boolean hasFlashGrantForCard(GameData gameData, UUID playerId, Card card) {
        if (gameData.playersWithFlashUntilEndOfTurn.contains(playerId)) return true;
        if (gameData.hasCardTypeFlashGrant(playerId, card)) return true;
        if (gameData.hasCardTypeFlashGrantUntilNextTurn(playerId, card)) return true;
        if (gameData.cardTypeFlashGrantsThisTurn.getOrDefault(playerId, Set.of()).stream()
                .anyMatch(filter -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, playerId))) {
            return true;
        }
        if (gameData.cardTypeFlashGrantsUntilNextTurn.getOrDefault(playerId, Set.of()).stream()
                .anyMatch(filter -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, playerId))) {
            return true;
        }
        // Quicken: an unconsumed grant for the next spell of a given type this turn.
        if (gameData.hasNextSpellFlashGrant(playerId, card)) return true;
        for (UUID ownerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(ownerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof GrantFlashToCardTypeEffect grant
                            && (grant.appliesToAllPlayers() || ownerId.equals(playerId))
                            && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canPlayLandsFromGraveyard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        for (Permanent perm : battlefield == null ? List.<Permanent>of() : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                if (resolved instanceof PlayLandsFromGraveyardEffect) {
                    return true;
                }
            }
        }
        return gameData.emblems.stream()
                .filter(emblem -> emblem.controllerId().equals(playerId))
                .flatMap(emblem -> emblem.staticEffects().stream())
                .anyMatch(PlayLandsFromGraveyardEffect.class::isInstance);
    }

    public boolean isLandPlayFromGraveyardRestricted(GameData gameData, UUID playerId) {
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(playerId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.isLosesAllAbilitiesUntilEndOfTurn()
                        || gameQueryService.computeStaticBonus(gameData, permanent).losesAllAbilities()) {
                    continue;
                }
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(OpponentsCantPlayLandsFromGraveyardEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canPlayLandsFromTopOfLibrary(GameData gameData, UUID playerId) {
        if (gameData.playersAllowedToPlayFromLibraryTopUntilEndOfTurn.contains(playerId)
                || gameData.libraryTopCardLifePlayPermissionsUntilEndOfTurn.contains(playerId)) {
            return true;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                if (resolved instanceof PlayLandsFromTopOfLibraryEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true when the controller has permission to take the plot special action for a
     * nonland card on top of their library.
     */
    public boolean canPlotNonlandCardsFromTopOfLibrary(GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.LAND)) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PlotNonlandCardsFromTopOfLibraryEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canPlayLandNow(GameData gameData, UUID playerId, Card card) {
        return card.hasType(CardType.LAND)
                && playerId.equals(gameData.activePlayerId)
                && (gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN)
                && gameData.landsPlayedThisTurn.getOrDefault(playerId, 0) < gameData.getMaxLandsThisTurn(playerId)
                && gameData.stack.isEmpty()
                && !gameData.playersCantPlayLandsThisTurn.contains(playerId)
                && !isLandPlayRestricted(gameData, playerId)
                && !isLandPlayForbiddenByChosenName(gameData, card);
    }

    public boolean canPlayLandFromTopOfLibrary(GameData gameData, UUID playerId, Card card) {
        return canPlayLandsFromTopOfLibrary(gameData, playerId)
                && canPlayLandNow(gameData, playerId, card);
    }

    /**
     * Returns true if the player controls a permanent granting permission to cast this card from
     * their graveyard as a spell matching a {@link CastSpellsFromGraveyardPermission} filter
     * (Abandoned Sarcophagus). Lands that are not also another permanent type are not spells.
     */
    public boolean canCastViaFilteredGraveyardPermission(GameData gameData, UUID playerId, Card card) {
        return findFilteredGraveyardPermissionSource(gameData, playerId, card).isPresent();
    }

    /**
     * Returns the permanent granting this player permission to cast {@code card} from their graveyard
     * via a {@link CastSpellsFromGraveyardPermission}, or empty if none applies. A once-per-your-turn
     * permission (Gisa and Geralf) only applies during its controller's own turn and only while that
     * permanent's use for the turn is unspent; the returned id keys that per-instance tracking.
     */
    public Optional<UUID> findFilteredGraveyardPermissionSource(GameData gameData, UUID playerId, Card card) {
        return findFilteredGraveyardPermission(gameData, playerId, card).map(permission -> permission.sourcePermanentId());
    }

    /** Returns the matching graveyard-cast permission, including its additional cast costs. */
    public Optional<FilteredGraveyardPermission> findFilteredGraveyardPermission(
            GameData gameData, UUID playerId, Card card) {
        if (!isCastableSpellCard(card)) {
            return Optional.empty();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return Optional.empty();
        }
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                if (!(resolved instanceof CastSpellsFromGraveyardPermission permission)
                        || !predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null)) {
                    continue;
                }
                if (permission.oncePerControllerTurn()
                        && (!playerId.equals(gameData.activePlayerId)
                            || gameData.oncePerTurnGraveyardCastPermissionsUsedThisTurn.contains(perm.getId()))) {
                    continue;
                }
                return Optional.of(new FilteredGraveyardPermission(perm.getId(), permission));
            }
        }
        return Optional.empty();
    }

    public record FilteredGraveyardPermission(UUID sourcePermanentId,
                                              CastSpellsFromGraveyardPermission permission) {
    }

    /**
     * Marks a once-per-your-turn graveyard cast permission as spent for this turn. No-op for
     * unlimited permissions (Abandoned Sarcophagus), which are not tracked.
     */
    public void markFilteredGraveyardPermissionUsed(GameData gameData, UUID playerId, UUID permanentId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;
        battlefield.stream()
                .filter(perm -> perm.getId().equals(permanentId))
                .findFirst()
                .ifPresent(perm -> {
                    boolean oncePerTurn = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                            .map(effect -> staticEffectConditionResolver.resolve(gameData, perm, playerId, effect))
                            .anyMatch(effect -> effect instanceof CastSpellsFromGraveyardPermission permission
                                    && permission.oncePerControllerTurn());
                    if (oncePerTurn) {
                        gameData.oncePerTurnGraveyardCastPermissionsUsedThisTurn.add(permanentId);
                    }
                });
    }

    private static boolean isCastableSpellCard(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        CardType primary = card.getType();
        if (primary.isPermanentType() && primary != CardType.LAND) {
            return true;
        }
        for (CardType additional : card.getAdditionalTypes()) {
            if (additional.isPermanentType() && additional != CardType.LAND) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the permanent ID of the first permanent the player controls that has
     * CastPermanentSpellsFromGraveyardEffect, or empty if none.
     * The returned UUID is used to key per-instance graveyard cast tracking.
     */
    public Optional<UUID> findGraveyardCastSourcePermanentId(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return Optional.empty();
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CastPermanentSpellsFromGraveyardEffect) {
                    return Optional.of(perm.getId());
                }
            }
        }
        return Optional.empty();
    }

    public boolean hasGrantedGraveyardCardCastPermission(GameData gameData, Card card, UUID playerId) {
        GameData.GraveyardCardCastPermission permission =
                gameData.graveyardCardCastPermissionsUntilEndOfTurn.get(card.getId());
        return permission != null && playerId.equals(permission.castingPlayerId());
    }

    public boolean hasGraveyardPlayPermission(GameData gameData, Card card, UUID playerId) {
        UUID permittedPlayer = gameData.graveyardPlayPermissions.get(card.getId());
        if (permittedPlayer != null && permittedPlayer.equals(playerId)) {
            return true;
        }
        if (hasGraveyardCastFilterPermission(gameData, card, playerId)) {
            return true;
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (graveyardOwnerId != null && gameData.graveyardPlayFilterPermissionsThisTurn.stream()
                .anyMatch(permission -> permission.playerId().equals(playerId)
                        && permission.scope().graveyardOwners(gameData.orderedPlayerIds, playerId)
                        .contains(graveyardOwnerId)
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null))) {
            return true;
        }
        return isCastableSpellCard(card) && gameData.emblems.stream()
                .filter(emblem -> emblem.controllerId().equals(playerId))
                .flatMap(emblem -> emblem.staticEffects().stream())
                .filter(CastSpellsFromGraveyardPermission.class::isInstance)
                .map(CastSpellsFromGraveyardPermission.class::cast)
                .anyMatch(permission -> predicateEvaluationService.matchesCardPredicate(
                        card, permission.filter(), null));
    }

    /**
     * True if a turn-scoped blanket grant (Liliana, Untouched by Death's −3) lets this player cast
     * {@code card} from their graveyard. Lands are not spells, so they never qualify.
     */
    private boolean hasGraveyardCastFilterPermission(GameData gameData, Card card, UUID playerId) {
        return findGraveyardCastFilterPermission(gameData, card, playerId).isPresent();
    }

    public Optional<GameData.GraveyardCastFilterPermission> findGraveyardCastFilterPermission(
            GameData gameData, Card card, UUID playerId) {
        if (!isCastableSpellCard(card)) {
            return Optional.empty();
        }
        return gameData.graveyardCastFilterPermissionsThisTurn.stream()
                .filter(permission -> permission.playerId().equals(playerId)
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null))
                .sorted((first, second) -> Boolean.compare(first.singleUse(), second.singleUse()))
                .findFirst();
    }

    public void consumeGraveyardCastFilterPermission(
            GameData gameData, GameData.GraveyardCastFilterPermission permission) {
        if (permission != null && permission.singleUse()) {
            gameData.graveyardCastFilterPermissionsThisTurn.remove(permission);
        }
    }

    public boolean graveyardCastFilterPermissionExiles(GameData gameData, Card card, UUID playerId) {
        return gameData.graveyardCastFilterPermissionsThisTurn.stream()
                .anyMatch(permission -> permission.playerId().equals(playerId)
                        && permission.exileInsteadOfGraveyard()
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null));
    }

    public boolean isGraveyardCastAvailable(GameData gameData, UUID playerId, GraveyardCast graveyardCast) {
        return isGraveyardCastAvailable(gameData, playerId, null, graveyardCast);
    }

    public boolean isGraveyardCastAvailable(GameData gameData, UUID playerId, Card card,
                                            GraveyardCast graveyardCast) {
        if (graveyardCast.availabilityCondition() != null
                && !conditionEvaluationService.isMet(gameData, graveyardCast.availabilityCondition(),
                        card == null ? ConditionContext.forCasting(playerId)
                                : ConditionContext.forCard(card, playerId))) {
            return false;
        }
        if (graveyardCast.controllerControlsPredicate() == null) {
            return true;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, graveyardCast.controllerControlsPredicate()));
    }

    /**
     * Returns the mayhem casting option granted by a permanent the player controls, if any.
     */
    public Optional<GraveyardCast> findMayhemCastOption(GameData gameData, UUID playerId, Card card) {
        if (!isCastableSpellCard(card)) {
            return Optional.empty();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return Optional.empty();
        }
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantMayhemToGraveyardCardsEffect mayhem
                        && predicateEvaluationService.matchesCardPredicate(card, mayhem.filter(), null)) {
                    return Optional.of(new GraveyardCast(
                            null, null, List.of(), new CardDiscardedThisTurn()));
                }
            }
        }
        return Optional.empty();
    }

    public boolean hasGrantedFlashback(GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null && battlefield.stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(GrantFlashbackToGraveyardCardsEffect.class::isInstance)
                .map(GrantFlashbackToGraveyardCardsEffect.class::cast)
                .anyMatch(effect -> effect.cardTypes().stream().anyMatch(card::hasType))) {
            return true;
        }
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(playerId)) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof EmblemGrantsFlashbackEffect egf) {
                    for (CardType type : egf.cardTypes()) {
                        if (card.hasType(type)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Bösium Strip: until end of turn, the player may cast the top card of their graveyard if it is
     * an instant or sorcery (normal mana cost; exile instead of graveyard on resolution).
     */
    public boolean canCastTopInstantOrSorceryFromGraveyard(GameData gameData, UUID playerId, Card card) {
        if (!gameData.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn.contains(playerId)) {
            return false;
        }
        if (!card.hasType(CardType.INSTANT) && !card.hasType(CardType.SORCERY)) {
            return false;
        }
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return false;
        }
        return graveyard.getLast().getId().equals(card.getId());
    }

    /**
     * Returns true if the card has at least one non-land permanent type whose slot
     * has not been used this turn (for Muldrotha-style graveyard casting).
     */
    public static boolean hasUnusedPermanentTypeSlot(Card card, Set<CardType> typesCastFromGraveyard) {
        // Check primary type
        CardType primary = card.getType();
        if (primary.isPermanentType() && primary != CardType.LAND && !typesCastFromGraveyard.contains(primary)) {
            return true;
        }
        // Check additional types
        for (CardType t : card.getAdditionalTypes()) {
            if (t.isPermanentType() && t != CardType.LAND && !typesCastFromGraveyard.contains(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the set of card types that the player can cast from the top of their library,
     * based on AllowCastFromTopOfLibraryEffect on their permanents.
     */
    public Set<CardType> getCastableTypesFromTopOfLibrary(GameData gameData, UUID playerId) {
        Set<CardType> castableTypes = new HashSet<>();
        if (gameData.playersAllowedToPlayFromLibraryTopUntilEndOfTurn.contains(playerId)) {
            castableTypes.addAll(EnumSet.allOf(CardType.class));
            castableTypes.remove(CardType.LAND);
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return castableTypes;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                if (resolved instanceof AllowCastFromTopOfLibraryEffect allow) {
                    castableTypes.addAll(allow.castableTypes());
                }
            }
        }
        return castableTypes;
    }

    /** Returns whether the current top card has a temporary free-play permission from the library. */
    public boolean hasLibraryTopCardFreePlayPermission(GameData gameData, UUID playerId, Card card) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty() || !deck.getFirst().getId().equals(card.getId())) {
            return false;
        }
        return card.getId().equals(gameData.libraryTopCardFreePlayPermissionsUntilEndOfTurn.get(playerId));
    }

    /** Returns whether the current top card may be played for life until end of turn. */
    public boolean hasLibraryTopCardLifePlayPermission(GameData gameData, UUID playerId, Card card) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        return deck != null && !deck.isEmpty()
                && deck.getFirst().getId().equals(card.getId())
                && gameData.libraryTopCardLifePlayPermissionsUntilEndOfTurn.contains(playerId);
    }

    /** Returns whether a specific card may be cast from the top of the player's library. */
    public boolean canCastFromTopOfLibrary(GameData gameData, UUID playerId, Card card) {
        if (!card.hasType(CardType.LAND)
                && (hasLibraryTopCardFreePlayPermission(gameData, playerId, card)
                || hasLibraryTopCardLifePlayPermission(gameData, playerId, card))) {
            return true;
        }
        if (!card.hasType(CardType.LAND)
                && gameData.playersAllowedToPlayFromLibraryTopUntilEndOfTurn.contains(playerId)) {
            return true;
        }
        return canCastFromTopOfLibraryNormally(gameData, playerId, card)
                || canCastFromTopOfLibraryByPayingLifeEqualToManaValue(gameData, playerId, card);
    }

    /** Returns whether a specific card may use the mana-value life alternative from the library top. */
    public boolean canCastFromTopOfLibraryByPayingLifeEqualToManaValue(
            GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.LAND)) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AllowCastFromTopOfLibraryByPayingLifeEqualToManaValueEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns whether a specific card may be cast from the top by paying its normal cost. */
    public boolean canCastFromTopOfLibraryNormally(GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.LAND)) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                if (resolved instanceof AllowCastFromTopOfLibraryEffect allow
                        && matchesTopLibraryPermission(gameData, playerId, perm, card, allow)
                        && (!allow.oncePerTurn()
                        || !gameData.oncePerTurnLibraryCastPermissionsUsedThisTurn.contains(perm.getId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Marks the once-each-turn top-library permission used by a successful normal-cost cast. */
    public void markOncePerTurnLibraryCastPermissionUsed(GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        boolean hasUnlimitedPermission = battlefield.stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(AllowCastFromTopOfLibraryEffect.class::isInstance)
                        .map(AllowCastFromTopOfLibraryEffect.class::cast)
                        .filter(permission -> !permission.oncePerTurn())
                        .filter(permission -> matchesTopLibraryPermission(
                                gameData, playerId, permanent, card, permission)))
                .findAny()
                .isPresent();
        if (hasUnlimitedPermission) return;

        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AllowCastFromTopOfLibraryEffect permission
                        && permission.oncePerTurn()
                        && !gameData.oncePerTurnLibraryCastPermissionsUsedThisTurn.contains(permanent.getId())
                        && matchesTopLibraryPermission(gameData, playerId, permanent, card, permission)) {
                    gameData.oncePerTurnLibraryCastPermissionsUsedThisTurn.add(permanent.getId());
                    return;
                }
            }
        }
    }

    private boolean matchesTopLibraryPermission(GameData gameData, UUID playerId, Permanent source,
                                                Card card, AllowCastFromTopOfLibraryEffect permission) {
        return permission.matches(card)
                || (card.getType() != CardType.LAND && permission.filter() != null
                && predicateEvaluationService.matchesCardPredicate(
                card, permission.filter(), source.getOriginalCard().getId(), gameData, playerId));
    }

    /**
     * Vizier of the Menagerie etc.: returns true if the player controls a permanent that lets them
     * spend mana of any type to cast spells sharing one of this card's types (e.g. creature spells).
     */
    public boolean canSpendAnyManaTypeToCast(GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                if (resolved instanceof AnyManaTypeCastEffect anyMana
                        && (cardHasAnyType(card, anyMana.spellTypes())
                        || cardHasAnySubtype(card, anyMana.spellSubtypes()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean cardHasAnyType(Card card, Set<CardType> types) {
        if (types.contains(card.getType())) return true;
        for (CardType type : card.getAdditionalTypes()) {
            if (types.contains(type)) return true;
        }
        return false;
    }

    private static boolean cardHasAnySubtype(Card card, Set<CardSubtype> subtypes) {
        for (CardSubtype subtype : card.getSubtypes()) {
            if (subtypes.contains(subtype)) return true;
        }
        return false;
    }

    /**
     * Returns the set of exiled card IDs that the player can cast via
     * {@link AllowCastFromCardsExiledWithSourceEffect} on their permanents.
     */
    public Set<UUID> getCastableExiledCardIds(GameData gameData, UUID playerId) {
        Set<UUID> castableIds = new HashSet<>();
        for (GameData.ExileCastPermission permission : gameData.exileCastPermissionsUntilEndOfTurn) {
            if (playerId.equals(permission.castingPlayerId())
                    && gameData.findExiledCard(permission.cardId()) != null) {
                castableIds.add(permission.cardId());
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (hasIceCounterPermission(gameData, playerId, entry.card().getId(), false)) {
                castableIds.add(entry.card().getId());
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (gameData.stashCounterCardIds.contains(entry.card().getId())
                    && hasStashCounterPermission(gameData, playerId, entry.card().getId(), false)) {
                castableIds.add(entry.card().getId());
            }
        }
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (ExiledCardEntry entry : gameData.getExiledWithPermanentEntries(
                        perm.getId(), perm.getCard().getId())) {
                    if (activeExileCastPermissions(gameData, perm, sourceControllerId)
                            .anyMatch(permission -> canAccessExiledEntry(
                                    perm, sourceControllerId, permission, entry, playerId)
                                    && applies(permission, gameData, playerId, perm, entry))) {
                        castableIds.add(entry.card().getId());
                    }
                }
            }
        }
        return castableIds;
    }

    /**
     * Returns the set of exiled card IDs for which "mana of any type" can be spent,
     * via {@link AllowCastFromCardsExiledWithSourceEffect#anyManaType()}.
     */
    public Set<UUID> getAnyManaTypeExiledCardIds(GameData gameData, UUID playerId) {
        Set<UUID> anyManaIds = new HashSet<>();
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (gameData.stashCounterCardIds.contains(entry.card().getId())
                    && hasStashCounterPermission(gameData, playerId, entry.card().getId(), true)) {
                anyManaIds.add(entry.card().getId());
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (gameData.exilePlayAnyManaTypeWhileExiled.contains(entry.card().getId())
                    && hasExilePlayPermission(gameData, playerId, entry.card().getId())) {
                anyManaIds.add(entry.card().getId());
            }
        }
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (ExiledCardEntry entry : gameData.getExiledWithPermanentEntries(
                        perm.getId(), perm.getCard().getId())) {
                    boolean anyMana = activeExileCastPermissions(gameData, perm, sourceControllerId)
                            .anyMatch(effect -> effect.anyManaType()
                                    && canAccessExiledEntry(perm, sourceControllerId, effect, entry, playerId)
                                    && applies(effect, gameData, playerId, perm, entry));
                    if (anyMana) {
                        anyManaIds.add(entry.card().getId());
                    }
                }
            }
        }
        return anyManaIds;
    }

    /** Returns whether the player has an active direct permission to play the exiled card. */
    public boolean hasExilePlayPermission(GameData gameData, UUID playerId, UUID cardId) {
        if (!playerId.equals(gameData.exilePlayPermissions.get(cardId))) return false;
        Condition condition = gameData.exilePlayPermissionConditions.get(cardId);
        return condition == null || conditionEvaluationService.isMet(
                gameData, condition, ConditionContext.forCasting(playerId));
    }

    public boolean hasCastFromExiledWithSourcePermission(GameData gameData, UUID playerId, UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        if (findTemporaryExileCastPermission(gameData, playerId, entry, false) != null) return true;
        if (hasStashCounterPermission(gameData, playerId, cardId, false)) return true;
        if (hasIceCounterPermission(gameData, playerId, cardId, false)) return true;
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (!perm.getId().equals(entry.sourcePermanentId())) continue;
                if (activeExileCastPermissions(gameData, perm, sourceControllerId)
                        .anyMatch(permission -> canAccessExiledEntry(
                                perm, sourceControllerId, permission, entry, playerId)
                                && applies(permission, gameData, playerId, perm, entry))) return true;
            }
        }
        return false;
    }

    /** Returns an entry counter granted by the source permission used for an exiled card cast. */
    public Optional<CounterType> findEntryCounterTypeFromExiledWithSource(
            GameData gameData, UUID playerId, UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return Optional.empty();
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (!perm.getId().equals(entry.sourcePermanentId())) continue;
                Optional<CounterType> counterType = activeExileCastPermissions(
                        gameData, perm, sourceControllerId)
                        .filter(permission -> canAccessExiledEntry(
                                perm, sourceControllerId, permission, entry, playerId))
                        .filter(permission -> applies(permission, gameData, playerId, perm, entry))
                        .map(AllowCastFromCardsExiledWithSourceEffect::entryCounterType)
                        .filter(java.util.Objects::nonNull)
                        .findFirst();
                if (counterType.isPresent()) return counterType;
            }
        }
        return Optional.empty();
    }

    public boolean hasFreeCastFromExiledWithSource(GameData gameData, UUID playerId, UUID cardId) {
        return findFreeCastPermission(gameData, playerId, cardId, false);
    }

    public boolean consumeFreeCastFromExiledWithSource(GameData gameData, UUID playerId, UUID cardId) {
        return findFreeCastPermission(gameData, playerId, cardId, true);
    }

    public boolean putsExileCastOnBottomOfOwnersLibrary(GameData gameData, UUID playerId, UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) {
            return false;
        }
        GameData.ExileCastPermission permission =
                findTemporaryExileCastPermission(gameData, playerId, entry, false);
        return permission != null && permission.putOnBottomOfOwnersLibrary();
    }

    private boolean findFreeCastPermission(GameData gameData, UUID playerId, UUID cardId, boolean consume) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        GameData.ExileCastPermission temporaryPermission =
                findTemporaryExileCastPermission(gameData, playerId, entry, true);
        if (temporaryPermission != null) {
            if (consume) {
                gameData.exileCastPermissionsUntilEndOfTurn.removeIf(permission ->
                        permission.grantId().equals(temporaryPermission.grantId()));
            }
            return true;
        }
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (!perm.getId().equals(entry.sourcePermanentId())) continue;
                for (AllowCastFromCardsExiledWithSourceEffect permission :
                        activeExileCastPermissions(gameData, perm, sourceControllerId).toList()) {
                    if (!permission.withoutPayingManaCost()
                            || !canAccessExiledEntry(perm, sourceControllerId, permission, entry, playerId)
                            || !applies(permission, gameData, playerId, perm, entry)) continue;
                    if (consume && permission.oncePerTurn()) {
                        gameData.freeCastPermanentUsedThisTurn.add(perm.getId());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the additional counter cost imposed by the applicable source permission for an
     * exiled card. A present zero means the card is castable without a counter cost; an empty
     * result means no source permission applies.
     */
    public OptionalInt findAdditionalCounterCostFromSource(GameData gameData, UUID playerId, UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return OptionalInt.empty();
        if (findTemporaryExileCastPermission(gameData, playerId, entry, false) != null) {
            return OptionalInt.of(0);
        }
        if (hasStashCounterPermission(gameData, playerId, cardId, false)) {
            return OptionalInt.of(0);
        }
        if (hasIceCounterPermission(gameData, playerId, cardId, false)) {
            return OptionalInt.of(0);
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return OptionalInt.empty();
        for (Permanent perm : battlefield) {
            if (!perm.getId().equals(entry.sourcePermanentId())) continue;
            OptionalInt cost = additionalCounterCostFromSource(gameData, playerId, perm, entry);
            if (cost.isPresent() && cost.getAsInt() == 0) return cost;
            if (cost.isPresent()) return cost;
        }
        return OptionalInt.empty();
    }

    public boolean hasSufficientCountersAmongControlledCreatures(GameData gameData, UUID playerId, int required) {
        if (required <= 0) return true;
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        int available = 0;
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) continue;
            for (CounterType counterType : CounterType.values()) {
                if (counterType == CounterType.ANY || counterType == CounterType.SILVER) continue;
                available += permanent.getCounterCount(counterType);
                if (available >= required) return true;
            }
        }
        return false;
    }

    private OptionalInt additionalCounterCostFromSource(GameData gameData, UUID playerId,
                                                        Permanent source, ExiledCardEntry entry) {
        if (activeExileCastPermissions(gameData, source, playerId)
                .noneMatch(permission -> applies(permission, gameData, playerId, source, entry))) {
            return OptionalInt.empty();
        }
        return activeExileCastPermissions(gameData, source, playerId)
                .filter(permission -> applies(permission, gameData, playerId, source, entry))
                .mapToInt(AllowCastFromCardsExiledWithSourceEffect::additionalCounterCost)
                .min();
    }

    private boolean applies(AllowCastFromCardsExiledWithSourceEffect permission,
                             GameData gameData, UUID playerId, Permanent source,
                             ExiledCardEntry entry) {
        if (permission.stashCounterOnly() && playerId.equals(entry.ownerId())) return false;
        if (permission.controllerTurnOnly() && !playerId.equals(gameData.activePlayerId)) return false;
        if (permission.ownOnly() && !playerId.equals(entry.ownerId())) return false;
        if (permission.thisTurnOnly() && entry.exiledTurnNumber() != gameData.turnNumber) return false;
        if (permission.oncePerTurn()
                && gameData.freeCastPermanentUsedThisTurn.contains(source.getId())) return false;
        if (permission.filter() != null
                && !predicateEvaluationService.matchesCardPredicate(entry.card(), permission.filter(), null)) {
            return false;
        }
        if (permission.manaValueLimit() != null) {
            int limit = amountEvaluationService.evaluate(gameData, permission.manaValueLimit(),
                    AmountContext.forStaticEffect(source, playerId));
            if (entry.card().getManaValue() > limit) return false;
        }
        return true;
    }

    private Stream<AllowCastFromCardsExiledWithSourceEffect> activeExileCastPermissions(
            GameData gameData, Permanent source, UUID sourceControllerId) {
        return source.getCard().getEffects(EffectSlot.STATIC).stream()
                .flatMap(effect -> activeExileCastPermissions(
                        gameData, source, sourceControllerId, effect));
    }

    private Stream<AllowCastFromCardsExiledWithSourceEffect> activeExileCastPermissions(
            GameData gameData, Permanent source, UUID sourceControllerId, CardEffect effect) {
        if (effect instanceof AllowCastFromCardsExiledWithSourceEffect permission) {
            return Stream.of(permission);
        }
        if (effect instanceof ConditionalEffect conditional
                && conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forStaticEffect(source, sourceControllerId))) {
            return activeExileCastPermissions(gameData, source, sourceControllerId, conditional.wrapped());
        }
        return Stream.empty();
    }

    private GameData.ExileCastPermission findTemporaryExileCastPermission(
            GameData gameData, UUID playerId, ExiledCardEntry entry, boolean freeOnly) {
        return gameData.exileCastPermissionsUntilEndOfTurn.stream()
                .filter(permission -> permission.cardId().equals(entry.card().getId()))
                .filter(permission -> permission.sourcePermanentId().equals(entry.sourcePermanentId()))
                .filter(permission -> permission.castingPlayerId().equals(playerId))
                .filter(permission -> !freeOnly || permission.withoutPayingManaCost())
                .findFirst()
                .orElse(null);
    }

    public boolean hasAnyManaTypePermission(GameData gameData, UUID playerId, UUID cardId) {
        GameData.GraveyardCardCastPermission graveyardPermission =
                gameData.graveyardCardCastPermissionsUntilEndOfTurn.get(cardId);
        if (graveyardPermission != null && graveyardPermission.anyManaType()
                && playerId.equals(graveyardPermission.castingPlayerId())) {
            return true;
        }
        if (hasStashCounterPermission(gameData, playerId, cardId, true)) return true;
        // Per-card any-mana grant from a "this turn" exile-cast permission (e.g. Nita, Forum Conciliator).
        if (gameData.exilePlayAnyManaType.contains(cardId)
                || (gameData.exilePlayAnyManaTypeWhileExiled.contains(cardId)
                && hasExilePlayPermission(gameData, playerId, cardId))) return true;

        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (ExiledCardEntry entry : gameData.getExiledWithPermanentEntries(
                        perm.getId(), perm.getCard().getId())) {
                    if (entry.card().getId().equals(cardId)
                            && activeExileCastPermissions(gameData, perm, sourceControllerId)
                            .anyMatch(effect -> effect.anyManaType()
                                    && canAccessExiledEntry(perm, sourceControllerId, effect, entry, playerId)
                                    && applies(effect, gameData, playerId, perm, entry))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Returns whether the player may spend snow mana as any color for an ice-counter card. */
    public boolean hasSnowManaAsAnyColorPermission(GameData gameData, UUID playerId, UUID cardId) {
        return hasIceCounterPermission(gameData, playerId, cardId, true);
    }

    private boolean hasStashCounterPermission(GameData gameData, UUID playerId, UUID cardId,
                                              boolean anyManaTypeRequired) {
        if (!gameData.stashCounterCardIds.contains(cardId)) return false;
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .anyMatch(source -> activeExileCastPermissions(gameData, source, playerId)
                        .anyMatch(permission -> permission.stashCounterOnly()
                                && (!anyManaTypeRequired || permission.anyManaType())
                                && applies(permission, gameData, playerId, source, entry)));
    }

    private boolean hasIceCounterPermission(GameData gameData, UUID playerId, UUID cardId,
                                            boolean anyManaTypeRequired) {
        if (!gameData.exiledCardsWithIceCounters.contains(cardId)) return false;
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null || entry.card().hasType(CardType.LAND) || playerId.equals(entry.ownerId())) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .anyMatch(source -> source.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(AllowCastFromCardsExiledWithIceCountersEffect.class::isInstance)
                        .map(AllowCastFromCardsExiledWithIceCountersEffect.class::cast)
                        .anyMatch(permission -> !anyManaTypeRequired || permission.anyManaType()));
    }

    private boolean canAccessExiledEntry(Permanent source, UUID sourceControllerId,
                                         AllowCastFromCardsExiledWithSourceEffect effect,
                                         ExiledCardEntry entry, UUID playerId) {
        return effect.accessScope() == ExileAccessScope.CONTROLLER
                ? sourceControllerId.equals(playerId)
                : playerId.equals(entry.exilerId());
    }
}

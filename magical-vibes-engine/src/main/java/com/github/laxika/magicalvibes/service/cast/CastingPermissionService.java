package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.AnyManaSpending;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantPlayChosenNameUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFaceUpCardsFromExileThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreSourceAuraEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardGrantFreePlayUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SenTripletsUpkeepEffect;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromAmongExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsExiledWithPlanarSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFromAnyLibraryTopEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastAdditionalNonartifactSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CardNameRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CastPermanentSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardPermission;
import com.github.laxika.magicalvibes.model.effect.SpendAnyManaTypeToCastEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetualAnyManaTypeToCastSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DampingEngineEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerCantCastSpellsWhileAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemGrantsCastingOptionEffect;
import com.github.laxika.magicalvibes.model.effect.FirstMatchingSpellFlashGrant;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalLandPlayRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantMayhemToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardPlayPermission;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardPermission;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.PlayerCantCastSpellsAndAttackWithCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlotNonlandCardsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.TopLibraryCardCastPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.model.effect.UbaMaskDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.WardOfBonesEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.SplitCardCastSupport;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.effect.MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextMatchingSpellEffect;

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

    /** Stand-in X for a cast whose X is not chosen yet; large enough for any mana value threshold. */
    private static final int UNCHOSEN_X_FOR_PERMISSION_CHECK = 1_000;

    public record FilteredGraveyardPermission(UUID sourcePermanentId,
                                               CastSpellsFromGraveyardPermission permission) {
    }

    public record GraveyardLandPermission(UUID sourcePermanentId,
                                          PlayLandsFromGraveyardPermission permission) {
    }

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
        if (isCardPlayRestrictedInHand(gameData, playerId, card)) return false;
        if (isSplitSecondActive(gameData)) return false;
        if (isSpellLimitReached(gameData, playerId, card)) return false;
        if (isPlayerPreventedFromCasting(gameData, playerId)) return false;
        if (isSpellTypeRestricted(gameData, playerId, card)) return false;
        Set<String> forbidden = getForbiddenCardNames(gameData, playerId);
        if (isCastForbiddenByName(card, forbidden)) return false;
        if (isNoncreatureSpellCastRestricted(gameData, playerId, card)) return false;
        if (isNoncreatureSpellRestrictedByResolvedEffect(gameData, playerId, card)) return false;
        if (isOpponentsSpellMatchingPredicateRestricted(gameData, playerId, card)) return false;
        if (isSpellRestrictedByResolvedFilter(gameData, playerId, card)) return false;
        if (isAdditionalNonartifactSpellRestricted(gameData, playerId, card)) return false;
        if (isSpellCastingRestrictedByMostRecentSpell(gameData, card)) return false;
        if (isSpellCastingRestrictedByTopLibraryCard(gameData, card)) return false;
        // CR 205.4e: legendary sorceries require controlling a legendary creature or planeswalker
        if (card.getSupertypes().contains(CardSupertype.LEGENDARY)
                && card.hasType(CardType.SORCERY)
                && !controlsLegendaryCreatureOrPlaneswalker(gameData, playerId)) return false;
        return true;
    }

    public int getMaxSpellsPerTurn(GameData gameData, UUID playerId) {
        int limit = gameData.spellLimitThisTurn(playerId);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    continue;
                }
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
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
        if (gameData.getSpellsCastThisTurnCount(playerId) >= gameData.spellLimitThisTurn(playerId)) {
            return true;
        }

        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    continue;
                }
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
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
     * Whether something stops {@code playerId} casting any spell now, whatever the spell and the zone it is cast from:
     * a resolved "can't cast spells" restriction (Silence, "you can't cast additional spells this turn"), a static lock
     * now holding (Grand Abolisher, City of Solitude, Dosan the Falling Leaf, Basandra, Teferi), Sen Triplets, or an
     * attack lock (Angelic Arbiter).
     * This is the one player-level check of the casting paths: the hand, command zone, graveyard, exile, library-top and
     * library-search paths ask it through {@link #isSpellCastingAllowed} or their playability context, suspend asks it
     * directly (CR 702.62a: "if you could begin to cast this card"), and a cast during a resolution asks it through
     * {@link #canCastDuringResolution(GameData, UUID, Card)}.
     */
    public boolean isPlayerPreventedFromCasting(GameData gameData, UUID playerId) {
        if (gameData.hasFloatingEffectOnPlayer(playerId, SenTripletsUpkeepEffect.class)) return true;
        // A resolved "can't cast spells" (Silence) or a static lock now holding (Grand Abolisher, City of Solitude, Dosan the
        // Falling Leaf, Basandra during combat, Teferi outside the player's sorcery timing).
        if (gameQueryService.isPlayerRestricted(gameData, playerId, PlayerRestriction::stopsEverySpell)) return true;
        synchronized (gameData.floatingEffects) {
            if (gameData.floatingEffects.stream().anyMatch(floating ->
                    playerId.equals(floating.affectedPlayerId())
                            && floating.effect() instanceof PlayerCantCastSpellsAndAttackWithCreaturesEffect)) {
                return true;
            }
        }

        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (!permanent.isAttacking()
                        || permanent.isFaceDown()
                        || gameQueryService.hasLostAllAbilities(gameData, permanent)
                        || permanent.getCard().getStaticEffects().stream()
                        .noneMatch(DefendingPlayerCantCastSpellsWhileAttackingEffect.class::isInstance)) {
                    continue;
                }
                UUID attackedTargetId = permanent.getAttackTarget();
                UUID defendingPlayerId = attackedTargetId != null && gameData.playerIds.contains(attackedTargetId)
                        ? attackedTargetId
                        : attackedTargetId == null
                                ? gameData.orderedPlayerIds.stream()
                                .filter(id -> !id.equals(controllerId)).findFirst().orElse(null)
                                : findAttackedPlaneswalkerController(gameData, attackedTargetId);
                if (playerId.equals(defendingPlayerId)) return true;
            }
        }

        boolean playerAttackedThisTurn = gameData.playersDeclaredAttackersThisTurn.contains(playerId);
        if (!playerAttackedThisTurn) return false;

        if (gameData.planechase != null) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof OpponentsCantCastSpellsIfAttackedThisTurnEffect restriction
                            && !restriction.onlyIfAttackedControllerOrPlaneswalker()
                            && SubjectResolutionService.namesPlayer(
                                    null, gameData.planechase.controllerId, playerId, restriction.players())) {
                        return true;
                    }
                }
            }
        }

        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    continue;
                }
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    if (effect instanceof OpponentsCantCastSpellsIfAttackedThisTurnEffect restriction
                            && SubjectResolutionService.namesPlayer(perm, pid, playerId, restriction.players())
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

    /**
     * The controller of the planeswalker an attacker is attacking, or {@code null} when that
     * planeswalker has left the battlefield (the attacker then has no defending player).
     */
    private UUID findAttackedPlaneswalkerController(GameData gameData, UUID planeswalkerId) {
        Permanent planeswalker = gameQueryService.findPermanentById(gameData, planeswalkerId);
        return planeswalker == null ? null : gameData.findControllerOf(planeswalker);
    }

    /**
     * The card types of the spells {@code playerId} can't cast now. A restriction on every spell (Silence) is not a type
     * set: {@link #isPlayerPreventedFromCasting} answers it, and every reader of this set asks that too.
     */
    public Set<CardType> getRestrictedSpellTypes(GameData gameData, UUID playerId) {
        // Cease-Fire, Abeyance, Single Combat (resolved); Hand to Hand during combat, Priority Avenger while the stack is
        // empty (static locks): "can't cast [type] spells".
        Set<CardType> restricted = gameQueryService.restrictedSpellTypes(gameData, playerId);
        // Controller-only restrictions (Steel Golem) come from the player's own permanents;
        // symmetric restrictions (Aether Storm) apply no matter whose battlefield they sit on.
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                // A face-down permanent has no characteristics beyond those its face-down rule lists (CR 708.2), so its
                // static restriction is off.
                if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    continue;
                }
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    if (effect instanceof CantCastSpellTypeEffect cantCast
                            && (cantCast.appliesToAllPlayers() || pid.equals(playerId))) {
                        restricted.addAll(cantCast.restrictedTypes());
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
        if (hasActiveDampingEngine(gameData, playerId)) {
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
     * Returns true if a static effect stops this player from playing lands: a static "can't play lands" naming them
     * (Aggressive Mining's {@code PlayersCantCastSpellsFromZonesEffect(you(), playingLands())}), a global
     * permanent-count restriction (Limited Resources), or an opponent's Ward of Bones (EVE) while this player
     * controls more lands than that opponent.
     */
    public boolean isLandPlayRestricted(GameData gameData, UUID playerId) {
        if (gameQueryService.isPlayerRestricted(gameData, playerId, restriction ->
                restriction.kind() == PlayerRestriction.Kind.PLAY_LANDS && !restriction.isZoneBound())) {
            return true;
        }
        if (hasActiveGlobalLandPlayRestriction(gameData)) return true;
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(playerId) || !controlsWardOfBones(gameData, controllerId)) continue;
            if (countControlledOfType(gameData, playerId, CardType.LAND)
                    > countControlledOfType(gameData, controllerId, CardType.LAND)) {
                return true;
            }
        }
        if (controlsMorePermanentsThanEachOtherPlayer(gameData, playerId) && hasActiveDampingEngine(gameData, playerId)) {
            return true;
        }
        return false;
    }

    /** Whether a zone-bound restriction stops {@code playerId} playing a land from their hand (Experimental Frenzy). */
    public boolean isLandPlayFromHandRestricted(GameData gameData, UUID playerId) {
        return isLandPlayFromZoneRestricted(gameData, playerId, Zone.HAND);
    }

    /** Whether a zone-bound restriction stops {@code playerId} casting spells from their hand (Experimental Frenzy). */
    public boolean isSpellCastingFromHandRestricted(GameData gameData, UUID playerId) {
        return gameQueryService.isPlayerRestricted(gameData, playerId,
                restriction -> restriction.stopsSpellsFrom(Zone.HAND));
    }

    /**
     * Whether a resolved or static restriction bound to some zones stops {@code playerId} playing a land from
     * {@code zone} ("they can't play cards from their hand", Memory Vessel; "other players can't play lands … from
     * their graveyards", Shaman's Trance; "your opponents can't play land cards from graveyards", Tomik). A "can't play
     * lands" restriction from every zone is asked apart.
     */
    private boolean isLandPlayFromZoneRestricted(GameData gameData, UUID playerId, Zone zone) {
        return gameQueryService.isPlayerRestricted(gameData, playerId,
                restriction -> restriction.isZoneBound() && restriction.stopsLandsFrom(zone));
    }

    private boolean hasActiveDampingEngine(GameData gameData, UUID playerId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (gameData.ignoresEffectOf(permanent.getId(), IgnoreSourceAuraEffectsUntilEndOfTurnEffect.class, playerId)) continue;
                if (permanent.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, permanent)) continue;
                if (permanent.getCard().getStaticEffects().stream()
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
                if (permanent.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, permanent)) continue;
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
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
        return bf.stream().anyMatch(perm -> gameQueryService.getActiveStaticEffects(gameData, perm, playerId).stream()
                .anyMatch(effectType::isInstance));
    }

    private long countControlledOfType(GameData gameData, UUID playerId, CardType type) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return 0;
        return bf.stream().filter(perm -> switch (type) {
            case CREATURE -> gameQueryService.isCreature(gameData, perm);
            case ARTIFACT -> gameQueryService.isArtifact(gameData, perm);
            case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, perm);
            case LAND -> gameQueryService.isLand(gameData, perm);
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
                if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
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
     * Whether a resolved restriction with a spell filter stops {@code card} for {@code playerId} (Living
     * Breakthrough's "your opponents can't cast spells with the same mana value as that spell until your next turn").
     * Playability overload (no chosen X): an {X} spell is stopped only if every X would be. The cast-time overload
     * counts the chosen X (CR 202.3e).
     */
    public boolean isSpellRestrictedByResolvedFilter(GameData gameData, UUID castingPlayerId, Card card) {
        return isSpellRestrictedByResolvedFilter(gameData, castingPlayerId, card, null);
    }

    public boolean isSpellRestrictedByResolvedFilter(GameData gameData, UUID castingPlayerId, Card card,
                                                     Integer chosenX) {
        for (PlayerRestriction restriction : gameData.restrictionsOnPlayer(castingPlayerId)) {
            if (restriction.spellFilter() != null && restrictionMatchesSpell(gameData, restriction.spellFilter(),
                    null, null, castingPlayerId, card, chosenX)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Static restrictions such as Llawan's apply to spells matching a card predicate, for the players the restriction
     * names: the opponents of the permanent's controller, every player, or the enchanted permanent's controller.
     */
    public boolean isOpponentsSpellMatchingPredicateRestricted(GameData gameData, UUID castingPlayerId,
                                                                 Card card) {
        return isOpponentsSpellMatchingPredicateRestricted(gameData, castingPlayerId, card, null);
    }

    /**
     * Static card-predicate restrictions with an optional chosen X value. The chosen value is
     * needed for predicates based on a spell's mana value, such as odd/even restrictions.
     */
    public boolean isOpponentsSpellMatchingPredicateRestricted(GameData gameData, UUID castingPlayerId,
                                                                 Card card, Integer chosenX) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                // A face-down permanent has no text (CR 708.2a).
                if (perm.isFaceDown()) continue;
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    if (effect instanceof OpponentsCantCastSpellsMatchingPredicateEffect restriction
                            && !gameQueryService.hasLostAllAbilities(gameData, perm)
                            && SubjectResolutionService.namesPlayer(
                                    gameData, perm, pid, castingPlayerId, restriction.players())
                            && restrictionMatchesSpell(gameData, restriction.predicate(), perm,
                            perm.getCard().getId(), castingPlayerId, card, chosenX)) {
                        return true;
                    }
                }
            }
        }
        for (Emblem emblem : gameData.emblems) {
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof OpponentsCantCastSpellsMatchingPredicateEffect restriction
                        && SubjectResolutionService.namesPlayer(
                                gameData, null, emblem.controllerId(), castingPlayerId, restriction.players())
                        && restrictionMatchesSpell(gameData, restriction.predicate(), null,
                        emblem.sourceCard().getId(), castingPlayerId, card, chosenX)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a spell restriction's {@code predicate} stops {@code card}, its amounts evaluated for the casting player.
     * With X chosen the spell is judged with it (CR 202.3e); before X is chosen a spell with {X} is stopped only if
     * every X would stop it. The X values judged, 0, 1 and a large one, decide each mana-value bound and parity a
     * restriction names.
     */
    private boolean restrictionMatchesSpell(GameData gameData, CardPredicate predicate, Permanent source,
                                            UUID sourceCardId, UUID castingPlayerId, Card card, Integer chosenX) {
        CardPredicate bound = amountEvaluationService.bindAmounts(gameData, predicate,
                new AmountContext(castingPlayerId, source, castingPlayerId, 0, 0), true);
        ManaCost cost = card.getParsedManaCost();
        if (chosenX != null || cost == null || !cost.hasX()) {
            return predicateEvaluationService.matchesCardPredicate(
                    card, bound, sourceCardId, gameData, castingPlayerId, null, null, chosenX);
        }
        for (int x : new int[]{0, 1, UNCHOSEN_X_FOR_PERMISSION_CHECK}) {
            if (!predicateEvaluationService.matchesCardPredicate(
                    card, bound, sourceCardId, gameData, castingPlayerId, null, null, x)) {
                return false;
            }
        }
        return true;
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
                .anyMatch(cast -> !cast.hasType(CardType.ARTIFACT)
                        && !gameData.wasArtifactSpellWhenCastThisTurn(cast.getId()));
    }

    public boolean isSpellCastingRestrictedByMostRecentSpell(GameData gameData, Card card) {
        return isSpellCastingRestrictedByMostRecentSpell(gameData, card, null);
    }

    public boolean isSpellCastingRestrictedByMostRecentSpell(GameData gameData, Card card,
                                                              Integer chosenX) {
        Card mostRecentSpell = gameData.getMostRecentSpellCastThisTurn();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    continue;
                }
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    if (effect instanceof com.github.laxika.magicalvibes.model.effect.CantCastSpellsSharingColorWithMostRecentSpellEffect) {
                        if (mostRecentSpell != null && card != null
                                && colorsWhenCast(gameData, mostRecentSpell).stream()
                                .anyMatch(gameQueryService.getEffectiveCardColors(gameData, card)::contains)) {
                            return true;
                        }
                        continue;
                    }
                    if (effect instanceof SpellCastingRestrictionEffect restriction
                            && restriction.preventsCasting(perm, mostRecentSpell, card, chosenX)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * The colors the most recently cast spell had when it was cast, as the cast event recorded them (a color
     * Painter's Servant gave it included): a color-changing effect that applied to it afterwards (Blind Seer)
     * doesn't change which spells Mana Maze restricts (its 2004-10-04 ruling).
     */
    private Set<CardColor> colorsWhenCast(GameData gameData, Card spell) {
        return gameData.getSpellColorsWhenCast(spell.getId())
                .orElseGet(() -> gameQueryService.getEffectiveCardColors(gameData, spell));
    }

    public boolean isSpellCastingRestrictedByTopLibraryCard(GameData gameData, Card card) {
        if (card == null || card.hasType(CardType.LAND) || gameData.planechase == null) {
            return false;
        }

        List<Card> topCards = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(pid);
            if (deck != null && !deck.isEmpty()) {
                topCards.add(deck.getFirst());
            }
        }
        if (topCards.isEmpty()) {
            return false;
        }

        for (var planar : gameData.planechase.faceUp) {
            for (CardEffect effect : planar.getCard().getStaticEffects()) {
                if (!(effect instanceof SpellCastingRestrictionEffect restriction)) {
                    continue;
                }
                for (Card topCard : topCards) {
                    if (restriction.preventsCastingFromTopOfLibrary(topCard, card)) {
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
                if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
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
                if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    continue;
                }
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    if (effect instanceof CantCastSpellsWithSameNameAsExiledCardEffect cantCast) {
                        // If opponentsOnly, skip if the casting player is the controller
                        if (cantCast.opponentsOnly() && pid.equals(castingPlayerId)) {
                            continue;
                        }
                        // The restriction names the exiled card, so it ends once that card leaves exile.
                        Card imprinted = gameData.getImprintedCard(perm.getCard());
                        if (imprinted != null && gameData.findExiledCard(imprinted.getId()) != null) {
                            forbidden.addAll(imprinted.getNames());
                        }
                        gameData.getCardsExiledByPermanent(perm.getId()).stream()
                                .map(Card::getNames)
                                .forEach(forbidden::addAll);
                    }
                    if (effect instanceof SpellsWithChosenNameCantBeCastEffect chosenNameCast) {
                        // Gideon's Intervention restricts only opponents; the controller may still cast.
                        if (chosenNameCast.opponentsOnly() && pid.equals(castingPlayerId)) {
                            continue;
                        }
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
        // Comply, Conjurer's Ban: until the namer's next turn, those players, or nobody, can cast spells so named.
        for (FloatingContinuousEffect fe : gameData.floatingEffectsOnPlayer(castingPlayerId,
                CantPlayChosenNameUntilNextTurnEffect.class)) {
            forbidden.add(((CantPlayChosenNameUntilNextTurnEffect) fe.effect()).cardName());
        }
        // Reflector Mage: until its controller's next turn, the owner can't cast spells with the returned name.
        for (FloatingContinuousEffect fe : gameData.floatingEffectsOnPlayer(castingPlayerId,
                ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect.class)) {
            forbidden.add(
                    ((ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect) fe.effect()).cardName());
        }
        for (FloatingContinuousEffect fe : gameData.floatingEffectsOfTypes(
                CantPlayChosenNameUntilNextTurnEffect.class)) {
            var rule = (CantPlayChosenNameUntilNextTurnEffect) fe.effect();
            if (rule.landsToo()) {
                forbidden.add(rule.cardName());
            }
        }
        return forbidden;
    }

    /**
     * "Lands with the chosen names can't be played" ({@code SpellsWithChosenNameCantBeCastEffect.landsToo}, Null Chamber). Land plays deliberately bypass the
     * spell-casting filters (they aren't spells), so the name check has its own entry point here.
     */
    public boolean isLandPlayForbiddenByChosenName(GameData gameData, Card card) {
        Set<String> nontokenPermanentNames = null;
        for (GameQueryService.ActiveStaticEffect<CardEffect> active
                : gameQueryService.activeStaticEffects(gameData, CardEffect.class)) {
            Permanent perm = active.source();
            CardEffect effect = active.effect();
            if (effect instanceof SpellsWithChosenNameCantBeCastEffect chosenNames && chosenNames.landsToo()
                    && (card.hasName(perm.getChosenName())
                        || card.hasName(perm.getSecondChosenName()))) {
                return true;
            }
            if (effect instanceof CardNameRestrictionEffect restriction) {
                if (nontokenPermanentNames == null) {
                    nontokenPermanentNames = getNontokenPermanentNames(gameData);
                }
                boolean nonbasicLandRestricted = !card.getSupertypes().contains(CardSupertype.BASIC)
                        && restriction.forbiddenNonbasicLandNames(nontokenPermanentNames)
                        .contains(card.getName());
                boolean landRestricted = restriction.forbiddenLandNames(nontokenPermanentNames)
                        .contains(card.getName());
                if (nonbasicLandRestricted || landRestricted) {
                    return true;
                }
            }
        }
        return gameData.floatingEffectsOfTypes(CantPlayChosenNameUntilNextTurnEffect.class).stream()
                .map(fe -> (CantPlayChosenNameUntilNextTurnEffect) fe.effect())
                .anyMatch(rule -> rule.landsToo() && card.hasName(rule.cardName()));
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
        if (isCardPlayRestrictedInHand(gameData, playerId, card)) return true;
        if (isSplitSecondActive(gameData)) return true;
        if (isSpellCastingRestrictedByMostRecentSpell(gameData, card)) return true;
        if (isSpellCastingRestrictedByTopLibraryCard(gameData, card)) return true;
        if (isNoncreatureSpellRestrictedByResolvedEffect(gameData, playerId, card)) return true;
        if (isOpponentsSpellMatchingPredicateRestricted(gameData, playerId, card)) return true;
        return isSpellRestricted(card, restrictedSpellTypes, forbiddenCardNames);
    }

    /** Returns whether this player's copy of a hand card is temporarily barred from being played. */
    public boolean isCardPlayRestrictedInHand(GameData gameData, UUID playerId, Card card) {
        if (!gameData.isCardPlayBarredInHand(card.getId(), playerId)) {
            return false;
        }
        return gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                .anyMatch(handCard -> handCard.getId().equals(card.getId()));
    }

    public boolean isSplitSecondActive(GameData gameData) {
        return gameData.stack.stream()
                .anyMatch(entry -> gameQueryService.hasSplitSecond(gameData, entry));
    }

    public boolean isSpellRestricted(Card card, Set<CardType> restrictedSpellTypes, Set<String> forbiddenCardNames) {
        if (restrictedSpellTypes.contains(card.getType())) return true;
        for (CardType type : card.getAdditionalTypes()) {
            if (restrictedSpellTypes.contains(type)) return true;
        }
        return isCastForbiddenByName(card, forbiddenCardNames);
    }

    /**
     * Whether the names {@link #getForbiddenCardNames} gives keep {@code card} from being cast. Of a split card that
     * isn't yet cast as one of its halves only a half that can be cast from anywhere has to be allowed, as only the
     * chosen half is evaluated (CR 709.3a): {@link #isSplitModeForbiddenByName} judges that half as it is chosen.
     * A Room's doors are such halves (CR 709.5j).
     */
    public static boolean isCastForbiddenByName(Card card, Set<String> forbiddenCardNames) {
        List<OracleData> halves = card.getSplitHalves();
        if (!isWholeSplitCard(card)) {
            return forbiddenCardNames.stream().anyMatch(card::hasName);
        }
        // An aftermath half is cast only from a graveyard, where it is judged by itself (CR 702.127a).
        return card.isAftermathSplitCard()
                ? forbiddenCardNames.contains(halves.getFirst().name())
                : halves.stream().allMatch(half -> forbiddenCardNames.contains(half.name()));
    }

    /**
     * Whether the names forbidden to {@code playerId} keep the split card {@code card} from being cast by its mode
     * {@code modeIndex}: a half by that half's name, the fused spell, which has both names (CR 709.4d), by either.
     */
    public boolean isSplitModeForbiddenByName(GameData gameData, UUID playerId, Card card, int modeIndex) {
        return isWholeSplitCard(card)
                && isSplitModeForbiddenByName(card, getForbiddenCardNames(gameData, playerId), modeIndex);
    }

    /** As the instance form, over names {@link #getForbiddenCardNames} gave. */
    public static boolean isSplitModeForbiddenByName(Card card, Set<String> forbidden, int modeIndex) {
        if (!isWholeSplitCard(card)) {
            return false;
        }
        List<OracleData> halves = card.getSplitHalves();
        return modeIndex < halves.size()
                ? forbidden.contains(halves.get(modeIndex).name())
                : halves.stream().anyMatch(half -> forbidden.contains(half.name()));
    }

    /** A split card with both halves, not the runtime copy cast as one of them, which has that half's name alone. */
    private static boolean isWholeSplitCard(Card card) {
        return card.getSplitHalves().size() >= 2 && card.getName() != null && card.getName().contains(" // ");
    }

    /**
     * Whether a resolved "can't cast noncreature spells" restriction on the player stops {@code card} (Calamity's Wake,
     * Hope of Ghirapur): not a set of card types, since an artifact creature spell is a creature spell.
     */
    private static boolean isNoncreatureSpellRestrictedByResolvedEffect(GameData gameData, UUID playerId, Card card) {
        return !card.hasType(CardType.CREATURE)
                && gameData.isRestricted(playerId, PlayerRestriction.Kind.CAST_NONCREATURE_SPELLS);
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

    private boolean cardHasFlash(GameData gameData, Card card) {
        return (card.getKeywords().contains(Keyword.FLASH)
                || gameData.perpetualCardKeywords.getOrDefault(card.getId(), Set.of()).contains(Keyword.FLASH))
                && !gameData.perpetualCardRemovedKeywords.getOrDefault(card.getId(), Set.of()).contains(Keyword.FLASH);
    }
    public boolean canCastWithTiming(GameData gameData, UUID playerId, Card card,
                                     boolean isActivePlayer, boolean isMainPhase, boolean stackEmpty) {
        return canCastWithTiming(gameData, playerId, card, isActivePlayer, isMainPhase, stackEmpty, false, 0);
    }

    public boolean canCastWithTimingFromLibraryTop(GameData gameData, UUID playerId, Card card,
                                                   boolean isActivePlayer, boolean isMainPhase,
                                                   boolean stackEmpty) {
        return canCastWithTiming(gameData, playerId, card, isActivePlayer, isMainPhase, stackEmpty, true, 0);
    }

    public boolean canCastWithTiming(GameData gameData, UUID playerId, Card card,
                                     boolean isActivePlayer, boolean isMainPhase, boolean stackEmpty,
                                     int xValue) {
        return canCastWithTiming(gameData, playerId, card, isActivePlayer, isMainPhase, stackEmpty, false, xValue);
    }

    private boolean canCastWithTiming(GameData gameData, UUID playerId, Card card,
                                      boolean isActivePlayer, boolean isMainPhase, boolean stackEmpty,
                                      boolean fromLibraryTop, int xValue) {
        return canCastWithTiming(gameData, playerId, card, isActivePlayer, isMainPhase, stackEmpty,
                fromLibraryTop, xValue, true);
    }

    /** Checks face-down characteristics while excluding timing tied to a different alternative cost. */
    public boolean canCastFaceDownWithTiming(GameData gameData, UUID playerId, Card physicalCard) {
        return canCastWithTiming(gameData, playerId, faceDownSpellCharacteristics(physicalCard),
                playerId.equals(gameData.activePlayerId),
                gameData.currentStep == TurnStep.PRECOMBAT_MAIN || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN,
                gameData.stack.isEmpty(), false, 0, false);
    }

    private boolean canCastWithTiming(GameData gameData, UUID playerId, Card card,
                                      boolean isActivePlayer, boolean isMainPhase, boolean stackEmpty,
                                      boolean fromLibraryTop, int xValue, boolean includeAlternativeCostFlash) {
        // Teferi: a player held to sorcery timing may cast only then, whatever permission the card has (CR 307.5).
        if (gameQueryService.isHeldToSorceryTiming(gameData, playerId)) {
            return sorceryTimingAvailable(gameData, playerId);
        }
        boolean sorceryTiming = isActivePlayer && isMainPhase && stackEmpty;

        boolean isInstantSpeed = card.hasType(CardType.INSTANT)
                || cardHasFlash(gameData, card)
                || hasFlashGrantForCard(gameData, playerId, card, includeAlternativeCostFlash)
                || (fromLibraryTop && hasTopLibraryFlashGrant(gameData, playerId, card))
                || grantsItselfFlashTiming(card)
                || hasMetFlashCastCondition(gameData, playerId, card, xValue)
                || card.getFlashCastTargetPredicate() != null
                || hasAvailableFlashAlternateCast(gameData, playerId, card)
                || hasSneakTiming(gameData, playerId, card);
        // A card-specific timing restriction is itself the permission to cast outside normal
        // sorcery timing (e.g. Throat Wolf during an opponent's combat phase). The restriction
        // is checked again below, so this only opens the timing window it explicitly defines.
        boolean cardSpecificTiming = card.getSpellCastTimingRestriction() != null
                && canCastWithSpellTimingRestriction(gameData, playerId, card);
        return isInstantSpeed || sorceryTiming || cardSpecificTiming;
    }

    private boolean hasSneakTiming(GameData gameData, UUID playerId, Card card) {
        return playerId.equals(gameData.activePlayerId)
                && gameData.currentStep == TurnStep.DECLARE_BLOCKERS
                && card.getCastingOption(AlternateHandCast.class)
                .map(AlternateHandCast::sneak)
                .orElse(false);
    }

    /**
     * Effects that stop a player casting spells (Dosan the Falling Leaf, Grand Abolisher) prevent a cast made during
     * another effect's resolution, and so does a sorcery-timing lock (Teferi): a resolution is never a time the player
     * could cast a sorcery (CR 608.2g; Teferi, Mage of Zhalfir's ruling).
     */
    public boolean canCastDuringResolution(GameData gameData, UUID playerId) {
        return !isPlayerPreventedFromCasting(gameData, playerId)
                && !gameQueryService.isHeldToSorceryTiming(gameData, playerId);
    }

    /**
     * Whether {@code playerId} may begin to cast {@code card} while another spell or ability resolves. Such a cast
     * follows the casting steps (CR 608.2g), which a player may begin only when no rule or effect prohibits it
     * (CR 601.3), so it asks every check of {@link #isSpellCastingAllowed} (Rule of Law, Ethersworn Canonist, a type
     * or name restriction, split second) as well as the player and timing locks of
     * {@link #canCastDuringResolution(GameData, UUID)}. The effect's permission replaces only the card's own timing.
     * A cast during a resolution asks this before anything moves, for each half of a split card as it is offered (the
     * Scrycast cast chosen in a scry answer does not yet).
     */
    public boolean canCastDuringResolution(GameData gameData, UUID playerId, Card card) {
        return canCastDuringResolution(gameData, playerId) && isSpellCastingAllowed(gameData, playerId, card);
    }

    /**
     * {@link #canCastDuringResolution(GameData, UUID, Card)} for a card offered as a whole, before a half or a land
     * play is chosen: a land is played, not cast, and the halves of a split card are each judged as they are offered
     * (CR 709.3a, {@code SplitCardCastSupport}), so only the player-level checks apply to the whole split card.
     */
    public boolean mayBeginCastDuringResolution(GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.LAND)) {
            return true;
        }
        return SplitCardCastSupport.isSplitCard(card)
                ? canCastDuringResolution(gameData, playerId)
                : canCastDuringResolution(gameData, playerId, card);
    }

    private boolean hasTopLibraryFlashGrant(GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.LAND) || !ownLibraryTopPermissionCovers(gameData, playerId, card)) return false;
        for (LibraryTopGrant grant : libraryTopGrants(gameData, playerId)) {
            if (grant.effect() instanceof AllowCastFromTopOfLibraryEffect allow
                    && allow.grantsFlash()
                    && matchesTopLibraryPermission(gameData, playerId, grant, card, allow)
                    && !isOncePerTurnCastPermissionUsed(gameData, grant, allow)
                    && canPayTopLibraryAdditionalCosts(gameData, playerId, allow)) {
                return true;
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
        if (!hasAvailableFlashAlternateCast(gameData, playerId, card)
                && !hasSneakTiming(gameData, playerId, card)) return false;
        return !card.hasType(CardType.INSTANT)
                && !cardHasFlash(gameData, card)
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
                && !cardHasFlash(gameData, card)
                && !hasFlashGrantForCard(gameData, playerId, card)
                && !grantsItselfFlashTiming(card)
                && !hasAvailableFlashAlternateCast(gameData, playerId, card);
    }

    public boolean isUsingCleanupSacrificeFlashPermission(GameData gameData, UUID playerId, Card card) {
        if (sorceryTimingAvailable(gameData, playerId) || !grantsItselfFlashTiming(card)) {
            return false;
        }
        return !card.hasType(CardType.INSTANT)
                && !cardHasFlash(gameData, card)
                && !hasFlashGrantForCard(gameData, playerId, card)
                && !hasMetFlashCastCondition(gameData, playerId, card)
                && !hasAvailableFlashAlternateCast(gameData, playerId, card);
    }

    /**
     * True if the card itself says "you may cast this spell as though it had flash" — the Mirage
     * flash clause. Unlike a battlefield flash grant this needs no permission source; the trade-off
     * (sacrifice at the next cleanup step) is applied when the permanent enters.
     */
    private boolean grantsItselfFlashTiming(Card card) {
        return card.getStaticEffects().stream()
                .anyMatch(FlashCastWithCleanupSacrificeEffect.class::isInstance);
    }

    /**
     * True if the card carries a condition-gated "you may cast this spell as though it had flash"
     * clause whose condition is currently met for the caster (e.g. Swift Reckoning's spell mastery).
     * The normal cost still applies — only the timing permission changes.
     */
    private boolean hasMetFlashCastCondition(GameData gameData, UUID playerId, Card card) {
        return hasMetFlashCastCondition(gameData, playerId, card, 0);
    }

    private boolean hasMetFlashCastCondition(GameData gameData, UUID playerId, Card card, int xValue) {
        Condition condition = card.getFlashCastCondition();
        return condition != null
                && conditionEvaluationService.isMet(gameData, condition,
                ConditionContext.forCasting(playerId)
                        .withXValue(xValue)
                        .withTeamworkCostPaid(hasAvailableTeamworkPayment(gameData, playerId, card)));
    }

    private boolean hasAvailableTeamworkPayment(GameData gameData, UUID playerId, Card card) {
        TeamworkCost teamworkCost = card.getCastingCosts().stream()
                .filter(TeamworkCost.class::isInstance)
                .map(TeamworkCost.class::cast)
                .findFirst()
                .orElse(null);
        if (teamworkCost == null) {
            return false;
        }
        int availablePower = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> !permanent.isTapped() && gameQueryService.isCreature(gameData, permanent))
                .mapToInt(permanent -> Math.max(0, gameQueryService.getEffectivePower(gameData, permanent)))
                .sum();
        return availablePower >= teamworkCost.requiredPower();
    }

    /**
     * Checks the target-dependent part of a conditional flash permission after targets are chosen.
     * The normal sorcery window never needs the target-dependent permission.
     */
    public boolean isFlashCastTargetPermissionSatisfied(GameData gameData, UUID playerId, Card card,
                                                        UUID targetId, List<UUID> targetIds) {
        PermanentPredicate predicate = card.getFlashCastTargetPredicate();
        if (predicate == null || sorceryTimingAvailable(gameData, playerId)) {
            return true;
        }
        if (cardHasFlash(gameData, card) || hasFlashGrantForCard(gameData, playerId, card)) {
            return true;
        }
        Stream<UUID> candidates = Stream.concat(
                targetId == null ? Stream.empty() : Stream.of(targetId),
                targetIds == null ? Stream.empty() : targetIds.stream());
        return candidates.distinct()
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(Objects::nonNull)
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, predicate, new FilterContext(gameData, card.getId(), playerId,
                                null, null, null, false, null, null, null, null)));
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
            case YOUR_DECLARE_ATTACKERS -> gameData.currentStep == TurnStep.DECLARE_ATTACKERS
                    && playerId.equals(gameData.activePlayerId);
            case BEFORE_ATTACKERS_DECLARED -> gameData.currentStep.isBeforeAttackersDeclared()
                    && gameData.combatPhasesThisTurn <= 1;
            case BEFORE_BLOCKERS_DECLARED -> gameData.currentStep.isBeforeBlockersDeclared()
                    && gameData.combatPhasesThisTurn <= 1;
            case DECLARE_ATTACKERS_IF_ATTACKED ->
                    gameData.currentStep == TurnStep.DECLARE_ATTACKERS
                            && (gameData.playersAttackedThisCombat.contains(playerId)
                            || gameQueryService.isPlayerBeingAttackedByDeclaredAttacker(gameData, playerId));
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
            case OPPONENTS_DECLARE_BLOCKERS ->
                    !playerId.equals(gameData.activePlayerId)
                            && gameData.currentStep == TurnStep.DECLARE_BLOCKERS;
            case OPPONENTS_TURN_BEFORE_ATTACKERS ->
                    !playerId.equals(gameData.activePlayerId)
                            && gameData.currentStep.isBeforeAttackersDeclared()
                            && gameData.combatPhasesThisTurn <= 1;
            case OPPONENTS_TURN -> !playerId.equals(gameData.activePlayerId);
            case OPPONENTS_COMBAT -> !playerId.equals(gameData.activePlayerId)
                    && gameData.currentStep.isCombatPhase();
            case OPPONENTS_TURN_AFTER_UPKEEP ->
                    !playerId.equals(gameData.activePlayerId)
                            && gameData.currentStep.ordinal() > TurnStep.UPKEEP.ordinal();
            case OPPONENTS_UPKEEP ->
                    gameData.currentStep == TurnStep.UPKEEP
                            && !playerId.equals(gameData.activePlayerId);
            case BEFORE_COMBAT_DAMAGE -> gameData.currentStep.isBeforeCombatDamage()
                    && !gameData.firstCombatDamageTimingClosed
                    && gameData.combatPhasesThisTurn <= 1;
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
        return hasFlashGrantForCard(gameData, playerId, card, true);
    }

    private boolean hasFlashGrantForCard(GameData gameData, UUID playerId, Card card,
                                          boolean includeAlternativeCostFlash) {
        if (gameData.hasFloatingEffectOnPlayer(playerId, GrantFlashToSpellsThisTurnEffect.class)) return true;
        if (gameData.floatingEffectsOnPlayer(playerId, GrantFlashToCardTypeThisTurnEffect.class).stream()
                .anyMatch(fe -> predicateEvaluationService.matchesCardPredicate(card,
                        ((GrantFlashToCardTypeThisTurnEffect) fe.effect()).filter(), null, gameData, playerId))) {
            return true;
        }
        // Quicken: an unconsumed grant for the next spell of a given type this turn.
        if (gameData.floatingEffectsOnPlayer(playerId, GrantFlashToNextMatchingSpellEffect.class).stream()
                .anyMatch(fe -> predicateEvaluationService.matchesCardPredicate(card,
                        ((GrantFlashToNextMatchingSpellEffect) fe.effect()).predicate(), null, gameData,
                        playerId))) {
            return true;
        }
        for (UUID ownerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(ownerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, ownerId, effect);
                    if (resolved instanceof FirstMatchingSpellFlashGrant grant
                            && !gameQueryService.hasLostAllAbilities(gameData, perm)
                            && ownerId.equals(playerId)
                            && predicateEvaluationService.matchesCardPredicate(
                            card, grant.predicate(), perm.getCard().getId(), gameData, playerId)
                            && gameData.getSpellsCastThisTurn(playerId).stream().noneMatch(previousSpell ->
                            predicateEvaluationService.matchesCardPredicate(
                                    previousSpell, grant.predicate(), perm.getCard().getId(), gameData, playerId))) {
                        return true;
                    }
                    if (resolved instanceof GrantFlashToCardTypeEffect grant
                            && (includeAlternativeCostFlash || !grant.alternativeCostOnly())
                            && !gameQueryService.hasLostAllAbilities(gameData, perm)
                            && (grant.appliesToAllPlayers() || ownerId.equals(playerId))
                            && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null,
                            gameData, card.getOwnerId() != null ? card.getOwnerId() : playerId)) {
                        return true;
                    }
                }
            }
        }
        if (gameData.planechase != null) {
            UUID planarControllerId = gameData.planechase.controllerId;
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof GrantFlashToCardTypeEffect grant
                            && (includeAlternativeCostFlash || !grant.alternativeCostOnly())
                            && (grant.appliesToAllPlayers() || Objects.equals(planarControllerId, playerId))
                            && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null)) {
                        return true;
                    }
                }
            }
        }
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent source : battlefield) {
                for (ExiledCardEntry entry : gameData.getExiledWithPermanentEntries(
                        source.getId(), source.getCard().getId())) {
                    if (entry.card().getId().equals(card.getId())
                            && activeExileCastPermissions(gameData, source, sourceControllerId)
                            .anyMatch(permission -> permission.grantsFlash()
                                    && canAccessExiledEntry(gameData, source, sourceControllerId, permission, entry, playerId)
                                    && applies(permission, gameData, playerId, source, entry))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canPlayLandsFromGraveyard(GameData gameData, UUID playerId) {
        if (findGraveyardLandPermission(gameData, playerId).isPresent()) {
            return true;
        }
        return gameData.emblems.stream()
                .filter(emblem -> emblem.controllerId().equals(playerId))
                .flatMap(emblem -> emblem.staticEffects().stream())
                .filter(PlayLandsFromGraveyardPermission.class::isInstance)
                .map(PlayLandsFromGraveyardPermission.class::cast)
                .anyMatch(permission -> permission.landFilter() == null);
    }

    /** Returns whether a specific land may be played from the controller's graveyard. */
    public boolean canPlayLandsFromGraveyard(GameData gameData, UUID playerId, Card card) {
        if (findGraveyardLandPermission(gameData, playerId, card).isPresent()) {
            return true;
        }
        return gameData.emblems.stream()
                .filter(emblem -> emblem.controllerId().equals(playerId))
                .flatMap(emblem -> emblem.staticEffects().stream())
                .filter(PlayLandsFromGraveyardPermission.class::isInstance)
                .map(PlayLandsFromGraveyardPermission.class::cast)
                .anyMatch(permission -> permission.landFilter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        card, permission.landFilter(), null, gameData, playerId));
    }

    /** Returns whether a specific land matches a currently available graveyard-play permission. */
    public boolean canPlayLandFromGraveyard(GameData gameData, UUID playerId, Card card) {
        if (findGraveyardLandPermission(gameData, playerId, card).isPresent()) {
            return true;
        }
        return gameData.emblems.stream()
                .filter(emblem -> emblem.controllerId().equals(playerId))
                .flatMap(emblem -> emblem.staticEffects().stream())
                .filter(PlayLandsFromGraveyardPermission.class::isInstance)
                .map(PlayLandsFromGraveyardPermission.class::cast)
                .anyMatch(permission -> matchesLandFilter(gameData, playerId, card, permission, null));
    }

    /** Returns the available permanent granting this player permission to play a land from a graveyard. */
    public Optional<GraveyardLandPermission> findGraveyardLandPermission(GameData gameData, UUID playerId) {
        return findGraveyardLandPermission(gameData, playerId, null);
    }

    /** Returns the available permanent granting permission for the specified graveyard land. */
    public Optional<GraveyardLandPermission> findGraveyardLandPermission(
            GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        Optional<GraveyardLandPermission> limitedPermission = Optional.empty();
        for (Permanent perm : battlefield == null ? List.<Permanent>of() : battlefield) {
            if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                if (!(resolved instanceof PlayLandsFromGraveyardPermission permission)) {
                    continue;
                }
                CardPredicate landFilter = permission.landFilter();
                if (landFilter != null) {
                    if (card != null) {
                        if (!predicateEvaluationService.matchesCardPredicate(
                                card, landFilter, perm.getOriginalCard().getId(), gameData, playerId)) {
                            continue;
                        }
                    } else if (gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                            .noneMatch(graveyardCard -> graveyardCard.hasType(CardType.LAND)
                                    && predicateEvaluationService.matchesCardPredicate(
                                    graveyardCard, landFilter, perm.getOriginalCard().getId(), gameData, playerId))) {
                        continue;
                    }
                }
                if (permission.oncePerControllerTurn()
                        && (!playerId.equals(gameData.activePlayerId)
                        || gameData.oncePerTurnGraveyardCastPermissionsUsedThisTurn.contains(perm.getId()))) {
                    continue;
                }
                if (permission.oncePerControllerTurnForLand()
                        && (!playerId.equals(gameData.activePlayerId)
                        || gameData.oncePerTurnGraveyardLandPermissionsUsedThisTurn.contains(perm.getId()))) {
                    continue;
                }
                if (permission.onlyDuringControllerTurn()
                        && !playerId.equals(gameData.activePlayerId)) {
                    continue;
                }
                if (card != null && !matchesLandFilter(gameData, playerId, card, permission,
                        perm.getOriginalCard().getId())) {
                    continue;
                }
                if (card != null && !matchesGraveyardPermission(gameData, playerId, card, permission)) {
                    continue;
                }
                GraveyardLandPermission available = new GraveyardLandPermission(perm.getId(), permission);
                if (!permission.oncePerControllerTurn() && !permission.oncePerControllerTurnForLand()) {
                    return Optional.of(available);
                }
                if (limitedPermission.isEmpty()) {
                    limitedPermission = Optional.of(available);
                }
            }
        }
        // A permission that is not limited to once per turn is used first so a limited one (Kagha)
        // is not spent on a land another permission (Crucible of Worlds) also lets the player play.
        return limitedPermission;
    }

    private boolean matchesLandFilter(GameData gameData, UUID playerId, Card card,
                                      PlayLandsFromGraveyardPermission permission,
                                      UUID sourceCardId) {
        return (!permission.onlyCardsPutIntoGraveyardFromLibraryThisTurn()
                || cardWasPutIntoGraveyardFromLibraryThisTurn(gameData, card))
                && (permission.landFilter() == null
                || predicateEvaluationService.matchesCardPredicate(
                card, permission.landFilter(), sourceCardId, gameData, playerId));
    }

    public boolean isLandPlayFromGraveyardRestricted(GameData gameData, UUID playerId) {
        return isLandPlayFromZoneRestricted(gameData, playerId, Zone.GRAVEYARD);
    }

    public boolean canPlayLandsFromTopOfLibrary(GameData gameData, UUID playerId) {
        if (hasAnyLibraryTopPermission(gameData, playerId)) {
            return true;
        }
        for (LibraryTopGrant grant : libraryTopGrants(gameData, playerId)) {
            if (grant.effect() instanceof PlayLandsFromTopOfLibraryEffect
                    || grant.effect() instanceof PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect
                    && !gameData.oncePerTurnLibraryPlayPermissionsUsedThisTurn.contains(grant.sourceId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true when the controller has permission to take the plot special action for a
     * nonland card on top of their library.
     */
    public boolean canPlotNonlandCardsFromTopOfLibrary(GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.LAND) || card.getManaCost() == null
                || !ownLibraryTopPermissionCovers(gameData, playerId, card)) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
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
                && gameData.landsPlayedThisTurn.getOrDefault(playerId, 0) < gameQueryService.getMaxLandsThisTurn(gameData, playerId)
                && gameData.stack.isEmpty()
                && !gameData.isRestricted(playerId, PlayerRestriction.Kind.PLAY_LANDS)
                && !isLandPlayRestricted(gameData, playerId)
                && !isLandPlayForbiddenByChosenName(gameData, card)
                && !isCardPlayRestrictedInHand(gameData, playerId, card);
    }

    /**
     * Returns true when a land discarded with madness may be played. Madness permits the land
     * play outside the normal main-phase/empty-stack timing window, but it still requires the
     * active player's turn, an available land play, and the usual land-play restrictions.
     */
    public boolean canPlayLandForMadness(GameData gameData, UUID playerId, Card card) {
        return card.hasType(CardType.LAND)
                && playerId.equals(gameData.activePlayerId)
                && gameData.landsPlayedThisTurn.getOrDefault(playerId, 0)
                < gameQueryService.getMaxLandsThisTurn(gameData, playerId)
                && !gameData.isRestricted(playerId, PlayerRestriction.Kind.PLAY_LANDS)
                && !isLandPlayRestricted(gameData, playerId)
                && !isLandPlayForbiddenByChosenName(gameData, card)
                && !isCardPlayRestrictedInHand(gameData, playerId, card);
    }

    public boolean canPlayLandFromTopOfLibrary(GameData gameData, UUID playerId, Card card) {
        if (!card.hasType(CardType.LAND)) {
            return false;
        }
        UUID libraryOwnerId = findLibraryTopOwners(gameData, playerId).stream()
                .filter(ownerId -> gameData.playerDecks.get(ownerId).getFirst().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
        if (libraryOwnerId == null) {
            return false;
        }
        boolean permission = libraryOwnerId.equals(playerId)
                ? hasAnyLibraryTopPermission(gameData, playerId)
                    || hasMatchingLandTopLibraryPermission(gameData, playerId, card)
                    || hasMatchingOncePerTurnTopLibraryPermission(gameData, playerId, card)
                    || hasLibraryTopCardFreePlayPermission(gameData, playerId, card)
                : hasAnyLibraryTopPermission(gameData, playerId)
                    || hasLibraryTopPermission(gameData, playerId, libraryOwnerId);
        return permission && canPlayLandNow(gameData, playerId, card);
    }

    private boolean hasMatchingLandTopLibraryPermission(GameData gameData, UUID playerId, Card card) {
        for (LibraryTopGrant grant : libraryTopGrants(gameData, playerId)) {
            if (grant.effect() instanceof PlayLandsFromTopOfLibraryEffect permission
                    && predicateEvaluationService.matchesCardPredicate(
                    card, permission.filter(), grant.sourceCardId(), gameData, playerId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A permission to play from the top of the player's own library that the player has: a static ability of a
     * face-up permanent they control whose abilities work ({@code source}), or a floating copy a resolution gave them
     * ({@code source} {@code null}; "until end of turn, you may play cards from the top of your library" is
     * {@link com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect} over the permission records).
     */
    private record LibraryTopGrant(Permanent source, CardEffect effect) {

        UUID sourceId() {
            return source == null ? null : source.getId();
        }

        UUID sourceCardId() {
            return source == null ? null : source.getOriginalCard().getId();
        }
    }

    /**
     * The player's {@link AllowCastFromTopOfLibraryEffect}, {@link PlayLandsFromTopOfLibraryEffect} and
     * {@link PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect} permissions, the floating copies first, then the
     * static abilities of their permanents with each condition applied.
     */
    private List<LibraryTopGrant> libraryTopGrants(GameData gameData, UUID playerId) {
        List<LibraryTopGrant> grants = new ArrayList<>();
        for (FloatingContinuousEffect fe : gameData.floatingEffectsOnPlayer(playerId,
                AllowCastFromTopOfLibraryEffect.class, PlayLandsFromTopOfLibraryEffect.class)) {
            grants.add(new LibraryTopGrant(null, fe.effect()));
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return grants;
        for (Permanent perm : battlefield) {
            if (perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                if (resolved instanceof AllowCastFromTopOfLibraryEffect
                        || resolved instanceof PlayLandsFromTopOfLibraryEffect
                        || resolved instanceof PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect) {
                    grants.add(new LibraryTopGrant(perm, resolved));
                }
            }
        }
        return grants;
    }

    private static boolean isOncePerTurnCastPermissionUsed(GameData gameData, LibraryTopGrant grant,
                                                           AllowCastFromTopOfLibraryEffect allow) {
        return allow.oncePerTurn() && grant.source() != null
                && gameData.oncePerTurnLibraryCastPermissionsUsedThisTurn.contains(grant.sourceId());
    }

    /** Returns whether an unused shared top-library play/cast permission matches this land. */
    private boolean hasMatchingOncePerTurnTopLibraryPermission(GameData gameData, UUID playerId,
                                                                Card card) {
        return findOncePerTurnTopLibraryPermissionSource(gameData, playerId, card).isPresent();
    }

    /** Finds an unused shared top-library permission that matches a land. */
    public Optional<UUID> findOncePerTurnTopLibraryPermissionSource(GameData gameData, UUID playerId,
                                                                    Card card) {
        if (!card.hasType(CardType.LAND) || !ownLibraryTopPermissionCovers(gameData, playerId, card)) {
            return Optional.empty();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return Optional.empty();
        for (Permanent perm : battlefield) {
            if (perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
            if (gameData.oncePerTurnLibraryPlayPermissionsUsedThisTurn.contains(perm.getId())) continue;
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                if (resolved instanceof PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect permission
                        && predicateEvaluationService.matchesCardPredicate(
                        card, permission.filter(), perm.getOriginalCard().getId(), gameData, playerId)) {
                    return Optional.of(perm.getId());
                }
            }
        }
        return Optional.empty();
    }

    /** Marks a shared top-library land-play permission after the land play succeeds. */
    public void markOncePerTurnTopLibraryLandPermissionUsed(GameData gameData, UUID playerId, Card card) {
        findOncePerTurnTopLibraryPermissionSource(gameData, playerId, card)
                .ifPresent(gameData.oncePerTurnLibraryPlayPermissionsUsedThisTurn::add);
    }

    /**
     * Returns true if the player controls a permanent granting permission to cast this card from
     * their graveyard as a spell matching a {@link CastSpellsFromGraveyardPermission} filter
     * (Abandoned Sarcophagus). Lands that are not also another permanent type are not spells.
     */
    public boolean canCastViaFilteredGraveyardPermission(GameData gameData, UUID playerId, Card card) {
        return findFilteredGraveyardPermission(gameData, playerId, card).isPresent();
    }

    /**
     * Returns the permanent granting this player permission to cast {@code card} from their graveyard
     * via a {@link CastSpellsFromGraveyardPermission}, or empty if none applies. A once-per-your-turn
     * permission (Gisa and Geralf) only applies during its controller's own turn and only while that
     * permanent's use for the turn is unspent; the returned id keys that per-instance tracking.
     */
    public Optional<UUID> findFilteredGraveyardPermissionSource(GameData gameData, UUID playerId, Card card) {
        return findFilteredGraveyardPermission(gameData, playerId, card)
                .map(FilteredGraveyardPermission::sourcePermanentId);
    }

    /** Returns the matching graveyard-cast permission, including its additional cast costs. */
    public Optional<FilteredGraveyardPermission> findFilteredGraveyardPermission(
            GameData gameData, UUID playerId, Card card) {
        if (!isCastableSpellCard(card)) {
            return Optional.empty();
        }
        // The filter judges the card as it is in its owner's graveyard (a "milled this turn" or "put there from a
        // library this turn" card is tracked by that graveyard).
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        UUID cardOwnerId = graveyardOwnerId != null ? graveyardOwnerId : playerId;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    continue;
                }
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    CardEffect resolved = staticEffectConditionResolver.resolve(gameData, perm, playerId, effect);
                    if (!(resolved instanceof CastSpellsFromGraveyardPermission permission)
                            || !predicateEvaluationService.matchesCardPredicate(
                            card, permission.filter(), perm.getOriginalCard().getId(), gameData, cardOwnerId)
                            || !matchesGraveyardPermission(gameData, playerId, card, permission)) {
                        continue;
                    }
                    if (!isGraveyardPermissionAvailable(gameData, playerId, perm.getId(), permission)) {
                        continue;
                    }
                    return Optional.of(new FilteredGraveyardPermission(perm.getId(), permission));
                }
            }
        }

        for (CardEffect effect : gameData.floatingPlayerEffects(playerId, CastSpellsFromGraveyardPermission.class)) {
            if (effect instanceof CastSpellsFromGraveyardPermission permission
                    && predicateEvaluationService.matchesCardPredicate(
                    card, permission.filter(), null, gameData, cardOwnerId)) {
                return Optional.of(new FilteredGraveyardPermission(null, permission));
            }
        }

        if (gameData.planechase != null && Objects.equals(gameData.planechase.controllerId, playerId)) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof CastSpellsFromGraveyardPermission permission
                            && predicateEvaluationService.matchesCardPredicate(
                            card, permission.filter(), null, gameData, cardOwnerId)
                            && matchesGraveyardPermission(gameData, playerId, card, permission)
                            && isGraveyardPermissionAvailable(gameData, playerId, planar.getId(), permission)) {
                        return Optional.of(new FilteredGraveyardPermission(planar.getId(), permission));
                    }
                }
            }
        }

        return Optional.empty();
    }

    private boolean isGraveyardPermissionAvailable(GameData gameData, UUID playerId,
                                                     UUID sourceId,
                                                     CastSpellsFromGraveyardPermission permission) {
        return (permission.availabilityCondition() == null
                || conditionEvaluationService.isMet(gameData, permission.availabilityCondition(),
                ConditionContext.forCasting(playerId)))
                && (!permission.onlyDuringControllerTurn()
                || playerId.equals(gameData.activePlayerId))
                && (!permission.oncePerControllerTurn()
                || (playerId.equals(gameData.activePlayerId)
                && !gameData.oncePerTurnGraveyardCastPermissionsUsedThisTurn.contains(sourceId)))
                && (!permission.oncePerControllerTurnForSpell()
                || (playerId.equals(gameData.activePlayerId)
                && !gameData.oncePerTurnGraveyardSpellPermissionsUsedThisTurn.contains(sourceId)));
    }

    private boolean matchesGraveyardPermission(GameData gameData, UUID playerId, Card card,
                                               GraveyardPlayPermission permission) {
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (graveyardOwnerId == null) {
            return false;
        }
        if (!permission.graveyardScope().graveyardOwners(gameData.orderedPlayerIds, playerId)
                .contains(graveyardOwnerId)
                && !isTreatedAsInPlayersGraveyardThisTurn(gameData, playerId, card, graveyardOwnerId)) {
            return false;
        }
        return (!permission.onlyCardsPutIntoGraveyardFromLibraryThisTurn()
                || gameData.cardsPutIntoGraveyardFromLibraryThisTurn
                .getOrDefault(graveyardOwnerId, Set.of()).contains(card.getId()));
    }

    /**
     * Whether a turn-wide grant (Shaman's Trance) lets the player treat this card, in another
     * graveyard, as though it were in their own graveyard, so that their own graveyard permissions
     * (Crucible of Worlds) apply to it.
     */
    private boolean isTreatedAsInPlayersGraveyardThisTurn(GameData gameData, UUID playerId, Card card,
                                                         UUID graveyardOwnerId) {
        return gameData.graveyardPlayFilterPermissions().stream()
                .anyMatch(grant -> grant.playerId().equals(playerId)
                        && grant.scope().graveyardOwners(gameData.orderedPlayerIds, playerId)
                        .contains(graveyardOwnerId)
                        && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null));
    }

    /**
     * Marks a once-per-your-turn graveyard cast permission as spent for this turn. No-op for
     * unlimited permissions (Abandoned Sarcophagus), which are not tracked.
     */
    public void markFilteredGraveyardPermissionUsed(GameData gameData, UUID playerId, UUID permanentId) {
        markOncePerTurnGraveyardPermissionUsed(gameData, playerId, permanentId, false);
    }

    /** Marks a once-per-turn land-from-graveyard permission as spent for this turn. */
    public void markGraveyardLandPermissionUsed(GameData gameData, UUID playerId, UUID permanentId) {
        markOncePerTurnGraveyardPermissionUsed(gameData, playerId, permanentId, true);
    }

    private void markOncePerTurnGraveyardPermissionUsed(GameData gameData, UUID playerId, UUID permanentId,
                                                        boolean land) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            Optional<Permanent> source = battlefield.stream()
                    .filter(perm -> perm.getId().equals(permanentId))
                    .findFirst();
            if (source.isPresent()) {
                Permanent perm = source.get();
                List<GraveyardPlayPermission> permissions = perm.getCard().getStaticEffects().stream()
                        .map(effect -> staticEffectConditionResolver.resolve(gameData, perm, playerId, effect))
                        .filter(GraveyardPlayPermission.class::isInstance)
                        .map(GraveyardPlayPermission.class::cast)
                        .toList();
                markGraveyardPermissionsUsed(gameData, permanentId, land, permissions);
                return;
            }
        }
        if (gameData.planechase != null && Objects.equals(gameData.planechase.controllerId, playerId)) {
            List<GraveyardPlayPermission> permissions = gameData.planechase.faceUp.stream()
                    .filter(planar -> planar.getId().equals(permanentId))
                    .flatMap(planar -> planar.getCard().getStaticEffects().stream())
                    .filter(GraveyardPlayPermission.class::isInstance)
                    .map(GraveyardPlayPermission.class::cast)
                    .toList();
            markGraveyardPermissionsUsed(gameData, permanentId, land, permissions);
        }
    }

    private void markGraveyardPermissionsUsed(GameData gameData, UUID sourceId, boolean land,
                                             List<GraveyardPlayPermission> permissions) {
        if (permissions.stream().anyMatch(GraveyardPlayPermission::oncePerControllerTurn)) {
            gameData.oncePerTurnGraveyardCastPermissionsUsedThisTurn.add(sourceId);
        }
        if (land && permissions.stream().anyMatch(GraveyardPlayPermission::oncePerControllerTurnForLand)) {
            gameData.oncePerTurnGraveyardLandPermissionsUsedThisTurn.add(sourceId);
        }
        if (!land && permissions.stream().anyMatch(GraveyardPlayPermission::oncePerControllerTurnForSpell)) {
            gameData.oncePerTurnGraveyardSpellPermissionsUsedThisTurn.add(sourceId);
        }
    }

    private static boolean isCastableSpellCard(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)
                || card.hasType(CardType.EMBLEM)) {
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
     * CastPermanentSpellsFromGraveyardEffect, still has its abilities, and has an unused permanent type slot
     * for the given card, or empty if none. Permissions only work during the controller's turn.
     * The returned UUID is used to key per-instance graveyard cast tracking.
     */
    public Optional<UUID> findGraveyardCastSourcePermanentId(GameData gameData, UUID playerId, Card card) {
        if (!playerId.equals(gameData.activePlayerId)) return Optional.empty();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return Optional.empty();
        for (Permanent perm : battlefield) {
            if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                if (effect instanceof CastPermanentSpellsFromGraveyardEffect
                        && hasUnusedPermanentTypeSlot(card,
                        gameData.permanentTypesCastFromGraveyardThisTurn.getOrDefault(perm.getId(), Set.of()))) {
                    return Optional.of(perm.getId());
                }
            }
        }
        return Optional.empty();
    }

    public boolean hasGrantedGraveyardCardCastPermission(GameData gameData, Card card, UUID playerId) {
        return gameData.graveyardCardCastPermission(card.getId(), playerId).isPresent();
    }

    public boolean hasGrantedGraveyardAdventureCastPermission(GameData gameData, Card card, UUID playerId) {
        return gameData.graveyardAdventureCastPermission(card.getId())
                .filter(permission -> playerId.equals(permission.castingPlayerId())).isPresent();
    }

    public boolean hasGraveyardPlayPermission(GameData gameData, Card card, UUID playerId) {
        return hasGraveyardPlayPermission(gameData, card, playerId, true);
    }

    /**
     * Like {@link #hasGraveyardPlayPermission(GameData, Card, UUID)} but ignores the turn-wide
     * "play from other players' graveyards" grant (Shaman's Trance), which only lets a card be cast
     * as though it were in the caster's graveyard and so does not replace its flashback cost.
     */
    public boolean hasGraveyardPlayPermissionOtherThanOtherGraveyardGrant(
            GameData gameData, Card card, UUID playerId) {
        return hasGraveyardPlayPermission(gameData, card, playerId, false);
    }

    private boolean hasGraveyardPlayPermission(GameData gameData, Card card, UUID playerId,
                                               boolean includeOtherGraveyardGrant) {
        UUID permittedPlayer = gameData.graveyardPlayPermittedPlayer(card.getId());
        if (permittedPlayer != null && permittedPlayer.equals(playerId)) {
            return true;
        }
        if (hasGraveyardCastFilterPermission(gameData, card, playerId)) {
            return true;
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (includeOtherGraveyardGrant && graveyardOwnerId != null
                && gameData.graveyardPlayFilterPermissions().stream()
                .anyMatch(permission -> permission.playerId().equals(playerId)
                        && permission.scope().graveyardOwners(gameData.orderedPlayerIds, playerId)
                        .contains(graveyardOwnerId)
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null))) {
            return true;
        }
        if (card.hasType(CardType.LAND) && canPlayLandsFromGraveyard(gameData, playerId, card)) {
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

    private boolean cardWasPutIntoGraveyardFromLibraryThisTurn(GameData gameData, Card card) {
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        return graveyardOwnerId != null
                && gameData.cardsPutIntoGraveyardFromLibraryThisTurn
                .getOrDefault(graveyardOwnerId, Set.of())
                .contains(card.getId());
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
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        return gameData.graveyardCastFilterPermissions().stream()
                .filter(permission -> permission.playerId().equals(playerId)
                        && graveyardCastFilterPermissionAppliesToCard(permission, graveyardOwnerId)
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null))
                .sorted((first, second) -> Boolean.compare(first.singleUse(), second.singleUse()))
                .findFirst();
    }

    public void consumeGraveyardCastFilterPermission(
            GameData gameData, GameData.GraveyardCastFilterPermission permission) {
        if (permission != null && permission.singleUse()) {
            gameData.endGraveyardCastFilterPermission(permission);
        }
    }

    public boolean graveyardCastFilterPermissionExiles(GameData gameData, Card card, UUID playerId) {
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        return gameData.graveyardCastFilterPermissions().stream()
                .anyMatch(permission -> permission.playerId().equals(playerId)
                        && graveyardCastFilterPermissionAppliesToCard(permission, graveyardOwnerId)
                        && permission.exileInsteadOfGraveyard()
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null));
    }

    private boolean graveyardCastFilterPermissionAppliesToCard(
            GameData.GraveyardCastFilterPermission permission, UUID graveyardOwnerId) {
        if (permission.anyGraveyard()) {
            return true;
        }
        if (permission.graveyardOwnerId() != null) {
            return permission.graveyardOwnerId().equals(graveyardOwnerId);
        }
        return true;
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
            if (gameQueryService.hasLostAllAbilities(gameData, permanent)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                if (effect instanceof GrantMayhemToGraveyardCardsEffect mayhem
                        && predicateEvaluationService.matchesCardPredicate(
                                card, mayhem.filter(), null, gameData, playerId)) {
                    return Optional.of(new GraveyardCast(
                            null, null, List.of(), new CardDiscardedThisTurn()));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<FlashbackCast> findGrantedFlashback(GameData gameData, UUID playerId, Card card) {
        if (!isCastableSpellCard(card)) {
            return Optional.empty();
        }
        if (!playerId.equals(gameQueryService.findGraveyardOwnerById(gameData, card.getId()))) {
            return Optional.empty();
        }
        Optional<FlashbackCast> fromStatic = findStaticGrantedFlashback(gameData, playerId, card);
        return fromStatic.isPresent() ? fromStatic : findEmblemGrantedFlashback(gameData, playerId, card);
    }

    /**
     * True when the card's flashback option comes only from an emblem. Such a spell is cast for its
     * mana cost and the emblem's own exile-instead-of-graveyard replacement applies; it is not cast
     * with flashback (CR 702.34a), so it is not treated as a flashback cast.
     */
    public boolean isFlashbackGrantedOnlyByEmblem(GameData gameData, UUID playerId, Card card) {
        return findGrantedFlashback(gameData, playerId, card).isPresent()
                && findStaticGrantedFlashback(gameData, playerId, card).isEmpty();
    }

    private Optional<FlashbackCast> findStaticGrantedFlashback(GameData gameData, UUID playerId, Card card) {
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (gameQueryService.hasLostAllAbilities(gameData, permanent)) {
                    continue;
                }
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    CardEffect resolved = staticEffectConditionResolver.resolve(
                            gameData, permanent, sourceControllerId, effect);
                    if (resolved instanceof GrantFlashbackToGraveyardCardsEffect grant
                            && (grant.allGraveyards() || playerId.equals(sourceControllerId))
                            && predicateEvaluationService.matchesCardPredicate(
                            card, grant.filter(), null, gameData, playerId)) {
                        Optional<FlashbackCast> option = flashbackOption(card, grant);
                        if (option.isPresent()) return option;
                    }
                }
            }
        }
        if (gameData.planechase != null) {
            UUID sourceControllerId = gameData.planechase.controllerId;
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof GrantFlashbackToGraveyardCardsEffect grant
                            && (grant.allGraveyards() || playerId.equals(sourceControllerId))
                            && predicateEvaluationService.matchesCardPredicate(
                            card, grant.filter(), null, gameData, playerId)) {
                        Optional<FlashbackCast> option = flashbackOption(card, grant);
                        if (option.isPresent()) return option;
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<FlashbackCast> findEmblemGrantedFlashback(GameData gameData, UUID playerId, Card card) {
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(playerId)) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof EmblemGrantsCastingOptionEffect grant
                        && grant.cardTypes().stream().anyMatch(card::hasType)
                        && grant.castingOption(card).orElse(null) instanceof FlashbackCast flashback) {
                    return Optional.of(flashback);
                }
            }
        }
        return Optional.empty();
    }

    public boolean hasGrantedRetrace(GameData gameData, UUID playerId, Card card) {
        if (!isCastableSpellCard(card)
                || !playerId.equals(gameQueryService.findGraveyardOwnerById(gameData, card.getId()))) {
            return false;
        }
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(playerId)) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof EmblemGrantsCastingOptionEffect grant
                        && grant.cardTypes().stream().anyMatch(card::hasType)
                        && grant.castingOption(card).orElse(null) instanceof Retrace) {
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<FlashbackCast> flashbackOption(Card card, GrantFlashbackToGraveyardCardsEffect grant) {
        String flashbackCost = grant.flashbackCost() != null ? grant.flashbackCost() : card.getManaCost();
        return flashbackCost == null ? Optional.empty() : Optional.of(new FlashbackCast(flashbackCost));
    }

    public boolean hasGrantedFlashback(GameData gameData, UUID playerId, Card card) {
        return findGrantedFlashback(gameData, playerId, card).isPresent();
    }

    /**
     * Bösium Strip: until end of turn, the player may cast the top card of their graveyard if it is
     * an instant or sorcery (normal mana cost; exile instead of graveyard on resolution).
     */
    public boolean canCastTopInstantOrSorceryFromGraveyard(GameData gameData, UUID playerId, Card card) {
        return canCastTopInstantOrSorceryFromGraveyard(gameData, playerId, card, card);
    }

    public boolean canCastTopInstantOrSorceryFromGraveyard(GameData gameData, UUID playerId,
                                                         Card card, Card castingFace) {
        if (!gameData.hasFloatingEffectOnPlayer(playerId, MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect.class)) {
            return false;
        }
        if (!castingFace.hasType(CardType.INSTANT) && !castingFace.hasType(CardType.SORCERY)) {
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
        for (LibraryTopGrant grant : libraryTopGrants(gameData, playerId)) {
            if (grant.effect() instanceof AllowCastFromTopOfLibraryEffect allow
                    && !isOncePerTurnCastPermissionUsed(gameData, grant, allow)) {
                castableTypes.addAll(allow.castableTypes());
            }
        }
        return castableTypes;
    }

    /**
     * Returns whether the current top card has a temporary free-play permission from the library. A permission whose
     * card is no longer the top card is gone: it ends as this finds that out.
     */
    public boolean hasLibraryTopCardFreePlayPermission(GameData gameData, UUID playerId, Card card) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        boolean granted = false;
        for (FloatingContinuousEffect fe : gameData.floatingEffectsOnPlayer(playerId,
                RevealTopCardGrantFreePlayUntilEndOfTurnEffect.class)) {
            UUID permittedId = ((RevealTopCardGrantFreePlayUntilEndOfTurnEffect) fe.effect()).cardId();
            if (deck == null || deck.isEmpty() || !permittedId.equals(deck.getFirst().getId())) {
                gameData.endFloatingEffect(fe);
            } else if (permittedId.equals(card.getId())) {
                granted = true;
            }
        }
        return granted;
    }

    /**
     * Returns whether the owner may privately look at their own top card via a static ability of a
     * permanent they control, including one granted to it by another permanent (such as an equipment
     * granting the equipped creature the ability), or a floating copy a resolution gave them ("until end of turn, you
     * may look at the top card of your library any time"). A card in the library has no abilities that function there,
     * so the card on top of the library does not grant this permission to itself.
     */
    public boolean mayLookAtOwnLibraryTop(GameData gameData, UUID playerId) {
        if (gameData.hasFloatingEffectOnPlayer(playerId, LookAtTopCardOfOwnLibraryEffect.class)) {
            return true;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .filter(permanent -> !permanent.isFaceDown())
                .filter(permanent -> !gameQueryService.hasLostPrintedAbilities(gameData, permanent))
                .flatMap(permanent -> Stream.concat(
                        gameQueryService.getActiveStaticEffects(gameData, permanent, playerId).stream(),
                        gameQueryService.getGrantedEffects(gameData, permanent).stream()))
                .anyMatch(effect -> effect instanceof LookAtTopCardOfOwnLibraryEffect
                        || effect instanceof ConditionalEffect conditional
                        && conditional.wrapped() instanceof LookAtTopCardOfOwnLibraryEffect
                        && conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forCasting(playerId)));
    }

    /** Returns whether the caster may use the specified opponent's current library top. */
    public boolean hasLibraryTopPermission(GameData gameData, UUID castingPlayerId, UUID libraryOwnerId) {
        return gameData.floatingEffectsOnPlayer(castingPlayerId,
                        GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect.class).stream()
                .anyMatch(fe -> libraryOwnerId.equals(
                        ((GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect) fe.effect())
                                .libraryOwnerId()));
    }

    /**
     * Returns the library whose top card is exposed to the player, preferring temporary access grants: the first of
     * {@link #findLibraryTopOwners}, or {@code null} when none is.
     */
    public UUID findLibraryTopOwner(GameData gameData, UUID playerId) {
        List<UUID> owners = findLibraryTopOwners(gameData, playerId);
        return owners.isEmpty() ? null : owners.getFirst();
    }

    /**
     * Returns every nonempty library whose top card the player may play from: each library a temporary grant opens
     * to them, then every player's library while they control a plane letting them play from the top of any player's
     * library (Windriddle Palaces), and their own library. Each permission covers only the library it names: see
     * {@link #ownLibraryTopPermissionCovers}.
     */
    public List<UUID> findLibraryTopOwners(GameData gameData, UUID playerId) {
        Set<UUID> owners = new LinkedHashSet<>();
        for (UUID libraryOwnerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(libraryOwnerId) && hasLibraryTopPermission(gameData, playerId, libraryOwnerId)
                    && hasNonemptyLibrary(gameData, libraryOwnerId)) {
                owners.add(libraryOwnerId);
            }
        }
        if (hasAnyLibraryTopPermission(gameData, playerId)) {
            for (UUID libraryOwnerId : gameData.orderedPlayerIds) {
                if (hasNonemptyLibrary(gameData, libraryOwnerId)) {
                    owners.add(libraryOwnerId);
                }
            }
        }
        if (hasNonemptyLibrary(gameData, playerId)) {
            owners.add(playerId);
        }
        return List.copyOf(owners);
    }

    /**
     * Returns the library whose top card the player plays: {@code requestedOwnerId} when the request names one,
     * which must be one of {@link #findLibraryTopOwners}, and otherwise {@link #findLibraryTopOwner}.
     */
    public UUID resolveLibraryTopOwner(GameData gameData, UUID playerId, UUID requestedOwnerId) {
        if (requestedOwnerId == null) {
            return findLibraryTopOwner(gameData, playerId);
        }
        if (!findLibraryTopOwners(gameData, playerId).contains(requestedOwnerId)) {
            throw new IllegalStateException("You can't play cards from the top of that player's library");
        }
        return requestedOwnerId;
    }

    /**
     * Returns whether a permission to play from the top of the player's own library (Future Sight, Courser of
     * Kruphix, Bolas's Citadel, a "you may play cards from the top of your library this turn" effect) may cover
     * {@code card}: not when the card is the top card of another player's library, which only a grant on that
     * library or a permission covering any player's library opens (Xanathar, Guild Kingpin).
     */
    private boolean ownLibraryTopPermissionCovers(GameData gameData, UUID playerId, Card card) {
        for (UUID libraryOwnerId : gameData.orderedPlayerIds) {
            if (playerId.equals(libraryOwnerId)) {
                continue;
            }
            List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
            if (deck != null && !deck.isEmpty() && deck.getFirst().getId().equals(card.getId())) {
                return false;
            }
        }
        return true;
    }

    private boolean hasNonemptyLibrary(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        return deck != null && !deck.isEmpty();
    }

    private boolean hasAnyLibraryTopPermission(GameData gameData, UUID playerId) {
        if (gameData.planechase == null || !Objects.equals(gameData.planechase.controllerId, playerId)) {
            return false;
        }
        return gameData.planechase.faceUp.stream()
                .flatMap(planar -> planar.getCard().getStaticEffects().stream())
                .anyMatch(AllowPlayFromAnyLibraryTopEffect.class::isInstance);
    }

    public boolean hasLibraryTopPermissionForCard(GameData gameData, UUID castingPlayerId, Card card) {
        for (FloatingContinuousEffect fe : gameData.floatingEffectsOnPlayer(castingPlayerId,
                GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect.class)) {
            UUID libraryOwnerId =
                    ((GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect) fe.effect())
                            .libraryOwnerId();
            List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
            if (deck != null && !deck.isEmpty() && deck.getFirst().getId().equals(card.getId())) {
                return true;
            }
        }
        return false;
    }

    /** Builds the characteristics used when announcing a face-down creature spell. */
    public static Card faceDownSpellCharacteristics(Card card) {
        return faceDownSpellCharacteristics(card, card.hasKeyword(Keyword.DISGUISE));
    }

    /**
     * {@code spell} as effects that care about a spell's characteristics see it: one cast face down has only the
     * characteristics listed for it (CR 708.2, 708.4), so the same object with those as its card.
     */
    public static StackEntry spellAsEffectsSeeIt(StackEntry spell) {
        if (spell == null || !spell.isCastFaceDown() || spell.getCard() == null) {
            return spell;
        }
        StackEntry seen = new StackEntry(spell);
        seen.setCastCard(faceDownSpellCharacteristics(spell.getCard(), spell.isCastWithDisguise()));
        return seen;
    }

    private static Card faceDownSpellCharacteristics(Card card, boolean disguise) {
        Card face = new Card();
        face.setName("");
        face.setType(CardType.CREATURE);
        face.setPower(2);
        face.setToughness(2);
        if (disguise) {
            face.setKeywords(Set.of(Keyword.WARD));
        }
        Card characteristics = card.createRuntimeCopyWithFace(face);
        // The printing would name the card wherever these characteristics are shown (CR 708.5).
        characteristics.setSetCode(null);
        characteristics.setCollectorNumber(null);
        return characteristics;
    }

    /**
     * Builds the characteristics used when casting an adventurer card as its Adventure: the card's
     * identity with only the Adventure's characteristics, so every castability check (a top-library
     * permission's card types included) sees the Adventure alone (CR 716.3a). Returns {@code null}
     * when the card has no Adventure that is an instant or sorcery.
     */
    public static Card adventureSpellCharacteristics(Card card) {
        Card adventure = card.getBackFaceCard();
        if (adventure == null || card.getCastingOption(AdventureCast.class).isEmpty()
                || (!adventure.hasType(CardType.INSTANT) && !adventure.hasType(CardType.SORCERY))) {
            return null;
        }
        return card.createRuntimeCopyWithFace(adventure);
    }

    /** Returns whether an adventurer card may be cast as its Adventure from the top of the library. */
    public boolean canCastFromTopOfLibraryAsAdventure(GameData gameData, UUID playerId, Card card) {
        Card adventure = adventureSpellCharacteristics(card);
        return adventure != null && canCastFromTopOfLibrary(gameData, playerId, adventure);
    }

    /** Returns whether a specific card may be cast from the top of the player's library. */
    public boolean canCastFromTopOfLibrary(GameData gameData, UUID playerId, Card card) {
        return canCastFromTopOfLibrary(gameData, playerId, card, null);
    }

    /**
     * As {@link #canCastFromTopOfLibrary(GameData, UUID, Card)}, checking a permission's mana value
     * filter with the {@code X} chosen for the cast (Glarb: X must make the mana value 4 or more).
     * A {@code null} X means it isn't chosen yet, so a card with {X} may still qualify.
     */
    public boolean canCastFromTopOfLibrary(GameData gameData, UUID playerId, Card card, Integer xValue) {
        if (!card.hasType(CardType.LAND) && hasLibraryTopCardFreePlayPermission(gameData, playerId, card)) {
            return true;
        }
        if (!card.hasType(CardType.LAND) && hasLibraryTopPermissionForCard(gameData, playerId, card)) {
            return true;
        }
        return canCastFromTopOfLibraryNormally(gameData, playerId, card, xValue)
                || canCastFromTopOfLibraryByPayingLifeEqualToManaValue(gameData, playerId, card);
    }

    /**
     * Returns whether a specific card may be cast from the library top paying life equal to its mana value rather than
     * its mana cost: an {@link AllowCastFromTopOfLibraryEffect#payingLifeEqualToManaValue()} permission covers it.
     */
    public boolean canCastFromTopOfLibraryByPayingLifeEqualToManaValue(
            GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.LAND) || !ownLibraryTopPermissionCovers(gameData, playerId, card)) return false;
        for (LibraryTopGrant grant : libraryTopGrants(gameData, playerId)) {
            if (grant.effect() instanceof AllowCastFromTopOfLibraryEffect allow
                    && allow.paysLifeEqualToManaValue()
                    && matchesTopLibraryPermission(gameData, playerId, grant, card, allow)
                    && !isOncePerTurnCastPermissionUsed(gameData, grant, allow)) {
                return true;
            }
        }
        return false;
    }

    /** Returns whether a specific card may be cast from the top by paying its normal cost. */
    public boolean canCastFromTopOfLibraryNormally(GameData gameData, UUID playerId, Card card) {
        return canCastFromTopOfLibraryNormally(gameData, playerId, card, null);
    }

    private boolean canCastFromTopOfLibraryNormally(GameData gameData, UUID playerId, Card card, Integer xValue) {
        return hasSourceCardTopLibraryCastPermission(gameData, playerId, card)
                || hasAnyLibraryTopPermission(gameData, playerId)
                || findTopLibraryCastPermissionSourceInternal(gameData, playerId, card, xValue) != null;
    }

    private boolean hasSourceCardTopLibraryCastPermission(GameData gameData, UUID playerId, Card card) {
        if (card == null || card.hasType(CardType.LAND)) return false;
        List<Card> deck = gameData.playerDecks.get(playerId);
        return deck != null && !deck.isEmpty()
                && deck.getFirst().getId().equals(card.getId())
                && card.getStaticEffects().stream()
                .anyMatch(TopLibraryCardCastPermissionEffect.class::isInstance);
    }

    /** Returns the source permanent granting a normal-cost top-library cast. */
    public Optional<UUID> findTopLibraryCastPermissionSource(GameData gameData, UUID playerId, Card card) {
        TopLibraryCastPermissionSource source = findTopLibraryCastPermissionSourceInternal(gameData, playerId, card, null);
        return source == null ? Optional.empty() : Optional.ofNullable(source.permanentId());
    }

    /**
     * Returns the keywords the selected top-library permission gives the spell until end of turn
     * ("If you cast a creature spell this way, it gains haste"). Empty when the spell is cast
     * through a permission that grants none.
     */
    public Set<Keyword> findTopLibraryCastGrantedKeywords(GameData gameData, UUID playerId, Card card) {
        TopLibraryCastPermissionSource source = findTopLibraryCastPermissionSourceInternal(gameData, playerId, card, null);
        return source == null ? Set.of() : source.grantedKeywords();
    }

    /** Marks the selected once-each-turn top-library permission as used by a successful cast. */
    public void markTopLibraryCastPermissionUsed(GameData gameData, UUID playerId, Card card) {
        TopLibraryCastPermissionSource source = findTopLibraryCastPermissionSourceInternal(gameData, playerId, card, null);
        if (source != null && source.enterWithCounterCount() > 0 && card.hasType(CardType.CREATURE)) {
            gameData.spellAdditionalEnterCounters.merge(card.getId(), source.enterWithCounterCount(), Integer::sum);
        }
        if (source != null && source.oncePerTurn()) {
            if (source.sharedLandAndCast()) {
                gameData.oncePerTurnLibraryPlayPermissionsUsedThisTurn.add(source.permanentId());
            } else {
                gameData.oncePerTurnLibraryCastPermissionsUsedThisTurn.add(source.permanentId());
            }
        }
    }

    private TopLibraryCastPermissionSource findTopLibraryCastPermissionSourceInternal(
            GameData gameData, UUID playerId, Card card, Integer xValue) {
        if (card.hasType(CardType.LAND) || !ownLibraryTopPermissionCovers(gameData, playerId, card)) return null;
        TopLibraryCastPermissionSource oncePerTurnFallback = null;
        TopLibraryCastPermissionSource sharedLandAndCastFallback = null;
        TopLibraryCastPermissionSource unlimitedFallback = null;
        for (LibraryTopGrant grant : libraryTopGrants(gameData, playerId)) {
            if (grant.effect() instanceof PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect permission
                    && !gameData.oncePerTurnLibraryPlayPermissionsUsedThisTurn.contains(grant.sourceId())
                    && predicateEvaluationService.matchesCardPredicate(
                    card, permission.filter(), grant.sourceCardId(), gameData, playerId)) {
                sharedLandAndCastFallback = new TopLibraryCastPermissionSource(
                        grant.sourceId(), true, 0, true);
                continue;
            }
            if (grant.effect() instanceof AllowCastFromTopOfLibraryEffect allow
                    && allow.alternativeCost() == null
                    && matchesTopLibraryPermission(gameData, playerId, grant, card, allow, xValue)
                    && canPayTopLibraryAdditionalCosts(gameData, playerId, allow)) {
                if (grant.source() == null) {
                    // A permission a resolution gave the player is used before any permanent's.
                    return new TopLibraryCastPermissionSource(null, false, allow.enterWithCounterCount(), false,
                            allow.grantedKeywords());
                }
                if (!allow.oncePerTurn()
                        && (unlimitedFallback == null || !allow.grantedKeywords().isEmpty()
                        || unlimitedFallback.grantedKeywords().isEmpty())) {
                    unlimitedFallback = new TopLibraryCastPermissionSource(
                            grant.sourceId(), false, allow.enterWithCounterCount(), false,
                            allow.grantedKeywords());
                }
                if (!gameData.oncePerTurnLibraryCastPermissionsUsedThisTurn.contains(grant.sourceId())
                        && oncePerTurnFallback == null) {
                    oncePerTurnFallback = new TopLibraryCastPermissionSource(
                            grant.sourceId(), true, allow.enterWithCounterCount(), false);
                }
            }
        }
        return sharedLandAndCastFallback != null ? sharedLandAndCastFallback
                : unlimitedFallback != null ? unlimitedFallback : oncePerTurnFallback;
    }

    private record TopLibraryCastPermissionSource(UUID permanentId, boolean oncePerTurn,
                                                  int enterWithCounterCount,
                                                  boolean sharedLandAndCast,
                                                  Set<Keyword> grantedKeywords) {
        TopLibraryCastPermissionSource(UUID permanentId, boolean oncePerTurn, int enterWithCounterCount,
                                       boolean sharedLandAndCast) {
            this(permanentId, oncePerTurn, enterWithCounterCount, sharedLandAndCast, Set.of());
        }
    }

    /**
     * Returns the additional counter cost imposed by the applicable top-library permission. A
     * present zero means the card is castable without a counter cost; an empty result means no
     * applicable permission was found.
     */
    public OptionalInt findAdditionalCounterCostFromTopOfLibrary(GameData gameData, UUID playerId,
                                                                  Card card) {
        if (card.hasType(CardType.LAND) || !ownLibraryTopPermissionCovers(gameData, playerId, card)) {
            return OptionalInt.empty();
        }
        OptionalInt lowestCounterCost = OptionalInt.empty();
        for (LibraryTopGrant grant : libraryTopGrants(gameData, playerId)) {
            if (!(grant.effect() instanceof AllowCastFromTopOfLibraryEffect allow)
                    || allow.alternativeCost() != null
                    || !matchesTopLibraryPermission(gameData, playerId, grant, card, allow)
                    || isOncePerTurnCastPermissionUsed(gameData, grant, allow)) {
                continue;
            }
            if (allow.additionalCosts().isEmpty()) return OptionalInt.of(0);
            OptionalInt counterCost = allow.counterRemovalCost();
            if (counterCost.isPresent()
                    && (lowestCounterCost.isEmpty()
                    || counterCost.getAsInt() < lowestCounterCost.getAsInt())) {
                lowestCounterCost = counterCost;
            }
        }
        return lowestCounterCost;
    }

    private boolean canPayTopLibraryAdditionalCosts(GameData gameData, UUID playerId,
                                                     AllowCastFromTopOfLibraryEffect permission) {
        if (permission.additionalCosts().isEmpty()) return true;
        OptionalInt counterCost = permission.counterRemovalCost();
        if (counterCost.isPresent()) {
            return hasSufficientCountersAmongControlledCreatures(gameData, playerId, counterCost.getAsInt());
        }
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        for (CastingCost cost : permission.additionalCosts()) {
            if (!(cost instanceof SacrificePermanentsCost sacrificeCost)) {
                return false;
            }
            long matching = battlefield.stream()
                    .filter(permanent -> gameQueryService.canChooseToSacrificeForCosts(gameData, permanent))
                    .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            permanent, sacrificeCost.filter(),
                            FilterContext.of(gameData).withSourceControllerId(playerId)))
                    .count();
            if (matching < sacrificeCost.count()) {
                return false;
            }
        }
        return true;
    }

    /** Returns the additional casting costs imposed by the selected top-library permission. */
    public List<CastingCost> findTopLibraryAdditionalCosts(GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.LAND)
                || !ownLibraryTopPermissionCovers(gameData, playerId, card)
                || hasAnyLibraryTopPermission(gameData, playerId)
                || hasLibraryTopPermissionForCard(gameData, playerId, card)) {
            return List.of();
        }
        for (LibraryTopGrant grant : libraryTopGrants(gameData, playerId)) {
            if (!(grant.effect() instanceof AllowCastFromTopOfLibraryEffect allow)
                    || allow.alternativeCost() != null
                    || !matchesTopLibraryPermission(gameData, playerId, grant, card, allow)
                    || isOncePerTurnCastPermissionUsed(gameData, grant, allow)
                    || !canPayTopLibraryAdditionalCosts(gameData, playerId, allow)) {
                continue;
            }
            return allow.additionalCosts();
        }
        return List.of();
    }

    /** Marks the once-each-turn top-library permission used by a successful normal-cost cast. */
    public void markOncePerTurnLibraryCastPermissionUsed(GameData gameData, UUID playerId, Card card) {
        markTopLibraryCastPermissionUsed(gameData, playerId, card);
    }

    private boolean matchesTopLibraryPermission(GameData gameData, UUID playerId, LibraryTopGrant grant,
                                                Card card, AllowCastFromTopOfLibraryEffect permission) {
        return matchesTopLibraryPermission(gameData, playerId, grant, card, permission, null);
    }

    /**
     * A {@code null} X is not chosen yet: a card with {X} in its cost is checked as if a large X were
     * chosen, so a mana value filter that some X satisfies still offers the cast.
     */
    private boolean matchesTopLibraryPermission(GameData gameData, UUID playerId, LibraryTopGrant grant,
                                                Card card, AllowCastFromTopOfLibraryEffect permission,
                                                Integer xValue) {
        Integer filterXValue = xValue != null ? xValue
                : card.getParsedManaCost() != null && card.getParsedManaCost().getXSymbolCount() > 0
                ? UNCHOSEN_X_FOR_PERMISSION_CHECK : null;
        return permission.matches(card)
                || (card.getType() != CardType.LAND && permission.filter() != null
                && predicateEvaluationService.matchesCardPredicate(
                card, permission.filter(), grant.sourceCardId(), gameData, playerId,
                null, null, filterXValue));
    }

    /**
     * Vizier of the Menagerie etc.: returns true if the player controls a permanent that lets them
     * spend mana of any type to cast spells sharing one of this card's types (e.g. creature spells). Xanathar's
     * library-top grant lets mana be spent only as though it were mana of any color
     * ({@link #hasLibraryTopPermissionForCard}), so it is not one of these (CR 107.4c).
     */
    public boolean canSpendAnyManaTypeToCast(GameData gameData, UUID playerId, Card card) {
        if (canSpendAnyManaTypeFromCard(card)) {
            return true;
        }
        if (gameData.hasPendingAnyManaTypeForNextSpellThisTurn(playerId)) {
            return true;
        }
        return canSpendAnyManaTypeFromBattlefield(gameData, playerId, card);
    }

    /** Returns whether the card itself carries a perpetual any-color casting permission. */
    public boolean canSpendAnyManaTypeFromCard(Card card) {
        return card != null && card.getStaticEffects().stream()
                .anyMatch(PerpetualAnyManaTypeToCastSelfEffect.class::isInstance);
    }

    /** Whether a permanent the player controls lets them spend mana of any type to cast {@code card} (Vizier). */
    public boolean canSpendAnyManaTypeFromBattlefield(GameData gameData, UUID playerId, Card card) {
        return hasBattlefieldSpendPermission(gameData, playerId, card, false);
    }

    /**
     * Whether a permanent the player controls lets them spend mana as though it were mana of any color to cast
     * {@code card} (Oath of Nissa, Case File Auditor, Nathan Drake): unlike any type, a {C} symbol is still paid only
     * with colorless mana (CR 107.4c).
     */
    public boolean canSpendManaAsAnyColorToCastFromBattlefield(GameData gameData, UUID playerId, Card card) {
        return hasBattlefieldSpendPermission(gameData, playerId, card, true);
    }

    private boolean hasBattlefieldSpendPermission(GameData gameData, UUID playerId, Card card,
                                                  boolean asThoughAnyColor) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect resolved : gameQueryService.getActiveStaticEffects(gameData, perm, playerId)) {
                if (resolved instanceof SpendAnyManaTypeToCastEffect anyMana
                        && anyMana.asThoughAnyColor() == asThoughAnyColor
                        && predicateEvaluationService.matchesCardPredicate(card, anyMana.spellFilter(),
                        perm.getOriginalCard().getId(), gameData, playerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the set of exiled card IDs that the player can play or cast via static permissions
     * on their permanents.
     */
    public Set<UUID> getCastableExiledCardIds(GameData gameData, UUID playerId) {
        materializeWildcardExileCastPermissions(gameData);
        Set<UUID> castableIds = new HashSet<>();
        for (GameData.ExileCastPermission permission : gameData.exileCastPermissions()) {
            if (playerId.equals(permission.castingPlayerId())
                    && gameData.findExiledCard(permission.cardId()) != null) {
                castableIds.add(permission.cardId());
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (hasPlayFromAmongExiledCardsPermission(gameData, playerId, entry.card().getId(), false)) {
                castableIds.add(entry.card().getId());
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (gameData.exiledCardHasCounter(entry.card().getId(), CounterType.STASH)
                    && hasStashCounterPermission(gameData, playerId, entry.card().getId(), false)) {
                castableIds.add(entry.card().getId());
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (hasLukkaExilePermission(gameData, playerId, entry)
                    || hasCollectionCounterPermission(gameData, playerId, entry.card().getId(), false)
                    || hasFetchCounterPermission(gameData, playerId, entry.card().getId(), false)) {
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
                                    gameData, perm, sourceControllerId, permission, entry, playerId)
                                    && applies(permission, gameData, playerId, perm, entry))) {
                        castableIds.add(entry.card().getId());
                    }
                }
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (hasPlanarExilePlayPermission(gameData, playerId, entry)) {
                castableIds.add(entry.card().getId());
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (findForetoldCardCastPermission(gameData, playerId, entry) != null) {
                castableIds.add(entry.card().getId());
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
            if (gameData.exiledCardHasCounter(entry.card().getId(), CounterType.STASH)
                    && hasStashCounterPermission(gameData, playerId, entry.card().getId(), true)) {
                anyManaIds.add(entry.card().getId());
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (hasCollectionCounterPermission(gameData, playerId, entry.card().getId(), true)
                    || hasFetchCounterPermission(gameData, playerId, entry.card().getId(), true)) {
                anyManaIds.add(entry.card().getId());
            }
        }
        for (ExiledCardEntry entry : gameData.exiledCards) {
            if (gameData.exilePlayPermission(entry.card().getId()).filter(ExilePlayPermissionEffect::anyManaType).isPresent()
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
                                    && canAccessExiledEntry(gameData, perm, sourceControllerId, effect, entry, playerId)
                                    && applies(effect, gameData, playerId, perm, entry));
                    if (anyMana) {
                        anyManaIds.add(entry.card().getId());
                    }
                }
            }
        }
        return anyManaIds;
    }

    /** Whether the permanent is still on the battlefield with the static ability that grants a permission. */
    private boolean staticAbilitySourceStillGrants(GameData gameData, UUID sourcePermanentId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (sourcePermanentId.equals(permanent.getId())) {
                    return gameQueryService.hasActiveStaticEffect(gameData, permanent,
                            UbaMaskDrawReplacementEffect.class);
                }
            }
        }
        return false;
    }

    /** Returns whether the player has an active direct permission to play the exiled card. */
    public boolean hasExilePlayPermission(GameData gameData, UUID playerId, UUID cardId) {
        for (var battlefield : gameData.playerBattlefields.entrySet()) {
            for (Permanent permanent : battlefield.getValue()) {
                if (permanent.isPrepared() && cardId.equals(permanent.getPreparedSpellCardId())) {
                    return playerId.equals(battlefield.getKey());
                }
            }
        }
        ExiledCardEntry exiledCard = gameData.findExiledCard(cardId);
        if (exiledCard != null
                && !exiledCard.faceDown()
                && playerId.equals(exiledCard.ownerId())
                && gameData.hasFloatingEffectOnPlayer(playerId, AllowPlayFaceUpCardsFromExileThisTurnEffect.class)) {
            return true;
        }
        ExilePlayPermissionEffect permission = gameData.exilePlayPermission(cardId).orElse(null);
        if (permission == null || !playerId.equals(permission.holderId())) return false;
        UUID staticSourceId = permission.staticSourcePermanentId();
        if (staticSourceId != null && !staticAbilitySourceStillGrants(gameData, staticSourceId)) return false;
        if (!gameData.hasExilePlayPermissionRemaining(cardId)) return false;
        Condition condition = permission.condition();
        return condition == null || conditionEvaluationService.isMet(
                gameData, condition, ConditionContext.forCasting(playerId));
    }

    /** Returns whether the player's temporary exile permission also offers the mana-value life alternative. */
    public boolean hasExilePlayForLifeEqualToManaValuePermission(
            GameData gameData, UUID playerId, UUID cardId) {
        return gameData.exilePlayPermission(cardId).filter(ExilePlayPermissionEffect::forLifeEqualToManaValue).isPresent()
                && hasExilePlayPermission(gameData, playerId, cardId);
    }

    public boolean hasCastFromExiledWithSourcePermission(GameData gameData, UUID playerId, UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        if (findForetoldCardCastPermission(gameData, playerId, entry) != null) return true;
        if (hasPlanarExilePlayPermission(gameData, playerId, entry)) return true;
        if (hasLukkaExilePermission(gameData, playerId, entry)) return true;
        if (findTemporaryExileCastPermission(gameData, playerId, entry, false) != null) return true;
        if (hasStashCounterPermission(gameData, playerId, cardId, false)) return true;
        if (hasPlayFromAmongExiledCardsPermission(gameData, playerId, cardId, false)) return true;
        if (hasCollectionCounterPermission(gameData, playerId, cardId, false)) return true;
        if (hasFetchCounterPermission(gameData, playerId, cardId, false)) return true;
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (!perm.getId().equals(entry.sourcePermanentId())) continue;
                if (activeExileCastPermissions(gameData, perm, sourceControllerId)
                        .anyMatch(permission -> !permission.waterbendManaValue()
                                && canAccessExiledEntry(
                                gameData, perm, sourceControllerId, permission, entry, playerId)
                                && applies(permission, gameData, playerId, perm, entry))) return true;
            }
        }
        return false;
    }

    private boolean hasPlanarExilePlayPermission(GameData gameData, UUID playerId,
                                                  ExiledCardEntry entry) {
        if (entry.faceDown() || entry.sourcePermanentId() == null || gameData.planechase == null
                || !playerId.equals(gameData.activePlayerId)
                || !playerId.equals(gameData.planechase.controllerId)) {
            return false;
        }
        return gameData.planechase.faceUp.stream()
                .filter(planar -> planar.getId().equals(entry.sourcePermanentId()))
                .flatMap(planar -> planar.getCard().getStaticEffects().stream())
                .anyMatch(AllowPlayCardsExiledWithPlanarSourceEffect.class::isInstance);
    }

    /** Returns whether a temporary exile permission replaces a spell's mana cost with life. */
    public boolean hasManaValueLifeAlternativeFromExile(GameData gameData, UUID playerId,
                                                         UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        GameData.ExileCastPermission permission =
                findTemporaryExileCastPermission(gameData, playerId, entry, false);
        return permission != null && permission.payLifeEqualToManaValue();
    }

    public boolean hasManaValueLifeAlternativeFromExiledWithSource(GameData gameData,
                                                                    UUID playerId, UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null || entry.card().hasType(CardType.LAND)) return false;
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (!perm.getId().equals(entry.sourcePermanentId())) continue;
                if (activeExileCastPermissions(gameData, perm, sourceControllerId)
                        .anyMatch(permission -> permission.payLifeEqualToManaValue()
                                && canAccessExiledEntry(
                                gameData, perm, sourceControllerId, permission, entry, playerId)
                                && applies(permission, gameData, playerId, perm, entry))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasWaterbendCastFromExiledWithSourcePermission(GameData gameData, UUID playerId,
                                                                   UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (!perm.getId().equals(entry.sourcePermanentId())) continue;
                if (activeExileCastPermissions(gameData, perm, sourceControllerId)
                        .anyMatch(permission -> permission.waterbendManaValue()
                                && canAccessExiledEntry(gameData, perm, sourceControllerId, permission, entry, playerId)
                                && applies(permission, gameData, playerId, perm, entry))) {
                    return true;
                }
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
                                gameData, perm, sourceControllerId, permission, entry, playerId))
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

    /** Consumes a temporary normal-cost source-linked exile-cast grant, if present. */
    public boolean consumeTemporaryCastFromExiledWithSource(GameData gameData, UUID playerId, UUID cardId) {
        materializeWildcardExileCastPermissions(gameData);
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        GameData.ExileCastPermission permission = gameData.exileCastPermissions().stream()
                .filter(candidate -> candidate.cardId().equals(cardId))
                .filter(candidate -> candidate.sourcePermanentId().equals(entry.sourcePermanentId()))
                .filter(candidate -> candidate.castingPlayerId().equals(playerId))
                .filter(candidate -> !candidate.withoutPayingManaCost())
                .findFirst()
                .orElse(null);
        if (permission == null) return false;
        gameData.endExileCastGrant(permission.grantId());
        return true;
    }

    /**
     * Gives every card exiled with a wildcard grant's source its own one-card permission, so a card
     * exiled after the grant was created is covered too (CR 611.2c).
     */
    private void materializeWildcardExileCastPermissions(GameData gameData) {
        for (GameData.ExileCastWildcardPermission wildcard : gameData.exileCastWildcardPermissions()) {
            for (ExiledCardEntry exiled : gameData.getExiledWithPermanentEntries(
                    wildcard.sourcePermanentId(), wildcard.sourceCardId())) {
                UUID cardId = exiled.card().getId();
                boolean covered = gameData.exileCastPermissions().stream().anyMatch(permission ->
                        permission.grantId().equals(wildcard.grantId()) && permission.cardId().equals(cardId));
                if (covered
                        || wildcard.ownOnly() && !wildcard.castingPlayerId().equals(exiled.ownerId())
                        || wildcard.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                                exiled.card(), wildcard.filter(), null)) {
                    continue;
                }
                gameData.grantExileCastPermission(new GameData.ExileCastPermission(
                        wildcard.grantId(), wildcard.sourcePermanentId(), wildcard.castingPlayerId(), cardId,
                        wildcard.withoutPayingManaCost(), wildcard.putOnBottomOfOwnersLibrary()));
            }
        }
    }

    /** Marks a once-per-turn or one-shot normal-cost exile-cast permission after a successful spell cast. */
    public void markOncePerTurnExileCastPermissionUsed(GameData gameData, UUID playerId, Card card) {
        if (card == null) return;
        ExiledCardEntry entry = gameData.findExiledCard(card.getId());
        if (entry == null || entry.sourcePermanentId() == null) return;

        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent source : battlefield) {
                if (!source.getId().equals(entry.sourcePermanentId())) continue;
                List<AllowCastFromCardsExiledWithSourceEffect> permissions =
                        activeExileCastPermissions(gameData, source, sourceControllerId).toList();
                boolean hasUnlimitedPermission = permissions.stream()
                        .filter(permission -> !permission.waterbendManaValue())
                        .anyMatch(permission -> !permission.oncePerTurn() && !permission.oneShot()
                                && canAccessExiledEntry(gameData, source, sourceControllerId, permission, entry, playerId)
                                && applies(permission, gameData, playerId, source, entry));
                if (hasUnlimitedPermission) return;

                boolean hasOncePerTurnPermission = permissions.stream()
                        .filter(AllowCastFromCardsExiledWithSourceEffect::oncePerTurn)
                        .filter(permission -> !permission.waterbendManaValue())
                        .anyMatch(permission -> canAccessExiledEntry(
                                gameData, source, sourceControllerId, permission, entry, playerId)
                                && applies(permission, gameData, playerId, source, entry));
                if (hasOncePerTurnPermission && (!card.hasType(CardType.LAND)
                        || permissions.stream().anyMatch(permission -> permission.oncePerTurn()
                        && permission.countsLandPlayForOncePerTurn()
                        && canAccessExiledEntry(gameData, source, sourceControllerId,
                        permission, entry, playerId)
                        && applies(permission, gameData, playerId, source, entry)))) {
                    gameData.oncePerTurnExileCastPermissionsUsedThisTurn.add(source.getId());
                }
                boolean hasOneShotPermission = permissions.stream()
                        .filter(AllowCastFromCardsExiledWithSourceEffect::oneShot)
                        .filter(permission -> !permission.waterbendManaValue())
                        .anyMatch(permission -> canAccessExiledEntry(
                                gameData, source, sourceControllerId, permission, entry, playerId)
                                && applies(permission, gameData, playerId, source, entry));
                if (hasOneShotPermission) {
                    gameData.oneShotExileCastPermissionsUsed.add(source.getId());
                }
                return;
            }
        }
    }

    /** Marks an Evelyn-style collection-counter permission as used after a successful cast/play. */
    public boolean consumeCollectionCounterPermission(GameData gameData, UUID playerId, UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null || !gameData.exiledCardHasCounter(cardId, CounterType.COLLECTION)
                || !playerId.equals(entry.exilerId())) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent source : battlefield) {
            for (AllowCastFromCardsExiledWithSourceEffect permission : activeExileCastPermissions(
                    gameData, source, playerId).toList()) {
                if (!permission.collectionCounterOnly()
                        || (permission.filter() != null
                        && !predicateEvaluationService.matchesCardPredicate(entry.card(), permission.filter(), null))
                        || (permission.oncePerTurn()
                        && gameData.freeCastPermanentUsedThisTurn.contains(source.getId()))) {
                    continue;
                }
                if (permission.oncePerTurn()) {
                    gameData.freeCastPermanentUsedThisTurn.add(source.getId());
                }
                return true;
            }
        }
        return false;
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
        GameData.ForetoldCardCastPermission foretoldPermission =
                findForetoldCardCastPermission(gameData, playerId, entry);
        if (foretoldPermission != null) {
            if (consume && foretoldPermission.singleUse()) {
                gameData.endForetoldCardCastPermission(foretoldPermission);
            }
            return true;
        }
        GameData.ExileCastPermission temporaryPermission =
                findTemporaryExileCastPermission(gameData, playerId, entry, true);
        if (temporaryPermission != null) {
            if (consume) {
                gameData.endExileCastGrant(temporaryPermission.grantId());
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
                            || permission.waterbendManaValue()
                            || !canAccessExiledEntry(gameData, perm, sourceControllerId, permission, entry, playerId)
                            || !applies(permission, gameData, playerId, perm, entry)) continue;
                    if (consume) {
                        if (permission.oncePerTurn()) {
                            gameData.freeCastPermanentUsedThisTurn.add(perm.getId());
                        }
                        if (permission.oneShot()) {
                            gameData.oneShotExileCastPermissionsUsed.add(perm.getId());
                        }
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
        if (hasPlayFromAmongExiledCardsPermission(gameData, playerId, cardId, false)) {
            return OptionalInt.of(0);
        }
        if (hasCollectionCounterPermission(gameData, playerId, cardId, false)) {
            return OptionalInt.of(0);
        }
        if (hasFetchCounterPermission(gameData, playerId, cardId, false)) {
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

    /**
     * Whether a permission that is lost for good once its source changes control (Taster of Wares: "for as long
     * as you control this creature") still applies; permissions with any other scope always do.
     */
    private boolean keepsAccessAfterControlChange(AllowCastFromCardsExiledWithSourceEffect permission,
                                                  Permanent source) {
        return permission.accessScope() != ExileAccessScope.CONTROLLER_WITHOUT_CONTROL_CHANGE
                || source.getControlChangeSequence() == 0;
    }

    private OptionalInt additionalCounterCostFromSource(GameData gameData, UUID playerId,
                                                        Permanent source, ExiledCardEntry entry) {
        if (activeExileCastPermissions(gameData, source, playerId)
                .noneMatch(permission -> !permission.waterbendManaValue()
                        && keepsAccessAfterControlChange(permission, source)
                        && applies(permission, gameData, playerId, source, entry))) {
            return OptionalInt.empty();
        }
        return activeExileCastPermissions(gameData, source, playerId)
                .filter(permission -> !permission.waterbendManaValue())
                .filter(permission -> keepsAccessAfterControlChange(permission, source))
                .filter(permission -> applies(permission, gameData, playerId, source, entry))
                .mapToInt(AllowCastFromCardsExiledWithSourceEffect::additionalCounterCost)
                .min();
    }

    private boolean applies(AllowCastFromCardsExiledWithSourceEffect permission,
                             GameData gameData, UUID playerId, Permanent source,
                             ExiledCardEntry entry) {
        if (permission.persistsAfterSourceLeaves() && entry.exilerId() != null
                && !playerId.equals(entry.exilerId())) return false;
        if (permission.collectionCounterOnly() || permission.fetchCounterOnly()) return false;
        if (permission.stashCounterOnly() && playerId.equals(entry.ownerId())) return false;
        if (permission.controllerTurnOnly() && !playerId.equals(gameData.activePlayerId)) return false;
        if (permission.ownOnly() && !playerId.equals(entry.ownerId())) return false;
        if (permission.notOwnedOnly() && playerId.equals(entry.ownerId())) return false;
        if (permission.thisTurnOnly() && entry.exiledTurnNumber() != gameData.turnNumber) return false;
        if (permission.oncePerTurn()
                && (!entry.card().hasType(CardType.LAND) || permission.countsLandPlayForOncePerTurn())
                && (gameData.freeCastPermanentUsedThisTurn.contains(source.getId())
                || gameData.oncePerTurnExileCastPermissionsUsedThisTurn.contains(source.getId()))) return false;
        if (permission.oneShot()
                && (gameData.oneShotExileCastPermissionsUsed.contains(source.getId())
                || source.getControlChangeSequence() > 0)) return false;
        if (permission.waterbendManaValue() && source.getControlChangeSequence() > 0) return false;
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
        if (source.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, source)) {
            return Stream.empty();
        }
        return source.getCard().getStaticEffects().stream()
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
        materializeWildcardExileCastPermissions(gameData);
        return gameData.exileCastPermissions().stream()
                .filter(permission -> permission.cardId().equals(entry.card().getId()))
                .filter(permission -> permission.sourcePermanentId().equals(entry.sourcePermanentId()))
                .filter(permission -> permission.castingPlayerId().equals(playerId))
                .filter(permission -> !freeOnly || permission.withoutPayingManaCost())
                .findFirst()
                .orElse(null);
    }

    private GameData.ForetoldCardCastPermission findForetoldCardCastPermission(
            GameData gameData, UUID playerId, ExiledCardEntry entry) {
        if (entry == null || !playerId.equals(entry.ownerId())
                || !gameData.foretoldCardIds.contains(entry.card().getId())
                || !isCastableSpellCard(entry.card())) {
            return null;
        }
        return gameData.foretoldCardCastPermissions().stream()
                .filter(permission -> permission.playerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public boolean hasAnyManaTypePermission(GameData gameData, UUID playerId, UUID cardId) {
        return anyManaSpending(gameData, playerId, cardId).isAny();
    }

    /**
     * How the permissions that apply to {@code playerId} casting {@code cardId} from outside their hand let mana be
     * spent: {@link AnyManaSpending#ANY_TYPE} if any one of them lets mana of any type be spent (CR 118.14),
     * {@link AnyManaSpending#AS_THOUGH_ANY_COLOR} if they only let it be spent as though it were mana of any color, which
     * leaves a {C} symbol to colorless mana (CR 107.4c).
     */
    public AnyManaSpending anyManaSpending(GameData gameData, UUID playerId, UUID cardId) {
        AnyManaSpending spending = AnyManaSpending.NONE;
        if (gameData.hasPendingAnyManaTypeForNextSpellThisTurn(playerId)) {
            return AnyManaSpending.ANY_TYPE;
        }
        GameData.GraveyardCardCastPermission graveyardPermission =
                gameData.graveyardCardCastPermission(cardId, playerId).orElse(null);
        if (graveyardPermission != null && graveyardPermission.anyManaType()) {
            return AnyManaSpending.ANY_TYPE;
        }
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (card != null) {
            spending = spending.or(findGraveyardCastFilterPermission(gameData, card, playerId)
                    .map(GameData.GraveyardCastFilterPermission::manaSpending)
                    .orElse(AnyManaSpending.NONE));
        }
        spending = spending.or(stashCounterManaSpending(gameData, playerId, cardId));
        spending = spending.or(collectionCounterManaSpending(gameData, playerId, cardId));
        spending = spending.or(fetchCounterManaSpending(gameData, playerId, cardId));
        // Per-card any-mana grant from a "this turn" exile-cast permission (e.g. Nita, Forum Conciliator).
        spending = spending.or(gameData.exileManaSpending(cardId));
        AnyManaSpending playPermission = gameData.exilePlayPermission(cardId)
                .map(ExilePlayPermissionEffect::manaSpending).orElse(AnyManaSpending.NONE);
        if (playPermission.isAny() && hasExilePlayPermission(gameData, playerId, cardId)) {
            spending = spending.or(playPermission);
        }

        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (ExiledCardEntry entry : gameData.getExiledWithPermanentEntries(
                        perm.getId(), perm.getCard().getId())) {
                    if (!entry.card().getId().equals(cardId)) {
                        continue;
                    }
                    for (AllowCastFromCardsExiledWithSourceEffect effect
                            : activeExileCastPermissions(gameData, perm, sourceControllerId).toList()) {
                        if (!effect.waterbendManaValue() && effect.anyManaType()
                                && canAccessExiledEntry(gameData, perm, sourceControllerId, effect, entry, playerId)
                                && applies(effect, gameData, playerId, perm, entry)) {
                            spending = spending.or(effect.manaSpending());
                        }
                    }
                }
            }
        }
        return spending;
    }

    public boolean hasCollectionCounterPermission(GameData gameData, UUID playerId, UUID cardId) {
        return hasCollectionCounterPermission(gameData, playerId, cardId, false);
    }

    private boolean hasCollectionCounterPermission(GameData gameData, UUID playerId, UUID cardId,
                                                    boolean anyManaTypeRequired) {
        return collectionCounterPermissions(gameData, playerId, cardId)
                .anyMatch(effect -> !anyManaTypeRequired || effect.anyManaType());
    }

    private AnyManaSpending collectionCounterManaSpending(GameData gameData, UUID playerId, UUID cardId) {
        return collectionCounterPermissions(gameData, playerId, cardId)
                .map(AllowCastFromCardsExiledWithSourceEffect::manaSpending)
                .reduce(AnyManaSpending.NONE, AnyManaSpending::or);
    }

    private Stream<AllowCastFromCardsExiledWithSourceEffect> collectionCounterPermissions(GameData gameData,
                                                                                         UUID playerId, UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null || !gameData.exiledCardHasCounter(cardId, CounterType.COLLECTION)
                || !playerId.equals(entry.exilerId())) {
            return Stream.empty();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return Stream.empty();
        return battlefield.stream().flatMap(source -> activeExileCastPermissions(gameData, source, playerId)
                .filter(effect -> effect.collectionCounterOnly()
                        && (!effect.oncePerTurn()
                        || !gameData.freeCastPermanentUsedThisTurn.contains(source.getId()))
                        && (effect.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        entry.card(), effect.filter(), null))))
                .toList().stream();
    }

    private boolean hasFetchCounterPermission(GameData gameData, UUID playerId, UUID cardId,
                                               boolean anyManaTypeRequired) {
        return fetchCounterPermissions(gameData, playerId, cardId)
                .anyMatch(effect -> !anyManaTypeRequired || effect.anyManaType());
    }

    private AnyManaSpending fetchCounterManaSpending(GameData gameData, UUID playerId, UUID cardId) {
        return fetchCounterPermissions(gameData, playerId, cardId)
                .map(AllowCastFromCardsExiledWithSourceEffect::manaSpending)
                .reduce(AnyManaSpending.NONE, AnyManaSpending::or);
    }

    private Stream<AllowCastFromCardsExiledWithSourceEffect> fetchCounterPermissions(GameData gameData,
                                                                                    UUID playerId, UUID cardId) {
        if (!gameData.exiledCardHasCounter(cardId, CounterType.FETCH)) return Stream.empty();
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null || !playerId.equals(entry.exilerId())) return Stream.empty();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return Stream.empty();
        return battlefield.stream()
                .flatMap(source -> activeExileCastPermissions(gameData, source, playerId)
                        .filter(effect -> effect.fetchCounterOnly()
                                && (effect.filter() == null
                                || predicateEvaluationService.matchesCardPredicate(
                                entry.card(), effect.filter(), null))))
                .toList().stream();
    }

    /** Returns whether the player may spend snow mana as any color for an exiled card (Draugr Necromancer). */
    public boolean hasSnowManaAsAnyColorPermission(GameData gameData, UUID playerId, UUID cardId) {
        return hasPlayFromAmongExiledCardsPermission(gameData, playerId, cardId, true);
    }

    private boolean hasStashCounterPermission(GameData gameData, UUID playerId, UUID cardId,
                                              boolean anyManaTypeRequired) {
        return stashCounterPermissions(gameData, playerId, cardId)
                .anyMatch(permission -> !anyManaTypeRequired || permission.anyManaType());
    }

    private AnyManaSpending stashCounterManaSpending(GameData gameData, UUID playerId, UUID cardId) {
        return stashCounterPermissions(gameData, playerId, cardId)
                .map(AllowCastFromCardsExiledWithSourceEffect::manaSpending)
                .reduce(AnyManaSpending.NONE, AnyManaSpending::or);
    }

    private Stream<AllowCastFromCardsExiledWithSourceEffect> stashCounterPermissions(GameData gameData,
                                                                                    UUID playerId, UUID cardId) {
        if (!gameData.exiledCardHasCounter(cardId, CounterType.STASH)) return Stream.empty();
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return Stream.empty();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return Stream.empty();
        return battlefield.stream()
                .flatMap(source -> activeExileCastPermissions(gameData, source, playerId)
                        .filter(permission -> permission.stashCounterOnly()
                                && applies(permission, gameData, playerId, source, entry)))
                .toList().stream();
    }

    /**
     * Whether a {@link PlayFromAmongExiledCardsEffect} of a permanent {@code playerId} controls names the exiled card
     * {@code cardId} (with {@code snowManaRequired}, one that also lets snow mana be spent as any color). The
     * permissions are read as active static abilities, so a face-down source or one that lost its abilities grants
     * none (CR 613.1f, 708.2).
     */
    private boolean hasPlayFromAmongExiledCardsPermission(GameData gameData, UUID playerId, UUID cardId,
                                                          boolean snowManaRequired) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        for (GameQueryService.ActiveStaticEffect<PlayFromAmongExiledCardsEffect> permission
                : gameQueryService.activeStaticEffects(gameData, playerId, PlayFromAmongExiledCardsEffect.class)) {
            if ((!snowManaRequired || permission.effect().snowManaAsAnyColor())
                    && SubjectResolutionService.namesExiledCard(permission.source(), playerId, entry,
                    permission.effect().cards(), (card, ownerId, filter) -> predicateEvaluationService
                            .matchesCardPredicate(card, filter, permission.source().getCard().getId(), gameData, ownerId))) {
                return true;
            }
        }
        return false;
    }

    private boolean canAccessExiledEntry(GameData gameData, Permanent source, UUID sourceControllerId,
                                         AllowCastFromCardsExiledWithSourceEffect effect,
                                         ExiledCardEntry entry, UUID playerId) {
        return switch (effect.accessScope()) {
            case CONTROLLER -> sourceControllerId.equals(playerId);
            case EXILER -> playerId.equals(entry.exilerId());
            case ACTIVE_PLAYER -> gameData.activePlayerId != null
                    && gameData.activePlayerId.equals(playerId);
            case CONTROLLER_WITHOUT_CONTROL_CHANGE -> sourceControllerId.equals(playerId)
                    && source.getControlChangeSequence() == 0;
        };
    }

    /**
     * Lukka's "you may cast this card from exile as long as you control a Lukka planeswalker": the permission holds
     * while {@code playerId} controls a permanent that is a planeswalker now, by the layered pass, and is named as a
     * Lukka. A face-down permanent has neither (CR 708.2a). {@code CardSubtype} has no Lukka type, so the name stands
     * in for the planeswalker type.
     */
    private boolean hasLukkaExilePermission(GameData gameData, UUID playerId,
                                            ExiledCardEntry entry) {
        if (!playerId.equals(gameData.lukkaExileCastPermissionHolder(entry.card().getId()))) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && battlefield.stream().anyMatch(permanent -> !permanent.isFaceDown()
                && gameQueryService.isPlaneswalker(gameData, permanent)
                && String.valueOf(gameQueryService.getEffectiveName(gameData, permanent)).startsWith("Lukka,"));
    }
}

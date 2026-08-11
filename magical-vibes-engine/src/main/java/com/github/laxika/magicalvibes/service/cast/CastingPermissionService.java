package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AnyManaTypeCastEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastAdditionalNonartifactSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastPermanentSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardPermission;
import com.github.laxika.magicalvibes.model.effect.ControllerCantPlayLandsEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemGrantsFlashbackEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentControllerCantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalLandPlayRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueAtMostEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsAndLandsWithChosenNamesCantBePlayedEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.WardOfBonesEffect;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

/**
 * Single source of truth for whether a player may cast a given spell: per-turn spell limits,
 * type/name restrictions, silence-style prevention, timing (flash grants), and permissions to
 * cast/play from non-hand zones (graveyard, exile, top of library).
 *
 * <p>Both the view side ({@code GameActionAvailabilityService} playable-card computation) and the
 * validation side ({@code SpellCastingService}) must go through this service.
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CastingPermissionService {

    private static final Set<CardType> WARD_OF_BONES_SPELL_TYPES =
            Set.of(CardType.CREATURE, CardType.ARTIFACT, CardType.ENCHANTMENT);

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    public CastingPermissionService(GameQueryService gameQueryService,
                                    PredicateEvaluationService predicateEvaluationService,
                                    ConditionEvaluationService conditionEvaluationService) {
        this(gameQueryService, predicateEvaluationService, conditionEvaluationService,
                new AmountEvaluationService(predicateEvaluationService, gameQueryService));
    }

    /**
     * Returns true if the player is allowed to cast this spell considering non-mana
     * restrictions: spell limit, type restrictions, forbidden names, silence, etc.
     */
    public boolean isSpellCastingAllowed(GameData gameData, UUID playerId, Card card) {
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        int maxSpells = getMaxSpellsPerTurn(gameData, playerId);
        if (spellsCast >= maxSpells) return false;
        if (isPlayerPreventedFromCasting(gameData, playerId)) return false;
        Set<CardType> restricted = getRestrictedSpellTypes(gameData, playerId);
        if (restricted.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(restricted::contains)) return false;
        Set<String> forbidden = getForbiddenCardNames(gameData, playerId);
        if (forbidden.contains(card.getName())) return false;
        if (isNoncreatureSpellCastRestricted(gameData, card)) return false;
        // Aurelia's Fury etc.: per-turn "can't cast noncreature spells" restriction on a player.
        if (!card.hasType(CardType.CREATURE)
                && gameData.playersCantCastNoncreatureSpellsThisTurn.contains(playerId)) return false;
        if (isOpponentsChosenColorSpellCastRestricted(gameData, playerId, card)) return false;
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
        int limit = Integer.MAX_VALUE;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof LimitSpellsPerTurnEffect spellLimit)) continue;
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
     * Returns true if the player is prevented from casting spells (e.g. Angelic Arbiter:
     * "Each opponent who attacked with a creature this turn can't cast spells").
     */
    public boolean isPlayerPreventedFromCasting(GameData gameData, UUID playerId) {
        if (gameData.playersSilencedThisTurn.contains(playerId)) return true;

        // Grand Abolisher: during its controller's turn their opponents can't cast spells.
        if (gameQueryService.isLockedOutByOpponentsTurnRestriction(gameData, playerId)) return true;

        // City of Solitude: players can cast spells only during their own turns.
        if (gameQueryService.isLockedOutByOwnTurnOnlyRestriction(gameData, playerId)) return true;

        // Dosan the Falling Leaf: players can cast spells only during their own turns.
        if (gameQueryService.isLockedOutByOwnTurnOnlySpellRestriction(gameData, playerId)) return true;

        if (!gameData.playersDeclaredAttackersThisTurn.contains(playerId)) return false;

        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsCantCastSpellsIfAttackedThisTurnEffect) {
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
        return restricted;
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
        return false;
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
     * Returns true if a global {@link NoncreatureSpellsCantBeCastEffect} (e.g. Gaddock Teeg) on any
     * player's battlefield prevents this noncreature spell from being cast. Symmetric — the source's
     * own controller is restricted too.
     */
    public boolean isNoncreatureSpellCastRestricted(GameData gameData, Card card) {
        if (card.hasType(CardType.CREATURE)) return false;
        int manaValue = card.getManaValue();
        boolean hasX = card.getParsedManaCost() != null && card.getParsedManaCost().hasX();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof NoncreatureSpellsCantBeCastEffect restriction) {
                        if (manaValue >= restriction.minManaValue()) return true;
                        if (restriction.restrictXSpells() && hasX) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Brisela, Voice of Nightmares: opponents of a source controller can't cast spells with mana
     * value ≤ the effect's {@code maxManaValue}. Playability overload (no chosen X): X-cost spells
     * are never blocked because a high enough X is always legal. Cast-time overload includes the
     * chosen X in mana value (CR 202.3c).
     */
    public boolean isOpponentsManaValueSpellCastRestricted(GameData gameData, UUID castingPlayerId, Card card) {
        return isOpponentsManaValueSpellCastRestricted(gameData, castingPlayerId, card, null);
    }

    public boolean isOpponentsManaValueSpellCastRestricted(GameData gameData, UUID castingPlayerId, Card card,
                                                           Integer chosenX) {
        int tightestMax = Integer.MAX_VALUE;
        boolean restricted = false;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(castingPlayerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsCantCastSpellsWithManaValueAtMostEffect restriction) {
                        restricted = true;
                        tightestMax = Math.min(tightestMax, restriction.maxManaValue());
                    }
                }
            }
        }
        if (!restricted) return false;

        ManaCost cost = card.getParsedManaCost();
        boolean hasX = cost != null && cost.hasX();
        if (chosenX == null) {
            // Playability: any {X} spell can pick X high enough to exceed the cap.
            if (hasX) return false;
            return card.getManaValue() <= tightestMax;
        }
        int manaValue = card.getManaValue();
        if (hasX) {
            manaValue += chosenX * Math.max(1, cost.getXSymbolCount());
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
     * Ethersworn Canonist: "Each player who has cast a nonartifact spell this turn can't cast
     * additional nonartifact spells." Returns true if {@code card} is a nonartifact spell, some
     * permanent carries the effect, and {@code playerId} has already cast a nonartifact spell this
     * turn. Artifact spells and each player's first nonartifact spell are never restricted; symmetric.
     */
    public boolean isAdditionalNonartifactSpellRestricted(GameData gameData, UUID playerId, Card card) {
        if (card.hasType(CardType.ARTIFACT)) return false;
        if (!anyPlayerControlsEtherswornCanonist(gameData)) return false;
        return gameData.getSpellsCastThisTurn(playerId).stream()
                .anyMatch(cast -> !cast.hasType(CardType.ARTIFACT));
    }

    public boolean isSpellCastingRestrictedByMostRecentSpell(GameData gameData, Card card) {
        Card mostRecentSpell = gameData.getMostRecentSpellCastThisTurn();
        if (mostRecentSpell == null) return false;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof SpellCastingRestrictionEffect restriction
                            && restriction.preventsCasting(mostRecentSpell, card)) {
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
        return forbidden;
    }

    /**
     * Null Chamber: "lands with the chosen names can't be played". Land plays deliberately bypass the
     * spell-casting filters (they aren't spells), so the name check has its own entry point here.
     */
    public boolean isLandPlayForbiddenByChosenName(GameData gameData, Card card) {
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
                }
            }
        }
        return false;
    }

    /**
     * As {@link #isSpellRestricted(Card, Set, Set)}, plus the per-turn "this player can't cast
     * noncreature spells" lock (Aurelia's Fury), which can't be expressed as a restricted type set.
     */
    public boolean isSpellRestricted(GameData gameData, UUID playerId, Card card,
                                     Set<CardType> restrictedSpellTypes, Set<String> forbiddenCardNames) {
        if (isSpellCastingRestrictedByMostRecentSpell(gameData, card)) return true;
        if (!card.hasType(CardType.CREATURE)
                && gameData.playersCantCastNoncreatureSpellsThisTurn.contains(playerId)) return true;
        if (isOpponentsChosenColorSpellCastRestricted(gameData, playerId, card)) return true;
        return isSpellRestricted(card, restrictedSpellTypes, forbiddenCardNames);
    }

    public boolean isSpellRestricted(Card card, Set<CardType> restrictedSpellTypes, Set<String> forbiddenCardNames) {
        if (restrictedSpellTypes.contains(card.getType())) return true;
        for (CardType type : card.getAdditionalTypes()) {
            if (restrictedSpellTypes.contains(type)) return true;
        }
        return forbiddenCardNames.contains(card.getName());
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
        boolean isInstantSpeed = card.hasType(CardType.INSTANT)
                || card.getKeywords().contains(Keyword.FLASH)
                || hasFlashGrantForCard(gameData, playerId, card)
                || grantsItselfFlashTiming(card)
                || hasMetFlashCastCondition(gameData, playerId, card)
                || hasAvailableFlashAlternateCast(gameData, playerId, card);
        return isInstantSpeed || (isActivePlayer && isMainPhase && stackEmpty);
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

    private boolean hasFlashGrantForCard(GameData gameData, UUID playerId, Card card) {
        if (gameData.playersWithFlashUntilEndOfTurn.contains(playerId)) return true;
        if (gameData.hasCardTypeFlashGrant(playerId, card)) return true;
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
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PlayLandsFromGraveyardEffect) {
                    return true;
                }
            }
        }
        return false;
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
        if (!isCastableSpellCard(card)) {
            return Optional.empty();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return Optional.empty();
        }
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof CastSpellsFromGraveyardPermission permission)
                        || !predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null)) {
                    continue;
                }
                if (permission.oncePerControllerTurn()
                        && (!playerId.equals(gameData.activePlayerId)
                            || gameData.oncePerTurnGraveyardCastPermissionsUsedThisTurn.contains(perm.getId()))) {
                    continue;
                }
                return Optional.of(perm.getId());
            }
        }
        return Optional.empty();
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
        return hasGraveyardCastFilterPermission(gameData, card, playerId);
    }

    /**
     * True if a turn-scoped blanket grant (Liliana, Untouched by Death's −3) lets this player cast
     * {@code card} from their graveyard. Lands are not spells, so they never qualify.
     */
    private boolean hasGraveyardCastFilterPermission(GameData gameData, Card card, UUID playerId) {
        if (!isCastableSpellCard(card)) {
            return false;
        }
        return gameData.graveyardCastFilterPermissionsThisTurn.stream()
                .anyMatch(permission -> permission.playerId().equals(playerId)
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null));
    }

    public boolean isGraveyardCastAvailable(GameData gameData, UUID playerId, GraveyardCast graveyardCast) {
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

    public boolean hasEmblemGrantedFlashback(GameData gameData, UUID playerId, Card card) {
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
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return castableTypes;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AllowCastFromTopOfLibraryEffect allow) {
                    castableTypes.addAll(allow.castableTypes());
                }
            }
        }
        return castableTypes;
    }

    /** Returns whether a specific card may be cast from the top of the player's library. */
    public boolean canCastFromTopOfLibrary(GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AllowCastFromTopOfLibraryEffect allow && allow.matches(card)) {
                    return true;
                }
            }
        }
        return false;
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
                if (effect instanceof AnyManaTypeCastEffect anyMana && cardHasAnyType(card, anyMana.spellTypes())) {
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

    /**
     * Returns the set of exiled card IDs that the player can cast via
     * {@link AllowCastFromCardsExiledWithSourceEffect} on their permanents.
     */
    public Set<UUID> getCastableExiledCardIds(GameData gameData, UUID playerId) {
        Set<UUID> castableIds = new HashSet<>();
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (ExiledCardEntry entry : gameData.getExiledWithPermanentEntries(
                        perm.getId(), perm.getCard().getId())) {
                    if (canAccessExiledEntry(perm, sourceControllerId, entry, playerId)) {
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
            UUID permittedPlayer = gameData.exilePlayPermissions.get(entry.card().getId());
            if (gameData.exilePlayAnyManaTypeWhileExiled.contains(entry.card().getId())
                    && playerId.equals(permittedPlayer)) {
                anyManaIds.add(entry.card().getId());
            }
        }
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (ExiledCardEntry entry : gameData.getExiledWithPermanentEntries(
                        perm.getId(), perm.getCard().getId())) {
                    boolean anyMana = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                            .filter(AllowCastFromCardsExiledWithSourceEffect.class::isInstance)
                            .map(AllowCastFromCardsExiledWithSourceEffect.class::cast)
                            .anyMatch(effect -> effect.anyManaType()
                                    && canAccessExiledEntry(perm, sourceControllerId, effect, entry, playerId));
                    if (anyMana) {
                        anyManaIds.add(entry.card().getId());
                    }
                }
            }
        }
        return anyManaIds;
    }

    public boolean hasCastFromExiledWithSourcePermission(GameData gameData, UUID playerId, UUID cardId) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (!perm.getId().equals(entry.sourcePermanentId())) continue;
                if (perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(AllowCastFromCardsExiledWithSourceEffect.class::isInstance)
                        .map(AllowCastFromCardsExiledWithSourceEffect.class::cast)
                        .anyMatch(permission -> canAccessExiledEntry(
                                perm, sourceControllerId, permission, entry, playerId)
                                && applies(permission, gameData, playerId, perm, entry))) return true;
            }
        }
        return false;
    }

    public boolean hasFreeCastFromExiledWithSource(GameData gameData, UUID playerId, UUID cardId) {
        return findFreeCastPermission(gameData, playerId, cardId, false);
    }

    public boolean consumeFreeCastFromExiledWithSource(GameData gameData, UUID playerId, UUID cardId) {
        return findFreeCastPermission(gameData, playerId, cardId, true);
    }

    private boolean findFreeCastPermission(GameData gameData, UUID playerId, UUID cardId, boolean consume) {
        ExiledCardEntry entry = gameData.findExiledCard(cardId);
        if (entry == null) return false;
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (!perm.getId().equals(entry.sourcePermanentId())) continue;
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof AllowCastFromCardsExiledWithSourceEffect permission)
                            || !permission.withoutPayingManaCost()
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
        if (source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(AllowCastFromCardsExiledWithSourceEffect.class::isInstance)
                .map(AllowCastFromCardsExiledWithSourceEffect.class::cast)
                .noneMatch(permission -> applies(permission, gameData, playerId, source, entry))) {
            return OptionalInt.empty();
        }
        return source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(AllowCastFromCardsExiledWithSourceEffect.class::isInstance)
                .map(AllowCastFromCardsExiledWithSourceEffect.class::cast)
                .filter(permission -> applies(permission, gameData, playerId, source, entry))
                .mapToInt(AllowCastFromCardsExiledWithSourceEffect::additionalCounterCost)
                .min();
    }

    private boolean applies(AllowCastFromCardsExiledWithSourceEffect permission,
                             GameData gameData, UUID playerId, Permanent source,
                             ExiledCardEntry entry) {
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

    public boolean hasAnyManaTypePermission(GameData gameData, UUID playerId, UUID cardId) {
        // Per-card any-mana grant from a "this turn" exile-cast permission (e.g. Nita, Forum Conciliator).
        if (gameData.exilePlayAnyManaType.contains(cardId)
                || (gameData.exilePlayAnyManaTypeWhileExiled.contains(cardId)
                && playerId.equals(gameData.exilePlayPermissions.get(cardId)))) return true;

        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (ExiledCardEntry entry : gameData.getExiledWithPermanentEntries(
                        perm.getId(), perm.getCard().getId())) {
                    if (entry.card().getId().equals(cardId)
                            && perm.getCard().getEffects(EffectSlot.STATIC).stream()
                            .filter(AllowCastFromCardsExiledWithSourceEffect.class::isInstance)
                            .map(AllowCastFromCardsExiledWithSourceEffect.class::cast)
                            .anyMatch(effect -> effect.anyManaType()
                                    && canAccessExiledEntry(perm, sourceControllerId, effect, entry, playerId))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canAccessExiledEntry(Permanent source, UUID sourceControllerId,
                                         ExiledCardEntry entry, UUID playerId) {
        return source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(AllowCastFromCardsExiledWithSourceEffect.class::isInstance)
                .map(AllowCastFromCardsExiledWithSourceEffect.class::cast)
                .anyMatch(effect -> canAccessExiledEntry(source, sourceControllerId, effect, entry, playerId));
    }

    private boolean canAccessExiledEntry(Permanent source, UUID sourceControllerId,
                                         AllowCastFromCardsExiledWithSourceEffect effect,
                                         ExiledCardEntry entry, UUID playerId) {
        return effect.accessScope() == ExileAccessScope.CONTROLLER
                ? sourceControllerId.equals(playerId)
                : playerId.equals(entry.exilerId());
    }
}

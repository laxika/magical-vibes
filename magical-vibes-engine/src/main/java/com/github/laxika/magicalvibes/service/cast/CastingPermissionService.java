package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastAdditionalNonartifactSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastPermanentSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemGrantsFlashbackEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsForControllerEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsForEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;
import com.github.laxika.magicalvibes.model.effect.WardOfBonesEffect;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Single source of truth for whether a player may cast a given spell: per-turn spell limits,
 * type/name restrictions, silence-style prevention, timing (flash grants), and permissions to
 * cast/play from non-hand zones (graveyard, exile, top of library).
 *
 * <p>Both the view side ({@code GameBroadcastService} playable-card computation) and the
 * validation side ({@code SpellCastingService}) must go through this service.
 */
@Component
@RequiredArgsConstructor
public class CastingPermissionService {

    private static final Set<CardType> WARD_OF_BONES_SPELL_TYPES =
            Set.of(CardType.CREATURE, CardType.ARTIFACT, CardType.ENCHANTMENT);

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;

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
        if (isAdditionalNonartifactSpellRestricted(gameData, playerId, card)) return false;
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
                    // Rule of Law etc.: applies to every player globally.
                    if (effect instanceof LimitSpellsPerTurnEffect global) {
                        limit = Math.min(limit, global.maxSpells());
                    }
                    // Curse of Exhaustion etc.: only applies to the enchanted player.
                    if (effect instanceof LimitSpellsForEnchantedPlayerEffect curse
                            && perm.isAttached() && playerId.equals(perm.getAttachedTo())) {
                        limit = Math.min(limit, curse.maxSpells());
                    }
                    // Colfenor's Plans etc.: only applies to the permanent's controller.
                    if (effect instanceof LimitSpellsForControllerEffect controllerLimit
                            && pid.equals(playerId)) {
                        limit = Math.min(limit, controllerLimit.maxSpells());
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
        // Moonhold etc.: per-turn "can't cast creature spells" restriction on a player.
        if (gameData.playersCantCastCreatureSpellsThisTurn.contains(playerId)) {
            restricted.add(CardType.CREATURE);
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
     * Ward of Bones (EVE): returns true if any opponent controls a Ward of Bones and this player
     * controls more lands than that opponent, in which case this player can't play lands.
     */
    public boolean isLandPlayRestrictedByWardOfBones(GameData gameData, UUID playerId) {
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(playerId) || !controlsWardOfBones(gameData, controllerId)) continue;
            if (countControlledOfType(gameData, playerId, CardType.LAND)
                    > countControlledOfType(gameData, controllerId, CardType.LAND)) {
                return true;
            }
        }
        return false;
    }

    private boolean controlsWardOfBones(GameData gameData, UUID playerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        return bf.stream().anyMatch(perm -> perm.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(WardOfBonesEffect.class::isInstance));
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
                    if (effect instanceof SpellsWithChosenNameCantBeCastEffect) {
                        String chosenName = perm.getChosenName();
                        if (chosenName != null) {
                            forbidden.add(chosenName);
                        }
                    }
                }
            }
        }
        return forbidden;
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
                || hasAvailableFlashAlternateCast(gameData, playerId, card);
        return isInstantSpeed || (isActivePlayer && isMainPhase && stackEmpty);
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
            case OPPONENTS_TURN_BEFORE_ATTACKERS ->
                    !playerId.equals(gameData.activePlayerId)
                            && gameData.currentStep.isBeforeAttackersDeclared();
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
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantFlashToCardTypeEffect grant) {
                    if (predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null)) {
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

    public boolean hasHavengulCastPermission(GameData gameData, Card card, UUID playerId) {
        GameData.GraveyardCreatureCastPermission permission =
                gameData.graveyardCreatureCastPermissionsUntilEndOfTurn.get(card.getId());
        return permission != null && playerId.equals(permission.castingPlayerId());
    }

    public boolean hasGraveyardPlayPermission(GameData gameData, UUID cardId, UUID playerId) {
        UUID permittedPlayer = gameData.graveyardPlayPermissions.get(cardId);
        return permittedPlayer != null && permittedPlayer.equals(playerId);
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

    /**
     * Returns the set of exiled card IDs that the player can cast via
     * {@link AllowCastFromCardsExiledWithSourceEffect} on their permanents.
     */
    public Set<UUID> getCastableExiledCardIds(GameData gameData, UUID playerId) {
        Set<UUID> castableIds = new HashSet<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return castableIds;
        for (Permanent perm : battlefield) {
            boolean hasEffect = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof AllowCastFromCardsExiledWithSourceEffect);
            if (hasEffect) {
                List<Card> exiledWithPerm = gameData.getCardsExiledByPermanent(perm.getId());
                for (Card c : exiledWithPerm) {
                    castableIds.add(c.getId());
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
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return anyManaIds;
        for (Permanent perm : battlefield) {
            boolean hasAnyMana = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof AllowCastFromCardsExiledWithSourceEffect a && a.anyManaType());
            if (hasAnyMana) {
                List<Card> exiledWithPerm = gameData.getCardsExiledByPermanent(perm.getId());
                for (Card c : exiledWithPerm) {
                    anyManaIds.add(c.getId());
                }
            }
        }
        return anyManaIds;
    }

    public boolean hasCastFromExiledWithSourcePermission(GameData gameData, UUID playerId, UUID cardId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            boolean hasEffect = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof AllowCastFromCardsExiledWithSourceEffect);
            if (hasEffect) {
                List<Card> exiledWithPerm = gameData.getCardsExiledByPermanent(perm.getId());
                for (Card c : exiledWithPerm) {
                    if (c.getId().equals(cardId)) return true;
                }
            }
        }
        return false;
    }

    public boolean hasAnyManaTypePermission(GameData gameData, UUID playerId, UUID cardId) {
        // Per-card any-mana grant from a "this turn" exile-cast permission (e.g. Nita, Forum Conciliator).
        if (gameData.exilePlayAnyManaType.contains(cardId)) return true;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            boolean hasAnyMana = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof AllowCastFromCardsExiledWithSourceEffect a && a.anyManaType());
            if (hasAnyMana) {
                List<Card> exiledWithPerm = gameData.getCardsExiledByPermanent(perm.getId());
                for (Card c : exiledWithPerm) {
                    if (c.getId().equals(cardId)) return true;
                }
            }
        }
        return false;
    }
}

package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared target selection logic for AI spell casting.
 */
class AiTargetSelector {

    private final GameQueryService gameQueryService;

    AiTargetSelector(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    UUID chooseTarget(GameData gameData, Card card, UUID aiPlayerId) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);

        // Handle player-only targeting (e.g. Haunting Echoes, Mind Rot)
        Set<TargetType> allowedTargets = card.getAllowedTargets();
        if (allowedTargets.contains(TargetType.PLAYER) && !allowedTargets.contains(TargetType.PERMANENT)) {
            return opponentId;
        }

        // Handle graveyard targeting (e.g. Unburial Rites, Gruesome Encore)
        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            List<Card> candidates = findValidGraveyardTargets(gameData, card, aiPlayerId);
            if (candidates.isEmpty()) return null;
            // Pick the highest mana value card (best reanimation/value target)
            return candidates.stream()
                    .max(Comparator.comparingInt(Card::getManaValue))
                    .map(Card::getId)
                    .orElse(null);
        }

        // Handle destroy effects (ETB creatures or removal spells)
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof DestroyTargetPermanentEffect) {
                return chooseDestroyTarget(gameData, card, aiPlayerId, opponentId);
            }
        }
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof DestroyTargetPermanentEffect) {
                return chooseDestroyTarget(gameData, card, aiPlayerId, opponentId);
            }
        }

        boolean isBeneficial = false;
        if (card.isAura()) {
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if ((effect instanceof StaticBoostEffect boost
                        && (boost.scope() == GrantScope.ENCHANTED_CREATURE || boost.scope() == GrantScope.EQUIPPED_CREATURE))
                        || (effect instanceof GrantKeywordEffect grant && grant.scope() == GrantScope.ENCHANTED_CREATURE)) {
                    isBeneficial = true;
                    break;
                }
            }
        }

        if (isBeneficial) {
            // Target own creature with highest toughness
            List<Permanent> ownBattlefield = gameData.playerBattlefields.get(aiPlayerId);
            if (ownBattlefield != null) {
                UUID target = ownBattlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .filter(p -> passesTargetFilter(gameData, card, p, aiPlayerId))
                        .max(Comparator.comparingInt(p -> gameQueryService.getEffectiveToughness(gameData, p)))
                        .map(Permanent::getId)
                        .orElse(null);
                if (target != null) return target;
            }
            return null; // Aura was handled by specific logic — don't fall through
        } else if (card.isAura()) {
            // Detrimental aura — target opponent's highest-power creature that doesn't already have this effect
            List<Permanent> oppBattlefield = gameData.playerBattlefields.get(opponentId);
            if (oppBattlefield != null) {
                List<Class<? extends CardEffect>> auraEffectClasses = card.getEffects(EffectSlot.STATIC).stream()
                        .map(CardEffect::getClass)
                        .toList();
                UUID target = oppBattlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .filter(p -> passesTargetFilter(gameData, card, p, aiPlayerId))
                        .filter(p -> auraEffectClasses.stream().noneMatch(ec -> gameQueryService.hasAuraWithEffect(gameData, p, ec)))
                        .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)))
                        .map(Permanent::getId)
                        .orElse(null);
                if (target != null) return target;
            }
            return null; // Aura was handled by specific logic — don't fall through
        }

        // General fallback: find any valid target using the card's target filter
        if (card.getTargetFilter() != null) {
            // Search own battlefield first (for beneficial ETB effects like Awakener Druid)
            List<Permanent> ownBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
            for (Permanent p : ownBattlefield) {
                if (passesTargetFilter(gameData, card, p, aiPlayerId)) {
                    return p.getId();
                }
            }
            // Then search opponent battlefield
            List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
            for (Permanent p : oppBattlefield) {
                if (passesTargetFilter(gameData, card, p, aiPlayerId)) {
                    return p.getId();
                }
            }
        }

        return null;
    }

    boolean passesTargetFilter(GameData gameData, Card card, Permanent target, UUID aiPlayerId) {
        if (card.getTargetFilter() == null) {
            return true;
        }
        try {
            gameQueryService.validateTargetFilter(card.getTargetFilter(),
                    target,
                    FilterContext.of(gameData)
                            .withSourceCardId(card.getId())
                            .withSourceControllerId(aiPlayerId));
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private UUID chooseDestroyTarget(GameData gameData, Card card, UUID aiPlayerId, UUID opponentId) {
        // Search opponent's battlefield first
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        UUID oppTarget = findDestroyCandidate(gameData, card, oppBattlefield, aiPlayerId);
        if (oppTarget != null) {
            return oppTarget;
        }

        // Fall back to own battlefield
        List<Permanent> ownBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        return findDestroyCandidate(gameData, card, ownBattlefield, aiPlayerId);
    }

    /**
     * Returns all valid graveyard cards that the given spell can target.
     * Examines the card's SPELL effects to determine the correct graveyard scope and filter.
     */
    List<Card> findValidGraveyardTargets(GameData gameData, Card card, UUID aiPlayerId) {
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (!effect.canTargetGraveyard()) continue;

            List<Card> candidates;
            if (effect instanceof ReturnCardFromGraveyardEffect rge) {
                candidates = getGraveyardCandidates(gameData, rge.source(), aiPlayerId, opponentId);
                if (rge.filter() != null) {
                    candidates = candidates.stream()
                            .filter(c -> gameQueryService.matchesCardPredicate(c, rge.filter(), card.getId()))
                            .toList();
                }
            } else {
                // For non-return effects: canTargetAnyGraveyard → all graveyards, otherwise → opponent's
                GraveyardSearchScope scope = effect.canTargetAnyGraveyard()
                        ? GraveyardSearchScope.ALL_GRAVEYARDS
                        : GraveyardSearchScope.OPPONENT_GRAVEYARD;
                candidates = getGraveyardCandidates(gameData, scope, aiPlayerId, opponentId);
            }

            if (!candidates.isEmpty()) {
                return new ArrayList<>(candidates);
            }
        }
        return List.of();
    }

    private List<Card> getGraveyardCandidates(GameData gameData, GraveyardSearchScope scope,
                                               UUID aiPlayerId, UUID opponentId) {
        List<Card> candidates = new ArrayList<>();
        switch (scope) {
            case CONTROLLERS_GRAVEYARD -> candidates.addAll(
                    gameData.playerGraveyards.getOrDefault(aiPlayerId, List.of()));
            case OPPONENT_GRAVEYARD -> candidates.addAll(
                    gameData.playerGraveyards.getOrDefault(opponentId, List.of()));
            case ALL_GRAVEYARDS -> {
                for (UUID playerId : gameData.orderedPlayerIds) {
                    candidates.addAll(gameData.playerGraveyards.getOrDefault(playerId, List.of()));
                }
            }
        }
        return candidates;
    }

    private UUID findDestroyCandidate(GameData gameData, Card card, List<Permanent> battlefield, UUID aiPlayerId) {
        List<Permanent> candidates = battlefield.stream()
                .filter(p -> card.getTargetFilter() == null || passesTargetFilter(gameData, card, p, aiPlayerId))
                .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        // Prefer creature kills when legal, then choose the most threatening one.
        UUID creatureTarget = candidates.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)))
                .map(Permanent::getId)
                .orElse(null);
        if (creatureTarget != null) {
            return creatureTarget;
        }

        return candidates.getFirst().getId();
    }
}

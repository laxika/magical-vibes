package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class GraveyardTargetingService {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;

    /**
     * Returns the given player's graveyard as a pool of legal targets, or {@code null} when no card
     * in a graveyard can be targeted (Ground Seal). Every targeting handler in this class reads
     * graveyards through here, so the restriction applies uniformly and an emptied pool makes the
     * trigger fizzle or the spell find no legal target.
     */
    /** How a prompt names the graveyards {@code scope} searches. */
    private String zoneLabel(GraveyardSearchScope scope) {
        return switch (scope) {
            case ALL_GRAVEYARDS -> "a graveyard";
            case OPPONENT_GRAVEYARD -> "an opponent's graveyard";
            case CONTROLLERS_GRAVEYARD -> "your graveyard";
        };
    }

    private List<Card> targetableGraveyard(GameData gameData, UUID playerId) {
        if (!gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            return null;
        }
        return gameData.playerGraveyards.get(playerId);
    }

    /**
     * ETB targeting for "you may exile up to N other target creatures from the battlefield and/or
     * creature cards from graveyards" (Angel of Serenity). The battlefield and graveyard halves are
     * offered as one card pool so the controller spends the N picks freely across both zones, which
     * is what the oracle's "and/or" means; the chosen ids land on the triggered ability's
     * {@code targetCardIds} and the effect handler exiles each from whichever zone it is in. The
     * source permanent is excluded ("other"). "Up to N" allows choosing zero, which covers the
     * "you may". With no legal target the trigger is still pushed onto the stack with no targets.
     */
    public void handleBattlefieldAndGraveyardExileETBTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, UUID sourcePermanentId,
            BattlefieldAndGraveyardCardChoosingEffect choosingEffect) {
        List<Card> pool = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(sourcePermanentId)) continue;
                if (gameQueryService.isCreature(gameData, permanent)) {
                    pool.add(permanent.getCard());
                }
            }
        });
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (graveyardCard.hasType(CardType.CREATURE)) {
                    pool.add(graveyardCard);
                }
            }
        }

        if (pool.isEmpty()) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ETB ability",
                    new ArrayList<>(effects),
                    0,
                    null,
                    sourcePermanentId,
                    Map.of(),
                    null,
                    List.of(),
                    List.of()
            ));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
            log.info("Game {} - {} ETB mixed-zone exile pushed onto stack with 0 targets (no creatures anywhere)",
                    gameData.id, card.getName());
            return;
        }

        int maxTargets = Math.min(choosingEffect.mixedZoneMaxTargets(), pool.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
        gameData.graveyardTargetOperation.sourcePermanentId = sourcePermanentId;
        gameData.graveyardTargetOperation.anyNumber = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, pool, maxTargets,
                card.getName() + "'s ability — Choose up to " + maxTargets + " target creature"
                        + (maxTargets != 1 ? "s" : "") + " on the battlefield and/or creature card"
                        + (maxTargets != 1 ? "s" : "") + " in graveyards to exile.");
    }


    public void handleGraveyardExileETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                  List<CardEffect> allEffects, ExileCardsFromGraveyardEffect exile) {
        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                matchingCards.add(graveyardCard);
            }
        }

        if (matchingCards.isEmpty()) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ETB ability",
                    new ArrayList<>(allEffects),
                    List.of()
            ));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
            log.info("Game {} - {} ETB ability pushed onto stack with 0 targets (no graveyard cards)", gameData.id, card.getName());
        } else {
            int maxTargets = Math.min(exile.maxTargets(), matchingCards.size());
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(allEffects);
            gameData.graveyardTargetOperation.singleGraveyard = exile.singleGraveyard();
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                    "Choose up to " + maxTargets + " target card" + (maxTargets != 1 ? "s" : "")
                            + (exile.singleGraveyard() ? " from a single graveyard" : " from graveyards")
                            + " to exile.");
        }
    }

    /**
     * ETB targeting for {@link ExileGraveyardCardsEffect} whose scope targets card(s) in a graveyard —
     * "exile target card from an opponent's graveyard" (Disposal Mummy) or "... from a graveyard"
     * ({@code TARGET_CARDS_ANY_GRAVEYARD}). Graveyard-targeting ETBs never target at cast time, so the
     * card is chosen as the trigger goes on the stack; the chosen ids land on the triggered ability's
     * {@code targetCardIds} and {@code ExileGraveyardCardsEffectHandler} exiles them at resolution.
     * Which graveyards are searched comes from the effect's declared {@link GraveyardSearchScope}.
     * With no legal target the trigger is still pushed onto the stack with 0 targets and fizzles
     * harmlessly.
     */
    public void handleGraveyardCardsExileETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                      List<CardEffect> allEffects, ExileGraveyardCardsEffect exile) {
        CardPredicate filter = exile.filter();
        GraveyardSearchScope scope = exile.targetSpec().graveyardScope().orElseThrow();

        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : scope.graveyardOwners(gameData.orderedPlayerIds, controllerId)) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (filter == null
                        || predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        if (matchingCards.isEmpty()) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ETB ability",
                    new ArrayList<>(allEffects),
                    List.of()
            ));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
            log.info("Game {} - {} ETB graveyard-exile pushed onto stack with 0 targets (no valid graveyard cards)",
                    gameData.id, card.getName());
            return;
        }

        int maxTargets = Math.min(exile.count(), matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(allEffects);
        if (exile.trackWithSource()) {
            gameData.graveyardTargetOperation.sourcePermanentId = gameData.playerBattlefields
                    .getOrDefault(controllerId, List.of()).stream()
                    .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                    .map(Permanent::getId)
                    .findFirst()
                    .orElse(null);
        }
        String zoneLabel = zoneLabel(scope);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose " + maxTargets + " target card" + (maxTargets != 1 ? "s" : "") + " from " + zoneLabel + " to exile.");
    }

    /**
     * ETB targeting for "return up to N target [type] cards from your graveyard to your hand"
     * (Tilling Treefolk). The controller picks up to {@code maxTargets} matching cards from their
     * own graveyard as the trigger goes on the stack; the chosen ids are stored on the triggered
     * ability and moved to hand at resolution by
     * {@code ReturnTargetCardsFromGraveyardToHandEffectHandler}. "Up to N" allows choosing zero,
     * which covers the "you may" clause. With no matching cards the trigger is still put onto the
     * stack with no targets.
     */
    public void handleReturnToHandETBTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, ReturnTargetCardsFromGraveyardToHandEffect returnEffect) {
        handleControllerGraveyardMultiTargetETB(gameData, controllerId, card, effects,
                returnEffect.filter(), returnEffect.maxTargets(),
                returnEffect.minTargets(), " from your graveyard to return to your hand.");
    }

    /** ETB targeting for returning up to a computed number of cards to the battlefield. */
    public void handleReturnToBattlefieldETBTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect,
            int maxTargets) {
        handleControllerGraveyardMultiTargetETB(gameData, controllerId, card, effects,
                returnEffect.filter(), maxTargets,
                " from your graveyard to return to the battlefield.");
    }

    /**
     * ETB targeting for "you may shuffle up to N target cards from your graveyard into your library"
     * (Ghostly Castigator). Same multi-select flow as return-to-hand; "up to N" covers the "you may".
     */
    public void handleShuffleIntoLibraryETBTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect shuffleEffect) {
        handleControllerGraveyardMultiTargetETB(gameData, controllerId, card, effects,
                shuffleEffect.filter(), shuffleEffect.maxTargets(),
                " from your graveyard to shuffle into your library.");
    }

    /**
     * ETB targeting for an effect that first targets a player and then chooses cards from that
     * player's graveyard. The player target is already selected when this method begins.
     */
    public void handleTargetPlayerGraveyardChoiceETBTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, UUID targetPlayerId, GraveyardCardChoosingEffect choosingEffect) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, targetPlayerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (choosingEffect.graveyardChoiceFilter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                                graveyardCard, choosingEffect.graveyardChoiceFilter(), card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        if (matchingCards.isEmpty()) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ETB ability",
                    new ArrayList<>(effects),
                    0,
                    targetPlayerId,
                    null,
                    Map.of(),
                    null,
                    List.of(),
                    List.of()
            ));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
            log.info("Game {} - {} ETB ability targeted player {} with no graveyard card targets",
                    gameData.id, card.getName(), targetPlayerId);
            return;
        }

        int maxTargets = Math.min(choosingEffect.graveyardChoiceMaxTargets(), matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
        gameData.graveyardTargetOperation.targetPlayerId = targetPlayerId;
        gameData.graveyardTargetOperation.anyNumber = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose any number of target cards from "
                        + gameData.playerIdToName.get(targetPlayerId) + "'s graveyard.");
    }

    private void handleControllerGraveyardMultiTargetETB(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, CardPredicate filter, int requestedMaxTargets, String promptSuffix) {
        handleControllerGraveyardMultiTargetETB(gameData, controllerId, card, effects, filter,
                requestedMaxTargets, 0, promptSuffix);
    }

    private void handleControllerGraveyardMultiTargetETB(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, CardPredicate filter, int requestedMaxTargets, int minTargets,
            String promptSuffix) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (filter == null
                        || predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        if (matchingCards.size() < minTargets) {
            if (minTargets > 0) {
                gameLogService.append(gameData, GameLog.cardThen(card,
                        "'s enter-the-battlefield ability has no legal targets."));
                log.info("Game {} - {} ETB ability skipped (not enough matching graveyard cards)",
                        gameData.id, card.getName());
                return;
            }
        }
        if (matchingCards.isEmpty() || requestedMaxTargets <= 0) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ETB ability",
                    new ArrayList<>(effects),
                    List.of()
            ));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
            log.info("Game {} - {} ETB ability pushed onto stack with 0 targets (no matching graveyard cards)",
                    gameData.id, card.getName());
        } else {
            int maxTargets = Math.min(requestedMaxTargets, matchingCards.size());
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
            String choicePrompt = minTargets > 0
                    ? "Choose " + minTargets + " target card" + (minTargets != 1 ? "s" : "") + promptSuffix
                    : "Choose up to " + maxTargets + " target card" + (maxTargets != 1 ? "s" : "")
                    + promptSuffix;
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                    minTargets, choicePrompt);
        }
    }

    /**
     * Unblocked-attack-trigger targeting for "Whenever this creature attacks and isn't blocked, you
     * may exile up to N target [type] cards from defending player's graveyard" (Rysorian Badger).
     * The cards are chosen from the defending player's graveyard as the trigger goes on the stack
     * (CR 603.3d); "up to N" allows choosing zero, which covers the "you may". The attacker rides
     * along as {@code sourcePermanentId} so the "if you do" rider (assigns no combat damage) knows
     * which creature it applies to. With no matching cards the trigger is still put onto the stack
     * with no targets and resolves as a no-op.
     */
    public void handleUnblockedAttackGraveyardChoiceTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, UUID sourcePermanentId, UUID defendingPlayerId,
            GraveyardCardChoosingEffect choosingEffect) {
        handleSinglePlayerGraveyardChoiceTargeting(gameData, controllerId, card, effects, sourcePermanentId,
                defendingPlayerId, choosingEffect, "unblocked-attack", "defending player's graveyard");
    }

    /**
     * Combat-damage-trigger targeting for "Whenever this creature deals combat damage to a player,
     * exile up to N target cards from that player's graveyard" (Skullsnatcher). Same trigger-time
     * up-to-N selection as the unblocked-attack flow (CR 603.3d), narrowed to the damaged player's
     * graveyard.
     */
    public void handleCombatDamageGraveyardChoiceTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, UUID sourcePermanentId, UUID damagedPlayerId,
            GraveyardCardChoosingEffect choosingEffect) {
        handleSinglePlayerGraveyardChoiceTargeting(gameData, controllerId, card, effects, sourcePermanentId,
                damagedPlayerId, choosingEffect, "combat damage", "that player's graveyard");
    }

    private void handleSinglePlayerGraveyardChoiceTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, UUID sourcePermanentId, UUID defendingPlayerId,
            GraveyardCardChoosingEffect choosingEffect, String triggerLabel, String zoneLabel) {
        CardPredicate filter = choosingEffect.graveyardChoiceFilter();

        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, defendingPlayerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (filter == null
                        || predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        if (matchingCards.isEmpty()) {
            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s " + triggerLabel + " trigger",
                    new ArrayList<>(effects),
                    defendingPlayerId,
                    sourcePermanentId);
            trigger.setNonTargeting(true);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.cardThen(card,
                    "'s " + triggerLabel + " ability triggers with no graveyard targets."));
            log.info("Game {} - {} {} graveyard trigger pushed with 0 targets", gameData.id, card.getName(), triggerLabel);
            return;
        }

        int maxTargets = Math.min(choosingEffect.graveyardChoiceMaxTargets(), matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
        gameData.graveyardTargetOperation.sourcePermanentId = sourcePermanentId;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                card.getName() + "'s ability — Choose up to " + maxTargets + " target card"
                        + (maxTargets != 1 ? "s" : "") + " from " + zoneLabel + " to exile.");
        gameLogService.append(gameData, GameLog.cardThen(card,
                "'s " + triggerLabel + " trigger — choose graveyard targets."));
    }

    /**
     * Attack-trigger targeting for "Whenever this creature attacks, exile target card from defending
     * player's graveyard" (Graven Abomination). Chooses the graveyard card as the trigger goes on the
     * stack. Prefer {@code defendingPlayerId} when known; otherwise search the graveyards the
     * effect's declared {@link GraveyardSearchScope} names. No legal target ⇒ trigger skipped
     * (CR 603.3c). Routes by {@code targetSpec()} so callers need no concrete-effect
     * {@code instanceof}.
     */
    public void handleAttackGraveyardTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, UUID sourcePermanentId, UUID defendingPlayerId) {
        GraveyardSearchScope scope = effects.stream()
                .map(e -> e.targetSpec().graveyardScope().orElse(null))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseGet(() -> effects.stream().anyMatch(GraveyardCardChoosingEffect.class::isInstance)
                        ? GraveyardSearchScope.ALL_GRAVEYARDS : null);
        if (scope == null) {
            return;
        }
        GraveyardTargetingSupport.Target target = graveyardTargetingSupport.findTarget(effects);
        CardPredicate filter = target == null ? null : target.filter();

        List<UUID> searchPlayerIds = scope == GraveyardSearchScope.CONTROLLERS_GRAVEYARD
                ? List.of(controllerId)
                : defendingPlayerId != null && scope == GraveyardSearchScope.OPPONENT_GRAVEYARD
                        ? List.of(defendingPlayerId)
                        : scope.graveyardOwners(gameData.orderedPlayerIds, controllerId);

        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : searchPlayerIds) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (filter == null || predicateEvaluationService.matchesCardPredicate(
                        graveyardCard, filter, card.getId(), gameData, playerId)) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(card,
                    "'s attack trigger has no valid graveyard targets."));
            log.info("Game {} - {} attack graveyard trigger skipped (no valid targets)",
                    gameData.id, card.getName());
            return;
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
        gameData.graveyardTargetOperation.sourcePermanentId = sourcePermanentId;

        String zoneLabel = scope == GraveyardSearchScope.CONTROLLERS_GRAVEYARD
                ? "your graveyard"
                : defendingPlayerId != null && scope == GraveyardSearchScope.OPPONENT_GRAVEYARD
                        ? "defending player's graveyard"
                        : zoneLabel(scope);
        String destination = target == null ? "to exile" : target.destination();
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, 1,
                card.getName() + "'s ability — Choose target card from " + zoneLabel + " " + destination + ".");

        gameLogService.append(gameData, GameLog.cardThen(card,
                "'s attack trigger — choose a graveyard target."));
        log.info("Game {} - {} attack graveyard trigger awaiting target selection",
                gameData.id, card.getName());
    }

    public void handleBeginningOfCombatGraveyardTargeting(GameData gameData, UUID controllerId, Card card,
            List<CardEffect> effects, UUID sourcePermanentId,
            ExileGraveyardCardsEffect exileEffect) {
        CardPredicate filter = exileEffect.filter();

        List<Card> matchingCards = new ArrayList<>();
        List<UUID> searchPlayerIds = exileEffect.targetSpec().graveyardScope().orElseThrow()
                .graveyardOwners(gameData.orderedPlayerIds, controllerId);
        for (UUID playerId : searchPlayerIds) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (filter == null
                        || predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        String description = card.getName() + "'s beginning of combat ability";

        if (matchingCards.isEmpty()) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    description,
                    new ArrayList<>(effects),
                    0,
                    null,
                    sourcePermanentId,
                    Map.of(),
                    null,
                    List.of(),
                    List.of()
            ));
            String logEntry = description + " triggers.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} beginning-of-combat trigger pushed onto stack with 0 graveyard targets",
                    gameData.id, card.getName());
            return;
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
        gameData.graveyardTargetOperation.sourcePermanentId = sourcePermanentId;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, 1,
                "Choose up to one target card from a graveyard to exile.");

        String logEntry = description + " triggers — choose a graveyard target.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} beginning-of-combat trigger awaiting graveyard target selection",
                gameData.id, card.getName());
    }

    public void handleGraveyardCastETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                 List<CardEffect> effects) {
        List<Card> matchingCards = collectCastableInstantsAndSorceries(gameData, controllerId, card, effects);

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability has no valid targets."));
            log.info("Game {} - {} ETB graveyard cast has no valid targets", gameData.id, card.getName());
        } else {
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, 1,
                    "Choose target instant or sorcery card from a graveyard to cast.");
        }
    }

    /**
     * Attack-trigger flavour of {@link #handleGraveyardCastETBTargeting} — "whenever this creature attacks,
     * you may cast target instant or sorcery card from your graveyard" (The Dawning Archaic). The target is
     * chosen as the trigger goes on the stack; with no legal target the trigger is skipped (CR 603.3c).
     */
    public void handleAttackGraveyardCastTargeting(GameData gameData, UUID controllerId, Card card,
                                                   List<CardEffect> effects, UUID sourcePermanentId) {
        List<Card> matchingCards = collectCastableInstantsAndSorceries(gameData, controllerId, card, effects);

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s attack trigger has no valid targets."));
            log.info("Game {} - {} attack graveyard cast has no valid targets", gameData.id, card.getName());
            return;
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
        gameData.graveyardTargetOperation.sourcePermanentId = sourcePermanentId;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, 1,
                "Choose target instant or sorcery card from a graveyard to cast.");
    }

    private List<Card> collectCastableInstantsAndSorceries(GameData gameData, UUID controllerId, Card sourceCard,
                                                           List<CardEffect> effects) {
        CastTargetInstantOrSorceryFromGraveyardEffect castEffect = effects.stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect)
                .map(e -> (CastTargetInstantOrSorceryFromGraveyardEffect) e)
                .findFirst().orElseThrow();
        GraveyardSearchScope scope = castEffect.scope();

        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            boolean include = switch (scope) {
                case OPPONENT_GRAVEYARD -> !playerId.equals(controllerId);
                case CONTROLLERS_GRAVEYARD -> playerId.equals(controllerId);
                case ALL_GRAVEYARDS -> true;
            };
            if (!include) continue;
            List<Card> graveyard = targetableGraveyard(gameData, playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if ((graveyardCard.hasType(CardType.INSTANT) || graveyardCard.hasType(CardType.SORCERY))
                        && (castEffect.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        graveyardCard, castEffect.filter(), sourceCard.getId()))) {
                    matchingCards.add(graveyardCard);
                }
            }
        }
        return matchingCards;
    }

    public void handleGraveyardMayPlayETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                    List<CardEffect> effects) {
        ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect mayPlayEffect = effects.stream()
                .filter(e -> e instanceof ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect)
                .map(e -> (ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect) e)
                .findFirst().orElseThrow();
        CardPredicate filter = mayPlayEffect.filter();

        List<Card> matchingCards = new ArrayList<>();
        List<UUID> searchPlayerIds = mayPlayEffect.targetSpec().graveyardScope().orElseThrow()
                .graveyardOwners(gameData.orderedPlayerIds, controllerId);
        for (UUID playerId : searchPlayerIds) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (filter == null
                        || predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability has no valid targets."));
            log.info("Game {} - {} ETB graveyard may-play has no valid targets", gameData.id, card.getName());
        } else {
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, 1,
                    "Choose target card from your graveyard to exile.");
        }
    }

    public void handlePutCreatureFromOpponentGraveyardETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                                    List<CardEffect> effects) {
        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            List<Card> graveyard = targetableGraveyard(gameData, playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (graveyardCard.hasType(CardType.CREATURE)) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability has no valid targets."));
            log.info("Game {} - {} ETB opponent-graveyard steal has no valid targets", gameData.id, card.getName());
        } else {
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, 1,
                    "Choose target creature card from an opponent's graveyard.");
        }
    }

    public void handleGrantFlashbackETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                  List<CardEffect> effects) {
        GrantFlashbackToTargetGraveyardCardEffect flashbackEffect = effects.stream()
                .filter(e -> e instanceof GrantFlashbackToTargetGraveyardCardEffect)
                .map(e -> (GrantFlashbackToTargetGraveyardCardEffect) e)
                .findFirst().orElseThrow();

        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                boolean matchesType = false;
                for (CardType type : flashbackEffect.cardTypes()) {
                    if (graveyardCard.hasType(type)) {
                        matchesType = true;
                        break;
                    }
                }
                if (matchesType) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability has no valid targets."));
            log.info("Game {} - {} ETB grant flashback has no valid targets", gameData.id, card.getName());
        } else {
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, 1,
                    "Choose target instant or sorcery card in your graveyard to gain flashback.");
        }
    }

    public void handleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                               StackEntryType entryType, int xValue) {
        handleExactNGraveyardSpellTargeting(gameData, controllerId, card, entryType, xValue,
                new com.github.laxika.magicalvibes.model.filter.CardTypePredicate(CardType.CREATURE),
                "to exile");
    }

    public void handleExactNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                     StackEntryType entryType, int targetCount,
                                                     CardPredicate filter, String destination) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = targetCount;
        gameData.graveyardTargetOperation.anyNumber = false;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, targetCount,
                targetCount, "Choose " + targetCount + " target " + CardPredicateUtils.describeFilter(filter)
                        + (targetCount != 1 ? "s" : "") + " from your graveyard " + destination + ".");
    }

    public void handleAnyNumberGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                        StackEntryType entryType, CardPredicate filter) {
        handleAnyNumberGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter,
                card.getEffects(EffectSlot.SPELL));
    }

    public void handleAnyNumberGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                        StackEntryType entryType, CardPredicate filter,
                                                        List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        int maxTargets = matchingCards.size();
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose any number of target " + filterLabel + "s from your graveyard.");
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                    List<CardEffect> spellEffects) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter, maxTargetsCap,
                spellEffects, 0, false, false);
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType,
                                                    ReturnTargetCardsFromGraveyardToHandEffect returnEffect,
                                                    int maxTargetsCap, List<CardEffect> spellEffects) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, returnEffect.filter(),
                maxTargetsCap, spellEffects, returnEffect.minTargets(),
                returnEffect.requireSharedCreatureType(), false);
    }

    /**
     * Targets the union of several independently optional graveyard target groups. The answer
     * handler validates that the selected cards can be assigned one-to-one to the effect's filters.
     */
    public void handleUpToOneOfEachFilterGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                                                   Card card, StackEntryType entryType,
                                                                   ReturnUpToOneOfEachFilterFromGraveyardToHandEffect effect,
                                                                   List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (effect.filters().stream().anyMatch(filter ->
                        predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId()))) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards,
                effect.filters().size(),
                "Choose up to " + effect.filters().size()
                        + " target cards from your graveyard to return to your hand.");
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                    StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                    List<CardEffect> spellEffects, boolean fromBattlefieldThisTurn) {
        handleUpToNGraveyardSpellTargeting(gameData, controllerId, card, entryType, filter, maxTargetsCap,
                spellEffects, 0, false, fromBattlefieldThisTurn);
    }

    private void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                     StackEntryType entryType, CardPredicate filter,
                                                     int maxTargetsCap, List<CardEffect> spellEffects,
                                                     int minTargets, boolean requireSharedCreatureType,
                                                     boolean fromBattlefieldThisTurn) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, controllerId);
        var trackedIds = fromBattlefieldThisTurn
                ? gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.getOrDefault(controllerId, java.util.Set.of())
                : null;
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if ((trackedIds == null || trackedIds.contains(graveyardCard.getId()))
                        && predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        if (requireSharedCreatureType) {
            matchingCards.removeIf(candidate -> matchingCards.stream()
                    .noneMatch(other -> !other.getId().equals(candidate.getId())
                            && gameQueryService.shareCreatureType(candidate, other)));
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        String prompt = minTargets > 0
                ? "Choose " + minTargets + " target " + filterLabel + "s that share a creature type from your graveyard."
                : "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from your graveyard.";
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                minTargets, prompt);
    }

    /**
     * "Put up to N target cards from an opponent's graveyard on top of their library"
     * (Misinformation). Pools every opponent's graveyard as a legal target but flags the choice
     * {@code singleGraveyard}, so all chosen cards must come from the same opponent's graveyard.
     */
    public void handleUpToNOpponentGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                            StackEntryType entryType, CardPredicate filter,
                                                            int maxTargetsCap, List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.singleGraveyard = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose up to " + maxTargetsCap + " target card" + (maxTargetsCap != 1 ? "s" : "")
                        + " from an opponent's graveyard.");
    }

    public void handleUpToNTargetPlayerGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                                                UUID targetPlayerId, Card card,
                                                                StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                                List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, targetPlayerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.targetPlayerId = targetPlayerId;
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from " + targetPlayerName + "'s graveyard.");
    }

    /**
     * Begins the next cast-time graveyard target group for a spell that has more than one such
     * group. Empty groups are recorded without opening a pointless prompt.
     */
    public boolean beginNextSpellGraveyardChoice(GameData gameData) {
        var operation = gameData.graveyardTargetOperation;
        while (true) {
            if (operation.activeSpellGraveyardChoiceEffect == null) {
                if (operation.pendingSpellGraveyardChoiceEffects == null
                        || operation.pendingSpellGraveyardChoiceEffects.isEmpty()) {
                    return false;
                }
                operation.activeSpellGraveyardChoiceEffect =
                    operation.pendingSpellGraveyardChoiceEffects.remove(0);
            }

            CardEffect choiceEffect = operation.activeSpellGraveyardChoiceEffect;
            if (choiceEffect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnEffect) {
                List<Card> matchingCards = matchingCardsInGraveyard(gameData, operation.controllerId,
                        returnEffect.filter(), operation.card.getId());
                if (returnEffect.requireSharedCreatureType()) {
                    matchingCards.removeIf(candidate -> matchingCards.stream()
                            .noneMatch(other -> !other.getId().equals(candidate.getId())
                                    && gameQueryService.shareCreatureType(candidate, other)));
                }
                if (!matchingCards.isEmpty()) {
                    handleUpToNGraveyardSpellTargeting(gameData, operation.controllerId, operation.card,
                            operation.entryType, returnEffect, returnEffect.maxTargets(), operation.effects);
                    return true;
                }
            } else if (choiceEffect instanceof ShuffleTargetCardsFromGraveyardIntoLibraryEffect shuffleEffect) {
                UUID targetPlayerId = operation.targetPlayerId;
                if (targetPlayerId == null) {
                    throw new IllegalStateException("Must target a player");
                }
                List<Card> matchingCards = matchingCardsInGraveyard(gameData, targetPlayerId,
                        shuffleEffect.filter(), operation.card.getId());
                if (!matchingCards.isEmpty()) {
                    handleUpToNTargetPlayerGraveyardSpellTargeting(gameData, operation.controllerId,
                            targetPlayerId, operation.card, operation.entryType, shuffleEffect.filter(),
                            shuffleEffect.maxTargets(), operation.effects);
                    return true;
                }
            } else {
                throw new IllegalStateException("Unsupported spell graveyard target group: "
                        + choiceEffect.getClass().getSimpleName());
            }

            operation.spellGraveyardCardIdsByEffect.put(choiceEffect, List.of());
            operation.activeSpellGraveyardChoiceEffect = null;
        }
    }

    private List<Card> matchingCardsInGraveyard(GameData gameData, UUID playerId,
                                                 CardPredicate filter, UUID sourceCardId) {
        List<Card> graveyard = targetableGraveyard(gameData, playerId);
        if (graveyard == null) {
            return new ArrayList<>();
        }
        return graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, sourceCardId))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    public void handleExactNTargetPlayerGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                                                 UUID targetPlayerId, Card card,
                                                                 StackEntryType entryType, int targetCount,
                                                                 CardPredicate filter, List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> graveyard = targetableGraveyard(gameData, targetPlayerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = targetCount;
        gameData.graveyardTargetOperation.anyNumber = false;
        gameData.graveyardTargetOperation.targetPlayerId = targetPlayerId;
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, targetCount,
                targetCount, "Choose " + targetCount + " target " + filterLabel + ""
                        + (targetCount != 1 ? "s" : "") + " from " + targetPlayerName + "'s graveyard.");
    }

    /**
     * "Exile up to N target cards from a single graveyard" (Scarab Feast). Pools every card in
     * every graveyard that matches the effect's filter, and flags the choice {@code singleGraveyard} so
     * {@code GraveyardChoiceHandlerService} rejects a selection spanning more than one graveyard.
     */
    public void handleUpToNSingleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                         StackEntryType entryType, int maxTargetsCap,
                                                         com.github.laxika.magicalvibes.model.filter.CardPredicate filter,
                                                         List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId);
            if (graveyard == null) continue;
            matchingCards.addAll(graveyard.stream()
                    .filter(candidate -> filter == null
                            || predicateEvaluationService.matchesCardPredicate(candidate, filter, card.getId()))
                    .toList());
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.singleGraveyard = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose up to " + maxTargetsCap + " target card" + (maxTargetsCap != 1 ? "s" : "")
                        + " from a single graveyard to exile.");
    }

    public void handleUpToNAllGraveyardsSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                        StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                                        List<CardEffect> spellEffects) {
        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = targetableGraveyard(gameData, playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCards.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCards, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from graveyards.");
    }
}

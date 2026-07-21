package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.input.PlayerInputService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggeredAbilityQueueService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TriggerTargetCollector triggerTargetCollector;
    private final com.github.laxika.magicalvibes.service.target.TargetLegalityService targetLegalityService;

    public void processNextDeathTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            PermanentChoiceContext.DeathTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);

            // Graveyard-targeting death trigger (e.g. Ruin Rat): choose a target card in a graveyard
            // to exile, at the time the trigger is put on the stack.
            ExileGraveyardCardsEffect gyExile = pending.effects().stream()
                    .filter(e -> e instanceof ExileGraveyardCardsEffect ege && ege.targetSpec().category().isGraveyard())
                    .map(e -> (ExileGraveyardCardsEffect) e)
                    .findFirst().orElse(null);
            if (gyExile != null) {
                if (beginDeathGraveyardTarget(gameData, pending, gyExile)) {
                    return;
                }
                continue;
            }

            var dyingCard = pending.dyingCard();
            TargetFilter deathFilter = dyingCard.getTargetFilter();
            // The card-level target filter belongs to whichever ability declared it via
            // target(...).addEffect(...). If none of THIS death trigger's effects are bound to a
            // declared target group, that filter is a different ability's cast-time filter (e.g.
            // Soulstinger's ETB "target creature you control") and must not narrow this death
            // trigger — its own effect targets any creature.
            if (deathFilter != null
                    && pending.effects().stream().noneMatch(e -> dyingCard.getEffectTargetIndex(e) >= 0)) {
                deathFilter = null;
            }

            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    deathFilter,
                    pending.controllerId(),
                    dyingCard,
                    TriggerTargetCollector.Options.DEATH);

            if (result.validTargets().isEmpty()) {
                // No valid targets - trigger can't go on the stack, skip it
                gameData.pollPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.dyingCard(),
                        "'s death trigger has no valid targets."));
                log.info("Game {} - {} death trigger skipped (no valid creature targets)",
                        gameData.id, pending.dyingCard().getName());
                continue;
            }

            // Remove from queue and begin permanent choice
            gameData.pollPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            String targetDescription = (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : "target creature";
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), result.validTargets(),
                    pending.dyingCard().getName() + "'s ability - Choose " + targetDescription + ".");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.dyingCard(),
                    "'s death trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} death trigger awaiting target selection", gameData.id, pending.dyingCard().getName());
            return;
        }
    }

    /**
     * Collects the graveyard target for a "when this creature dies, exile target card from a(n
     * opponent's) graveyard" trigger (Ruin Rat) and begins the card choice, at the time the trigger
     * is put on the stack. Opponent scope ({@code GRAVEYARD_CARD}) searches only opponents'
     * graveyards; any scope ({@code ANY_GRAVEYARD_CARD}) searches every graveyard. Returns
     * {@code true} if input was begun (caller should return), or {@code false} if the trigger was
     * skipped for lack of a legal target (caller should continue) — a targeted death trigger with no
     * legal target is never put on the stack (CR 603.3c).
     */
    private boolean beginDeathGraveyardTarget(GameData gameData,
            PermanentChoiceContext.DeathTriggerTarget pending, ExileGraveyardCardsEffect gyExile) {
        CardPredicate filter = gyExile.filter();
        boolean anyGraveyard = gyExile.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;

        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!anyGraveyard && playerId.equals(pending.controllerId())) continue; // opponent's graveyard only
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (filter == null
                        || predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, pending.dyingCard().getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        gameData.pollPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);

        if (matchingCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.dyingCard(),
                    "'s death trigger has no valid graveyard targets."));
            log.info("Game {} - {} death graveyard trigger skipped (no valid targets)",
                    gameData.id, pending.dyingCard().getName());
            return false;
        }

        gameData.graveyardTargetOperation.card = pending.dyingCard();
        gameData.graveyardTargetOperation.controllerId = pending.controllerId();
        gameData.graveyardTargetOperation.effects = new ArrayList<>(pending.effects());

        String zoneLabel = anyGraveyard ? "a graveyard" : "an opponent's graveyard";
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, pending.controllerId(), matchingCards, 1,
                pending.dyingCard().getName() + "'s ability — Choose target " + filterLabel + " from " + zoneLabel + " to exile.");

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.dyingCard(),
                "'s death trigger — choose a graveyard target."));
        log.info("Game {} - {} death graveyard trigger awaiting target selection",
                gameData.id, pending.dyingCard().getName());
        return true;
    }

    public void processNextSelfLeavesTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SelfLeavesTriggerTarget.class)) {
            PermanentChoiceContext.SelfLeavesTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.SelfLeavesTriggerTarget.class);

            // Graveyard-targeting self-leaves trigger (e.g. Offalsnout): choose a target card in a
            // graveyard to exile, at the time the trigger is put on the stack.
            ExileGraveyardCardsEffect gyExile = pending.effects().stream()
                    .filter(e -> e instanceof ExileGraveyardCardsEffect ege && ege.targetSpec().category().isGraveyard())
                    .map(e -> (ExileGraveyardCardsEffect) e)
                    .findFirst().orElse(null);
            if (gyExile != null) {
                if (beginSelfLeavesGraveyardTarget(gameData, pending, gyExile)) {
                    return;
                }
                continue;
            }

            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    pending.sourceCard().getTargetFilter(),
                    pending.controllerId(),
                    pending.sourceCard(),
                    TriggerTargetCollector.Options.END_STEP);

            if (result.validTargets().isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.SelfLeavesTriggerTarget.class);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s leaves-the-battlefield trigger has no valid targets."));
                log.info("Game {} - {} leaves-battlefield trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.SelfLeavesTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            String targetDescription = (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : "target creature";
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), result.validTargets(),
                    pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s leaves-the-battlefield trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} leaves-battlefield trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    /**
     * Collects the graveyard targets for a leaves-the-battlefield exile trigger (e.g. Offalsnout) and
     * begins the card choice. Returns {@code true} if input was begun (caller should return), or
     * {@code false} if the trigger was skipped for lack of a legal target (caller should continue).
     */
    private boolean beginSelfLeavesGraveyardTarget(GameData gameData,
            PermanentChoiceContext.SelfLeavesTriggerTarget pending, ExileGraveyardCardsEffect gyExile) {
        CardPredicate filter = gyExile.filter();
        boolean anyGraveyard = gyExile.targetSpec().category() == TargetCategory.ANY_GRAVEYARD_CARD;

        List<Card> matchingCards = new ArrayList<>();
        List<UUID> searchPlayerIds = anyGraveyard ? gameData.orderedPlayerIds : List.of(pending.controllerId());
        for (UUID playerId : searchPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (filter == null
                        || predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, pending.sourceCard().getId())) {
                    matchingCards.add(graveyardCard);
                }
            }
        }

        gameData.pollPendingInteraction(PermanentChoiceContext.SelfLeavesTriggerTarget.class);

        if (matchingCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s leaves-the-battlefield trigger has no valid graveyard targets."));
            log.info("Game {} - {} leaves-battlefield graveyard trigger skipped (no valid targets)",
                    gameData.id, pending.sourceCard().getName());
            return false;
        }

        gameData.graveyardTargetOperation.card = pending.sourceCard();
        gameData.graveyardTargetOperation.controllerId = pending.controllerId();
        gameData.graveyardTargetOperation.effects = new ArrayList<>(pending.effects());

        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, pending.controllerId(), matchingCards, 1,
                pending.sourceCard().getName() + "'s ability — Choose target " + filterLabel + " from a graveyard to exile.");

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                "'s leaves-the-battlefield trigger — choose a graveyard target."));
        log.info("Game {} - {} leaves-battlefield graveyard trigger awaiting target selection",
                gameData.id, pending.sourceCard().getName());
        return true;
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)) {
            PermanentChoiceContext.AttackTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);

            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    pending.sourceCard().getTargetFilter(),
                    pending.controllerId(),
                    pending.sourceCard(),
                    TriggerTargetCollector.Options.ATTACK);

            if (result.validTargets().isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s attack trigger has no valid targets."));
                log.info("Game {} - {} attack trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            String targetDescription = (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : "target permanent";
            gameData.pollPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), result.validTargets(),
                    pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s attack trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} attack trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    /**
     * Stage 1 of Decimator Beetle's attack trigger: prompt the controller to choose the creature they
     * control that a counter is removed from. The stage-2 (defending-player) choice is begun by the
     * stage-1 response handler in {@code PermanentChoiceTriggerHandlerService}.
     */
    public void processNextAttackCounterMoveFirstTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class)) {
            PermanentChoiceContext.AttackCounterMoveFirstTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class);

            List<UUID> validTargets = targetableCreaturesControlledBy(
                    gameData, pending.controllerId(), pending.sourceCard(), pending.controllerId());
            if (validTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s attack trigger has no valid targets."));
                log.info("Game {} - {} attack trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability - Choose target creature you control.");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s attack trigger - choose target creature you control."));
            log.info("Game {} - {} attack trigger awaiting first target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    /**
     * Ids of every creature the given player controls that {@code sourceCard}, controlled by
     * {@code choosingPlayerId}, may legally target (excludes shroud / opponent-hexproof / protection).
     * Shared by both stages of the attack counter-move target choices.
     */
    public List<UUID> targetableCreaturesControlledBy(GameData gameData, UUID playerId,
                                                      Card sourceCard, UUID choosingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        List<UUID> result = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && targetLegalityService.checkSpellPermanentTargetableReason(
                            gameData, permanent, sourceCard, choosingPlayerId).isEmpty()) {
                result.add(permanent.getId());
            }
        }
        return result;
    }

    public void processNextEntersTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)) {
            PermanentChoiceContext.EntersTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);

            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    pending.sourceCard().getTargetFilter(),
                    pending.controllerId(),
                    pending.sourceCard(),
                    TriggerTargetCollector.Options.ATTACK);

            if (result.validTargets().isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s enter trigger has no valid targets."));
                log.info("Game {} - {} enter trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            String targetDescription = (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : "target permanent";
            gameData.pollPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), result.validTargets(),
                    pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s enter trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} enter trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextDiscardControllerTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class)) {
            PermanentChoiceContext.DiscardControllerTriggerTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class);

            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    pending.sourceCard().getTargetFilter(),
                    pending.controllerId(),
                    pending.sourceCard(),
                    TriggerTargetCollector.Options.ATTACK);

            if (result.validTargets().isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s discard trigger has no valid targets."));
                log.info("Game {} - {} discard trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            String targetDescription = (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : "target permanent";
            gameData.pollPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), result.validTargets(),
                    pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s discard trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} discard trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            PermanentChoiceContext.DiscardTriggerAnyTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class);

            // Collect valid targets: all creatures and planeswalkers on all battlefields + all players
            List<UUID> validPermanentTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (gameQueryService.isCreature(gameData, p)
                            || p.getCard().hasType(CardType.PLANESWALKER)) {
                        validPermanentTargets.add(p.getId());
                    }
                }
            }

            List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

            // There are always valid targets (at least the players)
            gameData.pollPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.discardedCard().getName() + "'s ability - Choose any target.");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.discardedCard(),
                    "'s discard trigger - choose a target."));
            log.info("Game {} - {} discard trigger awaiting target selection", gameData.id, pending.discardedCard().getName());
            return;
        }
    }

    public void processNextSpellTargetTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)) {
            PermanentChoiceContext.SpellTargetTriggerAnyTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);

            // Collect valid targets based on whether this is player-only targeting
            List<UUID> validPermanentTargets = new ArrayList<>();
            if (!pending.playerTargetOnly()) {
                TargetFilter filter = pending.targetFilter();
                FilterContext filterContext = filter != null
                        ? FilterContext.of(gameData)
                                .withSourceControllerId(pending.controllerId())
                                .withSourceCardId(pending.sourceCard().getId())
                        : null;

                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (filter != null) {
                            if (predicateEvaluationService.matchesFilters(p, Set.of(filter), filterContext)) {
                                validPermanentTargets.add(p.getId());
                            }
                        } else if (gameQueryService.isCreature(gameData, p)
                                || p.getCard().hasType(CardType.PLANESWALKER)) {
                            validPermanentTargets.add(p.getId());
                        }
                    }
                }

                // If a target filter is present but no valid targets exist, skip this trigger
                if (filter != null && validPermanentTargets.isEmpty()) {
                    gameData.pollPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
                    log.info("Game {} - {} spell-target trigger skipped (no valid targets)",
                            gameData.id, pending.sourceCard().getName());
                    continue;
                }
            }

            List<UUID> validPlayerTargets = pending.targetFilter() != null
                    ? List.of()
                    : new ArrayList<>(gameData.orderedPlayerIds);

            String prompt = pending.playerTargetOnly()
                    ? pending.sourceCard().getName() + "'s ability - Choose target player."
                    : pending.sourceCard().getName() + "'s ability - Choose any target.";

            // There are always valid targets (at least the players, or filtered permanents)
            gameData.pollPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets, prompt);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s triggered ability - choose a target."));
            log.info("Game {} - {} spell-target trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextLifeGainTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class)) {
            PermanentChoiceContext.LifeGainTriggerAnyTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class);

            // Collect valid targets: all creatures on all battlefields + all players
            List<UUID> validPermanentTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (gameQueryService.isCreature(gameData, p)) {
                        validPermanentTargets.add(p.getId());
                    }
                }
            }

            // "Target creature" abilities (Cradle of Vitality) offer no player targets; if no creature
            // is a legal target the ability has no legal target and is removed without going on the stack.
            if (pending.creaturesOnly() && validPermanentTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class);
                log.info("Game {} - {} life gain trigger skipped (no legal creature target)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            List<UUID> validPlayerTargets = pending.creaturesOnly()
                    ? List.of()
                    : new ArrayList<>(gameData.orderedPlayerIds);

            String prompt = pending.creaturesOnly()
                    ? pending.sourceCard().getName() + "'s ability - Choose target creature."
                    : pending.sourceCard().getName() + "'s ability - Choose target creature or player.";

            // There are always valid targets (at least the players, or the checked creatures)
            gameData.pollPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets, prompt);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s life gain trigger - choose a target."));
            log.info("Game {} - {} life gain trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextDrawTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.DrawTriggerAnyTarget.class)) {
            PermanentChoiceContext.DrawTriggerAnyTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.DrawTriggerAnyTarget.class);

            // "Any target" — every creature on every battlefield, plus every player.
            List<UUID> validPermanentTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (gameQueryService.isCreature(gameData, p)) {
                        validPermanentTargets.add(p.getId());
                    }
                }
            }

            List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

            // There are always valid targets (at least the players)
            gameData.pollPendingInteraction(PermanentChoiceContext.DrawTriggerAnyTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.sourceCard().getName() + "'s ability - Choose target creature or player.");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s draw trigger - choose a target."));
            log.info("Game {} - {} draw trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextEntersFromGraveyardTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.EntersFromGraveyardTriggerTarget.class)) {
            PermanentChoiceContext.EntersFromGraveyardTriggerTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.EntersFromGraveyardTriggerTarget.class);

            // "Any target" — every creature and planeswalker on every battlefield, plus every player.
            List<UUID> validPermanentTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (gameQueryService.isCreature(gameData, p)
                            || p.getCard().hasType(CardType.PLANESWALKER)) {
                        validPermanentTargets.add(p.getId());
                    }
                }
            }

            List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

            // There are always valid targets (at least the players).
            gameData.pollPendingInteraction(PermanentChoiceContext.EntersFromGraveyardTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.sourceCard().getName() + "'s ability - Choose any target.");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s triggered ability - choose any target."));
            log.info("Game {} - {} enters-from-graveyard trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)) {
            PermanentChoiceContext.EmblemTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class);

            // Collect valid targets: all permanents (or only opponent permanents if opponentControlledOnly)
            List<UUID> validTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                if (pending.opponentControlledOnly() && pid.equals(pending.controllerId())) continue;
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    validTargets.add(p.getId());
                }
            }

            if (validTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class);
                String logEntry = pending.emblemDescription() + "'s trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} emblem trigger skipped (no valid permanent targets)",
                        gameData.id, pending.emblemDescription());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            String targetDesc = pending.opponentControlledOnly()
                    ? "target permanent an opponent controls to exile"
                    : "target permanent to exile";
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.emblemDescription() + "'s ability - Choose " + targetDesc + ".");

            String logEntry = pending.emblemDescription() + "'s triggered ability - choose " + targetDesc + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} emblem trigger awaiting target selection", gameData.id, pending.emblemDescription());
            return;
        }
    }

    public void processNextSagaChapterTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            PermanentChoiceContext.SagaChapterTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);

            // Collect valid creature targets, applying any saga target filter
            List<UUID> validCreatureTargets = collectSagaChapterTargets(gameData, pending);

            gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);

            if (validCreatureTargets.isEmpty()) {
                // "Up to one target creature" — no valid targets, push ability with no target
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s chapter " + pending.chapterName() + " ability",
                        new ArrayList<>(pending.effects()),
                        null,
                        pending.sourcePermanentId()
                ));
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s chapter " + pending.chapterName() + " has no valid creature targets."));
                log.info("Game {} - {} chapter {} no valid targets, pushed with null target",
                        gameData.id, pending.sourceCard().getName(), pending.chapterName());
                continue;
            }

            // "Up to one" — add controller player ID as a "skip" option
            List<UUID> allChoices = new ArrayList<>(validCreatureTargets);
            List<UUID> playerChoices = List.of(pending.controllerId());

            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(), allChoices, playerChoices,
                    pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                            + " — Choose target creature, or yourself to skip.");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s chapter " + pending.chapterName() + " - choose target creature."));
            log.info("Game {} - {} chapter {} awaiting target selection", gameData.id, pending.sourceCard().getName(), pending.chapterName());
            return;
        }
    }

    public void processNextSagaChapterGraveyardTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class)) {
            PermanentChoiceContext.SagaChapterGraveyardTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class);

            // Find the graveyard-targeting effect to extract its filter
            CardPredicate filter = null;
            for (CardEffect effect : pending.effects()) {
                if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
                    filter = returnEffect.filter();
                    break;
                }
            }

            // Collect valid graveyard targets from the controller's graveyard
            List<Card> matchingCards = new ArrayList<>();
            List<Card> graveyard = gameData.playerGraveyards.get(pending.controllerId());
            if (graveyard != null) {
                for (Card graveyardCard : graveyard) {
                    if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, null)) {
                        matchingCards.add(graveyardCard);
                    }
                }
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class);

            if (matchingCards.isEmpty()) {
                log.info("Game {} - {} chapter {} graveyard-target skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName(), pending.chapterName());
                continue;
            }

            // Set up graveyard target operation (entryType = null → triggered ability path)
            gameData.graveyardTargetOperation.card = pending.sourceCard();
            gameData.graveyardTargetOperation.controllerId = pending.controllerId();
            gameData.graveyardTargetOperation.effects = new ArrayList<>(pending.effects());
            gameData.graveyardTargetOperation.sourcePermanentId = pending.sourcePermanentId();
            gameData.graveyardTargetOperation.chapterName = pending.chapterName();

            String filterLabel = CardPredicateUtils.describeFilter(filter);
            playerInputService.beginMultiGraveyardChoice(gameData, pending.controllerId(), matchingCards, 1,
                    pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                            + " — Choose target " + filterLabel + " from your graveyard.");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s chapter " + pending.chapterName() + " ability triggers — choose a graveyard target."));
            log.info("Game {} - {} chapter {} graveyard-target trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName(), pending.chapterName());
            return;
        }
    }

    public void processNextSpellGraveyardTargetTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            PermanentChoiceContext.SpellGraveyardTargetTrigger pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);

            // Find the graveyard-targeting effect to extract its filter
            CardPredicate filter = null;
            boolean lifeGainedCap = false;
            for (CardEffect effect : pending.effects()) {
                if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
                    filter = returnEffect.filter();
                    lifeGainedCap = returnEffect.maxManaValueEqualsLifeGainedThisTurn();
                    break;
                }
            }

            // "mana value X or less, where X is the life you gained this turn" (e.g. Moseo)
            int maxManaValue = lifeGainedCap
                    ? gameData.getLifeGainedThisTurn(pending.controllerId()) : Integer.MAX_VALUE;

            // Collect valid graveyard targets from the controller's graveyard
            List<Card> matchingCards = new ArrayList<>();
            List<Card> graveyard = gameData.playerGraveyards.get(pending.controllerId());
            if (graveyard != null) {
                for (Card graveyardCard : graveyard) {
                    if (graveyardCard.getManaValue() > maxManaValue) {
                        continue;
                    }
                    if (predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, null)) {
                        matchingCards.add(graveyardCard);
                    }
                }
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);

            if (matchingCards.isEmpty()) {
                log.info("Game {} - {} spell-cast graveyard-target trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            // Set up graveyard target operation (entryType = null → triggered ability path)
            gameData.graveyardTargetOperation.card = pending.sourceCard();
            gameData.graveyardTargetOperation.controllerId = pending.controllerId();
            gameData.graveyardTargetOperation.effects = new ArrayList<>(pending.effects());

            String filterLabel = CardPredicateUtils.describeFilter(filter);
            playerInputService.beginMultiGraveyardChoice(gameData, pending.controllerId(), matchingCards, 1,
                    pending.sourceCard().getName() + "'s ability — Choose target " + filterLabel + " from your graveyard.");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s triggered ability triggers — choose a graveyard target."));
            log.info("Game {} - {} spell-cast graveyard-target trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextExploreTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ExploreTriggerTarget.class)) {
            PermanentChoiceContext.ExploreTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.ExploreTriggerTarget.class);

            // Collect valid targets: only creatures controlled by opponents
            List<UUID> validTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                if (pid.equals(pending.controllerId())) continue; // skip controller — opponent only
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (!gameQueryService.isCreature(gameData, p)) continue;
                    validTargets.add(p.getId());
                }
            }

            if (validTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ExploreTriggerTarget.class);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s explore trigger has no valid targets."));
                log.info("Game {} - {} explore trigger skipped (no valid creature targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ExploreTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability — Choose target creature an opponent controls.");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s explore trigger — choose target creature."));
            log.info("Game {} - {} explore trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextClashTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ClashTriggerTarget.class)) {
            PermanentChoiceContext.ClashTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.ClashTriggerTarget.class);

            // Collect valid targets: only creatures controlled by opponents.
            List<UUID> validTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                if (pid.equals(pending.controllerId())) continue; // opponent only
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (!gameQueryService.isCreature(gameData, p)) continue;
                    validTargets.add(p.getId());
                }
            }

            if (validTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ClashTriggerTarget.class);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s clash trigger has no valid targets."));
                log.info("Game {} - {} clash trigger skipped (no valid creature targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ClashTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability — Choose target creature an opponent controls.");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s clash trigger — choose target creature."));
            log.info("Game {} - {} clash trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    /**
     * Collects valid creature targets for a saga chapter, applying any target predicate
     * declared by the chapter's effects and any chapter-level target filters.
     */
    private List<UUID> collectSagaChapterTargets(GameData gameData,
                                                  PermanentChoiceContext.SagaChapterTarget pending) {
        // Extract target predicate from the first targeting effect that declares one
        PermanentPredicate targetPredicate = pending.effects().stream()
                .filter(e -> e.targetSpec().category().includesPermanents()
                        && EffectResolution.targetPredicateOf(e) != null)
                .map(EffectResolution::targetPredicateOf)
                .findFirst().orElse(null);

        // Also check for chapter-level target filters (e.g. "creature an opponent controls")
        Set<TargetFilter> chapterFilters = pending.targetFilters();
        boolean hasChapterFilters = chapterFilters != null && !chapterFilters.isEmpty();

        FilterContext filterContext = (targetPredicate != null || hasChapterFilters)
                ? FilterContext.of(gameData)
                        .withSourceCardId(pending.sourceCard().getId())
                        .withSourceControllerId(pending.controllerId())
                : null;

        List<UUID> validCreatureTargets = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (!gameQueryService.isCreature(gameData, p)) continue;
                if (targetPredicate != null
                        && !predicateEvaluationService.matchesPermanentPredicate(p, targetPredicate, filterContext)) {
                    continue;
                }
                if (hasChapterFilters
                        && !predicateEvaluationService.matchesFilters(p, chapterFilters, filterContext)) {
                    continue;
                }
                validCreatureTargets.add(p.getId());
            }
        }
        return validCreatureTargets;
    }
}

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

    public void processNextDeathTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            PermanentChoiceContext.DeathTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);

            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    pending.dyingCard().getTargetFilter(),
                    pending.controllerId(),
                    pending.dyingCard(),
                    TriggerTargetCollector.Options.DEATH);

            if (result.validTargets().isEmpty()) {
                // No valid targets - trigger can't go on the stack, skip it
                gameData.pollPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
                String logEntry = pending.dyingCard().getName() + "'s death trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            String logEntry = pending.dyingCard().getName() + "'s death trigger - choose " + targetDescription + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} death trigger awaiting target selection", gameData.id, pending.dyingCard().getName());
            return;
        }
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
                String logEntry = pending.sourceCard().getName() + "'s leaves-the-battlefield trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            String logEntry = pending.sourceCard().getName() + "'s leaves-the-battlefield trigger - choose " + targetDescription + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
            String logEntry = pending.sourceCard().getName() + "'s leaves-the-battlefield trigger has no valid graveyard targets.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

        String logEntry = pending.sourceCard().getName() + "'s leaves-the-battlefield trigger — choose a graveyard target.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                String logEntry = pending.sourceCard().getName() + "'s attack trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            String logEntry = pending.sourceCard().getName() + "'s attack trigger - choose " + targetDescription + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} attack trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
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
                String logEntry = pending.sourceCard().getName() + "'s enter trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            String logEntry = pending.sourceCard().getName() + "'s enter trigger - choose " + targetDescription + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} enter trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
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

            String logEntry = pending.discardedCard().getName() + "'s discard trigger - choose a target.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                        ? FilterContext.of(gameData).withSourceControllerId(pending.controllerId())
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

            String logEntry = pending.sourceCard().getName() + "'s triggered ability - choose a target.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

            // There are always valid targets (at least the players)
            gameData.pollPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.sourceCard().getName() + "'s ability - Choose target creature or player.");

            String logEntry = pending.sourceCard().getName() + "'s life gain trigger - choose a target.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} life gain trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
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

            String logEntry = pending.sourceCard().getName() + "'s triggered ability - choose any target.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                String logEntry = pending.sourceCard().getName() + "'s chapter " + pending.chapterName() + " has no valid creature targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            String logEntry = pending.sourceCard().getName() + "'s chapter " + pending.chapterName() + " - choose target creature.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            String logEntry = pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                    + " ability triggers — choose a graveyard target.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            String logEntry = pending.sourceCard().getName()
                    + "'s triggered ability triggers — choose a graveyard target.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                String logEntry = pending.sourceCard().getName() + "'s explore trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} explore trigger skipped (no valid creature targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ExploreTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability — Choose target creature an opponent controls.");

            String logEntry = pending.sourceCard().getName() + "'s explore trigger — choose target creature.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                String logEntry = pending.sourceCard().getName() + "'s clash trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} clash trigger skipped (no valid creature targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ClashTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability — Choose target creature an opponent controls.");

            String logEntry = pending.sourceCard().getName() + "'s clash trigger — choose target creature.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

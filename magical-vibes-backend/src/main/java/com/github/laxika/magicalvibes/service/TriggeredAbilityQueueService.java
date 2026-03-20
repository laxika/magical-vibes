package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.input.PlayerInputService;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
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
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    public void processNextDeathTriggerTarget(GameData gameData) {
        while (!gameData.pendingDeathTriggerTargets.isEmpty()) {
            PermanentChoiceContext.DeathTriggerTarget pending = gameData.pendingDeathTriggerTargets.peekFirst();

            // Check which target types the effects support
            boolean canTargetPlayers = pending.effects().stream().anyMatch(e -> e.canTargetPlayer());
            boolean canTargetPermanents = pending.effects().stream().anyMatch(e -> e.canTargetPermanent());

            // Collect valid targets based on what the effects can target,
            // respecting the card's target filter if present
            TargetFilter targetFilter = pending.dyingCard().getTargetFilter();
            FilterContext filterCtx = targetFilter != null
                    ? new FilterContext(gameData, pending.dyingCard().getId(), pending.controllerId(), null)
                    : null;

            List<UUID> validTargets = new ArrayList<>();
            if (canTargetPlayers) {
                validTargets.addAll(gameData.orderedPlayerIds);
            }
            if (canTargetPermanents) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (!gameQueryService.isCreature(gameData, p)) continue;
                        if (targetFilter instanceof ControlledPermanentPredicateTargetFilter cpf) {
                            if (!gameQueryService.matchesFilters(p, Set.of(cpf), filterCtx)) continue;
                        } else if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
                            if (!gameQueryService.matchesPermanentPredicate(p, ppf.predicate(), filterCtx)) continue;
                        }
                        validTargets.add(p.getId());
                    }
                }
            }

            if (validTargets.isEmpty()) {
                // No valid targets - trigger can't go on the stack, skip it
                gameData.pendingDeathTriggerTargets.removeFirst();
                String logEntry = pending.dyingCard().getName() + "'s death trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} death trigger skipped (no valid creature targets)",
                        gameData.id, pending.dyingCard().getName());
                continue;
            }

            // Remove from queue and begin permanent choice
            gameData.pendingDeathTriggerTargets.removeFirst();
            gameData.interaction.setPermanentChoiceContext(pending);
            String targetDescription = (canTargetPlayers && canTargetPermanents) ? "any target"
                    : canTargetPlayers ? "target player" : "target creature";
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.dyingCard().getName() + "'s ability - Choose " + targetDescription + ".");

            String logEntry = pending.dyingCard().getName() + "'s death trigger - choose " + targetDescription + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} death trigger awaiting target selection", gameData.id, pending.dyingCard().getName());
            return;
        }
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        while (!gameData.pendingAttackTriggerTargets.isEmpty()) {
            PermanentChoiceContext.AttackTriggerTarget pending = gameData.pendingAttackTriggerTargets.peekFirst();

            // Collect valid targets, respecting the card's target filter if present
            TargetFilter targetFilter = pending.sourceCard().getTargetFilter();
            FilterContext filterCtx = targetFilter != null
                    ? new FilterContext(gameData, pending.sourceCard().getId(), pending.controllerId(), null)
                    : null;

            List<UUID> validTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
                        if (gameQueryService.matchesPermanentPredicate(p, ppf.predicate(), filterCtx)) {
                            validTargets.add(p.getId());
                        }
                    } else {
                        validTargets.add(p.getId());
                    }
                }
            }

            if (validTargets.isEmpty()) {
                gameData.pendingAttackTriggerTargets.removeFirst();
                String logEntry = pending.sourceCard().getName() + "'s attack trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} attack trigger skipped (no valid permanent targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pendingAttackTriggerTargets.removeFirst();
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability - Choose target permanent.");

            String logEntry = pending.sourceCard().getName() + "'s attack trigger - choose a target permanent.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} attack trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        while (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            PermanentChoiceContext.DiscardTriggerAnyTarget pending = gameData.pendingDiscardSelfTriggers.peekFirst();

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
            gameData.pendingDiscardSelfTriggers.removeFirst();
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.discardedCard().getName() + "'s ability - Choose any target.");

            String logEntry = pending.discardedCard().getName() + "'s discard trigger - choose a target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discard trigger awaiting target selection", gameData.id, pending.discardedCard().getName());
            return;
        }
    }

    public void processNextSpellTargetTrigger(GameData gameData) {
        while (!gameData.pendingSpellTargetTriggers.isEmpty()) {
            PermanentChoiceContext.SpellTargetTriggerAnyTarget pending = gameData.pendingSpellTargetTriggers.peekFirst();

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
                            if (gameQueryService.matchesFilters(p, Set.of(filter), filterContext)) {
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
                    gameData.pendingSpellTargetTriggers.removeFirst();
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
            gameData.pendingSpellTargetTriggers.removeFirst();
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets, prompt);

            String logEntry = pending.sourceCard().getName() + "'s triggered ability - choose a target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} spell-target trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextLifeGainTriggerTarget(GameData gameData) {
        while (!gameData.pendingLifeGainTriggerTargets.isEmpty()) {
            PermanentChoiceContext.LifeGainTriggerAnyTarget pending = gameData.pendingLifeGainTriggerTargets.peekFirst();

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
            gameData.pendingLifeGainTriggerTargets.removeFirst();
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.sourceCard().getName() + "'s ability - Choose target creature or player.");

            String logEntry = pending.sourceCard().getName() + "'s life gain trigger - choose a target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} life gain trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        while (!gameData.pendingEmblemTriggerTargets.isEmpty()) {
            PermanentChoiceContext.EmblemTriggerTarget pending = gameData.pendingEmblemTriggerTargets.peekFirst();

            // Collect valid targets: all permanents on all battlefields
            List<UUID> validTargets = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    validTargets.add(p.getId());
                }
            }

            if (validTargets.isEmpty()) {
                gameData.pendingEmblemTriggerTargets.removeFirst();
                String logEntry = pending.emblemDescription() + "'s trigger has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} emblem trigger skipped (no valid permanent targets)",
                        gameData.id, pending.emblemDescription());
                continue;
            }

            gameData.pendingEmblemTriggerTargets.removeFirst();
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.emblemDescription() + "'s ability - Choose target permanent to exile.");

            String logEntry = pending.emblemDescription() + "'s triggered ability - choose target permanent.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} emblem trigger awaiting target selection", gameData.id, pending.emblemDescription());
            return;
        }
    }
}

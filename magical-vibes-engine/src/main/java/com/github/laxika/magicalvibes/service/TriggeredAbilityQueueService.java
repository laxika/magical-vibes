package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.input.PlayerInputService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
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

    /**
     * The permanent half of "any target" (CR 115.4), taken from the declared
     * {@link TargetPredicates#anyTarget()} rather than re-implemented. Hoisted because the
     * enumerations below ask it once per permanent on every battlefield.
     */
    private static final PermanentPredicate ANY_TARGET_PERMANENTS =
            TargetPredicates.anyTarget().permanentRestriction().orElseThrow();

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TriggerTargetCollector triggerTargetCollector;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final com.github.laxika.magicalvibes.service.target.TargetLegalityService targetLegalityService;
    private final com.github.laxika.magicalvibes.service.target.ValidTargetService validTargetService;

    public void processNextDeathTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            PermanentChoiceContext.DeathTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);

            // Graveyard-targeting death trigger (e.g. Ruin Rat): choose a target card in a graveyard
            // to exile, at the time the trigger is put on the stack.
            GraveyardTargetingSupport.Target graveyardTarget = graveyardTargetingSupport.findTarget(pending.effects());
            if (graveyardTarget != null) {
                if (beginDeathGraveyardTarget(gameData, pending, graveyardTarget)) {
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

            boolean optionalTarget = hasOptionalSingleTarget(dyingCard, pending.effects());
            if (result.validTargets().isEmpty()) {
                if (optionalTarget) {
                    gameData.pollPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            pending.dyingCard(),
                            pending.controllerId(),
                            pending.dyingCard().getName() + "'s ability",
                            new ArrayList<>(pending.effects()),
                            null,
                            pending.sourcePermanentSnapshot() == null
                                    ? null : pending.sourcePermanentSnapshot().getId()
                    );
                    entry.setSourcePermanentSnapshot(pending.sourcePermanentSnapshot());
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.cardThen(pending.dyingCard(),
                            "'s death trigger triggers without a target."));
                    continue;
                }
                // No valid targets - trigger can't go on the stack, skip it
                gameData.pollPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
                gameLogService.append(gameData, GameLog.cardThen(pending.dyingCard(),
                        "'s death trigger has no valid targets."));
                log.info("Game {} - {} death trigger skipped (no valid creature targets)",
                        gameData.id, pending.dyingCard().getName());
                continue;
            }

            // Remove from queue and begin permanent choice
            gameData.pollPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            String targetDescription = optionalTarget ? "target creature or yourself to decline"
                    : (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : "target creature";
            if (optionalTarget) {
                playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(), result.validTargets(),
                        List.of(pending.controllerId()),
                        pending.dyingCard().getName() + "'s ability - Choose " + targetDescription + ".");
            } else {
                playerInputService.beginPermanentChoice(gameData, pending.controllerId(), result.validTargets(),
                        pending.dyingCard().getName() + "'s ability - Choose " + targetDescription + ".");
            }

            gameLogService.append(gameData, GameLog.cardThen(pending.dyingCard(),
                    "'s death trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} death trigger awaiting target selection", gameData.id, pending.dyingCard().getName());
            return;
        }
    }

    private boolean hasOptionalSingleTarget(Card card, List<CardEffect> effects) {
        if (card.getSpellTargets().size() != 1) {
            return false;
        }
        var target = card.getSpellTargets().getFirst();
        return target.getMinTargets() == 0
                && target.getMaxTargets() == 1
                && effects.stream().anyMatch(effect -> card.getEffectTargetIndex(effect) == target.getIndex());
    }

    /**
     * Collects the graveyard target for a "when this creature dies, exile target card from a(n
     * opponent's) graveyard" trigger (Ruin Rat) and begins the card choice, at the time the trigger
     * is put on the stack. Which graveyards are searched comes from the effect's declared
     * {@link GraveyardSearchScope}. Returns
     * {@code true} if input was begun (caller should return), or {@code false} if the trigger was
     * skipped for lack of a legal target (caller should continue) — a targeted death trigger with no
     * legal target is never put on the stack (CR 603.3c).
     */
    private boolean beginDeathGraveyardTarget(GameData gameData,
            PermanentChoiceContext.DeathTriggerTarget pending, GraveyardTargetingSupport.Target target) {
        CardPredicate filter = target.filter();

        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : target.scope().graveyardOwners(gameData.orderedPlayerIds, pending.controllerId())) {
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
            if (target.maxTargets() > 1) {
                // "Any number of target cards" is legally satisfied by zero targets, so the trigger
                // still goes on the stack and its non-targeting half still resolves (Iname, Life
                // Aspect's "you may exile it").
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.dyingCard(),
                        pending.controllerId(),
                        pending.dyingCard().getName() + "'s ability",
                        new ArrayList<>(pending.effects()),
                        List.of()
                ));
                gameLogService.append(gameData, GameLog.cardThen(pending.dyingCard(),
                        "'s death trigger triggers targeting no cards."));
                log.info("Game {} - {} death graveyard trigger pushed with 0 targets",
                        gameData.id, pending.dyingCard().getName());
                return false;
            }
            gameLogService.append(gameData, GameLog.cardThen(pending.dyingCard(),
                    "'s death trigger has no valid graveyard targets."));
            log.info("Game {} - {} death graveyard trigger skipped (no valid targets)",
                    gameData.id, pending.dyingCard().getName());
            return false;
        }

        gameData.graveyardTargetOperation.card = pending.dyingCard();
        gameData.graveyardTargetOperation.controllerId = pending.controllerId();
        gameData.graveyardTargetOperation.effects = new ArrayList<>(pending.effects());

        String zoneLabel = switch (target.scope()) {
            case ALL_GRAVEYARDS -> "a graveyard";
            case OPPONENT_GRAVEYARD -> "an opponent's graveyard";
            case CONTROLLERS_GRAVEYARD -> "your graveyard";
        };
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        int maxTargets = Math.min(target.maxTargets(), matchingCards.size());
        String countLabel = maxTargets > 1 ? "up to " + maxTargets + " target " : "target ";
        playerInputService.beginMultiGraveyardChoice(gameData, pending.controllerId(), matchingCards, maxTargets,
                pending.dyingCard().getName() + "'s ability — Choose " + countLabel + filterLabel
                        + (maxTargets > 1 ? "s" : "") + " from " + zoneLabel
                        + " " + target.destination() + ".");

        gameLogService.append(gameData, GameLog.cardThen(pending.dyingCard(),
                "'s death trigger — choose a graveyard target."));
        log.info("Game {} - {} death graveyard trigger awaiting target selection",
                gameData.id, pending.dyingCard().getName());
        return true;
    }

    public void processNextSelfTriggeredAbilityTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)) {
            PermanentChoiceContext.SelfTriggeredAbilityTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);

            // Graveyard-targeting self-leaves trigger (e.g. Offalsnout): choose a target card in a
            // graveyard to exile, at the time the trigger is put on the stack.
            ExileGraveyardCardsEffect gyExile = pending.effects().stream()
                    .filter(e -> e instanceof ExileGraveyardCardsEffect ege && ege.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                    .map(e -> (ExileGraveyardCardsEffect) e)
                    .findFirst().orElse(null);
            if (gyExile != null) {
                if (beginSelfLeavesGraveyardTarget(gameData, pending, gyExile)) {
                    return;
                }
                continue;
            }

            TargetFilter selfLeavesFilter = targetFilterForTriggeredEffects(pending.sourceCard(), pending.effects());
            Permanent sourcePermanentSnapshot = pending.sourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    selfLeavesFilter,
                    pending.controllerId(),
                    pending.sourceCard(),
                    TriggerTargetCollector.Options.END_STEP,
                    sourcePermanentSnapshot);

            if (result.validTargets().isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s " + pending.eventDescription() + " trigger has no valid targets."));
                log.info("Game {} - {} {} trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName(), pending.eventDescription());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            String targetDescription = (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : "target creature";
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), result.validTargets(),
                    pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s " + pending.eventDescription() + " trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} {} trigger awaiting target selection", gameData.id,
                    pending.sourceCard().getName(), pending.eventDescription());
            return;
        }
    }

    private TargetFilter targetFilterForTriggeredEffects(Card sourceCard, List<CardEffect> effects) {
        int targetGroupIndex = effects.stream()
                .mapToInt(sourceCard::getEffectTargetIndex)
                .filter(index -> index >= 0 && index < sourceCard.getSpellTargets().size())
                .findFirst()
                .orElse(-1);
        return targetGroupIndex >= 0
                ? sourceCard.getSpellTargets().get(targetGroupIndex).getFilter()
                : sourceCard.getTargetFilter();
    }

    /**
     * Collects the graveyard targets for a leaves-the-battlefield exile trigger (e.g. Offalsnout) and
     * begins the card choice. Returns {@code true} if input was begun (caller should return), or
     * {@code false} if the trigger was skipped for lack of a legal target (caller should continue).
     */
    private boolean beginSelfLeavesGraveyardTarget(GameData gameData,
            PermanentChoiceContext.SelfTriggeredAbilityTarget pending, ExileGraveyardCardsEffect gyExile) {
        CardPredicate filter = gyExile.filter();

        List<Card> matchingCards = new ArrayList<>();
        List<UUID> searchPlayerIds = gyExile.targetSpec().graveyardScope().orElseThrow()
                .graveyardOwners(gameData.orderedPlayerIds, pending.controllerId());
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

        gameData.pollPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
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

        gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                "'s leaves-the-battlefield trigger — choose a graveyard target."));
        log.info("Game {} - {} leaves-battlefield graveyard trigger awaiting target selection",
                gameData.id, pending.sourceCard().getName());
        return true;
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)) {
            PermanentChoiceContext.AttackTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
            UUID choosingPlayerId = pending.choosingPlayerId();

            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    targetFilterForTriggeredEffects(pending.sourceCard(), pending.effects()),
                    pending.controllerId(),
                    pending.sourceCard(),
                    TriggerTargetCollector.Options.ATTACK,
                    null,
                    defendingPlayerId(gameData, pending.attackedTargetId()));

            if (result.validTargets().isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
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
            playerInputService.beginPermanentChoice(gameData, choosingPlayerId, result.validTargets(),
                    pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s attack trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} attack trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    private UUID defendingPlayerId(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
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
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s attack trigger has no valid targets."));
                log.info("Game {} - {} attack trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.AttackCounterMoveFirstTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability - Choose target creature you control.");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
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
                    && targetLegalityService.checkTriggeredPermanentTargetableReason(
                            gameData, permanent, sourceCard, choosingPlayerId).isEmpty()) {
                result.add(permanent.getId());
            }
        }
        return result;
    }

    public void processNextEntersTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)) {
            PermanentChoiceContext.EntersTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);

            TargetFilter targetFilter = pending.targetFilter() != null
                    ? pending.targetFilter()
                    : targetFilterForTriggeredEffects(pending.sourceCard(), pending.effects());
            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    targetFilter,
                    pending.controllerId(),
                    pending.sourceCard(),
                    TriggerTargetCollector.Options.ATTACK,
                    pending.targetSourcePermanentId() == null
                            ? null
                            : gameQueryService.findPermanentById(gameData, pending.targetSourcePermanentId()));
            boolean optionalTarget = hasOptionalSingleTarget(pending.sourceCard(), pending.effects());

            if (result.validTargets().isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
                if (optionalTarget) {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            pending.sourceCard(),
                            pending.controllerId(),
                            pending.sourceCard().getName() + "'s ability",
                            new ArrayList<>(pending.effects()),
                            null,
                            pending.sourcePermanentId());
                    entry.setTriggeringPermanentId(pending.enteringPermanentId());
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s enter trigger triggers without a target."));
                    log.info("Game {} - {} enter trigger pushed without a target",
                            gameData.id, pending.sourceCard().getName());
                } else {
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s enter trigger has no valid targets."));
                    log.info("Game {} - {} enter trigger skipped (no valid targets)",
                            gameData.id, pending.sourceCard().getName());
                }
                continue;
            }

            String targetDescription = (result.canTargetPlayers() && result.canTargetPermanents()) ? "any target"
                    : result.canTargetPlayers()
                            ? (result.opponentOnly() ? "target opponent" : "target player")
                            : optionalTarget ? "target permanent or yourself to decline" : "target permanent";
            gameData.pollPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            if (optionalTarget) {
                playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(), result.validTargets(),
                        List.of(pending.controllerId()),
                        pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");
            } else {
                playerInputService.beginPermanentChoice(gameData, pending.controllerId(), result.validTargets(),
                        pending.sourceCard().getName() + "'s ability - Choose " + targetDescription + ".");
            }

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s enter trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} enter trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextTriggeredModalTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)) {
            PermanentChoiceContext.TriggeredModalTrigger pending =
                    gameData.pollPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class);
            ChooseOneEffect effect = pending.effect();
            if (pending.modesResetEachTurn()) {
                Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
                Set<String> alreadyChosen = source == null ? Set.of() : source.getChosenModeLabelsThisTurn();
                List<ChooseOneEffect.ChooseOneOption> remaining = effect.options().stream()
                        .filter(option -> !alreadyChosen.contains(option.label()))
                        .toList();
                if (remaining.isEmpty()) {
                    gameLogService.append(gameData,
                            GameLog.cardThen(pending.sourceCard(), "'s discard trigger has no modes left to choose."));
                    continue;
                }
                effect = new ChooseOneEffect(remaining);
            }
            playerInputService.beginTriggeredModalChoice(gameData, pending.controllerId(), pending.sourceCard(),
                    effect, pending.sourcePermanentId(), pending.modesResetEachTurn());
            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(), "'s ability - choose a mode."));
            log.info("Game {} - {} triggered ability awaiting mode selection", gameData.id,
                    pending.sourceCard().getName());
            return;
        }
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, ChooseOneEffect.ChooseOneOption chosen) {
        List<CardEffect> effects = new ArrayList<>(chosen.effects());
        boolean needsTarget = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        if (needsTarget) {
            gameData.queueInteractionFirst(new PermanentChoiceContext.EntersTriggerTarget(
                    sourceCard, controllerId, effects, sourcePermanentId, null, null, chosen.targetFilter()));
            return;
        }

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                effects,
                null,
                sourcePermanentId));
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
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
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

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s discard trigger - choose " + targetDescription + "."));
            log.info("Game {} - {} discard trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            PermanentChoiceContext.DiscardTriggerAnyTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class);

            List<UUID> validPermanentTargets =
                    anyTargetPermanents(gameData, pending.controllerId(), pending.discardedCard());
            List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

            // There are always valid targets (at least the players)
            gameData.pollPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.discardedCard().getName() + "'s ability - Choose any target.");

            gameLogService.append(gameData, GameLog.cardThen(pending.discardedCard(),
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
                if (filter == null) {
                    validPermanentTargets =
                            anyTargetPermanents(gameData, pending.controllerId(), pending.sourceCard());
                    if (pending.permanentTargetControllerId() != null) {
                        validPermanentTargets = validPermanentTargets.stream()
                                .filter(id -> pending.permanentTargetControllerId().equals(
                                        gameQueryService.findPermanentController(gameData, id)))
                                .toList();
                    }
                } else {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceControllerId(pending.controllerId())
                            .withSourceCardId(pending.sourceCard().getId());
                    if (pending.sourcePermanentId() != null) {
                        filterContext = filterContext.withSourcePermanentId(pending.sourcePermanentId());
                    }
                    if (pending.sourcePermanentSnapshot() != null) {
                        filterContext = filterContext.withSourcePermanentSnapshot(pending.sourcePermanentSnapshot());
                    }
                    for (UUID pid : gameData.orderedPlayerIds) {
                        if (pending.permanentTargetControllerId() != null
                                && !pending.permanentTargetControllerId().equals(pid)) {
                            continue;
                        }
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) continue;
                        for (Permanent p : battlefield) {
                            if (predicateEvaluationService.matchesFilters(p, Set.of(filter), filterContext)) {
                                validPermanentTargets.add(p.getId());
                            }
                        }
                    }
                }

                // If a target filter is present but no valid targets exist, skip this trigger
                if (filter != null && !(filter instanceof AnyTargetPredicateTargetFilter)
                        && validPermanentTargets.isEmpty()) {
                    gameData.pollPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
                    if (pending.optionalTarget()) {
                        pushSpellTargetTriggerWithoutTarget(gameData, pending);
                        gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                                "'s triggered ability triggers without a target."));
                    } else {
                        log.info("Game {} - {} spell-target trigger skipped (no valid targets)",
                                gameData.id, pending.sourceCard().getName());
                    }
                    continue;
                }
            }

            List<UUID> validPlayerTargets;
            if (pending.playerTargetOnly()) {
                // Player-only triggers (e.g. Abundant Maw "target opponent") honour PlayerPredicateTargetFilter.
                validPlayerTargets = validTargetService.filterValidPlayerTargets(
                        gameData, pending.targetFilter(), gameData.orderedPlayerIds, pending.controllerId());
                if (validPlayerTargets.isEmpty()) {
                    gameData.pollPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
                    log.info("Game {} - {} spell-target trigger skipped (no valid player targets)",
                            gameData.id, pending.sourceCard().getName());
                    continue;
                }
            } else if (pending.targetFilter() instanceof AnyTargetPredicateTargetFilter) {
                validPlayerTargets = validTargetService.filterValidPlayerTargets(
                        gameData, pending.targetFilter(), gameData.orderedPlayerIds, pending.controllerId());
            } else if (pending.targetFilter() != null) {
                // Permanent-filtered path: players are not offered.
                validPlayerTargets = pending.optionalTarget() ? List.of(pending.controllerId()) : List.of();
            } else {
                validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);
            }

            if (pending.targetFilter() instanceof AnyTargetPredicateTargetFilter
                    && validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
                log.info("Game {} - {} spell-target trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            String prompt = pending.playerTargetOnly()
                    ? pending.sourceCard().getName() + "'s ability - Choose target player."
                    : pending.sourceCard().getName() + "'s ability - Choose any target.";

            // There are always valid targets (at least the players, or filtered permanents)
            gameData.pollPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.choosingPlayerId(),
                    validPermanentTargets, validPlayerTargets, prompt);

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s triggered ability - choose a target."));
            log.info("Game {} - {} spell-target trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    private void pushSpellTargetTriggerWithoutTarget(GameData gameData,
                                                      PermanentChoiceContext.SpellTargetTriggerAnyTarget pending) {
        StackEntry entry;
        if (pending.sourcePermanentId() != null) {
            entry = pending.spellManaSpentX() > 0
                    ? new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            pending.sourceCard(),
                            pending.controllerId(),
                            pending.sourceCard().getName() + "'s ability",
                            new ArrayList<>(pending.effects()),
                            pending.spellManaSpentX(),
                            pending.sourcePermanentId())
                    : new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            pending.sourceCard(),
                            pending.controllerId(),
                            pending.sourceCard().getName() + "'s ability",
                            new ArrayList<>(pending.effects()),
                            (UUID) null,
                            pending.sourcePermanentId());
        } else {
            entry = pending.spellManaSpentX() > 0
                    ? new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            pending.sourceCard(),
                            pending.controllerId(),
                            pending.sourceCard().getName() + "'s ability",
                            new ArrayList<>(pending.effects()),
                            pending.spellManaSpentX())
                    : new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            pending.sourceCard(),
                            pending.controllerId(),
                            pending.sourceCard().getName() + "'s ability",
                            new ArrayList<>(pending.effects()));
        }
        entry.setSourcePermanentSnapshot(pending.sourcePermanentSnapshot());
        gameData.stack.add(entry);
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

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
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

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s draw trigger - choose a target."));
            log.info("Game {} - {} draw trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextEnteringPermanentAnyTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class)) {
            PermanentChoiceContext.EnteringPermanentAnyTargetTrigger pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class);

            List<UUID> validPermanentTargets =
                    anyTargetPermanents(gameData, pending.controllerId(), pending.sourceCard());
            List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

            // There are always valid targets (at least the players).
            gameData.pollPendingInteraction(PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.sourceCard().getName() + "'s ability - Choose any target.");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s triggered ability - choose any target."));
            log.info("Game {} - {} enters-from-graveyard trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    /**
     * Every permanent on every battlefield that CR 115.4 admits as the permanent half of "any
     * target". The restriction is <em>evaluated</em> from the declared
     * {@link TargetPredicates#anyTarget()} rather than re-implemented here, so these trigger
     * enumerations cannot drift from the spell path in {@code ValidTargetService} /
     * {@code TargetValidationService}, and they are layer-aware (CR 613.1d): a planeswalker a
     * type-replacing effect turned into a land is no longer an any target.
     *
     * <p>Players are added by the callers — every one of them offers all of them.</p>
     */
    private List<UUID> anyTargetPermanents(GameData gameData, UUID controllerId, Card sourceCard) {
        FilterContext filterContext =
                new FilterContext(gameData, sourceCard == null ? null : sourceCard.getId(), controllerId, null, null);
        List<UUID> validTargets = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(p, ANY_TARGET_PERMANENTS, filterContext)) {
                    validTargets.add(p.getId());
                }
            }
        }
        return validTargets;
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)) {
            PermanentChoiceContext.EmblemTriggerTarget pending = gameData.peekPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class);

            TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                    gameData,
                    pending.effects(),
                    null,
                    pending.controllerId(),
                    pending.sourceCard(),
                    TriggerTargetCollector.Options.UPKEEP);
            List<UUID> validTargets = result.validTargets();
            if (pending.opponentControlledOnly()) {
                validTargets = validTargets.stream()
                        .filter(id -> !pending.controllerId().equals(
                                gameQueryService.findPermanentController(gameData, id)))
                        .toList();
            }

            if (validTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class);
                String logEntry = pending.emblemDescription() + "'s trigger has no valid targets.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} emblem trigger skipped (no valid permanent targets)",
                        gameData.id, pending.emblemDescription());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            String targetDesc = pending.opponentControlledOnly()
                    ? "target permanent an opponent controls to exile"
                    : "target permanent";
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.emblemDescription() + "'s ability - Choose " + targetDesc + ".");

            String logEntry = pending.emblemDescription() + "'s triggered ability - choose " + targetDesc + ".";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} emblem trigger awaiting target selection", gameData.id, pending.emblemDescription());
            return;
        }
    }

    public void processNextSagaChapterTarget(GameData gameData) {
        PermanentChoiceContext.SagaChapterTarget first =
                gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
        if (first != null && !first.targetGroups().isEmpty()) {
            processNextSagaChapterMultiTarget(gameData);
            return;
        }

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
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
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

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s chapter " + pending.chapterName() + " - choose target creature."));
            log.info("Game {} - {} chapter {} awaiting target selection", gameData.id, pending.sourceCard().getName(), pending.chapterName());
            return;
        }
    }

    public void processNextSagaChapterPlayerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class)) {
            PermanentChoiceContext.SagaChapterPlayerTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class);
            List<UUID> validPlayerTargets = collectSagaChapterPlayerTargets(gameData, pending);
            gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class);

            if (validPlayerTargets.isEmpty()) {
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s chapter " + pending.chapterName() + " has no valid player targets."));
                continue;
            }

            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPlayerChoice(gameData, pending.controllerId(), validPlayerTargets,
                    pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                            + " - Choose target opponent.");
            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s chapter " + pending.chapterName() + " - choose target opponent."));
            log.info("Game {} - {} chapter {} awaiting player target selection",
                    gameData.id, pending.sourceCard().getName(), pending.chapterName());
            return;
        }
    }

    private List<UUID> collectSagaChapterPlayerTargets(
            GameData gameData, PermanentChoiceContext.SagaChapterPlayerTarget pending) {
        boolean opponentOnly = !pending.effects().isEmpty()
                && pending.effects().stream().allMatch(e -> e.targetPlayerRelation() == PlayerRelation.OPPONENT);
        boolean selfOnly = !pending.effects().isEmpty()
                && pending.effects().stream().allMatch(e -> e.targetPlayerRelation() == PlayerRelation.SELF);

        List<UUID> validTargets = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (opponentOnly && playerId.equals(pending.controllerId())) {
                continue;
            }
            if (selfOnly && !playerId.equals(pending.controllerId())) {
                continue;
            }
            validTargets.add(playerId);
        }
        return validTargets;
    }

    private void processNextSagaChapterMultiTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            PermanentChoiceContext.SagaChapterTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
            if (pending.targetGroups().isEmpty()) {
                return;
            }

            int groupIndex = pending.currentGroupIndex();
            if (groupIndex >= pending.targetGroups().size()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s chapter " + pending.chapterName() + " ability",
                        new ArrayList<>(pending.effects()),
                        pending.sourcePermanentId(),
                        new ArrayList<>(pending.chosenTargetsSoFar())
                ));
                continue;
            }

            SagaChapterTargetGroup group = pending.targetGroups().get(groupIndex);
            List<UUID> validTargets = collectSagaChapterTargetGroup(gameData, pending, group);
            if (validTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class);
                if (group.minTargets() > 0) {
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s chapter " + pending.chapterName() + " has no valid targets."));
                    continue;
                }
                gameData.queueInteractionFirst(new PermanentChoiceContext.SagaChapterTarget(
                        pending.sourceCard(), pending.controllerId(), pending.effects(),
                        pending.sourcePermanentId(), pending.chapterName(), pending.targetFilters(),
                        pending.targetGroups(), pending.chosenTargetsSoFar(), groupIndex + 1));
                continue;
            }

            gameData.interaction.setPermanentChoiceContext(pending);
            boolean canSkip = group.minTargets() == 0
                    || (pending.targetGroups().size() == 1
                    && pending.chosenTargetsSoFar().size() >= group.minTargets());
            List<UUID> skipChoice = canSkip
                    ? List.of(pending.controllerId()) : List.of();
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(), validTargets, skipChoice,
                    pending.sourceCard().getName() + "'s chapter " + pending.chapterName()
                            + " - Choose target " + (groupIndex + 1) + ".");
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

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
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

            // Find the graveyard-targeting effect to extract its filter and search scope
            CardPredicate filter = null;
            boolean lifeGainedCap = false;
            boolean manaValueEqualsX = false;
            boolean manaValueAtMostX = false;
            GraveyardSearchScope scope = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
            GraveyardTargetingSupport.Target describedTarget =
                    graveyardTargetingSupport.findTarget(pending.effects());
            ReturnCardFromGraveyardEffect returnEffect = pending.effects().stream()
                    .map(this::targetedReturnEffect)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (returnEffect != null) {
                filter = returnEffect.filter();
                lifeGainedCap = returnEffect.maxManaValueEqualsLifeGainedThisTurn();
                manaValueEqualsX = returnEffect.requiresManaValueEqualsX();
                manaValueAtMostX = returnEffect.requiresManaValueAtMostX();
                scope = returnEffect.source();
            } else if (describedTarget != null) {
                filter = describedTarget.filter();
                scope = describedTarget.scope();
            } else {
                for (CardEffect effect : pending.effects()) {
                    CardEffect targetEffect = unwrapConditionalEffect(effect);
                    if (targetEffect instanceof com.github.laxika.magicalvibes.model.effect.ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect
                            && pending.sourceCard().getEffectTargetIndex(targetEffect) >= 0
                            && pending.sourceCard().getTargetFilter() instanceof GraveyardCardPredicateTargetFilter graveyardFilter) {
                        filter = graveyardFilter.predicate();
                        scope = graveyardFilter.scope();
                        break;
                    }
                    if (targetEffect.targetSpec().graveyardScope().orElse(null) == GraveyardSearchScope.ALL_GRAVEYARDS) {
                        // BecomeAuraReanimateFromGraveyardEffect (Necromancy): creature card from any graveyard
                        filter = new CardTypePredicate(CardType.CREATURE);
                        scope = GraveyardSearchScope.ALL_GRAVEYARDS;
                        break;
                    }
                    if (targetEffect instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect steal) {
                        // Ink-Eyes, Servant of Oni: creature card from an opponent's graveyard, narrowed
                        // to the damaged player by the pending trigger's graveyardOwnerId.
                        filter = steal.filter();
                        scope = GraveyardSearchScope.OPPONENT_GRAVEYARD;
                        break;
                    }
                    if (targetEffect.targetSpec().declaredTarget() instanceof TargetPredicate.GraveyardCards graveyardCards) {
                        filter = graveyardCards.inner();
                        scope = graveyardCards.scope();
                        break;
                    }
                    GraveyardSearchScope declaredScope = targetEffect.targetSpec().graveyardScope().orElse(null);
                    if (declaredScope != null) {
                        scope = declaredScope;
                        break;
                    }
                }
            }
            // "mana value X or less, where X is the life you gained this turn" (e.g. Moseo)
            int maxManaValue = lifeGainedCap
                    ? gameData.getLifeGainedThisTurn(pending.controllerId())
                    : manaValueAtMostX ? pending.xValue() : Integer.MAX_VALUE;

            List<UUID> searchPlayerIds = pending.graveyardOwnerId() != null
                    ? List.of(pending.graveyardOwnerId())
                    : scope.graveyardOwners(gameData.orderedPlayerIds, pending.controllerId());

            List<Card> matchingCards = new ArrayList<>();
            for (UUID playerId : searchPlayerIds) {
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                if (graveyard == null) {
                    continue;
                }
                for (Card graveyardCard : graveyard) {
                    if (manaValueEqualsX && graveyardCard.getManaValue() != pending.xValue()) {
                        continue;
                    }
                    if (graveyardCard.getManaValue() > maxManaValue) {
                        continue;
                    }
                    if (filter == null || predicateEvaluationService.matchesCardPredicate(
                            graveyardCard, filter, pending.sourceCard().getId(), gameData, playerId)) {
                        matchingCards.add(graveyardCard);
                    }
                }
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);

            int describedMinTargets = describedTarget == null ? 0 : describedTarget.minTargets();
            int minTargets = Math.max(pending.minCount(), describedMinTargets);
            if (matchingCards.isEmpty() || matchingCards.size() < minTargets) {
                log.info("Game {} - {} spell-cast graveyard-target trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            // Set up graveyard target operation (entryType = null → triggered ability path)
            gameData.graveyardTargetOperation.card = pending.sourceCard();
            gameData.graveyardTargetOperation.controllerId = pending.controllerId();
            gameData.graveyardTargetOperation.effects = new ArrayList<>(pending.effects());
            gameData.graveyardTargetOperation.xValue = pending.xValue();
            // ETB source permanent (for intervening-if / attach); find by card id on the controller's BF
            List<Permanent> bf = gameData.playerBattlefields.get(pending.controllerId());
            if (bf != null) {
                for (Permanent p : bf) {
                    if (p.getCard().getId().equals(pending.sourceCard().getId())) {
                        gameData.graveyardTargetOperation.sourcePermanentId = p.getId();
                        break;
                    }
                }
            }

            String filterLabel = CardPredicateUtils.describeFilter(filter);
            String zoneLabel = pending.graveyardOwnerId() != null ? "that player's graveyard" : switch (scope) {
                case ALL_GRAVEYARDS -> "a graveyard";
                case OPPONENT_GRAVEYARD -> "an opponent's graveyard";
                case CONTROLLERS_GRAVEYARD -> "your graveyard";
            };
            int maxTargets = describedTarget == null
                    ? Math.min(1, matchingCards.size())
                    : Math.min(describedTarget.maxTargets(), matchingCards.size());
            String countLabel = maxTargets == 1 ? "target " : minTargets == maxTargets
                    ? maxTargets + " target " : "up to " + maxTargets + " target ";
            playerInputService.beginMultiGraveyardChoice(gameData, pending.controllerId(), matchingCards, maxTargets,
                    minTargets,
                    pending.sourceCard().getName() + "'s ability — Choose " + countLabel + filterLabel
                            + (maxTargets == 1 ? "" : "s") + " from " + zoneLabel + ".");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s triggered ability triggers — choose a graveyard target."));
            log.info("Game {} - {} spell-cast graveyard-target trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    private CardEffect unwrapConditionalEffect(CardEffect effect) {
        while (effect instanceof ConditionalEffect conditional) {
            effect = conditional.wrapped();
        }
        return effect;
    }

    private ReturnCardFromGraveyardEffect targetedReturnEffect(CardEffect effect) {
        if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
            return returnEffect;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return targetedReturnEffect(conditional.wrapped());
        }
        if (effect instanceof MayEffect may) {
            return targetedReturnEffect(may.wrapped());
        }
        if (effect instanceof SacrificePermanentThenEffect sacrificeThen) {
            return targetedReturnEffect(sacrificeThen.thenEffect());
        }
        if (effect instanceof SacrificeSelfThenEffect sacrificeSelfThen) {
            return targetedReturnEffect(sacrificeSelfThen.thenEffect());
        }
        return null;
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
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s explore trigger has no valid targets."));
                log.info("Game {} - {} explore trigger skipped (no valid creature targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ExploreTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability — Choose target creature an opponent controls.");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s explore trigger — choose target creature."));
            log.info("Game {} - {} explore trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextExploitTriggerTarget(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ExploitTriggerTarget.class)) {
            PermanentChoiceContext.ExploitTriggerTarget pending =
                    gameData.peekPendingInteraction(PermanentChoiceContext.ExploitTriggerTarget.class);

            List<UUID> validStackCardIds = new ArrayList<>();
            for (StackEntry se : gameData.stack) {
                StackEntryType type = se.getEntryType();
                boolean isAbility = type == StackEntryType.ACTIVATED_ABILITY
                        || type == StackEntryType.TRIGGERED_ABILITY;
                boolean isSpell = type == StackEntryType.INSTANT_SPELL || type == StackEntryType.SORCERY_SPELL
                        || type == StackEntryType.CREATURE_SPELL || type == StackEntryType.ENCHANTMENT_SPELL
                        || type == StackEntryType.ARTIFACT_SPELL || type == StackEntryType.PLANESWALKER_SPELL;
                if (!isSpell && !(pending.includeAbilities() && isAbility)) {
                    continue;
                }
                if (pending.stackFilter() != null
                        && !targetLegalityService.matchesStackEntryPredicate(
                                gameData, se, pending.stackFilter(), pending.controllerId())) {
                    continue;
                }
                validStackCardIds.add(se.getCard().getId());
            }

            if (validStackCardIds.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ExploitTriggerTarget.class);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s exploit ability has no valid spell or ability targets."));
                log.info("Game {} - {} exploit trigger skipped (no valid stack targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ExploitTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validStackCardIds, List.of(),
                    pending.sourceCard().getName() + "'s ability — Choose target spell or ability.");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s exploit ability triggers — choose a target spell or ability."));
            log.info("Game {} - {} exploit trigger awaiting stack target selection",
                    gameData.id, pending.sourceCard().getName());
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
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s clash trigger has no valid targets."));
                log.info("Game {} - {} clash trigger skipped (no valid creature targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ClashTriggerTarget.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginPermanentChoice(gameData, pending.controllerId(), validTargets,
                    pending.sourceCard().getName() + "'s ability — Choose target creature an opponent controls.");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
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
                .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
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

    private List<UUID> collectSagaChapterTargetGroup(GameData gameData,
                                                      PermanentChoiceContext.SagaChapterTarget pending,
                                                      SagaChapterTargetGroup group) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(pending.sourceCard().getId())
                .withSourceControllerId(pending.controllerId());
        List<UUID> validTargets = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (pending.chosenTargetsSoFar().contains(permanent.getId())) {
                    continue;
                }
                if (group.filter() == null && !gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (group.filter() != null
                        && !predicateEvaluationService.matchesFilters(permanent, Set.of(group.filter()), filterContext)) {
                    continue;
                }
                validTargets.add(permanent.getId());
            }
        }
        return validTargets;
    }
}

package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LifeGainOpponentLifeLossWatcher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.rules.LifeEvent;
import com.github.laxika.magicalvibes.model.trigger.LifeTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triggers the life abilities of a {@link LifeEvent} (CR 603.2): "whenever you gain life", "whenever an opponent loses
 * life", "whenever you pay life" and their kin, each handed whole to the stack door with the record of the event. The
 * abilities are read from {@link TriggerIndex} as the layers leave them and matched by their {@link LifeTrigger}; an
 * ability's event condition, "for the first time each turn" and the spell that caused a gain are judged here, its
 * intervening "if" (CR 603.4) and limit (CR 603.2h) by the door.
 *
 * <ul>
 *   <li>A loss: the other players' "whenever an opponent loses life", then the losing player's "whenever you lose
 *       life".</li>
 *   <li>A payment: the paying player's "whenever you pay life".</li>
 *   <li>A gain: the gaining player's "whenever you gain life", the other players' "whenever an opponent gains life",
 *       the graveyard abilities of both, the delayed "whenever you gain life this turn" abilities, and every
 *       watcher's "whenever a player gains life".</li>
 * </ul>
 */
@Slf4j
@Service
public class LifeTriggers {

    private static final Trigger OPPONENT_LOSES_LIFE = Triggers.opponentLosesLife();
    private static final Trigger YOU_LOSE_LIFE = Triggers.youLoseLife();
    private static final Trigger YOU_PAY_LIFE = Triggers.youPayLife();
    private static final Trigger YOU_GAIN_LIFE = Triggers.youGainLife();
    private static final Trigger OPPONENT_GAINS_LIFE = Triggers.opponentGainsLife();
    private static final Trigger PLAYER_GAINS_LIFE = Triggers.playerGainsLife();

    private final EventAbilitySupport abilities;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;
    private final TriggeredAbilityStackService stackService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;

    public LifeTriggers(EventAbilitySupport abilities, @Lazy PredicateEvaluationService predicateEvaluationService,
                        @Lazy GameOutcomeService gameOutcomeService, @Lazy GameLogService gameLogService,
                        @Lazy TriggeredAbilityStackService stackService,
                        @Lazy TriggeredAbilityQueueService triggeredAbilityQueueService) {
        this.abilities = abilities;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameOutcomeService = gameOutcomeService;
        this.gameLogService = gameLogService;
        this.stackService = stackService;
        this.triggeredAbilityQueueService = triggeredAbilityQueueService;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, LifeEvent event) {
        if (event.amount() <= 0) {
            return;
        }
        switch (event) {
            case LifeEvent.Lost lost -> lifeLost(gameData, lost.playerId(), lost.amount());
            case LifeEvent.Paid paid -> lifePaid(gameData, paid.playerId(), paid.amount());
            case LifeEvent.Gained gained -> lifeGained(gameData, gained);
        }
    }

    private void lifeLost(GameData gameData, UUID losingPlayerId, int lifeLostAmount) {
        boolean[] anyTriggered = {false};
        // The battlefields are read afresh: an ability may change them (Mindcrank mills, Undead Alchemist makes tokens).
        for (Watcher watcher : abilities.watchers(gameData)) {
            if (watcher.controllerId().equals(losingPlayerId)) continue;
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher, OPPONENT_LOSES_LIFE)) {
                if (putOpponentLifeLossAbility(gameData, watcher, ability, losingPlayerId, lifeLostAmount)) {
                    anyTriggered[0] = true;
                }
            }
        }
        for (Watcher watcher : abilities.watchers(gameData)) {
            if (!watcher.controllerId().equals(losingPlayerId)) continue;
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher, YOU_LOSE_LIFE)) {
                LifeTrigger trigger = trigger(ability, YOU_LOSE_LIFE);
                // "For the first time each turn": the player's first loss of life this turn, counting those before
                // the source was on the battlefield.
                if (trigger.firstEachTurn()
                        && gameData.lifeLostThisTurn.getOrDefault(losingPlayerId, 0) != lifeLostAmount) {
                    continue;
                }
                if (putLifeAbility(gameData, watcher, ability, losingPlayerId, lifeLostAmount)) {
                    anyTriggered[0] = true;
                }
            }
        }
        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    /**
     * Hands one "whenever an opponent loses life" ability of {@code watcher} to the stack door. "For the first time during
     * each of their turns" (Valgavoth) is judged here: the losing player is the active player, it is their first loss
     * of life this turn, counting those before the source was on the battlefield, and this ability hasn't triggered
     * for them this turn yet (the mark is the ability's, by its {@link TriggeredAbility#limitKey()} and the player, so
     * a second such ability of the permanent triggers too).
     */
    private boolean putOpponentLifeLossAbility(GameData gameData, Watcher watcher, TriggeredAbility ability,
                                               UUID losingPlayerId, int lifeLostAmount) {
        LifeTrigger trigger = trigger(ability, OPPONENT_LOSES_LIFE);
        String firstLossKey = null;
        if (trigger.event() == LifeTrigger.LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN) {
            if (!losingPlayerId.equals(gameData.activePlayerId)
                    || gameData.lifeLostThisTurn.getOrDefault(losingPlayerId, 0) - lifeLostAmount > 0) {
                return false;
            }
            firstLossKey = ability.limitKey() + ":" + losingPlayerId;
            Set<String> fired = gameData.keyedOncePerTurnTriggersFiredThisTurn.get(watcher.id());
            if (fired != null && fired.contains(firstLossKey)) {
                return false;
            }
        }
        boolean triggered = putLifeAbility(gameData, watcher, ability, losingPlayerId, lifeLostAmount);
        if (triggered && firstLossKey != null) {
            gameData.keyedOncePerTurnTriggersFiredThisTurn.computeIfAbsent(watcher.id(),
                    ignored -> ConcurrentHashMap.newKeySet()).add(firstLossKey);
        }
        return triggered;
    }

    private void lifePaid(GameData gameData, UUID payingPlayerId, int lifePaidAmount) {
        for (Watcher watcher : abilities.watchers(gameData)) {
            if (!watcher.controllerId().equals(payingPlayerId)) continue;
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher, YOU_PAY_LIFE)) {
                putLifeAbility(gameData, watcher, ability, payingPlayerId, lifePaidAmount);
            }
        }
    }

    private void lifeGained(GameData gameData, LifeEvent.Gained event) {
        UUID gainingPlayerId = event.playerId();
        int lifeGainedAmount = event.amount();
        for (Watcher watcher : abilities.watchers(gameData)) {
            if (!watcher.controllerId().equals(gainingPlayerId)) continue;
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher, YOU_GAIN_LIFE)) {
                LifeTrigger trigger = trigger(ability, YOU_GAIN_LIFE);
                if (trigger.firstEachTurn()
                        && gameData.getLifeGainedThisTurn(gainingPlayerId) != lifeGainedAmount) {
                    continue;
                }
                // "Whenever a [filter] spell causes you to gain life": the spell itself, not an ability of its card.
                if (trigger.spellFilter() != null
                        && !lifeGainCausedBySpell(gameData, watcher, event, trigger.spellFilter())) {
                    continue;
                }
                putLifeAbility(gameData, watcher, ability, gainingPlayerId, lifeGainedAmount);
            }
        }
        for (Watcher watcher : abilities.watchers(gameData)) {
            if (watcher.controllerId().equals(gainingPlayerId)) continue;
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher, OPPONENT_GAINS_LIFE)) {
                putLifeAbility(gameData, watcher, ability, gainingPlayerId, lifeGainedAmount);
            }
        }
        graveyardControllerLifeGain(gameData, gainingPlayerId, lifeGainedAmount);
        graveyardOpponentLifeGain(gameData, gainingPlayerId, lifeGainedAmount);
        lifeGainOpponentLifeLoss(gameData, gainingPlayerId, lifeGainedAmount);
        playerGainsLife(gameData, gainingPlayerId, lifeGainedAmount);
        triggeredAbilityQueueService.beginStackDoorChoices(gameData);
    }

    /**
     * Whether the life gain {@code event} was caused by a spell matching {@code spellFilter}, judged from the
     * ability's source {@code perm}: the source of the life gain is a spell's stack entry (the spell resolving, or its
     * lifelink damage), not a triggered or activated ability of its card.
     */
    private boolean lifeGainCausedBySpell(GameData gameData, Watcher watcher, LifeEvent.Gained event,
                                          CardPredicate spellFilter) {
        return event.sourceCard() != null && event.sourceEntryType() != null
                && event.sourceEntryType() != StackEntryType.TRIGGERED_ABILITY
                && event.sourceEntryType() != StackEntryType.ACTIVATED_ABILITY
                && predicateEvaluationService.matchesCardPredicate(event.sourceCard(), spellFilter,
                watcher.card().getId(), gameData, event.sourceCard().getOwnerId(),
                watcher.permanent() == null ? null : watcher.permanent().getId(), null);
    }

    /**
     * Hands {@code ability}, a life gain, loss or payment ability of {@code perm} controlled by {@code controllerId}, to
     * the stack door for {@code amount} life of {@code playerId}: its event condition ("during your turn", a Class's
     * level, "exactly 1 life") is judged with the player as "that player" and the amount as the event's value. The
     * record names, whatever the effects are, the player as the triggering player and the non-targeting "that player"
     * (which the {@code TRIGGERING_PLAYER} recipients read), the amount as the event's value ("that much", "that
     * many") and the source as it was when the ability triggered; the door marks "do this only once each turn" as the
     * action is taken (CR 603.2h) and chooses a target the card declares as it goes on the stack (CR 603.3d).
     */
    private boolean putLifeAbility(GameData gameData, Watcher watcher, TriggeredAbility ability, UUID playerId,
                                   int amount) {
        ConditionContext context = watcher.conditionContext().withTargetId(playerId).withEventValue(amount);
        Permanent snapshot = watcher.permanent() == null ? null : new Permanent(watcher.permanent());
        return abilities.put(gameData, ability, watcher, context, entry -> {
            entry.setNonTargeting(true);
            entry.setTargetId(playerId);
            entry.setEventValue(amount);
            entry.setTriggeringPlayerId(playerId);
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
        });
    }

    /** The "whenever you gain life" abilities of the cards in the gaining player's graveyard. */
    private void graveyardControllerLifeGain(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        List<Card> graveyard = gameData.playerGraveyards.get(gainingPlayerId);
        if (graveyard == null) return;
        for (Card card : new ArrayList<>(graveyard)) {
            for (TriggeredAbility ability : abilities.graveyardAbilities(gameData, card, YOU_GAIN_LIFE)) {
                if (trigger(ability, YOU_GAIN_LIFE).firstEachTurn()
                        && gameData.getLifeGainedThisTurn(gainingPlayerId) != lifeGainedAmount) {
                    continue;
                }
                putGraveyardLifeAbility(gameData, card, gainingPlayerId, ability, gainingPlayerId, lifeGainedAmount);
            }
        }
    }

    /** The "whenever an opponent gains life" abilities of the cards in the other players' graveyards. */
    private void graveyardOpponentLifeGain(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(gainingPlayerId)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : new ArrayList<>(graveyard)) {
                for (TriggeredAbility ability : abilities.graveyardAbilities(gameData, card, OPPONENT_GAINS_LIFE)) {
                        putGraveyardLifeAbility(gameData, card, playerId, ability, gainingPlayerId, lifeGainedAmount);
                }
            }
        }
    }

    /**
     * Hands {@code ability}, a life gain ability of {@code card} in its owner {@code controllerId}'s graveyard, to the
     * stack door for {@code amount} life {@code playerId} gained: the record names the player and the amount as
     * {@link #putLifeAbility} does, and this graveyard incarnation of the card (CR 400.7).
     */
    private void putGraveyardLifeAbility(GameData gameData, Card card, UUID controllerId, TriggeredAbility ability,
                                         UUID playerId, int amount) {
        ConditionContext context = ConditionContext.forCard(card, controllerId).withTargetId(playerId)
                .withEventValue(amount);
        long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
        abilities.put(gameData, ability, card, null, controllerId, context, entry -> {
            entry.setNonTargeting(true);
            entry.setTargetId(playerId);
            entry.setEventValue(amount);
            entry.setTriggeringPlayerId(playerId);
            entry.setSourceGraveyardEntryVersion(graveyardEntryVersion);
        });
    }

    /**
     * The turn-scoped "whenever you gain life this turn, each opponent loses that much life" delayed abilities
     * (Vizkopa Guildmage): one for each watcher whose controller gained the life, the amount its event value.
     */
    private void lifeGainOpponentLifeLoss(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        for (LifeGainOpponentLifeLossWatcher watcher : List.copyOf(gameData.lifeGainOpponentLifeLossWatchers)) {
            if (!watcher.controllerId().equals(gainingPlayerId)) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT))),
                    (UUID) null,
                    (UUID) null);
            entry.setEventValue(lifeGainedAmount);
            stackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers (controller gained {} life), each opponent loses that much life",
                    gameData.id, watcher.sourceCard().getName(), lifeGainedAmount);
        }
    }

    /**
     * Every watcher's "whenever a player gains life" (False Cure's delayed one, CR 603.7b): the player who gained life
     * is "that player", the amount its event value. They are read with no count, limit or spell.
     */
    private void playerGainsLife(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        for (Watcher watcher : abilities.watchers(gameData)) {
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher, PLAYER_GAINS_LIFE)) {
                requireNothingToJudge(ability, PLAYER_GAINS_LIFE);
                putLifeAbility(gameData, watcher, ability, gainingPlayerId, lifeGainedAmount);
            }
        }
    }

    /**
     * {@code ability}'s life condition watched as {@code watched}, its trigger-level parts ("for the first time each
     * turn", the spell that caused a gain) for the matcher to judge. This throws on what no life matcher reads: a limit
     * other than "once each turn" or "do this only once each turn", an ability's own target filter, a spell filter or
     * a count on another player's event, options beside "for the first time during each of their turns".
     */
    static LifeTrigger trigger(TriggeredAbility ability, Trigger watched) {
        LifeTrigger condition = EventAbilitySupport.condition(ability, LifeTrigger.class, watched);
        if (condition == null) {
            throw new IllegalArgumentException(ability + " has no life condition watched as " + watched);
        }
        TriggerOptions options = ability.options();
        TriggerLimit limit = options.limit();
        if (limit != TriggerLimit.NONE && limit != TriggerLimit.ONCE_EACH_TURN
                && limit != TriggerLimit.DO_ONLY_ONCE_EACH_TURN) {
            throw new IllegalStateException("No life matcher reads " + limit + ": " + ability);
        }
        if (condition.spellFilter() != null && condition.player() != PlayerRelation.YOU) {
            throw new IllegalStateException("No life matcher judges the spell of " + condition + ": " + ability);
        }
        if (condition.firstEachTurn()) {
            if (condition.player() != PlayerRelation.YOU || condition.event() == LifeTrigger.LifeEvent.PAYS) {
                throw new IllegalStateException("No life matcher counts " + condition + ": " + ability);
            }
            if (limit != TriggerLimit.NONE) {
                throw new IllegalStateException("No life matcher reads a limit next to \"for the first time each "
                        + "turn\": " + ability);
            }
        }
        if (condition.event() == LifeTrigger.LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN
                && !options.equals(TriggerOptions.DEFAULT)) {
            throw new IllegalStateException("The \"an opponent loses life\" matcher reads no options on \"for the "
                    + "first time during each of their turns\": " + ability);
        }
        return condition;
    }

    /** Throws when {@code ability} has a narrower event, a count, a spell filter or a limit. */
    private static void requireNothingToJudge(TriggeredAbility ability, Trigger watched) {
        LifeTrigger condition = trigger(ability, watched);
        if (condition.event() != ((LifeTrigger) watched).event() || condition.firstEachTurn()
                || condition.spellFilter() != null || ability.options().limit() != TriggerLimit.NONE) {
            throw new IllegalStateException("The matcher of " + watched + " judges nothing of " + ability);
        }
    }
}

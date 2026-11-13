package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.KayaSpiritsJusticeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Trigger collectors for sacrifice, graveyard, enchanted-permanent-tap and other watches' events, and the shared
 * record of a player, counter or control event ({@link #queueEventAbility}).
 */
@Slf4j
@Service
public class MiscTriggerCollectorService {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final DrawService drawService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final ETBTokenTargetService etbTokenTargetService;
    private PermanentRemovalService permanentRemovalService;
    private final DeathTriggerCollectorService deathTriggerCollectorService;

    public MiscTriggerCollectorService(GameLogService gameLogService,
                                       @Lazy GraveyardService graveyardService,
                                       GameQueryService gameQueryService,
                                       @Lazy DrawService drawService,
                                       ConditionEvaluationService conditionEvaluationService,
                                       @Lazy ETBTokenTargetService etbTokenTargetService,
                                       @Lazy PermanentRemovalService permanentRemovalService,
                                       @Lazy DeathTriggerCollectorService deathTriggerCollectorService) {
        this.gameLogService = gameLogService;
        this.graveyardService = graveyardService;
        this.gameQueryService = gameQueryService;
        this.drawService = drawService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.permanentRemovalService = permanentRemovalService;
        this.deathTriggerCollectorService = deathTriggerCollectorService;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_GIVES_GIFT)
    private boolean handleControllerGivesGift(TriggerMatchContext match, TriggerContext ctx) {
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(),
                GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever a player sacrifices a creature": the sacrifice recorded whatever the effects are
     * ({@link DeathTriggerCollectorService#queueDepartureAbility}), the sacrificing player its non-targeting "that
     * player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CREATURE_SACRIFICED)
    private boolean handleAnyCreatureSacrificedDefault(TriggerMatchContext match, TriggerContext ctx) {
        queueSacrificeAbility(match, (TriggerContext.AllySacrificed) ctx);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever you sacrifice a token", from exile (Albiorix, Goose Tyrant, CR 113.6k): the sacrifice recorded whatever
     * the effects are ({@link DeathTriggerCollectorService#queueDepartureAbility}); the source is the exiled card.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.EXILE_ON_CONTROLLER_TOKEN_SACRIFICED)
    private boolean handleExileTokenSacrificedDefault(TriggerMatchContext match, TriggerContext ctx) {
        if (match.sourceCard() == null) {
            return false;
        }
        queueSacrificeAbility(match, (TriggerContext.AllySacrificed) ctx);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.sourceCard()));
        return true;
    }

    /**
     * The sacrifice {@code sacrificed} as {@link DeathTriggerCollectorService#queueDepartureAbility} records a
     * permanent's leaving the battlefield: "that card", the permanent as it last existed, the sacrificing player as its
     * controller and as the entry's non-targeting "that player".
     */
    private void queueSacrificeAbility(TriggerMatchContext match, TriggerContext.AllySacrificed sacrificed) {
        deathTriggerCollectorService.queueDepartureAbility(match, sacrificed.sacrificedCard(),
                sacrificed.sacrificingPlayerId(), sacrificed.sacrificedPermanent(), sacrificed.sacrificingPlayerId());
    }

    /**
     * Queues a watcher for a creature controlled by the player exploiting another creature. The collector has judged
     * the exploited creature against the ability's filter ("a non-Human creature").
     */
    @CollectsActionTrigger(value = CardEffect.class,
            watch = ActionWatch.ON_ALLY_CREATURE_EXPLOITS)
    @CollectsActionTrigger(value = CardEffect.class,
            watch = ActionWatch.ON_ALLY_CREATURE_EXPLOITS_NONTOKEN_CREATURE)
    private boolean handleAllyCreatureExploit(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CreatureExploit exploit = (TriggerContext.CreatureExploit) ctx;
        StackEntry triggeredEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        triggeredEntry.setEventValue(exploit.exploitedPower());
        match.gameData().handOffTriggerRecord(triggeredEntry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(), match.controllerId(),
                    match.stackEffects(), match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        match.gameData().enqueueTrigger(triggeredEntry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers when {} exploits {}",
                match.gameData().id, match.permanent().getCard().getName(),
                exploit.exploitingCard().getName(), exploit.exploitedCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_PERMANENT_SACRIFICED)
    private boolean handleAnyPermanentSacrificedDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.PermanentSacrificed sacrificed = (TriggerContext.PermanentSacrificed) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(), match.controllerId(),
                    match.stackEffects(), match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        match.gameData().enqueueTrigger(entry);
        return true;
    }

    /**
     * "Whenever you sacrifice a [filter] permanent": the trigger pipeline has matched the permanent. The sacrificed
     * permanent, as it last existed on the battlefield (CR 603.10a), is the stack entry's triggering permanent and its
     * card the triggering card; a "you may" goes on the stack and is chosen as it resolves (CR 603.5), and "do this
     * only once each turn" is used up by an accepted graveyard return.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED)
    private boolean handleSacrificeDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AllySacrificed as = (TriggerContext.AllySacrificed) ctx;
        String cardName = match.permanent().getCard().getName();
        Permanent sacrificed = as.sacrificedPermanent() != null ? as.sacrificedPermanent()
                : as.sacrificedCard() != null ? new Permanent(as.sacrificedCard()) : null;
        StackEntry triggerEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                as.sacrificingPlayerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        triggerEntry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        triggerEntry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        triggerEntry.setTriggeringPermanent(sacrificed);
        if (as.sacrificedCard() != null) {
            // CR 400.7: the trigger refers to this stay in the graveyard; if the card leaves and returns before
            // the trigger resolves, it is a new object the trigger no longer finds.
            triggerEntry.setTriggeringCard(as.sacrificedCard().getId(),
                    match.gameData().graveyardEntryVersion(as.sacrificedCard().getId()));
        }
        match.gameData().handOffTriggerRecord(triggerEntry);
        if (match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            // The card is targeted as the trigger is put on the stack (CR 603.3d), so the may choice
            // comes at resolution. "Do this only once each turn" is used up only by an accepted return.
            // A target restricted by the sacrificed permanent ("target creature card with lesser mana value",
            // Riveteers Ascendancy) reads it from the record: the entry's triggering permanent, as it last existed
            // (CR 603.10a).
            String markOnAcceptance = match.onceEachTurnActionKey();
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.permanent().getCard(), match.controllerId(), match.stackEffects(),
                    null, 0, 0, 0, null, false, null, match.permanent().getId(), markOnAcceptance));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.stackEffects(),
                    match.permanent().getId()).withTriggeringPermanent(sacrificed));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} triggers on matching permanent sacrifice (awaiting target)",
                    match.gameData().id, cardName);
            return true;
        }
        match.gameData().enqueueTrigger(triggerEntry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on matching permanent sacrifice", match.gameData().id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_SACRIFICED)
    private boolean handleOpponentPermanentSacrificed(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.OpponentPermanentSacrificed sacrificed =
                (TriggerContext.OpponentPermanentSacrificed) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                sacrificed.sacrificingPlayerId(),
                match.permanent().getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever an opponent sacrifices a nontoken permanent" (Tergrid, It That Betrays, the Nazgûl Battle-Mace's grant):
     * the sacrifice recorded whatever the effects are ({@link #queueSacrificeAbility}); "that card" may have been exiled
     * instead (Leyline of the Void), its exile incarnation recorded too.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED)
    private boolean handleOpponentNontokenPermanentSacrificed(TriggerMatchContext match, TriggerContext ctx) {
        queueSacrificeAbility(match, (TriggerContext.AllySacrificed) ctx);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * What a player, counter or control event an ability triggered on records on its stack entry (or target choice),
     * whatever the ability's effects are: the player the event was about ({@code triggeringPlayerId}), the other player
     * an ability that chooses no target names as its non-targeting "that player" ({@code thatPlayerId}, {@code null} for
     * an event of the ability's controller), the event's count ("that much", "that many": {@code eventValue}, or
     * {@code null} for an event with none), the permanent it was about with its controller as it happened, and the card
     * it was about.
     */
    record EventRecord(UUID triggeringPlayerId, UUID thatPlayerId, Integer eventValue, Permanent triggeringPermanent,
                       UUID triggeringPermanentControllerId, UUID triggeringCardId) {

        /** The event of {@code playerId}, the ability's controller, with no count. */
        static EventRecord of(UUID playerId) {
            return new EventRecord(playerId, null, null, null, null, null);
        }

        /** This record with {@code value} as the event's count. */
        EventRecord withEventValue(int value) {
            return new EventRecord(triggeringPlayerId, thatPlayerId, value, triggeringPermanent,
                    triggeringPermanentControllerId, triggeringCardId);
        }

        /** This record naming {@code permanent}, controlled by {@code controllerId} as the event happened. */
        EventRecord withPermanent(Permanent permanent, UUID controllerId) {
            return new EventRecord(triggeringPlayerId, thatPlayerId, eventValue, permanent, controllerId,
                    triggeringCardId);
        }
    }

    /**
     * Puts {@code effect}, an ability of {@code match}'s permanent a player, counter or control event triggered, on the
     * stack as written with {@code event} recorded whatever the effect is (CR 603.3), together with the source as it
     * was (CR 608.2h) and the "do this only once each turn" mark (CR 603.2h). A permanent or player target is chosen as
     * it goes on the stack (CR 603.3d) through the any-target choice, which carries the same record into the entry it
     * makes, with the target filter the card declares for it (the opponents for a "target opponent"); a graveyard
     * target through the graveyard choice, carrying the source and the triggering permanent. A "you may" is the effect,
     * asked as it resolves (CR 603.5).
     */
    private boolean queueEventAbility(TriggerMatchContext match, EventRecord event,
                                      String description) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        Card sourceCard = source.getCard();
        UUID triggeringPermanentId = event.triggeringPermanent() == null ? null : event.triggeringPermanent().getId();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                event.thatPlayerId(),
                source.getId());
        entry.setNonTargeting(true);
        entry.setTriggeringPlayerId(event.triggeringPlayerId());
        if (event.eventValue() != null) {
            entry.setEventValue(event.eventValue());
        }
        entry.setTriggeringPermanentId(triggeringPermanentId);
        entry.setTriggeringPermanentControllerId(event.triggeringPermanentControllerId());
        if (event.triggeringCardId() != null) {
            entry.setTriggeringCardId(event.triggeringCardId());
        }
        entry.setSourcePermanentSnapshot(new Permanent(source));
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        gameData.handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            var choice = new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    !match.admits(TargetPredicate.Kind.PERMANENT),
                    targetFilterForTriggeredEffect(sourceCard,
                            match.targetingEffect()), 0,
                    source.getId(), new Permanent(source),
                    hasOptionalTarget(sourceCard,
                            match.targetingEffect()), triggeringPermanentId, null,
                    match.controllerId(), null, null, false, event.triggeringCardId(), null, null,
                    match.onceEachTurnActionKey(), null, Set.of(), event.triggeringPlayerId())
                    .withTriggeringPermanentController(event.triggeringPermanentControllerId());
            gameData.queueInteraction(event.eventValue() == null ? choice : choice.withEventValue(event.eventValue()));
        } else if (match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, match.controllerId(), match.stackEffects(), null, 0, 0, 0, null, false,
                    triggeringPermanentId, source.getId(), match.onceEachTurnActionKey()));
        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on {}", gameData.id, sourceCard.getName(), description);
        return true;
    }

    /**
     * "Whenever you collect evidence", "whenever you investigate (for the first time each turn)" and "whenever you
     * foretell a card" (Surveillance Monitor's Thopter, Erdwal Illuminator's Clue, Dream Devourer's boost): the ability
     * goes on the stack as written, recording the acting player and, for a foretell, the card foretold
     * ({@link #queueEventAbility}).
     */
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_COLLECTS_EVIDENCE)
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_INVESTIGATES)
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_INVESTIGATES_EACH_TIME)
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_FORETELLS)
    private boolean handlePlayerActionDefault(TriggerMatchContext match, TriggerContext ctx) {
        return switch (ctx) {
            case TriggerContext.CollectEvidence evidence -> queueEventAbility(match,
                    EventRecord.of(evidence.collectingPlayerId()), "collect evidence");
            case TriggerContext.Investigate investigate -> queueEventAbility(match,
                    EventRecord.of(investigate.investigatingPlayerId()), "investigate");
            case TriggerContext.Foretell foretell -> queueEventAbility(match,
                    new EventRecord(foretell.foretellingPlayerId(), null, null, null, null,
                            foretell.foretoldCard().getId()), "foretell");
            default -> throw new IllegalStateException("No player action default reads " + ctx);
        };
    }

    /**
     * "Whenever an opponent pays a tax from a spell or permanent you control, create a number of Treasure tokens equal
     * to the amount of mana they paid" (Tax Taker): the paying opponent is the triggering player and the non-targeting
     * "that player", the mana paid the event value, whatever the effects are ({@link #queueEventAbility}).
     */
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_OPPONENT_PAYS_TAX)
    private boolean handleOpponentTaxPaymentDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.TaxPayment tax = (TriggerContext.TaxPayment) ctx;
        return queueEventAbility(match, new EventRecord(tax.payingPlayerId(), tax.payingPlayerId(),
                tax.manaPaid(), null, null, null), "an opponent paying a tax");
    }

    /**
     * "Whenever an opponent gains control of a permanent from you, you create a Treasure token" (Zidane, Tantalus
     * Thief): the permanent with its new controller, who is the triggering player and the non-targeting "that player",
     * recorded whatever the effects are ({@link #queueEventAbility}).
     */
    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT)
    private boolean handleOpponentGainsControlDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.PermanentControlChanged change = (TriggerContext.PermanentControlChanged) ctx;
        return queueEventAbility(match, new EventRecord(change.newControllerId(), change.newControllerId(),
                        null, change.changedPermanent(), change.newControllerId(), null),
                "an opponent gaining control of a permanent");
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP)
    private boolean handleCoinFlipWonDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent permanent = match.permanent();

        enqueueCoinFlipTrigger(match);
        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
        log.info("Game {} - {} triggers on winning a coin flip", gameData.id, permanent.getCard().getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_ANY_PLAYER_WINS_COIN_FLIP)
    private boolean handleAnyPlayerCoinFlipWonDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent permanent = match.permanent();

        enqueueCoinFlipTrigger(match);
        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
        log.info("Game {} - {} triggers when any player wins a coin flip", gameData.id,
                permanent.getCard().getName());
        return true;
    }

    /**
     * "Whenever you lose a coin flip, this creature deals 1 damage to any target of an opponent's choice" (Karplusan
     * Minotaur): as the ability goes on the stack its controller chooses one of their opponents, who chooses the
     * target. The only lose-a-coin-flip ability reads its target that way.
     */
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_LOSES_COIN_FLIP)
    private boolean handleCoinFlipLostDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent permanent = match.permanent();
        if (declaresAnyTarget(match)) {
            gameData.handOffTriggerRecord(coinFlipEntry(match));
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    permanent.getCard(), match.controllerId(), match.effects(), permanent.getId(), null)
                    .chosenByAnOpponent());
        } else {
            enqueueCoinFlipTrigger(match);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
        log.info("Game {} - {} triggers on losing a coin flip", gameData.id, permanent.getCard().getName());
        return true;
    }

    /** Whether an effect of {@code match}'s ability declares "any target". */
    private static boolean declaresAnyTarget(TriggerMatchContext match) {
        return match.effects().stream().anyMatch(effect -> effect.targetSpec().declares(TargetPredicates.anyTarget()));
    }

    private void enqueueCoinFlipTrigger(TriggerMatchContext match) {
        GameData gameData = match.gameData();
        Permanent permanent = match.permanent();
        UUID controllerId = match.controllerId();
        StackEntry entry = coinFlipEntry(match);
        gameData.handOffTriggerRecord(entry);
        if (declaresAnyTarget(match)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    permanent.getCard(), controllerId, match.effects(), permanent.getId(), controllerId));
            return;
        }
        gameData.enqueueTrigger(entry);
    }

    /**
     * The entry of a coin-flip ability that chooses nothing, which is also the record an ability that chooses a target
     * as it goes on the stack is given ({@code GameData.handOffTriggerRecord}).
     */
    private static StackEntry coinFlipEntry(TriggerMatchContext match) {
        Permanent permanent = match.permanent();
        return new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                permanent.getCard(),
                match.controllerId(),
                permanent.getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                permanent.getId()
        );
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleLandPutIntoGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (land put into graveyard)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_LAND_CARD_MILLED)
    private boolean handleLandCardMilledDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on a land card being milled", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    private boolean handleCreatureCardsPutIntoGraveyardFromLibraryDefault(TriggerMatchContext match,
            TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        if (ctx instanceof TriggerContext.CreatureCardsPutIntoGraveyardFromLibrary batch) {
            entry.setEventValue(batch.creatureCardCount());
            recordCardsPutIntoGraveyard(gameData, entry, batch.cards());
        } else if (ctx instanceof TriggerContext.CreatureCardPutIntoGraveyardFromLibrary cardPut) {
            recordCardsPutIntoGraveyard(gameData, entry, List.of(cardPut.creatureCard()));
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on creature cards put into graveyard from library",
                gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CARDS_PUT_INTO_LIBRARY)
    private boolean handleCardsPutIntoLibraryDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        var source = match.permanent();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                source.getId()));

        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        return true;
    }

    /**
     * "Whenever a card is put into your graveyard from anywhere" (and from your library, from your hand or library,
     * or from anywhere but the battlefield, of a filter): the cards recorded whatever the effects are
     * ({@link #queueGraveyardCardsAbility}).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_ARTIFACT_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    private boolean handleCardPutIntoGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        List<Card> cards = switch (ctx) {
            case TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut -> cardsPut.cards();
            case TriggerContext.CardPutIntoGraveyard cardPut -> List.of(cardPut.card());
            case TriggerContext.PermanentCardPutIntoGraveyard permanentPut -> List.of(permanentPut.permanentCard());
            default -> List.of();
        };
        return queueGraveyardCardsAbility(match, cards, null, null, null,
                "card put into controller's graveyard");
    }

    /** "Whenever a creature card is put into your graveyard from anywhere": {@link #queueGraveyardCardsAbility}. */
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD)
    private boolean handleCreatureCardPutIntoGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        List<Card> cards = ctx instanceof TriggerContext.CreatureCardPutIntoGraveyard creatureCard
                && creatureCard.creatureCard() != null ? List.of(creatureCard.creatureCard()) : List.of();
        return queueGraveyardCardsAbility(match, cards, null, null, null,
                "creature card put into graveyard from anywhere");
    }

    /**
     * "Whenever a nonblack card is put into a player's graveyard from anywhere, that player …" (The Dark Barony) and
     * "whenever a black card is put into an opponent's graveyard from anywhere" (Compost): the card recorded whatever
     * the effects are ({@link #queueGraveyardCardsAbility}), the graveyard's owner the non-targeting "that player".
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCardPutIntoPlayersGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        return queueGraveyardCardsAbility(match, List.of(cardPut.card()), cardPut.graveyardOwnerId(),
                cardPut.graveyardOwnerId(), null, "card put into a player's graveyard from anywhere");
    }

    /**
     * "Whenever a creature card is put into an opponent's graveyard from their library" (Undead Alchemist): the card
     * recorded whatever the effects are ({@link #queueGraveyardCardsAbility}), its owner the non-targeting "that
     * player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_CREATURE_CARD_MILLED)
    private boolean handleOpponentCreatureCardMilledDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        return queueGraveyardCardsAbility(match, List.of(cardPut.card()), cardPut.graveyardOwnerId(),
                cardPut.graveyardOwnerId(), null, "creature card milled");
    }

    /**
     * "Whenever an opponent mills one or more cards" (Lo and Li, Royal Advisors): the mill recorded whatever the
     * effects are ({@link #queueGraveyardCardsAbility}), the number of cards milled the event value and the milling
     * player the non-targeting "that player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_MILLS)
    private boolean handleOpponentMillsDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.Mill mill = (TriggerContext.Mill) ctx;
        return queueGraveyardCardsAbility(match, List.of(), mill.milledPlayerId(), mill.milledPlayerId(),
                mill.milledCount(), "opponent milled");
    }

    /**
     * Puts {@code effect}, an effect of an ability that cards put into a graveyard triggered, on the stack whatever the
     * effect is: the entry records {@code cards} ({@link #recordCardsPutIntoGraveyard}: "that card", "them", each with
     * the graveyard stay it went to, CR 400.7), {@code triggeringPlayerId} as its triggering player and
     * {@code thatPlayerId} as its non-targeting "that player", {@code eventValue} ("that many") when not {@code null},
     * the source as it triggered (a permanent's snapshot, a plane's planar object) and the "do this only once each
     * turn" mark (CR 603.2h). An ability that targets waits for its targets as it goes on the stack (CR 603.3d).
     * Returns whether it triggered: an ability of no source doesn't.
     */
    private boolean queueGraveyardCardsAbility(TriggerMatchContext match, List<Card> cards,
                                               UUID triggeringPlayerId, UUID thatPlayerId, Integer eventValue,
                                               String event) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        Card sourceCard = source != null ? source.getCard() : match.sourceCard();
        if (sourceCard == null) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                thatPlayerId,
                source == null ? null : source.getId());
        entry.setNonTargeting(true);
        recordCardsPutIntoGraveyard(gameData, entry, cards);
        entry.setTriggeringPlayerId(triggeringPlayerId);
        if (eventValue != null) {
            entry.setEventValue(eventValue);
        }
        // The source as the ability goes on the stack, whatever the effect is (CR 608.2h).
        if (source != null) {
            entry.setSourcePermanentSnapshot(new Permanent(source));
        }
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
        }
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        gameData.handOffTriggerRecord(entry);
        if (source != null && (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)
                || match.admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), match.stackEffects(), event, source.getId(),
                    new Permanent(source), eventValue));
        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers ({})", gameData.id, sourceCard.getName(), event);
        return true;
    }

    /**
     * "Whenever a card is put into an opponent's graveyard from anywhere", from a permanent or, while suspended, from
     * exile (Nihilith): the pipeline judged the ability's options as it triggered and wrapped its intervening "if"
     * around the effect, which checks it again as it resolves (CR 603.4). On the battlefield the graveyard's owner is
     * "that player" (the entry's target), and "that card" is the graveyard object the card became (CR 400.7).
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCardPutIntoOpponentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent() != null ? match.permanent().getCard() : match.sourceCard();
        if (sourceCard == null) return false;
        String cardName = sourceCard.getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                match.permanent() == null ? null : cardPut.graveyardOwnerId(),
                match.permanent() == null ? null : match.permanent().getId()
        );
        entry.setNonTargeting(!match.declaresTarget());
        entry.setTriggeringPermanentControllerId(cardPut.graveyardOwnerId());
        entry.setTriggeringCard(cardPut.card().getId(), gameData.graveyardEntryVersion(cardPut.card().getId()));
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers (card put into opponent's graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCreatureCardPutIntoOpponentGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        if (ctx instanceof TriggerContext.CreatureCardPutIntoGraveyard creatureCard
                && creatureCard.creatureCard() != null) {
            // "That card", as it was put into the graveyard, with its graveyard incarnation (CR 400.7): an effect that
            // returns "it" finds that incarnation there; "its mana value" reads the card as it was in the event.
            UUID cardId = creatureCard.creatureCard().getId();
            entry.setTriggeringCard(cardId, gameData.graveyardEntryVersion(cardId));
            entry.setTriggeringCards(List.of(creatureCard.creatureCard()));
        }
        entry.setNonTargeting(match.effects().stream().allMatch(effect -> effect.targetSpec() == TargetSpec.NONE));
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature card put into opponent's graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_NONLAND_CARDS_MILLED)
    private boolean handleNonlandCardsMilled(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.NonlandCardsMilled milled)) {
            return false;
        }

        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setEventValue(milled.nonlandCardCount());
        entry.setTriggeringPlayerId(milled.milledPlayerId());
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        gameData.handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    "nonland cards milled", match.permanent().getId(), milled.nonlandCardCount()));
        } else {
            gameData.enqueueTrigger(entry);
        }

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on nonland cards being milled",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_SELF_BECOMES_SADDLED)
    private boolean handleBecomesSaddledDefault(TriggerMatchContext match, TriggerContext ctx) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        if (source == null) return false;

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                source.getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers on becoming saddled",
                gameData.id, source.getCard().getName());
        return true;
    }

    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_SELF_BECOMES_CREWED)
    private boolean handleBecomesCrewedDefault(TriggerMatchContext match, TriggerContext ctx) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        if (source == null) return false;

        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : source.getCard();
        boolean needsTarget = match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)
                || match.admits(TargetPredicate.Kind.GRAVEYARD_CARD);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                source.getId());
        entry.setSourcePermanentSnapshot(new Permanent(source));
        gameData.handOffTriggerRecord(entry);
        if (needsTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    "becomes crewed", source.getId(), new Permanent(source), null));
        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on becoming crewed", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_SELF_BECOMES_STATIONED)
    private boolean handleBecomesStationed(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureStationed stationed)
                || match.permanent() == null) {
            return false;
        }

        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                stationed.creatureCard().getId(),
                match.permanent().getId());
        entry.setNonTargeting(true);
        gameData.pendingActivatedAbilityCostTriggers.add(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers when a creature stations it",
                gameData.id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_SELF_MUTATES)
    private boolean handleSelfMutatesDefault(TriggerMatchContext match, TriggerContext ctx) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        if (source == null) return false;

        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : source.getCard();
        List<CardEffect> effects = match.stackEffects();
        boolean needsTarget = effects.stream().anyMatch(candidate ->
                candidate.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || candidate.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || candidate.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                effects,
                null,
                source.getId());
        entry.setSourcePermanentSnapshot(new Permanent(source));
        gameData.handOffTriggerRecord(entry);
        if (needsTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), effects,
                    "mutation", source.getId()));
        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on mutating",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_ALLY_CREATURE_CONNIVES)
    private boolean handleAllyCreatureConnivesDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CreatureConnives connive = (TriggerContext.CreatureConnives) ctx;
        Permanent source = match.permanent();
        if (source == null || connive.connivingCreature() == null) return false;

        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : source.getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                connive.connivingCreature().getId(),
                source.getId());
        entry.setTriggeringPermanentId(connive.connivingCreature().getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(source));
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers for a controlled creature conniving",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_ALLY_CREATURE_MUTATES)
    private boolean handleAllyCreatureMutatesDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CreatureMutates mutation = (TriggerContext.CreatureMutates) ctx;
        Permanent source = match.permanent();
        if (source == null || mutation.mutatedPermanent() == null) return false;

        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : source.getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                mutation.mutatedPermanent().getId(),
                source.getId());
        entry.setTriggeringPermanentId(mutation.mutatedPermanent().getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(source));
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers for a controlled creature mutating",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    /**
     * "Whenever you surveil, …" and "whenever an opponent surveils, …": the ability goes on the stack as written, its
     * entry recording the surveilling player as its triggering player (and, for an opponent's surveil, its non-targeting
     * "that player": River Song's "to that player"), the source as it was and the "do this only once each turn" mark,
     * whatever its effects are. A permanent or player target of "whenever you surveil" is chosen as it goes on the stack
     * (CR 603.3d), the choice carrying the player.
     */
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_CONTROLLER_SURVEILS)
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_OPPONENT_SURVEILS)
    private boolean handleSurveilDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent source = match.permanent();
        UUID surveilingPlayerId = ((TriggerContext.Surveil) ctx).surveilingPlayerId();
        boolean opponentSurveil = !match.controllerId().equals(surveilingPlayerId);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                match.stackEffects(),
                opponentSurveil ? surveilingPlayerId : null,
                source.getId());
        if (opponentSurveil) {
            entry.setNonTargeting(true);
        }
        entry.setTriggeringPlayerId(surveilingPlayerId);
        entry.setSourcePermanentSnapshot(new Permanent(source));
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        gameData.handOffTriggerRecord(entry);
        if (!opponentSurveil && (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER))) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    source.getCard(),
                    match.controllerId(),
                    match.stackEffects(),
                    !match.admits(TargetPredicate.Kind.PERMANENT),
                    null,
                    0,
                    source.getId(),
                    new Permanent(source)).withTriggeringPlayer(surveilingPlayerId));
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
            return true;
        }

        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers on surveil", gameData.id, source.getCard().getName());
        return true;
    }

    @CollectsActionTriggers({
        @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_COMMITS_CRIME),
        @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_OPPONENT_COMMITS_CRIME)
    })
    private boolean handleCrimeDefault(TriggerMatchContext match, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent source = match.permanent();
        List<CardEffect> effects = match.stackEffects();
        boolean needsTarget = match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)
                || match.admits(TargetPredicate.Kind.GRAVEYARD_CARD);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                effects,
                null,
                source.getId());
        gameData.handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    source.getCard(), match.controllerId(), effects));
        } else if (needsTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    source.getCard(), match.controllerId(), effects,
                    "crime", source.getId()));
        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers on a crime", gameData.id, source.getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD)
    boolean handleControllerCardsLeaveGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!handleControllerArtifactOrCreatureCardsLeaveGraveyard(match, ctx)) {
            return false;
        }

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards left graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_CARD_RETURNED_FROM_GRAVEYARD_TO_HAND)
    boolean handleControllerCardReturnedFromGraveyardToHand(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardReturnedFromGraveyardToHand returned)) {
            return false;
        }

        Card snapshot = returned.returnedCard().createCardCopy();
        snapshot.freeze();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(returned.returnedCard().getId());
        entry.setTriggeringCardSnapshot(snapshot);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (card returned from graveyard to hand)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_CARD_PUT_INTO_HAND_FROM_LIBRARY)
    boolean handleControllerCardPutIntoHandFromLibrary(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardPutIntoHandFromLibrary moved)) {
            return false;
        }
        return enqueueControllerCardPutIntoHandFromLibraryTrigger(match, moved.card());
    }

    private boolean enqueueControllerCardPutIntoHandFromLibraryTrigger(TriggerMatchContext match, Card movedCard) {
        Card snapshot = movedCard.createCardCopy();
        snapshot.freeze();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(movedCard.getId());
        entry.setTriggeringCardSnapshot(snapshot);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (card put into hand from library)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_INSTANT_OR_SORCERY_CARD_LEAVES_GRAVEYARD)
    boolean handleControllerInstantOrSorceryCardLeavesGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerInstantOrSorceryCardLeavesGraveyard)) {
            return false;
        }
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (instant or sorcery card left graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_FROM_LIBRARY_OR_GRAVEYARD)
    boolean handleControllerCardsExiledFromLibraryOrGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsExiledFromLibraryOrGraveyard exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(exiled.count());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards exiled from library or graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_FROM_GRAVEYARD)
    boolean handleControllerCardsExiledFromGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsExiledFromGraveyard exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(exiled.count());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards exiled from graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED)
    boolean handleControllerCreaturesOrCreatureCardsExiled(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCreaturesOrCreatureCardsExiled exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(exiled.count());
        entry.setTriggeringCards(exiled.creatureCards());
        match.gameData().handOffTriggerRecord(entry);
        if (match.effectOf(KayaSpiritsJusticeEffect.class) != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DrawTriggerPermanentTarget(
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.stackEffects(),
                    match.permanent().getId(),
                    new ControlledPermanentPredicateTargetFilter(
                            new PermanentIsTokenPredicate(), "Target must be a token you control"))
                    .withTriggeringCards(exiled.creatureCards()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} triggers (creatures or creature cards exiled) and needs a token target",
                    match.gameData().id, match.permanent().getCard().getName());
            return true;
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creatures or creature cards exiled)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * "Whenever a creature is put into exile from the battlefield", its own exile included (CR 603.6c, 603.10a): the
     * entry records, whatever the effects are, "that card" with the exile incarnation it has now (CR 400.7), the
     * creature as it last existed on the battlefield with its controller and power then ("its power" is the event value,
     * CR 608.2h), the source as it triggered and the "do this only once each turn" mark (CR 603.2h). An ability that
     * targets waits for its targets as it goes on the stack (CR 603.3d), its choice carrying the creature.
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD)
    boolean handleAnyCreatureExiledFromBattlefield(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureExiledFromBattlefield exiled)) {
            return false;
        }
        Permanent source = match.permanent();
        Permanent lastKnown = exiled.exiledPermanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                source.getId()
        );
        entry.setNonTargeting(true);
        Card exiledCard = lastKnown.getOriginalCard() != null ? lastKnown.getOriginalCard() : lastKnown.getCard();
        entry.setTriggeringCardId(exiledCard.getId());
        entry.setTriggeringCardExileEntryVersion(
                match.gameData().exileEntryVersions.getOrDefault(exiledCard.getId(), 0L));
        entry.setTriggeringPermanentId(lastKnown.getId());
        entry.setTriggeringPermanent(lastKnown);
        entry.setTriggeringPermanentControllerId(exiled.exiledControllerId());
        entry.setTriggeringPermanentPowerAtTrigger(exiled.exiledPowerAtTrigger());
        entry.setEventValue(exiled.exiledPowerAtTrigger());
        entry.setSourcePermanentSnapshot(new Permanent(source));
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    source.getCard(), match.controllerId(), match.stackEffects(),
                    "creature exiled from the battlefield", source.getId(), new Permanent(source),
                    exiled.exiledPowerAtTrigger()).withTriggeringPermanent(lastKnown));
        } else {
            match.gameData().enqueueTrigger(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers (creature exiled from battlefield)",
                match.gameData().id, source.getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_DURING_TURN)
    boolean handleControllerCardsExiledDuringTurn(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardsExiledDuringTurn)) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards exiled during turn)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_OWNED_CARD_EXILED)
    boolean handleOpponentOwnedCardExiled(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.OpponentOwnedCardExiled exiled)
                || match.controllerId().equals(exiled.ownerId())) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent-owned card exiled)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_OTHER_PERMANENT_PHASES_OUT)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CARD_EXILED)
    boolean handleGlobalPhaseOutOrCardExileTriggers(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.PermanentPhasedOut)
                && !(ctx instanceof TriggerContext.CardExiled)) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on a permanent phasing out or a card being exiled",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_FROM_HAND)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_SPELL_OR_ABILITY_EXILES_PERMANENT)
    boolean handleControllerExileTriggers(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsExiledFromHand)
                && !(ctx instanceof TriggerContext.ControllerSpellOrAbilityExilesPermanent)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        match.gameData().stack.add(entry);
        if (ctx instanceof TriggerContext.ControllerCardsExiledFromHand exiled) {
            entry.setEventValue(exiled.count());
        } else {
            entry.setEventValue(1);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on cards or permanents being exiled",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD)
    boolean handleControllerCreatureCardsLeaveGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature cards left graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD)
    boolean handleControllerArtifactOrCreatureCardsLeaveGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsLeaveGraveyard cardsLeft)) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setTriggeringCards(cardsLeft.cards());
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    match.permanent().getCard(), match.controllerId(), match.stackEffects(),
                    "leaves-the-graveyard", match.permanent().getId()));
        } else {
            match.gameData().enqueueTrigger(entry);
        }
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_ARTIFACT_CARDS_LEAVE_GRAVEYARD)
    boolean handleControllerArtifactCardsLeaveGraveyard(TriggerMatchContext match, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        match.gameData().handOffTriggerRecord(entry);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    match.permanent().getCard(), match.controllerId(), match.stackEffects(),
                    "artifact-leaves-the-graveyard", match.permanent().getId()));
        } else {
            match.gameData().enqueueTrigger(entry);
        }
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN)
    boolean handleCardsExiledFromGraveyardsOrBattlefieldDuringYourTurn(TriggerMatchContext match, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardsExiledFromGraveyardsOrBattlefield exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(exiled.count());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards exiled from graveyards or battlefield during your turn)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * Records on {@code entry} the cards a put-into-a-graveyard event was about, whatever its effects are: as its
     * triggering cards ("them", "one of them"), each with the graveyard stay it went to (a card that left the graveyard
     * and returned is a new object, CR 400.7), and a lone card also as its triggering card ("that card").
     */
    static void recordCardsPutIntoGraveyard(GameData gameData, StackEntry entry, List<Card> cards) {
        entry.setTriggeringCards(cards);
        for (Card card : cards) {
            entry.getEventCardGraveyardEntryVersions().put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
        if (cards.size() == 1) {
            entry.setTriggeringCard(cards.getFirst().getId(), gameData.graveyardEntryVersion(cards.getFirst().getId()));
        }
    }

    /**
     * "Whenever a creature you control fights, [effect]" (Foe-Razer Regent's "put two +1/+1 counters on it at the
     * beginning of the next end step"): the card's effect goes on the stack as written, the fighting creature its
     * triggering permanent ("it", CR 701.14a).
     */
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_ALLY_CREATURE_FIGHTS)
    private boolean handleAllyCreatureFights(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CreatureFights fights = (TriggerContext.CreatureFights) ctx;
        Permanent fightingCreature = fights.fightingCreature();
        if (fightingCreature == null) return false;

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentId(fightingCreature.getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers after {} fought",
                match.gameData().id, sourceCard.getName(), fightingCreature.getCard().getName());
        return true;
    }

    /**
     * The target filter of a triggered ability's target choice: the one the card declares for {@code effect}, else the
     * effect's triggered filter, else the card's. With none, an effect whose player half is "target opponent"
     * ({@link CardEffect#targetPlayerRelation()}: "target opponent or planeswalker") is offered the opponents only, and
     * its permanent half (the planeswalkers) as its target predicate restricts it: the choice reads no relation of its
     * own.
     */
    static TargetFilter targetFilterForTriggeredEffect(Card card, CardEffect effect) {
        int targetIndex = card.getEffectTargetIndex(effect);
        if (targetIndex >= 0 && targetIndex < card.getTargetGroups().size()) {
            return card.getTargetGroups().get(targetIndex).getFilter();
        }
        if (effect instanceof DamageDealingEffect damageEffect
                && damageEffect.triggeredTargetFilter() != null) {
            return damageEffect.triggeredTargetFilter();
        }
        TargetFilter cardFilter = card.getTargetFilter();
        if (cardFilter != null || effect.targetPlayerRelation() != PlayerRelation.OPPONENT
                || !effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            return cardFilter;
        }
        PlayerRelationPredicate opponent = new PlayerRelationPredicate(PlayerRelation.OPPONENT);
        if (!effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            return new PlayerPredicateTargetFilter(opponent, "Target must be an opponent");
        }
        return new AnyTargetPredicateTargetFilter(
                effect.targetSpec().targetPredicate().permanentRestriction().orElse(new PermanentTruePredicate()),
                opponent, "Target must be an opponent or planeswalker");
    }

    static boolean hasOptionalTarget(Card card, CardEffect effect) {
        int targetIndex = card.getEffectTargetIndex(effect);
        return targetIndex >= 0 && targetIndex < card.getTargetGroups().size()
                && card.getTargetGroups().get(targetIndex).getMinTargets() == 0;
    }
}

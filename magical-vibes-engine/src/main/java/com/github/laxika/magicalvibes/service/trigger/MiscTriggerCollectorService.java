package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.KayaSpiritsJusticeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
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
    private boolean handleControllerGivesGift(
            TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(),
                GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_SELF_BECOMES_UNTAPPED)
    private boolean handleSelfBecomesUntapped(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SelfBecomesUntapped su = (TriggerContext.SelfBecomesUntapped) ctx;
        Card card = match.permanent().getCard();
        // One stack entry takes every effect of the card's "this becomes untapped" abilities, put there for the first
        // of them. The reader wraps an effect in its ability's options afresh on every read, so it is found by
        // equality.
        ConditionContext context = ConditionContext.forPermanent(match.permanent(), match.controllerId());
        List<CardEffect> cardEffects = TapAbilityReader.printedEffects(card, TapWatch.ON_SELF_BECOMES_UNTAPPED,
                conditionEvaluationService.judge(match.gameData(), context));
        if (cardEffects.isEmpty() || !cardEffects.getFirst().equals(effect)) {
            return true;
        }
        // The judge left out the abilities whose intervening "if" doesn't hold; the effects go on the stack as written,
        // an intervening "if" still around them for the check as the ability resolves (CR 603.4).
        List<CardEffect> triggeredEffects = cardEffects;

        boolean needsTarget = triggeredEffects.stream()
                .anyMatch(triggeredEffect ->
                        triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                                || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                                || card.getEffectTargetIndex(triggeredEffect) >= 0);
        if (needsTarget) {
            boolean multiTarget = card.getTargetGroups().stream()
                    .anyMatch(target -> target.getMaxTargets() > 1 || target.getMinTargets() == 0)
                    || card.getTargetGroups().size() > 1;
            if (multiTarget) {
                match.gameData().queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, su.controllerId(), new ArrayList<>(triggeredEffects), match.permanent().getId(),
                        List.of(), 0, 0, List.of(), 0));
            } else {
                match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        card, su.controllerId(), new ArrayList<>(triggeredEffects),
                        "becomes untapped", match.permanent().getId()));
            }
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    su.controllerId(),
                    card.getName() + "'s ability",
                    new ArrayList<>(triggeredEffects),
                    null,
                    match.permanent().getId());
            // "Return that exiled card" (Tawnos's Coffin): the cards exiled with this permanent as it untapped are the
            // entry's triggering cards, which the return reads as it resolves.
            entry.setTriggeringCards(match.gameData().exileReturnOnPermanentLeave
                    .getOrDefault(match.permanent().getId(), List.of()).stream()
                    .map(pending -> pending.card())
                    .toList());
            match.gameData().enqueueTrigger(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(card));
        log.info("Game {} - {} triggers on becoming untapped", match.gameData().id, card.getName());
        return true;
    }

    /**
     * "Whenever you untap one or more permanents during your untap step, …" (The Millennium Calendar's "put that many
     * time counters"): the ability goes on the stack as written, once for the untap step, with the number of
     * permanents untapped as its event value ("that many") and the source as it was, whatever its effects are.
     */
    @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP)
    private boolean handleUntapStepDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.UntapStep untapStep = (TriggerContext.UntapStep) ctx;
        GameData gameData = match.gameData();
        Card card = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                match.controllerId(),
                card.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(untapStep.untappedPermanentCount());
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
        log.info("Game {} - {} triggers on untap step", gameData.id, card.getName());
        return true;
    }

    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_SELF_BECOMES_MONSTROUS)
    private boolean handleSelfBecomesMonstrous(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SelfBecomesMonstrous sm = (TriggerContext.SelfBecomesMonstrous) ctx;
        Card card = match.permanent().getCard();
        // The collector dispatches the first of the permanent's monstrous effects, which go on the stack as one object.
        List<CardEffect> triggeredEffects = sm.effects();

        boolean needsTarget = triggeredEffects.stream().anyMatch(triggeredEffect ->
                triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        if (needsTarget) {
            boolean multiTarget = card.getTargetGroups().stream()
                    .anyMatch(target -> target.getMaxTargets() > 1 || target.getMinTargets() == 0)
                    || card.getTargetGroups().size() > 1;
            if (multiTarget) {
                match.gameData().queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, sm.controllerId(), new ArrayList<>(triggeredEffects), match.permanent().getId(),
                        List.of(), 0, 0, List.of(), sm.xValue()));
                if (!match.gameData().interaction.isAwaitingInput()) {
                    etbTokenTargetService.processNextETBTokenMultiTargetTrigger(match.gameData());
                }
            } else {
                match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        card, sm.controllerId(), new ArrayList<>(triggeredEffects),
                        "becomes monstrous", match.permanent().getId()));
            }
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    sm.controllerId(),
                    card.getName() + "'s ability",
                    new ArrayList<>(triggeredEffects),
                    sm.xValue(),
                    null,
                    match.permanent().getId(),
                    Map.of(),
                    null,
                    List.of(),
                    List.of()));
        }
        gameLogService.append(match.gameData(), GameLog.cardThen(card,
                "'s ability triggers (became monstrous)."));
        log.info("Game {} - {} triggers on becoming monstrous", match.gameData().id,
                card.getName());
        return true;
    }

    /**
     * "Whenever a player sacrifices a creature": the sacrifice recorded whatever the effects are
     * ({@link DeathTriggerCollectorService#queueDepartureAbility}), the sacrificing player its non-targeting "that
     * player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CREATURE_SACRIFICED)
    private boolean handleAnyCreatureSacrificedDefault(TriggerMatchContext match,
                                                        CardEffect effect, TriggerContext ctx) {
        queueSacrificeAbility(match, effect, (TriggerContext.AllySacrificed) ctx);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever you sacrifice a token", from exile (Albiorix, Goose Tyrant, CR 113.6k): the sacrifice recorded whatever
     * the effects are ({@link DeathTriggerCollectorService#queueDepartureAbility}); the source is the exiled card.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.EXILE_ON_CONTROLLER_TOKEN_SACRIFICED)
    private boolean handleExileTokenSacrificedDefault(TriggerMatchContext match,
                                                       CardEffect effect, TriggerContext ctx) {
        if (match.sourceCard() == null) {
            return false;
        }
        queueSacrificeAbility(match, effect, (TriggerContext.AllySacrificed) ctx);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.sourceCard()));
        return true;
    }

    /**
     * The sacrifice {@code sacrificed} as {@link DeathTriggerCollectorService#queueDepartureAbility} records a
     * permanent's leaving the battlefield: "that card", the permanent as it last existed, the sacrificing player as its
     * controller and as the entry's non-targeting "that player".
     */
    private void queueSacrificeAbility(TriggerMatchContext match, CardEffect effect,
                                       TriggerContext.AllySacrificed sacrificed) {
        deathTriggerCollectorService.queueDepartureAbility(match, effect, sacrificed.sacrificedCard(),
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
    private boolean handleAllyCreatureExploit(TriggerMatchContext match, CardEffect queuedEffect,
                                               TriggerContext ctx) {
        TriggerContext.CreatureExploit exploit = (TriggerContext.CreatureExploit) ctx;
        if (queuedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || queuedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(), match.controllerId(),
                    new ArrayList<>(List.of(queuedEffect)), match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        StackEntry triggeredEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(queuedEffect)),
                null,
                match.permanent().getId());
        triggeredEntry.setEventValue(exploit.exploitedPower());
        match.gameData().enqueueTrigger(triggeredEntry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers when {} exploits {}",
                match.gameData().id, match.permanent().getCard().getName(),
                exploit.exploitingCard().getName(), exploit.exploitedCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_PERMANENT_SACRIFICED)
    private boolean handleAnyPermanentSacrificedDefault(TriggerMatchContext match, CardEffect effect,
                                                        TriggerContext ctx) {
        TriggerContext.PermanentSacrificed sacrificed = (TriggerContext.PermanentSacrificed) ctx;
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(), match.controllerId(),
                    new ArrayList<>(List.of(effect)), match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        return true;
    }

    /**
     * "Whenever you sacrifice a [filter] permanent": the trigger pipeline has matched the permanent. The sacrificed
     * permanent, as it last existed on the battlefield (CR 603.10a), is the stack entry's triggering permanent and its
     * card the triggering card; a "you may" goes on the stack and is chosen as it resolves (CR 603.5), and "do this
     * only once each turn" is used up by an accepted graveyard return.
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED)
    private boolean handleSacrificeDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.AllySacrificed as = (TriggerContext.AllySacrificed) ctx;
        String cardName = match.permanent().getCard().getName();
        Permanent sacrificed = as.sacrificedPermanent() != null ? as.sacrificedPermanent()
                : as.sacrificedCard() != null ? new Permanent(as.sacrificedCard()) : null;
        CardEffect graveyardTargeting = effect instanceof MayEffect may ? may.wrapped() : effect;
        if (graveyardTargeting instanceof ReturnCardFromGraveyardEffect returnEffect
                && returnEffect.targetGraveyard()) {
            // The card is targeted as the trigger is put on the stack (CR 603.3d), so the may choice
            // comes at resolution. "Do this only once each turn" is used up only by an accepted return.
            // The choice's X is the sacrificed permanent's mana value as it last existed (CR 603.10a), which a
            // target filter's X reads ("target creature card with lesser mana value", Riveteers Ascendancy).
            boolean markOnAcceptance = match.markSourceOncePerTurnOnAcceptance();
            int sacrificedManaValue = as.sacrificedCard() == null ? 0 : as.sacrificedCard().getManaValue();
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    null, 0, sacrificedManaValue, 0, null, false, null, match.permanent().getId(), markOnAcceptance));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(),
                    match.controllerId(),
                    new ArrayList<>(List.of(effect)),
                    match.permanent().getId()).withTriggeringPermanent(sacrificed));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} triggers on matching permanent sacrifice (awaiting target)",
                    match.gameData().id, cardName);
            return true;
        }
        StackEntry triggerEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                as.sacrificingPlayerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        triggerEntry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        triggerEntry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
        triggerEntry.setTriggeringPermanent(sacrificed);
        if (as.sacrificedCard() != null) {
            // CR 400.7: the trigger refers to this stay in the graveyard; if the card leaves and returns before
            // the trigger resolves, it is a new object the trigger no longer finds.
            triggerEntry.setTriggeringCard(as.sacrificedCard().getId(),
                    match.gameData().graveyardEntryVersion(as.sacrificedCard().getId()));
        }
        match.gameData().enqueueTrigger(triggerEntry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on matching permanent sacrifice", match.gameData().id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_SACRIFICED)
    private boolean handleOpponentPermanentSacrificed(TriggerMatchContext match,
                                                       CardEffect effect,
                                                       TriggerContext ctx) {
        TriggerContext.OpponentPermanentSacrificed sacrificed =
                (TriggerContext.OpponentPermanentSacrificed) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    private boolean handleOpponentNontokenPermanentSacrificed(TriggerMatchContext match,
                                                               CardEffect effect,
                                                               TriggerContext ctx) {
        queueSacrificeAbility(match, effect, (TriggerContext.AllySacrificed) ctx);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever enchanted permanent becomes tapped": the ability is the Aura's (CR 113.7), and its entry records the
     * tapped permanent and its controller whatever its effects are ("destroy it", "its controller loses 2 life", "that
     * player mills three cards", read as the ability resolves, else from its last known information), the controller as
     * the non-targeting "that player" and the Aura as it was. A targeted ability's target is chosen as it goes on the
     * stack (CR 603.3d), the choice carrying the same record. A "choose one" (Relic Bind) goes on the stack the same
     * way, its mode and that mode's target announced there before a player gets priority (CR 603.3c,
     * {@link StackedModalTriggerService}); its modes choose their own targets, so it names no "that player".
     */
    @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapDefault(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        Card sourceCard = match.permanent().getCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    "enchanted permanent became tapped", match.permanent().getId(),
                    new Permanent(match.permanent()), null, false)
                    .withTriggeringPermanent(ept.tappedPermanent()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s triggered ability",
                    new ArrayList<>(List.of(effect)),
                    effect instanceof ChooseOneEffect ? null : ept.tappedPermanentControllerId(),
                    match.permanent().getId());
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentId(ept.tappedPermanent().getId());
            entry.setTriggeringPermanentControllerId(ept.tappedPermanentControllerId());
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            match.gameData().enqueueTrigger(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on enchanted permanent tap ({})",
                match.gameData().id, sourceCard.getName(), ept.tappedPermanent().getCard().getName());
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
    private boolean queueEventAbility(TriggerMatchContext match, CardEffect effect, EventRecord event,
                                      String description) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        Card sourceCard = source.getCard();
        UUID triggeringPermanentId = event.triggeringPermanent() == null ? null : event.triggeringPermanent().getId();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            var choice = new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT),
                    DamageTriggerCollectorService.targetFilterForTriggeredEffect(sourceCard, effect), 0,
                    source.getId(), new Permanent(source),
                    DamageTriggerCollectorService.hasOptionalTarget(sourceCard, effect), triggeringPermanentId, null,
                    match.controllerId(), null, null, false, event.triggeringCardId(), null, null,
                    match.markSourceOncePerTurnOnAcceptance(), null, Set.of(), event.triggeringPlayerId())
                    .withTriggeringPermanentController(event.triggeringPermanentControllerId());
            gameData.queueInteraction(event.eventValue() == null ? choice : choice.withEventValue(event.eventValue()));
        } else if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), null, 0, 0, 0, null, false,
                    triggeringPermanentId, source.getId(), match.markSourceOncePerTurnOnAcceptance()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
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
            entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
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
    private boolean handlePlayerActionDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        return switch (ctx) {
            case TriggerContext.CollectEvidence evidence -> queueEventAbility(match, effect,
                    EventRecord.of(evidence.collectingPlayerId()), "collect evidence");
            case TriggerContext.Investigate investigate -> queueEventAbility(match, effect,
                    EventRecord.of(investigate.investigatingPlayerId()), "investigate");
            case TriggerContext.Foretell foretell -> queueEventAbility(match, effect,
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
    private boolean handleOpponentTaxPaymentDefault(TriggerMatchContext match, CardEffect effect,
                                                    TriggerContext ctx) {
        TriggerContext.TaxPayment tax = (TriggerContext.TaxPayment) ctx;
        return queueEventAbility(match, effect, new EventRecord(tax.payingPlayerId(), tax.payingPlayerId(),
                tax.manaPaid(), null, null, null), "an opponent paying a tax");
    }

    /**
     * "Whenever you get one or more {E}" (Territorial Gorger, Fabrication Module's target, Brotherhood Scribe's
     * intervening "if"): the player and the energy gained ("that much") recorded whatever the effects are
     * ({@link #queueEventAbility}).
     */
    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_CONTROLLER_GETS_ENERGY)
    private boolean handleEnergyGainDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.EnergyGain energy = (TriggerContext.EnergyGain) ctx;
        return queueEventAbility(match, effect,
                EventRecord.of(energy.gainingPlayerId()).withEventValue(energy.energyGainedAmount()), "energy gain");
    }

    /**
     * "Whenever you put one or more counters on a permanent or player, … deals that much damage to target …" (All Will
     * Be One): the placing player and the number of counters put ("that much"), recorded whatever the effects are
     * ({@link #queueEventAbility}).
     */
    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER)
    private boolean handleCountersPlacedDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.CountersPlaced placed = (TriggerContext.CountersPlaced) ctx;
        return queueEventAbility(match, effect,
                EventRecord.of(placed.placingPlayerId()).withEventValue(placed.amount()), "counters being put");
    }

    /**
     * "Whenever you put a lore counter on a Saga you control, put a +1/+1 counter on up to one other target creature"
     * (Sigurd, Jarl of Ravensthorpe, whose target group says "up to one other"): the placing player and the Saga as
     * the triggering permanent, recorded whatever the effects are ({@link #queueEventAbility}).
     */
    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_YOU_PUT_LORE_COUNTERS_ON_SAGA)
    private boolean handleLoreCounterPlacedDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.LoreCounterPlaced lore = (TriggerContext.LoreCounterPlaced) ctx;
        return queueEventAbility(match, effect,
                EventRecord.of(lore.placingPlayerId()).withPermanent(lore.saga(), lore.placingPlayerId()),
                "a lore counter being put on a Saga");
    }

    /**
     * "Whenever one or more loyalty counters are removed from this, it deals that much damage to target opponent or
     * planeswalker" (Chandra, Fire Artisan) and "whenever one or more counters are removed from a permanent you
     * control" (Cloudsculpt Armorer's seek): the permanent the counters were removed from with its controller and the
     * number removed ("that much"), recorded whatever the effects are ({@link #queueEventAbility}).
     */
    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_SELF_LOYALTY_COUNTERS_REMOVED)
    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT)
    private boolean handleCountersRemovedDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        Permanent permanent;
        int amount;
        switch (ctx) {
            case TriggerContext.LoyaltyCountersRemoved loyalty -> {
                permanent = loyalty.permanent();
                amount = loyalty.amount();
            }
            case TriggerContext.CountersRemovedFromPermanent removed -> {
                permanent = removed.permanent();
                amount = removed.amount();
            }
            default -> throw new IllegalStateException("No counter-removal default reads " + ctx);
        }
        if (amount <= 0 || match.permanent() == null) {
            return false;
        }
        return queueEventAbility(match, effect, EventRecord.of(match.controllerId()).withEventValue(amount)
                .withPermanent(permanent, match.controllerId()), "counters being removed");
    }

    /**
     * "Whenever an opponent gains control of a permanent from you, you create a Treasure token" (Zidane, Tantalus
     * Thief): the permanent with its new controller, who is the triggering player and the non-targeting "that player",
     * recorded whatever the effects are ({@link #queueEventAbility}).
     */
    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT)
    private boolean handleOpponentGainsControlDefault(TriggerMatchContext match, CardEffect effect,
                                                      TriggerContext ctx) {
        TriggerContext.PermanentControlChanged change = (TriggerContext.PermanentControlChanged) ctx;
        return queueEventAbility(match, effect, new EventRecord(change.newControllerId(), change.newControllerId(),
                        null, change.changedPermanent(), change.newControllerId(), null),
                "an opponent gaining control of a permanent");
    }

    /**
     * "Whenever a mana ability of this creature resolves, put a number of +1/+1 counters on it equal to the amount of
     * mana this creature produced" (Tyvar the Bellicose's grant): the activating player and the mana produced (the event
     * value), recorded whatever the effects are ({@link #queueEventAbility}); its "triggers only once each turn" is the
     * reader's limit.
     */
    @CollectsSpellAbilityTrigger(value = CardEffect.class, watch = SpellAbilityWatch.ON_SELF_MANA_ABILITY_RESOLVES)
    private boolean handleManaAbilityResolvedDefault(TriggerMatchContext match, CardEffect effect,
                                                     TriggerContext ctx) {
        TriggerContext.ManaAbilityResolved mana = (TriggerContext.ManaAbilityResolved) ctx;
        return queueEventAbility(match, effect,
                EventRecord.of(mana.activatingPlayerId()).withEventValue(mana.manaProduced()),
                "a mana ability resolving");
    }

    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_CONTROLLER_PROLIFERATES)
    private boolean handleProliferateDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on proliferate", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP)
    private boolean handleCoinFlipWonDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent permanent = match.permanent();

        enqueueCoinFlipTrigger(gameData, permanent, match.controllerId(), effect);
        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
        log.info("Game {} - {} triggers on winning a coin flip", gameData.id, permanent.getCard().getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_ANY_PLAYER_WINS_COIN_FLIP)
    private boolean handleAnyPlayerCoinFlipWonDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent permanent = match.permanent();

        enqueueCoinFlipTrigger(gameData, permanent, match.controllerId(), effect);
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
    private boolean handleCoinFlipLostDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent permanent = match.permanent();
        if (effect.targetSpec().declares(TargetPredicates.anyTarget())) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    permanent.getCard(), match.controllerId(), List.of(effect), permanent.getId(), null)
                    .chosenByAnOpponent());
        } else {
            enqueueCoinFlipTrigger(gameData, permanent, match.controllerId(), effect);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
        log.info("Game {} - {} triggers on losing a coin flip", gameData.id, permanent.getCard().getName());
        return true;
    }

    private void enqueueCoinFlipTrigger(GameData gameData, Permanent permanent, UUID controllerId,
            CardEffect effect) {
        if (effect.targetSpec().declares(TargetPredicates.anyTarget())) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    permanent.getCard(), controllerId, List.of(effect), permanent.getId(), controllerId));
            return;
        }
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                permanent.getCard(),
                controllerId,
                permanent.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                permanent.getId()
        ));
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleLandPutIntoGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (land put into graveyard)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_LAND_CARD_MILLED)
    private boolean handleLandCardMilledDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
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
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    private boolean handleCardsPutIntoLibraryDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        var source = match.permanent();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    private boolean handleCardPutIntoGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        List<Card> cards = switch (ctx) {
            case TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut -> cardsPut.cards();
            case TriggerContext.CardPutIntoGraveyard cardPut -> List.of(cardPut.card());
            case TriggerContext.PermanentCardPutIntoGraveyard permanentPut -> List.of(permanentPut.permanentCard());
            default -> List.of();
        };
        return queueGraveyardCardsAbility(match, effect, cards, null, null, null,
                "card put into controller's graveyard");
    }

    /** "Whenever a creature card is put into your graveyard from anywhere": {@link #queueGraveyardCardsAbility}. */
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD)
    private boolean handleCreatureCardPutIntoGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        List<Card> cards = ctx instanceof TriggerContext.CreatureCardPutIntoGraveyard creatureCard
                && creatureCard.creatureCard() != null ? List.of(creatureCard.creatureCard()) : List.of();
        return queueGraveyardCardsAbility(match, effect, cards, null, null, null,
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
    private boolean handleCardPutIntoPlayersGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        return queueGraveyardCardsAbility(match, effect, List.of(cardPut.card()), cardPut.graveyardOwnerId(),
                cardPut.graveyardOwnerId(), null, "card put into a player's graveyard from anywhere");
    }

    /**
     * "Whenever a creature card is put into an opponent's graveyard from their library" (Undead Alchemist): the card
     * recorded whatever the effects are ({@link #queueGraveyardCardsAbility}), its owner the non-targeting "that
     * player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_CREATURE_CARD_MILLED)
    private boolean handleOpponentCreatureCardMilledDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        return queueGraveyardCardsAbility(match, effect, List.of(cardPut.card()), cardPut.graveyardOwnerId(),
                cardPut.graveyardOwnerId(), null, "creature card milled");
    }

    /**
     * "Whenever an opponent mills one or more cards" (Lo and Li, Royal Advisors): the mill recorded whatever the
     * effects are ({@link #queueGraveyardCardsAbility}), the number of cards milled the event value and the milling
     * player the non-targeting "that player".
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_MILLS)
    private boolean handleOpponentMillsDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.Mill mill = (TriggerContext.Mill) ctx;
        return queueGraveyardCardsAbility(match, effect, List.of(), mill.milledPlayerId(), mill.milledPlayerId(),
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
    private boolean queueGraveyardCardsAbility(TriggerMatchContext match, CardEffect effect, List<Card> cards,
                                               UUID triggeringPlayerId, UUID thatPlayerId, Integer eventValue,
                                               String event) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        Card sourceCard = source != null ? source.getCard() : match.sourceCard();
        if (sourceCard == null) {
            return false;
        }
        if (source != null && (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), event, source.getId(),
                    new Permanent(source), eventValue));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
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
            entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
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
    private boolean handleCardPutIntoOpponentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
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
                new ArrayList<>(List.of(effect)),
                match.permanent() == null ? null : cardPut.graveyardOwnerId(),
                match.permanent() == null ? null : match.permanent().getId()
        );
        entry.setNonTargeting(effect.targetSpec().declaredTarget() == null);
        entry.setTriggeringPermanentControllerId(cardPut.graveyardOwnerId());
        entry.setTriggeringCard(cardPut.card().getId(), gameData.graveyardEntryVersion(cardPut.card().getId()));
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers (card put into opponent's graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCreatureCardPutIntoOpponentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
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
        entry.setNonTargeting(effect.targetSpec() == TargetSpec.NONE);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature card put into opponent's graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_NONLAND_CARDS_MILLED)
    private boolean handleNonlandCardsMilled(TriggerMatchContext match,
                                             CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.NonlandCardsMilled milled)) {
            return false;
        }

        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    "nonland cards milled", match.permanent().getId(), milled.nonlandCardCount()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId());
            entry.setEventValue(milled.nonlandCardCount());
            entry.setTriggeringPlayerId(milled.milledPlayerId());
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
            gameData.enqueueTrigger(entry);
        }

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on nonland cards being milled",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_SELF_BECOMES_SADDLED)
    private boolean handleBecomesSaddledDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        if (source == null) return false;

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                source.getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers on becoming saddled",
                gameData.id, source.getCard().getName());
        return true;
    }

    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_SELF_BECOMES_CREWED)
    private boolean handleBecomesCrewedDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        if (source == null) return false;

        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : source.getCard();
        boolean needsTarget = effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD);
        if (needsTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    "becomes crewed", source.getId(), new Permanent(source), null));
        } else {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    source.getId()));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on becoming crewed", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_SELF_BECOMES_STATIONED)
    private boolean handleBecomesStationed(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
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
                new ArrayList<>(List.of(effect)),
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
    private boolean handleSelfMutatesDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        Permanent source = match.permanent();
        if (source == null) return false;

        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : source.getCard();
        List<CardEffect> effects = new ArrayList<>(List.of(effect));
        int targetGroupIndex = sourceCard.getEffectTargetIndex(effect);
        if (targetGroupIndex >= 0) {
            List<CardEffect> groupedEffects = ActionAbilityReader.printedEffects(sourceCard, ActionWatch.ON_SELF_MUTATES).stream()
                    .filter(candidate -> sourceCard.getEffectTargetIndex(candidate) == targetGroupIndex)
                    .toList();
            if (!groupedEffects.isEmpty()) {
                if (groupedEffects.getFirst() != effect) return true;
                effects = new ArrayList<>(groupedEffects);
            }
        }
        boolean needsTarget = effects.stream().anyMatch(candidate ->
                candidate.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || candidate.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || candidate.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        if (needsTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), effects,
                    "mutation", source.getId()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    effects,
                    null,
                    source.getId());
            entry.setSourcePermanentSnapshot(new Permanent(source));
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on mutating",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_ALLY_CREATURE_CONNIVES)
    private boolean handleAllyCreatureConnivesDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureConnives connive = (TriggerContext.CreatureConnives) ctx;
        Permanent source = match.permanent();
        if (source == null || connive.connivingCreature() == null) return false;

        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : source.getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    private boolean handleAllyCreatureMutatesDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureMutates mutation = (TriggerContext.CreatureMutates) ctx;
        Permanent source = match.permanent();
        if (source == null || mutation.mutatedPermanent() == null) return false;

        Card sourceCard = match.sourceCard() != null ? match.sourceCard() : source.getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    private boolean handleSurveilDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent source = match.permanent();
        UUID surveilingPlayerId = ((TriggerContext.Surveil) ctx).surveilingPlayerId();
        boolean opponentSurveil = !match.controllerId().equals(surveilingPlayerId);
        if (!opponentSurveil && (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    source.getCard(),
                    match.controllerId(),
                    new ArrayList<>(List.of(effect)),
                    !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT),
                    null,
                    0,
                    source.getId(),
                    new Permanent(source)).withTriggeringPlayer(surveilingPlayerId));
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                opponentSurveil ? surveilingPlayerId : null,
                source.getId());
        if (opponentSurveil) {
            entry.setNonTargeting(true);
        }
        entry.setTriggeringPlayerId(surveilingPlayerId);
        entry.setSourcePermanentSnapshot(new Permanent(source));
        entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers on surveil", gameData.id, source.getCard().getName());
        return true;
    }

    @CollectsActionTriggers({
        @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_COMMITS_CRIME),
        @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_OPPONENT_COMMITS_CRIME)
    })
    private boolean handleCrimeDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent source = match.permanent();
        List<CardEffect> effects = new ArrayList<>(List.of(effect));
        boolean needsTarget = effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD);
        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    source.getCard(), match.controllerId(), effects));
        } else if (needsTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    source.getCard(), match.controllerId(), effects,
                    "crime", source.getId()));
        } else {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    match.controllerId(),
                    source.getCard().getName() + "'s ability",
                    effects,
                    null,
                    source.getId()));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers on a crime", gameData.id, source.getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD)
    boolean handleControllerCardsLeaveGraveyard(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!handleControllerArtifactOrCreatureCardsLeaveGraveyard(match, effect, ctx)) {
            return false;
        }

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards left graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_CARD_RETURNED_FROM_GRAVEYARD_TO_HAND)
    boolean handleControllerCardReturnedFromGraveyardToHand(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
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
                new ArrayList<>(List.of(effect)),
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
    boolean handleControllerCardPutIntoHandFromLibrary(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardPutIntoHandFromLibrary moved)) {
            return false;
        }
        return enqueueControllerCardPutIntoHandFromLibraryTrigger(match, effect, moved.card());
    }

    private boolean enqueueControllerCardPutIntoHandFromLibraryTrigger(
            TriggerMatchContext match, CardEffect effect, Card movedCard) {
        Card snapshot = movedCard.createCardCopy();
        snapshot.freeze();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    boolean handleControllerInstantOrSorceryCardLeavesGraveyard(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerInstantOrSorceryCardLeavesGraveyard)) {
            return false;
        }
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (instant or sorcery card left graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_FROM_LIBRARY_OR_GRAVEYARD)
    boolean handleControllerCardsExiledFromLibraryOrGraveyard(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsExiledFromLibraryOrGraveyard exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    boolean handleControllerCardsExiledFromGraveyard(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsExiledFromGraveyard exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    boolean handleControllerCreaturesOrCreatureCardsExiled(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCreaturesOrCreatureCardsExiled exiled)) {
            return false;
        }
        if (effect instanceof KayaSpiritsJusticeEffect) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DrawTriggerPermanentTarget(
                    match.permanent().getCard(),
                    match.controllerId(),
                    new ArrayList<>(List.of(effect)),
                    match.permanent().getId(),
                    new ControlledPermanentPredicateTargetFilter(
                            new PermanentIsTokenPredicate(), "Target must be a token you control"))
                    .withTriggeringCards(exiled.creatureCards()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} triggers (creatures or creature cards exiled) and needs a token target",
                    match.gameData().id, match.permanent().getCard().getName());
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(exiled.count());
        entry.setTriggeringCards(exiled.creatureCards());
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
    boolean handleAnyCreatureExiledFromBattlefield(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureExiledFromBattlefield exiled)) {
            return false;
        }
        Permanent source = match.permanent();
        Permanent lastKnown = exiled.exiledPermanent();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    source.getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    "creature exiled from the battlefield", source.getId(), new Permanent(source),
                    exiled.exiledPowerAtTrigger()).withTriggeringPermanent(lastKnown));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    match.controllerId(),
                    source.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
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
            entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
            match.gameData().enqueueTrigger(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers (creature exiled from battlefield)",
                match.gameData().id, source.getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_EXILED_DURING_TURN)
    boolean handleControllerCardsExiledDuringTurn(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardsExiledDuringTurn)) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (cards exiled during turn)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_OWNED_CARD_EXILED)
    boolean handleOpponentOwnedCardExiled(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.OpponentOwnedCardExiled exiled)
                || match.controllerId().equals(exiled.ownerId())) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    boolean handleGlobalPhaseOutOrCardExileTriggers(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.PermanentPhasedOut)
                && !(ctx instanceof TriggerContext.CardExiled)) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    boolean handleControllerExileTriggers(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsExiledFromHand)
                && !(ctx instanceof TriggerContext.ControllerSpellOrAbilityExilesPermanent)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    boolean handleControllerCreatureCardsLeaveGraveyard(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    boolean handleControllerArtifactOrCreatureCardsLeaveGraveyard(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsLeaveGraveyard cardsLeft)) {
            return false;
        }

        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    "leaves-the-graveyard", match.permanent().getId()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()
            );
            entry.setTriggeringCards(cardsLeft.cards());
            match.gameData().enqueueTrigger(entry);
        }
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_ARTIFACT_CARDS_LEAVE_GRAVEYARD)
    boolean handleControllerArtifactCardsLeaveGraveyard(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    "artifact-leaves-the-graveyard", match.permanent().getId()));
        } else {
            match.gameData().enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()
            ));
        }
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN)
    boolean handleCardsExiledFromGraveyardsOrBattlefieldDuringYourTurn(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardsExiledFromGraveyardsOrBattlefield exiled)) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
}

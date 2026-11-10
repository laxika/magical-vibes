package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileMilledCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringPermanentCardFromLibraryWithCroakCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.KayaSpiritsJusticeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trigger collectors for sacrifice, enchanted-permanent-tap, life-loss, and life-gain events.
 */
@Slf4j
@Service
public class MiscTriggerCollectorService {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final ETBTokenTargetService etbTokenTargetService;
    private PermanentRemovalService permanentRemovalService;

    public MiscTriggerCollectorService(GameLogService gameLogService,
                                       @Lazy GraveyardService graveyardService,
                                       GameQueryService gameQueryService,
                                       PredicateEvaluationService predicateEvaluationService,
                                       @Lazy DrawService drawService,
                                       AmountEvaluationService amountEvaluationService,
                                       ConditionEvaluationService conditionEvaluationService,
                                       @Lazy ETBTokenTargetService etbTokenTargetService,
                                       @Lazy PermanentRemovalService permanentRemovalService) {
        this.gameLogService = gameLogService;
        this.graveyardService = graveyardService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.drawService = drawService;
        this.amountEvaluationService = amountEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.permanentRemovalService = permanentRemovalService;
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

    // ── ON_ALLY_PERMANENT_SACRIFICED ───────────────────────────────────

    @CollectsWatchTrigger(value = MayEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED)
    private boolean handleOpponentNontokenPermanentSacrificeMay(TriggerMatchContext match,
                                                                 MayEffect may,
                                                                 TriggerContext ctx) {
        TriggerContext.OpponentNontokenPermanentSacrificed sacrifice =
                (TriggerContext.OpponentNontokenPermanentSacrificed) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                sacrifice.sacrificedCard().getId());
        return true;
    }

    // ── ON_ANY_CREATURE_SACRIFICED ─────────────────────────────────────

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ANY_CREATURE_SACRIFICED)
    private boolean handleAnyCreatureSacrificedMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        // "Whenever a player sacrifices a creature, you may put a +1/+1 counter on this creature"
        // (Thraximundar). The wrapped PutCountersOnSourceEffect resolves onto the source permanent,
        // and the "you may" is offered to the source's controller (not the sacrificing player).
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                null, match.permanent().getId());
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsWatchTrigger(value = PutCountersOnSourceEffect.class, watch = ZoneChangeWatch.ON_ANY_CREATURE_SACRIFICED)
    private boolean handleAnyCreatureSacrificedPutCounters(TriggerMatchContext match,
            PutCountersOnSourceEffect effect, TriggerContext ctx) {
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
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CREATURE_SACRIFICED)
    private boolean handleAnyCreatureSacrificedDefault(TriggerMatchContext match,
                                                        CardEffect effect, TriggerContext ctx) {
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
        return true;
    }

    @CollectsWatchTrigger(value = PerpetuallyBoostCardEffect.class,
            watch = ZoneChangeWatch.EXILE_ON_CONTROLLER_TOKEN_SACRIFICED)
    private boolean handleExileTokenSacrificePerpetualBoost(TriggerMatchContext match,
                                                             PerpetuallyBoostCardEffect effect,
                                                             TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.AllySacrificed sacrificed)
                || sacrificed.sacrificedCard() == null
                || !sacrificed.sacrificedCard().isToken()
                || match.sourceCard() == null) {
            return false;
        }

        Card sourceCard = match.sourceCard();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect))));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        return true;
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

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED)
    private boolean handleOpponentNontokenPermanentSacrificed(TriggerMatchContext match,
                                                               CardEffect effect,
                                                               TriggerContext ctx) {
        TriggerContext.OpponentNontokenPermanentSacrificed sacrificed =
                (TriggerContext.OpponentNontokenPermanentSacrificed) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setTriggeringCardId(sacrificed.sacrificedCard().getId());
        entry.setTriggeringCardGraveyardEntryVersion(
                match.gameData().graveyardEntryVersion(sacrificed.sacrificedCard().getId()));
        entry.setTriggeringCardExileEntryVersion(match.gameData().exileEntryVersions
                .getOrDefault(sacrificed.sacrificedCard().getId(), 0L));
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsWatchTrigger(value = StealDyingOpponentPermanentUnlessPaysLifeEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED)
    private boolean handleOpponentNontokenPermanentSacrificeSteal(
            TriggerMatchContext match, StealDyingOpponentPermanentUnlessPaysLifeEffect effect,
            TriggerContext ctx) {
        TriggerContext.OpponentNontokenPermanentSacrificed sacrificed =
                (TriggerContext.OpponentNontokenPermanentSacrificed) ctx;
        // "That card" is the entry's triggering card and "that player" the player who sacrificed it.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        UUID sacrificedCardId = sacrificed.sacrificedCard().getId();
        entry.setTriggeringCard(sacrificedCardId, match.gameData().graveyardEntryVersion(sacrificedCardId));
        entry.setTriggeringPermanentControllerId(sacrificed.sacrificingPlayerId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /**
     * "Whenever enchanted permanent becomes tapped": the ability is the Aura's (CR 113.7), and its entry records the
     * tapped permanent and its controller whatever its effects are ("destroy it", "its controller loses 2 life", "that
     * player mills three cards", read as the ability resolves, else from its last known information), the controller as
     * the non-targeting "that player" and the Aura as it was. A targeted ability's target is chosen as it goes on the
     * stack (CR 603.3d), the choice carrying the same record.
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
                    ept.tappedPermanentControllerId(),
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
     * "Whenever enchanted artifact becomes tapped, choose one —" (Relic Bind): the card's modal goes to the mode choice as
     * written, its mode and that mode's target chosen as the ability is put on the stack (CR 603.3c), with the tapped
     * permanent as the ability's triggering permanent.
     */
    @CollectsTapTrigger(value = ChooseOneEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapModal(TriggerMatchContext match,
            ChooseOneEffect modal, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                match.permanent().getCard(), match.controllerId(), modal, match.permanent().getId(),
                false, false, null, null, ept.tappedPermanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on enchanted permanent tap (modal)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsActionTrigger(value = CreateTokenEffect.class, watch = ActionWatch.ON_OPPONENT_PAYS_TAX)
    private boolean handleOpponentTaxPayment(TriggerMatchContext match,
            CreateTokenEffect effect, TriggerContext ctx) {
        TriggerContext.TaxPayment taxPayment = (TriggerContext.TaxPayment) ctx;
        var gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(taxPayment.manaPaid());
        }
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers when an opponent pays a tax ({})", gameData.id,
                match.permanent().getCard().getName(), taxPayment.manaPaid());
        return true;
    }

    @CollectsSpellAbilityTrigger(value = PutCountersOnSelfEffect.class, watch = SpellAbilityWatch.ON_SELF_MANA_ABILITY_RESOLVES)
    private boolean handleManaAbilityResolutionPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        TriggerContext.ManaAbilityResolved manaAbility = (TriggerContext.ManaAbilityResolved) ctx;
        var gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(manaAbility.manaProduced());
        }
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsMiscTrigger(value = CreateTokenEffect.class,
            watch = MiscWatch.ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT)
    private boolean handleOpponentGainsControlCreateToken(TriggerMatchContext match,
            CreateTokenEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers when an opponent gains control of a permanent",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsCounterTrigger(value = BoostSelfEffect.class, watch = CounterWatch.ON_CONTROLLER_GETS_ENERGY)
    private boolean handleEnergyGainBoostSelf(TriggerMatchContext match,
            BoostSelfEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on energy gain (self-boost)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsCounterTrigger(value = CardEffect.class, watch = CounterWatch.ON_CONTROLLER_GETS_ENERGY)
    private boolean handleEnergyGainDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        List<CardEffect> effects = new ArrayList<>(List.of(effect));
        boolean needsTarget = effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD);
        if (needsTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), effects,
                    "energy gain", match.permanent().getId()));
        } else {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    effects,
                    null,
                    match.permanent().getId()));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on energy gain", gameData.id, sourceCard.getName());
        return true;
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

    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_ARTIFACT_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    private boolean handleCardPutIntoGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        Card triggeringCard = ctx instanceof TriggerContext.CardPutIntoGraveyard cardPut
                ? cardPut.card()
                : ctx instanceof TriggerContext.PermanentCardPutIntoGraveyard permanentPut
                ? permanentPut.permanentCard() : null;

        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    "card put into the graveyard", match.permanent().getId()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    cardName + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()
            );
            if (ctx instanceof TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut) {
                recordCardsPutIntoGraveyard(gameData, entry, cardsPut.cards());
            } else if (triggeringCard != null) {
                recordCardsPutIntoGraveyard(gameData, entry, List.of(triggeringCard));
            }
            gameData.enqueueTrigger(entry);
        }

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (card put into controller's graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = LoseLifeEffect.class,
            watch = ZoneChangeWatch.ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleNonblackCardPutIntoGraveyardLoseLife(TriggerMatchContext match,
            LoseLifeEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardPutIntoGraveyard cardPut)
                || effect.recipient() != LoseLifeRecipient.TARGET_PLAYER) {
            return false;
        }

        Card sourceCard = match.sourceCard();
        if (sourceCard == null) {
            return false;
        }
        UUID sourcePermanentId = match.permanent() == null ? null : match.permanent().getId();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                cardPut.graveyardOwnerId(),
                sourcePermanentId);
        entry.setNonTargeting(true);
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
        }
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers (nonblack card put into a graveyard from anywhere)",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = ExileTriggeringPermanentCardFromLibraryWithCroakCounterEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    private boolean handleExileTriggeringPermanentCardFromLibraryWithCroakCounter(
            TriggerMatchContext match, ExileTriggeringPermanentCardFromLibraryWithCroakCounterEffect effect,
            TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.PermanentCardPutIntoGraveyard cardPut)) {
            return false;
        }

        Card triggeringCard = cardPut.permanentCard();
        var gameData = match.gameData();
        List<Card> graveyard = gameData.playerGraveyards.get(cardPut.graveyardOwnerId());
        if (graveyard == null || graveyard.stream().noneMatch(card -> card.getId().equals(triggeringCard.getId()))) {
            return false;
        }

        String cardName = match.permanent().getCard().getName();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(triggeringCard.getId());
        entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(triggeringCard.getId()));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to exile {} with a croak counter", gameData.id, cardName,
                triggeringCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = CardEffect.class,
                    watch = ZoneChangeWatch.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD)
    private boolean handleCreatureCardPutIntoGraveyardDefault(TriggerMatchContext match,
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
        // The source as the ability goes on the stack, whatever the effect is (CR 608.2h, 701.27f).
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature card put into graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    @CollectsWatchTrigger(value = RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    private boolean handleDelayedReturnMatchingCardFromGraveyard(TriggerMatchContext match,
            RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect effect, TriggerContext ctx) {
        if (ctx instanceof TriggerContext.CardPutIntoGraveyard cardPut) {
            if (cardPut.sourceZone() != Zone.HAND
                    || !predicateEvaluationService.matchesCardPredicate(
                    cardPut.card(), effect.filter(), match.permanent().getCard().getId(),
                    match.gameData(), cardPut.graveyardOwnerId())) {
                return false;
            }
            enqueueDelayedReturnMatchingCard(match, effect, cardPut.card());
            return true;
        }

        if (!(ctx instanceof TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut)) {
            return false;
        }

        boolean triggered = false;
        for (Card card : cardsPut.cards()) {
            if (effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                    card, effect.filter(), match.permanent().getCard().getId(),
                    match.gameData(), cardsPut.graveyardOwnerId())) {
                continue;
            }
            enqueueDelayedReturnMatchingCard(match, effect, card);
            triggered = true;
        }
        return triggered;
    }

    private void enqueueDelayedReturnMatchingCard(TriggerMatchContext match,
            RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect effect, Card card) {
        // One ability for each card ("that card"), which is its stack entry's triggering card.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setTriggeringCard(card.getId(), match.gameData().graveyardEntryVersion(card.getId()));
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
    }

    @CollectsWatchTrigger(value = PerpetuallyGrantTriggeredAbilityEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)
    private boolean handlePerpetualTriggeredAbilityGrant(TriggerMatchContext match,
            PerpetuallyGrantTriggeredAbilityEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureCardPutIntoGraveyard creatureCard)
                || (creatureCard.sourceZone() != Zone.HAND
                && creatureCard.sourceZone() != Zone.LIBRARY)) {
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
        entry.setTriggeringCardId(creatureCard.creatureCard().getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to perpetually grant an ability to {}",
                match.gameData().id, match.permanent().getCard().getName(),
                creatureCard.creatureCard().getName());
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

    // ── ON_OPPONENT_CREATURE_CARD_MILLED ────────────────────────────────

    @CollectsWatchTrigger(value = ExileMilledCreatureAndCreateTokenEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_CREATURE_CARD_MILLED)
    private boolean handleExileMilledCreatureAndCreateToken(TriggerMatchContext match,
            ExileMilledCreatureAndCreateTokenEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureCardMilled milled = (TriggerContext.CreatureCardMilled) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)));
        entry.setTriggeringCardId(milled.milledCard().getId());
        entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(milled.milledCard().getId()));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on creature card milled", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = PutCounterOnEachControlledPermanentEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_MILLS)
    private boolean handlePutCountersOnOpponentMill(TriggerMatchContext match,
            PutCounterOnEachControlledPermanentEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on opponent mill (put counters on matching permanents)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = GainLifeEffect.class, watch = ZoneChangeWatch.ON_NONLAND_CARDS_MILLED)
    private boolean handleGainLifeOnNonlandCardsMilled(TriggerMatchContext match,
            GainLifeEffect effect, TriggerContext ctx) {
        int nonlandCardCount = ((TriggerContext.NonlandCardsMilled) ctx).nonlandCardCount();
        Card sourceCard = match.permanent().getCard();

        for (int i = 0; i < nonlandCardCount; i++) {
            match.gameData().enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        }

        log.info("Game {} - {} triggers once for each of {} nonland cards milled",
                match.gameData().id, sourceCard.getName(), nonlandCardCount);
        return nonlandCardCount > 0;
    }

    @CollectsWatchTrigger(value = SequenceEffect.class,
            watch = ZoneChangeWatch.ON_NONLAND_CARDS_MILLED)
    private boolean handleNonlandCardsMilledSequence(TriggerMatchContext match,
            SequenceEffect effect, TriggerContext ctx) {
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on nonland cards milled",
                match.gameData().id, match.permanent().getCard().getName());
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
            entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
            gameData.enqueueTrigger(entry);
        }

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on nonland cards being milled",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsActionTrigger(value = BoostSelfEffect.class, watch = ActionWatch.ON_CONTROLLER_FORETELLS)
    private boolean handleForetellBoostSelf(TriggerMatchContext match,
            BoostSelfEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on foretell (self-boost)",
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

    // ── ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD ────────────────────────────

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

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD)
    boolean handleAnyCreatureExiledFromBattlefield(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureExiledFromBattlefield exiled)) {
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
        entry.setTriggeringPermanentId(exiled.exiledPermanent().getId());
        entry.setTriggeringPermanentControllerId(exiled.exiledControllerId());
        entry.setTriggeringPermanentPowerAtTrigger(exiled.exiledPowerAtTrigger());
        entry.setEventValue(exiled.exiledPowerAtTrigger());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature exiled from battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = ReturnTriggeringPermanentToBattlefieldEffect.class,
            watch = ZoneChangeWatch.ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD)
    boolean handleAnyCreatureExiledReturnTriggeringPermanent(TriggerMatchContext match,
            ReturnTriggeringPermanentToBattlefieldEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureExiledFromBattlefield exiled)
                || effect.fromZone() != Zone.EXILE
                || exiled.exiledPermanent() == null
                || exiled.exiledPermanent().getCard() == null) {
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
        entry.setTriggeringCardId(exiled.exiledPermanent().getCard().getId());
        entry.setTriggeringCardExileEntryVersion(match.gameData().exileEntryVersions
                .getOrDefault(exiled.exiledPermanent().getCard().getId(), 0L));
        entry.setTriggeringPermanentId(exiled.exiledPermanent().getId());
        entry.setTriggeringPermanentControllerId(exiled.exiledControllerId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature exiled from battlefield, returning it)",
                match.gameData().id, match.permanent().getCard().getName());
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

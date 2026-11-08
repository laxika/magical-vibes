package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SourcePermanentSnapshotRequiredEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CradleOfVitalityLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToLifeGainedEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileForEachLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileMilledCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsExiledWithSourceOnUntapEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringPermanentCardFromLibraryWithCroakCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.MillOpponentOnLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.NykthosParagonLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaDrawXCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect;
import com.github.laxika.magicalvibes.model.effect.RelicBindTapEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.KayaSpiritsJusticeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnSpellLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
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
import java.util.Objects;
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

    @CollectsMiscTrigger(value = PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect.class,
            watch = MiscWatch.ON_ALLY_CREATURE_CREWS_VEHICLE)
    private boolean handlePerpetualVehicleBoostOnCrew(
            TriggerMatchContext match, PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect effect,
            TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureCrewsVehicle crew)) {
            return false;
        }
        FilterContext filterContext = FilterContext.of(match.gameData())
                .withSourceCardId(match.permanent().getCard().getId())
                .withSourceControllerId(match.controllerId())
                .withSourcePermanentId(match.permanent().getId())
                .withSourcePermanentSnapshot(match.permanent());
        if (!predicateEvaluationService.matchesPermanentPredicate(
                crew.crewingCreature(), effect.crewingCreaturePredicate(), filterContext)) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new PerpetuallyBoostCardEffect(
                        crew.vehicle().getCard(), effect.powerBoost(), effect.toughnessBoost()))),
                null,
                match.permanent().getId());
        entry.setTriggeringPermanentId(crew.vehicle().getId());
        entry.setNonTargeting(true);
        match.gameData().pendingActivatedAbilityCostTriggers.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

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
        List<CardEffect> triggeredEffects = cardEffects.stream()
                .map(triggeredEffect -> triggeredEffect instanceof ConditionalEffect gate
                        ? (conditionEvaluationService.isMet(match.gameData(), gate.condition(), context)
                                ? gate.wrapped() : null)
                        : triggeredEffect)
                .filter(Objects::nonNull)
                .filter(triggeredEffect -> !(triggeredEffect instanceof
                        com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect)
                        || match.permanent().getChosenPermanentId() != null)
                .map(triggeredEffect -> triggeredEffect instanceof com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect remove
                        ? new com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect(
                                remove.mode(), match.permanent().getChosenPermanentId(), null)
                        : triggeredEffect instanceof ReturnCardsExiledWithSourceOnUntapEffect
                        ? new ReturnCardsExiledWithSourceOnUntapEffect(
                                match.gameData().exileReturnOnPermanentLeave
                                        .getOrDefault(match.permanent().getId(), List.of()).stream()
                                        .map(pending -> pending.card().getId())
                                        .collect(java.util.stream.Collectors.toSet()))
                        : triggeredEffect)
                .toList();
        if (triggeredEffects.isEmpty()) return true;

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
            match.gameData().enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    su.controllerId(),
                    card.getName() + "'s ability",
                    new ArrayList<>(triggeredEffects),
                    null,
                    match.permanent().getId()));
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(card));
        log.info("Game {} - {} triggers on becoming untapped", match.gameData().id, card.getName());
        return true;
    }

    @CollectsTapTrigger(value = PutCountersOnSelfEffect.class, watch = TapWatch.ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP)
    private boolean handleUntapStepPutCountersOnSelf(TriggerMatchContext match,
                                                       PutCountersOnSelfEffect effect, TriggerContext ctx) {
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
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(untapStep.untappedPermanentCount());
        }
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

    // ── ON_ENCHANTED_PERMANENT_TAPPED ──────────────────────────────────

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

    @CollectsTapTrigger(value = GivePoisonCountersEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapPoison(TriggerMatchContext match,
            GivePoisonCountersEffect e, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        GivePoisonCountersEffect resolved = new GivePoisonCountersEffect(
                e.amount(), PoisonRecipient.TARGET_PERMANENT_CONTROLLER);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(resolved)),
                ept.tappedPermanent().getId(),
                match.permanent().getId()
        );
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentId(ept.tappedPermanent().getId());
        entry.setTriggeringPermanentControllerId(ept.tappedPermanentControllerId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on enchanted permanent tap ({})",
                match.gameData().id, match.permanent().getCard().getName(),
                ept.tappedPermanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = DestroyReferencedPermanentEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapDestroy(TriggerMatchContext match,
            DestroyReferencedPermanentEffect e, TriggerContext ctx) {
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                null,
                match.permanent().getId()
        );
        trigger.setTriggeringPermanentId(((TriggerContext.EnchantedPermanentTap) ctx).tappedPermanent().getId());
        trigger.setNonTargeting(true);
        match.gameData().enqueueTrigger(trigger);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to destroy enchanted permanent",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = RemoveCounterFromSourceEffect.class,
            watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapRemoveCounter(TriggerMatchContext match,
            RemoveCounterFromSourceEffect e, TriggerContext ctx) {
        // The removal acts on the source Aura, so the trigger only needs to carry the Aura as its source permanent.
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to remove a counter from itself",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = PutCounterOnReferencedPermanentEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapCounter(TriggerMatchContext match,
            PutCounterOnReferencedPermanentEffect e, TriggerContext ctx) {
        // The tapped creature is recorded as the triggering permanent, so a referenced counter
        // still lands on it if the Aura has moved by the time the trigger resolves.
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                null,
                match.permanent().getId()
        );
        entry.setTriggeringPermanentId(ept.tappedPermanent().getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to put a counter on enchanted creature",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = DealDamageToPlayersEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapDamage(TriggerMatchContext match,
            DealDamageToPlayersEffect e, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                ept.tappedPermanentControllerId(),
                match.permanent().getId()
        );
        entry.setTriggeringPermanentId(ept.tappedPermanent().getId());
        entry.setTriggeringPermanentControllerId(ept.tappedPermanentControllerId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to damage enchanted permanent's controller",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = ForcedCostOrElseEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapForcedCost(TriggerMatchContext match,
            ForcedCostOrElseEffect e, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        // Seizures: enchanted controller may pay or take damage. The tapped permanent is carried as the
        // triggering permanent so its controller is read again when the ability resolves (CR 608.2h).
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                ept.tappedPermanentControllerId(),
                match.permanent().getId()
        );
        entry.setTriggeringPermanentId(ept.tappedPermanent().getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (pay-or-penalty) on enchanted permanent tap",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = LoseLifeEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapLoseLife(TriggerMatchContext match,
            LoseLifeEffect e, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        // TARGET_PERMANENT_CONTROLLER re-derives the losing player from the entry's targetId
        // permanent; bake it to the tapped land so its controller loses life. Life loss (not
        // damage, CR 118.2) — fires "loses life" triggers via LifeSupport.
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                ept.tappedPermanent().getId(),
                match.permanent().getId());
        trigger.setNonTargeting(true);
        trigger.setTriggeringPermanentId(ept.tappedPermanent().getId());
        trigger.setTriggeringPermanentControllerId(match.gameData().findControllerOf(ept.tappedPermanent()));
        match.gameData().enqueueTrigger(trigger);
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers.";
        gameLogService.append(match.gameData(), GameLog.text(triggerLog));
        log.info("Game {} - {} triggers, enchanted permanent's controller loses life",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = MillEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapMill(TriggerMatchContext match,
            MillEffect e, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        // Chronic Flooding: "its controller mills three cards". TARGET_PLAYER reads the entry's
        // targetId, so bake the tapped permanent's controller — no target is chosen.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                ept.tappedPermanentControllerId(),
                match.permanent().getId()
        );
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers, enchanted permanent's controller mills",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = RelicBindTapEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapRelicBind(TriggerMatchContext match,
            RelicBindTapEffect e, TriggerContext ctx) {
        var choice = new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect(List.of(
                new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption(
                        com.github.laxika.magicalvibes.model.ChoiceContext.RelicBindModeChoice.DAMAGE,
                        new com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption(
                        com.github.laxika.magicalvibes.model.ChoiceContext.RelicBindModeChoice.LIFE,
                        new com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect(1))));
        match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                match.permanent().getCard(), match.controllerId(), choice, match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on enchanted permanent tap (modal)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = DrawCardEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapDraw(TriggerMatchContext match,
            DrawCardEffect e, TriggerContext ctx) {
        // Betrayal: controller draws. DrawCardEffect resolves for the stack entry's controller.
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(e)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to draw on enchanted permanent tap",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTapTrigger(value = BoostTargetCreatureEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapBoostTarget(TriggerMatchContext match,
            BoostTargetCreatureEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                "fortified permanent became tapped", match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        return true;
    }

    // ── ON_OPPONENT_LOSES_LIFE ─────────────────────────────────────────

    /**
     * "Whenever one or more opponents lose life, you may cast target instant or sorcery card from your graveyard. Do
     * this only once each turn" (Emet-Selch of the Third Seat): the collector judges the limit, and accepting the cast
     * marks it (CR 603.2h).
     */
    @CollectsLifeTrigger(value = CastTargetInstantOrSorceryFromGraveyardEffect.class,
            watch = LifeWatch.ON_OPPONENT_LOSES_LIFE)
    private boolean handleOpponentLifeLossCastFromGraveyard(TriggerMatchContext match,
            CastTargetInstantOrSorceryFromGraveyardEffect effect, TriggerContext ctx) {
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                match.permanent().getCard(), match.controllerId(), List.of(effect),
                null, 1, 0, 1, null, false, null, match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsLifeTrigger(value = MillOpponentOnLifeLossEffect.class, watch = LifeWatch.ON_OPPONENT_LOSES_LIFE)
    @CollectsLifeTrigger(value = MillOpponentOnLifeLossEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleMillOnLifeLoss(TriggerMatchContext match,
            MillOpponentOnLifeLossEffect trigger, TriggerContext ctx) {
        TriggerContext.LifeLoss ll = (TriggerContext.LifeLoss) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        String playerName = gameData.playerIdToName.get(ll.losingPlayerId());
        int amount = ll.lifeLostAmount();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(new MillEffect(amount, MillRecipient.TARGET_PLAYER))),
                ll.losingPlayerId(),
                match.permanent().getId());
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.cardThen(match.permanent().getCard(),
                " triggers — " + playerName + " mills " + amount + " card" + (amount != 1 ? "s" : "") + "."));
        log.info("Game {} - {} triggers on life loss, {} will mill {} cards",
                gameData.id, cardName, playerName, amount);
        return true;
    }

    /**
     * "Whenever an opponent loses life, you gain that much life" (Exquisite Blood). The trigger goes
     * on the stack; the life lost is snapshotted onto the entry's event value so an
     * {@code EventValue()} amount reads it back at resolution.
     */
    @CollectsLifeTrigger(value = GainLifeEffect.class, watch = LifeWatch.ON_OPPONENT_LOSES_LIFE)
    private boolean handleGainLifeOnOpponentLifeLoss(TriggerMatchContext match,
            GainLifeEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss ll = (TriggerContext.LifeLoss) ctx;
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
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(ll.lifeLostAmount());
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on opponent life loss ({} life), controller gains that much",
                gameData.id, cardName, ll.lifeLostAmount());
        return true;
    }

    @CollectsLifeTrigger(value = SequenceEffect.class, watch = LifeWatch.ON_OPPONENT_LOSES_LIFE)
    private boolean handleSequenceOnOpponentLifeLoss(TriggerMatchContext match,
            SequenceEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss lifeLoss = (TriggerContext.LifeLoss) ctx;
        if (lifeLoss.lifeLostAmount() != 1) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on exactly one life lost by an opponent",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    /**
     * "Whenever an opponent loses life": the ability goes on the stack with the life lost as the event's value (Kefka,
     * Ruler of Ruin's "you draw that many cards"). Its event condition ("during your turn") and intervening "if" were
     * judged as the collector read the ability; an intervening "if" stays around the effect, checked again as it
     * resolves (CR 603.4).
     */
    @CollectsLifeTrigger(value = CardEffect.class, watch = LifeWatch.ON_OPPONENT_LOSES_LIFE)
    private boolean handleOnOpponentLifeLoss(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss lifeLoss = (TriggerContext.LifeLoss) ctx;
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(lifeLoss.lifeLostAmount());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on opponent life loss", match.gameData().id, sourceCard.getName());
        return true;
    }

    /**
     * "Whenever an opponent gains life": the ability goes on the stack with the opponent as "that player" and the life
     * gained as the event's value. Its event condition ("during their turn", Wedding Ring) and intervening "if" were
     * judged as the collector read the ability; an intervening "if" stays around the effect, checked again as it
     * resolves (CR 603.4).
     */
    @CollectsLifeTrigger(value = CardEffect.class, watch = LifeWatch.ON_OPPONENT_GAINS_LIFE)
    private boolean handleOnOpponentLifeGain(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lifeGain = (TriggerContext.LifeGain) ctx;
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                lifeGain.gainingPlayerId(),
                match.permanent().getId());
        entry.setEventValue(lifeGain.lifeGainedAmount());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on opponent life gain", match.gameData().id, sourceCard.getName());
        return true;
    }

    // ── ON_CONTROLLER_GAINS_LIFE ────────────────────────────────────────

    @CollectsLifeTrigger(value = SequenceEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleSequenceOnControllerLifeLoss(TriggerMatchContext match,
            SequenceEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss lifeLoss = (TriggerContext.LifeLoss) ctx;
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(lifeLoss.lifeLostAmount());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on controller life loss ({} life)",
                match.gameData().id, sourceCard.getName(), lifeLoss.lifeLostAmount());
        return true;
    }

    @CollectsLifeTrigger(value = PutCountersOnSelfEffect.class, watch = LifeWatch.ON_CONTROLLER_PAYS_LIFE)
    private boolean handleLifePaymentPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        TriggerContext.LifePayment lifePayment = (TriggerContext.LifePayment) ctx;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(lifePayment.lifePaidAmount());
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life payment (put counter on self)", gameData.id, cardName);
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

    /**
     * "Whenever you lose life, you gain that much life". The trigger goes on the stack; the life
     * lost is snapshotted onto the entry's event value so an {@code EventValue()} amount reads it
     * back at resolution.
     */
    @CollectsLifeTrigger(value = GainLifeEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleGainLifeOnControllerLifeLoss(TriggerMatchContext match,
            GainLifeEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss ll = (TriggerContext.LifeLoss) ctx;
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
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(ll.lifeLostAmount());
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on controller life loss ({} life), controller gains life",
                gameData.id, cardName, ll.lifeLostAmount());
        return true;
    }

    @CollectsLifeTrigger(value = PutCountersOnSourceEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainPutCounters(TriggerMatchContext match,
            PutCountersOnSourceEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on life gain", gameData.id, cardName);
        return true;
    }

    @CollectsLifeTrigger(value = GrantKeywordEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainGrantKeyword(TriggerMatchContext match,
            GrantKeywordEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();

        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                    sourceCard,
                    match.controllerId(),
                    List.of(effect),
                    match.permanent().getId()));
        } else {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()));
        }

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life gain (keyword grant)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLifeTrigger(value = SequenceEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainSequence(TriggerMatchContext match,
            SequenceEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                    sourceCard, match.controllerId(), List.of(effect), match.permanent().getId()));
        } else {
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                    sourceCard, match.controllerId(), sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)), null, match.permanent().getId());
            entry.setEventValue(((TriggerContext.LifeGain) ctx).lifeGainedAmount());
            match.gameData().enqueueTrigger(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        return true;
    }

    /**
     * A "whenever you gain life" / "whenever you lose life" ability with an intervening "if" (CR 603.4), which the
     * collector judged from the ability as it triggered and the reader wrapped around the effect: the ability goes on
     * the stack with the conditional as it is, checked again as it resolves, and the life gained or lost as the
     * event's value.
     */
    @CollectsLifeTrigger(value = ConditionalEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    @CollectsLifeTrigger(value = ConditionalEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleConditionalOnControllerLifeChange(TriggerMatchContext match,
            ConditionalEffect conditional, TriggerContext ctx) {
        int amount = switch (ctx) {
            case TriggerContext.LifeGain lifeGain -> lifeGain.lifeGainedAmount();
            case TriggerContext.LifeLoss lifeLoss -> lifeLoss.lifeLostAmount();
            default -> throw new IllegalArgumentException("Not a life gain or loss: " + ctx);
        };
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(conditional)),
                null,
                match.permanent().getId());
        entry.setEventValue(amount);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on controller life change", match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsLifeTrigger(value = PutCounterOnTargetPermanentEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainPutCounterOnTarget(TriggerMatchContext match,
            PutCounterOnTargetPermanentEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        int lifeGained = ((TriggerContext.LifeGain) ctx).lifeGainedAmount();

        if (sourceCard.getTargetGroups().stream()
                .anyMatch(target -> target.getDynamicMaxTargets() != null)) {
            gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                    sourceCard,
                    match.controllerId(),
                    List.of(effect),
                    match.permanent().getId(),
                    List.of(),
                    0,
                    0,
                    List.of(),
                    0,
                    List.of(),
                    false,
                    null,
                    null,
                    lifeGained));

            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers on life gain (multi-target counter placement)",
                    gameData.id, sourceCard.getName());
            return true;
        }

        gameData.queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                sourceCard,
                match.controllerId(),
                List.of(effect),
                match.permanent().getId(),
                false,
                lifeGained));

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life gain (put counter on target)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLifeTrigger(value = SurveilEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainSurveil(TriggerMatchContext match,
            SurveilEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on life gain (surveil {})", gameData.id, cardName, effect.count());
        return true;
    }

    @CollectsLifeTrigger(value = CreateTokenEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainCreateToken(TriggerMatchContext match,
            CreateTokenEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on life gain (create token)", gameData.id, cardName);
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

    @CollectsLifeTrigger(value = PutCountersOnSelfEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    @CollectsLifeTriggers({
            @CollectsLifeTrigger(value = PutCountersOnSelfEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE),
            @CollectsLifeTrigger(value = PutCountersOnSelfEffect.class, watch = LifeWatch.ON_OPPONENT_GAINS_LIFE)
    })
    private boolean handleLifeGainPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        TriggerContext.LifeGain lifeGain = (TriggerContext.LifeGain) ctx;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(((TriggerContext.LifeGain) ctx).lifeGainedAmount());
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain (put counter on self)", gameData.id, cardName);
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

    @CollectsLifeTriggers({
            @CollectsLifeTrigger(value = BoostSelfEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE),
            @CollectsLifeTrigger(value = BoostSelfEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    })
    private boolean handleLifeChangeBoostSelf(TriggerMatchContext match,
            BoostSelfEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        if (ctx instanceof TriggerContext.LifeGain lifeGain) {
            entry.setEventValue(lifeGain.lifeGainedAmount());
        } else if (ctx instanceof TriggerContext.LifeLoss lifeLoss) {
            entry.setEventValue(lifeLoss.lifeLostAmount());
        }
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
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

    @CollectsLifeTrigger(value = UntapPermanentsEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainUntapSelf(TriggerMatchContext match,
            UntapPermanentsEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on life gain (untap self)", gameData.id, cardName);
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

    @CollectsTapTrigger(value = SequenceEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapSequence(TriggerMatchContext match,
            SequenceEffect sequence, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(sequence)),
                ept.tappedPermanentControllerId(),
                match.permanent().getId());
        entry.setTriggeringPermanentId(ept.tappedPermanent().getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on enchanted permanent tap (sequence)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP)
    private boolean handleCoinFlipWonDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent permanent = match.permanent();

        enqueueCoinFlipTrigger(gameData, permanent, match.controllerId(), match.controllerId(), effect);
        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
        log.info("Game {} - {} triggers on winning a coin flip", gameData.id, permanent.getCard().getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_ANY_PLAYER_WINS_COIN_FLIP)
    private boolean handleAnyPlayerCoinFlipWonDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent permanent = match.permanent();

        enqueueCoinFlipTrigger(gameData, permanent, match.controllerId(), match.controllerId(), effect);
        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
        log.info("Game {} - {} triggers when any player wins a coin flip", gameData.id,
                permanent.getCard().getName());
        return true;
    }

    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_CONTROLLER_LOSES_COIN_FLIP)
    private boolean handleCoinFlipLostDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent permanent = match.permanent();
        UUID choosingPlayerId = gameQueryService.getOpponentId(gameData, match.controllerId());
        if (choosingPlayerId == null) {
            return true;
        }

        enqueueCoinFlipTrigger(gameData, permanent, match.controllerId(), choosingPlayerId, effect);
        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
        log.info("Game {} - {} triggers on losing a coin flip", gameData.id, permanent.getCard().getName());
        return true;
    }

    private void enqueueCoinFlipTrigger(GameData gameData, Permanent permanent, UUID controllerId,
            UUID choosingPlayerId, CardEffect effect) {
        if (effect.targetSpec().declares(TargetPredicates.anyTarget())) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    permanent.getCard(), controllerId, List.of(effect), permanent.getId(), choosingPlayerId));
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

    @CollectsWatchTrigger(value = ReturnTriggeringLandFromGraveyardToBattlefieldEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    private boolean handleMilledLandsReturn(TriggerMatchContext match,
            ReturnTriggeringLandFromGraveyardToBattlefieldEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut)) {
            return false;
        }

        List<Card> lands = cardsPut.cards().stream()
                .filter(card -> !card.isToken() && card.hasType(CardType.LAND))
                .toList();
        if (lands.isEmpty()) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        // "Them" are the entry's event cards, as the graveyard objects the event put there (CR 400.7).
        entry.setEventCardIds(lands.stream().map(Card::getId).toList());
        for (Card land : lands) {
            entry.getEventCardGraveyardEntryVersions().put(land.getId(),
                    match.gameData().graveyardEntryVersion(land.getId()));
        }
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers for lands put into the graveyard from the library",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = ReturnCardFromGraveyardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY)
    private boolean handleEventCardsGraveyardReturn(TriggerMatchContext match,
            ReturnCardFromGraveyardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut)
                || cardsPut.cards().isEmpty()) {
            return false;
        }
        if (effect.filter() != null && cardsPut.cards().stream().noneMatch(card ->
                predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), null, match.gameData(), cardsPut.graveyardOwnerId()))) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventCardIds(cardsPut.cards().stream().map(Card::getId).toList());
        for (Card card : cardsPut.cards()) {
            entry.getEventCardGraveyardEntryVersions().put(card.getId(),
                    match.gameData().graveyardEntryVersion(card.getId()));
        }
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers for a card put into the graveyard from the library",
                match.gameData().id, sourceCard.getName());
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
            if (triggeringCard != null) {
                // "That card" is the graveyard object the card became (CR 400.7).
                entry.setTriggeringCard(triggeringCard.getId(), gameData.graveyardEntryVersion(triggeringCard.getId()));
            }
            if (ctx instanceof TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut) {
                entry.setTriggeringCards(cardsPut.cards());
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
        if (SourcePermanentSnapshotRequiredEffect.isRequiredBy(effect)) {
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature card put into graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsLifeTrigger(value = EnergyCountersEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainEnergyCounters(TriggerMatchContext match,
            EnergyCountersEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                sourceCard, match.controllerId(), sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)), null, match.permanent().getId());
        entry.setEventValue(((TriggerContext.LifeGain) ctx).lifeGainedAmount());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
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

    @CollectsWatchTrigger(value = ConditionalEffect.class,
            watch = ZoneChangeWatch.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleConditionalCardPutIntoOpponentGraveyard(TriggerMatchContext match,
            ConditionalEffect conditional, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CardPutIntoGraveyard cardPut)) {
            return false;
        }

        Card sourceCard = match.permanent() != null ? match.permanent().getCard() : match.sourceCard();
        if (sourceCard == null) return false;
        ConditionContext conditionContext = match.permanent() != null
                ? ConditionContext.forPermanent(match.permanent(), match.controllerId())
                : ConditionContext.forCard(sourceCard, match.controllerId());
        if (!conditionEvaluationService.isMet(match.gameData(), conditional.condition(), conditionContext)) {
            return false;
        }

        var gameData = match.gameData();
        String cardName = sourceCard.getName();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(conditional)),
                match.permanent() == null ? null : cardPut.graveyardOwnerId(),
                match.permanent() == null ? null : match.permanent().getId()
        );
        entry.setNonTargeting(conditional.targetSpec().declaredTarget() == null);
        entry.setTriggeringPermanentControllerId(cardPut.graveyardOwnerId());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers (card put into opponent's graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCardPutIntoOpponentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CardPutIntoGraveyard cardPut = (TriggerContext.CardPutIntoGraveyard) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                cardPut.graveyardOwnerId(),
                match.permanent().getId()
        );
        // "That card" is the graveyard object the card became (CR 400.7).
        entry.setTriggeringCard(cardPut.card().getId(), gameData.graveyardEntryVersion(cardPut.card().getId()));
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
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
            // "That card", as it was put into the graveyard: the ability doesn't look for it there again.
            entry.setTriggeringCardId(creatureCard.creatureCard().getId());
            entry.setTriggeringCards(List.of(creatureCard.creatureCard()));
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (creature card put into opponent's graveyard from anywhere)", gameData.id, cardName);
        return true;
    }

    @CollectsWatchTrigger(value = MayPayLifeEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    private boolean handleCreatureCardPutIntoOpponentGraveyardMayPayLife(TriggerMatchContext match,
            MayPayLifeEffect mayPay, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureCardPutIntoGraveyard creatureCard)) {
            return false;
        }

        Card triggeringCard = creatureCard.creatureCard();
        MayPayLifeEffect resolved = new MayPayLifeEffect(
                triggeringCard.getManaValue(), mayPay.wrapped(), mayPay.prompt(), mayPay.payer(), mayPay.elseEffect());
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolved)),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(triggeringCard.getId());
        entry.setTriggeringCardGraveyardEntryVersion(
                match.gameData().graveyardEntryVersion(triggeringCard.getId()));
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers to return {} by paying {} life", match.gameData().id,
                match.permanent().getCard().getName(), triggeringCard.getName(), triggeringCard.getManaValue());
        return true;
    }

    @CollectsLifeTrigger(value = DrawCardEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainDrawCard(TriggerMatchContext match,
            DrawCardEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on life gain (draw a card)", gameData.id, cardName);
        return true;
    }

    @CollectsLifeTrigger(value = PutCounterOnEachControlledPermanentEffect.class,
            watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainPutCountersOnMatching(TriggerMatchContext match,
            PutCounterOnEachControlledPermanentEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on life gain (put +1/+1 counters)", gameData.id, cardName);
        return true;
    }

    @CollectsLifeTrigger(value = DealDamageOnSpellLifeGainEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainDealDamageOnSpell(TriggerMatchContext match,
            DealDamageOnSpellLifeGainEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        // Only triggers when the life gain source is an instant or sorcery spell of the matching color
        if (lg.sourceEntryType() == null) return false;
        if (lg.sourceEntryType() != StackEntryType.INSTANT_SPELL
                && lg.sourceEntryType() != StackEntryType.SORCERY_SPELL) return false;
        if (lg.sourceCard() == null || !lg.sourceCard().getColors().contains(effect.triggeringColor())) return false;

        // Queue for target selection (creature or player)
        gameData.queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(new DealDamageToAnyTargetEffect(effect.damage())),
                match.permanent().getId()
        ));

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on spell life gain (source: {})",
                gameData.id, cardName, lg.sourceCard().getName());
        return true;
    }

    @CollectsLifeTrigger(value = CradleOfVitalityLifeGainEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainPayForCounters(TriggerMatchContext match,
            CradleOfVitalityLifeGainEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        int lifeGained = lg.lifeGainedAmount();
        if (lifeGained <= 0) return false;

        // "Target creature" — if no creature is on any battlefield the ability has no legal target
        // and never goes on the stack.
        boolean anyCreature = gameData.orderedPlayerIds.stream()
                .map(gameData.playerBattlefields::get)
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .anyMatch(p -> gameQueryService.isCreature(gameData, p));
        if (!anyCreature) return false;

        // The counter count is locked in at trigger time (life gained by this event). The "you may pay"
        // choice happens at resolution via MayPayManaEffect; the +1/+1 counters go on the chosen creature.
        CardEffect payAndCounter = new MayPayManaEffect(effect.manaCost(),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(lifeGained)),
                "Pay " + effect.manaCost() + " to put " + lifeGained + " +1/+1 counter(s) on target creature?");

        gameData.queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(payAndCounter),
                match.permanent().getId(),
                true));

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain ({} life), pay for +1/+1 counters on target creature",
                gameData.id, cardName, lifeGained);
        return true;
    }

    @CollectsLifeTrigger(value = NykthosParagonLifeGainEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleNykthosParagonLifeGain(TriggerMatchContext match,
            NykthosParagonLifeGainEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lifeGain = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        Permanent source = match.permanent();
        if (source == null || lifeGain.lifeGainedAmount() <= 0) {
            return false;
        }

        MayEffect may = new MayEffect(
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new Fixed(lifeGain.lifeGainedAmount()),
                        new PermanentIsCreaturePredicate()),
                "Put +1/+1 counters on each creature you control?");
        gameData.queueMayAbility(source.getCard(), match.controllerId(), may, null, source.getId(),
                lifeGain.lifeGainedAmount(), match.markSourceOncePerTurnOnAcceptance());

        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers on life gain ({} life), once per turn on acceptance",
                gameData.id, source.getCard().getName(), lifeGain.lifeGainedAmount());
        return true;
    }

    @CollectsLifeTrigger(value = LoseLifeEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainTargetPlayerLosesLife(TriggerMatchContext match,
            LoseLifeEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        UUID opponentId = gameQueryService.getOpponentId(gameData, match.controllerId());

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                opponentId,
                match.permanent().getId());
        // "Each opponent loses life" does not target, so hexproof/shroud cannot stop it; only an explicit
        // "target player" recipient is a real target.
        entry.setNonTargeting(effect.recipient() != LoseLifeRecipient.TARGET_PLAYER);
        if (effect.recipient() == LoseLifeRecipient.TARGET_PLAYER) {
            // "Target opponent loses that much life": a copy's new target must also be an opponent.
            entry.setTargetFilter(new com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter(
                    new com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate(
                            com.github.laxika.magicalvibes.model.filter.PlayerRelation.OPPONENT),
                    "Target must be an opponent"));
        }
        // Snapshot the life gained onto the entry's event value — parallel to the spell-mana-spent
        // xValue plumbing — so the effect's EventValue amount ("equal to the life gained") reads it
        // back at resolution.
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(lg.lifeGainedAmount());
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain ({} life), target opponent loses that much",
                gameData.id, cardName, lg.lifeGainedAmount());
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

    // ── ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE ──────────────────────────────

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

    @CollectsDamageTrigger(value = BoostSelfEffect.class, watch = DamageWatch.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)
    private boolean handleNoncombatDamageBoostSelf(TriggerMatchContext match,
            BoostSelfEffect effect, TriggerContext ctx) {
        return enqueueNoncombatDamageTrigger(match, effect);
    }

    @CollectsDamageTrigger(value = BoostAllOwnCreaturesEffect.class, watch = DamageWatch.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)
    private boolean handleNoncombatDamageBoostAllOwnCreatures(TriggerMatchContext match,
            BoostAllOwnCreaturesEffect effect, TriggerContext ctx) {
        return enqueueNoncombatDamageTrigger(match, effect);
    }

    private boolean enqueueNoncombatDamageTrigger(TriggerMatchContext match, CardEffect effect) {
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
        log.info("Game {} - {} triggers on noncombat damage to opponent", gameData.id, cardName);
        return true;
    }

    @CollectsDamageTrigger(value = DrawCardEffect.class, watch = DamageWatch.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)
    private boolean handleNoncombatDamageDraw(TriggerMatchContext match,
            DrawCardEffect effect, TriggerContext ctx) {
        TriggerContext.NoncombatDamageToOpponent damage = (TriggerContext.NoncombatDamageToOpponent) ctx;
        if (damage.sourceControllerId() == null
                || !damage.sourceControllerId().equals(match.controllerId())) {
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
        if (effect.amount() instanceof EventValue) {
            entry.setEventValue(damage.damageAmount());
        }
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on controlled-source noncombat damage ({} damage)",
                match.gameData().id, match.permanent().getCard().getName(), damage.damageAmount());
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

    @CollectsDamageTrigger(value = SequenceEffect.class,
            watch = DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT)
    private boolean handleAllySourceDealtNoncombatDamageToOpponent(TriggerMatchContext match,
            SequenceEffect effect, TriggerContext ctx) {
        return enqueueNoncombatDamageTrigger(match, effect);
    }

    @CollectsDamageTrigger(value = CreateTokenEffect.class,
            watch = DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT)
    private boolean handleAllySourceDealtNoncombatDamageToOpponentToken(TriggerMatchContext match,
            CreateTokenEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.NoncombatDamageToOpponent damage)
                || match.permanent() == null
                || !match.controllerId().equals(damage.sourceControllerId())) {
            return false;
        }
        return enqueueNoncombatDamageTrigger(match, effect);
    }

    @CollectsDamageTrigger(value = MayEffect.class,
            watch = DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT)
    private boolean handleAllySourceDealtNoncombatDamageToOpponentMay(TriggerMatchContext match,
            MayEffect effect, TriggerContext ctx) {
        TriggerContext.NoncombatDamageToOpponent damage = (TriggerContext.NoncombatDamageToOpponent) ctx;
        if (match.permanent() == null || !match.controllerId().equals(damage.sourceControllerId())) {
            return false;
        }

        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), effect,
                null, match.permanent().getId(), damage.damageAmount());
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on noncombat damage to opponent (may ability)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsDamageTrigger(value = DealDamageToTargetCreatureOrPlaneswalkerEffect.class,
            watch = DamageWatch.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)
    private boolean handleNoncombatDamageToDamagedPlayerPermanent(TriggerMatchContext match,
            DealDamageToTargetCreatureOrPlaneswalkerEffect effect, TriggerContext ctx) {
        TriggerContext.NoncombatDamageToOpponent damage = (TriggerContext.NoncombatDamageToOpponent) ctx;
        if (!match.controllerId().equals(damage.sourceControllerId())) return false;

        Card sourceCard = match.permanent().getCard();
        int targetGroupIndex = sourceCard.getEffectTargetIndex(effect);
        TargetFilter targetFilter = targetGroupIndex >= 0
                ? sourceCard.getTargetGroups().get(targetGroupIndex).getFilter()
                : sourceCard.getTargetFilter();
        // "That player controls" is the card's own PermanentControlledByTriggeringPlayerPredicate, judged against
        // the opponent dealt damage as the choice's triggering player and again from the entry as it resolves.
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                sourceCard,
                match.controllerId(),
                new ArrayList<>(List.of(effect)),
                false,
                targetFilter,
                damage.damageAmount(),
                match.permanent().getId()
        ).withTriggeringPlayer(damage.damagedPlayerId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on noncombat damage to opponent and awaits a target",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    // ── ON_CONTROLLER_GAINS_LIFE (draw cards equal to life gained) ────

    @CollectsLifeTrigger(value = PayXManaDrawXCardsEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainPayForDraw(TriggerMatchContext match,
            PayXManaDrawXCardsEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        int lifeGained = lg.lifeGainedAmount();
        if (lifeGained <= 0) return false;

        String cardName = match.permanent().getCard().getName();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(lifeGained);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain ({} life), may pay X to draw X",
                match.gameData().id, cardName, lifeGained);
        return true;
    }

    @CollectsLibraryTrigger(value = PutCountersOnSelfEffect.class, watch = LibraryWatch.ON_CONTROLLER_SURVEILS)
    @CollectsLibraryTrigger(value = PutCountersOnSelfEffect.class, watch = LibraryWatch.ON_OPPONENT_SURVEILS)
    private boolean handleSurveilPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        UUID surveilingPlayerId = ((TriggerContext.Surveil) ctx).surveilingPlayerId();
        boolean opponentSurveil = !match.controllerId().equals(surveilingPlayerId);

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                opponentSurveil ? surveilingPlayerId : null,
                match.permanent().getId());
        if (opponentSurveil) {
            entry.setNonTargeting(true);
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on surveil (put counter on self)", gameData.id, cardName);
        return true;
    }

    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_CONTROLLER_SURVEILS)
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_OPPONENT_SURVEILS)
    private boolean handleSurveilDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Permanent source = match.permanent();
        UUID surveilingPlayerId = ((TriggerContext.Surveil) ctx).surveilingPlayerId();
        boolean opponentSurveil = !match.controllerId().equals(surveilingPlayerId);

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

    @CollectsLifeTrigger(value = MayPayManaEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainMayPayMana(TriggerMatchContext match,
            MayPayManaEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.permanent().getCard(), match.controllerId(), List.of(effect)));
            gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} triggers on life gain and needs a graveyard target",
                    gameData.id, cardName);
            return true;
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.LifeGainTriggerAnyTarget(
                    match.permanent().getCard(),
                    match.controllerId(),
                    List.of(effect),
                    match.permanent().getId()));
            gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} triggers on life gain and needs a target",
                    gameData.id, cardName);
            return true;
        }

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain (may pay {})",
                gameData.id, cardName, effect.manaCost());
        return true;
    }

    @CollectsLifeTrigger(value = DrawCardsEqualToLifeGainedEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleDrawCardsEqualToLifeGained(TriggerMatchContext match,
            DrawCardsEqualToLifeGainedEffect effect, TriggerContext ctx) {
        TriggerContext.LifeGain lg = (TriggerContext.LifeGain) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(new DrawCardEffect(new EventValue()))),
                null,
                match.permanent().getId());
        entry.setEventValue(lg.lifeGainedAmount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life gain, drawing {} cards",
                gameData.id, cardName, lg.lifeGainedAmount());
        return true;
    }

    // ── ON_CONTROLLER_LOSES_LIFE (sacrifice/discard for each life lost) ─

    @CollectsLifeTrigger(value = SacrificePermanentsEffect.class,
            watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleSacrificePermanentsOnLifeLoss(TriggerMatchContext match,
            SacrificePermanentsEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss ll = (TriggerContext.LifeLoss) ctx;
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
        if (amountEvaluationService.referencesEventValue(effect.count())) {
            entry.setEventValue(ll.lifeLostAmount());
        }
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life loss ({} life) — sacrifice permanents",
                gameData.id, cardName, ll.lifeLostAmount());
        return true;
    }

    @CollectsLifeTrigger(value = SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffect.class,
            watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleSacrificeOtherUnlessDiscardForEachLifeLost(TriggerMatchContext match,
            SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss ll = (TriggerContext.LifeLoss) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        int amount = ll.lifeLostAmount();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(amount);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life loss ({} life) — sacrifice/discard per life lost",
                gameData.id, cardName, amount);
        return true;
    }

    // ── ON_CONTROLLER_LOSES_LIFE (exile for each life lost) ──────────

    /**
     * Handles a draw amount that is snapshotted from the life-loss event, such as Vilis, Broker of
     * Blood's "whenever you lose life, draw that many cards" ability.
     */
    @CollectsLifeTrigger(value = DrawCardEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleLifeLossDrawCards(TriggerMatchContext match,
            DrawCardEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss ll = (TriggerContext.LifeLoss) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        UUID controllerId = match.controllerId();
        int amount = ll.lifeLostAmount();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                controllerId,
                cardName + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(amount);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life loss ({} life), drawing that many cards",
                gameData.id, cardName, amount);
        return true;
    }

    @CollectsLifeTrigger(value = ExileForEachLifeLostEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleExileForEachLifeLost(TriggerMatchContext match,
            ExileForEachLifeLostEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss ll = (TriggerContext.LifeLoss) ctx;
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
        entry.setEventValue(ll.lifeLostAmount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on life loss, exiling {} objects",
                gameData.id, cardName, ll.lifeLostAmount());
        return true;
    }

    @CollectsLifeTrigger(value = EnergyCountersEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleLifeLossEnergyCounters(TriggerMatchContext match,
            EnergyCountersEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();

        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life loss", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLifeTrigger(value = PutCountersOnSourceEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleLifeLossPutCounters(TriggerMatchContext match,
            PutCountersOnSourceEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on life loss", gameData.id, cardName);
        return true;
    }

    @CollectsLifeTrigger(value = PutCounterOnTargetPermanentEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    private boolean handleLifeLossPutCounterOnTarget(TriggerMatchContext match,
            PutCounterOnTargetPermanentEffect effect, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();

        gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                sourceCard, match.controllerId(), List.of(effect), match.permanent().getId()));

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on life loss (put counter on target)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsLifeTriggers({
            @CollectsLifeTrigger(value = CreateTokenEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE),
            @CollectsLifeTrigger(value = DealDamageToPlayersEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE),
            @CollectsLifeTrigger(value = PutCountersOnSelfEffect.class, watch = LifeWatch.ON_CONTROLLER_LOSES_LIFE)
    })
    private boolean handleGenericControllerLifeLoss(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.LifeLoss lifeLoss = (TriggerContext.LifeLoss) ctx;
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        if (effect instanceof DealDamageToPlayersEffect damage
                && amountEvaluationService.referencesEventValue(damage.amount())) {
            entry.setEventValue(lifeLoss.lifeLostAmount());
        } else if (effect instanceof PutCountersOnSelfEffect counters
                && counters.amount() != null
                && amountEvaluationService.referencesEventValue(counters.amount())) {
            entry.setEventValue(lifeLoss.lifeLostAmount());
        }
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on controller life loss ({} life)",
                match.gameData().id, sourceCard.getName(), lifeLoss.lifeLostAmount());
        return true;
    }

    /**
     * Exiles cards/permanents for Lich's Mastery. Priority: graveyard cards first,
     * then hand cards, then other battlefield permanents (avoiding the source enchantment
     * unless it's the last resort).
     */

    // ── ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD ────────────────────────────

    @CollectsWatchTrigger(value = ChooseModeNotYetChosenThisTurnEffect.class,
            watch = ZoneChangeWatch.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD)
    boolean handleTurnScopedModalOnCardsLeaveGraveyard(TriggerMatchContext match,
            ChooseModeNotYetChosenThisTurnEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.ControllerCardsLeaveGraveyard)) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                sourceCard, match.controllerId(), new ChooseOneEffect(effect.options()),
                match.permanent().getId(), true));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cards leaving graveyard (turn-scoped modal)",
                match.gameData().id, sourceCard.getName());
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

    @CollectsLifeTrigger(value = BecomePreparedEffect.class, watch = LifeWatch.ON_CONTROLLER_GAINS_LIFE)
    private boolean handleLifeGainBecomePrepared(TriggerMatchContext match,
            BecomePreparedEffect effect, TriggerContext ctx) {
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
        log.info("Game {} - {} triggers on life gain (becomes prepared)", gameData.id, cardName);
        return true;
    }
}

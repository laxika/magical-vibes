package com.github.laxika.magicalvibes.service.turn;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.rules.StepEvent;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MaySkipDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUpkeepStepEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.model.effect.DrawDuringEachOpponentsDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.RemoveSuspendCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTransformedReturnService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.battlefield.SagaChapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Begins the steps and phases of a turn for their triggered abilities (CR 603.2b): upkeep, draw step, the main phases,
 * beginning and end of combat, end step and cleanup. Each step's beginning is a {@link StepEvent} the trigger pipeline
 * matches against every "at the beginning of" ability; this service puts the step's delayed and engine-made abilities
 * on the stack around it, then begins the modes and targets the stack door queued
 * ({@link #beginNextStepTriggerChoice}).
 */
@Slf4j
@Service
public class StepTriggerService {
    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;
    @Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.trigger.StackedModalTriggerService stackedModalTriggerService;
    @Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.trigger.StackedDividedDamageTriggerService
            stackedDividedDamageTriggerService;

    private static final StepTrigger YOUR_UPKEEP = Triggers.upkeep(ActivePlayer.YOU);

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final AuraAttachmentService auraAttachmentService;
    @Autowired
    @Lazy
    private BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final LifeSupport lifeSupport;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardTransformedReturnService graveyardTransformedReturnService;
    private final GraveyardService graveyardService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final TriggerTargetCollector triggerTargetCollector;
    private final ValidTargetService validTargetService;
    private final CreatureControlService creatureControlService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final AmountEvaluationService amountEvaluationService;
    private final SagaChapterService sagaChapterService;
    private final EffectResolutionService effectResolutionService;

    public StepTriggerService(DrawService drawService,
                              GameQueryService gameQueryService,
                              PredicateEvaluationService predicateEvaluationService,
                              ConditionEvaluationService conditionEvaluationService,
                              GameLogService gameLogService,
                              PlayerInputService playerInputService,
                              PermanentRemovalService permanentRemovalService,
                              AuraAttachmentService auraAttachmentService,
                              LifeSupport lifeSupport,
                              BattlefieldEntryService battlefieldEntryService,
                              GraveyardTransformedReturnService graveyardTransformedReturnService,
                              GraveyardService graveyardService,
                              TriggeredAbilityQueueService triggeredAbilityQueueService,
                              TriggerTargetCollector triggerTargetCollector,
                              ValidTargetService validTargetService,
                              CreatureControlService creatureControlService,
                              @Lazy ETBTokenTargetService etbTokenTargetService,
                              AmountEvaluationService amountEvaluationService,
                              @Lazy SagaChapterService sagaChapterService,
                              @Lazy EffectResolutionService effectResolutionService) {
        this.drawService = drawService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.permanentRemovalService = permanentRemovalService;
        this.auraAttachmentService = auraAttachmentService;
        this.lifeSupport = lifeSupport;
        this.battlefieldEntryService = battlefieldEntryService;
        this.graveyardTransformedReturnService = graveyardTransformedReturnService;
        this.graveyardService = graveyardService;
        this.triggeredAbilityQueueService = triggeredAbilityQueueService;
        this.triggerTargetCollector = triggerTargetCollector;
        this.validTargetService = validTargetService;
        this.creatureControlService = creatureControlService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.amountEvaluationService = amountEvaluationService;
        this.sagaChapterService = sagaChapterService;
        this.effectResolutionService = effectResolutionService;
    }

    /**
     * Begins {@code moment}, a step or phase of the active player's turn: the active player is the planar controller
     * (CR 901.6), and the step's beginning is a rules event of its own (CR 603.2b, 603.2c), which the trigger pipeline
     * matches at once against every "at the beginning of" ability, putting the ones that trigger on the stack.
     */
    private void beginStep(GameData gameData, StepTrigger.Moment moment) {
        if (gameData.planechase != null) {
            gameData.planechase.controllerId = gameData.activePlayerId;
        }
        StepEvent event = new StepEvent(moment, gameData.activePlayerId);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(event));
    }

    public void handleUpkeepTriggers(GameData gameData) {
        gameData.forEachPermanent((playerId, permanent) -> permanent.markUpkeepStarted(gameData.activePlayerId));
        // Echo's "came under your control since the beginning of your last upkeep" (CR 702.30a) counts this upkeep
        // for the active player's permanents, phased out ones too: phasing changes neither control nor history.
        gameData.playerBattlefields.getOrDefault(gameData.activePlayerId, List.of())
                .forEach(Permanent::markControllerUpkeepStarted);
        gameData.phasedOutPermanents.getOrDefault(gameData.activePlayerId, List.of())
                .forEach(Permanent::markControllerUpkeepStarted);

        if (gameData.activePlayerId != null
                && gameData.activePlayerId.equals(gameData.initiativePlayerId)) {
            // "At the beginning of the upkeep of the player who has the initiative, that player ventures into
            // Undercity" (CR 727.2).
            triggeredAbilityStackService.putInherent(gameData, gameData.activePlayerId, "The initiative's ability",
                    List.of(new VentureIntoDungeonEffect(Dungeon.UNDERCITY)), TriggerRecord.NONE);
        }

        UUID activePlayerId = gameData.activePlayerId;
        gameData.untappedLandsAtTurnStart.computeIfAbsent(activePlayerId, ignored -> {
            List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
            if (battlefield == null) {
                return 0;
            }
            return (int) battlefield.stream()
                    .filter(permanent -> !permanent.isTapped())
                    .filter(permanent -> gameQueryService.isLand(gameData, permanent))
                    .count();
        });

        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        // Every "at the beginning of [an] upkeep" ability, printed, granted or of a card in another zone, triggers as
        // the step begins (CR 603.2b), matched by the trigger pipeline.
        beginStep(gameData, StepTrigger.Moment.UPKEEP);
        // "Since your last upkeep" (Wiitigo) ends as the upkeep begins, not when an ability reading it resolves or only
        // when one triggers: the window closes here for every permanent, after the abilities took their snapshots.
        battlefield.forEach(permanent -> permanent.setBlockedOrWasBlockedSinceLastUpkeep(false));
        // Suspend: remove a time counter from every suspended spell owned by the active player.
        for (GameData.SuspendedSpellExile pending : new ArrayList<>(gameData.suspendedSpellExiles)) {
            if (!pending.ownerId().equals(activePlayerId)) {
                continue;
            }
            var suspendedEntry = gameData.findExiledCard(pending.cardId());
            if (suspendedEntry == null) {
                continue;
            }
            Card suspendedCard = suspendedEntry.card();
            // Suspend's "remove a time counter" upkeep ability of a spell exiled with time counters that gained suspend
            // (CR 702.62a).
            triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                    new TriggeredAbility(new TriggerCondition(YOUR_UPKEEP, AbilityZones.EXILE),
                            new RemoveSuspendCounterFromExiledSpellEffect(pending.cardId())),
                    suspendedCard, null, activePlayerId, suspendedCard.getName() + "'s suspend ability",
                    ConditionContext.forCard(suspendedCard, activePlayerId), TriggerRecord.NONE));

            gameLogService.append(gameData, GameLog.cardThen(suspendedCard, "'s suspend ability triggers."));
            log.info("Game {} - {} suspend upkeep trigger pushed onto stack", gameData.id, suspendedCard.getName());
        }

        // Phase-in targeted triggers were queued during the untap-step phasing action; choose targets
        // now as they go on the stack (before upkeep's own targeted triggers).

        processNextPucasMischiefTarget(gameData);
    }

    /**
     * Begins the next mode or target choice of a triggered ability the stack door put on the stack or queued as it went
     * there (CR 603.3c, 603.3d), before anyone gets priority: the mode of a modal ability already on the stack and its
     * division of damage, then the queued mode and target choices (its targets in one choice or group by group, its
     * graveyard cards); the answer's handler begins the one after it. With none left, the may abilities are asked.
     */
    public void beginNextStepTriggerChoice(GameData gameData) {
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        if (stackedModalTriggerService.announceNextMode(gameData)
                || stackedDividedDamageTriggerService.announceNextDivision(gameData)) {
            return;
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)) {
            triggeredAbilityQueueService.processNextTriggeredModalTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)) {
            triggeredAbilityQueueService.processNextAttackTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        triggeredAbilityQueueService.processNextDayNightTriggerTarget(gameData);
        triggeredAbilityQueueService.beginStackDoorChoices(gameData);
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Begins the first target choice of the next queued exchange whose second target depends on the first: "target
     * creature you control and target creature an opponent controls with power less than or equal to that creature's
     * power" (Spawnbroker), or, for a card with {@link MultiTargetConstraint#MANA_VALUE_AT_MOST_FIRST_TARGET}, "target
     * nonland permanent you control and target nonland permanent an opponent controls with an equal or lesser mana
     * value" (Puca's Mischief). A permanent you control is offered only with a legal partner (CR 601.2c via 603.3d).
     */
    public void processNextPucasMischiefTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
            beginNextStepTriggerChoice(gameData);
            return;
        }

        PermanentChoiceContext.PucasMischiefOwnTarget trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class);
        boolean byManaValue = trigger.sourceCard().getMultiTargetConstraint()
                == MultiTargetConstraint.MANA_VALUE_AT_MOST_FIRST_TARGET;

        List<UUID> validOwnTargets = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(trigger.controllerId());
        if (battlefield != null) {
            for (Permanent own : battlefield) {
                if (validTargetService.isBlockedByHexproofOrGrantedEffect(gameData, own, trigger.controllerId())) continue;
                boolean hasPartner = byManaValue
                        ? !own.getCard().hasType(CardType.LAND)
                        && hasOpponentNonlandAtMostManaValue(gameData, trigger.controllerId(),
                        own.getCard().getManaValue())
                        : gameQueryService.isCreature(gameData, own)
                        && hasOpponentCreatureAtMostPower(gameData, trigger.controllerId(),
                        gameQueryService.getEffectivePower(gameData, own));
                if (hasPartner) {
                    validOwnTargets.add(own.getId());
                }
            }
        }
        if (validOwnTargets.isEmpty()) {
            log.info("Game {} - {} exchange skipped (no legal target pair)",
                    gameData.id, trigger.sourceCard().getName());
            processNextPucasMischiefTarget(gameData);
            return;
        }
        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validOwnTargets,
                trigger.sourceCard().getName() + (byManaValue
                        ? " — Choose a nonland permanent you control." : " — Choose a creature you control."));
        gameLogService.append(gameData, GameLog.cardThen(trigger.sourceCard(), byManaValue
                ? "'s triggered ability triggers." : "'s enter-the-battlefield ability triggers."));
    }

    private boolean hasOpponentNonlandAtMostManaValue(GameData gameData, UUID controllerId, int manaValue) {
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(controllerId)) continue;
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                if (!permanent.getCard().hasType(CardType.LAND)
                        && permanent.getCard().getManaValue() <= manaValue
                        && !validTargetService.isBlockedByHexproofOrGrantedEffect(gameData, permanent, controllerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasOpponentCreatureAtMostPower(GameData gameData, UUID controllerId, int power) {
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(controllerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && gameQueryService.getEffectivePower(gameData, permanent) <= power
                        && !validTargetService.isBlockedByHexproofOrGrantedEffect(gameData, permanent, controllerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Executes the draw step: the active player draws a card (rule 504.1),
     * unless it is turn 1 for the starting player (rule 103.7a).
     * Then scans for {@code Triggers.drawStep(ActivePlayer.YOU)} and {@code Triggers.drawStep(ActivePlayer.ANY)}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    public void handleDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        gameData.drawStepSkippedThisStep = false;

        // The starting player skips their entire draw step on turn 1 (rule 103.7a)
        if (gameData.turnNumber == 1 && activePlayerId.equals(gameData.startingPlayerId)
                && gameData.additionalBeginningPhaseReturnStep == null) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips the draw (first turn).";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activePlayerId));
            gameData.drawStepSkippedThisStep = true;
            return;
        }

        if (gameData.skipsStepOrPhase(activePlayerId, SkipStepOrPhaseKind.DRAW_STEP)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (chosen step or phase)", gameData.id,
                    gameData.playerIdToName.get(activePlayerId));
            gameData.drawStepSkippedThisStep = true;
            return;
        }
        // A "skip your (next) draw step" effect is used up as it applies: one that lasts only this turn (Elfhame
        // Sanctuary) before one that waits (Ivory Gargoyle), which then waits for the next draw step (CR 614.10a).
        if (gameData.applySkipNext(activePlayerId, SkipKind.DRAW_STEP)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (queued skip)", gameData.id, gameData.playerIdToName.get(activePlayerId));
            gameData.drawStepSkippedThisStep = true;
            return;
        }

        // A permanent may instruct its controller to skip their draw step (e.g. Colfenor's Plans).
        if (controlsSkipDrawStep(gameData, activePlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (SkipDrawStepEffect)", gameData.id, gameData.playerIdToName.get(activePlayerId));
            gameData.drawStepSkippedThisStep = true;
            return;
        }

        List<DrawStepReplacementSource> replacements = findMaySkipDrawStepDrawSources(gameData, activePlayerId);
        if (!replacements.isEmpty()) {
            for (DrawStepReplacementSource replacement : replacements) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        replacement.card(),
                        activePlayerId,
                        List.of(new ReplaceSingleDrawEffect(activePlayerId, replacement.kind())),
                        replacement.kind() == DrawReplacementKind.FASTING
                                ? "Skip your draw step and gain 2 life?"
                                : "Skip your draw? Until your next turn you can only be attacked by creatures with flying and/or islandwalk."
                ));
            }

            if (replacements.getFirst().kind() == DrawReplacementKind.ISLAND_SANCTUARY) {
                // Draw step triggered abilities (e.g. Howling Mine) still trigger at the beginning of the step.
                drawDuringOpponentsDrawStep(gameData);
                handleDrawStepTriggers(gameData);
            }

            if (!gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                playerInputService.processNextMayAbility(gameData);
            }
            return;
        }

        // Normal draw (turn-based action, rule 504.1)
        drawService.resolveDrawCard(gameData, activePlayerId);
        drawDuringOpponentsDrawStep(gameData);

        // Check for draw step triggered abilities (e.g. Howling Mine)
        handleDrawStepTriggers(gameData);

        if (!gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    /**
     * "You draw a card during each opponent's draw step" (Teferi, Who Slows the Sunset's emblem,
     * {@link DrawDuringEachOpponentsDrawStepEffect}): each emblem controller other than the active player draws right
     * after the active player's draw-step draw (CR 504.1), with no player getting priority in between (the card's
     * ruling), in APNAP order. Abilities of emblems function in the command zone (CR 114.4).
     */
    private void drawDuringOpponentsDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<UUID> players = gameData.orderedPlayerIds;
        int activeIndex = players.indexOf(activePlayerId);
        for (int offset = 1; offset < players.size(); offset++) {
            UUID playerId = players.get((activeIndex + offset) % players.size());
            for (Emblem emblem : List.copyOf(gameData.emblems)) {
                if (!playerId.equals(emblem.controllerId())) {
                    continue;
                }
                for (CardEffect effect : emblem.staticEffects()) {
                    if (effect instanceof DrawDuringEachOpponentsDrawStepEffect draw) {
                        for (int i = 0; i < draw.amount(); i++) {
                            drawService.resolveDrawCard(gameData, playerId);
                        }
                    }
                }
            }
        }
    }

    /**
     * Every permanent offering the same kind of draw-step skip, in battlefield order. Each one is a separate
     * replacement effect the player may decline in turn (CR 616.1), so each gets its own offer.
     */
    private List<DrawStepReplacementSource> findMaySkipDrawStepDrawSources(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<DrawStepReplacementSource> sources = new ArrayList<>();
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                if (effect instanceof MaySkipDrawReplacementEffect replacement && replacement.drawStepOnly()) {
                    sources.add(new DrawStepReplacementSource(perm.getCard(), replacement.replacementKind()));
                }
            }
        }
        if (sources.isEmpty()) return List.of();
        DrawReplacementKind kind = sources.getFirst().kind();
        return sources.stream().filter(source -> source.kind() == kind).toList();
    }

    private record DrawStepReplacementSource(Card card, DrawReplacementKind kind) {
    }

    public boolean controlsSkipDrawStep(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                if (effect instanceof SkipDrawStepEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean playersSkipUpkeepStepApplies(GameData gameData) {
        boolean globalSkip = gameData.anyPermanentMatches(permanent ->
                gameQueryService.getActiveStaticEffects(gameData, permanent).stream()
                        .anyMatch(PlayersSkipUpkeepStepEffect::isGlobal));
        if (globalSkip) {
            return true;
        }

        if (!gameData.playerHands.getOrDefault(gameData.activePlayerId, List.of()).isEmpty()) {
            return false;
        }
        return gameData.playerBattlefields.getOrDefault(gameData.activePlayerId, List.of()).stream()
                .flatMap(permanent -> gameQueryService.getActiveStaticEffects(gameData, permanent).stream())
                .anyMatch(PlayersSkipUpkeepStepEffect::isControllerScoped);
    }

    public void handleDrawStepTriggers(GameData gameData) {
        beginStep(gameData, StepTrigger.Moment.DRAW_STEP);
        beginNextStepTriggerChoice(gameData);
    }

    /**
     * Begins a precombat main phase: the rad counters' ability, the active player's Sagas' lore counters, then the
     * phase's "at the beginning of" abilities, delayed ones included ("at the beginning of your first main phase of the
     * game", Chancellor of the Tangle).
     *
     * @param gameData the current game state to modify
     */
    public void handlePrecombatMainTriggers(GameData gameData) {
        gameData.mainPhasesBegunThisTurn++;
        if (gameData.mainPhasesBegunThisTurn == 2) {
            beginStep(gameData, StepTrigger.Moment.SECOND_MAIN_PHASE);
        }
        handleRadCounterTrigger(gameData);

        // Saga lore counters: add a lore counter to each Saga the active player controls (CR 715.3c)
        handleSagaLoreCounters(gameData);

        beginStep(gameData, StepTrigger.Moment.PRECOMBAT_MAIN_PHASE);
        beginNextStepTriggerChoice(gameData);
    }

    private void handleRadCounterTrigger(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        int radCounters = gameData.playerRadCounters.getOrDefault(activePlayerId, 0);
        if (radCounters <= 0) {
            return;
        }

        // Rad counters' inherent ability, controlled by the active player (CR 729.1); its intervening "if" is judged
        // again as it resolves.
        triggeredAbilityStackService.putInherent(gameData, activePlayerId,
                gameData.playerIdToName.get(activePlayerId) + "'s rad ability",
                List.of(new ResolveRadCountersEffect()), TriggerRecord.NONE);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(activePlayerId) + "'s rad ability triggers."));
        log.info("Game {} - {}'s rad ability triggers", gameData.id,
                gameData.playerIdToName.get(activePlayerId));
    }

    /**
     * Hands the "at the beginning of your precombat main phase" abilities of the active player's permanents (Abstract
     * Paintmage) to the stack door, each one whole with the source as it was and the permanent an Aura or Equipment is
     * attached to, the last one first; the door chooses a mode or targets as it goes on the stack.
     */
    /**
     * Fires triggered abilities on permanents the active player controls at the
     * beginning of each postcombat main phase (e.g. Neheb, the Eternal).
     *
     * @param gameData the current game state to modify
     */
    public void handlePostcombatMainTriggers(GameData gameData) {
        gameData.mainPhasesBegunThisTurn++;
        gameData.postcombatMainPhasesBegunThisTurn++;
        beginStep(gameData, StepTrigger.Moment.POSTCOMBAT_MAIN_PHASE);
        if (gameData.mainPhasesBegunThisTurn == 2) {
            beginStep(gameData, StepTrigger.Moment.SECOND_MAIN_PHASE);
        }
        beginNextStepTriggerChoice(gameData);
    }

    /**
     * Scans every battlefield for {@code Triggers.endOfCombat()} abilities and pushes them onto
     * the stack as the end of combat step begins (CR 511.2). Unlike the postcombat-main scan this
     * runs on all players' permanents, because "at end of combat" is not restricted to the
     * controller's own turn. Also collects emblems that trigger at the end of their controller's
     * first combat phase.
     *
     * @param gameData the current game state to modify
     */
    public void handleEndOfCombatTriggers(GameData gameData) {
        beginStep(gameData, StepTrigger.Moment.END_OF_COMBAT);
        beginNextStepTriggerChoice(gameData);
    }

    /**
     * Adds a lore counter to each Saga the active player controls with one or more chapter abilities, as the layers
     * leave it, and triggers the appropriate chapter ability (CR 715.3c, 715.2b).
     * Called at the beginning of the active player's precombat main phase.
     */
    private void handleSagaLoreCounters(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        // Collect Sagas first to avoid ConcurrentModificationException
        List<Permanent> sagas = battlefield.stream()
                .filter(p -> p.getCard().isSaga() && !p.getCard().isBedtimeStory())
                .filter(p -> gameQueryService.sagaFinalChapterNumber(gameData, p) > 0)
                .toList();

        // Each lore counter is placed through the replacement pipeline (Vorinclex doubles or halves it),
        // and every chapter reached by a placed counter triggers in order.
        List<Map.Entry<Permanent, Integer>> loreSteps = new ArrayList<>();
        for (Permanent saga : sagas) {
            int previousLoreCount = saga.getCounterCount(CounterType.LORE);
            int placed = gameQueryService.replaceCounters(gameData, saga, CounterType.LORE, 1, activePlayerId);
            for (int step = 1; step <= placed; step++) {
                loreSteps.add(Map.entry(saga, previousLoreCount + step));
            }
            saga.setCounterCount(CounterType.LORE, previousLoreCount + placed);
        }

        for (Map.Entry<Permanent, Integer> loreStep : loreSteps) {
            Permanent saga = loreStep.getKey();
            Card card = saga.getCard();
            int newLoreCount = loreStep.getValue();
            sagaChapterService.renewLoreCounterPlayPermissions(gameData, saga, activePlayerId);

            gameLogService.append(gameData,
                    GameLog.cardThen(card, " gets a lore counter (" + newLoreCount + ")."));
            log.info("Game {} - {} gets lore counter {}", gameData.id, card.getName(), newLoreCount);
            gameData.rulesEvents.occur(CounterChangeEvent.of(CounterChangeEvent.Kind.YOU_PUT_LORE_COUNTER, saga,
                    activePlayerId, CounterType.LORE, 1));

            // Each chapter a placed counter reached triggers (CR 715.2b); the stack door chooses its targets.
            sagaChapterService.putChapterAbility(gameData, saga, card, activePlayerId, newLoreCount, false);
        }
        sagaChapterService.beginChapterChoices(gameData);
    }

    /** Applies the scheduled return and obtains an attack choice when necessary. */
    public void resolvePendingExileReturn(GameData gameData, PendingExileReturn pending) {
        processPendingExileReturns(gameData, List.of(pending));
    }

    public void resolvePendingExileReturnAttackTarget(GameData gameData, UUID attackTargetId,
                                                       PermanentChoiceContext.ExileReturnAttackTarget context) {
        Set<UUID> validTargets = validExileReturnAttackTargets(gameData, context.pending().controllerId());
        UUID resolvedTarget = validTargets.contains(attackTargetId)
                ? attackTargetId
                : validTargets.stream().findFirst().orElse(null);
        returnPendingExileCard(gameData, context.pending(), resolvedTarget);
        processPendingExileReturns(gameData, context.remaining());
    }

    private void processPendingExileReturns(GameData gameData, List<PendingExileReturn> pendingReturns) {
        for (int i = 0; i < pendingReturns.size(); i++) {
            PendingExileReturn pending = pendingReturns.get(i);
            if (pending.returnAttacking()) {
                Set<UUID> validTargets = validExileReturnAttackTargets(gameData, pending.controllerId());
                if (validTargets.size() > 1) {
                    List<PendingExileReturn> remaining = pendingReturns.subList(i + 1, pendingReturns.size());
                    gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.ExileReturnAttackTarget(pending, remaining));
                    playerInputService.beginAnyTargetChoice(
                            gameData,
                            pending.controllerId(),
                            validTargets.stream().filter(id -> !gameData.playerIds.contains(id)).toList(),
                            validTargets.stream().filter(gameData.playerIds::contains).toList(),
                            "Choose an opponent or opposing planeswalker for " + pending.card().getName()
                                    + " to attack.");
                    return;
                }
                returnPendingExileCard(gameData, pending, validTargets.stream().findFirst().orElse(null));
            } else {
                returnPendingExileCard(gameData, pending, null);
            }
        }
    }

    private Set<UUID> validExileReturnAttackTargets(GameData gameData, UUID controllerId) {
        Set<UUID> validTargets = new LinkedHashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(playerId)) {
                continue;
            }
            validTargets.add(playerId);
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().hasType(CardType.PLANESWALKER)) {
                    validTargets.add(permanent.getId());
                }
            }
        }
        return validTargets;
    }

    /**
     * An Aura returning from exile without an attachment chosen by its effect is attached as its owner
     * chooses (CR 303.4f). With no legal object or player to enchant, it remains in exile (CR 303.4g).
     */
    private void beginAuraReturnChoice(GameData gameData, Card auraCard, UUID ownerId) {
        List<UUID> permanentTargets = new ArrayList<>();
        gameData.forEachPermanent((ignored, candidate) -> {
            if (auraAttachmentService.canEnchant(gameData, auraCard, ownerId, candidate)) {
                permanentTargets.add(candidate.getId());
            }
        });
        List<UUID> playerTargets = auraCard.isEnchantPlayer()
                ? gameData.orderedPlayerIds.stream()
                .filter(playerId -> auraAttachmentService.canEnchantPlayer(gameData, auraCard, ownerId, playerId))
                .toList()
                : List.of();
        if (permanentTargets.isEmpty() && playerTargets.isEmpty()) {
            log.info("Game {} - {} has no legal object to enchant and stays in exile", gameData.id,
                    auraCard.getName());
            return;
        }
        if (!gameData.removeFromExile(auraCard.getId())) {
            return;
        }
        gameData.interaction.setPendingAuraCard(auraCard);
        if (playerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, ownerId, permanentTargets,
                    "Choose a permanent for " + auraCard.getName() + " to enchant.");
        } else {
            playerInputService.beginAnyTargetChoice(gameData, ownerId, permanentTargets, playerTargets,
                    "Choose a permanent or player for " + auraCard.getName() + " to enchant.");
        }
    }

    private void returnPendingExileCard(GameData gameData, PendingExileReturn pending, UUID attackTargetId) {
        UUID controllerId = pending.controllerId();
        if (pending.card().isAura() && !pending.card().hasType(CardType.CREATURE)
                && pending.cardsToAttachToPrimary().isEmpty() && pending.additionalCards().isEmpty()) {
            beginAuraReturnChoice(gameData, pending.card(), controllerId);
            return;
        }

        List<Card> cards = new ArrayList<>();
        cards.add(pending.card());
        cards.addAll(pending.additionalCards());

        if (pending.cardsToAttachToPrimary().isEmpty()) {
            // CR 303.4f/303.4g: an Aura returning without an attachment chosen by the effect picks a host
            // among the permanents already on the battlefield (not those returning with it) and stays
            // in exile when none can be enchanted.
            List<BattlefieldEntryCard> returningAuras = new ArrayList<>();
            for (Card card : cards) {
                ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
                if (exiled != null && card.isAura() && !card.isEnchantZone() && !card.hasType(CardType.CREATURE)) {
                    returningAuras.add(new BattlefieldEntryCard(
                            pending.returnControllerByCardId().getOrDefault(card.getId(), controllerId),
                            exiled.ownerId(), card, Zone.EXILE, null).withTapped(pending.returnTapped()));
                }
            }
            if (!returningAuras.isEmpty()) {
                List<UUID> auraIds = returningAuras.stream().map(entry -> entry.card().getId()).toList();
                cards.removeIf(card -> auraIds.contains(card.getId()));
                battlefieldEntryBatchSupport.begin(gameData, returningAuras);
            }
        }

        List<Card> returningCards;
        if (pending.cardsToAttachToPrimary().isEmpty()) {
            returningCards = cards.stream()
                    .filter(card -> gameData.removeFromExile(card.getId()))
                    .toList();
        } else {
            if (!gameData.removeFromExile(pending.card().getId())) {
                log.info("Game {} - delayed attached return skipped because its primary card is no longer in exile",
                        gameData.id);
                return;
            }
            List<Card> returned = new ArrayList<>();
            returned.add(pending.card());
            // Auras attached to the primary card can only enter attached to a creature; when the
            // primary card returns as a noncreature they stay in exile.
            if (pending.card().hasType(CardType.CREATURE)) {
                for (Card additional : pending.additionalCards()) {
                    if (gameData.removeFromExile(additional.getId())) {
                        returned.add(additional);
                    }
                }
            }
            returningCards = returned;
        }
        if (returningCards.isEmpty()) {
            log.info("Game {} - delayed return skipped because its cards are no longer in exile", gameData.id);
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Permanent> returnedPermanents = new ArrayList<>();
        List<UUID> returnedControllerIds = new ArrayList<>();
        Permanent primaryPermanent = null;
        for (Card card : returningCards) {
            Permanent perm = new Permanent(card);
            boolean returningAttachment = pending.cardsToAttachToPrimary().contains(card.getId());
            if ((pending.returnTapped() && !returningAttachment)
                    || (pending.returnLandsTapped() && card.hasType(CardType.LAND))) {
                perm.tap();
            }
            if (pending.returnAttacking() && attackTargetId != null && !returningAttachment) {
                perm.setAttacking(true);
                perm.setAttackTarget(attackTargetId);
            }
            if (pending.returnCantBeBlockedUntilEndOfCombat()
                    && card.getId().equals(pending.card().getId())) {
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), card.getName(), null, controllerId,
                        new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.TARGET),
                        perm.getId(), null, null, Duration.UNTIL_END_OF_COMBAT, 0));
            }
            boolean isCreature = card.hasType(CardType.CREATURE)
                    || (pending.plusOnePlusOneCountersOnlyOnCreatures() && gameQueryService.isCreature(
                    gameData, perm, pending.returnControllerByCardId().getOrDefault(card.getId(), controllerId)));
            if (pending.plusOnePlusOneCounters() > 0
                    && (!pending.plusOnePlusOneCountersOnlyOnCreatures() || isCreature)
                    && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, perm, controllerId)) {
                int counters = gameQueryService.doublePlusOnePlusOneCounters(
                        gameData, perm, controllerId, pending.plusOnePlusOneCounters());
                if (counters > 0) {
                    perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
                }
            }
            if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                int loyalty = gameQueryService.replaceCounters(gameData, perm, controllerId, CounterType.LOYALTY,
                        card.getLoyalty() + pending.loyaltyCountersOnPlaneswalkers());
                perm.setCounterCount(CounterType.LOYALTY, loyalty);
            }
            if (pending.counterTypeOnReturn() != null && pending.counterAmountOnReturn() > 0) {
                int counters = gameQueryService.replaceCounters(gameData, perm, controllerId,
                        pending.counterTypeOnReturn(), pending.counterAmountOnReturn(), controllerId);
                if (counters > 0) {
                    perm.setCounterCount(pending.counterTypeOnReturn(), counters);
                }
            }
            for (Map.Entry<CounterType, Integer> counter : pending.countersOnReturn().entrySet()) {
                if (counter.getValue() <= 0) {
                    continue;
                }
                int counters = gameQueryService.replaceCounters(
                        gameData, perm, controllerId, counter.getKey(), counter.getValue(), controllerId);
                if (counters > 0) {
                    perm.setCounterCount(counter.getKey(), counters);
                }
            }
            perm.setEnteredFromExile(true);
            if (pending.grantHaste()) {
                gameData.addEffectOnPermanent(pending.card().getName(), null, controllerId, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET), perm.getId(), Duration.UNTIL_END_OF_TURN);
            }
            if (card.getId().equals(pending.card().getId())) {
                primaryPermanent = perm;
            }
            boolean attachToPrimary = primaryPermanent != null
                    && pending.cardsToAttachToPrimary().contains(card.getId());
            UUID cardControllerId = attachToPrimary && card.getOwnerId() != null
                    ? card.getOwnerId()
                    : pending.returnControllerByCardId().getOrDefault(card.getId(), controllerId);
            if (attachToPrimary) {
                perm.setAttachedTo(primaryPermanent.getId());
            }
            var cardTypeChoice = returningCards.size() == 1
                    ? card.getStaticEffects().stream()
                    .filter(com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect.class::isInstance)
                    .map(com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect.class::cast)
                    .filter(choice -> !choice.lookAtOpponentHand())
                    .findFirst().orElse(null)
                    : null;
            if (cardTypeChoice != null) {
                // "As this enters, choose a card type" is made before the permanent enters; the choice
                // handler puts the prepared permanent onto the battlefield (Stenn, Paranoid Partisan).
                playerInputService.beginCardTypeOnEnterChoice(
                        gameData, cardControllerId, card, cardTypeChoice.excludedTypes(), perm);
                return;
            }
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, cardControllerId, perm, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(perm);
            returnedPermanents.add(perm);
            returnedControllerIds.add(cardControllerId);
            String playerName = gameData.playerIdToName.get(cardControllerId);
            String attackText = pending.returnAttacking() && attackTargetId != null
                    ? " tapped and attacking" : "";
            gameLogService.append(gameData,
                    GameLog.cardThen(card, " returns to the battlefield" + attackText + " under "
                            + playerName + "'s control."));
            log.info("Game {} - {} returns from exile for {}", gameData.id, card.getName(), playerName);
        }
        for (int i = 0; i < returningCards.size(); i++) {
            Card card = returningCards.get(i);
            UUID cardControllerId = returnedControllerIds.get(i);
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, cardControllerId, card, null, false);
        }
        if (primaryPermanent != null && !pending.cardsToAttachToPrimary().isEmpty()) {
            creatureControlService.recomputeControl(gameData, primaryPermanent);
        }

        if (pending.discardControllerCardsEqualToReturnedToughness()
                && pending.followUpSourceCard() != null
                && pending.timingControllerId() != null) {
            int returnedIndex = -1;
            for (int i = 0; i < returningCards.size(); i++) {
                if (returningCards.get(i).getId().equals(pending.card().getId())) {
                    returnedIndex = i;
                    break;
                }
            }
            if (returnedIndex >= 0) {
                Permanent returned = returnedPermanents.get(returnedIndex);
                if (gameQueryService.isCreature(gameData, returned)) {
                    int discardCount = Math.max(0, gameQueryService.getEffectiveToughness(gameData, returned));
                    if (discardCount > 0) {
                        StackEntry discardEntry = StackEntry.carrier(
                                pending.followUpSourceCard(),
                                pending.timingControllerId(),
                                pending.followUpSourceCard().getName() + "'s delayed trigger — discard "
                                        + discardCount + " cards",
                                new ArrayList<>(List.of(new DiscardEffect(
                                        discardCount, DiscardRecipient.CONTROLLER))));
                        discardEntry.setNonTargeting(true);
                        gameLogService.append(gameData,
                                GameLog.cardThen(pending.followUpSourceCard(), "'s delayed trigger discards cards."));
                        effectResolutionService.resolveEffects(gameData, discardEntry);
                    }
                }
            }
        }
    }

    /**
     * Processes end-step triggers: exiles pending tokens (e.g. Mimic Vat),
     * then scans battlefields for
     * {@code Triggers.endStep(ActivePlayer.ANY)} and {@code Triggers.endStep(ActivePlayer.YOU)}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    public void handleEndStepTriggers(GameData gameData) {
        if (gameData.activePlayerId.equals(gameData.monarchPlayerId)) {
            // "At the beginning of the monarch's end step, that player draws a card" (CR 726.2).
            UUID monarchId = gameData.monarchPlayerId;
            triggeredAbilityStackService.putInherent(gameData, monarchId, "The monarch draws a card",
                    List.of(new com.github.laxika.magicalvibes.model.effect.DrawCardForMonarchEndStepEffect()),
                    entry -> {
                        entry.setTargetId(monarchId);
                        entry.setNonTargeting(true);
                    });
        }
        handleBedtimeStoryLoreCounters(gameData);
        UUID activePlayerId = gameData.activePlayerId;

        beginStep(gameData, StepTrigger.Moment.END_STEP);

        beginNextStepTriggerChoice(gameData);
    }

    private void handleBedtimeStoryLoreCounters(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        battlefield.stream()
                .filter(permanent -> permanent.getCard().isSaga()
                        && permanent.getCard().isBedtimeStory())
                .toList()
                .forEach(saga -> sagaChapterService.addLoreCounterAndTriggerChapter(
                        gameData, saga, saga.getCard(), activePlayerId));
    }

    /** Begins the declare attackers step for the delayed abilities that wait for it. */
    public void handleDeclareAttackersTriggers(GameData gameData) {
        beginStep(gameData, StepTrigger.Moment.DECLARE_ATTACKERS);
    }

    public void handleCleanupTriggers(GameData gameData) {
        beginStep(gameData, StepTrigger.Moment.CLEANUP);
    }

    /**
     * Scans the active player's battlefield, command zone, and graveyard for beginning-of-combat triggered
     * abilities and pushes them onto the stack.
     * {@code Triggers.beginningOfCombat(ActivePlayer.YOU)} fires only for the active player's permanents
     * (CR 507.1: "At the beginning of combat on your turn").
     * {@code Triggers.beginningOfCombat(ActivePlayer.ANY)} fires for every permanent on every
     * battlefield (e.g. Majestic Myriarch / Odric, Lunarch Marshal).
     * {@code Triggers.beginningOfCombat(ActivePlayer.OPPONENT)} fires only for permanents controlled by a
     * player other than the active player (Sentinel of the Eternal Watch).
     *
     * @param gameData the current game state to modify
     */
    public void handleBeginningOfCombatTriggers(GameData gameData) {
        beginStep(gameData, StepTrigger.Moment.BEGINNING_OF_COMBAT);

        beginNextStepTriggerChoice(gameData);
    }
}

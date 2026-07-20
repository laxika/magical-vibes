package com.github.laxika.magicalvibes.service.turn;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldTransformedReturn;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;
import com.github.laxika.magicalvibes.model.action.DelayedCreateToken;
import com.github.laxika.magicalvibes.model.action.DelayedUntapPermanents;
import com.github.laxika.magicalvibes.model.action.DamageAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.LoseLifeAtNextDrawStepUnlessPays;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.RevokeExilePlayPermissionAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.DelayedPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.DelayedPlusZeroPlusOneCounters;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DestroyNonAttackersAtEndStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtEndStep;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHandEmpty;
import com.github.laxika.magicalvibes.model.condition.CardsAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.condition.SelfDealtDamageToOpponentThisTurn;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.AllArtifactsUpkeepSacrificeUnlessPayEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfDidntCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyRandomOpponentPermanentWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlIfSubtypesDealtCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.UntapUpToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveEggCounterFromExileAndReturnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndReturnCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Collects and processes triggered abilities that fire at the beginning of
 * specific turn steps: upkeep, draw, precombat main, and end step.
 *
 * <p>Extracted from {@code TurnProgressionService} to isolate the trigger-
 * scanning logic.  For each step the service iterates the relevant
 * {@link EffectSlot}s on permanents (and graveyards, for upkeep triggers),
 * pushes {@link StackEntry}s onto the stack, and queues
 * {@link MayEffect}/{@link MayPayManaEffect} choices as needed.
 *
 * <p>Also handles the Chancellor cycle's opening-hand reveal triggers on
 * the first upkeep, and upkeep copy-trigger target selection (CR 603.3d).
 */
@Slf4j
@Service
public class StepTriggerService {

    private static final PermanentIsLandPredicate LAND_PREDICATE = new PermanentIsLandPredicate();

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;
    private final TriggerTargetCollector triggerTargetCollector;
    private final ParadigmService paradigmService;
    private final ValidTargetService validTargetService;
    private final CreatureControlService creatureControlService;

    public StepTriggerService(DrawService drawService,
                              GameQueryService gameQueryService,
                              PredicateEvaluationService predicateEvaluationService,
                              ConditionEvaluationService conditionEvaluationService,
                              GameBroadcastService gameBroadcastService,
                              PlayerInputService playerInputService,
                              PermanentRemovalService permanentRemovalService,
                              BattlefieldEntryService battlefieldEntryService,
                              GraveyardTargetingService graveyardTargetingService,
                              GraveyardService graveyardService,
                              TriggerCollectionService triggerCollectionService,
                              TriggerTargetCollector triggerTargetCollector,
                              @Lazy ParadigmService paradigmService,
                              ValidTargetService validTargetService,
                              CreatureControlService creatureControlService) {
        this.drawService = drawService;
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.gameBroadcastService = gameBroadcastService;
        this.playerInputService = playerInputService;
        this.permanentRemovalService = permanentRemovalService;
        this.battlefieldEntryService = battlefieldEntryService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.graveyardService = graveyardService;
        this.triggerCollectionService = triggerCollectionService;
        this.triggerTargetCollector = triggerTargetCollector;
        this.paradigmService = paradigmService;
        this.validTargetService = validTargetService;
        this.creatureControlService = creatureControlService;
    }

    /**
     * Scans battlefields, graveyards, and (on turn 1) hands for upkeep-triggered
     * abilities and pushes them onto the stack or queues may-ability prompts.
     *
     * <p>Handles slots: {@code UPKEEP_TRIGGERED}, {@code GRAVEYARD_UPKEEP_TRIGGERED},
     * {@code EACH_UPKEEP_TRIGGERED}, {@code OPPONENT_UPKEEP_TRIGGERED},
     * {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED}, and
     * {@code ON_OPENING_HAND_REVEAL} (Chancellor cycle, turn 1 only).
     *
     * @param gameData the current game state to modify
     */
    public void handleUpkeepTriggers(GameData gameData) {
        // Delayed "draw N cards at the beginning of the next turn's upkeep" (e.g. Library of Lat-Nam).
        // Drained regardless of who the active player is — the scheduling player draws.
        if (gameData.hasDelayedAction(DrawCardsAtNextUpkeep.class)) {
            List<DrawCardsAtNextUpkeep> pendingDraws = gameData.drainDelayedActions(DrawCardsAtNextUpkeep.class);
            for (DrawCardsAtNextUpkeep pending : pendingDraws) {
                String playerName = gameData.playerIdToName.get(pending.controllerId());
                for (int i = 0; i < pending.count(); i++) {
                    drawService.resolveDrawCard(gameData, pending.controllerId());
                }
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                        playerName + " draws " + pending.count() + " cards from ", pending.sourceCard(), "."));
                log.info("Game {} - {} draws {} cards from delayed upkeep trigger ({})",
                        gameData.id, playerName, pending.count(), pending.sourceCard().getName());
            }
        }

        // Quenchable Fire: "It deals an additional N damage to that player or planeswalker at the
        // beginning of your next upkeep step unless that player or that planeswalker's controller pays
        // {cost} before that step." Fires only at the spell controller's own upkeep; the paying party
        // is the targeted player, or the targeted planeswalker's controller (skip if the target is
        // gone). Pushed onto the stack as a "you may pay; if you don't, take damage" trigger.
        if (gameData.hasDelayedAction(DamageAtNextUpkeepUnlessPays.class)) {
            List<DamageAtNextUpkeepUnlessPays> pending = gameData.drainDelayedActions(
                    DamageAtNextUpkeepUnlessPays.class, a -> a.spellControllerId().equals(gameData.activePlayerId));
            for (DamageAtNextUpkeepUnlessPays action : pending) {
                UUID payerId = gameData.playerIds.contains(action.targetId())
                        ? action.targetId()
                        : gameQueryService.findPermanentController(gameData, action.targetId());
                if (payerId == null) continue; // targeted planeswalker (or player) is gone — trigger fizzles

                DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect effect =
                        new DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect(action.damage(), action.manaCost());
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, action.sourceCard(), payerId,
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(effect)), action.targetId(), (UUID) null));

                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(action.sourceCard().getName()
                        + "'s delayed ability triggers — pay " + action.manaCost() + " or take "
                        + action.damage() + " damage."));
                log.info("Game {} - {} delayed upkeep pay-or-take-damage trigger pushed for {}",
                        gameData.id, action.sourceCard().getName(), gameData.playerIdToName.get(payerId));
            }
        }

        // Grinning Totem: "At the beginning of your next upkeep, if you haven't played it, put it into
        // its owner's graveyard." Only the scheduling player's own upkeep resolves the cleanup.
        if (gameData.hasDelayedAction(ExileToOwnerGraveyardAtNextUpkeep.class)) {
            List<ExileToOwnerGraveyardAtNextUpkeep> pending = gameData.drainDelayedActions(
                    ExileToOwnerGraveyardAtNextUpkeep.class, a -> a.controllerId().equals(gameData.activePlayerId));
            for (ExileToOwnerGraveyardAtNextUpkeep action : pending) {
                gameData.exilePlayPermissions.remove(action.cardId());
                var exiled = gameData.findExiledCard(action.cardId());
                if (exiled == null) {
                    // The card was played (or otherwise left exile) — nothing to clean up.
                    continue;
                }
                gameData.removeFromExile(action.cardId());
                graveyardService.addCardToGraveyard(gameData, action.ownerId(), exiled.card());
                String sourceName = action.sourceCard() != null ? action.sourceCard().getName() : "an effect";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text("The card exiled with " + sourceName + " is put into its owner's graveyard."));
                log.info("Game {} - unplayed card exiled with {} put into owner's graveyard",
                        gameData.id, sourceName);
            }
        }

        // Elkin Bottle: "Until the beginning of your next upkeep, you may play that card." At the
        // scheduling player's next upkeep the permission is revoked; an unplayed card stays in exile.
        if (gameData.hasDelayedAction(RevokeExilePlayPermissionAtNextUpkeep.class)) {
            List<RevokeExilePlayPermissionAtNextUpkeep> pending = gameData.drainDelayedActions(
                    RevokeExilePlayPermissionAtNextUpkeep.class, a -> a.controllerId().equals(gameData.activePlayerId));
            for (RevokeExilePlayPermissionAtNextUpkeep action : pending) {
                if (gameData.exilePlayPermissions.remove(action.cardId()) != null
                        && gameData.findExiledCard(action.cardId()) != null) {
                    String sourceName = action.sourceCard() != null ? action.sourceCard().getName() : "an effect";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                            "The card exiled with " + sourceName + " can no longer be played."));
                }
            }
        }

        // Chancellor cycle: at the beginning of the first upkeep, check all players' hands
        // for cards with ON_OPENING_HAND_REVEAL effects (revealed from opening hand)
        if (gameData.turnNumber == 1) {
            handleOpeningHandTriggers(gameData);
        }

        UUID activePlayerId = gameData.activePlayerId;
        // Snapshot untapped lands the active player controls now (post-untap, pre-priority) — the
        // "number of untapped lands they controlled at the beginning of this turn" for Power Surge.
        // Locked here so tapping lands in response to the upkeep trigger cannot reduce the value.
        gameData.untappedLandsAtTurnStart.put(activePlayerId, countUntappedLands(gameData, activePlayerId));

        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> upkeepEffects = perm.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED);
            if (upkeepEffects == null || upkeepEffects.isEmpty()) continue;

            // If any effect can target both a player and a permanent (i.e. "any target" —
            // creature/planeswalker/player, e.g. Form of the Dragon's "deals 5 damage to any target"),
            // route through the any-target pipeline so the controller may pick a permanent as well as a
            // player. Creature-only targeted upkeep effects (e.g. become-a-copy) keep their own pipelines.
            boolean hasAnyTarget = upkeepEffects.stream()
                    .anyMatch(e -> e.targetSpec().category().includesPlayers() && e.targetSpec().category().includesPermanents());
            if (hasAnyTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.UpkeepAnyTargetTrigger(
                        perm.getCard(), activePlayerId, new ArrayList<>(upkeepEffects), perm.getId()));
                continue;
            }

            // If any effect targets a player, group all effects into a player-targeted trigger
            boolean hasPlayerTarget = upkeepEffects.stream().anyMatch(e -> e.targetSpec().category().includesPlayers());
            if (hasPlayerTarget) {
                int maxPlayerTargets = upkeepEffects.stream()
                        .mapToInt(e -> e.targetSpec().playerTargetCount())
                        .max().orElse(1);
                if (maxPlayerTargets >= 2) {
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger(
                            perm.getCard(), activePlayerId, new ArrayList<>(upkeepEffects), perm.getId()));
                } else {
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepPlayerTargetTrigger(
                            perm.getCard(), activePlayerId, new ArrayList<>(upkeepEffects), perm.getId()));
                }
                continue;
            }

            // Puca's Mischief: two interdependent nonland-permanent targets chosen at trigger time
            // (one you control, one an opponent controls with equal or lesser mana value). The
            // MayEffect wrapper is carried through so the "you may" is honoured at resolution.
            boolean isExchangeControl = upkeepEffects.stream().anyMatch(e ->
                    e instanceof ExchangeControlOfTargetPermanentsEffect
                            || (e instanceof MayEffect m && m.wrapped() instanceof ExchangeControlOfTargetPermanentsEffect));
            if (isExchangeControl) {
                gameData.queueInteraction(new PermanentChoiceContext.PucasMischiefOwnTarget(
                        perm.getCard(), activePlayerId, new ArrayList<>(upkeepEffects), perm.getId()));
                continue;
            }

            for (CardEffect effect : upkeepEffects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), activePlayerId, may, null, perm.getId());
                } else if (effect instanceof MayRevealSubtypeFromHandEffect mayReveal) {
                    List<Card> hand = gameData.playerHands.get(activePlayerId);
                    boolean hasSubtype = hand != null && hand.stream()
                            .anyMatch(c -> c.getSubtypes().contains(mayReveal.subtype()));
                    if (hasSubtype) {
                        MayEffect may = new MayEffect(mayReveal.thenEffect(), mayReveal.prompt());
                        gameData.queueMayAbility(perm.getCard(), activePlayerId, may, null, perm.getId());
                    }
                } else if (effect instanceof BecomeCopyOfTargetCreatureEffect) {
                    // Targeted upkeep trigger: target is chosen at trigger time (CR 603.3d).
                    // Collect valid creature targets excluding self ("another creature").
                    boolean hasValidTargets = false;
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> bf = gameData.playerBattlefields.get(pid);
                        if (bf == null) continue;
                        for (Permanent p : bf) {
                            if (p.getId().equals(perm.getId())) continue;
                            if (gameQueryService.isCreature(gameData, p)) {
                                hasValidTargets = true;
                                break;
                            }
                        }
                        if (hasValidTargets) break;
                    }
                    if (hasValidTargets) {
                        gameData.queueInteraction(new PermanentChoiceContext.UpkeepCopyTriggerTarget(
                                perm.getCard(), activePlayerId, perm.getId()));
                    }
                } else if (effect instanceof DestroyOneOfTargetsAtRandomEffect) {
                    // Targeted upkeep trigger: targets chosen at trigger time (CR 603.3d).
                    // The Efreet itself is a valid "nonland permanent you control" target,
                    // so this always triggers as long as it's on the battlefield.
                    gameData.queueInteraction(new PermanentChoiceContext.CapriciousEfreetOwnTarget(
                            perm.getCard(), activePlayerId, perm.getId()));
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof NoOtherPermanent) {
                    // Intervening-if: only trigger if controller has no other matching permanents
                    boolean conditionMet = conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, activePlayerId));
                    if (conditionMet) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: no other matching permanents)",
                                gameData.id, perm.getCard().getName());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ControllerLifeAtMost lifeCheck) {
                    // Intervening-if: only trigger if controller's life total <= threshold
                    int lifeTotal = gameData.playerLifeTotals.getOrDefault(activePlayerId, 20);
                    if (conditionEvaluationService.isMet(gameData, lifeCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: life {} <= {})",
                                gameData.id, perm.getCard().getName(), lifeTotal, lifeCheck.threshold());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ControlsPermanentCount countCheck) {
                    // Intervening-if: only trigger if controller has enough matching permanents
                    List<Permanent> controllerBf = gameData.playerBattlefields.get(activePlayerId);
                    long matchCount = controllerBf == null ? 0 : controllerBf.stream()
                            .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, countCheck.filter()))
                            .count();
                    if (conditionEvaluationService.isMet(gameData, countCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {} matching permanents >= {})",
                                gameData.id, perm.getCard().getName(), matchCount, countCheck.minCount());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof CardsInHandAtLeast handCheck) {
                    // Intervening-if: only trigger if controller has enough cards in hand (Imaginary Pet)
                    if (conditionEvaluationService.isMet(gameData, handCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: hand >= {})",
                                gameData.id, perm.getCard().getName(), handCheck.threshold());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ActivePlayerHandEmpty handEmptyCheck) {
                    // Intervening-if: only trigger if the active player (controller, on their own
                    // upkeep) has no cards in hand (Hollowborn Barghest)
                    if (conditionEvaluationService.isMet(gameData, handEmptyCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: active player has no cards in hand)",
                                gameData.id, perm.getCard().getName());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof CardsInLibraryAtLeast libraryCheck) {
                    // Intervening-if: only trigger if controller has enough cards in library (Battle of Wits)
                    if (conditionEvaluationService.isMet(gameData, libraryCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: library >= {})",
                                gameData.id, perm.getCard().getName(), libraryCheck.threshold());
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof SourceCounterThreshold counterCheck) {
                    // Intervening-if: only trigger if the source permanent has enough counters of the
                    // given type (Helix Pinnacle — 100+ tower counters)
                    if (conditionEvaluationService.isMet(gameData, counterCheck,
                            ConditionContext.forPermanent(perm, activePlayerId))) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect)),
                                (UUID) null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {}+ {} counters)",
                                gameData.id, perm.getCard().getName(), counterCheck.threshold(), counterCheck.counterType());
                    }
                } else if (effect instanceof SurveilEffect) {
                    // Surveil is part of a compound triggered ability (e.g. "surveil 1, then if...").
                    // Group ALL upkeep effects into a single stack entry so they resolve sequentially.
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            activePlayerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(upkeepEffects),
                            (UUID) null,
                            perm.getId()
                    ));

                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                    log.info("Game {} - {} upkeep trigger pushed onto stack (surveil compound)",
                            gameData.id, perm.getCard().getName());
                    break; // All effects grouped into one entry
                } else if (effect instanceof WinGameIfCreaturesInGraveyardEffect winEffect) {
                    // Intervening-if: only trigger if condition is met
                    List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
                    long creatureCount = 0;
                    if (graveyard != null) {
                        creatureCount = graveyard.stream()
                                .filter(c -> c.hasType(CardType.CREATURE))
                                .count();
                    }
                    if (creatureCount >= winEffect.threshold()) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(effect))
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                        log.info("Game {} - {} upkeep trigger pushed onto stack (intervening-if met: {} creatures in graveyard)",
                                gameData.id, perm.getCard().getName(), creatureCount);
                    }
                } else if (effect.targetSpec().category().includesPermanents()) {
                    // Generic targeted-permanent upkeep trigger (e.g. Weed-Pruner Poplar's
                    // "target creature other than this creature gets -1/-1"). Target is chosen
                    // at trigger time (CR 603.3d) via a permanent choice.
                    gameData.queueInteraction(new PermanentChoiceContext.UpkeepPermanentTargetTrigger(
                            perm.getCard(), activePlayerId, new ArrayList<>(List.of(effect)), perm.getId()));
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            activePlayerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect)),
                            (UUID) null,
                            perm.getId()
                    ));

                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                    log.info("Game {} - {} upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        }

        // Energy Flux: "All artifacts have 'At the beginning of your upkeep, sacrifice this artifact
        // unless you pay {N}.'" The grant is global (any controller's Energy Flux), and the granted
        // ability fires on each artifact's controller's own upkeep — so during the active player's
        // upkeep, push a pay-or-sacrifice trigger sourced at each artifact they control.
        handleGrantedArtifactSacrificeTriggers(gameData, activePlayerId, battlefield);

        List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> upkeepEffects = card.getEffects(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED);
                if (upkeepEffects == null || upkeepEffects.isEmpty()) continue;

                for (CardEffect effect : upkeepEffects) {
                    CardEffect innerEffect = effect;

                    // Unwrap intervening-if conditional — check the gate at trigger time before
                    // offering the ability (Kuldotha Phoenix metalcraft, Rekindled Flame's "if an
                    // opponent has no cards in hand")
                    if (innerEffect instanceof ConditionalEffect conditional
                            && (conditional.condition() instanceof Metalcraft
                                    || conditional.condition() instanceof AnOpponentHandEmpty
                                    || conditional.condition() instanceof CardsAboveSelfInGraveyard)) {
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                new ConditionContext(activePlayerId, null, null, card, false, false, null, 0, null, null, false))) {
                            log.info("Game {} - {} graveyard upkeep ability skipped ({})",
                                    gameData.id, card.getName(), conditional.condition().conditionNotMetReason());
                            continue;
                        }
                        innerEffect = conditional.wrapped();
                    }

                    if (innerEffect instanceof MayPayManaEffect mayPay) {
                        gameData.queueMayAbility(card, activePlayerId, mayPay, null);
                    } else if (innerEffect instanceof MayEffect may) {
                        gameData.queueMayAbility(card, activePlayerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                activePlayerId,
                                card.getName() + "'s upkeep ability",
                                new ArrayList<>(List.of(innerEffect))
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, "'s upkeep ability triggers."));
                        log.info("Game {} - {} graveyard upkeep trigger pushed onto stack", gameData.id, card.getName());
                    }
                }
            }
        }

        // Check all battlefields for EACH_UPKEEP_TRIGGERED effects
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> eachUpkeepEffects = perm.getCard().getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED);
            if (eachUpkeepEffects == null || eachUpkeepEffects.isEmpty()) return;

            for (CardEffect effect : eachUpkeepEffects) {
                // Intervening-if: werewolf transform conditions checked at trigger time
                if (effect instanceof ConditionalEffect conditional
                        && (conditional.condition() instanceof NoSpellsCastLastTurn
                                || conditional.condition() instanceof TwoOrMoreSpellsCastLastTurn)) {
                    if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, playerId))) {
                        continue;
                    }
                }

                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), playerId, may, null, perm.getId());
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect)),
                            activePlayerId,
                            perm.getId()
                    ));
                }

                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                log.info("Game {} - {} each-upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        });

        // Check all battlefields for OPPONENT_UPKEEP_TRIGGERED effects (only opponents of the active player)
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) return; // Skip the active player's own permanents

            for (Permanent perm : playerBattlefield) {
                List<CardEffect> opponentUpkeepEffects = perm.getCard().getEffects(EffectSlot.OPPONENT_UPKEEP_TRIGGERED);
                if (opponentUpkeepEffects == null || opponentUpkeepEffects.isEmpty()) continue;

                for (CardEffect effect : opponentUpkeepEffects) {
                    // Intervening-if: check condition at trigger time
                    if (effect instanceof DealDamageIfFewCardsInHandEffect fewCardsEffect) {
                        List<Card> hand = gameData.playerHands.get(activePlayerId);
                        int handSize = hand != null ? hand.size() : 0;
                        if (handSize > fewCardsEffect.maxCards()) {
                            continue; // Condition not met, don't trigger
                        }
                    }
                    // Intervening-if: only trigger if the active opponent has no cards in hand
                    // (Hollowborn Barghest's "if that player has no cards in hand")
                    if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof ActivePlayerHandEmpty handEmptyCheck
                            && !conditionEvaluationService.isMet(gameData, handEmptyCheck,
                                    ConditionContext.forPermanent(perm, playerId))) {
                        continue;
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect)),
                            activePlayerId,
                            (UUID) null
                    ));

                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                    log.info("Game {} - {} opponent-upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        });

        // Check all battlefields for auras with ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED effects
        // These fire during the enchanted permanent's controller's upkeep (e.g. Numbing Dose)
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedControllerUpkeepEffects = perm.getCard().getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED);
            if (enchantedControllerUpkeepEffects == null || enchantedControllerUpkeepEffects.isEmpty()) return;
            if (!perm.isAttached()) return;

            UUID enchantedPermanentControllerId = gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
            if (enchantedPermanentControllerId == null) return;
            if (!enchantedPermanentControllerId.equals(activePlayerId)) return;

            for (CardEffect effect : enchantedControllerUpkeepEffects) {
                // Bake the enchanted permanent's controller into effects that need it
                CardEffect effectForStack = effect;
                if (effect instanceof EnchantedCreatureControllerLosesLifeEffect e) {
                    effectForStack = new EnchantedCreatureControllerLosesLifeEffect(e.amount(), enchantedPermanentControllerId);
                }
                // DealDamageToPlayersEffect(ENCHANTED_PERMANENT_CONTROLLER) reads that player from the
                // stack entry's targetId (set below); the sacrifice/life-loss effects find their host
                // via the source aura, so baking the controller as targetId is safe for them.

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraOwnerId,
                        perm.getCard().getName() + "'s upkeep ability",
                        new ArrayList<>(List.of(effectForStack)),
                        enchantedPermanentControllerId,
                        perm.getId()
                ));

                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                log.info("Game {} - {} enchanted-permanent-controller upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        });

        // Check all battlefields for curses with ENCHANTED_PLAYER_UPKEEP_TRIGGERED effects
        // These fire during the enchanted player's upkeep (e.g. Curse of Oblivion, Curse of the Bloody Tome)
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedPlayerUpkeepEffects = perm.getCard().getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED);
            if (enchantedPlayerUpkeepEffects == null || enchantedPlayerUpkeepEffects.isEmpty()) return;
            if (!perm.isAttached()) return;

            // For curses, attachedTo is the enchanted player's UUID
            UUID enchantedPlayerId = perm.getAttachedTo();
            if (!enchantedPlayerId.equals(activePlayerId)) return;

            for (CardEffect effect : enchantedPlayerUpkeepEffects) {
                // Bake the enchanted player ID into effects that need it
                CardEffect effectForStack = effect;
                if (effect instanceof ExileGraveyardCardsEffect e && e.scope() == GraveyardExileScope.OWN) {
                    effectForStack = new ExileGraveyardCardsEffect(e.count(), GraveyardExileScope.OWN, null, enchantedPlayerId);
                }
                // DealDamageToPlayersEffect(ENCHANTED_PLAYER) reads the enchanted player from the
                // stack entry's targetId (set below), so no per-effect baking is needed here.

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraOwnerId,
                        perm.getCard().getName() + "'s upkeep ability",
                        new ArrayList<>(List.of(effectForStack)),
                        enchantedPlayerId,
                        perm.getId()
                ));

                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
                log.info("Game {} - {} enchanted-player upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        });

        // Check exiled cards with egg counters owned by the active player (e.g. Darigaaz Reincarnated).
        // "At the beginning of your upkeep, if this card is exiled with an egg counter on it,
        //  remove an egg counter from it. Then if it has no egg counters, return it to the battlefield."
        if (!gameData.exiledCardEggCounters.isEmpty()) {
            List<Card> exiledCards = gameData.getPlayerExiledCards(activePlayerId);
            if (!exiledCards.isEmpty()) {
                for (Card card : new ArrayList<>(exiledCards)) {
                    Integer eggCounters = gameData.exiledCardEggCounters.get(card.getId());
                    if (eggCounters != null && eggCounters > 0) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                activePlayerId,
                                card.getName() + "'s egg counter ability",
                                new ArrayList<>(List.of(new RemoveEggCounterFromExileAndReturnEffect(card.getId())))
                        ));

                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(card, "'s upkeep ability triggers (exiled with egg counters)."));
                        log.info("Game {} - {} egg counter upkeep trigger pushed onto stack", gameData.id, card.getName());
                    }
                }
            }
        }

        // Process upkeep any-target triggers first (e.g. Form of the Dragon, CR 603.3d)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepAnyTargetTrigger.class)) {
            processNextUpkeepAnyTargetTrigger(gameData);
            return;
        }

        // Process upkeep multi-player-targeted triggers first (e.g. Axis of Mortality, CR 603.3d)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger.class)) {
            processNextUpkeepMultiPlayerTarget(gameData);
            return;
        }

        // Process upkeep player-targeted triggers (mandatory targeting at trigger time, CR 603.3d)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPlayerTargetTrigger.class)) {
            processNextUpkeepPlayerTarget(gameData);
            return;
        }

        // Process upkeep copy trigger target selection (mandatory targeting at trigger time)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class)) {
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class)) {
            processNextCapriciousEfreetTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
            processNextPucasMischiefTarget(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPermanentTargetTrigger.class)) {
            processNextUpkeepPermanentTarget(gameData);
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /** Counts the untapped lands the given player currently controls (layer-aware land check). */
    private int countUntappedLands(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent perm : battlefield) {
            if (!perm.isTapped()
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, perm, LAND_PREDICATE)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Energy Flux grants every artifact "At the beginning of your upkeep, sacrifice this artifact
     * unless you pay {N}." If any {@link AllArtifactsUpkeepSacrificeUnlessPayEffect} is on the
     * battlefield (regardless of who controls it), this pushes one
     * {@link ForcedCostOrElseEffect} pay-or-sacrifice trigger per artifact the active player
     * controls, sourced at that artifact so the "pay {N}" prompt and the {@link SacrificeSelfEffect}
     * penalty both act on the individual artifact.
     *
     * @param gameData       the current game state to modify
     * @param activePlayerId the player whose upkeep is being processed
     * @param battlefield    the active player's battlefield
     */
    private void handleGrantedArtifactSacrificeTriggers(GameData gameData, UUID activePlayerId,
                                                        List<Permanent> battlefield) {
        String manaCost = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AllArtifactsUpkeepSacrificeUnlessPayEffect grant) {
                        manaCost = grant.manaCost();
                        break;
                    }
                }
                if (manaCost != null) break;
            }
            if (manaCost != null) break;
        }
        if (manaCost == null) return;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (!gameQueryService.isArtifact(gameData, perm)) continue;

            ForcedCostOrElseEffect payOrSacrifice = new ForcedCostOrElseEffect(
                    new PayManaCost(manaCost),
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    true);
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    activePlayerId,
                    perm.getCard().getName() + "'s upkeep ability",
                    new ArrayList<>(List.of(payOrSacrifice)),
                    (UUID) null,
                    perm.getId()));

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s upkeep ability triggers."));
            log.info("Game {} - Energy Flux upkeep sacrifice trigger pushed for {}",
                    gameData.id, perm.getCard().getName());
        }
    }

    /**
     * Processes the next pending upkeep player-targeted trigger (e.g. Bloodgift Demon).
     * Presents the controller with a player choice; when selected, the trigger is
     * pushed onto the stack with all its effects sharing the chosen target.
     *
     * @param gameData the current game state to modify
     */
    /**
     * Processes the next pending upkeep any-target trigger (e.g. Form of the Dragon's
     * "deals 5 damage to any target"). Presents the controller with a choice among all valid
     * players and permanents; when selected, the trigger is pushed onto the stack with the
     * chosen target. Mandatory targeting at trigger time (CR 603.3d).
     *
     * @param gameData the current game state to modify
     */
    public void processNextUpkeepAnyTargetTrigger(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepAnyTargetTrigger.class)) {
            processNextUpkeepMultiPlayerTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepAnyTargetTrigger trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepAnyTargetTrigger.class);

        TargetFilter targetFilter = trigger.sourceCard().getTargetFilter();
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                trigger.effects(),
                targetFilter,
                trigger.controllerId(),
                trigger.sourceCard(),
                TriggerTargetCollector.Options.END_STEP);
        List<UUID> validTargets = result.validTargets();

        if (validTargets.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger has no valid targets."));
            log.info("Game {} - {} upkeep any-target trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            processNextUpkeepAnyTargetTrigger(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (result.canTargetPlayers() && result.canTargetPermanents()) {
            targetDescription = "any target";
        } else if (result.canTargetPlayers()) {
            targetDescription = "target player";
        } else {
            targetDescription = "target permanent";
        }

        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".");

        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger — choose " + targetDescription + "."));
        log.info("Game {} - {} upkeep any-target trigger awaiting target selection",
                gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending upkeep permanent-target trigger (e.g. Weed-Pruner Poplar's
     * "target creature other than this creature gets -1/-1"). Presents the controller with a
     * permanent choice honouring the source card's target filter; when selected, the trigger is
     * pushed onto the stack with the chosen target. Mandatory targeting at trigger time (CR 603.3d).
     *
     * @param gameData the current game state to modify
     */
    public void processNextUpkeepPermanentTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPermanentTargetTrigger.class)) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepPermanentTargetTrigger trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepPermanentTargetTrigger.class);

        TargetFilter targetFilter = trigger.sourceCard().getTargetFilter();
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                trigger.effects(),
                targetFilter,
                trigger.controllerId(),
                trigger.sourceCard(),
                TriggerTargetCollector.Options.END_STEP);
        List<UUID> validTargets = result.validTargets();

        if (validTargets.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger has no valid targets."));
            log.info("Game {} - {} upkeep permanent-target trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            processNextUpkeepPermanentTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else {
            targetDescription = "target permanent";
        }

        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".");

        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.cardThen(trigger.sourceCard(), "'s upkeep trigger — choose " + targetDescription + "."));
        log.info("Game {} - {} upkeep permanent-target trigger awaiting target selection",
                gameData.id, trigger.sourceCard().getName());
    }

    public void processNextUpkeepPlayerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepPlayerTargetTrigger.class)) {
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepPlayerTargetTrigger trigger = gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepPlayerTargetTrigger.class);

        // Honour the trigger's target filter (e.g. "target opponent") so the controller is not
        // offered as a valid target. A null filter (e.g. "target player") leaves all players eligible.
        List<UUID> validPlayerTargets = validTargetService.filterValidPlayerTargets(
                gameData, trigger.sourceCard().getTargetFilter(),
                new ArrayList<>(gameData.orderedPlayerIds), trigger.controllerId());

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(),
                List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose target player.");

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep trigger awaiting player target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending upkeep multi-player-targeted trigger (e.g. Axis of Mortality).
     * Presents the controller with a player choice for the first target; when selected,
     * a second target selection is initiated via {@code UpkeepSecondPlayerTargetTrigger}.
     */
    public void processNextUpkeepMultiPlayerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger.class)) {
            processNextUpkeepPlayerTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger trigger = gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger.class);

        List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(),
                List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose first target player.");

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep trigger awaiting first player target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the second player target for an upkeep multi-player-targeted trigger.
     * After the second target is selected, the trigger is pushed onto the stack with both targets.
     */
    public void processUpkeepSecondPlayerTarget(GameData gameData, PermanentChoiceContext.UpkeepSecondPlayerTargetTrigger trigger) {
        List<UUID> validPlayerTargets = new ArrayList<>(gameData.orderedPlayerIds);
        // Cannot target the same player twice
        validPlayerTargets.remove(trigger.firstTargetPlayerId());

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(),
                List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s ability — Choose second target player.");

        log.info("Game {} - {} upkeep trigger awaiting second player target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending upkeep copy-trigger target selection
     * (e.g. Clone Shell).  If no targets remain, continues to may-ability
     * processing.
     *
     * @param gameData the current game state to modify
     */
    public void processNextUpkeepCopyTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class)) {
            // All copy triggers targeted, continue with Capricious Efreet targets then may abilities
            processNextCapriciousEfreetTarget(gameData);
            return;
        }

        PermanentChoiceContext.UpkeepCopyTriggerTarget trigger = gameData.peekPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class);

        // Collect valid creature targets (excluding source permanent)
        List<UUID> validTargets = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(trigger.sourcePermanentId())) continue;
                if (gameQueryService.isCreature(gameData, p)) {
                    validTargets.add(p.getId());
                }
            }
        }

        if (validTargets.isEmpty()) {
            // No valid targets remaining — skip
            gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class);
            processNextUpkeepCopyTarget(gameData);
            return;
        }

        gameData.pollPendingInteraction(PermanentChoiceContext.UpkeepCopyTriggerTarget.class);
        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + " — Choose a creature to target.");

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep copy trigger awaiting target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending Capricious Efreet upkeep trigger target selection.
     * Step 1: controller chooses one nonland permanent they control.
     */
    public void processNextCapriciousEfreetTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class)) {
            processNextPucasMischiefTarget(gameData);
            return;
        }

        PermanentChoiceContext.CapriciousEfreetOwnTarget trigger = gameData.pollPendingInteraction(PermanentChoiceContext.CapriciousEfreetOwnTarget.class);

        // Collect valid own nonland permanents (Efreet itself is a valid target)
        List<UUID> validOwnTargets = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(trigger.controllerId());
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (!p.getCard().hasType(CardType.LAND)) {
                    validOwnTargets.add(p.getId());
                }
            }
        }

        if (validOwnTargets.isEmpty()) {
            // No valid own targets — skip this trigger
            processNextCapriciousEfreetTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validOwnTargets,
                trigger.sourceCard().getName() + " — Choose a nonland permanent you control.");

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep trigger awaiting own target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Processes the next pending Puca's Mischief upkeep trigger target selection.
     * Step 1: controller chooses one nonland permanent they control that has at least one legal
     * opponent pairing (an opponent nonland permanent with equal or lesser mana value).
     */
    public void processNextPucasMischiefTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class)) {
            processNextUpkeepPermanentTarget(gameData);
            return;
        }

        PermanentChoiceContext.PucasMischiefOwnTarget trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.PucasMischiefOwnTarget.class);

        // A nonland permanent you control is a legal first target only if some opponent nonland
        // permanent has mana value <= its own — i.e. own MV >= the smallest opponent MV.
        int minOpponentManaValue = Integer.MAX_VALUE;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(trigger.controllerId())) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (!p.getCard().hasType(CardType.LAND)) {
                    minOpponentManaValue = Math.min(minOpponentManaValue, p.getCard().getManaValue());
                }
            }
        }

        List<UUID> validOwnTargets = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(trigger.controllerId());
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (!p.getCard().hasType(CardType.LAND) && p.getCard().getManaValue() >= minOpponentManaValue) {
                    validOwnTargets.add(p.getId());
                }
            }
        }

        if (validOwnTargets.isEmpty()) {
            // No legal pair of targets — the trigger does nothing.
            log.info("Game {} - {} upkeep trigger skipped (no legal target pair)",
                    gameData.id, trigger.sourceCard().getName());
            processNextPucasMischiefTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validOwnTargets,
                trigger.sourceCard().getName() + " — Choose a nonland permanent you control.");

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(trigger.sourceCard(), "'s upkeep ability triggers."));
        log.info("Game {} - {} upkeep trigger awaiting own target selection (Puca's Mischief)",
                gameData.id, trigger.sourceCard().getName());
    }

    private void handleOpeningHandTriggers(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null) continue;

            for (Card card : hand) {
                List<CardEffect> openingHandEffects = card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL);
                if (openingHandEffects == null || openingHandEffects.isEmpty()) continue;

                for (CardEffect effect : openingHandEffects) {
                    // Leyline effects are handled during the pregame procedure
                    // (MulliganService.startGame), not during the first upkeep.
                    if (effect instanceof MayEffect may
                            && may.wrapped() instanceof LeylineStartOnBattlefieldEffect) {
                        continue;
                    }
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(card, playerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                playerId,
                                card.getName() + "'s opening hand ability",
                                new ArrayList<>(List.of(effect))
                        ));

                        String playerName = gameData.playerIdToName.get(playerId);
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.textCardText(playerName + " reveals ", card, " from their opening hand."));
                        log.info("Game {} - {} reveals {} from opening hand, trigger pushed onto stack",
                                gameData.id, playerName, card.getName());
                    }
                }
            }
        }
    }

    /**
     * Executes the draw step: the active player draws a card (rule 504.1),
     * unless it is turn 1 for the starting player (rule 103.7a).
     * Then scans for {@code DRAW_TRIGGERED} and {@code EACH_DRAW_TRIGGERED}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    public void handleDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;

        // The starting player skips their entire draw step on turn 1 (rule 103.7a)
        if (gameData.turnNumber == 1 && activePlayerId.equals(gameData.startingPlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips the draw (first turn).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        // A permanent may instruct its controller to skip their draw step (e.g. Colfenor's Plans).
        if (controlsSkipDrawStep(gameData, activePlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips their draw step.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} skips draw step (SkipDrawStepEffect)", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        // Island Sanctuary — "If you would draw a card during your draw step, instead you may skip
        // that draw." Offer the may-ability instead of the turn-based draw; declining draws normally,
        // accepting skips the draw and stamps the attack-restriction shield (handled by
        // MayMiscHandlerService.handleSingleDrawReplacementChoice). Detected by slot presence.
        Card islandSanctuary = findMaySkipDrawStepDrawSource(gameData, activePlayerId);
        if (islandSanctuary != null) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    islandSanctuary,
                    activePlayerId,
                    List.of(new ReplaceSingleDrawEffect(activePlayerId, DrawReplacementKind.ISLAND_SANCTUARY)),
                    "Skip your draw? Until your next turn you can only be attacked by creatures with flying and/or islandwalk."
            ));

            // Draw step triggered abilities (e.g. Howling Mine) still trigger at the beginning of the step.
            handleDrawStepTriggers(gameData);

            if (!gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                playerInputService.processNextMayAbility(gameData);
            }
            return;
        }

        // Normal draw (turn-based action, rule 504.1)
        drawService.resolveDrawCard(gameData, activePlayerId);

        // Check for draw step triggered abilities (e.g. Howling Mine)
        handleDrawStepTriggers(gameData);

        if (!gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    private Card findMaySkipDrawStepDrawSource(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return null;
        for (Permanent perm : battlefield) {
            if (!perm.getCard().getEffects(EffectSlot.MAY_SKIP_DRAW_STEP_DRAW).isEmpty()) {
                return perm.getCard();
            }
        }
        return null;
    }

    private boolean controlsSkipDrawStep(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof SkipDrawStepEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleDrawStepTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;

        // Nafs Asp: "that player loses N life at the beginning of their next draw step unless they
        // pay {M} before that draw step." Delayed trigger keyed to the damaged player's own draw
        // step — fired here as a "you may pay {M}; if you don't, lose N life" prompt controlled by
        // that player (paying avoids the loss, declining incurs it).
        if (gameData.hasDelayedAction(LoseLifeAtNextDrawStepUnlessPays.class)) {
            List<LoseLifeAtNextDrawStepUnlessPays> pending = gameData.drainDelayedActions(
                    LoseLifeAtNextDrawStepUnlessPays.class, a -> a.playerId().equals(activePlayerId));
            for (LoseLifeAtNextDrawStepUnlessPays action : pending) {
                ForcedCostOrElseEffect payOrLoseLife = new ForcedCostOrElseEffect(
                        new PayManaCost("{" + action.payAmount() + "}"),
                        new ArrayList<>(List.of(new LoseLifeEffect(action.lifeLoss()))),
                        true);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        activePlayerId,
                        action.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(payOrLoseLife))));

                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed ability triggers — " + gameData.playerIdToName.get(activePlayerId)
                        + " loses " + action.lifeLoss() + " life unless they pay {" + action.payAmount() + "}."));
                log.info("Game {} - {} delayed draw-step pay-or-lose-life trigger pushed for {}",
                        gameData.id, action.sourceCard().getName(), gameData.playerIdToName.get(activePlayerId));
            }
        }

        // Check active player's battlefield for DRAW_TRIGGERED effects (controller's own draw step only)
        List<Permanent> activeBattlefield = gameData.playerBattlefields.get(activePlayerId);
        if (activeBattlefield != null) {
            for (Permanent perm : activeBattlefield) {
                List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.DRAW_TRIGGERED);
                if (drawEffects == null || drawEffects.isEmpty()) continue;

                for (CardEffect effect : drawEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s draw step ability",
                                new ArrayList<>(List.of(effect)),
                                activePlayerId,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s draw step ability triggers."));
                        log.info("Game {} - {} draw-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        // Check all battlefields for EACH_DRAW_TRIGGERED effects (all players' draw steps)
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.EACH_DRAW_TRIGGERED);
            if (drawEffects == null || drawEffects.isEmpty()) return;

            for (CardEffect effect : drawEffects) {
                // Intervening-if: skip trigger if the effect requires an untapped source and it's tapped
                if (effect instanceof DrawCardForTargetPlayerEffect dcEffect
                        && dcEffect.requireSourceUntapped() && perm.isTapped()) {
                    continue;
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s draw step ability",
                        new ArrayList<>(List.of(effect)),
                        activePlayerId,
                        perm.getId()
                ));

                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s draw step ability triggers."));
                log.info("Game {} - {} draw-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * Fires Chancellor-style delayed mana triggers at the beginning of the
     * revealing player's first precombat main phase.
     *
     * @param gameData the current game state to modify
     */
    public void handlePrecombatMainTriggers(GameData gameData) {
        // Saga lore counters: add a lore counter to each Saga the active player controls (MTG Rule 714.3b)
        handleSagaLoreCounters(gameData);

        handlePrecombatMainBattlefieldTriggers(gameData);

        paradigmService.firePrecombatMainTriggers(gameData);

        // Chancellor-style delayed mana triggers: fire at the beginning of the revealing player's first main phase
        if (!gameData.openingHandManaTriggers.isEmpty()) {
            UUID activePlayerId = gameData.activePlayerId;
            List<OpeningHandRevealTrigger> toFire = gameData.openingHandManaTriggers.stream()
                    .filter(t -> t.revealingPlayerId().equals(activePlayerId))
                    .toList();

            if (!toFire.isEmpty()) {
                gameData.openingHandManaTriggers.removeAll(toFire);
                for (OpeningHandRevealTrigger trigger : toFire) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            trigger.sourceCard(),
                            trigger.revealingPlayerId(),
                            trigger.sourceCard().getName() + "'s ability",
                            new ArrayList<>(List.of(trigger.effect()))
                    ));

                    gameBroadcastService.logAndBroadcast(gameData,
                            GameLog.cardThen(trigger.sourceCard(), "'s delayed trigger fires — adds mana."));
                    log.info("Game {} - {}'s opening hand mana trigger fires for {}",
                            gameData.id, trigger.sourceCard().getName(),
                            gameData.playerIdToName.get(activePlayerId));
                }
            }
        }

        // Scattering Stroke's clash-win reward: at the beginning of the controller's next main phase,
        // they may add mana equal to the countered spell's mana value (snapshotted when it resolved).
        UUID mainPhasePlayerId = gameData.activePlayerId;
        List<AddManaAtNextMainPhase> manaRewards = gameData.drainDelayedActions(
                AddManaAtNextMainPhase.class, a -> a.controllerId().equals(mainPhasePlayerId));
        for (AddManaAtNextMainPhase reward : manaRewards) {
            MayEffect mayAddMana = new MayEffect(
                    new AwardManaEffect(reward.color(), reward.amount()),
                    "Add " + reward.amount() + " " + reward.color().getCode() + "?");
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    reward.sourceCard(),
                    reward.controllerId(),
                    reward.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(mayAddMana))
            ));

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(reward.sourceCard(), "'s delayed ability triggers."));
            log.info("Game {} - {}'s delayed mana reward fires for {}",
                    gameData.id, reward.sourceCard().getName(),
                    gameData.playerIdToName.get(mainPhasePlayerId));
        }
    }

    /**
     * Fires triggered abilities on permanents the active player controls at the
     * beginning of the precombat main phase (e.g. Abstract Paintmage).
     */
    private void handlePrecombatMainBattlefieldTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) {
            return;
        }

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.PRECOMBAT_MAIN_TRIGGERED);
            if (effects == null || effects.isEmpty()) {
                continue;
            }

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    activePlayerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            ));

            gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} precombat main trigger pushed onto stack",
                    gameData.id, perm.getCard().getName());
        }
    }

    /**
     * Adds a lore counter to each Saga the active player controls and triggers
     * the appropriate chapter ability (MTG Rule 714.3b).
     * Called at the beginning of the active player's precombat main phase.
     */
    private void handleSagaLoreCounters(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        // Collect Sagas first to avoid ConcurrentModificationException
        List<Permanent> sagas = battlefield.stream()
                .filter(p -> p.getCard().isSaga())
                .toList();

        for (Permanent saga : sagas) {
            Card card = saga.getCard();
            int newLoreCount = saga.getCounterCount(CounterType.LORE) + 1;
            saga.setCounterCount(CounterType.LORE, newLoreCount);

            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(card, " gets a lore counter (" + newLoreCount + ")."));
            log.info("Game {} - {} gets lore counter {}", gameData.id, card.getName(), newLoreCount);

            // Trigger the appropriate chapter ability
            EffectSlot chapterSlot = switch (newLoreCount) {
                case 1 -> EffectSlot.SAGA_CHAPTER_I;
                case 2 -> EffectSlot.SAGA_CHAPTER_II;
                case 3 -> EffectSlot.SAGA_CHAPTER_III;
                default -> null;
            };
            if (chapterSlot == null) continue;

            List<CardEffect> chapterEffects = card.getEffects(chapterSlot);
            if (chapterEffects.isEmpty()) continue;

            String chapterName = switch (newLoreCount) {
                case 1 -> "I";
                case 2 -> "II";
                case 3 -> "III";
                default -> String.valueOf(newLoreCount);
            };

            boolean needsPermanentTarget = chapterEffects.stream().anyMatch(e -> e.targetSpec().category().includesPermanents());
            boolean needsGraveyardTarget = chapterEffects.stream().anyMatch(e -> e.targetSpec().category().isGraveyard());
            if (needsPermanentTarget) {
                gameData.queueInteraction(
                        new PermanentChoiceContext.SagaChapterTarget(card, activePlayerId,
                                new ArrayList<>(chapterEffects), saga.getId(), chapterName,
                                card.getSagaChapterTargetFilters(chapterSlot)));
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers (awaiting target selection)", gameData.id, card.getName(), chapterName);
            } else if (needsGraveyardTarget) {
                gameData.queueInteraction(
                        new PermanentChoiceContext.SagaChapterGraveyardTarget(card, activePlayerId,
                                new ArrayList<>(chapterEffects), saga.getId(), chapterName));
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers (awaiting graveyard target selection)", gameData.id, card.getName(), chapterName);
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        activePlayerId,
                        card.getName() + "'s chapter " + chapterName + " ability",
                        new ArrayList<>(chapterEffects),
                        null,
                        saga.getId()
                ));

                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
                log.info("Game {} - {} chapter {} triggers", gameData.id, card.getName(), chapterName);
            }
        }

        // Process any queued saga chapter target selections
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class)) {
            triggerCollectionService.processNextSagaChapterGraveyardTarget(gameData);
        }
    }

    /**
     * Returns exiled cards scheduled for the given step from exile to the battlefield
     * under their owner's control.
     */
    public void processPendingExileReturns(GameData gameData, TurnStep step) {
        List<PendingExileReturn> matching =
                gameData.drainDelayedActions(PendingExileReturn.class, p -> p.returnStep() == step);
        if (matching.isEmpty()) {
            return;
        }

        for (PendingExileReturn pending : matching) {
            Card card = pending.card();
            UUID controllerId = pending.controllerId();
            gameData.removeFromExile(card.getId());
            Permanent perm = new Permanent(card);
            if (pending.returnTapped()) {
                perm.tap();
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(card, " returns to the battlefield under " + playerName + "'s control."));
            log.info("Game {} - {} returns from exile for {}", gameData.id, card.getName(), playerName);
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
        }
    }

    /**
     * Processes end-step triggers: exiles pending tokens (e.g. Mimic Vat),
     * then scans battlefields for
     * {@code END_STEP_TRIGGERED} and {@code CONTROLLER_END_STEP_TRIGGERED}
     * abilities.
     *
     * @param gameData the current game state to modify
     */
    public void handleEndStepTriggers(GameData gameData) {
        // Perform the scheduled end-step zone changes: token exiles (e.g. Mimic Vat), nontoken
        // exiles (e.g. Dark Maze), sacrifices (e.g. Choreographed Sparks' creature-copy token) and
        // destructions (e.g. Stone Giant).
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.EXILE_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.SACRIFICE_AT_END_STEP);
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.DESTROY_AT_END_STEP);

        // Process Siren's Call: destroy all non-Wall creatures the player controls that didn't attack
        // this turn, ignoring creatures they didn't control continuously since the beginning of the
        // turn (summoning sick).
        if (gameData.hasDelayedAction(DestroyNonAttackersAtEndStep.class)) {
            List<DestroyNonAttackersAtEndStep> pending =
                    gameData.drainDelayedActions(DestroyNonAttackersAtEndStep.class);
            for (DestroyNonAttackersAtEndStep action : pending) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(action.playerId());
                if (battlefield == null) continue;
                // Snapshot first: tryDestroyPermanent mutates the battlefield list.
                List<Permanent> toDestroy = new ArrayList<>();
                for (Permanent perm : battlefield) {
                    if (gameQueryService.isCreature(gameData, perm)
                            && !GameQueryService.permanentHasSubtype(perm, CardSubtype.WALL)
                            && !perm.isAttackedThisTurn()
                            && !perm.isSummoningSick()) {
                        toDestroy.add(perm);
                    }
                }
                for (Permanent perm : toDestroy) {
                    if (permanentRemovalService.tryDestroyPermanent(gameData, perm)) {
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), " is destroyed for not attacking."));
                        log.info("Game {} - {} destroyed by Siren's Call for not attacking",
                                gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        // Perform the scheduled end-step returns to hand (e.g. Dragon Mask)
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);

        // Process delayed "lose the game" triggers (e.g. Last Chance, Glorious End). These fire at the
        // beginning of the scheduling player's *own* next end step ("your next end step"): only entries
        // scheduled on an earlier turn (so the scheduling turn's own end step is skipped) AND only while
        // that player is the active player (so opponents' end steps are skipped). For the extra-turn
        // cards the very next turn is the controller's, so the loss lands on it; for Glorious End cast
        // on your own turn it skips the intervening opponent turn and lands on your next turn's end step.
        if (gameData.hasDelayedAction(LoseGameAtEndStep.class)) {
            List<LoseGameAtEndStep> toLose = gameData.drainDelayedActions(
                    LoseGameAtEndStep.class,
                    a -> gameData.turnNumber > a.registeredTurnNumber()
                            && a.playerId().equals(gameData.activePlayerId));
            for (LoseGameAtEndStep action : toLose) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        action.sourceCard(),
                        action.playerId(),
                        action.sourceCard().getName() + "'s delayed trigger — you lose the game",
                        new ArrayList<>(List.of(new TargetPlayerLosesGameEffect(action.playerId())))
                ));
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(action.sourceCard(),
                        "'s delayed trigger — " + gameData.playerIdToName.get(action.playerId()) + " loses the game."));
                log.info("Game {} - {} delayed lose-game trigger pushed onto stack",
                        gameData.id, action.sourceCard().getName());
            }
        }

        // Process delayed +1/+1 counter regrowth triggers (e.g. Protean Hydra)
        // Ruling: "If multiple +1/+1 counters are removed at once, its last ability will trigger that many times."
        // Each removed counter creates a separate delayed trigger that adds 2 +1/+1 counters.
        // The pending map stores countersRemoved * 2 (total counters to add), so we divide by 2
        // to get the number of individual triggers, each adding 2 counters.
        if (gameData.hasDelayedAction(DelayedPlusOneCounters.class)) {
            List<DelayedPlusOneCounters> pendingCounters =
                    gameData.drainDelayedActions(DelayedPlusOneCounters.class);
            for (DelayedPlusOneCounters counterEntry : pendingCounters) {
                UUID permanentId = counterEntry.permanentId();
                int totalCountersToAdd = counterEntry.totalCounters();
                Permanent perm = gameQueryService.findPermanentById(gameData, permanentId);
                if (perm == null) continue;
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanentId);
                if (controllerId == null) continue;

                int triggerCount = totalCountersToAdd / 2; // each trigger adds 2 counters
                for (int i = 0; i < triggerCount; i++) {
                    PutCountersOnSourceEffect effect = new PutCountersOnSourceEffect(1, 1, 2);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s delayed +1/+1 counter trigger",
                            new ArrayList<>(List.of(effect)),
                            null,
                            perm.getId()
                    ));
                }

                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(),
                        "'s delayed trigger — " + triggerCount + " trigger(s), adding " + totalCountersToAdd + " +1/+1 counter(s)."));
                log.info("Game {} - {} delayed +1/+1 counter regrowth: {} trigger(s) pushed onto stack", gameData.id, perm.getCard().getName(), triggerCount);
            }
        }

        // Process delayed +0/+1 counter triggers (e.g. Sacred Boon: one +0/+1 counter per 1 damage prevented).
        if (gameData.hasDelayedAction(DelayedPlusZeroPlusOneCounters.class)) {
            List<DelayedPlusZeroPlusOneCounters> pendingCounters =
                    gameData.drainDelayedActions(DelayedPlusZeroPlusOneCounters.class);
            for (DelayedPlusZeroPlusOneCounters counterEntry : pendingCounters) {
                int totalCountersToAdd = counterEntry.totalCounters();
                Permanent perm = gameQueryService.findPermanentById(gameData, counterEntry.permanentId());
                if (perm == null || totalCountersToAdd <= 0) continue;
                UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                if (controllerId == null) continue;

                PutCountersOnSourceEffect effect = new PutCountersOnSourceEffect(0, 1, totalCountersToAdd);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s delayed +0/+1 counter trigger",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                log.info("Game {} - {} delayed +0/+1 counter trigger: {} counter(s) pushed onto stack", gameData.id, perm.getCard().getName(), totalCountersToAdd);
            }
        }

        // Process delayed untap permanents triggers (e.g. Teferi, Hero of Dominaria +1)
        if (gameData.hasDelayedAction(DelayedUntapPermanents.class)) {
            List<DelayedUntapPermanents> pendingUntaps =
                    gameData.drainDelayedActions(DelayedUntapPermanents.class);
            for (DelayedUntapPermanents pending : pendingUntaps) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — untap up to " + pending.count() + " permanent(s)",
                        new ArrayList<>(List.of(new UntapUpToControlledPermanentsEffect(pending.count(), pending.filter())))
                ));
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s delayed trigger — untap up to " + pending.count() + " permanent(s)."));
                log.info("Game {} - {} delayed untap {} permanent(s) trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName(), pending.count());
            }
        }

        // Process delayed token creations (e.g. Rukh Egg)
        if (gameData.hasDelayedAction(DelayedCreateToken.class)) {
            List<DelayedCreateToken> pendingTokens =
                    gameData.drainDelayedActions(DelayedCreateToken.class);
            for (DelayedCreateToken pending : pendingTokens) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pending.sourceCard(),
                        pending.controllerId(),
                        pending.sourceCard().getName() + "'s delayed trigger — create token",
                        new ArrayList<>(List.of(pending.tokenEffect()))
                ));
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(pending.sourceCard(), "'s delayed trigger — create token."));
                log.info("Game {} - {} delayed token creation trigger pushed onto stack",
                        gameData.id, pending.sourceCard().getName());
            }
        }

        // Process delayed graveyard-to-hand returns (e.g. Tiana, Ship's Caretaker)
        if (gameData.hasDelayedAction(DelayedGraveyardToHandReturn.class)) {
            List<DelayedGraveyardToHandReturn> pendingReturns =
                    gameData.drainDelayedActions(DelayedGraveyardToHandReturn.class);
            for (DelayedGraveyardToHandReturn pending : pendingReturns) {
                List<Card> graveyard = gameData.playerGraveyards.get(pending.ownerId());
                if (graveyard == null) continue;
                Card cardToReturn = null;
                for (Card card : graveyard) {
                    if (card.getId().equals(pending.cardId())) {
                        cardToReturn = card;
                        break;
                    }
                }
                if (cardToReturn != null) {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
                    gameData.addCardToHand(pending.ownerId(), cardToReturn);
                    String playerName = gameData.playerIdToName.get(pending.ownerId());
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(cardToReturn,
                            " returns to " + playerName + "'s hand (delayed trigger)."));
                    log.info("Game {} - {} returns to {}'s hand from graveyard (delayed end-step trigger)",
                            gameData.id, cardToReturn.getName(), playerName);
                } else {
                    log.info("Game {} - Delayed graveyard-to-hand return for card {} skipped (no longer in graveyard)",
                            gameData.id, pending.cardId());
                }
            }
        }

        // Process delayed exile-to-hand returns (e.g. Necropotence). Only the controller's own end
        // step returns their set-aside cards ("your next end step"), so filter by the active player.
        if (gameData.hasDelayedAction(ReturnExiledCardToHandAtEndStep.class)) {
            List<ReturnExiledCardToHandAtEndStep> pendingReturns = gameData.drainDelayedActions(
                    ReturnExiledCardToHandAtEndStep.class,
                    a -> gameData.activePlayerId != null && gameData.activePlayerId.equals(a.ownerId()));
            for (ReturnExiledCardToHandAtEndStep pending : pendingReturns) {
                var exiledEntry = gameData.findExiledCard(pending.cardId());
                if (exiledEntry == null) {
                    log.info("Game {} - Delayed exile-to-hand return for card {} skipped (no longer in exile)",
                            gameData.id, pending.cardId());
                    continue;
                }
                Card cardToReturn = exiledEntry.card();
                gameData.removeFromExile(pending.cardId());
                gameData.addCardToHand(pending.ownerId(), cardToReturn);
                String playerName = gameData.playerIdToName.get(pending.ownerId());
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(cardToReturn, " returns to " + playerName + "'s hand (delayed trigger)."));
                log.info("Game {} - {} returns to {}'s hand from exile (delayed end-step trigger)",
                        gameData.id, cardToReturn.getName(), playerName);
            }
        }

        // Process delayed graveyard-to-battlefield transformed returns (e.g. Loyal Cathar)
        if (gameData.hasDelayedAction(DelayedGraveyardToBattlefieldTransformedReturn.class)) {
            List<DelayedGraveyardToBattlefieldTransformedReturn> pendingReturns =
                    gameData.drainDelayedActions(DelayedGraveyardToBattlefieldTransformedReturn.class);
            for (DelayedGraveyardToBattlefieldTransformedReturn pending : pendingReturns) {
                List<Card> graveyard = gameData.playerGraveyards.get(pending.ownerId());
                if (graveyard == null) continue;
                Card cardToReturn = null;
                for (Card card : graveyard) {
                    if (card.getId().equals(pending.cardId())) {
                        cardToReturn = card;
                        break;
                    }
                }
                if (cardToReturn == null) {
                    log.info("Game {} - Delayed transformed return for card {} skipped (no longer in graveyard)",
                            gameData.id, pending.cardId());
                    continue;
                }
                Card backFace = cardToReturn.getBackFaceCard();
                if (backFace == null) {
                    log.warn("Game {} - Delayed transformed return skipped for {} (no back face)",
                            gameData.id, cardToReturn.getName());
                    continue;
                }
                if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, com.github.laxika.magicalvibes.model.Zone.GRAVEYARD)) {
                    gameBroadcastService.logAndBroadcast(gameData,
                            GameLog.cardThen(cardToReturn, " can't return from the graveyard; it stays in the graveyard."));
                    continue;
                }

                permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
                Permanent permanent = new Permanent(cardToReturn);
                permanent.setCard(backFace);
                permanent.setTransformed(true);
                permanent.setEnteredFromGraveyardOwnerId(pending.ownerId());
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, pending.controllerId(), permanent);

                String playerName = gameData.playerIdToName.get(pending.controllerId());
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(cardToReturn,
                        " returns to the battlefield transformed as ", backFace,
                        " under " + playerName + "'s control."));
                log.info("Game {} - {} returns transformed as {} for {}",
                        gameData.id, cardToReturn.getName(), backFace.getName(), playerName);
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, pending.controllerId(), backFace, null, false);
            }
        }

        // Process delayed graveyard-to-battlefield-under-control returns (Seraph)
        if (gameData.hasDelayedAction(DelayedGraveyardToBattlefieldUnderControl.class)) {
            List<DelayedGraveyardToBattlefieldUnderControl> pendingReturns =
                    gameData.drainDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class);
            for (DelayedGraveyardToBattlefieldUnderControl pending : pendingReturns) {
                UUID ownerId = null;
                Card cardToReturn = null;
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Card> graveyard = gameData.playerGraveyards.get(pid);
                    if (graveyard == null) continue;
                    for (Card card : graveyard) {
                        if (card.getId().equals(pending.cardId())) {
                            cardToReturn = card;
                            ownerId = pid;
                            break;
                        }
                    }
                    if (cardToReturn != null) break;
                }
                if (cardToReturn == null) {
                    // No longer in a graveyard (moved/exiled/reanimated already) — you don't get it back.
                    log.info("Game {} - Seraph delayed return for card {} skipped (no longer in a graveyard)",
                            gameData.id, pending.cardId());
                    continue;
                }
                if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, com.github.laxika.magicalvibes.model.Zone.GRAVEYARD)) {
                    gameBroadcastService.logAndBroadcast(gameData,
                            GameLog.cardThen(cardToReturn, " can't return from the graveyard; it stays in the graveyard."));
                    continue;
                }

                permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
                Permanent permanent = new Permanent(cardToReturn);
                permanent.setEnteredFromGraveyardOwnerId(ownerId);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, pending.controllerId(), permanent);

                // When the returned card belongs to another player, the controller keeps it via a
                // permanent control effect (CR 613 layer 2) — without one it would revert to its owner
                // — and its ownership is recorded so it dies to its owner's graveyard.
                if (!pending.controllerId().equals(ownerId)) {
                    gameData.stolenCreatures.put(permanent.getId(), ownerId);
                    creatureControlService.applyControlEffect(gameData, pending.controllerId(), permanent,
                            new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                            ControlDuration.PERMANENT.toEffectDuration(), null, "Seraph");
                }

                String playerName = gameData.playerIdToName.get(pending.controllerId());
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(cardToReturn,
                        " returns to the battlefield under " + playerName + "'s control (Seraph)."));
                log.info("Game {} - {} returns under {}'s control (Seraph)", gameData.id, cardToReturn.getName(), playerName);
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, pending.controllerId(), cardToReturn, null, false);

                // Link to the Seraph for the control-loss sacrifice, but only if it is still on the
                // battlefield — if it already left, you never have to sacrifice the returned creature.
                if (gameQueryService.findPermanentById(gameData, pending.seraphPermanentId()) != null) {
                    gameData.seraphReturnedCreatures
                            .computeIfAbsent(pending.seraphPermanentId(), k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                            .add(permanent.getId());
                    gameData.seraphControlWatch.putIfAbsent(pending.seraphPermanentId(), pending.controllerId());
                }
            }
        }

        UUID activePlayerId = gameData.activePlayerId;
        List<UUID> triggerOrder = new ArrayList<>();
        triggerOrder.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                triggerOrder.add(playerId);
            }
        }

        for (UUID playerId : triggerOrder) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent perm : battlefield) {
                List<CardEffect> endStepEffects = perm.getCard().getEffects(EffectSlot.END_STEP_TRIGGERED);
                if (endStepEffects == null || endStepEffects.isEmpty()) continue;

                for (CardEffect effect : endStepEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), playerId, may);
                    } else if (effect instanceof DealDamageIfDidntCastSpellThisTurnEffect) {
                        // Intervening-if (CR 603.4): only trigger if the end-step player (the active
                        // player) didn't cast a spell this turn. Bake that player into targetId so the
                        // damage is dealt to them; re-checked at resolution. Impatience.
                        if (gameData.getSpellsCastThisTurnCount(activePlayerId) > 0) {
                            log.info("Game {} - {} end-step trigger skipped (active player cast a spell this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                activePlayerId,
                                perm.getId()
                        ));
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step didn't-cast-spell trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof SacrificeSelfAndReturnCardsExiledWithSourceEffect sacReturn) {
                        // Intervening-if: only trigger if N or more cards have been exiled with the
                        // source permanent (CR 603.4). Re-checked again at resolution.
                        if (gameData.getCardsExiledByPermanent(perm.getId()).size() < sacReturn.minCount()) {
                            log.info("Game {} - {} end-step trigger skipped (fewer than {} cards exiled)",
                                    gameData.id, perm.getCard().getName(), sacReturn.minCount());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step sacrifice-and-return trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof NotKicked) {
                        // Intervening-if: only trigger if the permanent was NOT kicked (CR 603.4)
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (was kicked)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step not-kicked trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof NoOtherPermanent) {
                        // Intervening-if: only trigger if controller has no other matching permanents (CR 603.4)
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (intervening-if failed: matching permanent present)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step no-other-permanent trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof AnyPlayerControlsPermanentCountAtMost) {
                        // Intervening-if: only trigger if at most N matching permanents exist across all
                        // battlefields (CR 603.4) — e.g. Pestilence sacrifices itself when no creatures remain.
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (intervening-if failed: matching permanents present)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step count-at-most trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof SelfDealtDamageToOpponentThisTurn) {
                        // Intervening-if: only trigger if this permanent dealt damage to an opponent
                        // this turn (CR 603.4) — Whirling Dervish. Re-checked at resolution.
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped (dealt no damage to an opponent this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step dealt-damage-to-opponent trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect morbid
                            && morbid.condition() instanceof Morbid) {
                        // Intervening-if: only trigger if morbid condition is met (CR 603.4)
                        if (!conditionEvaluationService.isMet(gameData, morbid.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step morbid trigger skipped (no creature died this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        CardEffect wrapped = morbid.wrapped();
                        if (wrapped.targetSpec().category().includesPermanents() || wrapped.targetSpec().category().includesPlayers()) {
                            // Targeting triggered ability — queue for target selection
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), playerId, new ArrayList<>(List.of(effect)), perm.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    playerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(effect)),
                                    null,
                                    perm.getId()
                            ));
                            gameBroadcastService.logAndBroadcast(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} end-step morbid trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.wrapped() instanceof MayEffect) {
                        // Intervening-if "you may" (CR 603.4), e.g. Sygg, River Cutthroat: only
                        // trigger if the condition holds at the beginning of the end step. The
                        // ConditionalEffect wrapper is pushed intact so resolution re-checks the
                        // condition (CR 603.4) and then prompts the optional "you may".
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            log.info("Game {} - {} end-step trigger skipped ({})",
                                    gameData.id, perm.getCard().getName(), conditional.conditionNotMetReason());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step conditional-may trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        // CONTROLLER_END_STEP_TRIGGERED: only fires for the active player's permanents
        List<Permanent> activeBattlefield = gameData.playerBattlefields.get(activePlayerId);
        if (activeBattlefield != null) {
            for (Permanent perm : activeBattlefield) {
                List<CardEffect> controllerEndStepEffects = perm.getCard().getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED);
                if (controllerEndStepEffects == null || controllerEndStepEffects.isEmpty()) continue;

                for (CardEffect effect : controllerEndStepEffects) {
                    if (effect instanceof ConditionalEffect raidEffect
                            && raidEffect.condition() instanceof Raid) {
                        // Intervening-if: only trigger if the controller attacked this turn
                        if (!conditionEvaluationService.isMet(gameData, raidEffect.condition(),
                                ConditionContext.forPermanent(perm, activePlayerId))) {
                            log.info("Game {} - {} end-step raid trigger skipped (didn't attack this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        CardEffect wrapped = raidEffect.wrapped();
                        if (wrapped instanceof MayEffect may) {
                            gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
                        } else if (wrapped.targetSpec().category().includesPermanents() || wrapped.targetSpec().category().includesPlayers()) {
                            // Raid condition met, targeting required — queue for target selection
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped)), perm.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    activePlayerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(wrapped)),
                                    null,
                                    perm.getId()
                            ));

                            gameBroadcastService.logAndBroadcast(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step raid trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect instanceof MayEffect may) {
                        if (perm.getCard().getTargetFilter() == null
                                && (may.targetSpec().category().includesPermanents()
                                    || may.targetSpec().category().includesPlayers())) {
                            // "You may" end-step trigger whose targeting is declared by the wrapped
                            // effect's targetSpec rather than a card-level TargetFilter (e.g. Goblin
                            // Razerunners' "you may have this creature deal damage ... to target player or
                            // planeswalker"). Queue for target selection first; the "you may" is honoured at
                            // resolution. Card-level-filter targeting (e.g. Wall of Reverence) instead falls
                            // through to queueMayAbility, which resolves its target after the may prompt.
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(may)), perm.getId()));
                            String logEntry = perm.getCard().getName() + "'s end step ability triggers.";
                            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                            log.info("Game {} - {} controller end-step targeting may-trigger queued", gameData.id, perm.getCard().getName());
                        } else {
                            gameData.queueMayAbility(perm.getCard(), activePlayerId, may);
                        }
                    } else if (effect instanceof DestroyRandomOpponentPermanentWithCounterEffect destroyRandom) {
                        // Intervening-if: only trigger if enough opponent permanents have the counter
                        int count = 0;
                        for (UUID pid : gameData.orderedPlayerIds) {
                            if (pid.equals(activePlayerId)) continue;
                            List<Permanent> opponentBf = gameData.playerBattlefields.get(pid);
                            if (opponentBf == null) continue;
                            for (Permanent p : opponentBf) {
                                int counterCount = switch (destroyRandom.counterType()) {
                                    case AIM -> p.getCounterCount(CounterType.AIM);
                                    case CHARGE -> p.getCounterCount(CounterType.CHARGE);
                                    default -> 0;
                                };
                                if (counterCount > 0) count++;
                            }
                        }
                        if (count < destroyRandom.minRequired()) {
                            log.info("Game {} - {} end-step trigger skipped (only {} permanents with {} counters, need {})",
                                    gameData.id, perm.getCard().getName(), count,
                                    destroyRandom.counterType().name().toLowerCase(), destroyRandom.minRequired());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof ControlsPermanentCount countCheck) {
                        // Intervening-if: only trigger if controller has enough matching permanents
                        List<Permanent> controllerBf = gameData.playerBattlefields.get(activePlayerId);
                        long matchCount = controllerBf == null ? 0 : controllerBf.stream()
                                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, countCheck.filter()))
                                .count();
                        if (!conditionEvaluationService.isMet(gameData, countCheck,
                                ConditionContext.forPermanent(perm, activePlayerId))) {
                            log.info("Game {} - {} end-step trigger skipped (only {} matching permanents, need {})",
                                    gameData.id, perm.getCard().getName(), matchCount, countCheck.minCount());
                            continue;
                        }
                        CardEffect countWrapped = conditional.wrapped();
                        if (countWrapped.targetSpec().category().includesPermanents()
                                || countWrapped.targetSpec().category().includesPlayers()) {
                            // Condition met and the inner effect targets (e.g. Exuberant Firestoker's
                            // "deal 2 damage to target player or planeswalker") — queue for target selection.
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(countWrapped)), perm.getId()));
                            gameBroadcastService.logAndBroadcast(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step targeting trigger queued", gameData.id, perm.getCard().getName());
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    activePlayerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(effect)),
                                    null,
                                    perm.getId()
                            ));

                            gameBroadcastService.logAndBroadcast(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof DidntAttack) {
                        // Intervening-if: only trigger if the creature didn't attack this turn
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, activePlayerId))) {
                            log.info("Game {} - {} end-step trigger skipped (attacked this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof AllOf) {
                        // Intervening-if (CR 603.4): only trigger if every sub-condition holds
                        // (Erg Raiders — "didn't attack this turn" and "not came under your control this turn")
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, activePlayerId))) {
                            log.info("Game {} - {} end-step trigger skipped ({} not met)",
                                    gameData.id, perm.getCard().getName(), conditional.condition().conditionName());
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    } else if (effect instanceof GainControlIfSubtypesDealtCombatDamageEffect subtypeEffect) {
                        // Intervening-if: check if any opponent was dealt combat damage by enough
                        // creatures of the required subtype this turn
                        boolean conditionMet = false;
                        for (UUID opponentId : gameData.orderedPlayerIds) {
                            if (opponentId.equals(activePlayerId)) continue;
                            int count = 0;
                            for (var dmgEntry : gameData.combatDamageToPlayersThisTurn.entrySet()) {
                                UUID permId = dmgEntry.getKey();
                                if (!dmgEntry.getValue().contains(opponentId)) continue;
                                Set<CardSubtype> subtypes = gameData.combatDamageSourceSubtypesThisTurn
                                        .getOrDefault(permId, Set.of());
                                if (subtypes.contains(subtypeEffect.subtype())
                                        || gameData.combatDamageSourcesWithChangelingThisTurn.contains(permId)) {
                                    count++;
                                }
                            }
                            if (count >= subtypeEffect.threshold()) {
                                conditionMet = true;
                                break;
                            }
                        }
                        if (!conditionMet) {
                            log.info("Game {} - {} end-step trigger skipped (no opponent dealt combat damage by {} or more {}s)",
                                    gameData.id, perm.getCard().getName(), subtypeEffect.threshold(),
                                    subtypeEffect.subtype().getDisplayName());
                            continue;
                        }
                        // Condition met — queue for targeting with GainControlOfTargetEffect.
                        // The card's targetFilter restricts to nonland opponent permanents.
                        gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                perm.getCard(), activePlayerId,
                                new ArrayList<>(List.of(new GainControlOfTargetEffect(ControlDuration.PERMANENT))),
                                perm.getId()));
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof GainedLifeThisTurn) {
                        // Intervening-if: only trigger if the controller gained life this turn (CR 603.4)
                        if (gameData.getLifeGainedThisTurn(activePlayerId) <= 0) {
                            log.info("Game {} - {} end-step trigger skipped (didn't gain life this turn)",
                                    gameData.id, perm.getCard().getName());
                            continue;
                        }
                        CardEffect wrapped = conditional.wrapped();
                        if (wrapped.targetSpec().category().isGraveyard()) {
                            // Graveyard-targeting trigger (e.g. Moseo) — queue for graveyard target selection
                            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped))));
                            gameBroadcastService.logAndBroadcast(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step graveyard-target trigger queued", gameData.id, perm.getCard().getName());
                        } else if (wrapped.targetSpec().category().includesPermanents() || wrapped.targetSpec().category().includesPlayers()) {
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped)), perm.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    activePlayerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(wrapped)),
                                    null,
                                    perm.getId()
                            ));
                            gameBroadcastService.logAndBroadcast(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect instanceof ConditionalEffect conditional
                            && (conditional.condition() instanceof CreatureDiedUnderYourControlThisTurn
                                || conditional.condition() instanceof CardsLeftGraveyardThisTurn)) {
                        // Intervening-if (CR 603.4): only trigger if the controller-scoped condition holds this turn
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, activePlayerId))) {
                            log.info("Game {} - {} end-step trigger skipped ({} not met)",
                                    gameData.id, perm.getCard().getName(), conditional.condition().conditionName());
                            continue;
                        }
                        CardEffect wrapped = conditional.wrapped();
                        if (wrapped.targetSpec().category().includesPermanents() || wrapped.targetSpec().category().includesPlayers()) {
                            gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                    perm.getCard(), activePlayerId, new ArrayList<>(List.of(wrapped)), perm.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    activePlayerId,
                                    perm.getCard().getName() + "'s end step ability",
                                    new ArrayList<>(List.of(wrapped)),
                                    null,
                                    perm.getId()
                            ));
                            gameBroadcastService.logAndBroadcast(gameData,
                                    GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                            log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                        }
                    } else if (effect.targetSpec().category().includesPermanents() || effect.targetSpec().category().includesPlayers()) {
                        // Targeting triggered ability — queue for target selection
                        gameData.queueInteraction(new PermanentChoiceContext.EndStepTriggerTarget(
                                perm.getCard(), activePlayerId, new ArrayList<>(List.of(effect)), perm.getId()));
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                activePlayerId,
                                perm.getCard().getName() + "'s end step ability",
                                new ArrayList<>(List.of(effect)),
                                null,
                                perm.getId()
                        ));

                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                        log.info("Game {} - {} controller end-step trigger pushed onto stack", gameData.id, perm.getCard().getName());
                    }
                }
            }
        }

        // Check all battlefields for auras with ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED
        // effects. These fire during the enchanted permanent's controller's end step (e.g. Nettlevine
        // Blight). The ability is controlled by the enchanted permanent's controller, so the stack
        // entry's controller is that player even though the Aura keeps its own controller.
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            List<CardEffect> enchantedControllerEndStepEffects =
                    perm.getCard().getEffects(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED);
            if (enchantedControllerEndStepEffects == null || enchantedControllerEndStepEffects.isEmpty()) return;
            if (!perm.isAttached()) return;

            UUID enchantedPermanentControllerId = gameQueryService.findPermanentController(gameData, perm.getAttachedTo());
            if (enchantedPermanentControllerId == null) return;
            if (!enchantedPermanentControllerId.equals(activePlayerId)) return;

            for (CardEffect effect : enchantedControllerEndStepEffects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        enchantedPermanentControllerId,
                        perm.getCard().getName() + "'s end step ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));

                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(perm.getCard(), "'s end step ability triggers."));
                log.info("Game {} - {} enchanted-permanent-controller end-step trigger pushed onto stack",
                        gameData.id, perm.getCard().getName());
            }
        });

        // Process pending end-step targeted triggers (e.g. Reaper from the Abyss morbid, Voltaic Servant)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.EndStepTriggerTarget.class)) {
            processNextEndStepTriggerTarget(gameData);
            return;
        }

        // Process pending end-step graveyard-target triggers (e.g. Moseo, Vein's New Dean)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Processes the next pending end-step targeted trigger.
     * Presents the controller with a permanent choice; when selected, the trigger is
     * pushed onto the stack with the chosen target.
     *
     * @param gameData the current game state to modify
     */
    public void processNextEndStepTriggerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.EndStepTriggerTarget.class)) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        PermanentChoiceContext.EndStepTriggerTarget trigger = gameData.pollPendingInteraction(PermanentChoiceContext.EndStepTriggerTarget.class);

        TargetFilter targetFilter = trigger.sourceCard().getTargetFilter();
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                trigger.effects(),
                targetFilter,
                trigger.controllerId(),
                trigger.sourceCard(),
                TriggerTargetCollector.Options.END_STEP);
        List<UUID> validTargets = result.validTargets();
        boolean canTargetPlayers = result.canTargetPlayers();
        boolean canTargetPermanents = result.canTargetPermanents();

        if (validTargets.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s end step trigger has no valid targets."));
            log.info("Game {} - {} end-step trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            // Try next pending trigger
            processNextEndStepTriggerTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (canTargetPlayers && canTargetPermanents) {
            targetDescription = "any target";
        } else if (canTargetPlayers) {
            targetDescription = "target player";
        } else {
            targetDescription = "target permanent";
        }

        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".");

        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.cardThen(trigger.sourceCard(), "'s end step trigger — choose " + targetDescription + "."));
        log.info("Game {} - {} end-step trigger awaiting target selection", gameData.id, trigger.sourceCard().getName());
    }

    /**
     * Scans the active player's battlefield for permanents with
     * {@code BEGINNING_OF_COMBAT_TRIGGERED} effects and pushes them onto the stack.
     * Only fires for the active player's permanents (CR 507.1: "At the beginning
     * of combat on your turn").
     *
     * @param gameData the current game state to modify
     */
    public void handleBeginningOfCombatTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> combatEffects = perm.getCard().getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED);
            if (combatEffects == null || combatEffects.isEmpty()) continue;

            // For equipment triggers, only fire if the equipment is attached to a creature
            if (perm.isAttached()) {
                Permanent equippedCreature = gameQueryService.findPermanentById(gameData, perm.getAttachedTo());
                if (equippedCreature == null || !gameQueryService.isCreature(gameData, equippedCreature)) {
                    continue;
                }
            }

            List<CardEffect> mayEffects = combatEffects.stream()
                    .filter(e -> e instanceof MayEffect)
                    .toList();
            List<CardEffect> mandatoryEffects = combatEffects.stream()
                    .filter(e -> !(e instanceof MayEffect))
                    .toList();

            for (CardEffect effect : mayEffects) {
                gameData.queueMayAbility(perm.getCard(), activePlayerId, (MayEffect) effect, null, perm.getId());
            }

            if (!mandatoryEffects.isEmpty()) {
                boolean needsPermanentTarget = mandatoryEffects.stream()
                        .anyMatch(e -> e.targetSpec().category().includesPermanents() || e.targetSpec().category().includesPlayers());
                boolean needsGraveyardTarget = mandatoryEffects.stream()
                        .anyMatch(e -> e.targetSpec().category().isGraveyard());
                if (needsGraveyardTarget) {
                    ExileGraveyardCardsEffect exileEffect = mandatoryEffects.stream()
                            .filter(e -> e instanceof ExileGraveyardCardsEffect ge
                                    && ge.scope() == GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD)
                            .map(e -> (ExileGraveyardCardsEffect) e)
                            .findFirst()
                            .orElseThrow();
                    graveyardTargetingService.handleBeginningOfCombatGraveyardTargeting(
                            gameData, activePlayerId, perm.getCard(), mandatoryEffects, perm.getId(), exileEffect);
                } else if (needsPermanentTarget) {
                    gameData.queueInteraction(
                            new PermanentChoiceContext.BeginningOfCombatTriggerTarget(
                                    perm.getCard(), activePlayerId,
                                    new ArrayList<>(mandatoryEffects), perm.getId()));
                    gameBroadcastService.logAndBroadcast(gameData,
                            GameLog.cardThen(perm.getCard(), "'s beginning of combat ability triggers."));
                    log.info("Game {} - {} beginning-of-combat trigger queued for targeting",
                            gameData.id, perm.getCard().getName());
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            activePlayerId,
                            perm.getCard().getName() + "'s combat ability",
                            new ArrayList<>(mandatoryEffects),
                            (UUID) null,
                            perm.getId()
                    ));

                    gameBroadcastService.logAndBroadcast(gameData,
                            GameLog.cardThen(perm.getCard(), "'s beginning of combat ability triggers."));
                    log.info("Game {} - {} beginning-of-combat trigger pushed onto stack",
                            gameData.id, perm.getCard().getName());
                }
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.BeginningOfCombatTriggerTarget.class)) {
            processNextBeginningOfCombatTriggerTarget(gameData);
            return;
        }

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Processes the next pending beginning-of-combat targeted trigger.
     * Presents the controller with a permanent choice; when selected, the trigger is
     * pushed onto the stack with the chosen target.
     */
    public void processNextBeginningOfCombatTriggerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.BeginningOfCombatTriggerTarget.class)) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        PermanentChoiceContext.BeginningOfCombatTriggerTarget trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.BeginningOfCombatTriggerTarget.class);

        TargetFilter targetFilter = trigger.sourceCard().getTargetFilter();
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData,
                trigger.effects(),
                targetFilter,
                trigger.controllerId(),
                trigger.sourceCard(),
                TriggerTargetCollector.Options.END_STEP);
        List<UUID> validTargets = result.validTargets();

        if (validTargets.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s beginning of combat trigger has no valid targets."));
            log.info("Game {} - {} beginning-of-combat trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            processNextBeginningOfCombatTriggerTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);

        String targetDescription;
        if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
            targetDescription = ppf.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (result.canTargetPlayers() && result.canTargetPermanents()) {
            targetDescription = "any target";
        } else if (result.canTargetPlayers()) {
            targetDescription = "target player";
        } else {
            targetDescription = "target permanent";
        }

        playerInputService.beginPermanentChoice(gameData, trigger.controllerId(), validTargets,
                trigger.sourceCard().getName() + "'s ability — Choose " + targetDescription + ".");

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(trigger.sourceCard(),
                "'s beginning of combat trigger — choose " + targetDescription + "."));
        log.info("Game {} - {} beginning-of-combat trigger awaiting target selection",
                gameData.id, trigger.sourceCard().getName());
    }
}

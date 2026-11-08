package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreaturePowerBranchEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureSourcePowerBranchEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MoveChosenCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenDealDamageToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentLosesLifeEqualToPowerToughnessDifferenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Trigger collectors for enter-the-battlefield events. Mirrors the other {@code *CollectorService}
 * beans: each {@link CollectsTrigger}-annotated method handles one (slot, effect class) pair and the
 * per-slot {@code CardEffect.class} default puts the effect onto the stack. The scan orchestration
 * (which permanents/slots to visit, self-exclusion, conditional gating, Naban doubling, the
 * "skip targeting effects" rule) lives in {@link TriggerCollectionService}.
 */
@Slf4j
@Service
public class EnterTriggerCollectorService {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;

    public EnterTriggerCollectorService(GameLogService gameLogService,
                                        GameQueryService gameQueryService,
                                        GraveyardTargetingService graveyardTargetingService,
                                        PredicateEvaluationService predicateEvaluationService,
                                        ConditionEvaluationService conditionEvaluationService) {
        this.gameLogService = gameLogService;
        this.gameQueryService = gameQueryService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
    }

    // ── Default "put it on the stack" fallbacks (one per registry-backed slot) ─────────

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD)
    private boolean handleAnyPermanentEnterDefault(TriggerMatchContext match, CardEffect effect,
                                                    TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (effect instanceof MayEffect may && (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || may.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            // "You may have target player …": the target is chosen as the ability goes on the stack (CR 603.3d).
            return handleEnterMay(match, may, new TriggerContext.PermanentEnters(pe.enteringCard(),
                    pe.enteringControllerId(), null, pe.perEffectTriggerCount(), pe.mayPayTargetCardId()));
        }
        return enqueueAnyPermanentEnter(match, effect, pe);
    }

    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_ALLY_ROOM_FULLY_UNLOCKED)
    private boolean handleRoomFullyUnlockedDefault(TriggerMatchContext match, CardEffect effect,
                                                    TriggerContext ctx) {
        TriggerContext.RoomFullyUnlocked room = (TriggerContext.RoomFullyUnlocked) ctx;
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    match.permanent().getId(), room.roomPermanentId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} triggers when a Room is fully unlocked, awaiting target",
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
                match.permanent().getId());
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        entry.setTriggeringPermanentId(room.roomPermanentId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers when a Room is fully unlocked",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    private boolean enqueueAnyPermanentEnter(TriggerMatchContext match, CardEffect effect,
                                             TriggerContext.PermanentEnters pe) {
        if (effect instanceof MayPayManaEffect mayPay) {
            return handleEnterMayPay(match, mayPay, pe);
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
            if (enteringPermanentId == null) {
                return true;
            }
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        match.permanent().getCard(), match.controllerId(),
                        new ArrayList<>(List.of(effect)), match.permanent().getId(),
                        enteringPermanentId, match.permanent().getId(), null, false, null, null,
                        pe.enteringControllerId()));
            }
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            log.info("Game {} - {} any-permanent-enters trigger awaiting target selection",
                    match.gameData().id, match.permanent().getCard().getName());
            return true;
        }
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    pe.defaultTargetPlayerId() != null ? pe.defaultTargetPlayerId() : pe.enteringControllerId(),
                    match.permanent().getId());
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentId(pe.mayPayTargetCardId());
            entry.setTriggeringPermanentControllerId(pe.enteringControllerId());
            entry.setTriggeringCardId(pe.enteringCard().getId());
            entry.setTriggeringCardSnapshot(pe.enteringCard());
            match.gameData().stack.add(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} any-permanent-enters trigger queued", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

    private boolean usesTriggeringPermanentManaValue(CardEffect effect) {
        return effect instanceof TriggeringPermanentManaValueEffect valueEffect
                && valueEffect.usesTriggeringPermanentManaValue();
    }

    private Permanent findEnteringPermanent(com.github.laxika.magicalvibes.model.GameData gameData,
                                            TriggerContext.PermanentEnters pe) {
        if (pe.mayPayTargetCardId() == null) return null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(pe.enteringControllerId());
        if (battlefield == null) return null;
        return battlefield.stream()
                .filter(permanent -> pe.mayPayTargetCardId().equals(permanent.getId()))
                .findFirst()
                .orElse(null);
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURES_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_TRANSFORMED)
    private boolean handleEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                && !effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        match.permanent().getCard(), match.controllerId(), List.of(effect)));
            }
            logTriggered(match);
            return true;
        }
        // A targeting effect can't be pushed straight onto the stack with a pre-set target — queue a
        // pending choice so the controller picks one as the ability goes on the stack (CR 603.3d).
        // Permanent-targeting effects (e.g. Reaper King's "destroy target permanent") always route
        // through the choice; player-targeting ones only when the scan left the player unset — the
        // opponent-enters scans bake in the entering permanent's controller, while the ally scans
        // leave it null (Sage's Row Denizen's "target player mills two cards").
        boolean needsTargetChoice = effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || (effect.targetSpec().admits(TargetPredicate.Kind.PLAYER) && pe.defaultTargetPlayerId() == null);
        if (needsTargetChoice) {
            Card sourceCard = match.permanent().getCard();
            // The entering permanent rides along for effects phrased around "that creature"
            // (Gruul Ragebeast's fight); target-only effects such as Reaper King's ignore it.
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
                if (sourceCard.getTargetGroups().size() > 1) {
                    match.gameData().queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                            sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                            match.permanent().getId(), List.of(), 0, 0, List.of(), 0, List.of(),
                            false, pe.enteringCard().getId(), enteringPermanentId));
                } else {
                    match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                            sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                            match.permanent().getId(), enteringPermanentId));
                }
            }
            logTriggered(match);
            return true;
        }
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        enqueue(match, effect, pe.defaultTargetPlayerId(), pe.perEffectTriggerCount(), enteringPermanentId);
        if (effect instanceof CreateTokenCopyOfEnteringPermanentEffect && enteringPermanentId != null) {
            // The copy is made from last known information when the entering creature has already left.
            for (StackEntry queued : match.gameData().stack) {
                if (enteringPermanentId.equals(queued.getTriggeringPermanentId())
                        && queued.getEffectsToResolve().contains(effect)) {
                    queued.rememberLastKnownPermanentCard(enteringPermanentId, pe.enteringCard());
                }
            }
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = ChooseModeNotYetChosenEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureEnterModal(TriggerMatchContext match,
                                                   ChooseModeNotYetChosenEffect effect,
                                                   TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    match.permanent().getCard(), match.controllerId(), new ChooseOneEffect(effect.options()),
                    match.permanent().getId(), true, pe.enteringCard().getId()));
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = ChooseOneAtTriggerTimeEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = ChooseOneAtTriggerTimeEffect.class,
            watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = ChooseOneAtTriggerTimeEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = ChooseOneAtTriggerTimeEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureEnterModalAtTriggerTime(TriggerMatchContext match,
                                                               ChooseOneAtTriggerTimeEffect effect,
                                                               TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    match.permanent().getCard(), match.controllerId(), effect.choice(),
                    match.permanent().getId(), false, false, pe.enteringCard().getId(), null,
                    enteringPermanentId));
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = ChooseModeNotYetChosenThisTurnEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = ChooseModeNotYetChosenThisTurnEffect.class,
            watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureEnterTurnScopedModal(TriggerMatchContext match,
                                                   ChooseModeNotYetChosenThisTurnEffect effect,
                                                   TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    match.permanent().getCard(), match.controllerId(), new ChooseOneEffect(effect.options()),
                    match.permanent().getId(), true, pe.enteringCard().getId()));
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = EndureEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyNontokenCreatureEndure(TriggerMatchContext match,
                                                     EndureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId());
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setTriggeringCardId(pe.enteringCard().getId());
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = ReturnTargetCardsFromGraveyardToHandEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = ReturnTargetCardsFromGraveyardToHandEffect.class,
                    watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleCreatureEnterReturnToHand(TriggerMatchContext match,
                                                     ReturnTargetCardsFromGraveyardToHandEffect effect,
                                                     TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            graveyardTargetingService.handleReturnToHandETBTargeting(
                    match.gameData(), match.controllerId(), sourceCard, List.of(effect), effect);
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = ExileGraveyardCardsEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = ExileGraveyardCardsEffect.class,
                    watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleCreatureEnterExileGraveyardCard(TriggerMatchContext match,
                                                           ExileGraveyardCardsEffect effect,
                                                           TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            graveyardTargetingService.handleGraveyardCardsExileETBTargeting(
                    match.gameData(), match.controllerId(), sourceCard, List.of(effect), effect);
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = BecomeSaddledUntilEndOfTurnEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = BecomeSaddledUntilEndOfTurnEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = AnimatePermanentsEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD)
    private boolean handleEnteringPermanentSelfEffect(TriggerMatchContext match, CardEffect effect,
                                                       TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        // An animation's scope says which permanent it animates: this one, or the one that entered (Kolodin,
        // Triumph Caster's GrantScope.TRIGGERING_PERMANENT).
        UUID affectedPermanentId = effect instanceof AnimatePermanentsEffect
                ? match.permanent().getId()
                : enteringPermanentId;

        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    affectedPermanentId);
            entry.setTriggeringCardId(pe.enteringCard().getId());
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setNonTargeting(true);
            match.gameData().enqueueTrigger(entry);
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD)
    private boolean handleTokenEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.TokensEnter tokensEnter = (TriggerContext.TokensEnter) ctx;
        Card sourceCard = match.permanent().getCard();
        if (isTargeting(effect)) {
            for (int i = 0; i < tokensEnter.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                        match.permanent().getId()));
            }
            logTriggered(match);
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setEventValue(tokensEnter.count());
        entry.setNonTargeting(true);
        for (int i = 0; i < tokensEnter.perEffectTriggerCount(); i++) {
            match.gameData().enqueueTrigger(entry.copyForNewStackObject());
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD)
    private boolean handleOpponentTokenEnterCreateCopies(TriggerMatchContext match,
            CreateTokenCopyOfTargetPermanentEffect effect, TriggerContext ctx) {
        TriggerContext.TokensEnter tokensEnter = (TriggerContext.TokensEnter) ctx;
        if (tokensEnter.permanentIds().isEmpty()) {
            return false;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < tokensEnter.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    0,
                    null,
                    match.permanent().getId(),
                    Map.of(),
                    null,
                    tokensEnter.permanentIds(),
                    List.of());
            entry.setNonTargeting(true);
            for (UUID tokenId : tokensEnter.permanentIds()) {
                Permanent token = gameQueryService.findPermanentById(match.gameData(), tokenId);
                if (token != null) {
                    entry.rememberLastKnownPermanentCard(tokenId, token.getCard());
                }
            }
            match.gameData().enqueueTrigger(entry);
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = CreateTokenCopyOfEnteringTokenForTargetPlayerEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CreateTokenCopyOfEnteringTokenForTargetPlayerEffect.class,
                    watch = ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD)
    private boolean handleTokenEnterCopyForTargetPlayer(TriggerMatchContext match,
                                                         CreateTokenCopyOfEnteringTokenForTargetPlayerEffect effect,
                                                         TriggerContext ctx) {
        TriggerContext.TokensEnter tokensEnter = (TriggerContext.TokensEnter) ctx;
        if (tokensEnter.permanentIds().isEmpty()) {
            return false;
        }

        UUID enteringTokenId = tokensEnter.permanentIds().getFirst();
        MayEffect may = new MayEffect(effect, "Have target player create a token copy?");
        match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                match.permanent().getCard(), match.controllerId(), List.of(may),
                match.permanent().getId(), enteringTokenId, match.permanent().getId()));
        logTriggered(match);
        return true;
    }

    /**
     * The "any other creature enters" default queues the trigger directly when it needs no target
     * and routes targeted effects through the normal enter-trigger target choice.
     */
    @CollectsWatchTrigger(value = SacrificeSelfThenEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureSacrificeSelfThen(TriggerMatchContext match,
                                                        SacrificeSelfThenEffect effect,
                                                        TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (isTargeting(effect)) {
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                        match.permanent().getId(), enteringPermanentId));
            }
        } else {
            enqueue(match, effect, pe.defaultTargetPlayerId(), pe.perEffectTriggerCount(), enteringPermanentId);
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        boolean needsTargetChoice = isTargeting(effect);
        if (needsTargetChoice) {
            Card sourceCard = match.permanent().getCard();
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), match.permanent().getId(),
                        findEnteringPermanentId(match, pe.enteringCard())));
            }
            logTriggered(match);
            return true;
        }
        enqueue(match, effect, pe.defaultTargetPlayerId(), pe.perEffectTriggerCount(),
                findEnteringPermanentId(match, pe.enteringCard()));
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CREATURE_ENTERS_FROM_GRAVEYARD)
    private boolean handleCreatureEnterFromGraveyardDefault(TriggerMatchContext match,
                                                             CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        if (!isTargeting(effect)) {
            // "You may attach this Equipment to it" (Dredging Claw): "it" is the entry's triggering permanent.
            enqueue(match, effect, null, pe.perEffectTriggerCount(), enteringPermanentId);
            logTriggered(match);
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EnteringPermanentAnyTargetTrigger(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), enteringPermanentId));
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureSacrificeSelfCreateTokens(
            TriggerMatchContext match,
            SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect effect,
            TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId());
            entry.setEventValue(pe.enteringCard().getManaValue());
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (sacrifice and create tokens)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = SacrificeSelfThenDealDamageToEnteringCreatureEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureSacrificeSelfDealDamage(
            TriggerMatchContext match,
            SacrificeSelfThenDealDamageToEnteringCreatureEffect effect,
            TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId());
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setTriggeringCardId(pe.enteringCard().getId());
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (sacrifice and deal damage)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    // ── "May" wrappers (queued as a may-ability, not unwrapped onto the stack) ──────────

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleEnterMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.sourceCard();
        int eventValue = may.usesTriggeringPermanentManaValue()
                ? pe.enteringCard().getManaValue() : 0;
        boolean sourceIsEnteringPermanent = may instanceof TriggeringPermanentSourceEffect source
                && source.sourceIsTriggeringPermanent();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (sourceIsEnteringPermanent && enteringPermanentId == null) {
            return true;
        }
        // Always bind the source permanent so a "may put a counter on this creature" wrapper
        // (e.g. Godtracker of Jund) resolves against the source; ally scans leave the target
        // player unset (null), which is harmless for player-directed wrapped effects.
        if (may.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                && !may.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && !may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        sourceCard, match.controllerId(), List.of(may)));
            }
            logTriggered(match);
            return true;
        }
        boolean needsTargetChoice = may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || (may.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && pe.defaultTargetPlayerId() == null);
        if (needsTargetChoice) {
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                if (match.sourcePlanarObject() != null) {
                    Permanent enteringPermanent = gameQueryService.findPermanentById(
                            match.gameData(), enteringPermanentId);
                    match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard, match.controllerId(), new ArrayList<>(List.of(may)), false, null,
                            0, enteringPermanentId,
                            enteringPermanent == null ? null : new Permanent(enteringPermanent), false,
                            enteringPermanentId, null, pe.enteringControllerId(),
                            match.sourcePlanarObject().copy()));
                } else {
                    match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                            sourceCard, match.controllerId(), new ArrayList<>(List.of(may)),
                            sourceIsEnteringPermanent ? enteringPermanentId : match.permanent().getId(),
                            enteringPermanentId, sourceIsEnteringPermanent));
                }
            }
            logTriggered(match);
            return true;
        }
        boolean usesEnteringTarget = may.wrapped() instanceof DiscardCardThenEffect discard
                && discard.useEntryTarget();
        boolean targetsEnteringPermanent = may.wrapped().usesEnteringPermanentReference() || usesEnteringTarget;
        UUID mayTargetId = targetsEnteringPermanent ? enteringPermanentId : pe.defaultTargetPlayerId();
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    mayTargetId,
                    match.permanent().getId(),
                    eventValue,
                    match.markSourceOncePerTurnOnAcceptance(),
                    targetsEnteringPermanent && enteringPermanent != null ? gameQueryService.getEffectiveToughness(
                            match.gameData(), enteringPermanent) : null);
            StackEntry entry = match.gameData().stack.getLast();
            if (!isTargeting(may)) {
                // "You may attach this Equipment to it": "it" is the triggering permanent, never a target.
                entry.setNonTargeting(true);
            }
            if (may.wrapped().usesEnteringPermanentReference()) {
                entry.setNonTargeting(true);
                entry.setTriggeringCardId(pe.enteringCard().getId());
                entry.setTriggeringCardSnapshot(pe.enteringCard());
            }
            // The entering permanent is the event's: "that creature's power / toughness" reads it as the ability
            // resolves, or as it last existed on the battlefield (CR 608.2h).
            if (enteringPermanent != null) {
                entry.setTriggeringPermanentId(enteringPermanentId);
                entry.setTriggeringCardId(pe.enteringCard().getId());
                entry.setTriggeringCardSnapshot(pe.enteringCard());
                entry.setTriggeringPermanentPowerAtTrigger(gameQueryService.getEffectivePower(
                        match.gameData(), enteringPermanent));
                entry.setTriggeringPermanentToughnessAtTrigger(gameQueryService.getEffectiveToughness(
                        match.gameData(), enteringPermanent));
            }
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may effect)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD)
    private boolean handleTokenEnterMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {

        TriggerContext.TokensEnter tokensEnter = (TriggerContext.TokensEnter) ctx;
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < tokensEnter.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    null, match.permanent().getId(), match.markSourceOncePerTurnOnAcceptance());
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for tokens entering (may effect)",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ANY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureEnterMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        if (may.choicePlayer() == com.github.laxika.magicalvibes.model.MayChoicePlayer.CONTROLLER
                || may.targetSpec() != TargetSpec.NONE) {
            return handleEnterMay(match, may, ctx);
        }
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        if (enteringPermanent == null) {
            return true;
        }
        Card sourceCard = match.sourceCard();
        Integer enteringPower = gameQueryService.getEffectivePower(match.gameData(), enteringPermanent);
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(may)),
                    null,
                    match.permanent() == null ? null : match.permanent().getId());
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setTriggeringPermanentControllerId(pe.enteringControllerId());
            entry.setTriggeringCardId(pe.enteringCard().getId());
            entry.setTriggeringCardSnapshot(pe.enteringCard());
            entry.setTriggeringPermanentPowerAtTrigger(enteringPower);
            if (match.sourcePlanarObject() != null) {
                entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
            } else if (match.permanent() != null) {
                entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            }
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may effect for entering controller)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * Handles graft's non-targeting choice to move a counter onto the creature that just entered.
     * The source controller makes the choice, even when the entering creature is controlled by an
     * opponent, and the source must still have a counter when the trigger is created.
     */
    @CollectsWatchTrigger(value = MoveCounterFromSourceToEnteringCreatureEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureMoveCounterToEntering(TriggerMatchContext match,
                                                            MoveCounterFromSourceToEnteringCreatureEffect effect,
                                                            TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (match.permanent().getCounterCount(effect.counterType()) <= 0) {
            return false;
        }

        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        String counterDescription = effect.counterType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                    sourceCard,
                    match.controllerId(),
                    List.of(effect),
                    sourceCard.getName() + " — Move a " + counterDescription + " counter onto "
                            + pe.enteringCard().getName() + "?",
                    enteringPermanentId,
                    null,
                    match.permanent().getId(),
                    null,
                    0,
                    0,
                    null,
                    null,
                    match.controllerId(),
                    new Permanent(match.permanent())));
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may move a counter onto it)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /** Handles a controller's choice of which counter to move onto an entering creature. */
    @CollectsWatchTrigger(value = MoveChosenCounterFromSourceToEnteringCreatureEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureMoveChosenCounterToEntering(
            TriggerMatchContext match, MoveChosenCounterFromSourceToEnteringCreatureEffect effect,
            TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                    sourceCard,
                    match.controllerId(),
                    List.of(effect),
                    sourceCard.getName() + " — Move a counter onto " + pe.enteringCard().getName() + "?",
                    enteringPermanentId,
                    null,
                    match.permanent().getId(),
                    null,
                    0,
                    0,
                    null,
                    null,
                    match.controllerId(),
                    new Permanent(match.permanent())));
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may move a chosen counter onto it)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = MayPayManaEffect.class,
                    watch = ZoneChangeWatch.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayPayManaEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayPayManaEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayPayManaEffect.class, watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayPayManaEffect.class, watch = ZoneChangeWatch.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayPayManaEffect.class, watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayPayManaEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayPayManaEffect.class, watch = ZoneChangeWatch.ON_SELF_OR_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayPayManaEffect.class, watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayPayManaEffect.class, watch = ZoneChangeWatch.COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleEnterMayPay(TriggerMatchContext match, MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent() != null ? match.permanent().getCard() : match.sourceCard();
        int eventValue = mayPay.wrapped() instanceof TriggeringPermanentManaValueEffect valueEffect
                && valueEffect.usesTriggeringPermanentManaValue()
                ? pe.enteringCard().getManaValue() : 0;
        if (mayPay.sourceIsTriggeringPermanent()) {
            UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
            if (enteringPermanentId == null) {
                return true;
            }
            if (mayPay.targetSpec() != TargetSpec.NONE) {
                for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                    match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                            sourceCard, match.controllerId(), new ArrayList<>(List.of(mayPay)),
                            enteringPermanentId, enteringPermanentId));
                }
            } else {
                match.gameData().queueMayAbility(sourceCard, match.controllerId(), mayPay,
                        pe.mayPayTargetCardId(), enteringPermanentId, eventValue);
            }
            logTriggered(match);
            log.info("Game {} - {} triggers for {} entering (may pay mana)",
                    match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
            return true;
        }
        UUID targetCardId = mayPay.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                ? pe.mayPayTargetCardId()
                : null;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        Permanent enteringPermanent = enteringPermanentId == null ? null
                : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        if (match.permanent() != null && mayPay.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && !mayPay.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            // The player target is chosen as the trigger goes on the stack (CR 603.3d); the payment is
            // decided on resolution (CR 603.5).
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        sourceCard, match.controllerId(), new ArrayList<>(List.of(mayPay)),
                        match.permanent().getId(), enteringPermanentId));
            }
            logTriggered(match);
            return true;
        }
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            UUID sourcePermanentId = match.permanent() == null ? null : match.permanent().getId();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard,
                    match.controllerId(), sourceCard.getName() + "'s ability", List.of(mayPay),
                    targetCardId, sourcePermanentId);
            entry.setEventValue(eventValue);
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setTriggeringCardId(pe.enteringCard().getId());
            if (enteringPermanent != null && mayPay.wrapped() != null
                    && mayPay.wrapped().usesEnteringPermanentReference()) {
                entry.setAttachedPermanentSnapshot(new Permanent(enteringPermanent));
            }
            entry.setNonTargeting(!isTargeting(mayPay));
            CardEffect payOffEffect = mayPay.wrapped() instanceof ConditionalEffect payOffConditional
                    ? payOffConditional.wrapped() : mayPay.wrapped();
            if (enteringPermanentId != null && payOffEffect instanceof CreateTokenCopyOfTargetPermanentEffect) {
                // "that creature" is not a target (Flameshadow Conjuring), so the copy still happens
                // from last known information when the entering creature has already left.
                entry.setNonTargeting(true);
                entry.rememberLastKnownPermanentCard(enteringPermanentId, pe.enteringCard());
            }
            if (match.permanent() != null) {
                entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            }
            if (enteringPermanent != null && enteringPermanent.isAttacking()) {
                UUID attackedTargetId = enteringPermanent.getAttackTarget();
                entry.setAttackedTargetId(attackedTargetId);
                entry.setDefendingPlayerId(attackedTargetId == null ? null
                        : match.gameData().playerIds.contains(attackedTargetId) ? attackedTargetId
                        : gameQueryService.findPermanentController(match.gameData(), attackedTargetId));
            }
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may pay mana)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    // ── Value-materialising effects ─────────────────────────────────────────────────────

    @CollectsWatchTrigger(value = DrawCardForTargetPlayerEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureDrawEnteringController(TriggerMatchContext match,
            DrawCardForTargetPlayerEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        enqueue(match, effect, pe.enteringControllerId(), pe.perEffectTriggerCount());
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = ExploreEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureExplore(TriggerMatchContext match,
                                               ExploreEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    enteringPermanentId);
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = TargetOpponentLosesLifeEqualToPowerToughnessDifferenceEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handlePowerToughnessDifferenceLifeLoss(TriggerMatchContext match,
            TargetOpponentLosesLifeEqualToPowerToughnessDifferenceEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        int power = enteringPermanent == null
                ? (pe.enteringCard().getPower() == null ? 0 : pe.enteringCard().getPower())
                : gameQueryService.getEffectivePower(match.gameData(), enteringPermanent);
        int toughness = enteringPermanent == null
                ? (pe.enteringCard().getToughness() == null ? 0 : pe.enteringCard().getToughness())
                : gameQueryService.getEffectiveToughness(match.gameData(), enteringPermanent);

        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    match.permanent().getId(), enteringPermanentId, null, null, false, power, toughness));
        }
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = LoseLifeEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleLifeLossToEnteringController(TriggerMatchContext match,
            LoseLifeEffect lifeLoss, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID targetPlayerId = lifeLoss.recipient() == LoseLifeRecipient.TARGET_PLAYER
                ? pe.enteringControllerId()
                : pe.defaultTargetPlayerId();
        enqueue(match, lifeLoss, targetPlayerId, pe.perEffectTriggerCount());
        logTriggered(match);
        return true;
    }

    /**
     * "Whenever a creature enters, its controller sacrifices a [permanent] of their choice" (Tainted
     * Aether). The authored effect already carries {@code SacrificeRecipient.TARGET_PLAYER}; here we
     * queue it with the entering creature's controller as the sacrificing player, mirroring the
     * damage-to-entering-controller handler above.
     */
    @CollectsWatchTrigger(value = SacrificePermanentsEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureSacrifice(TriggerMatchContext match,
            SacrificePermanentsEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(), match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)), pe.enteringControllerId(), match.permanent().getId());
            entry.setTriggeringPermanentId(findEnteringPermanentId(match, pe.enteringCard()));
            entry.setTriggeringPermanentControllerId(pe.enteringControllerId());
            entry.setNonTargeting(true);
            match.gameData().enqueueTrigger(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (controller sacrifices)",
                match.gameData().id, match.permanent().getCard().getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a creature enters, this enchantment deals N damage to it" (Aether Flash). The entering
     * creature is the (non-chosen) recipient, so we resolve its permanent id now and queue a normal
     * {@link DealDamageToTargetCreatureEffect} with that id baked in as the target — the source being
     * this permanent, so prevention/protection/damage-triggers all key off it.
     */
    @CollectsWatchTrigger(value = DealDamageToTargetCreatureEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureDealDamageToEntering(TriggerMatchContext match,
            DealDamageToTargetCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to damage.
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId());
            damageEntry.setNonTargeting(true);
            match.gameData().stack.add(damageEntry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (deal damage to entering creature)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * Queues Marauding Raptor-style damage to the creature that caused the ally-creature trigger.
     * The entering creature is fixed by the event, so no target choice is presented.
     */
    @CollectsWatchTrigger(value = DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureDealDamageThenBoost(TriggerMatchContext match,
            DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId());
            entry.setNonTargeting(true);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (deal damage and boost source)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a creature you control enters, it deals damage equal to its power to any target"
     * (Warstorm Surge). The entering creature — not this permanent — is the damage source (CR 608.2h),
     * so the pending any-target choice carries the entering permanent's id as {@code sourcePermanentId};
     * that is what {@code SourcePower} and the damage pipeline read at resolution. The permanent-targeting
     * default would instead bind this enchantment and offer no player targets.
     */
    @CollectsWatchTrigger(value = DealDamageToAnyTargetEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureEntersDealsPowerDamage(TriggerMatchContext match,
            DealDamageToAnyTargetEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; it has no power to deal damage with.
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            PermanentChoiceContext.EnteringPermanentAnyTargetTrigger context = effect.damage() instanceof ChosenPermanentPower
                    ? new PermanentChoiceContext.EnteringPermanentAnyTargetTrigger(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    match.permanent().getId(), enteringPermanentId,
                    gameQueryService.getEffectivePower(match.gameData(),
                            gameQueryService.findPermanentById(match.gameData(), enteringPermanentId)))
                    : new PermanentChoiceContext.EnteringPermanentAnyTargetTrigger(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), enteringPermanentId);
            match.gameData().queueInteraction(context);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (entering creature deals its power to any target)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a creature you control enters, it deals damage equal to its power to each opponent."
     * The entering permanent is the damage source, while the permanent carrying the trigger remains
     * the source of the triggered ability itself.
     */
    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureDealsPowerDamageToEachOpponent(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (effect.recipient() != DamageRecipient.EACH_OPPONENT
                || !(effect.amount() instanceof SourcePower)) {
            return handleEnterDefault(match, effect, ctx);
        }

        Permanent enteringPermanent = findEnteringPermanent(match.gameData(), pe);
        if (enteringPermanent == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    enteringPermanent.getId());
            entry.setSourcePermanentSnapshot(enteringPermanent);
            entry.setDamageSourceCard(enteringPermanent.getCard());
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (entering creature deals power damage to each opponent)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a nontoken [creature] you control enters, create a token that's a copy of that
     * creature" (Necroduality). The entering creature is fixed at trigger time — bake its id as
     * {@code targetId} rather than routing through the EntersTriggerTarget choice pipeline that
     * the permanent-targeting default would use. Subtype gating (e.g. Zombie) is the trigger's
     * filter, and tokens are already excluded by it.
     */
    @CollectsWatchTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
                    watch = ZoneChangeWatch.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
                    watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
                    watch = ZoneChangeWatch.COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyNontokenCreatureCreateTokenCopy(TriggerMatchContext match,
            CreateTokenCopyOfTargetPermanentEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = pe.mayPayTargetCardId();
        if (enteringPermanentId == null) {
            enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        }
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to copy.
            return true;
        }
        Card sourceCard = match.sourceCard();
        UUID sourcePermanentId = match.permanent() == null ? null : match.permanent().getId();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    sourcePermanentId);
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentIds(List.of(enteringPermanentId));
            entry.setSourcePlanarObject(match.sourcePlanarObject() == null
                    ? null : match.sourcePlanarObject().copy());
            entry.rememberLastKnownPermanentCard(enteringPermanentId, pe.enteringCard());
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (create token copy of entering creature)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleOpponentCreatureCreateTokenCopyThenExileOthers(TriggerMatchContext match,
            CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.sourceCard();
        UUID sourcePermanentId = match.permanent() == null ? null : match.permanent().getId();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    sourcePermanentId);
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setTriggeringCardId(pe.enteringCard().getId());
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (create token copy and exile previous copies)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * Faerie Artisans: exile its previously created tokens before making the new entering-creature
     * copy. The entering creature is fixed at trigger time rather than chosen on resolution.
     */
    @CollectsWatchTrigger(value = ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleOpponentCreatureExileTrackedTokensAndCreateTokenCopy(
            TriggerMatchContext match, ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect effect,
            TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        Card sourceCard = match.sourceCard();
        UUID sourcePermanentId = match.permanent() == null ? null : match.permanent().getId();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    sourcePermanentId);
            entry.setNonTargeting(true);
            entry.setSourcePlanarObject(match.sourcePlanarObject() == null
                    ? null : match.sourcePlanarObject().copy());
            entry.rememberLastKnownPermanentCard(enteringPermanentId, pe.enteringCard());
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (exile old token copies and create a new copy)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = ExileTriggeringCreatureUntilSourceLeavesEffect.class,
            watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = ExileTriggeringCreatureUntilSourceLeavesEffect.class,
            watch = ZoneChangeWatch.ON_ANY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureExileUntilSourceLeaves(TriggerMatchContext match,
            ExileTriggeringCreatureUntilSourceLeavesEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null
                || countOtherCreatures(match.gameData(), enteringPermanentId) < effect.minimumOtherCreatures()) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId());
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setNonTargeting(true);
            match.gameData().enqueueTrigger(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (exile until source leaves)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyMayPayLifeAndPutCountersOnEnteringCreature(
            TriggerMatchContext match,
            MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect effect,
            TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Permanent enteringPermanent = findEnteringPermanent(match.gameData(), pe);
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanent == null || enteringPermanentId == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        int powerAtTrigger = Math.max(0, gameQueryService.getEffectivePower(match.gameData(), enteringPermanent));
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId());
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setTriggeringCardId(pe.enteringCard().getId());
            entry.setTriggeringPermanentPowerAtTrigger(powerAtTrigger);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may pay life equal to its power for counters)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * Queues a single branch effect for an entering creature whose power is checked at resolution.
     */
    @CollectsWatchTrigger(value = EnteringCreaturePowerBranchEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyEnteringCreaturePowerBranch(TriggerMatchContext match,
                                                            EnteringCreaturePowerBranchEffect effect,
                                                            TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        Permanent enteringPermanent = gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId());
            entry.setNonTargeting(true);
            entry.setSourcePermanentSnapshot(new Permanent(enteringPermanent));
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (power branch checked at resolution)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * Queues a single branch effect for an entering creature whose power is compared with the
     * source permanent's power at resolution.
     */
    @CollectsWatchTrigger(value = EnteringCreatureSourcePowerBranchEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyEnteringCreatureSourcePowerBranch(
            TriggerMatchContext match,
            EnteringCreatureSourcePowerBranchEffect effect,
            TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        Permanent enteringPermanent = gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        if (enteringPermanent == null) {
            return true;
        }

        Card sourceCard = match.permanent().getCard();
        int enteringPower = gameQueryService.getEffectivePower(match.gameData(), enteringPermanent);
        int sourcePower = gameQueryService.getEffectivePower(match.gameData(), match.permanent());
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId());
            entry.setNonTargeting(true);
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            entry.setTriggeringPermanentId(enteringPermanentId);
            entry.setTriggeringCardId(pe.enteringCard().getId());
            entry.setTriggeringPermanentPowerAtTrigger(enteringPower);
            entry.setSourcePowerAtTrigger(sourcePower);
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (power compared with source at resolution)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * Soulbond "whenever another unpaired creature enters" (CR 702.95a). Intervening-if: both this
     * permanent and the entering creature are unpaired creatures under the controller. Queues a may
     * with the entering permanent baked as {@code targetId}.
     */
    @CollectsWatchTrigger(value = SoulbondPairWithEnteringEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllySoulbondPairWithEntering(TriggerMatchContext match,
            SoulbondPairWithEnteringEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        if (match.permanent().getPairedWithId() != null) {
            return true;
        }
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        var entering = new Permanent[1];
        match.gameData().forEachPermanent((playerId, perm) -> {
            if (entering[0] == null && perm.getId().equals(enteringPermanentId)) {
                entering[0] = perm;
            }
        });
        if (entering[0] == null || entering[0].getPairedWithId() != null) {
            return true;
        }
        var may = new MayEffect(effect, "Pair " + sourceCard.getName() + " with " + pe.enteringCard().getName() + "?");
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    enteringPermanentId, match.permanent().getId());
        }
        logTriggered(match);
        log.info("Game {} - {} soulbond may-pair with entering {}",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    private UUID findEnteringPermanentId(TriggerMatchContext match, Card enteringCard) {
        UUID[] found = new UUID[1];
        match.gameData().forEachPermanent((playerId, perm) -> {
            if (found[0] == null && perm.getCard() == enteringCard) {
                found[0] = perm.getId();
            }
        });
        return found[0];
    }

    private int countOtherCreatures(GameData gameData, UUID enteringPermanentId) {
        int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!permanent.getId().equals(enteringPermanentId)
                    && gameQueryService.isCreature(gameData, permanent)) {
                count[0]++;
            }
        });
        return count[0];
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────────────

    private void enqueue(TriggerMatchContext match, CardEffect effect, UUID targetPlayerId, int count) {
        enqueue(match, effect, targetPlayerId, count, null);
    }

    private void enqueue(TriggerMatchContext match, CardEffect effect, UUID targetPlayerId, int count,
                         UUID enteringPermanentId) {
        Card sourceCard = match.sourceCard();
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        for (int i = 0; i < count; i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    targetPlayerId,
                    match.permanent() == null ? null : match.permanent().getId()
            );
            entry.setNonTargeting(!isTargeting(effect) || isPlayerFixedByEvent(effect, targetPlayerId));
            entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
            entry.setSourcePlanarObject(match.sourcePlanarObject() == null
                    ? null : match.sourcePlanarObject().copy());
            if (match.permanent() != null) {
                entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            }
            if (enteringPermanent != null) {
                entry.setTriggeringPermanentId(enteringPermanentId);
                entry.setTriggeringPermanentIds(List.of(enteringPermanentId));
                entry.setTriggeringCardId(enteringPermanent.getCard().getId());
                if (!(effect instanceof com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect)) {
                    // An attachment's "equipped/enchanted creature gets ..." refers to the creature it is attached
                    // to, not the entering creature, so no entering-creature snapshot is bound for it.
                    entry.setAttachedPermanentSnapshot(new Permanent(enteringPermanent));
                }
                entry.setTriggeringPermanentControllerId(gameQueryService.findPermanentController(
                        match.gameData(), enteringPermanentId));
                entry.setTriggeringPermanentPowerAtTrigger(
                        gameQueryService.getEffectivePower(match.gameData(), enteringPermanent));
                entry.setTriggeringPermanentToughnessAtTrigger(
                        gameQueryService.getEffectiveToughness(match.gameData(), enteringPermanent));
                if (usesTriggeringPermanentManaValue(effect)) {
                    entry.setEventValue(enteringPermanent.getCard().getManaValue());
                }
            }
            match.gameData().stack.add(entry);
        }
    }

    private void logTriggered(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.sourceCard()));
    }

    /**
     * Whether the only target-like slot of {@code effect} is a player the triggering event already
     * fixed ("that player loses 2 life"), which is referenced rather than targeted and so ignores
     * shroud and hexproof.
     */
    private static boolean isPlayerFixedByEvent(CardEffect effect, UUID fixedPlayerId) {
        TargetSpec spec = effect.targetSpec();
        return fixedPlayerId != null
                && spec.admits(TargetPredicate.Kind.PLAYER)
                && !spec.admits(TargetPredicate.Kind.PERMANENT)
                && !spec.admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                && !EffectResolution.targetsSpellOnStack(effect);
    }

    private static boolean isTargeting(CardEffect effect) {
        TargetSpec spec = effect.targetSpec();
        return spec.admits(TargetPredicate.Kind.PLAYER)
                || spec.admits(TargetPredicate.Kind.PERMANENT)
                || EffectResolution.targetsSpellOnStack(effect)
                || spec.admits(TargetPredicate.Kind.GRAVEYARD_CARD);
    }

}

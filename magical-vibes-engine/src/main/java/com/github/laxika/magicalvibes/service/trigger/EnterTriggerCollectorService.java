package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
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
                    pe.enteringControllerId(), null, pe.perEffectTriggerCount(), pe.enteringPermanentId()));
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
                        enteringPermanentId, match.permanent().getId(), null, pe.enteringControllerId()));
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
            entry.setTriggeringPermanentId(pe.enteringPermanentId());
            entry.setTriggeringPermanentControllerId(pe.enteringControllerId());
            entry.setTriggeringCardId(pe.enteringCard().getId());
            recordEnteringPermanent(match.gameData(), entry, pe.enteringCard(), pe.enteringPermanentId() == null
                    ? null : gameQueryService.findPermanentById(match.gameData(), pe.enteringPermanentId()),
                    pe.enteringControllerId());
            match.gameData().stack.add(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} any-permanent-enters trigger queued", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
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
        // "Choose one that hasn't been chosen this turn" (Galadriel, Light of Valinor): the mode is chosen, among
        // those the source hasn't chosen, as the ability goes on the stack (CR 603.3c), once for each instance.
        if (effect instanceof ChooseOneEffect restrictedModal
                && restrictedModal.modeRestriction() != ChooseOneEffect.ModeRestriction.NONE) {
            UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        match.permanent().getCard(), match.controllerId(), restrictedModal,
                        match.permanent().getId(), false, false, pe.enteringCard().getId(), null,
                        enteringPermanentId));
            }
            logTriggered(match);
            return true;
        }
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
        enqueue(match, effect, pe.defaultTargetPlayerId(), pe);
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

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD)
    private boolean handleTokenEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.TokensEnter tokensEnter = (TriggerContext.TokensEnter) ctx;
        Card sourceCard = match.permanent().getCard();
        if (isTargeting(effect)) {
            queueTokenEnterTargetChoices(match, effect, tokensEnter);
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
        entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
        recordEnteringTokens(match.gameData(), entry, tokensEnter);
        for (int i = 0; i < tokensEnter.perEffectTriggerCount(); i++) {
            match.gameData().enqueueTrigger(entry.copyForNewStackObject());
        }
        logTriggered(match);
        return true;
    }

    /**
     * Records the tokens a "whenever one or more tokens enter" event was about on {@code entry}, whatever the ability's
     * effects are: "them" (the entry's triggering permanents), "it" when the event was one token's (its triggering
     * permanent), each as it last existed on the battlefield should it leave before the ability resolves (CR 608.2h).
     */
    private void recordEnteringTokens(GameData gameData, StackEntry entry, TriggerContext.TokensEnter tokensEnter) {
        entry.setTriggeringPermanentIds(tokensEnter.permanentIds());
        for (UUID tokenId : tokensEnter.permanentIds()) {
            Permanent token = gameQueryService.findPermanentById(gameData, tokenId);
            if (token != null) {
                entry.rememberLastKnownPermanentCard(tokenId, token.getCard());
            }
        }
        if (tokensEnter.permanentIds().size() == 1) {
            Permanent token = gameQueryService.findPermanentById(gameData, tokensEnter.permanentIds().getFirst());
            if (token != null) {
                recordEnteringPermanent(gameData, entry, token.getCard(), token, null);
            }
        }
    }

    /**
     * Queues the target choice of a targeted "whenever a token enters" ability, once for each time it triggers, its
     * targets chosen as it goes on the stack (CR 603.3d). When the event is one token's ("whenever a token enters",
     * triggering for each), that token is the entry's triggering permanent, "it" for the effects that read it ("a token
     * that's a copy of it"), and a target restricted by it ("target player other than its controller") is judged
     * against it. The choice carries the ability's "do this only once each turn" mark (CR 603.2h) to its entry.
     */
    private void queueTokenEnterTargetChoices(TriggerMatchContext match, CardEffect effect,
                                              TriggerContext.TokensEnter tokensEnter) {
        UUID enteringTokenId = tokensEnter.permanentIds().size() == 1 ? tokensEnter.permanentIds().getFirst() : null;
        var choice = new PermanentChoiceContext.EntersTriggerTarget(match.permanent().getCard(), match.controllerId(),
                new ArrayList<>(List.of(effect)), match.permanent().getId(), enteringTokenId);
        for (int i = 0; i < tokensEnter.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(match.markSourceOncePerTurnOnAcceptance()
                    ? choice.withMarkSourceOncePerTurnOnAcceptance() : choice);
        }
        logTriggered(match);
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
        enqueue(match, effect, pe.defaultTargetPlayerId(), pe);
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
            enqueue(match, effect, null, pe);
            logTriggered(match);
            return true;
        }
        // "That creature deals damage equal to its power to any target" (Flayer of the Hatebound): the ability is the
        // watcher's, its target judged against it (CR 113.7); the effect reads the entry's triggering permanent.
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(match.permanent().getCard(),
                    match.controllerId(), new ArrayList<>(List.of(effect)), match.permanent().getId(),
                    enteringPermanentId));
        }
        logTriggered(match);
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
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
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
                            match.permanent().getId(), enteringPermanentId));
                }
            }
            logTriggered(match);
            return true;
        }
        UUID mayTargetId = pe.defaultTargetPlayerId();
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    mayTargetId,
                    match.permanent().getId(),
                    0,
                    match.markSourceOncePerTurnOnAcceptance(),
                    null);
            StackEntry entry = match.gameData().stack.getLast();
            if (!isTargeting(may)) {
                // "You may attach this Equipment to it", "you may move a counter onto that creature": "it" is the
                // triggering permanent, never a target.
                entry.setNonTargeting(true);
            }
            recordEnteringPermanent(match.gameData(), entry, pe.enteringCard(), enteringPermanent,
                    pe.enteringControllerId());
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may effect)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD)
    private boolean handleTokenEnterMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {

        TriggerContext.TokensEnter tokensEnter = (TriggerContext.TokensEnter) ctx;
        if (isTargeting(may)) {
            // "You may have target player …": the target is chosen as the ability goes on the stack (CR 603.3d), and
            // the "you may" is asked as it resolves (CR 603.5).
            queueTokenEnterTargetChoices(match, may, tokensEnter);
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < tokensEnter.perEffectTriggerCount(); i++) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    null, match.permanent().getId(), match.markSourceOncePerTurnOnAcceptance());
            recordEnteringTokens(match.gameData(), match.gameData().stack.getLast(), tokensEnter);
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
            recordEnteringPermanent(match.gameData(), entry, pe.enteringCard(), enteringPermanent,
                    pe.enteringControllerId());
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
     * "You may pay {N}. If you do, …" on an enter trigger. The ability is the watcher's (CR 113.7), whatever its payload
     * does: "that creature" is the entering permanent the entry records ({@link #recordEnteringPermanent}), read as the
     * ability resolves. A payload with a target gets it chosen as the ability goes on the stack (CR 603.3d); the payment
     * is decided as it resolves (CR 603.5).
     */
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
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (match.permanent() != null && (mayPay.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || mayPay.targetSpec().admits(TargetPredicate.Kind.PERMANENT))) {
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        sourceCard, match.controllerId(), new ArrayList<>(List.of(mayPay)),
                        match.permanent().getId(), enteringPermanentId));
            }
            logTriggered(match);
            return true;
        }
        Permanent enteringPermanent = enteringPermanentId == null ? null
                : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            UUID sourcePermanentId = match.permanent() == null ? null : match.permanent().getId();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard,
                    match.controllerId(), sourceCard.getName() + "'s ability", List.of(mayPay),
                    null, sourcePermanentId);
            recordEnteringPermanent(match.gameData(), entry, pe.enteringCard(), enteringPermanent,
                    pe.enteringControllerId());
            entry.setNonTargeting(!isTargeting(mayPay));
            if (match.permanent() != null) {
                entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            }
            match.gameData().stack.add(entry);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may pay mana)",
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

    /**
     * Puts {@code effect}'s ability on the stack, once for each time it triggers, with the permanent the enter event
     * was about recorded on it whatever the effect is ({@link #recordEnteringPermanent}).
     */
    private void enqueue(TriggerMatchContext match, CardEffect effect, UUID targetPlayerId,
                         TriggerContext.PermanentEnters pe) {
        Card sourceCard = match.sourceCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
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
            recordEnteringPermanent(match.gameData(), entry, pe.enteringCard(), enteringPermanent,
                    pe.enteringControllerId());
            if (enteringPermanent != null) {
                entry.setTriggeringPermanentIds(List.of(enteringPermanentId));
                entry.setAttachedPermanentSnapshot(new Permanent(enteringPermanent));
            }
            match.gameData().stack.add(entry);
        }
    }

    /**
     * Records the permanent an enter event was about on {@code entry}, whatever the ability's effects are: "it" and
     * "that creature" (the triggering permanent, read as the ability resolves), "its controller", its power and
     * toughness as the ability triggered, the card it entered as, and that card as it last existed on the battlefield
     * should it leave before the ability resolves (CR 608.2h), and the player or planeswalker it entered attacking.
     *
     * @param entering             the entering permanent, or {@code null} when it already left the battlefield (only
     *                             the card it entered as is recorded then)
     * @param enteringControllerId the player it entered under, its controller when it can't be looked up
     */
    private void recordEnteringPermanent(GameData gameData, StackEntry entry, Card enteringCard, Permanent entering,
                                         UUID enteringControllerId) {
        entry.setTriggeringCardSnapshot(enteringCard);
        if (entering == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, entering.getId());
        entry.setTriggeringPermanentId(entering.getId());
        entry.setTriggeringCardId(entering.getCard().getId());
        entry.setTriggeringPermanentControllerId(controllerId != null ? controllerId : enteringControllerId);
        entry.setTriggeringPermanentPowerAtTrigger(gameQueryService.getEffectivePower(gameData, entering));
        entry.setTriggeringPermanentToughnessAtTrigger(gameQueryService.getEffectiveToughness(gameData, entering));
        entry.rememberLastKnownPermanentCard(entering.getId(), entering.getCard());
        if (entering.isAttacking()) {
            UUID attackedTargetId = entering.getAttackTarget();
            entry.setAttackedTargetId(attackedTargetId);
            entry.setDefendingPlayerId(attackedTargetId == null ? null
                    : gameData.playerIds.contains(attackedTargetId) ? attackedTargetId
                    : gameQueryService.findPermanentController(gameData, attackedTargetId));
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

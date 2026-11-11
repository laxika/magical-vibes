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
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeredModalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Trigger collectors for enter-the-battlefield events: one {@code CardEffect.class} default for each enter watch, which
 * records the permanent (or tokens) the event was about on the ability's entry whatever its effects are, a "you may" and
 * a "you may pay" among them (asked as the ability resolves, CR 603.5), and routes a modal ability to its mode choice
 * (CR 603.3c) and a targeted one to its target choice (CR 603.3d). The scan orchestration (which permanents and watches
 * to visit, self-exclusion, the trigger conditions, Naban doubling) lives in {@link TriggerCollectionService}.
 */
@Slf4j
@Service
public class EnterTriggerCollectorService {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    public EnterTriggerCollectorService(GameLogService gameLogService, GameQueryService gameQueryService) {
        this.gameLogService = gameLogService;
        this.gameQueryService = gameQueryService;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD)
    private boolean handleAnyPermanentEnterDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (match.effects().stream().anyMatch(effect -> effect instanceof MayEffect may
                && (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)))) {
            // "You may have target player …": the target is chosen as the ability goes on the stack (CR 603.3d).
            queueTargetChoices(match, pe, false);
            return true;
        }
        return enqueueAnyPermanentEnter(match, pe);
    }

    @CollectsMiscTrigger(value = CardEffect.class, watch = MiscWatch.ON_ALLY_ROOM_FULLY_UNLOCKED)
    private boolean handleRoomFullyUnlockedDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.RoomFullyUnlocked room = (TriggerContext.RoomFullyUnlocked) ctx;
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), match.stackEffects(),
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
                match.stackEffects(),
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

    private boolean enqueueAnyPermanentEnter(TriggerMatchContext match, TriggerContext.PermanentEnters pe) {
        if (match.admits(TargetPredicate.Kind.PERMANENT)) {
            UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
            if (enteringPermanentId == null) {
                return true;
            }
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        match.permanent().getCard(), match.controllerId(),
                        match.stackEffects(), match.permanent().getId(),
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
                    match.stackEffects(),
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
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleEnterDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        // A trigger-time modal ("choose one" as Ayula, Queen Among Bears' ability triggers) or "choose one that hasn't
        // been chosen this turn" (Galadriel, Light of Valinor): the mode is chosen as the ability goes on the stack
        // (CR 603.3c), once for each instance.
        ChooseOneEffect modes = match.modal(TriggeredModalEffect::choiceMadeAsTriggered);
        if (modes != null) {
            UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
            for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
                match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        match.sourceCard(), match.controllerId(), modes,
                        match.permanent() == null ? null : match.permanent().getId(), false, false,
                        pe.enteringCard().getId(), null, enteringPermanentId));
            }
            logTriggered(match);
            return true;
        }
        if (match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                && !match.admits(TargetPredicate.Kind.PLAYER)
                && !match.admits(TargetPredicate.Kind.PERMANENT)) {
            queueGraveyardTargetChoices(match, pe);
            return true;
        }
        // Permanent-targeting effects (e.g. Reaper King's "destroy target permanent") always route through the
        // choice; player-targeting ones only when the scan left the player unset — the opponent-enters scans fix the
        // entering permanent's controller as "that player", while the ally scans leave it null (Sage's Row Denizen's
        // "target player mills two cards"). A command-zone source has no permanent to choose for, and puts the
        // ability on the stack as written.
        boolean needsTargetChoice = match.admits(TargetPredicate.Kind.PERMANENT)
                || (match.admits(TargetPredicate.Kind.PLAYER) && pe.defaultTargetPlayerId() == null);
        if (needsTargetChoice && (match.permanent() != null || match.sourcePlanarObject() != null)) {
            queueTargetChoices(match, pe, true);
            return true;
        }
        enqueue(match, pe.defaultTargetPlayerId(), pe);
        logTriggered(match);
        return true;
    }

    /**
     * Queues the graveyard-card target choice of an enters ability, once for each time it triggers (CR 603.3d), with
     * the permanent the event was about, the source and the "do this only once each turn" mark (CR 603.2h), which the
     * entry it makes records.
     */
    private void queueGraveyardTargetChoices(TriggerMatchContext match, TriggerContext.PermanentEnters pe) {
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.sourceCard(), match.controllerId(), match.effects(), null, 0, 0, 0, null, false,
                    enteringPermanentId, match.permanent() == null ? null : match.permanent().getId(),
                    match.onceEachTurnActionKey()));
        }
        logTriggered(match);
    }

    /**
     * Queues the target choice of a targeted enters ability, once for each time it triggers: its targets are chosen
     * as it goes on the stack (CR 603.3d) and judged against the watcher, the ability's source (CR 113.7), and the
     * choice carries the entering permanent, "that creature" for the effects that read it (Gruul Ragebeast's fight).
     * A plane's ability goes to the any-target choice with the plane; a source with several target groups, when
     * {@code multiTargetGroups}, to the multi-target choice.
     */
    private void queueTargetChoices(TriggerMatchContext match, TriggerContext.PermanentEnters pe,
                                    boolean multiTargetGroups) {
        Card sourceCard = match.sourceCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            if (match.sourcePlanarObject() != null) {
                Permanent entering = enteringPermanentId == null
                        ? null : gameQueryService.findPermanentById(match.gameData(), enteringPermanentId);
                match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        sourceCard, match.controllerId(), match.stackEffects(), false, null,
                        0, enteringPermanentId, entering == null ? null : new Permanent(entering), false,
                        enteringPermanentId, null, pe.enteringControllerId(), match.sourcePlanarObject().copy()));
            } else if (multiTargetGroups && sourceCard.getTargetGroups().size() > 1) {
                match.gameData().queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        sourceCard, match.controllerId(), match.stackEffects(),
                        match.permanent().getId(), List.of(), 0, 0, List.of(), 0, List.of(),
                        false, pe.enteringCard().getId(), enteringPermanentId));
            } else {
                match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        sourceCard, match.controllerId(), match.stackEffects(),
                        match.permanent().getId(), enteringPermanentId));
            }
        }
        logTriggered(match);
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD)
    private boolean handleTokenEnterDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.TokensEnter tokensEnter = (TriggerContext.TokensEnter) ctx;
        Card sourceCard = match.permanent().getCard();
        if (match.targets()) {
            queueTokenEnterTargetChoices(match, tokensEnter);
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setEventValue(tokensEnter.count());
        entry.setNonTargeting(true);
        entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
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
    private void queueTokenEnterTargetChoices(TriggerMatchContext match, TriggerContext.TokensEnter tokensEnter) {
        UUID enteringTokenId = tokensEnter.permanentIds().size() == 1 ? tokensEnter.permanentIds().getFirst() : null;
        var choice = new PermanentChoiceContext.EntersTriggerTarget(match.permanent().getCard(), match.controllerId(),
                match.stackEffects(), match.permanent().getId(), enteringTokenId);
        for (int i = 0; i < tokensEnter.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(choice.withOnceEachTurnActionKey(match.onceEachTurnActionKey()));
        }
        logTriggered(match);
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureEnterDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        if (match.targets()) {
            // "You may have it deal damage to any target" (Pandemonium, the Immersturm plane): the target is chosen as
            // the ability goes on the stack, the "you may" asked as it resolves (CR 603.5).
            queueTargetChoices(match, pe, false);
            return true;
        }
        enqueue(match, pe.defaultTargetPlayerId(), pe);
        logTriggered(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_CREATURE_ENTERS_FROM_GRAVEYARD)
    private boolean handleCreatureEnterFromGraveyardDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        if (!match.targets()) {
            // "You may attach this Equipment to it" (Dredging Claw): "it" is the entry's triggering permanent.
            enqueue(match, null, pe);
            logTriggered(match);
            return true;
        }
        // "That creature deals damage equal to its power to any target" (Flayer of the Hatebound): the ability is the
        // watcher's, its target judged against it (CR 113.7); its effects read the entry's triggering permanent.
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(match.permanent().getCard(),
                    match.controllerId(), match.stackEffects(), match.permanent().getId(),
                    enteringPermanentId));
        }
        logTriggered(match);
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

    /**
     * Puts {@code match}'s ability on the stack, whole, once for each time it triggers, with the permanent the enter
     * event was about recorded on it whatever its effects are ({@link #recordEnteringPermanent}).
     */
    private void enqueue(TriggerMatchContext match, UUID targetPlayerId,
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
                    match.stackEffects(),
                    targetPlayerId,
                    match.permanent() == null ? null : match.permanent().getId()
            );
            entry.setNonTargeting(!match.targets() || isPlayerFixedByEvent(match, targetPlayerId));
            entry.setOnceEachTurnActionKey(match.onceEachTurnActionKey());
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
     *                             the card it entered as and the player it entered under are recorded then)
     * @param enteringControllerId the player it entered under, its controller when it can't be looked up
     */
    private void recordEnteringPermanent(GameData gameData, StackEntry entry, Card enteringCard, Permanent entering,
                                         UUID enteringControllerId) {
        entry.setTriggeringCardSnapshot(enteringCard);
        if (entering == null) {
            entry.setTriggeringPermanentControllerId(enteringControllerId);
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
     * Whether the only target-like slot of {@code match}'s ability is a player the triggering event already
     * fixed ("that player loses 2 life"), which is referenced rather than targeted and so ignores
     * shroud and hexproof.
     */
    private static boolean isPlayerFixedByEvent(TriggerMatchContext match, UUID fixedPlayerId) {
        return fixedPlayerId != null
                && match.admits(TargetPredicate.Kind.PLAYER)
                && !match.admits(TargetPredicate.Kind.PERMANENT)
                && !match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                && !match.targetsSpellOnStack();
    }
}

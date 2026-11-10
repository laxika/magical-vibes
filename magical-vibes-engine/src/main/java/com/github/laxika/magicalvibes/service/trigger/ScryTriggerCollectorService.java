package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
/** The trigger collector of the scry watches: one default records the scry whatever the effects are. */
public class ScryTriggerCollectorService {

    private final GameLogService gameLogService;

    /**
     * "Whenever you scry, …" and "whenever an opponent scries, …": the ability goes on the stack as written, its entry
     * recording the scry whatever its effects are: the scrying player as its triggering player (and, for an opponent's
     * scry, its non-targeting "that player"), the number of cards put on the bottom as its event value ("that many") and
     * the number of cards looked at. A permanent or player target is chosen as it goes on the stack (CR 603.3d), the
     * choice carrying the scrying player and the count. "Whenever you choose to put one or more cards on the bottom of
     * your library while scrying" (The Temporal Anchor) is the ability's event condition on the count.
     */
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_CONTROLLER_SCRIES)
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_OPPONENT_SCRIES)
    private boolean handleScryDefault(TriggerMatchContext match, CardEffect trigger, TriggerContext ctx) {
        TriggerContext.Scry scry = (TriggerContext.Scry) ctx;
        Card sourceCard = match.permanent().getCard();
        TargetSpec targetSpec = trigger.targetSpec();
        boolean opponentScry = !match.controllerId().equals(scry.scryingPlayerId());
        if (!opponentScry && (targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                || targetSpec.admits(TargetPredicate.Kind.PLAYER))) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard,
                    match.controllerId(),
                    new ArrayList<>(List.of(trigger)),
                    !targetSpec.admits(TargetPredicate.Kind.PERMANENT),
                    targetFilter(targetSpec),
                    0,
                    match.permanent().getId())
                    .withTriggeringPlayer(scry.scryingPlayerId())
                    .withEventValue(scry.bottomedCardCount()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(trigger)),
                    opponentScry ? scry.scryingPlayerId() : null,
                    match.permanent().getId());
            entry.setTriggeringPlayerId(scry.scryingPlayerId());
            entry.setEventValue(scry.bottomedCardCount());
            entry.setCardsLookedAtWhileScrying(scry.cardsLookedAt());
            entry.setNonTargeting(opponentScry);
            entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
            match.gameData().enqueueTrigger(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on scry", match.gameData().id, sourceCard.getName());
        return true;
    }

    private TargetFilter targetFilter(TargetSpec targetSpec) {
        TargetPredicate predicate = targetSpec.targetPredicate();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                && targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            PermanentPredicate permanentPredicate = predicate.permanentRestriction().orElseThrow();
            PlayerPredicate playerPredicate = predicate.leaf(TargetPredicate.Kind.PLAYER)
                    .map(leaf -> ((TargetPredicate.Players) leaf).inner())
                    .orElseThrow();
            return new AnyTargetPredicateTargetFilter(
                    permanentPredicate,
                    playerPredicate,
                    "Target must match the ability's targeting restriction");
        }
        if (targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            PlayerPredicate playerPredicate = predicate.leaf(TargetPredicate.Kind.PLAYER)
                    .map(leaf -> ((TargetPredicate.Players) leaf).inner())
                    .orElseThrow();
            return new PlayerPredicateTargetFilter(
                    playerPredicate,
                    "Target must match the ability's targeting restriction");
        }
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            PermanentPredicate permanentPredicate = predicate.permanentRestriction().orElseThrow();
            return new PermanentPredicateTargetFilter(
                    permanentPredicate,
                    "Target must match the ability's targeting restriction");
        }
        return null;
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingConnive;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawBeforeConniveReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DrawDiscardAndConniveEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawDiscardAndConniveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawDiscardAndConniveEffect) effect;
        PermanentSubject subject = e.subject();
        List<UUID> conniverIds = subjectResolutionService.permanentIds(gameData, entry, effect, subject);
        if (subject.declaresTarget()) {
            // An illegal target is not affected (CR 608.2b).
            conniverIds = conniverIds.stream()
                    .filter(id -> gameQueryService.findPermanentById(gameData, id) != null)
                    .toList();
        }
        if (conniverIds.size() > 1) {
            conniveOneAtATime(gameData, entry, e, conniverIds);
            return;
        }
        UUID conniverId = conniverIds.isEmpty() ? null : conniverIds.getFirst();
        if (conniverId == null && !(subject instanceof PermanentSubject.Source)
                && !(subject instanceof PermanentSubject.Triggering)) {
            return;
        }
        connive(gameData, entry, e, conniverId);
    }

    /**
     * Several permanents connive one at a time (CR 701.50c): the first player in APNAP order who controls, or last
     * controlled, one of them chooses which connives next; the rest connive after it, in the same way.
     */
    private void conniveOneAtATime(GameData gameData, StackEntry entry, DrawDiscardAndConniveEffect connive,
                                   List<UUID> conniverIds) {
        for (UUID playerId : subjectResolutionService.players(gameData, entry, connive, Subjects.eachPlayer())) {
            List<UUID> theirs = conniverIds.stream()
                    .filter(id -> playerId.equals(connivingPlayerId(gameData, entry, id)))
                    .toList();
            if (theirs.isEmpty()) {
                continue;
            }
            List<UUID> live = theirs.stream()
                    .filter(id -> gameQueryService.findPermanentById(gameData, id) != null)
                    .toList();
            if (live.size() > 1) {
                playerInputService.beginPermanentChoice(gameData, playerId, live,
                        new PermanentChoiceContext.ConniveOrderChoice(entry, List.copyOf(conniverIds),
                                connive.amount()),
                        "Choose the next creature to connive.");
                return;
            }
            conniveNextThenTheRest(entry, connive.amount(), conniverIds,
                    live.isEmpty() ? theirs.getFirst() : live.getFirst());
            return;
        }
    }

    /** The chosen permanent connives first, then the rest one at a time. */
    public void completeOrderChoice(GameData gameData, UUID chosenId, PermanentChoiceContext.ConniveOrderChoice choice) {
        if (!choice.conniverIds().contains(chosenId)) {
            throw new IllegalStateException("Invalid creature to connive");
        }
        conniveNextThenTheRest(choice.resolvingEntry(), choice.amount(), choice.conniverIds(), chosenId);
    }

    private static void conniveNextThenTheRest(StackEntry entry, DynamicAmount amount, List<UUID> conniverIds,
                                               UUID nextId) {
        List<CardEffect> steps = new ArrayList<>();
        steps.add(new DrawDiscardAndConniveEffect(amount, Subjects.named(nextId)));
        List<PermanentSubject> rest = conniverIds.stream()
                .filter(id -> !id.equals(nextId))
                .<PermanentSubject>map(Subjects::named)
                .toList();
        if (rest.size() == 1) {
            steps.add(new DrawDiscardAndConniveEffect(amount, rest.getFirst()));
        } else if (rest.size() > 1) {
            steps.add(new DrawDiscardAndConniveEffect(amount, Subjects.allOf(rest.toArray(PermanentSubject[]::new))));
        }
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, steps);
    }

    private void connive(GameData gameData, StackEntry entry, DrawDiscardAndConniveEffect e, UUID sourcePermanentId) {
        UUID controllerId = connivingPlayerId(gameData, entry, sourcePermanentId);

        int replacementDraws = gameQueryService.countPlayerControlledStaticEffects(
                gameData, controllerId, DrawBeforeConniveReplacementEffect.class);
        for (int i = 0; i < replacementDraws; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
        Permanent source = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source)));

        playerInteractionSupport.applyDrawCards(gameData, controllerId, amount);
        gameData.pendingConnive = null;
        List<Card> hand = gameData.playerHands.get(controllerId);
        int discardAmount = Math.min(amount, hand == null ? 0 : hand.size());
        // CR 701.50b: a permanent no longer on the battlefield connives using its last known information.
        Permanent lastKnownSource = source == null && sourcePermanentId != null
                && entry.getSourcePermanentSnapshot() != null
                && sourcePermanentId.equals(entry.getSourcePermanentSnapshot().getId())
                ? entry.getSourcePermanentSnapshot()
                : source == null && sourcePermanentId != null && entry.getTriggeringPermanentSnapshot() != null
                && sourcePermanentId.equals(entry.getTriggeringPermanentSnapshot().getId())
                ? entry.getTriggeringPermanentSnapshot() : null;
        if (sourcePermanentId != null && discardAmount > 0) {
            gameData.pendingConnive = new PendingConnive(sourcePermanentId, discardAmount,
                    lastKnownSource, controllerId);
        }

        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, discardAmount);
        if (discardAmount == 0 && source != null) {
            ActionEvents.connived(gameData, source);
        } else if (discardAmount == 0 && lastKnownSource != null) {
            ActionEvents.connived(gameData, lastKnownSource, controllerId);
        }
    }

    /**
     * Determines who draws and discards for a connive: the conniving permanent's controller at resolution
     * (CR 701.50a). If that permanent is no longer on the battlefield, the ability's controller is used as
     * the last known controller (CR 701.50b).
     */
    private UUID connivingPlayerId(GameData gameData, StackEntry entry, UUID sourcePermanentId) {
        UUID currentController = sourcePermanentId == null
                ? null : gameQueryService.findPermanentController(gameData, sourcePermanentId);
        return currentController != null ? currentController : entry.getControllerId();
    }
}

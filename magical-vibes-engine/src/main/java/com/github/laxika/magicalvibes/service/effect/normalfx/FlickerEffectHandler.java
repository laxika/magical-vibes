package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves every "exile permanent(s), return under owner's (or controller's) control" flicker via
 * {@link FlickerEffect}, dispatching on {@link FlickerEffect#timing()} then {@link FlickerEffect#scope()}.
 *
 * <p>{@code AT_STEP} scopes delegate to {@link ExileSupport#exileAndScheduleReturn} (a delayed trigger
 * that survives the source leaving the battlefield). {@code IMMEDIATE} exiles and re-creates the
 * permanent inline, optionally with returned +1/+1 counters or a subtype-conditional bonus effect. An
 * immediate TARGET flicker bound to a multi-target group (Ghostly Flicker's two targets) exiles every
 * chosen permanent before returning any of them.
 * When {@link FlickerEffect#returnUnderController()} is true, the permanent returns under the effect
 * controller and is tracked as stolen if the owner differs (Restoration Angel).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlickerEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlickerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FlickerEffect) effect;
        if (e.timing() == ReturnTiming.IMMEDIATE) {
            resolveImmediate(gameData, entry, e);
            return;
        }
        switch (e.scope()) {
            case TARGET -> resolveTargetAtStep(gameData, entry, e);
            case SELF -> resolveSelfAtStep(gameData, entry, e);
            case TARGET_PLAYERS_PERMANENTS -> resolvePlayersPermanentsAtStep(gameData, entry, e);
        }
    }

    private void resolveTargetAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);

        exileSupport.exileAndScheduleReturn(gameData, entry, target, ownerId, e.returnTapped(), e.returnStep());
    }

    private void resolveSelfAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        exileSupport.exileAndScheduleReturn(gameData, entry, source, entry.getControllerId(), e.returnTapped(), e.returnStep());
    }

    private void resolvePlayersPermanentsAtStep(GameData gameData, StackEntry entry, FlickerEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> toExile = battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                .toList();

        for (Permanent permanent : toExile) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            UUID ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), controllerId);
            exileSupport.exileAndScheduleReturn(
                    gameData, entry, permanent, ownerId, e.returnTapped(), e.returnStep());
        }
    }

    private void resolveImmediate(GameData gameData, StackEntry entry, FlickerEffect e) {
        List<UUID> permanentIds = immediateFlickerTargets(entry, e);

        // Every chosen permanent leaves before any of them comes back, so a multi-target flicker
        // (Ghostly Flicker) returns them simultaneously and their ETB triggers see each other.
        List<FlickeredPermanent> exiled = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            if (target != null) {
                exiled.add(exileForImmediateReturn(gameData, entry, e, target));
            }
        }
        if (exiled.isEmpty()) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        for (FlickeredPermanent flickered : exiled) {
            returnAfterImmediateExile(gameData, entry, e, flickered);
        }
    }

    /**
     * The permanents an immediate flicker touches: the source for SELF, otherwise the chosen target
     * group — which is a single {@code targetId} for the usual one-target flicker and a real list for
     * a card that binds the effect to a multi-target group.
     */
    private List<UUID> immediateFlickerTargets(StackEntry entry, FlickerEffect e) {
        if (e.scope() == com.github.laxika.magicalvibes.model.effect.FlickerScope.SELF) {
            return List.of(entry.getSourcePermanentId());
        }
        List<UUID> group = entry.targetsForEffect(e);
        if (!group.isEmpty()) {
            return group;
        }
        return entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of();
    }

    /** A permanent that has already been exiled by an immediate flicker, with the state its return needs. */
    private record FlickeredPermanent(Card card, UUID ownerId, UUID returnControllerId, boolean hadBonusSubtype) {
    }

    private FlickeredPermanent exileForImmediateReturn(
            GameData gameData, StackEntry entry, FlickerEffect e, Permanent target) {
        UUID previousControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), previousControllerId);
        UUID returnControllerId = e.returnUnderController() ? entry.getControllerId() : ownerId;

        Card card = target.getOriginalCard();
        boolean hadBonusSubtype = e.bonusSubtype() != null
                && card.getSubtypes().contains(e.bonusSubtype());

        permanentRemovalService.removePermanentToExile(gameData, target);
        return new FlickeredPermanent(card, ownerId, returnControllerId, hadBonusSubtype);
    }

    private void returnAfterImmediateExile(
            GameData gameData, StackEntry entry, FlickerEffect e, FlickeredPermanent flickered) {
        Card card = flickered.card();
        UUID ownerId = flickered.ownerId();
        UUID returnControllerId = flickered.returnControllerId();
        boolean hadBonusSubtype = flickered.hadBonusSubtype();

        // Immediately return from exile as a new permanent
        gameData.removeFromExile(card.getId());
        Permanent returned = new Permanent(card);
        boolean applyReturnCounters = e.plusOnePlusOneCountersOnReturn() > 0
                && (e.bonusSubtype() == null || hadBonusSubtype);
        if (applyReturnCounters && !gameQueryService.cantHaveCounters(gameData, returned)) {
            returned.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, e.plusOnePlusOneCountersOnReturn());
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, returnControllerId, returned);
        if (e.returnUnderController() && !returnControllerId.equals(ownerId)) {
            graveyardReturnSupport.trackStolenCreature(gameData, returned.getId(), returnControllerId, ownerId);
        }

        gameLogService.append(gameData, GameLog.builder().card(card).text(" is exiled by ").card(entry.getCard()).text(" and returns to the battlefield under " + gameData.playerIdToName.get(returnControllerId) + "'s control.").build());
        log.info("Game {} - {} flickers {} (immediate return)", gameData.id, entry.getCard().getName(), card.getName());

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, returnControllerId, card, null, false);

        // Apply bonus if the exiled permanent had the required subtype
        if (hadBonusSubtype && e.bonusEffect() instanceof DrawCardEffect drawEffect) {
            int drawAmount = amountEvaluationService.evaluate(gameData, drawEffect.amount(),
                    AmountContext.forStackEntry(entry, null));
            for (int i = 0; i < drawAmount; i++) {
                drawService.resolveDrawCard(gameData, entry.getControllerId());
            }
            gameLogService.append(gameData, GameLog.builder().text(gameData.playerIdToName.get(entry.getControllerId()) + " draws a card (").card(card).text(" was a " + e.bonusSubtype().getDisplayName() + ").").build());
        }
    }
}

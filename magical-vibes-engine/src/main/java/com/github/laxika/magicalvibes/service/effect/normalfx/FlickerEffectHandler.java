package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves every "exile permanent(s), return under owner's control" flicker via {@link FlickerEffect},
 * dispatching on {@link FlickerEffect#timing()} then {@link FlickerEffect#scope()}.
 *
 * <p>{@code AT_STEP} scopes delegate to {@link ExileSupport#exileAndScheduleReturn} (a delayed trigger
 * that survives the source leaving the battlefield). {@code IMMEDIATE} exiles and re-creates the
 * permanent inline, optionally with returned +1/+1 counters or a subtype-conditional bonus effect.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlickerEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;

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
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);

        Card card = target.getOriginalCard();
        boolean hadBonusSubtype = e.bonusSubtype() != null
                && card.getSubtypes().contains(e.bonusSubtype());

        // Exile the permanent
        permanentRemovalService.removePermanentToExile(gameData, target);
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Immediately return from exile as a new permanent
        gameData.removeFromExile(card.getId());
        Permanent returned = new Permanent(card);
        if (e.plusOnePlusOneCountersOnReturn() > 0
                && !gameQueryService.cantHaveCounters(gameData, returned)) {
            returned.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, e.plusOnePlusOneCountersOnReturn());
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, returned);

        String logEntry = card.getName() + " is exiled by " + entry.getCard().getName()
                + " and returns to the battlefield under " + gameData.playerIdToName.get(ownerId) + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} flickers {} (immediate return)", gameData.id, entry.getCard().getName(), card.getName());

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);

        // Apply bonus if the exiled permanent had the required subtype
        if (hadBonusSubtype && e.bonusEffect() instanceof DrawCardEffect drawEffect) {
            int drawAmount = amountEvaluationService.evaluate(gameData, drawEffect.amount(),
                    AmountContext.forStackEntry(entry, null));
            for (int i = 0; i < drawAmount; i++) {
                drawService.resolveDrawCard(gameData, entry.getControllerId());
            }
            String drawLog = gameData.playerIdToName.get(entry.getControllerId())
                    + " draws a card (" + card.getName() + " was a " + e.bonusSubtype().getDisplayName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, drawLog);
        }
    }
}

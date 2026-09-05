package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCappedCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Resolves {@link PutCappedCountersOnSourceEffect}: put up to the evaluated amount of counters on
 * the source permanent, clamped so the total of that counter type never exceeds the cap. E.g.
 * Clockwork Beast's "{X}, {T}: Put up to X +1/+0 counters on this creature. This ability can't cause
 * the total number of +1/+0 counters on this creature to be greater than seven."
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCappedCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCappedCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCappedCountersOnSourceEffect) effect;
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }

        int requested = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        int current = source.getCounterCount(e.counterType());
        int maximum = Math.min(requested, e.cap() - current);
        if (maximum <= 0) {
            return;
        }

        if (gameData.chosenXValue == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    entry.getControllerId(), null, null,
                    new ChoiceContext.CappedCounterAmountChoice(source.getId()),
                    IntStream.rangeClosed(0, maximum).mapToObj(Integer::toString).toList(),
                    "Choose how many counters to put on " + source.getCard().getName() + "."));
            return;
        }

        int toAdd = Math.min(gameData.chosenXValue, maximum);
        gameData.chosenXValue = null;
        gameData.rerunCurrentEffectAfterInteraction = false;
        if (toAdd <= 0) {
            return;
        }

        source.setCounterCount(e.counterType(), current + toAdd);
        permanentCounterSupport.notifyCountersPlaced(gameData, entry, source, toAdd);
        if (e.counterType() == CounterType.PLUS_ONE_PLUS_ONE) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            if (controllerId != null) {
                gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(controllerId);
            }
        }
        
        gameLogService.append(gameData, GameLog.builder().card(source.getCard()).text(" gets " + toAdd + " counter(s).").build());
        log.info("Game {} - {} gets {} {} counter(s)", gameData.id,
                source.getCard().getName(), toAdd, e.counterType());
    }
}

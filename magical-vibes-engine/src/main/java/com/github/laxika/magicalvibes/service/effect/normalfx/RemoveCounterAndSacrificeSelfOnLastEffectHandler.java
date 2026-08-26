package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves vanishing-style upkeep counter removal and its last-counter sacrifice trigger. */
@Component
@RequiredArgsConstructor
public class RemoveCounterAndSacrificeSelfOnLastEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterAndSacrificeSelfOnLastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterAndSacrificeSelfOnLastEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        int current = source.getCounterCount(e.counterType());
        if (current <= 0) {
            return;
        }

        source.setCounterCount(e.counterType(), current - 1);
        if (e.counterType() == CounterType.OIL) {
            gameData.recordOilCounterRemoved(source, 1);
        }
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                " loses a " + permanentCounterSupport.counterTypeName(e.counterType()) + " counter."));

        if (current != 1) {
            return;
        }

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s ability",
                List.of(new SacrificeSelfEffect()),
                null,
                source.getId()));
    }
}

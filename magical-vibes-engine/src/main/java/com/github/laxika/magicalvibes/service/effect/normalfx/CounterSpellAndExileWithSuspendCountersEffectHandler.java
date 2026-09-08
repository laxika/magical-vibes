package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileWithSuspendCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a counter spell that exiles the countered card with suspend time counters. */
@Component
@RequiredArgsConstructor
public class CounterSpellAndExileWithSuspendCountersEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellAndExileWithSuspendCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        CounterSpellAndExileWithSuspendCountersEffect suspendEffect =
                (CounterSpellAndExileWithSuspendCountersEffect) effect;
        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        UUID ownerId = targetEntry.getOwnerId();
        Card card = targetEntry.getPhysicalCard();
        if (!counterSupport.counterSpellAndExile(gameData, entry, targetEntry, ownerId)) return;

        gameData.suspendedSpellExiles.add(new GameData.SuspendedSpellExile(
                card.getId(), ownerId, suspendEffect.counters()));
        gameLogService.append(gameData, GameLog.cardThen(card,
                " is exiled with " + suspendEffect.counters() + " time counters and gains suspend."));
    }
}

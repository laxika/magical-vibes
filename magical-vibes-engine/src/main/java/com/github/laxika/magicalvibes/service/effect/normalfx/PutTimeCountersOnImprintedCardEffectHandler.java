package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersOnImprintedCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTimeCountersOnImprintedCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTimeCountersOnImprintedCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutTimeCountersOnImprintedCardEffect counters = (PutTimeCountersOnImprintedCardEffect) effect;
        if (counters.cardId() == null) {
            return;
        }

        ExiledCardEntry exiledEntry = gameData.findExiledCard(counters.cardId());
        if (exiledEntry == null) {
            return;
        }

        gameData.exiledCardTimeCounters.merge(counters.cardId(), counters.amount(), Integer::sum);
        gameLogService.append(gameData,
                GameLog.cardThen(exiledEntry.card(), " gets " + counters.amount() + " time counters."));
    }
}

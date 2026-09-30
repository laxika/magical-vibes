package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfWithFetchCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Exiles the source permanent and marks its resulting exile entry with a fetch counter. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileSelfWithFetchCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfWithFetchCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        var leavingCards = new ArrayList<>(self.cardsLeavingBattlefield());
        if (!permanentRemovalService.removePermanentToExile(gameData, self)) {
            return;
        }
        for (var card : leavingCards) {
            gameData.markExiledCardWithFetchCounter(card.getId(), entry.getControllerId());
        }

        gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " is exiled with a fetch counter."));
        permanentRemovalService.removeOrphanedAuras(gameData);
        log.info("Game {} - {} exiles itself with a fetch counter", gameData.id, self.getCard().getName());
    }
}

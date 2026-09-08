package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfForVoyageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Exiles the source and records the controller and voyage-counter state for its landfall ability. */
@Component
@RequiredArgsConstructor
public class ExileSelfForVoyageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfForVoyageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null || !permanentRemovalService.removePermanentToExile(gameData, source)) {
            return;
        }

        Card card = source.getOriginalCard();
        ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
        if (exiled == null) {
            return;
        }

        gameData.exiledVoyageCounters.put(card.getId(), 0);
        gameData.exiledVoyageControllerIds.put(card.getId(), entry.getControllerId());
        gameLogService.append(gameData, GameLog.cardThen(card, " is exiled for its voyage."));
    }
}

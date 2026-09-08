package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves the source's cleanup trigger for a Stangg-style token relationship.
 */
@Component
@RequiredArgsConstructor
public class ExileTokensCreatedWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTokensCreatedWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTokensCreatedWithSourceEffect) effect;
        UUID sourceId = exile.sourcePermanentId() != null
                ? exile.sourcePermanentId() : entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }

        Set<UUID> trackedTokenIds = gameData.sourceCreatedTokens.remove(sourceId);
        if (trackedTokenIds == null || trackedTokenIds.isEmpty()) {
            return;
        }

        List<Permanent> tokens = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.stream()
                .filter(permanent -> trackedTokenIds.contains(permanent.getId()))
                .forEach(tokens::add));

        for (Permanent token : tokens) {
            token.setChosenPermanentId(null);
            if (permanentRemovalService.removePermanentToExile(gameData, token)) {
                gameLogService.append(gameData, GameLog.cardThen(token.getCard(), " is exiled."));
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}

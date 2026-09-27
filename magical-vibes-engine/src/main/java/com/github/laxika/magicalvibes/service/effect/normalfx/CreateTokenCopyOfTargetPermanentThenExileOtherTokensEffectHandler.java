package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Faerie Artisans' replace-the-previous-copy token behavior. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffectHandler
        implements NormalEffectHandlerBean {

    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyAndExile = (CreateTokenCopyOfTargetPermanentThenExileOtherTokensEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();
        if (entry.getTargetId() != null) {
            tokenCopyHandler.resolveForTarget(gameData, entry, copyAndExile.copyEffect(), entry.getTargetId());
        }

        Set<UUID> newTokenIds = new HashSet<>(entry.getCreatedPermanentIds()
                .subList(createdBefore, entry.getCreatedPermanentIds().size()));
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }

        Set<UUID> trackedTokenIds = gameData.sourceCreatedTokens.get(sourceId);
        if (trackedTokenIds == null || trackedTokenIds.isEmpty()) {
            return;
        }

        Set<UUID> oldTokenIds = new HashSet<>(trackedTokenIds);
        oldTokenIds.removeAll(newTokenIds);
        trackedTokenIds.removeAll(oldTokenIds);

        List<Permanent> oldTokens = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.stream()
                .filter(permanent -> oldTokenIds.contains(permanent.getId()))
                .forEach(oldTokens::add));

        for (Permanent token : oldTokens) {
            token.setChosenPermanentId(null);
            if (permanentRemovalService.removePermanentToExile(gameData, token)) {
                gameLogService.append(gameData, GameLog.cardThen(token.getCard(), " is exiled."));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (trackedTokenIds.isEmpty()) {
            gameData.sourceCreatedTokens.remove(sourceId, trackedTokenIds);
        }
    }
}

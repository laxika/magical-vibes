package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves the creature portion of Everything Comes to Dust. */
@Component
@RequiredArgsConstructor
public class ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> convokingCreatures = entry.getConvokeCreatureIds().stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(java.util.Objects::nonNull)
                .toList();
        List<Permanent> toExile = new ArrayList<>();

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && convokingCreatures.stream().noneMatch(convoker ->
                        gameQueryService.shareCreatureType(gameData, permanent, convoker))) {
                    toExile.add(permanent);
                }
            }
        });

        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent permanent : toExile) {
                if (permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                    gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                }
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        entry.setEventValue(toExile.size());
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}

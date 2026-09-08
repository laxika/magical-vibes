package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPlayerPermanentsIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleTargetPlayerPermanentsIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetPlayerPermanentsIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ShuffleTargetPlayerPermanentsIntoLibraryEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> toShuffle = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : List.copyOf(battlefield)) {
                if (e.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, e.filter())) {
                    toShuffle.add(permanent);
                }
            }
        }

        for (Permanent permanent : toShuffle) {
            String name = permanent.getCard().getName();
            if (permanentRemovalService.removePermanentToLibraryShuffled(gameData, permanent)) {
                gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
                log.info("Game {} - {} shuffled into owner's library", gameData.id, name);
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetOnTopOfLibraryEffect) effect;
        boolean bothAndShuffle = e.scope() == PutOnTopOfLibraryScope.SELF_AND_TARGET;

        if (e.scope() != PutOnTopOfLibraryScope.SELF) {
            tuck(gameData, gameQueryService.findPermanentById(gameData, entry.getTargetId()), bothAndShuffle);
        }
        if (e.scope() != PutOnTopOfLibraryScope.TARGET) {
            tuck(gameData, gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()), bothAndShuffle);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void tuck(GameData gameData, Permanent permanent, boolean shuffle) {
        if (permanent == null) return;

        if (permanentRemovalService.removePermanentToLibraryTop(gameData, permanent, shuffle)) {
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is put on top of its owner's library."));
            log.info("Game {} - {} put on top of library", gameData.id, permanent.getCard().getName());
        }
    }
}

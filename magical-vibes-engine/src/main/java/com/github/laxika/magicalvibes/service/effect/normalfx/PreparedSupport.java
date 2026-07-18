package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared helpers for Secrets of Strixhaven "Prepared" state changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreparedSupport {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    /**
     * Marks {@code permanent} prepared: exiles a castable copy of its prepare spell for {@code controllerId}.
     * No-op when already prepared or when the permanent has no prepare spell.
     *
     * @return {@code true} when the permanent became prepared
     */
    public boolean preparePermanent(GameData gameData, Permanent permanent, UUID controllerId) {
        if (permanent == null || permanent.isPrepared() || controllerId == null) {
            return false;
        }
        Card prepareSpell = permanent.getCard().getBackFaceCard();
        if (prepareSpell == null) {
            return false;
        }

        UUID prepareCopyControllerId = controllerId;
        Card copy = copySupport.createCopyCard(prepareSpell);
        exileService.exileCard(gameData, prepareCopyControllerId, copy);
        gameData.exilePlayPermissions.put(copy.getId(), prepareCopyControllerId);

        permanent.setPrepared(true);
        permanent.setPreparedSpellCardId(copy.getId());

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(permanent.getCard(), " becomes prepared."));
        log.info("Game {} - {} becomes prepared (prepare spell {} exiled)",
                gameData.id, permanent.getCard().getName(), copy.getName());
        return true;
    }

    /**
     * Marks {@code permanent} unprepared, removing its exiled prepare-spell copy if present.
     * No-op when the permanent is not prepared.
     *
     * @return {@code true} when the permanent was prepared and is now unprepared
     */
    public boolean unpreparePermanent(GameData gameData, Permanent permanent) {
        if (permanent == null || !permanent.isPrepared()) {
            return false;
        }
        UUID prepareCopyId = permanent.getPreparedSpellCardId();
        if (prepareCopyId != null) {
            gameData.removeFromExile(prepareCopyId);
            gameData.exilePlayPermissions.remove(prepareCopyId);
        }
        permanent.setPrepared(false);
        permanent.setPreparedSpellCardId(null);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(permanent.getCard(), " becomes unprepared."));
        log.info("Game {} - {} becomes unprepared", gameData.id, permanent.getCard().getName());
        return true;
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;

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
        if (!hasPrepareSpell(permanent.getCard())) {
            return false;
        }

        permanent.setPrepared(true);
        Card copy = exilePrepareSpellCopy(gameData, permanent, controllerId);

        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " becomes prepared."));
        log.info("Game {} - {} becomes prepared (prepare spell {} exiled)",
                gameData.id, permanent.getCard().getName(), copy.getName());
        return true;
    }

    /**
     * True when the card's back face is a prepare spell: an instant or sorcery that is not the
     * adventure half of an Adventure creature. Only such creatures can become prepared.
     */
    private static boolean hasPrepareSpell(Card card) {
        Card backFace = card.getBackFaceCard();
        if (backFace == null) {
            return false;
        }
        if (card.getKeywords().contains(Keyword.PREPARED)) {
            return true;
        }
        return (backFace.hasType(CardType.INSTANT) || backFace.hasType(CardType.SORCERY))
                && card.getCastingOption(AdventureCast.class).isEmpty();
    }

    /**
     * Removes the exiled prepare-spell copy of a prepared permanent that is leaving the battlefield
     * by phasing out (CR 722.3c: the copy stays in exile only while the prepared permanent remains
     * on the battlefield). The permanent keeps its prepared designation.
     */
    public void releasePreparedCopyOnPhaseOut(GameData gameData, Permanent permanent) {
        if (permanent == null || !permanent.isPrepared()) {
            return;
        }
        UUID prepareCopyId = permanent.getPreparedSpellCardId();
        if (prepareCopyId != null) {
            gameData.removeFromExile(prepareCopyId);
            gameData.exilePlayPermissions.remove(prepareCopyId);
        }
        permanent.setPreparedSpellCardId(null);
    }

    /**
     * Exiles a new prepare-spell copy for a prepared permanent that phases in (CR 722.3c). No-op
     * unless the permanent is prepared and currently has no copy in exile.
     */
    public void recreatePreparedCopyOnPhaseIn(GameData gameData, Permanent permanent, UUID controllerId) {
        if (permanent == null || !permanent.isPrepared() || permanent.getPreparedSpellCardId() != null
                || controllerId == null || permanent.getCard().getBackFaceCard() == null) {
            return;
        }
        Card copy = exilePrepareSpellCopy(gameData, permanent, controllerId);
        log.info("Game {} - {} phases in prepared (prepare spell {} exiled)",
                gameData.id, permanent.getCard().getName(), copy.getName());
    }

    private Card exilePrepareSpellCopy(GameData gameData, Permanent permanent, UUID controllerId) {
        Card copy = copySupport.createCopyCard(permanent.getCard().getBackFaceCard());
        exileService.exileCard(gameData, controllerId, copy);
        gameData.exilePlayPermissions.put(copy.getId(), controllerId);
        permanent.setPreparedSpellCardId(copy.getId());
        return copy;
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

        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " becomes unprepared."));
        log.info("Game {} - {} becomes unprepared", gameData.id, permanent.getCard().getName());
        return true;
    }
}

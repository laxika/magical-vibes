package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSagaCardFromGraveyardAndCopyChapterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.battlefield.SagaChapterService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves The Many Deeds of Belzenlok's graveyard Saga chapter copy. */
@Component
@RequiredArgsConstructor
public class ExileTargetSagaCardFromGraveyardAndCopyChapterEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final CopySupport copySupport;
    private final SagaChapterService sagaChapterService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetSagaCardFromGraveyardAndCopyChapterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTargetSagaCardFromGraveyardAndCopyChapterEffect copyEffect =
                (ExileTargetSagaCardFromGraveyardAndCopyChapterEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        if (targetCardId == null) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard == null || graveyardOwnerId == null || !targetCard.isSaga()) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCardId);
        exileService.exileCard(gameData, graveyardOwnerId, targetCard);
        gameLogService.append(gameData, GameLog.isExiled(targetCard));

        Card copiedSaga = copySupport.createCopyCard(targetCard);
        sagaChapterService.triggerCopiedSagaChapter(
                gameData, copiedSaga, entry.getControllerId(), copyEffect.chapterNumber());
    }
}

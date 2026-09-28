package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCardCopyAndCastWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a fixed card copy that must be cast for free as part of the resolving effect. */
@Component
@RequiredArgsConstructor
public class CreateCardCopyAndCastWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateCardCopyAndCastWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateCardCopyAndCastWithoutPayingManaCostEffect castEffect =
                (CreateCardCopyAndCastWithoutPayingManaCostEffect) effect;
        Card copy = copySupport.createCopyCard(castEffect.cardFactory().get());
        exileService.exileCard(gameData, entry.getControllerId(), copy);
        gameLogService.append(gameData, GameLog.cardTextCard(
                entry.getCard(), " creates a copy of ", copy, " and casts it without paying its mana cost."));
        exileFreeCastQueueSupport.queueCopiesForFreeCast(
                gameData, entry.getControllerId(), List.of(copy.getId()));
    }
}

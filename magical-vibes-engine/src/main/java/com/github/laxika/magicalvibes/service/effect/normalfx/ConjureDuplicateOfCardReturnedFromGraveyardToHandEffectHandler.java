package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCardReturnedFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Veteran Ghoulcaller's graveyard-return duplicate trigger. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfCardReturnedFromGraveyardToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfCardReturnedFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card returnedCard = entry.getTriggeringCardSnapshot();
        if (returnedCard == null) {
            return;
        }

        Card copy = returnedCard.createCardCopy();
        copy.setOwnerId(entry.getControllerId());
        copy.freeze();
        gameData.addCardToHand(entry.getControllerId(), copy);
        gameLogService.append(gameData, GameLog.cardThen(copy, " is conjured into "
                + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
    }
}

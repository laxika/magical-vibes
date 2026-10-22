package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Ugin's top-card exile and Spirit-token creation as one linked operation. */
@Component
@RequiredArgsConstructor
public class ExileTopCardAndCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardAndCreateTokenEffect exileAndCreate = (ExileTopCardAndCreateTokenEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        Card exiledCard = deck == null || deck.isEmpty() ? null : deck.removeFirst();

        if (exiledCard != null) {
            gameData.addToExile(controllerId, exiledCard, null, true);
            gameData.exileLookPermissions.put(exiledCard.getId(), controllerId);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability exiles the top card of its controller's library face down."));
        }

        int createdBefore = entry.getCreatedPermanentIds().size();
        createTokenEffectHandler.resolve(gameData, entry, exileAndCreate.token());
        if (exiledCard != null && entry.getCreatedPermanentIds().size() > createdBefore) {
            UUID tokenPermanentId = entry.getCreatedPermanentIds().getLast();
            gameData.exiledCardReturnToHandOnPermanentLeave.put(tokenPermanentId,
                    new ReturnExiledCardToHandEffect(exiledCard.getId(), controllerId));
        }
    }
}

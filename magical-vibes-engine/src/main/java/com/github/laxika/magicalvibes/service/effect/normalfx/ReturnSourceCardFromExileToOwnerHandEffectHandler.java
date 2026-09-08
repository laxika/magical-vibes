package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToOwnerHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceCardFromExileToOwnerHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceCardFromExileToOwnerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInExileById(gameData, cardId);
        if (sourceCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in exile)."));
            log.info("Game {} - {} return-to-hand trigger fizzles (card {} not in exile)",
                    gameData.id, entry.getCard().getName(), cardId);
            return;
        }

        UUID ownerId = gameQueryService.findExileOwnerById(gameData, cardId);
        if (!gameData.removeFromExile(cardId)) {
            return;
        }
        gameData.addCardToHand(ownerId, sourceCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData,
                GameLog.builder().card(sourceCard)
                        .text(" returns from exile to " + ownerName + "'s hand.").build());
        log.info("Game {} - {} returns from exile to {}'s hand",
                gameData.id, sourceCard.getName(), ownerName);
    }
}

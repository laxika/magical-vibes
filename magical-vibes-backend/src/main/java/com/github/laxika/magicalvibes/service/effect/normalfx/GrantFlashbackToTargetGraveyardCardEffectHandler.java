package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantFlashbackToTargetGraveyardCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFlashbackToTargetGraveyardCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantFlashbackToTargetGraveyardCardEffect) effect;
        if (entry.getTargetCardIds().isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " — no target selected.");
            return;
        }

        UUID targetCardId = entry.getTargetCardIds().getFirst();
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target no longer in graveyard).");
            return;
        }

        // Verify target still matches the required card types
        boolean matchesType = false;
        for (CardType type : e.cardTypes()) {
            if (targetCard.hasType(type)) {
                matchesType = true;
                break;
            }
        }
        if (!matchesType) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getDescription() + " fizzles (target is not a valid card type).");
            return;
        }

        gameData.cardsGrantedFlashbackUntilEndOfTurn.add(targetCard.getId());

        String logEntry = entry.getCard().getName() + " grants flashback to " + targetCard.getName() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} grants flashback to {} until end of turn", gameData.id, entry.getCard().getName(), targetCard.getName());
    }
}

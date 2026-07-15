package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantFlashbackToGraveyardCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFlashbackToGraveyardCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantFlashbackToGraveyardCardsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }

        int count = 0;
        for (Card card : graveyard) {
            boolean matchesType = false;
            for (CardType type : e.cardTypes()) {
                if (card.hasType(type)) {
                    matchesType = true;
                    break;
                }
            }
            if (!matchesType) {
                continue;
            }
            // Skip cards that already have a native flashback option
            if (card.getCastingOption(FlashbackCast.class).isPresent()) {
                continue;
            }
            gameData.cardsGrantedFlashbackUntilEndOfTurn.add(card.getId());
            count++;
        }

        String logEntry = entry.getCard().getName() + " grants flashback to " + count + " card(s) in graveyard until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} grants flashback to {} graveyard card(s)", gameData.id, entry.getCard().getName(), count);
    }
}

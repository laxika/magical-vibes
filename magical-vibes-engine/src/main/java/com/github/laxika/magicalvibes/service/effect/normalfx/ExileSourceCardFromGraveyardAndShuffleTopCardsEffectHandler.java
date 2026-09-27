package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardAndShuffleTopCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves a death trigger that exiles its source and randomizes a pile on top of its controller's library. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileSourceCardFromGraveyardAndShuffleTopCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceCardFromGraveyardAndShuffleTopCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileSourceCardFromGraveyardAndShuffleTopCardsEffect shuffleEffect =
                (ExileSourceCardFromGraveyardAndShuffleTopCardsEffect) effect;
        UUID sourceCardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, sourceCardId);
        if (sourceCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} shuffle-top trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), sourceCardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCardId);
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, sourceCardId);
        if (ownerId != null) {
            exileService.exileCard(gameData, ownerId, sourceCard);
        }
        gameLogService.append(gameData, GameLog.cardThen(sourceCard, " is exiled."));

        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.isEmpty() || shuffleEffect.count() == 0) {
            return;
        }

        int count = Math.min(shuffleEffect.count(), library.size());
        List<Card> pile = new ArrayList<>(library.subList(0, count));
        library.subList(0, count).clear();
        Collections.shuffle(pile);
        library.addAll(0, pile);

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                "'s ability shuffles the top " + count + " cards of its controller's library."));
        log.info("Game {} - {} exiled and shuffled the top {} cards of the controller's library",
                gameData.id, sourceCard.getName(), count);
    }
}

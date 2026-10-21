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

        if (entry.getTriggeringCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(sourceCardId) != entry.getTriggeringCardGraveyardEntryVersion()) {
            return;
        }
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCardId);
        if (ownerId == null || !ownerId.equals(entry.getControllerId())) {
            return;
        }
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.size() < shuffleEffect.count()) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, sourceCardId);
        List<Card> pile = new ArrayList<>(library.subList(0, shuffleEffect.count()));
        library.subList(0, shuffleEffect.count()).clear();
        pile.add(sourceCard);
        Collections.shuffle(pile);
        library.addAll(0, pile);

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                "'s ability exiles it and the top " + shuffleEffect.count()
                        + " cards of its controller's library, then shuffles them and puts them back on top."));
        log.info("Game {} - {} shuffled its pile back onto the top of the controller's library",
                gameData.id, sourceCard.getName());
    }
}

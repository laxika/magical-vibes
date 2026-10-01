package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenConjuresFromAdjacentPlayerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Juggle the Performance's symmetric discard and adjacent-library conjure. */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsHandThenConjuresFromAdjacentPlayerLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final DiscardHandEffectHandler discardHandEffectHandler;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsHandThenConjuresFromAdjacentPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerDiscardsHandThenConjuresFromAdjacentPlayerLibraryEffect conjure =
                (EachPlayerDiscardsHandThenConjuresFromAdjacentPlayerLibraryEffect) effect;
        discardHandEffectHandler.resolve(
                gameData, entry, new DiscardHandEffect(DiscardRecipient.EACH_PLAYER));

        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        if (players.size() < 2 || conjure.count() <= 0) {
            return;
        }

        for (UUID playerId : players) {
            int playerIndex = players.indexOf(playerId);
            UUID adjacentPlayerId = players.get(Math.floorMod(
                    playerIndex + conjure.direction().turnOrderOffset(), players.size()));
            List<Card> candidates = gameData.playerDecks.getOrDefault(adjacentPlayerId, List.of()).stream()
                    .filter(card -> !card.isToken())
                    .toList();
            if (candidates.isEmpty()) {
                continue;
            }

            for (int i = 0; i < conjure.count(); i++) {
                Card duplicate = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()))
                        .createCardCopy();
                duplicate.setOwnerId(playerId);
                duplicate.freeze();
                gameData.perpetualAnyColorManaForCastCardIds.add(duplicate.getId());
                gameData.addCardToHand(playerId, duplicate);
                gameLogService.append(gameData, GameLog.cardThen(duplicate, " is conjured into "
                        + gameData.playerIdToName.get(playerId) + "'s hand."));
            }
        }
    }
}

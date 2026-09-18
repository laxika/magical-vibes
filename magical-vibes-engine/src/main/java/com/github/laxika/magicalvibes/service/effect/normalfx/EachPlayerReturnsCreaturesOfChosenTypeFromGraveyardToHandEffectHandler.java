package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> players = apnapPlayers(gameData);
        UUID nextPlayer = players.stream()
                .filter(playerId -> !entry.getChosenCreatureTypes().containsKey(playerId))
                .findFirst()
                .orElse(null);

        if (gameData.chosenSpellSubtype != null && nextPlayer != null) {
            entry.getChosenCreatureTypes().put(nextPlayer, gameData.chosenSpellSubtype);
            gameData.chosenSpellSubtype = null;
            nextPlayer = players.stream()
                    .filter(playerId -> !entry.getChosenCreatureTypes().containsKey(playerId))
                    .findFirst()
                    .orElse(null);
        }

        if (nextPlayer != null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, nextPlayer);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        for (UUID playerId : players) {
            CardSubtype chosenSubtype = entry.getChosenCreatureTypes().get(playerId);
            if (chosenSubtype == null) {
                continue;
            }

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }

            CardPredicate filter = filterFor(chosenSubtype);
            int matchingCount = 0;
            for (Card card : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(card, filter, null)) {
                    matchingCount++;
                }
            }
            if (matchingCount > 0) {
                gameData.pendingGraveyardReturnQueue.addLast(new PendingGraveyardReturnChoice(
                        playerId, matchingCount, filter, GraveyardChoiceDestination.HAND, true));
            }
        }

        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }

    private CardPredicate filterFor(CardSubtype chosenSubtype) {
        return new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(chosenSubtype),
                new CardKeywordPredicate(Keyword.CHANGELING)
        ));
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return players;
        }
        List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
        rotated.addAll(players.subList(0, activeIndex));
        return rotated;
    }
}

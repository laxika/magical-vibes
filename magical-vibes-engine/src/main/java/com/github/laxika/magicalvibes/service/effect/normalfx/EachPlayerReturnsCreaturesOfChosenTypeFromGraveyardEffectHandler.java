package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Patriarch's Bidding's per-player type choices and mass reanimation. */
@Component
@RequiredArgsConstructor
public class EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerReturnsCreaturesOfChosenTypeFromGraveyardEffect.class;
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
        returnChosenCreatures(gameData, entry);
    }

    private void returnChosenCreatures(GameData gameData, StackEntry entry) {
        Map<UUID, List<Card>> cardsToReturn = new LinkedHashMap<>();
        Map<UUID, List<String>> returnedNamesByPlayer = new LinkedHashMap<>();

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID playerId : gameData.orderedPlayerIds) {
                CardSubtype chosenSubtype = entry.getChosenCreatureTypes().get(playerId);
                if (chosenSubtype == null) {
                    continue;
                }
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                if (graveyard == null || graveyard.isEmpty()) {
                    continue;
                }

                CardPredicate filter = new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(chosenSubtype),
                                new CardKeywordPredicate(Keyword.CHANGELING)
                        ))
                ));
                List<Card> matching = new ArrayList<>();
                for (Card card : graveyard) {
                    if (predicateEvaluationService.matchesCardPredicate(card, filter, null)) {
                        matching.add(card);
                    }
                }
                if (matching.isEmpty()) {
                    continue;
                }

                for (Card card : matching) {
                    graveyard.remove(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, playerId, card);
                }
                cardsToReturn.put(playerId, matching);
                returnedNamesByPlayer.put(playerId, matching.stream().map(Card::getName).toList());
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        graveyardReturnSupport.putCardsOntoBattlefieldSimultaneously(
                gameData, cardsToReturn, false, null);
        for (Map.Entry<UUID, List<String>> returned : returnedNamesByPlayer.entrySet()) {
            String playerName = gameData.playerIdToName.get(returned.getKey());
            gameLogService.append(gameData, GameLog.text(playerName + " returns "
                    + String.join(", ", returned.getValue())
                    + " from graveyard to the battlefield."));
        }
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}

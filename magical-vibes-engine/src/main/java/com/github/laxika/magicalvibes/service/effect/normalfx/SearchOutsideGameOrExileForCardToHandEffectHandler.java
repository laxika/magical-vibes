package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameOrExileForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchOutsideGameOrExileForCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchOutsideGameOrExileForCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var search = (SearchOutsideGameOrExileForCardToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        Map<UUID, Card> candidates = new LinkedHashMap<>();

        for (Card card : gameData.playerSideboards.getOrDefault(controllerId, List.of())) {
            if (matches(gameData, card, search.filter(), controllerId)) {
                candidates.put(card.getId(), card);
            }
        }
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (controllerId.equals(exiled.ownerId()) && !exiled.faceDown()
                        && matches(gameData, exiled.card(), search.filter(), controllerId)) {
                    candidates.putIfAbsent(exiled.card().getId(), exiled.card());
                }
            }
        }

        if (candidates.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " finds no "
                            + CardPredicateUtils.describeFilter(search.filter())
                            + " outside the game or face-up in exile."));
            return;
        }

        String cardLabel = CardPredicateUtils.describeFilter(search.filter());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SearchOutsideGameOrExileCardChoice(
                controllerId, new ArrayList<>(candidates.keySet()), search.filter(), cardLabel));
    }

    private boolean matches(GameData gameData, Card card, com.github.laxika.magicalvibes.model.filter.CardPredicate filter,
                            UUID ownerId) {
        return filter == null || predicateEvaluationService.matchesCardPredicate(
                card, filter, null, gameData, ownerId);
    }
}

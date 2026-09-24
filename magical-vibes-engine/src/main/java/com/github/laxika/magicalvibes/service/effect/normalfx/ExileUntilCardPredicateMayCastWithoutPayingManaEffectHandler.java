package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilCardPredicateMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileUntilCardPredicateMayCastWithoutPayingManaEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUntilCardPredicateMayCastWithoutPayingManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (ExileUntilCardPredicateMayCastWithoutPayingManaEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty - no cards are exiled."));
            return;
        }

        List<Card> exiledCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            exiledCards.add(card);
            exileService.exileCard(gameData, controllerId, card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId)) {
                foundCard = card;
                break;
            }
        }

        String exiledNames = exiledCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                playerName + " exiles " + exiledNames + " from the top of their library."));

        if (foundCard == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles their entire library - no matching card was found."));
            return;
        }

        exiledCards.remove(foundCard);
        exiledCards.forEach(card -> gameData.removeFromExile(card.getId()));
        Collections.shuffle(exiledCards);
        deck.addAll(exiledCards);

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "You may cast " + foundCard.getName() + " without paying its mana cost.",
                foundCard.getId()
        ));
        log.info("Game {} - {} may cast {} from exile without paying its mana cost",
                gameData.id, playerName, foundCard.getName());
    }
}

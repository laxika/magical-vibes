package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsAndPlotEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a top-library look that optionally plots one matching card and puts the rest into hand. */
@Component
@RequiredArgsConstructor
public class LookAtTopCardsAndPlotEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsAndPlotEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsAndPlotEffect e = (LookAtTopCardsAndPlotEffect) effect;
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, Math.max(0, e.count()));
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < topCards.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    topCards.get(i), e.filter(), entry.getCard().getId(), gameData, controllerId)) {
                matchingIndices.add(i);
            }
        }

        int handStart = gameData.playerHands.get(controllerId).size();
        topCards.forEach(card -> gameData.addCardToHand(controllerId, card));
        gameLogService.append(gameData, GameLog.text(result.playerName() + " looks at the top "
                + LibraryRevealSupport.pluralCards(topCards.size()) + " of their library."));

        if (matchingIndices.isEmpty()) {
            return;
        }

        List<Integer> handIndices = matchingIndices.stream()
                .map(index -> handStart + index)
                .toList();
        playerInputService.beginPlotFromHandChoice(gameData, controllerId, handIndices,
                "You may exile a nonland card from among them and plot it.");
    }
}

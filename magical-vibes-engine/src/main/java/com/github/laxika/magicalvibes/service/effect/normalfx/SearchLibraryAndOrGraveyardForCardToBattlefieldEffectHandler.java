package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryAndOrGraveyardForCardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndOrGraveyardForCardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryAndOrGraveyardForCardToBattlefieldEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                           SearchLibraryAndOrGraveyardForCardToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            Optional<Card> graveyardMatch = graveyard.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, effect.filter(), sourceCardId, gameData, controllerId))
                    .findFirst();
            if (graveyardMatch.isPresent()) {
                Card found = graveyardMatch.get();
                graveyard.remove(found);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, found);
                graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, found);
                gameLogService.append(gameData, GameLog.textCardText(
                        playerName + " searches their graveyard, reveals ", found,
                        ", and puts it onto the battlefield."));
                log.info("Game {} - {} finds {} in graveyard", gameData.id, playerName, found.getName());
                return;
            }
        }

        String description = CardPredicateUtils.describeFilter(effect.filter());
        String pluralDescription = description.replaceFirst("\\bcard\\b", "cards");
        String prompt = "Search your library for a " + description + " and put it onto the battlefield.";
        librarySearchSupport.performLibrarySearch(
                gameData,
                controllerId,
                card -> predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), sourceCardId, gameData, controllerId),
                pluralDescription,
                prompt,
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD);
    }
}

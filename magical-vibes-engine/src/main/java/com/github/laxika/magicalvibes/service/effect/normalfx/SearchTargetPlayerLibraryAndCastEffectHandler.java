package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryAndCastEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchTargetPlayerLibraryAndCastEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchTargetPlayerLibraryAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchTargetPlayerLibraryAndCastEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry, SearchTargetPlayerLibraryAndCastEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        Set<CardType> castableTypes = effect.castableTypes();

        // Search prevented (e.g. Leonin Arbiter): the target still shuffles per rules.
        if (!librarySearchSupport.checkSearchRestriction(gameData, controllerId)) {
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            gameBroadcastService.logAndBroadcast(gameData, targetName + "'s library is shuffled.");
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " searches " + targetName + "'s library but it is empty. Library is shuffled.");
            return;
        }

        List<Card> matching = deck.stream()
                .filter(c -> castableTypes.contains(c.getType())
                        || c.getAdditionalTypes().stream().anyMatch(castableTypes::contains))
                .toList();

        if (matching.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " finds no matching card in " + targetName + "'s library. Library is shuffled.");
            return;
        }

        String prompt = "Search " + targetName + "'s library for a card to cast without paying its mana cost.";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, matching)
                        .targetPlayerId(targetPlayerId)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING)
                        .build(),
                prompt, true, controllerName + " searches " + targetName + "'s library.");

        log.info("Game {} - {} searching {}'s library to cast without paying ({} matching cards)",
                gameData.id, controllerName, targetName, matching.size());
    }
}

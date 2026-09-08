package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.VerdantMasteryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VerdantMasteryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final LibrarySearchSupport librarySearchSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return VerdantMasteryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VerdantMasteryEffect verdantMastery = (VerdantMasteryEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null || librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            return;
        }

        LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId);
        List<Card> searchableCards = deck.subList(0, Math.min(topLimit, deck.size())).stream()
                .filter(this::isBasicLand)
                .toList();
        if (searchableCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " searches their library but finds no basic land cards. Library is shuffled."));
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.VerdantMasterySearchChoice(
                        controllerId, searchableCards, verdantMastery.alternateCost()));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId)
                        + " searches their library for up to four basic land cards."));
    }

    private boolean isBasicLand(Card card) {
        return card.hasType(CardType.LAND)
                && card.getSupertypes().contains(CardSupertype.BASIC);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandBottomExileEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsHandBottomExileEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsHandBottomExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsHandBottomExileEffect e = (LookAtTopCardsHandBottomExileEffect) effect;
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count());
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        if (topCards.size() == 1) {
            gameData.addCardToHand(controllerId, topCards.getFirst());
            gameLogService.append(gameData, GameLog.text(playerName
                    + " looks at the top card of their library and puts it into their hand."));
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.HandBottomExileChoice(controllerId, topCards));
        gameLogService.append(gameData, GameLog.text(playerName + " looks at the top "
                + LibraryRevealSupport.pluralCards(topCards.size()) + " of their library."));
        log.info("Game {} - {} resolving {} with {} cards", gameData.id, playerName,
                entry.getCard().getName(), topCards.size());
    }
}

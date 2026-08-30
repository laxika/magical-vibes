package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfChosenTypeToHandRestToBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealTopCardsOfChosenTypeToHandRestToBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsOfChosenTypeToHandRestToBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsOfChosenTypeToHandRestToBottomEffect reveal =
                (RevealTopCardsOfChosenTypeToHandRestToBottomEffect) effect;

        if (gameData.chosenSpellPermanentType == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCardTypeChoice(gameData, entry.getControllerId());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardType chosenType = gameData.chosenSpellPermanentType;
        gameData.chosenSpellPermanentType = null;

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, reveal.count());
        if (result == null) return;

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        String cardName = entry.getCard().getName();

        String revealedNames = topCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
        gameLogService.append(gameData, GameLog.text(playerName + " reveals " + revealedNames
                + " from the top of their library with " + cardName + "."));

        List<Card> toHand = new ArrayList<>();
        List<Card> toBottom = new ArrayList<>();
        for (Card card : topCards) {
            if (card.hasType(chosenType)) {
                toHand.add(card);
            } else {
                toBottom.add(card);
            }
        }

        for (Card card : toHand) {
            gameData.addCardToHand(controllerId, card);
        }
        if (!toHand.isEmpty()) {
            String handNames = toHand.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameLogService.append(gameData, GameLog.text(playerName + " puts " + handNames + " into their hand."));
        }

        if (!toBottom.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, toBottom);
        }
    }
}

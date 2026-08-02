package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilColorToHandRestExiledEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilColorToHandRestExiledEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilColorToHandRestExiledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CardColor wanted = ((RevealUntilColorToHandRestExiledEffect) effect).color();
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        List<Card> revealed = new ArrayList<>();
        Card found = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (hasColor(card, wanted)) {
                found = card;
                break;
            }
        }

        if (revealed.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed with " + cardName + "."));
            return;
        }

        String revealedNames = revealed.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals " + revealedNames + " from the top of their library with " + cardName + "."));

        List<Card> rest = new ArrayList<>(revealed);
        if (found != null) {
            rest.remove(found);
            gameData.playerHands.get(controllerId).add(found);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " puts " + found.getName() + " into their hand."));
        } else {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library — no "
                            + wanted.name().toLowerCase() + " card found."));
        }

        for (Card card : rest) {
            exileService.exileCard(gameData, controllerId, card);
        }

        log.info("Game {} - {} resolving {} — found={}, {} cards exiled",
                gameData.id, playerName, cardName,
                found != null ? found.getName() : "none", rest.size());
    }

    private boolean hasColor(Card card, CardColor wanted) {
        List<CardColor> colors = card.getColors();
        if (colors != null && !colors.isEmpty()) {
            return colors.contains(wanted);
        }
        return card.getColor() == wanted;
    }
}

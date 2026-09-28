package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTargetPlayerLibraryUntilCreatureAndBecomeCopyUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
public class RevealTargetPlayerLibraryUntilCreatureAndBecomeCopyUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BecomeCopyOfCardUntilEndOfTurnEffectHandler copyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTargetPlayerLibraryUntilCreatureAndBecomeCopyUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCreature = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (card.hasType(CardType.CREATURE)) {
                foundCreature = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData,
                GameLog.text(playerName + " reveals " + revealedNames + " from the top of their library."));

        if (foundCreature != null) {
            copyHandler.resolve(gameData, entry, new BecomeCopyOfCardUntilEndOfTurnEffect(foundCreature));
        } else {
            gameLogService.append(gameData,
                    GameLog.text(playerName + " reveals their entire library — no creature card was found."));
        }

        Collections.shuffle(revealedCards);
        deck.addAll(revealedCards);
        log.info("Game {} - {} reveals {} cards from their library and found creature={}",
                gameData.id, playerName, revealedCards.size(),
                foundCreature != null ? foundCreature.getName() : "none");
    }
}

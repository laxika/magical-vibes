package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandBottomThenBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealUntilNonlandBottomThenBoostSelfByManaValueEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilNonlandBottomThenBoostSelfByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card nonland = null;
        while (!deck.isEmpty() && nonland == null) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (!card.hasType(CardType.LAND)) {
                nonland = card;
            }
        }

        String revealedNames = revealed.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(playerName + " reveals " + revealedNames
                + " from the top of their library with " + entry.getCard().getName() + "."));

        if (nonland != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                int powerBoost = nonland.getManaValue();
                source.setPowerModifier(source.getPowerModifier() + powerBoost);
                gameLogService.append(gameData, GameLog.builder()
                        .card(source.getCard())
                        .text(String.format(" gets %+d/+0 until end of turn.", powerBoost))
                        .build());
            }
        }

        libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, revealed);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandBoostTargetCreatureByManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilNonlandBoostTargetCreatureByManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilNonlandBoostTargetCreatureByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty \u2014 no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card nonland = null;
        while (!deck.isEmpty() && nonland == null) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (!card.hasType(CardType.LAND)) {
                nonland = card;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals " + revealedNames + " from the top of their library."));

        if (nonland != null) {
            int manaValue = nonland.getManaValue();
            entry.setEventValue(manaValue);
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target != null) {
                target.setPowerModifier(target.getPowerModifier() + manaValue);
                target.setToughnessModifier(target.getToughnessModifier() - manaValue);
                gameLogService.append(gameData, GameLog.builder()
                        .card(target.getCard())
                        .text(String.format(" gets +%d/-%d until end of turn.", manaValue, manaValue))
                        .build());
                log.info("Game {} - {} gets +{}/-{}", gameData.id, target.getCard().getName(),
                        manaValue, manaValue);
            }
        } else {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library \u2014 no nonland card was found."));
        }

        libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, revealedCards);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandToHandRestToBottomDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealUntilNonlandToHandRestToBottomDealManaValueDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilNonlandToHandRestToBottomDealManaValueDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        List<Card> revealed = new ArrayList<>();
        List<Card> lands = new ArrayList<>();
        Card nonland = null;
        while (!deck.isEmpty() && nonland == null) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (card.hasType(CardType.LAND)) {
                lands.add(card);
            } else {
                nonland = card;
            }
        }

        String revealedNames = revealed.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
        gameLogService.append(gameData, GameLog.text(playerName + " reveals " + revealedNames
                + " from the top of their library with " + sourceName + "."));

        if (nonland != null) {
            int manaValue = nonland.getManaValue();
            gameLogService.append(gameData, GameLog.builder()
                    .text(sourceName + " deals damage equal to ")
                    .card(nonland)
                    .text("'s mana value (" + manaValue + ").")
                    .build());
            if (manaValue > 0 && entry.getTargetId() != null) {
                int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);
                damageSupport.resolveAnyTargetDamage(gameData, entry, entry.getTargetId(), damage, false);
                gameOutcomeService.checkWinCondition(gameData);
            }

            gameData.addCardToHand(controllerId, nonland);
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " puts ")
                    .card(nonland)
                    .text(" into their hand.")
                    .build());
        }

        if (!lands.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, lands);
        }
    }
}

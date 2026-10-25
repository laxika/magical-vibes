package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndDrainOpponentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardPutIntoHandAndDrainOpponentsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardPutIntoHandAndDrainOpponentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        var deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.removeFirst();
        int manaValue = topCard.getManaValue();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(topCard)
                .text(" (mana value " + manaValue + ") from the top of their library ("
                        + sourceName + ").")
                .build());
        gameData.addCardToHand(controllerId, topCard);

        if (manaValue <= 0) {
            return;
        }

        // The life losses and the gain are steps of their own, so a death replacement question a Lich's Duel
        // Mastery sacrifice asks (CR 616.1f) reruns only the losses, not the reveal.
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                new LoseLifeEffect(manaValue, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(manaValue)));

        log.info("Game {} - {} drains opponents for {} after revealing {} via {}",
                gameData.id, playerName, manaValue, topCard.getName(), sourceName);
    }
}

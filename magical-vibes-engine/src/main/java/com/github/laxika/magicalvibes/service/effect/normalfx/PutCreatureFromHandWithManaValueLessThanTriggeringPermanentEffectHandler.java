package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        int triggeringManaValue = entry.getEventValue();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (card.hasType(CardType.CREATURE) && card.getManaValue() < triggeringManaValue) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName
                    + " has no creature cards with mana value less than " + triggeringManaValue + " in hand."));
            log.info("Game {} - {} has no creature cards with mana value less than {} in hand",
                    gameData.id, playerName, triggeringManaValue);
            return;
        }

        playerInputService.beginCardChoice(gameData, playerId, validIndices,
                "You may put a creature card with mana value less than " + triggeringManaValue
                        + " from your hand onto the battlefield tapped and attacking.",
                true, false, false, null, true);
    }
}

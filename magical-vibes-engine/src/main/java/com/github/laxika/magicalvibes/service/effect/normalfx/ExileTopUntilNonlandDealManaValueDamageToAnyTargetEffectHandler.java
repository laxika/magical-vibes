package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandDealManaValueDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandDealManaValueDamageToAnyTargetEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandDealManaValueDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + entry.getCard().getName() + ")."));
            return;
        }

        Card nonland = null;
        int exiledCount = 0;
        while (!deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exiledCount++;
            if (!topCard.hasType(CardType.LAND)) {
                nonland = topCard;
                break;
            }
        }

        if (nonland == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles " + exiledCount + " card(s) from the top of their library"
                            + " — no nonland card found."));
            log.info("Game {} - {} exiled {} cards with {} without finding a nonland card",
                    gameData.id, playerName, exiledCount, entry.getCard().getName());
            return;
        }

        int manaValue = nonland.getManaValue();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles cards until ").card(nonland)
                .text(" (mana value " + manaValue + ") with " + entry.getCard().getName() + ".")
                .build());

        if (manaValue <= 0 || entry.getTargetId() == null) return;

        int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, entry.getTargetId(), damage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}

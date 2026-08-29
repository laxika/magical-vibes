package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardDealManaValueDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealTopCardDealManaValueDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardDealManaValueDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + entry.getCard().getName() + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        int manaValue = topCard.getManaValue();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(topCard)
                .text(" (mana value " + manaValue + ") from the top of their library ("
                        + entry.getCard().getName() + ").")
                .build());

        if (manaValue <= 0 || entry.getTargetId() == null) return;

        int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, entry.getTargetId(), damage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}

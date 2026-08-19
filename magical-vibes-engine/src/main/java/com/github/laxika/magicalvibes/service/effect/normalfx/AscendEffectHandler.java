package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AscendEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AscendEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        grantBlessingIfEligible(gameData, entry.getControllerId(), entry.getCard());
    }

    public void checkPermanentAscend(GameData gameData, UUID controllerId) {
        if (gameData.playersWithCityBlessing.contains(controllerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.size() < 10) {
            return;
        }

        Card ascendSource = battlefield.stream()
                .map(Permanent::getCard)
                .filter(card -> card.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(AscendEffect.class::isInstance))
                .findFirst()
                .orElse(null);
        if (ascendSource != null) {
            grantBlessingIfEligible(gameData, controllerId, ascendSource);
        }
    }

    private void grantBlessingIfEligible(GameData gameData, UUID controllerId, Card sourceCard) {
        if (gameData.playersWithCityBlessing.contains(controllerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.size() < 10) {
            return;
        }

        gameData.playersWithCityBlessing.add(controllerId);
        gameLogService.append(gameData,
                GameLog.cardThen(sourceCard, "'s controller receives the city's blessing."));
        log.info("Game {} - {} receives the city's blessing", gameData.id, controllerId);
    }
}

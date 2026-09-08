package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopyNextInstantOrSorceryCastThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextInstantOrSorceryCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopyNextInstantOrSorceryCastThisTurnEffect copyEffect =
                (CopyNextInstantOrSorceryCastThisTurnEffect) effect;
        if (copyEffect.maxManaValue() == null) {
            gameData.pendingNextInstantSorceryCopyThisTurnCount.merge(entry.getControllerId(), 1, Integer::sum);
        } else {
            gameData.pendingNextInstantSorceryCopyThisTurnMaxManaValues
                    .computeIfAbsent(entry.getControllerId(), ignored -> new ArrayList<>())
                    .add(copyEffect.maxManaValue());
        }
        log.info("Game {} - {} will copy their next instant or sorcery spell this turn",
                gameData.id, entry.getControllerId());
    }
}

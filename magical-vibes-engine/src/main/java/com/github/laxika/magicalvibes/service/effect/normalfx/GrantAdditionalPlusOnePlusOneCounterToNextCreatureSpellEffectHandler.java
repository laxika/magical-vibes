package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureSpellEmpowerment;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.addPersistentNextCreatureSpellEmpowerment(
                entry.getControllerId(), new CreatureSpellEmpowerment(false, 1));
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" empowers its controller's next creature spell with a +1/+1 counter.")
                .build());
        log.info("Game {} - {} grants its controller's next creature spell an additional +1/+1 counter",
                gameData.id, entry.getCard().getName());
    }
}

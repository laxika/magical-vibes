package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureSpellEmpowerment;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmpowerNextCreatureSpellThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EmpowerNextCreatureSpellThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EmpowerNextCreatureSpellThisTurnEffect empower = (EmpowerNextCreatureSpellThisTurnEffect) effect;
        gameData.addNextCreatureSpellEmpowerment(entry.getControllerId(),
                new CreatureSpellEmpowerment(empower.uncounterable(), empower.additionalPlusOneCounters()));
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" empowers its controller's next creature spell this turn.")
                .build());
        log.info("Game {} - {} empowers the next creature spell of player {} (uncounterable={}, +1/+1 counters={})",
                gameData.id, entry.getCard().getName(), entry.getControllerId(),
                empower.uncounterable(), empower.additionalPlusOneCounters());
    }
}

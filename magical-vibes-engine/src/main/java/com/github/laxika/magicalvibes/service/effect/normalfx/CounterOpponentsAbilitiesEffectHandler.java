package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentsAbilitiesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CounterOpponentsAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterOpponentsAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<StackEntry> candidates = new ArrayList<>(gameData.stack);
        int counteredCount = 0;

        for (StackEntry candidate : candidates) {
            if ((candidate.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                    && candidate.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                    || entry.getControllerId().equals(candidate.getControllerId())
                    || !gameData.stack.contains(candidate)) {
                continue;
            }

            if (gameQueryService.isUncounterable(gameData, candidate.getCard())) {
                log.info("Game {} - {}'s ability cannot be countered",
                        gameData.id, candidate.getCard().getName());
                continue;
            }

            if (counterSupport.counterSpell(gameData, entry, candidate)) {
                counteredCount++;
            }
        }

        log.info("Game {} - {} countered {} opponent abilities",
                gameData.id, entry.getCard().getName(), counteredCount);
    }
}

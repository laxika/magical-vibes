package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnForXValueEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CopyNextInstantOrSorceryCastThisTurnForXValueEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextInstantOrSorceryCastThisTurnForXValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int copies = entry.getXValue();
        if (copies <= 0) {
            return;
        }

        gameData.pendingNextInstantSorceryCopyThisTurnCount.merge(
                entry.getControllerId(), copies, Integer::sum);
        log.info("Game {} - {} will copy their next instant or sorcery spell {} times this turn",
                gameData.id, entry.getControllerId(), copies);
    }
}

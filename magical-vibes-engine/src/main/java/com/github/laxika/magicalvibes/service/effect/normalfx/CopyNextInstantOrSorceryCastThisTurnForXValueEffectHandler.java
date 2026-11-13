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

        // "Copy it X times": one delayed triggered ability making X copies (CR 603.7).
        gameData.addPendingSpellCopy(entry.getControllerId(), new GameData.PendingSpellCopy(entry.getCard(),
                entry.getSourcePermanentId(), copies, null, java.util.List.of(), gameData.resolutionOrdinal, null));
        log.info("Game {} - {} will copy their next instant or sorcery spell {} times this turn",
                gameData.id, entry.getControllerId(), copies);
    }
}

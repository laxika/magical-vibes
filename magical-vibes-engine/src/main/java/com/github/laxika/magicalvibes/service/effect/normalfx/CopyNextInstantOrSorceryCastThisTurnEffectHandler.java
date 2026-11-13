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
        // One delayed triggered ability of this resolution (CR 603.7), its source the object whose ability made it
        // (CR 603.7e).
        gameData.addPendingSpellCopy(entry.getControllerId(), new GameData.PendingSpellCopy(entry.getCard(),
                entry.getSourcePermanentId(), copyEffect.dynamicCopyCount() == null ? 1 : 0,
                copyEffect.dynamicCopyCount(),
                copyEffect.maxManaValue() == null ? java.util.List.of() : java.util.List.of(copyEffect.maxManaValue()),
                gameData.resolutionOrdinal, null));
        log.info("Game {} - {} will copy their next instant or sorcery spell this turn",
                gameData.id, entry.getControllerId());
    }
}

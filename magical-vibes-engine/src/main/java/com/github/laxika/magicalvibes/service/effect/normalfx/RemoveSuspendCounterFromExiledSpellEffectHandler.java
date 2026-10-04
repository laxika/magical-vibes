package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSuspendCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveSuspendCounterFromExiledSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final RemoveTimeCounterFromExiledCardEffectHandler removeTimeCounterHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveSuspendCounterFromExiledSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = ((RemoveSuspendCounterFromExiledSpellEffect) effect).cardId();
        removeTimeCounter(gameData, cardId);
    }

    public void removeTimeCounter(GameData gameData, UUID cardId) {
        int index = indexOf(gameData, cardId);
        if (index < 0) return;

        GameData.SuspendedSpellExile pending = gameData.suspendedSpellExiles.get(index);
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null) {
            gameData.suspendedSpellExiles.remove(index);
            return;
        }

        int remaining = pending.counters() - 1;
        if (remaining > 0) {
            gameData.suspendedSpellExiles.set(index,
                    new GameData.SuspendedSpellExile(cardId, pending.ownerId(), remaining));
            gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                    " loses a time counter (" + remaining + " left)."));
            return;
        }

        gameData.suspendedSpellExiles.remove(index);
        removeTimeCounterHandler.queueCastTrigger(gameData, exiled, true);
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                " loses its last time counter. Its owner may cast it without paying its mana cost."));
    }

    private int indexOf(GameData gameData, UUID cardId) {
        for (int i = 0; i < gameData.suspendedSpellExiles.size(); i++) {
            if (cardId.equals(gameData.suspendedSpellExiles.get(i).cardId())) return i;
        }
        return -1;
    }
}

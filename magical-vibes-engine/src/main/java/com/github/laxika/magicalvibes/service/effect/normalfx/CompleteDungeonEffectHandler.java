package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CompleteDungeonEffect;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompleteDungeonEffectHandler implements NormalEffectHandlerBean {

    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CompleteDungeonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CompleteDungeonEffect completion = (CompleteDungeonEffect) effect;
        DungeonProgress progress = gameData.playerDungeonProgress.get(entry.getControllerId());
        if (progress == null || progress.dungeon() != completion.dungeon() || !progress.isBottomRoom()) {
            return;
        }

        gameData.playerDungeonProgress.remove(entry.getControllerId());
        gameData.recordCompletedDungeon(entry.getControllerId(), completion.dungeon());
        triggerCollectionService.checkDungeonCompletionTriggers(gameData, entry.getControllerId());
    }
}

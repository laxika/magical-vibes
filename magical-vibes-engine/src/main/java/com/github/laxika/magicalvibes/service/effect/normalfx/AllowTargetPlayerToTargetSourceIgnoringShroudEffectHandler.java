package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowTargetPlayerToTargetSourceIgnoringShroudEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowTargetPlayerToTargetSourceIgnoringShroudEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowTargetPlayerToTargetSourceIgnoringShroudEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null || entry.getTargetId() == null) {
            return;
        }
        var source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        source.allowShroudIgnoredBy(entry.getTargetId());
        log.info("Game {} - {} can be targeted by {} this turn as though it didn't have shroud",
                gameData.id, source.getCard().getName(), gameData.playerIdToName.get(entry.getTargetId()));
    }
}

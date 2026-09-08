package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.GrantChosenLandwalkAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenLandwalkAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantChosenLandwalkAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantChosenLandwalkAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null
                || gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) == null) {
            return;
        }

        gameData.queueDelayedAction(new GrantChosenLandwalkAtNextUpkeep(
                entry.getControllerId(), entry.getSourcePermanentId(), entry.getCard()));
        log.info("Game {} - {} registers chosen landwalk at its controller's next upkeep",
                gameData.id, entry.getCard().getName());
    }
}

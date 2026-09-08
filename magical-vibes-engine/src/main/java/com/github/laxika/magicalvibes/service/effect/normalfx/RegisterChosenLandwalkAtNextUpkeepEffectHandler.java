package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.GrantChosenLandwalkAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterChosenLandwalkAtNextUpkeepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisterChosenLandwalkAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterChosenLandwalkAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        gameData.queueDelayedAction(new GrantChosenLandwalkAtNextUpkeep(
                entry.getSourcePermanentId(), entry.getControllerId(), entry.getCard()));
        log.info("Game {} - {} registers chosen landwalk at their next upkeep",
                gameData.id, entry.getCard().getName());
    }
}

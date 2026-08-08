package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawOnCreatureEntersThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;

@Slf4j
@Component
public class DrawOnCreatureEntersThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawOnCreatureEntersThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.creatureEntersDrawSourcesThisTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> Collections.synchronizedList(new ArrayList<>()))
                .add(entry.getCard());
        log.info("Game {} - {} may draw a card whenever a creature enters this turn",
                gameData.id, entry.getControllerId());
    }
}

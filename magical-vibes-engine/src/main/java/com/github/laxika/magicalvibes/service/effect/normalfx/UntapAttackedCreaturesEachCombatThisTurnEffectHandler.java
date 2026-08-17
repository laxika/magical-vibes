package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEachCombatThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;

@Slf4j
@Component
public class UntapAttackedCreaturesEachCombatThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapAttackedCreaturesEachCombatThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.untapAttackedCreaturesEachCombatThisTurnSources
                .computeIfAbsent(entry.getControllerId(), ignored ->
                        Collections.synchronizedList(new ArrayList<>()))
                .add(entry.getCard());
        log.info("Game {} - {} will untap attacked creatures at the beginning of each combat this turn",
                gameData.id, entry.getCard().getName());
    }
}

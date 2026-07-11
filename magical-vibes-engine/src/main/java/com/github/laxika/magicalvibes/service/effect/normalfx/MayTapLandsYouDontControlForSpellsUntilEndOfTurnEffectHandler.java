package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.mayTapLandsForSpellsUntilEndOfTurn.add(controllerId);

        String logEntry = entry.getCard().getName()
                + ": you may tap lands you don't control for mana until end of turn (spend only to cast spells).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} may tap foreign lands for spells until end of turn", gameData.id, controllerId);
    }
}

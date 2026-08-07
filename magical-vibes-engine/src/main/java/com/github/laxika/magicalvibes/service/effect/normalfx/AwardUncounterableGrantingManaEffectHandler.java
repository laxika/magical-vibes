package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardUncounterableGrantingManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwardUncounterableGrantingManaEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardUncounterableGrantingManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardUncounterableGrantingManaEffect) effect;
        UUID controllerId = entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        e.applyTo(pool);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds " + e.amount() + " " + e.color().getCode()
                + " (an instant or sorcery it is spent on can't be countered)."));
        log.info("Game {} - {} adds {} {} (uncounterable-granting)", gameData.id, playerName, e.amount(), e.color());
    }
}

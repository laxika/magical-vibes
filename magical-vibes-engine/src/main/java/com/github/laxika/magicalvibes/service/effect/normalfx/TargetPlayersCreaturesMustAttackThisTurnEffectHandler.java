package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayersCreaturesMustAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayersCreaturesMustAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent p : battlefield) {
            // "if able" is enforced by combat: the must-attack requirement only bites creatures that
            // can legally attack, so setting the flag on ones that can't is harmless.
            if (gameQueryService.isCreature(gameData, p)) {
                p.setMustAttackThisTurn(true);
                count++;
            }
        }

        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(entry.getCard().getName() + " forces " + count
                        + " creature(s) to attack this turn if able."));
        log.info("Game {} - {} forces {} creature(s) to attack this turn if able",
                gameData.id, entry.getCard().getName(), count);
    }
}

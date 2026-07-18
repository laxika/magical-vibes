package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoseAllCreatureTypesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseAllCreatureTypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var lose = (LoseAllCreatureTypesEffect) effect;

        if (lose.scope() == GrantScope.TARGET_PLAYERS_CREATURES) {
            UUID targetPlayerId = entry.getTargetId();
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                return;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            int count = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (!gameQueryService.isCreature(gameData, permanent)) {
                        continue;
                    }
                    permanent.setLosesAllCreatureTypesUntilEndOfTurn(true);
                    count++;
                }
            }
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" makes " + count + " creature(s) lose all creature types until end of turn.").build());
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        target.setLosesAllCreatureTypesUntilEndOfTurn(true);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " loses all creature types until end of turn."));
    }
}

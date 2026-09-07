package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.InvestigateForEachTargetPlayerCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InvestigateForEachTargetPlayerCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return InvestigateForEachTargetPlayerCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetPlayerIds = entry.getTargetIds();
        if (targetPlayerIds == null || targetPlayerIds.isEmpty()) {
            if (entry.getTargetId() == null) {
                return;
            }
            targetPlayerIds = List.of(entry.getTargetId());
        }

        int creatureCount = 0;
        for (UUID playerId : targetPlayerIds) {
            if (!gameData.playerIds.contains(playerId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureCount++;
                }
            }
        }

        if (creatureCount > 0) {
            createTokenEffectHandler.resolveForController(
                    gameData, entry, CreateTokenEffect.ofClueToken(creatureCount), entry.getControllerId());
        }
    }
}

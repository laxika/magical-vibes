package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachTargetPlayerCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.CreatureCountSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokensForEachTargetPlayerCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensForEachTargetPlayerCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetPlayerIds = entry.getTargetIds();
        if (targetPlayerIds == null || targetPlayerIds.isEmpty()) {
            return;
        }

        int creatureCount = 0;
        for (UUID playerId : targetPlayerIds) {
            if (!gameData.playerIds.contains(playerId)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureCount += CreatureCountSupport.creatureCount(gameData, permanent, gameQueryService);
                }
            }
        }

        if (creatureCount <= 0) {
            return;
        }

        CreateTokensForEachTargetPlayerCreatureEffect e =
                (CreateTokensForEachTargetPlayerCreatureEffect) effect;
        CreateTokenEffect token = e.tokenTemplate().withAmount(creatureCount);
        createTokenEffectHandler.resolveForController(gameData, entry, token, entry.getControllerId());
    }
}

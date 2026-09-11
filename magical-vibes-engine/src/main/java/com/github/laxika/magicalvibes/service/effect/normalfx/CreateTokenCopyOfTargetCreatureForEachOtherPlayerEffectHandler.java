package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetCreatureForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetCreatureForEachOtherPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetCreatureForEachOtherPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent targetCreature = gameQueryService.findPermanentById(gameData, targetId);
        if (targetCreature == null || !gameQueryService.isCreature(gameData, targetCreature)) return;

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
        if (targetControllerId == null) return;

        CreateTokenCopyOfTargetPermanentEffect copyProfile = new CreateTokenCopyOfTargetPermanentEffect();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(targetControllerId)) {
                tokenCopySupport.createTokenCopies(
                        gameData, entry, List.of(targetCreature.getCard()), null, playerId, copyProfile);
            }
        }
    }
}

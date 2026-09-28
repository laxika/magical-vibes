package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfSourceForEachOtherPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSourceForEachOtherPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenCopyOfSourceForEachOtherPlayerEffect copyEffect =
                (CreateTokenCopyOfSourceForEachOtherPlayerEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.getCard().isToken() || !gameQueryService.isCreature(gameData, source)) return;

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (sourceControllerId == null) return;

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(sourceControllerId)) {
                tokenCopySupport.createTokenCopies(
                        gameData, entry, List.of(source.getCard()), source, playerId,
                        copyEffect.copyProfile());
            }
        }
    }
}

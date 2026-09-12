package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensWithChosenCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokensWithChosenCountersEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensWithChosenCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokensWithChosenCountersEffect createAndChoose =
                (CreateTokensWithChosenCountersEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();

        createTokenEffectHandler.resolve(gameData, entry, createAndChoose.tokenTemplate());
        if (gameData.resolvingMayEffectFromStack || !gameData.pendingMayAbilities.isEmpty()) {
            return;
        }

        List<UUID> createdTokenIds = entry.getCreatedPermanentIds().subList(
                createdBefore, entry.getCreatedPermanentIds().size()).stream()
                .filter(tokenId -> gameQueryService.findPermanentById(gameData, tokenId) != null)
                .toList();
        if (!createdTokenIds.isEmpty()) {
            playerInputService.beginCreateTokenCounterChoice(
                    gameData, entry.getControllerId(), entry.getCard(), createdTokenIds,
                    createAndChoose.counterTypes());
        }
    }
}

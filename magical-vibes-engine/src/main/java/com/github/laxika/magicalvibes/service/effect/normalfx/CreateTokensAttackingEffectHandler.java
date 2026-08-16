package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokensAttackingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensAttackingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokensAttackingEffect create = (CreateTokensAttackingEffect) effect;
        if (create.amount() <= 0) {
            return;
        }

        int tokenCount = create.amount() * gameQueryService.getTokenMultiplier(gameData, entry.getControllerId());
        PermanentChoiceContext.CreateTokensAttacking context = new PermanentChoiceContext.CreateTokensAttacking(
                entry.getControllerId(), entry.getCard(), create.tokenEffect(), create.amount(), tokenCount, List.of());
        beginTargetChoice(gameData, context);
    }

    private void beginTargetChoice(GameData gameData, PermanentChoiceContext.CreateTokensAttacking context) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, context.controllerId());
        List<UUID> planeswalkerIds = gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                .map(Permanent::getId)
                .toList();

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginAnyTargetChoice(
                gameData,
                context.controllerId(),
                planeswalkerIds,
                List.of(opponentId),
                "Choose the player or planeswalker for the next Soldier token to attack.");
    }
}

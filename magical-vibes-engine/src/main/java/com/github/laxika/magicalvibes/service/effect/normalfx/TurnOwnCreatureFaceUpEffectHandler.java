package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnOwnCreatureFaceUpEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TurnOwnCreatureFaceUpEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final ObjectProvider<GameService> gameServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnOwnCreatureFaceUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> eligibleIds = eligibleIds(gameData, entry.getControllerId());
        if (eligibleIds.isEmpty()) {
            return;
        }
        if (eligibleIds.size() == 1) {
            turnFaceUp(gameData, eligibleIds.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TurnOwnCreatureFaceUp());
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), eligibleIds,
                "Choose a creature you control to turn face up.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect is waiting for a creature choice");
        }

        Permanent target = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (target == null
                || !target.isFaceDown()
                || !gameQueryService.isCreature(gameData, target)
                || !entry.getControllerId().equals(
                        gameQueryService.findPermanentController(gameData, chosenPermanentId))) {
            throw new IllegalStateException("Choose a face-down creature you control");
        }
        gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCost(gameData, target);
    }

    private List<UUID> eligibleIds(GameData gameData, UUID controllerId) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> permanent.isFaceDown() && gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private void turnFaceUp(GameData gameData, UUID permanentId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent != null) {
            gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCost(gameData, permanent);
        }
    }
}

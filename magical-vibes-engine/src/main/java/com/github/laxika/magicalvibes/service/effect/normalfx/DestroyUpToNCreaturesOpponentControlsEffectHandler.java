package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToNCreaturesOpponentControlsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link DestroyUpToNCreaturesOpponentControlsEffect}: gathers the creatures an opponent
 * of the resolving controller controls and prompts the controller to choose up to {@code maxCount}
 * of them to destroy (choosing none is legal). Completion runs in
 * {@code MultiPermanentChoiceHandlerService} via
 * {@link MultiPermanentChoiceContext.DestroyCreaturesOpponentControls}.
 */
@Component
@RequiredArgsConstructor
public class DestroyUpToNCreaturesOpponentControlsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyUpToNCreaturesOpponentControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyUpToNCreaturesOpponentControlsEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        if (opponentId == null) {
            return;
        }

        List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, opponentId, permanent -> true);
        if (creatureIds.isEmpty()) {
            return;
        }

        int maxCount = Math.min(e.maxCount(), creatureIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, creatureIds, maxCount,
                new MultiPermanentChoiceContext.DestroyCreaturesOpponentControls(
                        entry.getCard().getName(), e.cannotBeRegenerated()),
                "Choose up to " + maxCount + " creature" + (maxCount == 1 ? "" : "s") + " to destroy.");
    }
}

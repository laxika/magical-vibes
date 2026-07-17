package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToNTargetPermanentsToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Return up to N target permanents to their owners' hands." Gathers every permanent on the
 * battlefield and prompts the controller to choose up to N to return (they may choose none).
 * Completion is handled by {@link MultiPermanentChoiceContext.ReturnTargetPermanentsToHand}.
 */
@Component
@RequiredArgsConstructor
public class ReturnUpToNTargetPermanentsToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnUpToNTargetPermanentsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnUpToNTargetPermanentsToHandEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<UUID> permanentIds = new ArrayList<>();
        gameData.forEachPermanent((pid, permanent) -> permanentIds.add(permanent.getId()));

        if (permanentIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    entry.getCard().getName() + " has no permanents to return."));
            return;
        }

        int maxCount = Math.min(e.maxCount(), permanentIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, permanentIds, maxCount,
                new MultiPermanentChoiceContext.ReturnTargetPermanentsToHand(),
                "Choose up to " + maxCount + " permanent" + (maxCount == 1 ? "" : "s")
                        + " to return to their owners' hands.");
    }
}

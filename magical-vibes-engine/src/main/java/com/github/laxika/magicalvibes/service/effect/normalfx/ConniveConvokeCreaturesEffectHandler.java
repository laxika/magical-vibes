package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingConnive;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConniveConvokeCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawBeforeConniveReplacementEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConniveConvokeCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConniveConvokeCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> remaining = entry.getConvokeConniveCreatureIdsToProcess();
        if (remaining == null) {
            remaining = new ArrayList<>(entry.getConvokeCreatureIds());
        } else {
            remaining = new ArrayList<>(remaining);
        }
        entry.setConvokeConniveCreatureIdsToProcess(remaining);

        int replacementDraws = gameQueryService.countPlayerControlledStaticEffects(
                gameData, controllerId, DrawBeforeConniveReplacementEffect.class);
        while (!remaining.isEmpty()) {
            UUID convokeCreatureId = remaining.removeFirst();
            entry.setConvokeConniveCreatureIdsToProcess(remaining);
            if (gameQueryService.findPermanentById(gameData, convokeCreatureId) == null) {
                continue;
            }

            for (int i = 0; i < replacementDraws; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            playerInteractionSupport.applyDrawCards(gameData, controllerId, 1);

            List<Card> hand = gameData.playerHands.get(controllerId);
            int discardAmount = Math.min(1, hand == null ? 0 : hand.size());
            gameData.pendingConnive = null;
            if (discardAmount == 0) {
                continue;
            }

            gameData.pendingConnive = new PendingConnive(convokeCreatureId);
            gameData.discardCausedByOpponent = false;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, discardAmount);
            return;
        }

        entry.setConvokeConniveCreatureIdsToProcess(List.of());
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}

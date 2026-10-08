package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingConnive;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConniveEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawBeforeConniveReplacementEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConniveEachTargetEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConniveEachTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> remaining = entry.getTargetConniveCreatureIdsToProcess();
        if (remaining == null) {
            remaining = new ArrayList<>(entry.targetsForEffect(effect));
        } else {
            remaining = new ArrayList<>(remaining);
        }
        entry.setTargetConniveCreatureIdsToProcess(remaining);

        int replacementDraws = gameQueryService.countPlayerControlledStaticEffects(
                gameData, controllerId, DrawBeforeConniveReplacementEffect.class);
        while (!remaining.isEmpty()) {
            remaining.removeIf(id -> gameQueryService.findPermanentById(gameData, id) == null);
            if (remaining.isEmpty()) break;
            UUID chosen = entry.getChosenPermanentId();
            if (remaining.size() > 1 && (chosen == null || !remaining.contains(chosen))) {
                entry.setTargetConniveCreatureIdsToProcess(remaining);
                gameData.rerunCurrentEffectAfterInteraction = true;
                playerInputService.beginPermanentChoice(gameData, controllerId, remaining,
                        new PermanentChoiceContext.ConniveNextCreatureChoice(),
                        "Choose the next creature to connive.");
                return;
            }
            UUID creatureId = chosen != null && remaining.contains(chosen) ? chosen : remaining.getFirst();
            remaining.remove(creatureId);
            entry.setChosenPermanentId(null);
            entry.setTargetConniveCreatureIdsToProcess(remaining);
            if (gameQueryService.findPermanentById(gameData, creatureId) == null) {
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

            gameData.pendingConnive = new PendingConnive(creatureId);
            gameData.discardCausedByOpponent = false;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, discardAmount);
            return;
        }

        entry.setTargetConniveCreatureIdsToProcess(List.of());
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}

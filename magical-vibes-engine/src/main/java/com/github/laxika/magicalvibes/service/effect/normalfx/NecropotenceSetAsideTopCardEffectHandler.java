package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NecropotenceSetAsideTopCardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Necropotence's "Pay 1 life: Exile the top card of your library face down. Put that card
 * into your hand at the beginning of your next end step." The top library card is exiled and a
 * {@link ReturnExiledCardToHandAtEndStep} delayed action is registered to return it to hand at the
 * controller's next end step (drained in {@code StepTriggerService.handleEndStepTriggers}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NecropotenceSetAsideTopCardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NecropotenceSetAsideTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(controllerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        gameData.addToExile(controllerId, topCard);
        gameData.queueDelayedAction(new ReturnExiledCardToHandAtEndStep(topCard.getId(), controllerId));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName
                + " exiles the top card of their library face down; it returns to their hand at the beginning of their next end step."));
        log.info("Game {} - {} set aside {} with Necropotence (returns at next end step)",
                gameData.id, controllerName, topCard.getName());
    }
}

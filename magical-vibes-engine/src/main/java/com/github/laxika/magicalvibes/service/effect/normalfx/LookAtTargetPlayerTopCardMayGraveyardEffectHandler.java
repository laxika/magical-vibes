package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTargetPlayerTopCardMayGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Eye Spy: the controller looks at the top card of target player's
 * library, then may put it into that player's graveyard. Pushes a may-ability to
 * the controller carrying the resolved target player id.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTargetPlayerTopCardMayGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTargetPlayerTopCardMayGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    targetName + "'s library is empty (" + sourceName + ").");
            return;
        }

        Card topCard = deck.getFirst();
        gameBroadcastService.logAndBroadcast(gameData,
                controllerName + " looks at the top card of " + targetName + "'s library (" + sourceName + ").");
        log.info("Game {} - {} looks at top of {}'s library: {} ({})",
                gameData.id, controllerName, targetName, topCard.getName(), sourceName);

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new LookAtTargetPlayerTopCardMayGraveyardEffect(targetPlayerId)),
                sourceName + " — Put " + topCard.getName() + " into " + targetName + "'s graveyard?",
                null,
                null,
                entry.getSourcePermanentId()
        ));
    }
}

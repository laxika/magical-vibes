package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WordOfCommandEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Begins Word of Command's private selection from the targeted player's hand. */
@Component
@RequiredArgsConstructor
public class WordOfCommandEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WordOfCommandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();
        if (targetId == null || !gameData.playerIds.contains(targetId)) {
            return;
        }
        List<com.github.laxika.magicalvibes.model.Card> hand =
                gameData.playerHands.getOrDefault(targetId, List.of());
        if (hand.isEmpty()) {
            return;
        }

        gameData.wordOfCommandControllerPlayerId = controllerId;
        gameData.wordOfCommandControlledPlayerId = targetId;
        gameData.mindControllerPlayerId = controllerId;
        gameData.mindControlledPlayerId = targetId;
        gameData.mindControlUntilEndOfCombat = false;

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.WordOfCommandCardChoice(
                controllerId, targetId, validIndices,
                "Choose a card from " + gameData.playerIdToName.getOrDefault(targetId, "that player") + "'s hand."));
    }
}

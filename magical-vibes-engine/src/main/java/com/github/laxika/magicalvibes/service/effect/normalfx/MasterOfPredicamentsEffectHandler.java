package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MasterOfPredicamentsEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MasterOfPredicamentsEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MasterOfPredicamentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID guessingPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (guessingPlayerId == null || hand == null || hand.isEmpty()) {
            return;
        }

        List<Integer> validIndices = IntStream.range(0, hand.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MasterOfPredicamentsCardChoice(
                controllerId, validIndices, "Choose a card in your hand.", guessingPlayerId, entry.getCard()));
        log.info("Game {} - Awaiting {} to choose a card for Master of Predicaments",
                gameData.id, gameData.playerIdToName.get(controllerId));
    }
}

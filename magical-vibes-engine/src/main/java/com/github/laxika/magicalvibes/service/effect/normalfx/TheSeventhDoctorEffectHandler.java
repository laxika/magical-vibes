package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TheSeventhDoctorEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
public class TheSeventhDoctorEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TheSeventhDoctorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID attackedTargetId = entry.getAttackedTargetId();
        UUID defendingPlayerId = defendingPlayerId(gameData, attackedTargetId);
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (defendingPlayerId == null || hand == null || hand.isEmpty()) {
            return;
        }

        int artifactCount = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent))
                .mapToInt(ignored -> 1)
                .sum();
        List<Integer> validIndices = IntStream.range(0, hand.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MasterOfPredicamentsCardChoice(
                controllerId, validIndices, "Choose a card in your hand.", defendingPlayerId, entry.getCard(),
                artifactCount, CreateTokenEffect.ofClueToken(1)));
        log.info("Game {} - Awaiting {} to choose a card for The Seventh Doctor",
                gameData.id, gameData.playerIdToName.get(controllerId));
    }

    private UUID defendingPlayerId(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}

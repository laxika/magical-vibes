package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerExilesCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final ExileService exileService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerExilesCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerExilesCardFromGraveyardEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = targetName + " has no cards in graveyard to exile.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no graveyard cards to exile", gameData.id, targetName);
            return;
        }

        if (graveyard.size() == 1) {
            // Auto-exile the only card
            Card card = graveyard.removeFirst();
            graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId);
            exileService.exileCard(gameData, targetPlayerId, card);

            String logEntry = targetName + " exiles " + card.getName() + " from their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} exiles {} from graveyard", gameData.id, targetName, card.getName());

            if (e.lifeGainIfCreature() > 0 && card.hasType(CardType.CREATURE)) {
                lifeSupport.applyGainLife(gameData, controllerId, e.lifeGainIfCreature());
            }
            return;
        }

        // Multiple cards — target player must choose
        List<Integer> validIndices = IntStream.range(0, graveyard.size()).boxed().toList();
        PendingInteraction.GraveyardChoice.Builder choice = PendingInteraction.GraveyardChoice
                .builder(targetPlayerId, validIndices, GraveyardChoiceDestination.EXILE,
                        "Choose a card to exile from your graveyard.")
                .exileRemainingCount(1);
        if (e.lifeGainIfCreature() > 0) {
            choice.gainLifeIfCreature(e.lifeGainIfCreature(), controllerId);
        }
        interactionHandlerRegistry.begin(gameData, choice.build());
    }
}

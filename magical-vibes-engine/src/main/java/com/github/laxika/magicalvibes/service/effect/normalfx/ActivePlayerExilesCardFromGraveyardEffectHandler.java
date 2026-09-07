package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerExilesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Component
public class ActivePlayerExilesCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final EffectResolutionService effectResolutionService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final ExileService exileService;
    private final GraveyardService graveyardService;

    public ActivePlayerExilesCardFromGraveyardEffectHandler(
            @Lazy EffectResolutionService effectResolutionService,
            GameLogService gameLogService,
            InteractionHandlerRegistry interactionHandlerRegistry,
            ExileService exileService,
            GraveyardService graveyardService) {
        this.effectResolutionService = effectResolutionService;
        this.gameLogService = gameLogService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.exileService = exileService;
        this.graveyardService = graveyardService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ActivePlayerExilesCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ActivePlayerExilesCardFromGraveyardEffect) effect;
        UUID activePlayerId = entry.getActivePlayerId() != null
                ? entry.getActivePlayerId() : entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(activePlayerId);
        String playerName = gameData.playerIdToName.get(activePlayerId);

        if (graveyard == null || graveyard.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards in graveyard to exile."));
            resolveNoCardEffect(gameData, entry, e.noCardEffect());
            return;
        }

        if (graveyard.size() == 1) {
            Card card = graveyard.removeFirst();
            graveyardService.notifyCardsExiledFromGraveyard(gameData, activePlayerId, card);
            exileService.exileCard(gameData, activePlayerId, card);
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " exiles ", card, " from their graveyard."));
            log.info("Game {} - {} exiles {} from graveyard", gameData.id, playerName, card.getName());
            return;
        }

        List<Integer> validIndices = IntStream.range(0, graveyard.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(activePlayerId, validIndices, GraveyardChoiceDestination.EXILE,
                        "Choose a card to exile from your graveyard.")
                .exileRemainingCount(1)
                .build());
    }

    private void resolveNoCardEffect(GameData gameData, StackEntry entry, CardEffect noCardEffect) {
        if (noCardEffect == null) {
            return;
        }
        StackEntry fallbackEntry = new StackEntry(entry.getEntryType(), entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(noCardEffect), entry.getTargetId(), entry.getSourcePermanentId());
        fallbackEntry.setActivePlayerId(entry.getActivePlayerId());
        fallbackEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        fallbackEntry.setAttackedTargetId(entry.getAttackedTargetId());
        effectResolutionService.resolveEffects(gameData, fallbackEntry);
    }
}

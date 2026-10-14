package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ClashPlacementState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaceClashCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PlaceClashCardsEffect}: each clashing player, in APNAP order, decides whether the
 * card they revealed goes to the bottom of their library, then every card moves at the same time
 * (CR 701.30c). The revealed cards stay on top of their libraries while players decide. Each
 * decision is a one-card top/bottom {@link PendingInteraction.Scry} prompt that
 * {@code ScryInteractionHandler} records into {@link ClashPlacementState} instead of applying; the
 * handler re-runs after each answer via {@code rerunCurrentEffectAfterInteraction}.
 */
@Component
@RequiredArgsConstructor
public class PlaceClashCardsEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlaceClashCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PlaceClashCardsEffect) effect;
        ClashPlacementState state = gameData.clashPlacement;

        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(e.clashingPlayerIds());
        }

        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.pollFirst();
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck == null || deck.isEmpty()) {
                continue;
            }
            state.currentPlayerId = playerId;
            gameData.rerunCurrentEffectAfterInteraction = true;
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.Scry(playerId, List.of(deck.getFirst()), false, playerId, false));
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                    + " chooses whether to put the revealed card on the top or bottom of their library."));
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        List<ClashPlacementState.Placement> placements = List.copyOf(state.placements);
        state.reset();
        for (ClashPlacementState.Placement placement : placements) {
            if (!placement.toBottom()) {
                continue;
            }
            List<Card> deck = gameData.playerDecks.get(placement.playerId());
            Card card = deck.stream()
                    .filter(c -> c.getId().equals(placement.cardId()))
                    .findFirst()
                    .orElse(null);
            if (card != null) {
                deck.remove(card);
                deck.add(card);
            }
        }
        for (ClashPlacementState.Placement placement : placements) {
            String playerName = gameData.playerIdToName.get(placement.playerId());
            gameLogService.append(gameData, GameLog.text(playerName + " puts the revealed card on the "
                    + (placement.toBottom() ? "bottom" : "top") + " of their library."));
        }
    }

    /** The clashing players in APNAP order (CR 101.4): the active player decides first. */
    public static List<UUID> apnapOrder(GameData gameData, UUID controllerId, UUID opponentId) {
        if (opponentId == null) {
            return List.of(controllerId);
        }
        return opponentId.equals(gameData.activePlayerId)
                ? List.of(opponentId, controllerId)
                : List.of(controllerId, opponentId);
    }
}

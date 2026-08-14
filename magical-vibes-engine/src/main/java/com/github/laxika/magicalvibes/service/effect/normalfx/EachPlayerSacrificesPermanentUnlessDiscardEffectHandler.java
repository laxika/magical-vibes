package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerSacrificeOrDiscardState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentUnlessDiscardEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an APNAP each-player choice to discard a card or sacrifice a permanent. */
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesPermanentUnlessDiscardEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesPermanentUnlessDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerSacrificeOrDiscardState state = gameData.eachPlayerSacrificeOrDiscard;
        String sourceName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(apnapPlayers(gameData));
            advance(gameData, entry, sourceName);
            return;
        }

        if (state.chosenMode != null) {
            String chosenMode = state.chosenMode;
            state.chosenMode = null;
            applyMode(gameData, entry, sourceName, chosenMode);
            return;
        }

        advance(gameData, entry, sourceName);
    }

    private void advance(GameData gameData, StackEntry entry, String sourceName) {
        EachPlayerSacrificeOrDiscardState state = gameData.eachPlayerSacrificeOrDiscard;
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            if (!gameData.playerIds.contains(playerId)) {
                continue;
            }
            state.currentPlayerId = playerId;

            boolean hasPermanent = !permanentIds(gameData, playerId).isEmpty();
            boolean hasCard = hasCardToDiscard(gameData, playerId);
            if (!hasPermanent && !hasCard) {
                continue;
            }
            if (hasPermanent && hasCard) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null,
                        new ChoiceContext.EachPlayerSacrificeOrDiscardChoice(playerId, sourceName),
                        List.of(
                                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE,
                                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD
                        ),
                        sourceName + " — sacrifice a permanent unless you discard a card."
                ));
                return;
            }

            applyMode(gameData, entry, sourceName,
                    hasPermanent
                            ? ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE
                            : ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();
    }

    private void applyMode(GameData gameData, StackEntry entry, String sourceName, String mode) {
        EachPlayerSacrificeOrDiscardState state = gameData.eachPlayerSacrificeOrDiscard;
        UUID playerId = state.currentPlayerId;

        if (ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE.equals(mode)) {
            List<UUID> ids = permanentIds(gameData, playerId);
            if (ids.isEmpty()) {
                advance(gameData, entry, sourceName);
                return;
            }
            if (ids.size() == 1) {
                Permanent permanent = gameData.playerBattlefields.get(playerId).stream()
                        .filter(candidate -> candidate.getId().equals(ids.getFirst()))
                        .findFirst()
                        .orElse(null);
                if (permanent != null) {
                    destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
                }
                advance(gameData, entry, sourceName);
                return;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TormentSacrifice(playerId));
            playerInputService.beginPermanentChoice(gameData, playerId, ids,
                    sourceName + " — choose a permanent to sacrifice.");
            return;
        }

        if (ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD.equals(mode)) {
            if (!hasCardToDiscard(gameData, playerId)) {
                advance(gameData, entry, sourceName);
                return;
            }
            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (!gameData.interaction.isAwaitingInput()) {
                advance(gameData, entry, sourceName);
            }
            return;
        }

        advance(gameData, entry, sourceName);
    }

    private List<UUID> permanentIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectPermanentIds(gameData, playerId, permanent -> true);
    }

    private boolean hasCardToDiscard(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand != null && !hand.isEmpty();
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>();
        if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)) {
            players.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!players.contains(playerId) && gameData.playerIds.contains(playerId)) {
                players.add(playerId);
            }
        }
        return players;
    }
}

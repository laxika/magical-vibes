package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EumidianWastewakerState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EumidianWastewakerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Eumidian Wastewaker's two-player discard-or-sacrifice attack trigger. */
@Component
@RequiredArgsConstructor
public class EumidianWastewakerEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EumidianWastewakerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EumidianWastewakerState state = gameData.eumidianWastewaker;
        String sourceName = entry.getCard().getName();

        if (!state.active) {
            state.active = true;
            state.remaining.add(entry.getControllerId());
            UUID defendingPlayerId = defendingPlayerId(gameData, entry);
            if (defendingPlayerId != null && !defendingPlayerId.equals(entry.getControllerId())) {
                state.remaining.add(defendingPlayerId);
            }
        } else if (state.pendingDiscard) {
            if (gameData.lastDiscardedCardTypes.contains(CardType.LAND)) {
                state.landCardsPutIntoGraveyard++;
            }
            state.pendingDiscard = false;
            state.currentPlayerId = null;
        } else if (state.pendingSacrificeChoice) {
            boolean landSacrificed = state.pendingSacrificeLandIds.stream()
                    .anyMatch(id -> gameQueryService.findPermanentById(gameData, id) == null);
            if (landSacrificed) {
                state.landCardsPutIntoGraveyard++;
            }
            state.pendingSacrificeChoice = false;
            state.pendingSacrificeLandIds.clear();
            state.currentPlayerId = null;
        }

        if (state.chosenMode != null) {
            String chosenMode = state.chosenMode;
            state.chosenMode = null;
            applyMode(gameData, entry, sourceName, state, chosenMode);
            return;
        }

        advance(gameData, entry, sourceName, state);
    }

    private void advance(GameData gameData, StackEntry entry, String sourceName,
                         EumidianWastewakerState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            if (!gameData.playerIds.contains(playerId)) {
                continue;
            }
            state.currentPlayerId = playerId;

            boolean hasPermanent = !permanentIds(gameData, playerId, entry.getControllerId()).isEmpty();
            boolean hasCard = hasCardToDiscard(gameData, playerId);
            if (!hasPermanent && !hasCard) {
                state.currentPlayerId = null;
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
                        sourceName + " — sacrifice a permanent or discard a card."
                ));
                return;
            }

            applyMode(gameData, entry, sourceName, state,
                    hasPermanent
                            ? ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE
                            : ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
            return;
        }

        finish(gameData, entry, state);
    }

    private void applyMode(GameData gameData, StackEntry entry, String sourceName,
                           EumidianWastewakerState state, String mode) {
        UUID playerId = state.currentPlayerId;
        if (ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE.equals(mode)) {
            List<UUID> ids = permanentIds(gameData, playerId, entry.getControllerId());
            if (ids.isEmpty()) {
                state.currentPlayerId = null;
                advance(gameData, entry, sourceName, state);
                return;
            }
            if (ids.size() == 1) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, ids.getFirst());
                if (permanent != null) {
                    if (permanent.getCard().hasType(CardType.LAND)) {
                        state.landCardsPutIntoGraveyard++;
                    }
                    destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
                }
                state.currentPlayerId = null;
                advance(gameData, entry, sourceName, state);
                return;
            }

            state.pendingSacrificeChoice = true;
            state.pendingSacrificeLandIds.clear();
            for (UUID id : ids) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                if (permanent != null && permanent.getCard().hasType(CardType.LAND)) {
                    state.pendingSacrificeLandIds.add(id);
                }
            }
            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TormentSacrifice(playerId));
            playerInputService.beginPermanentChoice(gameData, playerId, ids,
                    sourceName + " — choose a permanent to sacrifice.");
            return;
        }

        if (ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD.equals(mode)) {
            if (!hasCardToDiscard(gameData, playerId)) {
                state.currentPlayerId = null;
                advance(gameData, entry, sourceName, state);
                return;
            }
            gameData.lastDiscardedCardTypes = java.util.Set.of();
            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            state.pendingDiscard = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (!gameData.interaction.isAwaitingInput()) {
                if (gameData.lastDiscardedCardTypes.contains(CardType.LAND)) {
                    state.landCardsPutIntoGraveyard++;
                }
                state.pendingDiscard = false;
                state.currentPlayerId = null;
                advance(gameData, entry, sourceName, state);
            }
            return;
        }

        state.currentPlayerId = null;
        advance(gameData, entry, sourceName, state);
    }

    private void finish(GameData gameData, StackEntry entry, EumidianWastewakerState state) {
        int drawAmount = state.landCardsPutIntoGraveyard;
        entry.setEventValue(drawAmount);
        state.reset();
        gameData.discardCausedByOpponent = false;
        gameData.rerunCurrentEffectAfterInteraction = false;
        if (drawAmount > 0) {
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), drawAmount);
        }
    }

    private List<UUID> permanentIds(GameData gameData, UUID playerId, UUID sourceControllerId) {
        return gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)
                ? destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                : List.of();
    }

    private boolean hasCardToDiscard(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand != null && !hand.isEmpty();
    }

    private UUID defendingPlayerId(GameData gameData, StackEntry entry) {
        UUID attackedTargetId = entry.getAttackedTargetId() != null
                ? entry.getAttackedTargetId() : entry.getTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TormentState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffect}: for each 1 life lost
 * (snapshotted on the entry's {@code eventValue}), the controller sacrifices a permanent other than
 * the source unless they discard a card. Reuses {@link GameData#torment} iteration progress and the
 * Torment sacrifice/discard sub-choice plumbing (no "lose life" option).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOtherPermanentUnlessDiscardForEachLifeLostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TormentState state = gameData.torment;
        String sourceName = entry.getCard().getName();
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.remainingIterations = Math.max(0, entry.getEventValue());
            advance(gameData, entry, sourceName, controllerId, sourcePermanentId);
            return;
        }

        if (state.chosenMode != null) {
            String mode = state.chosenMode;
            state.chosenMode = null;
            applyMode(gameData, entry, sourceName, controllerId, sourcePermanentId, mode);
            return;
        }

        // Re-entry after a discard / sacrifice sub-choice completed — next life point.
        advance(gameData, entry, sourceName, controllerId, sourcePermanentId);
    }

    private void advance(GameData gameData, StackEntry entry, String sourceName,
            UUID controllerId, UUID sourcePermanentId) {
        TormentState state = gameData.torment;
        while (state.remainingIterations > 0) {
            state.remainingIterations--;

            List<String> options = availableOptions(gameData, controllerId, sourcePermanentId);
            if (options.isEmpty()) {
                // No other permanents and empty hand — this life point is ignored.
                continue;
            }

            if (options.size() == 1) {
                applyMode(gameData, entry, sourceName, controllerId, sourcePermanentId, options.getFirst());
                return;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            String prompt = sourceName
                    + " — sacrifice a permanent other than this enchantment unless you discard a card.";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    controllerId, null, null,
                    new ChoiceContext.OathOfLimDulPenaltyChoice(controllerId, sourceName),
                    options, prompt));
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();
    }

    private void applyMode(GameData gameData, StackEntry entry, String sourceName,
            UUID controllerId, UUID sourcePermanentId, String mode) {
        if (ChoiceContext.OathOfLimDulPenaltyChoice.SACRIFICE.equals(mode)) {
            List<UUID> otherIds = otherPermanentIds(gameData, controllerId, sourcePermanentId);
            if (otherIds.isEmpty()) {
                advance(gameData, entry, sourceName, controllerId, sourcePermanentId);
                return;
            }
            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.TormentSacrifice(controllerId));
            playerInputService.beginPermanentChoice(gameData, controllerId, otherIds,
                    sourceName + " — choose a permanent other than this enchantment to sacrifice.");
            return;
        }

        if (ChoiceContext.OathOfLimDulPenaltyChoice.DISCARD.equals(mode)) {
            List<Card> hand = gameData.playerHands.get(controllerId);
            if (hand == null || hand.isEmpty()) {
                advance(gameData, entry, sourceName, controllerId, sourcePermanentId);
                return;
            }
            gameData.discardCausedByOpponent = false;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 1, DiscardFollowUp.NONE);
            if (!gameData.interaction.isAwaitingInput()) {
                advance(gameData, entry, sourceName, controllerId, sourcePermanentId);
            }
            return;
        }

        advance(gameData, entry, sourceName, controllerId, sourcePermanentId);
    }

    private List<String> availableOptions(GameData gameData, UUID controllerId, UUID sourcePermanentId) {
        List<String> options = new ArrayList<>();
        if (!otherPermanentIds(gameData, controllerId, sourcePermanentId).isEmpty()) {
            options.add(ChoiceContext.OathOfLimDulPenaltyChoice.SACRIFICE);
        }
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand != null && !hand.isEmpty()) {
            options.add(ChoiceContext.OathOfLimDulPenaltyChoice.DISCARD);
        }
        return options;
    }

    private List<UUID> otherPermanentIds(GameData gameData, UUID controllerId, UUID sourcePermanentId) {
        return destructionSupport.collectPermanentIds(gameData, controllerId,
                p -> !Objects.equals(p.getId(), sourcePermanentId));
    }
}

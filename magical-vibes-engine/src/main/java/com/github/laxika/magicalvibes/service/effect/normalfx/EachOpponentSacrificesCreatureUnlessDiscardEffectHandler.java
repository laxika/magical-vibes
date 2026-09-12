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
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the Long Reach of Night's opponent-only creature-or-discard choice in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentSacrificesCreatureUnlessDiscardEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentIsCreaturePredicate CREATURE = new PermanentIsCreaturePredicate();

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentSacrificesCreatureUnlessDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerSacrificeOrDiscardState state = gameData.eachPlayerSacrificeOrDiscard;
        String sourceName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(apnapOpponents(gameData, entry.getControllerId()));
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

            boolean hasCreature = !creatureIds(gameData, playerId, entry.getControllerId()).isEmpty();
            boolean hasCard = hasCardToDiscard(gameData, playerId);
            if (!hasCreature && !hasCard) {
                continue;
            }
            if (hasCreature && hasCard) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null,
                        new ChoiceContext.EachPlayerSacrificeOrDiscardChoice(playerId, sourceName),
                        List.of(
                                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE,
                                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD
                        ),
                        sourceName + " — sacrifice a creature unless you discard a card."
                ));
                return;
            }

            applyMode(gameData, entry, sourceName,
                    hasCreature
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
            List<UUID> ids = creatureIds(gameData, playerId, entry.getControllerId());
            if (ids.isEmpty()) {
                if (hasCardToDiscard(gameData, playerId)) {
                    applyMode(gameData, entry, sourceName,
                            ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
                } else {
                    advance(gameData, entry, sourceName);
                }
                return;
            }
            if (ids.size() == 1) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, ids.getFirst());
                if (permanent != null) {
                    destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
                }
                advance(gameData, entry, sourceName);
                return;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TormentSacrifice(playerId));
            playerInputService.beginPermanentChoice(gameData, playerId, ids,
                    sourceName + " — choose a creature to sacrifice.");
            return;
        }

        if (ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD.equals(mode)) {
            if (!hasCardToDiscard(gameData, playerId)) {
                applyMode(gameData, entry, sourceName,
                        ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE);
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

    private List<UUID> creatureIds(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return List.of();
        }
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent)
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, CREATURE));
    }

    private boolean hasCardToDiscard(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand != null && !hand.isEmpty();
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> opponents = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)
                && gameData.playerIds.contains(activePlayerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)
                    && gameData.playerIds.contains(playerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}

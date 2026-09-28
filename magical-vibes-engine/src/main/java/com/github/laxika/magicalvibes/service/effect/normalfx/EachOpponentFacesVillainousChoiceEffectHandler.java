package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Dr. Eggman's villainous choice in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentFacesVillainousChoiceEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentFacesVillainousChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentFacesVillainousChoiceEffect typed = (EachOpponentFacesVillainousChoiceEffect) effect;
        VillainousChoiceState state = gameData.villainousChoice;

        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(apnapOpponents(gameData, entry.getControllerId()));
        }

        if (state.waitingForDiscard) {
            state.waitingForDiscard = false;
            advance(gameData, entry, typed);
            return;
        }
        if (state.waitingForCardChoice) {
            state.waitingForCardChoice = false;
            advance(gameData, entry, typed);
            return;
        }
        if (state.waitingForControllerMay) {
            if (gameData.resolvedMayAccepted == null) {
                return;
            }
            boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            state.waitingForControllerMay = false;
            if (!accepted) {
                advance(gameData, entry, typed);
                return;
            }

            state.waitingForCardChoice = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            PutCardToBattlefieldEffect putEffect = new PutCardToBattlefieldEffect(
                    typed.predicate(), typed.label());
            playerInteractionSupport.applyPutCardToBattlefield(
                    gameData, entry.getControllerId(), putEffect, 0, null, null,
                    entry.getCard().getId(), null, null, null, ignored -> true,
                    entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot());
            if (!gameData.interaction.isAwaitingInput() && gameData.pendingMayAbilities.isEmpty()) {
                state.waitingForCardChoice = false;
                advance(gameData, entry, typed);
            }
            return;
        }

        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            if (ChoiceContext.VillainousChoice.DISCARD.equals(chosen)) {
                discard(gameData, entry, typed);
            } else {
                queueControllerMayPut(gameData, entry, typed);
            }
            return;
        }

        advance(gameData, entry, typed);
    }

    private void advance(GameData gameData, StackEntry entry,
                         EachOpponentFacesVillainousChoiceEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        while (!state.remaining.isEmpty()) {
            UUID opponentId = state.remaining.removeFirst();
            if (!gameData.playerIds.contains(opponentId)) {
                continue;
            }
            state.currentPlayerId = opponentId;

            boolean canDiscard = hasCardToDiscard(gameData, opponentId);
            boolean canPut = hasMatchingCard(gameData, entry, effect, entry.getControllerId());
            if (!canDiscard && !canPut) {
                continue;
            }
            if (canDiscard && canPut) {
                String putOption = "Let the controller put a " + effect.label()
                        + " card onto the battlefield";
                gameData.rerunCurrentEffectAfterInteraction = true;
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        opponentId, null, null,
                        new ChoiceContext.VillainousChoice(opponentId, entry.getCard().getName(), putOption),
                        List.of(ChoiceContext.VillainousChoice.DISCARD, putOption),
                        entry.getCard().getName() + " — Choose a villainous choice."));
                return;
            }
            if (canDiscard) {
                discard(gameData, entry, effect);
            } else {
                queueControllerMayPut(gameData, entry, effect);
            }
            return;
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private void discard(GameData gameData, StackEntry entry,
                         EachOpponentFacesVillainousChoiceEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        state.waitingForDiscard = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.discardCausedByOpponent = !state.currentPlayerId.equals(entry.getControllerId());
        playerInteractionSupport.resolveDiscardCards(gameData, state.currentPlayerId, 1,
                DiscardFollowUp.NONE);
        if (!gameData.interaction.isAwaitingInput() && gameData.pendingMayAbilities.isEmpty()) {
            state.waitingForDiscard = false;
            advance(gameData, entry, effect);
        }
    }

    private void queueControllerMayPut(GameData gameData, StackEntry entry,
                                       EachOpponentFacesVillainousChoiceEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (!hasMatchingCard(gameData, entry, effect, entry.getControllerId())) {
            advance(gameData, entry, effect);
            return;
        }

        state.waitingForControllerMay = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.resolvingMayEffectFromStack = true;
        gameData.queueMayAbilityForPlayer(
                entry.getCard(), entry.getControllerId(),
                new MayEffect(new PutCardToBattlefieldEffect(effect.predicate(), effect.label()),
                        "Put a " + effect.label() + " card onto the battlefield?"),
                null, entry.getSourcePermanentId(), entry.getControllerId(),
                entry.getSourcePermanentSnapshot());
    }

    private boolean hasCardToDiscard(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand != null && !hand.isEmpty();
    }

    private boolean hasMatchingCard(GameData gameData, StackEntry entry,
                                    EachOpponentFacesVillainousChoiceEffect effect, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return false;
        }
        return hand.stream().anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                card, effect.predicate(), entry.getCard().getId(), gameData, playerId,
                entry.getSourcePermanentId(), entry.getTriggeringPermanentPowerAtTrigger(),
                entry.getXValue(), entry.getSourcePermanentSnapshot()));
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

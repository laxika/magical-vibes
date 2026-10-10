package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentWhoLostLifeFacesVillainousChoiceEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Davros's qualifying-opponent villainous choices in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentWhoLostLifeFacesVillainousChoiceEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentWhoLostLifeFacesVillainousChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typed = (EachOpponentWhoLostLifeFacesVillainousChoiceEffect) effect;
        VillainousChoiceState state = gameData.villainousChoice;

        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(EachPlayerMayScryEffectHandler
                    .apnapOpponents(gameData, entry.getControllerId()).stream()
                    .filter(id -> gameData.lifeLostThisTurn.getOrDefault(id, 0)
                            >= Math.max(1, typed.minimumLifeLost()))
                    .toList());
        }

        if (state.waitingForDiscard) {
            state.waitingForDiscard = false;
            advance(gameData, entry);
            return;
        }
        if (state.waitingForDraw) {
            state.waitingForDraw = false;
            advance(gameData, entry);
            return;
        }
        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            if (ChoiceContext.VillainousChoice.DRAW.equals(chosen)) {
                draw(gameData, entry);
            } else {
                discard(gameData, entry);
            }
            return;
        }

        advance(gameData, entry);
    }

    private void advance(GameData gameData, StackEntry entry) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (villainousChoiceSupport.repeatIfNeeded(gameData)) {
            return;
        }
        while (!state.remaining.isEmpty()) {
            UUID opponentId = state.remaining.removeFirst();
            if (!gameData.playerIds.contains(opponentId)) {
                continue;
            }
            state.currentPlayerId = opponentId;

            // A villainous choice is offered even when an option is impossible (an empty hand can
            // still choose to discard, which does nothing).
            gameData.rerunCurrentEffectAfterInteraction = true;
            villainousChoiceSupport.beginChoice(gameData, opponentId, entry.getCard().getName(),
                    ChoiceContext.VillainousChoice.DRAW,
                    List.of(ChoiceContext.VillainousChoice.DRAW,
                            ChoiceContext.VillainousChoice.DISCARD),
                    entry.getCard().getName() + " — Choose a villainous choice.");
            return;
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private void draw(GameData gameData, StackEntry entry) {
        VillainousChoiceState state = gameData.villainousChoice;
        state.waitingForDraw = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
        if (!gameData.interaction.isAwaitingInput() && gameData.pendingMayAbilities.isEmpty()) {
            state.waitingForDraw = false;
            advance(gameData, entry);
        }
    }

    private void discard(GameData gameData, StackEntry entry) {
        VillainousChoiceState state = gameData.villainousChoice;
        state.waitingForDiscard = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveDiscardCards(gameData, state.currentPlayerId, 1,
                DiscardFollowUp.NONE);
        if (!gameData.interaction.isAwaitingInput() && gameData.pendingMayAbilities.isEmpty()) {
            state.waitingForDiscard = false;
            advance(gameData, entry);
        }
    }

}

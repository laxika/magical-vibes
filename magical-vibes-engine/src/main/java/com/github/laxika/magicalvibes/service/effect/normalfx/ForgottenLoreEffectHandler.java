package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ForgottenLoreState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForgottenLoreEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ForgottenLoreEffect}: the targeted opponent chooses a card in the controller's
 * graveyard, then the controller may pay {G} to repeat the process with that card (and every card
 * chosen before it) off the table. When the controller declines, can't pay, or no unchosen card is
 * left, the last chosen card goes to the controller's hand.
 *
 * <p>Both halves pause resolution: the graveyard pick completes in
 * {@code GraveyardChoiceHandlerService.handleGraveyardCardChosen} (via
 * {@code graveyardTargetOperation.resolutionTimeForgottenLoreResume}, which only records the card),
 * the payment pick in {@code ChoiceHandlerService.handleForgottenLorePaymentChoice}. Both resume
 * this same effect through {@link GameData#rerunCurrentEffectAfterInteraction}.
 */
@Component
@RequiredArgsConstructor
public class ForgottenLoreEffectHandler implements NormalEffectHandlerBean {

    private static final String COST = "{G}";

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForgottenLoreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ForgottenLoreState state = gameData.forgottenLore;
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        String sourceName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            promptChoice(gameData, entry, controllerId, opponentId, sourceName);
            return;
        }

        if (state.pendingChosenCardId != null) {
            state.lastChosenCardId = state.pendingChosenCardId;
            state.chosenCardIds.add(state.pendingChosenCardId);
            state.pendingChosenCardId = null;
            promptPayment(gameData, controllerId, sourceName);
            return;
        }

        if (state.chosenMode != null) {
            String mode = state.chosenMode;
            state.chosenMode = null;
            if (ChoiceContext.ForgottenLorePaymentChoice.PAY.equals(mode) && pay(gameData, controllerId)) {
                promptChoice(gameData, entry, controllerId, opponentId, sourceName);
                return;
            }
            finish(gameData, controllerId, sourceName);
            return;
        }

        finish(gameData, controllerId, sourceName);
    }

    /**
     * Asks the opponent to choose one of the controller's not-yet-chosen graveyard cards. With none
     * left there is nothing to choose, so the loop ends. The spell itself is excluded — CR 608.2n
     * puts it into the graveyard only as the last step of its own resolution, so it was never there
     * to be chosen.
     */
    private void promptChoice(GameData gameData, StackEntry entry, UUID controllerId, UUID opponentId,
            String sourceName) {
        ForgottenLoreState state = gameData.forgottenLore;
        UUID sourceCardId = entry.getCard().getId();
        List<Card> eligible = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (!card.getId().equals(sourceCardId) && !state.chosenCardIds.contains(card.getId())) {
                    eligible.add(card);
                }
            }
        }

        if (eligible.isEmpty() || opponentId == null) {
            finish(gameData, controllerId, sourceName);
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeForgottenLoreResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        List<Integer> indices = IntStream.range(0, eligible.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(opponentId, indices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        sourceName + " — choose a card in your opponent's graveyard.")
                .cardPool(eligible)
                .mandatory(true)
                .build());
    }

    /** Offers the controller the optional {G} that repeats the process; unaffordable means no prompt. */
    private void promptPayment(GameData gameData, UUID controllerId, String sourceName) {
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        if (pool == null || !new ManaCost(COST).canPay(pool)) {
            finish(gameData, controllerId, sourceName);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null,
                new ChoiceContext.ForgottenLorePaymentChoice(controllerId, sourceName),
                List.of(ChoiceContext.ForgottenLorePaymentChoice.PAY,
                        ChoiceContext.ForgottenLorePaymentChoice.DECLINE),
                sourceName + " — pay {G} to have your opponent choose another card?"));
    }

    /** Puts the last chosen card into the controller's hand and clears the loop state. */
    private void finish(GameData gameData, UUID controllerId, String sourceName) {
        ForgottenLoreState state = gameData.forgottenLore;
        UUID lastChosenCardId = state.lastChosenCardId;

        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();

        if (lastChosenCardId == null) {
            return;
        }
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        Card card = graveyard == null ? null : graveyard.stream()
                .filter(candidate -> candidate.getId().equals(lastChosenCardId))
                .findFirst()
                .orElse(null);
        if (card == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, lastChosenCardId);
        gameData.addCardToHand(controllerId, card);
        gameLogService.append(gameData, GameLog.textCardText(
                sourceName + " returns ", card, " from the graveyard to its owner's hand."));
    }

    private boolean pay(GameData gameData, UUID playerId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        ManaCost cost = new ManaCost(COST);
        if (pool == null || !cost.canPay(pool)) {
            return false;
        }
        cost.pay(pool);
        return true;
    }
}

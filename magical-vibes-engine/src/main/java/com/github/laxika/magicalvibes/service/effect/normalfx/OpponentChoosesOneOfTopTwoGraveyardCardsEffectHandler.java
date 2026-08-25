package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesOneOfTopTwoGraveyardCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link OpponentChoosesOneOfTopTwoGraveyardCardsEffect}: the targeted opponent picks one
 * of the top two cards of the controller's graveyard, that card is exiled and the other one goes to
 * the controller's hand.
 *
 * <p>The pick pauses resolution as a mandatory {@code GraveyardChoice} answered by the opponent; it
 * only records the card (via {@code graveyardTargetOperation.resolutionTimePhyrexianGrimoireResume})
 * and reruns this effect through {@link GameData#rerunCurrentEffectAfterInteraction}. With a single
 * card in the graveyard there is nothing to choose: it is exiled and no card goes to hand.
 */
@Component
@RequiredArgsConstructor
public class OpponentChoosesOneOfTopTwoGraveyardCardsEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentChoosesOneOfTopTwoGraveyardCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        String sourceName = entry.getCard().getName();
        List<Card> topTwo = topTwo(gameData, controllerId);

        UUID chosenCardId = state.phyrexianGrimoireChosenCardId;
        state.phyrexianGrimoireChosenCardId = null;

        if (chosenCardId != null) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            Card chosen = topTwo.stream()
                    .filter(card -> card.getId().equals(chosenCardId))
                    .findFirst()
                    .orElse(null);
            if (chosen != null) {
                apply(gameData, controllerId, topTwo, chosen, sourceName);
            }
            return;
        }

        if (topTwo.isEmpty()) {
            return;
        }

        if (topTwo.size() == 1 || opponentId == null) {
            apply(gameData, controllerId, topTwo, topTwo.getFirst(), sourceName);
            return;
        }

        state.resolutionTimePhyrexianGrimoireResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        List<Integer> indices = IntStream.range(0, topTwo.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(opponentId, indices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        sourceName + " — choose a card to exile. The other one goes to its owner's hand.")
                .cardPool(new ArrayList<>(topTwo))
                .mandatory(true)
                .build());
    }

    /** The top two cards of the player's graveyard, topmost first (cards are appended on entry). */
    private List<Card> topTwo(GameData gameData, UUID playerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return List.of();
        }
        List<Card> top = new ArrayList<>();
        top.add(graveyard.get(graveyard.size() - 1));
        if (graveyard.size() >= 2) {
            top.add(graveyard.get(graveyard.size() - 2));
        }
        return top;
    }

    /** Exiles the chosen card and puts the other one — if there is one — into the owner's hand. */
    private void apply(GameData gameData, UUID controllerId, List<Card> topTwo, Card chosen, String sourceName) {
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, chosen.getId());
        exileService.exileCard(gameData, controllerId, chosen);
        gameLogService.append(gameData, GameLog.textCardText(
                sourceName + " exiles ", chosen, " from the graveyard."));

        Card other = topTwo.stream()
                .filter(card -> !card.getId().equals(chosen.getId()))
                .findFirst()
                .orElse(null);
        if (other == null) {
            return;
        }
        permanentRemovalService.removeCardFromGraveyardById(gameData, other.getId());
        permanentRemovalService.addCardToHandFromGraveyard(gameData, controllerId, controllerId, other);
        gameLogService.append(gameData, GameLog.textCardText(
                sourceName + " puts ", other, " from the graveyard into its owner's hand."));
    }
}

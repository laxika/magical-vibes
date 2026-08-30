package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EachOpponentDiscardsTwoUnlessNonlandState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsTwoUnlessNonlandEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsTwoUnlessNonlandHandler implements MayEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDiscardsTwoUnlessNonlandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        EachOpponentDiscardsTwoUnlessNonlandState state = gameData.eachOpponentDiscardsTwoUnlessNonland;
        UUID discardingPlayerId = ability.controllerId();
        state.awaitingMayChoice = false;

        if (accepted) {
            List<Card> hand = gameData.playerHands.get(discardingPlayerId);
            List<Integer> nonlandIndices = new ArrayList<>();
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    if (!hand.get(i).hasType(CardType.LAND)) {
                        nonlandIndices.add(i);
                    }
                }
            }
            if (!nonlandIndices.isEmpty()) {
                beginDiscard(gameData, ability, state, discardingPlayerId, nonlandIndices);
                return;
            }
        }

        List<Card> hand = gameData.playerHands.get(discardingPlayerId);
        if (hand == null || hand.isEmpty()) {
            state.currentOpponentId = null;
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.discardCausedByOpponent = !discardingPlayerId.equals(sourceControllerId(ability));
        state.awaitingDiscard = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInteractionSupport.resolveDiscardCards(gameData, discardingPlayerId, 2);
    }

    private void beginDiscard(GameData gameData, PendingMayAbility ability,
            EachOpponentDiscardsTwoUnlessNonlandState state, UUID playerId, List<Integer> indices) {
        gameData.discardCausedByOpponent = !playerId.equals(sourceControllerId(ability));
        state.awaitingDiscard = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginDiscardChoice(gameData, playerId, indices,
                "Choose a nonland card to discard.", 1);
    }

    private UUID sourceControllerId(PendingMayAbility ability) {
        return ability.sourceControllerId() != null ? ability.sourceControllerId() : ability.controllerId();
    }
}

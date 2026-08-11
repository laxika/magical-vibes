package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCreatureEffect;
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
public class DiscardTwoUnlessCreatureHandler implements MayEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardTwoUnlessCreatureEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ability.effects().stream()
                .filter(DiscardTwoUnlessCreatureEffect.class::isInstance)
                .findFirst()
                .orElseThrow();

        UUID controllerId = ability.controllerId();
        if (accepted) {
            List<Card> hand = gameData.playerHands.get(controllerId);
            List<Integer> creatureIndices = new ArrayList<>();
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    if (hand.get(i).hasType(CardType.CREATURE)) {
                        creatureIndices.add(i);
                    }
                }
            }

            if (!creatureIndices.isEmpty()) {
                gameData.discardCausedByOpponent = false;
                playerInputService.beginDiscardChoice(
                        gameData, controllerId, creatureIndices,
                        "Choose a creature card to discard.", 1);
                return;
            }
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 2);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutChosenCardFromHandIntoGraveyardEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PutChosenCardFromHandIntoGraveyardEffectHandler implements NormalEffectHandlerBean {
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutChosenCardFromHandIntoGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutChosenCardFromHandIntoGraveyardEffect putEffect =
                (PutChosenCardFromHandIntoGraveyardEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (putEffect.validCardIds().contains(hand.get(i).getId())) {
                validIndices.add(i);
            }
        }
        if (!validIndices.isEmpty()) {
            playerInputService.beginPutCardFromHandIntoGraveyardChoice(
                    gameData, entry.getControllerId(), validIndices,
                    "Choose one of the other cards to put into your graveyard.");
        }
    }
}

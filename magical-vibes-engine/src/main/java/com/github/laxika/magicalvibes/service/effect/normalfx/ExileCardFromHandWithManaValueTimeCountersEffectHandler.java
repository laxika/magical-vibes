package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandWithManaValueTimeCountersEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves a hand-card exile effect that permits lands as well as nonlands. */
@Component
@RequiredArgsConstructor
public class ExileCardFromHandWithManaValueTimeCountersEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardFromHandWithManaValueTimeCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }
        playerInputService.beginExileCardFromHandWithTimeCountersChoice(
                gameData, entry.getControllerId(), validIndices,
                "Choose a card from your hand to exile.");
    }
}

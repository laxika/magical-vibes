package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNonlandCardFromHandWithManaValueTimeCountersEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileNonlandCardFromHandWithManaValueTimeCountersEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileNonlandCardFromHandWithManaValueTimeCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileNonlandCardFromHandWithManaValueTimeCountersEffect exileEffect =
                (ExileNonlandCardFromHandWithManaValueTimeCountersEffect) effect;
        UUID playerId = exileEffect.targetPlayer() ? entry.getTargetId() : entry.getControllerId();
        if (playerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (!hand.get(i).hasType(CardType.LAND)) {
                validIndices.add(i);
            }
        }
        if (!validIndices.isEmpty()) {
            playerInputService.beginExileNonlandCardFromHandWithTimeCountersChoice(
                    gameData, playerId, validIndices, "Choose a nonland card from your hand to exile.");
        }
    }
}

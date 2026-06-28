package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileFromHandToImprintEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileFromHandToImprintEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileFromHandToImprintEffect) effect;
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (sourcePermanent == null) {
            log.info("Game {} - Source permanent no longer on battlefield, imprint from hand fizzles", gameData.id);
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            log.info("Game {} - Controller has no cards in hand, imprint from hand skipped", gameData.id);
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (gameQueryService.matchesCardPredicate(hand.get(i), e.filter(), null)) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            log.info("Game {} - {} has no matching cards in hand, imprint from hand skipped", gameData.id, playerName);
            return;
        }

        playerInputService.beginImprintFromHandChoice(gameData, controllerId, validIndices,
                "Choose " + e.description() + " from your hand to exile and imprint.", sourcePermanent.getId());
    }
}

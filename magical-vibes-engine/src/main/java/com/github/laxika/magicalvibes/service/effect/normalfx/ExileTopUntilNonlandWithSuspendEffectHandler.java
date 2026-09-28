package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandWithSuspendEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves The Tenth Doctor's attack trigger. */
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandWithSuspendEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandWithSuspendEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopUntilNonlandWithSuspendEffect suspendEffect =
                (ExileTopUntilNonlandWithSuspendEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        Card nonland = null;
        while (!deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            if (!topCard.hasType(CardType.LAND)) {
                nonland = topCard;
                break;
            }
        }

        if (nonland == null) {
            return;
        }

        gameData.exiledCardTimeCounters.put(nonland.getId(), suspendEffect.timeCounters());
        gameLogService.append(gameData, GameLog.cardThen(nonland,
                " is exiled with " + suspendEffect.timeCounters()
                        + " time counters and gains suspend."));
    }
}

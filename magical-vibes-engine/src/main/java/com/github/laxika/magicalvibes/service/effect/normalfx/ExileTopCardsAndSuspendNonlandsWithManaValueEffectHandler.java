package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndSuspendNonlandsWithManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTopCardsAndSuspendNonlandsWithManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsAndSuspendNonlandsWithManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTopCardsAndSuspendNonlandsWithManaValueEffect) effect;
        UUID controllerId = entry.getControllerId();
        var library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        for (int i = 0; i < exileEffect.count() && !library.isEmpty(); i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            if (card.hasType(CardType.LAND) || gameData.findExiledCard(card.getId()) == null) {
                continue;
            }

            gameData.exiledCardTimeCounters.put(card.getId(), card.getManaValue());
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is exiled with " + card.getManaValue()
                            + " time counters and gains suspend."));
        }
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfEachOpponentEffect;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTopCardsOfEachOpponentEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfEachOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsOfEachOpponentEffect exileEffect = (ExileTopCardsOfEachOpponentEffect) effect;
        if (exileEffect.count() <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId)) {
                continue;
            }
            var library = gameData.playerDecks.get(opponentId);
            if (library == null) {
                continue;
            }

            int remaining = exileEffect.count();
            while (remaining > 0 && !library.isEmpty()) {
                Card card = library.removeFirst();
                exileService.exileCard(gameData, opponentId, card);
                remaining--;
            }
        }
    }
}

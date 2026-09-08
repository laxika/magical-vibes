package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsWithLukkaPermissionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTopCardsWithLukkaPermissionEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsWithLukkaPermissionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsWithLukkaPermissionEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null) {
            return;
        }

        int count = Math.min(Math.max(0, e.count()), deck.size());
        for (int i = 0; i < count; i++) {
            Card card = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, card, entry.getSourcePermanentId());
            if (card.hasType(CardType.CREATURE)) {
                gameData.lukkaExileCastPermissions.put(card.getId(), controllerId);
            }
        }

        if (count > 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles the top " + count + " card"
                            + (count == 1 ? "" : "s") + " of their library (")
                    .card(entry.getCard()).text(").").build());
        }
    }
}

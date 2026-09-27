package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastOnePerCardTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.AminatousAuguryChoiceInteractionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Aminatou's Augury's top-library exile and staged choices. */
@Component
@RequiredArgsConstructor
public class ExileTopCardsAndMayCastOnePerCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final AminatousAuguryChoiceInteractionHandler choiceInteractionHandler;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsAndMayCastOnePerCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = Math.max(0, ((ExileTopCardsAndMayCastOnePerCardTypeEffect) effect).count());
        if (count == 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<UUID> exiledCardIds = new ArrayList<>();
        String playerName = gameData.playerIdToName.get(controllerId);
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            gameData.addToExile(controllerId, card);
            exiledCardIds.add(card.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles ")
                    .card(card)
                    .text(" from the top of their library.")
                    .build());
        }

        choiceInteractionHandler.begin(gameData, controllerId, exiledCardIds);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KayaSpiritsJusticeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Begins Kaya, Spirits' Justice's resolution-time creature-card choice. */
@Component
@RequiredArgsConstructor
public class KayaSpiritsJusticeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KayaSpiritsJusticeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> creatureCards = new ArrayList<>();
        for (java.util.UUID cardId : entry.getTriggeringCardIds()) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled != null && !exiled.faceDown()
                    && !exiled.card().isToken()
                    && exiled.card().hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)) {
                creatureCards.add(exiled.card());
            }
        }

        if (creatureCards.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no creature cards to copy."));
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeKayaSpiritsJusticeResume = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(), creatureCards, 1,
                "Choose a creature card from among the exiled cards, or choose none.");
    }
}

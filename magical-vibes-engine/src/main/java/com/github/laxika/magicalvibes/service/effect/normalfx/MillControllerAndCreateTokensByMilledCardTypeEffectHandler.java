package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndCreateTokensByMilledCardTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillControllerAndCreateTokensByMilledCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndCreateTokensByMilledCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillControllerAndCreateTokensByMilledCardTypeEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + entry.getCard().getName() + ")."));
            return;
        }

        Card milledCard = deck.getFirst();
        List<Card> milledCards = graveyardService.resolveMillPlayer(gameData, controllerId, 1);
        if (!milledCards.contains(milledCard)) {
            return;
        }

        List<CardEffect> tokenEffects = new ArrayList<>(2);
        if (milledCard.hasType(CardType.LAND)) {
            tokenEffects.add(e.landToken());
        }
        if (milledCard.hasType(CardType.CREATURE)) {
            tokenEffects.add(e.creatureToken());
        }
        if (!milledCard.hasType(CardType.LAND) && !milledCard.hasType(CardType.CREATURE)) {
            tokenEffects.add(e.nonCreatureNonLandToken());
        }

        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("MillControllerAndCreateTokensByMilledCardTypeEffect is not in its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, tokenEffects);
        log.info("Game {} - {} milled {} and queued {} token effect(s)",
                gameData.id, playerName, milledCard.getName(), tokenEffects.size());
    }
}

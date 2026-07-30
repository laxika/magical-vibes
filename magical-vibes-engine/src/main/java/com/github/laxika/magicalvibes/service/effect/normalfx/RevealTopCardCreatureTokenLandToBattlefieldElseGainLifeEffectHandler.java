package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureTokenLandToBattlefieldElseGainLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardCreatureTokenLandToBattlefieldElseGainLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardCreatureTokenLandToBattlefieldElseGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealTopCardCreatureTokenLandToBattlefieldElseGainLifeEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(topCard)
                .text(" from the top of their library (" + sourceName + ").")
                .build());

        if (topCard.hasType(CardType.CREATURE)) {
            createTokenEffectHandler.resolve(gameData, entry, e.creatureToken());
        } else if (topCard.hasType(CardType.LAND)) {
            deck.removeFirst();
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, new Permanent(topCard));
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(topCard, playerName));

            log.info("Game {} - {} puts {} onto the battlefield ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
        } else {
            lifeSupport.applyGainLife(gameData, controllerId, e.lifeGain(), sourceName,
                    entry.getCard(), entry.getEntryType());
        }
    }
}

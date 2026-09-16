package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardLandToBattlefieldTappedElseDrawEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the land-or-draw top-card branch used by Thrasios, Triton Hero. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardLandToBattlefieldTappedElseDrawEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardLandToBattlefieldTappedElseDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
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

        if (topCard.hasType(CardType.LAND)) {
            deck.removeFirst();
            Permanent permanent = new Permanent(topCard, Zone.LIBRARY);
            permanent.tap();
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
            battlefieldEntryService.processLandETBEffects(gameData, controllerId, topCard);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(topCard, playerName));
            log.info("Game {} - {} puts {} onto the battlefield tapped ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
        } else {
            playerInteractionSupport.applyDrawCards(gameData, controllerId, 1);
            log.info("Game {} - {} draws the revealed {} ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
        }
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardLandToTappedBattlefieldElseDrawEffect;
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
public class RevealTopCardLandToTappedBattlefieldElseDrawEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardLandToTappedBattlefieldElseDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = library.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ").card(topCard)
                .text(" from the top of their library (" + sourceName + ").")
                .build());

        if (topCard.hasType(CardType.LAND)) {
            library.removeFirst();
            Permanent permanent = new Permanent(topCard);
            permanent.tap();
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
            battlefieldEntryService.processLandETBEffects(gameData, controllerId, topCard);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(topCard, playerName));
            log.info("Game {} - {} puts {} onto the battlefield tapped ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
            return;
        }

        playerInteractionSupport.applyDrawCards(gameData, controllerId, 1);
        log.info("Game {} - {} draws the revealed {} ({})",
                gameData.id, playerName, topCard.getName(), sourceName);
    }
}

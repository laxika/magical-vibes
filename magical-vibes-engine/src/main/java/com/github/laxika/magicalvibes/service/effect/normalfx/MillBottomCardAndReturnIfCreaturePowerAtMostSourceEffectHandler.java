package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomCardAndReturnIfCreaturePowerAtMostSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillBottomCardAndReturnIfCreaturePowerAtMostSourceEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillBottomCardAndReturnIfCreaturePowerAtMostSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — " + sourceName + "'s ability does nothing."));
            return;
        }

        Card bottomCard = deck.removeLast();
        graveyardService.addCardToGraveyard(gameData, controllerId, bottomCard, Zone.LIBRARY);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", bottomCard,
                " from the bottom of their library into their graveyard."));

        if (!bottomCard.hasType(CardType.CREATURE) || bottomCard.getPower() == null
                || gameQueryService.findCardInGraveyardById(gameData, bottomCard.getId()) == null) {
            return;
        }

        Integer sourcePower = sourcePowerAtResolution(gameData, entry);
        if (sourcePower != null && bottomCard.getPower() <= sourcePower) {
            graveyardReturnSupport.reanimateTargetedCard(gameData, controllerId, bottomCard);
            log.info("Game {} - {} returns {} from the graveyard to the battlefield",
                    gameData.id, sourceName, bottomCard.getName());
        }
    }

    private Integer sourcePowerAtResolution(GameData gameData, StackEntry entry) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            if (source != null) {
                return gameQueryService.getEffectivePower(gameData, source);
            }
        }
        Permanent sourceSnapshot = entry.getSourcePermanentSnapshot();
        return sourceSnapshot == null ? null : sourceSnapshot.getEffectivePower();
    }
}

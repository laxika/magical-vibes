package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSacrificedCardUnderControlEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Resolves a sacrifice trigger by returning its triggering card under the ability controller's
 * control, if the card is still in its owner's graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSacrificedCardUnderControlEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSacrificedCardUnderControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sacrificedCardId = entry.getTriggeringCardId();
        if (sacrificedCardId == null) return;

        Card card = gameQueryService.findCardInGraveyardById(gameData, sacrificedCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sacrificedCardId);
        if (card == null || ownerId == null) return;

        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " can't return from the graveyard; it stays in the graveyard."));
            return;
        }

        UUID controllerId = entry.getControllerId();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        permanentRemovalService.removeCardFromGraveyardById(gameData, sacrificedCardId);

        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        if (!controllerId.equals(ownerId)) {
            graveyardReturnSupport.trackStolenCreature(gameData, permanent.getId(), controllerId, ownerId);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.cardThen(card,
                " returns to the battlefield under " + playerName + "'s control."));
        log.info("Game {} - {} returns under {}'s control ({})",
                gameData.id, card.getName(), playerName, entry.getCard().getName());
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
    }
}

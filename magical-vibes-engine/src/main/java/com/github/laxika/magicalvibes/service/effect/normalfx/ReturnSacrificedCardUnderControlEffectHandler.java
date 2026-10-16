package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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
        boolean fromExile = false;
        if (card == null || ownerId == null) {
            // A replacement effect (Leyline of the Void) may have exiled the sacrificed card directly;
            // it is still the same card only if it has not left and re-entered exile since the trigger.
            ExiledCardEntry exiled = gameData.findExiledCard(sacrificedCardId);
            if (exiled == null || exiled.faceDown()
                    || gameData.exileEntryVersions.getOrDefault(sacrificedCardId, 0L)
                    != entry.getTriggeringCardExileEntryVersion()) {
                return;
            }
            card = exiled.card();
            ownerId = exiled.ownerId();
            fromExile = true;
        }

        Zone sourceZone = fromExile ? Zone.EXILE : Zone.GRAVEYARD;
        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, sourceZone)) {
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " can't return from the " + (fromExile ? "exile zone; it stays exiled."
                            : "graveyard; it stays in the graveyard.")));
            return;
        }

        UUID controllerId = entry.getControllerId();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        if (fromExile) {
            gameData.removeFromExile(sacrificedCardId);
        } else {
            permanentRemovalService.removeCardFromGraveyardById(gameData, sacrificedCardId);
        }

        Permanent permanent = new Permanent(card);
        if (fromExile) {
            permanent.setEnteredFromExile(true);
        } else {
            permanent.setEnteredFromGraveyardOwnerId(ownerId);
        }
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

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingDamagedCreatureUnderControlEffect;
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
 * Resolves Dread Slaver's {@code ON_DAMAGED_CREATURE_DIES} trigger: the creature that died returns
 * from its owner's graveyard straight to the battlefield under the ability controller's control,
 * with the effect's colour/subtype grants applied on top of its own.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnDyingDamagedCreatureUnderControlEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingDamagedCreatureUnderControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnDyingDamagedCreatureUnderControlEffect returnEffect =
                (ReturnDyingDamagedCreatureUnderControlEffect) effect;
        UUID dyingCardId = entry.getTriggeringCardId();
        if (dyingCardId == null) {
            return;
        }

        // Loses track if the card left the graveyard in response (or was a token).
        Card cardToReturn = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (cardToReturn == null) {
            return;
        }
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (ownerId == null) {
            return;
        }
        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, Zone.GRAVEYARD)) {
            gameLogService.append(gameData, GameLog.cardThen(cardToReturn,
                    " can't return from the graveyard; it stays in the graveyard."));
            return;
        }

        UUID controllerId = entry.getControllerId();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);

        Permanent permanent = new Permanent(cardToReturn);
        graveyardReturnSupport.applyPermanentGrants(permanent, returnEffect.grantColor(), returnEffect.grantSubtype());
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        if (!controllerId.equals(ownerId)) {
            graveyardReturnSupport.trackStolenCreature(gameData, permanent.getId(), controllerId, ownerId);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.cardThen(cardToReturn,
                " returns to the battlefield under " + playerName + "'s control."));
        log.info("Game {} - {} returns under {}'s control ({})",
                gameData.id, cardToReturn.getName(), playerName, entry.getCard().getName());
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, cardToReturn, null, false);
    }
}

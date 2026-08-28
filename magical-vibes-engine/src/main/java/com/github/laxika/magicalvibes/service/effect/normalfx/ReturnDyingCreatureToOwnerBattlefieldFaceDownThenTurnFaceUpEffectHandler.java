package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ObjectProvider<GameService> gameServiceProvider;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect) effect;
        UUID dyingCardId = returnEffect.dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        Card cardToReturn = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (cardToReturn == null || ownerId == null
                || !cardToReturn.getType().isPermanentType()
                || gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, Zone.GRAVEYARD)) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);

        Permanent permanent = new Permanent(cardToReturn);
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent, enterTappedTypes);

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(ownerId) + " returns ", cardToReturn,
                " to the battlefield face down under its owner's control."));

        if (cardToReturn.hasType(CardType.CREATURE)) {
            gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCost(gameData, permanent);
        }
    }
}

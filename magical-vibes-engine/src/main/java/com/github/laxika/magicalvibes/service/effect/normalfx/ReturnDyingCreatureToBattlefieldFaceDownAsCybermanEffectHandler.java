package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect) effect;
        UUID dyingCardId = returnEffect.dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        Card cardToReturn = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (cardToReturn == null || ownerId == null
                || !cardToReturn.hasType(CardType.CREATURE)
                || gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, Zone.GRAVEYARD)) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);

        Permanent permanent = new Permanent(cardToReturn);
        permanent.setFaceDown(2, 2, Set.of(CardType.ARTIFACT, CardType.CREATURE),
                Set.of(CardSubtype.CYBERMAN));
        permanent.tap();
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, entry.getControllerId(), permanent, enterTappedTypes);

        if (!entry.getControllerId().equals(ownerId)) {
            graveyardReturnSupport.trackStolenCreature(
                    gameData, permanent.getId(), entry.getControllerId(), ownerId);
        }
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " returns ", cardToReturn,
                " to the battlefield face down as a Cyberman under their control."));
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldEffect;
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
public class ReturnDyingCreatureToOwnerBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingCreatureToOwnerBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnDyingCreatureToOwnerBattlefieldEffect) effect;
        UUID dyingCardId = returnEffect.dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        Card cardToReturn = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (cardToReturn == null || ownerId == null) {
            return;
        }
        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, Zone.GRAVEYARD)) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);

        UUID battlefieldControllerId = returnEffect.returnUnderController() ? entry.getControllerId() : ownerId;

        Permanent permanent = new Permanent(cardToReturn);
        graveyardReturnSupport.applyPermanentGrants(permanent, null, returnEffect.grantSubtype());
        permanent.getPersistentGrantedKeywords().addAll(returnEffect.grantKeywords());
        if (returnEffect.enterWithCounter() != null && returnEffect.enterWithCounterCount() > 0) {
            permanent.setCounterCount(returnEffect.enterWithCounter(),
                    permanent.getCounterCount(returnEffect.enterWithCounter())
                            + returnEffect.enterWithCounterCount());
        }
        if (returnEffect.enterTapped()) {
            permanent.tap();
        }
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, battlefieldControllerId, permanent, enterTappedTypes);

        if (!ownerId.equals(battlefieldControllerId)) {
            graveyardReturnSupport.trackStolenCreature(
                    gameData, permanent.getId(), battlefieldControllerId, ownerId);
        }

        String controlText = returnEffect.returnUnderController()
                ? " under " + gameData.playerIdToName.get(battlefieldControllerId) + "'s control."
                : " to the battlefield under its owner's control.";
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(battlefieldControllerId) + " returns ", cardToReturn, controlText));
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                gameData, battlefieldControllerId, permanent, cardToReturn);
    }
}

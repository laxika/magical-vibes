package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAsNamedLandEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
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
public class ReturnDyingCreatureToBattlefieldAsNamedLandEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingCreatureToBattlefieldAsNamedLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnDyingCreatureToBattlefieldAsNamedLandEffect) effect;
        UUID dyingCardId = returnEffect.dyingCardId();
        UUID controllerId = entry.getControllerId();
        if (dyingCardId == null || controllerId == null) {
            return;
        }

        Card cardToReturn = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (cardToReturn == null || ownerId == null
                || gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, Zone.GRAVEYARD)) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);

        Permanent permanent = new Permanent(cardToReturn);
        permanent.tap();
        permanent.setEnteredFromGraveyardOwnerId(ownerId);

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, controllerId,
                new SetNameEffect(returnEffect.landName(), GrantScope.TARGET),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, controllerId,
                new SetCardTypesEffect(Set.of(CardType.LAND), GrantScope.TARGET),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, controllerId,
                new GrantActivatedAbilityEffect(
                        ManaAbilities.tapFor(ManaColor.COLORLESS), GrantScope.TARGET),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " returns ", cardToReturn,
                " to the battlefield tapped under their control as " + returnEffect.landName() + "."));
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, cardToReturn);
    }
}

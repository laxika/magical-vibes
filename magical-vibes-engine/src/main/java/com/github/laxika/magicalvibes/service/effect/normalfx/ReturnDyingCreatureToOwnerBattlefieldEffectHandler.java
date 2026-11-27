package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingCreatureToOwnerBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnDyingCreatureToOwnerBattlefieldEffect) effect;
        UUID dyingCardId = entry.getTriggeringCardId();
        if (dyingCardId == null) {
            return;
        }

        Card cardToReturn = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (cardToReturn == null || ownerId == null) {
            return;
        }
        if (!entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(dyingCardId))) {
            return;
        }
        if (gameQueryService.cantEnterFaceUp(cardToReturn)
                || gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn, Zone.GRAVEYARD)) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        UUID battlefieldControllerId = returnEffect.returnUnderController() ? entry.getControllerId() : ownerId;
        boolean aura = cardToReturn.isAura();
        if (!aura) {
            permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        }

        Permanent permanent = new Permanent(cardToReturn);
        graveyardReturnSupport.applyPermanentGrants(gameData, battlefieldControllerId, permanent, null,
                returnEffect.subtypeOverride().isEmpty() ? returnEffect.grantSubtype() : null);
        if (!returnEffect.grantKeywords().isEmpty()) {
            gameData.addEffectOnPermanent(entry,
                    new GrantKeywordEffect(Set.copyOf(returnEffect.grantKeywords()), Subjects.target()),
                    permanent.getId(), Duration.PERMANENT);
        }
        if (returnEffect.enterWithCounter() != null && returnEffect.enterWithCounterCount() > 0) {
            int counters = gameQueryService.replaceCounters(gameData, permanent, battlefieldControllerId,
                    returnEffect.enterWithCounter(), returnEffect.enterWithCounterCount());
            if (counters > 0) {
                permanent.setCounterCount(returnEffect.enterWithCounter(),
                        permanent.getCounterCount(returnEffect.enterWithCounter()) + counters);
            }
        }
        if (returnEffect.enterTapped()) {
            permanent.tap();
        }
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        if (aura) {
            // An Aura card (one that died animated) enters attached to what its controller chooses, or stays in the
            // graveyard with nothing to enchant (CR 303.4f, 303.4g).
            addSubtypeOverride(gameData, entry, returnEffect, cardToReturn, permanent);
            battlefieldEntryBatchSupport.begin(gameData, List.of(new BattlefieldEntryCard(battlefieldControllerId,
                    ownerId, cardToReturn, Zone.GRAVEYARD, null, null, permanent, List.of(), permanent.isTapped(),
                    PutCardsOntoBattlefieldEffect.EnteringAs.NOTHING_ELSE, null, null)));
            return;
        }
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, battlefieldControllerId, permanent, enterTappedTypes);

        addSubtypeOverride(gameData, entry, returnEffect, cardToReturn, permanent);

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

    private void addSubtypeOverride(GameData gameData, StackEntry entry,
                                    ReturnDyingCreatureToOwnerBattlefieldEffect returnEffect, Card cardToReturn,
                                    Permanent permanent) {
        boolean overriding = true;
        for (var subtype : returnEffect.subtypeOverride()) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), cardToReturn.getName(), null, entry.getControllerId(),
                    new GrantSubtypeEffect(subtype, Subjects.target(), overriding),
                    permanent.getId(), null, null, Duration.PERMANENT, 0));
            overriding = false;
        }
    }
}

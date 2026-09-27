package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
public class PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var putEffect = (PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffect) effect;
        UUID targetPlayerId = entry.targetsForGroup(putEffect.playerTargetGroup()).stream()
                .findFirst().orElse(entry.getTargetId());
        UUID targetCardId = entry.getTargetCardIdsForEffect(effect).stream().findFirst()
                .orElseGet(() -> entry.targetsForGroup(putEffect.graveyardTargetGroup()).stream()
                        .findFirst().orElse(null));

        if (targetPlayerId == null || targetCardId == null
                || !targetPlayerId.equals(gameData.activePlayerId)
                || targetPlayerId.equals(entry.getControllerId())
                || !gameData.orderedPlayerIds.contains(targetPlayerId)) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null
                || !entry.getControllerId().equals(gameQueryService.findGraveyardOwnerById(gameData, targetCardId))
                || !targetCard.hasType(CardType.CREATURE)
                || targetCard.getSupertypes().contains(CardSupertype.LEGENDARY)) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCardId);

        Permanent permanent = new Permanent(targetCard);
        permanent.setEnteredFromGraveyardOwnerId(entry.getControllerId());
        permanent.getGrantedKeywords().add(Keyword.HASTE);
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, permanent, enterTappedTypes);
        graveyardReturnSupport.trackStolenCreature(
                gameData, permanent.getId(), targetPlayerId, entry.getControllerId());
        gameData.queueDelayedAction(new DelayedPermanentAction(
                permanent.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP));
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new GoadTargetCreatureUntilNextTurnEffect(), permanent.getId(), null, null,
                EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(targetPlayerId) + " puts ", targetCard,
                " onto the battlefield under their control with haste and goad."));
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                gameData, targetPlayerId, permanent, targetCard);
    }
}

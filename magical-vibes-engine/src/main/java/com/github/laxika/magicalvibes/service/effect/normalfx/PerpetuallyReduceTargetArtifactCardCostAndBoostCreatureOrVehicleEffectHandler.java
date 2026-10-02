package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Richlau's perpetual artifact-card modification and library placement. */
@Component
@RequiredArgsConstructor
public class PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var modification = (PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard == null || !entry.getControllerId().equals(ownerId)
                || !gameQueryService.cardHasType(targetCard, CardType.ARTIFACT, gameData, ownerId)) {
            return;
        }

        PerpetualCardCastCostSupport.remember(gameData, targetCard, modification.costReduction());
        if (gameQueryService.cardHasType(targetCard, CardType.CREATURE, gameData, ownerId)
                || gameQueryService.cardHasSubtype(targetCard, CardSubtype.VEHICLE, gameData, ownerId)) {
            PerpetualCardPowerToughnessSupport.remember(
                    gameData, targetCard, modification.powerBoost(), modification.toughnessBoost());
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCardId);
        gameData.playerDecks.get(ownerId).add(Math.min(1, gameData.playerDecks.get(ownerId).size()), targetCard);
        gameLogService.append(gameData, GameLog.builder().card(targetCard)
                .text(" perpetually costs ").text(String.valueOf(modification.costReduction()))
                .text(" less to cast and is put second from the top of its owner's library.").build());
    }
}

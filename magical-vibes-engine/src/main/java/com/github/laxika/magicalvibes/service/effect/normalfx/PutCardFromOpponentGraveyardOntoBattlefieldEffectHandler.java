package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCardFromOpponentGraveyardOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardFromOpponentGraveyardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardFromOpponentGraveyardOntoBattlefieldEffect) effect;

        UUID controllerId = entry.getControllerId();
        int xValue = entry.getXValue();

        if (e.checkManaValueOnlyOnResolution() && e.maxManaValue() != null) {
            Card target = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
            int limit = amountEvaluationService.evaluate(gameData, e.maxManaValue(),
                    com.github.laxika.magicalvibes.service.effect.AmountContext.forStackEntry(entry, null));
            if (target == null || target.getManaValue() > limit) {
                return;
            }
        }

        Card targetedCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
        if (targetedCard != null && gameQueryService.isCardBlockedFromEnteringFromZone(gameData, targetedCard, Zone.GRAVEYARD)) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (creatures can't enter the battlefield from graveyards)."));
            return;
        }
        if (targetedCard != null && targetedCard.isAura()) {
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, targetedCard.getId());
            if (ownerId == null || ownerId.equals(controllerId)) return;
            var prepared = new com.github.laxika.magicalvibes.model.Permanent(targetedCard);
            prepared.setEnteredFromGraveyardOwnerId(ownerId);
            if (e.tapped()) prepared.tap();
            graveyardReturnSupport.returnPreparedPermanentsWithAuraChoices(
                    gameData, controllerId, List.of(prepared), null);
            return;
        }

        GraveyardReturnSupport.StolenCreatureResult result = graveyardReturnSupport.stealFromOpponentGraveyard(gameData, entry, controllerId);
        if (result == null) return;

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        if (e.tapped()) {
            result.permanent().tap();
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, result.permanent(), enterTappedTypes);

        graveyardReturnSupport.trackStolenCreature(gameData, result.permanent().getId(), controllerId, result.originalOwnerId());

        String tappedText = e.tapped() ? " tapped" : "";
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder().text(playerName + " puts ").card(result.card()).text(" onto the battlefield" + tappedText + " under their control.").build());

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, result.permanent(), result.card());

        if (e.requireManaValueEqualsX() && xValue > 0) {
            graveyardService.resolveMillPlayer(gameData, result.originalOwnerId(), xValue);
        }
    }
}

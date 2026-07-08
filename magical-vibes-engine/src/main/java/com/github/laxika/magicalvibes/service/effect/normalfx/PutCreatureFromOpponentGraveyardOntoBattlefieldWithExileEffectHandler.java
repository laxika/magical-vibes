package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.ExileTokenAtEndStep;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();

        GraveyardReturnSupport.StolenCreatureResult result = graveyardReturnSupport.stealFromOpponentGraveyard(gameData, entry, controllerId);
        if (result == null) return;

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        result.permanent().getGrantedKeywords().add(Keyword.HASTE);
        result.permanent().setExileIfLeavesBattlefield(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, result.permanent(), enterTappedTypes);

        graveyardReturnSupport.trackStolenCreature(gameData, result.permanent().getId(), result.originalOwnerId());
        gameData.queueDelayedAction(new ExileTokenAtEndStep(result.permanent().getId()));

        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " puts " + result.card().getName() + " onto the battlefield under their control with haste.");

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, result.permanent(), result.card());
    }
}

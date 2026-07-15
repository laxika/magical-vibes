package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCardFromOpponentGraveyardOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardFromOpponentGraveyardOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardFromOpponentGraveyardOntoBattlefieldEffect) effect;

        UUID controllerId = entry.getControllerId();
        int xValue = entry.getXValue();

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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + result.card().getName() + " onto the battlefield" + tappedText + " under their control."));

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, result.permanent(), result.card());

        if (xValue > 0) {
            List<Card> opponentDeck = gameData.playerDecks.get(result.originalOwnerId());
            List<Card> opponentGraveyard = gameData.playerGraveyards.get(result.originalOwnerId());
            int cardsToMill = Math.min(xValue, opponentDeck.size());
            List<String> milledNames = new ArrayList<>();
            for (int i = 0; i < cardsToMill; i++) {
                Card milled = opponentDeck.removeFirst();
                opponentGraveyard.add(milled);
                milledNames.add(milled.getName());
            }
            if (cardsToMill > 0) {
                String opponentName = gameData.playerIdToName.get(result.originalOwnerId());
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(opponentName + " mills " + cardsToMill + " cards (" + String.join(", ", milledNames) + ")."));
            }
        }
    }
}

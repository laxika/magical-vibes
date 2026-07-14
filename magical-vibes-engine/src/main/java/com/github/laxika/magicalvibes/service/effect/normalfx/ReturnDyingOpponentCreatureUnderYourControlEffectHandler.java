package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureUnderYourControlEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ReturnDyingOpponentCreatureUnderYourControlEffect} (Necroskitter): steals the
 * dying creature card from its owner's graveyard and puts it onto the battlefield under the
 * ability controller's control, tracking it as a stolen creature. Fizzles if it is no longer in a
 * graveyard.
 */
@Component
@RequiredArgsConstructor
public class ReturnDyingOpponentCreatureUnderYourControlEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingOpponentCreatureUnderYourControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnDyingOpponentCreatureUnderYourControlEffect) effect;
        UUID controllerId = entry.getControllerId();

        // The dying card id is carried on the effect (not the stack entry's targetId, which would
        // make the trigger validate against on-battlefield permanents and fizzle). Feed it to the
        // shared steal-from-graveyard helper via the entry.
        entry.setTargetId(e.dyingCardId());
        GraveyardReturnSupport.StolenCreatureResult result =
                graveyardReturnSupport.stealFromOpponentGraveyard(gameData, entry, controllerId);
        if (result == null) return;

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, result.permanent(), enterTappedTypes);

        graveyardReturnSupport.trackStolenCreature(gameData, result.permanent().getId(), controllerId, result.originalOwnerId());

        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " returns " + result.card().getName() + " to the battlefield under their control.");

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, result.permanent(), result.card());
    }
}

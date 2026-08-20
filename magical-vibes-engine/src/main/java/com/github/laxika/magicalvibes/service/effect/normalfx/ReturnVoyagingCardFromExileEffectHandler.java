package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnVoyagingCardFromExileEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the declined landfall half by returning the voyage card and drawing its payoff. */
@Component
@RequiredArgsConstructor
public class ReturnVoyagingCardFromExileEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnVoyagingCardFromExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cardId = entry.getCard().getId();
        Integer voyageCounters = gameData.exiledVoyageCounters.get(cardId);
        var voyageControllerId = gameData.exiledVoyageControllerIds.get(cardId);
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (voyageCounters == null || voyageControllerId == null || exiled == null
                || !gameData.removeFromExile(cardId)) {
            return;
        }

        Permanent permanent = new Permanent(exiled.card());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, voyageCounters);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, exiled.ownerId(), permanent);
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, exiled.ownerId(), exiled.card(), null, false);
        playerInteractionSupport.applyDrawCards(gameData, voyageControllerId, voyageCounters);
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(), " returns from exile."));
    }
}

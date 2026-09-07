package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandPermanentToBattlefieldOrHandHandler implements MayEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ExiledCardEntry exiled = ability.targetCardId() == null
                ? null
                : gameData.findExiledCard(ability.targetCardId());
        if (exiled != null && gameData.removeFromExile(exiled.card().getId())) {
            if (accepted) {
                graveyardReturnSupport.putCardOntoBattlefieldFromExile(
                        gameData, ability.controllerId(), exiled.card());
            } else {
                gameData.addCardToHand(ability.controllerId(), exiled.card());
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " puts ", exiled.card(), " into their hand."));
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}

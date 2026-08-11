package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastOrPutIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("mayExileTopUntilNonlandEffectHandler")
@RequiredArgsConstructor
public class ExileTopUntilNonlandMayCastOrPutIntoHandEffectHandler implements MayEffectHandlerBean {

    private final ExileFreeCastSupport exileFreeCastSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandMayCastOrPutIntoHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted && ability.targetCardId() != null) {
            exileFreeCastSupport.castFromExileWithoutPaying(gameData, player, ability.targetCardId());
            return;
        }

        ExiledCardEntry exiled = ability.targetCardId() == null
                ? null
                : gameData.findExiledCard(ability.targetCardId());
        if (exiled != null) {
            gameData.removeFromExile(exiled.card().getId());
            gameData.addCardToHand(ability.controllerId(), exiled.card());
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " declines to cast ").card(exiled.card())
                    .text(" and puts it into their hand.")
                    .build());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}

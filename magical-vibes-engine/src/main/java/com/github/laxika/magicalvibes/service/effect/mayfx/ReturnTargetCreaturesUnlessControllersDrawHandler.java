package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreaturesUnlessControllersDrawEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnTargetCreaturesUnlessControllersDrawEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one selected creature controller's Decoy Gambit choice. */
@Component
@RequiredArgsConstructor
public class ReturnTargetCreaturesUnlessControllersDrawHandler implements MayEffectHandlerBean {

    private final ReturnTargetCreaturesUnlessControllersDrawEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends com.github.laxika.magicalvibes.model.effect.CardEffect> handledEffect() {
        return ReturnTargetCreaturesUnlessControllersDrawEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        effectHandler.continueAfterChoice(gameData, ability, accepted);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

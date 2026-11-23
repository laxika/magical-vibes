package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BalduvianWarlordEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.BalduvianWarlordEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles the controller's choice to have a creature removed from combat block again ("you may have it block an
 * attacking creature of your choice", False Orders). The choice is part of the resolving spell, so an accepted reblock
 * happens at once instead of going on the stack.
 */
@Component
@RequiredArgsConstructor
public class BalduvianWarlordReblockMayHandler implements MayEffectHandlerBean {

    private final BalduvianWarlordEffectHandler balduvianWarlordEffectHandler;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BalduvianWarlordEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            Permanent blocker = gameQueryService.findPermanentById(gameData, ability.targetCardId());
            if (blocker != null) {
                balduvianWarlordEffectHandler.chooseAttackerToBlock(gameData, ability.controllerId(),
                        ability.sourceCard().getName(), blocker);
            }
        }
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}

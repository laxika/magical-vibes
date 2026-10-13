package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BalduvianWarlordEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.BalduvianWarlordEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Handles the controller's choice to have a creature removed from combat block again. The choice is
 * part of the resolving spell (e.g. False Orders), so an accepted reblock resolves immediately
 * instead of going on the stack.
 */
@Component
@RequiredArgsConstructor
public class BalduvianWarlordReblockMayHandler implements MayEffectHandlerBean {

    private final BalduvianWarlordEffectHandler balduvianWarlordEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BalduvianWarlordEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(ability.effects()),
                    0
            );
            entry.setTargetId(ability.targetCardId());
            for (CardEffect effect : ability.effects()) {
                if (effect instanceof BalduvianWarlordEffect warlordEffect) {
                    balduvianWarlordEffectHandler.resolve(gameData, entry, warlordEffect);
                }
            }
        }
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}

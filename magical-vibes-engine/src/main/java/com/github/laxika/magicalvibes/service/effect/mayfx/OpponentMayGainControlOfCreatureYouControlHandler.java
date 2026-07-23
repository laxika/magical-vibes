package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayGainControlOfCreatureYouControlEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.OpponentMayGainControlOfCreatureYouControlEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Infernal Denizen upkeep fallback: opponent may take a creature the Denizen's controller controls.
 * Decline does nothing; accept auto-takes a lone creature or prompts a permanent choice.
 */
@Component
@RequiredArgsConstructor
public class OpponentMayGainControlOfCreatureYouControlHandler implements MayEffectHandlerBean {

    private final OpponentMayGainControlOfCreatureYouControlEffectHandler stealHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentMayGainControlOfCreatureYouControlEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        var effect = (OpponentMayGainControlOfCreatureYouControlEffect) ability.effects().getFirst();
        UUID opponentId = ability.controllerId();
        UUID victimControllerId = ability.targetCardId();
        UUID sourcePermanentId = ability.sourcePermanentId();

        stealHandler.beginCreatureChoice(gameData, opponentId, victimControllerId,
                sourcePermanentId, ability.sourceCard().getName(), effect);

        // If a permanent choice was begun, its completion owns the epilogue.
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}

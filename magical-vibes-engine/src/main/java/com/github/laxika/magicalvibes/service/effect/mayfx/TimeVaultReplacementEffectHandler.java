package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TimeVaultReplacementEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimeVaultReplacementEffectHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TapUntapSupport tapUntapSupport;
    private final InputCompletionService inputCompletionService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TimeVaultReplacementEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted && ability.sourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            if (source != null) {
                tapUntapSupport.untapPermanent(gameData, source);
            }
        }

        turnProgressionService.completeTimeVaultChoice(
                gameData, ability.controllerId(), ability.sourcePermanentId(), accepted);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

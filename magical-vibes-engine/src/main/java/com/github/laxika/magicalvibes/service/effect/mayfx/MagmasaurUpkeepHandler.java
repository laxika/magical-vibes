package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MagmasaurUpkeepEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.MagmasaurUpkeepSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Accept/decline half of Magmasaur's upkeep trigger. Accepting removes one +1/+1 counter; declining
 * — or no longer having one to remove — sacrifices Magmasaur and blasts each creature without flying
 * and each player for its remaining +1/+1 counters.
 */
@Component
@RequiredArgsConstructor
public class MagmasaurUpkeepHandler implements MayEffectHandlerBean {

    private final MagmasaurUpkeepSupport magmasaurUpkeepSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MagmasaurUpkeepEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID controllerId = ability.controllerId();
        UUID sourcePermanentId = ability.sourcePermanentId();

        if (accepted && magmasaurUpkeepSupport.counters(gameData, sourcePermanentId) > 0) {
            magmasaurUpkeepSupport.removeCounter(gameData, sourcePermanentId);
        } else {
            magmasaurUpkeepSupport.applyPenalty(gameData, controllerId, sourcePermanentId, ability.sourceCard());
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}

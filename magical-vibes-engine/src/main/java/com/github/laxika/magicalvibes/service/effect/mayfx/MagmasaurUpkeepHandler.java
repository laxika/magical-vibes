package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MagmasaurUpkeepEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.MagmasaurUpkeepSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Accept/decline half of Magmasaur's upkeep trigger. Accepting removes one +1/+1 counter; declining
 * — or no longer having one to remove — sacrifices Magmasaur and blasts each creature without flying
 * and each player for its remaining +1/+1 counters. A decline changes nothing before the sacrifice, so
 * a Magmasaur with competing death replacements is asked about before it leaves (CR 616.1) and the
 * decline is completed again with that answer.
 */
@Component
@RequiredArgsConstructor
public class MagmasaurUpkeepHandler implements MayEffectHandlerBean {

    private final MagmasaurUpkeepSupport magmasaurUpkeepSupport;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

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
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        permanentChoiceHandlerServiceProvider.getObject().declineAskingDeathReplacementsFirst(
                gameData, player, ability, () -> {
                    magmasaurUpkeepSupport.applyPenalty(
                            gameData, controllerId, sourcePermanentId, ability.sourceCard());
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                });
    }
}

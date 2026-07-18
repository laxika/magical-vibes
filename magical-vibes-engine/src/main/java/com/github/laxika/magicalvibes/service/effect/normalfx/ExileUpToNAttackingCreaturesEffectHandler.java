package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToNAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Exile up to N target attacking creatures." Gathers every attacking creature on the battlefield
 * and prompts the controller to choose up to N to exile (they may choose none). Completion is
 * handled by {@link MultiPermanentChoiceContext.ExileAttackingCreatures}.
 */
@Component
@RequiredArgsConstructor
public class ExileUpToNAttackingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUpToNAttackingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileUpToNAttackingCreaturesEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<UUID> attackingCreatureIds = new ArrayList<>();
        gameData.forEachPermanent((pid, permanent) -> {
            if (permanent.isAttacking() && gameQueryService.isCreature(gameData, permanent)) {
                attackingCreatureIds.add(permanent.getId());
            }
        });

        if (attackingCreatureIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), " has no attacking creatures to exile."));
            return;
        }

        int maxCount = Math.min(e.maxCount(), attackingCreatureIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, attackingCreatureIds, maxCount,
                new MultiPermanentChoiceContext.ExileAttackingCreatures(),
                "Choose up to " + maxCount + " attacking creature" + (maxCount == 1 ? "" : "s") + " to exile.");
    }
}

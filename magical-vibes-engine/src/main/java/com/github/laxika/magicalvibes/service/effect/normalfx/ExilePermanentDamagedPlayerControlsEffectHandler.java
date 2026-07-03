package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExilePermanentDamagedPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExilePermanentDamagedPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExilePermanentDamagedPlayerControlsEffect) effect;

        UUID defenderId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();

        if (defenderId == null) return;

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<UUID> validIds = new ArrayList<>();
        if (defenderBattlefield != null) {
            for (Permanent perm : defenderBattlefield) {
                if (e.predicate() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.predicate())) {
                    validIds.add(perm.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            String logEntry = entry.getCard().getName() + "'s ability resolves, but "
                    + gameData.playerIdToName.get(defenderId) + " has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.pendingExileDamagedPlayerControlsPermanent = true;
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validIds, 1,
                entry.getCard().getName() + "'s ability — Choose a permanent "
                        + gameData.playerIdToName.get(defenderId) + " controls to exile.");
    }
}

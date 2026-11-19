package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyTargetPermanentAtEndOfCombatEffect}: schedules each permanent its subject names for
 * destruction at end of combat via {@link DelayedPermanentAction} (regeneration/indestructible apply). E.g. Goblin
 * Sappers' "Destroy it and this creature at end of combat".
 */
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyTargetPermanentAtEndOfCombatEffect destroy = (DestroyTargetPermanentAtEndOfCombatEffect) effect;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, destroy.subject())) {
            // The delayed trigger belongs to the ability that created it: that ability's source and controller, not
            // the destroyed permanent's, whatever happens to it before end of combat (CR 603.7e). Sharing the
            // ability's source lets "destroy it and this creature at end of combat" (Goblin Sappers) form one delayed
            // trigger.
            DelayedPermanentActions.schedule(gameData, entry,
                    new DelayedPermanentAction(permanent.getId(),
                            DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT, destroy.cannotBeRegenerated()),
                    ActivePlayer.ANY);
            gameLogService.append(gameData,
                    GameLog.cardThen(permanent.getCard(), " will be destroyed at end of combat."));
        }
    }
}

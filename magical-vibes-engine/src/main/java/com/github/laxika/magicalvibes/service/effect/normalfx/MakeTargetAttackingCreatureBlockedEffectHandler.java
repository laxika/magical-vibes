package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetAttackingCreatureBlockedEffect;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MakeTargetAttackingCreatureBlockedEffect}: each attacking creature its subject names becomes
 * blocked with no creature blocking it (CR 509.1h), so it assigns no combat damage (CR 510.1c).
 * "Becomes blocked" triggers that don't depend on a blocker still fire.
 */
@Component
@RequiredArgsConstructor
public class MakeTargetAttackingCreatureBlockedEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final CombatBlockService combatBlockService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeTargetAttackingCreatureBlockedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MakeTargetAttackingCreatureBlockedEffect) effect;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, e, e.subject())) {
            combatBlockService.makeAttackingCreatureBlockedWithoutBlockers(gameData, permanent);
        }
    }
}

package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import org.springframework.stereotype.Service;

@Service
public class BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffectTargetValidator {

    @ValidatesTarget(BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffect.class)
    public void validate(TargetValidationContext ctx,
                         BecomeTargetLegendaryCreatureCopyOfMemoryCounterExiledCreatureUntilNextTurnEffect effect) {
        if (ctx.targetZone() != Zone.EXILE) {
            return;
        }
        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        if (exiled == null || !ctx.gameData().exiledCardsWithMemoryCounters.contains(ctx.targetId())) {
            throw new IllegalStateException("Target card must have a memory counter on it");
        }
    }
}

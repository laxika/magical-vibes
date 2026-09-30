package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersOnTargetExiledCardWithSuspendEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import org.springframework.stereotype.Service;

@Service
public class PutTimeCountersOnTargetExiledCardWithSuspendEffectTargetValidator {

    @ValidatesTarget(PutTimeCountersOnTargetExiledCardWithSuspendEffect.class)
    public void validate(TargetValidationContext ctx, PutTimeCountersOnTargetExiledCardWithSuspendEffect effect) {
        if (ctx.targetZone() != Zone.EXILE || ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card in exile");
        }

        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        if (exiled == null || exiled.faceDown()) {
            throw new IllegalStateException("Target card is not face up in exile");
        }
        if (ctx.sourceControllerId() == null
                || !ctx.sourceControllerId().equals(exiled.ownerId())
                || !ctx.gameData().cardsExiledFromGraveyardThisTurn.contains(ctx.targetId())) {
            throw new IllegalStateException(
                    "Target card must have been put into exile from your graveyard this turn");
        }
    }
}

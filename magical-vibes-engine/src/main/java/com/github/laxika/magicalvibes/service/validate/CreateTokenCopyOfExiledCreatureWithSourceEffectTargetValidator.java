package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCreatureWithSourceEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CreateTokenCopyOfExiledCreatureWithSourceEffectTargetValidator {

    @ValidatesTarget(CreateTokenCopyOfExiledCreatureWithSourceEffect.class)
    public void validate(TargetValidationContext ctx,
                         CreateTokenCopyOfExiledCreatureWithSourceEffect effect) {
        if (!effect.targeted()) {
            return;
        }
        if (ctx.targetZone() != Zone.EXILE || ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target creature card in exile");
        }

        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        if (exiled == null || exiled.faceDown()) {
            throw new IllegalStateException("Target card is not face up in exile");
        }
        if (!exiled.card().hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Target card must be a creature");
        }

        Permanent source = ctx.sourcePermanentSnapshot() != null
                ? ctx.sourcePermanentSnapshot()
                : findSourcePermanent(ctx);
        if (source == null || !source.getId().equals(exiled.sourcePermanentId())) {
            throw new IllegalStateException("Target card was not exiled with this permanent");
        }
    }

    private Permanent findSourcePermanent(TargetValidationContext ctx) {
        if (ctx.sourceCard() == null) {
            return null;
        }
        UUID sourceCardId = ctx.sourceCard().getId();
        for (UUID playerId : ctx.gameData().orderedPlayerIds) {
            for (Permanent permanent : ctx.gameData().playerBattlefields
                    .getOrDefault(playerId, List.of())) {
                if (permanent.getCard().getId().equals(sourceCardId)
                        || permanent.getOriginalCard().getId().equals(sourceCardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }
}

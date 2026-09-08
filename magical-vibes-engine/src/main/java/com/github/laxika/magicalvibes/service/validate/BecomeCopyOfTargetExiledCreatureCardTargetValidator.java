package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetExiledCreatureCardEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BecomeCopyOfTargetExiledCreatureCardTargetValidator {

    @ValidatesTarget(BecomeCopyOfTargetExiledCreatureCardEffect.class)
    public void validate(TargetValidationContext ctx, BecomeCopyOfTargetExiledCreatureCardEffect effect) {
        if (ctx.targetZone() != Zone.EXILE) {
            throw new IllegalStateException("Effect requires an exile target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }

        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        if (exiled == null || exiled.faceDown()) {
            throw new IllegalStateException("Target card is not face up in exile");
        }
        if (!exiled.card().hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Target card must be a creature card");
        }

        Permanent source = ctx.sourcePermanentSnapshot() != null
                ? ctx.sourcePermanentSnapshot()
                : findSourcePermanent(ctx.gameData(), ctx.sourceCard());
        if (source == null || !source.getId().equals(exiled.sourcePermanentId())) {
            throw new IllegalStateException("Target card was not exiled with this permanent");
        }
    }

    private Permanent findSourcePermanent(GameData gameData, Card sourceCard) {
        if (sourceCard == null) {
            return null;
        }
        UUID sourceCardId = sourceCard.getId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (permanent.getCard().getId().equals(sourceCardId)
                        || permanent.getOriginalCard().getId().equals(sourceCardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }
}

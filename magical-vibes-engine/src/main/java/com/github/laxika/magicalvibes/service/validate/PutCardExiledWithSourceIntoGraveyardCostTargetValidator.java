package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoGraveyardCost;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PutCardExiledWithSourceIntoGraveyardCostTargetValidator {

    public void validate(TargetValidationContext ctx, PutCardExiledWithSourceIntoGraveyardCost effect) {
        if (ctx.targetZone() != Zone.EXILE) {
            throw new IllegalStateException("Cost requires an exile card");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Choose a card exiled with this permanent");
        }

        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        Permanent source = ctx.sourcePermanentSnapshot() != null
                ? ctx.sourcePermanentSnapshot()
                : findSourcePermanent(ctx.gameData(), ctx.sourceCard());
        if (exiled == null || source == null || !source.getId().equals(exiled.sourcePermanentId())) {
            throw new IllegalStateException("Card was not exiled with this permanent");
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

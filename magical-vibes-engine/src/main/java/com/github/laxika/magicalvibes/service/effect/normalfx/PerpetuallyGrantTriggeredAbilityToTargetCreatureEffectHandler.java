package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Records a perpetual triggered-ability grant on a targeted creature card identity. */
@Component
public class PerpetuallyGrantTriggeredAbilityToTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = findPermanent(gameData, targetId);
            var targetCard = target != null && target.getOriginalCard() != null
                    ? target.getOriginalCard() : findCardInExile(gameData, targetId);
            if (targetCard == null) {
                continue;
            }

            UUID cardId = targetCard.getId();
            gameData.grantPerpetualAbility(cardId, grant.triggeredAbilitySlot(), grant.triggeredAbility());

            if (target != null) {
                target.addPersistentTriggeredEffect(grant.triggeredAbilitySlot(),
                        grant.triggeredAbility());
            }
        }
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(permanentId)) {
                    return permanent;
                }
            }
        }
        return null;
    }

    private com.github.laxika.magicalvibes.model.Card findCardInExile(GameData gameData, UUID cardId) {
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        return exiled == null ? null : exiled.card();
    }
}

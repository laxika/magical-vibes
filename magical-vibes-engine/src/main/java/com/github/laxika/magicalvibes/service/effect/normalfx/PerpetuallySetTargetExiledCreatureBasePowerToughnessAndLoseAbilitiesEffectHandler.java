package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetTargetExiledCreatureBasePowerToughnessAndLoseAbilitiesEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Makes Rasaad's targeted exiled creature card a vanilla 1/1 perpetually. */
@Slf4j
@Component
public class PerpetuallySetTargetExiledCreatureBasePowerToughnessAndLoseAbilitiesEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallySetTargetExiledCreatureBasePowerToughnessAndLoseAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetZone() != Zone.EXILE) {
            return;
        }

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        UUID sourcePermanentId = entry.getSourcePermanentId();

        for (UUID targetId : targetIds) {
            ExiledCardEntry exiled = gameData.findExiledCard(targetId);
            if (exiled == null || exiled.faceDown() || !exiled.card().hasType(CardType.CREATURE)) {
                continue;
            }
            if (sourcePermanentId != null && !sourcePermanentId.equals(exiled.sourcePermanentId())) {
                continue;
            }

            Card modified = exiled.card().createRuntimeCopy();
            modified.clearRulesTextAndAbilities();
            modified.setCardText("");
            modified.setKeywords(java.util.Set.of());
            modified.setPower(1);
            modified.setToughness(1);
            modified.freeze();

            synchronized (gameData.exiledCards) {
                for (int i = 0; i < gameData.exiledCards.size(); i++) {
                    ExiledCardEntry current = gameData.exiledCards.get(i);
                    if (targetId.equals(current.card().getId())) {
                        gameData.exiledCards.set(i, new ExiledCardEntry(modified,
                                current.ownerId(), current.sourcePermanentId(), current.faceDown(),
                                current.exilerId(), current.exiledTurnNumber(),
                                current.controllerTurnsTakenAtExile(), current.abilityLink()));
                        break;
                    }
                }
            }
            log.info("Game {} - {} perpetually becomes a vanilla 1/1 in exile",
                    gameData.id, exiled.card().getName());
        }
    }
}

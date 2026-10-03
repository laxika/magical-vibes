package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesMustAttackTargetPlayerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect attackEffect =
                (TargetPlayersCreaturesMustAttackEachOtherUntilNextTurnEffect) effect;
        List<UUID> targetPlayers = entry.targetsForGroup(attackEffect.targetGroup());
        if (targetPlayers.size() < 2) {
            targetPlayers = entry.getTargetIds();
        }
        if (targetPlayers.size() < 2) {
            return;
        }

        UUID firstPlayer = targetPlayers.get(0);
        UUID secondPlayer = targetPlayers.get(1);
        if (firstPlayer.equals(secondPlayer)
                || !gameData.playerIds.contains(firstPlayer)
                || !gameData.playerIds.contains(secondPlayer)) {
            return;
        }

        addAttackRequirements(gameData, entry, firstPlayer, secondPlayer);
        addAttackRequirements(gameData, entry, secondPlayer, firstPlayer);
    }

    private void addAttackRequirements(GameData gameData, StackEntry entry,
                                       UUID creatureControllerId, UUID requiredAttackTargetId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) {
            return;
        }

        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    new CreaturesMustAttackTargetPlayerUntilNextTurnEffect(requiredAttackTargetId),
                    permanent.getId(), null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        }
    }
}

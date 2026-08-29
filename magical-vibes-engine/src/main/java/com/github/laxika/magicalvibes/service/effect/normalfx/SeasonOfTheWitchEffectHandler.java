package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeasonOfTheWitchEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeasonOfTheWitchEffectHandler implements NormalEffectHandlerBean {

    private final CombatAttackService combatAttackService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeasonOfTheWitchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null
                || gameData.skippedStepOrPhasesThisTurn.getOrDefault(activePlayerId, Set.of())
                .contains(SkipStepOrPhaseKind.COMBAT_PHASE)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) {
            return;
        }

        Set<Integer> attackableIndices = new HashSet<>(
                combatAttackService.getAttackableCreatureIndices(gameData, activePlayerId));
        List<Permanent> toDestroy = new ArrayList<>();
        for (int index : attackableIndices) {
            Permanent permanent = battlefield.get(index);
            if (!permanent.isTapped() && !permanent.isAttackedThisTurn()) {
                toDestroy.add(permanent);
            }
        }
        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), false);
    }
}

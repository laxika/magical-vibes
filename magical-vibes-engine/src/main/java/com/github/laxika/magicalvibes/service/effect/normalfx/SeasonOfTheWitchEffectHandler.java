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

        Set<UUID> ableToAttack = gameData.creaturesAbleToAttackAtDeclareAttackersThisTurn.get(activePlayerId);
        if (ableToAttack == null) {
            ableToAttack = combatAttackService.getAttackableCreatureIndices(gameData, activePlayerId).stream()
                    .map(battlefield::get)
                    .map(Permanent::getId)
                    .collect(java.util.stream.Collectors.toSet());
        }
        List<Permanent> toDestroy = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (ableToAttack.contains(permanent.getId()) && !permanent.isAttackedThisTurn()) {
                toDestroy.add(permanent);
            }
        }
        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), false);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Noble Ox's enters-the-battlefield blocking effect. */
@Component
@RequiredArgsConstructor
public class MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CombatBlockService combatBlockService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Permanent> attackers = gameData.orderedPlayerIds.stream()
                .flatMap(playerId -> gameData.playerBattlefields
                        .getOrDefault(playerId, List.of()).stream())
                .filter(attacker -> gameQueryService.isCreature(gameData, attacker))
                .filter(Permanent::isAttacking)
                .filter(attacker -> controllerId.equals(attacker.getAttackTarget()))
                .filter(attacker -> !attacker.isBlockedWithoutBlockers()
                        && !gameQueryService.isBlockedByAnyCreature(gameData, attacker))
                .toList();

        for (Permanent attacker : attackers) {
            combatBlockService.applyBlockFromEffect(
                    gameData, source, attacker, false, source.getCard().getName());
        }
    }
}

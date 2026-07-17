package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect) effect;

        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.isEmpty()) {
            return;
        }

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        UUID opponentId = targets.getFirst();
        if (gameData.playerIds.contains(opponentId) && !opponentId.equals(entry.getControllerId())) {
            int opponentDamage = gameQueryService.applyDamageMultiplier(gameData, e.opponentDamage(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, opponentId, opponentDamage);
        }

        for (int i = 1; i < targets.size(); i++) {
            UUID creatureId = targets.get(i);
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            UUID creatureControllerId = creature != null
                    ? gameQueryService.findPermanentController(gameData, creatureId)
                    : null;
            if (creature != null
                    && opponentId.equals(creatureControllerId)
                    && gameQueryService.isCreature(gameData, creature)
                    && !damageSupport.isDamagePreventedForCreature(gameData, entry, creature)) {
                int creatureDamage = gameQueryService.applyDamageMultiplier(gameData, e.creatureDamage(), entry);
                damageSupport.dealCreatureDamage(gameData, entry, creature, creatureDamage);
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}

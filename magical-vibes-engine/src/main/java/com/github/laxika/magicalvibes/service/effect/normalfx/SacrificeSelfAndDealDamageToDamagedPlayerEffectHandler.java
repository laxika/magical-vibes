package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDealDamageToDamagedPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * "You may sacrifice it. If you do, it deals N damage to that player" (Furnace Scamp): sacrifices the source as a
 * resolution step, then the source deals the damage, using its last-known information (CR 608.2h), to the damaged
 * player held in the entry's target, only if it was sacrificed.
 */
@Component
@RequiredArgsConstructor
public class SacrificeSelfAndDealDamageToDamagedPlayerEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAndDealDamageToDamagedPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID defenderId = entry.getTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        if (defenderId == null || sourcePermanentId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles — source no longer on the battlefield."));
            return;
        }

        // CR 701.21a: a player can't sacrifice a permanent they don't control, so no damage either
        boolean controlledByAbilityController = gameData.playerBattlefields.get(entry.getControllerId()).stream()
                .anyMatch(p -> p.getId().equals(sourcePermanentId));
        if (!controlledByAbilityController) {
            return;
        }

        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, sourcePermanentId, this);
    }

    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID defenderId = entry.getTargetId();
        if (!entry.getResolutionResults().anySacrificed() || !gameData.playerIds.contains(defenderId)) {
            return;
        }
        damageSupport.dealDamageToPlayer(gameData, entry, defenderId,
                ((SacrificeSelfAndDealDamageToDamagedPlayerEffect) effect).damage());
        gameOutcomeService.checkWinCondition(gameData);
    }
}

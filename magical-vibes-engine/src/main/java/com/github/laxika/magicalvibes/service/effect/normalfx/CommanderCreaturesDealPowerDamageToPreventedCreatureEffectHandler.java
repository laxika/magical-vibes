package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CommanderCreaturesDealPowerDamageToPreventedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Judgment of Alexander's non-targeting creature-damage trigger. */
@Component
@RequiredArgsConstructor
public class CommanderCreaturesDealPowerDamageToPreventedCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CommanderCreaturesDealPowerDamageToPreventedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID preventedCreatureId = entry.getTargetId();
        if (preventedCreatureId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, preventedCreatureId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        for (Permanent commander : List.copyOf(battlefield)) {
            if (!gameQueryService.isCreature(gameData, commander)
                    || !(commander.isCommander()
                    || gameData.isCommander(commander.getOriginalCard().getId()))) {
                continue;
            }
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, commander)) {
                gameLogService.append(gameData, GameLog.cardThen(commander.getCard(), "'s damage is prevented."));
                continue;
            }
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, target, commander)) {
                gameLogService.append(gameData, GameLog.textCardText(commander.getCard().getName()
                        + "'s damage to ", target.getCard(), " is prevented."));
                continue;
            }

            UUID commanderControllerId = gameQueryService.findPermanentController(gameData, commander.getId());
            if (commanderControllerId == null) {
                continue;
            }
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    commander.getCard(),
                    commanderControllerId,
                    commander.getCard().getName() + "'s ability",
                    List.of(),
                    null,
                    commander.getId());
            int power = gameQueryService.getPowerBasedDamage(gameData, commander);
            int damage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
            damageSupport.dealCreatureDamage(gameData, damageEntry, target, damage, commander);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}

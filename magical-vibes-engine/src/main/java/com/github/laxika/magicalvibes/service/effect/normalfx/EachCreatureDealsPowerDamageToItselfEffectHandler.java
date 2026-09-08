package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachCreatureDealsPowerDamageToItselfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachCreatureDealsPowerDamageToItselfEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachCreatureDealsPowerDamageToItselfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachCreatureDealsPowerDamageToItselfEffect) effect;
        List<Permanent> creatures = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)
                        || (e.predicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, e.predicate()))) continue;
                creatures.add(permanent);
            }
        }

        for (Permanent creature : creatures) {
            Permanent liveCreature = gameQueryService.findPermanentById(gameData, creature.getId());
            if (liveCreature == null || !gameQueryService.isCreature(gameData, liveCreature)) {
                continue;
            }

            UUID controllerId = gameQueryService.findPermanentController(gameData, liveCreature.getId());
            if (controllerId == null) {
                continue;
            }

            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, liveCreature)) {
                gameLogService.append(gameData, GameLog.cardThen(liveCreature.getCard(), "'s damage is prevented."));
                continue;
            }

            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, liveCreature, liveCreature)) {
                gameLogService.append(gameData, GameLog.cardThen(liveCreature.getCard(), "'s damage is prevented."));
                continue;
            }

            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    liveCreature.getCard(),
                    controllerId,
                    liveCreature.getCard().getName() + "'s ability",
                    List.of(),
                    null,
                    liveCreature.getId());
            int power = gameQueryService.getPowerBasedDamage(gameData, liveCreature);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
            damageSupport.dealCreatureDamage(gameData, damageEntry, liveCreature, rawDamage, liveCreature);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}

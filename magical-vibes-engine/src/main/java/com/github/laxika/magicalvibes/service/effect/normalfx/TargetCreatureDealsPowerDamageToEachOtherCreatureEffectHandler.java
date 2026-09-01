package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureDealsPowerDamageToEachOtherCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsPowerDamageToEachOtherCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null) {
            return;
        }

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (sourceControllerId == null) {
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, source);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                sourceControllerId,
                source.getCard().getName() + "'s ability",
                List.of(),
                null,
                source.getId());

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (permanent.getId().equals(source.getId())) {
                    continue;
                }
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (gameQueryService.isDamagePreventable(gameData)
                        && gameQueryService.hasProtectionFromSource(gameData, permanent, source)) {
                    gameLogService.append(gameData, GameLog.cardTextCard(permanent.getCard(),
                            " has protection — damage from ", source.getCard(), " prevented."));
                    continue;
                }
                damageSupport.dealCreatureDamage(gameData, damageEntry, permanent, rawDamage, source);
            }
        }
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToEachOtherCreatureAndEachOpponentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureDealsPowerDamageToEachOtherCreatureAndEachOpponentEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsPowerDamageToEachOtherCreatureAndEachOpponentEffect.class;
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

        // The targeted creature is the damage source (CR 608.2h). If it can't deal damage,
        // nothing happens at all.
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, source);

        // A temporary entry whose source is the creature, so prevention/protection, lifelink and
        // "deals damage" triggers key off the creature rather than off the spell.
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

        // "Each opponent" is measured from the spell's controller, not from the creature's.
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(entry.getControllerId())) {
                continue;
            }
            damageSupport.dealDamageToPlayer(gameData, damageEntry, playerId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}

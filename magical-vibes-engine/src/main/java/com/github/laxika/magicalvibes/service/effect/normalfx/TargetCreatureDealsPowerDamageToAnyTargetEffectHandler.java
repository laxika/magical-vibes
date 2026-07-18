package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureDealsPowerDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsPowerDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetCreatureDealsPowerDamageToAnyTargetEffect) effect;

        List<UUID> sourceGroup = entry.targetsForGroup(e.sourceTargetGroup());
        List<UUID> victimGroup = entry.targetsForGroup(e.victimTargetGroup());
        if (sourceGroup.isEmpty() || victimGroup.isEmpty()) {
            return;
        }

        Permanent biter = gameQueryService.findPermanentById(gameData, sourceGroup.getFirst());
        if (biter == null) {
            return;
        }
        UUID victimId = victimGroup.getFirst();

        UUID controllerId = gameQueryService.findPermanentController(gameData, biter.getId());
        if (controllerId == null) {
            return;
        }

        // The biting creature is the damage source (CR 608.2h). If it is prevented from dealing
        // damage, nothing happens.
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, biter)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(biter.getCard(), "'s damage is prevented."));
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, biter);

        // Build a temporary entry whose source is the biting creature, so prevention/protection/
        // lifelink and "deals damage" triggers key off the creature rather than the spell.
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                biter.getCard(),
                controllerId,
                biter.getCard().getName() + "'s ability",
                List.of(),
                null,
                biter.getId());

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
        damageSupport.resolveAnyTargetDamage(gameData, damageEntry, victimId, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}

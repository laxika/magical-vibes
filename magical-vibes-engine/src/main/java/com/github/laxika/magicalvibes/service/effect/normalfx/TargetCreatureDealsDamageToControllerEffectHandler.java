package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureDealsDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var targetEffect = (TargetCreatureDealsDamageToControllerEffect) effect;
        List<UUID> sourceIds = targetEffect.scope() == GrantScope.TOKENS_CREATED_THIS_RESOLUTION
                ? List.copyOf(entry.getCreatedPermanentIds())
                : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        for (UUID sourceId : sourceIds) {
            Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
            if (source != null) {
                dealDamage(gameData, entry, targetEffect, source,
                        gameQueryService.findPermanentController(gameData, source.getId()));
            } else if (entry.getSourcePermanentSnapshot() != null
                    && sourceId.equals(entry.getSourcePermanentSnapshot().getId())) {
                dealDamage(gameData, entry, targetEffect, entry.getSourcePermanentSnapshot(),
                        gameData.activePlayerId);
            } else {
                entry.getAttackingPermanentSnapshots().stream()
                        .filter(snapshot -> sourceId.equals(snapshot.getId()))
                        .findFirst()
                        .ifPresent(snapshot -> dealDamage(gameData, entry, targetEffect, snapshot,
                                entry.getTriggeringPermanentControllerId()));
            }
        }
    }

    /**
     * Deals the damage from a creature that may have left the battlefield. A creature that has left
     * uses its last known information (CR 608.2h); a creature that attacked was controlled by the
     * active player, so that player is its last known controller.
     */
    private void dealDamage(GameData gameData, StackEntry entry,
                            TargetCreatureDealsDamageToControllerEffect targetEffect, Permanent target,
                            UUID controllerId) {
        if (controllerId == null) {
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, target)) {
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), "'s damage is prevented."));
            return;
        }

        int damage = amountEvaluationService.evaluate(gameData, targetEffect.damage(),
                AmountContext.forStackEntry(entry, target));

        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                target.getCard(),
                controllerId,
                target.getCard().getName() + "'s ability",
                List.of(),
                null,
                target.getId());

        damageEntry.setSourcePermanentSnapshot(new Permanent(target));
        int rawDamage = damage;
        UUID recipientId = targetEffect.recipient() == DamageRecipient.CONTROLLER
                ? entry.getControllerId() : controllerId;
        damageSupport.dealDamageToPlayer(gameData, damageEntry, recipientId, rawDamage);
    }
}

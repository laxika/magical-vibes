package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureDealsPowerDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureDealsPowerDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = entry.getSourcePermanentSnapshot() != null
                && entry.getSourcePermanentSnapshot().getCard().isAura()
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent creature = aura != null && aura.isAttached()
                ? gameQueryService.findPermanentById(gameData, aura.getAttachedTo())
                : null;
        if (creature == null) {
            creature = entry.getAttachedPermanentSnapshot();
        }
        UUID targetId = entry.getTargetId();
        if (creature == null || targetId == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) {
            controllerId = entry.getTriggeringPermanentControllerId();
        }
        if (controllerId == null) {
            return;
        }

        int damage = amountEvaluationService.evaluate(gameData,
                ((EnchantedCreatureDealsPowerDamageToAnyTargetEffect) effect).damageAmount(),
                AmountContext.forStackEntry(entry, aura != null && aura.isAttached() ? aura : null));
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                creature.getCard(),
                controllerId,
                creature.getCard().getName() + "'s ability",
                List.of(),
                targetId,
                creature.getId());
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, damageEntry);
        damageSupport.resolveAnyTargetDamage(gameData, damageEntry, targetId, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}

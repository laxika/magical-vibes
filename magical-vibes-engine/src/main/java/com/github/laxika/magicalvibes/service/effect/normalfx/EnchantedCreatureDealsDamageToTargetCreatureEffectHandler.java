package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureDealsDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureDealsDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EnchantedCreatureDealsDamageToTargetCreatureEffect) effect;
        Permanent liveAura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent aura = liveAura;
        if (aura == null) aura = entry.getSourcePermanentSnapshot();
        if (aura == null) aura = departedSnapshot(gameData, entry.getSourcePermanentId());

        // An Aura still on the battlefield must still be attached. One that has left (it fell off with its
        // creature, CR 704.5m) uses the creature it enchanted when the ability was activated as last known
        // information (CR 608.2h).
        UUID enchantedId;
        if (liveAura != null || aura != null && aura.isAttached()) {
            if (aura == null || !aura.isAttached()) return;
            enchantedId = aura.getAttachedTo();
        } else if (entry.getAttachedPermanentSnapshot() != null
                && gameQueryService.findPermanentById(gameData, entry.getAttachedPermanentSnapshot().getId()) == null) {
            enchantedId = entry.getAttachedPermanentSnapshot().getId();
        } else {
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) enchantedCreature = departedSnapshot(gameData, enchantedId);
        if (enchantedCreature == null && entry.getAttachedPermanentSnapshot() != null
                && enchantedId.equals(entry.getAttachedPermanentSnapshot().getId())) {
            enchantedCreature = entry.getAttachedPermanentSnapshot();
        }
        if (enchantedCreature == null) return;

        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, enchantedCreature));
        int rawDamage = damage;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;
        if (gameQueryService.isDamagePreventable(gameData)
                && (gameQueryService.isPreventedFromDealingDamage(gameData, enchantedCreature)
                || gameQueryService.hasProtectionFromDamageSource(gameData, target, enchantedCreature))) {
            gameLogService.append(gameData, GameLog.cardThen(enchantedCreature.getCard(), "'s damage is prevented."));
            return;
        }
        damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage, enchantedCreature);
    }

    /** The last known state of a permanent that has left the battlefield (CR 608.2h), or null. */
    private static Permanent departedSnapshot(GameData gameData, UUID permanentId) {
        if (permanentId == null) return null;
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(snapshot -> permanentId.equals(snapshot.getId()))
                .findFirst().orElse(null);
    }
}

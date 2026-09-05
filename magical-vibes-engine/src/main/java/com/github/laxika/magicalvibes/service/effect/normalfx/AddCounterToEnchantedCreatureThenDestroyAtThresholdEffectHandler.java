package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AddCounterToEnchantedCreatureThenDestroyAtThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link AddCounterToEnchantedCreatureThenDestroyAtThresholdEffect} (Consuming Ferocity):
 * places the counter on the enchanted creature, then — if it has reached the threshold — has that
 * creature deal damage equal to its power to its controller before destroying it without regeneration.
 */
@Component
@RequiredArgsConstructor
public class AddCounterToEnchantedCreatureThenDestroyAtThresholdEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddCounterToEnchantedCreatureThenDestroyAtThresholdEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AddCounterToEnchantedCreatureThenDestroyAtThresholdEffect) effect;

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        if (aura == null || !aura.isAttached()) {
            return;
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (creature == null) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, creature, e.counterType(), 1);

        if (creature.getCounterCount(e.counterType()) < e.threshold()) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId != null) {
            dealPowerDamageToController(gameData, creature, controllerId);
        }
        destructionSupport.tryDestroyAndLog(gameData, creature, entry.getCard().getName(), true);
    }

    /**
     * The enchanted creature — not the Aura — is the damage source (CR 608.2h), so prevention,
     * protection, lifelink and "deals damage" triggers all key off that creature.
     */
    private void dealPowerDamageToController(GameData gameData, Permanent creature, UUID controllerId) {
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, creature)) {
            gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s damage is prevented."));
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, creature);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                creature.getCard(),
                controllerId,
                creature.getCard().getName() + "'s ability",
                List.of(),
                null,
                creature.getId());

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
        damageSupport.dealDamageToPlayer(gameData, damageEntry, controllerId, rawDamage);
    }
}

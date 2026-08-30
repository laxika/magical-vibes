package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsPowerDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureDealsPowerDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureDealsPowerDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent creature = findEnchantedCreature(gameData, entry);
        if (creature == null) {
            return;
        }

        UUID recipientId = entry.getControllerId();
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (recipientId == null || creatureControllerId == null) {
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, creature);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                creature.getCard(),
                creatureControllerId,
                creature.getCard().getName() + "'s ability",
                List.of(),
                recipientId,
                creature.getId());

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
        damageSupport.dealDamageToPlayer(gameData, damageEntry, recipientId, rawDamage);
        gameOutcomeService.checkWinCondition(gameData);
    }

    private Permanent findEnchantedCreature(GameData gameData, StackEntry entry) {
        if (entry.getTargetId() != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target != null) {
                return target;
            }
        }

        if (entry.getSourcePermanentId() == null) {
            return null;
        }

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || aura.getAttachedTo() == null) {
            return null;
        }
        return gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
    }
}

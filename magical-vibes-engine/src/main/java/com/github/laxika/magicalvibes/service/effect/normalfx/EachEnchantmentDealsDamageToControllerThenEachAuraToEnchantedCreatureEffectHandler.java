package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachEnchantmentDealsDamageToControllerThenEachAuraToEnchantedCreatureEffect;
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
public class EachEnchantmentDealsDamageToControllerThenEachAuraToEnchantedCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachEnchantmentDealsDamageToControllerThenEachAuraToEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachEnchantmentDealsDamageToControllerThenEachAuraToEnchantedCreatureEffect) effect;
        if (e.damage() <= 0) {
            return;
        }

        List<Permanent> enchantments = collectEnchantments(gameData);

        for (Permanent enchantment : enchantments) {
            if (gameQueryService.findPermanentById(gameData, enchantment.getId()) == null) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, enchantment.getId());
            if (controllerId == null) {
                continue;
            }
            StackEntry damageEntry = sourceEntry(enchantment, controllerId);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), damageEntry);
            damageSupport.dealDamageToPlayer(gameData, damageEntry, controllerId, rawDamage);
        }

        // "then" — re-read the battlefield rather than reusing the snapshot above.
        for (Permanent aura : collectEnchantments(gameData)) {
            if (!aura.getCard().isAura() || !aura.isAttached()) {
                continue;
            }
            Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
            if (enchanted == null || !gameQueryService.isCreature(gameData, enchanted)) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, aura.getId());
            if (controllerId == null) {
                continue;
            }
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, aura)) {
                gameLogService.append(gameData, GameLog.cardThen(aura.getCard(), "'s damage is prevented."));
                continue;
            }
            StackEntry damageEntry = sourceEntry(aura, controllerId);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), damageEntry);
            damageSupport.dealCreatureDamage(gameData, damageEntry, enchanted, rawDamage, aura);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    private List<Permanent> collectEnchantments(GameData gameData) {
        List<Permanent> enchantments = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isEnchantment(gameData, permanent)) {
                    enchantments.add(permanent);
                }
            }
        }
        return enchantments;
    }

    /** The enchantment itself is the damage source, not the resolving spell. */
    private StackEntry sourceEntry(Permanent enchantment, UUID controllerId) {
        return new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                enchantment.getCard(),
                controllerId,
                enchantment.getCard().getName() + "'s ability",
                List.of(),
                null,
                enchantment.getId());
    }
}

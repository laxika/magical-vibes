package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedCreatureDealsPowerToAttackedTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EquippedCreatureDealsPowerToAttackedTargetEffect} (Mage Slayer): the equipped
 * creature deals damage equal to its power to the player or planeswalker it's attacking.
 *
 * <p>The trigger's {@code sourcePermanentId} is the Equipment; the equipped creature is found via
 * {@link Permanent#getAttachedTo()} and the recipient via its live {@link Permanent#getAttackTarget()}.
 * Damage is dealt through a source-swapped stack entry whose source is the equipped creature, so its
 * power sets the amount and its keywords (e.g. infect) apply. Mirrors the player/planeswalker split of
 * {@link DealDamageToAttackedTargetEffectHandler}.</p>
 */
@Component
@RequiredArgsConstructor
public class EquippedCreatureDealsPowerToAttackedTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EquippedCreatureDealsPowerToAttackedTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (equipment == null || equipment.getAttachedTo() == null) return;

        Permanent equippedCreature = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (equippedCreature == null) return;

        UUID targetId = equippedCreature.getAttackTarget();
        if (targetId == null) return;

        // Deal the damage as the equipped creature (its power, keywords, and colour are the source's).
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                equippedCreature.getCard(),
                entry.getControllerId(),
                equippedCreature.getCard().getName() + "'s combat damage",
                List.of(),
                null,
                equippedCreature.getId());

        int power = gameQueryService.getPowerBasedDamage(gameData, equippedCreature);
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);

        if (gameData.playerIds.contains(targetId)) {
            if (!damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)) {
                damageSupport.dealDamageToPlayer(gameData, damageEntry, targetId, rawDamage);
            }
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !target.getCard().hasType(CardType.PLANESWALKER)) return;
        Card source = damageEntry.getEffectiveDamageSourceCard();
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)
                || damageSupport.isSourcePermanentPreventedFromDealingDamage(gameData, damageEntry)
                || gameQueryService.hasProtectionFromSource(gameData, target, source)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source, "'s damage is prevented."));
            return;
        }

        int newLoyalty = Math.max(0, target.getCounterCount(CounterType.LOYALTY) - rawDamage);
        target.setCounterCount(CounterType.LOYALTY, newLoyalty);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(source).text(" deals " + rawDamage + " damage to ").card(target.getCard()).text(".").build());
        gameOutcomeService.checkWinCondition(gameData);
    }
}

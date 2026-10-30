package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureDealsPowerDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureDealsPowerDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = entry.getSourcePermanentSnapshot() != null
                && entry.getSourcePermanentSnapshot().getCard().isAura()
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        // An Aura that left the battlefield is detached, but still remembers the creature it enchanted.
        UUID enchantedId = aura == null ? null
                : aura.getAttachedTo() != null ? aura.getAttachedTo() : aura.getLastAttachedTo();
        if (enchantedId == null) {
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, enchantedId);
        boolean creatureGone = false;
        if (creature == null && entry.getAttachedPermanentSnapshot() != null
                && enchantedId.equals(entry.getAttachedPermanentSnapshot().getId())) {
            creature = entry.getAttachedPermanentSnapshot();
            creatureGone = true;
        }
        if (creature == null) {
            creature = gameData.departedPermanentSnapshots.values().stream()
                    .filter(snapshot -> enchantedId.equals(snapshot.getId()))
                    .findFirst().orElse(null);
            creatureGone = creature != null;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (creature == null || target == null) {
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, creature)) {
            gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s damage is prevented."));
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.hasProtectionFromSource(gameData, target, creature)) {
            gameLogService.append(gameData,
                    GameLog.cardTextCard(creature.getCard(), "'s damage to ", target.getCard(), " is prevented."));
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null && creatureGone) {
            controllerId = entry.getTriggeringPermanentControllerId();
        }
        if (controllerId == null && creatureGone) {
            controllerId = gameData.departedPermanentControllerIds.get(creature.getCard().getId());
        }
        if (controllerId == null) {
            return;
        }

        // CR 608.2h: an enchanted creature that has already left the battlefield deals damage equal to the
        // power it had last.
        int power = creatureGone && creature.getLastKnownPower() != null
                ? Math.max(0, creature.getLastKnownPower())
                : gameQueryService.getPowerBasedDamage(gameData, creature);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                creature.getCard(),
                controllerId,
                creature.getCard().getName() + "'s ability",
                List.of(),
                target.getId(),
                creature.getId());
        int rawDamage = power;
        damageSupport.dealCreatureDamage(gameData, damageEntry, target, rawDamage, creature);
    }
}

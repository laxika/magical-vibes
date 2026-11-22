package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link TargetDealsPowerDamageToTargetEffect}: the dealer and the recipient are named by
 * {@link SubjectResolutionService}; the dealer is the damage source. A target dealer must still be on the battlefield
 * and a creature; a dealer that is not a target deals damage as it last existed if it left (CR 608.2h).
 */
@Component
@RequiredArgsConstructor
public class TargetDealsPowerDamageToTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetDealsPowerDamageToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetDealsPowerDamageToTargetEffect) effect;
        if (e.recordExcessDamage()) {
            entry.setEventValue(0);
        }
        switch (e.dealer()) {
            case PermanentSubject.Source ignored -> resolveFromSource(gameData, entry, e);
            case PermanentSubject.AttachedAtTrigger ignored -> resolveFromEnchantedAtTrigger(gameData, entry, e);
            case PermanentSubject.Triggering ignored -> resolveFromTriggering(gameData, entry, e);
            default -> resolveFromTargets(gameData, entry, e);
        }
    }

    /** Each target dealer still on the battlefield and still a creature deals its damage in turn. */
    private void resolveFromTargets(GameData gameData, StackEntry entry, TargetDealsPowerDamageToTargetEffect e) {
        List<UUID> dealerIds = subjectResolutionService.permanentIds(gameData, entry, e, e.dealer());
        Permanent target = subjectResolutionService.permanent(gameData, entry, e, e.recipient());
        if (dealerIds.isEmpty() || target == null) {
            return; // Optional target not chosen ("up to one"), or gone — no damage is dealt
        }

        for (UUID dealerId : dealerIds) {
            Permanent biter = gameQueryService.findPermanentById(gameData, dealerId);
            // A noncreature permanent has no power (CR 208.3) and a source dealing 0 damage deals none (CR 120.8).
            if (biter == null || !gameQueryService.isCreature(gameData, biter)) {
                continue;
            }

            // The biting creature deals the damage — check if it is prevented from dealing damage
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, biter)) {
                gameLogService.append(gameData, GameLog.cardThen(biter.getCard(), "'s damage is prevented."));
                continue;
            }

            // Use the biting creature's color for protection checks (not the spell's color)
            if (gameQueryService.isDamagePreventable(gameData)
                    && !gameQueryService.damageCantBePreventedFromSource(gameData, biter)
                    && gameQueryService.hasProtectionFromSource(gameData, target, biter)) {
                gameLogService.append(gameData, GameLog.cardThen(biter.getCard(), "'s damage is prevented."));
                continue;
            }

            UUID sourceController = gameQueryService.findPermanentController(gameData, biter.getId());
            if (sourceController == null) {
                continue;
            }
            StackEntry damageEntry = StackEntry.carrier(biter.getCard(), sourceController, FaceDownVisibility.publicName(gameData, biter),
                    List.of(), null, biter.getId());
            damageEntry.setSourcePermanentSnapshot(new Permanent(biter));
            int power = gameQueryService.getPowerBasedDamage(gameData, biter);
            int rawDamage = SaturatingMath.multiply(power, e.powerMultiplier());
            int markedDamageBefore = e.recordExcessDamage() ? target.getMarkedDamage() : 0;
            boolean deathtouch = e.recordExcessDamage()
                    && gameQueryService.sourceHasKeyword(gameData, entry, biter, Keyword.DEATHTOUCH);
            int damageDealt = damageSupport.dealCreatureDamage(gameData, damageEntry, target, rawDamage, biter);
            if (e.recordExcessDamage()) {
                entry.setEventValue(damageSupport.computeExcessDamageToCreature(
                        gameData, target, damageDealt, markedDamageBefore, deathtouch));
            }
        }
    }

    /**
     * "This creature deals damage equal to its power to target creature": the ability's own entry deals it; a source
     * that already left uses the power it had last, static boosts included (CR 608.2h), not the snapshot's base
     * values.
     */
    private void resolveFromSource(GameData gameData, StackEntry entry, TargetDealsPowerDamageToTargetEffect e) {
        List<UUID> sourceIds = subjectResolutionService.permanentIds(gameData, entry, e, e.dealer());
        Permanent source = sourceIds.isEmpty()
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, sourceIds.getFirst());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        Permanent target = subjectResolutionService.permanent(gameData, entry, e, e.recipient());
        if (source == null || target == null) {
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.hasProtectionFromSource(gameData, target, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }

        boolean sourceGone = gameQueryService.findPermanentById(gameData, source.getId()) == null;
        int power = sourceGone && source.getLastKnownPower() != null
                ? Math.max(0, source.getLastKnownPower())
                : gameQueryService.getPowerBasedDamage(gameData, source);
        damageSupport.dealCreatureDamage(gameData, entry, target, SaturatingMath.multiply(power, e.powerMultiplier()),
                source);
    }

    /**
     * The creature the source Aura enchanted as the ability triggered deals the damage; one that has left deals it as
     * it last existed, with the power it had last and its last controller (CR 608.2h).
     */
    private void resolveFromEnchantedAtTrigger(GameData gameData, StackEntry entry,
                                               TargetDealsPowerDamageToTargetEffect e) {
        List<UUID> enchantedIds = subjectResolutionService.permanentIds(gameData, entry, e, e.dealer());
        if (enchantedIds.isEmpty()) {
            return;
        }
        UUID enchantedId = enchantedIds.getFirst();

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
        Permanent target = subjectResolutionService.permanent(gameData, entry, e, e.recipient());
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

        int power = creatureGone && creature.getLastKnownPower() != null
                ? Math.max(0, creature.getLastKnownPower())
                : gameQueryService.getPowerBasedDamage(gameData, creature);
        StackEntry damageEntry = StackEntry.carrier(creature.getCard(),
                controllerId,
                FaceDownVisibility.publicName(gameData, creature) + "'s ability",
                List.of(),
                target.getId(),
                creature.getId());
        damageSupport.dealCreatureDamage(gameData, damageEntry, target,
                SaturatingMath.multiply(power, e.powerMultiplier()), creature);
    }

    /**
     * The creature the trigger event was about deals the damage; one that has left deals it as it was when the
     * ability triggered (its recorded snapshot, controller and power).
     */
    private void resolveFromTriggering(GameData gameData, StackEntry entry, TargetDealsPowerDamageToTargetEffect e) {
        if (subjectResolutionService.permanentIds(gameData, entry, e, e.dealer()).isEmpty()) {
            return;
        }
        Permanent creature = subjectResolutionService.permanent(gameData, entry, e, e.dealer());
        Permanent target = subjectResolutionService.permanent(gameData, entry, e, e.recipient());
        if (target == null) {
            return;
        }

        if (creature == null) {
            Permanent lastKnown = entry.getAttachedPermanentSnapshot();
            UUID lastKnownControllerId = entry.getTriggeringPermanentControllerId();
            if (lastKnown == null || lastKnownControllerId == null
                    || entry.getTriggeringPermanentPowerAtTrigger() == null) {
                return;
            }
            StackEntry lastKnownEntry = StackEntry.carrier(lastKnown.getCard(),
                    lastKnownControllerId,
                    FaceDownVisibility.publicName(gameData, lastKnown) + "'s ability",
                    List.of(),
                    target.getId(),
                    lastKnown.getId());
            damageSupport.dealCreatureDamage(gameData, lastKnownEntry, target, SaturatingMath.multiply(
                    Math.max(0, entry.getTriggeringPermanentPowerAtTrigger()), e.powerMultiplier()));
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

        int power = gameQueryService.getPowerBasedDamage(gameData, creature);
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (creatureControllerId == null) {
            return;
        }
        StackEntry damageEntry = StackEntry.carrier(creature.getCard(),
                creatureControllerId,
                FaceDownVisibility.publicName(gameData, creature) + "'s ability",
                List.of(),
                target.getId(),
                creature.getId());
        damageSupport.dealCreatureDamage(gameData, damageEntry, target,
                SaturatingMath.multiply(power, e.powerMultiplier()), creature);
    }
}

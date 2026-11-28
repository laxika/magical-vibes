package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared "two creatures fight" resolution (CR 701.14a): each of the two creatures deals damage
 * equal to its power to the other. Used by every fight flow — the targeted
 * {@code FightTargetsEffect} spell/ability path and the deferred Guild Feud upkeep flow, which
 * fights two creatures that were just put onto the battlefield.
 */
@Component
@RequiredArgsConstructor
public class FightSupport {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    /** Has {@code first} and {@code second} fight each other; either being gone is a no-op. */
    public void fight(GameData gameData, StackEntry entry, Permanent first, Permanent second) {
        if (first == null || second == null
                || !gameQueryService.isCreature(gameData, first)
                || !gameQueryService.isCreature(gameData, second)) {
            return;
        }
        UUID firstControllerId = gameQueryService.findPermanentController(gameData, first.getId());
        UUID secondControllerId = gameQueryService.findPermanentController(gameData, second.getId());
        gameData.permanentsThatFoughtThisTurn.add(first.getId());
        gameData.permanentsThatFoughtThisTurn.add(second.getId());
        int firstPower = gameQueryService.getPowerBasedDamage(gameData, first);
        int secondPower = gameQueryService.getPowerBasedDamage(gameData, second);
        dealMutualDamage(gameData, entry, first, second, firstPower);
        dealMutualDamage(gameData, entry, second, first, secondPower);
        ActionEvents.fought(gameData, first, firstControllerId);
        ActionEvents.fought(gameData, second, secondControllerId);
    }

    /**
     * Has each of {@code first} fight the creature at the same index of {@code second}, all the fights at the same time
     * ("each of the fights happens at the same time", Ezuri's Predation): every creature's power is read before any
     * damage is dealt, and the damage is dealt at once ({@link DamageSupport#dealAtOnce}). A pair whose creature is gone
     * or no creature doesn't fight (CR 701.14b).
     */
    public void fightTogether(GameData gameData, StackEntry entry, List<Permanent> first, List<Permanent> second) {
        record Fight(Permanent one, Permanent other, UUID oneControllerId, UUID otherControllerId, int onePower,
                     int otherPower) {
        }
        List<Fight> fights = new ArrayList<>();
        for (int i = 0; i < Math.min(first.size(), second.size()); i++) {
            Permanent one = first.get(i);
            Permanent other = second.get(i);
            if (one == null || other == null || !gameQueryService.isCreature(gameData, one)
                    || !gameQueryService.isCreature(gameData, other)) {
                continue;
            }
            fights.add(new Fight(one, other, gameQueryService.findPermanentController(gameData, one.getId()),
                    gameQueryService.findPermanentController(gameData, other.getId()),
                    gameQueryService.getPowerBasedDamage(gameData, one),
                    gameQueryService.getPowerBasedDamage(gameData, other)));
        }
        if (fights.isEmpty()) {
            return;
        }
        for (Fight fight : fights) {
            gameData.permanentsThatFoughtThisTurn.add(fight.one().getId());
            gameData.permanentsThatFoughtThisTurn.add(fight.other().getId());
        }
        damageSupport.dealAtOnce(gameData, () -> {
            for (Fight fight : fights) {
                dealMutualDamage(gameData, entry, fight.one(), fight.other(), fight.onePower());
                dealMutualDamage(gameData, entry, fight.other(), fight.one(), fight.otherPower());
            }
        });
        for (Fight fight : fights) {
            ActionEvents.fought(gameData, fight.one(), fight.oneControllerId());
            ActionEvents.fought(gameData, fight.other(), fight.otherControllerId());
        }
    }

    /** Has {@code first} and {@code second} deal damage equal to their power to each other, without fighting. */
    public void dealPowerDamageToEachOther(GameData gameData, StackEntry entry, Permanent first, Permanent second) {
        if (first == null || second == null) {
            return;
        }
        int firstPower = gameQueryService.getPowerBasedDamage(gameData, first);
        int secondPower = gameQueryService.getPowerBasedDamage(gameData, second);
        dealMutualDamage(gameData, entry, first, second, firstPower);
        dealMutualDamage(gameData, entry, second, first, secondPower);
    }

    public void dealToughnessDamageToEachOther(GameData gameData, StackEntry entry,
                                               Permanent first, Permanent second) {
        if (first == null || second == null) {
            return;
        }
        // Both amounts are read before either deals damage, so an infect source's -1/-1 counters
        // on one creature do not shrink the damage the other creature deals (simultaneous damage).
        int firstDamage = Math.max(0, gameQueryService.getEffectiveToughness(gameData, first));
        int secondDamage = Math.max(0, gameQueryService.getEffectiveToughness(gameData, second));
        dealMutualDamage(gameData, entry, first, second, firstDamage);
        dealMutualDamage(gameData, entry, second, first, secondDamage);
    }

    private void dealMutualDamage(GameData gameData, StackEntry entry, Permanent source,
                                  Permanent recipient, boolean usePower) {
        int baseDamage = usePower
                ? gameQueryService.getPowerBasedDamage(gameData, source)
                : Math.max(0, gameQueryService.getEffectiveToughness(gameData, source));
        dealMutualDamage(gameData, entry, source, recipient, baseDamage);
    }

    private void dealMutualDamage(GameData gameData, StackEntry entry, Permanent source,
                                   Permanent recipient, int baseDamage) {
        StackEntry damageEntry = StackEntry.carrier(source.getCard(), gameQueryService.findPermanentController(gameData, source.getId()),
                FaceDownVisibility.publicName(gameData, source) + " deals fight damage", java.util.List.of(),
                recipient.getId(), source.getId());
        damageEntry.setNonTargeting(true);
        damageEntry.setSourcePermanentSnapshot(new Permanent(source));
        int damage = baseDamage;
        int markedDamageBefore = recipient.getMarkedDamage();
        int damageDealt = damageSupport.dealCreatureDamage(gameData, damageEntry, recipient, damage, source);
        UUID recipientControllerId = gameQueryService.findPermanentController(gameData, recipient.getId());
        if (recipientControllerId != null
                && !recipientControllerId.equals(entry.getControllerId())
                && referencesExcessDamage(entry)) {
            boolean deathtouch = gameQueryService.sourceHasKeyword(
                    gameData, entry, source, Keyword.DEATHTOUCH);
            entry.setEventValue(damageSupport.computeExcessDamageToCreature(
                    gameData, recipient, damageDealt, markedDamageBefore, deathtouch));
        }
    }

    private boolean referencesExcessDamage(StackEntry entry) {
        return entry.getEffectsToResolve().stream()
                .anyMatch(this::referencesExcessDamage);
    }

    private boolean referencesExcessDamage(CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof EventValueAtLeast) {
            return true;
        }
        return effect instanceof AwardPersistentManaEffect mana
                && amountEvaluationService.referencesEventValue(mana.amount());
    }
}

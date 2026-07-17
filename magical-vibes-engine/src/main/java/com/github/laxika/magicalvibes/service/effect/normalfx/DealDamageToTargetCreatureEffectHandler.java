package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetCreatureEffect) effect;

        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluated = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));

        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluated, entry);

        // Multi-target: deal damage to each valid creature target (e.g. Dual Shot targeting two creatures,
        // Fire Shrine Keeper targeting up to two creatures via activated ability).
        // Enters this path when targetIds has 2+ entries, or when targetIds has 1 entry but targetId is null
        // (multi-target ability with a single target selected). Skips non-creature UUIDs (e.g. player
        // targets from kicked spells) since those are handled by other effects like a group-aimed DealDamageToAnyTargetEffect.
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()
                && (entry.getTargetIds().size() > 1 || entry.getTargetId() == null)) {
            for (UUID targetId : entry.getTargetIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) continue;
                if (e.unpreventable()) {
                    damageSupport.dealCreatureDamageUnpreventable(gameData, entry, target, damage);
                } else if (!damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
                    damageSupport.dealCreatureDamage(gameData, entry, target, damage);
                }
            }
            return;
        }

        // Excess-damage tracking for companion effects (e.g. Archaic's Agony exiles cards equal to
        // the excess damage dealt to the target). The excess is stored on the entry's event value,
        // which a later EventValue amount reads back — so only snapshot it when such an effect asks.
        boolean tracksExcess = entry.getEffectsToResolve().stream()
                .anyMatch(ef -> ef instanceof ExileTopCardsMayPlayUntilNextTurnEffect ex
                        && amountEvaluationService.referencesEventValue(ex.count()));
        if (tracksExcess) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null || damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
                entry.setEventValue(0);
            } else {
                int markedBefore = target.getMarkedDamage();
                boolean deathtouch = gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.DEATHTOUCH);
                entry.setEventValue(damageSupport.computeExcessDamageToCreature(
                        gameData, target, damage, markedBefore, deathtouch));
            }
        }

        // Single-target
        if (e.unpreventable()) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null) return;
            damageSupport.dealCreatureDamageUnpreventable(gameData, entry, target, damage);
        } else {
            damageSupport.resolveCreatureTargetDamage(gameData, entry, damage);
        }

    }
}

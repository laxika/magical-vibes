package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToTargetCreatureEffect}. A target is dealt the damage through the permanent-damage path the
 * any-target record uses ({@link DamageSupport#resolveAnyTargetDamage}: every prevention is an option of the target's
 * damage pool), with this record's multi-target loop, unpreventable damage and excess-damage tracking. Any other subject names one permanent through
 * {@link SubjectResolutionService}, and each keeps the prevention it always had: {@code source()} through the
 * any-target path, {@code triggering()} with the targeted-creature prevention check and its log line, the Aura
 * subjects with the Aura, as it is or last existed, as the damage's source and only the damage pipeline's prevention;
 * {@code thisWay(kind)} names each permanent the step recorded, with the targeted-creature prevention check.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The damage this step deals is added to its result as each damage event is dealt (DamageSupport).
        entry.recordStep(StepKind.DEAL_DAMAGE, StepOutcome.NONE);
        dealDamage(gameData, entry, (DealDamageToTargetCreatureEffect) effect);
    }

    private void dealDamage(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect e) {
        CardEffect effect = e;
        switch (e.subject()) {
            case PermanentSubject.Source ignored -> {
                damageSource(gameData, entry, e);
                return;
            }
            case PermanentSubject.Triggering ignored -> {
                damageTriggeringPermanent(gameData, entry, e);
                return;
            }
            case PermanentSubject.Attached ignored -> {
                damageAuraHost(gameData, entry, e);
                return;
            }
            case PermanentSubject.AttachedAtTrigger ignored -> {
                damageAuraHost(gameData, entry, e);
                return;
            }
            case PermanentSubject.ThisWay ignored -> {
                damageNamedThisWay(gameData, entry, e);
                return;
            }
            default -> {
            }
        }
        if (e.damageSource() == DamageSource.TRIGGERING_PERMANENT) {
            // The permanent the trigger event was about deals the damage (CR 608.2h), through an entry it is the
            // source of; the targets stay the resolving entry's.
            StackEntry dealing = damageSupport.entryDealtByTriggeringPermanent(gameData, entry);
            if (dealing == null) {
                return;
            }
            dealing.setTriggeringPermanentId(entry.getTriggeringPermanentId());
            int amount = amountEvaluationService.evaluate(gameData, e.damage(),
                    AmountContext.forStackEntry(entry, dealing.getSourcePermanentSnapshot()));
            damageSupport.resolveCreatureTargetDamage(gameData, dealing, amount);
            entry.addToStep(StepKind.DEAL_DAMAGE, dealing.getResolutionResults().outcome(StepKind.DEAL_DAMAGE));
            return;
        }

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

        int damage = evaluated;

        // Multi-target: deal damage to each valid creature target (e.g. Dual Shot targeting two creatures,
        // Fire Shrine Keeper targeting up to two creatures via activated ability).
        // Enters this path when targetIds has 2+ entries, or when targetIds has 1 entry but targetId is null
        // (multi-target ability with a single target selected). Skips non-creature UUIDs (e.g. player
        // targets from kicked spells) since those are handled by other effects like a group-aimed DealDamageToAnyTargetEffect.
        // When this effect is bound to its own target group (e.g. a "choose one or more" modal spell
        // where two chosen modes each damage a different creature), targetsForEffect narrows the flat
        // list to that group; unbound effects get the whole list, as before.
        List<UUID> effectTargets = entry.targetsForEffect(e);
        if (effectTargets != null && !effectTargets.isEmpty()
                && (effectTargets.size() > 1 || entry.getTargetId() == null)) {
            for (UUID targetId : effectTargets) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) continue;
                if (e.unpreventable()) {
                    damageSupport.dealCreatureDamageUnpreventable(gameData, entry, target, damage);
                } else {
                    damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, damage, false);
                }
            }
            return;
        }

        // Excess-damage tracking for companion effects (e.g. Archaic's Agony exiles cards equal to
        // the excess damage dealt to the target). The excess is stored on the entry's event value,
        // which a later EventValue amount reads back — so only snapshot it when such an effect asks.
        boolean tracksExcess = !amountEvaluationService.referencesEventValue(e.damage())
                && entry.getEffectsToResolve().stream().anyMatch(this::referencesExcessDamage);
        Permanent target = tracksExcess
                ? gameQueryService.findPermanentById(gameData, entry.getTargetId())
                : null;
        int markedBefore = target == null ? 0 : target.getMarkedDamage();
        int loyaltyBefore = target == null ? 0 : target.getCounterCount(CounterType.LOYALTY);
        boolean deathtouch = tracksExcess
                && gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.DEATHTOUCH);

        // Single-target
        int damageDealt;
        if (e.unpreventable()) {
            if (target == null) {
                target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            }
            if (target == null) return;
            damageDealt = damageSupport.dealCreatureDamageUnpreventable(gameData, entry, target, damage);
        } else {
            damageDealt = damageSupport.resolveCreatureTargetDamage(gameData, entry, damage);
        }

        if (tracksExcess) {
            entry.setEventValue(target == null ? 0 : damageSupport.computeExcessDamageToTargetedPermanent(
                    gameData, target, damageDealt, markedBefore, loyaltyBefore, deathtouch));
        }

    }

    /** "And X damage to itself": the live source permanent, through the any-target path. */
    private void damageSource(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect e) {
        Permanent source = subjectResolutionService.permanent(gameData, entry, e, e.subject());
        if (source == null) return;

        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        damageSupport.resolveAnyTargetDamage(gameData, entry, source.getId(), damage, false);
    }

    /**
     * "Deals damage to it": the permanent the trigger event was about, as it is when the ability resolves, if it is
     * still a creature, planeswalker or battle (CR 120.1); protection from the source and "prevent all damage this
     * would deal" apply as to a targeted creature.
     */
    private void damageTriggeringPermanent(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect e) {
        Permanent triggering = subjectResolutionService.permanent(gameData, entry, e, e.subject());
        if (triggering == null || !gameQueryService.isCreature(gameData, triggering)
                && !gameQueryService.isPlaneswalker(gameData, triggering)
                && !triggering.getCard().hasType(CardType.BATTLE)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        if (damageSupport.isDamagePreventedForCreature(gameData, entry, triggering)) {
            return;
        }
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" deals " + damage + " damage to ")
                .card(triggering.getCard()).text(".").build());
        damageSupport.dealCreatureDamage(gameData, entry, triggering, damage);
    }

    /**
     * "Deals 2 damage to [the creature chosen]": each permanent an earlier step of this resolution named, as it is now,
     * if it is still a creature; protection from the source and "prevent all damage this would deal" apply as to a
     * targeted creature.
     */
    private void damageNamedThisWay(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect e) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        for (Permanent creature : subjectResolutionService.permanents(gameData, entry, e, e.subject())) {
            if (!gameQueryService.isCreature(gameData, creature)
                    || damageSupport.isDamagePreventedForCreature(gameData, entry, creature)) {
                continue;
            }
            damageSupport.dealCreatureDamage(gameData, entry, creature, damage);
        }
    }

    /**
     * An Aura's damage to its creature: the Aura, as it is or last existed (CR 608.2h), must be attached and deals the
     * damage to the creature the subject names.
     */
    private void damageAuraHost(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect e) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        if (aura == null || !aura.isAttached()) {
            return;
        }
        Permanent creature = subjectResolutionService.permanent(gameData, entry, e, e.subject());
        if (creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, aura));
        damageSupport.dealCreatureDamage(gameData, entry, creature, damage, aura);
    }

    private boolean referencesExcessDamage(CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional) {
            return conditional.condition() instanceof EventValueAtLeast
                    || referencesExcessDamage(conditional.wrapped());
        }
        if (effect instanceof ExileTopCardMayPlayThisTurnEffect exile) {
            return amountEvaluationService.referencesEventValue(exile.count());
        }
        if (effect instanceof BoostAllOwnCreaturesEffect boost) {
            return amountEvaluationService.referencesEventValue(boost.powerBoost())
                    || amountEvaluationService.referencesEventValue(boost.toughnessBoost());
        }
        if (effect instanceof DealDamageToPlayersEffect playerDamage) {
            return playerDamage.player().equals(Subjects.controllerOf(Subjects.target()))
                    && amountEvaluationService.referencesEventValue(playerDamage.amount());
        }
        if (effect instanceof GainLifeEffect gainLife) {
            return amountEvaluationService.referencesEventValue(gainLife.amount());
        }
        return effect instanceof CreateTokenEffect createToken
                && amountEvaluationService.referencesEventValue(createToken.amount());
    }
}

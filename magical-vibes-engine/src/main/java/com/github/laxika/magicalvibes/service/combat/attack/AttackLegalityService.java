package com.github.laxika.magicalvibes.service.combat.attack;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatAttackTarget;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.AttackOrBlockRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithPowerGreaterThanAmountCantAttackEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughHasteUnlessEnteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCanAttackAsThoughHasteEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.NoDefenderAttackPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantAttackIfCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatHelper;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Declare-attackers legality (CR 508.1): whether a creature may be declared as an attacker at
 * all, which players and planeswalkers it may be declared against, and how many "attacks each
 * combat if able" requirements it carries. Owns defender, summoning sickness and the attack
 * locks, the "can't attack unless …" conditions, the board-wide and controller-scoped attack
 * restrictions, and the defender-scoped ones ("creatures without flying can't attack you").
 *
 * <p>This service answers legality questions only — declaring attackers, paying the attack tax,
 * tapping, and the resulting triggers belong to {@link CombatAttackService}, as do the
 * declaration-time restrictions that constrain the attacking group as a whole ("can't attack
 * alone", banding), which no single creature can be judged against.
 *
 * <p>Like {@link GameQueryService}, which it reads characteristics from, this service never
 * mutates game state. It is the attack-side mirror of
 * {@code com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService}.
 */
@Component
@RequiredArgsConstructor
public class AttackLegalityService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    /**
     * Returns {@code true} if the given creature permanent can legally be declared as an attacker,
     * independent of which target it would attack and of what the rest of the attacking group looks
     * like. {@code controllerId} is the player declaring the attack, used to evaluate the creature's
     * "can't attack unless …" conditions from its controller's perspective.
     */
    public boolean canAttack(GameData gameData, Permanent creature, UUID controllerId) {
        if (!gameQueryService.isCreature(gameData, creature)) return false;
        if (creature.isTapped()) return false;
        if (creature.isCantAttackThisTurn()) return false;
        if (gameQueryService.isLockedFromAttacking(gameData, creature.getId())) return false;
        if (gameQueryService.isPeaceTalksActive(gameData)) return false;
        if (isRestrictedByOtherCreaturesCantAttack(gameData, creature)) return false;
        if (isOutsideChosenAttackers(gameData, creature)) return false;
        if (creature.isSummoningSick() && !gameQueryService.hasKeyword(gameData, creature, Keyword.HASTE)
                && !gameQueryService.hasAuraWithEffect(gameData, creature, EnchantedCreatureCanAttackAsThoughHasteEffect.class)
                && !canAttackAsThoughHasteFromOwnStatic(gameData, creature)) return false;
        if (gameQueryService.hasKeyword(gameData, creature, Keyword.DEFENDER)
                && !canAttackDespiteDefender(gameData, creature)) return false;
        if (gameQueryService.hasAuraWithEffect(gameData, creature,
                e -> e instanceof EnchantedCreatureCantAttackOrBlockEffect r && r.preventsAttacking())) return false;
        if (isCantAttackUnlessConditionUnmet(gameData, creature, controllerId)) return false;
        if (isCantAttackDueToGlobalRestriction(gameData, creature)) return false;
        return true;
    }

    /**
     * Chaos Lord: "can attack as though it had haste unless it entered this turn". The permission is
     * printed on the creature itself, so it survives a change of control, but it is switched off for
     * the turn the permanent entered the battlefield.
     */
    private boolean canAttackAsThoughHasteFromOwnStatic(GameData gameData, Permanent creature) {
        boolean hasPermission = creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CanAttackAsThoughHasteUnlessEnteredThisTurnEffect.class::isInstance);
        if (!hasPermission) return false;
        return gameData.permanentsEnteredBattlefieldThisTurn.values().stream()
                .flatMap(List::stream)
                .noneMatch(card -> card.getId().equals(creature.getCard().getId()));
    }

    /**
     * Returns {@code true} if the given creature can attack despite having defender.
     * Checks for {@link CanAttackAsThoughNoDefenderEffect} in static effects, including
     * those wrapped in a {@link ConditionalEffect} (e.g. metalcraft).
     */
    private boolean canAttackDespiteDefender(GameData gameData, Permanent creature) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof NoDefenderAttackPermissionEffect permission
                    && permission.grantsCarrierAttackAsThoughNoDefender()) {
                return true;
            }
            if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof NoDefenderAttackPermissionEffect permission
                    && permission.grantsCarrierAttackAsThoughNoDefender()) {
                if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(creature, controllerId))) {
                    return true;
                }
            }
        }
        // An Aura attached to this creature that grants the permission (e.g. Animate Wall).
        if (gameQueryService.hasAuraWithEffect(gameData, creature, CanAttackAsThoughNoDefenderEffect.class)) {
            return true;
        }
        // Until-end-of-turn grants from a resolved activated ability (e.g. Wall of Wonder),
        // stored as floating effects affecting this creature.
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (floating.effect() instanceof NoDefenderAttackPermissionEffect permission
                        && permission.grantsCarrierAttackAsThoughNoDefender()
                        && creature.getId().equals(floating.affectedPermanentId())) {
                    return true;
                }
            }
        }
        // Global grants: any permanent (any controller) whose STATIC effects let matching
        // creatures attack despite defender (e.g. Rolling Stones for Wall creatures).
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent grantor : bf) {
                for (CardEffect effect : grantor.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof NoDefenderAttackPermissionEffect grant
                            && grant.noDefenderAttackMatcher() != null
                            && predicateEvaluationService.matchesPermanentPredicate(
                                    creature,
                                    grant.noDefenderAttackMatcher(),
                                    FilterContext.of(gameData)
                                            .withSourceCardId(grantor.getOriginalCard().getId())
                                            .withSourceControllerId(
                                                    gameQueryService.findPermanentController(gameData, grantor.getId())))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Evaluates the creature's {@link CantAttackUnlessEffect} restrictions (CR 508.1a): the
     * creature can't attack while any attached condition is unmet. Each restriction's condition
     * (controller controls a permanent, defending player poisoned, N Islands on the battlefield, …)
     * is routed through {@link ConditionEvaluationService} with the attacker as source.
     */
    private boolean isCantAttackUnlessConditionUnmet(GameData gameData, Permanent creature, UUID controllerId) {
        ConditionContext ctx = null;
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            Condition condition = null;
            if (effect instanceof CantAttackUnlessEffect restriction) {
                condition = restriction.condition();
            } else if (effect instanceof AttackOrBlockRestrictionEffect restriction) {
                condition = restriction.cantAttackOrBlockUnless();
            }
            if (condition != null) {
                if (ctx == null) {
                    ctx = ConditionContext.forPermanent(creature, controllerId);
                }
                if (!conditionEvaluationService.isMet(gameData, condition, ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Defender-scoped attack legality (CR 508.1a): the attacked player controls a permanent with a
     * {@link CreaturesCantAttackControllerUnlessPredicateEffect}, and the attacker does not match its
     * exemption predicate (e.g. Form of the Dragon's "Creatures without flying can't attack you").
     * When the attack is aimed at a planeswalker, only restrictions whose {@code protectsPlaneswalkers}
     * flag is set apply (Sandwurm Convergence — "can't attack you or planeswalkers you control").
     */
    public boolean canAttackDefender(GameData gameData, Permanent attacker, UUID targetId) {
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        // The protected player is the attacked player, or the controller of the attacked planeswalker.
        UUID protectedPlayerId = targetIsPlayer ? targetId
                : gameQueryService.findPermanentController(gameData, targetId);
        if (protectedPlayerId == null) return true;
        // Restrictions come from static abilities of the protected player's permanents (Form of the
        // Dragon, Sandwurm Convergence) and from player-scoped floating effects (Island Sanctuary's
        // "until your next turn" shield, which persists independently of its source permanent).
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(protectedPlayerId);
        if (defenderBattlefield != null) {
            for (Permanent source : defenderBattlefield) {
                FilterContext context = FilterContext.of(gameData)
                        .withSourceCardId(source.getCard().getId())
                        .withSourceControllerId(protectedPlayerId);
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CreaturesCantAttackControllerUnlessPredicateEffect restriction
                            && (targetIsPlayer || restriction.protectsPlaneswalkers())
                            && !predicateEvaluationService.matchesPermanentPredicate(
                                    attacker, restriction.exemptionPredicate(), context)) {
                        return false;
                    }
                }
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect fe : gameData.floatingEffects) {
                if (protectedPlayerId.equals(fe.affectedPlayerId())) {
                    CardEffect effect = fe.effect();
                    if (effect instanceof CreaturesCantAttackControllerUnlessPredicateEffect restriction
                            && (targetIsPlayer || restriction.protectsPlaneswalkers())
                            && !predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, attacker, restriction.exemptionPredicate())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Intimidation Bolt lock ("Other creatures can't attack this turn"). While
     * {@code gameData.otherCreaturesCantAttackExemptCreatureIds} holds any exemptions, a creature may
     * attack only if its ID equals every one of them — i.e. it is the creature every Intimidation Bolt
     * resolved this turn targeted. Evaluated at declaration time, so it also bars creatures that entered
     * after the spell resolved. Empty list = no restriction.
     */
    private boolean isRestrictedByOtherCreaturesCantAttack(GameData gameData, Permanent creature) {
        List<UUID> exemptions = gameData.otherCreaturesCantAttackExemptCreatureIds;
        if (exemptions.isEmpty()) {
            return false;
        }
        synchronized (exemptions) {
            for (UUID exemptId : exemptions) {
                if (!creature.getId().equals(exemptId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Attack restrictions that allow only a recorded set of creatures. An empty choice bars
     * everyone, and creatures that entered after the choice are barred too. No entries means no
     * restriction.
     */
    private boolean isOutsideChosenAttackers(GameData gameData, Permanent creature) {
        if (gameData.chosenAttackersThisTurn.isEmpty() && gameData.attackableCreaturesThisTurn.isEmpty()) {
            return false;
        }
        for (Set<UUID> chosen : gameData.chosenAttackersThisTurn.values()) {
            if (!chosen.contains(creature.getId())) {
                return true;
            }
        }
        for (Set<UUID> chosen : gameData.attackableCreaturesThisTurn.values()) {
            if (!chosen.contains(creature.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isCantAttackDueToGlobalRestriction(GameData gameData, Permanent creature) {
        boolean[] restricted = {false};
        UUID creatureController = gameData.findControllerOf(creature);
        gameData.forEachPermanent((playerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CreaturesCantAttackUnlessPredicateEffect restriction) {
                    if (!predicateEvaluationService.matchesPermanentPredicate(gameData, creature, restriction.exemptionPredicate())) {
                        restricted[0] = true;
                    }
                } else if (effect instanceof CreaturesWithPowerGreaterThanAmountCantAttackEffect restriction) {
                    int threshold = amountEvaluationService.evaluate(gameData, restriction.amount(),
                            AmountContext.forStaticEffect(permanent, playerId));
                    if (gameQueryService.getEffectivePower(gameData, creature) > threshold) {
                        restricted[0] = true;
                    }
                } else if (effect instanceof AttackOrBlockRestrictionEffect restriction
                        && restriction.globallyCantAttackOrBlock() != null) {
                    FilterContext context = FilterContext.of(gameData)
                            .withSourceControllerId(playerId)
                            .withSourceCardId(permanent.getOriginalCard().getId());
                    if (predicateEvaluationService.matchesPermanentPredicate(creature, restriction.globallyCantAttackOrBlock(), context)) {
                        restricted[0] = true;
                    }
                } else if (effect instanceof CreaturesCantAttackUnlessSacrificeEffect restriction) {
                    // Flooded Woodlands: matching creatures can't be declared at all unless their
                    // controller controls enough permanents to pay the sacrifice for this one attacker.
                    // The whole-declaration total is checked in CombatAttackService.declareAttackers.
                    if (predicateEvaluationService.matchesPermanentPredicate(gameData, creature, restriction.attackerPredicate())
                            && countMatching(gameData, creatureController, restriction.sacrificeFilter())
                            < restriction.countPerAttacker()) {
                        restricted[0] = true;
                    }
                } else if (effect instanceof ControlledCreaturesCantAttackUnlessPredicateEffect restriction) {
                    if (playerId.equals(creatureController)
                            && !predicateEvaluationService.matchesPermanentPredicate(gameData, creature, restriction.exemptionPredicate())) {
                        restricted[0] = true;
                    }
                }
            }
        });
        return restricted[0];
    }

    /**
     * Counts the permanents the given player controls that match {@code filter}. Used to check that a
     * sacrifice-to-attack cost can be paid.
     */
    private int countMatching(GameData gameData, UUID playerId, PermanentPredicate filter) {
        List<Permanent> battlefield = playerId == null ? null : gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns true if the player is globally prevented from attacking (e.g. Angelic Arbiter:
     * "Each opponent who cast a spell this turn can't attack with creatures").
     */
    public boolean isPlayerPreventedFromAttacking(GameData gameData, UUID playerId) {
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        if (spellsCast == 0) return false;

        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsCantAttackIfCastSpellThisTurnEffect) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Counts the "attacks each combat if able" requirements the given creature carries (CR 508.1d):
     * its own static and transient ones (including a {@link ConditionalEffect}-wrapped self
     * requirement, e.g. Marauding Maulhorn's "unless you control …"), those granted by Auras attached
     * to it or cursing its controller, the taunt forced by an attackable taunter, and board-wide
     * requirements matching it.
     * The count — not a boolean — is what lets a declaration be checked for satisfying as many
     * requirements as possible.
     */
    public int getMustAttackRequirementCount(GameData gameData, Permanent creature) {
        int[] count = {0};
        UUID selfControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof MustAttackEffect mustAttack && mustAttack.scope() == null) {
                count[0]++;
            } else if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof MustAttackEffect
                    && selfControllerId != null
                    && conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(creature, selfControllerId))) {
                count[0]++;
            }
        }

        UUID creatureControllerId = gameData.findControllerOf(creature);

        // Check for transient "must attack this turn" flag (e.g. Alluring Siren). When the flag names
        // a specific thing to attack (a planeswalker for Gideon, Battle-Forged's +2) the requirement
        // lapses once that permanent is no longer a legal attack target.
        if ((creature.isMustAttackThisTurn() || creature.isMustAttackThisCombat())
                && (creature.getMustAttackTargetId() == null
                        || gameData.playerIds.contains(creature.getMustAttackTargetId())
                        || getValidAttackTargetIds(gameData, creatureControllerId)
                                .contains(creature.getMustAttackTargetId()))) {
            count[0]++;
        }

        // Taunt: every creature the affected player controls must attack the taunter if able.
        UUID taunter = gameData.tauntedThisTurn.get(creatureControllerId);
        if (taunter != null && getValidAttackTargetIds(gameData, creatureControllerId).contains(taunter)) {
            count[0]++;
        }

        // Oracle en-Vec: a creature its controller chose last turn attacks this turn if able.
        for (Set<UUID> chosen : gameData.chosenAttackersThisTurn.values()) {
            if (chosen.contains(creature.getId())) {
                count[0]++;
            }
        }

        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttached()
                    && permanent.getAttachedTo().equals(creature.getId())) {
                count[0] += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(e -> e instanceof MustAttackEffect mae && mae.scope() == null)
                        .count();
            }
            // "Creatures you control attack each combat if able" (e.g. Hellraiser Goblin)
            if (creatureControllerId != null && creatureControllerId.equals(playerId)) {
                count[0] += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(e -> e instanceof MustAttackEffect mae
                                && (mae.scope() == GrantScope.ALL_OWN_CREATURES
                                        || (mae.scope() == GrantScope.OWN_CREATURES
                                                && !permanent.getId().equals(creature.getId()))))
                        .count();
            }
            // Check for curses on the creature's controller (e.g. Curse of the Nightly Hunt)
            if (permanent.isAttached()
                    && permanent.getAttachedTo().equals(creatureControllerId)) {
                count[0] += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(e -> e instanceof MustAttackEffect mae
                                && mae.scope() == GrantScope.ENCHANTED_PLAYER_CREATURES)
                        .count();
            }
            // Global "matching creatures attack each combat if able" (e.g. Goblin Assault). The
            // source ids let the matcher be source-relative ("other Goblin creatures you control",
            // Goblin Rabblemaster).
            FilterContext matcherContext = FilterContext.of(gameData)
                    .withSourceCardId(permanent.getOriginalCard().getId())
                    .withSourceControllerId(playerId);
            count[0] += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(MatchingCreaturesMustAttackEffect.class::isInstance)
                    .map(MatchingCreaturesMustAttackEffect.class::cast)
                    .filter(e -> predicateEvaluationService.matchesPermanentPredicate(
                            creature, e.matcher(), matcherContext))
                    .count();
        });

        return count[0];
    }

    /**
     * Builds the list of available attack targets (CR 508.1c): the defending player, their
     * planeswalkers, and battles the active player is allowed to attack (not the protector).
     */
    public List<CombatAttackTarget> buildAvailableTargets(GameData gameData, UUID activePlayerId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, activePlayerId);
        List<CombatAttackTarget> targets = new ArrayList<>();
        targets.add(new CombatAttackTarget(
                defenderId, gameData.playerIdToName.get(defenderId), true));
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);
        if (defBf != null) {
            for (Permanent p : defBf) {
                if (p.getCard().hasType(CardType.PLANESWALKER)) {
                    targets.add(new CombatAttackTarget(
                            p.getId(), p.getCard().getName(), false));
                } else if (p.getCard().hasType(CardType.BATTLE)
                        && !activePlayerId.equals(p.getProtectorPlayerId())) {
                    targets.add(new CombatAttackTarget(
                            p.getId(), p.getCard().getName(), false));
                }
            }
        }
        // Sieges sit on the controller's battlefield; the controller (and non-protectors) may attack them.
        List<Permanent> ownBf = gameData.playerBattlefields.get(activePlayerId);
        if (ownBf != null) {
            for (Permanent p : ownBf) {
                if (p.getCard().hasType(CardType.BATTLE)
                        && !activePlayerId.equals(p.getProtectorPlayerId())) {
                    targets.add(new CombatAttackTarget(
                            p.getId(), p.getCard().getName(), false));
                }
            }
        }
        return targets;
    }

    /**
     * The ids {@link #buildAvailableTargets} offers: the defending player plus the attackable
     * planeswalkers and battles, for validating a declared attack target. Kept as its own scan
     * rather than mapping the target list, because a {@link CombatAttackTarget} also carries a
     * display name and rejects a null one — legality must not depend on a permanent being nameable.
     */
    public Set<UUID> getValidAttackTargetIds(GameData gameData, UUID activePlayerId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, activePlayerId);
        Set<UUID> validIds = new HashSet<>();
        validIds.add(defenderId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);
        if (defBf != null) {
            for (Permanent p : defBf) {
                if (p.getCard().hasType(CardType.PLANESWALKER)) {
                    validIds.add(p.getId());
                } else if (p.getCard().hasType(CardType.BATTLE)
                        && !activePlayerId.equals(p.getProtectorPlayerId())) {
                    validIds.add(p.getId());
                }
            }
        }
        List<Permanent> ownBf = gameData.playerBattlefields.get(activePlayerId);
        if (ownBf != null) {
            for (Permanent p : ownBf) {
                if (p.getCard().hasType(CardType.BATTLE)
                        && !activePlayerId.equals(p.getProtectorPlayerId())) {
                    validIds.add(p.getId());
                }
            }
        }
        return validIds;
    }
}

package com.github.laxika.magicalvibes.service.combat.attack;

import com.github.laxika.magicalvibes.service.GameLogService;

import com.github.laxika.magicalvibes.model.CombatAttackTarget;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.effect.BoostAttackingCreatureOnAttacksYouEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerPoisoned;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.AllMatchingCreaturesAttack;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.AttackCounterMoveEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.action.DelayedOpponentAttackerBoost;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerBoost;
import com.github.laxika.magicalvibes.model.effect.CanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessCountAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatCreatureLimitEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithCounterAttackTogetherEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingAttackerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackIfAnotherCreatureAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.combat.CombatHelper;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatTriggerService;
import com.github.laxika.magicalvibes.service.effect.AttackReturnToHandCostService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles declare-attackers step: computing legal attackers, enforcing attack requirements
 * (CR 508.1d), attack tax payment, tapping, and collecting ON_ATTACK triggers.
 *
 * <p>Whether any single creature may attack, and which targets it may be declared against, is
 * {@link AttackLegalityService}'s to answer; what stays here is everything that needs the
 * declaration as a whole — the group restrictions ("can't attack alone", banding), the costs, and
 * the state changes and triggers a legal declaration produces.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatAttackService {

    private final GameQueryService gameQueryService;
    private final AttackLegalityService attackLegalityService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final CastingCostService castingCostService;
    private final TriggerCollectionService triggerCollectionService;
    private final CombatTriggerService combatTriggerService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.effect.AttackSacrificeCostService attackSacrificeCostService;
    private final AttackReturnToHandCostService attackReturnToHandCostService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final ETBTokenTargetService etbTokenTargetService;

    /**
     * Returns the battlefield indices of creatures the given player can legally declare as
     * attackers against at least one available attack target.
     */
    public List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        if (attackLegalityService.isPlayerPreventedFromAttacking(gameData, playerId)) return List.of();
        Set<UUID> validAttackTargetIds = attackLegalityService.getValidAttackTargetIds(gameData, playerId);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (attackLegalityService.canAttack(gameData, p, playerId)
                    && canAttackAnyTarget(gameData, p, validAttackTargetIds)) {
                indices.add(i);
            }
        }
        // CR 508.1c: if only one creature can attack and it has "can't attack alone", remove it
        if (indices.size() == 1) {
            Permanent sole = battlefield.get(indices.getFirst());
            if (hasCantAttackOrBlockAlone(gameData, sole)) {
                return List.of();
            }
        }
        return indices;
    }

    private boolean canAttackAnyTarget(GameData gameData, Permanent attacker, Set<UUID> validAttackTargetIds) {
        return validAttackTargetIds.stream()
                .anyMatch(targetId -> attackLegalityService.canAttackDefender(gameData, attacker, targetId));
    }

    /**
     * Returns the attackable creatures that can attack the specified player or planeswalker.
     * The ordinary attackable-creature query includes creatures with at least one legal attack
     * target, and this method narrows that result to the specified defender.
     */
    public List<Integer> getAttackableCreatureIndicesForTarget(GameData gameData, UUID playerId,
                                                                UUID targetId) {
        List<Integer> attackable = getAttackableCreatureIndices(gameData, playerId);
        if (attackable.isEmpty() || targetId == null) {
            return attackable;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }

        List<Integer> targetAttackable = attackable.stream()
                .filter(index -> attackLegalityService.canAttackDefender(
                        gameData, battlefield.get(index), targetId))
                .toList();
        if (targetAttackable.size() == 1) {
            Permanent sole = battlefield.get(targetAttackable.getFirst());
            if (hasCantAttackOrBlockAlone(gameData, sole)) {
                return List.of();
            }
        }
        return targetAttackable;
    }

    /**
     * Whether this creature may be declared only as the sole attacker, either from its own
     * static ability or from an attached Aura.
     */
    public boolean canOnlyAttackAlone(GameData gameData, Permanent creature) {
        boolean selfRestricted = creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CanOnlyAttackAloneEffect.class::isInstance);
        return selfRestricted || gameQueryService.hasAuraWithEffect(gameData, creature,
                EnchantedCreatureCanOnlyAttackAloneEffect.class);
    }

    /**
     * Returns the maximum number of creatures that may be declared as attackers this combat.
     */
    public int getMaximumAttackers(GameData gameData) {
        return getMaximumAttackers(gameData, null, false);
    }

    /**
     * Returns the maximum number of creatures that may attack the given target this combat.
     */
    public int getMaximumAttackers(GameData gameData, UUID attackTargetId) {
        return getMaximumAttackers(gameData, attackTargetId, true);
    }

    private int getMaximumAttackers(GameData gameData, UUID attackTargetId,
                                    boolean filterByAttackTarget) {
        int[] maximum = {Integer.MAX_VALUE};
        gameData.forEachPermanent((sourceControllerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CombatCreatureLimitEffect limit
                        && (!filterByAttackTarget
                        || limit.appliesToAttackTarget(sourceControllerId, attackTargetId))) {
                    maximum[0] = Math.min(maximum[0], limit.maxAttackers());
                }
            }
        });
        return maximum[0];
    }

    /**
     * Returns the subset of attackable indices whose creatures have at least one
     * "attacks each combat if able" requirement. Returns empty if an attack tax is in effect.
     */
    public List<Integer> getMustAttackIndices(GameData gameData, UUID playerId, List<Integer> attackableIndices) {
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return List.of();
        }
        if (!castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return List.of();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Integer> mustAttack = new ArrayList<>();
        for (int idx : attackableIndices) {
            Permanent p = battlefield.get(idx);
            if (attackLegalityService.getMustAttackRequirementCount(gameData, p) > 0) {
                mustAttack.add(idx);
            }
        }
        return mustAttack;
    }

    /**
     * Returns the conditional attack requirements that apply to a candidate declaration. Unlike
     * ordinary "attacks each combat if able" requirements, these requirements only apply after
     * another creature has been selected for the same combat, including board-wide counter-bearer
     * requirements such as Magnetic Web.
     */
    public List<Integer> getMustAttackAlongsideIndices(GameData gameData, UUID playerId,
                                                        List<Integer> attackableIndices,
                                                        List<Integer> declaredAttackerIndices) {
        if (declaredAttackerIndices.isEmpty()) {
            return List.of();
        }
        if (castingCostService.getAttackPaymentPerCreature(gameData, playerId) > 0
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return List.of();
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }

        Set<Integer> mustAttack = new LinkedHashSet<>();
        Set<Integer> attackingIndices = new LinkedHashSet<>(declaredAttackerIndices);
        for (int idx : attackableIndices) {
            if (idx < 0 || idx >= battlefield.size() || attackingIndices.contains(idx)) {
                continue;
            }
            Permanent creature = battlefield.get(idx);
            boolean conditional = creature.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(MustAttackIfAnotherCreatureAttacksEffect.class::isInstance);
            if (conditional && attackingIndices.stream().anyMatch(other -> other != idx)) {
                mustAttack.add(idx);
                attackingIndices.add(idx);
            }
        }

        Set<CounterType> attackTogetherCounterTypes = getAttackTogetherCounterTypes(gameData);
        boolean addedCounterBearer;
        do {
            addedCounterBearer = false;
            for (CounterType counterType : attackTogetherCounterTypes) {
                boolean bearerAttacking = attackingIndices.stream()
                        .filter(idx -> idx >= 0 && idx < battlefield.size())
                        .anyMatch(idx -> battlefield.get(idx).getCounterCount(counterType) > 0);
                if (!bearerAttacking) {
                    continue;
                }
                for (int idx : attackableIndices) {
                    if (attackingIndices.contains(idx)) {
                        continue;
                    }
                    Permanent creature = battlefield.get(idx);
                    if (creature.getCounterCount(counterType) > 0) {
                        mustAttack.add(idx);
                        attackingIndices.add(idx);
                        addedCounterBearer = true;
                    }
                }
            }
        } while (addedCounterBearer);
        return new ArrayList<>(mustAttack);
    }

    private Set<CounterType> getAttackTogetherCounterTypes(GameData gameData) {
        Set<CounterType> counterTypes = EnumSet.noneOf(CounterType.class);
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CreaturesWithCounterAttackTogetherEffect together) {
                        counterTypes.add(together.counterType());
                    }
                }
            }
        }
        return counterTypes;
    }

    /**
     * Initiates the declare-attackers step. Sends available attackers to the active player.
     * If no creatures can attack, the step is skipped.
     */
    public void handleDeclareAttackersStep(GameData gameData) {
        UUID activeId = gameData.activePlayerId;
        List<Integer> attackable = getAttackableCreatureIndices(gameData, activeId);

        if (attackable.isEmpty()) {
            String playerName = gameData.playerIdToName.get(activeId);
            log.info("Game {} - {} has no creatures that can attack, skipping combat", gameData.id, playerName);
            return;
        }

        List<Integer> mustAttack = getMustAttackIndices(gameData, activeId, attackable);
        List<CombatAttackTarget> availableTargets = attackLegalityService.buildAvailableTargets(gameData, activeId);
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, activeId);
        boolean mustAttackWithAtLeastOne = isOpponentForcedToAttack(gameData, activeId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.AttackerDeclaration(
                activeId, attackable, mustAttack, availableTargets,
                taxPerCreature, mustAttackWithAtLeastOne));
    }

    /**
     * Validates and processes a player's attacker declaration.
     */
    public CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        return declareAttackers(gameData, player, attackerIndices, attackTargets, null);
    }

    /**
     * Validates and processes a player's attacker declaration, including any attacking bands
     * (CR 702.22): each entry of {@code bands} is the set of attacker indices grouped into one band.
     */
    public CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices,
                                         Map<Integer, UUID> attackTargets, List<List<Integer>> bands) {
        if (gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class) == null) {
            throw new IllegalStateException("Not awaiting attacker declaration");
        }
        if (!player.getId().equals(gameData.activePlayerId)) {
            throw new IllegalStateException("Only the active player can declare attackers");
        }

        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Integer> attackable = getAttackableCreatureIndices(gameData, playerId);

        // Validate indices
        Set<Integer> uniqueIndices = new HashSet<>(attackerIndices);
        if (uniqueIndices.size() != attackerIndices.size()) {
            throw new IllegalStateException("Duplicate attacker indices");
        }
        for (int idx : attackerIndices) {
            if (!attackable.contains(idx)) {
                throw new IllegalStateException("Invalid attacker index: " + idx);
            }
        }

        // CR 508.1c: validate "can't attack alone" — if any declared attacker has this restriction,
        // there must be at least 2 total attackers
        validateCantAttackAlone(gameData, battlefield, attackerIndices);

        // Errantry: "can only attack alone" — an enchanted attacker with this restriction
        // may only be declared if it is the sole attacker
        validateCanOnlyAttackAlone(gameData, battlefield, attackerIndices);

        // Okk: "can't attack unless a creature with greater power also attacks"
        validateGreaterPowerAlsoAttacks(gameData, battlefield, attackerIndices);

        // Orcish Conscripts: "can't attack unless at least N other creatures also attack"
        validateCountAlsoAttacks(battlefield, attackerIndices);

        validateMatchingCreatureAlsoAttacks(gameData, battlefield, attackerIndices);

        // Validate attack requirements (CR 508.1d: satisfy as many as possible)
        validateMaximumAttackRequirements(gameData, playerId, attackable, uniqueIndices);

        // Ekundu Cyclops: "if a creature you control attacks, this creature also attacks if able"
        validateAttacksAlongsideOtherCreature(gameData, playerId, attackable, uniqueIndices);

        // Magnetic Web: "if a creature with a magnet counter attacks, all creatures with magnet
        // counters attack if able"
        validateCounterBearersAttackTogether(gameData, playerId, attackable, uniqueIndices);

        // Validate "must attack with at least one creature" (e.g. Trove of Temptation)
        if (attackerIndices.isEmpty() && !attackable.isEmpty() && isOpponentForcedToAttack(gameData, playerId)) {
            throw new IllegalStateException("Must attack with at least one creature");
        }

        // Empty declaration is always valid — no tax or target validation needed
        if (attackerIndices.isEmpty()) {
            gameData.interaction.clearAwaitingInput();
            log.info("Game {} - {} declares no attackers", gameData.id, player.getUsername());
            gameLogService.append(gameData, GameLog.text(player.getUsername() + " declares no attackers."));
            return CombatResult.AUTO_PASS_ONLY;
        }

        // Flooded Woodlands: the sacrifice cost is per matching attacker, so the whole declaration
        // must be affordable together, not just each attacker on its own.
        attackSacrificeCostService.validateGlobalSacrificeAttackCosts(gameData, playerId, attackerIndices);
        attackReturnToHandCostService.validateReturnToHandAttackCosts(gameData, playerId, attackerIndices);

        UUID defenderId = gameQueryService.getOpponentId(gameData, playerId);
        Set<UUID> validTargetIds = attackLegalityService.getValidAttackTargetIds(gameData, playerId);
        Map<Integer, UUID> resolvedTargets = new HashMap<>();
        for (int idx : attackerIndices) {
            UUID targetId = attackTargets != null ? attackTargets.get(idx) : null;
            if (targetId == null) {
                targetId = defenderId;
            }
            if (!validTargetIds.contains(targetId)) {
                throw new IllegalStateException("Invalid attack target for attacker at index " + idx);
            }
            // Validate must-attack-target constraints (e.g. Alluring Siren forces attack on specific player)
            Permanent attacker = battlefield.get(idx);
            // A permanent-directed requirement (Gideon, Battle-Forged's +2) lapses once that permanent
            // is no longer attackable; a player-directed one (Alluring Siren) always stands.
            if (attacker.getMustAttackTargetId() != null
                    && (gameData.playerIds.contains(attacker.getMustAttackTargetId())
                            || validTargetIds.contains(attacker.getMustAttackTargetId()))
                    && !attacker.getMustAttackTargetId().equals(targetId)) {
                throw new IllegalStateException(attacker.getCard().getName() + " must attack the specified player");
            }
            // Taunt: a taunted player's attacking creatures must attack the taunter if able.
            UUID taunter = gameData.tauntedThisTurn.get(playerId);
            if (taunter != null && validTargetIds.contains(taunter) && !taunter.equals(targetId)) {
                throw new IllegalStateException(attacker.getCard().getName() + " must attack the taunting player");
            }
            // Defender-scoped restriction (e.g. Form of the Dragon — "Creatures without flying can't attack you"):
            // the attacked player controls a permanent that forbids attackers not matching its exemption predicate.
            if (!attackLegalityService.canAttackDefender(gameData, attacker, targetId)) {
                throw new IllegalStateException(attacker.getCard().getName() + " can't attack that player");
            }
            resolvedTargets.put(idx, targetId);
        }

        CombatHelper.validateMaximumAttackers(gameData, attackerIndices, resolvedTargets);

        // Validate attack tax (e.g. Windborn Muse / Ghostly Prison — uniform per-attacker tax from the
        // defender's side; plus per-attacker taxes scoped to a single creature: aura taxes like Brainwash
        // {3}, and self AttackCostEffect taxes like Phyrexian Marauder {1} per +1/+1 counter)
        int selfTaxTotal = 0;
        int totalTax = 0;
        for (int idx : attackerIndices) {
            totalTax += castingCostService.getAttackPaymentPerCreature(gameData, playerId, resolvedTargets.get(idx));
            selfTaxTotal += gameQueryService.getCreatureAttackTax(gameData, battlefield.get(idx));
        }
        totalTax += selfTaxTotal;
        List<ManaColor> phyrexianPayments = castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId);
        if (totalTax > 0) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (pool.getTotal() < totalTax) {
                throw new IllegalStateException("Not enough mana to pay attack tax (" + totalTax + " required)");
            }
        }

        // Validate the Phyrexian portion before paying any attack costs. Costs must be
        // validated atomically: if the declaration cannot pay the required life, none of
        // its generic mana or life may already have been spent (CR 508.1j / 119.4).
        int phyrexianLifeCost = 0;
        if (!phyrexianPayments.isEmpty()) {
            ManaPool simulatedPool = new ManaPool(gameData.playerManaPools.get(playerId));
            if (totalTax > 0) {
                payGenericManaPreservingPhyrexianColors(
                        simulatedPool, totalTax, phyrexianPayments, attackerIndices.size());
            }
            phyrexianLifeCost = payPhyrexianAttackTax(simulatedPool, phyrexianPayments, attackerIndices.size());
            if (phyrexianLifeCost > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                throw new IllegalStateException("Life total can't change to pay Phyrexian attack tax");
            }
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 0);
            if (currentLife < phyrexianLifeCost) {
                throw new IllegalStateException("Not enough life to pay Phyrexian attack tax ("
                        + phyrexianLifeCost + " required)");
            }
        }

        // Validate attacking bands (CR 702.22c/d): each band needs >=1 creature with banding and
        // <=1 without, and all its members must attack the same player or planeswalker.
        List<Set<Integer>> validatedBands = validateBands(gameData, battlefield, uniqueIndices, resolvedTargets, bands);

        // --- All validation passed — commit state changes ---
        gameData.interaction.clearAwaitingInput();
        List<Permanent> declaredAttackers = attackerIndices.stream()
                .map(battlefield::get)
                .toList();

        // Pay attack tax (uniform per-attacker + per-creature aura taxes)
        if (totalTax > 0) {
            payGenericManaPreservingPhyrexianColors(
                    gameData.playerManaPools.get(playerId), totalTax, phyrexianPayments, attackerIndices.size());
        }

        // Pay Phyrexian attack tax (e.g. Norn's Annex — {W/P} per attacker)
        if (!phyrexianPayments.isEmpty()) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int lifeCost = payPhyrexianAttackTax(pool, phyrexianPayments, attackerIndices.size());
            if (lifeCost > 0) {
                int currentLife = gameData.playerLifeTotals.get(playerId);
                gameData.playerLifeTotals.put(playerId, currentLife - lifeCost);
                gameData.lifeLostThisTurn.merge(playerId, lifeCost, Integer::sum);
            }
        }

        // Track that this player declared attackers this turn (for Angelic Arbiter etc.)
        gameData.playersDeclaredAttackersThisTurn.add(playerId);
        gameData.creaturesAttackedCountThisTurn.merge(playerId, attackerIndices.size(), Integer::sum);
        Map<CardSubtype, Integer> subtypeCounts = gameData.creaturesAttackedCountBySubtypeThisTurn
                .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>());
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            Set<CardSubtype> subtypes = new HashSet<>(gameQueryService.effectiveCreatureSubtypes(gameData, attacker));
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.CHANGELING)) {
                for (CardSubtype subtype : CardSubtype.values()) {
                    if (gameQueryService.isCreatureSubtype(subtype)) {
                        subtypes.add(subtype);
                    }
                }
            }
            for (CardSubtype subtype : subtypes) {
                subtypeCounts.merge(subtype, 1, Integer::sum);
            }
        }

        // Mark creatures as attacking and tap them unless vigilance or a combat permission skips it.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            attacker.setAttacking(true);
            UUID attackTarget = resolvedTargets.get(idx);
            attacker.setAttackTarget(attackTarget);
            // "Attacked you this turn" only counts attacks aimed at the player, not at a planeswalker
            // they control, and has to outlive combat — so it is recorded on GameData, not on the
            // permanent's transient attacking state.
            if (attackTarget != null && gameData.playerIds.contains(attackTarget)) {
                gameData.recordAttackAgainstPlayer(attacker.getId(), attackTarget);
            }
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.VIGILANCE)
                    && !gameQueryService.attackingDoesNotCauseTapping(gameData, attacker)) {
                attacker.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, attacker);
            }
        }

        // Assign band membership (CR 702.22): every member of a band shares one band id, which
        // persists for the rest of combat even if banding is later removed (CR 702.22e).
        for (Set<Integer> band : validatedBands) {
            UUID bandId = UUID.randomUUID();
            for (int idx : band) {
                battlefield.get(idx).setBandId(bandId);
            }
        }

        String logEntry = player.getUsername() + " declares " + attackerIndices.size() +
                " attacker" + (attackerIndices.size() > 1 ? "s" : "") + ".";
        gameLogService.append(gameData, GameLog.text(logEntry));

        // Collect all attack-step triggers, then reorder per APNAP (CR 603.3b)
        int stackSizeBeforeAttackTriggers = gameData.stack.size();

        // Check for "when this creature attacks" triggers
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            List<CardEffect> nativeAttackEffects = attacker.getCard().getEffects(EffectSlot.ON_ATTACK);
            List<CardEffect> temporaryAttackEffects = attacker.getTemporaryTriggeredEffects(EffectSlot.ON_ATTACK);
            // Continuously granted ON_ATTACK abilities (Thorncaster Sliver giving every Sliver
            // "Whenever this creature attacks, it deals 1 damage to any target").
            List<CardEffect> grantedAttackEffects = grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, attacker, EffectSlot.ON_ATTACK);
            if (!nativeAttackEffects.isEmpty() || !temporaryAttackEffects.isEmpty()
                    || !grantedAttackEffects.isEmpty()) {
                List<CardEffect> allEffects = new ArrayList<>(nativeAttackEffects);
                allEffects.addAll(grantedAttackEffects);
                // Temporarily granted ON_ATTACK abilities (e.g. Tower Above's "target creature blocks
                // it this turn if able"). MustBlockSourceEffect's source is snapshotted to the attacker.
                for (CardEffect temp : temporaryAttackEffects) {
                    if (temp instanceof MustBlockSourceEffect) {
                        allEffects.add(new MustBlockSourceEffect(attacker.getId()));
                    } else {
                        allEffects.add(temp);
                    }
                }

                // "Whenever this creature attacks, defending player may draw a card" (Sibilant Spirit).
                // Route the optional draw to the defending player (or the controller of the attacked
                // planeswalker), not the attacking creature's controller.
                List<CardEffect> defendingPlayerDraws = allEffects.stream()
                        .filter(e -> e instanceof DefendingPlayerMayDrawCardEffect).toList();
                if (!defendingPlayerDraws.isEmpty()) {
                    allEffects.removeAll(defendingPlayerDraws);
                    UUID attackedTargetId = attacker.getAttackTarget();
                    UUID defendingPlayerId = attackedTargetId == null ? null
                            : gameData.playerIds.contains(attackedTargetId)
                                    ? attackedTargetId
                                    : gameQueryService.findPermanentController(gameData, attackedTargetId);
                    if (defendingPlayerId != null) {
                        for (CardEffect ignored : defendingPlayerDraws) {
                            gameData.queueMayAbility(attacker.getCard(), defendingPlayerId,
                                    new MayEffect(new DrawCardEffect(), "Draw a card?"));
                        }
                        gameLogService.append(gameData,
                                GameLog.builder().card(attacker.getCard()).text("'s ability triggers.").build());
                    }
                }

                // "Whenever this creature attacks for the first time each turn" (Aurelia, the
                // Warleader): drop the wrapped effects entirely once this permanent has already
                // fired them this turn, otherwise unwrap and mark it after the trigger is queued.
                // The gate is per permanent, so an extra combat phase it grants can't loop.
                boolean firesOnceEachTurn = false;
                if (allEffects.stream().anyMatch(e -> e instanceof OncePerTurnTriggerEffect)) {
                    if (gameData.onceEachTurnAttackTriggersFiredThisTurn.contains(attacker.getId())) {
                        allEffects.removeIf(e -> e instanceof OncePerTurnTriggerEffect);
                    } else {
                        firesOnceEachTurn = true;
                        allEffects.replaceAll(e -> e instanceof OncePerTurnTriggerEffect once ? once.wrapped() : e);
                    }
                }
                if (firesOnceEachTurn) {
                    gameData.onceEachTurnAttackTriggersFiredThisTurn.add(attacker.getId());
                }

                // Filter trigger-subject conditionals against this attacking creature, then
                // unwrap them so their condition is not re-evaluated during resolution.
                allEffects.removeIf(e -> e instanceof TriggeringPermanentConditionalEffect conditional
                        && !predicateEvaluationService.matchesPermanentPredicate(
                                gameData, attacker, conditional.predicate()));
                allEffects.replaceAll(e -> e instanceof TriggeringPermanentConditionalEffect conditional
                        ? conditional.wrapped() : e);

                // Filter out attacks-alone conditionals when not attacking alone (CR 506.5)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttacksAlone
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out controls-permanent conditionals when condition not met (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ControlsPermanent
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out controls-another-permanent conditionals when condition not met (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ControlsAnotherPermanent
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out renown conditionals when the attacker isn't renowned (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof SourceIsRenowned
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out defending-player conditionals when condition not met (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && isDefendingPlayerCondition(ce.condition())
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out empty-hand conditionals when controller has cards in hand (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ControllerHandEmpty
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof GraveyardCardThreshold
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out minimum-attackers conditionals (e.g. Odric, Master Tactician's
                // "whenever this and at least three other creatures attack") when attacker count
                // is below the threshold. Attacker count is snapshotted for resolution via xValue.
                ConditionContext attackCountCtx = ConditionContext.forPermanent(attacker, playerId)
                        .withXValue(attackerIndices.size());
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof MinimumAttackers
                        && !conditionEvaluationService.isMet(gameData, ce.condition(), attackCountCtx));

                // Battalion-style attacker counts are part of the trigger event, not an intervening-if
                // clause, so the surviving wrapper is unwrapped here: paths that route the trigger
                // through a target-selection interaction (AttackTriggerTarget) build the stack entry
                // later and would otherwise re-evaluate the condition without the attacker count.
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof MinimumAttackers
                        ? ce.wrapped() : e);

                if (!allEffects.isEmpty()) {
                    // Separate non-targeting "you may" effects (e.g. Primeval Titan's may-search) from
                    // effects that need the normal resolution path (mandatory effects and targeting may effects
                    // like Cyclops Gladiator's may-fight).
                    List<CardEffect> nonTargetingMayEffects = allEffects.stream()
                            .filter(e -> e instanceof com.github.laxika.magicalvibes.model.effect.MayEffect
                                    && !e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) && !e.targetSpec().admits(TargetPredicate.Kind.PLAYER)).toList();
                    List<CardEffect> otherEffects = allEffects.stream()
                            .filter(e -> !nonTargetingMayEffects.contains(e)).toList();

                    // Queue non-targeting may effects as pending may abilities
                    for (CardEffect effect : nonTargetingMayEffects) {
                        com.github.laxika.magicalvibes.model.effect.MayEffect may =
                                (com.github.laxika.magicalvibes.model.effect.MayEffect) effect;
                        gameData.queueMayAbility(attacker.getCard(), playerId, may, null, attacker.getId(),
                                attacker.getAttackTarget());
                    }

                    if (!otherEffects.isEmpty()) {
                        // Two-target "remove a counter from a creature you control, then put one on up
                        // to one creature the defending player controls" (Decimator Beetle). The normal
                        // pipeline collects only one target, so route to the bespoke two-step flow.
                        boolean isCounterMove = otherEffects.stream().anyMatch(e -> e instanceof AttackCounterMoveEffect);
                        boolean needsGraveyardTarget = otherEffects.stream()
                                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
                        boolean needsTarget = otherEffects.stream()
                                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                        UUID attackedTargetId = attacker.getAttackTarget();
                        UUID defendingPlayerId = attackedTargetId == null ? null
                                : gameData.playerIds.contains(attackedTargetId)
                                        ? attackedTargetId
                                        : gameQueryService.findPermanentController(gameData, attackedTargetId);
                        if (isCounterMove) {
                            gameData.queueInteraction(
                                    new PermanentChoiceContext.AttackCounterMoveFirstTarget(
                                            attacker.getCard(), playerId, otherEffects, attacker.getId(), defendingPlayerId));
                        } else if (needsGraveyardTarget) {
                            // "exile target card from defending player's graveyard" (Graven Abomination):
                            // choose as the trigger goes on the stack (same shape as ETB/death GY exile).
                            // Handler owns its own broadcast (including the no-legal-target skip).
                            boolean castsFromGraveyard = otherEffects.stream()
                                    .anyMatch(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect);
                            if (castsFromGraveyard) {
                                // "cast target instant or sorcery card from your graveyard" (The Dawning
                                // Archaic) — the scope comes from the effect, not the attacked player.
                                graveyardTargetingService.handleAttackGraveyardCastTargeting(
                                        gameData, playerId, attacker.getCard(), otherEffects, attacker.getId());
                            } else {
                                graveyardTargetingService.handleAttackGraveyardTargeting(
                                        gameData, playerId, attacker.getCard(), otherEffects,
                                        attacker.getId(), defendingPlayerId);
                            }
                        } else if (needsTarget) {
                            // Multi-target / "up to N" attack triggers (Archon of the Triumvirate):
                            // reuse the ETB slot-by-slot picker — AttackTriggerTarget collects only one.
                            Card attackCard = attacker.getCard();
                            if (attackCard.getSpellTargets().size() > 1
                                    || etbTokenTargetService.needsSlotBySlotTargetSelection(attackCard)) {
                                gameData.queueInteraction(
                                        new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                                attackCard, playerId, otherEffects, attacker.getId(),
                                                List.of(), 0, 0));
                            } else {
                                gameData.queueInteraction(
                                        new PermanentChoiceContext.AttackTriggerTarget(
                                                attackCard, playerId, otherEffects, attacker.getId()));
                            }
                        } else {
                            // Capture the attacked player/planeswalker so non-targeting attack
                            // triggers that act on the defending player (e.g. Nemesis of Reason's
                            // MillDefendingPlayerEffect) can read it as attackedTargetId.
                            // xValue locks the attacker count for MinimumAttackers (Odric / similar).
                            StackEntry attackTrigger = new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    attacker.getCard(),
                                    playerId,
                                    attacker.getCard().getName() + "'s attack trigger",
                                    otherEffects,
                                    attackerIndices.size(),
                                    attacker.getId()
                            );
                            attackTrigger.setAttackedTargetId(attacker.getAttackTarget());
                            gameData.stack.add(attackTrigger);
                        }

                        if (!needsGraveyardTarget) {
                            gameLogService.append(gameData,
                                    GameLog.builder().card(attacker.getCard()).text("'s attack ability triggers.").build());
                            log.info("Game {} - {} attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                        }
                    } else {
                        gameLogService.append(gameData,
                                GameLog.builder().card(attacker.getCard()).text("'s attack ability triggers.").build());
                        log.info("Game {} - {} attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                    }
                }
            }

            // Check for aura-based "when enchanted creature attacks" triggers
            combatTriggerService.checkAuraTriggersForCreature(gameData, attacker, EffectSlot.ON_ATTACK);
        }

        // Engine-level battle cry triggers (keyword-driven, no manual card wiring needed)
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.BATTLE_CRY)) {
                List<CardEffect> battleCryEffects = List.of(new BoostAllOwnCreaturesEffect(1, 0,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        ))
                ));
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        attacker.getCard(),
                        playerId,
                        attacker.getCard().getName() + "'s attack trigger",
                        battleCryEffects,
                        null,
                        attacker.getId()
                ));
                gameLogService.append(gameData,
                        GameLog.builder().card(attacker.getCard()).text("'s battle cry triggers.").build());
                log.info("Game {} - {} battle cry trigger pushed onto stack", gameData.id, attacker.getCard().getName());
            }
        }

        // Engine-level training triggers: attacks with another creature of greater power → +1/+1 counter
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.TRAINING)) {
                continue;
            }
            int selfPower = gameQueryService.getEffectivePower(gameData, attacker);
            boolean hasGreaterPowerAlly = false;
            for (int otherIdx : attackerIndices) {
                if (otherIdx == idx) {
                    continue;
                }
                Permanent other = battlefield.get(otherIdx);
                if (gameQueryService.getEffectivePower(gameData, other) > selfPower) {
                    hasGreaterPowerAlly = true;
                    break;
                }
            }
            if (!hasGreaterPowerAlly) {
                continue;
            }
            List<CardEffect> trainingEffects = List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    attacker.getCard(),
                    playerId,
                    attacker.getCard().getName() + "'s training",
                    trainingEffects,
                    null,
                    attacker.getId()
            ));
            gameLogService.append(gameData,
                    GameLog.builder().card(attacker.getCard()).text("'s training triggers.").build());
            log.info("Game {} - {} training trigger pushed onto stack", gameData.id, attacker.getCard().getName());
        }

        // Check for "whenever one or more creatures you control attack" triggers (ON_ALLY_CREATURES_ATTACK)
        // These fire once per combat (not per creature) when at least one creature attacks.
        // The attacker count is locked at trigger time via xValue (per MTG rules: creatures
        // removed before resolution still count, tokens entering attacking after don't).
        for (Permanent perm : battlefield) {
            List<CardEffect> allyAttackEffects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURES_ATTACK);
            if (allyAttackEffects.isEmpty()) continue;

            // Pre-filter attacker-group conditional effects — skip if no matching attacker exists,
            // if the attacker count is below the threshold, or if not every matching creature is
            // attacking (Mob Mentality). These event conditions are unwrapped onto the stack: the
            // trigger event already happened, and targeted triggers are assembled after target
            // selection without the combat's xValue.
            List<CardEffect> filteredEffects = new ArrayList<>();
            for (CardEffect effect : allyAttackEffects) {
                if (effect instanceof ConditionalEffect ce && ce.condition() instanceof MinimumAttackers minimumAttackers) {
                    boolean minimumMet = conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId).withXValue(attackerIndices.size()));
                    if (!minimumMet) {
                        log.info("Game {} - {} attack trigger skipped (too few attackers; minimum {})",
                                gameData.id, perm.getCard().getName(), minimumAttackers.minimumAttackers());
                        continue;
                    }
                    filteredEffects.add(ce.wrapped());
                } else if (effect instanceof ConditionalEffect ce && ce.condition() instanceof HasAttacker) {
                    boolean hasMatch = conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId));
                    if (!hasMatch) {
                        log.info("Game {} - {} attack trigger skipped (no matching attacker)",
                                gameData.id, perm.getCard().getName());
                        continue;
                    }
                    filteredEffects.add(effect);
                } else if (effect instanceof ConditionalEffect ce
                        && ce.condition() instanceof AllMatchingCreaturesAttack) {
                    boolean allMatch = conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId));
                    if (!allMatch) {
                        log.info("Game {} - {} attack trigger skipped (not all matching creatures attack)",
                                gameData.id, perm.getCard().getName());
                        continue;
                    }
                    filteredEffects.add(ce.wrapped());
                } else if (effect instanceof ConditionalEffect ce
                        && ce.condition() instanceof MinimumAttackingCreaturesOfSubtype) {
                    boolean hasEnoughMatchingAttackers = conditionEvaluationService.isMet(
                            gameData, ce.condition(), ConditionContext.forPermanent(perm, playerId));
                    if (!hasEnoughMatchingAttackers) {
                        log.info("Game {} - {} attack trigger skipped (not enough matching attackers)",
                                gameData.id, perm.getCard().getName());
                        continue;
                    }
                    filteredEffects.add(ce.wrapped());
                } else {
                    filteredEffects.add(effect);
                }
            }
            if (filteredEffects.isEmpty()) continue;

            boolean needsTarget = filteredEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            if (needsTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        perm.getCard(), playerId, filteredEffects, perm.getId()));
                gameLogService.append(gameData,
                        GameLog.builder().card(perm.getCard()).text("'s attack ability triggers.").build());
                log.info("Game {} - {} targeted ON_ALLY_CREATURES_ATTACK trigger queued for target selection",
                        gameData.id, perm.getCard().getName());
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s attack trigger",
                        filteredEffects,
                        attackerIndices.size(),
                        null,
                        perm.getId(),
                        null,
                        null,
                        null,
                        null
                ));
                gameLogService.append(gameData,
                        GameLog.builder().card(perm.getCard()).text("'s attack ability triggers.").build());
                log.info("Game {} - {} ON_ALLY_CREATURES_ATTACK trigger pushed onto stack (attacker count: {})",
                        gameData.id, perm.getCard().getName(), attackerIndices.size());
            }
        }

        // Check for "whenever a creature you control attacks" triggers (ON_ALLY_CREATURE_ATTACKS)
        // These fire once per attacking creature (not once per combat like ON_ALLY_CREATURES_ATTACK).
        // Supports TriggeringCardConditionalEffect to filter by the attacking creature.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (Permanent perm : battlefield) {
                List<CardEffect> perCreatureAttackEffects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ATTACKS);
                if (perCreatureAttackEffects.isEmpty()) continue;

                List<CardEffect> matchingEffects = new ArrayList<>();
                for (CardEffect effect : perCreatureAttackEffects) {
                    if (effect instanceof TriggeringCardConditionalEffect conditional) {
                        if (!predicateEvaluationService.matchesCardPredicate(attacker.getCard(), conditional.predicate(), null,
                                gameData, playerId)) {
                            continue;
                        }
                        matchingEffects.add(conditional.wrapped());
                    } else if (effect instanceof TriggeringPermanentConditionalEffect permConditional) {
                        // Filter by the attacking permanent itself (e.g. Rage Forger — "a creature you
                        // control with a +1/+1 counter on it attacks").
                        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, permConditional.predicate())) {
                            continue;
                        }
                        matchingEffects.add(permConditional.wrapped());
                    } else {
                        matchingEffects.add(effect);
                    }
                }

                // Filter out attacks-alone conditionals (e.g. Exalted) when the creature isn't
                // attacking alone (CR 702.83a) — the ability doesn't trigger at all, so no
                // do-nothing entry goes on the stack.
                matchingEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttacksAlone
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // The surviving attacks-alone conditionals are already satisfied, so unwrap them to
                // their inner effect. This lets a wrapped "you may" (e.g. Angelic Benediction's
                // "you may tap target creature") route through the may/mandatory split below.
                matchingEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttacksAlone ? ce.wrapped() : e);

                if (matchingEffects.isEmpty()) continue;

                // Optional ("you may") per-creature attack triggers go on the stack as CR 603.5
                // resolution-time may abilities: the source permanent is the *attacking* creature
                // ("that creature", the damage source), while the source card is the ability's owner
                // whose target filter governs legal targets (e.g. Rage Forger's ping to a
                // player/planeswalker). Mandatory effects keep the existing direct-stack path where
                // the ability's owner is the source and the attacked target is captured for effects
                // like Hellrider's DealDamageToAttackedTargetEffect.
                List<CardEffect> mayEffects = matchingEffects.stream()
                        .filter(e -> e instanceof MayEffect).toList();
                List<CardEffect> mandatoryEffects = matchingEffects.stream()
                        .filter(e -> !(e instanceof MayEffect)).toList();

                for (CardEffect effect : mayEffects) {
                    gameData.queueMayAbility(perm.getCard(), playerId, (MayEffect) effect, null, attacker.getId());
                }

                if (!mandatoryEffects.isEmpty()) {
                    StackEntry attackTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s attack trigger",
                            mandatoryEffects,
                            null,
                            perm.getId()
                    );
                    attackTrigger.setAttackedTargetId(attacker.getAttackTarget());
                    // Record the triggering attacker as a non-targeting reference so effects that
                    // act on "that creature" (e.g. Shared Animosity's +1/+0 boost) can find it.
                    // Non-targeting so this never fizzles triggers that ignore it (e.g. Hellrider).
                    attackTrigger.setTargetId(attacker.getId());
                    attackTrigger.setNonTargeting(true);
                    gameData.stack.add(attackTrigger);
                }

                gameLogService.append(gameData,
                        GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                log.info("Game {} - {} ON_ALLY_CREATURE_ATTACKS trigger for {} attacking",
                        gameData.id, perm.getCard().getName(), attacker.getCard().getName());
            }
        }

        // Check for graveyard-based "whenever you attack with N or more creatures" triggers
        // (GRAVEYARD_ON_ALLY_CREATURES_ATTACK). These fire from the controller's graveyard.
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> gyAttackEffects = card.getEffects(EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK);
                if (gyAttackEffects.isEmpty()) continue;

                for (CardEffect effect : gyAttackEffects) {
                    CardEffect innerEffect = effect;

                    // Unwrap minimum-attackers conditionals — check minimum before offering the trigger
                    if (innerEffect instanceof ConditionalEffect ce
                            && ce.condition() instanceof MinimumAttackers mac) {
                        ConditionContext ctx = new ConditionContext(playerId, null, null, card,
                                false, false, false, false, null, attackerIndices.size(), null, null, false);
                        if (!conditionEvaluationService.isMet(gameData, mac, ctx)) {
                            log.info("Game {} - {} graveyard attack trigger skipped ({} attackers, need {})",
                                    gameData.id, card.getName(), attackerIndices.size(), mac.minimumAttackers());
                            continue;
                        }
                        innerEffect = ce.wrapped();
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            playerId,
                            card.getName() + "'s graveyard attack trigger",
                            new ArrayList<>(List.of(innerEffect)),
                            attackerIndices.size(),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    ));
                    gameLogService.append(gameData,
                            GameLog.builder().card(card).text("'s graveyard attack ability triggers.").build());
                    log.info("Game {} - {} GRAVEYARD_ON_ALLY_CREATURES_ATTACK trigger pushed onto stack (attacker count: {})",
                            gameData.id, card.getName(), attackerIndices.size());
                }
            }
        }

        // Check for "whenever a creature attacks you or a planeswalker you control" triggers
        // (ON_CREATURE_ATTACKS_YOU). These fire once per attacking creature, on the permanents of
        // the player being attacked (directly or via one of their planeswalkers). The attacking
        // creature is stored as a non-targeting targetId so the effect can act on it.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            UUID attackedTargetId = resolvedTargets.get(idx);
            UUID attackedPlayerId = gameData.playerIds.contains(attackedTargetId)
                    ? attackedTargetId
                    : gameQueryService.findPermanentController(gameData, attackedTargetId);
            if (attackedPlayerId == null) continue;
            List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(attackedPlayerId);
            if (defenderBattlefield == null) continue;
            for (Permanent perm : new ArrayList<>(defenderBattlefield)) {
                List<CardEffect> attackedTriggerEffects = new ArrayList<>();
                for (CardEffect attackedEffect : perm.getCard().getEffects(EffectSlot.ON_CREATURE_ATTACKS_YOU)) {
                    // Some triggers only fire for attackers matching a condition (e.g. Raking Canopy:
                    // "a creature with flying"). The condition is checked here at declaration time.
                    if (attackedEffect instanceof DealDamageToTriggeringAttackerEffect damageEffect
                            && !predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, attacker, damageEffect.attackerCondition())) {
                        continue;
                    }
                    // Intervening "if" (e.g. Guardian of the Ages — "if this creature has defender"):
                    // only triggers when the condition is met at declaration; re-checked on resolution.
                    if (attackedEffect instanceof ConditionalEffect conditional
                            && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                                    ConditionContext.forPermanent(perm, attackedPlayerId))) {
                        continue;
                    }
                    attackedTriggerEffects.add(attackedEffect);
                }
                if (attackedTriggerEffects.isEmpty()) continue;

                StackEntry attackedTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        attackedPlayerId,
                        perm.getCard().getName() + "'s trigger",
                        new ArrayList<>(attackedTriggerEffects),
                        attacker.getId(),
                        perm.getId()
                );
                attackedTrigger.setNonTargeting(true);
                gameData.stack.add(attackedTrigger);
                gameLogService.append(gameData,
                        GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                log.info("Game {} - {} ON_CREATURE_ATTACKS_YOU trigger for {} attacking",
                        gameData.id, perm.getCard().getName(), attacker.getCard().getName());
            }
        }

        // Check for "whenever one or more creatures attack you" triggers (ON_CREATURES_ATTACK_YOU).
        // Unlike ON_CREATURE_ATTACKS_YOU these fire once per combat per attacked player, and only for
        // creatures attacking that player directly — attacking a planeswalker they control does not
        // count. No targetId is set; effects size themselves off the attacking creatures at resolution.
        Set<UUID> directlyAttackedPlayerIds = new LinkedHashSet<>();
        for (int idx : attackerIndices) {
            UUID attackedTargetId = resolvedTargets.get(idx);
            if (gameData.playerIds.contains(attackedTargetId)) {
                directlyAttackedPlayerIds.add(attackedTargetId);
            }
        }
        for (UUID attackedPlayerId : directlyAttackedPlayerIds) {
            List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(attackedPlayerId);
            if (defenderBattlefield == null) continue;
            for (Permanent perm : new ArrayList<>(defenderBattlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_CREATURES_ATTACK_YOU);
                if (effects.isEmpty()) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        attackedPlayerId,
                        perm.getCard().getName() + "'s trigger",
                        new ArrayList<>(effects),
                        null,
                        perm.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData,
                        GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                log.info("Game {} - {} ON_CREATURES_ATTACK_YOU trigger for player {}",
                        gameData.id, perm.getCard().getName(), attackedPlayerId);
            }
        }

        // Emblem version of "whenever a creature attacks you" (Garruk, Apex Predator's emblem). The
        // emblem wording lacks the "or a planeswalker you control" clause, so it only fires when the
        // attacked target is the emblem's controller themselves.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            UUID attackedTargetId = resolvedTargets.get(idx);
            if (!gameData.playerIds.contains(attackedTargetId)) continue;
            for (Emblem emblem : gameData.emblems) {
                if (!emblem.controllerId().equals(attackedTargetId)) continue;
                for (CardEffect emblemEffect : emblem.staticEffects()) {
                    if (!(emblemEffect instanceof BoostAttackingCreatureOnAttacksYouEffect boost)) continue;

                    Card source = emblem.sourceCard();
                    String desc = (source != null ? source.getName() : "Emblem") + "'s emblem";
                    StackEntry trigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source,
                            attackedTargetId,
                            desc,
                            new ArrayList<>(List.of(
                                    new BoostTargetCreatureEffect(boost.powerBoost(), boost.toughnessBoost()),
                                    new GrantKeywordEffect(boost.keywords(), GrantScope.TARGET, null,
                                            GrantDuration.END_OF_TURN, null)
                            )),
                            attacker.getId(),
                            (UUID) null
                    );
                    trigger.setNonTargeting(true);
                    gameData.stack.add(trigger);
                    gameLogService.append(gameData, GameLog.text(desc + " triggers."));
                    log.info("Game {} - emblem attack trigger boosts {}", gameData.id, attacker.getCard().getName());
                }
            }
        }

        // Check for "whenever a creature attacks" triggers (ON_ANY_CREATURE_ATTACKS). These fire once
        // per attacking creature, on every permanent with this slot across all battlefields, regardless
        // of who controls the attacker or whom it attacks (e.g. Caltrops pings every attacker). The
        // attacking creature is stored as a non-targeting targetId so the effect can act on "it".
        // Supports TriggeringPermanentConditionalEffect to restrict which attackers trigger the
        // ability (e.g. Windreader Sphinx — "whenever a creature with flying attacks").
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (Map.Entry<UUID, List<Permanent>> bf : gameData.playerBattlefields.entrySet()) {
                UUID permController = bf.getKey();
                for (Permanent perm : new ArrayList<>(bf.getValue())) {
                    List<CardEffect> anyAttackEffects = perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_ATTACKS);
                    if (anyAttackEffects.isEmpty()) continue;

                    List<CardEffect> matchingAnyAttackEffects = new ArrayList<>();
                    for (CardEffect effect : anyAttackEffects) {
                        if (effect instanceof TriggeringPermanentConditionalEffect permConditional) {
                            if (predicateEvaluationService.matchesPermanentPredicate(gameData, attacker,
                                    permConditional.predicate())) {
                                matchingAnyAttackEffects.add(permConditional.wrapped());
                            }
                        } else {
                            matchingAnyAttackEffects.add(effect);
                        }
                    }
                    if (matchingAnyAttackEffects.isEmpty()) continue;

                    StackEntry anyAttackTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            permController,
                            perm.getCard().getName() + "'s trigger",
                            matchingAnyAttackEffects,
                            attacker.getId(),
                            perm.getId()
                    );
                    anyAttackTrigger.setNonTargeting(true);
                    gameData.stack.add(anyAttackTrigger);
                    gameLogService.append(gameData,
                            GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                    log.info("Game {} - {} ON_ANY_CREATURE_ATTACKS trigger for {} attacking",
                            gameData.id, perm.getCard().getName(), attacker.getCard().getName());
                }
            }
        }

        // Fire delayed "until your next turn, whenever a creature an opponent controls attacks, it
        // gets X/Y until end of turn" triggers (Jace, Architect of Thought's +1). The attacking
        // player is always the attackers' controller here, so a single controller comparison per
        // registered action decides whether it applies to this whole declaration.
        for (DelayedOpponentAttackerBoost boost : gameData.getDelayedActions(DelayedOpponentAttackerBoost.class)) {
            if (boost.controllerId().equals(playerId)) continue;
            for (int idx : attackerIndices) {
                Permanent attacker = battlefield.get(idx);
                StackEntry boostTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        boost.sourceCard(),
                        boost.controllerId(),
                        boost.sourceCard().getName() + "'s delayed trigger",
                        List.of(new BoostSelfEffect(boost.power(), boost.toughness())),
                        attacker.getId(),
                        attacker.getId());
                boostTrigger.setNonTargeting(true);
                gameData.stack.add(boostTrigger);
                gameLogService.append(gameData, GameLog.cardTextCard(
                        boost.sourceCard(), " — ", attacker.getCard(),
                        " gets " + formatBoostPair(boost.power(), boost.toughness())
                                + " until end of turn."));
                log.info("Game {} - {} delayed opponent-attacker boost fires for {}",
                        gameData.id, boost.sourceCard().getName(), attacker.getCard().getName());
            }
        }

        // Check for "whenever a player attacks with one or more creatures" triggers
        // (ON_ANY_PLAYER_ATTACKS). Unlike ON_ALLY_CREATURES_ATTACK these fire for any attacking
        // player, on every permanent with this slot across all battlefields, and only once per
        // combat. The attacking player is stored as a non-targeting targetId so player-scoped
        // effects can act on "that player" (e.g. Total War's sweep of their non-attackers).
        for (Map.Entry<UUID, List<Permanent>> bf : gameData.playerBattlefields.entrySet()) {
            UUID permController = bf.getKey();
            for (Permanent perm : new ArrayList<>(bf.getValue())) {
                List<CardEffect> playerAttackEffects = perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_ATTACKS);
                if (playerAttackEffects.isEmpty()) continue;

                StackEntry playerAttackTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        permController,
                        perm.getCard().getName() + "'s trigger",
                        new ArrayList<>(playerAttackEffects),
                        playerId,
                        perm.getId()
                );
                playerAttackTrigger.setNonTargeting(true);
                gameData.stack.add(playerAttackTrigger);
                gameLogService.append(gameData,
                        GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                log.info("Game {} - {} ON_ANY_PLAYER_ATTACKS trigger for attacking player {}",
                        gameData.id, perm.getCard().getName(), playerId);
            }
        }

        processDelayedAttackerBoostTriggers(gameData, battlefield, attackerIndices);

        // APNAP: active player's triggers on bottom, non-active player's on top (resolves first)
        combatTriggerService.reorderTriggersAPNAP(gameData, stackSizeBeforeAttackTriggers, playerId);

        log.info("Game {} - {} declares {} attackers", gameData.id, player.getUsername(), attackerIndices.size());
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            int p = gameQueryService.getEffectivePower(gameData, attacker);
            int t = gameQueryService.getEffectiveToughness(gameData, attacker);
            List<String> kws = new ArrayList<>();
            for (Keyword kw : List.of(Keyword.TRAMPLE, Keyword.FIRST_STRIKE, Keyword.DOUBLE_STRIKE,
                    Keyword.DEATHTOUCH, Keyword.LIFELINK, Keyword.FLYING, Keyword.VIGILANCE, Keyword.MENACE,
                    Keyword.INDESTRUCTIBLE, Keyword.INFECT)) {
                if (gameQueryService.hasKeyword(gameData, attacker, kw)) kws.add(kw.name().toLowerCase());
            }
            log.info("Game {} -   Attacker [{}]: {} {}/{}{}", gameData.id, idx,
                    attacker.getCard().getName(), p, t, kws.isEmpty() ? "" : " (" + String.join(", ", kws) + ")");
        }

        // Pay "can't attack unless you sacrifice N [permanents]" additional attack costs (Leviathan).
        // Done last so all index-based combat bookkeeping is complete; the cost services use the
        // attacker snapshot because paying one cost can remove permanents from the battlefield.
        attackSacrificeCostService.paySacrificeAttackCosts(gameData, playerId, declaredAttackers);
        attackReturnToHandCostService.payReturnToHandAttackCosts(gameData, playerId, declaredAttackers);

        return CombatResult.AUTO_PASS_ONLY;
    }

    /**
     * Returns the battlefield indices of creatures currently declared as attackers.
     */
    public List<Integer> getAttackingCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).isAttacking()) {
                indices.add(i);
            }
        }
        return indices;
    }


    /**
     * Song of Blood-style delayed triggers: whenever a creature attacks this turn, it gets
     * +power/+toughness until end of turn. One stack entry per attacker per registered boost.
     */
    private void processDelayedAttackerBoostTriggers(GameData gameData, List<Permanent> battlefield,
                                                     List<Integer> attackerIndices) {
        if (attackerIndices.isEmpty() || !gameData.hasDelayedAction(DelayedAttackerBoost.class)) {
            return;
        }
        for (DelayedAttackerBoost boost : gameData.getDelayedActions(DelayedAttackerBoost.class)) {
            for (int idx : attackerIndices) {
                Permanent attacker = battlefield.get(idx);
                StackEntry se = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        boost.sourceCard(),
                        boost.controllerId(),
                        boost.sourceCard().getName() + "'s delayed trigger",
                        List.of(new BoostSelfEffect(boost.power(), boost.toughness())),
                        attacker.getId(),
                        attacker.getId());
                se.setNonTargeting(true);
                gameData.stack.add(se);
                gameLogService.append(gameData, GameLog.cardTextCard(
                        boost.sourceCard(), " — ", attacker.getCard(),
                        " gets +" + boost.power() + "/+" + boost.toughness() + " until end of turn."));
                log.info("Game {} - {} delayed attacker boost fires for {}",
                        gameData.id, boost.sourceCard().getName(), attacker.getCard().getName());
            }
        }
    }

    /**
     * Whether a condition is evaluated against the defending player, and so can be resolved at
     * attack-trigger time. Recurses through {@link NotCondition} so negated wordings such as
     * Spectral Bears' "if defending player controls no black nontoken permanents" are covered.
     */
    private boolean isDefendingPlayerCondition(Condition condition) {
        return switch (condition) {
            case DefendingPlayerControlsPermanent ignored -> true;
            case DefendingPlayerPoisoned ignored -> true;
            case NotCondition not -> isDefendingPlayerCondition(not.inner());
            default -> false;
        };
    }

    private void validateMaximumAttackRequirements(GameData gameData, UUID playerId,
                                                    List<Integer> attackableIndices,
                                                    Set<Integer> declaredAttackerIndices) {
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return;
        }
        if (!castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);

        int maxRequirements = 0;
        for (int idx : attackableIndices) {
            maxRequirements += attackLegalityService.getMustAttackRequirementCount(gameData, battlefield.get(idx));
        }

        int satisfiedRequirements = 0;
        for (int idx : declaredAttackerIndices) {
            satisfiedRequirements += attackLegalityService.getMustAttackRequirementCount(gameData, battlefield.get(idx));
        }

        if (satisfiedRequirements < maxRequirements) {
            for (int idx : attackableIndices) {
                if (!declaredAttackerIndices.contains(idx)
                        && attackLegalityService.getMustAttackRequirementCount(gameData, battlefield.get(idx)) > 0) {
                    throw new IllegalStateException("Creature at index " + idx + " must attack this combat");
                }
            }
            throw new IllegalStateException("Attack declaration satisfies too few attack requirements");
        }
    }

    /**
     * Returns true if an opponent controls a permanent with
     * {@link OpponentsMustAttackControllerEffect}, forcing this player to attack
     * with at least one creature each combat if able. Respects attack tax exemption
     * (CR 508.1d — the player is not required to pay optional attack costs).
     */
    public boolean isOpponentForcedToAttack(GameData gameData, UUID playerId) {
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return false;
        }
        if (!castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return false;
        }
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsMustAttackControllerEffect) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Ekundu Cyclops (CR 508.1d): a creature with a conditional "if a creature you control
     * attacks, this creature also attacks if able" requirement must be declared whenever any
     * other creature its controller controls is declared as an attacker. The requirement is
     * waived when attacking would cost the player mana (CR 508.1d — optional attack costs are
     * never mandatory), matching {@code validateMaximumAttackRequirements}.
     */
    private void validateAttacksAlongsideOtherCreature(GameData gameData, UUID playerId,
                                                       List<Integer> attackableIndices,
                                                       Set<Integer> declaredAttackerIndices) {
        if (castingCostService.getAttackPaymentPerCreature(gameData, playerId) > 0
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        for (int idx : attackableIndices) {
            if (declaredAttackerIndices.contains(idx)) {
                continue;
            }
            Permanent creature = battlefield.get(idx);
            boolean conditional = creature.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(MustAttackIfAnotherCreatureAttacksEffect.class::isInstance);
            if (conditional && declaredAttackerIndices.stream().anyMatch(other -> other != idx)) {
                throw new IllegalStateException(creature.getCard().getName()
                        + " must also attack when another creature you control attacks");
            }
        }
    }

    /**
     * Magnetic Web (CR 508.1d): while a permanent with a
     * {@link CreaturesWithCounterAttackTogetherEffect} is on the battlefield, declaring an attacker
     * that carries the named counter turns "attacks if able" on for every other creature the active
     * player controls with such a counter. Waived under an attack tax for the same reason as
     * {@link #validateAttacksAlongsideOtherCreature} (CR 508.1d never forces a player to pay a cost).
     */
    private void validateCounterBearersAttackTogether(GameData gameData, UUID playerId,
                                                      List<Integer> attackableIndices,
                                                      Set<Integer> declaredAttackerIndices) {
        if (declaredAttackerIndices.isEmpty()) {
            return;
        }
        if (castingCostService.getAttackPaymentPerCreature(gameData, playerId) > 0
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return;
        }

        Set<CounterType> activeCounterTypes = getAttackTogetherCounterTypes(gameData);
        if (activeCounterTypes.isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        for (CounterType counterType : activeCounterTypes) {
            boolean bearerAttacking = declaredAttackerIndices.stream()
                    .anyMatch(idx -> battlefield.get(idx).getCounterCount(counterType) > 0);
            if (!bearerAttacking) {
                continue;
            }
            for (int idx : attackableIndices) {
                if (declaredAttackerIndices.contains(idx)) {
                    continue;
                }
                Permanent creature = battlefield.get(idx);
                if (creature.getCounterCount(counterType) > 0) {
                    throw new IllegalStateException(creature.getCard().getName()
                            + " must also attack when a creature with such a counter attacks");
                }
            }
        }
    }

    private void validateCantAttackAlone(GameData gameData, List<Permanent> battlefield,
                                         List<Integer> attackerIndices) {
        if (attackerIndices.size() == 1) {
            Permanent sole = battlefield.get(attackerIndices.getFirst());
            if (hasCantAttackOrBlockAlone(gameData, sole)) {
                throw new IllegalStateException(sole.getCard().getName() + " can't attack alone");
            }
        }
    }

    private void validateCanOnlyAttackAlone(GameData gameData, List<Permanent> battlefield,
                                            List<Integer> attackerIndices) {
        if (attackerIndices.size() <= 1) {
            return;
        }
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (canOnlyAttackAlone(gameData, attacker)) {
                throw new IllegalStateException(attacker.getCard().getName() + " can only attack alone");
            }
        }
    }

    private boolean hasCantAttackOrBlockAlone(GameData gameData, Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance)
                || gameQueryService.hasAuraWithEffect(gameData, creature,
                        EnchantedCreatureCantAttackOrBlockAloneEffect.class);
    }

    /**
     * Okk (CR 508.1a): a creature with "can't attack unless a creature with greater power also
     * attacks" may only be declared as an attacker if another declared attacker has strictly
     * greater power. The comparison is checked only at declaration time.
     */
    private void validateGreaterPowerAlsoAttacks(GameData gameData, List<Permanent> battlefield,
                                                 List<Integer> attackerIndices) {
        for (int idx : attackerIndices) {
            Permanent restricted = battlefield.get(idx);
            if (!hasGreaterPowerRestriction(restricted)) {
                continue;
            }
            int power = gameQueryService.getEffectivePower(gameData, restricted);
            boolean greaterPowerAlsoAttacks = attackerIndices.stream()
                    .filter(other -> other != idx)
                    .map(battlefield::get)
                    .anyMatch(other -> gameQueryService.getEffectivePower(gameData, other) > power);
            if (!greaterPowerAlsoAttacks) {
                throw new IllegalStateException(restricted.getCard().getName()
                        + " can't attack unless a creature with greater power also attacks");
            }
        }
    }

    private boolean hasGreaterPowerRestriction(Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect.class::isInstance);
    }

    /**
     * Orcish Conscripts (CR 508.1a): a creature with "can't attack unless at least N other
     * creatures also attack" may only be declared as an attacker if at least N other creatures
     * are declared as attackers in the same combat. Checked only at declaration time.
     */
    private void validateCountAlsoAttacks(List<Permanent> battlefield, List<Integer> attackerIndices) {
        for (int idx : attackerIndices) {
            Permanent restricted = battlefield.get(idx);
            restricted.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(CantAttackOrBlockUnlessCountAlsoDoesEffect.class::isInstance)
                    .map(CantAttackOrBlockUnlessCountAlsoDoesEffect.class::cast)
                    .findFirst()
                    .ifPresent(effect -> {
                        long otherAttackers = attackerIndices.stream().filter(other -> other != idx).count();
                        if (otherAttackers < effect.otherCount()) {
                            throw new IllegalStateException(restricted.getCard().getName()
                                    + " can't attack unless at least " + effect.otherCount()
                                    + " other creatures attack");
                        }
                    });
        }
    }

    private void validateMatchingCreatureAlsoAttacks(GameData gameData, List<Permanent> battlefield,
                                                     List<Integer> attackerIndices) {
        for (int idx : attackerIndices) {
            Permanent restricted = battlefield.get(idx);
            restricted.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(MatchingAttackerRestrictionEffect.class::isInstance)
                    .map(MatchingAttackerRestrictionEffect.class::cast)
                    .findFirst()
                    .ifPresent(effect -> {
                        boolean matchingAttacker = attackerIndices.stream()
                                .filter(other -> other != idx)
                                .map(battlefield::get)
                                .anyMatch(other -> predicateEvaluationService.matchesPermanentPredicate(
                                        gameData, other, effect.matchingAttackerPredicate()));
                        if (!matchingAttacker) {
                            throw new IllegalStateException(restricted.getCard().getName()
                                    + " can't attack unless " + effect.restrictionDescription());
                        }
                    });
        }
    }

    /**
     * Validates the declared attacking bands (CR 702.22c/d) and returns them as index sets ready to
     * stamp with band ids. Each band must: contain at least two declared attackers, include at least
     * one creature with banding and at most one without, keep every member attacking the same target,
     * and not share a creature with another band. Returns an empty list when no bands are declared.
     */
    private List<Set<Integer>> validateBands(GameData gameData, List<Permanent> battlefield,
                                             Set<Integer> declaredAttackerIndices,
                                             Map<Integer, UUID> resolvedTargets,
                                             List<List<Integer>> bands) {
        List<Set<Integer>> result = new ArrayList<>();
        if (bands == null || bands.isEmpty()) {
            return result;
        }
        Set<Integer> alreadyBanded = new HashSet<>();
        for (List<Integer> band : bands) {
            if (band == null || band.isEmpty()) {
                continue;
            }
            Set<Integer> members = new LinkedHashSet<>(band);
            if (members.size() < 2) {
                throw new IllegalStateException("A band must contain at least two creatures");
            }
            int withBanding = 0;
            int withoutBanding = 0;
            UUID sharedTarget = null;
            boolean first = true;
            for (int idx : members) {
                if (!declaredAttackerIndices.contains(idx)) {
                    throw new IllegalStateException("Band member " + idx + " is not a declared attacker");
                }
                if (!alreadyBanded.add(idx)) {
                    throw new IllegalStateException("Creature at index " + idx + " can't be a member of more than one band");
                }
                Permanent creature = battlefield.get(idx);
                if (gameQueryService.hasKeyword(gameData, creature, Keyword.BANDING)) {
                    withBanding++;
                } else {
                    withoutBanding++;
                }
                UUID target = resolvedTargets.get(idx);
                if (first) {
                    sharedTarget = target;
                    first = false;
                } else if (!Objects.equals(sharedTarget, target)) {
                    throw new IllegalStateException("All creatures in a band must attack the same player or planeswalker");
                }
            }
            if (withBanding < 1) {
                throw new IllegalStateException("A band must contain at least one creature with banding");
            }
            if (withoutBanding > 1) {
                throw new IllegalStateException("A band can contain at most one creature without banding");
            }
            result.add(members);
        }
        return result;
    }

    public void payGenericMana(ManaPool pool, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            ManaColor highestColor = null;
            int highestCount = 0;
            for (ManaColor color : ManaColor.values()) {
                int count = pool.get(color);
                if (count > highestCount) {
                    highestCount = count;
                    highestColor = color;
                }
            }
            if (highestColor != null) {
                pool.remove(highestColor);
                remaining--;
            } else {
                break;
            }
        }
    }

    private void payGenericManaPreservingPhyrexianColors(ManaPool pool, int amount,
                                                          List<ManaColor> paymentsPerAttacker,
                                                          int attackerCount) {
        if (paymentsPerAttacker.isEmpty()) {
            payGenericMana(pool, amount);
            return;
        }

        EnumMap<ManaColor, Integer> reserved = new EnumMap<>(ManaColor.class);
        for (ManaColor color : paymentsPerAttacker) {
            reserved.merge(color, attackerCount, Integer::sum);
        }

        int remaining = amount;
        while (remaining > 0) {
            ManaColor bestColor = null;
            int bestSurplus = Integer.MIN_VALUE;
            int bestCount = 0;
            for (ManaColor color : ManaColor.values()) {
                int count = pool.get(color);
                if (count <= 0) {
                    continue;
                }
                int surplus = count - reserved.getOrDefault(color, 0);
                if (surplus > bestSurplus || (surplus == bestSurplus && count > bestCount)) {
                    bestColor = color;
                    bestSurplus = surplus;
                    bestCount = count;
                }
            }
            if (bestColor == null) {
                return;
            }
            pool.remove(bestColor);
            remaining--;
        }
    }

    private int payPhyrexianAttackTax(ManaPool pool, List<ManaColor> paymentsPerAttacker, int attackerCount) {
        int lifeCost = 0;
        for (int i = 0; i < attackerCount; i++) {
            for (ManaColor color : paymentsPerAttacker) {
                if (pool.get(color) > 0) {
                    pool.remove(color);
                } else {
                    lifeCost += 2;
                }
            }
        }
        return lifeCost;
    }

    /**
     * Renders a boost the way Magic writes it — "+1/+1", "-1/-0". A zero component takes the sign of
     * the non-zero one, so a -1/-0 debuff never reads as "-1/+0".
     */
    private static String formatBoostPair(int power, int toughness) {
        String sign = (power < 0 || toughness < 0) ? "-" : "+";
        return sign + Math.abs(power) + "/" + sign + Math.abs(toughness);
    }
}

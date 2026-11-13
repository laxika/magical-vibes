package com.github.laxika.magicalvibes.service.combat.attack;

import com.github.laxika.magicalvibes.model.Zone;
import java.util.function.Supplier;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.trigger.CombatAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.TriggerJudge;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatAttackTarget;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TargetGroup;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.action.DelayedAttackDamage;
import com.github.laxika.magicalvibes.model.action.DelayedAttackTokenCreation;
import com.github.laxika.magicalvibes.model.action.DelayedAttackUntap;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerBoost;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerDeclarationControl;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerKeywordGrant;
import com.github.laxika.magicalvibes.model.action.DelayedNontokenAttackTokenCreation;
import com.github.laxika.magicalvibes.model.action.DelayedOpponentAttackerBoost;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureAttack;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.AttackCounterMoveEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyRemoveKeywordEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessCountAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CombatCreatureLimitEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithCounterAttackTogetherEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingAttackerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackIfAnotherCreatureAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCreaturesAttackTogetherEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackRequirementEffect;
import com.github.laxika.magicalvibes.model.effect.OtherCreaturesMustAttackIfSourceAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TrainEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksPlayerWithMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.combat.CombatHelper;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatTriggerService;
import com.github.laxika.magicalvibes.service.effect.AttackReturnToHandCostService;
import com.github.laxika.magicalvibes.service.effect.CombatTapCostService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.CombatWatch;
import com.github.laxika.magicalvibes.service.trigger.MiscAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.MiscWatch;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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

    /** Decayed's triggered ability: "When this creature attacks, sacrifice it at end of combat" (CR 702.147a). */
    private static final TriggeredAbility DECAYED_ABILITY = new TriggeredAbility(Triggers.attacks(),
            new SacrificeAtEndOfCombatEffect());

    /** Battle cry: "Whenever this creature attacks, each other attacking creature gets +1/+0" (CR 702.91a). */
    private static final TriggeredAbility BATTLE_CRY_ABILITY = new TriggeredAbility(Triggers.attacks(),
            new BoostAllOwnCreaturesEffect(1, 0, new PermanentAllOfPredicate(List.of(
                    new PermanentIsAttackingPredicate(),
                    new PermanentNotPredicate(new PermanentIsSourceCardPredicate())))));

    /**
     * Dethrone: "Whenever this creature attacks the player with the most life or tied for most life, put a +1/+1
     * counter on this creature" (CR 702.105a); its collector judges the attacked player.
     */
    private static final TriggeredAbility DETHRONE_ABILITY = new TriggeredAbility(Triggers.attacks(),
            new PutCountersOnSourceEffect(1, 1, 1));

    /**
     * Training: "Whenever this creature and at least one other creature with power greater than this creature's power
     * attack, put a +1/+1 counter on this creature" (CR 702.149a); its collector judges the other attacker.
     */
    private static final TriggeredAbility TRAINING_ABILITY = new TriggeredAbility(Triggers.attacks(), new TrainEffect());

    /**
     * Enlist's triggered ability: "When you do, this creature gets +X/+0 until end of turn, where X is the tapped
     * creature's power" (CR 702.154a).
     */
    private static final TriggeredAbility ENLIST_ABILITY = new TriggeredAbility(Triggers.enlists(),
            new BoostSelfEffect(new ChosenPermanentPower(), new Fixed(0)));

    /**
     * An exalted a static ability grants: "Whenever a creature you control attacks alone, that creature gets +1/+1
     * until end of turn" (CR 702.83a); its collector judges that the creature attacks alone.
     */
    private static final TriggeredAbility GRANTED_EXALTED_ABILITY = new TriggeredAbility(
            Triggers.creatureYouControlAttacks(), new BoostTargetCreatureEffect(1, 1));

    private final GameQueryService gameQueryService;
    private final AttackLegalityService attackLegalityService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final CastingCostService castingCostService;
    private final TriggerCollectionService triggerCollectionService;
    private final CombatTriggerService combatTriggerService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.effect.AttackSacrificeCostService attackSacrificeCostService;
    private final AttackReturnToHandCostService attackReturnToHandCostService;
    private final CombatTapCostService combatTapCostService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final ETBTokenTargetService etbTokenTargetService;
    private final StaticEffectConditionResolver staticEffectConditionResolver;
    private final CardCatalog cardCatalog;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    @Autowired @Lazy
    private LifeSupport lifeSupport;

    @Autowired @Lazy
    private DepartureService departureService;

    private record AttackerDeclarationPrompt(List<Integer> attackableIndices,
                                             List<Integer> mustAttackIndices,
                                             List<CombatAttackTarget> availableTargets,
                                             int taxPerCreature,
                                             boolean mustAttackWithAtLeastOne) {
    }

    /**
     * One "whenever you attack" ability of {@code permanent} that a declaration may trigger, with the attackers of
     * the band it triggers for (EACH_BAND), or {@code null} for all the declared attackers.
     */
    private record AttackTriggerSource(Permanent permanent, TriggeredAbility ability, List<Integer> bandAttackerIndices) {
    }

    /**
     * Returns the battlefield indices of creatures the given player can legally declare as
     * attackers against at least one available attack target.
     */
    public List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        return gameQueryService.withQueryScope(gameData,
                () -> getAttackableCreatureIndicesUnscoped(gameData, playerId));
    }

    private List<Integer> getAttackableCreatureIndicesUnscoped(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        if (attackLegalityService.isPlayerPreventedFromAttacking(gameData, playerId)) return List.of();
        Set<UUID> validAttackTargetIds = attackLegalityService.getValidAttackTargetIds(gameData, playerId);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (attackLegalityService.canAttack(gameData, p, playerId)
                    && combatTapCostService.canPayAttackCost(gameData, p)
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
     * Returns whether the given attacker group leaves enough eligible creatures to pay every
     * creature-tap combat cost in the declaration.
     */
    public boolean canPayAttackTapCosts(GameData gameData, UUID playerId, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return attackerIndices.isEmpty();
        }
        List<Permanent> attackers = new ArrayList<>(attackerIndices.size());
        for (int index : attackerIndices) {
            if (index < 0 || index >= battlefield.size()) {
                return false;
            }
            attackers.add(battlefield.get(index));
        }
        return combatTapCostService.canPayAttackCosts(gameData, playerId, attackers);
    }

    /**
     * Returns the attackable creatures that can attack the specified player or planeswalker.
     * The ordinary attackable-creature query includes creatures with at least one legal attack
     * target, and this method narrows that result to the specified defender.
     */
    public List<Integer> getAttackableCreatureIndicesForTarget(GameData gameData, UUID playerId,
                                                                UUID targetId) {
        return gameQueryService.withQueryScope(gameData, () -> {
            List<Integer> attackable = getAttackableCreatureIndicesUnscoped(gameData, playerId);
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
        });
    }

    /**
     * Whether this creature may be declared only as the sole attacker, either from its own
     * static ability or from an attached Aura.
     */
    public boolean canOnlyAttackAlone(GameData gameData, Permanent creature) {
        boolean selfRestricted = creature.getCard().getStaticEffects().stream()
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
            for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, permanent)) {
                if (effect instanceof CombatCreatureLimitEffect limit
                        && (!filterByAttackTarget
                        || limit.appliesToAttackTarget(sourceControllerId, permanent.getId(), attackTargetId))) {
                    maximum[0] = Math.min(maximum[0], limit.maxAttackers());
                }
            }
        });
        if (gameData.planechase != null) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof CombatCreatureLimitEffect limit
                            && (!filterByAttackTarget
                            || limit.appliesToAttackTarget(gameData.planechase.controllerId,
                            planar.getId(), attackTargetId))) {
                        maximum[0] = Math.min(maximum[0], limit.maxAttackers());
                    }
                }
            }
        }
        return maximum[0];
    }

    /**
     * Returns the subset of attackable indices whose creatures have at least one
     * "attacks each combat if able" requirement that can be met without paying an attack tax.
     */
    public List<Integer> getMustAttackIndices(GameData gameData, UUID playerId, List<Integer> attackableIndices) {
        return gameQueryService.withQueryScope(gameData,
                () -> getMustAttackIndicesUnscoped(gameData, playerId, attackableIndices));
    }

    private List<Integer> getMustAttackIndicesUnscoped(GameData gameData, UUID playerId,
                                                        List<Integer> attackableIndices) {
        if (castingCostService.hasAttackLifePaymentForAnyCreature(gameData, playerId)
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return List.of();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        Set<Integer> restrictionValidAttackers = new HashSet<>(
                getRestrictionValidGroupAttackers(gameData, playerId, attackableIndices));
        for (int idx : attackableIndices) {
            if (isRestrictionValidSingleton(gameData, battlefield, idx)) {
                restrictionValidAttackers.add(idx);
            }
        }
        List<Integer> mustAttack = new ArrayList<>();
        for (int idx : attackableIndices) {
            Permanent p = battlefield.get(idx);
            if (restrictionValidAttackers.contains(idx)
                    && getMaximumUnpaidAttackRequirementCount(gameData, p) > 0) {
                mustAttack.add(idx);
            }
        }
        return mustAttack;
    }

    private List<Integer> getRestrictionValidGroupAttackers(GameData gameData, UUID playerId,
                                                              List<Integer> attackableIndices) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }

        List<Integer> candidates = attackableIndices.stream()
                .filter(idx -> !canOnlyAttackAlone(gameData, battlefield.get(idx)))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        boolean changed;
        do {
            changed = false;
            for (int idx : List.copyOf(candidates)) {
                if (hasUnmetAttackRestriction(gameData, battlefield, candidates, idx)) {
                    candidates.remove(Integer.valueOf(idx));
                    changed = true;
                }
            }
        } while (changed);
        return candidates;
    }

    private boolean isRestrictionValidSingleton(GameData gameData, List<Permanent> battlefield,
                                                 int attackerIndex) {
        return !hasUnmetAttackRestriction(gameData, battlefield, List.of(attackerIndex), attackerIndex);
    }

    private boolean hasUnmetAttackRestriction(GameData gameData, List<Permanent> battlefield,
                                               List<Integer> attackerIndices, int attackerIndex) {
        Permanent attacker = battlefield.get(attackerIndex);
        if (attackerIndices.size() == 1 && hasCantAttackOrBlockAlone(gameData, attacker)) {
            return true;
        }
        if (attackerIndices.size() > 1 && canOnlyAttackAlone(gameData, attacker)) {
            return true;
        }
        if (gameQueryService.hasLostPrintedAbilities(gameData, attacker)) {
            return false;
        }

        for (CardEffect effect : attacker.getCard().getStaticEffects()) {
            if (effect instanceof CantAttackOrBlockUnlessCountAlsoDoesEffect restriction
                    && attackerIndices.size() - 1 < restriction.otherCount()) {
                return true;
            }
            if (effect instanceof CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect) {
                int power = gameQueryService.getEffectivePower(gameData, attacker);
                boolean hasGreaterPowerAttacker = attackerIndices.stream()
                        .filter(other -> other != attackerIndex)
                        .map(battlefield::get)
                        .anyMatch(other -> gameQueryService.getEffectivePower(gameData, other) > power);
                if (!hasGreaterPowerAttacker) {
                    return true;
                }
            }
            if (effect instanceof MatchingAttackerRestrictionEffect restriction) {
                boolean hasMatchingAttacker = attackerIndices.stream()
                        .filter(other -> other != attackerIndex)
                        .map(battlefield::get)
                        .anyMatch(other -> predicateEvaluationService.matchesPermanentPredicate(
                                gameData, other, restriction.matchingAttackerPredicate()));
                if (!hasMatchingAttacker) {
                    return true;
                }
            }
        }
        return false;
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
        if (castingCostService.hasAttackPaymentForAnyCreature(gameData, playerId)
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
            boolean conditional = creature.getCard().getStaticEffects().stream()
                    .anyMatch(MustAttackIfAnotherCreatureAttacksEffect.class::isInstance);
            if (conditional && attackingIndices.stream().anyMatch(other -> other != idx)) {
                mustAttack.add(idx);
                attackingIndices.add(idx);
            }
        }

        if (hasOpponentCreaturesAttackTogetherEffect(gameData, playerId)) {
            for (int idx : attackableIndices) {
                if (attackingIndices.add(idx)) {
                    mustAttack.add(idx);
                }
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

    private boolean hasOpponentCreaturesAttackTogetherEffect(GameData gameData, UUID playerId) {
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            if (sourceControllerId.equals(playerId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getStaticEffects().stream()
                        .anyMatch(OpponentCreaturesAttackTogetherEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<CounterType> getAttackTogetherCounterTypes(GameData gameData) {
        Set<CounterType> counterTypes = EnumSet.noneOf(CounterType.class);
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    if (effect instanceof CreaturesWithCounterAttackTogetherEffect together) {
                        counterTypes.add(together.counterType());
                    }
                }
            }
        }
        return counterTypes;
    }

    /**
     * Adds creatures to the set of creatures that could have attacked this turn, accumulated across
     * every declaration of attackers this turn so that a creature able to attack in an earlier
     * combat stays recorded after it becomes unable to attack later.
     */
    private void recordCreaturesAbleToAttackThisTurn(GameData gameData, UUID playerId, Set<UUID> creatureIds) {
        Set<UUID> merged = new HashSet<>(gameData.creaturesAbleToAttackAtDeclareAttackersThisTurn
                .getOrDefault(playerId, Set.of()));
        merged.addAll(creatureIds);
        gameData.creaturesAbleToAttackAtDeclareAttackersThisTurn.put(playerId, merged);
    }

    /**
     * Initiates the declare-attackers step. Sends available attackers to the active player.
     * If no creatures can attack, the step is skipped.
     */
    public void handleDeclareAttackersStep(GameData gameData) {
        AttackerDeclarationPrompt prompt = gameQueryService.withQueryScope(gameData, () -> {
            UUID activeId = gameData.activePlayerId;
            List<Integer> attackable = getAttackableCreatureIndicesUnscoped(gameData, activeId);

            if (attackable.isEmpty()) {
                return new AttackerDeclarationPrompt(List.of(), List.of(), List.of(), 0, false);
            }

            List<Integer> mustAttack = getMustAttackIndicesUnscoped(gameData, activeId, attackable);
            List<CombatAttackTarget> availableTargets =
                    attackLegalityService.buildAvailableTargets(gameData, activeId);
            int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, activeId);
            boolean mustAttackWithAtLeastOne = isOpponentForcedToAttack(gameData, activeId);
            return new AttackerDeclarationPrompt(
                    attackable, mustAttack, availableTargets, taxPerCreature, mustAttackWithAtLeastOne);
        });

        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(gameData.activePlayerId, List.of());
        Set<UUID> ableToAttack = prompt.attackableIndices().stream()
                .filter(index -> index >= 0 && index < battlefield.size())
                .map(index -> battlefield.get(index).getId())
                .collect(Collectors.toSet());
        recordCreaturesAbleToAttackThisTurn(gameData, gameData.activePlayerId, ableToAttack);

        if (prompt.attackableIndices().isEmpty()) {
            UUID activeId = gameData.activePlayerId;
            String playerName = gameData.playerIdToName.get(activeId);
            log.info("Game {} - {} has no creatures that can attack, skipping combat", gameData.id, playerName);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.AttackerDeclaration(
                gameData.activePlayerId, prompt.attackableIndices(), prompt.mustAttackIndices(),
                prompt.availableTargets(), prompt.taxPerCreature(), prompt.mustAttackWithAtLeastOne(),
                attackerDeclarationChooser(gameData, gameData.activePlayerId)));
    }

    /**
     * Validates and processes a player's attacker declaration.
     */
    public CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        return declareAttackers(gameData, player, attackerIndices, attackTargets, null);
    }

    /**
     * CR 508.1j and CR 616.1: once an attack declaration's costs are validated and before any is paid, asks the death
     * replacement questions of the Lich's Duel Mastery that paying {@code lifePayment} life would sacrifice instead
     * (paying life is losing it, CR 119.4). When one is asked, nothing is declared and {@code replay} is stored; the
     * declaration is made again once every answer is in ({@link #completeAttackAfterLifeCostAnswers}), and the life
     * payment's sacrifice consumes the answers.
     *
     * @return {@code true} when the declaration stopped to ask
     */
    private boolean askAttackLifeCostFirst(GameData gameData, UUID playerId, int lifePayment,
                                           GameData.AttackDeclarationReplay replay) {
        DepartureIntent sacrifice = lifePayment > 0
                ? lifeSupport.lifeLossSacrifice(gameData, playerId, lifePayment) : null;
        if (sacrifice == null || !departureService.askBeforePayment(gameData, DepartureBatch.of(List.of(sacrifice)))) {
            return false;
        }
        gameData.pendingDeathReplacementChoiceReplay = replay;
        return true;
    }

    /**
     * Declares again the attack {@link #askAttackLifeCostFirst} stopped, once every death replacement it asked about
     * is chosen: its declaration prompt is active again and answered with the same attackers.
     */
    public void completeAttackAfterLifeCostAnswers(GameData gameData, GameData.AttackDeclarationReplay replay) {
        interactionHandlerRegistry.begin(gameData, replay.declaration());
        if (!interactionHandlerRegistry.dispatchAnswer(gameData, replay.player(),
                new InteractionAnswer.AttackersDeclared(replay.attackerIndices(), replay.attackTargets(),
                        replay.bands()))) {
            throw new IllegalStateException("The attacker declaration could not be made again");
        }
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
        PendingInteraction.AttackerDeclaration pending =
                gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class);
        if (!player.getId().equals(pending.decidingPlayerId())) {
            throw new IllegalStateException(pending.choosingForOpponent()
                    ? "Only the player choosing attackers can declare attackers"
                    : "Only the active player can declare attackers");
        }

        UUID playerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Integer> attackable = getAttackableCreatureIndices(gameData, playerId);

        // CR 508.1c: a lone creature with "can't attack alone" is removed from the attackable set, so its
        // restriction is reported here rather than as an invalid index
        if (attackerIndices.size() == 1 && attackerIndices.getFirst() >= 0
                && attackerIndices.getFirst() < battlefield.size()) {
            validateCantAttackAlone(gameData, battlefield, attackerIndices);
        }

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
        validateCountAlsoAttacks(gameData, battlefield, attackerIndices);

        validateMatchingCreatureAlsoAttacks(gameData, battlefield, attackerIndices);

        // Validate attack requirements (CR 508.1d: satisfy as many as possible)
        validateMaximumAttackRequirements(gameData, playerId, attackable, uniqueIndices, attackTargets);

        // Ekundu Cyclops: "if a creature you control attacks, this creature also attacks if able"
        validateAttacksAlongsideOtherCreature(gameData, playerId, attackable, uniqueIndices);

        // War's Toll: if one of the active player's creatures attacks, all of that player's
        // creatures attack if able.
        validateOpponentCreaturesAttackTogether(gameData, playerId, attackable, uniqueIndices);
        validateOtherCreaturesAttackWithSource(gameData, playerId, attackable, uniqueIndices);

        // Magnetic Web: "if a creature with a magnet counter attacks, all creatures with magnet
        // counters attack if able"
        validateCounterBearersAttackTogether(gameData, playerId, attackable, uniqueIndices);

        // Validate "must attack with at least one creature" (e.g. Trove of Temptation)
        validateOpponentRequiredAttackTargets(gameData, playerId, attackable, attackerIndices, attackTargets);

        // Empty declaration is always valid — no tax or target validation needed
        recordCreaturesAbleToAttackThisTurn(gameData, playerId, attackable.stream()
                .map(battlefield::get)
                .map(Permanent::getId)
                .collect(Collectors.toSet()));

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
        List<Permanent> selectedAttackers = attackerIndices.stream().map(battlefield::get).toList();
        combatTapCostService.validateAttackCosts(gameData, playerId, selectedAttackers);

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
            if (pending.choosingForOpponent() && !targetId.equals(defenderId)) {
                throw new IllegalStateException("Only the active player can choose what an attacker attacks");
            }
            // Validate must-attack-target constraints (e.g. Alluring Siren forces attack on specific player)
            Permanent attacker = battlefield.get(idx);
            // CR 508.1d: a must-attack requirement is only "able" to be obeyed without paying an attack
            // tax, since the controller is not required to pay that cost.
            if (attacker.getMustAttackTargetId() != null
                    && validTargetIds.contains(attacker.getMustAttackTargetId())
                    && attackLegalityService.canAttackDefender(gameData, attacker, attacker.getMustAttackTargetId())
                    && castingCostService.getAttackPaymentPerCreature(gameData, playerId,
                    attacker.getMustAttackTargetId(), attacker) == 0
                    && castingCostService.getAttackLifePaymentPerCreature(gameData, playerId,
                    attacker.getMustAttackTargetId(), attacker) == 0
                    && !attacker.getMustAttackTargetId().equals(targetId)) {
                throw new IllegalStateException(attacker.getCard().getName() + " must attack the specified player");
            }
            if (gameQueryService.computeStaticBonus(gameData, attacker).grantedEffects().stream()
                    .anyMatch(MustAttackPlayerEffect.class::isInstance)
                    && attackLegalityService.canAttackDefender(gameData, attacker, defenderId)
                    && !gameData.playerIds.contains(targetId)) {
                throw new IllegalStateException(attacker.getCard().getName() + " must attack a player");
            }
            // Taunt: a taunted player's attacking creatures must attack the taunter if able.
            UUID taunter = gameData.tauntedThisTurn.get(playerId);
            if (taunter != null && validTargetIds.contains(taunter) && !taunter.equals(targetId)
                    && attackLegalityService.canAttackDefender(gameData, attacker, taunter)) {
                throw new IllegalStateException(attacker.getCard().getName() + " must attack the taunting player");
            }
            if (attackLegalityService.mustAttackOtherPlayerIfAble(gameData, attacker, targetId)) {
                throw new IllegalStateException(attacker.getCard().getName()
                        + " must attack a player other than you if able");
            }
            // Defender-scoped restriction (e.g. Form of the Dragon — "Creatures without flying can't attack you"):
            // the attacked player controls a permanent that forbids attackers not matching its exemption predicate.
            if (!attackLegalityService.canAttackDefender(gameData, attacker, targetId)) {
                throw new IllegalStateException(attacker.getCard().getName() + " can't attack that player");
            }
            resolvedTargets.put(idx, targetId);
        }

        CombatHelper.validateCommandZoneAttackCount(gameData, playerId, attackerIndices.size());
        CombatHelper.validateMaximumAttackers(gameData, attackerIndices, resolvedTargets,
                gameQueryService);

        // Validate attack tax (e.g. Windborn Muse / Ghostly Prison — uniform per-attacker tax from the
        // defender's side; plus per-attacker taxes scoped to a single creature: aura taxes like Brainwash
        // {3}, and self AttackCostEffect taxes like Phyrexian Marauder {1} per +1/+1 counter)
        int selfTaxTotal = 0;
        int defenderTaxTotal = 0;
        int totalTax = 0;
        int attackLifeTaxTotal = 0;
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            int defenderTax = castingCostService.getAttackPaymentPerCreature(
                    gameData, playerId, resolvedTargets.get(idx), attacker);
            defenderTaxTotal += defenderTax;
            totalTax += defenderTax;
            selfTaxTotal += gameQueryService.getCreatureAttackTax(gameData, attacker);
            attackLifeTaxTotal += castingCostService.getAttackLifePaymentPerCreature(
                    gameData, playerId, resolvedTargets.get(idx), attacker);
        }
        totalTax += selfTaxTotal;
        List<ManaColor> phyrexianPayments = castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId);
        // Phyrexian attack taxes (e.g. Norn's Annex) cover attacks on the defender and their planeswalkers,
        // never on a battle, so only attackers aimed at a non-battle target count toward them.
        int phyrexianTaxedAttackerCount = (int) attackerIndices.stream()
                .filter(idx -> {
                    Permanent attacked = gameQueryService.findPermanentById(gameData, resolvedTargets.get(idx));
                    return attacked == null || !gameQueryService.isBattle(gameData, attacked);
                })
                .count();
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
                        simulatedPool, totalTax, phyrexianPayments, phyrexianTaxedAttackerCount);
            }
            phyrexianLifeCost = payPhyrexianAttackTax(simulatedPool, phyrexianPayments, phyrexianTaxedAttackerCount);
            if (phyrexianLifeCost > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                throw new IllegalStateException("Life total can't change to pay Phyrexian attack tax");
            }
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 0);
            if (currentLife < phyrexianLifeCost) {
                throw new IllegalStateException("Not enough life to pay Phyrexian attack tax ("
                        + phyrexianLifeCost + " required)");
            }
        }
        if (attackLifeTaxTotal > 0) {
            if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                throw new IllegalStateException("Life total can't change to pay attack life cost");
            }
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 0);
            if (currentLife < attackLifeTaxTotal) {
                throw new IllegalStateException("Not enough life to pay attack life cost ("
                        + attackLifeTaxTotal + " required)");
            }
        }

        // Validate attacking bands (CR 702.22c/d): ordinary bands need >=1 creature with banding
        // and <=1 without, while a "bands with other" band needs matching named creatures and at
        // least one creature with that ability. All members must attack the same target.
        List<Set<Integer>> validatedBands = validateBands(gameData, battlefield, uniqueIndices, resolvedTargets, bands);

        // --- All validation passed — commit state changes ---
        gameData.interaction.clearAwaitingInput();
        if (askAttackLifeCostFirst(gameData, playerId, phyrexianLifeCost + attackLifeTaxTotal,
                new GameData.AttackDeclarationReplay(player, pending, List.copyOf(attackerIndices),
                        attackTargets == null ? null : new HashMap<>(attackTargets),
                        bands == null ? null : bands.stream().map(List::copyOf).toList()))) {
            return CombatResult.DONE;
        }
        List<Permanent> declaredAttackers = attackerIndices.stream()
                .map(battlefield::get)
                .toList();

        // Pay attack tax (uniform per-attacker + per-creature aura taxes)
        if (totalTax > 0) {
            payGenericManaPreservingPhyrexianColors(
                    gameData.playerManaPools.get(playerId), totalTax, phyrexianPayments, phyrexianTaxedAttackerCount);
        }
        if (defenderTaxTotal > 0) {
            triggerCollectionService.checkTaxPaymentTriggers(
                    gameData, playerId, gameQueryService.getOpponentId(gameData, playerId), defenderTaxTotal);
        }

        // Pay Phyrexian attack tax (e.g. Norn's Annex — {W/P} per attacker)
        if (!phyrexianPayments.isEmpty()) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int lifeCost = payPhyrexianAttackTax(pool, phyrexianPayments, phyrexianTaxedAttackerCount);
            // Each {W/P} symbol paid with life is unpaid mana and costs 2 life, so the mana actually paid
            // is the symbol count minus the symbols paid with life (Tax Taker counts only that mana).
            int manaPaid = phyrexianTaxedAttackerCount * phyrexianPayments.size() - lifeCost / 2;
            if (manaPaid > 0) {
                triggerCollectionService.checkTaxPaymentTriggers(
                        gameData, playerId, gameQueryService.getOpponentId(gameData, playerId), manaPaid);
            }
            if (lifeCost > 0) {
                lifeSupport.applyLifePayment(gameData, playerId, lifeCost, "Phyrexian attack tax");
            }
        }
        if (attackLifeTaxTotal > 0) {
            lifeSupport.applyLifePayment(gameData, playerId, attackLifeTaxTotal, "attack life tax");
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " pays " + attackLifeTaxTotal
                            + " life to declare attackers."));
        }

        // A life payment that sacrificed a Lich's Duel Mastery moved the attackers on the battlefield, so they are
        // found again by permanent.
        Map<Integer, Integer> movedIndices = movedAttackerIndices(battlefield, attackerIndices, declaredAttackers);
        List<Integer> committedIndices = attackerIndices.stream()
                .map(idx -> movedIndices.getOrDefault(idx, idx)).toList();
        Map<Integer, UUID> committedTargets = new HashMap<>();
        resolvedTargets.forEach((idx, target) -> committedTargets.put(movedIndices.getOrDefault(idx, idx), target));
        List<Set<Integer>> committedBands = movedIndices.isEmpty() ? validatedBands : validatedBands.stream()
                .map(band -> band.stream().map(idx -> movedIndices.getOrDefault(idx, idx))
                        .collect(Collectors.<Integer>toSet()))
                .toList();

        combatTapCostService.payAttackCosts(gameData, playerId, declaredAttackers);
        declaredAttackers.forEach(attacker -> gameData.declaredAttackerIdsThisCombat.add(attacker.getId()));

        // Track that this player declared attackers this turn (for Angelic Arbiter etc.)
        gameData.playersDeclaredAttackersThisTurn.add(playerId);
        if (declaredAttackers.stream().anyMatch(attacker -> attacker.getCard().isToken())) {
            gameData.playersWhoAttackedWithTokenThisTurn.add(playerId);
        }
        gameData.creaturesAttackedCountThisTurn.merge(playerId, committedIndices.size(), Integer::sum);
        declaredAttackers.forEach(attacker -> gameData.creaturesAttackedWithThisTurn
                .computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet()).add(attacker.getId()));
        Map<CardSubtype, Integer> subtypeCounts = gameData.creaturesAttackedCountBySubtypeThisTurn
                .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>());
        for (int idx : committedIndices) {
            Permanent attacker = battlefield.get(idx);
            if (gameData.isCommander(attacker.getOriginalCard().getId())) {
                gameData.playersWhoAttackedWithCommanderThisTurn.add(playerId);
            }
            Set<CardSubtype> subtypes = new HashSet<>(gameQueryService.getEffectiveSubtypes(gameData, attacker));
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
        triggerCollectionService.beginPermanentTapTriggerBatch(gameData);
        for (int idx : committedIndices) {
            Permanent attacker = battlefield.get(idx);
            attacker.setAttacking(true);
            attacker.setAttackedOrBlockedSinceLastUpkeep(true);
            UUID attackTarget = committedTargets.get(idx);
            attacker.setAttackTarget(attackTarget);
            // "Attacked you this turn" only counts attacks aimed at the player, not at a planeswalker
            // they control, and has to outlive combat — so it is recorded on GameData, not on the
            // permanent's transient attacking state.
            if (attackTarget != null && gameData.playerIds.contains(attackTarget)) {
                gameData.recordAttackAgainstPlayer(attacker.getId(), attackTarget);
                gameData.recordPlayerAttackAgainstPlayer(playerId, attackTarget);
                var attackSubtypes = gameQueryService.effectiveCreatureSubtypes(gameData, attacker);
                gameData.recordDeclaredAttackerSnapshot(attacker.getId(), playerId, attackSubtypes);
                if (attacker.isFaceDown() || gameQueryService.isEnchantment(gameData, attacker)
                        || attackSubtypes.contains(CardSubtype.HORROR)
                        || attackSubtypes.contains(CardSubtype.NIGHTMARE)) {
                    gameData.playersAfraidOfControllerAfterAttackThisTurn
                            .computeIfAbsent(playerId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                            .add(attackTarget);
                }
            } else if (attackTarget != null) {
                Permanent attackedPermanent = gameQueryService.findPermanentById(gameData, attackTarget);
                if (attackedPermanent != null && gameQueryService.isBattle(gameData, attackedPermanent)) {
                    gameData.permanentsThatAttackedBattlesThisTurn.add(attacker.getId());
                }
            }
            if (attackTarget != null) {
                Permanent targetPermanent = gameQueryService.findPermanentById(gameData, attackTarget);
                if (gameData.playerIds.contains(attackTarget)
                        || (targetPermanent != null && targetPermanent.getCard().hasType(CardType.PLANESWALKER))) {
                    UUID attackedPlayerId = gameData.playerIds.contains(attackTarget)
                            ? attackTarget
                            : gameQueryService.findPermanentController(gameData, attackTarget);
                    gameData.recordAttackAgainstPlayerOrPlaneswalker(playerId, attackedPlayerId);
                }
            }
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.VIGILANCE)
                    && !gameQueryService.attackingDoesNotCauseTapping(gameData, attacker)) {
                attacker.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, attacker);
            }
        }
        triggerCollectionService.endPermanentTapTriggerBatch(gameData);

        // Assign band membership (CR 702.22): every member of a band shares one band id, which
        // persists for the rest of combat even if banding is later removed (CR 702.22e).
        for (Set<Integer> band : committedBands) {
            UUID bandId = UUID.randomUUID();
            for (int idx : band) {
                battlefield.get(idx).setBandId(bandId);
            }
        }

        if (beginEnlistmentChoice(gameData, playerId, battlefield, committedIndices,
                committedTargets, declaredAttackers)) {
            return CombatResult.AUTO_PASS_ONLY;
        }
        return finishAttackerDeclaration(gameData, playerId, player.getUsername(), battlefield,
                committedIndices, committedTargets, declaredAttackers, Map.of(), Map.of());
    }

    /**
     * The new battlefield index of each declared attacker that moved since {@code attackerIndices} were read (a
     * permanent before it left the battlefield), keyed by its declared index.
     */
    private static Map<Integer, Integer> movedAttackerIndices(List<Permanent> battlefield,
                                                              List<Integer> attackerIndices,
                                                              List<Permanent> declaredAttackers) {
        Map<Integer, Integer> moved = new HashMap<>();
        for (int i = 0; i < attackerIndices.size(); i++) {
            int current = battlefield.indexOf(declaredAttackers.get(i));
            if (current != attackerIndices.get(i)) {
                moved.put(attackerIndices.get(i), current);
            }
        }
        return moved;
    }

    /** Completes attack declaration after all Enlist choices have been answered. */
    public CombatResult finishAttackerDeclaration(GameData gameData, UUID playerId, String playerName,
                                                  List<Permanent> battlefield, List<Integer> attackerIndices,
                                                  Map<Integer, UUID> resolvedTargets,
                                                  List<Permanent> declaredAttackers,
                                                  Map<UUID, Integer> enlistmentBoosts,
                                                  Map<UUID, UUID> enlistedSupporters) {
        int stackSizeBeforeAttackTriggers = gameData.stack.size();
        addEnlistmentTriggers(gameData, playerId, enlistmentBoosts, enlistedSupporters);
        // The creatures declared as attackers, as they were declared: the event's attacking creatures of every
        // "whenever this creature attacks" entry, snapshotted once for the declaration when an ability needs them.
        List<List<Permanent>> declaredSnapshotsHolder = new ArrayList<>(1);
        Supplier<List<Permanent>> declaredAttackerSnapshots = () -> {
            if (declaredSnapshotsHolder.isEmpty()) {
                declaredSnapshotsHolder.add(attackerIndices.stream().map(battlefield::get)
                        .map(declared -> snapshotDeclaredAttacker(gameData, declared)).toList());
            }
            return declaredSnapshotsHolder.getFirst();
        };

        String logEntry = playerName + " declares " + attackerIndices.size() +
                " attacker" + (attackerIndices.size() > 1 ? "s" : "") + ".";
        gameLogService.append(gameData, GameLog.text(logEntry));

        // Collect all attack-step triggers, then reorder per APNAP (CR 603.3b)
        // Check for "when this creature attacks" triggers
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            // Decayed's "when this creature attacks, sacrifice it at end of combat" is its own triggered ability
            // (CR 702.147a), so it goes on the stack as a separate object from any printed attack ability.
            boolean decayedTrigger = gameQueryService.hasKeyword(gameData, attacker, Keyword.DECAYED);
            collectSelfAttackAbilities(gameData, playerId, attacker, attackerIndices, declaredAttackerSnapshots);
            if (decayedTrigger) {
                putKeywordAttackAbility(gameData, playerId, attacker, DECAYED_ABILITY, "'s decayed trigger", entry -> {
                    entry.setXValue(attackerIndices.size());
                    entry.setSourcePermanentSnapshot(new Permanent(attacker));
                });
            }

            // Check for aura-based "when enchanted creature attacks" triggers
            combatTriggerService.checkAuraTriggersForCreature(gameData, attacker, CombatWatch.ON_ATTACK);

            // Delayed triggers watching this specific creature (Okoye, Mighty and Adored): the
            // registering card is the source and its controller controls the trigger (CR 603.7).
            for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
                if (!watcher.ability().hasWatchedTrigger(CombatWatch.ON_ATTACK.trigger())
                        || !attacker.getId().equals(watcher.watchedPermanentId())) {
                    continue;
                }
                List<CombatAbilityReader.CombatEffect> watcherReads =
                        CombatAbilityReader.read(watcher.ability(), CombatWatch.ON_ATTACK.trigger());
                CombatAbilityReader.CombatEffect watcherRead = watcherReads.getFirst();
                CombatAbilityReader.requireNoLimitOrMultiplicity(watcherRead, CombatWatch.ON_ATTACK.trigger());
                if (watcherRead.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, attacker, watcherRead.filter())) {
                    continue;
                }
                StackEntry delayedTrigger = TriggeredAbilityStackService.madeAbility(
                        watcher.sourceCard(),
                        watcher.controllerId(),
                        watcher.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(CombatAbilityReader.effectsOf(watcherReads)),
                        null,
                        attacker.getId());
                delayedTrigger.setNonTargeting(true);
                delayedTrigger.setTriggeringPermanentId(attacker.getId());
                triggeredAbilityStackService.putDelayed(gameData, delayedTrigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            }
        }

        // Melee (CR 702.121a): each attacking creature with melee gets +1/+1 until end of turn for each distinct
        // opponent attacked by this combat.
        Set<UUID> opponentsAttackedThisCombat = new HashSet<>();
        for (int idx : attackerIndices) {
            UUID attackTarget = resolvedTargets.get(idx);
            if (attackTarget != null && gameData.playerIds.contains(attackTarget)) {
                opponentsAttackedThisCombat.add(attackTarget);
            }
        }
        int meleeBoost = opponentsAttackedThisCombat.size();
        TriggeredAbility meleeAbility = new TriggeredAbility(Triggers.attacks(),
                new BoostSelfEffect(meleeBoost, meleeBoost));
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.MELEE)) {
                continue;
            }
            for (int instance = 0; instance < gameQueryService.meleeInstances(gameData, attacker); instance++) {
                if (putKeywordAttackAbility(gameData, playerId, attacker, meleeAbility, "'s melee", entry -> { })) {
                    gameLogService.append(gameData,
                            GameLog.builder().card(attacker.getCard()).text("'s melee triggers.").build());
                }
            }
        }

        // Battle cry (CR 702.91a): each instance triggers separately.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            int battleCryInstances = gameQueryService.triggerKeywordInstances(gameData, attacker, Keyword.BATTLE_CRY);
            for (int instance = 0; instance < battleCryInstances; instance++) {
                if (putKeywordAttackAbility(gameData, playerId, attacker, BATTLE_CRY_ABILITY, "'s battle cry",
                        entry -> { })) {
                    gameLogService.append(gameData,
                            GameLog.builder().card(attacker.getCard()).text("'s battle cry triggers.").build());
                }
            }
        }

        // Dethrone (CR 702.105a): attacking the player with the most life or tied for most life puts a +1/+1 counter on
        // the attacking creature; each instance triggers separately (CR 702.105b).
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.DETHRONE)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    gameData, attacker, new PermanentAttacksPlayerWithMostLifePredicate())) {
                continue;
            }
            int dethroneInstances = gameQueryService.triggerKeywordInstances(gameData, attacker, Keyword.DETHRONE);
            for (int instance = 0; instance < dethroneInstances; instance++) {
                if (putKeywordAttackAbility(gameData, playerId, attacker, DETHRONE_ABILITY, "'s dethrone",
                        entry -> { })) {
                    gameLogService.append(gameData,
                            GameLog.builder().card(attacker.getCard()).text("'s dethrone triggers.").build());
                }
            }
        }

        // Training (CR 702.149a): attacking with another creature of greater power puts a +1/+1 counter on it.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.TRAINING)) {
                continue;
            }
            int selfPower = gameQueryService.getEffectivePower(gameData, attacker);
            boolean hasGreaterPowerAlly = false;
            for (int otherIdx : attackerIndices) {
                if (otherIdx != idx && gameQueryService.getEffectivePower(gameData, battlefield.get(otherIdx)) > selfPower) {
                    hasGreaterPowerAlly = true;
                    break;
                }
            }
            if (hasGreaterPowerAlly && putKeywordAttackAbility(gameData, playerId, attacker, TRAINING_ABILITY,
                    "'s training", entry -> { })) {
                gameLogService.append(gameData,
                        GameLog.builder().card(attacker.getCard()).text("'s training triggers.").build());
            }
        }

        // Double team: "when this creature attacks, if it's not a token, conjure a duplicate of it into your hand. Then
        // both cards perpetually lose double team." Each instance of the keyword triggers separately.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.DOUBLE_TEAM)
                    || attacker.getCard().isToken()) {
                continue;
            }
            int doubleTeamInstances = gameQueryService.triggerKeywordInstances(
                    gameData, attacker, Keyword.DOUBLE_TEAM);
            List<CardEffect> doubleTeamEffects = new ArrayList<>();
            doubleTeamEffects.add(new PerpetuallyRemoveKeywordEffect(Keyword.DOUBLE_TEAM));
            conjureDoubleTeamDuplicate(attacker.getCard()).ifPresent(doubleTeamEffects::add);
            TriggeredAbility doubleTeamAbility = new TriggeredAbility(Triggers.attacks(),
                    doubleTeamEffects.toArray(CardEffect[]::new));
            for (int instance = 0; instance < doubleTeamInstances; instance++) {
                if (putKeywordAttackAbility(gameData, playerId, attacker, doubleTeamAbility, "'s double team",
                        entry -> {
                            entry.setTargetId(attacker.getId());
                            entry.setNonTargeting(true);
                        })) {
                    gameLogService.append(gameData,
                            GameLog.builder().card(attacker.getCard()).text("'s double team triggers.").build());
                }
            }
        }

        // Check for "whenever one or more creatures you control attack" triggers (ON_ALLY_CREATURES_ATTACK)
        // These fire once per combat (not per creature) when at least one creature attacks.
        // The attacker count is locked at trigger time via xValue (per MTG rules: creatures
        // removed before resolution still count, tokens entering attacking after don't).
        // Each ability, printed or granted, is one object on the stack (CR 603.3).
        Map<UUID, List<Integer>> declaredBands = new java.util.LinkedHashMap<>();
        for (int attackerIndex : attackerIndices) {
            Permanent attacker = battlefield.get(attackerIndex);
            if (attacker.getBandId() != null) {
                declaredBands.computeIfAbsent(attacker.getBandId(), ignored -> new ArrayList<>()).add(attackerIndex);
            }
        }
        List<AttackTriggerSource> attackTriggerSources = new ArrayList<>();
        Trigger youAttack = CombatWatch.ON_ALLY_CREATURES_ATTACK.trigger();
        for (Permanent perm : battlefield) {
            List<TriggeredAbility> youAttackAbilities = new ArrayList<>(
                    WatchedAbilities.printed(perm.getCard(), CombatWatch.ON_ALLY_CREATURES_ATTACK));
            youAttackAbilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, perm, youAttack));
            for (TriggeredAbility ability : youAttackAbilities) {
                // "Whenever two or more creatures you control attack in a band" (EACH_BAND): an ability of its own for
                // each declared band of two or more (CR 702.22c), whose attacking creatures are the band's.
                if (ability.options().multiplicity() == TriggerMultiplicity.EACH_BAND) {
                    for (List<Integer> members : declaredBands.values()) {
                        if (members.size() >= 2) {
                            attackTriggerSources.add(new AttackTriggerSource(perm, ability, List.copyOf(members)));
                        }
                    }
                    continue;
                }
                attackTriggerSources.add(new AttackTriggerSource(perm, ability, null));
            }
        }
        for (AttackTriggerSource triggerSource : attackTriggerSources) {
            Permanent perm = triggerSource.permanent();
            TriggeredAbility ability = triggerSource.ability();
            TriggerOptions options = ability.options();
            // "Whenever you attack with one or more Wraiths": the ability triggers only if a declared attacker matches
            // its filter (CR 603.2), and "they", "those creatures" and "their total power" are the matching attackers
            // as they were declared.
            PermanentPredicate attackerFilter = CombatAbilityReader.filter(ability, youAttack);
            List<Integer> declaredAttackerIndices = triggerSource.bandAttackerIndices() != null
                    ? triggerSource.bandAttackerIndices() : attackerIndices;
            List<Integer> sourceAttackerIndices = attackerFilter == null ? declaredAttackerIndices
                    : matchingAttackerIndices(gameData, battlefield, declaredAttackerIndices, perm, playerId,
                    attackerFilter);
            if (sourceAttackerIndices.isEmpty()) continue;

            // "Triggers only once each turn" (Akki Battle Squad) and "this ability triggers only once" (Ruby
            // Collector) are the ability's limit, which the stack door judges and marks for this ability of the
            // permanent: another limited ability of the same permanent keeps its own.
            boolean oncePerTurnForPlayer = options.limit() == TriggerLimit.FIRST_TIME_EACH_TURN;
            if ((options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN
                    && options.limit() != TriggerLimit.ONCE && !oncePerTurnForPlayer)
                    || (options.multiplicity() != TriggerMultiplicity.EACH
                    && options.multiplicity() != TriggerMultiplicity.EACH_BAND)) {
                throw new IllegalStateException("The \"whenever you attack\" collector reads no "
                        + options + ": " + ability);
            }
            // The ability's event condition (CR 603.2) and intervening "if" (CR 603.4), with the number of
            // attackers.
            ConditionContext youAttackContext = ConditionContext.forPermanent(perm, playerId)
                    .withXValue(sourceAttackerIndices.size());
            if (!conditionEvaluationService.eventConditionMet(gameData, options, youAttackContext)) {
                continue;
            }
            // "Whenever you attack with creatures with total power 12 or greater for the first time each turn"
            // (Desert Were-Worm, its total power the event condition) counts the player's qualifying attacks, not
            // each source's: sources present for that attack all trigger, later arrivals never do.
            if (oncePerTurnForPlayer && !isFirstQualifyingAttackThisTurn(gameData, playerId)) {
                continue;
            }
            // "They" / "those creatures" (GrantScope.DECLARED_ATTACKERS) and a band's creatures are the entry's
            // attacking creatures, recorded whatever the effects are; "that many" (The Ur-Dragon's "whenever one or
            // more Dragons you control attack, draw that many cards") and "each of them" (Vrestin) are the matching
            // attackers.
            boolean sourceAttacked = sourceAttackerIndices.stream().anyMatch(index -> battlefield.get(index) == perm);
            List<Permanent> attackingSnapshots = sourceAttackerIndices.stream()
                    .map(battlefield::get).map(attacker -> snapshotDeclaredAttacker(gameData, attacker)).toList();
            TriggeredAbilityRequest youAttackRequest = new TriggeredAbilityRequest(ability,
                    perm.getCard(), perm, playerId, perm.getCard().getName() + "'s attack trigger", youAttackContext,
                    entry -> {
                        entry.setXValue(sourceAttackerIndices.size());
                        if (attackerFilter != null) {
                            entry.setEventValue(sourceAttackerIndices.size());
                            entry.setTriggeringPermanentIds(sourceAttackerIndices.stream()
                                    .map(battlefield::get).map(Permanent::getId).toList());
                        }
                        entry.setAttackingPermanentSnapshots(attackingSnapshots);
                        entry.setSourcePermanentSnapshot(new Permanent(perm));
                    });
            boolean triggered = triggeredAbilityStackService.put(gameData, sourceAttacked
                    ? youAttackRequest.causedBySourceAttacking() : youAttackRequest.causedByAttack());
            if (!triggered) {
                continue;
            }
            if (oncePerTurnForPlayer) {
                markFirstQualifyingAttackThisTurn(gameData, playerId);
            }
            gameLogService.append(gameData,
                    GameLog.builder().card(perm.getCard()).text("'s attack ability triggers.").build());
        }

        checkCommandZoneAllyCreatureAttackTriggers(gameData, playerId, attackerIndices.size());

        if (!attackerIndices.isEmpty()) {
            triggerCollectionService.checkTemporaryGlobalAllyCreatureAttackTriggers(gameData, playerId);
        }

        // Check for "whenever you attack a player with one or more creatures" triggers. These
        // fire once for each distinct player directly attacked by the controller's creatures.
        Set<UUID> directlyAttackedPlayers = new LinkedHashSet<>();
        for (int idx : attackerIndices) {
            UUID attackedTargetId = resolvedTargets.get(idx);
            if (gameData.playerIds.contains(attackedTargetId)) {
                directlyAttackedPlayers.add(attackedTargetId);
            }
        }
        // "Whenever you attack a player or planeswalker" also fires once for each attacked planeswalker.
        // Attacked battles don't count (CR 508.1b: only players, planeswalkers and battles can be attacked,
        // and the ability names players and planeswalkers).
        Set<UUID> directlyAttackedPlaneswalkers = new LinkedHashSet<>();
        for (int idx : attackerIndices) {
            UUID attackedTargetId = resolvedTargets.get(idx);
            Permanent attackedPermanent = gameData.playerIds.contains(attackedTargetId)
                    ? null : gameQueryService.findPermanentById(gameData, attackedTargetId);
            if (attackedPermanent != null && gameQueryService.isPlaneswalker(gameData, attackedPermanent)) {
                directlyAttackedPlaneswalkers.add(attackedTargetId);
            }
        }
        List<Map.Entry<CombatWatch, UUID>> attackedDefenderSlots = new ArrayList<>();
        for (UUID attackedPlayerId : directlyAttackedPlayers) {
            attackedDefenderSlots.add(Map.entry(CombatWatch.ON_ALLY_CREATURES_ATTACK_PLAYER, attackedPlayerId));
            attackedDefenderSlots.add(Map.entry(
                    CombatWatch.ON_ALLY_CREATURES_ATTACK_PLAYER_OR_PLANESWALKER, attackedPlayerId));
        }
        for (UUID attackedPlaneswalkerId : directlyAttackedPlaneswalkers) {
            attackedDefenderSlots.add(Map.entry(
                    CombatWatch.ON_ALLY_CREATURES_ATTACK_PLAYER_OR_PLANESWALKER, attackedPlaneswalkerId));
        }
        for (Map.Entry<CombatWatch, UUID> attackedDefenderSlot : attackedDefenderSlots) {
            CombatWatch attackPlayerSlot = attackedDefenderSlot.getKey();
            UUID attackedPlayerId = attackedDefenderSlot.getValue();
            Trigger attackPlayer = attackPlayerSlot.trigger();
            int targetAttackerCount = (int) attackerIndices.stream()
                    .map(resolvedTargets::get)
                    .filter(attackedPlayerId::equals)
                    .count();
            for (Permanent perm : new ArrayList<>(battlefield)) {
                // Each ability's event condition ("with one or more Pirates", "with two or more creatures", a Class's
                // level; CR 603.2) and intervening "if" (CR 603.4) are judged against the attacked player or
                // planeswalker and the number of creatures attacking it.
                ConditionContext attackedContext = ConditionContext.forPermanent(perm, playerId)
                        .withTargetId(attackedPlayerId).withXValue(targetAttackerCount);
                List<TriggeredAbility> attackPlayerAbilities = new ArrayList<>(
                        WatchedAbilities.printed(perm.getCard(), attackPlayerSlot));
                attackPlayerAbilities.addAll(
                        grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, perm, attackPlayer));
                // Each ability is one object on the stack (CR 603.3), its target chosen for all its effects
                // (CR 603.3d); the entry records the attacked player or planeswalker and the number of creatures
                // attacking it whatever the effects are.
                for (TriggeredAbility ability : attackPlayerAbilities) {
                    requireNoFilterLimitOrMultiplicity(ability, attackPlayer);
                    if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), attackedContext)) {
                        continue;
                    }
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            ability, perm.getCard(), perm, playerId, perm.getCard().getName() + "'s attack trigger",
                            attackedContext, entry -> {
                                entry.setEventValue(targetAttackerCount);
                                entry.setAttackedTargetId(attackedPlayerId);
                                entry.setNonTargeting(true);
                                entry.setSourcePermanentSnapshot(new Permanent(perm));
                            }).causedByAttack());
                    if (triggered) {
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s attack ability triggers.").build());
                    }
                }
            }
        }

        // Check for "whenever a creature you control attacks" triggers (ON_ALLY_CREATURE_ATTACKS)
        // These fire once per attacking creature (not once per combat like ON_ALLY_CREATURES_ATTACK).
        // Supports card and permanent conditional effects to filter by the attacking creature.
        Trigger allyAttacks = CombatWatch.ON_ALLY_CREATURE_ATTACKS.trigger();
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (Permanent perm : battlefield) {
                List<TriggeredAbility> allyAttackAbilities = new ArrayList<>();
                if (!perm.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                    allyAttackAbilities.addAll(WatchedAbilities.printed(
                            LayerSystemService.currentCard(gameData, perm), CombatWatch.ON_ALLY_CREATURE_ATTACKS));
                }
                if (!gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    allyAttackAbilities.addAll(WatchedAbilities.temporary(perm, allyAttacks));
                    allyAttackAbilities.addAll(WatchedAbilities.persistent(perm, allyAttacks));
                }
                allyAttackAbilities.addAll(
                        grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, perm, allyAttacks));
                if (allyAttackAbilities.isEmpty()) continue;

                // Filter by the attacking permanent itself (e.g. Rage Forger — "a creature you control with a
                // +1/+1 counter on it attacks"): the trigger's filter.
                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceCardId(perm.getOriginalCard().getId())
                        .withSourceControllerId(playerId)
                        .withSourcePermanentId(perm.getId());
                // The ability's event condition ("attacks alone", exalted, CR 702.83a) and intervening "if" (CR 603.4:
                // a Class's level, "if it's the first combat phase", "if no other creatures are attacking that
                // player"), with the attacking creature.
                ConditionContext allyAttackContext = ConditionContext.forPermanent(perm, playerId)
                        .withTriggeringPermanentId(attacker.getId());
                for (TriggeredAbility ability : allyAttackAbilities) {
                    if (ability.options().limit() != TriggerLimit.NONE
                            || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
                        throw new IllegalStateException("The collector of " + allyAttacks + " reads no "
                                + ability.options() + ": " + ability);
                    }
                    PermanentPredicate filter = CombatAbilityReader.filter(ability, allyAttacks);
                    if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(
                            attacker, filter, filterContext)) {
                        continue;
                    }
                    if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(),
                            allyAttackContext)) {
                        continue;
                    }
                    putAllyCreatureAttackAbility(gameData, playerId, perm, attacker, ability, allyAttackContext);
                }
            }
        }

        // Granted exalted abilities are keyword-driven: each separate instance of granted Exalted on a
        // permanent (one per granting source) contributes its own +1/+1 trigger to a lone attacker.
        if (attackerIndices.size() == 1) {
            Permanent attacker = battlefield.get(attackerIndices.getFirst());
            for (Permanent perm : battlefield) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, perm)
                        || !gameQueryService.hasKeyword(gameData, perm, Keyword.EXALTED)) {
                    continue;
                }

                // A printed exalted ability is implemented as an ON_ALLY_CREATURE_ATTACKS trigger on the
                // card, so only the instances beyond the printed one are granted ones.
                int printedInstances = perm.getCard().getKeywords().contains(Keyword.EXALTED) ? 1 : 0;
                int instances = gameQueryService.triggerKeywordInstances(gameData, perm, Keyword.EXALTED)
                        - printedInstances;
                // Each granted instance is the ability "whenever a creature you control attacks alone, that creature
                // gets +1/+1 until end of turn" (CR 702.83a), put as a printed one is: the attacking creature caused it.
                for (int i = 0; i < instances; i++) {
                    putAllyCreatureAttackAbility(gameData, playerId, perm, attacker, GRANTED_EXALTED_ABILITY,
                            ConditionContext.forPermanent(perm, playerId)
                                    .withTriggeringPermanentId(attacker.getId()));
                }
            }
        }

        for (int idx : attackerIndices) {
            triggerCollectionService.checkPlanarAllyCreatureAttackTriggers(gameData, battlefield.get(idx));
        }
        triggerCollectionService.checkPlanarAllyCreaturesAttackTriggers(
                gameData, playerId, attackerIndices.size());

        // Graveyard-based "whenever you attack with N or more creatures" abilities (GRAVEYARD_ON_ALLY_CREATURES_ATTACK),
        // which trigger from the attacking player's graveyard; each is one object on the stack (CR 603.3). The entry
        // records the number of attackers as X and the card's graveyard incarnation: a card that leaves and comes back
        // before the ability resolves is a new object (CR 400.7).
        List<Card> attackingPlayersGraveyard = gameData.playerGraveyards.get(playerId);
        if (attackingPlayersGraveyard != null) {
            Trigger graveyardAttack = CombatWatch.GRAVEYARD_ON_ALLY_CREATURES_ATTACK.trigger();
            for (Card card : new ArrayList<>(attackingPlayersGraveyard)) {
                // Each ability's event condition ("with one or more Rats") and intervening "if" ("if there are
                // seven or more cards in your graveyard"), read from the card and its owner, with the number of
                // attackers.
                ConditionContext graveyardContext = new ConditionContext(playerId, null, null, card, false, false,
                        false, false, null, attackerIndices.size(), null, null, false);
                for (TriggeredAbility ability : WatchedAbilities.withWatchedTrigger(
                        gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card),
                        graveyardAttack, Zone.GRAVEYARD)) {
                    requireNoFilterLimitOrMultiplicity(ability, graveyardAttack);
                    if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), graveyardContext)) {
                        continue;
                    }
                    long graveyardVersion = gameData.graveyardEntryVersion(card.getId());
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            ability, card, null, playerId, card.getName() + "'s graveyard attack trigger",
                            graveyardContext, entry -> {
                                entry.setXValue(attackerIndices.size());
                                entry.setSourceGraveyardEntryVersion(graveyardVersion);
                            }).causedByAttack());
                    if (triggered) {
                        gameLogService.append(gameData,
                                GameLog.builder().card(card).text("'s graveyard attack ability triggers.").build());
                    }
                }
            }
        }

        // Exile-based "whenever you attack" abilities, which trigger once each combat while the face-up card is in
        // its owner's exile zone; each is one object on the stack (CR 603.3), with the number of attackers as X.
        if (!attackerIndices.isEmpty()) {
            Trigger exileAttack = CombatWatch.EXILE_ON_ALLY_CREATURES_ATTACK.trigger();
            for (ExiledCardEntry exiled : new ArrayList<>(gameData.exiledCards)) {
                if (exiled.faceDown() || !playerId.equals(exiled.ownerId())) {
                    continue;
                }
                ConditionContext exileContext = ConditionContext.forCard(exiled.card(), playerId)
                        .withXValue(attackerIndices.size());
                for (TriggeredAbility ability
                        : WatchedAbilities.printed(exiled.card(), CombatWatch.EXILE_ON_ALLY_CREATURES_ATTACK)) {
                    requireNoFilterLimitOrMultiplicity(ability, exileAttack);
                    if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), exileContext)) {
                        continue;
                    }
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            ability, exiled.card(), null, playerId, exiled.card().getName() + "'s exile attack trigger",
                            exileContext, entry -> entry.setXValue(attackerIndices.size())).causedByAttack());
                    if (triggered) {
                        gameLogService.append(gameData, GameLog.builder().card(exiled.card())
                                .text("'s exile attack ability triggers.").build());
                    }
                }
            }
        }

        // Check for "whenever a creature attacks you or a planeswalker you control" triggers
        // (ON_CREATURE_ATTACKS_YOU). These fire once per attacking creature, on the permanents of
        // the player being attacked (directly or via one of their planeswalkers). The attacking
        // creature is stored as a non-targeting targetId so the effect can act on it. A battle is
        // attacked rather than its protector (CR 506.2, CR 508.1b), so attacks on battles don't fire these.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            UUID attackedTargetId = resolvedTargets.get(idx);
            Permanent attackedBattle = gameData.playerIds.contains(attackedTargetId)
                    ? null : gameQueryService.findPermanentById(gameData, attackedTargetId);
            boolean battleAttacked = attackedBattle != null && gameQueryService.isBattle(gameData, attackedBattle);
            // CR 310.9d: for an attacked battle, "defending player" refers to its protector.
            UUID attackedPlayerId = gameData.playerIds.contains(attackedTargetId)
                    ? attackedTargetId
                    : battleAttacked
                    ? attackedBattle.getProtectorPlayerId()
                    : gameQueryService.findPermanentController(gameData, attackedTargetId);
            if (attackedPlayerId == null) continue;
            if (!battleAttacked) {
                triggerCollectionService.checkTemporaryGlobalCreatureAttacksYouTriggers(
                        gameData, attacker, attackedPlayerId, attackedTargetId);
            }
            List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(attackedPlayerId);
            if (defenderBattlefield == null) continue;
            for (Permanent perm : new ArrayList<>(defenderBattlefield)) {
                if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)
                        || gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                    continue;
                }
                List<CombatWatch> watches = new ArrayList<>();
                if (!battleAttacked) {
                    watches.add(CombatWatch.ON_CREATURE_ATTACKS_YOU);
                }
                if (gameData.playerIds.contains(attackedTargetId)) {
                    watches.add(CombatWatch.ON_CREATURE_ATTACKS_YOU_DIRECTLY);
                }
                watches.add(CombatWatch.ON_CREATURE_ATTACKS_DEFENDING_PLAYER);
                FilterContext attackedSourceContext = FilterContext.of(gameData)
                        .withSourceCardId(perm.getOriginalCard().getId())
                        .withSourceControllerId(attackedPlayerId)
                        .withSourcePermanentId(perm.getId());
                ConditionContext attackedContext = ConditionContext.forPermanent(perm, attackedPlayerId)
                        .withTriggeringPermanentId(attacker.getId()).withTargetId(attackedTargetId);
                for (CombatWatch watch : watches) {
                    for (TriggeredAbility ability : WatchedAbilities.printed(perm.getCard(), watch)) {
                        if (ability.options().limit() != TriggerLimit.NONE
                                || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
                            throw new IllegalStateException("The collector of " + watch.trigger() + " reads no "
                                    + ability.options() + ": " + ability);
                        }
                        // The trigger's filter on the attacking creature ("a creature with flying", Raking Canopy),
                        // and the ability's event condition and intervening "if" (Guardian of the Ages' "if this
                        // creature has defender", CR 603.4), judged as the creature is declared an attacker.
                        PermanentPredicate filter = CombatAbilityReader.filter(ability, watch.trigger());
                        if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(
                                attacker, filter, attackedSourceContext)) {
                            continue;
                        }
                        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(),
                                attackedContext)) {
                            continue;
                        }
                        // The attacking creature is "that creature", the entry's non-targeting target, and its
                        // controller "that creature's controller", whatever the effects are.
                        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                                ability, perm.getCard(), perm, attackedPlayerId, perm.getCard().getName() + "'s trigger",
                                attackedContext, entry -> {
                                    entry.setTargetId(attacker.getId());
                                    entry.setNonTargeting(true);
                                    entry.setTriggeringPermanentId(attacker.getId());
                                    entry.setTriggeringPermanentControllerId(playerId);
                                    entry.setSourcePermanentSnapshot(new Permanent(perm));
                                }).causedByAttack());
                        if (triggered) {
                            gameLogService.append(gameData,
                                    GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                        }
                    }
                }
            }
        }

        // Check for "whenever one or more creatures attack you" triggers (ON_CREATURES_ATTACK_YOU).
        // Unlike ON_CREATURE_ATTACKS_YOU these fire once per combat per attacked player, and only for
        // creatures attacking that player directly — attacking a planeswalker they control does not
        // count. Non-targeting effects size themselves off the attacking creatures at resolution;
        // targeted effects wait for the appropriate player to choose a target.
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
            Trigger creaturesAttackYou = CombatWatch.ON_CREATURES_ATTACK_YOU.trigger();
            List<Permanent> attackingYou = attackerIndices.stream()
                    .filter(idx -> attackedPlayerId.equals(resolvedTargets.get(idx)))
                    .map(battlefield::get)
                    .map(attacker -> snapshotDeclaredAttacker(gameData, attacker)).toList();
            for (Permanent perm : new ArrayList<>(defenderBattlefield)) {
                ConditionContext attackedContext = ConditionContext.forPermanent(perm, attackedPlayerId);
                // Each ability is one object on the stack (CR 603.3), its target chosen for all its effects
                // (CR 603.3d). The entry records the attacking player as the active player and the creatures
                // attacking this player whatever the effects are.
                for (TriggeredAbility ability
                        : WatchedAbilities.printed(perm.getCard(), CombatWatch.ON_CREATURES_ATTACK_YOU)) {
                    requireNoFilterLimitOrMultiplicity(ability, creaturesAttackYou);
                    if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), attackedContext)) {
                        continue;
                    }
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            ability, perm.getCard(), perm, attackedPlayerId, perm.getCard().getName() + "'s trigger",
                            attackedContext, entry -> {
                                entry.setNonTargeting(true);
                                entry.setActivePlayerId(playerId);
                                entry.setSourcePermanentSnapshot(new Permanent(perm));
                                entry.setAttackingPermanentSnapshots(attackingYou);
                            }).causedByAttack());
                    if (triggered) {
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                    }
                }
            }
        }

        // Check for "whenever an opponent attacks a planeswalker you control with one or more
        // creatures" triggers. These fire once per attacked planeswalker, no matter how many
        // creatures attack it, for each permanent of the defending player that has the ability.
        Set<UUID> attackedPlaneswalkerIds = new LinkedHashSet<>();
        for (int idx : attackerIndices) {
            UUID attackedTargetId = resolvedTargets.get(idx);
            if (gameData.playerIds.contains(attackedTargetId)) continue;
            UUID attackedPlayerId = gameQueryService.findPermanentController(gameData, attackedTargetId);
            if (attackedPlayerId == null || attackedPlayerId.equals(playerId)) continue;
            List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(attackedPlayerId);
            if (defenderBattlefield != null && defenderBattlefield.stream()
                    .anyMatch(permanent -> permanent.getId().equals(attackedTargetId)
                            && gameQueryService.isPlaneswalker(gameData, permanent))) {
                attackedPlaneswalkerIds.add(attackedTargetId);
            }
        }
        for (UUID attackedPlaneswalkerId : attackedPlaneswalkerIds) {
            UUID attackedPlayerId = gameQueryService.findPermanentController(gameData, attackedPlaneswalkerId);
            if (attackedPlayerId == null) continue;
            List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(attackedPlayerId);
            if (defenderBattlefield == null) continue;
            Trigger attacksPlaneswalker = CombatWatch.ON_OPPONENT_ATTACKS_PLANESWALKER_YOU_CONTROL.trigger();
            for (Permanent perm : new ArrayList<>(defenderBattlefield)) {
                ConditionContext context = ConditionContext.forPermanent(perm, attackedPlayerId);
                // Each ability is one object on the stack (CR 603.3); "that player" is the attacking player, the
                // entry's non-targeting target.
                for (TriggeredAbility ability : WatchedAbilities.printed(perm.getCard(),
                        CombatWatch.ON_OPPONENT_ATTACKS_PLANESWALKER_YOU_CONTROL)) {
                    requireNoFilterLimitOrMultiplicity(ability, attacksPlaneswalker);
                    if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                        continue;
                    }
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            ability, perm.getCard(), perm, attackedPlayerId, perm.getCard().getName() + "'s trigger",
                            context, entry -> {
                                entry.setTargetId(playerId);
                                entry.setNonTargeting(true);
                            }).causedByAttack());
                    if (triggered) {
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                    }
                }
            }
        }

        // The emblems' "whenever a creature attacks you" abilities (Garruk, Apex Predator's emblem).
        for (int idx : attackerIndices) {
            triggerCollectionService.checkEmblemAttackTriggers(gameData, battlefield.get(idx), resolvedTargets.get(idx),
                    playerId);
        }

        // Check for "whenever a creature attacks" triggers (ON_ANY_CREATURE_ATTACKS). These fire once
        // per attacking creature, on every permanent with this slot across all battlefields, regardless
        // of who controls the attacker or whom it attacks (e.g. Caltrops pings every attacker). The
        // attacking creature is stored as a non-targeting targetId so the effect can act on "it".
        // The trigger's filter restricts which attackers trigger the ability (Windreader Sphinx; "an equipped
        // creature you control" for Nahiri, Forged in Fury, judged from the ability's controller).
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            UUID attackedTargetId = attacker.getAttackTarget();
            Trigger anyAttack = CombatWatch.ON_ANY_CREATURE_ATTACKS.trigger();
            for (Map.Entry<UUID, List<Permanent>> bf : gameData.playerBattlefields.entrySet()) {
                UUID permController = bf.getKey();
                for (Permanent perm : new ArrayList<>(bf.getValue())) {
                    // A permanent that lost its abilities, or a face-down one, has no printed ability to trigger.
                    if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                        continue;
                    }
                    FilterContext sourceContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(permController)
                            .withSourcePermanentId(perm.getId());
                    ConditionContext anyAttackContext = ConditionContext.forPermanent(perm, permController)
                            .withTriggeringPermanentId(attacker.getId()).withTargetId(attackedTargetId);
                    for (TriggeredAbility ability
                            : WatchedAbilities.printed(perm.getCard(), CombatWatch.ON_ANY_CREATURE_ATTACKS)) {
                        if (ability.options().limit() != TriggerLimit.NONE
                                || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
                            throw new IllegalStateException("The collector of " + anyAttack + " reads no "
                                    + ability.options() + ": " + ability);
                        }
                        // The trigger's filter on the attacking creature (Windreader Sphinx), and the ability's event
                        // condition and intervening "if" (M'Baku's "if that player is the monarch", CR 603.4) against
                        // the attack target.
                        PermanentPredicate filter = CombatAbilityReader.filter(ability, anyAttack);
                        if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(
                                attacker, filter, sourceContext)) {
                            continue;
                        }
                        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(),
                                anyAttackContext)) {
                            continue;
                        }
                        // The attacking creature is "it", the entry's non-targeting target and triggering permanent,
                        // with its attack target and controller, whatever the effects are.
                        TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability, perm.getCard(), perm,
                                permController, perm.getCard().getName() + "'s trigger", anyAttackContext, entry -> {
                                    entry.setSourcePermanentSnapshot(new Permanent(perm));
                                    entry.setAttackingPermanentSnapshots(List.of(new Permanent(attacker)));
                                    entry.setTargetId(attacker.getId());
                                    entry.setNonTargeting(true);
                                    entry.setAttackedTargetId(attacker.getAttackTarget());
                                    entry.setTriggeringPermanentId(attacker.getId());
                                    entry.setTriggeringPermanentControllerId(
                                            gameQueryService.findPermanentController(gameData, attacker.getId()));
                                });
                        boolean triggered = triggeredAbilityStackService.put(gameData, perm == attacker
                                ? request.causedBySourceAttacking() : request.causedByAttack());
                        if (triggered) {
                            gameLogService.append(gameData,
                                    GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                        }
                    }
                }
            }
            checkGraveyardCreatureAttacksTriggers(gameData, attacker);
            triggerCollectionService.checkTemporaryGlobalCreatureAttackTriggers(gameData, attacker);
        }

        // Fire delayed "until your next turn, whenever a creature an opponent controls attacks, it
        // gets X/Y until end of turn" triggers (Jace, Architect of Thought's +1). The attacking
        // player is always the attackers' controller here, so a single controller comparison per
        // registered action decides whether it applies to this whole declaration.
        for (DelayedOpponentAttackerBoost boost : gameData.getDelayedActions(DelayedOpponentAttackerBoost.class)) {
            if (boost.controllerId().equals(playerId)) continue;
            for (int idx : attackerIndices) {
                Permanent attacker = battlefield.get(idx);
                StackEntry boostTrigger = TriggeredAbilityStackService.madeAbility(
                        boost.sourceCard(),
                        boost.controllerId(),
                        boost.sourceCard().getName() + "'s delayed trigger",
                        List.of(new BoostSelfEffect(boost.power(), boost.toughness())),
                        attacker.getId(),
                        attacker.getId());
                boostTrigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, boostTrigger);
                gameLogService.append(gameData, GameLog.cardTextCard(
                        boost.sourceCard(), " — ", attacker.getCard(),
                        " gets " + formatBoostPair(boost.power(), boost.toughness())
                                + " until end of turn."));
                log.info("Game {} - {} delayed opponent-attacker boost fires for {}",
                        gameData.id, boost.sourceCard().getName(), attacker.getCard().getName());
            }
        }

        // Check for "whenever a player attacks" triggers (ON_ANY_PLAYER_ATTACKS). Unlike ON_ALLY_CREATURES_ATTACK
        // these fire for any attacking player, on every permanent with the ability across all battlefields, and only
        // once per combat. The attacking player is stored as a non-targeting targetId so player-scoped effects can act
        // on "that player" (e.g. Total War's sweep of their non-attackers). "Whenever a player attacks one of your
        // opponents" fires once for each opponent attacked (checkPlayerAttacksYourOpponentTriggers).
        Trigger playerAttacks = CombatWatch.ON_ANY_PLAYER_ATTACKS.trigger();
        for (Map.Entry<UUID, List<Permanent>> bf : gameData.playerBattlefields.entrySet()) {
            UUID permController = bf.getKey();
            for (Permanent perm : new ArrayList<>(bf.getValue())) {
                checkPlayerAttacksYourOpponentTriggers(gameData, permController, perm, playerId,
                        attackerIndices.size(), resolvedTargets);
                // Each ability's filter ("one or more creatures that are enchanted by an Aura you control attack",
                // Killian), event condition ("with three or more creatures", "attacks enchanted player", CR 603.2) and
                // intervening "if" ("if Tahngarth is tapped", CR 603.4), against the attacking player and the number of
                // attackers; the intervening "if" stays wrapped for the check as the ability resolves.
                ConditionContext playerAttackContext = ConditionContext.forPermanent(perm, permController)
                        .withTargetId(playerId).withXValue(attackerIndices.size());
                for (TriggeredAbility ability
                        : WatchedAbilities.printed(perm.getCard(), CombatWatch.ON_ANY_PLAYER_ATTACKS)) {
                    if (ability.options().limit() != TriggerLimit.NONE
                            || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
                        throw new IllegalStateException("The collector of " + playerAttacks + " reads no "
                                + ability.options() + ": " + ability);
                    }
                    PermanentPredicate filter = CombatAbilityReader.filter(ability, playerAttacks);
                    List<Integer> triggeringAttackers = filter == null ? attackerIndices
                            : matchingAttackerIndices(gameData, battlefield, attackerIndices, perm, permController,
                            filter);
                    if (triggeringAttackers.isEmpty()
                            || !conditionEvaluationService.eventConditionMet(gameData, ability.options(),
                            playerAttackContext)) {
                        continue;
                    }
                    // "That player" is the attacking player, the entry's non-targeting target, and "those creatures"
                    // the attackers that triggered it, whatever the effects are.
                    List<Permanent> attackingSnapshots = triggeringAttackers.stream().map(battlefield::get)
                            .map(attacker -> snapshotDeclaredAttacker(gameData, attacker)).toList();
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            ability, perm.getCard(), perm, permController, perm.getCard().getName() + "'s trigger",
                            playerAttackContext, entry -> {
                                entry.setXValue(attackerIndices.size());
                                entry.setTargetId(playerId);
                                entry.setTriggeringPermanentControllerId(playerId);
                                entry.setNonTargeting(true);
                                entry.setSourcePermanentSnapshot(new Permanent(perm));
                                entry.setAttackingPermanentSnapshots(attackingSnapshots);
                            }).causedByAttack());
                    if (triggered) {
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                    }
                }
            }
        }

        processDelayedAttackerBoostTriggers(gameData, battlefield, attackerIndices);
        processDelayedAttackerKeywordGrantTriggers(gameData, battlefield, attackerIndices);
        processDelayedNontokenAttackTokenTriggers(gameData, battlefield, attackerIndices);
        processDelayedAttackTokenCreationTriggers(gameData, playerId, attackerIndices);
        processDelayedAttackDamageTriggers(gameData, playerId, attackerIndices);
        processDelayedAttackUntapTriggers(gameData, playerId, attackerIndices);
        processDelayedWatchedCreatureAttackTriggers(gameData, battlefield, attackerIndices);

        // APNAP: active player's triggers on bottom, non-active player's on top (resolves first)
        combatTriggerService.reorderTriggersAPNAP(gameData, stackSizeBeforeAttackTriggers, playerId);

        log.info("Game {} - {} declares {} attackers", gameData.id, playerName, attackerIndices.size());
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

        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }

        return CombatResult.AUTO_PASS_ONLY;
    }

    /**
     * "Whenever this creature attacks" (ON_ATTACK) of {@code attacker}: its printed abilities, the ones a static
     * ability grants it and the ones it was given until end of turn, until end of combat or for as long as it stays on
     * the battlefield. Each ability is judged once, from the ability (the trigger's filter on the creature and its
     * event condition; CR 603.2), and goes on the stack through the stack door as one object of all its effects
     * (CR 603.3), which judges its limit and intervening "if" (CR 603.2h, 603.4); two abilities are two objects,
     * whoever gave them to the creature (CR 603.3b). The entry records the attack whatever the effects are: the
     * attacked player, planeswalker or battle and the defending player (which stays the one the creature attacked if
     * an attacked planeswalker or battle leaves, CR 508.5), the number of attackers as X, the attacker as it attacked
     * and its power then ("mana value less than this creature's power" read after it left, Narset, Enlightened
     * Exile), and the creatures declared as attackers ("the other one you attacked with", Creepy Puppeteer).
     */
    private void collectSelfAttackAbilities(GameData gameData, UUID playerId, Permanent attacker,
                                            List<Integer> attackerIndices,
                                            Supplier<List<Permanent>> declaredAttackerSnapshots) {
        // An Equipment's printed attack trigger is "whenever equipped creature attacks", so it
        // doesn't fire when an animated Equipment attacks itself (Captain America's Shield).
        boolean equipmentAttackTrigger = gameQueryService.hasEffectiveSubtype(gameData, attacker, CardSubtype.EQUIPMENT)
                && !gameQueryService.hasReconfigure(gameData, attacker);
        Trigger onAttack = CombatWatch.ON_ATTACK.trigger();
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (!attacker.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, attacker)
                && !equipmentAttackTrigger) {
            abilities.addAll(WatchedAbilities.printed(attacker.getCard(), CombatWatch.ON_ATTACK));
        }
        // Continuously granted ON_ATTACK abilities (Thorncaster Sliver giving every Sliver
        // "Whenever this creature attacks, it deals 1 damage to any target").
        abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, attacker, onAttack));
        // A granted triggered ability is an ability of the creature, so losing all abilities removes it. Temporarily
        // granted attack abilities (Tower Above's "target creature blocks it this turn if able": the attacker is the
        // ability's source, which MustBlockSourceEffect reads from the stack entry); two Tower Aboves are two
        // triggers, each with its own target.
        if (!gameQueryService.hasLostAllAbilities(gameData, attacker)) {
            abilities.addAll(WatchedAbilities.temporary(attacker, onAttack));
            abilities.addAll(WatchedAbilities.persistent(attacker, onAttack));
        }
        // "You may exert this creature as it attacks" is an optional cost to attack (CR 701.43d, 508.1g), asked as the
        // creature attacks and carried out at once; its "when you do" is a triggered ability linked to it.
        if (gameQueryService.getActiveStaticEffects(gameData, attacker).stream()
                .anyMatch(effect -> effect instanceof ExertAsItAttacksEffect exert && (exert.condition() == null
                        || conditionEvaluationService.isMet(gameData, exert.condition(),
                        ConditionContext.forPermanent(attacker, playerId))))) {
            gameData.queueMayAbilityForPlayer(attacker.getCard(), playerId,
                    new MayEffect(SkipNextUntapEffect.exert(),
                            "Exert " + attacker.getCard().getName() + " as it attacks?"),
                    null, attacker.getId(), playerId, new Permanent(attacker));
        }
        // Each ability's event condition (CR 603.2: "attacks while you control a Dinosaur", "attacks alone") and
        // intervening "if" (CR 603.4), read from the attacker with the number of attackers and its attack target.
        ConditionContext attackTriggerContext = ConditionContext.forPermanent(attacker, playerId)
                .withXValue(attackerIndices.size()).withTargetId(attacker.getAttackTarget());
        UUID defendingPlayerId = defendingPlayerOf(gameData, attacker);
        for (TriggeredAbility ability : abilities) {
            TriggerOptions options = ability.options();
            // "One or more" is an attack-or-block ability's "blocks" once each combat (CR 509.3a); a creature attacks
            // once each combat anyway.
            if ((options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE_EACH_TURN
                    && options.limit() != TriggerLimit.FIRST_TIME_EACH_TURN)
                    || (options.multiplicity() != TriggerMultiplicity.EACH
                    && options.multiplicity() != TriggerMultiplicity.ONE_OR_MORE)) {
                throw new IllegalStateException("The attack collector reads no " + options + ": " + ability);
            }
            // "Whenever this creature attacks for the first time each turn" (Aurelia, the Warleader) is part of the
            // event (FIRST_TIME_EACH_TURN): the creature's first attack this turn, counting the attacks it made
            // without the ability. A later attack is not that event, so an extra combat phase can't loop.
            if (options.limit() == TriggerLimit.FIRST_TIME_EACH_TURN && attacker.getAttacksThisTurn() > 1) {
                continue;
            }
            PermanentPredicate filter = CombatAbilityReader.filter(ability, onAttack);
            if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, filter)) {
                continue;
            }
            if (!conditionEvaluationService.eventConditionMet(gameData, options, attackTriggerContext)) {
                continue;
            }
            boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                    attacker.getCard(), attacker, playerId, attacker.getCard().getName() + "'s attack trigger",
                    attackTriggerContext, entry -> {
                        entry.setXValue(attackerIndices.size());
                        entry.setAttackedTargetId(attacker.getAttackTarget());
                        entry.setDefendingPlayerId(defendingPlayerId);
                        entry.setSourcePermanentSnapshot(snapshotAttackingSource(gameData, attacker));
                        entry.setTriggeringPermanentPowerAtTrigger(
                                gameQueryService.getEffectivePower(gameData, attacker));
                        entry.setAttackingPermanentSnapshots(declaredAttackerSnapshots.get());
                    }).causedBySourceAttacking());
            if (triggered) {
                logSelfAttackTrigger(gameData, attacker);
            }
        }
    }

    /**
     * Throws when {@code ability} has a filter on the creature, a limit or a multiplicity for {@code watched}, which its
     * collector doesn't read: no card writes one.
     */
    private static void requireNoFilterLimitOrMultiplicity(TriggeredAbility ability, Trigger watched) {
        if (CombatAbilityReader.filter(ability, watched) != null
                || ability.options().limit() != TriggerLimit.NONE
                || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The collector of " + watched + " reads no filter, limit or "
                    + "multiplicity: " + ability);
        }
    }

    private void logSelfAttackTrigger(GameData gameData, Permanent attacker) {
        gameLogService.append(gameData,
                GameLog.builder().card(attacker.getCard()).text("'s attack ability triggers.").build());
        log.info("Game {} - {} attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
    }

    /**
     * Puts one "whenever a creature you control attacks" ability of {@code perm}, triggered by {@code attacker}, on the
     * stack through the stack door as one object of all its effects (CR 603.3). Its source is {@code perm}; the entry
     * records the attacked player, planeswalker or battle (Hellrider's DealDamageToAttackedTargetEffect), the
     * defending player, and the attacking creature as its triggering permanent and non-targeting "that creature"
     * (Shadow Puppeteers' "have it become"), which "have that creature deal 1 damage" (Rage Forger) reads as the damage
     * source, whatever the effects are.
     */
    private void putAllyCreatureAttackAbility(GameData gameData, UUID playerId, Permanent perm, Permanent attacker,
                                              TriggeredAbility ability, ConditionContext ifContext) {
        UUID attackedTargetId = attacker.getAttackTarget();
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability,
                perm.getCard(), perm, playerId, perm.getCard().getName() + "'s attack trigger", ifContext, entry -> {
                    entry.setSourcePermanentSnapshot(new Permanent(perm));
                    entry.setAttackedTargetId(attackedTargetId);
                    entry.setDefendingPlayerId(gameData.playerIds.contains(attackedTargetId)
                            ? attackedTargetId : gameQueryService.findPermanentController(gameData, attackedTargetId));
                    entry.setTargetId(attacker.getId());
                    entry.setNonTargeting(true);
                    entry.setTriggeringPermanentId(attacker.getId());
                    entry.setTriggeringPermanentOwnerId(gameData.defaultControllerOf(attacker.getId()));
                });
        boolean triggered = triggeredAbilityStackService.put(gameData,
                perm == attacker ? request.causedBySourceAttacking() : request.causedByAttack());
        if (triggered) {
            gameLogService.append(gameData,
                    GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
        }
    }

    /**
     * "Whenever a creature attacks" from a graveyard: "whenever your commander attacks" (Vivi's Persistence, Endless Ranks
     * of HYDRA), {@code Triggers.creatureAttacks()} filtered to a commander the card's owner owns. It triggers once for
     * each such creature declared as an attacker, whoever controls it, from its owner's graveyard; the trigger's filter
     * and the ability's options are judged from the card and its owner.
     */
    private void checkGraveyardCreatureAttacksTriggers(GameData gameData, Permanent attacker) {
        Trigger graveyardAttack = CombatWatch.GRAVEYARD_ON_ANY_CREATURE_ATTACKS.trigger();
        for (UUID graveyardOwnerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            if (graveyard == null) continue;
            for (Card card : new ArrayList<>(graveyard)) {
                FilterContext graveyardContext = FilterContext.of(gameData)
                        .withSourceCardId(card.getId())
                        .withSourceControllerId(graveyardOwnerId);
                ConditionContext conditionContext = ConditionContext.forCard(card, graveyardOwnerId)
                        .withTriggeringPermanentId(attacker.getId()).withTargetId(attacker.getAttackTarget());
                for (TriggeredAbility ability : WatchedAbilities.withWatchedTrigger(
                        gameQueryService.getEffectiveGraveyardTriggeredAbilities(gameData, card),
                        graveyardAttack, Zone.GRAVEYARD)) {
                    if (ability.options().limit() != TriggerLimit.NONE
                            || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
                        throw new IllegalStateException("The collector of " + graveyardAttack + " reads no "
                                + ability.options() + ": " + ability);
                    }
                    PermanentPredicate filter = CombatAbilityReader.filter(ability, graveyardAttack);
                    if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(
                            attacker, filter, graveyardContext)) {
                        continue;
                    }
                    if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), conditionContext)) {
                        continue;
                    }
                    long graveyardVersion = gameData.graveyardEntryVersion(card.getId());
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            ability, card, null, graveyardOwnerId, card.getName() + "'s graveyard attack trigger",
                            conditionContext, entry -> {
                                entry.setXValue(1);
                                entry.setSourceGraveyardEntryVersion(graveyardVersion);
                            }).causedByAttack());
                    if (triggered) {
                        gameLogService.append(gameData,
                                GameLog.builder().card(card).text("'s graveyard attack ability triggers.").build());
                    }
                }
            }
        }
    }

    /**
     * "Whenever you attack" of the face-up cards in {@code playerId}'s command zone (eminence): each ability is one
     * object on the stack (CR 603.3), with the number of attackers as X; its event condition ("with one or more
     * Knights"; CR 603.2) and intervening "if" (CR 603.4) are read with that number.
     */
    private void checkCommandZoneAllyCreatureAttackTriggers(GameData gameData, UUID playerId,
                                                              int attackerCount) {
        List<Card> commandZone = gameData.playerCommandZones.get(playerId);
        if (commandZone == null) {
            return;
        }
        Trigger commandZoneAttack = CombatWatch.COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK.trigger();
        for (Card card : new ArrayList<>(commandZone)) {
            if (gameData.faceDownCommandZoneCards.contains(card.getId())) {
                continue;
            }
            ConditionContext context = ConditionContext.forCard(card, playerId).withXValue(attackerCount);
            for (TriggeredAbility ability
                    : WatchedAbilities.printed(card, CombatWatch.COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK)) {
                requireNoFilterLimitOrMultiplicity(ability, commandZoneAttack);
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        card, null, playerId, card.getName() + "'s attack trigger", context,
                        entry -> entry.setXValue(attackerCount)).causedByAttack());
                if (triggered) {
                    gameLogService.append(gameData,
                            GameLog.builder().card(card).text("'s attack ability triggers.").build());
                }
            }
        }
    }

    private boolean beginEnlistmentChoice(GameData gameData, UUID playerId, List<Permanent> battlefield,
                                          List<Integer> attackerIndices, Map<Integer, UUID> resolvedTargets,
                                          List<Permanent> declaredAttackers) {
        List<UUID> enlistmentAttackers = attackerIndices.stream()
                .map(battlefield::get)
                .filter(attacker -> gameQueryService.hasKeyword(gameData, attacker, Keyword.ENLIST))
                .filter(attacker -> !eligibleEnlistmentSupporters(gameData, playerId, battlefield, Set.of())
                        .isEmpty())
                .map(Permanent::getId)
                .toList();
        if (enlistmentAttackers.isEmpty()) {
            return false;
        }

        MultiPermanentChoiceContext.Enlistment context = new MultiPermanentChoiceContext.Enlistment(
                playerId, attackerIndices, resolvedTargets, declaredAttackers, enlistmentAttackers,
                Set.of(), Map.of(), Map.of());
        beginNextEnlistmentChoice(gameData, battlefield, context);
        return true;
    }

    private void beginNextEnlistmentChoice(GameData gameData, List<Permanent> battlefield,
                                           MultiPermanentChoiceContext.Enlistment context) {
        List<UUID> remaining = context.remainingAttackerIds();
        while (!remaining.isEmpty()) {
            UUID attackerId = remaining.getFirst();
            Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
            List<UUID> validSupporters = attacker == null
                    ? List.of()
                    : eligibleEnlistmentSupporters(gameData, context.playerId(), battlefield,
                    context.usedSupporterIds());
            if (!validSupporters.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, context.playerId(), validSupporters, 1,
                        context, "Choose a creature to enlist " + attacker.getCard().getName()
                                + " or choose none.");
                return;
            }
            remaining = remaining.subList(1, remaining.size());
            context = new MultiPermanentChoiceContext.Enlistment(
                    context.playerId(), context.attackerIndices(), context.resolvedTargets(),
                    context.declaredAttackers(), remaining, context.usedSupporterIds(), context.boostPowers(),
                    context.enlistedSupporters());
        }
    }

    private List<UUID> eligibleEnlistmentSupporters(GameData gameData, UUID playerId,
                                                     List<Permanent> battlefield, Set<UUID> usedSupporterIds) {
        return battlefield.stream()
                .filter(permanent -> !usedSupporterIds.contains(permanent.getId()))
                .filter(permanent -> !permanent.isTapped() && !permanent.isAttacking())
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> !permanent.isSummoningSick()
                        || gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE))
                .map(Permanent::getId)
                .toList();
    }

    /** Applies one Enlist support choice and either opens the next choice or resumes combat setup. */
    public CombatResult completeEnlistmentChoice(GameData gameData, List<UUID> permanentIds,
                                                 MultiPermanentChoiceContext.Enlistment context) {
        List<UUID> remaining = context.remainingAttackerIds();
        if (remaining.isEmpty()) {
            return finishAttackerDeclaration(gameData, context.playerId(),
                    gameData.playerIdToName.get(context.playerId()),
                    gameData.playerBattlefields.get(context.playerId()), context.attackerIndices(),
                    context.resolvedTargets(), context.declaredAttackers(), context.boostPowers(),
                    context.enlistedSupporters());
        }

        UUID attackerId = remaining.getFirst();
        Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
        Set<UUID> usedSupporterIds = new HashSet<>(context.usedSupporterIds());
        Map<UUID, Integer> boostPowers = new LinkedHashMap<>(context.boostPowers());
        Map<UUID, UUID> enlistedSupporters = new LinkedHashMap<>(context.enlistedSupporters());
        if (attacker != null && permanentIds != null && permanentIds.size() == 1) {
            Permanent supporter = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
            List<UUID> eligibleIds = eligibleEnlistmentSupporters(gameData, context.playerId(),
                    gameData.playerBattlefields.get(context.playerId()), usedSupporterIds);
            if (supporter != null && eligibleIds.contains(supporter.getId())) {
                supporter.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(
                        gameData, supporter, context.playerId());
                usedSupporterIds.add(supporter.getId());
                gameData.creaturesThatEnlistedThisCombat.add(attacker.getId());
                boostPowers.put(attacker.getId(), gameQueryService.getEffectivePower(gameData, supporter));
                enlistedSupporters.put(attacker.getId(), supporter.getId());
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(context.playerId()) + " taps "
                                + supporter.getCard().getName() + " to enlist "
                                + attacker.getCard().getName() + "."));
            }
        }

        List<UUID> nextRemaining = remaining.subList(1, remaining.size());
        MultiPermanentChoiceContext.Enlistment nextContext = new MultiPermanentChoiceContext.Enlistment(
                context.playerId(), context.attackerIndices(), context.resolvedTargets(),
                context.declaredAttackers(), nextRemaining, usedSupporterIds, boostPowers, enlistedSupporters);
        List<Permanent> battlefield = gameData.playerBattlefields.get(context.playerId());
        if (!nextRemaining.isEmpty()) {
            beginNextEnlistmentChoice(gameData, battlefield, nextContext);
            if (gameData.interaction.isAwaitingInput()) {
                return null;
            }
        }
        return finishAttackerDeclaration(gameData, context.playerId(),
                gameData.playerIdToName.get(context.playerId()), battlefield,
                context.attackerIndices(), context.resolvedTargets(), context.declaredAttackers(), boostPowers,
                enlistedSupporters);
    }

    /**
     * Enlist's triggered ability ("when you do, this creature gets +X/+0 until end of turn, where X is the tapped
     * creature's power", CR 702.154a) of each attacker that enlisted a creature, then the attacker's "whenever this
     * creature enlists a creature" abilities. The enlisted creature is the entry's chosen permanent, whose power X reads.
     * Its trigger event is the enlist cost's tap, not the attack, so it isn't an ability its source's attacking caused:
     * Isshin, Two Heavens as One doesn't double it (only conditions directly related to attacking) and Firebender
     * Ascension doesn't count it.
     */
    private void addEnlistmentTriggers(GameData gameData, UUID playerId, Map<UUID, Integer> boostPowers,
                                       Map<UUID, UUID> enlistedSupporters) {
        for (Map.Entry<UUID, Integer> boost : boostPowers.entrySet()) {
            Permanent attacker = gameQueryService.findPermanentById(gameData, boost.getKey());
            if (attacker == null) {
                continue;
            }
            UUID enlistedId = enlistedSupporters.get(boost.getKey());
            if (triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ENLIST_ABILITY,
                    attacker.getCard(), attacker, playerId, attacker.getCard().getName() + "'s enlist trigger",
                    ConditionContext.forPermanent(attacker, playerId), entry -> {
                        entry.setTargetId(attacker.getId());
                        entry.setNonTargeting(true);
                        entry.setChosenPermanentId(enlistedId);
                        entry.setSourcePermanentSnapshot(new Permanent(attacker));
                    }))) {
                gameLogService.append(gameData,
                        GameLog.builder().card(attacker.getCard()).text("'s enlist ability triggers.").build());
            }
            Permanent enlistedCreature = gameQueryService.findPermanentById(gameData, enlistedId);
            if (enlistedCreature != null) {
                collectEnlistAbilities(gameData, playerId, attacker, enlistedCreature);
            }
        }
    }

    /**
     * "Whenever this creature enlists a [filter] creature" abilities of {@code attacker} ({@code Triggers.enlists()};
     * Guardian of New Benalia, Goblin Morale Sergeant, Benalish Knight-Counselor): they trigger as its controller pays
     * its enlist cost by tapping {@code enlisted} (CR 702.154c), the trigger's filter judged on the enlisted creature as
     * it is then, with its event condition. Each ability goes on the stack through the stack door as one object
     * (CR 603.3), the enlisted creature its triggering permanent ("that creature", read as the ability resolves, else
     * as it last existed, CR 608.2h). Enlisting is no attack, so Isshin, Two Heavens as One doesn't apply. A creature
     * that has lost its abilities or is face down has none.
     */
    private void collectEnlistAbilities(GameData gameData, UUID playerId, Permanent attacker, Permanent enlisted) {
        Trigger enlists = CombatWatch.ON_ENLISTS.trigger();
        List<TriggeredAbility> enlistAbilities = new ArrayList<>();
        if (!attacker.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, attacker)) {
            enlistAbilities.addAll(WatchedAbilities.printed(attacker.getCard(), CombatWatch.ON_ENLISTS));
        }
        if (!gameQueryService.hasLostAllAbilities(gameData, attacker)) {
            enlistAbilities.addAll(WatchedAbilities.temporary(attacker, enlists));
            enlistAbilities.addAll(WatchedAbilities.persistent(attacker, enlists));
            enlistAbilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, attacker, enlists));
        }
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(attacker.getOriginalCard().getId())
                .withSourceControllerId(playerId)
                .withSourcePermanentId(attacker.getId());
        ConditionContext conditionContext = ConditionContext.forPermanent(attacker, playerId)
                .withTriggeringPermanentId(enlisted.getId());
        for (TriggeredAbility ability : enlistAbilities) {
            if (ability.options().limit() != TriggerLimit.NONE
                    || ability.options().multiplicity() != TriggerMultiplicity.EACH) {
                throw new IllegalStateException("The collector of " + enlists + " reads no " + ability.options()
                        + ": " + ability);
            }
            PermanentPredicate filter = CombatAbilityReader.filter(ability, enlists);
            if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(
                    enlisted, filter, filterContext)) {
                continue;
            }
            if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), conditionContext)) {
                continue;
            }
            boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                    attacker.getCard(), attacker, playerId, attacker.getCard().getName() + "'s enlist trigger",
                    conditionContext, entry -> {
                        entry.setNonTargeting(true);
                        entry.setSourcePermanentSnapshot(new Permanent(attacker));
                        entry.setTriggeringPermanentId(enlisted.getId());
                        entry.setTriggeringPermanentControllerId(playerId);
                        entry.setTriggeringPermanent(enlisted);
                    }));
            if (triggered) {
                gameLogService.append(gameData, GameLog.abilityTriggers(attacker.getCard()));
            }
        }
    }

    /**
     * Returns the player who declares this combat's attackers: the active player unless a
     * "you choose which creatures attack this turn" effect is in force.
     */
    private UUID attackerDeclarationChooser(GameData gameData, UUID activePlayerId) {
        List<DelayedAttackerDeclarationControl> controls =
                gameData.getDelayedActions(DelayedAttackerDeclarationControl.class);
        return controls.isEmpty() ? activePlayerId : controls.getLast().chooserId();
    }

    /**
     * Puts {@code ability}, an ability a keyword of {@code attacker} gives it, on the stack through the stack door as one
     * object, described as the attacker's name and {@code description}: its source attacking caused it to trigger
     * (CR 508.3a), so the "triggers an additional time" effects that count those count it, and the "a creature you
     * control attacking causes a triggered ability of that creature to trigger" watchers see it. {@code record} stamps
     * what the attack was.
     *
     * @return whether it triggered
     */
    private boolean putKeywordAttackAbility(GameData gameData, UUID playerId, Permanent attacker,
                                            TriggeredAbility ability, String description,
                                            TriggeredAbilityRequest.TriggerRecord record) {
        return triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, attacker.getCard(),
                attacker, playerId, attacker.getCard().getName() + description,
                ConditionContext.forPermanent(attacker, playerId), record).causedBySourceAttacking());
    }

    /**
     * "Whenever a player attacks one of your opponents" (ON_ANY_PLAYER_ATTACKS_YOUR_OPPONENT) and "whenever an opponent
     * attacks another one of your opponents" (ON_OPPONENT_ATTACKS_ANOTHER_OPPONENT) of {@code source}: once for each of
     * its controller's opponents attacked (CR 603.2c), each ability its own object on the stack (CR 603.3). The ability's
     * event condition and intervening "if" ("if that opponent has more life than another of your opponents", Breena)
     * are judged against that opponent as the attacked target; the entry carries it as its attacked target, so the
     * intervening "if" reads it again as the ability resolves (CR 603.4), and the attacking player as its
     * non-targeting target.
     */
    private void checkPlayerAttacksYourOpponentTriggers(GameData gameData, UUID sourceControllerId, Permanent source,
                                                        UUID attackingPlayerId, int attackerCount,
                                                        Map<Integer, UUID> resolvedTargets) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        List<CombatWatch> watches = sourceControllerId.equals(attackingPlayerId)
                ? List.of(CombatWatch.ON_ANY_PLAYER_ATTACKS_YOUR_OPPONENT)
                : List.of(CombatWatch.ON_ANY_PLAYER_ATTACKS_YOUR_OPPONENT, CombatWatch.ON_OPPONENT_ATTACKS_ANOTHER_OPPONENT);
        for (CombatWatch watch : watches) {
            for (TriggeredAbility ability : WatchedAbilities.printed(source.getCard(), watch)) {
                requireNoFilterLimitOrMultiplicity(ability, watch.trigger());
                abilities.add(ability);
            }
        }
        if (abilities.isEmpty()) {
            return;
        }
        // One ability for each opponent attacked; "that player" is the attacking player, the entry's non-targeting
        // target, and the attacked opponent its attacked target, whatever the effects are.
        for (UUID attackedOpponentId : attackedOpponents(gameData, sourceControllerId, resolvedTargets)) {
            ConditionContext attackedOpponentContext = ConditionContext.forPermanent(source, sourceControllerId)
                    .withTargetId(attackingPlayerId).withAttackedTargetId(attackedOpponentId)
                    .withXValue(attackerCount);
            for (TriggeredAbility ability : abilities) {
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(),
                        attackedOpponentContext)) {
                    continue;
                }
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        source.getCard(), source, sourceControllerId, source.getCard().getName() + "'s trigger",
                        attackedOpponentContext, entry -> {
                            entry.setXValue(attackerCount);
                            entry.setTargetId(attackingPlayerId);
                            entry.setTriggeringPermanentControllerId(attackingPlayerId);
                            entry.setAttackedTargetId(attackedOpponentId);
                            entry.setSourcePermanentSnapshot(new Permanent(source));
                            entry.setNonTargeting(true);
                        }).causedByAttack());
                if (triggered) {
                    gameLogService.append(gameData,
                            GameLog.builder().card(source.getCard()).text("'s ability triggers.").build());
                }
            }
        }
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
                StackEntry se = TriggeredAbilityStackService.madeAbility(
                        boost.sourceCard(),
                        boost.controllerId(),
                        boost.sourceCard().getName() + "'s delayed trigger",
                        List.of(new BoostSelfEffect(boost.power(), boost.toughness())),
                        attacker.getId(),
                        attacker.getId());
                se.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, se);
                gameLogService.append(gameData, GameLog.cardTextCard(
                        boost.sourceCard(), " — ", attacker.getCard(),
                        " gets +" + boost.power() + "/+" + boost.toughness() + " until end of turn."));
                log.info("Game {} - {} delayed attacker boost fires for {}",
                        gameData.id, boost.sourceCard().getName(), attacker.getCard().getName());
            }
        }
    }

    /** Fires delayed triggers for a watched creature attacking one of its registering player's opponents. */
    private void processDelayedWatchedCreatureAttackTriggers(GameData gameData,
                                                              List<Permanent> battlefield,
                                                              List<Integer> attackerIndices) {
        if (attackerIndices.isEmpty() || !gameData.hasDelayedAction(DelayedWatchedCreatureAttack.class)) {
            return;
        }
        for (DelayedWatchedCreatureAttack watch
                : gameData.getDelayedActions(DelayedWatchedCreatureAttack.class)) {
            for (int idx : attackerIndices) {
                Permanent attacker = battlefield.get(idx);
                UUID attackedTargetId = attacker.getAttackTarget();
                if (!watch.watchedPermanentId().equals(attacker.getId())
                        || attackedTargetId == null
                        || !gameData.playerIds.contains(attackedTargetId)
                        || watch.controllerId().equals(attackedTargetId)) {
                    continue;
                }

                StackEntry trigger = TriggeredAbilityStackService.madeAbility(
                        watch.sourceCard(),
                        watch.controllerId(),
                        watch.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(watch.effects()),
                        (UUID) null,
                        attacker.getId());
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData,
                        GameLog.builder().card(watch.sourceCard()).text("'s ability triggers.").build());
            }
        }
    }

    /**
     * Delayed attack triggers that grant keywords until end of turn. One stack entry is created
     * for each attacker and each registered grant.
     */
    private void processDelayedAttackerKeywordGrantTriggers(GameData gameData,
                                                              List<Permanent> battlefield,
                                                              List<Integer> attackerIndices) {
        if (attackerIndices.isEmpty() || !gameData.hasDelayedAction(DelayedAttackerKeywordGrant.class)) {
            return;
        }
        for (DelayedAttackerKeywordGrant grant
                : gameData.getDelayedActions(DelayedAttackerKeywordGrant.class)) {
            for (int idx : attackerIndices) {
                Permanent attacker = battlefield.get(idx);
                StackEntry se = TriggeredAbilityStackService.madeAbility(
                        grant.sourceCard(),
                        grant.controllerId(),
                        grant.sourceCard().getName() + "'s delayed trigger",
                        List.of(new GrantKeywordEffect(
                                grant.keywords(), GrantScope.TARGET, null,
                                GrantDuration.END_OF_TURN, null)),
                        attacker.getId(),
                        (UUID) null);
                se.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, se);
                gameLogService.append(gameData, GameLog.cardTextCard(
                        grant.sourceCard(), " — ", attacker.getCard(),
                        " gains " + grant.keywords() + " until end of turn."));
                log.info("Game {} - {} delayed attacker keyword grant fires for {}",
                        gameData.id, grant.sourceCard().getName(), attacker.getCard().getName());
            }
        }
    }

    private void processDelayedNontokenAttackTokenTriggers(GameData gameData, List<Permanent> battlefield,
                                                            List<Integer> attackerIndices) {
        if (attackerIndices.isEmpty() || !gameData.hasDelayedAction(DelayedNontokenAttackTokenCreation.class)) return;
        int nontokenAttackers = (int) attackerIndices.stream()
                .map(battlefield::get)
                .filter(attacker -> !attacker.getCard().isToken())
                .count();
        if (nontokenAttackers == 0) return;

        for (DelayedNontokenAttackTokenCreation action
                : gameData.getDelayedActions(DelayedNontokenAttackTokenCreation.class)) {
            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    action.sourceCard(),
                    action.controllerId(),
                    action.sourceCard().getName() + "'s delayed trigger",
                    List.of(new CreateTokensAttackingEffect(nontokenAttackers, action.tokenEffect())));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s delayed ability triggers."));
            log.info("Game {} - {} delayed token creation trigger fires for {} nontoken attacker(s)",
                    gameData.id, action.sourceCard().getName(), nontokenAttackers);
        }
    }

    private void processDelayedAttackUntapTriggers(GameData gameData, UUID attackingPlayerId,
                                                    List<Integer> attackerIndices) {
        if (attackerIndices.isEmpty() || !gameData.hasDelayedAction(DelayedAttackUntap.class)) return;

        List<DelayedAttackUntap> matchingActions = gameData.getDelayedActions(DelayedAttackUntap.class).stream()
                .filter(action -> action.controllerId().equals(attackingPlayerId))
                .toList();
        if (matchingActions.isEmpty()) return;

        for (DelayedAttackUntap action : matchingActions) {
            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    action.sourceCard(),
                    action.controllerId(),
                    action.sourceCard().getName() + "'s delayed trigger",
                    List.of(new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                            new PermanentIsCreaturePredicate())));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s delayed ability triggers."));
            log.info("Game {} - {} delayed attack untap trigger fires", gameData.id, action.sourceCard().getName());
        }

        gameData.clearDelayedActions(DelayedAttackUntap.class,
                action -> action.controllerId().equals(attackingPlayerId));
    }

    private void processDelayedAttackTokenCreationTriggers(GameData gameData, UUID attackingPlayerId,
                                                            List<Integer> attackerIndices) {
        if (attackerIndices.isEmpty() || !gameData.hasDelayedAction(DelayedAttackTokenCreation.class)) return;

        for (DelayedAttackTokenCreation action : gameData.getDelayedActions(DelayedAttackTokenCreation.class)) {
            if (!action.controllerId().equals(attackingPlayerId)) continue;

            if (action.attackerPredicate() == null) {
                addDelayedAttackTokenCreationTrigger(gameData, action);
                continue;
            }

            for (int idx : attackerIndices) {
                Permanent attacker = gameData.playerBattlefields.get(attackingPlayerId).get(idx);
                if (!predicateEvaluationService.matchesPermanentPredicate(
                        gameData, attacker, action.attackerPredicate())) {
                    continue;
                }

                addDelayedAttackTokenCreationTrigger(gameData, action);
            }
        }
    }

    private void addDelayedAttackTokenCreationTrigger(GameData gameData, DelayedAttackTokenCreation action) {
        StackEntry entry = TriggeredAbilityStackService.madeAbility(
                action.sourceCard(),
                action.controllerId(),
                action.sourceCard().getName() + "'s delayed trigger",
                List.of(new CreateTokensAttackingEffect(
                        action.amount(), action.tokenEffect(), action.sacrificeAtEndStep())));
        entry.setNonTargeting(true);
        triggeredAbilityStackService.putDelayed(gameData, entry);
        gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s delayed ability triggers."));
        log.info("Game {} - {} delayed attack token creation trigger fires",
                gameData.id, action.sourceCard().getName());
    }

    private void processDelayedAttackDamageTriggers(GameData gameData, UUID attackingPlayerId,
                                                    List<Integer> attackerIndices) {
        if (attackerIndices.isEmpty() || !gameData.hasDelayedAction(DelayedAttackDamage.class)) return;

        for (DelayedAttackDamage action : gameData.getDelayedActions(DelayedAttackDamage.class)) {
            if (!action.controllerId().equals(attackingPlayerId)) continue;

            // "that creature" refers to the creature chosen when the delayed ability was created: the
            // trigger doesn't target again, so control changes and later hexproof don't stop it.
            Permanent target = gameQueryService.findPermanentById(gameData, action.targetPermanentId());
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }

            StackEntry entry = TriggeredAbilityStackService.madeAbility(
                    action.sourceCard(),
                    action.controllerId(),
                    action.sourceCard().getName() + "'s delayed trigger",
                    List.of(new DealDamageToTargetCreatureEffect(new PermanentCount(
                            new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER))),
                    action.targetPermanentId(),
                    action.sourcePermanentId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s delayed ability triggers."));
            log.info("Game {} - {} delayed attack damage trigger fires", gameData.id,
                    action.sourceCard().getName());
        }
    }

    private void validateMaximumAttackRequirements(GameData gameData, UUID playerId,
                                                    List<Integer> attackableIndices,
                                                    Set<Integer> declaredAttackerIndices,
                                                    Map<Integer, UUID> attackTargets) {
        if (!castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);

        List<Integer> restrictionValidGroupAttackers = getRestrictionValidGroupAttackers(
                gameData, playerId, attackableIndices);
        int maxRequirements = 0;
        for (int idx : restrictionValidGroupAttackers) {
            maxRequirements += getMaximumUnpaidAttackRequirementCount(
                    gameData, battlefield.get(idx));
        }
        for (int idx : attackableIndices) {
            if (isRestrictionValidSingleton(gameData, battlefield, idx)) {
                maxRequirements = Math.max(maxRequirements,
                        getMaximumUnpaidAttackRequirementCount(
                                gameData, battlefield.get(idx)));
            }
        }

        UUID defaultTargetId = gameQueryService.getOpponentId(gameData, playerId);
        int satisfiedRequirements = 0;
        for (int idx : declaredAttackerIndices) {
            UUID targetId = attackTargets != null ? attackTargets.get(idx) : null;
            if (targetId == null) {
                targetId = defaultTargetId;
            }
            satisfiedRequirements += attackLegalityService.getMustAttackRequirementCount(
                    gameData, battlefield.get(idx), targetId);
        }

        if (satisfiedRequirements < maxRequirements) {
            Set<Integer> restrictionValidAttackers = new HashSet<>(restrictionValidGroupAttackers);
            for (int idx : attackableIndices) {
                if (isRestrictionValidSingleton(gameData, battlefield, idx)) {
                    restrictionValidAttackers.add(idx);
                }
            }
            for (int idx : restrictionValidAttackers) {
                if (!declaredAttackerIndices.contains(idx)
                        && getMaximumUnpaidAttackRequirementCount(
                        gameData, battlefield.get(idx)) > 0) {
                    throw new IllegalStateException("Creature at index " + idx + " must attack this combat");
                }
            }
            throw new IllegalStateException("Attack declaration satisfies too few attack requirements");
        }
    }

    private int getMaximumUnpaidAttackRequirementCount(GameData gameData, Permanent creature) {
        UUID controllerId = gameData.findControllerOf(creature);
        if (controllerId == null) return 0;
        return attackLegalityService.getValidAttackTargetIds(gameData, controllerId).stream()
                .filter(targetId -> attackLegalityService.canAttackDefender(gameData, creature, targetId))
                .filter(targetId -> castingCostService.getAttackPaymentPerCreature(
                        gameData, controllerId, targetId, creature) == 0)
                .filter(targetId -> castingCostService.getAttackLifePaymentPerCreature(
                        gameData, controllerId, targetId, creature) == 0)
                .mapToInt(targetId -> attackLegalityService.getMustAttackRequirementCount(gameData, creature, targetId))
                .max().orElse(0);
    }

    /**
     * Returns true if an opponent controls a permanent with
     * {@link OpponentsMustAttackRequirementEffect} and at least one of this player's attackable
     * creatures can attack a required target without paying any attack cost. CR 508.1d: the player
     * is not required to pay optional attack costs to satisfy the requirement, so a requirement whose
     * only satisfying attacks are taxed is not enforced.
     */
    public boolean isOpponentForcedToAttack(GameData gameData, UUID playerId) {
        return isOpponentRequirementEnforced(gameData, playerId,
                getAttackableCreatureIndices(gameData, playerId));
    }

    private boolean isOpponentRequirementEnforced(GameData gameData, UUID playerId, List<Integer> attackableIndices) {
        if (attackableIndices.isEmpty()
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return false;
        }
        Set<UUID> requiredTargetIds = getOpponentRequiredAttackTargetIds(gameData, playerId);
        if (requiredTargetIds.isEmpty()) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        for (int idx : attackableIndices) {
            Permanent creature = battlefield.get(idx);
            for (UUID targetId : requiredTargetIds) {
                if (attackLegalityService.canAttackDefender(gameData, creature, targetId)
                        && castingCostService.getAttackPaymentPerCreature(gameData, playerId, targetId, creature) == 0
                        && castingCostService.getAttackLifePaymentPerCreature(gameData, playerId, targetId, creature) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the attack targets named by opponents' {@link OpponentsMustAttackRequirementEffect}
     * permanents: the controller of each such permanent and the planeswalkers that controller controls.
     */
    private Set<UUID> getOpponentRequiredAttackTargetIds(GameData gameData, UUID playerId) {
        Set<UUID> requiredTargetIds = new HashSet<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.isLosesAllAbilitiesUntilEndOfTurn()
                        || gameQueryService.computeStaticBonus(gameData, perm).losesAllAbilities()) {
                    continue;
                }
                boolean requiresAttack = perm.getCard().getStaticEffects().stream()
                        .anyMatch(OpponentsMustAttackRequirementEffect.class::isInstance);
                if (!requiresAttack) continue;
                requiredTargetIds.add(pid);
                for (Permanent target : bf) {
                    if (gameQueryService.isPlaneswalker(gameData, target)) {
                        requiredTargetIds.add(target.getId());
                    }
                }
            }
        }
        return requiredTargetIds;
    }

    private void validateOpponentRequiredAttackTargets(GameData gameData, UUID playerId,
                                                       List<Integer> attackableIndices,
                                                       List<Integer> attackerIndices,
                                                       Map<Integer, UUID> attackTargets) {
        if (!isOpponentRequirementEnforced(gameData, playerId, attackableIndices)) {
            return;
        }
        Set<UUID> requiredTargetIds = getOpponentRequiredAttackTargetIds(gameData, playerId);
        UUID defaultTargetId = gameQueryService.getOpponentId(gameData, playerId);
        for (int idx : attackerIndices) {
            UUID targetId = attackTargets != null ? attackTargets.get(idx) : null;
            if (targetId == null) {
                targetId = defaultTargetId;
            }
            if (requiredTargetIds.contains(targetId)) {
                return;
            }
        }
        throw new IllegalStateException("Must attack with at least one creature");
    }

    /**
     * Ekundu Cyclops (CR 508.1d): a creature with a conditional "if a creature you control
     * attacks, this creature also attacks if able" requirement must be declared whenever any
     * other creature its controller controls is declared as an attacker. The requirement is
     * waived when attacking would cost the player mana (CR 508.1d — optional attack costs are
     * never mandatory), matching {@code validateMaximumAttackRequirements}. The requirement is also
     * waived when the Cyclops has lost all abilities, or when it or another declared attacker can only
     * attack alone, since it then cannot be declared alongside the other creature.
     */
    private void validateAttacksAlongsideOtherCreature(GameData gameData, UUID playerId,
                                                       List<Integer> attackableIndices,
                                                       Set<Integer> declaredAttackerIndices) {
        if (castingCostService.hasAttackPaymentForAnyCreature(gameData, playerId)
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        for (int idx : attackableIndices) {
            if (declaredAttackerIndices.contains(idx)) {
                continue;
            }
            Permanent creature = battlefield.get(idx);
            boolean conditional = !gameQueryService.hasLostAllAbilities(gameData, creature)
                    && creature.getCard().getStaticEffects().stream()
                    .anyMatch(MustAttackIfAnotherCreatureAttacksEffect.class::isInstance);
            if (conditional && declaredAttackerIndices.stream().anyMatch(other -> other != idx)
                    && !canOnlyAttackAlone(gameData, creature)
                    && declaredAttackerIndices.stream()
                    .noneMatch(attacker -> canOnlyAttackAlone(gameData, battlefield.get(attacker)))) {
                throw new IllegalStateException(creature.getCard().getName()
                        + " must also attack when another creature you control attacks");
            }
        }
    }

    private void validateOpponentCreaturesAttackTogether(GameData gameData, UUID playerId,
                                                         List<Integer> attackableIndices,
                                                         Set<Integer> declaredAttackerIndices) {
        if (declaredAttackerIndices.isEmpty()
                || castingCostService.hasAttackPaymentForAnyCreature(gameData, playerId)
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()
                || !hasOpponentCreaturesAttackTogetherEffect(gameData, playerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        // CR 508.1d: a creature that can only attack alone can't be attacking with the others, so the
        // "attacks if able" requirements of the other creatures can't all be obeyed without disobeying it.
        if (declaredAttackerIndices.stream().anyMatch(idx -> canOnlyAttackAlone(gameData, battlefield.get(idx)))) {
            return;
        }
        for (int idx : attackableIndices) {
            if (!declaredAttackerIndices.contains(idx)
                    && !canOnlyAttackAlone(gameData, battlefield.get(idx))) {
                throw new IllegalStateException(battlefield.get(idx).getCard().getName()
                        + " must also attack when another creature you control attacks");
            }
        }
    }

    private void validateOtherCreaturesAttackWithSource(GameData gameData, UUID playerId,
                                                       List<Integer> attackableIndices,
                                                       Set<Integer> declaredAttackerIndices) {
        if (castingCostService.hasAttackPaymentForAnyCreature(gameData, playerId)
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        boolean sourceAttacks = declaredAttackerIndices.stream().map(battlefield::get)
                .anyMatch(source -> !source.isFaceDown() && !gameQueryService.hasLostAllAbilities(gameData, source)
                        && source.getCard().getStaticEffects().stream()
                        .anyMatch(OtherCreaturesMustAttackIfSourceAttacksEffect.class::isInstance));
        if (!sourceAttacks) return;
        if (declaredAttackerIndices.stream()
                .anyMatch(attacker -> canOnlyAttackAlone(gameData, battlefield.get(attacker)))) {
            return;
        }
        for (int index : attackableIndices) {
            Permanent other = battlefield.get(index);
            if (!declaredAttackerIndices.contains(index)
                    && !canOnlyAttackAlone(gameData, other)
                    && gameQueryService.getCreatureAttackTax(gameData, other) == 0) {
                throw new IllegalStateException(other.getCard().getName()
                        + " must attack when the source creature attacks");
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
        if (castingCostService.hasAttackPaymentForAnyCreature(gameData, playerId)
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
                if (creature.getCounterCount(counterType) > 0
                        && !canOnlyAttackAlone(gameData, creature)
                        && declaredAttackerIndices.stream()
                        .noneMatch(attacker -> canOnlyAttackAlone(gameData, battlefield.get(attacker)))) {
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
        return gameQueryService.getActiveStaticEffects(gameData, creature).stream()
                .anyMatch(effect -> effect instanceof CantAttackOrBlockAloneEffect restriction
                        && restriction.restrictsAttacking())
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
            if (!hasGreaterPowerRestriction(restricted)
                    || gameQueryService.hasLostPrintedAbilities(gameData, restricted)) {
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
        return creature.getCard().getStaticEffects().stream()
                .anyMatch(CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect.class::isInstance);
    }

    /**
     * Orcish Conscripts (CR 508.1a): a creature with "can't attack unless at least N other
     * creatures also attack" may only be declared as an attacker if at least N other creatures
     * are declared as attackers in the same combat. Checked only at declaration time.
     */
    private void validateCountAlsoAttacks(GameData gameData, List<Permanent> battlefield,
                                          List<Integer> attackerIndices) {
        for (int idx : attackerIndices) {
            Permanent restricted = battlefield.get(idx);
            if (gameQueryService.hasLostPrintedAbilities(gameData, restricted)) continue;
            restricted.getCard().getStaticEffects().stream()
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
            if (gameQueryService.hasLostPrintedAbilities(gameData, restricted)) continue;
            restricted.getCard().getStaticEffects().stream()
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
     * stamp with band ids. Each band must: contain at least one declared attacker, include at least
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
                List<Permanent> bandPermanents = members.stream().map(battlefield::get).toList();
                if (!gameQueryService.canUseBandsWithOther(gameData, bandPermanents, false)) {
                    throw new IllegalStateException("A band must contain at least one creature with banding");
                }
            }
            if (withoutBanding > 1 && !gameQueryService.canUseBandsWithOther(
                    gameData, members.stream().map(battlefield::get).toList())) {
                throw new IllegalStateException("A band can contain at most one creature without banding");
            }
            result.add(members);
        }
        return result;
    }

    /** The declared attackers that match {@code predicate}, judged for {@code source}'s ability as it triggers. */
    private List<Integer> matchingAttackerIndices(
            GameData gameData, List<Permanent> battlefield, List<Integer> attackerIndices,
            Permanent source, UUID controllerId, PermanentPredicate predicate) {
        FilterContext sourceContext = FilterContext.of(gameData)
                .withSourceCardId(source.getOriginalCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(source.getId());
        return attackerIndices.stream()
                .filter(index -> predicateEvaluationService.matchesPermanentPredicate(
                        battlefield.get(index), predicate, sourceContext))
                .toList();
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
    private static final String FIRST_QUALIFYING_ATTACK_KEY = "firstQualifyingAttackInPhase:";

    /**
     * Whether no qualifying attack by {@code playerId} was recorded earlier this turn than the current combat
     * phase. Sources that trigger off the same attack declaration all see the same phase and so all trigger.
     */
    private boolean isFirstQualifyingAttackThisTurn(GameData gameData, UUID playerId) {
        String current = FIRST_QUALIFYING_ATTACK_KEY + gameData.combatPhasesThisTurn;
        return gameData.keyedOncePerTurnTriggersFiredThisTurn.getOrDefault(playerId, java.util.Set.of()).stream()
                .filter(key -> key.startsWith(FIRST_QUALIFYING_ATTACK_KEY))
                .allMatch(current::equals);
    }

    private void markFirstQualifyingAttackThisTurn(GameData gameData, UUID playerId) {
        gameData.keyedOncePerTurnTriggersFiredThisTurn
                .computeIfAbsent(playerId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(FIRST_QUALIFYING_ATTACK_KEY + gameData.combatPhasesThisTurn);
    }

    private Permanent snapshotAttackingSource(GameData gameData, Permanent attacker) {
        Permanent snapshot = new Permanent(attacker);
        snapshot.setLastKnownPower(gameQueryService.getEffectivePower(gameData, attacker));
        return snapshot;
    }

    private Permanent snapshotDeclaredAttacker(GameData gameData, Permanent attacker) {
        Permanent snapshot = new Permanent(attacker);
        Card card = attacker.getCard().createRuntimeCopy();
        Set<com.github.laxika.magicalvibes.model.CardType> types = gameQueryService.getEffectiveCardTypes(gameData, attacker);
        card.setType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
        card.setAdditionalTypes(types);
        card.setSupertypes(java.util.Arrays.stream(com.github.laxika.magicalvibes.model.CardSupertype.values())
                .filter(type -> gameQueryService.hasEffectiveSupertype(gameData, attacker, type))
                .collect(Collectors.toSet()));
        snapshot.setCard(card);
        snapshot.setLastKnownPower(gameQueryService.getEffectivePower(gameData, attacker));
        return snapshot;
    }

    private static String formatBoostPair(int power, int toughness) {
        String sign = (power < 0 || toughness < 0) ? "-" : "+";
        return sign + Math.abs(power) + "/" + sign + Math.abs(toughness);
    }
    private Set<UUID> attackedOpponents(GameData gameData, UUID controllerId,
                                         Map<Integer, UUID> resolvedTargets) {
        return resolvedTargets.values().stream()
                .filter(gameData.playerIds::contains)
                .filter(targetId -> !controllerId.equals(targetId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * The player {@code attacker} attacks, the controller of the planeswalker it attacks or the protector of the battle
     * it attacks (CR 508.5), or {@code null} when it attacks nothing.
     */
    private UUID defendingPlayerOf(GameData gameData, Permanent attacker) {
        UUID attackedTargetId = attacker.getAttackTarget();
        if (attackedTargetId == null || gameData.playerIds.contains(attackedTargetId)) {
            return attackedTargetId;
        }
        Permanent attackedPermanent = gameQueryService.findPermanentById(gameData, attackedTargetId);
        return attackedPermanent != null && gameQueryService.isBattle(gameData, attackedPermanent)
                ? attackedPermanent.getProtectorPlayerId()
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }

    /**
     * Double team's duplicate is a fresh card of the attacker's printing, without double team. A
     * card that carries no printing of its own (built straight from its class) is found by name.
     */
    private Optional<ConjureCardToHandEffect> conjureDoubleTeamDuplicate(Card attackerCard) {
        if (attackerCard.getSetCode() != null && attackerCard.getCollectorNumber() != null) {
            return Optional.of(ConjureCardToHandEffect.withoutKeyword(
                    attackerCard.getSetCode(), attackerCard.getCollectorNumber(), Keyword.DOUBLE_TEAM));
        }
        return cardCatalog.findLoadedPrintingByName(attackerCard.getName())
                .map(printing -> ConjureCardToHandEffect.withoutKeyword(
                        printing.setCode(), printing.collectorNumber(), Keyword.DOUBLE_TEAM));
    }
}

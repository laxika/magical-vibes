package com.github.laxika.magicalvibes.service.combat.attack;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DeclarationReplacementEffect;
import com.github.laxika.magicalvibes.model.rules.TapEvent;
import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatAttackTarget;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.rules.AttackEvent;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyRemoveKeywordEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatCreatureLimitEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithCounterAttackTogetherEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TrainEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksPlayerWithMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.combat.CombatCompanionRestrictions;
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
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
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
            PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

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
            BoostEffect.self(new ChosenPermanentPower(), new Fixed(0)));

    /**
     * An exalted a static ability grants: "Whenever a creature you control attacks alone, that creature gets +1/+1
     * until end of turn" (CR 702.83a); its collector judges that the creature attacks alone.
     */
    private static final TriggeredAbility GRANTED_EXALTED_ABILITY = new TriggeredAbility(
            Triggers.creatureYouControlAttacks(), BoostEffect.target(1, 1));

    private final GameQueryService gameQueryService;
    private final AttackLegalityService attackLegalityService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final CastingCostService castingCostService;
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
    private final CombatCompanionRestrictions combatCompanionRestrictions;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.trigger.pipeline.AttackTriggers attackTriggers;
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
     * static ability, while it has it (a face-down creature, or one that lost its abilities, has
     * not), or from an attached Aura's restriction on it (Errantry).
     */
    public boolean canOnlyAttackAlone(GameData gameData, Permanent creature) {
        boolean selfRestricted = gameQueryService.hasActiveStaticEffectIncludingGranted(gameData, creature,
                CanOnlyAttackAloneEffect.class);
        return selfRestricted || gameQueryService.hasAttachedPermanentRestriction(gameData, creature,
                CanOnlyAttackAloneEffect.class::isInstance);
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
        for (GameQueryService.ActiveStaticEffect<CombatCreatureLimitEffect> active
                : gameQueryService.activeStaticEffects(gameData, CombatCreatureLimitEffect.class)) {
            CombatCreatureLimitEffect limit = active.effect();
            if (!filterByAttackTarget
                    || limit.appliesToAttackTarget(active.controllerId(), active.source().getId(), attackTargetId)) {
                maximum[0] = Math.min(maximum[0], limit.maxAttackers());
            }
        }
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
     * Returns the attackable creatures with an "attacks if able" requirement that attack in a declaration obeying the
     * most requirements possible without disobeying a restriction or paying a cost to attack (CR 508.1d). When the
     * restrictions let only some of them attack, these are one such choice.
     */
    public List<Integer> getMustAttackIndices(GameData gameData, UUID playerId, List<Integer> attackableIndices) {
        return gameQueryService.withQueryScope(gameData,
                () -> getMustAttackIndicesUnscoped(gameData, playerId, attackableIndices));
    }

    private List<Integer> getMustAttackIndicesUnscoped(GameData gameData, UUID playerId,
                                                        List<Integer> attackableIndices) {
        if (castingCostService.hasAttackLifePaymentForAnyCreature(gameData, playerId)) {
            return List.of();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        RequirementDeclaration best = searchAttackRequirements(
                gameData, playerId, attackableIndices, Integer.MAX_VALUE);
        List<Integer> mustAttack = new ArrayList<>();
        for (int idx : attackableIndices) {
            UUID targetId = best.attackers().get(idx);
            if (targetId != null && attackLegalityService.getMustAttackRequirementCount(
                    gameData, battlefield.get(idx), targetId) > 0) {
                mustAttack.add(idx);
            }
        }
        return mustAttack;
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
        return combatCompanionRestrictions.firstUnmet(gameData, attacker, true,
                attackerIndices.stream().map(battlefield::get).toList()) != null;
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
        List<GameQueryService.ActiveStaticEffect<CreaturesWithCounterAttackTogetherEffect>> requirements =
                gameQueryService.activeStaticEffects(gameData, CreaturesWithCounterAttackTogetherEffect.class);
        boolean added;
        do {
            added = false;
            for (var requirement : requirements) {
                if (!attackTogetherApplies(gameData, battlefield, attackingIndices, requirement)) {
                    continue;
                }
                for (int idx : attackableIndices) {
                    if (idx < 0 || idx >= battlefield.size() || attackingIndices.contains(idx)) {
                        continue;
                    }
                    if (attackTogetherRequires(gameData, battlefield.get(idx), requirement)
                            && gameQueryService.getCreatureAttackTax(gameData, battlefield.get(idx)) == 0) {
                        mustAttack.add(idx);
                        attackingIndices.add(idx);
                        added = true;
                    }
                }
            }
        } while (added);
        return new ArrayList<>(mustAttack);
    }

    /**
     * Whether a declared attacker among {@code attackingIndices} matches the attacker filter of an
     * {@link CreaturesWithCounterAttackTogetherEffect} requirement, judged with the requirement's permanent as source.
     */
    private boolean attackTogetherApplies(GameData gameData, List<Permanent> battlefield, Collection<Integer> attackingIndices,
                                          GameQueryService.ActiveStaticEffect<CreaturesWithCounterAttackTogetherEffect> requirement) {
        FilterContext context = attackTogetherContext(gameData, requirement);
        return attackingIndices.stream()
                .filter(idx -> idx >= 0 && idx < battlefield.size())
                .anyMatch(idx -> predicateEvaluationService.matchesPermanentPredicate(battlefield.get(idx),
                        requirement.effect().attackers(), context));
    }

    /** Whether {@code creature} is one the requirement says attacks if able. */
    private boolean attackTogetherRequires(GameData gameData, Permanent creature,
                                           GameQueryService.ActiveStaticEffect<CreaturesWithCounterAttackTogetherEffect> requirement) {
        return predicateEvaluationService.matchesPermanentPredicate(creature, requirement.effect().required(),
                attackTogetherContext(gameData, requirement));
    }

    private static FilterContext attackTogetherContext(GameData gameData,
                                                       GameQueryService.ActiveStaticEffect<?> requirement) {
        return FilterContext.of(gameData)
                .withSourceCardId(requirement.source().getCard().getId())
                .withSourcePermanentId(requirement.source().getId())
                .withSourceControllerId(requirement.controllerId());
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

        // "can't attack unless at least N other [creatures matching a filter] also attack" (Okk, Orcish Conscripts)
        validateCompanionsAlsoAttack(gameData, battlefield, attackerIndices);

        // Validate attack requirements (CR 508.1d: satisfy as many as possible)
        validateMaximumAttackRequirements(gameData, playerId, attackable, uniqueIndices, attackTargets);

        // "If [a creature] attacks, [these creatures] attack if able" (Ekundu Cyclops, Viashino Bey, War's Toll,
        // Magnetic Web)
        validateAttackTogetherRequirements(gameData, playerId, attackable, uniqueIndices, attackTargets);

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
            UUID requiredDefenderId = gameData.mustAttackDefender(attacker.getId());
            if (requiredDefenderId != null
                    && validTargetIds.contains(requiredDefenderId)
                    && attackLegalityService.canAttackDefender(gameData, attacker, requiredDefenderId)
                    && castingCostService.getAttackPaymentPerCreature(gameData, playerId,
                    requiredDefenderId, attacker) == 0
                    && castingCostService.getAttackLifePaymentPerCreature(gameData, playerId,
                    requiredDefenderId, attacker) == 0
                    && !requiredDefenderId.equals(targetId)) {
                throw new IllegalStateException(FaceDownVisibility.publicName(gameData, attacker) + " must attack the specified player");
            }
            if (gameQueryService.computeStaticBonus(gameData, attacker).grantedEffects().stream()
                    .anyMatch(MustAttackPlayerEffect.class::isInstance)
                    && attackLegalityService.canAttackAPlayer(gameData, attacker, playerId)
                    && !gameData.playerIds.contains(targetId)) {
                throw new IllegalStateException(FaceDownVisibility.publicName(gameData, attacker) + " must attack a player");
            }
            if (attackLegalityService.mustAttackOtherPlayerIfAble(gameData, attacker, targetId)) {
                throw new IllegalStateException(FaceDownVisibility.publicName(gameData, attacker)
                        + " must attack a player other than you if able");
            }
            // Defender-scoped restriction (e.g. Form of the Dragon — "Creatures without flying can't attack you"):
            // the attacked player controls a permanent that forbids attackers not matching its exemption predicate.
            if (!attackLegalityService.canAttackDefender(gameData, attacker, targetId)) {
                throw new IllegalStateException(FaceDownVisibility.publicName(gameData, attacker) + " can't attack that player");
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
            ActionEvents.taxPaid(
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
                ActionEvents.taxPaid(
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
        gameData.beginPermanentTapTriggerBatch();
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
                gameData.rulesEvents.occur(new TapEvent.BecameTapped(attacker, null, true));
            }
        }
        gameData.endPermanentTapTriggerBatch();

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
            queueExertAsItAttacks(gameData, playerId, attacker);
            if (decayedTrigger) {
                putKeywordAttackAbility(gameData, playerId, attacker, DECAYED_ABILITY, "'s decayed trigger", entry -> {
                    entry.setXValue(attackerIndices.size());
                    entry.setSourcePermanentSnapshot(new Permanent(attacker));
                });
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
                BoostEffect.self(meleeBoost, meleeBoost));
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

        // Every attack ability of a permanent or a card in another zone triggers on the declaration (CR 508.3a), matched
        // by the trigger pipeline as one occurrence.
        Map<UUID, UUID> attackTargets = new LinkedHashMap<>();
        List<UUID> attackerIds = new ArrayList<>();
        List<Permanent> declaredSnapshots = new ArrayList<>();
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            attackerIds.add(attacker.getId());
            attackTargets.put(attacker.getId(), resolvedTargets.get(idx));
            declaredSnapshots.add(snapshotDeclaredAttacker(gameData, attacker));
        }
        Map<UUID, UUID> enlisted = new LinkedHashMap<>();
        enlistmentBoosts.keySet().forEach(attackerId -> {
            if (enlistedSupporters.get(attackerId) != null) {
                enlisted.put(attackerId, enlistedSupporters.get(attackerId));
            }
        });
        // "Whenever [a player] attacks" triggers if one or more creatures are declared as attackers (CR 508.3d).
        if (!attackerIds.isEmpty()) {
            AttackEvent attackEvent = new AttackEvent(playerId, attackerIds, attackTargets, declaredSnapshots,
                    enlisted);
            gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(attackEvent));
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
                    attackTriggers.putCreatureYouControlAttacks(gameData, playerId, perm, attacker,
                            GRANTED_EXALTED_ABILITY, ConditionContext.forPermanent(perm, playerId)
                                    .withTriggeringPermanentId(attacker.getId()));
                }
            }
        }

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
     * "You may exert this creature as it attacks" is an optional cost to attack (CR 701.43d, 508.1g), asked as the
     * creature attacks and carried out at once; its "when you do" is a triggered ability linked to it.
     */
    private void queueExertAsItAttacks(GameData gameData, UUID playerId, Permanent attacker) {
        if (gameQueryService.getActiveStaticEffects(gameData, attacker).stream()
                .anyMatch(effect -> effect instanceof ExertAsItAttacksEffect exert && (exert.condition() == null
                        || conditionEvaluationService.isMet(gameData, exert.condition(),
                        ConditionContext.forPermanent(attacker, playerId))))) {
            gameData.queueMayAbilityForPlayer(attacker.getCard(), playerId,
                    new MayEffect(SkipNextUntapEffect.exert(),
                            "Exert " + FaceDownVisibility.publicName(gameData, attacker) + " as it attacks?"),
                    null, attacker.getId(), playerId, new Permanent(attacker));
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
                        context, "Choose a creature to enlist " + FaceDownVisibility.publicName(gameData, attacker)
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
                gameData.rulesEvents.occur(new TapEvent.BecameTapped(supporter, context.playerId(), false));
                usedSupporterIds.add(supporter.getId());
                gameData.creaturesThatEnlistedThisCombat.add(attacker.getId());
                boostPowers.put(attacker.getId(), gameQueryService.getEffectivePower(gameData, supporter));
                enlistedSupporters.put(attacker.getId(), supporter.getId());
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(context.playerId()) + " taps "
                                + FaceDownVisibility.publicName(gameData, supporter) + " to enlist "
                                + FaceDownVisibility.publicName(gameData, attacker) + "."));
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
                    attacker.getCard(), attacker, playerId, FaceDownVisibility.publicName(gameData, attacker) + "'s enlist trigger",
                    ConditionContext.forPermanent(attacker, playerId), entry -> {
                        entry.setTargetId(attacker.getId());
                        entry.setNonTargeting(true);
                        entry.setChosenPermanentId(enlistedId);
                        entry.setSourcePermanentSnapshot(new Permanent(attacker));
                    }))) {
                gameLogService.append(gameData,
                        GameLog.builder().card(attacker.getCard()).text("'s enlist ability triggers.").build());
            }
        }
    }

    /**
     * Returns the player who declares this combat's attackers: the active player unless a
     * "you choose which creatures attack this turn" effect is in force.
     */
    private UUID attackerDeclarationChooser(GameData gameData, UUID activePlayerId) {
        var controls = gameData.declarationReplacements(DeclarationReplacementEffect.Kind.CONTROLLER_CHOOSES_ATTACKERS);
        return controls.isEmpty() ? activePlayerId : controls.getLast().controllerId();
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
                attacker, playerId, FaceDownVisibility.publicName(gameData, attacker) + description,
                ConditionContext.forPermanent(attacker, playerId), record).causedBySourceAttacking());
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
     * CR 508.1d: the declaration is illegal when it obeys fewer attack requirements than some declaration that disobeys
     * no restriction and needs no cost to attack could obey.
     */
    private void validateMaximumAttackRequirements(GameData gameData, UUID playerId,
                                                    List<Integer> attackableIndices,
                                                    Set<Integer> declaredAttackerIndices,
                                                    Map<Integer, UUID> attackTargets) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
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

        RequirementDeclaration better = searchAttackRequirements(
                gameData, playerId, attackableIndices, satisfiedRequirements);
        if (better.obeyed() > satisfiedRequirements) {
            for (Map.Entry<Integer, UUID> attacker : new TreeMap<>(better.attackers()).entrySet()) {
                if (!declaredAttackerIndices.contains(attacker.getKey())
                        && attackLegalityService.getMustAttackRequirementCount(
                        gameData, battlefield.get(attacker.getKey()), attacker.getValue()) > 0) {
                    throw new IllegalStateException("Creature at index " + attacker.getKey()
                            + " must attack this combat");
                }
            }
            throw new IllegalStateException("Attack declaration satisfies too few attack requirements");
        }
    }

    /** Upper bound on the declarations an attack-requirement search visits before it settles for what it found. */
    private static final int REQUIREMENT_SEARCH_BUDGET = 200_000;

    /**
     * One creature a requirement search may declare: the attack targets it can attack without its player paying a
     * cost, each with the number of attack requirements attacking that target obeys, best first.
     */
    private record AttackOptions(int index, List<UUID> targets, List<Integer> requirementCounts) {

        int maximum() {
            return requirementCounts.isEmpty() ? 0 : requirementCounts.getFirst();
        }
    }

    /**
     * The best declaration a requirement search found ({@code attackers} mapped to what each attacks) and the number of
     * attack requirements it obeys.
     */
    private record RequirementDeclaration(Map<Integer, UUID> attackers, int obeyed) {
    }

    /**
     * CR 508.1d: a declaration of attackers obeying the most attack requirements possible without disobeying any
     * restriction (who may attack, how many creatures may attack, which creatures must or can't attack together) and
     * without its player paying a cost to attack. The search stops once it finds one obeying more than
     * {@code enough}; with {@code Integer.MAX_VALUE} it finds a best one.
     */
    private RequirementDeclaration searchAttackRequirements(GameData gameData, UUID playerId,
                                                            List<Integer> attackableIndices, int enough) {
        RequirementDeclaration none = new RequirementDeclaration(Map.of(), 0);
        if (!castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return none;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return none;
        }
        Set<UUID> validTargetIds = attackLegalityService.getValidAttackTargetIds(gameData, playerId);
        List<AttackOptions> options = new ArrayList<>();
        for (int idx : attackableIndices) {
            Permanent creature = battlefield.get(idx);
            if (requiresNonManaAttackCost(gameData, playerId, creature, idx)) {
                continue;
            }
            List<Map.Entry<UUID, Integer>> targets = new ArrayList<>();
            for (UUID targetId : validTargetIds) {
                if (attackLegalityService.canAttackDefender(gameData, creature, targetId)
                        && castingCostService.getAttackPaymentPerCreature(gameData, playerId, targetId, creature) == 0
                        && castingCostService.getAttackLifePaymentPerCreature(
                        gameData, playerId, targetId, creature) == 0) {
                    targets.add(Map.entry(targetId, attackLegalityService.getMustAttackRequirementCount(
                            gameData, creature, targetId)));
                }
            }
            if (targets.isEmpty()) {
                continue;
            }
            targets.sort(Map.Entry.<UUID, Integer>comparingByValue().reversed()
                    .thenComparing(entry -> entry.getKey().toString()));
            options.add(new AttackOptions(idx, targets.stream().map(Map.Entry::getKey).toList(),
                    targets.stream().map(Map.Entry::getValue).toList()));
        }
        options.sort(Comparator.comparingInt(AttackOptions::maximum).reversed());
        int[] remaining = new int[options.size() + 1];
        for (int i = options.size() - 1; i >= 0; i--) {
            remaining[i] = remaining[i + 1] + options.get(i).maximum();
        }
        if (remaining[0] == 0) {
            return none;
        }
        AttackRequirementSearch search = new AttackRequirementSearch(gameData, playerId, battlefield, options,
                remaining, enough);
        search.visit(0, 0);
        return search.best;
    }

    /**
     * Whether attacking with {@code creature} needs its player to pay a cost other than the mana and life a target's
     * controller charges per attacker (CR 508.1d: the player is never required to pay it): its own attack tax, a
     * creature to tap, permanents to sacrifice or permanents to return to hand.
     */
    private boolean requiresNonManaAttackCost(GameData gameData, UUID playerId, Permanent creature, int index) {
        if (gameQueryService.getCreatureAttackTax(gameData, creature) > 0
                || combatTapCostService.hasCombatTapCost(gameData, creature)) {
            return true;
        }
        return attackSacrificeCostService.hasSacrificeAttackCost(gameData, playerId, creature)
                || attackReturnToHandCostService.hasReturnToHandAttackCost(gameData, playerId, index);
    }

    /**
     * A branch-and-bound search over declarations of attackers for {@link #searchAttackRequirements}: each creature in
     * turn attacks one of its targets or stays home, a branch is cut once it can't beat the best declaration found, and
     * once a restriction on how many creatures attack is already broken.
     */
    private final class AttackRequirementSearch {

        private final GameData gameData;
        private final UUID playerId;
        private final List<Permanent> battlefield;
        private final List<AttackOptions> options;
        private final int[] remaining;
        private final int enough;
        private final List<Integer> attackers = new ArrayList<>();
        private final Map<Integer, UUID> targets = new HashMap<>();
        private RequirementDeclaration best = new RequirementDeclaration(Map.of(), 0);
        private int visited;

        private AttackRequirementSearch(GameData gameData, UUID playerId, List<Permanent> battlefield,
                                        List<AttackOptions> options, int[] remaining, int enough) {
            this.gameData = gameData;
            this.playerId = playerId;
            this.battlefield = battlefield;
            this.options = options;
            this.remaining = remaining;
            this.enough = enough;
        }

        private boolean done() {
            return best.obeyed() > enough || visited > REQUIREMENT_SEARCH_BUDGET;
        }

        private void visit(int position, int obeyed) {
            visited++;
            int toBeat = enough == Integer.MAX_VALUE ? best.obeyed() : Math.max(best.obeyed(), enough);
            if (obeyed + remaining[position] <= toBeat || done()) {
                return;
            }
            if (position == options.size()) {
                if (restrictionsAllow(gameData, playerId, battlefield, attackers, targets)) {
                    best = new RequirementDeclaration(Map.copyOf(targets), obeyed);
                }
                return;
            }
            AttackOptions creature = options.get(position);
            for (int i = 0; i < creature.targets().size() && !done(); i++) {
                attackers.add(creature.index());
                targets.put(creature.index(), creature.targets().get(i));
                if (attackerCountAllowed(gameData, playerId, battlefield, attackers, targets)) {
                    visit(position + 1, obeyed + creature.requirementCounts().get(i));
                }
                attackers.removeLast();
                targets.remove(creature.index());
            }
            if (!done()) {
                visit(position + 1, obeyed);
            }
        }
    }

    /**
     * Whether no restriction on how many creatures attack, or on which creatures may attack together, is broken by
     * {@code attackers} attacking {@code targets}; adding attackers never mends one of these.
     */
    private boolean attackerCountAllowed(GameData gameData, UUID playerId, List<Permanent> battlefield,
                                         List<Integer> attackers, Map<Integer, UUID> targets) {
        if (attackers.size() > 1 && attackers.stream()
                .anyMatch(idx -> canOnlyAttackAlone(gameData, battlefield.get(idx)))) {
            return false;
        }
        try {
            CombatHelper.validateMaximumAttackers(gameData, attackers, targets, gameQueryService);
        } catch (IllegalStateException tooMany) {
            return false;
        }
        return true;
    }

    /**
     * CR 508.1c: whether {@code attackers} attacking {@code targets} disobey no restriction — who can't attack alone or
     * can only attack alone, who needs other or more powerful attackers beside it, and how many creatures may attack.
     */
    private boolean restrictionsAllow(GameData gameData, UUID playerId, List<Permanent> battlefield,
                                      List<Integer> attackers, Map<Integer, UUID> targets) {
        if (attackers.isEmpty()) {
            return true;
        }
        for (int idx : attackers) {
            if (hasUnmetAttackRestriction(gameData, battlefield, attackers, idx)) {
                return false;
            }
        }
        try {
            CombatHelper.validateCommandZoneAttackCount(gameData, playerId, attackers.size());
            CombatHelper.validateMaximumAttackers(gameData, attackers, targets, gameQueryService);
        } catch (IllegalStateException disobeyed) {
            return false;
        }
        return true;
    }

    /**
     * Whether a creature that "attacks if able" when the declared attackers make it able can join them: declaring it
     * beside {@code declaredAttackerIndices} disobeys no restriction (CR 508.1d obeys requirements only as far as the
     * restrictions allow).
     */
    private boolean canAttackAlongside(GameData gameData, UUID playerId, List<Permanent> battlefield,
                                       Set<Integer> declaredAttackerIndices, Map<Integer, UUID> attackTargets,
                                       int index) {
        List<Integer> attackers = new ArrayList<>(declaredAttackerIndices);
        attackers.add(index);
        UUID defaultTargetId = gameQueryService.getOpponentId(gameData, playerId);
        Map<Integer, UUID> targets = new HashMap<>();
        attackers.forEach(attacker -> targets.put(attacker, attackTargets == null || attacker == index
                ? defaultTargetId : attackTargets.getOrDefault(attacker, defaultTargetId)));
        return restrictionsAllow(gameData, playerId, battlefield, attackers, targets);
    }

    /**
     * Returns true if an opponent's {@link OpponentsMustAttackControllerEffect} applies to this player's declaration:
     * at least one of this player's attackable creatures can attack a target that obeys it without paying any attack
     * cost. CR 508.1d: the player is not required to pay optional attack costs to obey a requirement, so a requirement
     * only taxed attacks would obey is not enforced.
     */
    public boolean isOpponentForcedToAttack(GameData gameData, UUID playerId) {
        return enforcedOpponentRequirementTargets(gameData, playerId,
                getAttackableCreatureIndices(gameData, playerId)) != null;
    }

    /**
     * The attack targets an attack must go to for every enforced opponent requirement to be obeyed, or {@code null}
     * when none is enforced. "Attack you or a planeswalker you control" (Trove of Temptation) names its controller and
     * their planeswalkers; "attack with at least one creature" (Seeker of Slaanesh) any target, so an attack obeying the
     * first obeys the second too. When no free attack can obey the named targets, only the any-target requirement is
     * left to enforce.
     */
    private Set<UUID> enforcedOpponentRequirementTargets(GameData gameData, UUID playerId,
                                                         List<Integer> attackableIndices) {
        if (attackableIndices.isEmpty()
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return null;
        }
        Set<UUID> namedTargetIds = new HashSet<>();
        boolean anyDefender = false;
        for (var active : gameQueryService.activeStaticEffects(gameData, OpponentsMustAttackControllerEffect.class)) {
            if (active.controllerId().equals(playerId)) {
                continue;
            }
            if (active.effect().anyDefender()) {
                anyDefender = true;
                continue;
            }
            namedTargetIds.add(active.controllerId());
            for (Permanent target : gameData.playerBattlefields.getOrDefault(active.controllerId(), List.of())) {
                if (gameQueryService.isPlaneswalker(gameData, target)) {
                    namedTargetIds.add(target.getId());
                }
            }
        }
        if (!namedTargetIds.isEmpty() && canAttackForFree(gameData, playerId, attackableIndices, namedTargetIds)) {
            return namedTargetIds;
        }
        if (anyDefender) {
            Set<UUID> anyTargetIds = attackLegalityService.getValidAttackTargetIds(gameData, playerId);
            if (canAttackForFree(gameData, playerId, attackableIndices, anyTargetIds)) {
                return anyTargetIds;
            }
        }
        return null;
    }

    private boolean canAttackForFree(GameData gameData, UUID playerId, List<Integer> attackableIndices,
                                     Set<UUID> targetIds) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        for (int idx : attackableIndices) {
            Permanent creature = battlefield.get(idx);
            for (UUID targetId : targetIds) {
                if (attackLegalityService.canAttackDefender(gameData, creature, targetId)
                        && castingCostService.getAttackPaymentPerCreature(gameData, playerId, targetId, creature) == 0
                        && castingCostService.getAttackLifePaymentPerCreature(gameData, playerId, targetId, creature) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateOpponentRequiredAttackTargets(GameData gameData, UUID playerId,
                                                       List<Integer> attackableIndices,
                                                       List<Integer> attackerIndices,
                                                       Map<Integer, UUID> attackTargets) {
        Set<UUID> requiredTargetIds = enforcedOpponentRequirementTargets(gameData, playerId, attackableIndices);
        if (requiredTargetIds == null) {
            return;
        }
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
     * CR 508.1d: a declaration with an attacker that turns on an {@link CreaturesWithCounterAttackTogetherEffect}
     * requirement ("if a creature you control attacks, this creature also attacks if able", Ekundu Cyclops; "if this
     * creature attacks, all creatures you control attack if able", Viashino Bey; War's Toll; Magnetic Web) must hold
     * every attackable creature the requirement names. Waived while any attack would cost the player something and for
     * a creature with its own attack cost (CR 508.1d never forces a payment), and for a creature whose joining would
     * disobey a restriction (one of them can only attack alone, or no more creatures may attack).
     */
    private void validateAttackTogetherRequirements(GameData gameData, UUID playerId,
                                                    List<Integer> attackableIndices,
                                                    Set<Integer> declaredAttackerIndices,
                                                    Map<Integer, UUID> attackTargets) {
        if (declaredAttackerIndices.isEmpty()
                || castingCostService.hasAttackPaymentForAnyCreature(gameData, playerId)
                || !castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        for (var requirement : gameQueryService.activeStaticEffects(gameData,
                CreaturesWithCounterAttackTogetherEffect.class)) {
            if (!attackTogetherApplies(gameData, battlefield, declaredAttackerIndices, requirement)) {
                continue;
            }
            for (int idx : attackableIndices) {
                if (declaredAttackerIndices.contains(idx)) {
                    continue;
                }
                Permanent creature = battlefield.get(idx);
                if (attackTogetherRequires(gameData, creature, requirement)
                        && gameQueryService.getCreatureAttackTax(gameData, creature) == 0
                        && canAttackAlongside(gameData, playerId, battlefield, declaredAttackerIndices, attackTargets,
                        idx)) {
                    throw new IllegalStateException(FaceDownVisibility.publicName(gameData, creature)
                            + " must attack if able: it must also attack beside the declared attackers");
                }
            }
        }
    }

    private void validateCantAttackAlone(GameData gameData, List<Permanent> battlefield,
                                         List<Integer> attackerIndices) {
        if (attackerIndices.size() == 1) {
            validateCompanionsAlsoAttack(gameData, battlefield, attackerIndices);
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
                throw new IllegalStateException(FaceDownVisibility.publicName(gameData, attacker) + " can only attack alone");
            }
        }
    }

    /**
     * Whether {@code creature} disobeys a "can't attack / block alone" or "unless [others] also attack / block"
     * restriction of its own when {@code declared} (the creature among them, or not) are the declared attackers
     * ({@code attacking}) or blockers (CR 508.1c, 509.1b); for the AI, which builds declarations before they are made.
     */
    public boolean hasUnmetCompanionRestriction(GameData gameData, Permanent creature, boolean attacking,
                                                Collection<Permanent> declared) {
        return combatCompanionRestrictions.firstUnmet(gameData, creature, attacking, declared) != null;
    }

    private boolean hasCantAttackOrBlockAlone(GameData gameData, Permanent creature) {
        return combatCompanionRestrictions.isRestricted(gameData, creature, true);
    }

    /**
     * CR 508.1c: every declared attacker with "can't attack alone" or "can't attack unless [N other creatures, a
     * creature with greater power, a black or green creature] also attack" has those others beside it, judged as the
     * attackers are declared.
     */
    private void validateCompanionsAlsoAttack(GameData gameData, List<Permanent> battlefield,
                                              List<Integer> attackerIndices) {
        List<Permanent> attackers = attackerIndices.stream().map(battlefield::get).toList();
        for (Permanent attacker : attackers) {
            CantAttackOrBlockAloneEffect unmet = combatCompanionRestrictions.firstUnmet(gameData, attacker, true,
                    attackers);
            if (unmet != null) {
                throw new IllegalStateException(FaceDownVisibility.publicName(gameData, attacker) + " "
                        + unmet.unmetText(true));
            }
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

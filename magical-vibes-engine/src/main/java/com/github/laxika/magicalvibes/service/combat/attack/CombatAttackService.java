package com.github.laxika.magicalvibes.service.combat.attack;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatAttackTarget;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TargetGroup;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.effect.BoostAttackingCreatureOnAttacksYouEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnAttackingCreatureOnAttacksYouEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetMatches;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsMonarch;
import com.github.laxika.magicalvibes.model.action.DelayedAttackDamage;
import com.github.laxika.magicalvibes.model.action.DelayedAttackTokenCreation;
import com.github.laxika.magicalvibes.model.action.DelayedAttackUntap;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerBoost;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerDeclarationControl;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerKeywordGrant;
import com.github.laxika.magicalvibes.model.action.DelayedNontokenAttackTokenCreation;
import com.github.laxika.magicalvibes.model.action.DelayedOpponentAttackerBoost;
import com.github.laxika.magicalvibes.model.action.DelayedVehicleAttack;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureAttack;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AllMatchingCreaturesAttack;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetMatches;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesGreaterThanSourceCounters;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.condition.AttackedOpponentHasMoreLifeThanAnotherOpponent;
import com.github.laxika.magicalvibes.model.condition.AttacksPlayerAlone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerAndEnchantedPlayerAttackEachOther;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsMoreLandsThanController;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerHandAtMost;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerPoisoned;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksPlaneswalker;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.condition.OpponentsAttackWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksOneOfYourOpponents;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksAnotherOpponent;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksOneOrMoreOfYourOpponents;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksOneOfYourOpponentsWithPowerOrToughnessEqualToSourceChosenNumber;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksNotController;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedThisCombat;
import com.github.laxika.magicalvibes.model.condition.SourceClassLevelAtLeast;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.AttackCounterMoveEffect;
import com.github.laxika.magicalvibes.model.effect.AttackingCreaturesAwareEffect;
import com.github.laxika.magicalvibes.model.effect.AttackingPermanentSnapshot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyRemoveKeywordEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDefendingPlayerCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OtherAttackingCreatureReferenceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedVehicleAttackEffect;
import com.github.laxika.magicalvibes.model.action.DelayedOpponentAttackerBoost;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureAttack;
import com.github.laxika.magicalvibes.model.action.DelayedAttackUntap;
import com.github.laxika.magicalvibes.model.action.DelayedAttackTokenCreation;
import com.github.laxika.magicalvibes.model.action.DelayedAttackDamage;
import com.github.laxika.magicalvibes.model.action.DelayedVehicleAttack;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentManaValueEffect;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerDeclarationControl;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAttackingCreatureOnAttacksYouEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessCountAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RandomChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotChosenDuringLastCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CombatCreatureLimitEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithCounterAttackTogetherEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingAttackerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackIfAnotherCreatureAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCreaturesAttackTogetherEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackRequirementEffect;
import com.github.laxika.magicalvibes.model.effect.OtherCreaturesMustAttackIfSourceAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.OtherAttackingCreatureReferenceEffect;
import com.github.laxika.magicalvibes.model.effect.OtherCreaturesMustAttackIfSourceAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnAttackingCreatureOnAttacksYouEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedVehicleAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TrainEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksPlayerWithMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
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
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.OnceOnlyTriggerSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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

    private record AttackTriggerSource(Permanent permanent, EffectSlot effectSlot,
                                       List<CardEffect> effects, List<UUID> bandMembers) {
        private AttackTriggerSource(Permanent permanent, EffectSlot effectSlot, List<CardEffect> effects) {
            this(permanent, effectSlot, effects, null);
        }

        private AttackTriggerSource(Permanent permanent, EffectSlot effectSlot) {
            this(permanent, effectSlot, null, null);
        }
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

        String logEntry = playerName + " declares " + attackerIndices.size() +
                " attacker" + (attackerIndices.size() > 1 ? "s" : "") + ".";
        gameLogService.append(gameData, GameLog.text(logEntry));

        // Collect all attack-step triggers, then reorder per APNAP (CR 603.3b)
        // Check for "when this creature attacks" triggers
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            int previousCopies = beginAttackTriggerCopies(gameData, playerId, attacker);
            try {
            // An Equipment's printed attack trigger is "whenever equipped creature attacks", so it
            // doesn't fire when an animated Equipment attacks itself (Captain America's Shield).
            boolean equipmentAttackTrigger = gameQueryService.hasEffectiveSubtype(gameData, attacker, CardSubtype.EQUIPMENT)
                    && !gameQueryService.hasReconfigure(gameData, attacker);
            List<CardEffect> nativeAttackEffects = attacker.isFaceDown()
                    || gameQueryService.hasLostPrintedAbilities(gameData, attacker)
                    || equipmentAttackTrigger
                    ? List.of() : attacker.getCard().getEffects(EffectSlot.ON_ATTACK);
            // A granted triggered ability is an ability of the creature, so losing all abilities removes it.
            List<CardEffect> temporaryAttackEffects = new ArrayList<>();
            if (!gameQueryService.hasLostAllAbilities(gameData, attacker)) {
                temporaryAttackEffects.addAll(attacker.getTemporaryTriggeredEffects(EffectSlot.ON_ATTACK));
                temporaryAttackEffects.addAll(attacker.getPersistentTriggeredEffects(EffectSlot.ON_ATTACK));
            }
            // Continuously granted ON_ATTACK abilities (Thorncaster Sliver giving every Sliver
            // "Whenever this creature attacks, it deals 1 damage to any target").
            List<CardEffect> grantedAttackEffects = grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, attacker, EffectSlot.ON_ATTACK);
            // Decayed's "sacrifice it at end of combat" is its own triggered ability (CR 702.147a),
            // so it goes on the stack as a separate object from any printed attack ability.
            boolean decayedTrigger = gameQueryService.hasKeyword(gameData, attacker, Keyword.DECAYED);
            if (!nativeAttackEffects.isEmpty() || !temporaryAttackEffects.isEmpty()
                    || !grantedAttackEffects.isEmpty()) {
                List<CardEffect> allEffects = new ArrayList<>(nativeAttackEffects);
                allEffects.addAll(grantedAttackEffects);
                // Temporarily granted ON_ATTACK abilities (e.g. Tower Above's "target creature blocks
                // it this turn if able"). MustBlockSourceEffect's source is snapshotted to the attacker.
                // The snapshotted copies replace the granted effects in temporaryAttackEffects so each
                // grant still matches its own entry when separate granted abilities are split out below
                // (two Tower Aboves are two triggers, each with its own target).
                temporaryAttackEffects.replaceAll(temp -> temp instanceof MustBlockSourceEffect mustBlock
                        ? new MustBlockSourceEffect(attacker.getId(),
                                mustBlock.targetRestriction(), mustBlock.untilEndOfCombat())
                        : temp);
                allEffects.addAll(temporaryAttackEffects);

                allEffects.replaceAll(effect -> {
                    if (!(effect instanceof RandomChoiceEffect random)) return effect;
                    List<CardEffect> chosen = random.options().get(java.util.concurrent.ThreadLocalRandom.current()
                            .nextInt(random.options().size()));
                    return chosen.size() == 1 ? chosen.getFirst()
                            : new com.github.laxika.magicalvibes.model.effect.SequenceEffect(chosen);
                });

                // "Whenever this creature attacks for the first time each turn" (Aurelia, the
                // Warleader): drop the wrapped effects entirely once this permanent has already
                // attacked this turn, otherwise consume the first-attack marker before any
                // intervening-if or trigger-subject filters can remove the wrapped effect.
                // The gate is per permanent, so an extra combat phase it grants can't loop. Attacks
                // made while the creature had lost its abilities still count as its attacks this turn,
                // so the attack count is checked as well as the marker.
                if (allEffects.stream().anyMatch(e -> e instanceof OncePerTurnTriggerEffect)) {
                    if (attacker.getAttacksThisTurn() > 1
                            || gameData.onceEachTurnAttackTriggersFiredThisTurn.contains(attacker.getId())) {
                        allEffects.removeIf(e -> e instanceof OncePerTurnTriggerEffect);
                    } else {
                        gameData.onceEachTurnAttackTriggersFiredThisTurn.add(attacker.getId());
                        allEffects.replaceAll(e -> e instanceof OncePerTurnTriggerEffect once ? once.wrapped() : e);
                    }
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
                        && ce.interveningIf()
                        && ce.condition() instanceof ControlsPermanent
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ControlsPermanentCountAtMost
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof Equipped
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.interveningIf()
                        && ce.condition() instanceof ControlsPermanentCount
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.interveningIf()
                        && ce.condition() instanceof ControlledCreaturesTotalPowerAtLeast
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out controls-another-permanent conditionals when condition not met (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ControlsAnotherPermanent
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ControllerCastSpellThisTurn
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof VoidCondition
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof VoidCondition ? ce.wrapped() : e);

                // Filter out renown conditionals when the attacker isn't renowned (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof SourceIsRenowned
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out saddled attack conditionals when the attacker wasn't saddled when
                // it was declared.
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof SourceIsSaddled
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof SourceIsSaddled ? ce.wrapped() : e);

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

                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ExactlyAttackers
                        && !conditionEvaluationService.isMet(gameData, ce.condition(), attackCountCtx));

                // Battalion-style attacker counts are part of the trigger event, not an intervening-if
                // clause, so the surviving wrapper is unwrapped here: paths that route the trigger
                // through a target-selection interaction (AttackTriggerTarget) build the stack entry
                // later and would otherwise re-evaluate the condition without the attacker count.
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof MinimumAttackers
                        ? ce.wrapped() : e);
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ExactlyAttackers
                        ? ce.wrapped() : e);

                // Pack-tactics-style intervening-if conditions qualify the attack event itself, so the
                // condition is measured now and the surviving effect is not re-evaluated during resolution.
                // A non-intervening "if" in the effect text (Formidable) is checked only on resolution.
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce && ce.interveningIf()
                        && ce.condition() instanceof AttackingCreaturesTotalPowerAtLeast
                        && !conditionEvaluationService.isMet(gameData, ce.condition(), attackCountCtx));
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce && ce.interveningIf()
                        && ce.condition() instanceof AttackingCreaturesTotalPowerAtLeast
                        ? ce.wrapped() : e);
                ConditionContext attackedTargetContext = ConditionContext.forPermanent(attacker, playerId)
                        .withTargetId(attacker.getAttackTarget());
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttackedTargetMatches
                        && !conditionEvaluationService.isMet(gameData, ce.condition(), attackedTargetContext));
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttackedTargetMatches ? ce.wrapped() : e);
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttackedTargetIsOpponent
                        && !conditionEvaluationService.isMet(gameData, ce.condition(), attackedTargetContext));
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttackedTargetIsOpponent ? ce.wrapped() : e);
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttackedTargetIsMonarch
                        && !conditionEvaluationService.isMet(gameData, ce.condition(), attackedTargetContext));
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttackedTargetIsMonarch ? ce.wrapped() : e);

                // Granted abilities can bundle the opponent/otherwise branches in a SequenceEffect
                // (e.g. Scriv's Contract). Resolve those event conditions now, while the declared
                // attack target is still available; a normal stack-entry condition context points
                // at the triggering permanent instead.
                allEffects.replaceAll(effect -> resolveAttackedTargetConditions(
                        gameData, attacker, playerId, effect));
                allEffects.removeIf(Objects::isNull);

                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.interveningIf()
                        && !conditionEvaluationService.isMet(
                                gameData, ce.condition(), ConditionContext.forPermanent(attacker, playerId)));
                // A trigger-time-only condition has been checked; unwrap it so a wrapped "you may"
                // (Combat Celebrant's exert choice) is offered as declared rather than re-checked.
                allEffects.replaceAll(e -> e instanceof ConditionalEffect ce && ce.triggerTimeOnly()
                        ? ce.wrapped() : e);

                if (!allEffects.isEmpty()) {
                    // Separate non-targeting "you may" effects (e.g. Primeval Titan's may-search) from
                    // effects that need the normal resolution path (mandatory effects and targeting may effects
                    // like Cyclops Gladiator's may-fight).
                    List<CardEffect> nonTargetingMayEffects = allEffects.stream()
                            .filter(e -> e instanceof com.github.laxika.magicalvibes.model.effect.MayEffect
                                    && e.targetSpec().declaredTarget() == null).toList();
                    List<CardEffect> otherEffects = allEffects.stream()
                            .filter(e -> !nonTargetingMayEffects.contains(e)).toList();

                    // Queue non-targeting may effects as pending may abilities
                    for (CardEffect effect : nonTargetingMayEffects) {
                        com.github.laxika.magicalvibes.model.effect.MayEffect may =
                                (com.github.laxika.magicalvibes.model.effect.MayEffect) effect;
                        UUID otherAttackerId = may.wrapped() instanceof OtherAttackingCreatureReferenceEffect
                                ? findOtherAttackerId(attackerIndices, idx, battlefield) : null;
                        if ((may.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.SequenceEffect sequence
                                ? sequence.steps() : List.of(may.wrapped())).stream().anyMatch(step ->
                                step instanceof com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect skip
                                        && skip.controllerStepOnly())) {
                            gameData.queueMayAbilityForPlayer(attacker.getCard(), playerId, may, otherAttackerId,
                                    attacker.getId(), playerId, new Permanent(attacker));
                        } else {
                            gameData.queueMayAbility(attacker.getCard(), playerId, may, otherAttackerId, attacker.getId(),
                                    attackedTargetForPendingMay(gameData, attacker, may));
                        }
                    }

                    if (!otherEffects.isEmpty()) {
                        // Two-target "remove a counter from a creature you control, then put one on up
                        // to one creature the defending player controls" (Decimator Beetle). The normal
                        // pipeline collects only one target, so route to the bespoke two-step flow.
                        boolean isTriggerTimeModal = otherEffects.size() == 1
                                && (otherEffects.getFirst() instanceof ChooseOneAtTriggerTimeEffect
                                || otherEffects.getFirst() instanceof ChooseModeNotChosenDuringLastCombatEffect);
                        boolean isCounterMove = otherEffects.stream().anyMatch(e -> e instanceof AttackCounterMoveEffect);
                        boolean needsGraveyardTarget = otherEffects.stream()
                                .filter(e -> !e.targetsChosenByReflexiveAbility())
                                .anyMatch(e -> e instanceof GraveyardCardChoosingEffect choosingEffect
                                        && choosingEffect.choosesGraveyardCards()
                                        || e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
                        boolean needsTarget = otherEffects.stream()
                                .filter(e -> !e.targetsChosenByReflexiveAbility())
                                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                        || e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                                        || e.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD));
                        UUID attackedTargetId = attacker.getAttackTarget();
                        Permanent attackedPermanent = attackedTargetId == null ? null
                                : gameQueryService.findPermanentById(gameData, attackedTargetId);
                        UUID defendingPlayerId = attackedTargetId == null ? null
                                : gameData.playerIds.contains(attackedTargetId) ? attackedTargetId
                                : attackedPermanent != null && gameQueryService.isBattle(gameData, attackedPermanent)
                                        ? attackedPermanent.getProtectorPlayerId()
                                        : gameQueryService.findPermanentController(gameData, attackedTargetId);
                        if (otherEffects.size() > 1
                                && otherEffects.stream().allMatch(e -> e instanceof ChooseOneAtTriggerTimeEffect)) {
                            Set<UUID> usedGrantingPermanents = new HashSet<>();
                            for (CardEffect modalTrigger : otherEffects) {
                                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                                        attacker.getCard(),
                                        grantedAbilityController(gameData, attacker, modalTrigger, playerId,
                                                usedGrantingPermanents),
                                        ((ChooseOneAtTriggerTimeEffect) modalTrigger).choice(), attacker.getId(),
                                        false, false, null, null, null, false));
                            }
                        } else if (isTriggerTimeModal) {
                            CardEffect modalEffect = otherEffects.getFirst();
                            ChooseOneEffect modal;
                            boolean rememberLastChosenMode = false;
                            if (modalEffect instanceof ChooseModeNotChosenDuringLastCombatEffect lastCombatModal) {
                                List<ChooseOneEffect.ChooseOneOption> availableOptions = lastCombatModal.options().stream()
                                        .filter(option -> !option.label().equals(attacker.getChosenMode()))
                                        .toList();
                                if (availableOptions.isEmpty()) {
                                    continue;
                                }
                                modal = new ChooseOneEffect(availableOptions);
                                rememberLastChosenMode = true;
                            } else {
                                modal = ((ChooseOneAtTriggerTimeEffect) modalEffect).choice();
                            }
                            gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                                    attacker.getCard(),
                                    grantedAbilityController(gameData, attacker, modalEffect, playerId, new HashSet<>()),
                                    modal, attacker.getId(), false, false,
                                    null, null, null, rememberLastChosenMode));
                        } else if (isCounterMove) {
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
                                // Each separately granted ability is its own trigger with its own target.
                                List<CardEffect> bundledEffects = new ArrayList<>(otherEffects);
                                List<CardEffect> separateAbilities = new ArrayList<>();
                                for (CardEffect granted : grantedAttackEffects) {
                                    if (bundledEffects.remove(granted)) separateAbilities.add(granted);
                                }
                                for (CardEffect granted : temporaryAttackEffects) {
                                    if (bundledEffects.remove(granted)) separateAbilities.add(granted);
                                }
                                if (bundledEffects.isEmpty()) {
                                    bundledEffects.add(separateAbilities.removeFirst());
                                }
                                graveyardTargetingService.handleAttackGraveyardTargeting(
                                        gameData, playerId, attacker.getCard(), bundledEffects,
                                        attacker.getId(), defendingPlayerId);
                                for (CardEffect separateAbility : separateAbilities) {
                                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                            attacker.getCard(), playerId, List.of(separateAbility),
                                            defendingPlayerId, 1, 0, 1, null, false, null, attacker.getId()));
                                }
                            }
                        } else if (needsTarget) {
                            List<CardEffect> bundledEffects = new ArrayList<>(otherEffects);
                            List<List<CardEffect>> abilities = new ArrayList<>();
                            for (CardEffect granted : grantedAttackEffects) {
                                if (bundledEffects.remove(granted)) {
                                    abilities.add(List.of(granted));
                                }
                            }
                            for (CardEffect granted : temporaryAttackEffects) {
                                if (bundledEffects.remove(granted)) {
                                    abilities.add(List.of(granted));
                                }
                            }
                            for (var registration : attacker.getCard().getEffectRegistrations(EffectSlot.ON_ATTACK)) {
                                if (registration.triggerMode()
                                        == com.github.laxika.magicalvibes.model.TriggerMode.INDEPENDENT
                                        && bundledEffects.remove(registration.effect())) {
                                    abilities.add(List.of(registration.effect()));
                                }
                            }
                            if (!bundledEffects.isEmpty()) {
                                abilities.addFirst(bundledEffects);
                            }
                            for (List<CardEffect> abilityEffects : abilities) {
                                boolean abilityNeedsTarget = abilityEffects.stream().anyMatch(effect ->
                                        effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                                                || effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD));
                                if (!abilityNeedsTarget) {
                                    StackEntry attackTrigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                                            attacker.getCard(), playerId, attacker.getCard().getName() + "'s attack trigger",
                                            abilityEffects, attackerIndices.size(), attacker.getId());
                                    attackTrigger.setAttackedTargetId(attacker.getAttackTarget());
                                    attackTrigger.setDefendingPlayerId(defendingPlayerId);
                                    attackTrigger.setSourcePermanentSnapshot(snapshotAttackingSource(gameData, attacker));
                                    gameData.stack.add(attackTrigger);
                                    triggerCollectionService.checkAttackingCreatureTriggeredAbilityTriggers(
                                            gameData, attacker, attackTrigger);
                                    continue;
                                }
                                // A static single "up to N" group uses the multi-permanent choice
                                // flow. Equipment attachments need individual selections so each
                                // chosen Equipment is attached when the trigger resolves.
                                Card attackCard = attacker.getCard();
                                boolean staticSingleMultiTargetGroup = attackCard.getTargetGroups().size() == 1
                                        && attackCard.getTargetGroups().getFirst().getMaxTargets() > 1
                                        && attackCard.getTargetGroups().getFirst().getDynamicMinTargets() == null
                                        && attackCard.getTargetGroups().getFirst().getDynamicMaxTargets() == null;
                                boolean attachesEquipment = abilityEffects.stream()
                                        .anyMatch(effect -> effect instanceof AttachTargetEquipmentToTriggeringPermanentEffect
                                                || effect instanceof MayEffect may
                                                && may.wrapped() instanceof AttachTargetEquipmentToTriggeringPermanentEffect);
                                boolean declaredGroupsTargetOtherAbilities = !attackCard.getTargetGroups().isEmpty()
                                        && attackCard.getTargetGroups().stream().allMatch(group ->
                                        attackCard.bindsEffectToTargetGroup(group.getIndex())
                                                && attackCard.getEffectsBoundToTargetGroup(group.getIndex()).stream()
                                                .noneMatch(attackCard.getEffects(EffectSlot.ON_ATTACK)::contains));
                                if (!declaredGroupsTargetOtherAbilities
                                        && (attackCard.getTargetGroups().size() > 1
                                        || ((!staticSingleMultiTargetGroup || attachesEquipment)
                                        && etbTokenTargetService.needsSlotBySlotTargetSelection(attackCard)))) {
                                    gameData.queueInteraction(
                                            new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                                    attackCard, playerId, abilityEffects, attacker.getId(),
                                                    List.of(), 0, 0));
                                } else {
                                    UUID targetChooserId = attacker.getCard().isAttackTriggerTargetChosenByDefendingPlayer()
                                            ? defendingPlayerId : playerId;
                                    gameData.queueInteraction(
                                            new PermanentChoiceContext.AttackTriggerTarget(
                                                    attackCard, playerId, abilityEffects, attacker.getId(),
                                                    targetChooserId, defendingPlayerId, attacker.getId()));
                                }
                            }
                        } else {
                            // Capture the attacked player/planeswalker so non-targeting attack
                            // triggers that act on the defending player (e.g. Nemesis of Reason's
                            // MillDefendingPlayerEffect) can read it as attackedTargetId.
                            // xValue locks the attacker count for MinimumAttackers (Odric / similar).
                            List<CardEffect> bundledEffects = new ArrayList<>(otherEffects);
                            List<List<CardEffect>> abilities = new ArrayList<>();
                            for (CardEffect granted : grantedAttackEffects) {
                                if (bundledEffects.remove(granted)) {
                                    abilities.add(List.of(granted));
                                }
                            }
                            for (CardEffect granted : temporaryAttackEffects) {
                                if (bundledEffects.remove(granted)) {
                                    abilities.add(List.of(granted));
                                }
                            }
                            for (var registration : attacker.getCard().getEffectRegistrations(EffectSlot.ON_ATTACK)) {
                                if (registration.triggerMode()
                                        == com.github.laxika.magicalvibes.model.TriggerMode.INDEPENDENT
                                        && bundledEffects.remove(registration.effect())) {
                                    abilities.add(List.of(registration.effect()));
                                }
                            }
                            if (!bundledEffects.isEmpty()) {
                                abilities.addFirst(bundledEffects);
                            }
                            for (List<CardEffect> abilityEffects : abilities) {
                                StackEntry attackTrigger = new StackEntry(
                                        StackEntryType.TRIGGERED_ABILITY,
                                        attacker.getCard(),
                                        playerId,
                                        attacker.getCard().getName() + "'s attack trigger",
                                        abilityEffects,
                                        attackerIndices.size(),
                                        attacker.getId()
                                );
                                attackTrigger.setAttackedTargetId(attacker.getAttackTarget());
                                attackTrigger.setDefendingPlayerId(defendingPlayerId);
                                attackTrigger.setSourcePermanentSnapshot(snapshotAttackingSource(gameData, attacker));
                                gameData.stack.add(attackTrigger);
                                triggerCollectionService.checkAttackingCreatureTriggeredAbilityTriggers(
                                        gameData, attacker, attackTrigger);
                            }
                        }

                        if (!needsGraveyardTarget && !isTriggerTimeModal) {
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
            if (decayedTrigger) {
                StackEntry decayedSacrifice = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        attacker.getCard(),
                        playerId,
                        attacker.getCard().getName() + "'s decayed trigger",
                        List.of(new SacrificeAtEndOfCombatEffect()),
                        attackerIndices.size(),
                        attacker.getId()
                );
                decayedSacrifice.setSourcePermanentSnapshot(new Permanent(attacker));
                gameData.stack.add(decayedSacrifice);
            }
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }

            // Check for aura-based "when enchanted creature attacks" triggers
            combatTriggerService.checkAuraTriggersForCreature(gameData, attacker, EffectSlot.ON_ATTACK);

            // Delayed triggers watching this specific creature (Okoye, Mighty and Adored): the
            // registering card is the source and its controller controls the trigger (CR 603.7).
            for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
                if (watcher.slot() != EffectSlot.ON_ATTACK || !attacker.getId().equals(watcher.watchedPermanentId())) {
                    continue;
                }
                CardEffect watcherEffect = watcher.effect();
                if (watcherEffect instanceof TriggeringPermanentConditionalEffect conditional) {
                    if (!predicateEvaluationService.matchesPermanentPredicate(
                            gameData, attacker, conditional.predicate())) {
                        continue;
                    }
                    watcherEffect = conditional.wrapped();
                }
                StackEntry delayedTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.sourceCard(),
                        watcher.controllerId(),
                        watcher.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(List.of(watcherEffect)),
                        null,
                        attacker.getId());
                delayedTrigger.setNonTargeting(true);
                delayedTrigger.setTriggeringPermanentId(attacker.getId());
                gameData.stack.add(delayedTrigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            }
        }

        // Engine-level melee triggers: each attacking creature with melee gets +1/+1 until end
        // of turn for each distinct opponent attacked by this combat.
        Set<UUID> opponentsAttackedThisCombat = new HashSet<>();
        for (int idx : attackerIndices) {
            UUID attackTarget = resolvedTargets.get(idx);
            if (attackTarget == null) {
                continue;
            }
            if (gameData.playerIds.contains(attackTarget)) {
                opponentsAttackedThisCombat.add(attackTarget);
                continue;
            }
        }
        {
            int meleeBoost = opponentsAttackedThisCombat.size();
            for (int idx : attackerIndices) {
                Permanent attacker = battlefield.get(idx);
                if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.MELEE)) {
                    continue;
                }
                int previousCopies = beginAttackTriggerCopies(gameData, playerId, attacker);
                try {
                    for (int instance = 0; instance < gameQueryService.meleeInstances(gameData, attacker); instance++) {
                    StackEntry meleeTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            attacker.getCard(),
                            playerId,
                            attacker.getCard().getName() + "'s melee",
                            List.of(new BoostSelfEffect(meleeBoost, meleeBoost)),
                            null,
                            attacker.getId()
                    );
                    gameData.stack.add(meleeTrigger);
                    triggerCollectionService.checkAttackingCreatureTriggeredAbilityTriggers(
                            gameData, attacker, meleeTrigger);
                    gameLogService.append(gameData,
                            GameLog.builder().card(attacker.getCard()).text("'s melee triggers.").build());
                    log.info("Game {} - {} melee trigger pushed onto stack", gameData.id,
                            attacker.getCard().getName());
                    }
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
            }
        }

        // Engine-level battle cry triggers (keyword-driven, no manual card wiring needed)
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            int previousCopies = beginAttackTriggerCopies(gameData, playerId, attacker);
            try {
            int battleCryInstances = gameQueryService.triggerKeywordInstances(gameData, attacker, Keyword.BATTLE_CRY);
            for (int instance = 0; instance < battleCryInstances; instance++) {
                List<CardEffect> battleCryEffects = List.of(new BoostAllOwnCreaturesEffect(1, 0,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        ))
                ));
                StackEntry battleCryTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        attacker.getCard(),
                        playerId,
                        attacker.getCard().getName() + "'s battle cry",
                        battleCryEffects,
                        null,
                        attacker.getId()
                );
                gameData.stack.add(battleCryTrigger);
                triggerCollectionService.checkAttackingCreatureTriggeredAbilityTriggers(
                        gameData, attacker, battleCryTrigger);
                gameLogService.append(gameData,
                        GameLog.builder().card(attacker.getCard()).text("'s battle cry triggers.").build());
                log.info("Game {} - {} battle cry trigger pushed onto stack", gameData.id, attacker.getCard().getName());
            }
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
        }

        // Engine-level dethrone triggers: attack the player with the most life (including ties)
        // to put a +1/+1 counter on the attacking creature.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.DETHRONE)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    gameData, attacker, new PermanentAttacksPlayerWithMostLifePredicate())) {
                continue;
            }
            // CR 702.105b — each separate instance of dethrone (printed or granted) triggers on its own.
            int dethroneInstances = gameQueryService.triggerKeywordInstances(gameData, attacker, Keyword.DETHRONE);
            for (int instance = 0; instance < dethroneInstances; instance++) {
                int previousCopies = beginAttackTriggerCopies(gameData, playerId, attacker);
                try {
                    List<CardEffect> dethroneEffects = List.of(new PutCountersOnSourceEffect(1, 1, 1));
                    StackEntry dethroneTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            attacker.getCard(),
                            playerId,
                            attacker.getCard().getName() + "'s dethrone",
                            dethroneEffects,
                            null,
                            attacker.getId()
                    );
                    gameData.stack.add(dethroneTrigger);
                    triggerCollectionService.checkAttackingCreatureTriggeredAbilityTriggers(
                            gameData, attacker, dethroneTrigger);
                    gameLogService.append(gameData,
                            GameLog.builder().card(attacker.getCard()).text("'s dethrone triggers.").build());
                    log.info("Game {} - {} dethrone trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
            }
        }

        // Engine-level training triggers: attacks with another creature of greater power → +1/+1 counter
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            int previousCopies = beginAttackTriggerCopies(gameData, playerId, attacker);
            try {
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
            List<CardEffect> trainingEffects = List.of(new TrainEffect());
            StackEntry trainingTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    attacker.getCard(),
                    playerId,
                    attacker.getCard().getName() + "'s training",
                    trainingEffects,
                    null,
                    attacker.getId()
            );
            gameData.stack.add(trainingTrigger);
            triggerCollectionService.checkAttackingCreatureTriggeredAbilityTriggers(
                    gameData, attacker, trainingTrigger);
            gameLogService.append(gameData,
                    GameLog.builder().card(attacker.getCard()).text("'s training triggers.").build());
            log.info("Game {} - {} training trigger pushed onto stack", gameData.id, attacker.getCard().getName());
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
        }

        // Engine-level double team triggers: a nontoken attacker conjures a copy into its
        // controller's hand, then both cards lose double team permanently. Each instance of the
        // keyword triggers separately.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.DOUBLE_TEAM)
                    || attacker.getCard().isToken()) {
                continue;
            }
            int doubleTeamInstances = gameQueryService.triggerKeywordInstances(
                    gameData, attacker, Keyword.DOUBLE_TEAM);
            Optional<ConjureCardToHandEffect> conjureDuplicate = conjureDoubleTeamDuplicate(attacker.getCard());
            int previousCopies = beginAttackTriggerCopies(gameData, playerId, attacker);
            try {
                for (int instance = 0; instance < doubleTeamInstances; instance++) {
                    List<CardEffect> doubleTeamEffects = new ArrayList<>();
                    doubleTeamEffects.add(new PerpetuallyRemoveKeywordEffect(Keyword.DOUBLE_TEAM));
                    conjureDuplicate.ifPresent(doubleTeamEffects::add);
                    StackEntry doubleTeamTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            attacker.getCard(),
                            playerId,
                            attacker.getCard().getName() + "'s double team",
                            doubleTeamEffects,
                            attacker.getId(),
                            attacker.getId()
                    );
                    doubleTeamTrigger.setNonTargeting(true);
                    gameData.stack.add(doubleTeamTrigger);
                    triggerCollectionService.checkAttackingCreatureTriggeredAbilityTriggers(
                            gameData, attacker, doubleTeamTrigger);
                    gameLogService.append(gameData,
                            GameLog.builder().card(attacker.getCard()).text("'s double team triggers.").build());
                    log.info("Game {} - {} double team trigger pushed onto stack", gameData.id,
                            attacker.getCard().getName());
                }
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
        }

        // Check for "whenever one or more creatures you control attack" triggers (ON_ALLY_CREATURES_ATTACK)
        // These fire once per combat (not per creature) when at least one creature attacks.
        // The attacker count is locked at trigger time via xValue (per MTG rules: creatures
        // removed before resolution still count, tokens entering attacking after don't).
        List<AttackTriggerSource> attackTriggerSources = new ArrayList<>();
        for (Permanent perm : battlefield) {
            List<CardEffect> combinedEffects = new ArrayList<>();
            for (var registration : perm.getCard().getEffectRegistrations(EffectSlot.ON_ALLY_CREATURES_ATTACK)) {
                if (registration.effect() instanceof com.github.laxika.magicalvibes.model.effect.BoostCreaturesInAttackingBandsEffect) {
                    Map<UUID, List<UUID>> declaredBands = new java.util.LinkedHashMap<>();
                    for (int attackerIndex : attackerIndices) {
                        Permanent attacker = battlefield.get(attackerIndex);
                        if (attacker.getBandId() != null) {
                            declaredBands.computeIfAbsent(attacker.getBandId(), ignored -> new ArrayList<>())
                                    .add(attacker.getId());
                        }
                    }
                    for (List<UUID> members : declaredBands.values()) {
                        if (members.size() >= 2) {
                            attackTriggerSources.add(new AttackTriggerSource(perm,
                                    EffectSlot.ON_ALLY_CREATURES_ATTACK, List.of(registration.effect()), List.copyOf(members)));
                        }
                    }
                    continue;
                }
                if (registration.triggerMode() == com.github.laxika.magicalvibes.model.TriggerMode.INDEPENDENT) {
                    attackTriggerSources.add(new AttackTriggerSource(perm,
                            EffectSlot.ON_ALLY_CREATURES_ATTACK, List.of(registration.effect())));
                } else {
                    combinedEffects.add(registration.effect());
                }
            }
            combinedEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, perm, EffectSlot.ON_ALLY_CREATURES_ATTACK));
            if (!combinedEffects.isEmpty()) {
                attackTriggerSources.add(new AttackTriggerSource(perm,
                        EffectSlot.ON_ALLY_CREATURES_ATTACK, combinedEffects));
            }
        }
        for (AttackTriggerSource triggerSource : attackTriggerSources) {
            Permanent perm = triggerSource.permanent();
            EffectSlot effectSlot = triggerSource.effectSlot();
            List<CardEffect> allyAttackEffects = new ArrayList<>(triggerSource.effects() != null
                    ? triggerSource.effects() : perm.getCard().getEffects(effectSlot));
            if (allyAttackEffects.isEmpty()) continue;

            // Pre-filter attacker-group conditional effects — skip if no matching attacker exists,
            // if the attacker count is below the threshold, or if not every matching creature is
            // attacking (Mob Mentality). These event conditions are unwrapped onto the stack: the
            // trigger event already happened, and targeted triggers are assembled after target
            // selection without the combat's xValue.
            // or if not every matching creature is attacking (Mob Mentality). AllMatching is
            // unwrapped onto the stack: the trigger event already happened. Compound HasAttacker
            // gates are checked and unwrapped here for the same event-snapshot reason.
            List<CardEffect> filteredEffects = new ArrayList<>();
            Integer matchingAttackerCount = null;
            boolean oncePerTurn = false;
            boolean oncePerTurnForPlayer = false;
            boolean markOncePerTurnOnAcceptance = false;
            boolean onceOnly = false;
            for (CardEffect effect : allyAttackEffects) {
                CardEffect normalizedEffect = effect;
                boolean firstQualifyingAttackOfPlayer = effect instanceof ConditionalEffect firstTimeCondition
                        && firstTimeCondition.condition() instanceof AttackingCreaturesTotalPowerAtLeast
                        && firstTimeCondition.wrapped() instanceof OncePerTurnTriggerEffect;
                if (effect instanceof AttackingCreaturesAwareEffect aware) {
                    List<AttackingPermanentSnapshot> attackers = snapshotMatchingAttackerPowers(
                            gameData, battlefield, attackerIndices, perm, playerId,
                            aware.attackingCreaturePredicate());
                    if (attackers.isEmpty()) {
                        continue;
                    }
                    normalizedEffect = aware.withAttackingCreatures(attackers);
                }
                if (effect instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttackingCreaturesTotalPowerAtLeast) {
                    if (!conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId))) {
                        continue;
                    }
                    normalizedEffect = ce.wrapped();
                }
                if (effect instanceof ConditionalEffect ce
                        && ce.condition() instanceof MinimumAttackers minimumAttackers) {
                    boolean minimumMet = conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId).withXValue(attackerIndices.size()));
                    if (!minimumMet) {
                        log.info("Game {} - {} attack trigger skipped (too few attackers; minimum {})",
                                gameData.id, perm.getCard().getName(), minimumAttackers.minimumAttackers());
                        continue;
                    }
                    normalizedEffect = ce.wrapped();
                }
                if (normalizedEffect instanceof OncePerTurnTriggerEffect onceEffect) {
                    if (firstQualifyingAttackOfPlayer) {
                        // "For the first time each turn" counts the player's qualifying attacks, not each
                        // source's: sources present for that attack all trigger, later arrivals never do.
                        if (!isFirstQualifyingAttackThisTurn(gameData, playerId)) {
                            continue;
                        }
                        oncePerTurnForPlayer = true;
                    } else if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                        continue;
                    }
                    oncePerTurn = true;
                    markOncePerTurnOnAcceptance |= onceEffect.markOnAcceptance();
                    normalizedEffect = onceEffect.wrapped();
                }
                if (normalizedEffect instanceof OnceOnlyTriggerEffect onceEffect) {
                    if (OnceOnlyTriggerSupport.isFired(gameData, perm)) {
                        continue;
                    }
                    onceOnly = true;
                    normalizedEffect = onceEffect.wrapped();
                }
                if (normalizedEffect != effect) {
                    if (normalizedEffect instanceof ConditionalEffect conditional
                            && containsHasAttackerCondition(conditional.condition())) {
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, playerId))) {
                            continue;
                        }
                        normalizedEffect = conditional.wrapped();
                    }
                    filteredEffects.add(normalizedEffect);
                } else if (effect instanceof ConditionalEffect ce && ce.condition() instanceof MinimumAttackers minimumAttackers) {
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
                    filteredEffects.add(ce.wrapped());
                } else if (effect instanceof ConditionalEffect ce
                        && containsHasAttackerCondition(ce.condition())) {
                    if (!conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId))) {
                        log.info("Game {} - {} attack trigger skipped (attacker condition not met)",
                                gameData.id, perm.getCard().getName());
                        continue;
                    }
                    filteredEffects.add(residualAfterAttackerGate(ce));
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
                } else if (effect instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttackingCreaturesGreaterThanSourceCounters) {
                    boolean countGreaterThanCounters = conditionEvaluationService.isMet(
                            gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId).withXValue(attackerIndices.size()));
                    if (!countGreaterThanCounters) {
                        log.info("Game {} - {} attack trigger skipped (attacker count does not exceed source counters)",
                                gameData.id, perm.getCard().getName());
                        continue;
                    }
                    filteredEffects.add(effect);
                } else if (effect instanceof ConditionalEffect ce
                        && ce.condition() instanceof ExactlyAttackers exactlyAttackers) {
                    boolean exactCountMet = conditionEvaluationService.isMet(
                            gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId).withXValue(attackerIndices.size()));
                    if (!exactCountMet) {
                        log.info("Game {} - {} attack trigger skipped (not exactly {} attackers)",
                                gameData.id, perm.getCard().getName(), exactlyAttackers.attackerCount());
                        continue;
                    }
                    filteredEffects.add(ce.wrapped());
                } else if (effect instanceof ConditionalEffect ce
                        && ce.condition() instanceof MinimumMatchingAttackers matchingAttackers) {
                    boolean met = conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId));
                    if (!met) {
                        log.info("Game {} - {} attack trigger skipped (not enough matching attackers)",
                                gameData.id, perm.getCard().getName());
                        continue;
                    }
                    if (matchingAttackerCount == null) {
                        matchingAttackerCount = countMatchingAttackerIndices(
                                gameData, battlefield, attackerIndices, perm, playerId, matchingAttackers.predicate());
                    }
                    filteredEffects.add(ce.wrapped());
                } else if (effect instanceof ConditionalEffect ce
                        && ce.condition() instanceof SourceHasChosenMode) {
                    if (!conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId))) {
                        continue;
                    }
                    filteredEffects.add(ce.wrapped());
                } else if (effect instanceof ConditionalEffect ce && ce.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                        ConditionContext.forPermanent(perm, playerId)
                                .withXValue(attackerIndices.size()))) {
                    continue;
                } else {
                    filteredEffects.add(effect);
                }
            }
            if (filteredEffects.isEmpty()) continue;

            List<UUID> filteredDeclaredAttackerIds = null;
            for (int effectIndex = 0; effectIndex < filteredEffects.size(); effectIndex++) {
                if (filteredEffects.get(effectIndex) instanceof GrantKeywordEffect grant
                        && grant.scope() == GrantScope.TARGETS && grant.filter() != null) {
                    FilterContext attackFilterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getCard().getId())
                            .withSourceControllerId(playerId);
                    filteredDeclaredAttackerIds = attackerIndices.stream()
                            .map(battlefield::get)
                            .filter(attacker -> predicateEvaluationService.matchesPermanentPredicate(
                                    attacker, grant.filter(), attackFilterContext))
                            .map(Permanent::getId)
                            .toList();
                    filteredEffects.set(effectIndex, new GrantKeywordEffect(grant.keywords(), GrantScope.TARGETS,
                            null, grant.duration(), grant.grantCondition()));
                }
            }
            if (filteredDeclaredAttackerIds != null && filteredDeclaredAttackerIds.isEmpty()) continue;

            int previousCopies = oncePerTurn ? gameData.beginTriggeredAbilityCopies(1)
                    : beginAttackTriggerCopies(gameData, playerId, perm);
            try {
                if (filteredEffects.size() == 1
                        && filteredEffects.getFirst() instanceof ChooseOneAtTriggerTimeEffect modal) {
                    gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                            perm.getCard(), playerId, modal.choice(), perm.getId()));
                    gameLogService.append(gameData,
                            GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                    log.info("Game {} - {} attack trigger queued for mode selection",
                            gameData.id, perm.getCard().getName());
                } else {
                    boolean needsGraveyardTarget = filteredEffects.stream()
                            .anyMatch(e -> e instanceof GraveyardCardChoosingEffect choosingEffect
                                    && choosingEffect.choosesGraveyardCards()
                                    || e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
                    boolean needsTarget = filteredEffects.stream()
                            .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                    || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                    if (needsGraveyardTarget) {
                        if (gameData.interaction.isAwaitingInput()) {
                            // A previous attack trigger is already choosing its graveyard targets; queue this one
                            // so it prompts after the first resolves its choice instead of overwriting it.
                            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                    perm.getCard(), playerId, filteredEffects, null, 0, 0, 0, null, false, null,
                                    perm.getId(), false,
                                    attackerIndices.stream().map(battlefield::get)
                                            .map(attacker -> snapshotDeclaredAttacker(gameData, attacker)).toList()));
                        } else {
                            graveyardTargetingService.handleAttackGraveyardTargeting(
                                    gameData, playerId, perm.getCard(), filteredEffects, perm.getId(), null,
                                    attackerIndices.stream().map(battlefield::get)
                                            .map(attacker -> snapshotDeclaredAttacker(gameData, attacker)).toList());
                        }
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s attack ability triggers.").build());
                        log.info("Game {} - {} targeted ON_ALLY_CREATURES_ATTACK graveyard trigger queued",
                                gameData.id, perm.getCard().getName());
                    } else if (needsTarget) {
                        Card allyAttackCard = perm.getCard();
                        // A group no effect is bound to is a bare positional target that a bound effect reads by
                        // index (Gimli's Reckless Might's attacking creature read by its fight), so it must be
                        // chosen too and the single-group shortcut cannot apply. A bare group belongs to the
                        // ability of the next bound group after it, so one read by another ability (Hunter's
                        // Talent's ETB "creature you control") does not count.
                        boolean singleBoundTargetGroup = filteredEffects.stream().allMatch(allyAttackCard::hasEffectTargetIndex)
                                && filteredEffects.stream().map(allyAttackCard::getEffectTargetIndex).distinct().count() == 1
                                && allyAttackCard.getTargetGroups().stream()
                                .allMatch(group -> allyAttackCard.bindsEffectToTargetGroup(group.getIndex())
                                        || !bareGroupBelongsToEffects(allyAttackCard, group.getIndex(), filteredEffects));
                        if (allyAttackCard.getTargetGroups().size() > 1 && !singleBoundTargetGroup) {
                            gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                    perm.getCard(), playerId, filteredEffects, perm.getId(),
                                    List.of(), 0, 0, List.of(), attackerIndices.size()));
                        } else {
                            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                    perm.getCard(), playerId, filteredEffects, perm.getId(), playerId, null,
                                    null, attackerIndices.size()));
                        }
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s attack ability triggers.").build());
                        log.info("Game {} - {} targeted ON_ALLY_CREATURES_ATTACK trigger queued for target selection",
                                gameData.id, perm.getCard().getName());
                    } else {
                        boolean referencesDeclaredAttackers = filteredEffects.stream()
                                .anyMatch(e -> e instanceof GrantKeywordEffect grant
                                        && grant.scope() == GrantScope.TARGETS);
                        StackEntry attackTrigger = new StackEntry(
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
                                filteredDeclaredAttackerIds != null
                                        ? filteredDeclaredAttackerIds
                                        : referencesDeclaredAttackers
                                        ? attackerIndices.stream().map(battlefield::get).map(Permanent::getId).toList()
                                        : null
                        );
                        attackTrigger.setNonTargeting(referencesDeclaredAttackers);
                        if (triggerSource.bandMembers() != null) {
                            attackTrigger.setDeclaredTargetIds(triggerSource.bandMembers());
                            attackTrigger.setNonTargeting(true);
                        }
                        if (matchingAttackerCount != null) {
                            attackTrigger.setEventValue(matchingAttackerCount);
                        }
                        attackTrigger.setMarkSourceOncePerTurnOnAcceptance(markOncePerTurnOnAcceptance);
                        attackTrigger.setAttackingPermanentSnapshots(attackerIndices.stream()
                                .map(battlefield::get).map(attacker -> snapshotDeclaredAttacker(gameData, attacker)).toList());
                        gameData.stack.add(attackTrigger);
                        if (perm.isAttacking()) {
                            triggerCollectionService.checkAttackingCreatureTriggeredAbilityTriggers(
                                    gameData, perm, attackTrigger);
                        }
                        if (oncePerTurnForPlayer) {
                            markFirstQualifyingAttackThisTurn(gameData, playerId);
                        } else if (oncePerTurn && !markOncePerTurnOnAcceptance) {
                            gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                        }
                        if (onceOnly) {
                            OnceOnlyTriggerSupport.mark(gameData, perm);
                        }
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s attack ability triggers.").build());
                        log.info("Game {} - {} ON_ALLY_CREATURES_ATTACK trigger pushed onto stack (attacker count: {})",
                                gameData.id, perm.getCard().getName(), attackerIndices.size());
                    }
                }
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
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
        List<Map.Entry<EffectSlot, UUID>> attackedDefenderSlots = new ArrayList<>();
        for (UUID attackedPlayerId : directlyAttackedPlayers) {
            attackedDefenderSlots.add(Map.entry(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER, attackedPlayerId));
            attackedDefenderSlots.add(Map.entry(
                    EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER_OR_PLANESWALKER, attackedPlayerId));
        }
        for (UUID attackedPlaneswalkerId : directlyAttackedPlaneswalkers) {
            attackedDefenderSlots.add(Map.entry(
                    EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER_OR_PLANESWALKER, attackedPlaneswalkerId));
        }
        for (Map.Entry<EffectSlot, UUID> attackedDefenderSlot : attackedDefenderSlots) {
            EffectSlot attackPlayerSlot = attackedDefenderSlot.getKey();
            UUID attackedPlayerId = attackedDefenderSlot.getValue();
            for (Permanent perm : new ArrayList<>(battlefield)) {
                List<CardEffect> effects = new ArrayList<>(perm.getCard().getEffects(attackPlayerSlot));
                effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                        gameData, perm, attackPlayerSlot));
                if (effects.isEmpty()) continue;

                ConditionContext targetContext = ConditionContext.forPermanent(perm, playerId)
                        .withTargetId(attackedPlayerId);
                int targetAttackerCount = (int) attackerIndices.stream()
                        .map(resolvedTargets::get)
                        .filter(attackedPlayerId::equals)
                        .count();
                List<CardEffect> matchingEffects = new ArrayList<>();
                for (CardEffect effect : effects) {
                    if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof MinimumAttackers minimumAttackers) {
                        if (targetAttackerCount < minimumAttackers.minimumAttackers()) continue;
                        matchingEffects.add(conditional.wrapped());
                    } else if (effect instanceof ConditionalEffect conditional
                            && (conditional.condition() instanceof HasAttacker
                            || containsHasAttackerCondition(conditional.condition())
                            || conditional.condition() instanceof MinimumMatchingAttackers)) {
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(), targetContext)) {
                            continue;
                        }
                        matchingEffects.add(conditional.wrapped());
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.condition() instanceof SourceClassLevelAtLeast) {
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(), targetContext)) {
                            continue;
                        }
                        matchingEffects.add(effect);
                    } else {
                        matchingEffects.add(effect);
                    }
                }
                if (matchingEffects.isEmpty()) continue;

                int previousCopies = beginAttackTriggerCopies(gameData, playerId, perm);
                try {
                    boolean needsTarget = matchingEffects.stream()
                            .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                    if (needsTarget) {
                        gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                perm.getCard(), playerId, matchingEffects, perm.getId(), playerId,
                                attackedPlayerId));
                    } else {
                        StackEntry attackTrigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s attack trigger",
                                matchingEffects,
                                0,
                                perm.getId());
                        attackTrigger.setEventValue(targetAttackerCount);
                        attackTrigger.setAttackedTargetId(attackedPlayerId);
                        attackTrigger.setNonTargeting(true);
                        gameData.stack.add(attackTrigger);
                    }
                    gameLogService.append(gameData,
                            GameLog.builder().card(perm.getCard()).text("'s attack ability triggers.").build());
                    log.info("Game {} - {} attack-player trigger for player {}",
                            gameData.id, perm.getCard().getName(), attackedPlayerId);
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
            }
        }

        // Check for "whenever a creature you control attacks" triggers (ON_ALLY_CREATURE_ATTACKS)
        // These fire once per attacking creature (not once per combat like ON_ALLY_CREATURES_ATTACK).
        // Supports card and permanent conditional effects to filter by the attacking creature.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (Permanent perm : battlefield) {
                List<CardEffect> perCreatureAttackEffects = new ArrayList<>();
                List<CardEffect> printedAttackEffects = LayerSystemService.currentCard(gameData, perm)
                        .getEffects(EffectSlot.ON_ALLY_CREATURE_ATTACKS);
                if (!printedAttackEffects.isEmpty() && !perm.isFaceDown()
                        && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                    perCreatureAttackEffects.addAll(printedAttackEffects);
                }
                List<CardEffect> temporaryAttackEffects =
                        perm.getTemporaryTriggeredEffects(EffectSlot.ON_ALLY_CREATURE_ATTACKS);
                List<CardEffect> persistentAttackEffects =
                        perm.getPersistentTriggeredEffects(EffectSlot.ON_ALLY_CREATURE_ATTACKS);
                if ((!temporaryAttackEffects.isEmpty() || !persistentAttackEffects.isEmpty())
                        && !gameQueryService.hasLostAllAbilities(gameData, perm)) {
                    perCreatureAttackEffects.addAll(temporaryAttackEffects);
                    perCreatureAttackEffects.addAll(persistentAttackEffects);
                }
                List<CardEffect> grantedAttackEffects = grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                        gameData, perm, EffectSlot.ON_ALLY_CREATURE_ATTACKS);
                perCreatureAttackEffects.addAll(grantedAttackEffects);
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
                        FilterContext sourceContext = FilterContext.of(gameData)
                                .withSourceCardId(perm.getOriginalCard().getId())
                                .withSourceControllerId(playerId)
                                .withSourcePermanentId(perm.getId());
                        if (!predicateEvaluationService.matchesPermanentPredicate(
                                attacker, permConditional.predicate(), sourceContext)) {
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

                matchingEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttacksPlayerAlone
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                matchingEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof SourceAttackedThisCombat
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(perm, playerId)));

                // An intervening-if (CR 603.4) on the ability stops it from triggering at all, so its
                // targets are not chosen for a trigger that would never go on the stack (e.g. a Class's
                // higher-level ability while the Class is a lower level). "Attacks a player alone" was
                // already checked against the attacking creature above, not the ability's source.
                matchingEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.interveningIf()
                        && !(ce.condition() instanceof AttacksPlayerAlone)
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(perm, playerId)));

                // The surviving attacks-alone conditionals are already satisfied, so unwrap them to
                // their inner effect. This lets a wrapped "you may" (e.g. Angelic Benediction's
                // "you may tap target creature") route through the may/mandatory split below.
                matchingEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttacksAlone ? ce.wrapped() : e);
                matchingEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttacksPlayerAlone ? ce.wrapped() : e);

                if (matchingEffects.isEmpty()) continue;

                // A modal choice made while the trigger goes on the stack (CR 603.3c) is prompted now,
                // with the attacking creature as the triggering permanent for its mode effects.
                if (matchingEffects.size() == 1
                        && matchingEffects.getFirst() instanceof ChooseOneAtTriggerTimeEffect modal) {
                    int previousCopies = beginAttackTriggerCopies(gameData, playerId, perm);
                    try {
                        gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                                perm.getCard(), playerId, modal.choice(), perm.getId(), false, false, null,
                                attacker.getAttackTarget(), attacker.getId(), false));
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                        log.info("Game {} - {} attack trigger queued for mode selection",
                                gameData.id, perm.getCard().getName());
                    } finally {
                        gameData.restoreTriggeredAbilityCopies(previousCopies);
                    }
                    continue;
                }

                if (matchingEffects.size() == 1
                        && matchingEffects.getFirst() instanceof ChooseModeNotYetChosenThisTurnEffect modal) {
                    int previousCopies = beginAttackTriggerCopies(gameData, playerId, perm);
                    try {
                        gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                                perm.getCard(), playerId, new ChooseOneEffect(modal.options()), perm.getId(), true));
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                        log.info("Game {} - {} attack trigger queued for mode selection",
                                gameData.id, perm.getCard().getName());
                    } finally {
                        gameData.restoreTriggeredAbilityCopies(previousCopies);
                    }
                    continue;
                }

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

                int previousCopies = beginAttackTriggerCopies(gameData, playerId, perm);
                try {
                    for (CardEffect effect : mayEffects) {
                        if (effect.targetSpec().declaredTarget() != null) {
                            UUID damageSourceId = effect instanceof MayEffect may
                                    && may.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect
                                    ? attacker.getId() : perm.getId();
                            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                    perm.getCard(), playerId, List.of(effect), damageSourceId, playerId,
                                    attacker.getAttackTarget(), attacker.getId()));
                        } else {
                            gameData.queueMayAbility(perm.getCard(), playerId, (MayEffect) effect, null,
                                    attacker.getId(), attacker.getAttackTarget());
                        }
                    }

                    if (!mandatoryEffects.isEmpty()) {
                        List<CardEffect> bundledEffects = new ArrayList<>(mandatoryEffects);
                        List<List<CardEffect>> abilities = new ArrayList<>();
                        for (var registration : perm.getCard().getEffectRegistrations(EffectSlot.ON_ALLY_CREATURE_ATTACKS)) {
                            if (registration.triggerMode()
                                    != com.github.laxika.magicalvibes.model.TriggerMode.INDEPENDENT) {
                                continue;
                            }
                            CardEffect registeredEffect = registration.effect();
                            if (registeredEffect instanceof TriggeringCardConditionalEffect conditional) {
                                registeredEffect = conditional.wrapped();
                            } else if (registeredEffect instanceof TriggeringPermanentConditionalEffect conditional) {
                                registeredEffect = conditional.wrapped();
                            } else if (registeredEffect instanceof ConditionalEffect conditional
                                    && (conditional.condition() instanceof AttacksAlone
                                    || conditional.condition() instanceof AttacksPlayerAlone)) {
                                registeredEffect = conditional.wrapped();
                            }
                            if (bundledEffects.remove(registeredEffect)) {
                                abilities.add(List.of(registeredEffect));
                            }
                        }
                        for (CardEffect grantedEffect : grantedAttackEffects) {
                            CardEffect unwrappedGranted = grantedEffect;
                            if (unwrappedGranted instanceof TriggeringCardConditionalEffect conditional) {
                                unwrappedGranted = conditional.wrapped();
                            } else if (unwrappedGranted instanceof TriggeringPermanentConditionalEffect conditional) {
                                unwrappedGranted = conditional.wrapped();
                            } else if (unwrappedGranted instanceof ConditionalEffect conditional
                                    && (conditional.condition() instanceof AttacksAlone
                                    || conditional.condition() instanceof AttacksPlayerAlone)) {
                                unwrappedGranted = conditional.wrapped();
                            }
                            if (bundledEffects.remove(unwrappedGranted)) {
                                abilities.add(List.of(unwrappedGranted));
                            }
                        }
                        if (!bundledEffects.isEmpty()) {
                            abilities.addFirst(bundledEffects);
                        }
                        for (List<CardEffect> abilityEffects : abilities) {
                            boolean needsTarget = abilityEffects.stream()
                                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                                            || perm.getCard().getEffectTargetIndex(e) >= 0);
                            UUID attackedTargetId = attacker.getAttackTarget();
                            if (needsTarget) {
                                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                        perm.getCard(), playerId, abilityEffects, perm.getId(), playerId,
                                        attackedTargetId, attacker.getId()));
                            } else {
                                StackEntry attackTrigger = new StackEntry(
                                        StackEntryType.TRIGGERED_ABILITY,
                                        perm.getCard(),
                                        playerId,
                                        perm.getCard().getName() + "'s attack trigger",
                                        abilityEffects,
                                        null,
                                        perm.getId()
                                );
                                attackTrigger.setSourcePermanentSnapshot(new Permanent(perm));
                                attackTrigger.setAttackedTargetId(attackedTargetId);
                                attackTrigger.setDefendingPlayerId(gameData.playerIds.contains(attackedTargetId)
                                        ? attackedTargetId : gameQueryService.findPermanentController(gameData, attackedTargetId));
                                // Record the triggering attacker as a non-targeting reference so effects that
                                // act on "that creature" can find it.
                                attackTrigger.setTargetId(attacker.getId());
                                attackTrigger.setNonTargeting(true);
                                attackTrigger.setTriggeringPermanentId(attacker.getId());
                                attackTrigger.setTriggeringPermanentOwnerId(
                                        gameData.defaultControllerOf(attacker.getId()));
                                if (abilityEffects.stream().anyMatch(e -> e instanceof MayPayManaEffect mayPay
                                        && mayPay.wrapped() instanceof TriggeringPermanentManaValueEffect valueEffect
                                        && valueEffect.usesTriggeringPermanentManaValue())) {
                                    attackTrigger.setEventValue(attacker.getCard().getManaValue());
                                }
                                gameData.stack.add(attackTrigger);
                            }
                        }
                    }

                    gameLogService.append(gameData,
                            GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                    log.info("Game {} - {} ON_ALLY_CREATURE_ATTACKS trigger for {} attacking",
                            gameData.id, perm.getCard().getName(), attacker.getCard().getName());
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
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
                for (int i = 0; i < instances; i++) {
                    StackEntry exaltedTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s exalted ability",
                            List.of(new BoostTargetCreatureEffect(1, 1)),
                            null,
                            perm.getId());
                    exaltedTrigger.setTargetId(attacker.getId());
                    exaltedTrigger.setNonTargeting(true);
                    exaltedTrigger.setTriggeringPermanentId(attacker.getId());
                    gameData.stack.add(exaltedTrigger);
                    gameLogService.append(gameData, GameLog.builder().card(perm.getCard())
                            .text("'s exalted ability triggers.").build());
                }
            }
        }

        for (int idx : attackerIndices) {
            triggerCollectionService.checkPlanarAllyCreatureAttackTriggers(gameData, battlefield.get(idx));
        }
        triggerCollectionService.checkPlanarAllyCreaturesAttackTriggers(
                gameData, playerId, attackerIndices.size());

        // Check for graveyard-based "whenever you attack with N or more creatures" triggers
        // (GRAVEYARD_ON_ALLY_CREATURES_ATTACK). These fire from the controller's graveyard; "whenever your
        // commander attacks" abilities also fire from the graveyard of a commander's owner who isn't
        // the attacking player.
        for (UUID graveyardOwnerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            if (graveyard == null) continue;
            boolean attackingPlayersGraveyard = graveyardOwnerId.equals(playerId);
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> gyAttackEffects = gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK);
                if (gyAttackEffects.isEmpty()) continue;

                for (CardEffect effect : gyAttackEffects) {
                    CardEffect innerEffect = effect;

                    if (isOwnedCommanderAttackTrigger(effect)) {
                        ConditionalEffect commanderTrigger = (ConditionalEffect) effect;
                        PermanentPredicate commanderPredicate = ((HasAttacker) commanderTrigger.condition()).predicate();
                        long attackingCommanders = attackerIndices.stream()
                                .map(battlefield::get)
                                .filter(attacker -> predicateEvaluationService.matchesPermanentPredicate(
                                        attacker, commanderPredicate, FilterContext.of(gameData)
                                                .withSourceCardId(card.getId())
                                                .withSourceControllerId(graveyardOwnerId)))
                                .count();
                        for (long i = 0; i < attackingCommanders; i++) {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    card,
                                    graveyardOwnerId,
                                    card.getName() + "'s graveyard attack trigger",
                                    new ArrayList<>(List.of(commanderTrigger.wrapped())),
                                    1,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            ));
                            gameLogService.append(gameData,
                                    GameLog.builder().card(card).text("'s graveyard attack ability triggers.").build());
                            log.info("Game {} - {} graveyard commander attack trigger pushed onto stack",
                                    gameData.id, card.getName());
                        }
                        continue;
                    }
                    if (!attackingPlayersGraveyard) continue;

                    ConditionContext triggerContext = new ConditionContext(playerId, null, null, card,
                            false, false, false, false, null, attackerIndices.size(), null, null, false);
                    if (!graveyardAttackConditionsMet(gameData, innerEffect, triggerContext)) {
                        log.info("Game {} - {} graveyard attack trigger skipped because its condition was not met",
                                gameData.id, card.getName());
                        continue;
                    }

                    // Unwrap minimum-attackers conditionals — check minimum before offering the trigger
                    if (innerEffect instanceof ConditionalEffect ce
                            && ce.condition() instanceof MinimumAttackers mac) {
                        innerEffect = ce.wrapped();
                    }
                    // "Whenever one or more legendary creatures you control attack" is a plain event
                    // trigger: the attackers were checked above and the ability resolves even if they
                    // have left the battlefield since.
                    if (innerEffect instanceof ConditionalEffect ce
                            && containsHasAttackerCondition(ce.condition())) {
                        innerEffect = ce.wrapped();
                    }

                    StackEntry attackTrigger = new StackEntry(
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
                    );
                    // CR 400.7: a card that leaves and re-enters the graveyard before this trigger resolves is a
                    // new object, so the trigger must not act on it.
                    attackTrigger.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                    gameData.stack.add(attackTrigger);
                    gameLogService.append(gameData,
                            GameLog.builder().card(card).text("'s graveyard attack ability triggers.").build());
                    log.info("Game {} - {} GRAVEYARD_ON_ALLY_CREATURES_ATTACK trigger pushed onto stack (attacker count: {})",
                            gameData.id, card.getName(), attackerIndices.size());
                }
            }
        }

        // Check for exile-based "whenever you attack" triggers. These fire once per combat
        // while the face-up card is in its owner's exile zone.
        if (!attackerIndices.isEmpty()) {
            for (ExiledCardEntry exiled : new ArrayList<>(gameData.exiledCards)) {
                if (exiled.faceDown() || !playerId.equals(exiled.ownerId())) {
                    continue;
                }
                List<CardEffect> exileAttackEffects = exiled.card().getEffects(
                        EffectSlot.EXILE_ON_ALLY_CREATURES_ATTACK);
                if (exileAttackEffects.isEmpty()) {
                    continue;
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        exiled.card(),
                        playerId,
                        exiled.card().getName() + "'s exile attack trigger",
                        new ArrayList<>(exileAttackEffects),
                        attackerIndices.size(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ));
                gameLogService.append(gameData,
                        GameLog.builder().card(exiled.card()).text("'s exile attack ability triggers.").build());
                log.info("Game {} - {} EXILE_ON_ALLY_CREATURES_ATTACK trigger pushed onto stack (attacker count: {})",
                        gameData.id, exiled.card().getName(), attackerIndices.size());
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
                List<CardEffect> attackedTriggerEffects = new ArrayList<>();
                List<CardEffect> candidateEffects = new ArrayList<>();
                if (!battleAttacked) {
                    candidateEffects.addAll(perm.getCard().getEffects(EffectSlot.ON_CREATURE_ATTACKS_YOU));
                }
                candidateEffects.addAll(
                        perm.getCard().getEffects(EffectSlot.ON_CREATURE_ATTACKS_DEFENDING_PLAYER));
                if (gameData.playerIds.contains(attackedTargetId)) {
                    candidateEffects.addAll(
                            perm.getCard().getEffects(EffectSlot.ON_CREATURE_ATTACKS_YOU_DIRECTLY));
                }
                for (CardEffect attackedEffect : candidateEffects) {
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

                int previousCopies = beginAttackTriggerCopies(gameData, attackedPlayerId, perm);
                try {
                    List<List<CardEffect>> abilities = new ArrayList<>();
                    List<CardEffect> bundledEffects = new ArrayList<>(attackedTriggerEffects);
                    for (EffectSlot triggerSlot : List.of(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                            EffectSlot.ON_CREATURE_ATTACKS_YOU_DIRECTLY,
                            EffectSlot.ON_CREATURE_ATTACKS_DEFENDING_PLAYER)) {
                        for (var registration : perm.getCard().getEffectRegistrations(triggerSlot)) {
                            if (registration.triggerMode() == com.github.laxika.magicalvibes.model.TriggerMode.INDEPENDENT
                                    && bundledEffects.remove(registration.effect())) {
                                abilities.add(List.of(registration.effect()));
                            }
                        }
                    }
                    if (!bundledEffects.isEmpty()) abilities.addFirst(bundledEffects);
                    for (List<CardEffect> abilityEffects : abilities) {
                        StackEntry attackedTrigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                attackedPlayerId,
                                perm.getCard().getName() + "'s trigger",
                                new ArrayList<>(abilityEffects),
                                attacker.getId(),
                                perm.getId()
                        );
                        attackedTrigger.setNonTargeting(true);
                        attackedTrigger.setTriggeringPermanentId(attacker.getId());
                        attackedTrigger.setTriggeringPermanentControllerId(playerId);
                        attackedTrigger.setSourcePermanentSnapshot(new Permanent(perm));
                        gameData.stack.add(attackedTrigger);
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                        log.info("Game {} - {} attacked-player trigger for {} attacking",
                                gameData.id, perm.getCard().getName(), attacker.getCard().getName());
                    }
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
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
            for (Permanent perm : new ArrayList<>(defenderBattlefield)) {
                List<CardEffect> effects = new ArrayList<>();
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CREATURES_ATTACK_YOU)) {
                    if (effect instanceof ConditionalEffect conditional && conditional.interveningIf()
                            && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, attackedPlayerId))) {
                        continue;
                    }
                    effects.add(effect);
                }
                if (effects.isEmpty()) continue;

                int previousCopies = beginAttackTriggerCopies(gameData, attackedPlayerId, perm);
                try {
                    boolean needsTarget = effects.stream()
                            .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                    if (needsTarget) {
                        UUID targetChooserId = effects.stream()
                                .filter(MayEffect.class::isInstance)
                                .map(MayEffect.class::cast)
                                .anyMatch(may -> may.choicePlayer() == MayChoicePlayer.ACTIVE_PLAYER)
                                ? playerId : attackedPlayerId;
                        gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                perm.getCard(), attackedPlayerId, effects, perm.getId(), targetChooserId,
                                attackedPlayerId));
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                        log.info("Game {} - {} ON_CREATURES_ATTACK_YOU trigger awaiting target for player {}",
                                gameData.id, perm.getCard().getName(), attackedPlayerId);
                    } else {
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
                        trigger.setActivePlayerId(playerId);
                        trigger.setAttackingPermanentSnapshots(attackerIndices.stream()
                                .filter(idx -> attackedPlayerId.equals(resolvedTargets.get(idx)))
                                .map(battlefield::get)
                                .map(attacker -> snapshotDeclaredAttacker(gameData, attacker)).toList());
                        gameData.stack.add(trigger);
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                        log.info("Game {} - {} ON_CREATURES_ATTACK_YOU trigger for player {}",
                                gameData.id, perm.getCard().getName(), attackedPlayerId);
                    }
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
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
            for (Permanent perm : new ArrayList<>(defenderBattlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(
                        EffectSlot.ON_OPPONENT_ATTACKS_PLANESWALKER_YOU_CONTROL);
                if (effects.isEmpty()) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        attackedPlayerId,
                        perm.getCard().getName() + "'s trigger",
                        new ArrayList<>(effects),
                        playerId,
                        perm.getId()
                );
                trigger.setNonTargeting(true);
                // The attack causes this trigger, so "triggers an additional time" effects (Isshin) apply.
                int previousCopies = gameData.beginTriggeredAbilityCopies(1
                        + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        gameData, attackedPlayerId, perm, true));
                try {
                    gameData.stack.add(trigger);
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
                gameLogService.append(gameData,
                        GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                log.info("Game {} - {} ON_OPPONENT_ATTACKS_PLANESWALKER_YOU_CONTROL trigger for attacking player {}",
                        gameData.id, perm.getCard().getName(), playerId);
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
        // Supports nested TriggeringPermanentConditionalEffect and
        // TriggeringPermanentControllerConditionalEffect wrappers to restrict which attackers
        // trigger the ability (e.g. Windreader Sphinx and Nahiri, Forged in Fury).
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            UUID attackedTargetId = attacker.getAttackTarget();
            for (Map.Entry<UUID, List<Permanent>> bf : gameData.playerBattlefields.entrySet()) {
                UUID permController = bf.getKey();
                for (Permanent perm : new ArrayList<>(bf.getValue())) {
                    List<CardEffect> anyAttackEffects = perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_ATTACKS);
                    if (anyAttackEffects.isEmpty()) continue;

                    List<CardEffect> matchingAnyAttackEffects = new ArrayList<>();
                    for (CardEffect effect : anyAttackEffects) {
                        CardEffect matchingEffect = effect;
                        boolean matches = true;
                        FilterContext sourceContext = FilterContext.of(gameData)
                                .withSourceCardId(perm.getOriginalCard().getId())
                                .withSourceControllerId(permController)
                                .withSourcePermanentId(perm.getId());
                        while (matches) {
                            if (matchingEffect instanceof PutCounterOnAttackingCreatureOnAttacksYouEffect counterEffect) {
                                if (!perm.isAttached()
                                        || !gameData.playerIds.contains(attackedTargetId)
                                        || !attackedTargetId.equals(perm.getAttachedTo())) {
                                    matches = false;
                                } else {
                                    matchingEffect = new PutCounterOnTargetPermanentEffect(counterEffect.counterType());
                                }
                            } else if (matchingEffect instanceof TriggeringPermanentControllerConditionalEffect controllerConditional) {
                                if (!permController.equals(gameQueryService.findPermanentController(gameData, attacker.getId()))) {
                                    matches = false;
                                } else {
                                    matchingEffect = controllerConditional.wrapped();
                                }
                            } else if (matchingEffect instanceof TriggeringPermanentConditionalEffect permConditional) {
                                if (!predicateEvaluationService.matchesPermanentPredicate(attacker,
                                        permConditional.predicate(), sourceContext)) {
                                    matches = false;
                                } else {
                                    matchingEffect = permConditional.wrapped();
                                }
                            } else if (matchingEffect instanceof ConditionalEffect attackedTargetConditional
                                    && attackedTargetConditional.condition() instanceof AttackedTargetIsMonarch) {
                                // Kept on the stack entry so it is checked again on resolution (CR 603.4).
                                ConditionContext attackedTargetContext = ConditionContext
                                        .forPermanent(perm, permController)
                                        .withTargetId(attackedTargetId);
                                matches = conditionEvaluationService.isMet(gameData,
                                        attackedTargetConditional.condition(), attackedTargetContext);
                                break;
                            } else {
                                break;
                            }
                        }
                        if (matches) matchingAnyAttackEffects.add(matchingEffect);
                    }
                    if (matchingAnyAttackEffects.isEmpty()) continue;

                    int previousCopies = beginAttackTriggerCopies(gameData, permController, perm);
                    try {
                        StackEntry anyAttackTrigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                permController,
                                perm.getCard().getName() + "'s trigger",
                                matchingAnyAttackEffects,
                                attacker.getId(),
                                perm.getId()
                        );
                        anyAttackTrigger.setTriggeringPermanentId(attacker.getId());
                        anyAttackTrigger.setSourcePermanentSnapshot(new Permanent(perm));
                        anyAttackTrigger.setAttackingPermanentSnapshots(List.of(new Permanent(attacker)));
                        anyAttackTrigger.setTriggeringPermanentControllerId(playerId);
                        anyAttackTrigger.setNonTargeting(true);
                        anyAttackTrigger.setAttackedTargetId(attacker.getAttackTarget());
                        anyAttackTrigger.setTriggeringPermanentId(attacker.getId());
                        anyAttackTrigger.setTriggeringPermanentControllerId(
                                gameQueryService.findPermanentController(gameData, attacker.getId()));
                        gameData.stack.add(anyAttackTrigger);
                        gameLogService.append(gameData,
                                GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                        log.info("Game {} - {} ON_ANY_CREATURE_ATTACKS trigger for {} attacking",
                                gameData.id, perm.getCard().getName(), attacker.getCard().getName());
                    } finally {
                        gameData.restoreTriggeredAbilityCopies(previousCopies);
                    }
                }
            }
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
        // combat. PlayerAttacksOneOfYourOpponents is the exception: it queues once per distinct
        // opponent directly attacked. The attacking player is stored as a non-targeting targetId
        // so player-scoped effects can act on "that player" (e.g. Total War's sweep of their
        // non-attackers).
        for (Map.Entry<UUID, List<Permanent>> bf : gameData.playerBattlefields.entrySet()) {
            UUID permController = bf.getKey();
            for (Permanent perm : new ArrayList<>(bf.getValue())) {
                List<CardEffect> playerAttackEffects = new ArrayList<>();
                Map<UUID, List<CardEffect>> effectsByAttackedOpponent = new LinkedHashMap<>();
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_ATTACKS)) {
                    if (effect instanceof ConditionalEffect conditional) {
                        if (conditional.condition()
                                instanceof PlayerAttacksOneOfYourOpponentsWithPowerOrToughnessEqualToSourceChosenNumber) {
                            for (UUID attackedOpponentId : attackedOpponents(gameData, permController, resolvedTargets)) {
                                if (hasMatchingAttackerForTarget(gameData, battlefield, attackerIndices,
                                        resolvedTargets, attackedOpponentId, perm)) {
                                    effectsByAttackedOpponent
                                            .computeIfAbsent(attackedOpponentId, ignored -> new ArrayList<>())
                                            .add(conditional.wrapped());
                                }
                            }
                            continue;
                        }
                        if (conditional.condition() instanceof PlayerAttacksOneOfYourOpponents) {
                            for (UUID attackedOpponentId : attackedOpponents(gameData, permController, resolvedTargets)) {
                                effectsByAttackedOpponent
                                        .computeIfAbsent(attackedOpponentId, ignored -> new ArrayList<>())
                                        .add(conditional.wrapped());
                            }
                            continue;
                        }
                        if (conditional.condition() instanceof OpponentAttacksAnotherOpponent) {
                            if (!permController.equals(playerId)) {
                                for (UUID attackedOpponentId : attackedOpponents(gameData, permController, resolvedTargets)) {
                                    if (!attackedOpponentId.equals(playerId)) {
                                        effectsByAttackedOpponent
                                                .computeIfAbsent(attackedOpponentId, ignored -> new ArrayList<>())
                                                .add(conditional.wrapped());
                                    }
                                }
                            }
                            continue;
                        }
                        if (conditional.condition() instanceof PlayerAttacksOneOrMoreOfYourOpponents) {
                            if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                                    ConditionContext.forPermanent(perm, permController).withTargetId(playerId))) {
                                playerAttackEffects.add(conditional.wrapped());
                            }
                            continue;
                        }
                        if (conditional.condition() instanceof AttackedOpponentHasMoreLifeThanAnotherOpponent) {
                            for (UUID attackedOpponentId : attackedOpponents(gameData, permController, resolvedTargets)) {
                                if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                                        ConditionContext.forPermanent(perm, permController).withTargetId(attackedOpponentId))) {
                                    effectsByAttackedOpponent.computeIfAbsent(attackedOpponentId,
                                            ignored -> new ArrayList<>()).add(conditional);
                                }
                            }
                            continue;
                        }
                        if (conditional.condition() instanceof AllOf) {
                            ConditionContext attackContext = ConditionContext.forPermanent(perm, permController)
                                    .withTargetId(playerId)
                                    .withXValue(attackerIndices.size());
                            if (conditionEvaluationService.isMet(gameData, conditional.condition(), attackContext)) {
                                // Kept as a ConditionalEffect so the intervening "if" is rechecked on resolution.
                                playerAttackEffects.add(conditional);
                            }
                            continue;
                        }
                        if (conditional.condition() instanceof MinimumMatchingAttackers matchingAttackers
                                && countMatchingAttackerIndices(gameData, battlefield, attackerIndices, perm,
                                permController, matchingAttackers.predicate()) < matchingAttackers.minimum()) {
                            continue;
                        }
                        if (conditional.condition() instanceof MinimumAttackers
                                && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, permController)
                                        .withXValue(attackerIndices.size()))) {
                            continue;
                        }
                        if (conditional.condition() instanceof AttacksEnchantedPlayer
                                || conditional.condition() instanceof ControllerAndEnchantedPlayerAttackEachOther
                                || conditional.condition() instanceof OpponentAttacksWithAtLeastCreatures
                                || conditional.condition() instanceof OpponentsAttackWithAtLeastCreatures
                                || conditional.condition() instanceof OpponentAttacksPlaneswalker
                                || conditional.condition() instanceof PlayerAttacksNotController) {
                            if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                    ConditionContext.forPermanent(perm, permController).withTargetId(playerId))) {
                                continue;
                            }
                        }
                        if (conditional.condition() instanceof MinimumAttackers) {
                            playerAttackEffects.add(conditional.wrapped());
                            continue;
                        }
                        if (conditional.condition() instanceof MinimumMatchingAttackers) {
                            playerAttackEffects.add(conditional.wrapped());
                            continue;
                        }
                    }
                    if (effect instanceof ConditionalEffect conditional
                            && (conditional.condition() instanceof AttacksEnchantedPlayer
                            || conditional.condition() instanceof ControllerAndEnchantedPlayerAttackEachOther
                            || conditional.condition() instanceof OpponentsAttackWithAtLeastCreatures
                            || conditional.condition() instanceof OpponentAttacksPlaneswalker)) {
                        playerAttackEffects.add(conditional.wrapped());
                    } else {
                        playerAttackEffects.add(effect);
                    }
                }
                for (Map.Entry<UUID, List<CardEffect>> attackedOpponentEffects : effectsByAttackedOpponent.entrySet()) {
                    addPlayerAttackTrigger(gameData, permController, perm, attackedOpponentEffects.getValue(),
                            attackerIndices.size(), playerId, attackedOpponentEffects.getKey());
                }
                if (playerAttackEffects.isEmpty()) continue;

                int previousCopies = beginAttackTriggerCopies(gameData, permController, perm);
                try {
                    List<List<CardEffect>> triggerEffectGroups = new ArrayList<>();
                    List<CardEffect> bundledEffects = new ArrayList<>(playerAttackEffects);
                    for (var registration : perm.getCard().getEffectRegistrations(EffectSlot.ON_ANY_PLAYER_ATTACKS)) {
                        if (registration.triggerMode() == com.github.laxika.magicalvibes.model.TriggerMode.INDEPENDENT) {
                            CardEffect registeredEffect = registration.effect();
                            CardEffect resolvedEffect = registeredEffect instanceof ConditionalEffect conditional
                                    ? conditional.wrapped() : registeredEffect;
                            CardEffect bundledEffect = bundledEffects.contains(registeredEffect)
                                    ? registeredEffect : resolvedEffect;
                            if (bundledEffects.remove(bundledEffect)) {
                                triggerEffectGroups.add(List.of(bundledEffect));
                            }
                        }
                    }
                    if (!bundledEffects.isEmpty()) {
                        triggerEffectGroups.addFirst(bundledEffects);
                    }

                    for (List<CardEffect> triggerEffects : triggerEffectGroups) {
                        boolean needsTarget = triggerEffects.stream()
                                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                        || (perm.getCard().getDeclaredTargetFilter() != null
                                        && effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)));
                        if (needsTarget) {
                            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                    perm.getCard(), permController, triggerEffects, perm.getId(),
                                    triggerEffects.stream().anyMatch(effect -> effect instanceof MayEffect may
                                            && may.choicePlayer() == com.github.laxika.magicalvibes.model.MayChoicePlayer.ACTIVE_PLAYER)
                                            ? playerId : permController,
                                    null, null, attackerIndices.size()));
                            gameLogService.append(gameData,
                                    GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                            log.info("Game {} - {} targeted ON_ANY_PLAYER_ATTACKS trigger queued for target selection",
                                    gameData.id, perm.getCard().getName());
                        } else {
                            StackEntry playerAttackTrigger = new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    permController,
                                    perm.getCard().getName() + "'s trigger",
                                    new ArrayList<>(triggerEffects),
                                    attackerIndices.size(),
                                    perm.getId());
                            playerAttackTrigger.setTargetId(playerId);
                            playerAttackTrigger.setTriggeringPermanentControllerId(playerId);
                            playerAttackTrigger.setNonTargeting(true);
                            playerAttackTrigger.setSourcePermanentSnapshot(new Permanent(perm));
                            playerAttackTrigger.setAttackingPermanentSnapshots(attackerIndices.stream()
                                    .map(battlefield::get)
                                    .map(attacker -> snapshotDeclaredAttacker(gameData, attacker)).toList());
                            gameData.stack.add(playerAttackTrigger);
                            gameLogService.append(gameData,
                                    GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                            log.info("Game {} - {} ON_ANY_PLAYER_ATTACKS trigger for attacking player {}",
                                    gameData.id, perm.getCard().getName(), playerId);
                        }
                    }
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
            }
        }

        processDelayedAttackerBoostTriggers(gameData, battlefield, attackerIndices);
        processDelayedAttackerKeywordGrantTriggers(gameData, battlefield, attackerIndices);
        processDelayedNontokenAttackTokenTriggers(gameData, battlefield, attackerIndices);
        processDelayedAttackTokenCreationTriggers(gameData, playerId, attackerIndices);
        processDelayedAttackDamageTriggers(gameData, playerId, attackerIndices);
        processDelayedAttackUntapTriggers(gameData, playerId, attackerIndices);
        processDelayedVehicleAttackTriggers(gameData, battlefield, attackerIndices);
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

    private void checkCommandZoneAllyCreatureAttackTriggers(GameData gameData, UUID playerId,
                                                              int attackerCount) {
        List<Card> commandZone = gameData.playerCommandZones.get(playerId);
        if (commandZone == null) {
            return;
        }
        for (Card card : new ArrayList<>(commandZone)) {
            if (gameData.faceDownCommandZoneCards.contains(card.getId())) {
                continue;
            }
            List<CardEffect> effects = new ArrayList<>();
            for (CardEffect effect : card.getEffects(EffectSlot.COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK)) {
                if (effect instanceof ConditionalEffect conditional
                        && containsHasAttackerCondition(conditional.condition())) {
                    if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forCard(card, playerId))) {
                        continue;
                    }
                    effects.add(conditional.wrapped());
                } else {
                    effects.add(effect);
                }
            }
            if (effects.isEmpty()) {
                continue;
            }

            boolean needsTarget = effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            if (needsTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        card, playerId, effects, null, playerId, null, null, attackerCount));
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        playerId,
                        card.getName() + "'s attack trigger",
                        effects,
                        attackerCount,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
            }
            gameLogService.append(gameData,
                    GameLog.builder().card(card).text("'s attack ability triggers.").build());
            log.info("Game {} - {} command-zone attack trigger queued", gameData.id, card.getName());
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

    private void addEnlistmentTriggers(GameData gameData, UUID playerId, Map<UUID, Integer> boostPowers,
                                       Map<UUID, UUID> enlistedSupporters) {
        for (Map.Entry<UUID, Integer> boost : boostPowers.entrySet()) {
            Permanent attacker = gameQueryService.findPermanentById(gameData, boost.getKey());
            if (attacker == null) {
                continue;
            }
            int previousCopies = beginAttackTriggerCopies(gameData, playerId, attacker);
            try {
                StackEntry enlistTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        attacker.getCard(),
                        playerId,
                        attacker.getCard().getName() + "'s enlist trigger",
                        List.of(new BoostSelfEffect(
                                new ChosenPermanentPower(), new Fixed(0))),
                        attacker.getId(),
                        attacker.getId());
                enlistTrigger.setNonTargeting(true);
                enlistTrigger.setChosenPermanentId(enlistedSupporters.get(boost.getKey()));
                enlistTrigger.setSourcePermanentSnapshot(new Permanent(attacker));
                gameData.stack.add(enlistTrigger);
                Permanent enlistedCreature = gameQueryService.findPermanentById(
                        gameData, enlistedSupporters.get(boost.getKey()));
                triggerCollectionService.checkEnlistmentTriggeredAbilityTriggers(
                        gameData, attacker, enlistTrigger, enlistedCreature);
                gameLogService.append(gameData,
                        GameLog.builder().card(attacker.getCard()).text("'s enlist ability triggers.").build());
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
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

    private int beginAttackTriggerCopies(GameData gameData, UUID controllerId, Permanent source) {
        return gameData.beginTriggeredAbilityCopies(1 +
                gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        gameData, controllerId, source, true));
    }

    private void addPlayerAttackTrigger(GameData gameData, UUID sourceControllerId, Permanent source,
                                        List<CardEffect> effects, int attackerCount,
                                        UUID attackingPlayerId, UUID attackedTargetId) {
        if (effects.isEmpty()) {
            return;
        }
        int previousCopies = beginAttackTriggerCopies(gameData, sourceControllerId, source);
        try {
            StackEntry playerAttackTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    sourceControllerId,
                    source.getCard().getName() + "'s trigger",
                    new ArrayList<>(effects),
                    attackerCount,
                    source.getId());
            playerAttackTrigger.setTargetId(attackingPlayerId);
            playerAttackTrigger.setTriggeringPermanentControllerId(attackingPlayerId);
            playerAttackTrigger.setAttackedTargetId(attackedTargetId);
            playerAttackTrigger.setSourcePermanentSnapshot(new Permanent(source));
            playerAttackTrigger.setNonTargeting(true);
            gameData.stack.add(playerAttackTrigger);
            gameLogService.append(gameData,
                    GameLog.builder().card(source.getCard()).text("'s ability triggers.").build());
            log.info("Game {} - {} ON_ANY_PLAYER_ATTACKS trigger for attacking player {}",
                    gameData.id, source.getCard().getName(), attackingPlayerId);
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    private UUID findOtherAttackerId(List<Integer> attackerIndices, int sourceIndex,
                                     List<Permanent> battlefield) {
        if (attackerIndices.size() != 2) {
            return null;
        }
        for (int attackerIndex : attackerIndices) {
            if (attackerIndex != sourceIndex) {
                return battlefield.get(attackerIndex).getId();
            }
        }
        return null;
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

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watch.sourceCard(),
                        watch.controllerId(),
                        watch.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(watch.effects()),
                        (UUID) null,
                        attacker.getId());
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
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
                StackEntry se = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        grant.sourceCard(),
                        grant.controllerId(),
                        grant.sourceCard().getName() + "'s delayed trigger",
                        List.of(new GrantKeywordEffect(
                                grant.keywords(), GrantScope.TARGET, null,
                                GrantDuration.END_OF_TURN, null)),
                        attacker.getId(),
                        (UUID) null);
                se.setNonTargeting(true);
                gameData.stack.add(se);
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
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    action.sourceCard(),
                    action.controllerId(),
                    action.sourceCard().getName() + "'s delayed trigger",
                    List.of(new CreateTokensAttackingEffect(nontokenAttackers, action.tokenEffect())));
            entry.setNonTargeting(true);
            gameData.stack.add(entry);
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
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    action.sourceCard(),
                    action.controllerId(),
                    action.sourceCard().getName() + "'s delayed trigger",
                    List.of(new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                            new PermanentIsCreaturePredicate())));
            entry.setNonTargeting(true);
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s delayed ability triggers."));
            log.info("Game {} - {} delayed attack untap trigger fires", gameData.id, action.sourceCard().getName());
        }

        gameData.clearDelayedActions(DelayedAttackUntap.class,
                action -> action.controllerId().equals(attackingPlayerId));
    }

    private void processDelayedVehicleAttackTriggers(GameData gameData, List<Permanent> battlefield,
                                                      List<Integer> attackerIndices) {
        if (attackerIndices.isEmpty() || gameData.combatPhasesThisTurn != 1
                || !gameData.hasDelayedAction(DelayedVehicleAttack.class)) {
            return;
        }

        Set<UUID> attackingPermanentIds = attackerIndices.stream()
                .map(battlefield::get)
                .map(Permanent::getId)
                .collect(Collectors.toSet());
        List<DelayedVehicleAttack> matchingActions = gameData.getDelayedActions(DelayedVehicleAttack.class).stream()
                .filter(action -> attackingPermanentIds.contains(action.vehicleId()))
                .toList();
        if (matchingActions.isEmpty()) return;

        for (DelayedVehicleAttack action : matchingActions) {
            Permanent source = gameQueryService.findPermanentById(gameData, action.sourcePermanentId());
            Permanent vehicle = gameQueryService.findPermanentById(gameData, action.vehicleId());
            GameQueryService.StaticBonus sourceStaticBonus = source == null
                    ? null : gameQueryService.computeStaticBonus(gameData, source);
            if (source == null || vehicle == null
                    || source.isLosesAllAbilitiesUntilEndOfTurn()
                    || sourceStaticBonus.losesAllAbilities()
                    || sourceStaticBonus.losesAllNonManaAbilities()
                    || source.getCard().getEffects(EffectSlot.ON_CREWS_VEHICLE).stream()
                    .noneMatch(RegisterDelayedVehicleAttackEffect.class::isInstance)
                    || !gameQueryService.hasEffectiveSubtype(gameData, vehicle, CardSubtype.VEHICLE)) {
                continue;
            }

            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            if (controllerId == null) continue;
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    action.sourceCard(),
                    controllerId,
                    action.sourceCard().getName() + "'s delayed trigger",
                    List.of(action.effect()),
                    null,
                    action.sourcePermanentId());
            entry.setNonTargeting(true);
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s delayed ability triggers."));
            log.info("Game {} - {} delayed Vehicle attack trigger fires", gameData.id,
                    action.sourceCard().getName());
        }

        gameData.clearDelayedActions(DelayedVehicleAttack.class,
                action -> attackingPermanentIds.contains(action.vehicleId()));
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
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                action.sourceCard(),
                action.controllerId(),
                action.sourceCard().getName() + "'s delayed trigger",
                List.of(new CreateTokensAttackingEffect(
                        action.amount(), action.tokenEffect(), action.sacrificeAtEndStep())));
        entry.setNonTargeting(true);
        gameData.stack.add(entry);
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

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    action.sourceCard(),
                    action.controllerId(),
                    action.sourceCard().getName() + "'s delayed trigger",
                    List.of(new DealDamageToTargetCreatureEffect(new PermanentCount(
                            new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER))),
                    action.targetPermanentId(),
                    action.sourcePermanentId());
            entry.setNonTargeting(true);
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s delayed ability triggers."));
            log.info("Game {} - {} delayed attack damage trigger fires", gameData.id,
                    action.sourceCard().getName());
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
            case DefendingPlayerControlsMoreLandsThanController ignored -> true;
            case DefendingPlayerHasMoreCardsInHandThanController ignored -> true;
            case DefendingPlayerHandAtMost ignored -> true;
            case DefendingPlayerPoisoned ignored -> true;
            case NotCondition not -> isDefendingPlayerCondition(not.inner());
            default -> false;
        };
    }

    private CardEffect resolveAttackedTargetConditions(GameData gameData, Permanent attacker,
                                                       UUID attackerControllerId, CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional
                && isAttackedTargetOpponentCondition(conditional.condition())) {
            ConditionContext context = ConditionContext.forPermanent(attacker, attackerControllerId)
                    .withTargetId(attacker.getAttackTarget());
            return conditionEvaluationService.isMet(gameData, conditional.condition(), context)
                    ? conditional.wrapped() : null;
        }
        if (!(effect instanceof SequenceEffect sequence)) {
            return effect;
        }

        List<CardEffect> resolvedSteps = sequence.steps().stream()
                .map(step -> resolveAttackedTargetConditions(gameData, attacker,
                        attackerControllerId, step))
                .filter(Objects::nonNull)
                .toList();
        if (resolvedSteps.isEmpty()) {
            return null;
        }
        if (resolvedSteps.size() == 1) {
            return resolvedSteps.getFirst();
        }
        if (resolvedSteps.size() == sequence.steps().size()) {
            return effect;
        }
        return sequence.withSteps(resolvedSteps);
    }

    private boolean isAttackedTargetOpponentCondition(Condition condition) {
        return condition instanceof AttackedTargetIsOpponent
                || condition instanceof NotCondition not
                && not.inner() instanceof AttackedTargetIsOpponent;
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

    /**
     * Drops the attacker gates of a compound condition, which were already checked when the attack
     * event occurred, and keeps the remaining conditions to be re-checked as the trigger resolves (CR 603.4).
     */
    private CardEffect residualAfterAttackerGate(ConditionalEffect ce) {
        if (!(ce.condition() instanceof AllConditions all)) return ce.wrapped();
        List<Condition> remaining = all.conditions().stream()
                .filter(c -> !containsHasAttackerCondition(c))
                .toList();
        if (remaining.isEmpty()) return ce.wrapped();
        Condition residual = remaining.size() == 1 ? remaining.getFirst() : new AllConditions(remaining);
        return new ConditionalEffect(residual, ce.wrapped(), ce.interveningIf(), ce.triggerTimeOnly());
    }

    private boolean containsHasAttackerCondition(Condition condition) {
        return switch (condition) {
            case HasAttacker ignored -> true;
            case AllConditions c -> c.conditions().stream().anyMatch(this::containsHasAttackerCondition);
            case AllOf c -> c.conditions().stream().anyMatch(this::containsHasAttackerCondition);
            case AnyOf c -> c.conditions().stream().anyMatch(this::containsHasAttackerCondition);
            case NotCondition c -> containsHasAttackerCondition(c.inner());
            default -> false;
        };
    }

    private int countMatchingAttackerIndices(GameData gameData, List<Permanent> battlefield,
                                              List<Integer> attackerIndices, Permanent source,
                                              UUID controllerId, PermanentPredicate predicate) {
        FilterContext sourceContext = FilterContext.of(gameData)
                .withSourceCardId(source.getOriginalCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(source.getId());
        int count = 0;
        for (int attackerIndex : attackerIndices) {
            if (predicateEvaluationService.matchesPermanentPredicate(
                    battlefield.get(attackerIndex), predicate, sourceContext)) {
                count++;
            }
        }
        return count;
    }

    private List<AttackingPermanentSnapshot> snapshotMatchingAttackerPowers(
            GameData gameData, List<Permanent> battlefield, List<Integer> attackerIndices,
            Permanent source, UUID controllerId, PermanentPredicate predicate) {
        FilterContext sourceContext = FilterContext.of(gameData)
                .withSourceCardId(source.getOriginalCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(source.getId());
        return attackerIndices.stream()
                .map(battlefield::get)
                .filter(attacker -> predicateEvaluationService.matchesPermanentPredicate(
                        attacker, predicate, sourceContext))
                .map(attacker -> new AttackingPermanentSnapshot(
                        attacker.getId(), gameQueryService.getEffectivePower(gameData, attacker)))
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

    /** Whether a graveyard attack ability is a "whenever your commander attacks" trigger (one per attacking commander). */
    private static boolean isOwnedCommanderAttackTrigger(CardEffect effect) {
        return effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof HasAttacker hasAttacker
                && hasAttacker.predicate() instanceof PermanentIsCommanderPredicate commander
                && commander.ownedBySourceController();
    }

    private boolean graveyardAttackConditionsMet(
            GameData gameData, CardEffect effect, ConditionContext context) {
        if (!(effect instanceof ConditionalEffect conditional)) {
            return true;
        }
        return conditionEvaluationService.isMet(gameData, conditional.condition(), context)
                && graveyardAttackConditionsMet(gameData, conditional.wrapped(), context);
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

    /**
     * Returns whether an unbound target group is read positionally by one of the given effects: it belongs to
     * the ability of the next bound group after it (or to every ability when no bound group follows it).
     */
    private boolean bareGroupBelongsToEffects(Card card, int groupIndex, List<CardEffect> effects) {
        for (TargetGroup group : card.getTargetGroups()) {
            if (group.getIndex() > groupIndex && card.bindsEffectToTargetGroup(group.getIndex())) {
                return effects.stream().anyMatch(effect -> card.isEffectBoundToTargetGroup(effect, group.getIndex()));
            }
        }
        return true;
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

    private boolean hasMatchingAttackerForTarget(GameData gameData, List<Permanent> battlefield,
                                                  List<Integer> attackerIndices,
                                                  Map<Integer, UUID> resolvedTargets,
                                                  UUID attackedTargetId, Permanent source) {
        int chosenNumber = source.getChosenNumber();
        return attackerIndices.stream()
                .filter(index -> attackedTargetId.equals(resolvedTargets.get(index)))
                .map(battlefield::get)
                .anyMatch(attacker -> gameQueryService.getEffectivePower(gameData, attacker) == chosenNumber
                        || gameQueryService.getEffectiveToughness(gameData, attacker) == chosenNumber);
    }

    /**
     * An ability granted by an attached permanent ("Whenever equipped creature attacks ...") is
     * controlled by the player who controls the granting permanent, not by the creature's controller
     * (CR 603.3a). Returns that player for a granted effect, or {@code fallbackControllerId} when the
     * effect is not granted by an attachment. {@code usedGrantingPermanents} keeps several equal
     * grants from resolving to the same attachment.
     */
    private UUID grantedAbilityController(GameData gameData, Permanent attacker, CardEffect effect,
                                          UUID fallbackControllerId, Set<UUID> usedGrantingPermanents) {
        for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
            for (Permanent source : battlefield.getValue()) {
                if (!source.isAttached() || !attacker.getId().equals(source.getAttachedTo())
                        || usedGrantingPermanents.contains(source.getId())) {
                    continue;
                }
                boolean grantsEffect = source.getCard().getStaticEffects().stream()
                        .anyMatch(e -> e instanceof GrantTriggeredAbilityEffect grant
                                && grant.slot() == EffectSlot.ON_ATTACK
                                && grant.grantedEffect().equals(effect));
                if (grantsEffect) {
                    usedGrantingPermanents.add(source.getId());
                    return battlefield.getKey();
                }
            }
        }
        return fallbackControllerId;
    }

    /**
     * CR 508.5: "the defending player" stays the player the creature attacked even if an attacked
     * planeswalker or battle leaves before the pending "you may" is answered, so a may-ability that only
     * needs the defending player is queued with that player rather than with the attacked permanent.
     */
    private UUID attackedTargetForPendingMay(GameData gameData, Permanent attacker,
                                             com.github.laxika.magicalvibes.model.effect.MayEffect may) {
        UUID attackedTargetId = attacker.getAttackTarget();
        if (attackedTargetId == null || gameData.playerIds.contains(attackedTargetId)
                || !(may.wrapped() instanceof DealDamageToDefendingPlayerCreaturesEffect)) {
            return attackedTargetId;
        }
        Permanent attackedPermanent = gameQueryService.findPermanentById(gameData, attackedTargetId);
        UUID defendingPlayerId = attackedPermanent != null && gameQueryService.isBattle(gameData, attackedPermanent)
                ? attackedPermanent.getProtectorPlayerId()
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
        return defendingPlayerId != null ? defendingPlayerId : attackedTargetId;
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

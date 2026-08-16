package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.RegisterNextRedInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsAmongControlledEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeSacrificedLandCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeUntappedLandCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardOneManaOfEachColorAmongControlledEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaToChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.AddNotedManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddNotedManaForLastExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.AwardHasteGrantingManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardUncounterableGrantingManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.DoubleManaPoolEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandExcessManaWithColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedPermanentSelfTargetingEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ManaAbilityCardDrawingEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnGrantingEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerGainsControlOfGrantingEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.amount.CountersOnGrantingPermanent;
import com.github.laxika.magicalvibes.model.amount.CountersOnLinkedPermanent;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceToHandAtNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAsCostEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersForManaEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.BounceScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.effect.UnattachSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingManaActivation;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.service.effect.manafx.ManaAbilityEffectHandler;
import com.github.laxika.magicalvibes.service.effect.manafx.ManaAbilityEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.EquipSupport;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivatedAbilityExecutionService {

    private final DamagePreventionService damagePreventionService;
    private final DrawService drawService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final StateBasedActionService stateBasedActionService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final ManaAbilityEffectHandlerRegistry manaAbilityEffectHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final EquipSupport equipSupport;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameMutationCoordinator mutationCoordinator;

    /**
     * Completes an activated ability activation after all additional costs (mana, sacrifice creature,
     * discard, exile from graveyard, etc.) have already been paid by the caller.
     *
     * <p>This method performs the remaining activation steps in order:
     * <ol>
     *   <li>Determines the effective target — auto-targets the source permanent for self-targeting
     *       effects (e.g. {@code BoostSelfEffect}, {@code RegenerateEffect}, {@code AnimatePermanentsEffect}).</li>
     *   <li>Taps the permanent if the ability requires a tap cost.</li>
     *   <li>Snapshots charge counters into {@code effectiveXValue} for counter-dependent effects
     *       (e.g. {@code DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect}) so the
     *       value survives sacrifice.</li>
     *   <li>Executes {@link com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost} if present —
     *       removes the permanent from the battlefield, adds it to the graveyard, and collects death triggers.</li>
     *   <li>Logs the activation and records the canonical player-view invalidation.</li>
     *   <li>Snapshots the effect list — filters out cost effects and bakes runtime values into effects
     *       like {@code CantBlockSourceEffect} (source permanent ID) and
     *       {@code PreventNextColorDamageToControllerEffect} (chosen color).</li>
     *   <li>Detects whether the ability is a mana ability (CR 605.1a: no target, could add mana,
     *       not a loyalty ability) and resolves it immediately without the stack, or pushes a
     *       {@link StackEntry} onto the stack for non-mana abilities.</li>
     * </ol>
     *
     * @param gameData                          the current game state
     * @param player                            the player activating the ability
     * @param permanent                         the permanent whose ability is being activated
     * @param ability                           the activated ability definition
     * @param abilityEffects                    the full effect list including cost effects
     * @param effectiveXValue                   the X value (from user input or 0); may be overridden
     *                                          by charge counter snapshotting
     * @param targetId                 the chosen target permanent, or {@code null} if none
     * @param targetZone                        the zone of the target, or {@code null} for battlefield targets
     * @param markAsNonTargetingForSacCreatureCost if {@code true}, marks the resulting stack entry as
     *                                          non-targeting (used when the target selection was for a
     *                                          sacrifice-creature cost, not the ability's actual target)
     */
    public void completeActivationAfterCosts(GameData gameData,
                                             Player player,
                                             Permanent permanent,
                                             ActivatedAbility ability,
                                             List<CardEffect> abilityEffects,
                                             int effectiveXValue,
                                             UUID targetId,
                                             Zone targetZone,
                                             boolean markAsNonTargetingForSacCreatureCost) {
        completeActivationAfterCosts(gameData, player, permanent, ability, abilityEffects, effectiveXValue, targetId, targetZone, markAsNonTargetingForSacCreatureCost, null, null);
    }

    public void completeActivationAfterCosts(GameData gameData,
                                             Player player,
                                             Permanent permanent,
                                             ActivatedAbility ability,
                                             List<CardEffect> abilityEffects,
                                             int effectiveXValue,
                                             UUID targetId,
                                             Zone targetZone,
                                             boolean markAsNonTargetingForSacCreatureCost,
                                             List<UUID> targetIds) {
        completeActivationAfterCosts(gameData, player, permanent, ability, abilityEffects, effectiveXValue, targetId, targetZone, markAsNonTargetingForSacCreatureCost, targetIds, null);
    }

    public void completeActivationAfterCosts(GameData gameData,
                                             Player player,
                                             Permanent permanent,
                                             ActivatedAbility ability,
                                             List<CardEffect> abilityEffects,
                                             int effectiveXValue,
                                             UUID targetId,
                                             Zone targetZone,
                                             boolean markAsNonTargetingForSacCreatureCost,
                                             List<UUID> targetIds,
                                             Map<UUID, Integer> damageAssignments) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            throw new IllegalStateException("Invalid battlefield");
        }

        UUID effectiveTargetId = targetId;
        if (effectiveTargetId == null && (targetIds == null || targetIds.isEmpty()) && targetZone == null) {
            boolean capturesAttachedPermanent = (permanent.getCard().isAura()
                    || permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT))
                    && abilityEffects.stream()
                    .anyMatch(e -> (e instanceof AttachedPermanentSelfTargetingEffect && e.targetSpec().selfTargeting())
                            || (e instanceof RegenerateEffect && e.targetSpec().selfTargeting())
                            || e.resolvesAgainstAttachedPermanent());
            if (capturesAttachedPermanent) {
                // "Sacrifice this Aura: Regenerate enchanted creature." / "{2}: Regenerate equipped
                // creature." / "Sacrifice this Aura: Return enchanted creature to its owner's hand."
                // Capture the attached creature now, before a sacrifice cost removes the
                // attachment and its attachedTo link.
                // "Sacrifice this Aura: Regenerate enchanted creature." / "{2}: Regenerate equipped
                // creature." / "Return this Aura to its owner's hand: Put two +1/+1 counters on
                // enchanted creature." Capture the attached creature now, before a sacrifice or
                // bounce cost removes the attachment and its attachedTo link.
                effectiveTargetId = permanent.getAttachedTo();
            } else if (abilityEffects.stream().anyMatch(e -> e.targetSpec().selfTargeting())) {
                effectiveTargetId = permanent.getId();
            }
        }

        // Collect "enchanted permanent becomes tapped" triggers (e.g. Relic Putrescence).
        // We fire the check now (before a possible sacrifice cost removes the aura from the
        // battlefield) but defer adding the entries to the stack so they end up ON TOP of the
        // activated ability — per CR rules the trigger resolves first for non-mana abilities.
        List<StackEntry> deferredTapTriggers = List.of();
        if (ability.isRequiresTap()) {
            permanent.tap();
            int stackBefore = gameData.stack.size();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
            deferredTapTriggers = new ArrayList<>(gameData.stack.subList(stackBefore, gameData.stack.size()));
            gameData.stack.subList(stackBefore, gameData.stack.size()).clear();
        } else if (ability.isRequiresUntap()) {
            // {Q} untap symbol: paying the cost untaps the source permanent.
            permanent.untap();
        }

        // "Remove all {type} counters from this permanent" is an additional cost — snapshot the
        // number removed into xValue before clearing them so a companion effect scales correctly.
        Optional<RemoveAllCountersAsCostEffect> removeAllCounters = abilityEffects.stream()
                .filter(RemoveAllCountersAsCostEffect.class::isInstance)
                .map(RemoveAllCountersAsCostEffect.class::cast)
                .findFirst();
        if (removeAllCounters.isPresent()) {
            CounterType counterType = removeAllCounters.get().counterType();
            // Hankyu keeps its aim counters on the Equipment while the equipped creature activates
            // the ability, so the cost is paid from the granting Equipment.
            Permanent counterSource = permanent;
            if (removeAllCounters.get().fromGrantingEquipment() && ability.getGrantSourcePermanentId() != null) {
                counterSource = gameQueryService.findPermanentById(gameData, ability.getGrantSourcePermanentId());
            }
            if (counterSource != null) {
                effectiveXValue = counterSource.getCounterCount(counterType);
                counterSource.setCounterCount(counterType, 0);
            }
        }

        // Snapshot charge counters before sacrifice so the value survives in the stack entry's xValue
        else if (abilityEffects.stream().anyMatch(e -> e instanceof DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect)) {
            effectiveXValue = permanent.getCounterCount(CounterType.CHARGE);
        }

        // Per CR 602.2a the ability goes on the stack during announcement, then CR 601.2h
        // costs (including sacrifice) are paid.  CR 603.3 says death triggers from the
        // sacrifice wait until a player would receive priority and then go on top.
        // We defer any stack entries added by sacrifice/exile so they end up ON TOP of
        // the activated ability — matching the same pattern used for tap triggers above.
        int stackBeforeCosts = gameData.stack.size();

        // Pay life here, after the activation has established its cost-trigger boundary. The life
        // loss trigger must be deferred until the activated ability is on the stack, so it resolves
        // above the ability rather than below it.
        abilityEffects.stream()
                .filter(PayLifeCost.class::isInstance)
                .map(PayLifeCost.class::cast)
                .findFirst()
                .ifPresent(cost -> {
                    int currentLife = gameData.getLife(playerId);
                    int sourceCounterCount = cost.perSourceCounter() == null
                            ? 0
                            : permanent.getCounterCount(cost.perSourceCounter());
                    int amount = cost.effectiveAmount(currentLife, sourceCounterCount);
                    if (amount > 0) {
                        lifeSupport.applyLifeLoss(gameData, playerId, amount, permanent.getCard().getName());
                    }
                });

        abilityEffects.stream()
                .filter(PayEnergyCost.class::isInstance)
                .map(PayEnergyCost.class::cast)
                .findFirst()
                .ifPresent(cost -> {
                    int current = gameData.playerEnergyCounters.getOrDefault(playerId, 0);
                    if (current < cost.amount()) {
                        throw new IllegalStateException("Not enough energy to pay");
                    }
                    int updated = current - cost.amount();
                    gameData.playerEnergyCounters.put(playerId, updated);
                    String playerName = gameData.playerIdToName.getOrDefault(playerId, "Player");
                    gameLogService.append(gameData,
                            GameLog.text(playerName + " pays " + cost.amount() + " energy counter(s)."));
                });

        boolean shouldExileSelf = abilityEffects.stream().anyMatch(e -> e instanceof ExileSelfCost);
        if (shouldExileSelf) {
            permanentRemovalService.removePermanentToExile(gameData, permanent);
        }

        // "Return this permanent to its owner's hand: …" (Cycle of Life). Paid before the ability
        // goes on the stack; the ability resolves normally from the stack afterwards.
        boolean shouldReturnSelfToHand = abilityEffects.stream().anyMatch(e -> e instanceof ReturnSelfToHandCost);
        if (shouldReturnSelfToHand) {
            permanentRemovalService.removePermanentToHand(gameData, permanent);
        }

        Optional<SacrificeSelfCost> sacrificeSelfCost = abilityEffects.stream()
                .filter(SacrificeSelfCost.class::isInstance)
                .map(SacrificeSelfCost.class::cast)
                .findFirst();
        if (sacrificeSelfCost.isPresent()) {
            // Snapshot power before leaving the battlefield (CR 608.2h last-known information).
            if (sacrificeSelfCost.get().trackPower()) {
                effectiveXValue = Math.max(0, gameQueryService.getEffectivePower(gameData, permanent));
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, permanent);
            triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, player.getId(), permanent.getCard());
        }

        // Sacrifice the source equipment (e.g. Blazing Torch's "{T}, Sacrifice Blazing Torch: ...")
        // Snapshot the equipment's card before sacrifice so it can be used as the damage source
        // per MTG rulings: "The source of the damage is Blazing Torch, not the equipped creature."
        Card sacrificedEquipmentCard = null;
        boolean shouldSacrificeEquipment = abilityEffects.stream().anyMatch(SacrificeSourceEquipmentCost.class::isInstance);
        if (shouldSacrificeEquipment && ability.getGrantSourcePermanentId() != null) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, ability.getGrantSourcePermanentId());
            if (equipment != null) {
                sacrificedEquipmentCard = equipment.getCard();
                permanentRemovalService.removePermanentToGraveyard(gameData, equipment);
            }
        }

        boolean shouldTapGrantingEquipment = abilityEffects.stream().anyMatch(effect -> effect instanceof CostEffect cost
                && cost.tapsGrantingEquipment());
        if (shouldTapGrantingEquipment && ability.getGrantSourcePermanentId() != null) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, ability.getGrantSourcePermanentId());
            if (equipment == null) {
                throw new IllegalStateException("The granting Equipment is not on the battlefield");
            }
            if (equipment.isTapped()) {
                throw new IllegalStateException("The granting Equipment is already tapped");
            }
            equipment.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, equipment);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " taps ", equipment.getCard(), " as a cost."));
        }

        Card unattachedEquipmentCard = null;
        boolean shouldUnattachEquipment = abilityEffects.stream().anyMatch(UnattachSourceEquipmentCost.class::isInstance);
        if (shouldUnattachEquipment) {
            Permanent equipment = ability.getGrantSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, ability.getGrantSourcePermanentId());
            if (equipment == null || !equipment.isAttached()) {
                throw new IllegalStateException("The granting Equipment is not attached");
            }
            unattachedEquipmentCard = equipment.getCard();
            equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, equipment.getAttachedTo(), null);
            equipment.setAttachedTo(null);
            gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        }

        List<StackEntry> deferredCostTriggers = new ArrayList<>();
        if (!gameData.pendingActivatedAbilityCostTriggers.isEmpty()) {
            deferredCostTriggers.addAll(gameData.pendingActivatedAbilityCostTriggers);
            gameData.pendingActivatedAbilityCostTriggers.clear();
        }
        if (gameData.stack.size() > stackBeforeCosts) {
            deferredCostTriggers.addAll(gameData.stack.subList(stackBeforeCosts, gameData.stack.size()));
            gameData.stack.subList(stackBeforeCosts, gameData.stack.size()).clear();
        }

        // "Whenever you activate an ability of ..." triggers (e.g. Ceaseless Searblades). Collected
        // here so they end up ON TOP of the activated ability (non-mana), or deferred to the next
        // priority window alongside cost triggers (mana abilities, per CR 603.3).
        int stackBeforeActivationTriggers = gameData.stack.size();
        triggerCollectionService.checkControllerActivatesAbilityTriggers(gameData, playerId, permanent);
        List<StackEntry> deferredActivationTriggers = List.of();
        if (gameData.stack.size() > stackBeforeActivationTriggers) {
            deferredActivationTriggers = new ArrayList<>(gameData.stack.subList(stackBeforeActivationTriggers, gameData.stack.size()));
            gameData.stack.subList(stackBeforeActivationTriggers, gameData.stack.size()).clear();
        }

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " activates " , permanent.getCard(), "'s ability."));
        log.info("Game {} - {} activates {}'s ability", gameData.id, player.getUsername(), permanent.getCard().getName());

        List<CardEffect> snapshotEffects = snapshotEffects(gameData, abilityEffects, permanent, ability);
        // CR 605.1a: A mana ability doesn't require a target, could add mana, isn't a loyalty ability,
        // and its cost and effect don't move cards to or from a library.
        // Pain lands (e.g. Adarkar Wastes) include a DealDamageToPlayersEffect(CONTROLLER) alongside mana production
        // and are still mana abilities — they resolve immediately without using the stack.
        boolean isManaAbility = AbilityActivationService.isManaAbility(ability, abilityEffects);

        if (isManaAbility) {
            // A "pure" mana activation (tap-only cost, only fixed-shape mana output) can be undone
            // by the MTGO-style cancel-casting UI: snapshot the pool around resolution so the exact
            // mana added (incl. Damping Sphere replacement) is recorded. AwardAnyColorManaEffect
            // qualifies even though it stops to ask for a colour — its output is still just N mana
            // of one colour — but it produces that mana only once the answer arrives, so it parks
            // the snapshot below instead of recording here. A spend-restricted any-color ability
            // does NOT qualify: its mana lands in a bucket the pool snapshot doesn't cover.
            boolean revertable = ability.isRequiresTap()
                    && ability.getManaCost() == null
                    && abilityEffects.stream().noneMatch(e -> e instanceof CostEffect)
                    && snapshotEffects.stream().allMatch(e -> e instanceof AwardManaEffect
                            || (e instanceof AwardAnyColorManaEffect anyColor
                                    && anyColor.restriction() == ManaSpendRestriction.NONE)
                            || manaAbilityEffectHandlerRegistry.isRevertable(e));
            ManaPool pool = gameData.playerManaPools.get(playerId);
            java.util.EnumMap<ManaColor, Integer> poolBefore =
                    revertable ? AbilityActivationService.snapshotPoolColors(pool) : null;
            java.util.EnumMap<ManaColor, Integer> creatureManaBefore =
                    revertable ? AbilityActivationService.snapshotCreatureManaColors(pool) : null;
            int pendingTriggersBefore = gameData.pendingManaAbilityTriggers.size();

            resolveManaAbility(gameData, playerId, player, permanent, snapshotEffects, effectiveXValue);
            if (ability.isRequiresTap() && gameQueryService.isCreature(gameData, permanent)) {
                triggerCollectionService.checkCreatureTapForManaTriggers(gameData, playerId, permanent.getId());
            }
            // A land whose mana ability is written as an ActivatedAbility (Forbidden Orchard,
            // Undiscovered Paradise, Cavern of Souls) is still "tapped for mana", so the land-tap
            // watchers must see it exactly as they see a printed ON_TAP land.
            if (ability.isRequiresTap() && permanent.getCard().hasType(CardType.LAND)) {
                int stackBeforeLandTapTriggers = gameData.stack.size();
                triggerCollectionService.checkLandTapTriggers(gameData, playerId, permanent.getId());
                if (gameData.stack.size() > stackBeforeLandTapTriggers) {
                    List<StackEntry> deferredLandTapTriggers = new ArrayList<>(
                            gameData.stack.subList(stackBeforeLandTapTriggers, gameData.stack.size()));
                    gameData.stack.subList(stackBeforeLandTapTriggers, gameData.stack.size()).clear();
                    gameData.pendingManaAbilityTriggers.addAll(deferredLandTapTriggers);
                }
            }
            // CR 603.3: Triggered abilities from mana-ability costs (sacrifice, tap)
            // wait until the next time a player would receive priority before going
            // on the stack.  This prevents them from blocking sorcery-speed spell
            // casting when a mana ability is activated to pay for a spell.
            if (!deferredTapTriggers.isEmpty() || !deferredCostTriggers.isEmpty() || !deferredActivationTriggers.isEmpty()) {
                gameData.pendingManaAbilityTriggers.addAll(deferredTapTriggers);
                gameData.pendingManaAbilityTriggers.addAll(deferredCostTriggers);
                gameData.pendingManaAbilityTriggers.addAll(deferredActivationTriggers);
            }
            if (revertable) {
                List<StackEntry> deferred = new ArrayList<>(gameData.pendingManaAbilityTriggers.subList(
                        pendingTriggersBefore, gameData.pendingManaAbilityTriggers.size()));
                if (AbilityActivationService.isAwaitingOwnManaColorChoice(gameData, playerId)) {
                    // The mana has not been produced yet — park the snapshot for the colour-choice
                    // answer to complete (ChoiceHandlerService.completeParkedManaActivation).
                    gameData.pendingRevertableManaActivation = new PendingManaActivation(
                            playerId, permanent.getId(), poolBefore, creatureManaBefore, List.copyOf(deferred));
                } else {
                    AbilityActivationService.recordRevertableManaActivation(
                            gameData, playerId, permanent, poolBefore, creatureManaBefore, deferred);
                }
            } else {
                // A mana ability with side effects (pain-land damage, pool doubling, extra costs)
                // can't be undone — and undoing earlier activations after it could interact with
                // its result (e.g. doubled mana), so bar the whole window.
                gameData.revertableManaActivations.clear();
            }
            return;
        }

        int abilityStackIndex = gameData.stack.size();
        pushAbilityOnStack(gameData, playerId, permanent, ability, snapshotEffects, effectiveXValue, effectiveTargetId, targetZone, targetIds, damageAssignments);
        if (markAsNonTargetingForSacCreatureCost && !gameData.stack.isEmpty()) {
            gameData.stack.getLast().setNonTargeting(true);
        }
        // Set the damage source card for equipment-granted abilities (e.g. Blazing Torch)
        // Per MTG rulings: "The source of the damage is Blazing Torch, not the equipped creature."
        if (sacrificedEquipmentCard != null && !gameData.stack.isEmpty()) {
            gameData.stack.getLast().setDamageSourceCard(sacrificedEquipmentCard);
        } else if (unattachedEquipmentCard != null && !gameData.stack.isEmpty()) {
            gameData.stack.getLast().setDamageSourceCard(unattachedEquipmentCard);
        }
        // Rings of Brighthearth: "whenever you activate an ability, if it isn't a mana ability, you
        // may pay {2} to copy it." Collected after the ability is on the stack so it can be snapshotted.
        StackEntry abilityEntry = abilityStackIndex < gameData.stack.size() ? gameData.stack.get(abilityStackIndex) : null;
        triggerCollectionService.checkControllerActivatesNonManaAbilityTriggers(gameData, playerId, abilityEntry, ability);
        // "Whenever an opponent activates a non-mana ability" triggers (Harsh Mentor). Reached only on
        // the non-mana path, so the "if it isn't a mana ability" clause is satisfied automatically.
        triggerCollectionService.checkOpponentActivatesNonManaAbilityTriggers(gameData, playerId, permanent);
        // Add "whenever you activate an ability" triggers ON TOP so they resolve first (per CR rules)
        gameData.stack.addAll(deferredActivationTriggers);
        // Add "becomes tapped" triggers ON TOP of the ability so they resolve first (per CR rules)
        gameData.stack.addAll(deferredTapTriggers);
        // Add death triggers from sacrifice/exile ON TOP so they resolve first (per CR 603.3)
        gameData.stack.addAll(deferredCostTriggers);
        // Flush any deferred mana-ability triggers (e.g. from mana abilities activated
        // earlier to pay for this ability) — they go on top per CR 603.3.
        if (!gameData.pendingManaAbilityTriggers.isEmpty()) {
            gameData.stack.addAll(gameData.pendingManaAbilityTriggers);
            gameData.pendingManaAbilityTriggers.clear();
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    private List<CardEffect> snapshotEffects(GameData gameData, List<CardEffect> abilityEffects,
                                             Permanent permanent, ActivatedAbility ability) {
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect printedEffect : abilityEffects) {
            CardEffect effect = TextChangeTransformer.transform(printedEffect,
                    permanent.getTextReplacements(), TextChangeTransformer.globalColorWordReplacements(gameData));
            if (effect instanceof CostEffect) {
                continue;
            }
            if (effect instanceof PutCountersOnGrantingEquipmentEffect grantCounters) {
                // Bind the granting Equipment now (Hankyu): at resolution the ability's source is the
                // equipped creature, and the Equipment may no longer be attached to it.
                snapshotEffects.add(new PutCountersOnGrantingEquipmentEffect(grantCounters.counterType(),
                        grantCounters.count(), ability.getGrantSourcePermanentId()));
            } else if (effect instanceof TargetPermanentControllerGainsControlOfGrantingEquipmentEffect gainControl) {
                boolean sourceHadExcludedSubtype = gainControl.excludedSourceSubtype() != null
                        && GameQueryService.permanentHasSubtype(permanent, gainControl.excludedSourceSubtype());
                snapshotEffects.add(new TargetPermanentControllerGainsControlOfGrantingEquipmentEffect(
                        gainControl.duration(), gainControl.excludedSourceSubtype(),
                        ability.getGrantSourcePermanentId(), sourceHadExcludedSubtype));
            } else if (effect instanceof CantBlockSourceEffect) {
                snapshotEffects.add(new CantBlockSourceEffect(permanent.getId()));
            } else if (effect instanceof MustBlockSourceEffect) {
                snapshotEffects.add(new MustBlockSourceEffect(permanent.getId()));
            } else if (effect instanceof PreventNextColorDamageToControllerEffect && permanent.getChosenColor() != null) {
                snapshotEffects.add(new PreventNextColorDamageToControllerEffect(permanent.getChosenColor()));
            } else if (effect instanceof AwardChosenColorManaEffect && permanent.getChosenColor() != null) {
                snapshotEffects.add(new AwardManaEffect(ManaColor.valueOf(permanent.getChosenColor().name())));
            } else if (effect instanceof ReturnToHandEffect bounce && bounce.scope() == BounceScope.ENCHANTED
                    && permanent.getAttachedTo() != null) {
                // Bind the enchanted permanent now: the Aura may be gone by resolution (Phantom Wings
                // sacrifices itself as a cost), and the ability still uses last known information.
                snapshotEffects.add(ReturnToHandEffect.enchantedSnapshot(permanent.getAttachedTo()));
            } else if (effect instanceof ReturnToHandEffect bounce
                    && bounce.scope() == BounceScope.GRANTING_EQUIPMENT) {
                // Bind the granting Equipment before the ability resolves: the equipped creature is
                // the ability's source, but the Equipment is the permanent that returns to hand.
                snapshotEffects.add(ReturnToHandEffect.grantingEquipmentSnapshot(
                        ability.getGrantSourcePermanentId()));
            } else if (effect instanceof ExileEnchantedCreatureEffect && permanent.getAttachedTo() != null) {
                snapshotEffects.add(new ExileEnchantedCreatureEffect(permanent.getAttachedTo()));
            } else if (effect instanceof GrantKeywordToChosenCreatureUntilEndOfTurnEffect gk) {
                snapshotEffects.add(new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(gk.keyword(), permanent.getChosenPermanentId()));
            } else if (effect instanceof DealDamageToAnyTargetEffect dd
                    && dd.damage() instanceof CountersOnGrantingPermanent counters) {
                // Archery Training: the granted "{T}: … deals X damage … where X is the number of
                // arrow counters on Archery Training" — the counters live on the granting Aura, not
                // on the creature activating the ability, so bind the granting permanent at activation.
                snapshotEffects.add(new DealDamageToAnyTargetEffect(
                        new CountersOnLinkedPermanent(counters.counterType(), ability.getGrantSourcePermanentId()),
                        dd.cantRegenerate(), dd.exileInsteadOfDie(), dd.targetGroup(), dd.unpreventableWhen()));
            } else if (effect instanceof DealDamageToTargetCreatureEffect dc
                    && dc.damage() instanceof CountersOnGrantingPermanent counters) {
                snapshotEffects.add(new DealDamageToTargetCreatureEffect(
                        new CountersOnLinkedPermanent(counters.counterType(), ability.getGrantSourcePermanentId()),
                        dc.unpreventable()));
            } else {
                snapshotEffects.add(effect);
            }
        }
        return snapshotEffects;
    }

    private void resolveManaAbility(GameData gameData, UUID playerId, Player player, Permanent permanent, List<CardEffect> snapshotEffects, int xValue) {
        // CR 603.2 / 603.3: triggers fired from effects resolving inside a mana ability
        // (e.g. Pristine Talisman's life-gain triggering Sanguine Bond) must queue and
        // wait for the next priority window, not land on the stack immediately.
        gameData.manaAbilityResolutionDepth++;
        try {
            doResolveManaAbility(gameData, playerId, player, permanent, snapshotEffects, xValue);
        } finally {
            gameData.manaAbilityResolutionDepth--;
        }
    }

    private void doResolveManaAbility(GameData gameData, UUID playerId, Player player, Permanent permanent, List<CardEffect> snapshotEffects, int xValue) {
        boolean isCreatureSource = gameQueryService.isCreature(gameData, permanent);

        // Mana Reflection: tapping a permanent for mana produces twice as much of that mana (2^count).
        int manaMultiplier = gameQueryService.manaProductionMultiplier(gameData, playerId);

        boolean chosenLandManaReplacement = permanent.getCard().hasType(CardType.LAND)
                && gameData.playersWithLandManaChoiceReplacementThisTurn.contains(playerId);
        if (chosenLandManaReplacement) {
            ChoiceContext.ManaColorChoice choiceContext =
                    new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, manaMultiplier);
            List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    playerId, null, null, choiceContext, colors,
                    "Choose a color of mana to add (Harvest Mage)."));
        }

        // Damping Sphere replacement: if a land is tapped for two or more mana, it produces {C} instead.
        boolean dampingReplacement = false;
        if (!chosenLandManaReplacement && permanent.getCard().hasType(CardType.LAND)
                && isDampingManaReplacementActive(gameData)) {
            int totalMana = calculateTotalManaProduction(gameData, playerId, permanent, snapshotEffects, xValue);
            if (totalMana >= 2) {
                dampingReplacement = true;
                gameData.playerManaPools.get(playerId).add(ManaColor.COLORLESS, 1);
                
                gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " adds {C} from ").card(permanent.getCard()).text(" (Damping Sphere replaces " + totalMana + " mana).").build());
            }
        }

        // Reality Twist / Infernal Darkness: lands produce remapped or fixed colors instead.
        boolean twistReplacement = false;
        ManaColor fixedLandColor = null;
        if (!chosenLandManaReplacement && !dampingReplacement && permanent.getCard().hasType(CardType.LAND)) {
            fixedLandColor = gameQueryService.fixedLandManaColor(gameData, permanent);
            if (fixedLandColor != null) {
                int totalMana = calculateTotalManaProduction(gameData, playerId, permanent, snapshotEffects, xValue)
                        * manaMultiplier;
                if (totalMana > 0) {
                    twistReplacement = true;
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    pool.add(fixedLandColor, totalMana);
                    if (isCreatureSource) {
                        pool.addCreatureMana(fixedLandColor, totalMana);
                    }
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " adds " + totalMana + " " + fixedLandColor.getCode()
                                    + " from ").card(permanent.getCard())
                            .text(" (land mana type replaced).").build());
                }
            } else {
                Set<ManaColor> twistedColors = gameQueryService.twistedLandManaColors(gameData, permanent);
                if (!twistedColors.isEmpty()) {
                    int totalMana = calculateTotalManaProduction(gameData, playerId, permanent, snapshotEffects, xValue)
                            * manaMultiplier;
                    if (totalMana > 0) {
                        twistReplacement = true;
                        if (twistedColors.size() == 1) {
                            ManaColor color = twistedColors.iterator().next();
                            ManaPool pool = gameData.playerManaPools.get(playerId);
                            pool.add(color, totalMana);
                            if (isCreatureSource) {
                                pool.addCreatureMana(color, totalMana);
                            }
                            gameLogService.append(gameData, GameLog.builder()
                                    .text(player.getUsername() + " adds " + totalMana + " " + color.getCode()
                                            + " from ").card(permanent.getCard())
                                    .text(" (Reality Twist).").build());
                        } else {
                            ChoiceContext.ManaColorChoice choiceContext =
                                    new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, totalMana);
                            List<String> colors = twistedColors.stream().map(Enum::name).toList();
                            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                                    playerId, null, null, choiceContext, colors,
                                    "Choose a color of mana to add (Reality Twist)."));
                            log.info("Game {} - Awaiting {} to choose Reality Twist mana color",
                                    gameData.id, player.getUsername());
                        }
                    }
                } else if (gameQueryService.basicLandManaProducesAnyColor(gameData, permanent)) {
                    int totalMana = calculateTotalManaProduction(gameData, playerId, permanent,
                            snapshotEffects, xValue) * manaMultiplier;
                    if (totalMana > 0) {
                        twistReplacement = true;
                        ChoiceContext.ManaColorChoice choiceContext =
                                new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, totalMana);
                        List<String> colors = ManaColor.COLORS.stream().map(Enum::name).toList();
                        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                                playerId, null, null, choiceContext, colors,
                                "Choose a color of mana to add."));
                    }
                }
            }
        }

        // ConditionalEffect riders ("If you control a Nissa planeswalker, you gain 2 life") are
        // evaluated here and unwrapped to their inner effect when the condition is met; unmet
        // conditions drop the rider. Supported wrapped riders are the same fixed set below.
        List<CardEffect> effectsToResolve = new ArrayList<>(snapshotEffects.size());
        for (CardEffect effect : snapshotEffects) {
            if (effect instanceof ConditionalEffect conditional) {
                if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(permanent, playerId))) {
                    effectsToResolve.add(conditional.wrapped());
                }
            } else {
                effectsToResolve.add(effect);
            }
        }

        for (int effectIndex = 0; effectIndex < effectsToResolve.size(); effectIndex++) {
            CardEffect effect = effectsToResolve.get(effectIndex);
            if ((chosenLandManaReplacement || dampingReplacement || twistReplacement)
                    && effect instanceof ManaProducingEffect) {
                continue;
            }
            ManaAbilityEffectHandler manaAbilityEffectHandler = manaAbilityEffectHandlerRegistry.getHandler(effect);
            if (manaAbilityEffectHandler != null) {
                manaAbilityEffectHandler.resolve(gameData, playerId, player, permanent,
                        manaMultiplier, isCreatureSource);
                continue;
            }
            if (effect instanceof AwardManaEffect award) {
                int amount = amountEvaluationService.evaluate(gameData, award.amount(),
                        AmountContext.forManaAbility(permanent, playerId, xValue)) * manaMultiplier;
                if (amount > 0) {
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    pool.add(award.color(), amount);
                    if (isCreatureSource) {
                        pool.addCreatureMana(award.color(), amount);
                    }
                    // Dynamic amounts (per-permanent counts, charge counters, source power) log the
                    // realized quantity for clarity; a flat "Add {G}" is covered by the activation log.
                    if (!(award.amount() instanceof com.github.laxika.magicalvibes.model.amount.Fixed)) {
                        
                        gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " adds " + amount + " " + award.color().getCode() + " from ").card(permanent.getCard()).text(".").build());
                    }
                }
            } else if (effect instanceof RevealAnyNumberOfCardsFromHandEffect reveal) {
                List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
                List<UUID> validCardIds = hand.stream()
                        .filter(card -> predicateEvaluationService.matchesCardPredicate(
                                card, reveal.filter(), permanent.getCard().getId(), gameData, playerId))
                        .map(Card::getId)
                        .toList();
                if (!validCardIds.isEmpty()
                        && effectIndex + 1 < effectsToResolve.size()
                        && effectsToResolve.get(effectIndex + 1) instanceof AwardManaEffect award) {
                    interactionHandlerRegistry.begin(gameData,
                            new PendingInteraction.RevealAnyNumberOfCardsFromHandChoice(
                                    playerId, validCardIds, permanent.getCard().getName(),
                                    new PendingInteraction.ManaAbilityRevealContext(
                                            permanent.getId(), award.color(), award.amount(), xValue,
                                            manaMultiplier, isCreatureSource)));
                    log.info("Game {} - Awaiting {} to choose cards to reveal for {}",
                            gameData.id, player.getUsername(), permanent.getCard().getName());
                    break;
                }
            } else if (effect instanceof RemoveCountersForManaEffect rc) {
                // Storage land: "Remove any number of [type] counters: Add [color] for each removed."
                // Prompt the controller for how many counters (0..present) to remove; the resume
                // handler removes them and adds that much mana. With no counters there is nothing to
                // choose, so the activation just taps the land and produces no mana.
                int available = permanent.getCounterCount(rc.counterType());
                if (available > 0) {
                    ChoiceContext.RemoveCountersForManaChoice choiceContext =
                            new ChoiceContext.RemoveCountersForManaChoice(playerId, permanent.getId(),
                                    rc.color(), rc.counterType(), isCreatureSource, manaMultiplier);
                    List<String> options = java.util.stream.IntStream.rangeClosed(0, available)
                            .mapToObj(Integer::toString)
                            .toList();
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            playerId, null, null, choiceContext, options,
                            "Choose how many " + rc.counterType().name().toLowerCase()
                                    + " counters to remove."));
                    log.info("Game {} - Awaiting {} to choose how many {} counters to remove (0-{})",
                            gameData.id, player.getUsername(), rc.counterType(), available);
                }
            } else if (effect instanceof AwardManaToChosenPlayerEffect chosen) {
                // "Choose a player. That player adds mana." Not targeting (CR 605.1a); the recipient
                // is picked via an inline player choice and the mana is routed into their pool.
                List<UUID> validPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
                PermanentChoiceContext.ManaAbilityAddToChosenPlayer context =
                        new PermanentChoiceContext.ManaAbilityAddToChosenPlayer(
                                chosen.color(), chosen.amount() * manaMultiplier, isCreatureSource,
                                permanent.getCard().getName());
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentChoice(
                        playerId, List.of(), validPlayerIds, context, "Choose a player to add mana."));
                log.info("Game {} - Awaiting {} to choose a player to receive mana", gameData.id, player.getUsername());
            } else if (effect instanceof DoubleManaPoolEffect) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                for (ManaColor color : ManaColor.values()) {
                    int current = pool.get(color);
                    for (int i = 0; i < current; i++) {
                        pool.add(color);
                    }
                }
            } else if (effect instanceof RegisterNextRedInstantSorceryCopyEffect) {
                // Mana-linked rider (Pyromancer's Goggles): the next red instant/sorcery cast
                // before this mana drains is copied.
                gameData.pendingNextRedInstantSorceryCopyCount.merge(playerId, 1, Integer::sum);
                log.info("Game {} - {} registered a red instant/sorcery copy trigger", gameData.id, player.getUsername());
            } else if (effect instanceof AwardAnyColorManaEffect anyColor) {
                int picks = amountEvaluationService.evaluate(gameData, anyColor.amount(),
                        AmountContext.forManaAbility(permanent, playerId, xValue)) * manaMultiplier;
                boolean prompted = AnyColorManaChoiceSupport.beginColorChoice(interactionHandlerRegistry, gameData,
                        playerId, anyColor, picks, isCreatureSource, permanent.getChosenSubtype(), permanent.getCard(),
                        permanent.getId());
                if (prompted) {
                    log.info("Game {} - Awaiting {} to choose a mana color ({}, amount={})",
                            gameData.id, player.getUsername(), anyColor.restriction(), picks);
                }
            } else if (effect instanceof AwardManaOfColorsEffect ofColors) {
                int picks = amountEvaluationService.evaluate(gameData, ofColors.amount(),
                        AmountContext.forManaAbility(permanent, playerId, xValue)) * manaMultiplier;
                if (picks <= 0) {
                    // no-op
                } else if (ofColors.colors().size() == 1) {
                    ManaColor manaColor = ofColors.colors().get(0);
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    pool.add(manaColor, picks);
                    if (isCreatureSource) {
                        pool.addCreatureMana(manaColor, picks);
                    }
                } else {
                    // Each of the `picks` mana is chosen individually from the fixed color list; the
                    // color-choice handler re-prompts per pick (filter lands: "{R}{R}, {R}{G}, or {G}{G}").
                    ChoiceContext.ManaColorChoice choiceContext = ChoiceContext.ManaColorChoice
                            .fixedColorCombination(playerId, isCreatureSource, picks, ofColors.colors());
                    List<String> colors = ofColors.colors().stream().map(Enum::name).toList();
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            playerId, null, null, choiceContext, colors, "Choose a color of mana to add."));
                    log.info("Game {} - Awaiting {} to choose a mana color from a fixed set", gameData.id, player.getUsername());
                }
            } else if (effect instanceof AwardRestrictedManaEffect arm) {
                arm.applyTo(gameData.playerManaPools.get(playerId));
            } else if (effect instanceof AwardHasteGrantingManaEffect ahg) {
                ahg.applyTo(gameData.playerManaPools.get(playerId));
            } else if (effect instanceof AwardUncounterableGrantingManaEffect aug) {
                aug.applyTo(gameData.playerManaPools.get(playerId));
            } else if (effect instanceof AddNotedManaForLastExiledCardEffect) {
                addNotedManaForLastExiledCard(gameData, player, permanent);
            } else if (effect instanceof AddNotedManaEffect) {
                addNotedMana(gameData, player, permanent, manaMultiplier);
            } else if (effect instanceof AwardManaOfColorsAmongControlledEffect manaAmong) {
                Set<CardColor> availableColors = collectColorsAmongControlled(gameData, playerId, manaAmong);
                if (availableColors.size() == 1) {
                    CardColor onlyColor = availableColors.iterator().next();
                    ManaColor manaColor = ManaColor.valueOf(onlyColor.name());
                    gameData.playerManaPools.get(playerId).add(manaColor);
                    
                    gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " adds {" + onlyColor.getCode() + "} from " , permanent.getCard(), "."));
                } else if (availableColors.size() > 1) {
                    ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource);
                    List<String> colors = availableColors.stream()
                            .map(Enum::name)
                            .sorted()
                            .toList();
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            playerId, null, null, choiceContext, colors, "Choose a color of mana to add."));
                    log.info("Game {} - Awaiting {} to choose a mana color from legendary colors", gameData.id, player.getUsername());
                } else {
                    
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " activates ")
                            .card(permanent.getCard())
                            .text(" but produces no mana (no colors among legendary creatures and planeswalkers).")
                            .build());
                }
            } else if (effect instanceof AwardOneManaOfEachColorAmongControlledEffect eachColor) {
                // "For each color among permanents you control, add one mana of that color." Adds
                // one mana of every color found simultaneously (no player choice), respecting the
                // mana-production multiplier and marking it as creature mana when the source is one.
                Set<CardColor> availableColors = collectColorsAmongControlled(gameData, playerId, eachColor.predicate());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                for (CardColor color : availableColors) {
                    ManaColor manaColor = ManaColor.valueOf(color.name());
                    pool.add(manaColor, manaMultiplier);
                    if (isCreatureSource) {
                        pool.addCreatureMana(manaColor, manaMultiplier);
                    }
                }
                if (availableColors.isEmpty()) {
                    
                    gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " activates ").card(permanent.getCard()).text(" but produces no mana (no colors among permanents controlled).").build());
                } else {
                    
                    gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " adds " + availableColors.stream() .sorted().map(c -> "{" + c.getCode() + "}").reduce("", String::concat) + " from ").card(permanent.getCard()).text(".").build());
                }
            } else if (effect instanceof AwardManaOfColorsLandsCouldProduceEffect landColors) {
                Set<CardColor> availableColors = collectColorsLandsCouldProduce(gameData, playerId, landColors);
                if (availableColors.size() == 1) {
                    CardColor onlyColor = availableColors.iterator().next();
                    ManaColor manaColor = ManaColor.valueOf(onlyColor.name());
                    gameData.playerManaPools.get(playerId).add(manaColor);
                    
                    gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " adds {" + onlyColor.getCode() + "} from " , permanent.getCard(), "."));
                } else if (availableColors.size() > 1) {
                    ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource);
                    List<String> colors = availableColors.stream()
                            .map(Enum::name)
                            .sorted()
                            .toList();
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            playerId, null, null, choiceContext, colors, "Choose a color of mana to add."));
                    log.info("Game {} - Awaiting {} to choose a mana color from lands' colors", gameData.id, player.getUsername());
                } else {
                    
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " activates ")
                            .card(permanent.getCard())
                            .text(" but produces no mana (no matching land could produce colored mana).")
                            .build());
                }
            } else if (effect instanceof AwardManaOfTypeUntappedLandCouldProduceEffect) {
                Set<ManaColor> availableTypes = collectManaTypesUntappedLandCouldProduce(gameData, permanent);
                if (availableTypes.size() == 1) {
                    ManaColor onlyType = availableTypes.iterator().next();
                    gameData.playerManaPools.get(playerId).add(onlyType);
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " adds {" + onlyType.getCode() + "} from ", permanent.getCard(), "."));
                } else if (availableTypes.size() > 1) {
                    ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource);
                    List<String> types = availableTypes.stream().map(Enum::name).sorted().toList();
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            playerId, null, null, choiceContext, types, "Choose a type of mana to add."));
                    log.info("Game {} - Awaiting {} to choose a mana type from the untapped land", gameData.id, player.getUsername());
                } else {
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " activates ")
                            .card(permanent.getCard())
                            .text(" but produces no mana (the untapped land could produce none).")
                            .build());
                }
            } else if (effect instanceof AwardManaOfTypeSacrificedLandCouldProduceEffect) {
                Set<ManaColor> availableTypes = collectManaTypesSacrificedLandCouldProduce(permanent);
                if (availableTypes.size() == 1) {
                    ManaColor onlyType = availableTypes.iterator().next();
                    gameData.playerManaPools.get(playerId).add(onlyType);
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " adds {" + onlyType.getCode() + "} from ", permanent.getCard(), "."));
                } else if (availableTypes.size() > 1) {
                    ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource);
                    List<String> types = availableTypes.stream().map(Enum::name).sorted().toList();
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            playerId, null, null, choiceContext, types, "Choose a type of mana to add."));
                    log.info("Game {} - Awaiting {} to choose a mana type from the sacrificed land", gameData.id, player.getUsername());
                } else {
                    gameLogService.append(gameData, GameLog.builder()
                            .text(player.getUsername() + " activates ")
                            .card(permanent.getCard())
                            .text(" but produces no mana (the sacrificed land could produce none).")
                            .build());
                }
            } else if (effect instanceof GainLifeEffect gain) {
                int amount = amountEvaluationService.evaluate(gameData, gain.amount(),
                        new AmountContext(playerId, permanent, null, xValue, 0));
                lifeSupport.applyGainLife(gameData, playerId, amount);
            } else if (effect instanceof LoseLifeEffect loss && isNonTargetedLifeLoss(loss)) {
                // Life loss riding along with mana production (Cryptolith Fragment: "Add one mana
                // of any color. Each player loses 1 life."). Still a mana ability, so it resolves
                // here rather than on the stack.
                int amount = amountEvaluationService.evaluate(gameData, loss.amount(),
                        new AmountContext(playerId, permanent, null, xValue, 0));
                for (UUID victimId : lifeLossRecipients(gameData, playerId, loss.recipient())) {
                    lifeSupport.applyLifeLoss(gameData, victimId, amount, permanent.getCard().getName());
                }
            } else if (effect instanceof DealDamageToPlayersEffect dmg && dmg.recipient() == DamageRecipient.CONTROLLER) {
                String cardName = permanent.getCard().getName();
                int damage = amountEvaluationService.evaluate(gameData, dmg.amount(),
                        new AmountContext(playerId, permanent, null, 0, 0));
                if (gameQueryService.isDamagePreventable(gameData)) {
                    CardColor sourceColor = gameQueryService.getEffectiveColor(gameData, permanent);
                    boolean sourceDamagePrevented = damagePreventionService.isSourceDamagePreventedForPlayer(
                            gameData, playerId, permanent.getId());
                    if (sourceDamagePrevented && !gameQueryService.isDamageFromPermanentSourcePrevented(gameData, permanent)) {
                        damagePreventionService.applySourceDamagePreventionForPlayer(
                                gameData, playerId, permanent.getId(), damage,
                                gameQueryService.getEffectiveColors(gameData, permanent));
                    }
                    if (gameQueryService.isDamageFromPermanentSourcePrevented(gameData, permanent)
                            || sourceDamagePrevented
                            || gameData.isPreventedFromDealingDamage(permanent.getId())
                            || damagePreventionService.applyColorDamagePreventionForPlayer(gameData, playerId, sourceColor)) {
                        damage = 0;
                    } else {
                        // One-shot Circle-of-Protection shields may prevent only part of the damage
                        damage = damagePreventionService.applyPlayerNextSourceDamageShield(gameData, playerId, permanent.getId(), damage);
                    }
                }
                if (damage > 0) {
                    int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, playerId, damage);
                    effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);
                    effectiveDamage -= damagePreventionService.applyDamageToControllerAndPutCounterOnSelf(
                            gameData, playerId, effectiveDamage);
                    if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId)) {
                        if (gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
                            int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                            gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                            String logEntry = player.getUsername() + " gets " + effectiveDamage + " poison counters from " + cardName + ".";
                            gameLogService.append(gameData, GameLog.text(logEntry));
                        }
                    } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                        gameLogService.append(gameData, GameLog.text(player.getUsername() + "'s life total can't change."));
                    } else {
                        int currentLife = gameData.getLife(playerId);
                        gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);
                        if (effectiveDamage > 0) {
                            String logEntry = player.getUsername() + " takes " + effectiveDamage + " damage from " + cardName + ".";
                            gameLogService.append(gameData, GameLog.text(logEntry));
                            log.info("Game {} - {} takes {} damage from {}", gameData.id, player.getUsername(), effectiveDamage, cardName);
                        }
                    }
                    if (effectiveDamage > 0) {
                        gameData.recordDamageToPlayer(playerId, effectiveDamage);
                        gameData.recordDamageRecipientBySource(permanent.getId(), playerId);
                        gameData.recordNoncombatDamageSourceToPlayer(permanent.getId(), playerId);
                        triggerCollectionService.checkOpponentDealtDamageTriggers(gameData, playerId, effectiveDamage);
                        if (gameQueryService.isCreature(gameData, permanent)) {
                            gameData.recordCreatureDamageSourceToPlayer(permanent.getId(), playerId);
                        }
                    }
                }
            } else if (effect instanceof DealDamageToPlayersEffect dmg && dmg.recipient() == DamageRecipient.EACH_OPPONENT) {
                // Reflexive "When you do" rider on a mana ability, e.g. Rubble Rouser:
                // "Add {R}. When you do, this creature deals 1 damage to each opponent."
                int damage = amountEvaluationService.evaluate(gameData, dmg.amount(),
                        new AmountContext(playerId, permanent, null, 0, 0));
                for (UUID opponentId : gameData.orderedPlayerIds) {
                    if (opponentId.equals(playerId)) continue;
                    dealManaAbilityRiderDamageToPlayer(gameData, permanent, opponentId, damage);
                }
            } else if (effect instanceof RegisterDrawCardsAtNextUpkeepEffect draw) {
                // "Draw a card at the beginning of the next turn's upkeep." rider on a mana ability (Barbed Sextant).
                gameData.queueDelayedAction(new DrawCardsAtNextUpkeep(playerId, draw.count(), permanent.getCard()));
            } else if (effect instanceof DrawCardEffect draw) {
                int amount = amountEvaluationService.evaluate(gameData, draw.amount(),
                        AmountContext.forManaAbility(permanent, playerId, xValue));
                for (int i = 0; i < amount; i++) {
                    drawService.resolveDrawCard(gameData, playerId);
                }
            } else if (effect instanceof ManaAbilityCardDrawingEffect draw) {
                int amount = amountEvaluationService.evaluate(gameData, draw.drawnCardAmount(),
                        AmountContext.forManaAbility(permanent, playerId, xValue));
                playerInteractionSupport.applyDrawCards(gameData, playerId, amount);
            } else if (effect instanceof PutCountersOnSelfEffect counters
                    && !gameQueryService.cantHaveCounters(gameData, permanent)) {
                // "Add one mana of any color. Put a brick counter on this artifact." (Pyramid of the
                // Pantheon). A mana ability resolves without the stack, so the counter is placed
                // inline here rather than through the normal effect-handler path.
                int count = counters.amount() != null
                        ? amountEvaluationService.evaluate(gameData, counters.amount(),
                                AmountContext.forManaAbility(permanent, playerId))
                        : counters.count();
                count = gameQueryService.replaceCounters(
                        gameData, permanent, counters.counterType(), count);
                if (count > 0) {
                    permanent.setCounterCount(counters.counterType(),
                            permanent.getCounterCount(counters.counterType()) + count);
                    if (counters.counterType() == CounterType.PLUS_ONE_PLUS_ONE) {
                        gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(playerId);
                    }
                    String counterName = counters.counterType().name().toLowerCase();
                    String counterText = count == 1
                            ? "a " + counterName + " counter"
                            : count + " " + counterName + " counters";
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " puts " + counterText + " on ", permanent.getCard(), "."));
                }
            } else if (effect instanceof SkipNextUntapEffect skip
                    && skip.scope() == TapUntapScope.SELF) {
                // "{T}, Exert this creature: Add …" (Oasis Ritualist). Exert is modeled as
                // SkipNextUntapEffect(SELF); on a mana ability it must apply inline here because
                // mana abilities never hit the stack / NormalEffectHandlerBean path.
                permanent.setSkipUntapCount(permanent.getSkipUntapCount() + 1);
                gameLogService.append(gameData, GameLog.cardThen(
                        permanent.getCard(), " won't untap during its controller's next untap step."));
            } else if (effect instanceof ReturnSourceToHandAtNextUntapEffect) {
                // "Add one mana of any color. During your next untap step, … return this land to
                // its owner's hand" (Undiscovered Paradise). Mana abilities resolve without the
                // stack, so the bounce flag is set inline rather than through the normal handler.
                permanent.setReturnToHandAtNextUntap(true);
                gameLogService.append(gameData, GameLog.cardThen(
                        permanent.getCard(), " will return to its owner's hand during its controller's next untap step."));
            }
        }
        stateBasedActionService.performStateBasedActions(gameData);
        // CR 605.3b: Do NOT clear priorityPassedBy here — mana abilities don't affect priority.
        // Priority clearing is handled by the caller when deferred triggers are pushed onto the stack.
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)) {
            triggerCollectionService.processNextSelfTriggeredAbilityTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /**
     * Deals a mana-ability rider's damage to a single player, applying the same prevention/infect/
     * redirect handling used by the {@link DealDamageToPlayersEffect} CONTROLLER rider. Used by
     * {@link DealDamageToPlayersEffect} EACH_OPPONENT riders (e.g. Rubble Rouser).
     */
    private void dealManaAbilityRiderDamageToPlayer(GameData gameData, Permanent permanent, UUID playerId, int damage) {
        String cardName = permanent.getCard().getName();
        String playerName = gameData.playerIdToName.get(playerId);
        if (gameQueryService.isDamagePreventable(gameData)) {
            CardColor sourceColor = gameQueryService.getEffectiveColor(gameData, permanent);
            boolean sourceDamagePrevented = damagePreventionService.isSourceDamagePreventedForPlayer(
                    gameData, playerId, permanent.getId());
            if (sourceDamagePrevented && !gameQueryService.isDamageFromPermanentSourcePrevented(gameData, permanent)) {
                damagePreventionService.applySourceDamagePreventionForPlayer(
                        gameData, playerId, permanent.getId(), damage,
                        gameQueryService.getEffectiveColors(gameData, permanent));
            }
            if (gameQueryService.isDamageFromPermanentSourcePrevented(gameData, permanent)
                    || sourceDamagePrevented
                    || gameData.isPreventedFromDealingDamage(permanent.getId())
                    || damagePreventionService.applyColorDamagePreventionForPlayer(gameData, playerId, sourceColor)) {
                damage = 0;
            } else {
                // One-shot Circle-of-Protection shields may prevent only part of the damage
                damage = damagePreventionService.applyPlayerNextSourceDamageShield(gameData, playerId, permanent.getId(), damage);
            }
        }
        if (damage > 0) {
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, playerId, damage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);
            effectiveDamage -= damagePreventionService.applyDamageToControllerAndPutCounterOnSelf(
                    gameData, playerId, effectiveDamage);
            if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId)) {
                if (gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                    gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                    gameLogService.append(gameData, GameLog.text(playerName + " gets " + effectiveDamage + " poison counters from " + cardName + "."));
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            } else {
                int currentLife = gameData.getLife(playerId);
                gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);
                if (effectiveDamage > 0) {
                    gameLogService.append(gameData, GameLog.text(playerName + " takes " + effectiveDamage + " damage from " + cardName + "."));
                    log.info("Game {} - {} takes {} damage from {}", gameData.id, playerName, effectiveDamage, cardName);
                }
            }
            if (effectiveDamage > 0) {
                gameData.recordDamageToPlayer(playerId, effectiveDamage);
                gameData.recordDamageRecipientBySource(permanent.getId(), playerId);
                gameData.recordNoncombatDamageSourceToPlayer(permanent.getId(), playerId);
                triggerCollectionService.checkOpponentDealtDamageTriggers(gameData, playerId, effectiveDamage);
                if (gameQueryService.isCreature(gameData, permanent)) {
                    gameData.recordCreatureDamageSourceToPlayer(permanent.getId(), playerId);
                }
            }
        }
    }

    private boolean isDampingManaReplacementActive(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null) {
                for (Permanent perm : bf) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof ReplaceLandExcessManaWithColorlessEffect) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Whether a {@link LoseLifeEffect} bundled into a mana ability can be applied without the
     * stack. Only the recipients that need no chosen target qualify; a {@code TARGET_*} recipient
     * would make the ability targeted, which disqualifies it as a mana ability (CR 605.1a).
     */
    private static boolean isNonTargetedLifeLoss(LoseLifeEffect effect) {
        return effect.recipient() == LoseLifeRecipient.CONTROLLER
                || effect.recipient() == LoseLifeRecipient.EACH_PLAYER
                || effect.recipient() == LoseLifeRecipient.EACH_OPPONENT;
    }

    /** The players losing life, in turn order, for a non-targeted {@link LoseLifeRecipient}. */
    private static List<UUID> lifeLossRecipients(GameData gameData, UUID controllerId, LoseLifeRecipient recipient) {
        return switch (recipient) {
            case CONTROLLER -> List.of(controllerId);
            case EACH_PLAYER -> List.copyOf(gameData.orderedPlayerIds);
            case EACH_OPPONENT -> gameData.orderedPlayerIds.stream()
                    .filter(pid -> !pid.equals(controllerId))
                    .toList();
            default -> List.of();
        };
    }

    private int calculateTotalManaProduction(GameData gameData, UUID playerId, Permanent permanent, List<CardEffect> effects, int xValue) {
        int total = 0;
        for (CardEffect effect : effects) {
            if (effect instanceof AwardManaEffect award) {
                total += amountEvaluationService.evaluate(gameData, award.amount(),
                        AmountContext.forManaAbility(permanent, playerId, xValue));
            } else if (effect instanceof AwardManaToChosenPlayerEffect chosen) {
                total += chosen.amount();
            } else if (effect instanceof AwardAnyColorManaEffect anyColor) {
                total += amountEvaluationService.evaluate(gameData, anyColor.amount(),
                        AmountContext.forManaAbility(permanent, playerId, xValue));
            } else if (effect instanceof AwardManaOfColorsEffect ofColors) {
                total += amountEvaluationService.evaluate(gameData, ofColors.amount(),
                        AmountContext.forManaAbility(permanent, playerId, xValue));
            } else if (effect instanceof AwardRestrictedManaEffect arm) {
                total += arm.amount();
            } else if (effect instanceof AwardHasteGrantingManaEffect ahg) {
                total += ahg.amount();
            } else if (effect instanceof AwardUncounterableGrantingManaEffect aug) {
                total += aug.amount();
            } else if (effect instanceof AwardManaOfColorsAmongControlledEffect manaAmong) {
                Set<CardColor> colors = collectColorsAmongControlled(gameData, playerId, manaAmong);
                if (!colors.isEmpty()) {
                    total += 1;
                }
            } else if (effect instanceof AwardOneManaOfEachColorAmongControlledEffect eachColor) {
                total += collectColorsAmongControlled(gameData, playerId, eachColor.predicate()).size();
            } else if (effect instanceof AwardManaOfColorsLandsCouldProduceEffect landColors) {
                if (!collectColorsLandsCouldProduce(gameData, playerId, landColors).isEmpty()) {
                    total += 1;
                }
            } else if (effect instanceof AwardManaOfTypeUntappedLandCouldProduceEffect) {
                if (!collectManaTypesUntappedLandCouldProduce(gameData, permanent).isEmpty()) {
                    total += 1;
                }
            } else if (effect instanceof AwardManaOfTypeSacrificedLandCouldProduceEffect) {
                if (!collectManaTypesSacrificedLandCouldProduce(permanent).isEmpty()) {
                    total += 1;
                }
            } else if (effect instanceof AddNotedManaForLastExiledCardEffect) {
                for (int amount : notedManaOf(gameData, permanent).values()) {
                    total += amount;
                }
            } else if (effect instanceof DoubleManaPoolEffect) {
                total += gameData.playerManaPools.get(playerId).getTotal();
            }
        }
        return total;
    }

    /** The mana noted on {@code permanent} by {@code NoteManaSpentForActivationEffect}, per color. */
    private Map<ManaColor, Integer> notedManaOf(GameData gameData, Permanent permanent) {
        return gameData.notedMana.getOrDefault(permanent.getCard().getId(), Map.of());
    }

    /**
     * Ice Cauldron's second ability: add the noted mana, reserved for casting the card the artifact
     * last exiled. Reserved mana is keyed by that card's id, so it is unspendable once the card
     * leaves exile.
     */
    private void addNotedManaForLastExiledCard(GameData gameData, Player player, Permanent permanent) {
        Map<ManaColor, Integer> noted = notedManaOf(gameData, permanent);
        Card exiledCard = gameData.getImprintedCard(permanent.getCard());
        if (noted.isEmpty() || exiledCard == null) {
            log.info("Game {} - {} has no noted mana or no exiled card, adds nothing", gameData.id, permanent.getCard().getName());
            return;
        }

        ManaPool pool = gameData.playerManaPools.get(player.getId());
        StringBuilder added = new StringBuilder();
        for (Map.Entry<ManaColor, Integer> entry : noted.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            pool.addExiledCardOnlyMana(exiledCard.getId(), entry.getKey(), entry.getValue());
            added.append(("{" + entry.getKey().getCode() + "}").repeat(entry.getValue()));
        }

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " adds " + added.toString().trim() + ", spendable only to cast ",
                exiledCard, "."));
        log.info("Game {} - {} adds noted mana {} for exiled card {}", gameData.id, player.getUsername(), noted, exiledCard.getName());
    }

    /**
     * Jeweled Amulet's second ability: add one mana of the source's last noted type, with no
     * restriction on how it may be spent. The noted map holds the mana spent to pay the noting
     * activation cost, which for Jeweled Amulet is exactly one mana.
     */
    private void addNotedMana(GameData gameData, Player player, Permanent permanent, int manaMultiplier) {
        Map<ManaColor, Integer> noted = notedManaOf(gameData, permanent);
        ManaColor notedColor = noted.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (notedColor == null) {
            log.info("Game {} - {} has no noted mana, adds nothing", gameData.id, permanent.getCard().getName());
            return;
        }

        gameData.playerManaPools.get(player.getId()).add(notedColor, manaMultiplier);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " adds " + ("{" + notedColor.getCode() + "}").repeat(manaMultiplier) + " from ",
                permanent.getCard(), "."));
        log.info("Game {} - {} adds noted mana {} from {}", gameData.id, player.getUsername(), notedColor,
                permanent.getCard().getName());
    }

    private Set<CardColor> collectColorsAmongControlled(GameData gameData, UUID playerId,
                                                         AwardManaOfColorsAmongControlledEffect effect) {
        return collectColorsAmongControlled(gameData, playerId, effect.predicate());
    }

    private Set<CardColor> collectColorsAmongControlled(GameData gameData, UUID playerId,
                                                         PermanentPredicate predicate) {
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return colors;
        }
        for (Permanent p : battlefield) {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, p, predicate)) {
                continue;
            }
            if (p.isColorOverridden()) {
                colors.addAll(p.getTransientColors());
            } else {
                if (p.getCard().getColors() != null) {
                    colors.addAll(p.getCard().getColors());
                }
                colors.addAll(p.getTransientColors());
                colors.addAll(p.getGrantedColors());
            }
        }
        return colors;
    }

    /**
     * Collects the colors of mana that lands in the effect's scope, matching its land predicate,
     * could produce (CR: a land's mana abilities). Colorless is not a color and is excluded.
     * Used by Fellwar Stone (opponent lands) and Star Compass (basic lands you control).
     */
    private Set<CardColor> collectColorsLandsCouldProduce(GameData gameData, UUID playerId,
                                                          AwardManaOfColorsLandsCouldProduceEffect effect) {
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        for (UUID ownerId : gameData.orderedPlayerIds) {
            boolean isSelf = ownerId.equals(playerId);
            if (effect.scope() == ManaColorLandScope.CONTROLLER ? !isSelf : isSelf) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(ownerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent p : battlefield) {
                if (!p.getCard().hasType(CardType.LAND)) {
                    continue;
                }
                if (!predicateEvaluationService.matchesPermanentPredicate(gameData, p, effect.landPredicate())) {
                    continue;
                }
                collectManaColorsFromEffects(p.getCard().getEffects(EffectSlot.ON_TAP), colors);
                for (ActivatedAbility ability : p.getCard().getActivatedAbilities()) {
                    collectManaColorsFromEffects(ability.getEffects(), colors);
                }
            }
        }
        return colors;
    }

    /**
     * The mana types the land untapped to pay this ability's untap cost could produce, colorless
     * included ("one mana of any type that land could produce", Benthic Explorers). Empty if the
     * recorded permanent is gone or is not a land that produces mana.
     */
    private Set<ManaColor> collectManaTypesUntappedLandCouldProduce(GameData gameData, Permanent source) {
        UUID untappedLandId = source.getChosenPermanentId();
        Permanent land = untappedLandId == null ? null : gameQueryService.findPermanentById(gameData, untappedLandId);
        Set<ManaColor> types = EnumSet.noneOf(ManaColor.class);
        if (land == null || !land.getCard().hasType(CardType.LAND)) {
            return types;
        }
        collectManaTypesFromEffects(land.getCard().getEffects(EffectSlot.ON_TAP), types);
        for (ActivatedAbility ability : land.getCard().getActivatedAbilities()) {
            collectManaTypesFromEffects(ability.getEffects(), types);
        }
        return types;
    }

    /**
     * The mana types the land sacrificed to pay this ability's cost could produce, colorless
     * included (Squandered Resources). Reads the card recorded on the source at payment time,
     * because the permanent itself is already gone.
     */
    private Set<ManaColor> collectManaTypesSacrificedLandCouldProduce(Permanent source) {
        Card landCard = source.getChosenCard();
        Set<ManaColor> types = EnumSet.noneOf(ManaColor.class);
        if (landCard == null || !landCard.hasType(CardType.LAND)) {
            return types;
        }
        collectManaTypesFromEffects(landCard.getEffects(EffectSlot.ON_TAP), types);
        for (ActivatedAbility ability : landCard.getActivatedAbilities()) {
            collectManaTypesFromEffects(ability.getEffects(), types);
        }
        return types;
    }

    private void collectManaTypesFromEffects(List<CardEffect> effects, Set<ManaColor> types) {
        for (CardEffect effect : effects) {
            if (effect instanceof AwardManaEffect award) {
                if (award.color() != null) {
                    types.add(award.color());
                }
            } else if (effect instanceof AwardAnyColorManaEffect) {
                types.add(ManaColor.WHITE);
                types.add(ManaColor.BLUE);
                types.add(ManaColor.BLACK);
                types.add(ManaColor.RED);
                types.add(ManaColor.GREEN);
            }
        }
    }

    private void collectManaColorsFromEffects(List<CardEffect> effects, Set<CardColor> colors) {
        for (CardEffect effect : effects) {
            if (effect instanceof AwardManaEffect award) {
                ManaColor manaColor = award.color();
                if (manaColor != null && manaColor != ManaColor.COLORLESS) {
                    colors.add(CardColor.valueOf(manaColor.name()));
                }
            } else if (effect instanceof AwardAnyColorManaEffect) {
                colors.add(CardColor.WHITE);
                colors.add(CardColor.BLUE);
                colors.add(CardColor.BLACK);
                colors.add(CardColor.RED);
                colors.add(CardColor.GREEN);
            }
        }
    }

    private void pushAbilityOnStack(GameData gameData,
                                    UUID playerId,
                                    Permanent permanent,
                                    ActivatedAbility ability,
                                    List<CardEffect> snapshotEffects,
                                    int effectiveXValue,
                                    UUID effectiveTargetId,
                                    Zone targetZone,
                                    List<UUID> targetIds,
                                    Map<UUID, Integer> damageAssignments) {
        Zone effectiveTargetZone = targetZone;
        if (ability.targetsSpellOnStack(targetZone)) {
            effectiveTargetZone = Zone.STACK;
        }
        if (effectiveTargetZone == Zone.BATTLEFIELD) {
            effectiveTargetZone = null;
        }
        List<UUID> effectiveTargetIds = targetIds != null ? targetIds : List.of();
        boolean mixedBattlefieldAndGraveyardTargets = effectiveTargetZone == null
                && effectiveTargetId == null
                && !effectiveTargetIds.isEmpty()
                && ability.getMultiTargetFilters().stream()
                        .anyMatch(GraveyardCardPredicateTargetFilter.class::isInstance);
        // When targeting graveyard cards with multiple targets, use targetCardIds
        // (for proper fizzle checking and resolution by graveyard handlers)
        List<UUID> effectiveTargetCardIds;
        List<UUID> effectivePermanentTargetIds;
        if (mixedBattlefieldAndGraveyardTargets) {
            List<UUID> graveyardTargetIds = new ArrayList<>();
            List<UUID> permanentTargetIds = new ArrayList<>();
            for (int i = 0; i < effectiveTargetIds.size(); i++) {
                if (i < ability.getMultiTargetFilters().size()
                        && ability.getMultiTargetFilters().get(i) instanceof GraveyardCardPredicateTargetFilter) {
                    graveyardTargetIds.add(effectiveTargetIds.get(i));
                } else {
                    permanentTargetIds.add(effectiveTargetIds.get(i));
                }
            }
            if (!permanentTargetIds.isEmpty()) {
                effectiveTargetId = permanentTargetIds.removeFirst();
            }
            effectiveTargetCardIds = graveyardTargetIds;
            effectivePermanentTargetIds = permanentTargetIds;
        } else {
            effectiveTargetCardIds = (effectiveTargetZone == Zone.GRAVEYARD && !effectiveTargetIds.isEmpty())
                    ? effectiveTargetIds : List.of();
            effectivePermanentTargetIds = (effectiveTargetZone == Zone.GRAVEYARD && !effectiveTargetIds.isEmpty())
                    ? List.of() : effectiveTargetIds;
        }
        StackEntry stackEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                permanent.getCard(),
                playerId,
                permanent.getCard().getName() + "'s ability",
                snapshotEffects,
                effectiveXValue,
                effectiveTargetId,
                permanent.getId(),
                damageAssignments != null ? damageAssignments : Map.of(),
                effectiveTargetZone,
                effectiveTargetCardIds,
                effectivePermanentTargetIds
        );
        stackEntry.setTargetFilter(ability.getTargetFilter());
        stackEntry.setSourcePermanentSnapshot(permanent);
        Map<ManaColor, Integer> activationManaSpent = gameData.abilityActivationManaSpent.get(permanent.getCard().getId());
        stackEntry.setActivationManaSpent(activationManaSpent == null ? Map.of() : Map.copyOf(activationManaSpent));
        // Carry the creature chosen during activation (e.g. tapped for a TapCreatureCost) so
        // ChosenPermanentPower can read its power as the ability resolves (Impelled Giant).
        stackEntry.setChosenPermanentId(permanent.getChosenPermanentId());
        gameData.stack.add(stackEntry);
        triggerCollectionService.checkBecomesTargetOfAbilityTriggers(gameData);
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.priorityPassedBy.clear();
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)) {
            triggerCollectionService.processNextSelfTriggeredAbilityTarget(gameData);
        }
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }
}


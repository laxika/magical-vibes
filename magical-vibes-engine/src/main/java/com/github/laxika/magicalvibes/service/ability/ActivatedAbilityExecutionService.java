package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorSubtypeSpellOrAbilityManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyOneColorInstantSorceryOnlyManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardFlashbackOnlyAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsAmongControlledEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaToChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleManaPoolEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandExcessManaWithColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAsCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
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
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final StateBasedActionService stateBasedActionService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;

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
     *   <li>Logs the activation and broadcasts to all players.</li>
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
        if (effectiveTargetId == null) {
            boolean needsSelfTarget = abilityEffects.stream().anyMatch(e ->
                    e.isSelfTargeting() && !(e instanceof RegenerateEffect && permanent.getCard().isAura()));
            if (needsSelfTarget) {
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
            effectiveXValue = permanent.getCounterCount(counterType);
            permanent.setCounterCount(counterType, 0);
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

        boolean shouldExileSelf = abilityEffects.stream().anyMatch(e -> e instanceof ExileSelfCost);
        if (shouldExileSelf) {
            permanentRemovalService.removePermanentToExile(gameData, permanent);
        }

        boolean shouldSacrifice = abilityEffects.stream().anyMatch(e -> e instanceof SacrificeSelfCost);
        if (shouldSacrifice) {
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

        List<StackEntry> deferredCostTriggers = List.of();
        if (gameData.stack.size() > stackBeforeCosts) {
            deferredCostTriggers = new ArrayList<>(gameData.stack.subList(stackBeforeCosts, gameData.stack.size()));
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

        String logEntry = player.getUsername() + " activates " + permanent.getCard().getName() + "'s ability.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} activates {}'s ability", gameData.id, player.getUsername(), permanent.getCard().getName());

        List<CardEffect> snapshotEffects = snapshotEffects(abilityEffects, permanent);
        // CR 605.1a: A mana ability doesn't require a target, could add mana, and isn't a loyalty ability.
        // Pain lands (e.g. Adarkar Wastes) include a DealDamageToPlayersEffect(CONTROLLER) alongside mana production
        // and are still mana abilities — they resolve immediately without using the stack.
        boolean isManaAbility = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget()
                && ability.getLoyaltyCost() == null
                && !snapshotEffects.isEmpty()
                && snapshotEffects.stream().anyMatch(e -> e instanceof ManaProducingEffect);

        if (isManaAbility) {
            // A "pure" mana activation (tap-only cost, only fixed-shape AwardManaEffect output)
            // can be undone by the MTGO-style cancel-casting UI: snapshot the pool around
            // resolution so the exact mana added (incl. Damping Sphere replacement) is recorded.
            boolean revertable = ability.isRequiresTap()
                    && ability.getManaCost() == null
                    && abilityEffects.stream().noneMatch(e -> e instanceof CostEffect)
                    && snapshotEffects.stream().allMatch(e -> e instanceof AwardManaEffect);
            ManaPool pool = gameData.playerManaPools.get(playerId);
            java.util.EnumMap<ManaColor, Integer> poolBefore =
                    revertable ? AbilityActivationService.snapshotPoolColors(pool) : null;
            java.util.EnumMap<ManaColor, Integer> creatureManaBefore =
                    revertable ? AbilityActivationService.snapshotCreatureManaColors(pool) : null;
            int pendingTriggersBefore = gameData.pendingManaAbilityTriggers.size();

            resolveManaAbility(gameData, playerId, player, permanent, snapshotEffects);
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
                AbilityActivationService.recordRevertableManaActivation(
                        gameData, playerId, permanent, poolBefore, creatureManaBefore, deferred);
            } else {
                // A mana ability with side effects (pain-land damage, pool doubling, color choice,
                // extra costs) can't be undone — and undoing earlier activations after it could
                // interact with its result (e.g. doubled mana), so bar the whole window.
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
        }
        // Rings of Brighthearth: "whenever you activate an ability, if it isn't a mana ability, you
        // may pay {2} to copy it." Collected after the ability is on the stack so it can be snapshotted.
        StackEntry abilityEntry = abilityStackIndex < gameData.stack.size() ? gameData.stack.get(abilityStackIndex) : null;
        triggerCollectionService.checkControllerActivatesNonManaAbilityTriggers(gameData, playerId, abilityEntry, ability);
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

    private List<CardEffect> snapshotEffects(List<CardEffect> abilityEffects, Permanent permanent) {
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect effect : abilityEffects) {
            if (effect instanceof CostEffect) {
                continue;
            }
            if (effect instanceof CantBlockSourceEffect) {
                snapshotEffects.add(new CantBlockSourceEffect(permanent.getId()));
            } else if (effect instanceof MustBlockSourceEffect) {
                snapshotEffects.add(new MustBlockSourceEffect(permanent.getId()));
            } else if (effect instanceof PreventNextColorDamageToControllerEffect && permanent.getChosenColor() != null) {
                snapshotEffects.add(new PreventNextColorDamageToControllerEffect(permanent.getChosenColor()));
            } else if (effect instanceof GrantKeywordToChosenCreatureUntilEndOfTurnEffect gk) {
                snapshotEffects.add(new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(gk.keyword(), permanent.getChosenPermanentId()));
            } else {
                snapshotEffects.add(effect);
            }
        }
        return snapshotEffects;
    }

    private void resolveManaAbility(GameData gameData, UUID playerId, Player player, Permanent permanent, List<CardEffect> snapshotEffects) {
        // CR 603.2 / 603.3: triggers fired from effects resolving inside a mana ability
        // (e.g. Pristine Talisman's life-gain triggering Sanguine Bond) must queue and
        // wait for the next priority window, not land on the stack immediately.
        gameData.manaAbilityResolutionDepth++;
        try {
            doResolveManaAbility(gameData, playerId, player, permanent, snapshotEffects);
        } finally {
            gameData.manaAbilityResolutionDepth--;
        }
    }

    private void doResolveManaAbility(GameData gameData, UUID playerId, Player player, Permanent permanent, List<CardEffect> snapshotEffects) {
        boolean isCreatureSource = gameQueryService.isCreature(gameData, permanent);

        // Mana Reflection: tapping a permanent for mana produces twice as much of that mana (2^count).
        int manaMultiplier = gameQueryService.manaProductionMultiplier(gameData, playerId);

        // Damping Sphere replacement: if a land is tapped for two or more mana, it produces {C} instead.
        boolean dampingReplacement = false;
        if (permanent.getCard().hasType(CardType.LAND) && isDampingManaReplacementActive(gameData)) {
            int totalMana = calculateTotalManaProduction(gameData, playerId, permanent, snapshotEffects);
            if (totalMana >= 2) {
                dampingReplacement = true;
                gameData.playerManaPools.get(playerId).add(ManaColor.COLORLESS, 1);
                String logEntry = player.getUsername() + " adds {C} from " + permanent.getCard().getName()
                        + " (Damping Sphere replaces " + totalMana + " mana).";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        for (CardEffect effect : snapshotEffects) {
            if (dampingReplacement && effect instanceof ManaProducingEffect) {
                continue;
            }
            if (effect instanceof AwardManaEffect award) {
                int amount = amountEvaluationService.evaluate(gameData, award.amount(),
                        AmountContext.forManaAbility(permanent, playerId)) * manaMultiplier;
                if (amount > 0) {
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    pool.add(award.color(), amount);
                    if (isCreatureSource) {
                        pool.addCreatureMana(award.color(), amount);
                    }
                    // Dynamic amounts (per-permanent counts, charge counters, source power) log the
                    // realized quantity for clarity; a flat "Add {G}" is covered by the activation log.
                    if (!(award.amount() instanceof com.github.laxika.magicalvibes.model.amount.Fixed)) {
                        String logEntry = player.getUsername() + " adds " + amount + " " + award.color().getCode()
                                + " from " + permanent.getCard().getName() + ".";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    }
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
            } else if (effect instanceof AwardAnyColorChosenSubtypeCreatureManaEffect) {
                CardSubtype chosenSubtype = permanent.getChosenSubtype();
                if (chosenSubtype != null) {
                    ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, false, manaMultiplier, chosenSubtype);
                    List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            playerId, null, null, choiceContext, colors, "Choose a color of mana to add."));
                    log.info("Game {} - Awaiting {} to choose a mana color (restricted to {} creatures)", gameData.id, player.getUsername(), chosenSubtype);
                }
            } else if (effect instanceof AwardAnyColorSubtypeSpellOrAbilityManaEffect soa) {
                ChoiceContext.ManaColorChoice choiceContext =
                        ChoiceContext.ManaColorChoice.subtypeSpellOrAbility(playerId, soa.amount() * manaMultiplier, soa.subtype());
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null, choiceContext, colors, "Choose a color of mana to add."));
                log.info("Game {} - Awaiting {} to choose a mana color (restricted to {} spells/abilities)",
                        gameData.id, player.getUsername(), soa.subtype());
            } else if (effect instanceof AwardAnyColorManaWithInstantSorceryCopyEffect aacse) {
                ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, aacse.amount() * manaMultiplier);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null, choiceContext, colors, "Choose a color of mana to add."));
                // Register delayed trigger: copy next instant/sorcery spell cast with this mana
                gameData.pendingNextInstantSorceryCopyCount.merge(playerId, 1, Integer::sum);
                log.info("Game {} - Awaiting {} to choose a mana color (with spell copy trigger)", gameData.id, player.getUsername());
            } else if (effect instanceof AwardAnyColorManaEffect aace) {
                ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, aace.amount() * manaMultiplier);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null, choiceContext, colors, "Choose a color of mana to add."));
                log.info("Game {} - Awaiting {} to choose a mana color", gameData.id, player.getUsername());
            } else if (effect instanceof AwardManaOfColorsEffect ofColors) {
                int picks = ofColors.amount() * manaMultiplier;
                if (ofColors.colors().size() == 1) {
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
            } else if (effect instanceof AwardAnyOneColorInstantSorceryOnlyManaEffect aisom) {
                ChoiceContext.ManaColorChoice choiceContext = ChoiceContext.ManaColorChoice.instantSorceryOnly(playerId, aisom.amount() * manaMultiplier);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null, choiceContext, colors, "Choose a color of mana to add (instant and sorcery spells only)."));
                log.info("Game {} - Awaiting {} to choose an instant/sorcery-only mana color", gameData.id, player.getUsername());
            } else if (effect instanceof AwardFlashbackOnlyAnyColorManaEffect fba) {
                ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, fba.amount() * manaMultiplier, null, true);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null, choiceContext, colors, "Choose a color of mana to add (flashback only)."));
                log.info("Game {} - Awaiting {} to choose a flashback-only mana color", gameData.id, player.getUsername());
            } else if (effect instanceof AwardRestrictedManaEffect arm) {
                arm.applyTo(gameData.playerManaPools.get(playerId));
            } else if (effect instanceof AwardManaOfColorsAmongControlledEffect manaAmong) {
                Set<CardColor> availableColors = collectColorsAmongControlled(gameData, playerId, manaAmong);
                if (availableColors.size() == 1) {
                    CardColor onlyColor = availableColors.iterator().next();
                    ManaColor manaColor = ManaColor.valueOf(onlyColor.name());
                    gameData.playerManaPools.get(playerId).add(manaColor);
                    String logEntry = player.getUsername() + " adds {" + onlyColor.getCode() + "} from " + permanent.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
                    String logEntry = player.getUsername() + " activates " + permanent.getCard().getName()
                            + " but produces no mana (no colors among legendary creatures and planeswalkers).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            } else if (effect instanceof AwardManaOfColorsLandsCouldProduceEffect landColors) {
                Set<CardColor> availableColors = collectColorsLandsCouldProduce(gameData, playerId, landColors);
                if (availableColors.size() == 1) {
                    CardColor onlyColor = availableColors.iterator().next();
                    ManaColor manaColor = ManaColor.valueOf(onlyColor.name());
                    gameData.playerManaPools.get(playerId).add(manaColor);
                    String logEntry = player.getUsername() + " adds {" + onlyColor.getCode() + "} from " + permanent.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
                    String logEntry = player.getUsername() + " activates " + permanent.getCard().getName()
                            + " but produces no mana (no matching land could produce colored mana).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            } else if (effect instanceof GainLifeEffect gain) {
                int amount = amountEvaluationService.evaluate(gameData, gain.amount(),
                        new AmountContext(playerId, permanent, null, 0, 0, false));
                lifeSupport.applyGainLife(gameData, playerId, amount);
            } else if (effect instanceof DealDamageToPlayersEffect dmg && dmg.recipient() == DamageRecipient.CONTROLLER) {
                String cardName = permanent.getCard().getName();
                int damage = amountEvaluationService.evaluate(gameData, dmg.amount(),
                        new AmountContext(playerId, permanent, null, 0, 0, false));
                if (gameQueryService.isDamagePreventable(gameData)) {
                    CardColor sourceColor = gameQueryService.getEffectiveColor(gameData, permanent);
                    if (gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)
                            || damagePreventionService.isSourceDamagePreventedForPlayer(gameData, playerId, permanent.getId())
                            || gameData.permanentsPreventedFromDealingDamage.contains(permanent.getId())
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
                    if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId)) {
                        if (gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
                            int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                            gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                            String logEntry = player.getUsername() + " gets " + effectiveDamage + " poison counters from " + cardName + ".";
                            gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        }
                    } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                        gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + "'s life total can't change.");
                    } else {
                        int currentLife = gameData.getLife(playerId);
                        gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);
                        if (effectiveDamage > 0) {
                            String logEntry = player.getUsername() + " takes " + effectiveDamage + " damage from " + cardName + ".";
                            gameBroadcastService.logAndBroadcast(gameData, logEntry);
                            log.info("Game {} - {} takes {} damage from {}", gameData.id, player.getUsername(), effectiveDamage, cardName);
                        }
                    }
                    if (effectiveDamage > 0) {
                        gameData.recordDamageToPlayer(playerId, effectiveDamage);
                    }
                }
            } else if (effect instanceof DealDamageToPlayersEffect dmg && dmg.recipient() == DamageRecipient.EACH_OPPONENT) {
                // Reflexive "When you do" rider on a mana ability, e.g. Rubble Rouser:
                // "Add {R}. When you do, this creature deals 1 damage to each opponent."
                int damage = amountEvaluationService.evaluate(gameData, dmg.amount(),
                        new AmountContext(playerId, permanent, null, 0, 0, false));
                for (UUID opponentId : gameData.orderedPlayerIds) {
                    if (opponentId.equals(playerId)) continue;
                    dealManaAbilityRiderDamageToPlayer(gameData, permanent, opponentId, damage);
                }
            }
        }
        stateBasedActionService.performStateBasedActions(gameData);
        // CR 605.3b: Do NOT clear priorityPassedBy here — mana abilities don't affect priority.
        // Priority clearing is handled by the caller when deferred triggers are pushed onto the stack.
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.SelfLeavesTriggerTarget.class)) {
            triggerCollectionService.processNextSelfLeavesTriggerTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
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
            if (gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)
                    || damagePreventionService.isSourceDamagePreventedForPlayer(gameData, playerId, permanent.getId())
                    || gameData.permanentsPreventedFromDealingDamage.contains(permanent.getId())
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
            if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId)) {
                if (gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                    gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                    gameBroadcastService.logAndBroadcast(gameData, playerName + " gets " + effectiveDamage + " poison counters from " + cardName + ".");
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            } else {
                int currentLife = gameData.getLife(playerId);
                gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);
                if (effectiveDamage > 0) {
                    gameBroadcastService.logAndBroadcast(gameData, playerName + " takes " + effectiveDamage + " damage from " + cardName + ".");
                    log.info("Game {} - {} takes {} damage from {}", gameData.id, playerName, effectiveDamage, cardName);
                }
            }
            if (effectiveDamage > 0) {
                gameData.recordDamageToPlayer(playerId, effectiveDamage);
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

    private int calculateTotalManaProduction(GameData gameData, UUID playerId, Permanent permanent, List<CardEffect> effects) {
        int total = 0;
        for (CardEffect effect : effects) {
            if (effect instanceof AwardManaEffect award) {
                total += amountEvaluationService.evaluate(gameData, award.amount(),
                        AmountContext.forManaAbility(permanent, playerId));
            } else if (effect instanceof AwardManaToChosenPlayerEffect chosen) {
                total += chosen.amount();
            } else if (effect instanceof AwardAnyColorManaEffect aace) {
                total += aace.amount();
            } else if (effect instanceof AwardManaOfColorsEffect ofColors) {
                total += ofColors.amount();
            } else if (effect instanceof AwardRestrictedManaEffect arm) {
                total += arm.amount();
            } else if (effect instanceof AwardFlashbackOnlyAnyColorManaEffect fba) {
                total += fba.amount();
            } else if (effect instanceof AwardAnyColorSubtypeSpellOrAbilityManaEffect soa) {
                total += soa.amount();
            } else if (effect instanceof AwardManaOfColorsAmongControlledEffect manaAmong) {
                Set<CardColor> colors = collectColorsAmongControlled(gameData, playerId, manaAmong);
                if (!colors.isEmpty()) {
                    total += 1;
                }
            } else if (effect instanceof AwardManaOfColorsLandsCouldProduceEffect landColors) {
                if (!collectColorsLandsCouldProduce(gameData, playerId, landColors).isEmpty()) {
                    total += 1;
                }
            } else if (effect instanceof DoubleManaPoolEffect) {
                total += gameData.playerManaPools.get(playerId).getTotal();
            }
        }
        return total;
    }

    private Set<CardColor> collectColorsAmongControlled(GameData gameData, UUID playerId,
                                                         AwardManaOfColorsAmongControlledEffect effect) {
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return colors;
        }
        for (Permanent p : battlefield) {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, p, effect.predicate())) {
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
        if (ability.isNeedsSpellTarget()) {
            effectiveTargetZone = Zone.STACK;
        }
        if (effectiveTargetZone == Zone.BATTLEFIELD) {
            effectiveTargetZone = null;
        }
        List<UUID> effectiveTargetIds = targetIds != null ? targetIds : List.of();
        // When targeting graveyard cards with multiple targets, use targetCardIds
        // (for proper fizzle checking and resolution by graveyard handlers)
        List<UUID> effectiveTargetCardIds = (effectiveTargetZone == Zone.GRAVEYARD && !effectiveTargetIds.isEmpty())
                ? effectiveTargetIds : List.of();
        List<UUID> effectivePermanentTargetIds = (effectiveTargetZone == Zone.GRAVEYARD && !effectiveTargetIds.isEmpty())
                ? List.of() : effectiveTargetIds;
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
        gameData.stack.add(stackEntry);
        triggerCollectionService.checkBecomesTargetOfAbilityTriggers(gameData);
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.priorityPassedBy.clear();
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput() && gameData.hasPendingInteraction(PermanentChoiceContext.SelfLeavesTriggerTarget.class)) {
            triggerCollectionService.processNextSelfLeavesTriggerTarget(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
    }
}


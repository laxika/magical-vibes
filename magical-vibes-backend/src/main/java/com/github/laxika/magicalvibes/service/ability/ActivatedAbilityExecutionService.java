package com.github.laxika.magicalvibes.service.ability;

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
import com.github.laxika.magicalvibes.model.effect.AddColorlessManaPerChargeCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEqualToSourcePowerEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardFlashbackOnlyAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.AwardArtifactOnlyColorlessManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsAmongControlledEffect;
import com.github.laxika.magicalvibes.model.effect.AwardKickedOnlyManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardMyrOnlyColorlessManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByChargeCountersEffect;
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
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSourceEquipmentCost;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final LifeResolutionService lifeResolutionService;

    /**
     * Completes an activated ability activation after all additional costs (mana, sacrifice creature,
     * discard, exile from graveyard, etc.) have already been paid by the caller.
     *
     * <p>This method performs the remaining activation steps in order:
     * <ol>
     *   <li>Determines the effective target — auto-targets the source permanent for self-targeting
     *       effects (e.g. {@code BoostSelfEffect}, {@code RegenerateEffect}, {@code AnimateSelfEffect}).</li>
     *   <li>Taps the permanent if the ability requires a tap cost.</li>
     *   <li>Snapshots charge counters into {@code effectiveXValue} for counter-dependent effects
     *       (e.g. {@code DrawCardsEqualToChargeCountersOnSourceEffect}) so the value survives sacrifice.</li>
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
        }

        // Snapshot charge counters before sacrifice so the value survives in the stack entry's xValue
        if (abilityEffects.stream().anyMatch(e -> e instanceof DrawCardsEqualToChargeCountersOnSourceEffect
                || e instanceof GainLifeEqualToChargeCountersOnSourceEffect
                || e instanceof MillTargetPlayerByChargeCountersEffect
                || e instanceof TargetPlayerDiscardsByChargeCountersEffect
                || e instanceof DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect
                || e instanceof DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect
                || e instanceof CreateTokensEqualToChargeCountersOnSourceEffect
                || e instanceof LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect)) {
            effectiveXValue = permanent.getChargeCounters();
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

        String logEntry = player.getUsername() + " activates " + permanent.getCard().getName() + "'s ability.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} activates {}'s ability", gameData.id, player.getUsername(), permanent.getCard().getName());

        List<CardEffect> snapshotEffects = snapshotEffects(abilityEffects, permanent);
        // CR 605.1a: A mana ability doesn't require a target, could add mana, and isn't a loyalty ability.
        // Pain lands (e.g. Adarkar Wastes) include DealDamageToControllerEffect alongside mana production
        // and are still mana abilities — they resolve immediately without using the stack.
        boolean isManaAbility = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget()
                && ability.getLoyaltyCost() == null
                && !snapshotEffects.isEmpty()
                && snapshotEffects.stream().anyMatch(e -> e instanceof ManaProducingEffect);

        if (isManaAbility) {
            resolveManaAbility(gameData, playerId, player, permanent, snapshotEffects);
            // Mana resolves immediately, then deferred triggers go on the stack.
            // CR 605.3b: Activating a mana ability does not change who has priority.
            // Only clear priority when deferred triggers are pushed onto the stack,
            // since those require both players to pass priority again.
            if (!deferredTapTriggers.isEmpty() || !deferredCostTriggers.isEmpty()) {
                gameData.stack.addAll(deferredTapTriggers);
                gameData.stack.addAll(deferredCostTriggers);
                gameData.priorityPassedBy.clear();
            }
            return;
        }

        pushAbilityOnStack(gameData, playerId, permanent, ability, snapshotEffects, effectiveXValue, effectiveTargetId, targetZone, targetIds, damageAssignments);
        if (markAsNonTargetingForSacCreatureCost && !gameData.stack.isEmpty()) {
            gameData.stack.getLast().setNonTargeting(true);
        }
        // Set the damage source card for equipment-granted abilities (e.g. Blazing Torch)
        // Per MTG rulings: "The source of the damage is Blazing Torch, not the equipped creature."
        if (sacrificedEquipmentCard != null && !gameData.stack.isEmpty()) {
            gameData.stack.getLast().setDamageSourceCard(sacrificedEquipmentCard);
        }
        // Add "becomes tapped" triggers ON TOP of the ability so they resolve first (per CR rules)
        gameData.stack.addAll(deferredTapTriggers);
        // Add death triggers from sacrifice/exile ON TOP so they resolve first (per CR 603.3)
        gameData.stack.addAll(deferredCostTriggers);

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
        boolean isCreatureSource = gameQueryService.isCreature(gameData, permanent);

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
                gameData.playerManaPools.get(playerId).add(award.color(), award.amount());
                if (isCreatureSource) {
                    gameData.playerManaPools.get(playerId).addCreatureMana(award.color(), award.amount());
                }
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
                    ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, false, 1, chosenSubtype);
                    gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);
                    List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                    sessionManager.sendToPlayer(playerId, new ChooseFromListMessage(colors, "Choose a color of mana to add."));
                    log.info("Game {} - Awaiting {} to choose a mana color (restricted to {} creatures)", gameData.id, player.getUsername(), chosenSubtype);
                }
            } else if (effect instanceof AwardAnyColorManaWithInstantSorceryCopyEffect aacse) {
                ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, aacse.amount());
                gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                sessionManager.sendToPlayer(playerId, new ChooseFromListMessage(colors, "Choose a color of mana to add."));
                // Register delayed trigger: copy next instant/sorcery spell cast with this mana
                gameData.pendingNextInstantSorceryCopyCount.merge(playerId, 1, Integer::sum);
                log.info("Game {} - Awaiting {} to choose a mana color (with spell copy trigger)", gameData.id, player.getUsername());
            } else if (effect instanceof AwardAnyColorManaEffect aace) {
                ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, aace.amount());
                gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                sessionManager.sendToPlayer(playerId, new ChooseFromListMessage(colors, "Choose a color of mana to add."));
                log.info("Game {} - Awaiting {} to choose a mana color", gameData.id, player.getUsername());
            } else if (effect instanceof AwardFlashbackOnlyAnyColorManaEffect fba) {
                ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(playerId, isCreatureSource, fba.amount(), null, true);
                gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                sessionManager.sendToPlayer(playerId, new ChooseFromListMessage(colors, "Choose a color of mana to add (flashback only)."));
                log.info("Game {} - Awaiting {} to choose a flashback-only mana color", gameData.id, player.getUsername());
            } else if (effect instanceof AwardArtifactOnlyColorlessManaEffect aom) {
                gameData.playerManaPools.get(playerId).addArtifactOnlyColorless(aom.amount());
            } else if (effect instanceof AwardMyrOnlyColorlessManaEffect mom) {
                gameData.playerManaPools.get(playerId).addMyrOnlyColorless(mom.amount());
            } else if (effect instanceof AwardKickedOnlyManaEffect kom) {
                gameData.playerManaPools.get(playerId).addKickedOnlyGreen(kom.amount());
            } else if (effect instanceof AwardRestrictedManaEffect arm) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                if (arm.color() == ManaColor.RED) {
                    pool.addRestrictedRed(arm.amount());
                } else if (arm.color() == ManaColor.COLORLESS) {
                    pool.addInstantSorceryOnlyColorless(arm.amount());
                } else {
                    pool.add(arm.color(), arm.amount());
                }
            } else if (effect instanceof AddManaPerControlledPermanentEffect manaPerPermanent) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                int count = 0;
                FilterContext filterContext = FilterContext.of(gameData).withSourceCardId(permanent.getCard().getId());
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (gameQueryService.matchesPermanentPredicate(p, manaPerPermanent.predicate(), filterContext)) {
                            count++;
                        }
                    }
                }
                ManaPool pool = gameData.playerManaPools.get(playerId);
                for (int i = 0; i < count; i++) {
                    pool.add(manaPerPermanent.color());
                    if (isCreatureSource) {
                        pool.addCreatureMana(manaPerPermanent.color(), 1);
                    }
                }
                String logEntry = player.getUsername() + " adds " + count + " " + manaPerPermanent.color().getCode()
                        + " (" + manaPerPermanent.description() + " controlled).";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
                    gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);
                    List<String> colors = availableColors.stream()
                            .map(Enum::name)
                            .sorted()
                            .toList();
                    sessionManager.sendToPlayer(playerId, new ChooseFromListMessage(colors, "Choose a color of mana to add."));
                    log.info("Game {} - Awaiting {} to choose a mana color from legendary colors", gameData.id, player.getUsername());
                } else {
                    String logEntry = player.getUsername() + " activates " + permanent.getCard().getName()
                            + " but produces no mana (no colors among legendary creatures and planeswalkers).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            } else if (effect instanceof AddColorlessManaPerChargeCounterOnSourceEffect) {
                int count = permanent.getChargeCounters();
                if (count > 0) {
                    gameData.playerManaPools.get(playerId).add(ManaColor.COLORLESS, count);
                    String logEntry = player.getUsername() + " adds " + count + " {C} from " + permanent.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            } else if (effect instanceof AwardManaEqualToSourcePowerEffect powerMana) {
                int power = gameQueryService.getEffectivePower(gameData, permanent);
                if (power > 0) {
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    pool.add(powerMana.color(), power);
                    if (isCreatureSource) {
                        pool.addCreatureMana(powerMana.color(), power);
                    }
                    String logEntry = player.getUsername() + " adds " + power + " " + powerMana.color().getCode()
                            + " from " + permanent.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            } else if (effect instanceof GainLifeEffect gain) {
                lifeResolutionService.applyGainLife(gameData, playerId, gain.amount());
            } else if (effect instanceof DealDamageToControllerEffect dmg) {
                String cardName = permanent.getCard().getName();
                int damage = dmg.damage();
                if (!gameQueryService.isDamagePreventable(gameData)
                        || (!gameQueryService.isDamageFromSourcePrevented(gameData, permanent.getEffectiveColor())
                            && !damagePreventionService.isSourceDamagePreventedForPlayer(gameData, playerId, permanent.getId())
                            && !gameData.permanentsPreventedFromDealingDamage.contains(permanent.getId())
                            && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, playerId, permanent.getEffectiveColor()))) {
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
                        gameData.playersDealtDamageThisTurn.add(playerId);
                    }
                }
            }
        }
        stateBasedActionService.performStateBasedActions(gameData);
        // CR 605.3b: Do NOT clear priorityPassedBy here — mana abilities don't affect priority.
        // Priority clearing is handled by the caller when deferred triggers are pushed onto the stack.
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingDeathTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
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
                total += award.amount();
            } else if (effect instanceof AwardAnyColorManaEffect aace) {
                total += aace.amount();
            } else if (effect instanceof AwardArtifactOnlyColorlessManaEffect aom) {
                total += aom.amount();
            } else if (effect instanceof AwardMyrOnlyColorlessManaEffect mom) {
                total += mom.amount();
            } else if (effect instanceof AwardKickedOnlyManaEffect kom) {
                total += kom.amount();
            } else if (effect instanceof AwardFlashbackOnlyAnyColorManaEffect fba) {
                total += fba.amount();
            } else if (effect instanceof AddManaPerControlledPermanentEffect manaPerPermanent) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                FilterContext filterContext = FilterContext.of(gameData).withSourceCardId(permanent.getCard().getId());
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (gameQueryService.matchesPermanentPredicate(p, manaPerPermanent.predicate(), filterContext)) {
                            total++;
                        }
                    }
                }
            } else if (effect instanceof AwardManaOfColorsAmongControlledEffect manaAmong) {
                Set<CardColor> colors = collectColorsAmongControlled(gameData, playerId, manaAmong);
                if (!colors.isEmpty()) {
                    total += 1;
                }
            } else if (effect instanceof AddColorlessManaPerChargeCounterOnSourceEffect) {
                total += permanent.getChargeCounters();
            } else if (effect instanceof AwardManaEqualToSourcePowerEffect) {
                total += gameQueryService.getEffectivePower(gameData, permanent);
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
            if (!gameQueryService.matchesPermanentPredicate(gameData, p, effect.predicate())) {
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
        gameData.stack.add(stackEntry);
        triggerCollectionService.checkBecomesTargetOfAbilityTriggers(gameData);
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.priorityPassedBy.clear();
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingDeathTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
    }
}


package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardArtifactOnlyColorlessManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardMyrOnlyColorlessManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DoubleManaPoolEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivatedAbilityExecutionService {

    private final GameHelper gameHelper;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final StateBasedActionService stateBasedActionService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;

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
     * @param targetPermanentId                 the chosen target permanent, or {@code null} if none
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
                                             UUID targetPermanentId,
                                             Zone targetZone,
                                             boolean markAsNonTargetingForSacCreatureCost) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            throw new IllegalStateException("Invalid battlefield");
        }

        UUID effectiveTargetId = targetPermanentId;
        if (effectiveTargetId == null) {
            boolean needsSelfTarget = abilityEffects.stream().anyMatch(e ->
                    (e instanceof RegenerateEffect regen && !regen.targetsPermanent() && !permanent.getCard().isAura())
                            || e instanceof BoostSelfEffect || e instanceof UntapSelfEffect
                            || e instanceof AnimateSelfEffect || e instanceof AnimateSelfByChargeCountersEffect
                            || e instanceof AnimateSelfWithStatsEffect
                            || e instanceof AnimateLandEffect || e instanceof PutChargeCounterOnSelfEffect
                            || (e instanceof GrantKeywordEffect grant && grant.scope() == GrantScope.SELF));
            if (needsSelfTarget) {
                effectiveTargetId = permanent.getId();
            }
        }

        if (ability.isRequiresTap()) {
            permanent.tap();
        }

        // Snapshot charge counters before sacrifice so the value survives in the stack entry's xValue
        if (abilityEffects.stream().anyMatch(e -> e instanceof DrawCardsEqualToChargeCountersOnSourceEffect
                || e instanceof GainLifeEqualToChargeCountersOnSourceEffect
                || e instanceof MillTargetPlayerByChargeCountersEffect)) {
            effectiveXValue = permanent.getChargeCounters();
        }

        boolean shouldSacrifice = abilityEffects.stream().anyMatch(e -> e instanceof SacrificeSelfCost);
        if (shouldSacrifice) {
            boolean wasCreature = gameQueryService.isCreature(gameData, permanent);
            battlefield.remove(permanent);
            gameHelper.addCardToGraveyard(gameData, playerId, permanent.getCard(), Zone.BATTLEFIELD);
            gameHelper.collectDeathTrigger(gameData, permanent.getCard(), playerId, wasCreature);
            if (wasCreature) {
                gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
            }
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
                && snapshotEffects.stream().anyMatch(e ->
                e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect
                        || e instanceof DoubleManaPoolEffect || e instanceof AwardArtifactOnlyColorlessManaEffect
                        || e instanceof AwardMyrOnlyColorlessManaEffect)
                && snapshotEffects.stream().allMatch(e ->
                e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect
                        || e instanceof DoubleManaPoolEffect || e instanceof DealDamageToControllerEffect
                        || e instanceof AwardArtifactOnlyColorlessManaEffect
                        || e instanceof AwardMyrOnlyColorlessManaEffect);

        if (isManaAbility) {
            resolveManaAbility(gameData, playerId, player, permanent, snapshotEffects);
            return;
        }

        pushAbilityOnStack(gameData, playerId, permanent, ability, snapshotEffects, effectiveXValue, effectiveTargetId, targetZone);
        if (markAsNonTargetingForSacCreatureCost && !gameData.stack.isEmpty()) {
            gameData.stack.getLast().setNonTargeting(true);
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    private List<CardEffect> snapshotEffects(List<CardEffect> abilityEffects, Permanent permanent) {
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect effect : abilityEffects) {
            if (effect instanceof SacrificeSelfCost
                    || effect instanceof SacrificeCreatureCost
                    || effect instanceof SacrificeSubtypeCreatureCost
                    || effect instanceof SacrificeArtifactCost
                    || effect instanceof SacrificeMultiplePermanentsCost
                    || effect instanceof DiscardCardTypeCost
                    || effect instanceof ExileCardFromGraveyardCost
                    || effect instanceof RemoveCounterFromSourceCost
                    || effect instanceof RemoveChargeCountersFromSourceCost
                    || effect instanceof TapCreatureCost) {
                continue;
            }
            if (effect instanceof CantBlockSourceEffect) {
                snapshotEffects.add(new CantBlockSourceEffect(permanent.getId()));
            } else if (effect instanceof PreventNextColorDamageToControllerEffect && permanent.getChosenColor() != null) {
                snapshotEffects.add(new PreventNextColorDamageToControllerEffect(permanent.getChosenColor()));
            } else {
                snapshotEffects.add(effect);
            }
        }
        return snapshotEffects;
    }

    private void resolveManaAbility(GameData gameData, UUID playerId, Player player, Permanent permanent, List<CardEffect> snapshotEffects) {
        for (CardEffect effect : snapshotEffects) {
            if (effect instanceof AwardManaEffect award) {
                gameData.playerManaPools.get(playerId).add(award.color());
            } else if (effect instanceof DoubleManaPoolEffect) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                for (ManaColor color : ManaColor.values()) {
                    int current = pool.get(color);
                    for (int i = 0; i < current; i++) {
                        pool.add(color);
                    }
                }
            } else if (effect instanceof AwardAnyColorManaEffect) {
                ColorChoiceContext.ManaColorChoice choiceContext = new ColorChoiceContext.ManaColorChoice(playerId);
                gameData.interaction.beginColorChoice(playerId, null, null, choiceContext);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                sessionManager.sendToPlayer(playerId, new ChooseColorMessage(colors, "Choose a color of mana to add."));
                log.info("Game {} - Awaiting {} to choose a mana color", gameData.id, player.getUsername());
            } else if (effect instanceof AwardArtifactOnlyColorlessManaEffect aom) {
                gameData.playerManaPools.get(playerId).addArtifactOnlyColorless(aom.amount());
            } else if (effect instanceof AwardMyrOnlyColorlessManaEffect mom) {
                gameData.playerManaPools.get(playerId).addMyrOnlyColorless(mom.amount());
            } else if (effect instanceof DealDamageToControllerEffect dmg) {
                String cardName = permanent.getCard().getName();
                int damage = dmg.damage();
                if (!gameQueryService.isDamageFromSourcePrevented(gameData, permanent.getEffectiveColor())
                        && !gameHelper.isSourceDamagePreventedForPlayer(gameData, playerId, permanent.getId())
                        && !gameHelper.applyColorDamagePreventionForPlayer(gameData, playerId, permanent.getEffectiveColor())) {
                    int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, playerId, damage);
                    effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);
                    int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
                    gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);
                    if (effectiveDamage > 0) {
                        String logEntry = player.getUsername() + " takes " + effectiveDamage + " damage from " + cardName + ".";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} takes {} damage from {}", gameData.id, player.getUsername(), effectiveDamage, cardName);
                    }
                }
            }
        }
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.priorityPassedBy.clear();
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingDeathTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
    }

    private void pushAbilityOnStack(GameData gameData,
                                    UUID playerId,
                                    Permanent permanent,
                                    ActivatedAbility ability,
                                    List<CardEffect> snapshotEffects,
                                    int effectiveXValue,
                                    UUID effectiveTargetId,
                                    Zone targetZone) {
        Zone effectiveTargetZone = targetZone;
        StackEntry stackEntry;
        if (ability.isNeedsSpellTarget()) {
            effectiveTargetZone = Zone.STACK;
        }
        if (effectiveTargetZone != null && effectiveTargetZone != Zone.BATTLEFIELD) {
            stackEntry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY,
                    permanent.getCard(),
                    playerId,
                    permanent.getCard().getName() + "'s ability",
                    snapshotEffects,
                    effectiveXValue,
                    effectiveTargetId,
                    permanent.getId(),
                    Map.of(),
                    effectiveTargetZone,
                    List.of(),
                    List.of()
            );
        } else {
            stackEntry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY,
                    permanent.getCard(),
                    playerId,
                    permanent.getCard().getName() + "'s ability",
                    snapshotEffects,
                    effectiveXValue,
                    effectiveTargetId,
                    permanent.getId(),
                    Map.of(),
                    null,
                    List.of(),
                    List.of()
            );
        }
        stackEntry.setTargetFilter(ability.getTargetFilter());
        gameData.stack.add(stackEntry);
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.priorityPassedBy.clear();
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingDeathTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
    }
}


package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetZone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleManaPoolEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.ControllerOnlyTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CreatureYouControlTargetFilter;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
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
public class AbilityActivationService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TargetValidationService targetValidationService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;

    public void tapPermanent(GameData gameData, Player player, int permanentIndex) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
            throw new IllegalStateException("Invalid permanent index");
        }

        Permanent permanent = battlefield.get(permanentIndex);
        if (permanent.isTapped()) {
            throw new IllegalStateException("Permanent is already tapped");
        }
        if (permanent.getCard().getEffects(EffectSlot.ON_TAP).isEmpty()) {
            throw new IllegalStateException("Permanent has no tap effects");
        }
        if (permanent.isSummoningSick() && gameQueryService.isCreature(gameData, permanent) && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)) {
            throw new IllegalStateException("Creature has summoning sickness");
        }

        permanent.tap();

        ManaPool manaPool = gameData.playerManaPools.get(playerId);
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_TAP)) {
            if (effect instanceof AwardManaEffect awardMana) {
                manaPool.add(awardMana.color());
            }
        }

        String logEntry = player.getUsername() + " taps " + permanent.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} taps {}", gameData.id, player.getUsername(), permanent.getCard().getName());

        gameBroadcastService.broadcastGameState(gameData);
    }

    public void sacrificePermanent(GameData gameData, Player player, int permanentIndex, UUID targetPermanentId) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
            throw new IllegalStateException("Invalid permanent index");
        }

        Permanent permanent = battlefield.get(permanentIndex);
        if (permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE).isEmpty()) {
            throw new IllegalStateException("Permanent has no sacrifice abilities");
        }

        // Validate target for effects that need one
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE)) {
            if (effect instanceof DestroyTargetPermanentEffect destroy) {
                if (targetPermanentId == null) {
                    throw new IllegalStateException("Sacrifice ability requires a target");
                }
                Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
                if (target == null) {
                    throw new IllegalStateException("Invalid target permanent");
                }
                if (!destroy.targetTypes().contains(target.getCard().getType())) {
                    throw new IllegalStateException("Invalid target type for sacrifice ability");
                }
                if (gameQueryService.hasProtectionFrom(gameData, target, permanent.getCard().getColor())) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from " + permanent.getCard().getColor().name().toLowerCase());
                }
            }
        }

        // Sacrifice: remove from battlefield, add to graveyard
        boolean wasCreature = gameQueryService.isCreature(gameData, permanent);
        battlefield.remove(permanentIndex);
        gameHelper.addCardToGraveyard(gameData, playerId, permanent.getOriginalCard());
        gameHelper.collectDeathTrigger(gameData, permanent.getCard(), playerId, wasCreature);
        if (wasCreature) {
            gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
        }
        gameHelper.removeOrphanedAuras(gameData);

        String logEntry = player.getUsername() + " sacrifices " + permanent.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, player.getUsername(), permanent.getCard().getName());

        // Put activated ability on stack
        gameData.stack.add(new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                permanent.getCard(),
                playerId,
                permanent.getCard().getName() + "'s ability",
                new ArrayList<>(permanent.getCard().getEffects(EffectSlot.ON_SACRIFICE)),
                0,
                targetPermanentId,
                Map.of()
        ));
        gameData.priorityPassedBy.clear();

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetPermanentId, TargetZone targetZone) {
        int effectiveXValue = xValue != null ? xValue : 0;

        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
            throw new IllegalStateException("Invalid permanent index");
        }

        Permanent permanent = battlefield.get(permanentIndex);
        List<ActivatedAbility> ownAbilities = permanent.getCard().getActivatedAbilities();
        List<ActivatedAbility> grantedAbilities = gameQueryService.computeStaticBonus(gameData, permanent).grantedActivatedAbilities();
        List<ActivatedAbility> abilities = new ArrayList<>(ownAbilities);
        abilities.addAll(grantedAbilities);
        if (abilities.isEmpty()) {
            throw new IllegalStateException("Permanent has no activated ability");
        }

        int effectiveIndex = abilityIndex != null ? abilityIndex : 0;
        if (effectiveIndex < 0 || effectiveIndex >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }

        ActivatedAbility ability = abilities.get(effectiveIndex);
        List<CardEffect> abilityEffects = ability.getEffects();
        String abilityCost = ability.getManaCost();
        boolean isTapAbility = ability.isRequiresTap();

        // Validate activation timing restrictions (e.g. "Activate only during your upkeep")
        validateTimingRestrictions(gameData, playerId, ability);

        // Validate loyalty ability restrictions
        if (ability.getLoyaltyCost() != null) {
            validateAndPayLoyaltyCost(gameData, playerId, permanent, ability);
        }

        // Validate tap requirement
        if (isTapAbility) {
            if (permanent.isTapped()) {
                throw new IllegalStateException("Permanent is already tapped");
            }
            if (permanent.isSummoningSick() && gameQueryService.isCreature(gameData, permanent) && !gameQueryService.hasKeyword(gameData, permanent, Keyword.HASTE)) {
                throw new IllegalStateException("Creature has summoning sickness");
            }
        }

        // Validate spell target for abilities that counter spells
        if (ability.isNeedsSpellTarget()) {
            validateSpellTarget(gameData, targetPermanentId);
        }

        // Pay mana cost
        if (abilityCost != null) {
            payManaCost(gameData, playerId, abilityCost, effectiveXValue);
        }

        // Validate target for effects that need one
        targetValidationService.validateEffectTargets(abilityEffects,
                new TargetValidationContext(gameData, targetPermanentId, targetZone, permanent.getCard()));

        // Generic target filter validation
        if (ability.getTargetFilter() != null && targetPermanentId != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
            if (target != null) {
                gameQueryService.validateTargetFilter(ability.getTargetFilter(), target);

                // Controller ownership validation
                if (ability.getTargetFilter() instanceof ControllerOnlyTargetFilter
                        || ability.getTargetFilter() instanceof CreatureYouControlTargetFilter) {
                    List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
                    if (playerBf == null || !playerBf.contains(target)) {
                        throw new IllegalStateException("Target must be a permanent you control");
                    }
                }

                // Creature type validation for equip and similar effects
                if (ability.getTargetFilter() instanceof CreatureYouControlTargetFilter) {
                    if (!gameQueryService.isCreature(gameData, target)) {
                        throw new IllegalStateException("Target must be a creature you control");
                    }
                }
            }
        }

        // Creature shroud validation for abilities
        if (targetPermanentId != null) {
            Permanent shroudTarget = gameQueryService.findPermanentById(gameData, targetPermanentId);
            if (shroudTarget != null && gameQueryService.hasKeyword(gameData, shroudTarget, Keyword.SHROUD)) {
                throw new IllegalStateException(shroudTarget.getCard().getName() + " has shroud and can't be targeted");
            }
        }

        // Player shroud validation for abilities
        if (targetPermanentId != null && gameData.playerIds.contains(targetPermanentId)
                && gameQueryService.playerHasShroud(gameData, targetPermanentId)) {
            throw new IllegalStateException(gameData.playerIdToName.get(targetPermanentId) + " has shroud and can't be targeted");
        }

        // Self-target if effects need the source permanent
        UUID effectiveTargetId = targetPermanentId;
        if (effectiveTargetId == null) {
            boolean needsSelfTarget = abilityEffects.stream().anyMatch(e ->
                    e instanceof RegenerateEffect || e instanceof BoostSelfEffect || e instanceof UntapSelfEffect
                            || e instanceof AnimateSelfEffect || e instanceof GrantKeywordToSelfEffect);
            if (needsSelfTarget) {
                effectiveTargetId = permanent.getId();
            }
        }

        // Tap the permanent (only for tap abilities)
        if (isTapAbility) {
            permanent.tap();
        }

        // Sacrifice the permanent (for sacrifice-as-cost abilities)
        boolean shouldSacrifice = abilityEffects.stream().anyMatch(e -> e instanceof SacrificeSelfCost);
        if (shouldSacrifice) {
            boolean wasCreature = gameQueryService.isCreature(gameData, permanent);
            battlefield.remove(permanent);
            gameHelper.addCardToGraveyard(gameData, playerId, permanent.getCard());
            gameHelper.collectDeathTrigger(gameData, permanent.getCard(), playerId, wasCreature);
            if (wasCreature) {
                gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
            }
        }

        String logEntry = player.getUsername() + " activates " + permanent.getCard().getName() + "'s ability.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} activates {}'s ability", gameData.id, player.getUsername(), permanent.getCard().getName());

        // Snapshot permanent state into effects so the ability resolves independently of its source
        // Filter out SacrificeSelfCost since it's already been paid as a cost
        List<CardEffect> snapshotEffects = snapshotEffects(abilityEffects, permanent);

        // Check if this is a mana ability (CR 605.1a: doesn't target, could produce mana, not loyalty)
        // Mana abilities resolve immediately without using the stack (CR 605.3a)
        boolean isManaAbility = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget()
                && ability.getLoyaltyCost() == null
                && !snapshotEffects.isEmpty()
                && snapshotEffects.stream().allMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect || e instanceof DoubleManaPoolEffect);

        if (isManaAbility) {
            resolveManaAbility(gameData, playerId, player, snapshotEffects);
        } else {
            pushAbilityOnStack(gameData, playerId, permanent, ability, snapshotEffects, effectiveXValue, effectiveTargetId, targetZone);
        }
    }

    private void validateTimingRestrictions(GameData gameData, UUID playerId, ActivatedAbility ability) {
        if (ability.getTimingRestriction() != null) {
            if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated during your upkeep");
                }
                if (gameData.currentStep != TurnStep.UPKEEP) {
                    throw new IllegalStateException("This ability can only be activated during your upkeep");
                }
            }
            if (ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("This ability can only be activated at sorcery speed");
                }
                if (gameData.currentStep != TurnStep.PRECOMBAT_MAIN && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN) {
                    throw new IllegalStateException("This ability can only be activated during a main phase");
                }
                if (!gameData.stack.isEmpty()) {
                    throw new IllegalStateException("This ability can only be activated when the stack is empty");
                }
            }
        }
    }

    private void validateAndPayLoyaltyCost(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability) {
        // Sorcery-speed timing: must be active player, main phase, stack empty
        if (!playerId.equals(gameData.activePlayerId)) {
            throw new IllegalStateException("Loyalty abilities can only be activated on your turn");
        }
        if (gameData.currentStep != TurnStep.PRECOMBAT_MAIN && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN) {
            throw new IllegalStateException("Loyalty abilities can only be activated during a main phase");
        }
        if (!gameData.stack.isEmpty()) {
            throw new IllegalStateException("Loyalty abilities can only be activated when the stack is empty");
        }
        // Once per turn
        if (permanent.isLoyaltyAbilityUsedThisTurn()) {
            throw new IllegalStateException("Only one loyalty ability per planeswalker per turn");
        }
        // For negative loyalty costs, check sufficient loyalty
        int loyaltyCost = ability.getLoyaltyCost();
        if (loyaltyCost < 0 && permanent.getLoyaltyCounters() < Math.abs(loyaltyCost)) {
            throw new IllegalStateException("Not enough loyalty counters");
        }
        // Pay loyalty cost
        permanent.setLoyaltyCounters(permanent.getLoyaltyCounters() + loyaltyCost);
        permanent.setLoyaltyAbilityUsedThisTurn(true);
    }

    private void validateSpellTarget(GameData gameData, UUID targetPermanentId) {
        if (targetPermanentId == null) {
            throw new IllegalStateException("Ability requires a spell target");
        }
        boolean foundSpellOnStack = gameData.stack.stream()
                .anyMatch(se -> se.getCard().getId().equals(targetPermanentId)
                        && se.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                        && se.getEntryType() != StackEntryType.ACTIVATED_ABILITY);
        if (!foundSpellOnStack) {
            throw new IllegalStateException("Target must be a spell on the stack");
        }
    }

    private void payManaCost(GameData gameData, UUID playerId, String abilityCost, int effectiveXValue) {
        ManaCost cost = new ManaCost(abilityCost);
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (cost.hasX()) {
            if (effectiveXValue < 0) {
                throw new IllegalStateException("X value cannot be negative");
            }
            if (!cost.canPay(pool, effectiveXValue)) {
                throw new IllegalStateException("Not enough mana to activate ability");
            }
            cost.pay(pool, effectiveXValue);
        } else {
            if (!cost.canPay(pool)) {
                throw new IllegalStateException("Not enough mana to activate ability");
            }
            cost.pay(pool);
        }
    }

    private List<CardEffect> snapshotEffects(List<CardEffect> abilityEffects, Permanent permanent) {
        List<CardEffect> snapshotEffects = new ArrayList<>();
        for (CardEffect effect : abilityEffects) {
            if (effect instanceof SacrificeSelfCost) {
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

    private void resolveManaAbility(GameData gameData, UUID playerId, Player player, List<CardEffect> snapshotEffects) {
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
                gameData.colorChoiceContext = new ColorChoiceContext.ManaColorChoice(playerId);
                gameData.awaitingInput = AwaitingInput.COLOR_CHOICE;
                gameData.awaitingColorChoicePlayerId = playerId;
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                sessionManager.sendToPlayer(playerId, new ChooseColorMessage(colors, "Choose a color of mana to add."));
                log.info("Game {} - Awaiting {} to choose a mana color", gameData.id, player.getUsername());
            }
        }
        gameHelper.performStateBasedActions(gameData);
        gameData.priorityPassedBy.clear();
        if (gameData.awaitingInput == null && !gameData.pendingDeathTriggerTargets.isEmpty()) {
            gameHelper.processNextDeathTriggerTarget(gameData);
        }
        if (gameData.awaitingInput == null && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
    }

    private void pushAbilityOnStack(GameData gameData, UUID playerId, Permanent permanent, ActivatedAbility ability,
                                     List<CardEffect> snapshotEffects, int effectiveXValue, UUID effectiveTargetId, TargetZone targetZone) {
        TargetZone effectiveTargetZone = targetZone;
        if (ability.isNeedsSpellTarget()) {
            effectiveTargetZone = TargetZone.STACK;
        }
        if (effectiveTargetZone != null && effectiveTargetZone != TargetZone.BATTLEFIELD) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY,
                    permanent.getCard(),
                    playerId,
                    permanent.getCard().getName() + "'s ability",
                    snapshotEffects,
                    effectiveTargetId,
                    effectiveTargetZone
            ));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY,
                    permanent.getCard(),
                    playerId,
                    permanent.getCard().getName() + "'s ability",
                    snapshotEffects,
                    effectiveXValue,
                    effectiveTargetId,
                    Map.of()
            ));
        }
        gameHelper.performStateBasedActions(gameData);
        gameData.priorityPassedBy.clear();
        if (gameData.awaitingInput == null && !gameData.pendingDeathTriggerTargets.isEmpty()) {
            gameHelper.processNextDeathTriggerTarget(gameData);
        }
        gameBroadcastService.broadcastGameState(gameData);
    }
}

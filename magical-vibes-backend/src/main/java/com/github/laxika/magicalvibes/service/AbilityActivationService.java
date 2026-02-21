package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingAbilityActivation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbilityActivationService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TargetLegalityService targetLegalityService;
    private final ActivatedAbilityExecutionService activatedAbilityExecutionService;
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

        // Check for "whenever a player taps a land for mana" triggers (e.g. Manabarbs)
        if (permanent.getCard().getType() == CardType.LAND) {
            gameHelper.checkLandTapTriggers(gameData, playerId, permanent.getId());
        }

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
        gameHelper.addCardToGraveyard(gameData, playerId, permanent.getOriginalCard(), Zone.BATTLEFIELD);
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

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetPermanentId, Zone targetZone) {
        activateAbilityInternal(gameData, player, permanentIndex, abilityIndex, xValue, targetPermanentId, targetZone, null);
    }

    public void handleActivatedAbilityDiscardCostChosen(GameData gameData, Player player, int cardIndex) {
        InteractionContext.CardChoice cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null || !player.getId().equals(cardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (gameData.pendingAbilityActivation == null) {
            throw new IllegalStateException("No pending ability activation");
        }
        if (cardChoice.validIndices() == null || !cardChoice.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        PendingAbilityActivation pending = gameData.pendingAbilityActivation;
        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        if (source == null) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer on the battlefield");
        }

        int permanentIndex = gameData.playerBattlefields.get(player.getId()).indexOf(source);
        if (permanentIndex < 0) {
            clearPendingAbilityActivation(gameData);
            throw new IllegalStateException("Source permanent is no longer under your control");
        }

        clearPendingAbilityActivation(gameData);
        activateAbilityInternal(
                gameData,
                player,
                permanentIndex,
                pending.abilityIndex(),
                pending.xValue(),
                pending.targetPermanentId(),
                pending.targetZone(),
                cardIndex
        );
    }

    private void activateAbilityInternal(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue,
                                         UUID targetPermanentId, Zone targetZone, Integer discardCardIndex) {
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
        validateActivationLimitPerTurn(gameData, permanent, ability, effectiveIndex);

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
            targetLegalityService.validateSpellTargetOnStack(gameData, targetPermanentId, ability.getTargetFilter());
        }

        boolean hasSacCreatureCost = abilityEffects.stream().anyMatch(e -> e instanceof SacrificeCreatureCost);
        Optional<SacrificeSubtypeCreatureCost> sacSubtypeCost = abilityEffects.stream()
                .filter(SacrificeSubtypeCreatureCost.class::isInstance)
                .map(SacrificeSubtypeCreatureCost.class::cast)
                .findFirst();

        // For regular targeting abilities, validate legality before costs are paid (CR 602.2b/601.2c).
        if (!hasSacCreatureCost) {
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, abilityEffects, targetPermanentId, targetZone, permanent.getCard());
        }
        if (sacSubtypeCost.isPresent() && !hasSubtypeCreatureToSacrifice(gameData, playerId, sacSubtypeCost.get())) {
            throw new IllegalStateException("Must choose a " + sacSubtypeCost.get().subtype().getDisplayName() + " to sacrifice");
        }

        DiscardCardTypeCost discardCardTypeCost = abilityEffects.stream()
                .filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast)
                .findFirst()
                .orElse(null);
        if (discardCardTypeCost != null) {
            List<Card> hand = gameData.playerHands.get(playerId);
            List<Integer> validDiscardIndices = collectDiscardIndicesForType(hand, discardCardTypeCost.requiredType());
            if (validDiscardIndices.isEmpty()) {
                throw new IllegalStateException("Must discard a " + discardCardTypeCost.requiredType().name().toLowerCase() + " card to activate ability");
            }
            if (discardCardIndex == null) {
                beginDiscardCostChoice(gameData, playerId, permanent, effectiveIndex, effectiveXValue, targetPermanentId, targetZone,
                        discardCardTypeCost.requiredType(), validDiscardIndices);
                return;
            }
        }

        // Pay mana cost
        if (abilityCost != null) {
            payManaCost(gameData, playerId, abilityCost, effectiveXValue);
        }

        if (discardCardTypeCost != null) {
            payDiscardCost(gameData, player, discardCardTypeCost.requiredType(), discardCardIndex);
        }
        if (sacSubtypeCost.isPresent()) {
            if (handleSubtypeSacrificeCostSelection(gameData, player, permanent, effectiveIndex, effectiveXValue,
                    targetPermanentId, targetZone, sacSubtypeCost.get())) {
                return;
            }
        }

        // Handle sacrifice-a-creature cost (e.g. Nantuko Husk: "Sacrifice a creature: ...")
        if (hasSacCreatureCost) {
            if (targetPermanentId == null) {
                throw new IllegalStateException("Must choose a creature to sacrifice");
            }
            Permanent sacTarget = gameQueryService.findPermanentById(gameData, targetPermanentId);
            if (sacTarget == null) {
                throw new IllegalStateException("Invalid sacrifice target");
            }
            if (!gameQueryService.isCreature(gameData, sacTarget)) {
                throw new IllegalStateException("Must sacrifice a creature");
            }
            List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
            if (playerBf == null || !playerBf.contains(sacTarget)) {
                throw new IllegalStateException("Must sacrifice a creature you control");
            }

            // Sacrifice the creature as cost
            playerBf.remove(sacTarget);
            gameHelper.addCardToGraveyard(gameData, playerId, sacTarget.getCard(), Zone.BATTLEFIELD);
            gameHelper.collectDeathTrigger(gameData, sacTarget.getCard(), playerId, true);
            gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);

            String sacLog = player.getUsername() + " sacrifices " + sacTarget.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, sacLog);

            // Clear targetPermanentId so it's not used for effect targeting
            targetPermanentId = null;
        }

        // Sacrifice-a-creature cost abilities use target selection as UI for cost payment.
        // Validate any remaining real targets after the cost has been paid.
        if (hasSacCreatureCost) {
            targetLegalityService.validateActivatedAbilityTargeting(
                    gameData, playerId, ability, abilityEffects, targetPermanentId, targetZone, permanent.getCard());
        }
        activatedAbilityExecutionService.completeActivationAfterCosts(
                gameData, player, permanent, ability, abilityEffects, effectiveXValue, targetPermanentId, targetZone, hasSacCreatureCost);
        recordAbilityActivationUse(gameData, permanent, effectiveIndex);
    }

    public void completeActivatedAbilitySubtypeSacrificeChoice(GameData gameData,
                                                               Player player,
                                                               PermanentChoiceContext.ActivatedAbilitySacrificeSubtype context,
                                                               UUID sacrificedPermanentId) {
        UUID playerId = player.getId();
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (sourcePermanent == null) {
            throw new IllegalStateException("Source permanent no longer exists");
        }

        List<ActivatedAbility> ownAbilities = sourcePermanent.getCard().getActivatedAbilities();
        List<ActivatedAbility> grantedAbilities = gameQueryService.computeStaticBonus(gameData, sourcePermanent).grantedActivatedAbilities();
        List<ActivatedAbility> abilities = new ArrayList<>(ownAbilities);
        abilities.addAll(grantedAbilities);
        int effectiveIndex = context.abilityIndex() != null ? context.abilityIndex() : 0;
        if (effectiveIndex < 0 || effectiveIndex >= abilities.size()) {
            throw new IllegalStateException("Invalid ability index");
        }
        ActivatedAbility ability = abilities.get(effectiveIndex);
        List<CardEffect> abilityEffects = ability.getEffects();
        boolean hasSubtypeCost = abilityEffects.stream()
                .filter(SacrificeSubtypeCreatureCost.class::isInstance)
                .map(SacrificeSubtypeCreatureCost.class::cast)
                .anyMatch(c -> c.subtype() == context.subtype());
        if (!hasSubtypeCost) {
            throw new IllegalStateException("Activated ability no longer has the required sacrifice cost");
        }

        Permanent sacrificed = gameQueryService.findPermanentById(gameData, sacrificedPermanentId);
        if (sacrificed == null) {
            throw new IllegalStateException("Invalid sacrifice target");
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || !battlefield.contains(sacrificed)) {
            throw new IllegalStateException("Must sacrifice a creature you control");
        }
        if (!gameQueryService.isCreature(gameData, sacrificed)) {
            throw new IllegalStateException("Must sacrifice a creature");
        }
        if (!sacrificed.getCard().getSubtypes().contains(context.subtype())) {
            throw new IllegalStateException("Must sacrifice a " + context.subtype().getDisplayName());
        }

        sacrificeCreatureAsCost(gameData, player, sacrificed);
        activatedAbilityExecutionService.completeActivationAfterCosts(
                gameData, player, sourcePermanent, ability, abilityEffects,
                context.xValue() != null ? context.xValue() : 0, context.targetPermanentId(), context.targetZone(), false);
        recordAbilityActivationUse(gameData, sourcePermanent, effectiveIndex);
    }

    private boolean handleSubtypeSacrificeCostSelection(GameData gameData,
                                                        Player player,
                                                        Permanent sourcePermanent,
                                                        int abilityIndex,
                                                        int xValue,
                                                        UUID targetPermanentId,
                                                        Zone targetZone,
                                                        SacrificeSubtypeCreatureCost subtypeCost) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            throw new IllegalStateException("Invalid battlefield for player");
        }

        List<UUID> validSacrificeIds = battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> p.getCard().getSubtypes().contains(subtypeCost.subtype()))
                .map(Permanent::getId)
                .toList();
        if (validSacrificeIds.isEmpty()) {
            throw new IllegalStateException("Must choose a " + subtypeCost.subtype().getDisplayName() + " to sacrifice");
        }
        if (validSacrificeIds.size() == 1) {
            Permanent onlyChoice = gameQueryService.findPermanentById(gameData, validSacrificeIds.getFirst());
            if (onlyChoice != null) {
                sacrificeCreatureAsCost(gameData, player, onlyChoice);
            }
            return false;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ActivatedAbilitySacrificeSubtype(
                playerId,
                sourcePermanent.getId(),
                abilityIndex,
                xValue,
                targetPermanentId,
                targetZone,
                subtypeCost.subtype()
        ));
        playerInputService.beginPermanentChoice(gameData, playerId, validSacrificeIds,
                "Choose a " + subtypeCost.subtype().getDisplayName() + " to sacrifice.");
        gameBroadcastService.broadcastGameState(gameData);
        return true;
    }

    private boolean hasSubtypeCreatureToSacrifice(GameData gameData, UUID playerId, SacrificeSubtypeCreatureCost subtypeCost) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .anyMatch(p -> gameQueryService.isCreature(gameData, p)
                        && p.getCard().getSubtypes().contains(subtypeCost.subtype()));
    }

    private void sacrificeCreatureAsCost(GameData gameData, Player player, Permanent sacTarget) {
        UUID playerId = player.getId();
        List<Permanent> playerBf = gameData.playerBattlefields.get(playerId);
        if (playerBf == null || !playerBf.contains(sacTarget)) {
            throw new IllegalStateException("Must sacrifice a creature you control");
        }
        playerBf.remove(sacTarget);
        gameHelper.addCardToGraveyard(gameData, playerId, sacTarget.getCard(), Zone.BATTLEFIELD);
        gameHelper.collectDeathTrigger(gameData, sacTarget.getCard(), playerId, true);
        gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
        String sacLog = player.getUsername() + " sacrifices " + sacTarget.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, sacLog);
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

    private List<Integer> collectDiscardIndicesForType(List<Card> hand, CardType requiredType) {
        List<Integer> validIndices = new ArrayList<>();
        if (hand == null) {
            return validIndices;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getType() == requiredType) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }

    private void beginDiscardCostChoice(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, int xValue,
                                        UUID targetPermanentId, Zone targetZone, CardType requiredType, List<Integer> validDiscardIndices) {
        gameData.pendingAbilityActivation = new PendingAbilityActivation(
                permanent.getId(),
                abilityIndex,
                xValue,
                targetPermanentId,
                targetZone,
                requiredType
        );
        gameData.interaction.beginCardChoice(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE, playerId, new HashSet<>(validDiscardIndices), null);
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(
                validDiscardIndices,
                "Choose a " + requiredType.name().toLowerCase() + " card to discard as an activation cost."
        ));
    }

    private void payDiscardCost(GameData gameData, Player player, CardType requiredType, Integer discardCardIndex) {
        if (discardCardIndex == null) {
            throw new IllegalStateException("Must choose a card to discard");
        }

        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Integer> validDiscardIndices = collectDiscardIndicesForType(hand, requiredType);
        Set<Integer> validSet = new HashSet<>(validDiscardIndices);
        if (!validSet.contains(discardCardIndex)) {
            throw new IllegalStateException("Must discard a " + requiredType.name().toLowerCase() + " card");
        }

        Card discarded = hand.remove((int) discardCardIndex);
        gameHelper.addCardToGraveyard(gameData, player.getId(), discarded);
        gameData.discardCausedByOpponent = false;
        gameHelper.checkDiscardTriggers(gameData, player.getId(), discarded);

        String logEntry = player.getUsername() + " discards " + discarded.getName() + " as an activation cost.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} discards {} as activation cost", gameData.id, player.getUsername(), discarded.getName());
    }

    private void clearPendingAbilityActivation(GameData gameData) {
        gameData.pendingAbilityActivation = null;
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearCardChoice();
    }

    private void validateActivationLimitPerTurn(GameData gameData, Permanent permanent, ActivatedAbility ability, int abilityIndex) {
        Integer maxActivationsPerTurn = ability.getMaxActivationsPerTurn();
        if (maxActivationsPerTurn == null) {
            return;
        }

        Map<Integer, Integer> perAbilityCounts = gameData.activatedAbilityUsesThisTurn.get(permanent.getId());
        int currentCount = perAbilityCounts != null ? perAbilityCounts.getOrDefault(abilityIndex, 0) : 0;
        if (currentCount >= maxActivationsPerTurn) {
            throw new IllegalStateException("This ability can be activated no more than " + maxActivationsPerTurn + " times each turn");
        }
    }

    private void recordAbilityActivationUse(GameData gameData, Permanent permanent, int abilityIndex) {
        Map<Integer, Integer> perAbilityCounts = gameData.activatedAbilityUsesThisTurn
                .computeIfAbsent(permanent.getId(), ignored -> new ConcurrentHashMap<>());
        perAbilityCounts.merge(abilityIndex, 1, Integer::sum);
    }
}





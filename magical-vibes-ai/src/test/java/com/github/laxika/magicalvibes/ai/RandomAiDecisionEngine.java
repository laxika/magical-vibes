package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessCountAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * AI that makes purely random decisions from legal options. Designed for fuzz
 * testing — exercises far more code paths than heuristic-based AIs because it
 * plays unusual lines the smart AIs would never consider.
 *
 * <p>Accepts a {@link Random} instance so tests can fix the seed for
 * reproducible failures.</p>
 */
class RandomAiDecisionEngine extends AiDecisionEngine {

    private static final Logger log = LoggerFactory.getLogger(RandomAiDecisionEngine.class);
    private static final int MAX_ACTIVATIONS_PER_ABILITY_PER_TURN = 4;

    private final Random rng;
    private final FuzzTelemetry telemetry;
    private final Map<AbilityActivationKey, Integer> abilityActivationsThisTurn = new HashMap<>();
    private UUID plannedGraveyardExileCostCardId;
    private int trackedActivationTurn = Integer.MIN_VALUE;

    private record AbilityActivationKey(UUID permanentId, int abilityIndex) {}

    RandomAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                           GameService gameService, GameQueryService gameQueryService,
                           BlockLegalityService blockLegalityService,
                           CombatAttackService combatAttackService,
                           GameActionAvailabilityService actionAvailabilityService,
                           com.github.laxika.magicalvibes.service.cast.CastingCostService castingCostService,
                           com.github.laxika.magicalvibes.service.cast.CastingPermissionService castingPermissionService,
                           TargetValidationService targetValidationService,
                           TargetLegalityService targetLegalityService, Random rng, FuzzTelemetry telemetry) {
        super(gameId, aiPlayer, gameRegistry, gameService, gameQueryService, blockLegalityService, combatAttackService, actionAvailabilityService, castingCostService, castingPermissionService, targetValidationService, targetLegalityService);
        this.rng = rng;
        this.telemetry = telemetry;
    }

    /**
     * Records every interaction prompt this engine is responsible for answering, so the
     * batch report shows which interaction kinds the fuzzer actually exercised.
     */
    @Override
    protected void handleInteractionPrompt(GameData gameData) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active != null && AiUtils.isRespondingFor(gameData, aiPlayer.getId(), active.decidingPlayerId())) {
            telemetry.recordInteractionPrompt(active.getClass().getSimpleName());
        }
        if (active instanceof PendingInteraction.GraveyardExileCostChoice choice
                && plannedGraveyardExileCostCardId != null) {
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());
            for (int i = 0; i < graveyard.size(); i++) {
                if (graveyard.get(i).getId().equals(plannedGraveyardExileCostCardId)
                        && choice.validIndices().contains(i)) {
                    plannedGraveyardExileCostCardId = null;
                    final int chosenIndex = i;
                    send(() -> gameActions.answerInteraction(
                            new InteractionAnswer.GraveyardCardChosen(chosenIndex)));
                    return;
                }
            }
            plannedGraveyardExileCostCardId = null;
        }
        super.handleInteractionPrompt(gameData);
    }

    // ===== Priority / Main Phase =====

    @Override
    protected void handleGameState(GameData gameData) {
        if (!hasPriority(gameData)) {
            return;
        }

        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean isActivePlayer = aiPlayer.getId().equals(gameData.activePlayerId);

        if (isMainPhase && isActivePlayer && gameData.stack.isEmpty()) {
            // Always try to play a land (maximizes mana for more interesting games)
            tryPlayLand(gameData);

            // Re-check priority: playing a land can trigger abilities that set
            // awaiting input (e.g. a queued death trigger needing target selection).
            if (!hasPriority(gameData)) {
                return;
            }

            if (gameData.stack.isEmpty() && tryCastRandomSpell(gameData, false)) {
                return;
            }
        }

        // Re-check priority: casting a sorcery-speed spell above may have triggered
        // abilities that set awaiting input.
        if (!hasPriority(gameData)) {
            return;
        }

        // Outside main phase (or after failing to cast a sorcery), try an instant
        if (tryCastRandomSpell(gameData, true)) {
            return;
        }

        // Re-check priority: casting an instant may have triggered abilities that
        // set awaiting input.
        if (!hasPriority(gameData)) {
            return;
        }

        // Half the time, try activating a random non-mana activated ability.
        // Abilities are instant-speed (loyalty abilities are gated to sorcery speed
        // inside canActivateAbility); the 50% keeps games moving.
        if (rng.nextBoolean() && tryActivateRandomAbility(gameData)) {
            return;
        }

        send(() -> gameActions.handlePassPriority(new PassPriorityRequest()));
    }

    // ===== Random Ability Activation =====

    /**
     * Attempts to activate a randomly chosen legal non-mana activated ability.
     * Abilities this engine can't parameterize (X mana costs, variable loyalty
     * costs, multi-target, spell targets) are skipped. Returns true if an
     * activation (or a mana-tap pending choice) was initiated.
     */
    private boolean tryActivateRandomAbility(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        if (battlefield.isEmpty()) {
            return false;
        }

        if (trackedActivationTurn != gameData.turnNumber) {
            abilityActivationsThisTurn.clear();
            trackedActivationTurn = gameData.turnNumber;
        }

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        record AbilityCandidate(Permanent permanent, int abilityIndex, ActivatedAbility ability) {}
        List<AbilityCandidate> candidates = new ArrayList<>();

        for (Permanent permanent : List.copyOf(battlefield)) {
            List<ActivatedAbility> abilities = buildEffectiveAbilityList(gameData, permanent);
            for (int abilIdx = 0; abilIdx < abilities.size(); abilIdx++) {
                ActivatedAbility ability = abilities.get(abilIdx);
                if (isManaAbility(ability)) continue;
                if (ability.isVariableLoyaltyCost()) {
                    telemetry.recordSkip("ability: variable loyalty cost (unsupported)", permanent.getCard().getName());
                    continue;
                }
                if (ability.isMultiTarget()) {
                    telemetry.recordSkip("ability: multi-target (unsupported)", permanent.getCard().getName());
                    continue;
                }
                if (ability.isNeedsSpellTarget()) {
                    telemetry.recordSkip("ability: targets a spell (unsupported)", permanent.getCard().getName());
                    continue;
                }
                if (ability.getManaCost() != null && new ManaCost(ability.getManaCost()).hasX()) {
                    telemetry.recordSkip("ability: X mana cost (unsupported)", permanent.getCard().getName());
                    continue;
                }
                AbilityActivationKey key = new AbilityActivationKey(permanent.getId(), abilIdx);
                if (abilityActivationsThisTurn.getOrDefault(key, 0)
                        >= MAX_ACTIVATIONS_PER_ABILITY_PER_TURN) {
                    continue;
                }
                if (!canActivateAbility(gameData, permanent, ability, abilIdx, virtualPool)) continue;
                candidates.add(new AbilityCandidate(permanent, abilIdx, ability));
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }
        Collections.shuffle(candidates, rng);

        for (AbilityCandidate candidate : candidates) {
            Permanent permanent = candidate.permanent();
            if (!battlefield.contains(permanent)) {
                continue; // Left the battlefield since candidates were collected
            }

            UUID targetId = null;
            if (candidate.ability().isNeedsTarget()) {
                targetId = targetSelector.chooseAbilityTarget(gameData, candidate.ability(),
                        aiPlayer.getId(), permanent);
                if (targetId == null) {
                    telemetry.recordSkip("ability: no valid target", permanent.getCard().getName());
                    continue;
                }
            }

            if (!canActivateAbility(
                    gameData, permanent, candidate.ability(), candidate.abilityIndex(),
                    virtualPool, targetId, null)) {
                continue;
            }

            int additionalGenericCost =
                    gameActions.getActivatedAbilityAdditionalGenericCost(
                            gameData, permanent, candidate.abilityIndex(), targetId, null);
            ExileCardFromGraveyardCost dynamicManaCost =
                    findPayExiledCardManaCost(candidate.ability());
            Card plannedGraveyardCard = null;
            if (dynamicManaCost != null) {
                plannedGraveyardCard = chooseAffordableGraveyardCostCard(
                        gameData, dynamicManaCost, virtualPool, additionalGenericCost);
                if (plannedGraveyardCard == null) {
                    telemetry.recordSkip("ability: dynamic mana cost unpayable",
                            permanent.getCard().getName());
                    continue;
                }
            }

            String manaCost = plannedGraveyardCard != null
                    ? plannedGraveyardCard.getManaCost()
                    : candidate.ability().getManaCost();
            if (manaCost != null || additionalGenericCost > 0) {
                // A {T}-ability's own source must not be tapped for mana
                manaManager.tapSourcesForAbilityCost(
                        gameData, aiPlayer.getId(), manaCost,
                        additionalGenericCost, manaTapAction(),
                        candidate.ability().isRequiresTap() ? candidate.permanent().getId() : null);
                if (gameData.interaction.isAwaitingInput()) {
                    return true; // Mana ability triggered a pending choice; will resume after it resolves
                }
            }

            // Re-resolve the index at send time: paying mana costs can remove
            // permanents from the battlefield (e.g. sacrifice-for-mana artifacts),
            // shifting or invalidating indexes captured during collection.
            int permIdx = battlefield.indexOf(permanent);
            if (permIdx < 0) {
                continue;
            }

            ManaPool actualPool = gameData.playerManaPools.get(aiPlayer.getId());
            if (dynamicManaCost != null) {
                plannedGraveyardCard = chooseAffordableGraveyardCostCard(
                        gameData, dynamicManaCost, actualPool, additionalGenericCost);
                if (plannedGraveyardCard == null) {
                    continue;
                }
            }

            // Re-verify with the engine against the ACTUAL pool: tapping can under-deliver
            // relative to the virtual-pool plan (e.g. the {T}-ability's own source was the
            // only untapped producer left), and a doomed request is rejected silently.
            if (!canActivateAbility(gameData, permanent, candidate.ability(),
                    candidate.abilityIndex(), actualPool,
                    targetId, null)) {
                continue;
            }

            log.info("Random AI: Activating ability {} on {} in game {}", candidate.abilityIndex(),
                    permanent.getCard().getName(), gameId);
            final int finalPermIdx = permIdx;
            final int abilIdx = candidate.abilityIndex();
            final UUID finalTargetId = targetId;
            int stackSizeBefore = gameData.stack.size();
            boolean tappedBefore = permanent.isTapped();
            plannedGraveyardExileCostCardId = plannedGraveyardCard != null
                    ? plannedGraveyardCard.getId()
                    : null;
            send(() -> gameActions.handleActivateAbility(
                    new ActivateAbilityRequest(finalPermIdx, abilIdx, null, finalTargetId, null, null, null)));

            // The engine rejects some invalid activations by returning silently. If the
            // AI treated such an activation as its action for this priority, no state
            // change is broadcast, no new message arrives, and the game deadlocks —
            // detect it and fall through to the next candidate (or pass priority).
            boolean activated = gameData.stack.size() > stackSizeBefore
                    || gameData.interaction.isAwaitingInput()
                    || (!tappedBefore && permanent.isTapped())
                    || gameData.status != GameStatus.RUNNING;
            if (!activated) {
                plannedGraveyardExileCostCardId = null;
                log.warn("Random AI: ActivateAbility failed silently in game {}. Permanent='{}' abilityIndex={} step={} activePlayer={}",
                        gameId, permanent.getCard().getName(), candidate.abilityIndex(),
                        gameData.currentStep, gameData.activePlayerId);
                continue;
            }
            if (!(gameData.interaction.activeInteraction()
                    instanceof PendingInteraction.GraveyardExileCostChoice)) {
                plannedGraveyardExileCostCardId = null;
            }
            abilityActivationsThisTurn.merge(
                    new AbilityActivationKey(permanent.getId(), candidate.abilityIndex()), 1, Integer::sum);
            telemetry.recordAbilityActivation(permanent.getCard().getName());
            return true;
        }
        return false;
    }

    private ExileCardFromGraveyardCost findPayExiledCardManaCost(ActivatedAbility ability) {
        return ability.getEffects().stream()
                .filter(ExileCardFromGraveyardCost.class::isInstance)
                .map(ExileCardFromGraveyardCost.class::cast)
                .filter(ExileCardFromGraveyardCost::payExiledCardManaCost)
                .findFirst()
                .orElse(null);
    }

    private Card chooseAffordableGraveyardCostCard(
            GameData gameData, ExileCardFromGraveyardCost cost, ManaPool manaPool,
            int additionalGenericCost) {
        List<Card> affordable = gameData.playerGraveyards
                .getOrDefault(aiPlayer.getId(), List.of())
                .stream()
                .filter(card -> cost.requiredType() == null || card.hasType(cost.requiredType()))
                .filter(card -> cost.requiredSubtype() == null
                        || card.getSubtypes().contains(cost.requiredSubtype()))
                .filter(card -> card.getManaCost() != null)
                .filter(card -> new ManaCost(card.getManaCost())
                        .canPay(manaPool, additionalGenericCost))
                .toList();
        if (affordable.isEmpty()) {
            return null;
        }
        return affordable.get(rng.nextInt(affordable.size()));
    }

    // ===== Random Spell Casting =====

    private boolean tryCastRandomSpell(GameData gameData, boolean instantsOnly) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());

        // Collect all castable spell indices
        List<Integer> castableIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) {
                continue;
            }
            if (instantsOnly && !card.hasType(CardType.INSTANT)) {
                continue;
            }
            if (card.getManaCost() == null) {
                continue;
            }

            // Skip spells that target spells on the stack (e.g. Twincast) — AI can't pick spell targets
            if (EffectResolution.needsSpellTarget(card)) {
                telemetry.recordSkip("spell: targets a spell on the stack (unsupported)", card.getName());
                continue;
            }

            if (!isSpellCastable(gameData, card, virtualPool)) {
                continue;
            }
            castableIndices.add(i);
        }

        if (castableIndices.isEmpty()) {
            return false;
        }

        // Shuffle to pick a random castable spell
        Collections.shuffle(castableIndices, rng);

        for (int cardIndex : castableIndices) {
            Card card = hand.get(cardIndex);

            // Handle modal spells (ChooseOneEffect)
            ModalCastPlan modalPlan = prepareModalSpellCast(gameData, card);
            if (modalPlan == null && findChooseOneEffect(card) != null) {
                telemetry.recordSkip("spell: modal with no castable mode", card.getName());
                continue; // No valid mode for this modal spell
            }

            // Build damage assignments for divided damage spells
            Map<UUID, Integer> damageAssignments = null;
            if (modalPlan == null && EffectResolution.needsDamageDistribution(card)) {
                damageAssignments = targetSelector.buildDamageAssignments(gameData, card, aiPlayer.getId());
                if (damageAssignments == null) {
                    telemetry.recordSkip("spell: no damage-distribution targets", card.getName());
                    continue; // No valid targets for damage distribution
                }
            }

            // Determine target if needed (skip for modal and damage distribution spells)
            UUID targetId = modalPlan != null ? modalPlan.targetId() : null;
            List<UUID> multiTargetIds = modalPlan != null ? modalPlan.targetIds() : null;
            // Shared classifier, same as every other engine: a single "up to N" group (Synchronized
            // Strike) also belongs on the multi-target path — routing it to the single-target one
            // submits one target and can offer a player the spell can't legally target.
            boolean isMultiTarget = targetSelector.needsMultiTargetSelection(card);
            if (isMultiTarget && modalPlan == null) {
                multiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
                if (multiTargetIds == null) {
                    telemetry.recordSkip("spell: multi-target requirements unsatisfiable", card.getName());
                    continue; // Can't satisfy mandatory targets, try next spell
                }
            } else if (modalPlan == null && !EffectResolution.needsDamageDistribution(card) && (EffectResolution.needsTarget(card) || card.isAura())) {
                targetId = pickRandomTarget(gameData, card);
                if (targetId == null) {
                    telemetry.recordSkip("spell: no valid target", card.getName());
                    continue; // No valid target, try next spell
                }
            }

            // Check targeting tax (e.g. Kopala, Warden of Waves)
            int targetingTax = computeTargetingTax(gameData, targetId, multiTargetIds);
            if (targetingTax > 0 && !canAffordSpell(gameData, card, virtualPool, targetingTax)) {
                telemetry.recordSkip("spell: targeting tax unaffordable", card.getName());
                continue; // Can't afford with targeting tax, try next spell
            }

            // Determine exile graveyard card index if needed (single card exile)
            Integer exileGraveyardCardIndex = null;
            ExileCardFromGraveyardCost exileCost = findExileGraveyardCost(card);
            if (exileCost != null) {
                exileGraveyardCardIndex = findValidGraveyardIndex(graveyard, exileCost);
                if (exileGraveyardCardIndex == null) {
                    telemetry.recordSkip("spell: graveyard exile cost unpayable", card.getName());
                    continue; // No valid graveyard card, try next spell
                }
            }

            // Determine exile graveyard card indices if needed (X cards exile, e.g. Harvest Pyre)
            List<Integer> exileGraveyardCardIndices = null;
            if (findExileXGraveyardCost(card) != null) {
                List<Integer> allIndices = selectAllGraveyardIndices(gameData);
                if (allIndices.isEmpty()) {
                    telemetry.recordSkip("spell: graveyard exile cost unpayable", card.getName());
                    continue; // No graveyard cards, try next spell
                }
                Collections.shuffle(allIndices, rng);
                int count = rng.nextInt(allIndices.size()) + 1;
                exileGraveyardCardIndices = new ArrayList<>(allIndices.subList(0, count));
            } else if (findExileNGraveyardCost(card) != null) {
                exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
                if (exileGraveyardCardIndices == null) {
                    telemetry.recordSkip("spell: graveyard exile cost unpayable", card.getName());
                    continue; // Not enough matching graveyard cards, try next spell
                }
            }

            // Pick a random card to discard if the spell has a "discard a card" additional cost
            // (e.g. Seize the Spoils) — the engine rejects the cast without a selection.
            // Discard-or-pay-mana (Lightning Axe) may leave the index null to pay the mana option.
            Integer discardHandCardIndex = null;
            List<Integer> validDiscardIndices = castingCostService.validDiscardCostIndices(
                    gameData, aiPlayer.getId(), card);
            if (validDiscardIndices != null) {
                if (validDiscardIndices.isEmpty()) {
                    boolean hasDiscardOrPay = card.getEffects(EffectSlot.SPELL).stream()
                            .anyMatch(DiscardCardOrPayManaCost.class::isInstance);
                    if (!hasDiscardOrPay) {
                        telemetry.recordSkip("spell: discard cost unpayable", card.getName());
                        continue;
                    }
                } else {
                    discardHandCardIndex = validDiscardIndices.get(rng.nextInt(validDiscardIndices.size()));
                }
            }

            // Select sacrifice target if the spell has a sacrifice cost
            UUID sacrificePermanentId = selectRandomSacrificeTarget(gameData, card);

            // Calculate X value (for modal spells, xValue is the mode index)
            ManaCost castCost = new ManaCost(card.getManaCost());
            Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
            if (castCost.hasX() && xValue == null) {
                int costModifier = castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card) + targetingTax;
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool, costModifier);
                maxX = Math.min(maxX, getMaxXForGraveyardRequirements(gameData, card));
                if (maxX <= 0) {
                    telemetry.recordSkip("spell: X cost unaffordable", card.getName());
                    continue;
                }
                // For requiresManaValueEqualsX spells (e.g. Postmortem Lunge), X must match the
                // graveyard target's mana value — re-pick an affordable target and set X accordingly.
                if (hasRequiresManaValueEqualsX(card)) {
                    List<Card> validTargets = targetSelector.findValidGraveyardTargets(
                            gameData, card, aiPlayer.getId(), maxX);
                    if (validTargets.isEmpty()) {
                        telemetry.recordSkip("spell: no affordable mana-value-X target", card.getName());
                        continue;
                    }
                    Card chosen = validTargets.get(rng.nextInt(validTargets.size()));
                    targetId = chosen.getId();
                    xValue = chosen.getManaValue();
                } else if (hasPermanentManaValueEqualsXTarget(card)) {
                    // For PermanentManaValueEqualsXPredicate spells (e.g. Entrancing Melody),
                    // X must match the target permanent's mana value — co-select target and X.
                    List<Permanent> validTargets = targetSelector.findValidPermanentTargetsForManaValueX(
                            gameData, card, aiPlayer.getId(), maxX);
                    if (validTargets.isEmpty()) {
                        telemetry.recordSkip("spell: no affordable mana-value-X target", card.getName());
                        continue;
                    }
                    Permanent chosen = validTargets.get(rng.nextInt(validTargets.size()));
                    targetId = chosen.getId();
                    xValue = chosen.getCard().getManaValue();
                } else {
                    // Pick a random X between 1 and maxX
                    xValue = rng.nextInt(maxX) + 1;
                }
            }

            log.info("Random AI: Casting {}{} in game {}", card.getName(),
                    xValue != null ? " (X=" + xValue + ")" : "", gameId);
            if (tapManaForSpell(gameData, card, xValue, targetingTax)) {
                return true; // Mana ability triggered a pending choice; will resume after it resolves
            }
            if (targetId != null
                    && modalPlan == null
                    && !isMultiTarget
                    && !EffectResolution.needsDamageDistribution(card)
                    && !castCost.hasX()) {
                List<UUID> currentTargets = findRandomTargets(gameData, card);
                while (!currentTargets.contains(targetId)) {
                    if (currentTargets.isEmpty()) {
                        telemetry.recordSkip("spell: no valid target after mana payment", card.getName());
                        targetId = null;
                        break;
                    }
                    targetId = currentTargets.get(rng.nextInt(currentTargets.size()));
                    int refreshedTargetingTax = computeTargetingTax(gameData, targetId, null);
                    ManaPool refreshedVirtualPool = manaManager.buildVirtualManaPool(
                            gameData, aiPlayer.getId());
                    if (!canAffordSpell(gameData, card, refreshedVirtualPool, refreshedTargetingTax)) {
                        telemetry.recordSkip("spell: refreshed targeting tax unaffordable", card.getName());
                        targetId = null;
                        break;
                    }
                    if (tapManaForSpell(gameData, card, xValue, refreshedTargetingTax)) {
                        return true;
                    }
                    currentTargets = findRandomTargets(gameData, card);
                }
                if (targetId == null) {
                    continue;
                }
            }

            // Chosen after mana payment: tapping can sacrifice a permanent for mana, which would
            // invalidate a selection made earlier and get the whole cast rejected.
            List<UUID> multiSacrificeIds = selectRandomMultiSacrificeTargets(gameData, card);
            if (multiSacrificeIds == null) {
                telemetry.recordSkip("spell: multi-permanent sacrifice cost unpayable", card.getName());
                continue;
            }

            final UUID finalTargetId = targetId;
            final Integer finalXValue = xValue;
            final Integer finalExileGraveyardCardIndex = exileGraveyardCardIndex;
            final List<Integer> finalExileGraveyardCardIndices = exileGraveyardCardIndices;
            final UUID finalSacrificePermanentId = sacrificePermanentId;
            final Map<UUID, Integer> finalDamageAssignments = damageAssignments;
            final List<UUID> finalMultiTargetIds = multiTargetIds;
            final Integer finalDiscardHandCardIndex = discardHandCardIndex;
            final List<UUID> finalMultiSacrificeIds = multiSacrificeIds;
            send(() -> gameActions.handlePlayCard(
                    new PlayCardRequest(cardIndex, finalXValue, finalTargetId, finalDamageAssignments, finalMultiTargetIds, null, null, finalSacrificePermanentId, null, null, null, null, finalExileGraveyardCardIndex, finalExileGraveyardCardIndices, null, null, null, finalDiscardHandCardIndex, null, null, finalMultiSacrificeIds)));

            // Game may have ended while paying costs (e.g. Manabarbs killing the caster
            // on a land tap) — every later action no-ops, which is not a legality bug.
            if (gameData.status != GameStatus.RUNNING) {
                return true;
            }

            // Identity check: hand size alone is unreliable because ETB/cast triggers
            // can add cards back to hand (e.g. Explore revealing a land), masking a
            // successful cast.
            if (hand.contains(card)) {
                ManaPool actualPool = gameData.playerManaPools.get(aiPlayer.getId());
                log.warn("Random AI: PlayCard failed silently in game {}. Card='{}' index={} step={} activePlayer={} isActive={} stackEmpty={} actualPool={} virtualPool={} priorityPassed={}",
                        gameId, card.getName(), cardIndex, gameData.currentStep,
                        gameData.activePlayerId, aiPlayer.getId().equals(gameData.activePlayerId),
                        gameData.stack.isEmpty(), actualPool != null ? actualPool.toMap() : "null",
                        virtualPool.toMap(), gameData.priorityPassedBy);
                continue; // Try next spell
            }
            telemetry.recordSpellCast(card.getName());
            return true;
        }
        return false;
    }

    /**
     * Finds an ExileCardFromGraveyardCost in the card's SPELL effects, if any.
     */
    private ExileCardFromGraveyardCost findExileGraveyardCost(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ExileCardFromGraveyardCost cost) {
                return cost;
            }
        }
        return null;
    }

    /**
     * Finds a random valid graveyard card index matching the exile cost's required type.
     * Returns null if no valid card exists.
     */
    private Integer findValidGraveyardIndex(List<Card> graveyard, ExileCardFromGraveyardCost cost) {
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            Card graveyardCard = graveyard.get(i);
            if (cost.requiredType() == null || graveyardCard.hasType(cost.requiredType())) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return null;
        }
        return validIndices.get(rng.nextInt(validIndices.size()));
    }

    /**
     * Returns true if the card has a ReturnCardFromGraveyardEffect with requiresManaValueEqualsX,
     * meaning X must match the graveyard target's mana value (e.g. Postmortem Lunge).
     */
    private boolean hasRequiresManaValueEqualsX(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e instanceof ReturnCardFromGraveyardEffect rge && rge.requiresManaValueEqualsX());
    }

    // ===== Random Sacrifice Target Selection =====

    /**
     * Selects a random permanent to pay whichever additional cast cost consumes a payer-chosen
     * permanent (sacrifice, return to hand, put a counter on a creature you control). Driven by
     * {@link CostEffect#consumedPermanentFilter()} so a new cost record is covered as soon as it
     * declares its filter — an unrecognized cost would send a null id and have the cast rejected.
     * Returns null if the card has no such cost. Multi-permanent sacrifice costs also declare a
     * filter but are paid through their own list field, so they are handled by
     * {@link #selectRandomMultiSacrificeTargets} instead.
     */
    private UUID selectRandomSacrificeTarget(GameData gameData, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (!(effect instanceof CostEffect cost) || effect instanceof SacrificeMultiplePermanentsCost) {
                continue;
            }
            PermanentPredicate filter = cost.consumedPermanentFilter();
            if (filter == null) {
                continue;
            }
            List<Permanent> matching = battlefield.stream()
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter))
                    .toList();
            return matching.isEmpty() ? null : matching.get(rng.nextInt(matching.size())).getId();
        }
        return null;
    }

    /**
     * Selects random distinct permanents to pay a multi-permanent sacrifice additional cast cost
     * (Phyrexian Tribute's "sacrifice two creatures"). Returns an empty list when the card has no
     * such cost, and null when it has one but too few matching permanents remain — the engine
     * rejects the whole cast unless exactly {@code count} ids are supplied.
     */
    private List<UUID> selectRandomMultiSacrificeTargets(GameData gameData, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (!(effect instanceof SacrificeMultiplePermanentsCost cost)) {
                continue;
            }
            List<Permanent> matching = new ArrayList<>(battlefield.stream()
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                    .toList());
            if (matching.size() < cost.count()) {
                return null;
            }
            Collections.shuffle(matching, rng);
            return matching.subList(0, cost.count()).stream().map(Permanent::getId).toList();
        }
        return List.of();
    }

    // ===== Random Target Selection =====

    private UUID pickRandomTarget(GameData gameData, Card card) {
        List<UUID> validTargets = findRandomTargets(gameData, card);
        if (validTargets.isEmpty()) {
            return null;
        }
        return validTargets.get(rng.nextInt(validTargets.size()));
    }

    private List<UUID> findRandomTargets(GameData gameData, Card card) {
        List<UUID> validTargets = new ArrayList<>();
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());

        // Use base-mode targeting since AI never kicks spells
        Set<TargetType> allowed = targetSelector.computeBaseAllowedTargets(card);

        // Add players as targets if allowed, respecting player relation predicates and hexproof/shroud.
        // The engine check is the last word: an allowed set that merely includes players (a no-op
        // PLAYER_OR_PERMANENT spec on a live multi-target scope) does not make one legal.
        if (allowed.contains(TargetType.PLAYER)) {
            PlayerRelation relation = PlayerRelation.ANY;
            if (card.getTargetFilter() instanceof PlayerPredicateTargetFilter ptf
                    && ptf.predicate() instanceof PlayerRelationPredicate prp) {
                relation = prp.relation();
            }
            if (relation != PlayerRelation.OPPONENT
                    && !gameQueryService.playerHasShroud(gameData, aiPlayer.getId())
                    && targetSelector.isValidPlayerTarget(gameData, card, aiPlayer.getId(), aiPlayer.getId())) {
                validTargets.add(aiPlayer.getId());
            }
            if (relation != PlayerRelation.SELF && opponentId != null
                    && !gameQueryService.playerHasShroud(gameData, opponentId)
                    && !gameQueryService.playerHasHexproof(gameData, opponentId)
                    && targetSelector.isValidPlayerTarget(gameData, card, opponentId, aiPlayer.getId())) {
                validTargets.add(opponentId);
            }
        }

        // Add permanents as targets (unless it only targets players)
        if (!card.isEnchantPlayer() && (!allowed.contains(TargetType.PLAYER) || allowed.contains(TargetType.PERMANENT) || card.isAura())) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> field = gameData.playerBattlefields.getOrDefault(playerId, List.of());
                for (Permanent p : field) {
                    if (targetSelector.isValidPermanentTarget(gameData, card, p, aiPlayer.getId())) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }

        // Add graveyard cards as targets if allowed
        if (allowed.contains(TargetType.GRAVEYARD)) {
            for (Card c : targetSelector.findValidGraveyardTargets(gameData, card, aiPlayer.getId())) {
                validTargets.add(c.getId());
            }
        }
        return validTargets;
    }

    // ===== Combat: Random Attackers =====

    @Override
    protected void handleAttackers(GameData gameData) {
        UUID actingPlayerId = activeDecisionPlayerId(gameData);
        List<Permanent> battlefield = gameData.playerBattlefields.get(actingPlayerId);
        List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gameData, actingPlayerId);
        if (battlefield == null || availableIndices.isEmpty()) {
            sendAttackerDeclaration(new DeclareAttackersRequest(List.of(), null));
            return;
        }

        // Each available attacker has a 50% chance of attacking
        List<Integer> attackerIndices = new ArrayList<>();
        for (int i : availableIndices) {
            if (rng.nextBoolean()) {
                attackerIndices.add(i);
            }
        }

        // Ensure creatures with "attacks each combat if able" are included
        List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gameData, actingPlayerId, availableIndices);
        attackerIndices = enforceMustAttack(attackerIndices, mustAttackIndices);

        // CR 508.1c: if only one attacker selected and it can't attack alone, try to
        // pair it with another available attacker before tax prep. prepareAttackersForTax
        // applies a final safety net if it can't.
        if (attackerIndices.size() == 1) {
            Permanent sole = battlefield.get(attackerIndices.getFirst());
            if (sole.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance)) {
                List<Integer> others = new ArrayList<>(availableIndices);
                others.removeAll(attackerIndices);
                if (!others.isEmpty()) {
                    attackerIndices.add(others.get(rng.nextInt(others.size())));
                } else {
                    attackerIndices.clear();
                }
            }
        }

        // Ensure at least one attacker when forced (e.g. Trove of Temptation)
        attackerIndices = enforceMustAttackWithAtLeastOne(gameData, attackerIndices, availableIndices);

        // Cap attackers to what we can afford given attack tax, and tap mana to pay
        attackerIndices = prepareAttackersForTax(gameData, attackerIndices);

        log.info("Random AI: Declaring {} of {} attackers in game {}",
                attackerIndices.size(), availableIndices.size(), gameId);
        sendAttackerDeclaration(new DeclareAttackersRequest(attackerIndices, null));
    }

    // ===== Combat: Random Blockers =====

    @Override
    protected void handleBlockers(GameData gameData) {
        try {
            handleBlockersInternal(gameData);
        } catch (Exception e) {
            log.error("Random AI: Error in handleBlockers in game {}, sending empty blockers", gameId, e);
            sendBlockerDeclaration(new DeclareBlockersRequest(List.of()));
        }
    }

    private void handleBlockersInternal(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        if (battlefield == null || opponentBattlefield == null) {
            sendBlockerDeclaration(new DeclareBlockersRequest(List.of()));
            return;
        }

        List<Integer> attackerIndices = new ArrayList<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent perm = opponentBattlefield.get(i);
            if (perm.isAttacking() && !gameQueryService.hasCantBeBlocked(gameData, perm)) {
                attackerIndices.add(i);
            }
        }
        if (attackerIndices.isEmpty()) {
            sendBlockerDeclaration(new DeclareBlockersRequest(List.of()));
            return;
        }

        List<Integer> availableBlockerIndices = new ArrayList<>();
        for (int j = 0; j < battlefield.size(); j++) {
            Permanent blocker = battlefield.get(j);
            if (blockLegalityService.canBlock(gameData, blocker)) {
                availableBlockerIndices.add(j);
            }
        }

        // Resolve per-creature mustBlockIds (Provoke, etc.): blocker → attacker pairs it's
        // obligated to attempt. Collected per-attacker so menace/lure logic can fold them
        // in atomically.
        Map<Integer, List<Integer>> provokedBlockersByAttacker = new HashMap<>();
        for (int blockerIdx : availableBlockerIndices) {
            Permanent blocker = battlefield.get(blockerIdx);
            if (blocker.getMustBlockIds().isEmpty()) continue;
            for (UUID mustBlockId : blocker.getMustBlockIds()) {
                for (int attackerIdx : attackerIndices) {
                    Permanent attacker = opponentBattlefield.get(attackerIdx);
                    if (attacker.getId().equals(mustBlockId) && canBlock(gameData, blocker, attacker)) {
                        provokedBlockersByAttacker.computeIfAbsent(attackerIdx, k -> new ArrayList<>()).add(blockerIdx);
                        break;
                    }
                }
            }
        }

        Set<Integer> lureAttackerIndices = findLureAttackers(gameData, opponentBattlefield);
        Set<Integer> mustBeBlockedAttackerIndices = findMustBeBlockedAttackers(gameData, opponentBattlefield);

        // Sort by priority group: lure → menace-lure → mustBlockIfAble → provoked → regular.
        // Random within each group (Random AI preserves randomness but respects constraint priority).
        List<Integer> sortedAttackers = new ArrayList<>(attackerIndices);
        Collections.shuffle(sortedAttackers, rng);
        sortedAttackers.sort((a, b) -> Integer.compare(priorityGroup(gameData, opponentBattlefield, b,
                lureAttackerIndices, mustBeBlockedAttackerIndices, provokedBlockersByAttacker),
                priorityGroup(gameData, opponentBattlefield, a,
                        lureAttackerIndices, mustBeBlockedAttackerIndices, provokedBlockersByAttacker)));

        List<BlockerAssignment> assignments = new ArrayList<>();
        boolean[] blockerUsed = new boolean[battlefield.size()];
        // Block taxes (Hipparion) are budgeted against what is already floating: the fuzzer picks
        // blockers at random rather than around a payment plan, and CR 509.1c never requires
        // paying. Anything short of that is still caught by the shared affordability pass, which
        // floats mana for what it keeps.
        int blockTaxBudget = gameData.playerManaPools.get(aiPlayer.getId()).getTotal();
        // Board-wide block life costs (Heat Wave) come out of the AI's life total, and can't be
        // paid at all while its life total is locked.
        int blockLifeTaxBudget = gameQueryService.canPlayerLifeChange(gameData, aiPlayer.getId())
                ? gameData.getLife(aiPlayer.getId())
                : 0;
        int teamMaxBlockers = teamMaxBlockersPerAttacker(opponentBattlefield);

        for (int attackerIdx : sortedAttackers) {
            Permanent attacker = opponentBattlefield.get(attackerIdx);
            int minimumBlockers = AiUtils.minimumBlockersRequiredToBlock(
                    gameData, gameQueryService, attacker);
            int maximumBlockers = Math.min(teamMaxBlockers, maxBlockersForAttacker(attacker));
            // e.g. menace plus "can't be blocked by more than one creature" — no legal block exists.
            if (minimumBlockers > maximumBlockers) continue;
            boolean lure = lureAttackerIndices.contains(attackerIdx);
            boolean mustBlock = mustBeBlockedAttackerIndices.contains(attackerIdx);
            List<Integer> provoked = provokedBlockersByAttacker.getOrDefault(attackerIdx, List.of());

            List<Integer> candidates = new ArrayList<>();
            for (int blockerIdx : availableBlockerIndices) {
                if (blockerUsed[blockerIdx]) continue;
                Permanent blocker = battlefield.get(blockerIdx);
                if (!canBlock(gameData, blocker, attacker)) continue;
                // CR 509.1f: the whole block cost is paid at once, and CR 509.1c never requires
                // paying it — so a block we can't cover simply isn't declared.
                if (blockTaxFor(gameData, blocker, attacker) > blockTaxBudget) continue;
                if (blockLifeTaxFor(gameData, blocker, attacker) > blockLifeTaxBudget) continue;
                candidates.add(blockerIdx);
            }
            if (candidates.isEmpty()) continue;
            // A block is legal only when enough creatures can be assigned together.
            if (candidates.size() < minimumBlockers) continue;

            List<Integer> chosen;
            if (lure) {
                // Matching able blockers must block this attacker (filter may narrow).
                chosen = new ArrayList<>();
                for (int blockerIdx : candidates) {
                    if (gameQueryService.isRequiredToBlockByLure(
                            gameData, attacker, battlefield.get(blockerIdx))) {
                        chosen.add(blockerIdx);
                    }
                }
            } else if (!provoked.isEmpty()) {
                // Provoked blockers must block; add enough partners to make the block legal.
                List<Integer> provokedUnused = new ArrayList<>();
                for (int p : provoked) {
                    if (!blockerUsed[p] && candidates.contains(p)) provokedUnused.add(p);
                }
                if (provokedUnused.isEmpty()) {
                    chosen = List.of();
                } else {
                    chosen = new ArrayList<>(provokedUnused);
                    for (int c : candidates) {
                        if (chosen.size() >= minimumBlockers) break;
                        if (!chosen.contains(c)) chosen.add(c);
                    }
                    if (chosen.size() < minimumBlockers) chosen = List.of();
                }
            } else if (mustBlock) {
                List<Integer> shuffled = new ArrayList<>(candidates);
                Collections.shuffle(shuffled, rng);
                chosen = shuffled.subList(0, minimumBlockers);
            } else {
                // Voluntary block: 50% to try, then assign the minimum legal number.
                if (!rng.nextBoolean()) continue;
                List<Integer> shuffled = new ArrayList<>(candidates);
                Collections.shuffle(shuffled, rng);
                chosen = shuffled.subList(0, minimumBlockers);
            }

            if (chosen.isEmpty()) continue;
            if (chosen.size() > maximumBlockers) {
                chosen = chosen.subList(0, maximumBlockers);
            }
            int chosenTax = 0;
            int chosenLifeTax = 0;
            for (int blockerIdx : chosen) {
                Permanent blocker = battlefield.get(blockerIdx);
                chosenTax += blockTaxFor(gameData, blocker, attacker);
                chosenLifeTax += blockLifeTaxFor(gameData, blocker, attacker);
            }
            if (chosenTax > blockTaxBudget) continue;
            if (chosenLifeTax > blockLifeTaxBudget) continue;
            blockTaxBudget -= chosenTax;
            blockLifeTaxBudget -= chosenLifeTax;

            for (int blockerIdx : chosen) {
                assignments.add(new BlockerAssignment(blockerIdx, attackerIdx));
                blockerUsed[blockerIdx] = true;
            }
        }

        removeBlockersWithUnmetPartnerRequirement(gameData, battlefield, opponentBattlefield, assignments);

        // CR 509.1a: if only one unique blocker and it can't block alone, remove it.
        Set<Integer> uniqueBlockerIndices = new HashSet<>();
        for (BlockerAssignment a : assignments) {
            uniqueBlockerIndices.add(a.blockerIndex());
        }
        if (uniqueBlockerIndices.size() == 1) {
            Permanent sole = battlefield.get(uniqueBlockerIndices.iterator().next());
            if (sole.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance)) {
                assignments.clear();
            }
        }

        log.info("Random AI: Declaring {} blockers in game {}", assignments.size(), gameId);
        sendBlockerDeclaration(new DeclareBlockersRequest(assignments));
    }

    /**
     * Drops declared blockers whose "can't block unless …" partner requirement the rest of the
     * declaration fails to satisfy, then drops any block left with too few blockers to be legal.
     * Repeats until stable, since removing a blocker can invalidate the ones that remain.
     */
    private void removeBlockersWithUnmetPartnerRequirement(
            GameData gameData,
            List<Permanent> battlefield,
            List<Permanent> opponentBattlefield,
            List<BlockerAssignment> assignments) {
        boolean changed;
        do {
            Set<Integer> selectedBlockers = new HashSet<>();
            for (BlockerAssignment assignment : assignments) {
                selectedBlockers.add(assignment.blockerIndex());
            }

            Set<Integer> invalidRestrictedBlockers = new HashSet<>();
            for (int blockerIdx : selectedBlockers) {
                if (hasUnmetBlockPartnerRequirement(gameData, battlefield, selectedBlockers, blockerIdx)) {
                    invalidRestrictedBlockers.add(blockerIdx);
                }
            }

            changed = assignments.removeIf(
                    assignment -> invalidRestrictedBlockers.contains(assignment.blockerIndex()));

            Map<Integer, Integer> blockersPerAttacker = new HashMap<>();
            for (BlockerAssignment assignment : assignments) {
                blockersPerAttacker.merge(assignment.attackerIndex(), 1, Integer::sum);
            }
            Set<Integer> undersizedBlocks = new HashSet<>();
            for (var entry : blockersPerAttacker.entrySet()) {
                Permanent attacker = opponentBattlefield.get(entry.getKey());
                int minimumBlockers = AiUtils.minimumBlockersRequiredToBlock(
                        gameData, gameQueryService, attacker);
                if (entry.getValue() < minimumBlockers) {
                    undersizedBlocks.add(entry.getKey());
                }
            }
            changed |= assignments.removeIf(
                    assignment -> undersizedBlocks.contains(assignment.attackerIndex()));
        } while (changed);
    }

    /**
     * CR 509.1a: whether a declared blocker's own "can't block unless …" partner restriction is
     * disobeyed by the rest of the declaration — Okk needs a co-blocker with strictly greater
     * power, Orcish Conscripts needs a minimum number of other creatures blocking. Both are
     * checked only at declaration time, against the blockers declared alongside it.
     */
    private boolean hasUnmetBlockPartnerRequirement(GameData gameData, List<Permanent> battlefield,
                                                    Set<Integer> selectedBlockers, int blockerIdx) {
        Permanent blocker = battlefield.get(blockerIdx);
        for (CardEffect effect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect) {
                int blockerPower = gameQueryService.getEffectivePower(gameData, blocker);
                boolean hasGreaterPowerPartner = selectedBlockers.stream()
                        .filter(otherIdx -> otherIdx != blockerIdx)
                        .map(battlefield::get)
                        .anyMatch(other -> gameQueryService.getEffectivePower(gameData, other) > blockerPower);
                if (!hasGreaterPowerPartner) {
                    return true;
                }
            }
            if (effect instanceof CantAttackOrBlockUnlessCountAlsoDoesEffect restriction
                    && selectedBlockers.size() - 1 < restriction.otherCount()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The most creatures that may be assigned to the given attacker under its own
     * "can't be blocked by more than N creatures" restriction (Charging Rhino), or
     * {@link Integer#MAX_VALUE} when it carries none.
     */
    private int maxBlockersForAttacker(Permanent attacker) {
        int maximum = Integer.MAX_VALUE;
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanBeBlockedByAtMostNCreaturesEffect restriction) {
                maximum = Math.min(maximum, restriction.maxBlockers());
            }
        }
        return maximum;
    }

    /**
     * The attacking player's team-wide cap on blockers per creature (Familiar Ground, Yuan Shao,
     * the Indecisive), or {@link Integer#MAX_VALUE} when nothing on their battlefield imposes one.
     */
    private int teamMaxBlockersPerAttacker(List<Permanent> opponentBattlefield) {
        int maximum = Integer.MAX_VALUE;
        for (Permanent permanent : opponentBattlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect restriction) {
                    maximum = Math.min(maximum, restriction.maxBlockers());
                }
            }
        }
        return maximum;
    }

    /**
     * Generic mana the AI would owe to declare this blocker against this attacker — Hipparion's
     * {1} to block power 3 or greater, plus the Aura taxes on either creature (Awesome Presence,
     * Oppressive Rays). Delegates to the engine so the AI's affordability check and the engine's
     * validation are computed from the same rule.
     */
    private int blockTaxFor(GameData gameData, Permanent blocker, Permanent attacker) {
        return gameQueryService.getBlockManaTax(gameData, blocker, attacker);
    }

    /**
     * Life the AI would owe to declare this blocker against this attacker under a board-wide
     * block life cost (Heat Wave). Zero when the AI's life total can't change, since the engine
     * rejects a block whose life cost can't be paid.
     */
    private int blockLifeTaxFor(GameData gameData, Permanent blocker, Permanent attacker) {
        return gameQueryService.getGlobalBlockLifeTax(gameData, blocker, attacker);
    }

    /**
     * Priority rank for attacker iteration. Higher ranks are processed first so the most
     * constrained attackers claim their required blockers before less-constrained ones drain
     * the pool.
     */
    private int priorityGroup(GameData gameData, List<Permanent> opponentBattlefield, int attackerIdx,
                              Set<Integer> lureAttackers, Set<Integer> mustBlockAttackers,
                              Map<Integer, List<Integer>> provokedByAttacker) {
        Permanent attacker = opponentBattlefield.get(attackerIdx);
        boolean lure = lureAttackers.contains(attackerIdx);
        int minimumBlockers = AiUtils.minimumBlockersRequiredToBlock(
                gameData, gameQueryService, attacker);
        if (lure && minimumBlockers > 1) return 5;
        if (lure) return 4;
        if (mustBlockAttackers.contains(attackerIdx)) return 3;
        if (provokedByAttacker.containsKey(attackerIdx)) return 2;
        return 1;
    }

    // ===== Block legality check =====

    private boolean canBlock(GameData gameData, Permanent blocker, Permanent attacker) {
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        return blockLegalityService.canBlockAttacker(gameData, blocker, attacker, defenderBattlefield);
    }

    private Set<Integer> findLureAttackers(GameData gameData, List<Permanent> opponentBattlefield) {
        Set<Integer> lureIndices = new HashSet<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent attacker = opponentBattlefield.get(i);
            if (!attacker.isAttacking()) continue;
            boolean hasLure = attacker.isMustBeBlockedByAllThisTurn()
                    || attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(MustBeBlockedByAllCreaturesEffect.class::isInstance)
                    || gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedByAllCreaturesEffect.class);
            if (hasLure) {
                lureIndices.add(i);
            }
        }
        return lureIndices;
    }

    private Set<Integer> findMustBeBlockedAttackers(GameData gameData, List<Permanent> opponentBattlefield) {
        Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent attacker = opponentBattlefield.get(i);
            if (!attacker.isAttacking()) continue;
            boolean mustBeBlocked = attacker.isMustBeBlockedThisTurn()
                    || attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(MustBeBlockedIfAbleEffect.class::isInstance)
                    || gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedIfAbleEffect.class);
            if (mustBeBlocked) {
                indices.add(i);
            }
        }
        return indices;
    }

    // ===== Card Choice (random discard) =====

    @Override
    protected void handleCardChoice(GameData gameData) {
        if (!(gameData.interaction.activeInteraction() instanceof PendingInteraction.HandChoice cardChoice)) {
            return;
        }
        UUID choicePlayerId = cardChoice.playerId();
        List<Integer> validIndices = cardChoice.validIndices();

        if (!AiUtils.isRespondingFor(gameData, aiPlayer.getId(), choicePlayerId)) {
            return;
        }

        if (validIndices == null || validIndices.isEmpty()) {
            return;
        }

        // Pick a random valid index
        List<Integer> indices = new ArrayList<>(validIndices);
        int chosen = indices.get(rng.nextInt(indices.size()));

        log.info("Random AI: Choosing card at index {} in game {}", chosen, gameId);
        send(() -> gameActions.answerInteraction(new InteractionAnswer.CardIndexChosen(chosen)));
    }

    @Override
    protected void handleMayAbilityChoice(GameData gameData) {
        PendingInteraction.MayAbilityChoice mayChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        if (mayChoice == null
                || !AiUtils.isRespondingFor(gameData, aiPlayer.getId(), mayChoice.playerId())) {
            choiceHandler.handleMayAbilityChoice(gameData);
            return;
        }

        boolean accept = rng.nextBoolean() && floatManaForMayCost(gameData);
        log.info("Random AI: {} may ability '{}' in game {}",
                accept ? "Accepting" : "Declining", mayChoice.description(), gameId);
        send(() -> gameActions.answerInteraction(new InteractionAnswer.MayAbilityChosen(accept)));
    }

    // ===== Mulligan: mostly keep, occasionally mulligan =====

    @Override
    protected boolean shouldKeepHand(GameData gameData) {
        // Mulligan 10% of the time (at most twice) so the London mulligan and
        // card-bottoming paths get fuzzed without slowing games down much.
        int mulliganCount = gameData.mulliganCounts.getOrDefault(aiPlayer.getId(), 0);
        return mulliganCount >= 2 || rng.nextInt(10) > 0;
    }
}

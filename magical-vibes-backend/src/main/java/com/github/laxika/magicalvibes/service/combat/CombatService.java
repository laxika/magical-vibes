package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CombatDamagePhase1State;
import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;

import com.github.laxika.magicalvibes.model.effect.PutAwakeningCountersOnTargetLandsEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.model.CombatDamageTargetView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the complete combat phase lifecycle including attacker/blocker declaration,
 * combat damage calculation and assignment, and end-of-combat cleanup.
 *
 * <p>Handles all combat-related rules including first/double strike, trample, deathtouch,
 * lifelink, landwalk evasion, blocking restrictions, and combat triggers (ON_ATTACK, ON_BLOCK,
 * ON_COMBAT_DAMAGE_TO_PLAYER, ON_COMBAT_DAMAGE_TO_CREATURE).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    private static final Map<Keyword, CardSubtype> LANDWALK_MAP = Map.of(
            Keyword.FORESTWALK, CardSubtype.FOREST,
            Keyword.MOUNTAINWALK, CardSubtype.MOUNTAIN,
            Keyword.ISLANDWALK, CardSubtype.ISLAND,
            Keyword.SWAMPWALK, CardSubtype.SWAMP
    );

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    // ===== Query methods =====

    /**
     * Returns the battlefield indices of creatures the given player can legally declare as attackers.
     * Filters out tapped creatures, summoning-sick creatures without haste, defenders, and creatures
     * restricted by aura or land-based attack conditions.
     *
     * @param gameData the current game state
     * @param playerId the attacking player's ID
     * @return list of battlefield indices eligible to attack
     */
    public List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        UUID defenderId = gameQueryService.getOpponentId(gameData, playerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (canCreatureAttack(gameData, p, defenderBattlefield)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private boolean canCreatureAttack(GameData gameData, Permanent creature, List<Permanent> defenderBattlefield) {
        if (!gameQueryService.isCreature(gameData, creature)) return false;
        if (creature.isTapped()) return false;
        if (creature.isSummoningSick() && !gameQueryService.hasKeyword(gameData, creature, Keyword.HASTE)) return false;
        if (gameQueryService.hasKeyword(gameData, creature, Keyword.DEFENDER)) return false;
        if (gameQueryService.hasAuraWithEffect(gameData, creature, EnchantedCreatureCantAttackOrBlockEffect.class)) return false;
        if (isCantAttackDueToLandRestriction(gameData, creature, defenderBattlefield)) return false;
        return true;
    }

    private boolean isCantAttackDueToLandRestriction(GameData gameData, Permanent attacker, List<Permanent> defenderBattlefield) {
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantAttackUnlessDefenderControlsMatchingPermanentEffect restriction) {
                boolean defenderMatches = defenderBattlefield != null && defenderBattlefield.stream()
                        .anyMatch(p -> gameQueryService.matchesPermanentPredicate(gameData, p, restriction.defenderPermanentPredicate()));
                if (!defenderMatches) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCantBeBlockedDueToDefenderCondition(GameData gameData, Permanent attacker, List<Permanent> defenderBattlefield) {
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeBlockedIfDefenderControlsMatchingPermanentEffect restriction) {
                boolean defenderMatches = defenderBattlefield != null && defenderBattlefield.stream()
                        .anyMatch(p -> gameQueryService.matchesPermanentPredicate(gameData, p, restriction.defenderPermanentPredicate()));
                if (defenderMatches) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the subset of attackable indices whose creatures have at least one
     * "attacks each combat if able" requirement (e.g. {@link MustAttackEffect}).
     * Returns an empty list if an attack tax is in effect (CR 508.1d).
     *
     * @param gameData         the current game state
     * @param playerId         the attacking player's ID
     * @param attackableIndices indices already determined to be legal attackers
     * @return list of indices that must be declared as attackers
     */
    public List<Integer> getMustAttackIndices(GameData gameData, UUID playerId, List<Integer> attackableIndices) {
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return List.of();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Integer> mustAttack = new ArrayList<>();
        for (int idx : attackableIndices) {
            Permanent p = battlefield.get(idx);
            if (getMustAttackRequirementCount(gameData, p) > 0) {
                mustAttack.add(idx);
            }
        }
        return mustAttack;
    }

    /**
     * Counts "attacks each combat if able" requirements affecting this creature
     * from itself and attached Auras.
     */
    private int getMustAttackRequirementCount(GameData gameData, Permanent creature) {
        int[] count = {(int) creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(MustAttackEffect.class::isInstance)
                .count()};

        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getAttachedTo() != null
                    && permanent.getAttachedTo().equals(creature.getId())) {
                count[0] += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(MustAttackEffect.class::isInstance)
                        .count();
            }
        });

        return count[0];
    }

    /**
     * Per CR 508.1d, attack requirements must be satisfied as much as possible
     * without violating restrictions. If attacking has a tax/cost, no creature
     * is required to attack solely due to MustAttack requirements.
     */
    private void validateMaximumAttackRequirements(GameData gameData,
                                                   UUID playerId,
                                                   List<Integer> attackableIndices,
                                                   Set<Integer> declaredAttackerIndices) {
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);

        int maxRequirements = 0;
        for (int idx : attackableIndices) {
            maxRequirements += getMustAttackRequirementCount(gameData, battlefield.get(idx));
        }

        int satisfiedRequirements = 0;
        for (int idx : declaredAttackerIndices) {
            satisfiedRequirements += getMustAttackRequirementCount(gameData, battlefield.get(idx));
        }

        if (satisfiedRequirements < maxRequirements) {
            for (int idx : attackableIndices) {
                if (!declaredAttackerIndices.contains(idx)
                        && getMustAttackRequirementCount(gameData, battlefield.get(idx)) > 0) {
                    throw new IllegalStateException("Creature at index " + idx + " must attack this combat");
                }
            }
            throw new IllegalStateException("Attack declaration satisfies too few attack requirements");
        }
    }

    /**
     * Returns the battlefield indices of creatures the given player can legally declare as blockers.
     * Filters out tapped creatures, creatures with {@link CantBlockEffect}, creatures temporarily
     * prevented from blocking this turn, and creatures enchanted with effects that prevent blocking.
     *
     * @param gameData the current game state
     * @param playerId the defending player's ID
     * @return list of battlefield indices eligible to block
     */
    public List<Integer> getBlockableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (gameQueryService.isCreature(gameData, p)
                    && !p.isTapped()
                    && !p.isCantBlockThisTurn()
                    && p.getCard().getEffects(EffectSlot.STATIC).stream().noneMatch(CantBlockEffect.class::isInstance)
                    && !gameQueryService.hasAuraWithEffect(gameData, p, EnchantedCreatureCantAttackOrBlockEffect.class)) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Computes which attackers each potential blocker can legally block, accounting for
     * evasion abilities (flying, landwalk, fear, intimidate, shadow, horsemanship),
     * blocking restrictions, and "can only be blocked by" filters.
     *
     * @param gameData       the current game state
     * @param blockerIndices indices of potential blockers on the defender's battlefield
     * @param attackerIndices indices of declared attackers on the attacker's battlefield
     * @param defenderId     the defending player's ID
     * @param attackerId     the attacking player's ID
     * @return map from each blocker index to the list of attacker indices it can legally block
     */
    public Map<Integer, List<Integer>> computeLegalBlockPairs(GameData gameData,
                                                       List<Integer> blockerIndices,
                                                       List<Integer> attackerIndices,
                                                       UUID defenderId,
                                                       UUID attackerId) {
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
        Map<Integer, List<Integer>> pairs = new LinkedHashMap<>();
        for (int blockerIdx : blockerIndices) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            List<Integer> legalAttackers = new ArrayList<>();
            for (int attackerIdx : attackerIndices) {
                Permanent attacker = attackerBattlefield.get(attackerIdx);
                if (canBlockAttacker(gameData, blocker, attacker, defenderBattlefield)) {
                    legalAttackers.add(attackerIdx);
                }
            }
            pairs.put(blockerIdx, legalAttackers);
        }
        return pairs;
    }

    /**
     * Returns the battlefield indices of creatures currently declared as attackers for the given player.
     *
     * @param gameData the current game state
     * @param playerId the attacking player's ID
     * @return list of battlefield indices of attacking creatures
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

    // ===== Combat flow methods =====

    /**
     * Initiates the declare-attackers step. Computes which creatures can attack and which must attack,
     * then sends an {@link AvailableAttackersMessage} to the active player. If no creatures can attack,
     * the step is skipped automatically.
     *
     * @param gameData the current game state
     */
    public void handleDeclareAttackersStep(GameData gameData) {
        UUID activeId = gameData.activePlayerId;
        List<Integer> attackable = getAttackableCreatureIndices(gameData, activeId);

        if (attackable.isEmpty()) {
            String playerName = gameData.playerIdToName.get(activeId);
            log.info("Game {} - {} has no creatures that can attack, skipping combat", gameData.id, playerName);
            return;
        }

        List<Integer> mustAttack = getMustAttackIndices(gameData, activeId, attackable);
        gameData.interaction.beginAttackerDeclaration(activeId);
        sessionManager.sendToPlayer(getEffectiveRecipient(gameData, activeId), new AvailableAttackersMessage(attackable, mustAttack));
    }

    CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.ATTACKER_DECLARATION)) {
            throw new IllegalStateException("Not awaiting attacker declaration");
        }
        if (!player.getId().equals(gameData.activePlayerId)) {
            throw new IllegalStateException("Only the active player can declare attackers");
        }

        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Integer> attackable = getAttackableCreatureIndices(gameData, playerId);

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

        // Validate attack requirements (CR 508.1d: satisfy as many as possible)
        validateMaximumAttackRequirements(gameData, playerId, attackable, uniqueIndices);

        gameData.interaction.clearAwaitingInput();

        if (attackerIndices.isEmpty()) {
            log.info("Game {} - {} declares no attackers", gameData.id, player.getUsername());
            gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + " declares no attackers.");
            return CombatResult.AUTO_PASS_ONLY;
        }

        // Check attack tax (e.g. Windborn Muse / Ghostly Prison)
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            int totalTax = taxPerCreature * attackerIndices.size();
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (pool.getTotal() < totalTax) {
                throw new IllegalStateException("Not enough mana to pay attack tax (" + totalTax + " required)");
            }
            gameHelper.payGenericMana(pool, totalTax);

        }

        // Mark creatures as attacking and tap them (vigilance skips tapping)
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            attacker.setAttacking(true);
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.VIGILANCE)) {
                attacker.tap();
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, attacker);
            }
        }

        String logEntry = player.getUsername() + " declares " + attackerIndices.size() +
                " attacker" + (attackerIndices.size() > 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);



        // Collect all attack-step triggers, then reorder per APNAP (CR 603.3b)
        int stackSizeBeforeAttackTriggers = gameData.stack.size();

        // Check for "when this creature attacks" triggers
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!attacker.getCard().getEffects(EffectSlot.ON_ATTACK).isEmpty()) {
                List<CardEffect> effects = new ArrayList<>(attacker.getCard().getEffects(EffectSlot.ON_ATTACK));
                boolean needsTarget = effects.stream().anyMatch(CardEffect::canTargetPermanent);
                if (needsTarget) {
                    gameData.pendingAttackTriggerTargets.add(
                            new PermanentChoiceContext.AttackTriggerTarget(
                                    attacker.getCard(), playerId, effects, attacker.getId()));
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            attacker.getCard(),
                            playerId,
                            attacker.getCard().getName() + "'s attack trigger",
                            effects,
                            null,
                            attacker.getId()
                    ));
                }
                String triggerLog = attacker.getCard().getName() + "'s attack ability triggers.";
                gameData.gameLog.add(triggerLog);
                log.info("Game {} - {} attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
            }

            // Check for aura-based "when enchanted creature attacks" triggers
            checkAuraTriggersForCreature(gameData, attacker, EffectSlot.ON_ATTACK);
        }

        // APNAP: active player's triggers on bottom, non-active player's on top (resolves first)
        reorderTriggersAPNAP(gameData, stackSizeBeforeAttackTriggers, playerId);

        log.info("Game {} - {} declares {} attackers", gameData.id, player.getUsername(), attackerIndices.size());

        return CombatResult.AUTO_PASS_ONLY;
    }

    CombatResult handleDeclareBlockersStep(GameData gameData) {
        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);
        List<Integer> attackerIndices = getAttackingCreatureIndices(gameData, activeId);

        // Filter out attackers that can't be blocked
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        attackerIndices = attackerIndices.stream()
                .filter(idx -> !gameQueryService.hasCantBeBlocked(gameData, attackerBattlefield.get(idx)))
                .filter(idx -> !isCantBeBlockedDueToDefenderCondition(gameData, attackerBattlefield.get(idx), defenderBattlefield))
                .toList();

        if (blockable.isEmpty() || attackerIndices.isEmpty()) {
            log.info("Game {} - Defending player has no creatures that can block or no blockable attackers", gameData.id);
            return CombatResult.ADVANCE_ONLY;
        }

        Map<Integer, List<Integer>> legalPairs = computeLegalBlockPairs(gameData, blockable, attackerIndices, defenderId, activeId);
        gameData.interaction.beginBlockerDeclaration(defenderId);
        sessionManager.sendToPlayer(getEffectiveRecipient(gameData, defenderId), new AvailableBlockersMessage(blockable, attackerIndices, legalPairs));
        return CombatResult.DONE;
    }

    CombatResult declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.BLOCKER_DECLARATION)) {
            throw new IllegalStateException("Not awaiting blocker declaration");
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);

        if (!player.getId().equals(defenderId)) {
            throw new IllegalStateException("Only the defending player can declare blockers");
        }

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);

        // Validate assignments
        Map<Integer, Integer> blockerUsageCount = new HashMap<>();
        Set<String> blockerAttackerPairs = new HashSet<>();
        Map<Integer, Integer> blockersPerAttacker = new HashMap<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            int blockerIdx = assignment.blockerIndex();
            int attackerIdx = assignment.attackerIndex();

            if (!blockable.contains(blockerIdx)) {
                throw new IllegalStateException("Invalid blocker index: " + blockerIdx);
            }
            int usageCount = blockerUsageCount.merge(blockerIdx, 1, Integer::sum);
            int maxBlocks = getMaxBlocksForCreature(defenderBattlefield.get(blockerIdx), defenderBattlefield);
            if (usageCount > maxBlocks) {
                throw new IllegalStateException("Blocker " + blockerIdx + " assigned too many times");
            }
            if (!blockerAttackerPairs.add(blockerIdx + ":" + attackerIdx)) {
                throw new IllegalStateException("Duplicate blocker-attacker pair: " + blockerIdx + " -> " + attackerIdx);
            }
            if (attackerIdx < 0 || attackerIdx >= attackerBattlefield.size() || !attackerBattlefield.get(attackerIdx).isAttacking()) {
                throw new IllegalStateException("Invalid attacker index: " + attackerIdx);
            }

            Permanent attacker = attackerBattlefield.get(attackerIdx);
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            getBlockingIllegalityReason(gameData, blocker, attacker, defenderBattlefield)
                    .ifPresent(reason -> { throw new IllegalStateException(reason); });

            blockersPerAttacker.merge(attackerIdx, 1, Integer::sum);
        }

        for (var entry : blockersPerAttacker.entrySet()) {
            int attackerIdx = entry.getKey();
            int blockerCount = entry.getValue();
            Permanent attacker = attackerBattlefield.get(attackerIdx);
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.MENACE) && blockerCount == 1) {
                throw new IllegalStateException(attacker.getCard().getName() + " can't be blocked except by two or more creatures");
            }
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CanBeBlockedByAtMostNCreaturesEffect restriction
                        && blockerCount > restriction.maxBlockers()) {
                    throw new IllegalStateException(attacker.getCard().getName()
                            + " can't be blocked by more than " + restriction.maxBlockers()
                            + " creature" + (restriction.maxBlockers() == 1 ? "" : "s"));
                }
            }
        }

        validateMaximumBlockRequirements(gameData, attackerBattlefield, defenderBattlefield, blockable,
                blockerAssignments);
        validatePerCreatureMustBlockRequirements(gameData, attackerBattlefield, defenderBattlefield, blockable,
                blockerAssignments);

        gameData.interaction.clearAwaitingInput();

        // Mark creatures as blocking
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(assignment.attackerIndex());
        }

        if (!blockerAssignments.isEmpty()) {
            String logEntry = player.getUsername() + " declares " + blockerAssignments.size() +
                    " blocker" + (blockerAssignments.size() > 1 ? "s" : "") + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        // Collect all blocker-step triggers, then reorder per APNAP (CR 603.3b)
        int stackSizeBeforeBlockerTriggers = gameData.stack.size();

        // Check for "when this creature blocks" triggers (defending player's / NAP's)
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            if (!blocker.getCard().getEffects(EffectSlot.ON_BLOCK).isEmpty()) {
                Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());

                // Resolve conditional block effects (e.g. "when blocking a creature with flying")
                List<CardEffect> blockEffects = new ArrayList<>();
                for (CardEffect e : blocker.getCard().getEffects(EffectSlot.ON_BLOCK)) {
                    if (e instanceof BoostSelfWhenBlockingKeywordEffect kwEffect) {
                        if (gameQueryService.hasKeyword(gameData, attacker, kwEffect.requiredKeyword())) {
                            blockEffects.add(new BoostSelfEffect(kwEffect.powerBoost(), kwEffect.toughnessBoost()));
                        }
                    } else {
                        blockEffects.add(e);
                    }
                }
                if (blockEffects.isEmpty()) continue;

                // Set target: attacker ID for effects that need it (e.g. DestroyBlockedCreatureAndSelfEffect),
                // otherwise blocker's own ID for self-targeting effects (e.g. BoostSelfEffect)
                boolean needsAttackerTarget = blockEffects.stream()
                        .anyMatch(e -> e instanceof DestroyBlockedCreatureAndSelfEffect
                                || e instanceof DestroyTargetCreatureAndGainLifeEqualToToughnessEffect);
                StackEntry blockTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        blocker.getCard(),
                        defenderId,
                        blocker.getCard().getName() + "'s block trigger",
                        new ArrayList<>(blockEffects),
                        needsAttackerTarget ? attacker.getId() : blocker.getId(),
                        blocker.getId()
                );
                // Block triggers reference "that creature" but don't target — they can't fizzle
                blockTrigger.setNonTargeting(true);
                gameData.stack.add(blockTrigger);
                String triggerLog = blocker.getCard().getName() + "'s block ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} block trigger pushed onto stack", gameData.id, blocker.getCard().getName());
            }

            // Check for aura-based "when enchanted creature blocks" triggers
            checkAuraTriggersForCreature(gameData, blocker, EffectSlot.ON_BLOCK);
        }

        // Check for "when this creature becomes blocked" triggers (active player's / AP's)
        Set<Integer> blockedAttackerIndices = new LinkedHashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            blockedAttackerIndices.add(assignment.attackerIndex());
        }
        for (int atkIdx : blockedAttackerIndices) {
            Permanent attacker = attackerBattlefield.get(atkIdx);
            List<EffectRegistration> becomesBlockedRegs = attacker.getCard().getEffectRegistrations(EffectSlot.ON_BECOMES_BLOCKED);
            if (!becomesBlockedRegs.isEmpty()) {
                List<CardEffect> blockerSpecificEffects = becomesBlockedRegs.stream()
                        .filter(r -> r.triggerMode() == TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
                        .toList();
                List<CardEffect> regularEffects = becomesBlockedRegs.stream()
                        .filter(r -> r.triggerMode() != TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
                        .toList();

                if (!regularEffects.isEmpty()) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            attacker.getCard(),
                            activeId,
                            attacker.getCard().getName() + "'s becomes-blocked trigger",
                            new ArrayList<>(regularEffects),
                            attacker.getId(),
                            attacker.getId()
                    ));
                    String triggerLog = attacker.getCard().getName() + "'s becomes-blocked ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} becomes-blocked trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                }

                if (!blockerSpecificEffects.isEmpty()) {
                    for (BlockerAssignment assignment : blockerAssignments) {
                        if (assignment.attackerIndex() != atkIdx) {
                            continue;
                        }
                        Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
                        StackEntry trigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                attacker.getCard(),
                                activeId,
                                attacker.getCard().getName() + "'s becomes-blocked trigger",
                                new ArrayList<>(blockerSpecificEffects),
                                blocker.getId(),
                                attacker.getId()
                        );
                        // "That creature" wording references a blocker without targeting it.
                        trigger.setNonTargeting(true);
                        gameData.stack.add(trigger);
                        String triggerLog = attacker.getCard().getName() + "'s becomes-blocked ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} becomes-blocked trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                    }
                }
            }

            // Check for aura/equipment-based "when enchanted/equipped creature becomes blocked" triggers
            checkAuraTriggersForCreature(gameData, attacker, EffectSlot.ON_BECOMES_BLOCKED);
            checkAttachedPerBlockerTriggers(gameData, attacker, blockerAssignments, defenderBattlefield, atkIdx);
        }

        // APNAP: active player's triggers on bottom, non-active player's on top (resolves first)
        reorderTriggersAPNAP(gameData, stackSizeBeforeBlockerTriggers, activeId);

        log.info("Game {} - {} declares {} blockers", gameData.id, player.getUsername(), blockerAssignments.size());

        return CombatResult.AUTO_PASS_ONLY;
    }

    private int getMaxBlocksForCreature(Permanent creature, List<Permanent> battlefield) {
        int additionalBlocks = 0;
        for (Permanent p : battlefield) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantAdditionalBlockEffect e) {
                    boolean isAttachable = p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                            || p.getCard().isAura();
                    if (isAttachable) {
                        // Equipment/aura: only applies when attached to this creature
                        if (creature.getId().equals(p.getAttachedTo())) {
                            additionalBlocks += e.additionalBlocks();
                        }
                    } else {
                        // Global effect (e.g. High Ground): applies to all creatures
                        additionalBlocks += e.additionalBlocks();
                    }
                }
            }
        }
        return 1 + additionalBlocks;
    }

    private void validateMaximumBlockRequirements(GameData gameData,
                                                  List<Permanent> attackerBattlefield,
                                                  List<Permanent> defenderBattlefield,
                                                  List<Integer> blockable,
                                                  List<BlockerAssignment> blockerAssignments) {
        Set<Integer> lureAttackerIndices = new HashSet<>();
        for (int i = 0; i < attackerBattlefield.size(); i++) {
            Permanent attacker = attackerBattlefield.get(i);
            if (!attacker.isAttacking()) continue;
            boolean hasRequirement = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(MustBeBlockedByAllCreaturesEffect.class::isInstance)
                    || gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedByAllCreaturesEffect.class);
            if (hasRequirement) {
                lureAttackerIndices.add(i);
            }
        }
        if (lureAttackerIndices.isEmpty()) {
            return;
        }

        for (int blockerIdx : blockable) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            int currentLureBlocks = 0;
            for (BlockerAssignment assignment : blockerAssignments) {
                if (assignment.blockerIndex() == blockerIdx && lureAttackerIndices.contains(assignment.attackerIndex())) {
                    currentLureBlocks++;
                }
            }

            int possibleLureBlocks = 0;
            for (int attackerIdx : lureAttackerIndices) {
                Permanent attacker = attackerBattlefield.get(attackerIdx);
                if (canBlockAttacker(gameData, blocker, attacker, defenderBattlefield)) {
                    possibleLureBlocks++;
                }
            }

            int maxSatisfiable = Math.min(getMaxBlocksForCreature(blocker, defenderBattlefield), possibleLureBlocks);
            if (currentLureBlocks < maxSatisfiable) {
                throw new IllegalStateException(blocker.getCard().getName() + " must block enchanted creature if able");
            }
        }
    }

    private void validatePerCreatureMustBlockRequirements(GameData gameData,
                                                          List<Permanent> attackerBattlefield,
                                                          List<Permanent> defenderBattlefield,
                                                          List<Integer> blockable,
                                                          List<BlockerAssignment> blockerAssignments) {
        for (int blockerIdx : blockable) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            if (blocker.getMustBlockIds().isEmpty()) {
                continue;
            }

            // Find which must-block sources are attacking and can be legally blocked
            Set<Integer> requiredAttackerIndices = new HashSet<>();
            for (UUID mustBlockId : blocker.getMustBlockIds()) {
                for (int i = 0; i < attackerBattlefield.size(); i++) {
                    Permanent attacker = attackerBattlefield.get(i);
                    if (attacker.isAttacking() && attacker.getId().equals(mustBlockId)
                            && canBlockAttacker(gameData, blocker, attacker, defenderBattlefield)) {
                        requiredAttackerIndices.add(i);
                    }
                }
            }

            if (requiredAttackerIndices.isEmpty()) {
                continue;
            }

            int currentMustBlocks = 0;
            for (BlockerAssignment assignment : blockerAssignments) {
                if (assignment.blockerIndex() == blockerIdx && requiredAttackerIndices.contains(assignment.attackerIndex())) {
                    currentMustBlocks++;
                }
            }

            int maxSatisfiable = Math.min(getMaxBlocksForCreature(blocker, defenderBattlefield), requiredAttackerIndices.size());
            if (currentMustBlocks < maxSatisfiable) {
                throw new IllegalStateException(blocker.getCard().getName() + " must block target creature this turn if able");
            }
        }
    }

    private boolean canBlockAttacker(GameData gameData,
                                     Permanent blocker,
                                     Permanent attacker,
                                     List<Permanent> defenderBattlefield) {
        return getBlockingIllegalityReason(gameData, blocker, attacker, defenderBattlefield).isEmpty();
    }

    /**
     * Returns the reason why the given blocker cannot legally block the given attacker,
     * or empty if the block is legal. Used by both declareBlockers (throws) and
     * canBlockAttacker (returns false).
     */
    private Optional<String> getBlockingIllegalityReason(GameData gameData,
                                                          Permanent blocker,
                                                          Permanent attacker,
                                                          List<Permanent> defenderBattlefield) {
        if (gameQueryService.hasCantBeBlocked(gameData, attacker)) {
            return Optional.of(attacker.getCard().getName() + " can't be blocked");
        }
        if (isCantBeBlockedDueToDefenderCondition(gameData, attacker, defenderBattlefield)) {
            return Optional.of(attacker.getCard().getName() + " can't be blocked");
        }
        if (gameQueryService.hasKeyword(gameData, attacker, Keyword.FLYING)
                && !gameQueryService.hasKeyword(gameData, blocker, Keyword.FLYING)
                && !gameQueryService.hasKeyword(gameData, blocker, Keyword.REACH)) {
            return Optional.of(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (flying)");
        }
        if (gameQueryService.hasKeyword(gameData, attacker, Keyword.FEAR)
                && !gameQueryService.isArtifact(blocker)
                && blocker.getEffectiveColor() != CardColor.BLACK) {
            return Optional.of(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (fear)");
        }
        if (gameQueryService.hasKeyword(gameData, attacker, Keyword.INTIMIDATE)
                && !gameQueryService.isArtifact(blocker)
                && blocker.getEffectiveColor() != attacker.getEffectiveColor()) {
            return Optional.of(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (intimidate)");
        }
        for (CardEffect blockerStaticEffect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
            if (blockerStaticEffect instanceof CanBlockOnlyIfAttackerMatchesPredicateEffect restriction
                    && !gameQueryService.matchesPermanentPredicate(gameData, attacker, restriction.attackerPredicate())) {
                return Optional.of(blocker.getCard().getName() + " can only block " + restriction.allowedAttackersDescription());
            }
        }
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanBeBlockedOnlyByFilterEffect restriction
                    && !gameQueryService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return Optional.of(attacker.getCard().getName() + " can only be blocked by " + restriction.allowedBlockersDescription());
            }
        }
        for (CanBeBlockedOnlyByFilterEffect restriction : getAuraGrantedBlockingRestrictions(gameData, attacker)) {
            if (!gameQueryService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return Optional.of(attacker.getCard().getName() + " can only be blocked by " + restriction.allowedBlockersDescription());
            }
        }
        for (var entry : LANDWALK_MAP.entrySet()) {
            if (gameQueryService.hasKeyword(gameData, attacker, entry.getKey())
                    && defenderBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(entry.getValue()))) {
                return Optional.of(attacker.getCard().getName() + " can't be blocked (" + entry.getValue().getDisplayName().toLowerCase() + "walk)");
            }
        }
        if (blocker.isCantBlockThisTurn()) {
            return Optional.of(blocker.getCard().getName() + " can't block this turn");
        }
        boolean hasCantBlockStatic = blocker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantBlockEffect.class::isInstance);
        if (hasCantBlockStatic) {
            return Optional.of(blocker.getCard().getName() + " can't block");
        }
        if (blocker.getCantBlockIds().contains(attacker.getId())) {
            return Optional.of(blocker.getCard().getName() + " can't block " + attacker.getCard().getName() + " this turn");
        }
        if (gameQueryService.hasProtectionFrom(gameData, attacker, blocker.getEffectiveColor())) {
            return Optional.of(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (protection)");
        }
        return Optional.empty();
    }

    // ===== Combat damage resolution =====

    CombatResult resolveCombatDamage(GameData gameData) {
        if (gameData.preventAllCombatDamage) {
            String logEntry = "All combat damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            return CombatResult.ADVANCE_AND_AUTO_PASS;
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);

        List<Permanent> atkBf = gameData.playerBattlefields.get(activeId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);

        // Re-entry: if pending damage assignments remain, send next assignment request
        if (gameData.combatDamagePhase1Complete && !gameData.combatDamagePendingIndices.isEmpty()) {
            sendNextCombatDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
            return CombatResult.DONE;
        }

        // Check for combat damage redirect (e.g. Kjeldoran Royal Guard)
        Permanent redirectTarget = gameData.combatDamageRedirectTarget != null
                ? gameQueryService.findPermanentById(gameData, gameData.combatDamageRedirectTarget) : null;

        List<Integer> attackingIndices = getAttackingCreatureIndices(gameData, activeId);

        // Build blocker map: attackerIndex -> list of blockerIndices
        Map<Integer, List<Integer>> blockerMap = new LinkedHashMap<>();
        for (int atkIdx : attackingIndices) {
            List<Integer> blockers = new ArrayList<>();
            for (int i = 0; i < defBf.size(); i++) {
                if (defBf.get(i).isBlocking() && defBf.get(i).getBlockingTargets().contains(atkIdx)) {
                    blockers.add(i);
                }
            }
            blockerMap.put(atkIdx, blockers);
        }

        // Check if any combat creature has first strike or double strike
        boolean anyFirstStrike = attackingIndices.stream()
                        .anyMatch(i -> hasFirstOrDoubleStrike(gameData, atkBf.get(i)))
                || blockerMap.values().stream().flatMapToInt(l -> l.stream().mapToInt(i -> i))
                        .anyMatch(i -> hasFirstOrDoubleStrike(gameData, defBf.get(i)));

        CombatDamageState state = new CombatDamageState();

        // Restore phase 1 state if re-entering after damage assignment
        if (gameData.combatDamagePhase1Complete) {
            CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
            state.deadAttackerIndices.addAll(p1.deadAttackerIndices);
            state.deadDefenderIndices.addAll(p1.deadDefenderIndices);
            state.atkDamageTaken.putAll(p1.atkDamageTaken);
            state.defDamageTaken.putAll(p1.defDamageTaken);
            state.damageToDefendingPlayer = p1.damageToDefendingPlayer;
            state.damageRedirectedToGuard = p1.damageRedirectedToGuard;
            for (var e : p1.combatDamageDealt.entrySet()) {
                Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
                if (perm != null) state.combatDamageDealt.put(perm, e.getValue());
            }
            for (var e : p1.combatDamageDealtToPlayer.entrySet()) {
                Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
                if (perm != null) state.combatDamageDealtToPlayer.put(perm, e.getValue());
            }
            for (var e : p1.combatDamageDealtToCreatures.entrySet()) {
                Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
                if (perm != null) state.combatDamageDealtToCreatures.put(perm, new ArrayList<>(e.getValue()));
            }
            for (var e : p1.combatDamageDealerControllers.entrySet()) {
                Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
                if (perm != null) state.combatDamageDealerControllers.put(perm, e.getValue());
            }
            state.deathtouchDamagedAttackerIndices.addAll(p1.deathtouchDamagedAttackerIndices);
            state.deathtouchDamagedDefenderIndices.addAll(p1.deathtouchDamagedDefenderIndices);
        }

        // Phase 1: First strike damage (skip on re-entry)
        if (!gameData.combatDamagePhase1Complete && anyFirstStrike) {
            resolveDamagePhase(gameData, state, blockerMap, atkBf, defBf,
                    attackingIndices, activeId, defenderId, redirectTarget, true);
        }

        // Check if any phase 2 attackers need manual damage assignment
        if (!gameData.combatDamagePhase1Complete) {
            List<Integer> needsManual = new ArrayList<>();
            for (var bEntry : blockerMap.entrySet()) {
                int bAtkIdx = bEntry.getKey();
                if (state.deadAttackerIndices.contains(bAtkIdx)) continue;
                Permanent bAtk = atkBf.get(bAtkIdx);
                boolean bAtkSkipPhase2 = gameQueryService.hasKeyword(gameData, bAtk, Keyword.FIRST_STRIKE)
                        && !gameQueryService.hasKeyword(gameData, bAtk, Keyword.DOUBLE_STRIKE);
                if (bAtkSkipPhase2) continue;
                if (gameQueryService.isPreventedFromDealingDamage(gameData, bAtk)) continue;
                List<Integer> livingBlockers = bEntry.getValue().stream()
                        .filter(i -> !state.deadDefenderIndices.contains(i))
                        .toList();
                if (needsManualDamageAssignment(gameData, bAtk, livingBlockers)) {
                    needsManual.add(bAtkIdx);
                }
            }
            if (!needsManual.isEmpty()) {
                gameData.combatDamagePhase1State = savePhase1State(state, blockerMap, anyFirstStrike);
                gameData.combatDamagePhase1Complete = true;
                gameData.combatDamagePendingIndices.addAll(needsManual);
                sendNextCombatDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
                return CombatResult.DONE;
            }
        }

        resolveDamagePhase(gameData, state, blockerMap, atkBf, defBf,
                attackingIndices, activeId, defenderId, redirectTarget, false);

        resolveRedirectedDamage(gameData, state, redirectTarget);

        // Process lifelink before removing dead creatures
        processLifelink(gameData, state.combatDamageDealt);
        processGainLifeEqualToDamageDealt(gameData, state.combatDamageDealt);

        List<String> deadCreatureNames = removeDeadCreatures(gameData, state, atkBf, defBf, activeId, defenderId);

        applyPlayerDamage(gameData, state, defenderId);

        if (!deadCreatureNames.isEmpty()) {
            String logEntry = String.join(", ", deadCreatureNames) + " died in combat.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - Combat damage resolved: {} damage to defender, {} creatures died",
                gameData.id, state.damageToDefendingPlayer, state.deadAttackerIndices.size() + state.deadDefenderIndices.size());

        // Check win condition
        if (gameHelper.checkWinCondition(gameData)) {
            return CombatResult.DONE;
        }

        int stackSizeBeforeDamageTriggers = gameData.stack.size();
        processCombatDamageToCreatureTriggers(gameData, state.combatDamageDealtToCreatures, state.combatDamageDealerControllers);

        // Process combat damage to player triggers (e.g. Cephalid Constable) after all combat is resolved
        processCombatDamageToPlayerTriggers(gameData, state.combatDamageDealtToPlayer, activeId, defenderId);

        // Process defender-side damage triggers (e.g. Dissipation Field)
        for (var dmgEntry : state.combatDamageDealtToPlayer.entrySet()) {
            if (dmgEntry.getValue() > 0) {
                triggerCollectionService.checkDamageDealtToControllerTriggers(gameData, defenderId, dmgEntry.getKey().getId());
            }
        }

        reorderTriggersAPNAP(gameData, stackSizeBeforeDamageTriggers, activeId);
        if (gameData.interaction.isAwaitingInput()) {
            return CombatResult.DONE;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return CombatResult.DONE;
        }

        // If combat damage triggers were added to the stack, don't advance the step yet —
        // let the auto-pass loop resolve the stack first, then it will advance naturally.
        if (gameData.stack.size() > stackSizeBeforeDamageTriggers) {
            return CombatResult.AUTO_PASS_RESOLVE_COMBAT_TRIGGERS;
        }

        return CombatResult.ADVANCE_AND_AUTO_PASS;
    }

    private void processLifelink(GameData gameData, Map<Permanent, Integer> combatDamageDealt) {
        for (var entry : combatDamageDealt.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            if (!gameQueryService.hasKeyword(gameData, creature, Keyword.LIFELINK)) continue;

            UUID controllerId = findControllerOf(gameData, creature);
            if (controllerId == null) continue;

            grantLifeToPlayer(gameData, controllerId, damageDealt, "lifelink");
        }
    }

    private void processGainLifeEqualToDamageDealt(GameData gameData, Map<Permanent, Integer> combatDamageDealt) {
        for (var entry : combatDamageDealt.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            gameData.forEachPermanent((playerId, perm) -> {
                if (perm.getAttachedTo() != null && perm.getAttachedTo().equals(creature.getId())) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof GainLifeEqualToDamageDealtEffect) {
                            grantLifeToPlayer(gameData, playerId, damageDealt, perm.getCard().getName());
                        }
                    }
                }
            });
        }
    }

    private void grantLifeToPlayer(GameData gameData, UUID playerId, int amount, String source) {
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(playerId) + "'s life total can't change.");
            return;
        }
        int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
        gameData.playerLifeTotals.put(playerId, currentLife + amount);
        String logEntry = gameData.playerIdToName.get(playerId) + " gains " + amount + " life from " + source + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }

    private void processCombatDamageToPlayerTriggers(GameData gameData, Map<Permanent, Integer> combatDamageDealtToPlayer, UUID attackerId, UUID defenderId) {
        for (var entry : combatDamageDealtToPlayer.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            // Track which permanents dealt combat damage to which players this turn
            gameData.combatDamageToPlayersThisTurn
                    .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                    .add(defenderId);

            List<CardEffect> allDamageEffects = new ArrayList<>();
            allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
            allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
            for (CardEffect effect : allDamageEffects) {
                // Metalcraft: intervening-if check at trigger time
                if (effect instanceof MetalcraftConditionalEffect metalcraft) {
                    List<Permanent> bf = gameData.playerBattlefields.get(attackerId);
                    long artifactCount = bf == null ? 0 : bf.stream()
                            .filter(gameQueryService::isArtifact)
                            .count();
                    if (artifactCount < 3) {
                        log.info("Game {} - {}'s metalcraft combat damage trigger does not fire (only {} artifacts)",
                                gameData.id, creature.getCard().getName(), artifactCount);
                        continue;
                    }
                    StackEntry se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability",
                            List.of(metalcraft), defenderId, creature.getId());
                    se.setNonTargeting(true);
                    gameData.stack.add(se);
                    gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName() + "'s metalcraft ability triggers:");
                    continue;
                }

                // TargetPlayerLosesGame: wrap effect with defenderId, custom log
                if (effect instanceof TargetPlayerLosesGameEffect) {
                    gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability",
                            List.of(new TargetPlayerLosesGameEffect(defenderId)), null, creature.getId()));
                    gameBroadcastService.logAndBroadcast(gameData,
                            creature.getCard().getName() + "'s ability triggers \u2014 " + gameData.playerIdToName.get(defenderId) + " loses the game.");
                    continue;
                }

                // Common path: build StackEntry based on effect type, then push + log
                String desc = creature.getCard().getName() + "'s triggered ability";
                StackEntry se;
                if (effect instanceof ReturnPermanentsOnCombatDamageToPlayerEffect) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), damageDealt, defenderId, null);
                } else if (effect instanceof ExileTopCardsRepeatOnDuplicateEffect
                        || effect instanceof TargetPlayerRandomDiscardEffect) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), defenderId, creature.getId());
                } else {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect));
                }
                se.setNonTargeting(true);
                gameData.stack.add(se);
                gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName() + "'s combat damage trigger goes on the stack.");
            }

            // Check attached equipment/auras for combat damage to player triggers
            checkAttachedCombatDamageToPlayerTriggers(gameData, creature, attackerId, defenderId);
        }
    }

    private void checkAttachedCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId, UUID defenderId) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.getAttachedTo() != null && perm.getAttachedTo().equals(creature.getId())) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER);
                if (!effects.isEmpty()) {
                    StackEntry se = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            attackerId,
                            perm.getCard().getName() + "'s triggered ability",
                            new ArrayList<>(effects),
                            defenderId,
                            perm.getId()
                    );
                    se.setNonTargeting(true);
                    gameData.stack.add(se);
                    String logEntry = perm.getCard().getName() + "'s combat damage trigger goes on the stack.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        });
    }

    private void processCombatDamageToCreatureTriggers(GameData gameData,
                                                       Map<Permanent, List<UUID>> combatDamageDealtToCreatures,
                                                       Map<Permanent, UUID> combatDamageDealerControllers) {
        for (var entry : combatDamageDealtToCreatures.entrySet()) {
            Permanent source = entry.getKey();
            UUID controllerId = combatDamageDealerControllers.get(source);
            if (controllerId == null) {
                continue;
            }

            List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE);
            if (effects.isEmpty()) {
                continue;
            }

            for (UUID damagedCreatureId : entry.getValue()) {
                for (CardEffect effect : effects) {
                    if (effect instanceof DestroyTargetPermanentEffect destroyEffect) {
                        StackEntry trigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                source.getCard(),
                                controllerId,
                                source.getCard().getName() + "'s triggered ability",
                                List.of(destroyEffect),
                                damagedCreatureId,
                                source.getId()
                        );
                        trigger.setNonTargeting(true);
                        gameData.stack.add(trigger);
                        String logEntry = source.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    }
                }
            }
        }
    }

    private void recordCombatDamageToCreature(GameData gameData,
                                              CombatDamageState state,
                                              Permanent source,
                                              UUID controllerId,
                                              Permanent target,
                                              int damage) {
        if (damage <= 0) {
            return;
        }
        state.combatDamageDealerControllers.putIfAbsent(source, controllerId);
        state.combatDamageDealtToCreatures.computeIfAbsent(source, ignored -> new ArrayList<>()).add(target.getId());
        gameHelper.recordCreatureDamagedByPermanent(gameData, source.getId(), target, damage);
    }

    // ===== Combat damage phase submethods =====

    /**
     * Resolves one phase of combat damage. Phase 1 (first strike) only processes creatures with
     * first/double strike; phase 2 (regular) processes all remaining creatures, skipping those
     * killed in phase 1 and those that already dealt first-strike-only damage.
     *
     * @param isFirstStrikePhase true for phase 1, false for phase 2
     */
    private void resolveDamagePhase(GameData gameData, CombatDamageState state,
                                     Map<Integer, List<Integer>> blockerMap,
                                     List<Permanent> atkBf, List<Permanent> defBf,
                                     List<Integer> attackingIndices,
                                     UUID activeId, UUID defenderId,
                                     Permanent redirectTarget, boolean isFirstStrikePhase) {
        Map<Integer, Integer> blockerDamage = precomputeBlockerDamage(
                gameData, blockerMap, defBf, state.deadDefenderIndices, isFirstStrikePhase);

        for (var entry : blockerMap.entrySet()) {
            int atkIdx = entry.getKey();
            List<Integer> blkIndices = entry.getValue();

            // Phase 2: skip attackers killed in phase 1
            if (!isFirstStrikePhase && state.deadAttackerIndices.contains(atkIdx)) continue;

            Permanent atk = atkBf.get(atkIdx);
            boolean atkParticipates = participatesInDamagePhase(gameData, atk, isFirstStrikePhase);

            // Phase 2: check for player-provided damage assignment
            Map<UUID, Integer> playerAssignment = isFirstStrikePhase ? null
                    : gameData.combatDamagePlayerAssignments.get(atkIdx);

            boolean assignAsUnblocked = !blkIndices.isEmpty() && assignsCombatDamageAsThoughUnblocked(atk);

            if (playerAssignment != null) {
                // Player assigned damage manually (phase 2 only)
                if (atkParticipates && !gameQueryService.isPreventedFromDealingDamage(gameData, atk)) {
                    applyPlayerAssignedDamage(gameData, state, atk, blkIndices, defBf,
                            playerAssignment, activeId, defenderId, redirectTarget);
                }
            } else if (blkIndices.isEmpty() || assignAsUnblocked) {
                // Unblocked attacker deals damage to player (or redirect target)
                if (atkParticipates && !gameQueryService.isPreventedFromDealingDamage(gameData, atk)) {
                    int power = gameQueryService.applyDamageMultiplier(gameData, gameQueryService.getEffectiveCombatDamage(gameData, atk));
                    accumulatePlayerDamage(gameData, atk, power, defenderId, redirectTarget, state);
                }
            } else {
                // Attacker deals damage to blockers
                if (atkParticipates && !gameQueryService.isPreventedFromDealingDamage(gameData, atk)) {
                    distributeAttackerDamageToBlockers(gameData, state, atk, blkIndices, defBf,
                            activeId, defenderId, redirectTarget, !isFirstStrikePhase);
                }
            }

            if (!blkIndices.isEmpty()) {
                // Blockers deal damage to attacker
                for (int blkIdx : blkIndices) {
                    if (!isFirstStrikePhase && state.deadDefenderIndices.contains(blkIdx)) continue;
                    Permanent blk = defBf.get(blkIdx);
                    boolean blkParticipates = participatesInDamagePhase(gameData, blk, isFirstStrikePhase);
                    if (blkParticipates && !gameQueryService.isPreventedFromDealingDamage(gameData, blk)
                            && !gameQueryService.hasProtectionFrom(gameData, atk, blk.getEffectiveColor())) {
                        int actualDmg = gameQueryService.applyDamageMultiplier(gameData, blockerDamage.getOrDefault(blkIdx, 0));
                        applyCombatCreatureDamage(gameData, blk, atk, atkIdx, actualDmg, state.atkDamageTaken, state.deathtouchDamagedAttackerIndices);
                        state.combatDamageDealt.merge(blk, actualDmg, Integer::sum);
                        recordCombatDamageToCreature(gameData, state, blk, defenderId, atk, actualDmg);
                    }
                }
            }
        }

        determineCasualties(gameData, attackingIndices, atkBf, state.atkDamageTaken,
                state.deathtouchDamagedAttackerIndices, state.deadAttackerIndices, !isFirstStrikePhase);
        determineBlockerCasualties(gameData, blockerMap, defBf, state, !isFirstStrikePhase);
    }

    /**
     * Distributes an attacker's combat damage across its blockers, respecting deathtouch lethal
     * thresholds and trample overflow to the defending player.
     */
    private void distributeAttackerDamageToBlockers(GameData gameData, CombatDamageState state,
                                                     Permanent atk, List<Integer> blkIndices,
                                                     List<Permanent> defBf,
                                                     UUID activeId, UUID defenderId,
                                                     Permanent redirectTarget,
                                                     boolean skipDeadBlockers) {
        boolean atkHasDeathtouch = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);
        int remaining = gameQueryService.getEffectiveCombatDamage(gameData, atk);
        for (int blkIdx : blkIndices) {
            if (skipDeadBlockers && state.deadDefenderIndices.contains(blkIdx)) continue;
            Permanent blk = defBf.get(blkIdx);
            int lethalNeeded = atkHasDeathtouch
                    ? Math.max(0, 1 - state.defDamageTaken.getOrDefault(blkIdx, 0))
                    : gameQueryService.getEffectiveToughness(gameData, blk) - state.defDamageTaken.getOrDefault(blkIdx, 0);
            int dmg = Math.min(remaining, Math.max(0, lethalNeeded));
            if (!gameQueryService.hasProtectionFrom(gameData, blk, atk.getEffectiveColor())) {
                int actualDmg = gameQueryService.applyDamageMultiplier(gameData, dmg);
                applyCombatCreatureDamage(gameData, atk, blk, blkIdx, actualDmg, state.defDamageTaken, state.deathtouchDamagedDefenderIndices);
                state.combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                recordCombatDamageToCreature(gameData, state, atk, activeId, blk, actualDmg);
            }
            remaining -= dmg;
        }
        // Trample: excess damage goes to defending player
        if (remaining > 0 && gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) {
            int doubledRemaining = gameQueryService.applyDamageMultiplier(gameData, remaining);
            accumulatePlayerDamage(gameData, atk, doubledRemaining, defenderId, redirectTarget, state);
        }
    }

    /**
     * Applies manually assigned combat damage from the player (used when trample or
     * assign-as-unblocked requires explicit damage distribution).
     */
    private void applyPlayerAssignedDamage(GameData gameData, CombatDamageState state,
                                            Permanent atk, List<Integer> blkIndices,
                                            List<Permanent> defBf,
                                            Map<UUID, Integer> playerAssignment,
                                            UUID activeId, UUID defenderId,
                                            Permanent redirectTarget) {
        for (var dmgEntry : playerAssignment.entrySet()) {
            UUID targetId = dmgEntry.getKey();
            int dmg = dmgEntry.getValue();
            if (dmg <= 0) continue;
            if (targetId.equals(defenderId)) {
                int actualDmg = gameQueryService.applyDamageMultiplier(gameData, dmg);
                accumulatePlayerDamage(gameData, atk, actualDmg, defenderId, redirectTarget, state);
            } else {
                for (int blkIdx : blkIndices) {
                    Permanent blk = defBf.get(blkIdx);
                    if (blk.getId().equals(targetId)) {
                        if (!gameQueryService.hasProtectionFrom(gameData, blk, atk.getEffectiveColor())) {
                            int actualDmg = gameQueryService.applyDamageMultiplier(gameData, dmg);
                            applyCombatCreatureDamage(gameData, atk, blk, blkIdx, actualDmg, state.defDamageTaken, state.deathtouchDamagedDefenderIndices);
                            state.combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                            recordCombatDamageToCreature(gameData, state, atk, activeId, blk, actualDmg);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void resolveRedirectedDamage(GameData gameData, CombatDamageState state,
                                          Permanent redirectTarget) {
        // Apply infect redirected damage to guard creature as -1/-1 counters
        if (redirectTarget != null && state.infectDamageRedirectedToGuard > 0) {
            state.infectDamageRedirectedToGuard = gameHelper.applyCreaturePreventionShield(gameData, redirectTarget, state.infectDamageRedirectedToGuard);
            if (state.infectDamageRedirectedToGuard > 0) {
                redirectTarget.setMinusOneMinusOneCounters(redirectTarget.getMinusOneMinusOneCounters() + state.infectDamageRedirectedToGuard);
                String redirectLog = redirectTarget.getCard().getName() + " gets " + state.infectDamageRedirectedToGuard + " -1/-1 counters from redirected infect damage.";
                gameBroadcastService.logAndBroadcast(gameData, redirectLog);
            }
        }

        // Apply redirected damage to guard creature (e.g. Kjeldoran Royal Guard)
        if (redirectTarget != null && (state.damageRedirectedToGuard > 0 || (state.infectDamageRedirectedToGuard > 0 && gameQueryService.getEffectiveToughness(gameData, redirectTarget) <= 0))) {
            state.damageRedirectedToGuard = gameHelper.applyCreaturePreventionShield(gameData, redirectTarget, state.damageRedirectedToGuard);
            if (state.damageRedirectedToGuard > 0) {
                String redirectLog = redirectTarget.getCard().getName() + " absorbs " + state.damageRedirectedToGuard + " redirected combat damage.";
                gameBroadcastService.logAndBroadcast(gameData, redirectLog);
            }

            int guardToughness = gameQueryService.getEffectiveToughness(gameData, redirectTarget);
            if (guardToughness <= 0) {
                // CR 704.5f: 0 toughness from -1/-1 counters
                permanentRemovalService.removePermanentToGraveyard(gameData, redirectTarget);
                String deathLog = redirectTarget.getCard().getName() + " dies from 0 toughness.";
                gameBroadcastService.logAndBroadcast(gameData, deathLog);
            } else if (state.damageRedirectedToGuard >= guardToughness
                    && !gameQueryService.hasKeyword(gameData, redirectTarget, Keyword.INDESTRUCTIBLE)
                    && !gameHelper.tryRegenerate(gameData, redirectTarget)) {
                permanentRemovalService.removePermanentToGraveyard(gameData, redirectTarget);
                String deathLog = redirectTarget.getCard().getName() + " is destroyed by redirected combat damage.";
                gameBroadcastService.logAndBroadcast(gameData, deathLog);
            }
        }
    }

    private List<String> removeDeadCreatures(GameData gameData, CombatDamageState state,
                                              List<Permanent> atkBf, List<Permanent> defBf,
                                              UUID activeId, UUID defenderId) {
        List<String> deadCreatureNames = new ArrayList<>();
        for (int idx : state.deadAttackerIndices) {
            processDeadCreature(gameData, atkBf, activeId, deadCreatureNames, idx);
        }
        for (int idx : state.deadDefenderIndices) {
            processDeadCreature(gameData, defBf, defenderId, deadCreatureNames, idx);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        return deadCreatureNames;
    }

    private void processDeadCreature(GameData gameData, List<Permanent> battlefield,
                                      UUID controllerId, List<String> deadNames, int idx) {
        Permanent dead = battlefield.get(idx);
        deadNames.add(gameData.playerIdToName.get(controllerId) + "'s " + dead.getCard().getName());
        UUID graveyardOwner = gameData.stolenCreatures.getOrDefault(dead.getId(), controllerId);
        gameData.stolenCreatures.remove(dead.getId());
        gameHelper.addCardToGraveyard(gameData, graveyardOwner, dead.getOriginalCard(), Zone.BATTLEFIELD);
        gameHelper.collectDeathTrigger(gameData, dead.getCard(), controllerId, true);
        gameHelper.checkAllyCreatureDeathTriggers(gameData, controllerId);
        battlefield.remove(idx);
    }

    private void applyPlayerDamage(GameData gameData, CombatDamageState state, UUID defenderId) {
        // Apply life loss (with prevention shield and Pariah redirect)
        state.damageToDefendingPlayer = gameHelper.applyPlayerPreventionShield(gameData, defenderId, state.damageToDefendingPlayer);
        state.damageToDefendingPlayer = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, defenderId, state.damageToDefendingPlayer, "combat");
        if (state.damageToDefendingPlayer > 0) {
            if (gameQueryService.canPlayerLifeChange(gameData, defenderId)) {
                int currentLife = gameData.playerLifeTotals.getOrDefault(defenderId, 20);
                gameData.playerLifeTotals.put(defenderId, currentLife - state.damageToDefendingPlayer);

                String logEntry = gameData.playerIdToName.get(defenderId) + " takes " + state.damageToDefendingPlayer + " combat damage.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                gameBroadcastService.logAndBroadcast(gameData,
                        gameData.playerIdToName.get(defenderId) + "'s life total can't change.");
            }
        }

        // Apply poison counters from infect combat damage
        state.poisonDamageToDefendingPlayer = gameHelper.applyPlayerPreventionShield(gameData, defenderId, state.poisonDamageToDefendingPlayer);
        if (state.poisonDamageToDefendingPlayer > 0) {
            int currentPoison = gameData.playerPoisonCounters.getOrDefault(defenderId, 0);
            gameData.playerPoisonCounters.put(defenderId, currentPoison + state.poisonDamageToDefendingPlayer);

            String logEntry = gameData.playerIdToName.get(defenderId) + " gets " + state.poisonDamageToDefendingPlayer + " poison counters.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }
    }

    /**
     * Determines which creatures are dead after a combat damage step.
     * Applies prevention shields, checks 0-toughness (CR 704.5f), lethal damage,
     * deathtouch, indestructible, and regeneration.
     *
     * @param indices     the creature indices to check
     * @param battlefield the battlefield containing these creatures
     * @param damageTaken damage accumulated per index
     * @param deathtouchSet indices that received deathtouch damage
     * @param deadSet     output set of dead creature indices
     * @param skipAlreadyDead if true, skip indices already in deadSet (used for phase 2)
     */
    private void determineCasualties(GameData gameData, List<Integer> indices,
                                      List<Permanent> battlefield, Map<Integer, Integer> damageTaken,
                                      Set<Integer> deathtouchSet, Set<Integer> deadSet,
                                      boolean skipAlreadyDead) {
        for (int idx : indices) {
            if (skipAlreadyDead && deadSet.contains(idx)) continue;
            int dmg = damageTaken.getOrDefault(idx, 0);
            dmg = gameHelper.applyCreaturePreventionShield(gameData, battlefield.get(idx), dmg);
            damageTaken.put(idx, dmg);
            int effToughness = gameQueryService.getEffectiveToughness(gameData, battlefield.get(idx));
            if (effToughness <= 0) {
                deadSet.add(idx);
            } else if ((dmg >= effToughness || (dmg >= 1 && deathtouchSet.contains(idx)))
                    && !gameQueryService.hasKeyword(gameData, battlefield.get(idx), Keyword.INDESTRUCTIBLE)
                    && !gameHelper.tryRegenerate(gameData, battlefield.get(idx))) {
                deadSet.add(idx);
            }
        }
    }

    /**
     * Determines blocker casualties from a blocker map. Iterates all blocker indices
     * from the map values and delegates to {@link #determineCasualties}.
     */
    private void determineBlockerCasualties(GameData gameData, Map<Integer, List<Integer>> blockerMap,
                                             List<Permanent> defBf, CombatDamageState state,
                                             boolean skipAlreadyDead) {
        Set<Integer> seen = new LinkedHashSet<>();
        for (List<Integer> blkIndices : blockerMap.values()) {
            seen.addAll(blkIndices);
        }
        determineCasualties(gameData, new ArrayList<>(seen), defBf, state.defDamageTaken,
                state.deathtouchDamagedDefenderIndices, state.deadDefenderIndices, skipAlreadyDead);
    }

    /**
     * Accumulates combat damage to the defending player, handling redirect target,
     * source-specific prevention, color prevention, and infect vs. regular damage.
     * Also records damage dealt and damage dealt to player for trigger processing.
     */
    private void accumulatePlayerDamage(GameData gameData, Permanent atk, int damage,
                                         UUID defenderId, Permanent redirectTarget,
                                         CombatDamageState state) {
        boolean atkHasInfect = gameQueryService.hasKeyword(gameData, atk, Keyword.INFECT);
        if (redirectTarget != null) {
            if (atkHasInfect) {
                state.infectDamageRedirectedToGuard += damage;
            } else {
                state.damageRedirectedToGuard += damage;
            }
        } else if (gameHelper.isSourceDamagePreventedForPlayer(gameData, defenderId, atk.getId())) {
            // Source-specific damage prevention — skip this damage
        } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getEffectiveColor())) {
            if (atkHasInfect) {
                state.poisonDamageToDefendingPlayer += damage;
            } else {
                state.damageToDefendingPlayer += damage;
            }
        }
        state.combatDamageDealt.merge(atk, damage, Integer::sum);
        state.combatDamageDealtToPlayer.merge(atk, damage, Integer::sum);
    }

    /**
     * Pre-computes blocker power before any infect -1/-1 counters land.
     * CR 510.1: combat damage within the same step is simultaneous.
     *
     * @param requireFirstStrike if true, only includes blockers with first/double strike (phase 1);
     *                           if false, only includes living blockers not already dead (phase 2).
     */
    private Map<Integer, Integer> precomputeBlockerDamage(GameData gameData,
                                                           Map<Integer, List<Integer>> blockerMap,
                                                           List<Permanent> defBf,
                                                           Set<Integer> deadDefenderIndices,
                                                           boolean requireFirstStrike) {
        Map<Integer, Integer> blockerDamage = new HashMap<>();
        for (List<Integer> blkList : blockerMap.values()) {
            for (int blkIdx : blkList) {
                Permanent blk = defBf.get(blkIdx);
                if (requireFirstStrike) {
                    if (hasFirstOrDoubleStrike(gameData, blk)) {
                        blockerDamage.putIfAbsent(blkIdx,
                                gameQueryService.getEffectiveCombatDamage(gameData, blk));
                    }
                } else {
                    if (!deadDefenderIndices.contains(blkIdx)) {
                        blockerDamage.putIfAbsent(blkIdx,
                                gameQueryService.getEffectiveCombatDamage(gameData, blk));
                    }
                }
            }
        }
        return blockerDamage;
    }

    /**
     * Applies combat damage to a creature. If the source has infect, applies -1/-1 counters
     * (with prevention shield consumed immediately). Otherwise, accumulates regular damage.
     */
    private void applyCombatCreatureDamage(GameData gameData, Permanent source, Permanent target,
                                           int targetIdx, int damage, Map<Integer, Integer> damageTakenMap,
                                           Set<Integer> deathtouchDamagedSet) {
        if (gameQueryService.hasKeyword(gameData, source, Keyword.INFECT)) {
            int afterShield = gameHelper.applyCreaturePreventionShield(gameData, target, damage);
            if (afterShield > 0) {
                target.setMinusOneMinusOneCounters(target.getMinusOneMinusOneCounters() + afterShield);
            }
        } else {
            damageTakenMap.merge(targetIdx, damage, Integer::sum);
        }
        if (damage > 0 && gameQueryService.hasKeyword(gameData, source, Keyword.DEATHTOUCH)) {
            deathtouchDamagedSet.add(targetIdx);
        }
    }

    private UUID findControllerOf(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf != null && bf.contains(permanent)) {
                return playerId;
            }
        }
        return null;
    }

    // ===== Aura trigger helpers =====

    private void checkAuraTriggersForCreature(GameData gameData, Permanent creature, EffectSlot slot) {
        UUID creatureControllerId = findControllerOf(gameData, creature);
        if (creatureControllerId == null) return;
        final UUID finalCreatureControllerId = creatureControllerId;

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (perm.getAttachedTo() != null && perm.getAttachedTo().equals(creature.getId())) {
                List<EffectRegistration> auraRegs = perm.getCard().getEffectRegistrations(slot);
                // Skip per-blocker effects — they are handled by checkAttachedPerBlockerTriggers
                List<CardEffect> nonPerBlockerEffects = auraRegs.stream()
                        .filter(r -> r.triggerMode() != TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
                        .toList();
                if (!nonPerBlockerEffects.isEmpty()) {
                    // Bake the creature's controller into effects that need it
                    List<CardEffect> effectsForStack = new ArrayList<>();
                    for (CardEffect effect : nonPerBlockerEffects) {
                        if (effect instanceof EnchantedCreatureControllerLosesLifeEffect e) {
                            effectsForStack.add(new EnchantedCreatureControllerLosesLifeEffect(e.amount(), finalCreatureControllerId));
                        } else {
                            effectsForStack.add(effect);
                        }
                    }

                    // Check if any effect needs a permanent target — queue for target selection
                    boolean needsTarget = effectsForStack.stream().anyMatch(CardEffect::canTargetPermanent);
                    if (needsTarget) {
                        gameData.pendingAttackTriggerTargets.add(
                                new PermanentChoiceContext.AttackTriggerTarget(
                                        perm.getCard(), auraOwnerId, effectsForStack, perm.getId()));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} targeted attack trigger queued for target selection (attached to {})",
                                gameData.id, perm.getCard().getName(), creature.getCard().getName());
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                auraOwnerId,
                                perm.getCard().getName() + "'s triggered ability",
                                effectsForStack,
                                null,
                                perm.getId()
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} aura trigger pushed onto stack (enchanted creature {})",
                                gameData.id, perm.getCard().getName(), creature.getCard().getName());
                    }
                }
            }
        });
    }

    /**
     * For attached permanents (equipment/auras) with ON_BECOMES_BLOCKED effects that trigger
     * per blocking creature (e.g. Infiltration Lens: "Whenever equipped creature becomes blocked
     * by a creature, you may draw two cards"), create one stack entry per blocker.
     */
    private void checkAttachedPerBlockerTriggers(GameData gameData, Permanent attacker,
                                                  List<BlockerAssignment> blockerAssignments,
                                                  List<Permanent> defenderBattlefield, int attackerIndex) {
        UUID controllerId = findControllerOf(gameData, attacker);
        if (controllerId == null) return;
        final UUID finalControllerId = controllerId;

        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.getAttachedTo() != null && perm.getAttachedTo().equals(attacker.getId())) {
                List<CardEffect> perBlockerEffects = perm.getCard().getEffectRegistrations(EffectSlot.ON_BECOMES_BLOCKED).stream()
                        .filter(r -> r.triggerMode() == TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
                        .toList();
                if (!perBlockerEffects.isEmpty()) {
                    for (BlockerAssignment assignment : blockerAssignments) {
                        if (assignment.attackerIndex() != attackerIndex) {
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                finalControllerId,
                                perm.getCard().getName() + "'s triggered ability",
                                new ArrayList<>(perBlockerEffects),
                                null,
                                perm.getId()
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} per-blocker trigger pushed onto stack (attached to {})",
                                gameData.id, perm.getCard().getName(), attacker.getCard().getName());
                    }
                }
            }
        });
    }

    private List<CanBeBlockedOnlyByFilterEffect> getAuraGrantedBlockingRestrictions(GameData gameData, Permanent creature) {
        List<CanBeBlockedOnlyByFilterEffect> restrictions = new ArrayList<>();
        gameData.forEachPermanent((playerId, aura) -> {
            if (aura.getAttachedTo() == null || !aura.getAttachedTo().equals(creature.getId())) {
                return;
            }
            for (CardEffect effect : aura.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CanBeBlockedOnlyByFilterEffect restriction) {
                    restrictions.add(restriction);
                }
            }
        });
        return restrictions;
    }

    // ===== APNAP trigger ordering =====

    /**
     * Reorders triggered abilities added to the stack since {@code startIndex} according to APNAP
     * (Active Player, Non-Active Player) ordering per CR 603.3b.
     * <p>
     * Active player's triggers are placed on the stack first (bottom), then the non-active player's
     * triggers on top. Since the stack resolves LIFO, the non-active player's triggers resolve first.
     */
    private void reorderTriggersAPNAP(GameData gameData, int startIndex, UUID activePlayerId) {
        int totalEntries = gameData.stack.size() - startIndex;
        if (totalEntries <= 1) return;

        List<StackEntry> newEntries = new ArrayList<>(gameData.stack.subList(startIndex, gameData.stack.size()));

        List<StackEntry> apTriggers = new ArrayList<>();
        List<StackEntry> napTriggers = new ArrayList<>();
        for (StackEntry entry : newEntries) {
            if (entry.getControllerId().equals(activePlayerId)) {
                apTriggers.add(entry);
            } else {
                napTriggers.add(entry);
            }
        }

        // Only reorder if both players have triggers
        if (apTriggers.isEmpty() || napTriggers.isEmpty()) return;

        // Remove new entries and re-add in APNAP order: AP first (bottom), NAP on top
        gameData.stack.subList(startIndex, gameData.stack.size()).clear();
        gameData.stack.addAll(apTriggers);
        gameData.stack.addAll(napTriggers);
    }

    private boolean hasFirstOrDoubleStrike(GameData gameData, Permanent creature) {
        return gameQueryService.hasKeyword(gameData, creature, Keyword.FIRST_STRIKE)
                || gameQueryService.hasKeyword(gameData, creature, Keyword.DOUBLE_STRIKE);
    }

    /**
     * Returns true if the creature deals damage in the given phase.
     * Phase 1 (first strike): creatures with first strike or double strike.
     * Phase 2 (regular): creatures without first strike, or with double strike.
     */
    private boolean participatesInDamagePhase(GameData gameData, Permanent creature, boolean isFirstStrikePhase) {
        boolean hasFirstStrike = gameQueryService.hasKeyword(gameData, creature, Keyword.FIRST_STRIKE);
        boolean hasDoubleStrike = gameQueryService.hasKeyword(gameData, creature, Keyword.DOUBLE_STRIKE);
        return isFirstStrikePhase ? (hasFirstStrike || hasDoubleStrike) : (!hasFirstStrike || hasDoubleStrike);
    }

    private boolean assignsCombatDamageAsThoughUnblocked(Permanent attacker) {
        return attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(AssignCombatDamageAsThoughUnblockedEffect.class::isInstance);
    }

    /**
     * Returns the effective message recipient for a player, redirecting to the
     * Mindslaver controller if the player is currently being mind-controlled.
     */
    private UUID getEffectiveRecipient(GameData gameData, UUID playerId) {
        if (gameData.mindControlledPlayerId != null
                && gameData.mindControlledPlayerId.equals(playerId)
                && gameData.mindControllerPlayerId != null) {
            return gameData.mindControllerPlayerId;
        }
        return playerId;
    }

    // ===== Combat damage assignment =====

    private boolean needsManualDamageAssignment(GameData gameData, Permanent atk, List<Integer> livingBlockerIndices) {
        if (livingBlockerIndices.isEmpty()) return false;
        if (livingBlockerIndices.size() >= 2) return true;
        // Single blocker: needs manual only if trample or assign-as-unblocked
        if (gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) return true;
        if (assignsCombatDamageAsThoughUnblocked(atk)) return true;
        return false;
    }

    private CombatDamagePhase1State savePhase1State(CombatDamageState state,
                                                    Map<Integer, List<Integer>> blockerMap,
                                                    boolean anyFirstStrike) {
        // Convert Permanent-keyed maps to UUID-keyed maps for serialization
        Map<UUID, Integer> dealtByUUID = new HashMap<>();
        for (var e : state.combatDamageDealt.entrySet()) {
            dealtByUUID.put(e.getKey().getId(), e.getValue());
        }
        Map<UUID, Integer> dealtToPlayerByUUID = new HashMap<>();
        for (var e : state.combatDamageDealtToPlayer.entrySet()) {
            dealtToPlayerByUUID.put(e.getKey().getId(), e.getValue());
        }
        Map<UUID, List<UUID>> dealtToCreaturesByUUID = new HashMap<>();
        for (var e : state.combatDamageDealtToCreatures.entrySet()) {
            dealtToCreaturesByUUID.put(e.getKey().getId(), new ArrayList<>(e.getValue()));
        }
        Map<UUID, UUID> dealerControllersByUUID = new HashMap<>();
        for (var e : state.combatDamageDealerControllers.entrySet()) {
            dealerControllersByUUID.put(e.getKey().getId(), e.getValue());
        }
        return new CombatDamagePhase1State(
                new TreeSet<>(state.deadAttackerIndices), new TreeSet<>(state.deadDefenderIndices),
                new HashMap<>(state.atkDamageTaken), new HashMap<>(state.defDamageTaken),
                dealtByUUID, dealtToPlayerByUUID, dealtToCreaturesByUUID, dealerControllersByUUID,
                state.damageToDefendingPlayer, state.damageRedirectedToGuard,
                new LinkedHashMap<>(blockerMap), anyFirstStrike,
                new HashSet<>(state.deathtouchDamagedAttackerIndices), new HashSet<>(state.deathtouchDamagedDefenderIndices));
    }

    private void sendNextCombatDamageAssignment(GameData gameData, List<Permanent> atkBf,
                                                 List<Permanent> defBf, UUID activeId, UUID defenderId) {
        int atkIdx = gameData.combatDamagePendingIndices.get(0);
        Permanent atk = atkBf.get(atkIdx);
        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
        List<Integer> blkIndices = p1.blockerMap.get(atkIdx);
        List<Integer> livingBlockers = blkIndices.stream()
                .filter(i -> !p1.deadDefenderIndices.contains(i))
                .toList();

        List<CombatDamageTargetView> targetViews = new ArrayList<>();
        List<CombatDamageTarget> domainTargets = new ArrayList<>();
        for (int blkIdx : livingBlockers) {
            Permanent blk = defBf.get(blkIdx);
            int toughness = gameQueryService.getEffectiveToughness(gameData, blk);
            int damageTaken = p1.defDamageTaken.getOrDefault(blkIdx, 0);
            targetViews.add(new CombatDamageTargetView(
                    blk.getId().toString(), blk.getCard().getName(), toughness, damageTaken, false));
            domainTargets.add(new CombatDamageTarget(
                    blk.getId(), blk.getCard().getName(), toughness, damageTaken, false));
        }

        boolean isTrample = gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE);
        boolean isDeathtouch = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);
        boolean addPlayer = isTrample || assignsCombatDamageAsThoughUnblocked(atk);
        if (addPlayer) {
            String defenderName = gameData.playerIdToName.get(defenderId);
            targetViews.add(new CombatDamageTargetView(
                    defenderId.toString(), defenderName, 0, 0, true));
            domainTargets.add(new CombatDamageTarget(
                    defenderId, defenderName, 0, 0, true));
        }

        int totalDamage = gameQueryService.getEffectiveCombatDamage(gameData, atk);

        gameData.interaction.beginCombatDamageAssignment(activeId, atkIdx, atk.getId(),
                atk.getCard().getName(), totalDamage, domainTargets, isTrample, isDeathtouch);

        CombatDamageAssignmentNotification notification = new CombatDamageAssignmentNotification(
                atkIdx, atk.getId().toString(), atk.getCard().getName(), totalDamage, targetViews, isTrample, isDeathtouch);
        sessionManager.sendToPlayer(getEffectiveRecipient(gameData, activeId), notification);
    }

    /**
     * Processes a player's combat damage assignment for a single attacker. Validates that total assigned
     * damage equals the attacker's power, that all targets are valid (living blockers and/or the defending
     * player for trample), and that trample assignments meet the lethal-damage-first requirement (CR 702.19c).
     *
     * @param gameData      the current game state
     * @param attackerIndex the battlefield index of the attacker whose damage is being assigned
     * @param assignments   map from target ID (blocker permanent ID or defending player ID) to damage amount
     * @throws IllegalStateException if not in the combat damage assignment phase, the attacker is not pending,
     *                               or the assignment is invalid
     */
    public void handleCombatDamageAssigned(GameData gameData, int attackerIndex, Map<UUID, Integer> assignments) {
        if (!gameData.combatDamagePhase1Complete) {
            throw new IllegalStateException("Not in combat damage assignment phase");
        }
        if (!gameData.combatDamagePendingIndices.contains(attackerIndex)) {
            throw new IllegalStateException("Attacker index " + attackerIndex + " is not pending assignment");
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);
        List<Permanent> atkBf = gameData.playerBattlefields.get(activeId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);
        Permanent atk = atkBf.get(attackerIndex);
        int totalDamage = gameQueryService.getEffectiveCombatDamage(gameData, atk);

        // Validate total damage
        int totalAssigned = assignments.values().stream().mapToInt(Integer::intValue).sum();
        if (totalAssigned != totalDamage) {
            throw new IllegalStateException("Total assigned damage (" + totalAssigned
                    + ") must equal attacker's combat damage (" + totalDamage + ")");
        }

        // Validate targets
        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
        List<Integer> blkIndices = p1.blockerMap.get(attackerIndex);
        List<Integer> livingBlockers = blkIndices.stream()
                .filter(i -> !p1.deadDefenderIndices.contains(i))
                .toList();

        Set<UUID> validTargetIds = new HashSet<>();
        for (int blkIdx : livingBlockers) {
            validTargetIds.add(defBf.get(blkIdx).getId());
        }
        boolean canTargetPlayer = gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)
                || assignsCombatDamageAsThoughUnblocked(atk);
        if (canTargetPlayer) {
            validTargetIds.add(defenderId);
        }

        for (UUID targetId : assignments.keySet()) {
            if (!validTargetIds.contains(targetId)) {
                throw new IllegalStateException("Invalid damage target: " + targetId);
            }
        }

        // Validate trample: each blocker must receive at least lethal damage
        if (gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) {
            boolean atkHasDeathtouchForValidation = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);
            for (int blkIdx : livingBlockers) {
                Permanent blk = defBf.get(blkIdx);
                int alreadyTaken = p1.defDamageTaken.getOrDefault(blkIdx, 0);
                int lethal = atkHasDeathtouchForValidation
                        ? Math.max(0, 1 - alreadyTaken)
                        : gameQueryService.getEffectiveToughness(gameData, blk) - alreadyTaken;
                int assigned = assignments.getOrDefault(blk.getId(), 0);
                if (assigned < lethal) {
                    throw new IllegalStateException("Trample: must assign at least " + lethal
                            + " damage to " + blk.getCard().getName());
                }
            }
        }

        // Validate non-trample non-unblocked: no damage to player
        if (!canTargetPlayer && assignments.containsKey(defenderId)) {
            throw new IllegalStateException("Cannot assign damage to defending player");
        }

        // Store and remove from pending
        gameData.combatDamagePlayerAssignments.put(attackerIndex, assignments);
        gameData.combatDamagePendingIndices.remove(Integer.valueOf(attackerIndex));
        gameData.interaction.clearAwaitingInput();
    }

    // ===== Combat state management =====

    /**
     * Resets all combat-related state on permanents and game data. Clears attacking/blocking flags
     * on all permanents and removes combat damage assignment tracking from the game data.
     * Called at the end of combat or when combat is skipped.
     *
     * @param gameData the current game state
     */
    public void clearCombatState(GameData gameData) {
        gameData.forEachBattlefield((playerId, battlefield) ->
                battlefield.forEach(Permanent::clearCombatState));
        // Clear combat damage assignment state
        gameData.combatDamagePlayerAssignments.clear();
        gameData.combatDamagePendingIndices.clear();
        gameData.combatDamagePhase1Complete = false;
        gameData.combatDamagePhase1State = null;
    }

    /**
     * Sacrifices all permanents marked for end-of-combat sacrifice (e.g. creatures created by
     * temporary token effects). Moves them to the graveyard, fires death triggers, and cleans up
     * orphaned auras.
     *
     * @param gameData the current game state
     */
    public void processEndOfCombatSacrifices(GameData gameData) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            List<Permanent> toSacrifice = battlefield.stream()
                    .filter(p -> gameData.permanentsToSacrificeAtEndOfCombat.contains(p.getId()))
                    .toList();
            for (Permanent perm : toSacrifice) {
                boolean wasCreature = gameQueryService.isCreature(gameData, perm);
                battlefield.remove(perm);
                gameHelper.addCardToGraveyard(gameData, playerId, perm.getOriginalCard(), Zone.BATTLEFIELD);
                gameHelper.collectDeathTrigger(gameData, perm.getCard(), playerId, wasCreature);
                if (wasCreature) {
                    gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                }
                String logEntry = perm.getCard().getName() + " is sacrificed.";
                gameData.gameLog.add(logEntry);
                log.info("Game {} - {} sacrificed at end of combat", gameData.id, perm.getCard().getName());
            }
        });
        gameData.permanentsToSacrificeAtEndOfCombat.clear();
        permanentRemovalService.removeOrphanedAuras(gameData);


    }

}



package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CombatDamagePhase1State;
import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.effect.RandomDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.model.CombatDamageTargetView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    // ===== Query methods =====

    List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        UUID defenderId = gameQueryService.getOpponentId(gameData, playerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (gameQueryService.isCreature(gameData, p) && !p.isTapped() && (!p.isSummoningSick() || gameQueryService.hasKeyword(gameData, p, Keyword.HASTE)) && !gameQueryService.hasKeyword(gameData, p, Keyword.DEFENDER) && !gameQueryService.hasAuraWithEffect(gameData, p, EnchantedCreatureCantAttackOrBlockEffect.class)) {
                if (isCantAttackDueToLandRestriction(gameData, p, defenderBattlefield)) {
                    continue;
                }
                indices.add(i);
            }
        }
        return indices;
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

    /**
     * Returns attackable indices whose creature has at least one "attacks each combat if able" requirement.
     */
    List<Integer> getMustAttackIndices(GameData gameData, UUID playerId, List<Integer> attackableIndices) {
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

    List<Integer> getBlockableCreatureIndices(GameData gameData, UUID playerId) {
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

    Map<Integer, List<Integer>> computeLegalBlockPairs(GameData gameData,
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

    List<Integer> getAttackingCreatureIndices(GameData gameData, UUID playerId) {
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

    void handleDeclareAttackersStep(GameData gameData) {
        UUID activeId = gameData.activePlayerId;
        List<Integer> attackable = getAttackableCreatureIndices(gameData, activeId);

        if (attackable.isEmpty()) {
            String playerName = gameData.playerIdToName.get(activeId);
            log.info("Game {} - {} has no creatures that can attack, skipping combat", gameData.id, playerName);
            return;
        }

        List<Integer> mustAttack = getMustAttackIndices(gameData, activeId, attackable);
        gameData.interaction.beginAttackerDeclaration(activeId);
        sessionManager.sendToPlayer(activeId, new AvailableAttackersMessage(attackable, mustAttack));
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

        if (!gameData.stack.isEmpty()) {

        }

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
        attackerIndices = attackerIndices.stream()
                .filter(idx -> !gameQueryService.hasCantBeBlocked(gameData, attackerBattlefield.get(idx)))
                .toList();

        if (blockable.isEmpty() || attackerIndices.isEmpty()) {
            log.info("Game {} - Defending player has no creatures that can block or no blockable attackers", gameData.id);
            return CombatResult.ADVANCE_ONLY;
        }

        Map<Integer, List<Integer>> legalPairs = computeLegalBlockPairs(gameData, blockable, attackerIndices, defenderId, activeId);
        gameData.interaction.beginBlockerDeclaration(defenderId);
        sessionManager.sendToPlayer(defenderId, new AvailableBlockersMessage(blockable, attackerIndices, legalPairs));
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
            if (gameQueryService.hasCantBeBlocked(gameData, attacker)) {
                throw new IllegalStateException(attacker.getCard().getName() + " can't be blocked");
            }
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.FLYING)
                    && !gameQueryService.hasKeyword(gameData, blocker, Keyword.FLYING)
                    && !gameQueryService.hasKeyword(gameData, blocker, Keyword.REACH)) {
                throw new IllegalStateException(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (flying)");
            }
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.FEAR)
                    && !gameQueryService.isArtifact(blocker)
                    && blocker.getEffectiveColor() != CardColor.BLACK) {
                throw new IllegalStateException(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (fear)");
            }
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.INTIMIDATE)
                    && !gameQueryService.isArtifact(blocker)
                    && blocker.getEffectiveColor() != attacker.getEffectiveColor()) {
                throw new IllegalStateException(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (intimidate)");
            }
            for (CardEffect blockerStaticEffect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
                if (blockerStaticEffect instanceof CanBlockOnlyIfAttackerMatchesPredicateEffect restriction
                        && !gameQueryService.matchesPermanentPredicate(gameData, attacker, restriction.attackerPredicate())) {
                    throw new IllegalStateException(blocker.getCard().getName() + " can only block " + restriction.allowedAttackersDescription());
                }
            }
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CanBeBlockedOnlyByFilterEffect restriction) {
                    if (!gameQueryService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                        throw new IllegalStateException(attacker.getCard().getName()
                                + " can only be blocked by " + restriction.allowedBlockersDescription());
                    }
                }
            }
            for (CanBeBlockedOnlyByFilterEffect restriction : getAuraGrantedBlockingRestrictions(gameData, attacker)) {
                if (!gameQueryService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                    throw new IllegalStateException(attacker.getCard().getName()
                            + " can only be blocked by " + restriction.allowedBlockersDescription());
                }
            }
            for (var entry : Map.of(
                    Keyword.FORESTWALK, CardSubtype.FOREST,
                    Keyword.MOUNTAINWALK, CardSubtype.MOUNTAIN,
                    Keyword.ISLANDWALK, CardSubtype.ISLAND,
                    Keyword.SWAMPWALK, CardSubtype.SWAMP
            ).entrySet()) {
                if (gameQueryService.hasKeyword(gameData, attacker, entry.getKey())
                        && defenderBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(entry.getValue()))) {
                    throw new IllegalStateException(attacker.getCard().getName() + " can't be blocked (" + entry.getValue().getDisplayName().toLowerCase() + "walk)");
                }
            }
            if (blocker.isCantBlockThisTurn()) {
                throw new IllegalStateException(blocker.getCard().getName() + " can't block this turn");
            }
            boolean hasCantBlockStatic = blocker.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(CantBlockEffect.class::isInstance);
            if (hasCantBlockStatic) {
                throw new IllegalStateException(blocker.getCard().getName() + " can't block");
            }
            if (blocker.getCantBlockIds().contains(attacker.getId())) {
                throw new IllegalStateException(blocker.getCard().getName() + " can't block " + attacker.getCard().getName() + " this turn");
            }
            if (gameQueryService.hasProtectionFrom(gameData, attacker, blocker.getEffectiveColor())) {
                throw new IllegalStateException(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (protection)");
            }

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

        if (!gameData.stack.isEmpty()) {

        }

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

    private boolean canBlockAttacker(GameData gameData,
                                     Permanent blocker,
                                     Permanent attacker,
                                     List<Permanent> defenderBattlefield) {
        if (gameQueryService.hasCantBeBlocked(gameData, attacker)) return false;

        if (gameQueryService.hasKeyword(gameData, attacker, Keyword.FLYING)
                && !gameQueryService.hasKeyword(gameData, blocker, Keyword.FLYING)
                && !gameQueryService.hasKeyword(gameData, blocker, Keyword.REACH)) {
            return false;
        }
        if (gameQueryService.hasKeyword(gameData, attacker, Keyword.FEAR)
                && !gameQueryService.isArtifact(blocker)
                && blocker.getEffectiveColor() != CardColor.BLACK) {
            return false;
        }
        if (gameQueryService.hasKeyword(gameData, attacker, Keyword.INTIMIDATE)
                && !gameQueryService.isArtifact(blocker)
                && blocker.getEffectiveColor() != attacker.getEffectiveColor()) {
            return false;
        }

        for (CardEffect blockerStaticEffect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
            if (blockerStaticEffect instanceof CanBlockOnlyIfAttackerMatchesPredicateEffect restriction
                    && !gameQueryService.matchesPermanentPredicate(gameData, attacker, restriction.attackerPredicate())) {
                return false;
            }
        }
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanBeBlockedOnlyByFilterEffect restriction
                    && !gameQueryService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return false;
            }
        }
        for (CanBeBlockedOnlyByFilterEffect restriction : getAuraGrantedBlockingRestrictions(gameData, attacker)) {
            if (!gameQueryService.matchesPermanentPredicate(gameData, blocker, restriction.blockerPredicate())) {
                return false;
            }
        }

        for (var entry : Map.of(
                Keyword.FORESTWALK, CardSubtype.FOREST,
                Keyword.MOUNTAINWALK, CardSubtype.MOUNTAIN,
                Keyword.ISLANDWALK, CardSubtype.ISLAND,
                Keyword.SWAMPWALK, CardSubtype.SWAMP
        ).entrySet()) {
            if (gameQueryService.hasKeyword(gameData, attacker, entry.getKey())
                    && defenderBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(entry.getValue()))) {
                return false;
            }
        }

        if (blocker.isCantBlockThisTurn()) return false;
        boolean hasCantBlockStatic = blocker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantBlockEffect.class::isInstance);
        if (hasCantBlockStatic) return false;
        if (blocker.getCantBlockIds().contains(attacker.getId())) return false;

        return !gameQueryService.hasProtectionFrom(gameData, attacker, blocker.getEffectiveColor());
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
        int damageRedirectedToGuard = 0;

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
        boolean anyFirstStrike = false;
        for (int atkIdx : attackingIndices) {
            if (gameQueryService.hasKeyword(gameData, atkBf.get(atkIdx), Keyword.FIRST_STRIKE)
                    || gameQueryService.hasKeyword(gameData, atkBf.get(atkIdx), Keyword.DOUBLE_STRIKE)) {
                anyFirstStrike = true;
                break;
            }
        }
        if (!anyFirstStrike) {
            for (List<Integer> blkIndices : blockerMap.values()) {
                for (int blkIdx : blkIndices) {
                    if (gameQueryService.hasKeyword(gameData, defBf.get(blkIdx), Keyword.FIRST_STRIKE)
                            || gameQueryService.hasKeyword(gameData, defBf.get(blkIdx), Keyword.DOUBLE_STRIKE)) {
                        anyFirstStrike = true;
                        break;
                    }
                }
                if (anyFirstStrike) break;
            }
        }

        int damageToDefendingPlayer = 0;
        int poisonDamageToDefendingPlayer = 0;
        int infectDamageRedirectedToGuard = 0;
        Set<Integer> deadAttackerIndices = new TreeSet<>(Collections.reverseOrder());
        Set<Integer> deadDefenderIndices = new TreeSet<>(Collections.reverseOrder());

        // Track cumulative damage on each creature
        Map<Integer, Integer> atkDamageTaken = new HashMap<>();
        Map<Integer, Integer> defDamageTaken = new HashMap<>();
        Map<Permanent, Integer> combatDamageDealt = new HashMap<>();
        Map<Permanent, Integer> combatDamageDealtToPlayer = new HashMap<>();
        Map<Permanent, List<UUID>> combatDamageDealtToCreatures = new HashMap<>();
        Map<Permanent, UUID> combatDamageDealerControllers = new HashMap<>();

        // Restore phase 1 state if re-entering after damage assignment
        if (gameData.combatDamagePhase1Complete) {
            CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
            deadAttackerIndices.addAll(p1.deadAttackerIndices);
            deadDefenderIndices.addAll(p1.deadDefenderIndices);
            atkDamageTaken.putAll(p1.atkDamageTaken);
            defDamageTaken.putAll(p1.defDamageTaken);
            damageToDefendingPlayer = p1.damageToDefendingPlayer;
            damageRedirectedToGuard = p1.damageRedirectedToGuard;
            for (var e : p1.combatDamageDealt.entrySet()) {
                Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
                if (perm != null) combatDamageDealt.put(perm, e.getValue());
            }
            for (var e : p1.combatDamageDealtToPlayer.entrySet()) {
                Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
                if (perm != null) combatDamageDealtToPlayer.put(perm, e.getValue());
            }
            for (var e : p1.combatDamageDealtToCreatures.entrySet()) {
                Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
                if (perm != null) combatDamageDealtToCreatures.put(perm, new ArrayList<>(e.getValue()));
            }
            for (var e : p1.combatDamageDealerControllers.entrySet()) {
                Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
                if (perm != null) combatDamageDealerControllers.put(perm, e.getValue());
            }
        }

        // Phase 1: First strike damage (skip on re-entry)
        if (!gameData.combatDamagePhase1Complete && anyFirstStrike) {
            // Pre-compute blocker first-strike damage before any infect -1/-1 counters
            // (CR 510.1: combat damage within the same step is simultaneous)
            Map<Integer, Integer> phase1BlockerDamage = new HashMap<>();
            for (List<Integer> blkList : blockerMap.values()) {
                for (int blkIdx : blkList) {
                    Permanent blk = defBf.get(blkIdx);
                    if (gameQueryService.hasKeyword(gameData, blk, Keyword.FIRST_STRIKE)
                            || gameQueryService.hasKeyword(gameData, blk, Keyword.DOUBLE_STRIKE)) {
                        phase1BlockerDamage.putIfAbsent(blkIdx,
                                gameQueryService.getEffectiveCombatDamage(gameData, blk));
                    }
                }
            }

            for (var entry : blockerMap.entrySet()) {
                int atkIdx = entry.getKey();
                List<Integer> blkIndices = entry.getValue();
                Permanent atk = atkBf.get(atkIdx);
                boolean atkHasFS = gameQueryService.hasKeyword(gameData, atk, Keyword.FIRST_STRIKE)
                        || gameQueryService.hasKeyword(gameData, atk, Keyword.DOUBLE_STRIKE);

                boolean assignAsUnblocked = !blkIndices.isEmpty() && assignsCombatDamageAsThoughUnblocked(atk);
                if (blkIndices.isEmpty() || assignAsUnblocked) {
                    // Unblocked first striker deals damage to player (or redirect target)
                    if (atkHasFS && !gameQueryService.isPreventedFromDealingDamage(gameData, atk)) {
                        int power = gameQueryService.applyDamageMultiplier(gameData, gameQueryService.getEffectiveCombatDamage(gameData, atk));
                        boolean atkHasInfect = gameQueryService.hasKeyword(gameData, atk, Keyword.INFECT);
                        if (redirectTarget != null) {
                            if (atkHasInfect) {
                                infectDamageRedirectedToGuard += power;
                            } else {
                                damageRedirectedToGuard += power;
                            }
                        } else if (gameHelper.isSourceDamagePreventedForPlayer(gameData, defenderId, atk.getId())) {
                            // Source-specific damage prevention — skip first-strike damage
                        } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getEffectiveColor())) {
                            if (atkHasInfect) {
                                poisonDamageToDefendingPlayer += power;
                            } else {
                                damageToDefendingPlayer += power;
                            }
                        }
                        combatDamageDealt.merge(atk, power, Integer::sum);
                        combatDamageDealtToPlayer.merge(atk, power, Integer::sum);
                    }
                } else {
                    // First strike attacker deals damage to blockers
                    if (atkHasFS && !gameQueryService.isPreventedFromDealingDamage(gameData, atk)) {
                        boolean atkHasInfect = gameQueryService.hasKeyword(gameData, atk, Keyword.INFECT);
                        int remaining = gameQueryService.getEffectiveCombatDamage(gameData, atk);
                        for (int blkIdx : blkIndices) {
                            Permanent blk = defBf.get(blkIdx);
                            int dmg = Math.min(remaining, gameQueryService.getEffectiveToughness(gameData, blk) - defDamageTaken.getOrDefault(blkIdx, 0));
                            if (!gameQueryService.hasProtectionFrom(gameData, blk, atk.getEffectiveColor())) {
                                int actualDmg = gameQueryService.applyDamageMultiplier(gameData, dmg);
                                applyCombatCreatureDamage(gameData, atk, blk, blkIdx, actualDmg, defDamageTaken);
                                combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                                recordCombatDamageToCreature(gameData, combatDamageDealtToCreatures, combatDamageDealerControllers,
                                        atk, activeId, blk, actualDmg);
                            }
                            remaining -= dmg;
                        }
                        // Trample: excess damage goes to defending player
                        if (remaining > 0 && gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) {
                            int doubledRemaining = gameQueryService.applyDamageMultiplier(gameData, remaining);
                            if (redirectTarget != null) {
                                if (atkHasInfect) {
                                    infectDamageRedirectedToGuard += doubledRemaining;
                                } else {
                                    damageRedirectedToGuard += doubledRemaining;
                                }
                            } else if (gameHelper.isSourceDamagePreventedForPlayer(gameData, defenderId, atk.getId())) {
                                // Source-specific damage prevention — skip first-strike trample damage
                            } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getEffectiveColor())) {
                                if (atkHasInfect) {
                                    poisonDamageToDefendingPlayer += doubledRemaining;
                                } else {
                                    damageToDefendingPlayer += doubledRemaining;
                                }
                            }
                            combatDamageDealt.merge(atk, doubledRemaining, Integer::sum);
                            combatDamageDealtToPlayer.merge(atk, doubledRemaining, Integer::sum);
                        }
                    }
                }
                if (!blkIndices.isEmpty()) {
                    // First strike / double strike blockers deal damage to attacker
                    for (int blkIdx : blkIndices) {
                        Permanent blk = defBf.get(blkIdx);
                        if ((gameQueryService.hasKeyword(gameData, blk, Keyword.FIRST_STRIKE) || gameQueryService.hasKeyword(gameData, blk, Keyword.DOUBLE_STRIKE))
                                && !gameQueryService.isPreventedFromDealingDamage(gameData, blk)
                                && !gameQueryService.hasProtectionFrom(gameData, atk, blk.getEffectiveColor())) {
                            int actualDmg = gameQueryService.applyDamageMultiplier(gameData, phase1BlockerDamage.getOrDefault(blkIdx, 0));
                            applyCombatCreatureDamage(gameData, blk, atk, atkIdx, actualDmg, atkDamageTaken);
                            combatDamageDealt.merge(blk, actualDmg, Integer::sum);
                            recordCombatDamageToCreature(gameData, combatDamageDealtToCreatures, combatDamageDealerControllers,
                                    blk, defenderId, atk, actualDmg);
                        }
                    }
                }
            }

            // Determine phase 1 casualties (apply prevention shields)
            for (int atkIdx : attackingIndices) {
                int dmg = atkDamageTaken.getOrDefault(atkIdx, 0);
                dmg = gameHelper.applyCreaturePreventionShield(gameData, atkBf.get(atkIdx), dmg);
                atkDamageTaken.put(atkIdx, dmg);
                int effToughness = gameQueryService.getEffectiveToughness(gameData, atkBf.get(atkIdx));
                if (effToughness <= 0) {
                    // CR 704.5f: 0 toughness from -1/-1 counters — dies regardless of indestructible
                    deadAttackerIndices.add(atkIdx);
                } else if (dmg >= effToughness
                        && !gameQueryService.hasKeyword(gameData, atkBf.get(atkIdx), Keyword.INDESTRUCTIBLE)
                        && !gameHelper.tryRegenerate(gameData, atkBf.get(atkIdx))) {
                    deadAttackerIndices.add(atkIdx);
                }
            }
            for (List<Integer> blkIndices : blockerMap.values()) {
                for (int blkIdx : blkIndices) {
                    int dmg = defDamageTaken.getOrDefault(blkIdx, 0);
                    dmg = gameHelper.applyCreaturePreventionShield(gameData, defBf.get(blkIdx), dmg);
                    defDamageTaken.put(blkIdx, dmg);
                    int effToughness = gameQueryService.getEffectiveToughness(gameData, defBf.get(blkIdx));
                    if (effToughness <= 0) {
                        deadDefenderIndices.add(blkIdx);
                    } else if (dmg >= effToughness
                            && !gameQueryService.hasKeyword(gameData, defBf.get(blkIdx), Keyword.INDESTRUCTIBLE)
                            && !gameHelper.tryRegenerate(gameData, defBf.get(blkIdx))) {
                        deadDefenderIndices.add(blkIdx);
                    }
                }
            }
        }

        // Check if any phase 2 attackers need manual damage assignment
        if (!gameData.combatDamagePhase1Complete) {
            List<Integer> needsManual = new ArrayList<>();
            for (var bEntry : blockerMap.entrySet()) {
                int bAtkIdx = bEntry.getKey();
                if (deadAttackerIndices.contains(bAtkIdx)) continue;
                Permanent bAtk = atkBf.get(bAtkIdx);
                boolean bAtkSkipPhase2 = gameQueryService.hasKeyword(gameData, bAtk, Keyword.FIRST_STRIKE)
                        && !gameQueryService.hasKeyword(gameData, bAtk, Keyword.DOUBLE_STRIKE);
                if (bAtkSkipPhase2) continue;
                if (gameQueryService.isPreventedFromDealingDamage(gameData, bAtk)) continue;
                List<Integer> livingBlockers = bEntry.getValue().stream()
                        .filter(i -> !deadDefenderIndices.contains(i))
                        .toList();
                if (needsManualDamageAssignment(gameData, bAtk, livingBlockers)) {
                    needsManual.add(bAtkIdx);
                }
            }
            if (!needsManual.isEmpty()) {
                gameData.combatDamagePhase1State = savePhase1State(deadAttackerIndices, deadDefenderIndices,
                        atkDamageTaken, defDamageTaken, combatDamageDealt, combatDamageDealtToPlayer,
                        combatDamageDealtToCreatures, combatDamageDealerControllers,
                        damageToDefendingPlayer, damageRedirectedToGuard, blockerMap, anyFirstStrike);
                gameData.combatDamagePhase1Complete = true;
                gameData.combatDamagePendingIndices.addAll(needsManual);
                sendNextCombatDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
                return CombatResult.DONE;
            }
        }

        // Pre-compute blocker regular-step damage before any infect -1/-1 counters
        // (CR 510.1: combat damage within the same step is simultaneous)
        Map<Integer, Integer> phase2BlockerDamage = new HashMap<>();
        for (List<Integer> blkList : blockerMap.values()) {
            for (int blkIdx : blkList) {
                if (!deadDefenderIndices.contains(blkIdx)) {
                    phase2BlockerDamage.putIfAbsent(blkIdx,
                            gameQueryService.getEffectiveCombatDamage(gameData, defBf.get(blkIdx)));
                }
            }
        }

        // Phase 2: Regular damage (skip dead creatures, skip first-strikers who already dealt — double strikers deal again)
        for (var entry : blockerMap.entrySet()) {
            int atkIdx = entry.getKey();
            List<Integer> blkIndices = entry.getValue();
            if (deadAttackerIndices.contains(atkIdx)) continue;

            Permanent atk = atkBf.get(atkIdx);
            boolean atkSkipPhase2 = gameQueryService.hasKeyword(gameData, atk, Keyword.FIRST_STRIKE)
                    && !gameQueryService.hasKeyword(gameData, atk, Keyword.DOUBLE_STRIKE);

            // Check for player-provided damage assignment
            Map<UUID, Integer> playerAssignment = gameData.combatDamagePlayerAssignments.get(atkIdx);
            boolean assignAsUnblocked = !blkIndices.isEmpty() && assignsCombatDamageAsThoughUnblocked(atk);
            boolean atkHasInfect = gameQueryService.hasKeyword(gameData, atk, Keyword.INFECT);
            if (playerAssignment != null) {
                // Player assigned damage manually
                if (!atkSkipPhase2 && !gameQueryService.isPreventedFromDealingDamage(gameData, atk)) {
                    for (var dmgEntry : playerAssignment.entrySet()) {
                        UUID targetId = dmgEntry.getKey();
                        int dmg = dmgEntry.getValue();
                        if (dmg <= 0) continue;
                        if (targetId.equals(defenderId)) {
                            int actualDmg = gameQueryService.applyDamageMultiplier(gameData, dmg);
                            if (redirectTarget != null) {
                                if (atkHasInfect) {
                                    infectDamageRedirectedToGuard += actualDmg;
                                } else {
                                    damageRedirectedToGuard += actualDmg;
                                }
                            } else if (gameHelper.isSourceDamagePreventedForPlayer(gameData, defenderId, atk.getId())) {
                                // Source-specific damage prevention — skip player-assigned damage
                            } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getEffectiveColor())) {
                                if (atkHasInfect) {
                                    poisonDamageToDefendingPlayer += actualDmg;
                                } else {
                                    damageToDefendingPlayer += actualDmg;
                                }
                            }
                            combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                            combatDamageDealtToPlayer.merge(atk, actualDmg, Integer::sum);
                        } else {
                            for (int blkIdx : blkIndices) {
                                Permanent blk = defBf.get(blkIdx);
                                if (blk.getId().equals(targetId)) {
                                    if (!gameQueryService.hasProtectionFrom(gameData, blk, atk.getEffectiveColor())) {
                                        int actualDmg = gameQueryService.applyDamageMultiplier(gameData, dmg);
                                        applyCombatCreatureDamage(gameData, atk, blk, blkIdx, actualDmg, defDamageTaken);
                                        combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                                        recordCombatDamageToCreature(gameData, combatDamageDealtToCreatures, combatDamageDealerControllers,
                                                atk, activeId, blk, actualDmg);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (blkIndices.isEmpty() || assignAsUnblocked) {
                // Unblocked regular attacker deals damage to player (or redirect target)
                if (!atkSkipPhase2 && !gameQueryService.isPreventedFromDealingDamage(gameData, atk)) {
                    int power = gameQueryService.applyDamageMultiplier(gameData, gameQueryService.getEffectiveCombatDamage(gameData, atk));
                    if (redirectTarget != null) {
                        if (atkHasInfect) {
                            infectDamageRedirectedToGuard += power;
                        } else {
                            damageRedirectedToGuard += power;
                        }
                    } else if (gameHelper.isSourceDamagePreventedForPlayer(gameData, defenderId, atk.getId())) {
                    // Source-specific damage prevention — skip this attacker's damage
                } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getEffectiveColor())) {
                        if (atkHasInfect) {
                            poisonDamageToDefendingPlayer += power;
                        } else {
                            damageToDefendingPlayer += power;
                        }
                    }
                    combatDamageDealt.merge(atk, power, Integer::sum);
                    combatDamageDealtToPlayer.merge(atk, power, Integer::sum);
                }
            } else {
                // Attacker deals damage to surviving blockers (skip first-strike-only, allow double strike)
                if (!atkSkipPhase2 && !gameQueryService.isPreventedFromDealingDamage(gameData, atk)) {
                    int remaining = gameQueryService.getEffectiveCombatDamage(gameData, atk);
                    for (int blkIdx : blkIndices) {
                        if (deadDefenderIndices.contains(blkIdx)) continue;
                        Permanent blk = defBf.get(blkIdx);
                        int remainingToughness = gameQueryService.getEffectiveToughness(gameData, blk) - defDamageTaken.getOrDefault(blkIdx, 0);
                        int dmg = Math.min(remaining, Math.max(0, remainingToughness));
                        if (!gameQueryService.hasProtectionFrom(gameData, blk, atk.getEffectiveColor())) {
                            int actualDmg = gameQueryService.applyDamageMultiplier(gameData, dmg);
                            applyCombatCreatureDamage(gameData, atk, blk, blkIdx, actualDmg, defDamageTaken);
                            combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                            recordCombatDamageToCreature(gameData, combatDamageDealtToCreatures, combatDamageDealerControllers,
                                    atk, activeId, blk, actualDmg);
                        }
                        remaining -= dmg;
                    }
                    // Trample: excess damage goes to defending player
                    if (remaining > 0 && gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) {
                        int doubledRemaining = gameQueryService.applyDamageMultiplier(gameData, remaining);
                        if (redirectTarget != null) {
                            if (atkHasInfect) {
                                infectDamageRedirectedToGuard += doubledRemaining;
                            } else {
                                damageRedirectedToGuard += doubledRemaining;
                            }
                        } else if (gameHelper.isSourceDamagePreventedForPlayer(gameData, defenderId, atk.getId())) {
                            // Source-specific damage prevention — skip trample damage
                        } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getEffectiveColor())) {
                            if (atkHasInfect) {
                                poisonDamageToDefendingPlayer += doubledRemaining;
                            } else {
                                damageToDefendingPlayer += doubledRemaining;
                            }
                        }
                        combatDamageDealt.merge(atk, doubledRemaining, Integer::sum);
                        combatDamageDealtToPlayer.merge(atk, doubledRemaining, Integer::sum);
                    }
                }
            }
            if (!blkIndices.isEmpty()) {
                // Surviving blockers deal damage to attacker (skip first-strike-only, allow double strike)
                for (int blkIdx : blkIndices) {
                    if (deadDefenderIndices.contains(blkIdx)) continue;
                    Permanent blk = defBf.get(blkIdx);
                    boolean blkSkipPhase2 = gameQueryService.hasKeyword(gameData, blk, Keyword.FIRST_STRIKE)
                            && !gameQueryService.hasKeyword(gameData, blk, Keyword.DOUBLE_STRIKE);
                    if (!blkSkipPhase2 && !gameQueryService.isPreventedFromDealingDamage(gameData, blk)
                            && !gameQueryService.hasProtectionFrom(gameData, atk, blk.getEffectiveColor())) {
                        int actualDmg = gameQueryService.applyDamageMultiplier(gameData, phase2BlockerDamage.getOrDefault(blkIdx, 0));
                        applyCombatCreatureDamage(gameData, blk, atk, atkIdx, actualDmg, atkDamageTaken);
                        combatDamageDealt.merge(blk, actualDmg, Integer::sum);
                        recordCombatDamageToCreature(gameData, combatDamageDealtToCreatures, combatDamageDealerControllers,
                                blk, defenderId, atk, actualDmg);
                    }
                }
            }
        }

        // Determine phase 2 casualties (apply prevention shields)
        for (int atkIdx : attackingIndices) {
            if (deadAttackerIndices.contains(atkIdx)) continue;
            int dmg = atkDamageTaken.getOrDefault(atkIdx, 0);
            dmg = gameHelper.applyCreaturePreventionShield(gameData, atkBf.get(atkIdx), dmg);
            atkDamageTaken.put(atkIdx, dmg);
            int effToughness = gameQueryService.getEffectiveToughness(gameData, atkBf.get(atkIdx));
            if (effToughness <= 0) {
                deadAttackerIndices.add(atkIdx);
            } else if (dmg >= effToughness
                    && !gameQueryService.hasKeyword(gameData, atkBf.get(atkIdx), Keyword.INDESTRUCTIBLE)
                    && !gameHelper.tryRegenerate(gameData, atkBf.get(atkIdx))) {
                deadAttackerIndices.add(atkIdx);
            }
        }
        for (List<Integer> blkIndices : blockerMap.values()) {
            for (int blkIdx : blkIndices) {
                if (deadDefenderIndices.contains(blkIdx)) continue;
                int dmg = defDamageTaken.getOrDefault(blkIdx, 0);
                dmg = gameHelper.applyCreaturePreventionShield(gameData, defBf.get(blkIdx), dmg);
                defDamageTaken.put(blkIdx, dmg);
                int effToughness = gameQueryService.getEffectiveToughness(gameData, defBf.get(blkIdx));
                if (effToughness <= 0) {
                    deadDefenderIndices.add(blkIdx);
                } else if (dmg >= effToughness
                        && !gameQueryService.hasKeyword(gameData, defBf.get(blkIdx), Keyword.INDESTRUCTIBLE)
                        && !gameHelper.tryRegenerate(gameData, defBf.get(blkIdx))) {
                    deadDefenderIndices.add(blkIdx);
                }
            }
        }

        // Apply infect redirected damage to guard creature as -1/-1 counters
        if (redirectTarget != null && infectDamageRedirectedToGuard > 0) {
            infectDamageRedirectedToGuard = gameHelper.applyCreaturePreventionShield(gameData, redirectTarget, infectDamageRedirectedToGuard);
            if (infectDamageRedirectedToGuard > 0) {
                redirectTarget.setMinusOneMinusOneCounters(redirectTarget.getMinusOneMinusOneCounters() + infectDamageRedirectedToGuard);
                String redirectLog = redirectTarget.getCard().getName() + " gets " + infectDamageRedirectedToGuard + " -1/-1 counters from redirected infect damage.";
                gameBroadcastService.logAndBroadcast(gameData, redirectLog);
            }
        }

        // Apply redirected damage to guard creature (e.g. Kjeldoran Royal Guard)
        if (redirectTarget != null && (damageRedirectedToGuard > 0 || (infectDamageRedirectedToGuard > 0 && gameQueryService.getEffectiveToughness(gameData, redirectTarget) <= 0))) {
            damageRedirectedToGuard = gameHelper.applyCreaturePreventionShield(gameData, redirectTarget, damageRedirectedToGuard);
            if (damageRedirectedToGuard > 0) {
                String redirectLog = redirectTarget.getCard().getName() + " absorbs " + damageRedirectedToGuard + " redirected combat damage.";
                gameBroadcastService.logAndBroadcast(gameData, redirectLog);
            }

            int guardToughness = gameQueryService.getEffectiveToughness(gameData, redirectTarget);
            if (guardToughness <= 0) {
                // CR 704.5f: 0 toughness from -1/-1 counters
                permanentRemovalService.removePermanentToGraveyard(gameData, redirectTarget);
                String deathLog = redirectTarget.getCard().getName() + " dies from 0 toughness.";
                gameBroadcastService.logAndBroadcast(gameData, deathLog);
            } else if (damageRedirectedToGuard >= guardToughness
                    && !gameQueryService.hasKeyword(gameData, redirectTarget, Keyword.INDESTRUCTIBLE)
                    && !gameHelper.tryRegenerate(gameData, redirectTarget)) {
                permanentRemovalService.removePermanentToGraveyard(gameData, redirectTarget);
                String deathLog = redirectTarget.getCard().getName() + " is destroyed by redirected combat damage.";
                gameBroadcastService.logAndBroadcast(gameData, deathLog);
            }
        }

        // Process lifelink before removing dead creatures
        processLifelink(gameData, combatDamageDealt);

        // Process life gain from damage triggers (e.g. Spirit Link) before removing dead creatures
        processGainLifeEqualToDamageDealt(gameData, combatDamageDealt);

        // Remove dead creatures (descending order to preserve indices) and move to graveyard
        List<String> deadCreatureNames = new ArrayList<>();
        for (int idx : deadAttackerIndices) {
            Permanent dead = atkBf.get(idx);
            deadCreatureNames.add(gameData.playerIdToName.get(activeId) + "'s " + dead.getCard().getName());
            // Stolen creatures go to their owner's graveyard
            UUID atkGraveyardOwner = gameData.stolenCreatures.getOrDefault(dead.getId(), activeId);
            gameData.stolenCreatures.remove(dead.getId());
            gameHelper.addCardToGraveyard(gameData, atkGraveyardOwner, dead.getOriginalCard(), Zone.BATTLEFIELD);
            gameHelper.collectDeathTrigger(gameData, dead.getCard(), activeId, true);
            gameHelper.checkAllyCreatureDeathTriggers(gameData, activeId);
            atkBf.remove(idx);
        }
        for (int idx : deadDefenderIndices) {
            Permanent dead = defBf.get(idx);
            deadCreatureNames.add(gameData.playerIdToName.get(defenderId) + "'s " + dead.getCard().getName());
            // Stolen creatures go to their owner's graveyard
            UUID defGraveyardOwner = gameData.stolenCreatures.getOrDefault(dead.getId(), defenderId);
            gameData.stolenCreatures.remove(dead.getId());
            gameHelper.addCardToGraveyard(gameData, defGraveyardOwner, dead.getOriginalCard(), Zone.BATTLEFIELD);
            gameHelper.collectDeathTrigger(gameData, dead.getCard(), defenderId, true);
            gameHelper.checkAllyCreatureDeathTriggers(gameData, defenderId);
            defBf.remove(idx);
        }
        if (!deadAttackerIndices.isEmpty() || !deadDefenderIndices.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        // Apply life loss (with prevention shield and Pariah redirect)
        damageToDefendingPlayer = gameHelper.applyPlayerPreventionShield(gameData, defenderId, damageToDefendingPlayer);
        damageToDefendingPlayer = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, defenderId, damageToDefendingPlayer, "combat");
        if (damageToDefendingPlayer > 0) {
            int currentLife = gameData.playerLifeTotals.getOrDefault(defenderId, 20);
            gameData.playerLifeTotals.put(defenderId, currentLife - damageToDefendingPlayer);

            String logEntry = gameData.playerIdToName.get(defenderId) + " takes " + damageToDefendingPlayer + " combat damage.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        // Apply poison counters from infect combat damage
        poisonDamageToDefendingPlayer = gameHelper.applyPlayerPreventionShield(gameData, defenderId, poisonDamageToDefendingPlayer);
        if (poisonDamageToDefendingPlayer > 0) {
            int currentPoison = gameData.playerPoisonCounters.getOrDefault(defenderId, 0);
            gameData.playerPoisonCounters.put(defenderId, currentPoison + poisonDamageToDefendingPlayer);

            String logEntry = gameData.playerIdToName.get(defenderId) + " gets " + poisonDamageToDefendingPlayer + " poison counters.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        if (!deadCreatureNames.isEmpty()) {
            String logEntry = String.join(", ", deadCreatureNames) + " died in combat.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }



        if (!deadAttackerIndices.isEmpty() || !deadDefenderIndices.isEmpty()) {

        }

        log.info("Game {} - Combat damage resolved: {} damage to defender, {} creatures died",
                gameData.id, damageToDefendingPlayer, deadAttackerIndices.size() + deadDefenderIndices.size());

        // Check win condition
        if (gameHelper.checkWinCondition(gameData)) {
            return CombatResult.DONE;
        }

        int stackSizeBeforeDamageTriggers = gameData.stack.size();
        processCombatDamageToCreatureTriggers(gameData, combatDamageDealtToCreatures, combatDamageDealerControllers);

        // Process combat damage to player triggers (e.g. Cephalid Constable) after all combat is resolved
        processCombatDamageToPlayerTriggers(gameData, combatDamageDealtToPlayer, activeId, defenderId);

        // Process defender-side damage triggers (e.g. Dissipation Field)
        for (var dmgEntry : combatDamageDealtToPlayer.entrySet()) {
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

        return CombatResult.ADVANCE_AND_AUTO_PASS;
    }

    private void processLifelink(GameData gameData, Map<Permanent, Integer> combatDamageDealt) {
        for (var entry : combatDamageDealt.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            if (!gameQueryService.hasKeyword(gameData, creature, Keyword.LIFELINK)) continue;

            // Find the controller of this creature
            UUID controllerId = null;
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(playerId);
                if (bf != null && bf.contains(creature)) {
                    controllerId = playerId;
                    break;
                }
            }
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

            List<CardEffect> allDamageEffects = new ArrayList<>();
            allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
            allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
            for (CardEffect effect : allDamageEffects) {
                if (effect instanceof DrawCardEffect drawEffect) {
                    String logEntry = creature.getCard().getName() + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " draws " + drawEffect.amount() + " card" + (drawEffect.amount() > 1 ? "s" : "") + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    for (int i = 0; i < drawEffect.amount(); i++) {
                        gameHelper.resolveDrawCard(gameData, attackerId);
                    }
                } else if (effect instanceof ExileTopCardsRepeatOnDuplicateEffect exileEffect) {
                    gameHelper.resolveExileTopCardsRepeatOnDuplicate(gameData, creature, defenderId, exileEffect);
                } else if (effect instanceof RandomDiscardEffect randomDiscardEffect) {
                    List<Card> defenderHand = gameData.playerHands.get(defenderId);
                    if (defenderHand == null || defenderHand.isEmpty()) {
                        String logEntry = creature.getCard().getName() + "'s ability triggers, but " + gameData.playerIdToName.get(defenderId) + " has no cards to discard.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    } else {
                        gameData.discardCausedByOpponent = true;
                        for (int i = 0; i < randomDiscardEffect.amount(); i++) {
                            List<Card> currentHand = gameData.playerHands.get(defenderId);
                            if (currentHand.isEmpty()) break;
                            int randomIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(currentHand.size());
                            Card discarded = currentHand.remove(randomIndex);
                            gameHelper.addCardToGraveyard(gameData, defenderId, discarded);
                            String logEntry = creature.getCard().getName() + "'s ability triggers — " + gameData.playerIdToName.get(defenderId) + " discards " + discarded.getName() + " at random.";
                            gameBroadcastService.logAndBroadcast(gameData, logEntry);
                            log.info("Game {} - {} triggers random discard: {} discards {}", gameData.id, creature.getCard().getName(), gameData.playerIdToName.get(defenderId), discarded.getName());
                            triggerCollectionService.checkDiscardTriggers(gameData, defenderId, discarded);
                        }
                    }
                } else if (effect instanceof ReturnPermanentsOnCombatDamageToPlayerEffect) {
                    // Collect valid permanents the damaged player controls
                    List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
                    List<UUID> validIds = new ArrayList<>();
                    for (Permanent perm : defenderBattlefield) {
                        validIds.add(perm.getId());
                    }

                    if (validIds.isEmpty()) {
                        String logEntry = creature.getCard().getName() + "'s ability triggers, but " + gameData.playerIdToName.get(defenderId) + " has no permanents.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        continue;
                    }

                    String logEntry = creature.getCard().getName() + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " may return up to " + damageDealt + " permanent" + (damageDealt > 1 ? "s" : "") + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} combat damage trigger: {} damage, {} valid targets", gameData.id, creature.getCard().getName(), damageDealt, validIds.size());

                    gameData.pendingCombatDamageBounceTargetPlayerId = defenderId;
                    int maxCount = Math.min(damageDealt, validIds.size());
                    playerInputService.beginMultiPermanentChoice(gameData, attackerId, validIds, maxCount, "Return up to " + damageDealt + " permanent" + (damageDealt > 1 ? "s" : "") + " to their owner's hand.");
                    return;
                } else if (effect instanceof PutAwakeningCountersOnTargetLandsEffect) {
                    List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
                    List<UUID> validLandIds = new ArrayList<>();
                    for (Permanent perm : attackerBattlefield) {
                        if (perm.getCard().getType() == CardType.LAND
                                || perm.getCard().getAdditionalTypes().contains(CardType.LAND)) {
                            validLandIds.add(perm.getId());
                        }
                    }

                    if (validLandIds.isEmpty()) {
                        String logEntry = creature.getCard().getName() + "'s ability triggers, but " + gameData.playerIdToName.get(attackerId) + " controls no lands.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        continue;
                    }

                    String logEntry = creature.getCard().getName() + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " may put awakening counters on lands.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} combat damage trigger: {} valid lands", gameData.id, creature.getCard().getName(), validLandIds.size());

                    gameData.pendingAwakeningCounterPlacement = true;
                    playerInputService.beginMultiPermanentChoice(gameData, attackerId, validLandIds, validLandIds.size(), "Choose any number of lands to put awakening counters on.");
                    return;
                } else if (effect instanceof TargetPlayerLosesGameEffect) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            creature.getCard(),
                            attackerId,
                            creature.getCard().getName() + "'s triggered ability",
                            List.of(new TargetPlayerLosesGameEffect(defenderId)),
                            null,
                            creature.getId()
                    ));
                    String logEntry = creature.getCard().getName() + "'s ability triggers â€” " + gameData.playerIdToName.get(defenderId) + " loses the game.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        }
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
                                              Map<Permanent, List<UUID>> combatDamageDealtToCreatures,
                                              Map<Permanent, UUID> combatDamageDealerControllers,
                                              Permanent source,
                                              UUID controllerId,
                                              Permanent target,
                                              int damage) {
        if (damage <= 0) {
            return;
        }
        combatDamageDealerControllers.putIfAbsent(source, controllerId);
        combatDamageDealtToCreatures.computeIfAbsent(source, ignored -> new ArrayList<>()).add(target.getId());
        gameHelper.recordCreatureDamagedByPermanent(gameData, source.getId(), target, damage);
    }

    /**
     * Applies combat damage to a creature. If the source has infect, applies -1/-1 counters
     * (with prevention shield consumed immediately). Otherwise, accumulates regular damage.
     */
    private void applyCombatCreatureDamage(GameData gameData, Permanent source, Permanent target,
                                           int targetIdx, int damage, Map<Integer, Integer> damageTakenMap) {
        if (gameQueryService.hasKeyword(gameData, source, Keyword.INFECT)) {
            int afterShield = gameHelper.applyCreaturePreventionShield(gameData, target, damage);
            if (afterShield > 0) {
                target.setMinusOneMinusOneCounters(target.getMinusOneMinusOneCounters() + afterShield);
            }
        } else {
            damageTakenMap.merge(targetIdx, damage, Integer::sum);
        }
    }

    // ===== Aura trigger helpers =====

    private void checkAuraTriggersForCreature(GameData gameData, Permanent creature, EffectSlot slot) {
        // Find the creature's controller at trigger time
        UUID creatureControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(creature)) {
                creatureControllerId = pid;
                break;
            }
        }
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
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(attacker)) {
                controllerId = pid;
                break;
            }
        }
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

        List<StackEntry> newEntries = new ArrayList<>();
        for (int i = startIndex; i < gameData.stack.size(); i++) {
            newEntries.add(gameData.stack.get(i));
        }

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
        for (int i = gameData.stack.size() - 1; i >= startIndex; i--) {
            gameData.stack.remove(i);
        }
        gameData.stack.addAll(apTriggers);
        gameData.stack.addAll(napTriggers);
    }

    private boolean assignsCombatDamageAsThoughUnblocked(Permanent attacker) {
        return attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(AssignCombatDamageAsThoughUnblockedEffect.class::isInstance);
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

    private CombatDamagePhase1State savePhase1State(
            Set<Integer> deadAttackerIndices, Set<Integer> deadDefenderIndices,
            Map<Integer, Integer> atkDamageTaken, Map<Integer, Integer> defDamageTaken,
            Map<Permanent, Integer> combatDamageDealt, Map<Permanent, Integer> combatDamageDealtToPlayer,
            Map<Permanent, List<UUID>> combatDamageDealtToCreatures,
            Map<Permanent, UUID> combatDamageDealerControllers,
            int damageToDefendingPlayer, int damageRedirectedToGuard,
            Map<Integer, List<Integer>> blockerMap, boolean anyFirstStrike) {
        // Convert Permanent-keyed maps to UUID-keyed maps for serialization
        Map<UUID, Integer> dealtByUUID = new HashMap<>();
        for (var e : combatDamageDealt.entrySet()) {
            dealtByUUID.put(e.getKey().getId(), e.getValue());
        }
        Map<UUID, Integer> dealtToPlayerByUUID = new HashMap<>();
        for (var e : combatDamageDealtToPlayer.entrySet()) {
            dealtToPlayerByUUID.put(e.getKey().getId(), e.getValue());
        }
        Map<UUID, List<UUID>> dealtToCreaturesByUUID = new HashMap<>();
        for (var e : combatDamageDealtToCreatures.entrySet()) {
            dealtToCreaturesByUUID.put(e.getKey().getId(), new ArrayList<>(e.getValue()));
        }
        Map<UUID, UUID> dealerControllersByUUID = new HashMap<>();
        for (var e : combatDamageDealerControllers.entrySet()) {
            dealerControllersByUUID.put(e.getKey().getId(), e.getValue());
        }
        return new CombatDamagePhase1State(
                new TreeSet<>(deadAttackerIndices), new TreeSet<>(deadDefenderIndices),
                new HashMap<>(atkDamageTaken), new HashMap<>(defDamageTaken),
                dealtByUUID, dealtToPlayerByUUID, dealtToCreaturesByUUID, dealerControllersByUUID,
                damageToDefendingPlayer, damageRedirectedToGuard,
                new LinkedHashMap<>(blockerMap), anyFirstStrike);
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
        for (int blkIdx : livingBlockers) {
            Permanent blk = defBf.get(blkIdx);
            targetViews.add(new CombatDamageTargetView(
                    blk.getId().toString(), blk.getCard().getName(),
                    gameQueryService.getEffectiveToughness(gameData, blk),
                    p1.defDamageTaken.getOrDefault(blkIdx, 0),
                    false));
        }

        boolean isTrample = gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE);
        boolean addPlayer = isTrample || assignsCombatDamageAsThoughUnblocked(atk);
        if (addPlayer) {
            targetViews.add(new CombatDamageTargetView(
                    defenderId.toString(), gameData.playerIdToName.get(defenderId),
                    0, 0, true));
        }

        int totalDamage = gameQueryService.getEffectiveCombatDamage(gameData, atk);

        // Build domain targets for interaction context
        List<CombatDamageTarget> domainTargets = new ArrayList<>();
        for (int blkIdx : livingBlockers) {
            Permanent blk = defBf.get(blkIdx);
            domainTargets.add(new CombatDamageTarget(
                    blk.getId(), blk.getCard().getName(),
                    gameQueryService.getEffectiveToughness(gameData, blk),
                    p1.defDamageTaken.getOrDefault(blkIdx, 0),
                    false));
        }
        if (addPlayer) {
            domainTargets.add(new CombatDamageTarget(
                    defenderId, gameData.playerIdToName.get(defenderId),
                    0, 0, true));
        }

        gameData.interaction.beginCombatDamageAssignment(activeId, atkIdx, atk.getId(),
                atk.getCard().getName(), totalDamage, domainTargets, isTrample);

        CombatDamageAssignmentNotification notification = new CombatDamageAssignmentNotification(
                atkIdx, atk.getId().toString(), atk.getCard().getName(), totalDamage, targetViews, isTrample);
        sessionManager.sendToPlayer(activeId, notification);
    }

    void handleCombatDamageAssigned(GameData gameData, int attackerIndex, Map<UUID, Integer> assignments) {
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
            for (int blkIdx : livingBlockers) {
                Permanent blk = defBf.get(blkIdx);
                int lethal = gameQueryService.getEffectiveToughness(gameData, blk)
                        - p1.defDamageTaken.getOrDefault(blkIdx, 0);
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

    void clearCombatState(GameData gameData) {
        gameData.forEachBattlefield((playerId, battlefield) ->
                battlefield.forEach(Permanent::clearCombatState));
        // Clear combat damage assignment state
        gameData.combatDamagePlayerAssignments.clear();
        gameData.combatDamagePendingIndices.clear();
        gameData.combatDamagePhase1Complete = false;
        gameData.combatDamagePhase1State = null;
    }

    void processEndOfCombatSacrifices(GameData gameData) {
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



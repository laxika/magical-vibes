package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BlockOnlyFlyersEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsLandTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.IslandwalkEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    private final GameHelper gameHelper;

    // ===== Query methods =====

    List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        UUID defenderId = gameHelper.getOpponentId(gameData, playerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (gameHelper.isCreature(gameData, p) && !p.isTapped() && !p.isSummoningSick() && !gameHelper.hasKeyword(gameData, p, Keyword.DEFENDER) && !gameHelper.hasAuraWithEffect(gameData, p, EnchantedCreatureCantAttackOrBlockEffect.class)) {
                if (isCantAttackDueToLandRestriction(p, defenderBattlefield)) {
                    continue;
                }
                indices.add(i);
            }
        }
        return indices;
    }

    private boolean isCantAttackDueToLandRestriction(Permanent attacker, List<Permanent> defenderBattlefield) {
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantAttackUnlessDefenderControlsLandTypeEffect restriction) {
                boolean defenderHasLand = defenderBattlefield != null && defenderBattlefield.stream()
                        .anyMatch(p -> p.getCard().getSubtypes().contains(restriction.landType()));
                if (!defenderHasLand) {
                    return true;
                }
            }
        }
        return false;
    }

    List<Integer> getBlockableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (gameHelper.isCreature(gameData, p) && !p.isTapped() && !gameHelper.hasAuraWithEffect(gameData, p, EnchantedCreatureCantAttackOrBlockEffect.class)) {
                indices.add(i);
            }
        }
        return indices;
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
            skipToEndOfCombat(gameData);
            return;
        }

        gameData.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        gameHelper.getSessionManager().sendToPlayer(activeId, new AvailableAttackersMessage(attackable));
    }

    void skipToEndOfCombat(GameData gameData) {
        gameData.currentStep = TurnStep.END_OF_COMBAT;
        gameData.awaitingInput = null;
        clearCombatState(gameData);

        String logEntry = "Step: " + TurnStep.END_OF_COMBAT.getDisplayName();
        gameHelper.logAndBroadcast(gameData, logEntry);
        gameHelper.broadcastGameState(gameData);
    }

    void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices, TurnProgressionCallback callback) {
        if (gameData.awaitingInput != AwaitingInput.ATTACKER_DECLARATION) {
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

        gameData.awaitingInput = null;

        if (attackerIndices.isEmpty()) {
            log.info("Game {} - {} declares no attackers", gameData.id, player.getUsername());
            skipToEndOfCombat(gameData);
            callback.resolveAutoPass(gameData);
            return;
        }

        // Check attack tax (e.g. Windborn Muse / Ghostly Prison)
        int taxPerCreature = gameHelper.getAttackPaymentPerCreature(gameData, playerId);
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
            if (!gameHelper.hasKeyword(gameData, attacker, Keyword.VIGILANCE)) {
                attacker.tap();
            }
        }

        String logEntry = player.getUsername() + " declares " + attackerIndices.size() +
                " attacker" + (attackerIndices.size() > 1 ? "s" : "") + ".";
        gameHelper.logAndBroadcast(gameData, logEntry);



        // Check for "when this creature attacks" triggers
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!attacker.getCard().getEffects(EffectSlot.ON_ATTACK).isEmpty()) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        attacker.getCard(),
                        playerId,
                        attacker.getCard().getName() + "'s attack trigger",
                        new ArrayList<>(attacker.getCard().getEffects(EffectSlot.ON_ATTACK)),
                        null,
                        attacker.getId()
                ));
                String triggerLog = attacker.getCard().getName() + "'s attack ability triggers.";
                gameData.gameLog.add(triggerLog);
                log.info("Game {} - {} attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
            }
        }
        if (!gameData.stack.isEmpty()) {

        }

        log.info("Game {} - {} declares {} attackers", gameData.id, player.getUsername(), attackerIndices.size());

        callback.advanceStep(gameData);
        callback.resolveAutoPass(gameData);
    }

    void handleDeclareBlockersStep(GameData gameData, TurnProgressionCallback callback) {
        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameHelper.getOpponentId(gameData, activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);
        List<Integer> attackerIndices = getAttackingCreatureIndices(gameData, activeId);

        // Filter out attackers that can't be blocked
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        attackerIndices = attackerIndices.stream()
                .filter(idx -> !attackerBattlefield.get(idx).isCantBeBlocked()
                        && attackerBattlefield.get(idx).getCard().getEffects(EffectSlot.STATIC).stream()
                                .noneMatch(e -> e instanceof CantBeBlockedEffect))
                .toList();

        if (blockable.isEmpty() || attackerIndices.isEmpty()) {
            log.info("Game {} - Defending player has no creatures that can block or no blockable attackers", gameData.id);
            callback.advanceStep(gameData);
            return;
        }

        gameData.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        gameHelper.getSessionManager().sendToPlayer(defenderId, new AvailableBlockersMessage(blockable, attackerIndices));
    }

    void declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments, TurnProgressionCallback callback) {
        if (gameData.awaitingInput != AwaitingInput.BLOCKER_DECLARATION) {
            throw new IllegalStateException("Not awaiting blocker declaration");
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameHelper.getOpponentId(gameData, activeId);

        if (!player.getId().equals(defenderId)) {
            throw new IllegalStateException("Only the defending player can declare blockers");
        }

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);

        // Compute max blocks per creature (1 + additional from static effects)
        int additionalBlocks = 0;
        for (Permanent p : defenderBattlefield) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantAdditionalBlockEffect e) {
                    additionalBlocks += e.additionalBlocks();
                }
            }
        }
        int maxBlocksPerCreature = 1 + additionalBlocks;

        // Validate assignments
        Map<Integer, Integer> blockerUsageCount = new HashMap<>();
        Set<String> blockerAttackerPairs = new HashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            int blockerIdx = assignment.blockerIndex();
            int attackerIdx = assignment.attackerIndex();

            if (!blockable.contains(blockerIdx)) {
                throw new IllegalStateException("Invalid blocker index: " + blockerIdx);
            }
            int usageCount = blockerUsageCount.merge(blockerIdx, 1, Integer::sum);
            if (usageCount > maxBlocksPerCreature) {
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
            if (attacker.isCantBeBlocked()) {
                throw new IllegalStateException(attacker.getCard().getName() + " can't be blocked this turn");
            }
            boolean hasCantBeBlockedStatic = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof CantBeBlockedEffect);
            if (hasCantBeBlockedStatic) {
                throw new IllegalStateException(attacker.getCard().getName() + " can't be blocked");
            }
            if (gameHelper.hasKeyword(gameData, attacker, Keyword.FLYING)
                    && !gameHelper.hasKeyword(gameData, blocker, Keyword.FLYING)
                    && !gameHelper.hasKeyword(gameData, blocker, Keyword.REACH)) {
                throw new IllegalStateException(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (flying)");
            }
            boolean blockOnlyFlyers = blocker.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof BlockOnlyFlyersEffect);
            if (blockOnlyFlyers && !gameHelper.hasKeyword(gameData, attacker, Keyword.FLYING)) {
                throw new IllegalStateException(blocker.getCard().getName() + " can only block creatures with flying");
            }
            boolean hasIslandwalk = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof IslandwalkEffect);
            if (hasIslandwalk) {
                boolean defenderControlsIsland = defenderBattlefield.stream()
                        .anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.ISLAND));
                if (defenderControlsIsland) {
                    throw new IllegalStateException(attacker.getCard().getName() + " can't be blocked (islandwalk)");
                }
            }
            if (gameHelper.hasProtectionFrom(gameData, attacker, blocker.getCard().getColor())) {
                throw new IllegalStateException(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (protection)");
            }
        }

        gameData.awaitingInput = null;

        // Mark creatures as blocking
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(assignment.attackerIndex());
        }

        if (!blockerAssignments.isEmpty()) {
            String logEntry = player.getUsername() + " declares " + blockerAssignments.size() +
                    " blocker" + (blockerAssignments.size() > 1 ? "s" : "") + ".";
            gameHelper.logAndBroadcast(gameData, logEntry);
        }

        // Check for "when this creature blocks" triggers
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            if (!blocker.getCard().getEffects(EffectSlot.ON_BLOCK).isEmpty()) {
                Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());
                // Only set target if effects need the attacker reference
                boolean needsAttackerTarget = blocker.getCard().getEffects(EffectSlot.ON_BLOCK).stream()
                        .anyMatch(e -> e instanceof DestroyBlockedCreatureAndSelfEffect);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        blocker.getCard(),
                        defenderId,
                        blocker.getCard().getName() + "'s block trigger",
                        new ArrayList<>(blocker.getCard().getEffects(EffectSlot.ON_BLOCK)),
                        needsAttackerTarget ? attacker.getId() : null,
                        blocker.getId()
                ));
                String triggerLog = blocker.getCard().getName() + "'s block ability triggers.";
                gameHelper.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} block trigger pushed onto stack", gameData.id, blocker.getCard().getName());
            }
        }


        if (!gameData.stack.isEmpty()) {

        }

        log.info("Game {} - {} declares {} blockers", gameData.id, player.getUsername(), blockerAssignments.size());

        callback.advanceStep(gameData);
        callback.resolveAutoPass(gameData);
    }

    // ===== Combat damage resolution =====

    void resolveCombatDamage(GameData gameData, TurnProgressionCallback callback) {
        if (gameData.preventAllCombatDamage) {
            String logEntry = "All combat damage is prevented.";
            gameHelper.logAndBroadcast(gameData, logEntry);

            callback.advanceStep(gameData);
            callback.resolveAutoPass(gameData);
            return;
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameHelper.getOpponentId(gameData, activeId);

        List<Permanent> atkBf = gameData.playerBattlefields.get(activeId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);

        // Check for combat damage redirect (e.g. Kjeldoran Royal Guard)
        Permanent redirectTarget = gameData.combatDamageRedirectTarget != null
                ? gameHelper.findPermanentById(gameData, gameData.combatDamageRedirectTarget) : null;
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
            if (gameHelper.hasKeyword(gameData, atkBf.get(atkIdx), Keyword.FIRST_STRIKE)
                    || gameHelper.hasKeyword(gameData, atkBf.get(atkIdx), Keyword.DOUBLE_STRIKE)) {
                anyFirstStrike = true;
                break;
            }
        }
        if (!anyFirstStrike) {
            for (List<Integer> blkIndices : blockerMap.values()) {
                for (int blkIdx : blkIndices) {
                    if (gameHelper.hasKeyword(gameData, defBf.get(blkIdx), Keyword.FIRST_STRIKE)
                            || gameHelper.hasKeyword(gameData, defBf.get(blkIdx), Keyword.DOUBLE_STRIKE)) {
                        anyFirstStrike = true;
                        break;
                    }
                }
                if (anyFirstStrike) break;
            }
        }

        int damageToDefendingPlayer = 0;
        Set<Integer> deadAttackerIndices = new TreeSet<>(Collections.reverseOrder());
        Set<Integer> deadDefenderIndices = new TreeSet<>(Collections.reverseOrder());

        // Track cumulative damage on each creature
        Map<Integer, Integer> atkDamageTaken = new HashMap<>();
        Map<Integer, Integer> defDamageTaken = new HashMap<>();
        Map<Permanent, Integer> combatDamageDealt = new HashMap<>();
        Map<Permanent, Integer> combatDamageDealtToPlayer = new HashMap<>();

        // Phase 1: First strike damage
        if (anyFirstStrike) {
            for (var entry : blockerMap.entrySet()) {
                int atkIdx = entry.getKey();
                List<Integer> blkIndices = entry.getValue();
                Permanent atk = atkBf.get(atkIdx);
                boolean atkHasFS = gameHelper.hasKeyword(gameData, atk, Keyword.FIRST_STRIKE)
                        || gameHelper.hasKeyword(gameData, atk, Keyword.DOUBLE_STRIKE);

                if (blkIndices.isEmpty()) {
                    // Unblocked first striker deals damage to player (or redirect target)
                    if (atkHasFS && !gameHelper.isPreventedFromDealingDamage(gameData, atk)) {
                        int power = gameHelper.getEffectivePower(gameData, atk);
                        if (redirectTarget != null) {
                            damageRedirectedToGuard += power;
                        } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getCard().getColor())) {
                            damageToDefendingPlayer += power;
                        }
                        combatDamageDealt.merge(atk, power, Integer::sum);
                        combatDamageDealtToPlayer.merge(atk, power, Integer::sum);
                    }
                } else {
                    // First strike attacker deals damage to blockers
                    if (atkHasFS && !gameHelper.isPreventedFromDealingDamage(gameData, atk)) {
                        int remaining = gameHelper.getEffectivePower(gameData, atk);
                        for (int blkIdx : blkIndices) {
                            Permanent blk = defBf.get(blkIdx);
                            int dmg = Math.min(remaining, gameHelper.getEffectiveToughness(gameData, blk));
                            if (!gameHelper.hasProtectionFrom(gameData, blk, atk.getCard().getColor())) {
                                defDamageTaken.merge(blkIdx, dmg, Integer::sum);
                                combatDamageDealt.merge(atk, dmg, Integer::sum);
                            }
                            remaining -= dmg;
                        }
                    }
                    // First strike / double strike blockers deal damage to attacker
                    for (int blkIdx : blkIndices) {
                        Permanent blk = defBf.get(blkIdx);
                        if ((gameHelper.hasKeyword(gameData, blk, Keyword.FIRST_STRIKE) || gameHelper.hasKeyword(gameData, blk, Keyword.DOUBLE_STRIKE))
                                && !gameHelper.isPreventedFromDealingDamage(gameData, blk)
                                && !gameHelper.hasProtectionFrom(gameData, atk, blk.getCard().getColor())) {
                            atkDamageTaken.merge(atkIdx, gameHelper.getEffectivePower(gameData, blk), Integer::sum);
                            combatDamageDealt.merge(blk, gameHelper.getEffectivePower(gameData, blk), Integer::sum);
                        }
                    }
                }
            }

            // Determine phase 1 casualties (apply prevention shields)
            for (int atkIdx : attackingIndices) {
                int dmg = atkDamageTaken.getOrDefault(atkIdx, 0);
                dmg = gameHelper.applyCreaturePreventionShield(gameData, atkBf.get(atkIdx), dmg);
                atkDamageTaken.put(atkIdx, dmg);
                if (dmg >= gameHelper.getEffectiveToughness(gameData, atkBf.get(atkIdx))
                        && !gameHelper.tryRegenerate(gameData, atkBf.get(atkIdx))) {
                    deadAttackerIndices.add(atkIdx);
                }
            }
            for (List<Integer> blkIndices : blockerMap.values()) {
                for (int blkIdx : blkIndices) {
                    int dmg = defDamageTaken.getOrDefault(blkIdx, 0);
                    dmg = gameHelper.applyCreaturePreventionShield(gameData, defBf.get(blkIdx), dmg);
                    defDamageTaken.put(blkIdx, dmg);
                    if (dmg >= gameHelper.getEffectiveToughness(gameData, defBf.get(blkIdx))
                            && !gameHelper.tryRegenerate(gameData, defBf.get(blkIdx))) {
                        deadDefenderIndices.add(blkIdx);
                    }
                }
            }
        }

        // Phase 2: Regular damage (skip dead creatures, skip first-strikers who already dealt — double strikers deal again)
        for (var entry : blockerMap.entrySet()) {
            int atkIdx = entry.getKey();
            List<Integer> blkIndices = entry.getValue();
            if (deadAttackerIndices.contains(atkIdx)) continue;

            Permanent atk = atkBf.get(atkIdx);
            boolean atkSkipPhase2 = gameHelper.hasKeyword(gameData, atk, Keyword.FIRST_STRIKE)
                    && !gameHelper.hasKeyword(gameData, atk, Keyword.DOUBLE_STRIKE);

            if (blkIndices.isEmpty()) {
                // Unblocked regular attacker deals damage to player (or redirect target)
                if (!atkSkipPhase2 && !gameHelper.isPreventedFromDealingDamage(gameData, atk)) {
                    int power = gameHelper.getEffectivePower(gameData, atk);
                    if (redirectTarget != null) {
                        damageRedirectedToGuard += power;
                    } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getCard().getColor())) {
                        damageToDefendingPlayer += power;
                    }
                    combatDamageDealt.merge(atk, power, Integer::sum);
                    combatDamageDealtToPlayer.merge(atk, power, Integer::sum);
                }
            } else {
                // Attacker deals damage to surviving blockers (skip first-strike-only, allow double strike)
                if (!atkSkipPhase2 && !gameHelper.isPreventedFromDealingDamage(gameData, atk)) {
                    int remaining = gameHelper.getEffectivePower(gameData, atk);
                    for (int blkIdx : blkIndices) {
                        if (deadDefenderIndices.contains(blkIdx)) continue;
                        Permanent blk = defBf.get(blkIdx);
                        int remainingToughness = gameHelper.getEffectiveToughness(gameData, blk) - defDamageTaken.getOrDefault(blkIdx, 0);
                        int dmg = Math.min(remaining, remainingToughness);
                        if (!gameHelper.hasProtectionFrom(gameData, blk, atk.getCard().getColor())) {
                            defDamageTaken.merge(blkIdx, dmg, Integer::sum);
                            combatDamageDealt.merge(atk, dmg, Integer::sum);
                        }
                        remaining -= dmg;
                    }
                }
                // Surviving blockers deal damage to attacker (skip first-strike-only, allow double strike)
                for (int blkIdx : blkIndices) {
                    if (deadDefenderIndices.contains(blkIdx)) continue;
                    Permanent blk = defBf.get(blkIdx);
                    boolean blkSkipPhase2 = gameHelper.hasKeyword(gameData, blk, Keyword.FIRST_STRIKE)
                            && !gameHelper.hasKeyword(gameData, blk, Keyword.DOUBLE_STRIKE);
                    if (!blkSkipPhase2 && !gameHelper.isPreventedFromDealingDamage(gameData, blk)
                            && !gameHelper.hasProtectionFrom(gameData, atk, blk.getCard().getColor())) {
                        atkDamageTaken.merge(atkIdx, gameHelper.getEffectivePower(gameData, blk), Integer::sum);
                        combatDamageDealt.merge(blk, gameHelper.getEffectivePower(gameData, blk), Integer::sum);
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
            if (dmg >= gameHelper.getEffectiveToughness(gameData, atkBf.get(atkIdx))
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
                if (dmg >= gameHelper.getEffectiveToughness(gameData, defBf.get(blkIdx))
                        && !gameHelper.tryRegenerate(gameData, defBf.get(blkIdx))) {
                    deadDefenderIndices.add(blkIdx);
                }
            }
        }

        // Apply redirected damage to guard creature (e.g. Kjeldoran Royal Guard)
        if (redirectTarget != null && damageRedirectedToGuard > 0) {
            damageRedirectedToGuard = gameHelper.applyCreaturePreventionShield(gameData, redirectTarget, damageRedirectedToGuard);
            String redirectLog = redirectTarget.getCard().getName() + " absorbs " + damageRedirectedToGuard + " redirected combat damage.";
            gameHelper.logAndBroadcast(gameData, redirectLog);

            if (damageRedirectedToGuard >= gameHelper.getEffectiveToughness(gameData, redirectTarget)
                    && !gameHelper.tryRegenerate(gameData, redirectTarget)) {
                gameHelper.removePermanentToGraveyard(gameData, redirectTarget);
                String deathLog = redirectTarget.getCard().getName() + " is destroyed by redirected combat damage.";
                gameHelper.logAndBroadcast(gameData, deathLog);
            }
        }

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
            gameData.playerGraveyards.get(atkGraveyardOwner).add(dead.getOriginalCard());
            gameHelper.collectDeathTrigger(gameData, dead.getCard(), activeId, true);
            atkBf.remove(idx);
        }
        for (int idx : deadDefenderIndices) {
            Permanent dead = defBf.get(idx);
            deadCreatureNames.add(gameData.playerIdToName.get(defenderId) + "'s " + dead.getCard().getName());
            // Stolen creatures go to their owner's graveyard
            UUID defGraveyardOwner = gameData.stolenCreatures.getOrDefault(dead.getId(), defenderId);
            gameData.stolenCreatures.remove(dead.getId());
            gameData.playerGraveyards.get(defGraveyardOwner).add(dead.getOriginalCard());
            gameHelper.collectDeathTrigger(gameData, dead.getCard(), defenderId, true);
            defBf.remove(idx);
        }
        if (!deadAttackerIndices.isEmpty() || !deadDefenderIndices.isEmpty()) {
            gameHelper.removeOrphanedAuras(gameData);
        }

        gameHelper.removeOrphanedAuras(gameData);

        // Apply life loss (with prevention shield and Pariah redirect)
        damageToDefendingPlayer = gameHelper.applyPlayerPreventionShield(gameData, defenderId, damageToDefendingPlayer);
        damageToDefendingPlayer = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, defenderId, damageToDefendingPlayer, "combat");
        if (damageToDefendingPlayer > 0) {
            int currentLife = gameData.playerLifeTotals.getOrDefault(defenderId, 20);
            gameData.playerLifeTotals.put(defenderId, currentLife - damageToDefendingPlayer);

            String logEntry = gameData.playerIdToName.get(defenderId) + " takes " + damageToDefendingPlayer + " combat damage.";
            gameHelper.logAndBroadcast(gameData, logEntry);
        }

        if (!deadCreatureNames.isEmpty()) {
            String logEntry = String.join(", ", deadCreatureNames) + " died in combat.";
            gameHelper.logAndBroadcast(gameData, logEntry);
        }



        if (!deadAttackerIndices.isEmpty() || !deadDefenderIndices.isEmpty()) {

        }

        log.info("Game {} - Combat damage resolved: {} damage to defender, {} creatures died",
                gameData.id, damageToDefendingPlayer, deadAttackerIndices.size() + deadDefenderIndices.size());

        // Check win condition
        if (gameHelper.checkWinCondition(gameData)) {
            return;
        }

        // Process combat damage to player triggers (e.g. Cephalid Constable) after all combat is resolved
        processCombatDamageToPlayerTriggers(gameData, combatDamageDealtToPlayer, activeId, defenderId);
        if (gameData.awaitingInput != null) {
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            gameHelper.processNextMayAbility(gameData);
            return;
        }

        callback.advanceStep(gameData);
        callback.resolveAutoPass(gameData);
    }

    private void processGainLifeEqualToDamageDealt(GameData gameData, Map<Permanent, Integer> combatDamageDealt) {
        for (var entry : combatDamageDealt.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            for (UUID playerId : gameData.orderedPlayerIds) {
                for (Permanent perm : gameData.playerBattlefields.get(playerId)) {
                    if (perm.getAttachedTo() != null && perm.getAttachedTo().equals(creature.getId())) {
                        for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                            if (effect instanceof GainLifeEqualToDamageDealtEffect) {
                                int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
                                gameData.playerLifeTotals.put(playerId, currentLife + damageDealt);
                                String logEntry = gameData.playerIdToName.get(playerId) + " gains " + damageDealt + " life from " + perm.getCard().getName() + ".";
                                gameHelper.logAndBroadcast(gameData, logEntry);
                            }
                        }
                    }
                }
            }
        }
    }

    private void processCombatDamageToPlayerTriggers(GameData gameData, Map<Permanent, Integer> combatDamageDealtToPlayer, UUID attackerId, UUID defenderId) {
        for (var entry : combatDamageDealtToPlayer.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            for (CardEffect effect : creature.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)) {
                if (effect instanceof ExileTopCardsRepeatOnDuplicateEffect exileEffect) {
                    gameHelper.resolveExileTopCardsRepeatOnDuplicate(gameData, creature, defenderId, exileEffect);
                } else if (effect instanceof ReturnPermanentsOnCombatDamageToPlayerEffect) {
                    // Collect valid permanents the damaged player controls
                    List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
                    List<UUID> validIds = new ArrayList<>();
                    for (Permanent perm : defenderBattlefield) {
                        validIds.add(perm.getId());
                    }

                    if (validIds.isEmpty()) {
                        String logEntry = creature.getCard().getName() + "'s ability triggers, but " + gameData.playerIdToName.get(defenderId) + " has no permanents.";
                        gameHelper.logAndBroadcast(gameData, logEntry);
                        continue;
                    }

                    String logEntry = creature.getCard().getName() + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " may return up to " + damageDealt + " permanent" + (damageDealt > 1 ? "s" : "") + ".";
                    gameHelper.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} combat damage trigger: {} damage, {} valid targets", gameData.id, creature.getCard().getName(), damageDealt, validIds.size());

                    gameData.pendingCombatDamageBounceTargetPlayerId = defenderId;
                    int maxCount = Math.min(damageDealt, validIds.size());
                    gameHelper.beginMultiPermanentChoice(gameData, attackerId, validIds, maxCount, "Return up to " + damageDealt + " permanent" + (damageDealt > 1 ? "s" : "") + " to their owner's hand.");
                    return;
                }
            }
        }
    }

    // ===== Combat state management =====

    void clearCombatState(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                battlefield.forEach(Permanent::clearCombatState);
            }
        }

    }

    void processEndOfCombatSacrifices(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                List<Permanent> toSacrifice = battlefield.stream()
                        .filter(p -> gameData.permanentsToSacrificeAtEndOfCombat.contains(p.getId()))
                        .toList();
                for (Permanent perm : toSacrifice) {
                    boolean wasCreature = gameHelper.isCreature(gameData, perm);
                    battlefield.remove(perm);
                    gameData.playerGraveyards.get(playerId).add(perm.getOriginalCard());
                    gameHelper.collectDeathTrigger(gameData, perm.getCard(), playerId, wasCreature);
                    String logEntry = perm.getCard().getName() + " is sacrificed.";
                    gameData.gameLog.add(logEntry);
                    log.info("Game {} - {} sacrificed at end of combat", gameData.id, perm.getCard().getName());
                }
            }
        }
        gameData.permanentsToSacrificeAtEndOfCombat.clear();
        gameHelper.removeOrphanedAuras(gameData);


    }
}

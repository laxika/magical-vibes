package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderPoisonedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;

/**
 * Handles declare-attackers step: computing legal attackers, enforcing attack requirements
 * (CR 508.1d), attack tax payment, tapping, and collecting ON_ATTACK triggers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatAttackService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final TriggerCollectionService triggerCollectionService;
    private final CombatTriggerService combatTriggerService;

    /**
     * Returns the battlefield indices of creatures the given player can legally declare as attackers.
     */
    public List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        UUID defenderId = gameQueryService.getOpponentId(gameData, playerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (canCreatureAttack(gameData, p, defenderId, defenderBattlefield)) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Returns the subset of attackable indices whose creatures have at least one
     * "attacks each combat if able" requirement. Returns empty if an attack tax is in effect.
     */
    public List<Integer> getMustAttackIndices(GameData gameData, UUID playerId, List<Integer> attackableIndices) {
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return List.of();
        }
        if (!gameBroadcastService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
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
     * Initiates the declare-attackers step. Sends available attackers to the active player.
     * If no creatures can attack, the step is skipped.
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
        sessionManager.sendToPlayer(CombatHelper.getEffectiveRecipient(gameData, activeId),
                new AvailableAttackersMessage(attackable, mustAttack));
    }

    /**
     * Validates and processes a player's attacker declaration.
     */
    public CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
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
            payGenericMana(pool, totalTax);
        }

        // Check Phyrexian attack tax (e.g. Norn's Annex — {W/P} per attacker)
        List<ManaColor> phyrexianPayments = gameBroadcastService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId);
        if (!phyrexianPayments.isEmpty()) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            int lifeCost = 0;
            for (int i = 0; i < attackerIndices.size(); i++) {
                for (ManaColor color : phyrexianPayments) {
                    if (pool.get(color) > 0) {
                        pool.remove(color);
                    } else {
                        lifeCost += 2;
                    }
                }
            }
            if (lifeCost > 0) {
                int currentLife = gameData.playerLifeTotals.get(playerId);
                gameData.playerLifeTotals.put(playerId, currentLife - lifeCost);
            }
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
            combatTriggerService.checkAuraTriggersForCreature(gameData, attacker, EffectSlot.ON_ATTACK);
        }

        // Engine-level battle cry triggers (keyword-driven, no manual card wiring needed)
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.BATTLE_CRY)) {
                List<CardEffect> battleCryEffects = List.of(new BoostAllOwnCreaturesEffect(1, 0,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        ))
                ));
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        attacker.getCard(),
                        playerId,
                        attacker.getCard().getName() + "'s attack trigger",
                        battleCryEffects,
                        null,
                        attacker.getId()
                ));
                String triggerLog = attacker.getCard().getName() + "'s battle cry triggers.";
                gameData.gameLog.add(triggerLog);
                log.info("Game {} - {} battle cry trigger pushed onto stack", gameData.id, attacker.getCard().getName());
            }
        }

        // APNAP: active player's triggers on bottom, non-active player's on top (resolves first)
        combatTriggerService.reorderTriggersAPNAP(gameData, stackSizeBeforeAttackTriggers, playerId);

        log.info("Game {} - {} declares {} attackers", gameData.id, player.getUsername(), attackerIndices.size());
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            int p = gameQueryService.getEffectivePower(gameData, attacker);
            int t = gameQueryService.getEffectiveToughness(gameData, attacker);
            List<String> kws = new ArrayList<>();
            for (Keyword kw : List.of(Keyword.TRAMPLE, Keyword.FIRST_STRIKE, Keyword.DOUBLE_STRIKE,
                    Keyword.DEATHTOUCH, Keyword.LIFELINK, Keyword.FLYING, Keyword.VIGILANCE, Keyword.MENACE,
                    Keyword.INDESTRUCTIBLE, Keyword.INFECT)) {
                if (gameQueryService.hasKeyword(gameData, attacker, kw)) kws.add(kw.name().toLowerCase());
            }
            log.info("Game {} -   Attacker [{}]: {} {}/{}{}", gameData.id, idx,
                    attacker.getCard().getName(), p, t, kws.isEmpty() ? "" : " (" + String.join(", ", kws) + ")");
        }

        return CombatResult.AUTO_PASS_ONLY;
    }

    /**
     * Returns the battlefield indices of creatures currently declared as attackers.
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

    // ===== Private helpers =====

    private boolean canCreatureAttack(GameData gameData, Permanent creature,
                                       UUID defenderId, List<Permanent> defenderBattlefield) {
        if (!gameQueryService.isCreature(gameData, creature)) return false;
        if (creature.isTapped()) return false;
        if (creature.isSummoningSick() && !gameQueryService.hasKeyword(gameData, creature, Keyword.HASTE)) return false;
        if (gameQueryService.hasKeyword(gameData, creature, Keyword.DEFENDER)
                && !gameQueryService.canAttackDespiteDefender(gameData, creature)) return false;
        if (gameQueryService.hasAuraWithEffect(gameData, creature, EnchantedCreatureCantAttackOrBlockEffect.class)) return false;
        if (gameQueryService.hasAuraWithEffect(gameData, creature, EnchantedCreatureCantAttackEffect.class)) return false;
        if (CombatHelper.isCantAttackOrBlockUnlessEquipped(gameQueryService, gameData, creature)) return false;
        if (isCantAttackDueToLandRestriction(gameData, creature, defenderBattlefield)) return false;
        if (isCantAttackUnlessBattlefieldCount(gameData, creature)) return false;
        if (isCantAttackUnlessDefenderPoisoned(gameData, creature, defenderId)) return false;
        return true;
    }

    private boolean isCantAttackUnlessDefenderPoisoned(GameData gameData, Permanent creature, UUID defenderId) {
        boolean hasRestriction = creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackUnlessDefenderPoisonedEffect.class::isInstance);
        if (!hasRestriction) return false;
        int poison = gameData.playerPoisonCounters.getOrDefault(defenderId, 0);
        return poison <= 0;
    }

    private boolean isCantAttackDueToLandRestriction(GameData gameData, Permanent attacker,
                                                      List<Permanent> defenderBattlefield) {
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

    private boolean isCantAttackUnlessBattlefieldCount(GameData gameData, Permanent attacker) {
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect restriction) {
                int[] count = {0};
                gameData.forEachBattlefield((playerId, battlefield) ->
                        count[0] += (int) battlefield.stream()
                                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, restriction.permanentPredicate()))
                                .count()
                );
                if (count[0] < restriction.minimumCount()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getMustAttackRequirementCount(GameData gameData, Permanent creature) {
        int[] count = {(int) creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(MustAttackEffect.class::isInstance)
                .count()};

        // Check for transient "must attack this turn" flag (e.g. Alluring Siren)
        if (creature.isMustAttackThisTurn()) {
            count[0]++;
        }

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

    private void validateMaximumAttackRequirements(GameData gameData, UUID playerId,
                                                    List<Integer> attackableIndices,
                                                    Set<Integer> declaredAttackerIndices) {
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return;
        }
        if (!gameBroadcastService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
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

    private void payGenericMana(ManaPool pool, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            ManaColor highestColor = null;
            int highestCount = 0;
            for (ManaColor color : ManaColor.values()) {
                int count = pool.get(color);
                if (count > highestCount) {
                    highestCount = count;
                    highestColor = color;
                }
            }
            if (highestColor != null) {
                pool.remove(highestColor);
                remaining--;
            } else {
                break;
            }
        }
    }
}

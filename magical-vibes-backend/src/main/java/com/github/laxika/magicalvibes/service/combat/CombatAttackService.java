package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttacksAloneConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.HasNontokenSubtypeAttackerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MinimumAttackersConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessControllerControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderPoisonedEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessOpponentDealtDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantAttackIfCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AttackTarget;
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
        if (isPlayerPreventedFromAttacking(gameData, playerId)) return List.of();
        UUID defenderId = gameQueryService.getOpponentId(gameData, playerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (canCreatureAttack(gameData, p, defenderId, defenderBattlefield)) {
                indices.add(i);
            }
        }
        // CR 508.1b: if only one creature can attack and it has "can't attack alone", remove it
        if (indices.size() == 1) {
            Permanent sole = battlefield.get(indices.getFirst());
            if (hasCantAttackOrBlockAlone(sole)) {
                return List.of();
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
        List<AttackTarget> availableTargets = buildAvailableTargets(gameData, activeId);
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, activeId);
        boolean mustAttackWithAtLeastOne = isOpponentForcedToAttack(gameData, activeId);
        gameData.interaction.beginAttackerDeclaration(activeId);
        sessionManager.sendToPlayer(CombatHelper.getEffectiveRecipient(gameData, activeId),
                new AvailableAttackersMessage(attackable, mustAttack, availableTargets, taxPerCreature, mustAttackWithAtLeastOne));
    }

    /**
     * Validates and processes a player's attacker declaration.
     */
    public CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
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

        // CR 508.1b: validate "can't attack alone" — if any declared attacker has this restriction,
        // there must be at least 2 total attackers
        validateCantAttackAlone(battlefield, attackerIndices);

        // Validate attack requirements (CR 508.1d: satisfy as many as possible)
        validateMaximumAttackRequirements(gameData, playerId, attackable, uniqueIndices);

        // Validate "must attack with at least one creature" (e.g. Trove of Temptation)
        if (attackerIndices.isEmpty() && !attackable.isEmpty() && isOpponentForcedToAttack(gameData, playerId)) {
            throw new IllegalStateException("Must attack with at least one creature");
        }

        // Empty declaration is always valid — no tax or target validation needed
        if (attackerIndices.isEmpty()) {
            gameData.interaction.clearAwaitingInput();
            log.info("Game {} - {} declares no attackers", gameData.id, player.getUsername());
            gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + " declares no attackers.");
            return CombatResult.AUTO_PASS_ONLY;
        }

        // Validate attack tax (e.g. Windborn Muse / Ghostly Prison)
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            int totalTax = taxPerCreature * attackerIndices.size();
            ManaPool pool = gameData.playerManaPools.get(playerId);
            if (pool.getTotal() < totalTax) {
                throw new IllegalStateException("Not enough mana to pay attack tax (" + totalTax + " required)");
            }
        }

        // Validate attack targets
        UUID defenderId = gameQueryService.getOpponentId(gameData, playerId);
        Set<UUID> validTargetIds = buildValidAttackTargetIds(gameData, playerId);
        Map<Integer, UUID> resolvedTargets = new HashMap<>();
        for (int idx : attackerIndices) {
            UUID targetId = attackTargets != null ? attackTargets.get(idx) : null;
            if (targetId == null) {
                targetId = defenderId;
            }
            if (!validTargetIds.contains(targetId)) {
                throw new IllegalStateException("Invalid attack target for attacker at index " + idx);
            }
            // Validate must-attack-target constraints (e.g. Alluring Siren forces attack on specific player)
            Permanent attacker = battlefield.get(idx);
            if (attacker.getMustAttackTargetId() != null && !attacker.getMustAttackTargetId().equals(targetId)) {
                throw new IllegalStateException(attacker.getCard().getName() + " must attack the specified player");
            }
            resolvedTargets.put(idx, targetId);
        }

        // --- All validation passed — commit state changes ---
        gameData.interaction.clearAwaitingInput();

        // Pay attack tax
        if (taxPerCreature > 0) {
            payGenericMana(gameData.playerManaPools.get(playerId), taxPerCreature * attackerIndices.size());
        }

        // Pay Phyrexian attack tax (e.g. Norn's Annex — {W/P} per attacker)
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

        // Track that this player declared attackers this turn (for Angelic Arbiter etc.)
        gameData.playersDeclaredAttackersThisTurn.add(playerId);

        // Mark creatures as attacking and tap them (vigilance skips tapping)
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            attacker.setAttacking(true);
            attacker.setAttackTarget(resolvedTargets.get(idx));
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
                List<CardEffect> allEffects = new ArrayList<>(attacker.getCard().getEffects(EffectSlot.ON_ATTACK));

                // Filter out AttacksAloneConditionalEffect when not attacking alone (CR 506.5)
                if (attackerIndices.size() != 1) {
                    allEffects.removeIf(e -> e instanceof AttacksAloneConditionalEffect);
                }

                // Filter out ControlsSubtypeConditionalEffect when condition not met (intervening-if, CR 603.4)
                allEffects.removeIf(e -> {
                    if (e instanceof ControlsSubtypeConditionalEffect csc) {
                        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
                        return bf == null || bf.stream().noneMatch(
                                p -> p.getCard().getSubtypes().contains(csc.subtype()));
                    }
                    return false;
                });

                // Filter out ControlsAnotherSubtypeConditionalEffect when condition not met (intervening-if, CR 603.4)
                allEffects.removeIf(e -> {
                    if (e instanceof ControlsAnotherSubtypeConditionalEffect cas) {
                        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
                        if (bf == null) return true;
                        return bf.stream().noneMatch(
                                p -> !p.getId().equals(attacker.getId())
                                        && (!cas.nontokenOnly() || !p.getCard().isToken())
                                        && !Collections.disjoint(p.getCard().getSubtypes(), cas.subtypes()));
                    }
                    return false;
                });

                if (!allEffects.isEmpty()) {
                    // Separate non-targeting "you may" effects (e.g. Primeval Titan's may-search) from
                    // effects that need the normal resolution path (mandatory effects and targeting may effects
                    // like Cyclops Gladiator's may-fight).
                    List<CardEffect> nonTargetingMayEffects = allEffects.stream()
                            .filter(e -> e instanceof com.github.laxika.magicalvibes.model.effect.MayEffect
                                    && !e.canTargetPermanent() && !e.canTargetPlayer()).toList();
                    List<CardEffect> otherEffects = allEffects.stream()
                            .filter(e -> !nonTargetingMayEffects.contains(e)).toList();

                    // Queue non-targeting may effects as pending may abilities
                    for (CardEffect effect : nonTargetingMayEffects) {
                        com.github.laxika.magicalvibes.model.effect.MayEffect may =
                                (com.github.laxika.magicalvibes.model.effect.MayEffect) effect;
                        gameData.queueMayAbility(attacker.getCard(), playerId, may, null, attacker.getId());
                    }

                    if (!otherEffects.isEmpty()) {
                        boolean needsTarget = otherEffects.stream()
                                .anyMatch(e -> e.canTargetPermanent() || e.canTargetPlayer());
                        if (needsTarget) {
                            gameData.pendingAttackTriggerTargets.add(
                                    new PermanentChoiceContext.AttackTriggerTarget(
                                            attacker.getCard(), playerId, otherEffects, attacker.getId()));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    attacker.getCard(),
                                    playerId,
                                    attacker.getCard().getName() + "'s attack trigger",
                                    otherEffects,
                                    null,
                                    attacker.getId()
                            ));
                        }
                    }

                    String triggerLog = attacker.getCard().getName() + "'s attack ability triggers.";
                    gameData.gameLog.add(triggerLog);
                    log.info("Game {} - {} attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                }
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

        // Check for "whenever one or more creatures you control attack" triggers (ON_ALLY_CREATURES_ATTACK)
        // These fire once per combat (not per creature) when at least one creature attacks.
        // The attacker count is locked at trigger time via xValue (per MTG rules: creatures
        // removed before resolution still count, tokens entering attacking after don't).
        for (Permanent perm : battlefield) {
            List<CardEffect> allyAttackEffects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURES_ATTACK);
            if (allyAttackEffects.isEmpty()) continue;

            // Pre-filter HasNontokenSubtypeAttackerConditionalEffect — skip if no
            // nontoken creature of the required subtype is among the attackers.
            List<CardEffect> filteredEffects = new ArrayList<>();
            for (CardEffect effect : allyAttackEffects) {
                if (effect instanceof HasNontokenSubtypeAttackerConditionalEffect hnsac) {
                    boolean hasMatch = battlefield.stream()
                            .filter(Permanent::isAttacking)
                            .filter(p -> !p.getCard().isToken())
                            .anyMatch(p -> GameQueryService.permanentHasSubtype(p, hnsac.requiredSubtype()));
                    if (!hasMatch) {
                        log.info("Game {} - {} attack trigger skipped (no nontoken {} among attackers)",
                                gameData.id, perm.getCard().getName(),
                                hnsac.requiredSubtype().name().toLowerCase());
                        continue;
                    }
                }
                filteredEffects.add(effect);
            }
            if (filteredEffects.isEmpty()) continue;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    playerId,
                    perm.getCard().getName() + "'s attack trigger",
                    filteredEffects,
                    attackerIndices.size(),
                    null,
                    perm.getId(),
                    null,
                    null,
                    null,
                    null
            ));
            String triggerLog = perm.getCard().getName() + "'s attack ability triggers.";
            gameData.gameLog.add(triggerLog);
            log.info("Game {} - {} ON_ALLY_CREATURES_ATTACK trigger pushed onto stack (attacker count: {})",
                    gameData.id, perm.getCard().getName(), attackerIndices.size());
        }

        // Check for "whenever a creature you control attacks" triggers (ON_ALLY_CREATURE_ATTACKS)
        // These fire once per attacking creature (not once per combat like ON_ALLY_CREATURES_ATTACK).
        // Supports SubtypeConditionalEffect to filter by the attacking creature's subtype.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (Permanent perm : battlefield) {
                List<CardEffect> perCreatureAttackEffects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ATTACKS);
                if (perCreatureAttackEffects.isEmpty()) continue;

                List<CardEffect> matchingEffects = new ArrayList<>();
                for (CardEffect effect : perCreatureAttackEffects) {
                    if (effect instanceof SubtypeConditionalEffect conditional) {
                        if (!GameQueryService.permanentHasSubtype(attacker, conditional.subtype())) {
                            continue;
                        }
                        matchingEffects.add(conditional.wrapped());
                    } else {
                        matchingEffects.add(effect);
                    }
                }
                if (matchingEffects.isEmpty()) continue;

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s attack trigger",
                        matchingEffects,
                        null,
                        perm.getId()
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameData.gameLog.add(triggerLog);
                log.info("Game {} - {} ON_ALLY_CREATURE_ATTACKS trigger for {} attacking",
                        gameData.id, perm.getCard().getName(), attacker.getCard().getName());
            }
        }

        // Check for graveyard-based "whenever you attack with N or more creatures" triggers
        // (GRAVEYARD_ON_ALLY_CREATURES_ATTACK). These fire from the controller's graveyard.
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> gyAttackEffects = card.getEffects(EffectSlot.GRAVEYARD_ON_ALLY_CREATURES_ATTACK);
                if (gyAttackEffects.isEmpty()) continue;

                for (CardEffect effect : gyAttackEffects) {
                    CardEffect innerEffect = effect;

                    // Unwrap MinimumAttackersConditionalEffect — check minimum before offering the trigger
                    if (innerEffect instanceof MinimumAttackersConditionalEffect mac) {
                        if (attackerIndices.size() < mac.minimumAttackers()) {
                            log.info("Game {} - {} graveyard attack trigger skipped ({} attackers, need {})",
                                    gameData.id, card.getName(), attackerIndices.size(), mac.minimumAttackers());
                            continue;
                        }
                        innerEffect = mac.wrapped();
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            playerId,
                            card.getName() + "'s graveyard attack trigger",
                            new ArrayList<>(List.of(innerEffect)),
                            attackerIndices.size(),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    ));
                    String triggerLog = card.getName() + "'s graveyard attack ability triggers.";
                    gameData.gameLog.add(triggerLog);
                    log.info("Game {} - {} GRAVEYARD_ON_ALLY_CREATURES_ATTACK trigger pushed onto stack (attacker count: {})",
                            gameData.id, card.getName(), attackerIndices.size());
                }
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
        if (isCantAttackUnlessControllerControls(gameData, creature)) return false;
        if (isCantAttackUnlessDefenderPoisoned(gameData, creature, defenderId)) return false;
        if (isCantAttackUnlessOpponentDealtDamage(gameData, creature)) return false;
        if (isCantAttackDueToGlobalRestriction(gameData, creature)) return false;
        return true;
    }

    private boolean isCantAttackUnlessDefenderPoisoned(GameData gameData, Permanent creature, UUID defenderId) {
        boolean hasRestriction = creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackUnlessDefenderPoisonedEffect.class::isInstance);
        if (!hasRestriction) return false;
        int poison = gameData.playerPoisonCounters.getOrDefault(defenderId, 0);
        return poison <= 0;
    }

    private boolean isCantAttackUnlessOpponentDealtDamage(GameData gameData, Permanent creature) {
        boolean hasRestriction = creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackUnlessOpponentDealtDamageThisTurnEffect.class::isInstance);
        if (!hasRestriction) return false;
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        // Check if any opponent of the creature's controller has been dealt damage this turn
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId) && gameData.playersDealtDamageThisTurn.contains(playerId)) {
                return false;
            }
        }
        return true;
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

    private boolean isCantAttackUnlessControllerControls(GameData gameData, Permanent creature) {
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantAttackUnlessControllerControlsMatchingPermanentEffect restriction) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(controllerId);
                boolean controllerMatches = controllerBattlefield != null && controllerBattlefield.stream()
                        .anyMatch(p -> gameQueryService.matchesPermanentPredicate(gameData, p, restriction.controllerPermanentPredicate()));
                if (!controllerMatches) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCantAttackDueToGlobalRestriction(GameData gameData, Permanent creature) {
        boolean[] restricted = {false};
        gameData.forEachPermanent((playerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CreaturesCantAttackUnlessPredicateEffect restriction) {
                    if (!gameQueryService.matchesPermanentPredicate(gameData, creature, restriction.exemptionPredicate())) {
                        restricted[0] = true;
                    }
                }
            }
        });
        return restricted[0];
    }

    private int getMustAttackRequirementCount(GameData gameData, Permanent creature) {
        int[] count = {(int) creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(MustAttackEffect.class::isInstance)
                .count()};

        // Check for transient "must attack this turn" flag (e.g. Alluring Siren)
        if (creature.isMustAttackThisTurn()) {
            count[0]++;
        }

        UUID creatureControllerId = CombatHelper.findControllerOf(gameData, creature);

        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttached()
                    && permanent.getAttachedTo().equals(creature.getId())) {
                count[0] += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(MustAttackEffect.class::isInstance)
                        .count();
            }
            // Check for curses on the creature's controller (e.g. Curse of the Nightly Hunt)
            if (permanent.isAttached()
                    && permanent.getAttachedTo().equals(creatureControllerId)) {
                count[0] += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(e -> e instanceof MustAttackEffect mae
                                && mae.scope() == GrantScope.ENCHANTED_PLAYER_CREATURES)
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

    /**
     * Returns true if an opponent controls a permanent with
     * {@link OpponentsMustAttackControllerEffect}, forcing this player to attack
     * with at least one creature each combat if able. Respects attack tax exemption
     * (CR 508.1d — the player is not required to pay optional attack costs).
     */
    public boolean isOpponentForcedToAttack(GameData gameData, UUID playerId) {
        int taxPerCreature = gameBroadcastService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return false;
        }
        if (!gameBroadcastService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
            return false;
        }
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsMustAttackControllerEffect) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the player is globally prevented from attacking (e.g. Angelic Arbiter:
     * "Each opponent who cast a spell this turn can't attack with creatures").
     */
    private boolean isPlayerPreventedFromAttacking(GameData gameData, UUID playerId) {
        int spellsCast = gameData.getSpellsCastThisTurnCount(playerId);
        if (spellsCast == 0) return false;

        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentsCantAttackIfCastSpellThisTurnEffect) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Builds the list of available attack targets: the defending player plus any planeswalkers they control.
     */
    public List<AttackTarget> buildAvailableTargets(GameData gameData, UUID activePlayerId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, activePlayerId);
        List<AttackTarget> targets = new ArrayList<>();
        targets.add(new AttackTarget(defenderId.toString(), gameData.playerIdToName.get(defenderId), true));
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);
        if (defBf != null) {
            for (Permanent p : defBf) {
                if (p.getCard().hasType(CardType.PLANESWALKER)) {
                    targets.add(new AttackTarget(p.getId().toString(), p.getCard().getName(), false));
                }
            }
        }
        return targets;
    }

    /**
     * Returns the set of valid attack target UUIDs: the defending player plus their planeswalkers.
     */
    private Set<UUID> buildValidAttackTargetIds(GameData gameData, UUID activePlayerId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, activePlayerId);
        Set<UUID> validIds = new HashSet<>();
        validIds.add(defenderId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);
        if (defBf != null) {
            for (Permanent p : defBf) {
                if (p.getCard().hasType(CardType.PLANESWALKER)) {
                    validIds.add(p.getId());
                }
            }
        }
        return validIds;
    }

    private void validateCantAttackAlone(List<Permanent> battlefield, List<Integer> attackerIndices) {
        if (attackerIndices.size() == 1) {
            Permanent sole = battlefield.get(attackerIndices.getFirst());
            if (hasCantAttackOrBlockAlone(sole)) {
                throw new IllegalStateException(sole.getCard().getName() + " can't attack alone");
            }
        }
    }

    private boolean hasCantAttackOrBlockAlone(Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance);
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

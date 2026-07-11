package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
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
import com.github.laxika.magicalvibes.networking.message.AttackTarget;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
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
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final CastingCostService castingCostService;
    private final TriggerCollectionService triggerCollectionService;
    private final CombatTriggerService combatTriggerService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /**
     * Returns the battlefield indices of creatures the given player can legally declare as attackers.
     */
    public List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        if (isPlayerPreventedFromAttacking(gameData, playerId)) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (canCreatureAttack(gameData, p, playerId)) {
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
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return List.of();
        }
        if (!castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
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

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.AttackerDeclaration(activeId));
    }

    /**
     * Validates and processes a player's attacker declaration.
     */
    public CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        if (gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class) == null) {
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
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, playerId);
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
            // Taunt: a taunted player's attacking creatures must attack the taunter if able.
            UUID taunter = gameData.tauntedThisTurn.get(playerId);
            if (taunter != null && validTargetIds.contains(taunter) && !taunter.equals(targetId)) {
                throw new IllegalStateException(attacker.getCard().getName() + " must attack the taunting player");
            }
            // Defender-scoped restriction (e.g. Form of the Dragon — "Creatures without flying can't attack you"):
            // the attacked player controls a permanent that forbids attackers not matching its exemption predicate.
            if (isCantAttackDefenderDueToRestriction(gameData, attacker, targetId)) {
                throw new IllegalStateException(attacker.getCard().getName() + " can't attack that player");
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
        List<ManaColor> phyrexianPayments = castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId);
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
        gameData.creaturesAttackedCountThisTurn.merge(playerId, attackerIndices.size(), Integer::sum);

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

                // Filter out attacks-alone conditionals when not attacking alone (CR 506.5)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttacksAlone
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out controls-permanent conditionals when condition not met (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ControlsPermanent
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // Filter out controls-another-permanent conditionals when condition not met (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ControlsAnotherPermanent
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

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
                            gameData.queueInteraction(
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

            // Pre-filter attacker-group conditional effects — skip if no matching attacker exists.
            List<CardEffect> filteredEffects = new ArrayList<>();
            for (CardEffect effect : allyAttackEffects) {
                if (effect instanceof ConditionalEffect ce && ce.condition() instanceof HasAttacker) {
                    boolean hasMatch = conditionEvaluationService.isMet(gameData, ce.condition(),
                            ConditionContext.forPermanent(perm, playerId));
                    if (!hasMatch) {
                        log.info("Game {} - {} attack trigger skipped (no matching attacker)",
                                gameData.id, perm.getCard().getName());
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
        // Supports TriggeringCardConditionalEffect to filter by the attacking creature.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (Permanent perm : battlefield) {
                List<CardEffect> perCreatureAttackEffects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ATTACKS);
                if (perCreatureAttackEffects.isEmpty()) continue;

                List<CardEffect> matchingEffects = new ArrayList<>();
                for (CardEffect effect : perCreatureAttackEffects) {
                    if (effect instanceof TriggeringCardConditionalEffect conditional) {
                        if (!predicateEvaluationService.matchesCardPredicate(attacker.getCard(), conditional.predicate(), null,
                                gameData, playerId)) {
                            continue;
                        }
                        matchingEffects.add(conditional.wrapped());
                    } else {
                        matchingEffects.add(effect);
                    }
                }
                if (matchingEffects.isEmpty()) continue;

                StackEntry attackTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s attack trigger",
                        matchingEffects,
                        null,
                        perm.getId()
                );
                attackTrigger.setAttackedTargetId(attacker.getAttackTarget());
                gameData.stack.add(attackTrigger);
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

                    // Unwrap minimum-attackers conditionals — check minimum before offering the trigger
                    if (innerEffect instanceof ConditionalEffect ce
                            && ce.condition() instanceof MinimumAttackers mac) {
                        ConditionContext ctx = new ConditionContext(playerId, null, null, card,
                                false, null, attackerIndices.size(), null, null, false);
                        if (!conditionEvaluationService.isMet(gameData, mac, ctx)) {
                            log.info("Game {} - {} graveyard attack trigger skipped ({} attackers, need {})",
                                    gameData.id, card.getName(), attackerIndices.size(), mac.minimumAttackers());
                            continue;
                        }
                        innerEffect = ce.wrapped();
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

        // Check for "whenever a creature attacks you or a planeswalker you control" triggers
        // (ON_CREATURE_ATTACKS_YOU). These fire once per attacking creature, on the permanents of
        // the player being attacked (directly or via one of their planeswalkers). The attacking
        // creature is stored as a non-targeting targetId so the effect can act on it.
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            UUID attackedTargetId = resolvedTargets.get(idx);
            UUID attackedPlayerId = gameData.playerIds.contains(attackedTargetId)
                    ? attackedTargetId
                    : gameQueryService.findPermanentController(gameData, attackedTargetId);
            if (attackedPlayerId == null) continue;
            List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(attackedPlayerId);
            if (defenderBattlefield == null) continue;
            for (Permanent perm : new ArrayList<>(defenderBattlefield)) {
                List<CardEffect> attackedTriggerEffects = perm.getCard().getEffects(EffectSlot.ON_CREATURE_ATTACKS_YOU);
                if (attackedTriggerEffects.isEmpty()) continue;

                StackEntry attackedTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        attackedPlayerId,
                        perm.getCard().getName() + "'s trigger",
                        new ArrayList<>(attackedTriggerEffects),
                        attacker.getId(),
                        perm.getId()
                );
                attackedTrigger.setNonTargeting(true);
                gameData.stack.add(attackedTrigger);
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameData.gameLog.add(triggerLog);
                log.info("Game {} - {} ON_CREATURE_ATTACKS_YOU trigger for {} attacking",
                        gameData.id, perm.getCard().getName(), attacker.getCard().getName());
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


    private boolean canCreatureAttack(GameData gameData, Permanent creature, UUID controllerId) {
        if (!gameQueryService.isCreature(gameData, creature)) return false;
        if (creature.isTapped()) return false;
        if (creature.isSummoningSick() && !gameQueryService.hasKeyword(gameData, creature, Keyword.HASTE)) return false;
        if (gameQueryService.hasKeyword(gameData, creature, Keyword.DEFENDER)
                && !gameQueryService.canAttackDespiteDefender(gameData, creature)) return false;
        if (gameQueryService.hasAuraWithEffect(gameData, creature, EnchantedCreatureCantAttackOrBlockEffect.class)) return false;
        if (gameQueryService.hasAuraWithEffect(gameData, creature, EnchantedCreatureCantAttackEffect.class)) return false;
        if (CombatHelper.isCantAttackOrBlockUnlessEquipped(gameQueryService, gameData, creature)) return false;
        if (isCantAttackUnlessConditionUnmet(gameData, creature, controllerId)) return false;
        if (isCantAttackDueToGlobalRestriction(gameData, creature)) return false;
        return true;
    }

    /**
     * Evaluates the creature's {@link CantAttackUnlessEffect} restrictions (CR 508.1a): the
     * creature can't attack while any attached condition is unmet. Each restriction's condition
     * (controller controls a permanent, defending player poisoned, N Islands on the battlefield, …)
     * is routed through {@link ConditionEvaluationService} with the attacker as source.
     */
    private boolean isCantAttackUnlessConditionUnmet(GameData gameData, Permanent creature, UUID controllerId) {
        ConditionContext ctx = null;
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            Condition condition = null;
            if (effect instanceof CantAttackUnlessEffect restriction) {
                condition = restriction.condition();
            } else if (effect instanceof CantAttackOrBlockUnlessEffect restriction) {
                condition = restriction.condition();
            }
            if (condition != null) {
                if (ctx == null) {
                    ctx = ConditionContext.forPermanent(creature, controllerId);
                }
                if (!conditionEvaluationService.isMet(gameData, condition, ctx)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Defender-scoped attack restriction (CR 508.1a): the attacked player controls a permanent with a
     * {@link CreaturesCantAttackControllerUnlessPredicateEffect}, and the attacker does not match its
     * exemption predicate (e.g. Form of the Dragon's "Creatures without flying can't attack you").
     * Only players can carry this restriction, so it never applies to attacks aimed at a planeswalker.
     */
    private boolean isCantAttackDefenderDueToRestriction(GameData gameData, Permanent attacker, UUID targetId) {
        if (!gameData.playerIds.contains(targetId)) return false;
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(targetId);
        if (defenderBattlefield == null) return false;
        for (Permanent perm : defenderBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CreaturesCantAttackControllerUnlessPredicateEffect restriction
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, restriction.exemptionPredicate())) {
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
                    if (!predicateEvaluationService.matchesPermanentPredicate(gameData, creature, restriction.exemptionPredicate())) {
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

        // Taunt: every creature the affected player controls must attack the taunter if able.
        UUID taunter = gameData.tauntedThisTurn.get(creatureControllerId);
        if (taunter != null && buildValidAttackTargetIds(gameData, creatureControllerId).contains(taunter)) {
            count[0]++;
        }

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
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return;
        }
        if (!castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
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
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, playerId);
        if (taxPerCreature > 0) {
            return false;
        }
        if (!castingCostService.getPhyrexianAttackPaymentsPerCreature(gameData, playerId).isEmpty()) {
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

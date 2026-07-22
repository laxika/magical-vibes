package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.AttackCounterMoveEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AttackOrBlockRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessCountAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithPowerGreaterThanAmountCantAttackEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantAttackIfCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCanAttackAsThoughHasteEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.networking.message.AttackTarget;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
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
    private final AmountEvaluationService amountEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final CastingCostService castingCostService;
    private final TriggerCollectionService triggerCollectionService;
    private final CombatTriggerService combatTriggerService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.effect.AttackSacrificeCostService attackSacrificeCostService;
    private final GraveyardTargetingService graveyardTargetingService;

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
        return declareAttackers(gameData, player, attackerIndices, attackTargets, null);
    }

    /**
     * Validates and processes a player's attacker declaration, including any attacking bands
     * (CR 702.22): each entry of {@code bands} is the set of attacker indices grouped into one band.
     */
    public CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices,
                                         Map<Integer, UUID> attackTargets, List<List<Integer>> bands) {
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

        // Errantry: "can only attack alone" — an enchanted attacker with this restriction
        // may only be declared if it is the sole attacker
        validateCanOnlyAttackAlone(gameData, battlefield, attackerIndices);

        // Okk: "can't attack unless a creature with greater power also attacks"
        validateGreaterPowerAlsoAttacks(gameData, battlefield, attackerIndices);

        // Orcish Conscripts: "can't attack unless at least N other creatures also attack"
        validateCountAlsoAttacks(battlefield, attackerIndices);

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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(player.getUsername() + " declares no attackers."));
            return CombatResult.AUTO_PASS_ONLY;
        }

        // Validate attack tax (e.g. Windborn Muse / Ghostly Prison — uniform per-attacker tax from the
        // defender's side; plus per-attacker aura taxes scoped to a single creature, e.g. Brainwash {3})
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, playerId);
        int selfTaxTotal = 0;
        for (int idx : attackerIndices) {
            selfTaxTotal += gameQueryService.getEnchantedCreatureAttackTax(gameData, battlefield.get(idx));
        }
        int totalTax = taxPerCreature * attackerIndices.size() + selfTaxTotal;
        if (totalTax > 0) {
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

        // Validate attacking bands (CR 702.22c/d): each band needs >=1 creature with banding and
        // <=1 without, and all its members must attack the same player or planeswalker.
        List<Set<Integer>> validatedBands = validateBands(gameData, battlefield, uniqueIndices, resolvedTargets, bands);

        // --- All validation passed — commit state changes ---
        gameData.interaction.clearAwaitingInput();

        // Pay attack tax (uniform per-attacker + per-creature aura taxes)
        if (totalTax > 0) {
            payGenericMana(gameData.playerManaPools.get(playerId), totalTax);
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

        // Assign band membership (CR 702.22): every member of a band shares one band id, which
        // persists for the rest of combat even if banding is later removed (CR 702.22e).
        for (Set<Integer> band : validatedBands) {
            UUID bandId = UUID.randomUUID();
            for (int idx : band) {
                battlefield.get(idx).setBandId(bandId);
            }
        }

        String logEntry = player.getUsername() + " declares " + attackerIndices.size() +
                " attacker" + (attackerIndices.size() > 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        // Collect all attack-step triggers, then reorder per APNAP (CR 603.3b)
        int stackSizeBeforeAttackTriggers = gameData.stack.size();

        // Check for "when this creature attacks" triggers
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            List<CardEffect> nativeAttackEffects = attacker.getCard().getEffects(EffectSlot.ON_ATTACK);
            List<CardEffect> temporaryAttackEffects = attacker.getTemporaryTriggeredEffects(EffectSlot.ON_ATTACK);
            if (!nativeAttackEffects.isEmpty() || !temporaryAttackEffects.isEmpty()) {
                List<CardEffect> allEffects = new ArrayList<>(nativeAttackEffects);
                // Temporarily granted ON_ATTACK abilities (e.g. Tower Above's "target creature blocks
                // it this turn if able"). MustBlockSourceEffect's source is snapshotted to the attacker.
                for (CardEffect temp : temporaryAttackEffects) {
                    if (temp instanceof MustBlockSourceEffect) {
                        allEffects.add(new MustBlockSourceEffect(attacker.getId()));
                    } else {
                        allEffects.add(temp);
                    }
                }

                // "Whenever this creature attacks, defending player may draw a card" (Sibilant Spirit).
                // Route the optional draw to the defending player (or the controller of the attacked
                // planeswalker), not the attacking creature's controller.
                List<CardEffect> defendingPlayerDraws = allEffects.stream()
                        .filter(e -> e instanceof DefendingPlayerMayDrawCardEffect).toList();
                if (!defendingPlayerDraws.isEmpty()) {
                    allEffects.removeAll(defendingPlayerDraws);
                    UUID attackedTargetId = attacker.getAttackTarget();
                    UUID defendingPlayerId = attackedTargetId == null ? null
                            : gameData.playerIds.contains(attackedTargetId)
                                    ? attackedTargetId
                                    : gameQueryService.findPermanentController(gameData, attackedTargetId);
                    if (defendingPlayerId != null) {
                        for (CardEffect ignored : defendingPlayerDraws) {
                            gameData.queueMayAbility(attacker.getCard(), defendingPlayerId,
                                    new MayEffect(new DrawCardEffect(), "Draw a card?"));
                        }
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.builder().card(attacker.getCard()).text("'s ability triggers.").build());
                    }
                }

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

                // Filter out empty-hand conditionals when controller has cards in hand (intervening-if, CR 603.4)
                allEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof ControllerHandEmpty
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                if (!allEffects.isEmpty()) {
                    // Separate non-targeting "you may" effects (e.g. Primeval Titan's may-search) from
                    // effects that need the normal resolution path (mandatory effects and targeting may effects
                    // like Cyclops Gladiator's may-fight).
                    List<CardEffect> nonTargetingMayEffects = allEffects.stream()
                            .filter(e -> e instanceof com.github.laxika.magicalvibes.model.effect.MayEffect
                                    && !e.targetSpec().category().includesPermanents() && !e.targetSpec().category().includesPlayers()).toList();
                    List<CardEffect> otherEffects = allEffects.stream()
                            .filter(e -> !nonTargetingMayEffects.contains(e)).toList();

                    // Queue non-targeting may effects as pending may abilities
                    for (CardEffect effect : nonTargetingMayEffects) {
                        com.github.laxika.magicalvibes.model.effect.MayEffect may =
                                (com.github.laxika.magicalvibes.model.effect.MayEffect) effect;
                        gameData.queueMayAbility(attacker.getCard(), playerId, may, null, attacker.getId());
                    }

                    if (!otherEffects.isEmpty()) {
                        // Two-target "remove a counter from a creature you control, then put one on up
                        // to one creature the defending player controls" (Decimator Beetle). The normal
                        // pipeline collects only one target, so route to the bespoke two-step flow.
                        boolean isCounterMove = otherEffects.stream().anyMatch(e -> e instanceof AttackCounterMoveEffect);
                        boolean needsGraveyardTarget = otherEffects.stream()
                                .anyMatch(e -> e.targetSpec().category().isGraveyard());
                        boolean needsTarget = otherEffects.stream()
                                .anyMatch(e -> e.targetSpec().category().includesPermanents() || e.targetSpec().category().includesPlayers());
                        UUID attackedTargetId = attacker.getAttackTarget();
                        UUID defendingPlayerId = attackedTargetId == null ? null
                                : gameData.playerIds.contains(attackedTargetId)
                                        ? attackedTargetId
                                        : gameQueryService.findPermanentController(gameData, attackedTargetId);
                        if (isCounterMove) {
                            gameData.queueInteraction(
                                    new PermanentChoiceContext.AttackCounterMoveFirstTarget(
                                            attacker.getCard(), playerId, otherEffects, attacker.getId(), defendingPlayerId));
                        } else if (needsGraveyardTarget) {
                            // "exile target card from defending player's graveyard" (Graven Abomination):
                            // choose as the trigger goes on the stack (same shape as ETB/death GY exile).
                            // Handler owns its own broadcast (including the no-legal-target skip).
                            graveyardTargetingService.handleAttackGraveyardTargeting(
                                    gameData, playerId, attacker.getCard(), otherEffects,
                                    attacker.getId(), defendingPlayerId);
                        } else if (needsTarget) {
                            gameData.queueInteraction(
                                    new PermanentChoiceContext.AttackTriggerTarget(
                                            attacker.getCard(), playerId, otherEffects, attacker.getId()));
                        } else {
                            // Capture the attacked player/planeswalker so non-targeting attack
                            // triggers that act on the defending player (e.g. Nemesis of Reason's
                            // MillDefendingPlayerEffect) can read it as attackedTargetId.
                            StackEntry attackTrigger = new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    attacker.getCard(),
                                    playerId,
                                    attacker.getCard().getName() + "'s attack trigger",
                                    otherEffects,
                                    null,
                                    attacker.getId()
                            );
                            attackTrigger.setAttackedTargetId(attacker.getAttackTarget());
                            gameData.stack.add(attackTrigger);
                        }

                        if (!needsGraveyardTarget) {
                            gameBroadcastService.logAndBroadcast(gameData,
                                    GameLog.builder().card(attacker.getCard()).text("'s attack ability triggers.").build());
                            log.info("Game {} - {} attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                        }
                    } else {
                        gameBroadcastService.logAndBroadcast(gameData,
                                GameLog.builder().card(attacker.getCard()).text("'s attack ability triggers.").build());
                        log.info("Game {} - {} attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                    }
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
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.builder().card(attacker.getCard()).text("'s battle cry triggers.").build());
                log.info("Game {} - {} battle cry trigger pushed onto stack", gameData.id, attacker.getCard().getName());
            }
        }

        // Engine-level training triggers: attacks with another creature of greater power → +1/+1 counter
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.TRAINING)) {
                continue;
            }
            int selfPower = gameQueryService.getEffectivePower(gameData, attacker);
            boolean hasGreaterPowerAlly = false;
            for (int otherIdx : attackerIndices) {
                if (otherIdx == idx) {
                    continue;
                }
                Permanent other = battlefield.get(otherIdx);
                if (gameQueryService.getEffectivePower(gameData, other) > selfPower) {
                    hasGreaterPowerAlly = true;
                    break;
                }
            }
            if (!hasGreaterPowerAlly) {
                continue;
            }
            List<CardEffect> trainingEffects = List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    attacker.getCard(),
                    playerId,
                    attacker.getCard().getName() + "'s training",
                    trainingEffects,
                    null,
                    attacker.getId()
            ));
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.builder().card(attacker.getCard()).text("'s training triggers.").build());
            log.info("Game {} - {} training trigger pushed onto stack", gameData.id, attacker.getCard().getName());
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
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.builder().card(perm.getCard()).text("'s attack ability triggers.").build());
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
                    } else if (effect instanceof TriggeringPermanentConditionalEffect permConditional) {
                        // Filter by the attacking permanent itself (e.g. Rage Forger — "a creature you
                        // control with a +1/+1 counter on it attacks").
                        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, permConditional.predicate())) {
                            continue;
                        }
                        matchingEffects.add(permConditional.wrapped());
                    } else {
                        matchingEffects.add(effect);
                    }
                }

                // Filter out attacks-alone conditionals (e.g. Exalted) when the creature isn't
                // attacking alone (CR 702.83a) — the ability doesn't trigger at all, so no
                // do-nothing entry goes on the stack.
                matchingEffects.removeIf(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttacksAlone
                        && !conditionEvaluationService.isMet(gameData, ce.condition(),
                                ConditionContext.forPermanent(attacker, playerId)));

                // The surviving attacks-alone conditionals are already satisfied, so unwrap them to
                // their inner effect. This lets a wrapped "you may" (e.g. Angelic Benediction's
                // "you may tap target creature") route through the may/mandatory split below.
                matchingEffects.replaceAll(e -> e instanceof ConditionalEffect ce
                        && ce.condition() instanceof AttacksAlone ? ce.wrapped() : e);

                if (matchingEffects.isEmpty()) continue;

                // Optional ("you may") per-creature attack triggers go on the stack as CR 603.5
                // resolution-time may abilities: the source permanent is the *attacking* creature
                // ("that creature", the damage source), while the source card is the ability's owner
                // whose target filter governs legal targets (e.g. Rage Forger's ping to a
                // player/planeswalker). Mandatory effects keep the existing direct-stack path where
                // the ability's owner is the source and the attacked target is captured for effects
                // like Hellrider's DealDamageToAttackedTargetEffect.
                List<CardEffect> mayEffects = matchingEffects.stream()
                        .filter(e -> e instanceof MayEffect).toList();
                List<CardEffect> mandatoryEffects = matchingEffects.stream()
                        .filter(e -> !(e instanceof MayEffect)).toList();

                for (CardEffect effect : mayEffects) {
                    gameData.queueMayAbility(perm.getCard(), playerId, (MayEffect) effect, null, attacker.getId());
                }

                if (!mandatoryEffects.isEmpty()) {
                    StackEntry attackTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s attack trigger",
                            mandatoryEffects,
                            null,
                            perm.getId()
                    );
                    attackTrigger.setAttackedTargetId(attacker.getAttackTarget());
                    // Record the triggering attacker as a non-targeting reference so effects that
                    // act on "that creature" (e.g. Shared Animosity's +1/+0 boost) can find it.
                    // Non-targeting so this never fizzles triggers that ignore it (e.g. Hellrider).
                    attackTrigger.setTargetId(attacker.getId());
                    attackTrigger.setNonTargeting(true);
                    gameData.stack.add(attackTrigger);
                }

                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
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
                                false, false, null, attackerIndices.size(), null, null, false);
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
                    gameBroadcastService.logAndBroadcast(gameData,
                            GameLog.builder().card(card).text("'s graveyard attack ability triggers.").build());
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
                List<CardEffect> attackedTriggerEffects = new ArrayList<>();
                for (CardEffect attackedEffect : perm.getCard().getEffects(EffectSlot.ON_CREATURE_ATTACKS_YOU)) {
                    // Some triggers only fire for attackers matching a condition (e.g. Raking Canopy:
                    // "a creature with flying"). The condition is checked here at declaration time.
                    if (attackedEffect instanceof DealDamageToTriggeringAttackerEffect damageEffect
                            && !predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, attacker, damageEffect.attackerCondition())) {
                        continue;
                    }
                    attackedTriggerEffects.add(attackedEffect);
                }
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
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                log.info("Game {} - {} ON_CREATURE_ATTACKS_YOU trigger for {} attacking",
                        gameData.id, perm.getCard().getName(), attacker.getCard().getName());
            }
        }

        // Check for "whenever a creature attacks" triggers (ON_ANY_CREATURE_ATTACKS). These fire once
        // per attacking creature, on every permanent with this slot across all battlefields, regardless
        // of who controls the attacker or whom it attacks (e.g. Caltrops pings every attacker). The
        // attacking creature is stored as a non-targeting targetId so the effect can act on "it".
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            for (Map.Entry<UUID, List<Permanent>> bf : gameData.playerBattlefields.entrySet()) {
                UUID permController = bf.getKey();
                for (Permanent perm : new ArrayList<>(bf.getValue())) {
                    List<CardEffect> anyAttackEffects = perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_ATTACKS);
                    if (anyAttackEffects.isEmpty()) continue;

                    StackEntry anyAttackTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            permController,
                            perm.getCard().getName() + "'s trigger",
                            new ArrayList<>(anyAttackEffects),
                            attacker.getId(),
                            perm.getId()
                    );
                    anyAttackTrigger.setNonTargeting(true);
                    gameData.stack.add(anyAttackTrigger);
                    gameBroadcastService.logAndBroadcast(gameData,
                            GameLog.builder().card(perm.getCard()).text("'s ability triggers.").build());
                    log.info("Game {} - {} ON_ANY_CREATURE_ATTACKS trigger for {} attacking",
                            gameData.id, perm.getCard().getName(), attacker.getCard().getName());
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

        // Pay "can't attack unless you sacrifice N [permanents]" additional attack costs (Leviathan).
        // Done last so removing sacrificed permanents from the battlefield can't shift the indices
        // used above; the paired CantAttackUnlessEffect gate guarantees the cost is payable.
        attackSacrificeCostService.paySacrificeAttackCosts(gameData, playerId, attackerIndices);

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
        if (creature.isCantAttackThisTurn()) return false;
        if (gameQueryService.isLockedFromAttacking(gameData, creature.getId())) return false;
        if (isRestrictedByOtherCreaturesCantAttack(gameData, creature)) return false;
        if (creature.isSummoningSick() && !gameQueryService.hasKeyword(gameData, creature, Keyword.HASTE)
                && !gameQueryService.hasAuraWithEffect(gameData, creature, EnchantedCreatureCanAttackAsThoughHasteEffect.class)) return false;
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
            } else if (effect instanceof AttackOrBlockRestrictionEffect restriction) {
                condition = restriction.cantAttackOrBlockUnless();
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
     * When the attack is aimed at a planeswalker, only restrictions whose {@code protectsPlaneswalkers}
     * flag is set apply (Sandwurm Convergence — "can't attack you or planeswalkers you control").
     */
    private boolean isCantAttackDefenderDueToRestriction(GameData gameData, Permanent attacker, UUID targetId) {
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        // The protected player is the attacked player, or the controller of the attacked planeswalker.
        UUID protectedPlayerId = targetIsPlayer ? targetId
                : gameQueryService.findPermanentController(gameData, targetId);
        if (protectedPlayerId == null) return false;
        // Restrictions come from static abilities of the protected player's permanents (Form of the
        // Dragon, Sandwurm Convergence) and from player-scoped floating effects (Island Sanctuary's
        // "until your next turn" shield, which persists independently of its source permanent).
        List<CardEffect> restrictions = new ArrayList<>();
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(protectedPlayerId);
        if (defenderBattlefield != null) {
            for (Permanent perm : defenderBattlefield) {
                restrictions.addAll(perm.getCard().getEffects(EffectSlot.STATIC));
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect fe : gameData.floatingEffects) {
                if (protectedPlayerId.equals(fe.affectedPlayerId())) {
                    restrictions.add(fe.effect());
                }
            }
        }
        for (CardEffect effect : restrictions) {
            if (effect instanceof CreaturesCantAttackControllerUnlessPredicateEffect restriction
                    && (targetIsPlayer || restriction.protectsPlaneswalkers())
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, restriction.exemptionPredicate())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Intimidation Bolt lock ("Other creatures can't attack this turn"). While
     * {@code gameData.otherCreaturesCantAttackExemptCreatureIds} holds any exemptions, a creature may
     * attack only if its ID equals every one of them — i.e. it is the creature every Intimidation Bolt
     * resolved this turn targeted. Evaluated at declaration time, so it also bars creatures that entered
     * after the spell resolved. Empty list = no restriction.
     */
    private boolean isRestrictedByOtherCreaturesCantAttack(GameData gameData, Permanent creature) {
        List<UUID> exemptions = gameData.otherCreaturesCantAttackExemptCreatureIds;
        if (exemptions.isEmpty()) {
            return false;
        }
        synchronized (exemptions) {
            for (UUID exemptId : exemptions) {
                if (!creature.getId().equals(exemptId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCantAttackDueToGlobalRestriction(GameData gameData, Permanent creature) {
        boolean[] restricted = {false};
        UUID creatureController = CombatHelper.findControllerOf(gameData, creature);
        gameData.forEachPermanent((playerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CreaturesCantAttackUnlessPredicateEffect restriction) {
                    if (!predicateEvaluationService.matchesPermanentPredicate(gameData, creature, restriction.exemptionPredicate())) {
                        restricted[0] = true;
                    }
                } else if (effect instanceof CreaturesWithPowerGreaterThanAmountCantAttackEffect restriction) {
                    int threshold = amountEvaluationService.evaluate(gameData, restriction.amount(),
                            AmountContext.forStaticEffect(permanent, playerId));
                    if (gameQueryService.getEffectivePower(gameData, creature) > threshold) {
                        restricted[0] = true;
                    }
                } else if (effect instanceof AttackOrBlockRestrictionEffect restriction
                        && restriction.globallyCantAttackOrBlock() != null) {
                    FilterContext context = FilterContext.of(gameData)
                            .withSourceControllerId(playerId)
                            .withSourceCardId(permanent.getOriginalCard().getId());
                    if (predicateEvaluationService.matchesPermanentPredicate(creature, restriction.globallyCantAttackOrBlock(), context)) {
                        restricted[0] = true;
                    }
                } else if (effect instanceof ControlledCreaturesCantAttackUnlessPredicateEffect restriction) {
                    if (playerId.equals(creatureController)
                            && !predicateEvaluationService.matchesPermanentPredicate(gameData, creature, restriction.exemptionPredicate())) {
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
            // Global "matching creatures attack each combat if able" (e.g. Goblin Assault)
            count[0] += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(MatchingCreaturesMustAttackEffect.class::isInstance)
                    .map(MatchingCreaturesMustAttackEffect.class::cast)
                    .filter(e -> predicateEvaluationService.matchesPermanentPredicate(gameData, creature, e.matcher()))
                    .count();
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
     * Builds the list of available attack targets: the defending player, their planeswalkers,
     * and battles the active player is allowed to attack (not the protector).
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
                } else if (p.getCard().hasType(CardType.BATTLE)
                        && !activePlayerId.equals(p.getProtectorPlayerId())) {
                    targets.add(new AttackTarget(p.getId().toString(), p.getCard().getName(), false));
                }
            }
        }
        // Sieges sit on the controller's battlefield; the controller (and non-protectors) may attack them.
        List<Permanent> ownBf = gameData.playerBattlefields.get(activePlayerId);
        if (ownBf != null) {
            for (Permanent p : ownBf) {
                if (p.getCard().hasType(CardType.BATTLE)
                        && !activePlayerId.equals(p.getProtectorPlayerId())) {
                    targets.add(new AttackTarget(p.getId().toString(), p.getCard().getName(), false));
                }
            }
        }
        return targets;
    }

    /**
     * Returns the set of valid attack target UUIDs: the defending player plus attackable planeswalkers/battles.
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
                } else if (p.getCard().hasType(CardType.BATTLE)
                        && !activePlayerId.equals(p.getProtectorPlayerId())) {
                    validIds.add(p.getId());
                }
            }
        }
        List<Permanent> ownBf = gameData.playerBattlefields.get(activePlayerId);
        if (ownBf != null) {
            for (Permanent p : ownBf) {
                if (p.getCard().hasType(CardType.BATTLE)
                        && !activePlayerId.equals(p.getProtectorPlayerId())) {
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

    private void validateCanOnlyAttackAlone(GameData gameData, List<Permanent> battlefield,
                                            List<Integer> attackerIndices) {
        if (attackerIndices.size() <= 1) {
            return;
        }
        for (int idx : attackerIndices) {
            Permanent attacker = battlefield.get(idx);
            if (gameQueryService.hasAuraWithEffect(gameData, attacker,
                    EnchantedCreatureCanOnlyAttackAloneEffect.class)) {
                throw new IllegalStateException(attacker.getCard().getName() + " can only attack alone");
            }
        }
    }

    private boolean hasCantAttackOrBlockAlone(Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance);
    }

    /**
     * Okk (CR 508.1a): a creature with "can't attack unless a creature with greater power also
     * attacks" may only be declared as an attacker if another declared attacker has strictly
     * greater power. The comparison is checked only at declaration time.
     */
    private void validateGreaterPowerAlsoAttacks(GameData gameData, List<Permanent> battlefield,
                                                 List<Integer> attackerIndices) {
        for (int idx : attackerIndices) {
            Permanent restricted = battlefield.get(idx);
            if (!hasGreaterPowerRestriction(restricted)) {
                continue;
            }
            int power = gameQueryService.getEffectivePower(gameData, restricted);
            boolean greaterPowerAlsoAttacks = attackerIndices.stream()
                    .filter(other -> other != idx)
                    .map(battlefield::get)
                    .anyMatch(other -> gameQueryService.getEffectivePower(gameData, other) > power);
            if (!greaterPowerAlsoAttacks) {
                throw new IllegalStateException(restricted.getCard().getName()
                        + " can't attack unless a creature with greater power also attacks");
            }
        }
    }

    private boolean hasGreaterPowerRestriction(Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect.class::isInstance);
    }

    /**
     * Orcish Conscripts (CR 508.1a): a creature with "can't attack unless at least N other
     * creatures also attack" may only be declared as an attacker if at least N other creatures
     * are declared as attackers in the same combat. Checked only at declaration time.
     */
    private void validateCountAlsoAttacks(List<Permanent> battlefield, List<Integer> attackerIndices) {
        for (int idx : attackerIndices) {
            Permanent restricted = battlefield.get(idx);
            restricted.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(CantAttackOrBlockUnlessCountAlsoDoesEffect.class::isInstance)
                    .map(CantAttackOrBlockUnlessCountAlsoDoesEffect.class::cast)
                    .findFirst()
                    .ifPresent(effect -> {
                        long otherAttackers = attackerIndices.stream().filter(other -> other != idx).count();
                        if (otherAttackers < effect.otherCount()) {
                            throw new IllegalStateException(restricted.getCard().getName()
                                    + " can't attack unless at least " + effect.otherCount()
                                    + " other creatures attack");
                        }
                    });
        }
    }

    /**
     * Validates the declared attacking bands (CR 702.22c/d) and returns them as index sets ready to
     * stamp with band ids. Each band must: contain at least two declared attackers, include at least
     * one creature with banding and at most one without, keep every member attacking the same target,
     * and not share a creature with another band. Returns an empty list when no bands are declared.
     */
    private List<Set<Integer>> validateBands(GameData gameData, List<Permanent> battlefield,
                                             Set<Integer> declaredAttackerIndices,
                                             Map<Integer, UUID> resolvedTargets,
                                             List<List<Integer>> bands) {
        List<Set<Integer>> result = new ArrayList<>();
        if (bands == null || bands.isEmpty()) {
            return result;
        }
        Set<Integer> alreadyBanded = new HashSet<>();
        for (List<Integer> band : bands) {
            if (band == null || band.isEmpty()) {
                continue;
            }
            Set<Integer> members = new LinkedHashSet<>(band);
            if (members.size() < 2) {
                throw new IllegalStateException("A band must contain at least two creatures");
            }
            int withBanding = 0;
            int withoutBanding = 0;
            UUID sharedTarget = null;
            boolean first = true;
            for (int idx : members) {
                if (!declaredAttackerIndices.contains(idx)) {
                    throw new IllegalStateException("Band member " + idx + " is not a declared attacker");
                }
                if (!alreadyBanded.add(idx)) {
                    throw new IllegalStateException("Creature at index " + idx + " can't be a member of more than one band");
                }
                Permanent creature = battlefield.get(idx);
                if (gameQueryService.hasKeyword(gameData, creature, Keyword.BANDING)) {
                    withBanding++;
                } else {
                    withoutBanding++;
                }
                UUID target = resolvedTargets.get(idx);
                if (first) {
                    sharedTarget = target;
                    first = false;
                } else if (!Objects.equals(sharedTarget, target)) {
                    throw new IllegalStateException("All creatures in a band must attack the same player or planeswalker");
                }
            }
            if (withBanding < 1) {
                throw new IllegalStateException("A band must contain at least one creature with banding");
            }
            if (withoutBanding > 1) {
                throw new IllegalStateException("A band can contain at most one creature without banding");
            }
            result.add(members);
        }
        return result;
    }

    void payGenericMana(ManaPool pool, int amount) {
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

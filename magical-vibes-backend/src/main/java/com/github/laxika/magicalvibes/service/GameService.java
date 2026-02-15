package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.TargetZone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.AttackingOrBlockingTargetFilter;
import com.github.laxika.magicalvibes.model.filter.AttackingTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ControllerOnlyTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CreatureColorTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ExcludeSelfTargetFilter;
import com.github.laxika.magicalvibes.model.filter.MaxPowerTargetFilter;
import com.github.laxika.magicalvibes.model.filter.SpellColorTargetFilter;
import com.github.laxika.magicalvibes.model.filter.SpellTypeTargetFilter;
import com.github.laxika.magicalvibes.model.filter.WithoutKeywordTargetFilter;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final Random random = new Random();

    private final SessionManager sessionManager;
    private final GameRegistry gameRegistry;
    private final GameHelper gameHelper;
    private final EffectResolutionService effectResolutionService;
    private final CombatService combatService;

    public void passPriority(GameData gameData, Player player) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            if (gameData.awaitingInput != null) {
                throw new IllegalStateException("Cannot pass priority while awaiting input");
            }

            UUID priorityHolder = gameHelper.getPriorityPlayerId(gameData);
            if (priorityHolder == null || !priorityHolder.equals(player.getId())) {
                throw new IllegalStateException("You do not have priority");
            }

            gameData.priorityPassedBy.add(player.getId());
            log.info("Game {} - {} passed priority on step {} (passed: {}/2)",
                    gameData.id, player.getUsername(), gameData.currentStep, gameData.priorityPassedBy.size());

            if (gameData.priorityPassedBy.size() >= 2) {
                if (!gameData.stack.isEmpty()) {
                    resolveTopOfStack(gameData);
                } else {
                    advanceStep(gameData);
                }
            } else {
                gameHelper.broadcastGameState(gameData);
            }

            resolveAutoPass(gameData);
        }
    }

    public void advanceStep(GameData gameData) {
        // Process end-of-combat sacrifices when leaving END_OF_COMBAT
        if (gameData.currentStep == TurnStep.END_OF_COMBAT && !gameData.permanentsToSacrificeAtEndOfCombat.isEmpty()) {
            combatService.processEndOfCombatSacrifices(gameData);
            gameData.priorityPassedBy.clear();
            return;
        }

        gameData.priorityPassedBy.clear();
        gameData.awaitingInput = null;
        TurnStep next = gameData.currentStep.next();

        gameHelper.drainManaPools(gameData);

        if (next != null) {
            gameData.currentStep = next;
            String logEntry = "Step: " + next.getDisplayName();
            gameHelper.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Step advanced to {}", gameData.id, next);
            gameHelper.broadcastGameState(gameData);

            if (gameData.status == GameStatus.FINISHED) return;

            if (next == TurnStep.UPKEEP) {
                handleUpkeepTriggers(gameData);
            } else if (next == TurnStep.DRAW) {
                handleDrawStep(gameData);
            } else if (next == TurnStep.DECLARE_ATTACKERS) {
                combatService.handleDeclareAttackersStep(gameData);
            } else if (next == TurnStep.DECLARE_BLOCKERS) {
                handleCombatResult(combatService.handleDeclareBlockersStep(gameData), gameData);
            } else if (next == TurnStep.COMBAT_DAMAGE) {
                handleCombatResult(combatService.resolveCombatDamage(gameData), gameData);
            } else if (next == TurnStep.END_OF_COMBAT) {
                combatService.clearCombatState(gameData);
            } else if (next == TurnStep.CLEANUP) {
                gameHelper.resetEndOfTurnModifiers(gameData);
            }
        } else {
            advanceTurn(gameData);
        }
    }

    private void handleUpkeepTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> upkeepEffects = perm.getCard().getEffects(EffectSlot.UPKEEP_TRIGGERED);
            if (upkeepEffects == null || upkeepEffects.isEmpty()) continue;

            for (CardEffect effect : upkeepEffects) {
                if (effect instanceof MayEffect may) {
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            perm.getCard(),
                            activePlayerId,
                            List.of(may.wrapped()),
                            perm.getCard().getName() + " — " + may.prompt()
                    ));
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            activePlayerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect))
                    ));

                    String logEntry = perm.getCard().getName() + "'s upkeep ability triggers.";
                    gameHelper.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        }

        // Check all battlefields for EACH_UPKEEP_TRIGGERED effects
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> playerBattlefield = gameData.playerBattlefields.get(playerId);
            if (playerBattlefield == null) continue;

            for (Permanent perm : playerBattlefield) {
                List<CardEffect> eachUpkeepEffects = perm.getCard().getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED);
                if (eachUpkeepEffects == null || eachUpkeepEffects.isEmpty()) continue;

                for (CardEffect effect : eachUpkeepEffects) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s upkeep ability",
                            new ArrayList<>(List.of(effect)),
                            activePlayerId,
                            (UUID) null
                    ));

                    String logEntry = perm.getCard().getName() + "'s upkeep ability triggers.";
                    gameHelper.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} each-upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        }

        if (!gameData.stack.isEmpty()) {

        }

        gameHelper.processNextMayAbility(gameData);
    }

    private void handleDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;

        // The starting player skips their draw on turn 1
        if (gameData.turnNumber == 1 && activePlayerId.equals(gameData.startingPlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips the draw (first turn).";
            gameHelper.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        // Check for Plagiarize replacement effect
        UUID replacementController = gameData.drawReplacementTargetToController.get(activePlayerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(activePlayerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is replaced by Plagiarize — " + controllerName + " draws a card instead.";
            gameHelper.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Plagiarize replaces {}'s draw step draw, {} draws instead",
                    gameData.id, playerName, controllerName);
            gameHelper.resolveDrawCard(gameData, replacementController);
            return;
        }

        List<Card> deck = gameData.playerDecks.get(activePlayerId);
        List<Card> hand = gameData.playerHands.get(activePlayerId);

        if (deck == null || deck.isEmpty()) {
            log.warn("Game {} - {} has no cards to draw", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        Card drawn = deck.removeFirst();
        hand.add(drawn);





        String playerName = gameData.playerIdToName.get(activePlayerId);
        String logEntry = playerName + " draws a card.";
        gameHelper.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} draws a card (hand: {}, deck: {})", gameData.id, playerName, hand.size(), deck.size());
    }

    private void advanceTurn(GameData gameData) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        UUID currentActive = gameData.activePlayerId;
        UUID nextActive = ids.get(0).equals(currentActive) ? ids.get(1) : ids.get(0);
        String nextActiveName = gameData.playerIdToName.get(nextActive);

        gameData.activePlayerId = nextActive;
        gameData.turnNumber++;
        gameData.currentStep = TurnStep.first();
        gameData.awaitingInput = null;
        gameData.priorityPassedBy.clear();
        gameData.landsPlayedThisTurn.clear();
        gameData.spellsCastThisTurn.clear();

        gameHelper.drainManaPools(gameData);

        // Untap all permanents for the new active player (skip those with "doesn't untap" effects)
        List<Permanent> battlefield = gameData.playerBattlefields.get(nextActive);
        if (battlefield != null) {
            battlefield.forEach(p -> {
                if (!gameHelper.hasAuraWithEffect(gameData, p, EnchantedCreatureDoesntUntapEffect.class)) {
                    p.untap();
                }
                p.setSummoningSick(false);
            });
        }


        String untapLog = nextActiveName + " untaps their permanents.";
        gameHelper.logAndBroadcast(gameData, untapLog);
        log.info("Game {} - {} untaps their permanents", gameData.id, nextActiveName);

        String logEntry = "Turn " + gameData.turnNumber + " begins. " + nextActiveName + "'s turn.";
        gameHelper.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, nextActiveName);
        gameHelper.broadcastGameState(gameData);
    }

    private void resolveTopOfStack(GameData gameData) {
        if (gameData.stack.isEmpty()) return;

        StackEntry entry = gameData.stack.removeLast();
        gameData.priorityPassedBy.clear();

        if (entry.getEntryType() == StackEntryType.CREATURE_SPELL) {
            Card card = entry.getCard();
            UUID controllerId = entry.getControllerId();

            gameData.playerBattlefields.get(controllerId).add(new Permanent(card));


            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            gameHelper.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

            gameHelper.handleCreatureEnteredBattlefield(gameData, controllerId, card, entry.getTargetPermanentId());
            if (gameData.awaitingInput == null) {
                gameHelper.checkLegendRule(gameData, controllerId);
            }
        } else if (entry.getEntryType() == StackEntryType.ENCHANTMENT_SPELL) {
            Card card = entry.getCard();
            UUID controllerId = entry.getControllerId();

            // Aura fizzles if its target is no longer on the battlefield
            if (card.isAura() && entry.getTargetPermanentId() != null) {
                Permanent target = gameHelper.findPermanentById(gameData, entry.getTargetPermanentId());
                if (target == null) {
                    String fizzleLog = card.getName() + " fizzles (enchanted creature no longer exists).";
                    gameHelper.logAndBroadcast(gameData, fizzleLog);
                    gameData.playerGraveyards.get(controllerId).add(card);
        
                    log.info("Game {} - {} fizzles, target {} no longer exists", gameData.id, card.getName(), entry.getTargetPermanentId());
                } else {
                    Permanent perm = new Permanent(card);
                    perm.setAttachedTo(entry.getTargetPermanentId());
                    gameData.playerBattlefields.get(controllerId).add(perm);

                    String playerName = gameData.playerIdToName.get(controllerId);
                    String logEntry = card.getName() + " enters the battlefield attached to " + target.getCard().getName() + " under " + playerName + "'s control.";
                    gameHelper.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} resolves, attached to {} for {}", gameData.id, card.getName(), target.getCard().getName(), playerName);

                    // Handle control-changing auras (e.g., Persuasion)
                    boolean hasControlEffect = card.getEffects(EffectSlot.STATIC).stream()
                            .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
                    if (hasControlEffect) {
                        gameHelper.stealCreature(gameData, controllerId, target);
                    }

        
                }
            } else {
                gameData.playerBattlefields.get(controllerId).add(new Permanent(card));
    

                String playerName = gameData.playerIdToName.get(controllerId);
                String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

                // Check if enchantment has "as enters" color choice
                boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .anyMatch(e -> e instanceof ChooseColorEffect);
                if (needsColorChoice) {
                    List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                    Permanent justEntered = bf.get(bf.size() - 1);
                    gameHelper.beginColorChoice(gameData, controllerId, justEntered.getId(), null);
                }
                if (gameData.awaitingInput == null) {
                    gameHelper.checkLegendRule(gameData, controllerId);
                }
            }
        } else if (entry.getEntryType() == StackEntryType.ARTIFACT_SPELL) {
            Card card = entry.getCard();
            UUID controllerId = entry.getControllerId();

            gameData.playerBattlefields.get(controllerId).add(new Permanent(card));


            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            gameHelper.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);
            if (gameData.awaitingInput == null) {
                gameHelper.checkLegendRule(gameData, controllerId);
            }
        } else if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == StackEntryType.SORCERY_SPELL
                || entry.getEntryType() == StackEntryType.INSTANT_SPELL) {
            // Check if targeted spell/ability fizzles due to illegal target
            boolean targetFizzled = false;
            if (entry.getTargetPermanentId() != null) {
                if (entry.getTargetZone() == TargetZone.GRAVEYARD) {
                    targetFizzled = gameHelper.findCardInGraveyardById(gameData, entry.getTargetPermanentId()) == null;
                } else if (entry.getTargetZone() == TargetZone.STACK) {
                    targetFizzled = gameData.stack.stream()
                            .noneMatch(se -> se.getCard().getId().equals(entry.getTargetPermanentId()));
                } else {
                    Permanent targetPerm = gameHelper.findPermanentById(gameData, entry.getTargetPermanentId());
                    if (targetPerm == null && !gameData.playerIds.contains(entry.getTargetPermanentId())) {
                        targetFizzled = true;
                    } else if (targetPerm != null && entry.getCard() != null && entry.getCard().getTargetFilter() != null) {
                        try {
                            gameHelper.validateTargetFilter(entry.getCard().getTargetFilter(), targetPerm);
                        } catch (IllegalStateException e) {
                            targetFizzled = true;
                        }
                    }
                }
            }
            // Check multi-target graveyard fizzle: if ALL targeted cards are gone, fizzle
            if (!targetFizzled && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
                boolean allGone = true;
                for (UUID cardId : entry.getTargetCardIds()) {
                    if (gameHelper.findCardInGraveyardById(gameData, cardId) != null) {
                        allGone = false;
                        break;
                    }
                }
                if (allGone) {
                    targetFizzled = true;
                }
            }
            if (targetFizzled) {
                String fizzleLog = entry.getDescription() + " fizzles (illegal target).";
                gameHelper.logAndBroadcast(gameData, fizzleLog);
                log.info("Game {} - {} fizzles, target {} is illegal",
                        gameData.id, entry.getDescription(), entry.getTargetPermanentId());

                // Fizzled spells still go to graveyard
                if (entry.getEntryType() == StackEntryType.SORCERY_SPELL
                        || entry.getEntryType() == StackEntryType.INSTANT_SPELL) {
                    gameData.playerGraveyards.get(entry.getControllerId()).add(entry.getCard());
        
                }
            } else {
                String logEntry = entry.getDescription() + " resolves.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} resolves", gameData.id, entry.getDescription());

                effectResolutionService.resolveEffects(gameData, entry);

                if (entry.getEntryType() == StackEntryType.SORCERY_SPELL
                        || entry.getEntryType() == StackEntryType.INSTANT_SPELL) {
                    boolean shuffled = entry.getEffectsToResolve().stream()
                            .anyMatch(e -> e instanceof ShuffleIntoLibraryEffect);
                    if (!shuffled) {
                        gameData.playerGraveyards.get(entry.getControllerId()).add(entry.getCard());
            
                    }
                }
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {

            gameHelper.processNextMayAbility(gameData);
            return;
        }

        gameHelper.broadcastGameState(gameData);
    }

    public JoinGame getJoinGame(GameData data, UUID playerId) {
        return toJoinGame(data, playerId);
    }

    public void resendAwaitingInput(GameData gameData, UUID playerId) {
        synchronized (gameData) {
            if (gameData.awaitingInput == null) return;

            switch (gameData.awaitingInput) {
                case ATTACKER_DECLARATION -> {
                    if (playerId.equals(gameData.activePlayerId)) {
                        List<Integer> attackable = combatService.getAttackableCreatureIndices(gameData, playerId);
                        sessionManager.sendToPlayer(playerId, new AvailableAttackersMessage(attackable));
                    }
                }
                case BLOCKER_DECLARATION -> {
                    UUID defenderId = gameHelper.getOpponentId(gameData, gameData.activePlayerId);
                    if (playerId.equals(defenderId)) {
                        List<Integer> blockable = combatService.getBlockableCreatureIndices(gameData, defenderId);
                        List<Integer> attackerIndices = combatService.getAttackingCreatureIndices(gameData, gameData.activePlayerId);
                        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
                        attackerIndices = attackerIndices.stream()
                                .filter(idx -> !attackerBattlefield.get(idx).isCantBeBlocked()
                                        && attackerBattlefield.get(idx).getCard().getEffects(EffectSlot.STATIC).stream()
                                                .noneMatch(e -> e instanceof CantBeBlockedEffect))
                                .toList();
                        sessionManager.sendToPlayer(defenderId, new AvailableBlockersMessage(blockable, attackerIndices));
                    }
                }
                case CARD_CHOICE, TARGETED_CARD_CHOICE -> {
                    if (playerId.equals(gameData.awaitingCardChoicePlayerId)) {
                        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(
                                new ArrayList<>(gameData.awaitingCardChoiceValidIndices), "Choose a card from your hand."));
                    }
                }
                case DISCARD_CHOICE -> {
                    if (playerId.equals(gameData.awaitingCardChoicePlayerId)) {
                        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(
                                new ArrayList<>(gameData.awaitingCardChoiceValidIndices), "Choose a card to discard."));
                    }
                }
                case PERMANENT_CHOICE -> {
                    if (playerId.equals(gameData.awaitingPermanentChoicePlayerId)) {
                        sessionManager.sendToPlayer(playerId, new ChoosePermanentMessage(
                                new ArrayList<>(gameData.awaitingPermanentChoiceValidIds), "Choose a permanent."));
                    }
                }
                case GRAVEYARD_CHOICE -> {
                    if (playerId.equals(gameData.awaitingGraveyardChoicePlayerId)) {
                        sessionManager.sendToPlayer(playerId, new ChooseCardFromGraveyardMessage(
                                new ArrayList<>(gameData.awaitingGraveyardChoiceValidIndices), "Choose a card from the graveyard."));
                    }
                }
                case COLOR_CHOICE -> {
                    if (playerId.equals(gameData.awaitingColorChoicePlayerId)) {
                        List<String> options;
                        String prompt;
                        if (gameData.colorChoiceContext instanceof ColorChoiceContext.TextChangeFromWord) {
                            options = new ArrayList<>();
                            options.addAll(GameHelper.TEXT_CHANGE_COLOR_WORDS);
                            options.addAll(GameHelper.TEXT_CHANGE_LAND_TYPES);
                            prompt = "Choose a color word or basic land type to replace.";
                        } else if (gameData.colorChoiceContext instanceof ColorChoiceContext.TextChangeToWord ctx) {
                            if (ctx.isColor()) {
                                options = GameHelper.TEXT_CHANGE_COLOR_WORDS.stream().filter(c -> !c.equals(ctx.fromWord())).toList();
                                prompt = "Choose the replacement color word.";
                            } else {
                                options = GameHelper.TEXT_CHANGE_LAND_TYPES.stream().filter(t -> !t.equals(ctx.fromWord())).toList();
                                prompt = "Choose the replacement basic land type.";
                            }
                        } else {
                            options = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                            prompt = "Choose a color.";
                        }
                        sessionManager.sendToPlayer(playerId, new ChooseColorMessage(options, prompt));
                    }
                }
                case MAY_ABILITY_CHOICE -> {
                    if (playerId.equals(gameData.awaitingMayAbilityPlayerId) && !gameData.pendingMayAbilities.isEmpty()) {
                        PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
                        sessionManager.sendToPlayer(playerId, new MayAbilityMessage(next.description()));
                    }
                }
                case MULTI_PERMANENT_CHOICE -> {
                    if (playerId.equals(gameData.awaitingMultiPermanentChoicePlayerId)) {
                        sessionManager.sendToPlayer(playerId, new ChooseMultiplePermanentsMessage(
                                new ArrayList<>(gameData.awaitingMultiPermanentChoiceValidIds),
                                gameData.awaitingMultiPermanentChoiceMaxCount, "Choose permanents."));
                    }
                }
                case MULTI_GRAVEYARD_CHOICE -> {
                    if (playerId.equals(gameData.awaitingMultiGraveyardChoicePlayerId)) {
                        List<UUID> validCardIds = new ArrayList<>(gameData.awaitingMultiGraveyardChoiceValidCardIds);
                        List<CardView> cardViews = new ArrayList<>();
                        for (UUID pid : gameData.orderedPlayerIds) {
                            List<Card> graveyard = gameData.playerGraveyards.get(pid);
                            if (graveyard == null) continue;
                            for (Card card : graveyard) {
                                if (gameData.awaitingMultiGraveyardChoiceValidCardIds.contains(card.getId())) {
                                    cardViews.add(gameHelper.getCardViewFactory().create(card));
                                }
                            }
                        }
                        sessionManager.sendToPlayer(playerId, new ChooseMultipleCardsFromGraveyardsMessage(
                                validCardIds, cardViews, gameData.awaitingMultiGraveyardChoiceMaxCount,
                                "Exile up to " + gameData.awaitingMultiGraveyardChoiceMaxCount + " cards from graveyards."));
                    }
                }
                case LIBRARY_REORDER -> {
                    if (playerId.equals(gameData.awaitingLibraryReorderPlayerId) && gameData.awaitingLibraryReorderCards != null) {
                        List<CardView> cardViews = gameData.awaitingLibraryReorderCards.stream().map(gameHelper.getCardViewFactory()::create).toList();
                        sessionManager.sendToPlayer(playerId, new ReorderLibraryCardsMessage(
                                cardViews, "Put these cards back on top of your library in any order (top to bottom)."));
                    }
                }
                case LIBRARY_SEARCH -> {
                    if (playerId.equals(gameData.awaitingLibrarySearchPlayerId) && gameData.awaitingLibrarySearchCards != null) {
                        List<CardView> cardViews = gameData.awaitingLibrarySearchCards.stream().map(gameHelper.getCardViewFactory()::create).toList();
                        sessionManager.sendToPlayer(playerId, new ChooseCardFromLibraryMessage(
                                cardViews, "Search your library for a basic land card to put into your hand."));
                    }
                }
                case REVEALED_HAND_CHOICE -> {
                    if (playerId.equals(gameData.awaitingCardChoicePlayerId) && gameData.awaitingRevealedHandChoiceTargetPlayerId != null) {
                        UUID targetPlayerId = gameData.awaitingRevealedHandChoiceTargetPlayerId;
                        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
                        String targetName = gameData.playerIdToName.get(targetPlayerId);
                        List<CardView> cardViews = targetHand.stream().map(gameHelper.getCardViewFactory()::create).toList();
                        List<Integer> validIndices = new ArrayList<>(gameData.awaitingCardChoiceValidIndices);
                        sessionManager.sendToPlayer(playerId, new ChooseFromRevealedHandMessage(
                                cardViews, validIndices, "Choose a card to put on top of " + targetName + "'s library."));
                    }
                }
            }
        }
    }

    public void keepHand(GameData gameData, Player player) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            if (gameData.playerKeptHand.contains(player.getId())) {
                throw new IllegalStateException("You have already kept your hand");
            }

            gameData.playerKeptHand.add(player.getId());
            int mulliganCount = gameData.mulliganCounts.getOrDefault(player.getId(), 0);
            List<Card> hand = gameData.playerHands.get(player.getId());

            sessionManager.sendToPlayers(gameData.orderedPlayerIds,new MulliganResolvedMessage(player.getUsername(), true, mulliganCount));

            if (mulliganCount > 0 && !hand.isEmpty()) {
                int cardsToBottom = Math.min(mulliganCount, hand.size());
                gameData.playerNeedsToBottom.put(player.getId(), cardsToBottom);

                String logEntry = player.getUsername() + " keeps their hand and must put " + cardsToBottom +
                        " card" + (cardsToBottom > 1 ? "s" : "") + " on the bottom of their library.";
                gameHelper.logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} kept hand, needs to bottom {} cards (mulligan count: {})", gameData.id, player.getUsername(), cardsToBottom, mulliganCount);

                gameHelper.broadcastGameState(gameData);
                sessionManager.sendToPlayer(player.getId(), new SelectCardsToBottomMessage(cardsToBottom));
            } else {
                String logEntry = player.getUsername() + " keeps their hand.";
                gameHelper.logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} kept hand (no mulligans)", gameData.id, player.getUsername());

                checkStartGame(gameData);
            }
        }
    }

    public void bottomCards(GameData gameData, Player player, List<Integer> cardIndices) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            Integer neededCount = gameData.playerNeedsToBottom.get(player.getId());
            if (neededCount == null) {
                throw new IllegalStateException("You don't need to put cards on the bottom");
            }
            if (cardIndices.size() != neededCount) {
                throw new IllegalStateException("You must select exactly " + neededCount + " card(s) to put on the bottom");
            }

            List<Card> hand = gameData.playerHands.get(player.getId());
            List<Card> deck = gameData.playerDecks.get(player.getId());

            Set<Integer> uniqueIndices = new HashSet<>(cardIndices);
            if (uniqueIndices.size() != cardIndices.size()) {
                throw new IllegalStateException("Duplicate card indices are not allowed");
            }
            for (int idx : cardIndices) {
                if (idx < 0 || idx >= hand.size()) {
                    throw new IllegalStateException("Invalid card index: " + idx);
                }
            }

            // Sort indices descending so removal doesn't shift earlier indices
            List<Integer> sorted = new ArrayList<>(cardIndices);
            sorted.sort(Collections.reverseOrder());
            List<Card> bottomCards = new ArrayList<>();
            for (int idx : sorted) {
                bottomCards.add(hand.remove(idx));
            }
            deck.addAll(bottomCards);

            gameData.playerNeedsToBottom.remove(player.getId());



            String logEntry = player.getUsername() + " puts " + bottomCards.size() +
                    " card" + (bottomCards.size() > 1 ? "s" : "") + " on the bottom of their library (keeping " + hand.size() + " cards).";
            gameHelper.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} bottomed {} cards, hand size now {}", gameData.id, player.getUsername(), bottomCards.size(), hand.size());

            gameHelper.broadcastGameState(gameData);
            checkStartGame(gameData);
        }
    }

    private void checkStartGame(GameData gameData) {
        if (gameData.playerKeptHand.size() >= 2 && gameData.playerNeedsToBottom.isEmpty()) {
            startGame(gameData);
        }
    }

    public void mulligan(GameData gameData, Player player) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            if (gameData.playerKeptHand.contains(player.getId())) {
                throw new IllegalStateException("You have already kept your hand");
            }
            int currentMulliganCount = gameData.mulliganCounts.getOrDefault(player.getId(), 0);
            if (currentMulliganCount >= 7) {
                throw new IllegalStateException("Maximum mulligans reached");
            }
            List<Card> hand = gameData.playerHands.get(player.getId());
            List<Card> deck = gameData.playerDecks.get(player.getId());

            deck.addAll(hand);
            hand.clear();
            Collections.shuffle(deck, random);

            List<Card> newHand = new ArrayList<>(deck.subList(0, 7));
            deck.subList(0, 7).clear();
            gameData.playerHands.put(player.getId(), newHand);

            int newMulliganCount = currentMulliganCount + 1;
            gameData.mulliganCounts.put(player.getId(), newMulliganCount);


            sessionManager.sendToPlayers(gameData.orderedPlayerIds,new MulliganResolvedMessage(player.getUsername(), false, newMulliganCount));


            String logEntry = player.getUsername() + " takes a mulligan (mulligan #" + newMulliganCount + ").";
            gameHelper.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} mulliganed (count: {})", gameData.id, player.getUsername(), newMulliganCount);
            gameHelper.broadcastGameState(gameData);
        }
    }

    private void startGame(GameData gameData) {
        gameData.status = GameStatus.RUNNING;
        gameData.activePlayerId = gameData.startingPlayerId;
        gameData.turnNumber = 1;
        gameData.currentStep = TurnStep.first();

        String logEntry1 = "Mulligan phase complete!";
        String logEntry2 = "Turn 1 begins. " + gameData.playerIdToName.get(gameData.activePlayerId) + "'s turn.";
        gameHelper.logAndBroadcast(gameData, logEntry1);
        gameHelper.logAndBroadcast(gameData, logEntry2);

        gameHelper.broadcastGameState(gameData);

        log.info("Game {} - Game started! Turn 1 begins. Active player: {}", gameData.id, gameData.playerIdToName.get(gameData.activePlayerId));

        resolveAutoPass(gameData);
    }

    private JoinGame toJoinGame(GameData data, UUID playerId) {
        List<CardView> hand = playerId != null
                ? data.playerHands.getOrDefault(playerId, List.of()).stream().map(gameHelper.getCardViewFactory()::create).toList()
                : List.of();
        int mulliganCount = playerId != null ? data.mulliganCounts.getOrDefault(playerId, 0) : 0;
        Map<String, Integer> manaPool = gameHelper.getManaPool(data, playerId);
        List<TurnStep> autoStopSteps = playerId != null && data.playerAutoStopSteps.containsKey(playerId)
                ? new ArrayList<>(data.playerAutoStopSteps.get(playerId))
                : List.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
        return new JoinGame(
                data.id,
                data.gameName,
                data.status,
                new ArrayList<>(data.playerNames),
                new ArrayList<>(data.orderedPlayerIds),
                new ArrayList<>(data.gameLog),
                data.currentStep,
                data.activePlayerId,
                data.turnNumber,
                gameHelper.getPriorityPlayerId(data),
                hand,
                mulliganCount,
                gameHelper.getDeckSizes(data),
                gameHelper.getHandSizes(data),
                gameHelper.getBattlefields(data),
                manaPool,
                autoStopSteps,
                gameHelper.getLifeTotals(data),
                data.stack.stream().map(gameHelper.getStackEntryViewFactory()::create).toList(),
                gameHelper.getGraveyardViews(data)
        );
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            UUID playerId = player.getId();
            List<Integer> playable = gameHelper.getPlayableCardIndices(gameData, playerId);
            if (!playable.contains(cardIndex)) {
                throw new IllegalStateException("Card is not playable");
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            Card card = hand.get(cardIndex);

            // For X-cost spells, validate that player can pay colored + generic + xValue + any cost increases
            if (card.getManaCost() != null) {
                ManaCost cost = new ManaCost(card.getManaCost());
                if (cost.hasX()) {
                    if (effectiveXValue < 0) {
                        throw new IllegalStateException("X value cannot be negative");
                    }
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    int additionalCost = gameHelper.getOpponentCostIncrease(gameData, playerId, card.getType());
                    if (!cost.canPay(pool, effectiveXValue + additionalCost)) {
                        throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                    }
                }
            }

            // Validate spell target (targeting a spell on the stack)
            if (card.isNeedsSpellTarget()) {
                if (targetPermanentId == null) {
                    throw new IllegalStateException("Must target a spell on the stack");
                }
                boolean validSpellTarget = gameData.stack.stream()
                        .anyMatch(se -> se.getCard().getId().equals(targetPermanentId)
                                && se.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                                && se.getEntryType() != StackEntryType.ACTIVATED_ABILITY);
                if (!validSpellTarget) {
                    throw new IllegalStateException("Target must be a spell on the stack");
                }

                // Validate spell color filter (e.g., "Counter target red or green spell")
                if (card.getTargetFilter() instanceof SpellColorTargetFilter colorFilter) {
                    StackEntry targetSpell = gameData.stack.stream()
                            .filter(se -> se.getCard().getId().equals(targetPermanentId))
                            .findFirst().orElse(null);
                    if (targetSpell != null && !colorFilter.colors().contains(targetSpell.getCard().getColor())) {
                        throw new IllegalStateException("Target spell must be " +
                                colorFilter.colors().stream().map(c -> c.name().toLowerCase()).reduce((a, b) -> a + " or " + b).orElse("") + ".");
                    }
                }

                // Validate spell type filter (e.g., "Counter target creature spell")
                if (card.getTargetFilter() instanceof SpellTypeTargetFilter typeFilter) {
                    StackEntry targetSpell = gameData.stack.stream()
                            .filter(se -> se.getCard().getId().equals(targetPermanentId))
                            .findFirst().orElse(null);
                    if (targetSpell != null && !typeFilter.spellTypes().contains(targetSpell.getEntryType())) {
                        throw new IllegalStateException("Target must be a creature spell.");
                    }
                }
            }

            // Validate target if specified (can be a permanent or a player)
            if (targetPermanentId != null && !card.isNeedsSpellTarget()) {
                Permanent target = gameHelper.findPermanentById(gameData, targetPermanentId);
                if (target == null && !gameData.playerIds.contains(targetPermanentId)) {
                    throw new IllegalStateException("Invalid target");
                }

                // Protection validation
                if (target != null && card.isNeedsTarget() && gameHelper.hasProtectionFrom(gameData, target, card.getColor())) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from " + card.getColor().name().toLowerCase());
                }

                // Creature shroud validation
                if (target != null && card.isNeedsTarget() && hasKeyword(gameData, target, Keyword.SHROUD)) {
                    throw new IllegalStateException(target.getCard().getName() + " has shroud and can't be targeted");
                }

                // Player shroud validation
                if (target == null && card.isNeedsTarget() && gameData.playerIds.contains(targetPermanentId)
                        && gameHelper.playerHasShroud(gameData, targetPermanentId)) {
                    throw new IllegalStateException(gameData.playerIdToName.get(targetPermanentId) + " has shroud and can't be targeted");
                }

                // Generic target filter validation for spells
                if (card.getTargetFilter() != null && target != null) {
                    gameHelper.validateTargetFilter(card.getTargetFilter(), target);
                }

                // Effect-specific target validation
                for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
                    if (effect instanceof PutTargetOnBottomOfLibraryEffect) {
                        if (target == null || !isCreature(gameData, target)) {
                            throw new IllegalStateException("Target must be a creature");
                        }
                    }
                    if (effect instanceof BoostTargetBlockingCreatureEffect) {
                        if (target == null || !isCreature(gameData, target) || !target.isBlocking()) {
                            throw new IllegalStateException("Target must be a blocking creature");
                        }
                    }
                    if (effect instanceof GainControlOfTargetAuraEffect) {
                        if (target == null || target.getCard().getType() != CardType.ENCHANTMENT
                                || !target.getCard().getSubtypes().contains(CardSubtype.AURA)
                                || target.getAttachedTo() == null) {
                            throw new IllegalStateException("Target must be an Aura attached to a permanent");
                        }
                    }
                }
            }

            hand.remove(cardIndex);

            if (card.getType() == CardType.BASIC_LAND) {
                // Lands bypass the stack — go directly onto battlefield
                gameData.playerBattlefields.get(playerId).add(new Permanent(card));
                gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);


    
    

                String logEntry = player.getUsername() + " plays " + card.getName() + ".";
                gameHelper.logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} plays {}", gameData.id, player.getUsername(), card.getName());

                resolveAutoPass(gameData);
            } else if (card.getType() == CardType.CREATURE) {
                paySpellManaCost(gameData, playerId, card, 0);
                gameData.stack.add(new StackEntry(
                        StackEntryType.CREATURE_SPELL, card, playerId, card.getName(),
                        List.of(), 0, targetPermanentId, null
                ));
                finishSpellCast(gameData, playerId, player, hand, card);
            } else if (card.getType() == CardType.ENCHANTMENT) {
                paySpellManaCost(gameData, playerId, card, 0);
                gameData.stack.add(new StackEntry(
                        StackEntryType.ENCHANTMENT_SPELL, card, playerId, card.getName(),
                        List.of(), 0, targetPermanentId, null
                ));
                finishSpellCast(gameData, playerId, player, hand, card);
            } else if (card.getType() == CardType.ARTIFACT) {
                paySpellManaCost(gameData, playerId, card, 0);
                gameData.stack.add(new StackEntry(
                        StackEntryType.ARTIFACT_SPELL, card, playerId, card.getName(),
                        List.of(), 0, null, null
                ));
                finishSpellCast(gameData, playerId, player, hand, card);
            } else if (card.getType() == CardType.SORCERY) {
                paySpellManaCost(gameData, playerId, card, effectiveXValue);
                gameData.stack.add(new StackEntry(
                        StackEntryType.SORCERY_SPELL, card, playerId, card.getName(),
                        new ArrayList<>(card.getEffects(EffectSlot.SPELL)), effectiveXValue, targetPermanentId, null
                ));
                finishSpellCast(gameData, playerId, player, hand, card);
            } else if (card.getType() == CardType.INSTANT) {
                paySpellManaCost(gameData, playerId, card, effectiveXValue);

                // Validate damage assignments for damage distribution spells
                if (card.isNeedsDamageDistribution()) {
                    if (damageAssignments == null || damageAssignments.isEmpty()) {
                        throw new IllegalStateException("Damage assignments required");
                    }
                    int totalDamage = damageAssignments.values().stream().mapToInt(Integer::intValue).sum();
                    if (totalDamage != effectiveXValue) {
                        throw new IllegalStateException("Damage assignments must sum to X (" + effectiveXValue + ")");
                    }
                    for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                        Permanent target = gameHelper.findPermanentById(gameData, assignment.getKey());
                        if (target == null || !isCreature(gameData, target) || !target.isAttacking()) {
                            throw new IllegalStateException("All targets must be attacking creatures");
                        }
                        if (assignment.getValue() <= 0) {
                            throw new IllegalStateException("Each damage assignment must be positive");
                        }
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.INSTANT_SPELL, card, playerId, card.getName(),
                            new ArrayList<>(card.getEffects(EffectSlot.SPELL)), effectiveXValue, null, damageAssignments
                    ));
                } else if (card.isNeedsSpellTarget()) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.INSTANT_SPELL, card, playerId, card.getName(),
                            new ArrayList<>(card.getEffects(EffectSlot.SPELL)), targetPermanentId, TargetZone.STACK
                    ));
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.INSTANT_SPELL, card, playerId, card.getName(),
                            new ArrayList<>(card.getEffects(EffectSlot.SPELL)), effectiveXValue, targetPermanentId, null
                    ));
                }
                finishSpellCast(gameData, playerId, player, hand, card);
            }
        }
    }

    private void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue) {
        if (card.getManaCost() == null) return;
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int additionalCost = gameHelper.getOpponentCostIncrease(gameData, playerId, card.getType());
        if (cost.hasX()) {
            cost.pay(pool, effectiveXValue + additionalCost);
        } else {
            cost.pay(pool, additionalCost);
        }

    }

    private void finishSpellCast(GameData gameData, UUID playerId, Player player, List<Card> hand, Card card) {
        gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
        gameData.priorityPassedBy.clear();

        String logEntry = player.getUsername() + " casts " + card.getName() + ".";
        gameHelper.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

        gameHelper.checkSpellCastTriggers(gameData, card);
        gameHelper.broadcastGameState(gameData);
        resolveAutoPass(gameData);
    }

    public void tapPermanent(GameData gameData, Player player, int permanentIndex) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
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
            if (permanent.isSummoningSick() && isCreature(gameData, permanent)) {
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
            gameHelper.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} taps {}", gameData.id, player.getUsername(), permanent.getCard().getName());

            gameHelper.broadcastGameState(gameData);
        }
    }

    public void sacrificePermanent(GameData gameData, Player player, int permanentIndex, UUID targetPermanentId) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
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
                    Permanent target = gameHelper.findPermanentById(gameData, targetPermanentId);
                    if (target == null) {
                        throw new IllegalStateException("Invalid target permanent");
                    }
                    if (!destroy.targetTypes().contains(target.getCard().getType())) {
                        throw new IllegalStateException("Invalid target type for sacrifice ability");
                    }
                    if (gameHelper.hasProtectionFrom(gameData, target, permanent.getCard().getColor())) {
                        throw new IllegalStateException(target.getCard().getName() + " has protection from " + permanent.getCard().getColor().name().toLowerCase());
                    }
                }
            }

            // Sacrifice: remove from battlefield, add to graveyard
            boolean wasCreature = isCreature(gameData, permanent);
            battlefield.remove(permanentIndex);
            gameData.playerGraveyards.get(playerId).add(permanent.getOriginalCard());
            gameHelper.collectDeathTrigger(gameData, permanent.getCard(), playerId, wasCreature);
            gameHelper.removeOrphanedAuras(gameData);

            String logEntry = player.getUsername() + " sacrifices " + permanent.getCard().getName() + ".";
            gameHelper.logAndBroadcast(gameData, logEntry);
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
                gameHelper.processNextMayAbility(gameData);
            }
            gameHelper.broadcastGameState(gameData);
        }
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetPermanentId, TargetZone targetZone) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            UUID playerId = player.getId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
                throw new IllegalStateException("Invalid permanent index");
            }

            Permanent permanent = battlefield.get(permanentIndex);
            List<ActivatedAbility> abilities = permanent.getCard().getActivatedAbilities();
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

            // Validate tap requirement
            if (isTapAbility) {
                if (permanent.isTapped()) {
                    throw new IllegalStateException("Permanent is already tapped");
                }
                if (permanent.isSummoningSick() && isCreature(gameData, permanent)) {
                    throw new IllegalStateException("Creature has summoning sickness");
                }
            }

            // Validate spell target for abilities that counter spells
            if (ability.isNeedsSpellTarget()) {
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

            // Pay mana cost
            if (abilityCost != null) {
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

            // Validate target for effects that need one
            for (CardEffect effect : abilityEffects) {
                if (effect instanceof DealXDamageToTargetCreatureEffect) {
                    if (targetPermanentId == null) {
                        throw new IllegalStateException("Ability requires a target");
                    }
                    Permanent target = gameHelper.findPermanentById(gameData, targetPermanentId);
                    if (target == null) {
                        throw new IllegalStateException("Invalid target permanent");
                    }
                    if (!isCreature(gameData, target)) {
                        throw new IllegalStateException("Target must be a creature");
                    }
                    if (gameHelper.hasProtectionFrom(gameData, target, permanent.getCard().getColor())) {
                        throw new IllegalStateException(target.getCard().getName() + " has protection from " + permanent.getCard().getColor().name().toLowerCase());
                    }
                }
                if (effect instanceof TapTargetPermanentEffect tapEffect) {
                    if (targetPermanentId == null) {
                        throw new IllegalStateException("Ability requires a target");
                    }
                    Permanent target = gameHelper.findPermanentById(gameData, targetPermanentId);
                    if (target == null) {
                        throw new IllegalStateException("Invalid target permanent");
                    }
                    if (!tapEffect.allowedTypes().contains(target.getCard().getType())) {
                        throw new IllegalStateException("Target must be an artifact, creature, or land");
                    }
                }
                if (effect instanceof MillTargetPlayerEffect) {
                    if (targetPermanentId == null) {
                        throw new IllegalStateException("Ability requires a target player");
                    }
                    if (!gameData.playerIds.contains(targetPermanentId)) {
                        throw new IllegalStateException("Target must be a player");
                    }
                }
                if (effect instanceof RevealTopCardOfLibraryEffect) {
                    if (targetPermanentId == null) {
                        throw new IllegalStateException("Ability requires a target player");
                    }
                    if (!gameData.playerIds.contains(targetPermanentId)) {
                        throw new IllegalStateException("Target must be a player");
                    }
                }
                if (effect instanceof GainControlOfEnchantedTargetEffect) {
                    if (targetPermanentId == null) {
                        throw new IllegalStateException("Ability requires a target");
                    }
                    Permanent target = gameHelper.findPermanentById(gameData, targetPermanentId);
                    if (target == null) {
                        throw new IllegalStateException("Invalid target permanent");
                    }
                    if (!isCreature(gameData, target)) {
                        throw new IllegalStateException("Target must be a creature");
                    }
                }
                if (effect instanceof ReturnAuraFromGraveyardToBattlefieldEffect) {
                    if (targetZone != TargetZone.GRAVEYARD) {
                        throw new IllegalStateException("Ability requires a graveyard target");
                    }
                    if (targetPermanentId == null) {
                        throw new IllegalStateException("Ability requires a target Aura card");
                    }
                    Card graveyardCard = gameHelper.findCardInGraveyardById(gameData, targetPermanentId);
                    if (graveyardCard == null) {
                        throw new IllegalStateException("Target card not found in any graveyard");
                    }
                    if (!graveyardCard.isAura()) {
                        throw new IllegalStateException("Target card must be an Aura");
                    }
                }
            }

            // Generic target filter validation
            if (ability.getTargetFilter() != null && targetPermanentId != null) {
                Permanent target = gameHelper.findPermanentById(gameData, targetPermanentId);
                if (target != null) {
                    gameHelper.validateTargetFilter(ability.getTargetFilter(), target);
                }
            }

            // Creature shroud validation for abilities
            if (targetPermanentId != null) {
                Permanent shroudTarget = gameHelper.findPermanentById(gameData, targetPermanentId);
                if (shroudTarget != null && hasKeyword(gameData, shroudTarget, Keyword.SHROUD)) {
                    throw new IllegalStateException(shroudTarget.getCard().getName() + " has shroud and can't be targeted");
                }
            }

            // Player shroud validation for abilities
            if (targetPermanentId != null && gameData.playerIds.contains(targetPermanentId)
                    && gameHelper.playerHasShroud(gameData, targetPermanentId)) {
                throw new IllegalStateException(gameData.playerIdToName.get(targetPermanentId) + " has shroud and can't be targeted");
            }

            // Self-target if effects need the source permanent
            UUID effectiveTargetId = targetPermanentId;
            if (effectiveTargetId == null) {
                boolean needsSelfTarget = abilityEffects.stream().anyMatch(e ->
                        e instanceof RegenerateEffect || e instanceof BoostSelfEffect || e instanceof UntapSelfEffect);
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
                battlefield.remove(permanent);
                gameData.playerGraveyards.get(playerId).add(permanent.getCard());
            }

            String logEntry = player.getUsername() + " activates " + permanent.getCard().getName() + "'s ability.";
            gameHelper.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} activates {}'s ability", gameData.id, player.getUsername(), permanent.getCard().getName());

            // Snapshot permanent state into effects so the ability resolves independently of its source
            // Filter out SacrificeSelfCost since it's already been paid as a cost
            List<CardEffect> snapshotEffects = new ArrayList<>();
            for (CardEffect effect : abilityEffects) {
                if (effect instanceof SacrificeSelfCost) {
                    continue;
                }
                if (effect instanceof PreventNextColorDamageToControllerEffect && permanent.getChosenColor() != null) {
                    snapshotEffects.add(new PreventNextColorDamageToControllerEffect(permanent.getChosenColor()));
                } else {
                    snapshotEffects.add(effect);
                }
            }

            // Push activated ability on stack
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
            gameData.priorityPassedBy.clear();
            gameHelper.broadcastGameState(gameData);
        }
    }

    public void setAutoStops(GameData gameData, Player player, List<TurnStep> stops) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            Set<TurnStep> stopSet = ConcurrentHashMap.newKeySet();
            stopSet.addAll(stops);
            stopSet.add(TurnStep.PRECOMBAT_MAIN);
            stopSet.add(TurnStep.POSTCOMBAT_MAIN);
            gameData.playerAutoStopSteps.put(player.getId(), stopSet);
            gameHelper.broadcastGameState(gameData);
        }
    }

    public void handleColorChosen(GameData gameData, Player player, String colorName) {
        synchronized (gameData) {
            if (gameData.awaitingInput != AwaitingInput.COLOR_CHOICE) {
                throw new IllegalStateException("Not awaiting color choice");
            }
            if (!player.getId().equals(gameData.awaitingColorChoicePlayerId)) {
                throw new IllegalStateException("Not your turn to choose");
            }

            // Text-changing effects (Mind Bend, etc.) — two-step color/land-type choice
            if (gameData.colorChoiceContext instanceof ColorChoiceContext.TextChangeFromWord ctx) {
                handleTextChangeFromWordChosen(gameData, player, colorName, ctx);
                return;
            }
            if (gameData.colorChoiceContext instanceof ColorChoiceContext.TextChangeToWord ctx) {
                handleTextChangeToWordChosen(gameData, player, colorName, ctx);
                return;
            }

            CardColor color = CardColor.valueOf(colorName);
            UUID permanentId = gameData.awaitingColorChoicePermanentId;
            UUID etbTargetId = gameData.pendingColorChoiceETBTargetId;

            gameData.awaitingInput = null;
            gameData.awaitingColorChoicePlayerId = null;
            gameData.awaitingColorChoicePermanentId = null;
            gameData.pendingColorChoiceETBTargetId = null;

            Permanent perm = gameHelper.findPermanentById(gameData, permanentId);
            if (perm != null) {
                perm.setChosenColor(color);

                String logEntry = player.getUsername() + " chooses " + color.name().toLowerCase() + " for " + perm.getCard().getName() + ".";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), color, perm.getCard().getName());

                if (isCreature(gameData, perm)) {
                    gameHelper.processCreatureETBEffects(gameData, player.getId(), perm.getCard(), etbTargetId);
                }
            }

            gameData.priorityPassedBy.clear();
            gameHelper.broadcastGameState(gameData);
            resolveAutoPass(gameData);
        }
    }

    private void handleTextChangeFromWordChosen(GameData gameData, Player player, String chosenWord, ColorChoiceContext.TextChangeFromWord ctx) {
        boolean isColor = GameHelper.TEXT_CHANGE_COLOR_WORDS.contains(chosenWord);
        boolean isLandType = GameHelper.TEXT_CHANGE_LAND_TYPES.contains(chosenWord);
        if (!isColor && !isLandType) {
            throw new IllegalArgumentException("Invalid choice: " + chosenWord);
        }

        gameData.colorChoiceContext = new ColorChoiceContext.TextChangeToWord(ctx.targetPermanentId(), chosenWord, isColor);

        List<String> remainingOptions;
        String promptType;
        if (isColor) {
            remainingOptions = GameHelper.TEXT_CHANGE_COLOR_WORDS.stream().filter(c -> !c.equals(chosenWord)).toList();
            promptType = "color word";
        } else {
            remainingOptions = GameHelper.TEXT_CHANGE_LAND_TYPES.stream().filter(t -> !t.equals(chosenWord)).toList();
            promptType = "basic land type";
        }

        sessionManager.sendToPlayer(player.getId(), new ChooseColorMessage(remainingOptions, "Choose the replacement " + promptType + "."));
        log.info("Game {} - Awaiting {} to choose replacement word for text change", gameData.id, player.getUsername());
    }

    private void handleTextChangeToWordChosen(GameData gameData, Player player, String chosenWord, ColorChoiceContext.TextChangeToWord ctx) {
        if (ctx.isColor()) {
            if (!GameHelper.TEXT_CHANGE_COLOR_WORDS.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid color choice: " + chosenWord);
            }
        } else {
            if (!GameHelper.TEXT_CHANGE_LAND_TYPES.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid land type choice: " + chosenWord);
            }
        }

        gameData.awaitingInput = null;
        gameData.awaitingColorChoicePlayerId = null;
        gameData.colorChoiceContext = null;

        Permanent target = gameHelper.findPermanentById(gameData, ctx.targetPermanentId());
        if (target != null) {
            String fromText = textChangeChoiceToWord(ctx.fromWord());
            String toText = textChangeChoiceToWord(chosenWord);
            target.getTextReplacements().add(new TextReplacement(fromText, toText));

            // If the permanent has a chosenColor matching the from-color, update it
            if (ctx.isColor()) {
                CardColor fromColor = CardColor.valueOf(ctx.fromWord());
                CardColor toColor = CardColor.valueOf(chosenWord);
                if (fromColor.equals(target.getChosenColor())) {
                    target.setChosenColor(toColor);
                }
            }

            String logEntry = player.getUsername() + " changes all instances of " + fromText + " to " + toText + " on " + target.getCard().getName() + ".";
            gameHelper.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} changes {} to {} on {}", gameData.id, player.getUsername(), fromText, toText, target.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameHelper.broadcastGameState(gameData);
        resolveAutoPass(gameData);
    }

    private String textChangeChoiceToWord(String choice) {
        return switch (choice) {
            case "WHITE" -> "white";
            case "BLUE" -> "blue";
            case "BLACK" -> "black";
            case "RED" -> "red";
            case "GREEN" -> "green";
            case "PLAINS" -> "Plains";
            case "ISLAND" -> "Island";
            case "SWAMP" -> "Swamp";
            case "MOUNTAIN" -> "Mountain";
            case "FOREST" -> "Forest";
            default -> throw new IllegalArgumentException("Invalid choice: " + choice);
        };
    }

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            if (gameData.awaitingInput == AwaitingInput.DISCARD_CHOICE) {
                handleDiscardCardChosen(gameData, player, cardIndex);
                return;
            }

            if (gameData.awaitingInput == AwaitingInput.REVEALED_HAND_CHOICE) {
                handleRevealedHandCardChosen(gameData, player, cardIndex);
                return;
            }

            if (gameData.awaitingInput != AwaitingInput.CARD_CHOICE && gameData.awaitingInput != AwaitingInput.TARGETED_CARD_CHOICE) {
                throw new IllegalStateException("Not awaiting card choice");
            }
            if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
                throw new IllegalStateException("Not your turn to choose");
            }

            UUID playerId = player.getId();
            Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
            boolean isTargeted = gameData.awaitingInput == AwaitingInput.TARGETED_CARD_CHOICE;

            gameData.awaitingInput = null;
            gameData.awaitingCardChoicePlayerId = null;
            gameData.awaitingCardChoiceValidIndices = null;

            UUID targetPermanentId = gameData.pendingCardChoiceTargetPermanentId;
            gameData.pendingCardChoiceTargetPermanentId = null;

            if (cardIndex == -1) {
                String logEntry = player.getUsername() + " chooses not to put a card onto the battlefield.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines to put a card onto the battlefield", gameData.id, player.getUsername());
            } else {
                if (!validIndices.contains(cardIndex)) {
                    throw new IllegalStateException("Invalid card index: " + cardIndex);
                }

                List<Card> hand = gameData.playerHands.get(playerId);
                Card card = hand.remove(cardIndex);

                if (isTargeted) {
                    resolveTargetedCardChoice(gameData, player, playerId, hand, card, targetPermanentId);
                } else {
                    resolveUntargetedCardChoice(gameData, player, playerId, hand, card);
                }
            }

            resolveAutoPass(gameData);
        }
    }

    private void handleDiscardCardChosen(GameData gameData, Player player, int cardIndex) {
        if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        gameData.playerGraveyards.get(playerId).add(card);





        String logEntry = player.getUsername() + " discards " + card.getName() + ".";
        gameHelper.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} discards {}", gameData.id, player.getUsername(), card.getName());

        gameData.awaitingDiscardRemainingCount--;

        if (gameData.awaitingDiscardRemainingCount > 0 && !hand.isEmpty()) {
            gameHelper.beginDiscardChoice(gameData, playerId);
        } else {
            gameData.awaitingInput = null;
            gameData.awaitingCardChoicePlayerId = null;
            gameData.awaitingCardChoiceValidIndices = null;
            gameData.awaitingDiscardRemainingCount = 0;
            resolveAutoPass(gameData);
        }
    }

    private void handleRevealedHandCardChosen(GameData gameData, Player player, int cardIndex) {
        if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID targetPlayerId = gameData.awaitingRevealedHandChoiceTargetPlayerId;
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        Card chosenCard = targetHand.remove(cardIndex);
        gameData.awaitingRevealedHandChosenCards.add(chosenCard);

        String logEntry = player.getUsername() + " chooses " + chosenCard.getName() + " from " + targetName + "'s hand.";
        gameHelper.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} chooses {} from {}'s hand", gameData.id, player.getUsername(), chosenCard.getName(), targetName);

        gameData.awaitingRevealedHandChoiceRemainingCount--;

        if (gameData.awaitingRevealedHandChoiceRemainingCount > 0 && !targetHand.isEmpty()) {
            // More cards to choose — update valid indices and prompt again
            List<Integer> newValidIndices = new ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                newValidIndices.add(i);
            }

            gameHelper.beginRevealedHandChoice(gameData, player.getId(), targetPlayerId, newValidIndices,
                    "Choose another card to put on top of " + targetName + "'s library.");
        } else {
            // All cards chosen — put them on top of library
            gameData.awaitingInput = null;
            gameData.awaitingCardChoicePlayerId = null;
            gameData.awaitingCardChoiceValidIndices = null;

            List<Card> deck = gameData.playerDecks.get(targetPlayerId);
            List<Card> chosenCards = new ArrayList<>(gameData.awaitingRevealedHandChosenCards);

            // Insert in reverse order so first chosen ends up on top
            for (int i = chosenCards.size() - 1; i >= 0; i--) {
                deck.addFirst(chosenCards.get(i));
            }

            String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
            String putLog = player.getUsername() + " puts " + cardNames + " on top of " + targetName + "'s library.";
            gameHelper.logAndBroadcast(gameData, putLog);
            log.info("Game {} - {} puts {} on top of {}'s library", gameData.id, player.getUsername(), cardNames, targetName);

            gameData.awaitingRevealedHandChoiceTargetPlayerId = null;
            gameData.awaitingRevealedHandChoiceRemainingCount = 0;
            gameData.awaitingRevealedHandChosenCards.clear();

            resolveAutoPass(gameData);
        }
    }

    private void resolveTargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card, UUID targetPermanentId) {
        Permanent target = gameHelper.findPermanentById(gameData, targetPermanentId);
        if (target != null) {
            Permanent auraPerm = new Permanent(card);
            auraPerm.setAttachedTo(target.getId());
            gameData.playerBattlefields.get(playerId).add(auraPerm);

            String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield attached to " + target.getCard().getName() + ".";
            gameHelper.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} puts {} onto the battlefield attached to {}", gameData.id, player.getUsername(), card.getName(), target.getCard().getName());
        } else {
            hand.add(card);
            String logEntry = card.getName() + " can't be attached (target left the battlefield).";
            gameHelper.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Aura target gone, {} returned to hand", gameData.id, card.getName());
        }
    }

    private void resolveUntargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card) {
        gameData.playerBattlefields.get(playerId).add(new Permanent(card));

        String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield.";
        gameHelper.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts {} onto the battlefield", gameData.id, player.getUsername(), card.getName());

        gameHelper.handleCreatureEnteredBattlefield(gameData, playerId, card, null);
    }

    public void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        synchronized (gameData) {
            if (gameData.awaitingInput != AwaitingInput.PERMANENT_CHOICE) {
                throw new IllegalStateException("Not awaiting permanent choice");
            }
            if (!player.getId().equals(gameData.awaitingPermanentChoicePlayerId)) {
                throw new IllegalStateException("Not your turn to choose");
            }

            UUID playerId = player.getId();
            Set<UUID> validIds = gameData.awaitingPermanentChoiceValidIds;

            gameData.awaitingInput = null;
            gameData.awaitingPermanentChoicePlayerId = null;
            gameData.awaitingPermanentChoiceValidIds = null;

            if (!validIds.contains(permanentId)) {
                throw new IllegalStateException("Invalid permanent: " + permanentId);
            }

            PermanentChoiceContext context = gameData.permanentChoiceContext;
            gameData.permanentChoiceContext = null;

            if (context instanceof PermanentChoiceContext.CloneCopy cloneCopy) {
                Permanent clonePerm = gameHelper.findPermanentById(gameData, cloneCopy.clonePermanentId());
                if (clonePerm == null) {
                    throw new IllegalStateException("Clone permanent no longer exists");
                }

                Permanent targetPerm = gameHelper.findPermanentById(gameData, permanentId);
                if (targetPerm == null) {
                    throw new IllegalStateException("Target creature no longer exists");
                }

                gameHelper.applyCloneCopy(clonePerm, targetPerm);

                String logEntry = "Clone enters as a copy of " + targetPerm.getCard().getName() + ".";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - Clone copies {}", gameData.id, targetPerm.getCard().getName());

    

                // Check legend rule (Clone may have copied a legendary creature)
                if (!gameHelper.checkLegendRule(gameData, playerId)) {
                    gameHelper.performStateBasedActions(gameData);
        
                    resolveAutoPass(gameData);
                }
            } else if (context instanceof PermanentChoiceContext.AuraGraft auraGraft) {
                Permanent aura = gameHelper.findPermanentById(gameData, auraGraft.auraPermanentId());
                if (aura == null) {
                    throw new IllegalStateException("Aura permanent no longer exists");
                }

                Permanent newTarget = gameHelper.findPermanentById(gameData, permanentId);
                if (newTarget == null) {
                    throw new IllegalStateException("Target permanent no longer exists");
                }

                aura.setAttachedTo(permanentId);

                String logEntry = aura.getCard().getName() + " is now attached to " + newTarget.getCard().getName() + ".";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(), newTarget.getCard().getName());

    
    

                resolveAutoPass(gameData);
            } else if (context instanceof PermanentChoiceContext.LegendRule legendRule) {
                // Legend rule: keep chosen permanent, move all others with the same name to graveyard
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                List<Permanent> toRemove = new ArrayList<>();
                for (Permanent perm : battlefield) {
                    if (perm.getCard().getName().equals(legendRule.cardName()) && !perm.getId().equals(permanentId)) {
                        toRemove.add(perm);
                    }
                }
                for (Permanent perm : toRemove) {
                    boolean wasCreature = isCreature(gameData, perm);
                    battlefield.remove(perm);
                    gameData.playerGraveyards.get(playerId).add(perm.getOriginalCard());
                    gameHelper.collectDeathTrigger(gameData, perm.getCard(), playerId, wasCreature);
                    String logEntry = perm.getCard().getName() + " is put into the graveyard (legend rule).";
                    gameHelper.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} sent to graveyard by legend rule", gameData.id, perm.getCard().getName());
                }

                gameHelper.removeOrphanedAuras(gameData);
    
    
    

                resolveAutoPass(gameData);
            } else if (context instanceof PermanentChoiceContext.BounceCreature bounceCreature) {
                Permanent target = gameHelper.findPermanentById(gameData, permanentId);
                if (target == null) {
                    throw new IllegalStateException("Target creature no longer exists");
                }

                UUID bouncingPlayerId = bounceCreature.bouncingPlayerId();
                List<Permanent> battlefield = gameData.playerBattlefields.get(bouncingPlayerId);
                if (battlefield != null && battlefield.remove(target)) {
                    gameHelper.removeOrphanedAuras(gameData);
                    UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), bouncingPlayerId);
                    gameData.stolenCreatures.remove(target.getId());
                    List<Card> hand = gameData.playerHands.get(ownerId);
                    hand.add(target.getOriginalCard());

                    String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
                    gameHelper.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returned to owner's hand by Sunken Hope", gameData.id, target.getCard().getName());
                }

                gameHelper.performStateBasedActions(gameData);

                resolveAutoPass(gameData);
            } else if (gameData.pendingAuraCard != null) {
                Card auraCard = gameData.pendingAuraCard;
                gameData.pendingAuraCard = null;

                Permanent creatureTarget = gameHelper.findPermanentById(gameData, permanentId);
                if (creatureTarget == null) {
                    throw new IllegalStateException("Target creature no longer exists");
                }

                // Create Aura permanent attached to the creature, under controller's control
                Permanent auraPerm = new Permanent(auraCard);
                auraPerm.setAttachedTo(creatureTarget.getId());
                gameData.playerBattlefields.get(playerId).add(auraPerm);

                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = auraCard.getName() + " enters the battlefield from graveyard attached to " + creatureTarget.getCard().getName() + " under " + playerName + "'s control.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned {} from graveyard to battlefield attached to {}",
                        gameData.id, playerName, auraCard.getName(), creatureTarget.getCard().getName());

    
    
    

                resolveAutoPass(gameData);
            } else {
                throw new IllegalStateException("No pending permanent choice context");
            }
        }
    }

    public void handleGraveyardCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            if (gameData.awaitingInput != AwaitingInput.GRAVEYARD_CHOICE) {
                throw new IllegalStateException("Not awaiting graveyard choice");
            }
            if (!player.getId().equals(gameData.awaitingGraveyardChoicePlayerId)) {
                throw new IllegalStateException("Not your turn to choose");
            }

            UUID playerId = player.getId();
            Set<Integer> validIndices = gameData.awaitingGraveyardChoiceValidIndices;

            gameData.awaitingInput = null;
            gameData.awaitingGraveyardChoicePlayerId = null;
            gameData.awaitingGraveyardChoiceValidIndices = null;
            GraveyardChoiceDestination destination = gameData.graveyardChoiceDestination;
            gameData.graveyardChoiceDestination = null;

            if (cardIndex == -1) {
                // Player declined
                String logEntry = player.getUsername() + " chooses not to return a card.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines to return a card from graveyard", gameData.id, player.getUsername());
            } else {
                if (!validIndices.contains(cardIndex)) {
                    throw new IllegalStateException("Invalid card index: " + cardIndex);
                }

                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                Card card = graveyard.remove(cardIndex);

                switch (destination) {
                    case HAND -> {
                        gameData.playerHands.get(playerId).add(card);

                        String logEntry = player.getUsername() + " returns " + card.getName() + " from graveyard to hand.";
                        gameHelper.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} returns {} from graveyard to hand", gameData.id, player.getUsername(), card.getName());

            

            
                    }
                    case BATTLEFIELD -> {
                        Permanent perm = new Permanent(card);
                        gameData.playerBattlefields.get(playerId).add(perm);

                        String logEntry = player.getUsername() + " returns " + card.getName() + " from graveyard to the battlefield.";
                        gameHelper.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} returns {} from graveyard to battlefield", gameData.id, player.getUsername(), card.getName());

            
            

                        gameHelper.handleCreatureEnteredBattlefield(gameData, playerId, card, null);
                        if (gameData.awaitingInput == null) {
                            gameHelper.checkLegendRule(gameData, playerId);
                        }
                    }
                }
            }

            resolveAutoPass(gameData);
        }
    }

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        synchronized (gameData) {
            if (gameData.awaitingInput != AwaitingInput.MULTI_PERMANENT_CHOICE) {
                throw new IllegalStateException("Not awaiting multi-permanent choice");
            }
            if (!player.getId().equals(gameData.awaitingMultiPermanentChoicePlayerId)) {
                throw new IllegalStateException("Not your turn to choose");
            }

            UUID playerId = player.getId();
            Set<UUID> validIds = gameData.awaitingMultiPermanentChoiceValidIds;
            int maxCount = gameData.awaitingMultiPermanentChoiceMaxCount;

            gameData.awaitingInput = null;
            gameData.awaitingMultiPermanentChoicePlayerId = null;
            gameData.awaitingMultiPermanentChoiceValidIds = null;
            gameData.awaitingMultiPermanentChoiceMaxCount = 0;

            if (permanentIds == null) {
                permanentIds = List.of();
            }

            if (permanentIds.size() > maxCount) {
                throw new IllegalStateException("Too many permanents selected: " + permanentIds.size() + " > " + maxCount);
            }

            // Validate no duplicates
            Set<UUID> uniqueIds = new HashSet<>(permanentIds);
            if (uniqueIds.size() != permanentIds.size()) {
                throw new IllegalStateException("Duplicate permanent IDs in selection");
            }

            for (UUID permId : permanentIds) {
                if (!validIds.contains(permId)) {
                    throw new IllegalStateException("Invalid permanent: " + permId);
                }
            }

            if (gameData.pendingCombatDamageBounceTargetPlayerId != null) {
                UUID targetPlayerId = gameData.pendingCombatDamageBounceTargetPlayerId;
                gameData.pendingCombatDamageBounceTargetPlayerId = null;

                if (permanentIds.isEmpty()) {
                    String logEntry = gameData.playerIdToName.get(playerId) + " chooses not to return any permanents.";
                    gameHelper.logAndBroadcast(gameData, logEntry);
                } else {
                    List<Permanent> targetBattlefield = gameData.playerBattlefields.get(targetPlayerId);
                    List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
                    List<String> bouncedNames = new ArrayList<>();

                    for (UUID permId : permanentIds) {
                        Permanent toReturn = null;
                        for (Permanent p : targetBattlefield) {
                            if (p.getId().equals(permId)) {
                                toReturn = p;
                                break;
                            }
                        }
                        if (toReturn != null) {
                            targetBattlefield.remove(toReturn);
                            targetHand.add(toReturn.getCard());
                            bouncedNames.add(toReturn.getCard().getName());
                        }
                    }

                    if (!bouncedNames.isEmpty()) {
                        gameHelper.removeOrphanedAuras(gameData);
                        String logEntry = String.join(", ", bouncedNames) + (bouncedNames.size() == 1 ? " is" : " are") + " returned to " + gameData.playerIdToName.get(targetPlayerId) + "'s hand.";
                        gameHelper.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} bounced {} permanents", gameData.id, gameData.playerIdToName.get(playerId), bouncedNames.size());

            

            
                    }
                }

                if (!gameData.pendingMayAbilities.isEmpty()) {
                    gameHelper.processNextMayAbility(gameData);
                    return;
                }

                advanceStep(gameData);
                resolveAutoPass(gameData);
            } else {
                throw new IllegalStateException("No pending multi-permanent choice context");
            }
        }
    }

    public void handleMultipleGraveyardCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        synchronized (gameData) {
            if (gameData.awaitingInput != AwaitingInput.MULTI_GRAVEYARD_CHOICE) {
                throw new IllegalStateException("Not awaiting multi-graveyard choice");
            }
            if (!player.getId().equals(gameData.awaitingMultiGraveyardChoicePlayerId)) {
                throw new IllegalStateException("Not your turn to choose");
            }

            Set<UUID> validIds = gameData.awaitingMultiGraveyardChoiceValidCardIds;
            int maxCount = gameData.awaitingMultiGraveyardChoiceMaxCount;

            if (cardIds == null) {
                cardIds = List.of();
            }

            if (cardIds.size() > maxCount) {
                throw new IllegalStateException("Too many cards selected: " + cardIds.size() + " > " + maxCount);
            }

            Set<UUID> uniqueIds = new HashSet<>(cardIds);
            if (uniqueIds.size() != cardIds.size()) {
                throw new IllegalStateException("Duplicate card IDs in selection");
            }

            for (UUID cardId : cardIds) {
                if (!validIds.contains(cardId)) {
                    throw new IllegalStateException("Invalid card: " + cardId);
                }
            }

            // Retrieve the pending ETB info
            Card pendingCard = gameData.pendingGraveyardTargetCard;
            UUID controllerId = gameData.pendingGraveyardTargetControllerId;
            List<CardEffect> pendingEffects = gameData.pendingGraveyardTargetEffects;

            // Clear awaiting state
            gameData.awaitingInput = null;
            gameData.awaitingMultiGraveyardChoicePlayerId = null;
            gameData.awaitingMultiGraveyardChoiceValidCardIds = null;
            gameData.awaitingMultiGraveyardChoiceMaxCount = 0;
            gameData.pendingGraveyardTargetCard = null;
            gameData.pendingGraveyardTargetControllerId = null;
            gameData.pendingGraveyardTargetEffects = null;

            // Put the ETB ability on the stack with the chosen targets
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    pendingCard,
                    controllerId,
                    pendingCard.getName() + "'s ETB ability",
                    new ArrayList<>(pendingEffects),
                    new ArrayList<>(cardIds)
            ));

            if (cardIds.isEmpty()) {
                String etbLog = pendingCard.getName() + "'s enter-the-battlefield ability triggers targeting no cards.";
                gameHelper.logAndBroadcast(gameData, etbLog);
            } else {
                List<String> targetNames = new ArrayList<>();
                for (UUID cardId : cardIds) {
                    Card card = gameHelper.findCardInGraveyardById(gameData, cardId);
                    if (card != null) {
                        targetNames.add(card.getName());
                    }
                }
                String etbLog = pendingCard.getName() + "'s enter-the-battlefield ability triggers targeting " + String.join(", ", targetNames) + ".";
                gameHelper.logAndBroadcast(gameData, etbLog);
            }
            log.info("Game {} - {} ETB ability pushed onto stack with {} graveyard targets", gameData.id, pendingCard.getName(), cardIds.size());

            resolveAutoPass(gameData);
        }
    }

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        synchronized (gameData) {
            if (gameData.awaitingInput != AwaitingInput.MAY_ABILITY_CHOICE) {
                throw new IllegalStateException("Not awaiting may ability choice");
            }
            if (!player.getId().equals(gameData.awaitingMayAbilityPlayerId)) {
                throw new IllegalStateException("Not your turn to choose");
            }

            PendingMayAbility ability = gameData.pendingMayAbilities.removeFirst();
            gameData.awaitingInput = null;
            gameData.awaitingMayAbilityPlayerId = null;

            // Counter-unless-pays — handled via the may ability system
            boolean isCounterUnlessPays = ability.effects().stream().anyMatch(e -> e instanceof CounterUnlessPaysEffect);
            if (isCounterUnlessPays) {
                handleCounterUnlessPaysChoice(gameData, player, accepted, ability);
                return;
            }

            // Clone copy creature effect — handled separately (not via the stack)
            boolean isCloneCopy = ability.effects().stream().anyMatch(e -> e instanceof CopyCreatureOnEnterEffect);
            if (isCloneCopy) {
                if (accepted) {
                    // Collect valid creature targets
                    UUID cloneId = gameData.permanentChoiceContext instanceof PermanentChoiceContext.CloneCopy cloneCopy
                            ? cloneCopy.clonePermanentId() : null;
                    List<UUID> creatureIds = new ArrayList<>();
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) continue;
                        for (Permanent p : battlefield) {
                            if (isCreature(gameData, p) && !p.getId().equals(cloneId)) {
                                creatureIds.add(p.getId());
                            }
                        }
                    }
                    gameHelper.beginPermanentChoice(gameData, ability.controllerId(), creatureIds, "Choose a creature to copy.");

                    String logEntry = player.getUsername() + " accepts — choosing a creature to copy.";
                    gameHelper.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} accepts clone copy", gameData.id, player.getUsername());
                } else {
                    gameData.permanentChoiceContext = null;
                    String logEntry = player.getUsername() + " declines to copy a creature. Clone enters as 0/0.";
                    gameHelper.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} declines clone copy", gameData.id, player.getUsername());

                    gameHelper.performStateBasedActions(gameData);
        
        
        
                    resolveAutoPass(gameData);
                }
                return;
            }

            if (accepted) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(),
                        ability.controllerId(),
                        ability.sourceCard().getName() + "'s ability",
                        new ArrayList<>(ability.effects())
                ));
    

                String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName() + "'s triggered ability goes on the stack.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
            } else {
                String logEntry = player.getUsername() + " declines " + ability.sourceCard().getName() + "'s triggered ability.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
            }

            gameHelper.processNextMayAbility(gameData);

            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameHelper.broadcastGameState(gameData);
                resolveAutoPass(gameData);
            }
        }
    }

    private void handleCounterUnlessPaysChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        int amount = ability.effects().stream()
                .filter(e -> e instanceof CounterUnlessPaysEffect)
                .map(e -> ((CounterUnlessPaysEffect) e).amount())
                .findFirst().orElse(0);
        UUID targetCardId = ability.targetCardId();

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter-unless-pays target no longer on stack", gameData.id);
            gameHelper.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                gameHelper.broadcastGameState(gameData);
                resolveAutoPass(gameData);
            }
            return;
        }

        if (accepted) {
            ManaCost cost = new ManaCost("{" + amount + "}");
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            if (cost.canPay(pool)) {
                cost.pay(pool);
                String logEntry = player.getUsername() + " pays {" + amount + "}. " + targetEntry.getCard().getName() + " is not countered.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} pays {} to avoid counter", gameData.id, player.getUsername(), amount);
            } else {
                gameData.stack.remove(targetEntry);
                gameData.playerGraveyards.get(targetEntry.getControllerId()).add(targetEntry.getCard());
                String logEntry = player.getUsername() + " can't pay {" + amount + "}. " + targetEntry.getCard().getName() + " is countered.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} can't pay {} — spell countered", gameData.id, player.getUsername(), amount);
            }
        } else {
            gameData.stack.remove(targetEntry);
            gameData.playerGraveyards.get(targetEntry.getControllerId()).add(targetEntry.getCard());
            String logEntry = player.getUsername() + " declines to pay {" + amount + "}. " + targetEntry.getCard().getName() + " is countered.";
            gameHelper.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to pay {} — spell countered", gameData.id, player.getUsername(), amount);
        }

        gameHelper.performStateBasedActions(gameData);
        gameHelper.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
            gameData.priorityPassedBy.clear();
            gameHelper.broadcastGameState(gameData);
            resolveAutoPass(gameData);
        }
    }

    public void resolveAutoPass(GameData gameData) {
        for (int safety = 0; safety < 100; safety++) {
            if (gameData.awaitingInput != null) {
                gameHelper.broadcastGameState(gameData);
                return;
            }
            if (gameData.status == GameStatus.FINISHED) return;

            // When stack is non-empty, never auto-pass — players must explicitly pass
            if (!gameData.stack.isEmpty()) {
                gameHelper.broadcastGameState(gameData);
                return;
            }

            UUID priorityHolder = gameHelper.getPriorityPlayerId(gameData);

            // If no one holds priority (both already passed), advance the step
            if (priorityHolder == null) {
                advanceStep(gameData);
                continue;
            }

            List<Integer> playable = gameHelper.getPlayableCardIndices(gameData, priorityHolder);
            if (!playable.isEmpty()) {
                // Priority holder can act — stop and let them decide
                gameHelper.broadcastGameState(gameData);
                return;
            }

            // Check if current step is in the priority holder's auto-stop set
            Set<TurnStep> stopSteps = gameData.playerAutoStopSteps.get(priorityHolder);
            if (stopSteps != null && stopSteps.contains(gameData.currentStep)) {
                gameHelper.broadcastGameState(gameData);
                return;
            }

            // Priority holder has nothing to play — auto-pass for them
            String playerName = gameData.playerIdToName.get(priorityHolder);
            log.info("Game {} - Auto-passing priority for {} on step {} (no playable cards)",
                    gameData.id, playerName, gameData.currentStep);

            gameData.priorityPassedBy.add(priorityHolder);

            if (gameData.priorityPassedBy.size() >= 2) {
                advanceStep(gameData);
            } else {
                gameHelper.broadcastGameState(gameData);
            }
        }

        // Safety: if we somehow looped 100 times, broadcast current state and stop
        log.warn("Game {} - resolveAutoPass hit safety limit", gameData.id);
        gameHelper.broadcastGameState(gameData);
    }

    public void handleLibraryCardsReordered(GameData gameData, Player player, List<Integer> cardOrder) {
        synchronized (gameData) {
            if (gameData.awaitingInput != AwaitingInput.LIBRARY_REORDER) {
                throw new IllegalStateException("Not awaiting library reorder");
            }
            if (!player.getId().equals(gameData.awaitingLibraryReorderPlayerId)) {
                throw new IllegalStateException("Not your turn to reorder");
            }

            List<Card> reorderCards = gameData.awaitingLibraryReorderCards;
            int count = reorderCards.size();

            if (cardOrder.size() != count) {
                throw new IllegalStateException("Must specify order for all " + count + " cards");
            }

            // Validate that cardOrder is a permutation of 0..count-1
            Set<Integer> seen = new HashSet<>();
            for (int idx : cardOrder) {
                if (idx < 0 || idx >= count) {
                    throw new IllegalStateException("Invalid card index: " + idx);
                }
                if (!seen.add(idx)) {
                    throw new IllegalStateException("Duplicate card index: " + idx);
                }
            }

            // Apply the reorder: replace top N cards of deck with the reordered ones
            List<Card> deck = gameData.playerDecks.get(player.getId());
            for (int i = 0; i < count; i++) {
                deck.set(i, reorderCards.get(cardOrder.get(i)));
            }

            // Clear awaiting state
            gameData.awaitingInput = null;
            gameData.awaitingLibraryReorderPlayerId = null;
            gameData.awaitingLibraryReorderCards = null;

            String logMsg = player.getUsername() + " puts " + count + " cards back on top of their library.";
            gameHelper.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} reordered top {} cards", gameData.id, player.getUsername(), count);

            resolveAutoPass(gameData);
        }
    }


    public void handleLibraryCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            if (gameData.awaitingInput != AwaitingInput.LIBRARY_SEARCH) {
                throw new IllegalStateException("Not awaiting library search");
            }
            if (!player.getId().equals(gameData.awaitingLibrarySearchPlayerId)) {
                throw new IllegalStateException("Not your turn to choose");
            }

            UUID playerId = player.getId();
            List<Card> searchCards = gameData.awaitingLibrarySearchCards;

            gameData.awaitingInput = null;
            gameData.awaitingLibrarySearchPlayerId = null;
            gameData.awaitingLibrarySearchCards = null;

            List<Card> deck = gameData.playerDecks.get(playerId);

            if (cardIndex == -1) {
                // Player declined (fail to find)
                Collections.shuffle(deck);
                String logEntry = player.getUsername() + " chooses not to take a card. Library is shuffled.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines to take a basic land from library", gameData.id, player.getUsername());
            } else {
                if (cardIndex < 0 || cardIndex >= searchCards.size()) {
                    throw new IllegalStateException("Invalid card index: " + cardIndex);
                }

                Card chosenCard = searchCards.get(cardIndex);

                // Remove the chosen card from the library by identity
                boolean removed = false;
                for (int i = 0; i < deck.size(); i++) {
                    if (deck.get(i).getId().equals(chosenCard.getId())) {
                        deck.remove(i);
                        removed = true;
                        break;
                    }
                }

                if (!removed) {
                    throw new IllegalStateException("Chosen card not found in library");
                }

                gameData.playerHands.get(playerId).add(chosenCard);
                Collections.shuffle(deck);

                String logEntry = player.getUsername() + " reveals " + chosenCard.getName() + " and puts it into their hand. Library is shuffled.";
                gameHelper.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} searches library and puts {} into hand", gameData.id, player.getUsername(), chosenCard.getName());
            }

            resolveAutoPass(gameData);
        }
    }

    private void handleCombatResult(CombatResult result, GameData gameData) {
        if (result == CombatResult.ADVANCE_AND_AUTO_PASS || result == CombatResult.ADVANCE_ONLY) {
            advanceStep(gameData);
        }
        if (result == CombatResult.ADVANCE_AND_AUTO_PASS || result == CombatResult.AUTO_PASS_ONLY) {
            resolveAutoPass(gameData);
        }
    }

    // ===== Combat wrapper methods =====

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
        synchronized (gameData) {
            handleCombatResult(combatService.declareAttackers(gameData, player, attackerIndices), gameData);
        }
    }

    public void declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        synchronized (gameData) {
            handleCombatResult(combatService.declareBlockers(gameData, player, blockerAssignments), gameData);
        }
    }

    // ===== Thin delegates for test API =====

    public boolean isCreature(GameData gameData, Permanent permanent) {
        return gameHelper.isCreature(gameData, permanent);
    }

    public int getEffectivePower(GameData gameData, Permanent permanent) {
        return gameHelper.getEffectivePower(gameData, permanent);
    }

    public int getEffectiveToughness(GameData gameData, Permanent permanent) {
        return gameHelper.getEffectiveToughness(gameData, permanent);
    }

    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return gameHelper.hasKeyword(gameData, permanent, keyword);
    }
}

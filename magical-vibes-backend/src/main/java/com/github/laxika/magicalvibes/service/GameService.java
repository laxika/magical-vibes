package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.message.AutoStopsUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.networking.message.BattlefieldUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.DeckSizesUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.GameLogEntryMessage;
import com.github.laxika.magicalvibes.networking.message.GraveyardUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.networking.message.GameStartedMessage;
import com.github.laxika.magicalvibes.networking.message.HandDrawnMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.LifeUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.ManaUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.networking.message.PlayableCardsMessage;
import com.github.laxika.magicalvibes.networking.message.PriorityUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.networking.message.StackUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.StepAdvancedMessage;
import com.github.laxika.magicalvibes.networking.message.TurnChangedMessage;
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
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.BlockOnlyFlyersEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.IslandwalkEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToFlyingAndPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.AttackingOrBlockingTargetFilter;
import com.github.laxika.magicalvibes.model.filter.AttackingTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ControllerOnlyTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ExcludeSelfTargetFilter;
import com.github.laxika.magicalvibes.model.filter.MaxPowerTargetFilter;
import com.github.laxika.magicalvibes.model.filter.SpellColorTargetFilter;
import com.github.laxika.magicalvibes.model.filter.SpellTypeTargetFilter;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PlagiarizeEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.WithoutKeywordTargetFilter;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
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
    private final CardViewFactory cardViewFactory;
    private final PermanentViewFactory permanentViewFactory;
    private final StackEntryViewFactory stackEntryViewFactory;

    public void passPriority(GameData gameData, Player player) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
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
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
            }

            resolveAutoPass(gameData);
        }
    }

    private void advanceStep(GameData gameData) {
        // Process end-of-combat sacrifices when leaving END_OF_COMBAT
        if (gameData.currentStep == TurnStep.END_OF_COMBAT && !gameData.permanentsToSacrificeAtEndOfCombat.isEmpty()) {
            processEndOfCombatSacrifices(gameData);
            gameData.priorityPassedBy.clear();
            return;
        }

        gameData.priorityPassedBy.clear();
        TurnStep next = gameData.currentStep.next();

        drainManaPools(gameData);

        if (next != null) {
            gameData.currentStep = next;
            String logEntry = "Step: " + next.getDisplayName();
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Step advanced to {}", gameData.id, next);
            sessionManager.sendToPlayers(gameData.orderedPlayerIds,new StepAdvancedMessage(getPriorityPlayerId(gameData), next));

            if (gameData.status == GameStatus.FINISHED) return;

            if (next == TurnStep.UPKEEP) {
                handleUpkeepTriggers(gameData);
            } else if (next == TurnStep.DRAW) {
                handleDrawStep(gameData);
            } else if (next == TurnStep.DECLARE_ATTACKERS) {
                handleDeclareAttackersStep(gameData);
            } else if (next == TurnStep.DECLARE_BLOCKERS) {
                handleDeclareBlockersStep(gameData);
            } else if (next == TurnStep.COMBAT_DAMAGE) {
                resolveCombatDamage(gameData);
            } else if (next == TurnStep.END_OF_COMBAT) {
                clearCombatState(gameData);
            } else if (next == TurnStep.CLEANUP) {
                resetEndOfTurnModifiers(gameData);
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
                    logAndBroadcast(gameData, logEntry);
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
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} each-upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
                }
            }
        }

        if (!gameData.stack.isEmpty()) {
            broadcastStackUpdate(gameData);
        }

        processNextMayAbility(gameData);
    }

    private void handleDrawStep(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;

        // The starting player skips their draw on turn 1
        if (gameData.turnNumber == 1 && activePlayerId.equals(gameData.startingPlayerId)) {
            String logEntry = gameData.playerIdToName.get(activePlayerId) + " skips the draw (first turn).";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activePlayerId));
            return;
        }

        // Check for Plagiarize replacement effect
        UUID replacementController = gameData.drawReplacementTargetToController.get(activePlayerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(activePlayerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is replaced by Plagiarize — " + controllerName + " draws a card instead.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Plagiarize replaces {}'s draw step draw, {} draws instead",
                    gameData.id, playerName, controllerName);
            resolveDrawCard(gameData, replacementController);
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

        sessionManager.sendToPlayer(activePlayerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(activePlayerId, 0)));
        broadcastDeckSizes(gameData);

        String playerName = gameData.playerIdToName.get(activePlayerId);
        String logEntry = playerName + " draws a card.";
        logAndBroadcast(gameData, logEntry);

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
        gameData.priorityPassedBy.clear();
        gameData.landsPlayedThisTurn.clear();
        gameData.spellsCastThisTurn.clear();

        drainManaPools(gameData);

        // Untap all permanents for the new active player (skip those with "doesn't untap" effects)
        List<Permanent> battlefield = gameData.playerBattlefields.get(nextActive);
        if (battlefield != null) {
            battlefield.forEach(p -> {
                if (!hasAuraWithEffect(gameData, p, EnchantedCreatureDoesntUntapEffect.class)) {
                    p.untap();
                }
                p.setSummoningSick(false);
            });
        }
        broadcastBattlefields(gameData);

        String untapLog = nextActiveName + " untaps their permanents.";
        logAndBroadcast(gameData, untapLog);
        log.info("Game {} - {} untaps their permanents", gameData.id, nextActiveName);

        String logEntry = "Turn " + gameData.turnNumber + " begins. " + nextActiveName + "'s turn.";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, nextActiveName);
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new TurnChangedMessage(
                getPriorityPlayerId(gameData), TurnStep.first(), nextActive, gameData.turnNumber
        ));
    }

    private void broadcastLogEntry(GameData gameData, String logEntry) {
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new GameLogEntryMessage(logEntry));
    }

    private void logAndBroadcast(GameData gameData, String logEntry) {
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
    }

    private void broadcastStackUpdate(GameData gameData) {
        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new StackUpdatedMessage(gameData.stack.stream().map(stackEntryViewFactory::create).toList()));
    }

    private void resolveTopOfStack(GameData gameData) {
        if (gameData.stack.isEmpty()) return;

        StackEntry entry = gameData.stack.removeLast();
        gameData.priorityPassedBy.clear();

        if (entry.getEntryType() == StackEntryType.CREATURE_SPELL) {
            Card card = entry.getCard();
            UUID controllerId = entry.getControllerId();

            gameData.playerBattlefields.get(controllerId).add(new Permanent(card));
            broadcastBattlefields(gameData);

            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

            handleCreatureEnteredBattlefield(gameData, controllerId, card, entry.getTargetPermanentId());
            if (gameData.awaitingInput == null) {
                checkLegendRule(gameData, controllerId);
            }
        } else if (entry.getEntryType() == StackEntryType.ENCHANTMENT_SPELL) {
            Card card = entry.getCard();
            UUID controllerId = entry.getControllerId();

            // Aura fizzles if its target is no longer on the battlefield
            if (card.isAura() && entry.getTargetPermanentId() != null) {
                Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
                if (target == null) {
                    String fizzleLog = card.getName() + " fizzles (enchanted creature no longer exists).";
                    logAndBroadcast(gameData, fizzleLog);
                    gameData.playerGraveyards.get(controllerId).add(card);
                    broadcastGraveyards(gameData);
                    log.info("Game {} - {} fizzles, target {} no longer exists", gameData.id, card.getName(), entry.getTargetPermanentId());
                } else {
                    Permanent perm = new Permanent(card);
                    perm.setAttachedTo(entry.getTargetPermanentId());
                    gameData.playerBattlefields.get(controllerId).add(perm);

                    String playerName = gameData.playerIdToName.get(controllerId);
                    String logEntry = card.getName() + " enters the battlefield attached to " + target.getCard().getName() + " under " + playerName + "'s control.";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} resolves, attached to {} for {}", gameData.id, card.getName(), target.getCard().getName(), playerName);

                    // Handle control-changing auras (e.g., Persuasion)
                    boolean hasControlEffect = card.getEffects(EffectSlot.STATIC).stream()
                            .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
                    if (hasControlEffect) {
                        stealCreature(gameData, controllerId, target);
                    }

                    broadcastBattlefields(gameData);
                }
            } else {
                gameData.playerBattlefields.get(controllerId).add(new Permanent(card));
                broadcastBattlefields(gameData);

                String playerName = gameData.playerIdToName.get(controllerId);
                String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

                // Check if enchantment has "as enters" color choice
                boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .anyMatch(e -> e instanceof ChooseColorEffect);
                if (needsColorChoice) {
                    List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                    Permanent justEntered = bf.get(bf.size() - 1);
                    beginColorChoice(gameData, controllerId, justEntered.getId(), null);
                }
                if (gameData.awaitingInput == null) {
                    checkLegendRule(gameData, controllerId);
                }
            }
        } else if (entry.getEntryType() == StackEntryType.ARTIFACT_SPELL) {
            Card card = entry.getCard();
            UUID controllerId = entry.getControllerId();

            gameData.playerBattlefields.get(controllerId).add(new Permanent(card));
            broadcastBattlefields(gameData);

            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
            logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);
            if (gameData.awaitingInput == null) {
                checkLegendRule(gameData, controllerId);
            }
        } else if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == StackEntryType.SORCERY_SPELL
                || entry.getEntryType() == StackEntryType.INSTANT_SPELL) {
            // Check if targeted spell/ability fizzles due to illegal target
            boolean targetFizzled = false;
            if (entry.getTargetPermanentId() != null) {
                if (entry.getTargetZone() == TargetZone.GRAVEYARD) {
                    targetFizzled = findCardInGraveyardById(gameData, entry.getTargetPermanentId()) == null;
                } else if (entry.getTargetZone() == TargetZone.STACK) {
                    targetFizzled = gameData.stack.stream()
                            .noneMatch(se -> se.getCard().getId().equals(entry.getTargetPermanentId()));
                } else {
                    targetFizzled = findPermanentById(gameData, entry.getTargetPermanentId()) == null
                            && !gameData.playerIds.contains(entry.getTargetPermanentId());
                }
            }
            if (targetFizzled) {
                String fizzleLog = entry.getDescription() + " fizzles (target no longer exists).";
                logAndBroadcast(gameData, fizzleLog);
                log.info("Game {} - {} fizzles, target {} no longer exists",
                        gameData.id, entry.getDescription(), entry.getTargetPermanentId());

                // Fizzled spells still go to graveyard
                if (entry.getEntryType() == StackEntryType.SORCERY_SPELL
                        || entry.getEntryType() == StackEntryType.INSTANT_SPELL) {
                    gameData.playerGraveyards.get(entry.getControllerId()).add(entry.getCard());
                    broadcastGraveyards(gameData);
                }
            } else {
                String logEntry = entry.getDescription() + " resolves.";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} resolves", gameData.id, entry.getDescription());

                resolveEffects(gameData, entry);

                if (entry.getEntryType() == StackEntryType.SORCERY_SPELL
                        || entry.getEntryType() == StackEntryType.INSTANT_SPELL) {
                    boolean shuffled = entry.getEffectsToResolve().stream()
                            .anyMatch(e -> e instanceof ShuffleIntoLibraryEffect);
                    if (!shuffled) {
                        gameData.playerGraveyards.get(entry.getControllerId()).add(entry.getCard());
                        broadcastGraveyards(gameData);
                    }
                }
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            broadcastStackUpdate(gameData);
            processNextMayAbility(gameData);
            return;
        }

        broadcastStackUpdate(gameData);
        if (gameData.awaitingInput == null) {
            sessionManager.sendToPlayers(gameData.orderedPlayerIds, new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
        }
    }

    private void resolveEffects(GameData gameData, StackEntry entry) {
        for (CardEffect effect : entry.getEffectsToResolve()) {
            if (effect instanceof OpponentMayPlayCreatureEffect) {
                resolveOpponentMayPlayCreature(gameData, entry.getControllerId());
            } else if (effect instanceof GainLifeEffect gainLife) {
                resolveGainLife(gameData, entry.getControllerId(), gainLife.amount());
            } else if (effect instanceof GainLifePerGraveyardCardEffect) {
                resolveGainLifePerGraveyardCard(gameData, entry.getControllerId());
            } else if (effect instanceof DestroyAllCreaturesEffect destroy) {
                resolveDestroyAllCreatures(gameData, destroy.cannotBeRegenerated());
            } else if (effect instanceof DestroyAllEnchantmentsEffect) {
                resolveDestroyAllEnchantments(gameData);
            } else if (effect instanceof DestroyTargetPermanentEffect destroy) {
                resolveDestroyTargetPermanent(gameData, entry, destroy);
            } else if (effect instanceof DealXDamageToTargetCreatureEffect) {
                resolveDealXDamageToTargetCreature(gameData, entry);
            } else if (effect instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect) {
                resolveDealXDamageDividedAmongTargetAttackingCreatures(gameData, entry);
            } else if (effect instanceof DealDamageToFlyingAndPlayersEffect) {
                resolveDealDamageToFlyingAndPlayers(gameData, entry);
            } else if (effect instanceof BoostSelfEffect boost) {
                resolveBoostSelf(gameData, entry, boost);
            } else if (effect instanceof BoostTargetCreatureEffect boost) {
                resolveBoostTargetCreature(gameData, entry, boost);
            } else if (effect instanceof BoostTargetBlockingCreatureEffect boost) {
                resolveBoostTargetCreature(gameData, entry, new BoostTargetCreatureEffect(boost.powerBoost(), boost.toughnessBoost()));
            } else if (effect instanceof BoostAllOwnCreaturesEffect boost) {
                resolveBoostAllOwnCreatures(gameData, entry, boost);
            } else if (effect instanceof GrantKeywordToTargetEffect grant) {
                resolveGrantKeywordToTarget(gameData, entry, grant);
            } else if (effect instanceof MakeTargetUnblockableEffect) {
                resolveMakeTargetUnblockable(gameData, entry);
            } else if (effect instanceof PreventDamageToTargetEffect prevent) {
                resolvePreventDamageToTarget(gameData, entry, prevent);
            } else if (effect instanceof PreventNextDamageEffect prevent) {
                resolvePreventNextDamage(gameData, prevent);
            } else if (effect instanceof DrawCardEffect drawCard) {
                resolveDrawCards(gameData, entry.getControllerId(), drawCard.amount());
            } else if (effect instanceof DiscardCardEffect discard) {
                resolveDiscardCards(gameData, entry.getControllerId(), discard.amount());
                if (gameData.awaitingInput == AwaitingInput.DISCARD_CHOICE) {
                    break;
                }
            } else if (effect instanceof ReturnSelfToHandEffect) {
                resolveReturnSelfToHand(gameData, entry);
            } else if (effect instanceof DoubleTargetPlayerLifeEffect) {
                resolveDoubleTargetPlayerLife(gameData, entry);
            } else if (effect instanceof ShuffleIntoLibraryEffect) {
                List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
                deck.add(entry.getCard());
                Collections.shuffle(deck);
                broadcastDeckSizes(gameData);

                String shuffleLog = entry.getCard().getName() + " is shuffled into its owner's library.";
                logAndBroadcast(gameData, shuffleLog);
            } else if (effect instanceof ShuffleGraveyardIntoLibraryEffect) {
                resolveShuffleGraveyardIntoLibrary(gameData, entry);
            } else if (effect instanceof GainLifeEqualToTargetToughnessEffect) {
                resolveGainLifeEqualToTargetToughness(gameData, entry);
            } else if (effect instanceof PutTargetOnBottomOfLibraryEffect) {
                resolvePutTargetOnBottomOfLibrary(gameData, entry);
            } else if (effect instanceof DestroyBlockedCreatureAndSelfEffect) {
                resolveDestroyBlockedCreatureAndSelf(gameData, entry);
            } else if (effect instanceof SacrificeAtEndOfCombatEffect) {
                resolveSacrificeAtEndOfCombat(gameData, entry);
            } else if (effect instanceof PreventAllCombatDamageEffect) {
                resolvePreventAllCombatDamage(gameData);
            } else if (effect instanceof PreventDamageFromColorsEffect prevent) {
                resolvePreventDamageFromColors(gameData, prevent);
            } else if (effect instanceof RedirectUnblockedCombatDamageToSelfEffect) {
                resolveRedirectUnblockedCombatDamageToSelf(gameData, entry);
            } else if (effect instanceof ReturnAuraFromGraveyardToBattlefieldEffect) {
                resolveReturnAuraFromGraveyardToBattlefield(gameData, entry);
            } else if (effect instanceof CreateCreatureTokenEffect token) {
                resolveCreateCreatureToken(gameData, entry.getControllerId(), token);
            } else if (effect instanceof ReturnCreatureFromGraveyardToBattlefieldEffect) {
                resolveReturnCardFromGraveyardToZone(gameData, entry, CardType.CREATURE,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        "You may return a creature card from your graveyard to the battlefield.");
            } else if (effect instanceof ReturnArtifactFromGraveyardToHandEffect) {
                resolveReturnCardFromGraveyardToZone(gameData, entry, CardType.ARTIFACT,
                        GraveyardChoiceDestination.HAND,
                        "You may return an artifact card from your graveyard to your hand.");
            } else if (effect instanceof RegenerateEffect) {
                resolveRegenerate(gameData, entry);
            } else if (effect instanceof TapCreaturesEffect tap) {
                resolveTapCreatures(gameData, entry, tap);
            } else if (effect instanceof TapTargetCreatureEffect) {
                resolveTapTargetPermanent(gameData, entry);
            } else if (effect instanceof TapTargetPermanentEffect) {
                resolveTapTargetPermanent(gameData, entry);
            } else if (effect instanceof UntapSelfEffect) {
                resolveUntapSelf(gameData, entry);
            } else if (effect instanceof PreventNextColorDamageToControllerEffect prevent) {
                resolvePreventNextColorDamageToController(gameData, entry, prevent);
            } else if (effect instanceof PutAuraFromHandOntoSelfEffect) {
                resolvePutAuraFromHandOntoSelf(gameData, entry);
            } else if (effect instanceof MillByHandSizeEffect) {
                resolveMillByHandSize(gameData, entry);
            } else if (effect instanceof MillTargetPlayerEffect mill) {
                resolveMillTargetPlayer(gameData, entry, mill);
            } else if (effect instanceof LookAtHandEffect) {
                resolveLookAtHand(gameData, entry);
            } else if (effect instanceof RevealTopCardOfLibraryEffect) {
                resolveRevealTopCardOfLibrary(gameData, entry);
            } else if (effect instanceof GainControlOfTargetAuraEffect) {
                resolveGainControlOfTargetAura(gameData, entry);
            } else if (effect instanceof ReturnTargetPermanentToHandEffect) {
                resolveReturnTargetPermanentToHand(gameData, entry);
            } else if (effect instanceof ReturnCreaturesToOwnersHandEffect bounce) {
                resolveReturnCreaturesToOwnersHand(gameData, entry, bounce);
            } else if (effect instanceof ReturnArtifactsTargetPlayerOwnsToHandEffect) {
                resolveReturnArtifactsTargetPlayerOwnsToHand(gameData, entry);
            } else if (effect instanceof ChangeColorTextEffect) {
                resolveChangeColorText(gameData, entry);
                if (gameData.awaitingInput == AwaitingInput.COLOR_CHOICE) {
                    break;
                }
            } else if (effect instanceof CounterSpellEffect) {
                resolveCounterSpell(gameData, entry);
            } else if (effect instanceof PlagiarizeEffect) {
                resolvePlagiarize(gameData, entry);
            } else if (effect instanceof ReorderTopCardsOfLibraryEffect reorder) {
                resolveReorderTopCardsOfLibrary(gameData, entry, reorder);
                if (gameData.awaitingInput == AwaitingInput.LIBRARY_REORDER) {
                    break;
                }
            }
        }
        removeOrphanedAuras(gameData);
    }

    private UUID getPriorityPlayerId(GameData data) {
        if (data.activePlayerId == null) {
            return null;
        }
        if (!data.priorityPassedBy.contains(data.activePlayerId)) {
            return data.activePlayerId;
        }
        List<UUID> ids = new ArrayList<>(data.orderedPlayerIds);
        UUID nonActive = ids.get(0).equals(data.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!data.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }

    public JoinGame getJoinGame(GameData data, UUID playerId) {
        return toJoinGame(data, playerId);
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
                sessionManager.sendToPlayer(player.getId(), new SelectCardsToBottomMessage(cardsToBottom));

                String logEntry = player.getUsername() + " keeps their hand and must put " + cardsToBottom +
                        " card" + (cardsToBottom > 1 ? "s" : "") + " on the bottom of their library.";
                logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} kept hand, needs to bottom {} cards (mulligan count: {})", gameData.id, player.getUsername(), cardsToBottom, mulliganCount);
            } else {
                String logEntry = player.getUsername() + " keeps their hand.";
                logAndBroadcast(gameData, logEntry);

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

            sessionManager.sendToPlayer(player.getId(), new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(player.getId(), 0)));

            String logEntry = player.getUsername() + " puts " + bottomCards.size() +
                    " card" + (bottomCards.size() > 1 ? "s" : "") + " on the bottom of their library (keeping " + hand.size() + " cards).";
            logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} bottomed {} cards, hand size now {}", gameData.id, player.getUsername(), bottomCards.size(), hand.size());

            broadcastDeckSizes(gameData);
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

            sessionManager.sendToPlayer(player.getId(), new HandDrawnMessage(newHand.stream().map(cardViewFactory::create).toList(), newMulliganCount));
            sessionManager.sendToPlayers(gameData.orderedPlayerIds,new MulliganResolvedMessage(player.getUsername(), false, newMulliganCount));

            String logEntry = player.getUsername() + " takes a mulligan (mulligan #" + newMulliganCount + ").";
            logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} mulliganed (count: {})", gameData.id, player.getUsername(), newMulliganCount);
        }
    }

    private void startGame(GameData gameData) {
        gameData.status = GameStatus.RUNNING;
        gameData.activePlayerId = gameData.startingPlayerId;
        gameData.turnNumber = 1;
        gameData.currentStep = TurnStep.first();

        String logEntry1 = "Mulligan phase complete!";
        String logEntry2 = "Turn 1 begins. " + gameData.playerIdToName.get(gameData.activePlayerId) + "'s turn.";
        logAndBroadcast(gameData, logEntry1);
        logAndBroadcast(gameData, logEntry2);

        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new GameStartedMessage(
                gameData.activePlayerId, gameData.turnNumber, gameData.currentStep, getPriorityPlayerId(gameData)
        ));

        broadcastLifeTotals(gameData);

        log.info("Game {} - Game started! Turn 1 begins. Active player: {}", gameData.id, gameData.playerIdToName.get(gameData.activePlayerId));

        resolveAutoPass(gameData);
    }

    private JoinGame toJoinGame(GameData data, UUID playerId) {
        List<CardView> hand = playerId != null
                ? data.playerHands.getOrDefault(playerId, List.of()).stream().map(cardViewFactory::create).toList()
                : List.of();
        int mulliganCount = playerId != null ? data.mulliganCounts.getOrDefault(playerId, 0) : 0;
        Map<String, Integer> manaPool = getManaPool(data, playerId);
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
                getPriorityPlayerId(data),
                hand,
                mulliganCount,
                getDeckSizes(data),
                getBattlefields(data),
                manaPool,
                autoStopSteps,
                getLifeTotals(data),
                data.stack.stream().map(stackEntryViewFactory::create).toList(),
                getGraveyardViews(data)
        );
    }

    private List<Integer> getDeckSizes(GameData data) {
        List<Integer> sizes = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> deck = data.playerDecks.get(pid);
            sizes.add(deck != null ? deck.size() : 0);
        }
        return sizes;
    }

    private void broadcastDeckSizes(GameData data) {
        sessionManager.sendToPlayers(data.orderedPlayerIds, new DeckSizesUpdatedMessage(getDeckSizes(data)));
    }

    private List<List<PermanentView>> getBattlefields(GameData data) {
        List<List<PermanentView>> battlefields = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Permanent> bf = data.playerBattlefields.get(pid);
            if (bf == null) {
                battlefields.add(new ArrayList<>());
            } else {
                List<PermanentView> views = new ArrayList<>();
                for (Permanent p : bf) {
                    StaticBonus bonus = computeStaticBonus(data, p);
                    views.add(permanentViewFactory.create(p, bonus.power(), bonus.toughness(), bonus.keywords(), bonus.animatedCreature()));
                }
                battlefields.add(views);
            }
        }
        return battlefields;
    }

    private void broadcastBattlefields(GameData data) {
        sessionManager.sendToPlayers(data.orderedPlayerIds, new BattlefieldUpdatedMessage(getBattlefields(data)));
    }

    private List<List<CardView>> getGraveyardViews(GameData data) {
        List<List<CardView>> graveyards = new ArrayList<>();
        for (UUID pid : data.orderedPlayerIds) {
            List<Card> gy = data.playerGraveyards.get(pid);
            graveyards.add(gy != null ? gy.stream().map(cardViewFactory::create).toList() : new ArrayList<>());
        }
        return graveyards;
    }

    private void broadcastGraveyards(GameData data) {
        sessionManager.sendToPlayers(data.orderedPlayerIds, new GraveyardUpdatedMessage(getGraveyardViews(data)));
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            UUID playerId = player.getId();
            List<Integer> playable = getPlayableCardIndices(gameData, playerId);
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
                    int additionalCost = getOpponentCostIncrease(gameData, playerId, card.getType());
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
                Permanent target = findPermanentById(gameData, targetPermanentId);
                if (target == null && !gameData.playerIds.contains(targetPermanentId)) {
                    throw new IllegalStateException("Invalid target");
                }

                // Protection validation
                if (target != null && card.isNeedsTarget() && hasProtectionFrom(gameData, target, card.getColor())) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from " + card.getColor().name().toLowerCase());
                }

                // Creature shroud validation
                if (target != null && card.isNeedsTarget() && hasKeyword(gameData, target, Keyword.SHROUD)) {
                    throw new IllegalStateException(target.getCard().getName() + " has shroud and can't be targeted");
                }

                // Player shroud validation
                if (target == null && card.isNeedsTarget() && gameData.playerIds.contains(targetPermanentId)
                        && playerHasShroud(gameData, targetPermanentId)) {
                    throw new IllegalStateException(gameData.playerIdToName.get(targetPermanentId) + " has shroud and can't be targeted");
                }

                // Generic target filter validation for spells
                if (card.getTargetFilter() != null && target != null) {
                    validateTargetFilter(card.getTargetFilter(), target);
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

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastBattlefields(gameData);

                String logEntry = player.getUsername() + " plays " + card.getName() + ".";
                logAndBroadcast(gameData, logEntry);

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
                        Permanent target = findPermanentById(gameData, assignment.getKey());
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
        int additionalCost = getOpponentCostIncrease(gameData, playerId, card.getType());
        if (cost.hasX()) {
            cost.pay(pool, effectiveXValue + additionalCost);
        } else {
            cost.pay(pool, additionalCost);
        }
        sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));
    }

    private void finishSpellCast(GameData gameData, UUID playerId, Player player, List<Card> hand, Card card) {
        gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
        gameData.priorityPassedBy.clear();

        sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
        broadcastStackUpdate(gameData);
        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

        String logEntry = player.getUsername() + " casts " + card.getName() + ".";
        logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

        checkSpellCastTriggers(gameData, card);
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

            broadcastBattlefields(gameData);
            sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(manaPool.toMap()));

            String logEntry = player.getUsername() + " taps " + permanent.getCard().getName() + ".";
            logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} taps {}", gameData.id, player.getUsername(), permanent.getCard().getName());

            broadcastPlayableCards(gameData);
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
                    Permanent target = findPermanentById(gameData, targetPermanentId);
                    if (target == null) {
                        throw new IllegalStateException("Invalid target permanent");
                    }
                    if (!destroy.targetTypes().contains(target.getCard().getType())) {
                        throw new IllegalStateException("Invalid target type for sacrifice ability");
                    }
                    if (hasProtectionFrom(gameData, target, permanent.getCard().getColor())) {
                        throw new IllegalStateException(target.getCard().getName() + " has protection from " + permanent.getCard().getColor().name().toLowerCase());
                    }
                }
            }

            // Sacrifice: remove from battlefield, add to graveyard
            boolean wasCreature = isCreature(gameData, permanent);
            battlefield.remove(permanentIndex);
            gameData.playerGraveyards.get(playerId).add(permanent.getOriginalCard());
            collectDeathTrigger(gameData, permanent.getCard(), playerId, wasCreature);
            removeOrphanedAuras(gameData);

            String logEntry = player.getUsername() + " sacrifices " + permanent.getCard().getName() + ".";
            logAndBroadcast(gameData, logEntry);
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

            broadcastBattlefields(gameData);
            broadcastGraveyards(gameData);
            broadcastStackUpdate(gameData);

            if (!gameData.pendingMayAbilities.isEmpty()) {
                processNextMayAbility(gameData);
            } else {
                sessionManager.sendToPlayers(gameData.orderedPlayerIds, new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
            }

            broadcastPlayableCards(gameData);
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
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));
            }

            // Validate target for effects that need one
            for (CardEffect effect : abilityEffects) {
                if (effect instanceof DealXDamageToTargetCreatureEffect) {
                    if (targetPermanentId == null) {
                        throw new IllegalStateException("Ability requires a target");
                    }
                    Permanent target = findPermanentById(gameData, targetPermanentId);
                    if (target == null) {
                        throw new IllegalStateException("Invalid target permanent");
                    }
                    if (!isCreature(gameData, target)) {
                        throw new IllegalStateException("Target must be a creature");
                    }
                    if (hasProtectionFrom(gameData, target, permanent.getCard().getColor())) {
                        throw new IllegalStateException(target.getCard().getName() + " has protection from " + permanent.getCard().getColor().name().toLowerCase());
                    }
                }
                if (effect instanceof TapTargetPermanentEffect tapEffect) {
                    if (targetPermanentId == null) {
                        throw new IllegalStateException("Ability requires a target");
                    }
                    Permanent target = findPermanentById(gameData, targetPermanentId);
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
                if (effect instanceof ReturnAuraFromGraveyardToBattlefieldEffect) {
                    if (targetZone != TargetZone.GRAVEYARD) {
                        throw new IllegalStateException("Ability requires a graveyard target");
                    }
                    if (targetPermanentId == null) {
                        throw new IllegalStateException("Ability requires a target Aura card");
                    }
                    Card graveyardCard = findCardInGraveyardById(gameData, targetPermanentId);
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
                Permanent target = findPermanentById(gameData, targetPermanentId);
                if (target != null) {
                    validateTargetFilter(ability.getTargetFilter(), target);
                }
            }

            // Creature shroud validation for abilities
            if (targetPermanentId != null) {
                Permanent shroudTarget = findPermanentById(gameData, targetPermanentId);
                if (shroudTarget != null && hasKeyword(gameData, shroudTarget, Keyword.SHROUD)) {
                    throw new IllegalStateException(shroudTarget.getCard().getName() + " has shroud and can't be targeted");
                }
            }

            // Player shroud validation for abilities
            if (targetPermanentId != null && gameData.playerIds.contains(targetPermanentId)
                    && playerHasShroud(gameData, targetPermanentId)) {
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

            String logEntry = player.getUsername() + " activates " + permanent.getCard().getName() + "'s ability.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} activates {}'s ability", gameData.id, player.getUsername(), permanent.getCard().getName());

            // Snapshot permanent state into effects so the ability resolves independently of its source
            List<CardEffect> snapshotEffects = new ArrayList<>();
            for (CardEffect effect : abilityEffects) {
                if (effect instanceof PreventNextColorDamageToControllerEffect && permanent.getChosenColor() != null) {
                    snapshotEffects.add(new PreventNextColorDamageToControllerEffect(permanent.getChosenColor()));
                } else {
                    snapshotEffects.add(effect);
                }
            }

            // Push activated ability on stack
            if (targetZone != null && targetZone != TargetZone.BATTLEFIELD) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.ACTIVATED_ABILITY,
                        permanent.getCard(),
                        playerId,
                        permanent.getCard().getName() + "'s ability",
                        snapshotEffects,
                        effectiveTargetId,
                        targetZone
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

            broadcastBattlefields(gameData);
            broadcastStackUpdate(gameData);
            sessionManager.sendToPlayers(gameData.orderedPlayerIds, new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

            broadcastPlayableCards(gameData);
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
            sessionManager.sendToPlayer(player.getId(), new AutoStopsUpdatedMessage(new ArrayList<>(stopSet)));
        }
    }

    // ===== ETB effect methods =====

    private void resolveOpponentMayPlayCreature(GameData gameData, UUID controllerId) {
        UUID opponentId = getOpponentId(gameData, controllerId);
        List<Card> opponentHand = gameData.playerHands.get(opponentId);

        List<Integer> creatureIndices = new ArrayList<>();
        if (opponentHand != null) {
            for (int i = 0; i < opponentHand.size(); i++) {
                if (opponentHand.get(i).getType() == CardType.CREATURE) {
                    creatureIndices.add(i);
                }
            }
        }

        if (creatureIndices.isEmpty()) {
            String opponentName = gameData.playerIdToName.get(opponentId);
            String logEntry = opponentName + " has no creature cards in hand.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures in hand for ETB effect", gameData.id, opponentName);
            return;
        }

        String prompt = "You may put a creature card from your hand onto the battlefield.";
        beginCardChoice(gameData, opponentId, creatureIndices, prompt);
    }

    private void resolvePutAuraFromHandOntoSelf(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        // Find the Academy Researchers permanent on the battlefield
        Permanent self = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(entry.getCard().getId())) {
                    self = p;
                    break;
                }
            }
        }

        if (self == null) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (no longer on the battlefield).";
            logAndBroadcast(gameData, fizzleLog);
            log.info("Game {} - {} ETB fizzles, creature left battlefield", gameData.id, entry.getCard().getName());
            return;
        }

        // Find Aura cards in controller's hand
        List<Card> hand = gameData.playerHands.get(controllerId);
        List<Integer> auraIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).isAura()) {
                    auraIndices.add(i);
                }
            }
        }

        if (auraIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " has no Aura cards in hand.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no Auras in hand for {} ETB", gameData.id, playerName, entry.getCard().getName());
            return;
        }

        String prompt = "You may put an Aura card from your hand onto the battlefield attached to " + entry.getCard().getName() + ".";
        beginTargetedCardChoice(gameData, controllerId, auraIndices, prompt, self.getId());
    }

    private void resolveGainLife(GameData gameData, UUID controllerId, int amount) {
        Integer currentLife = gameData.playerLifeTotals.get(controllerId);
        gameData.playerLifeTotals.put(controllerId, currentLife + amount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gains " + amount + " life.";
        logAndBroadcast(gameData, logEntry);
        broadcastLifeTotals(gameData);
        log.info("Game {} - {} gains {} life", gameData.id, playerName, amount);
    }

    private void resolveGainLifePerGraveyardCard(GameData gameData, UUID controllerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int amount = graveyard != null ? graveyard.size() : 0;
        if (amount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " has no cards in their graveyard.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no graveyard cards for life gain", gameData.id, playerName);
            return;
        }
        resolveGainLife(gameData, controllerId, amount);
    }

    private void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId) {
        // "As enters" effects — require player choice before any ETB triggers fire

        // Clone / copy creature effect
        boolean needsCopyChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof CopyCreatureOnEnterEffect);
        if (needsCopyChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent clonePerm = bf.get(bf.size() - 1);
            gameData.permanentChoiceContext = new PermanentChoiceContext.CloneCopy(clonePerm.getId());

            // Collect all creatures on all battlefields (excluding Clone itself)
            List<UUID> creatureIds = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (isCreature(gameData, p) && !p.getId().equals(clonePerm.getId())) {
                        creatureIds.add(p.getId());
                    }
                }
            }

            if (!creatureIds.isEmpty()) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        card,
                        controllerId,
                        List.of(new CopyCreatureOnEnterEffect()),
                        card.getName() + " — You may have it enter as a copy of any creature on the battlefield."
                ));
                processNextMayAbility(gameData);
                return;
            } else {
                // No creatures to copy — Clone stays as 0/0
                gameData.permanentChoiceContext = null;
                performStateBasedActions(gameData);
                return;
            }
        }

        boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseColorEffect);
        if (needsColorChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            beginColorChoice(gameData, controllerId, justEntered.getId(), targetPermanentId);
            return;
        }

        processCreatureETBEffects(gameData, controllerId, card, targetPermanentId);
    }

    private void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId) {
        // 1. Self ETB effects (filter out "as enters" effects already handled)
        List<CardEffect> triggeredEffects = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> !(e instanceof ChooseColorEffect))
                .toList();
        if (!triggeredEffects.isEmpty()) {
            List<CardEffect> mayEffects = triggeredEffects.stream().filter(e -> e instanceof MayEffect).toList();
            List<CardEffect> mandatoryEffects = triggeredEffects.stream().filter(e -> !(e instanceof MayEffect)).toList();

            for (CardEffect effect : mayEffects) {
                MayEffect may = (MayEffect) effect;
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        card,
                        controllerId,
                        List.of(may.wrapped()),
                        card.getName() + " — " + may.prompt()
                ));
            }

            if (!mandatoryEffects.isEmpty()) {
                if (!card.isNeedsTarget() || targetPermanentId != null) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            controllerId,
                            card.getName() + "'s ETB ability",
                            new ArrayList<>(mandatoryEffects),
                            0,
                            targetPermanentId,
                            Map.of()
                    ));
                    String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
                    logAndBroadcast(gameData, etbLog);
                    log.info("Game {} - {} ETB ability pushed onto stack", gameData.id, card.getName());
                }
            }
        }

        // 2. Ally creature enters triggers (e.g. Angelic Chorus)
        checkAllyCreatureEntersTriggers(gameData, controllerId, card);

        // 3. Any other creature enters triggers (e.g. Soul Warden)
        checkAnyCreatureEntersTriggers(gameData, controllerId, card);
    }

    private void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof GainLifeEqualToToughnessEffect) {
                    int toughness = enteringCreature.getToughness();
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            List.of(new GainLifeEffect(toughness))
                    ));
                    String triggerLog = perm.getCard().getName() + " triggers — " +
                            gameData.playerIdToName.get(controllerId) + " will gain " + toughness + " life.";
                    logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (toughness={})",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), toughness);
                }
            }
        }
    }

    private void checkAnyCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                // "Another creature" — skip if the entering creature IS this permanent
                if (perm.getCard() == enteringCreature) continue;

                for (CardEffect effect : effects) {
                    if (effect instanceof GainLifeEffect gainLife) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                List.of(new GainLifeEffect(gainLife.amount()))
                        ));
                        String triggerLog = perm.getCard().getName() + " triggers — " +
                                gameData.playerIdToName.get(playerId) + " will gain " + gainLife.amount() + " life.";
                        logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (gain {} life)",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(), gainLife.amount());
                    }
                }
            }
        }
    }

    private void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetPermanentId) {
        gameData.awaitingInput = AwaitingInput.COLOR_CHOICE;
        gameData.awaitingColorChoicePlayerId = playerId;
        gameData.awaitingColorChoicePermanentId = permanentId;
        gameData.pendingColorChoiceETBTargetId = etbTargetPermanentId;
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        sessionManager.sendToPlayer(playerId, new ChooseColorMessage(colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
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

            Permanent perm = findPermanentById(gameData, permanentId);
            if (perm != null) {
                perm.setChosenColor(color);

                String logEntry = player.getUsername() + " chooses " + color.name().toLowerCase() + " for " + perm.getCard().getName() + ".";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), color, perm.getCard().getName());

                if (isCreature(gameData, perm)) {
                    processCreatureETBEffects(gameData, player.getId(), perm.getCard(), etbTargetId);
                }
            }

            gameData.priorityPassedBy.clear();
            broadcastBattlefields(gameData);
            broadcastStackUpdate(gameData);
            broadcastPlayableCards(gameData);
            sessionManager.sendToPlayers(gameData.orderedPlayerIds, new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
            resolveAutoPass(gameData);
        }
    }

    private static final List<String> TEXT_CHANGE_COLOR_WORDS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
    private static final List<String> TEXT_CHANGE_LAND_TYPES = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");

    private void resolveChangeColorText(GameData gameData, StackEntry entry) {
        UUID targetPermanentId = entry.getTargetPermanentId();
        Permanent target = findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            return;
        }

        gameData.colorChoiceContext = new ColorChoiceContext.TextChangeFromWord(targetPermanentId);
        gameData.awaitingInput = AwaitingInput.COLOR_CHOICE;
        gameData.awaitingColorChoicePlayerId = entry.getControllerId();

        List<String> options = new ArrayList<>();
        options.addAll(TEXT_CHANGE_COLOR_WORDS);
        options.addAll(TEXT_CHANGE_LAND_TYPES);
        sessionManager.sendToPlayer(entry.getControllerId(), new ChooseColorMessage(options, "Choose a color word or basic land type to replace."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a color word or basic land type for text change", gameData.id, playerName);
    }

    private void handleTextChangeFromWordChosen(GameData gameData, Player player, String chosenWord, ColorChoiceContext.TextChangeFromWord ctx) {
        boolean isColor = TEXT_CHANGE_COLOR_WORDS.contains(chosenWord);
        boolean isLandType = TEXT_CHANGE_LAND_TYPES.contains(chosenWord);
        if (!isColor && !isLandType) {
            throw new IllegalArgumentException("Invalid choice: " + chosenWord);
        }

        gameData.colorChoiceContext = new ColorChoiceContext.TextChangeToWord(ctx.targetPermanentId(), chosenWord, isColor);

        List<String> remainingOptions;
        String promptType;
        if (isColor) {
            remainingOptions = TEXT_CHANGE_COLOR_WORDS.stream().filter(c -> !c.equals(chosenWord)).toList();
            promptType = "color word";
        } else {
            remainingOptions = TEXT_CHANGE_LAND_TYPES.stream().filter(t -> !t.equals(chosenWord)).toList();
            promptType = "basic land type";
        }

        sessionManager.sendToPlayer(player.getId(), new ChooseColorMessage(remainingOptions, "Choose the replacement " + promptType + "."));
        log.info("Game {} - Awaiting {} to choose replacement word for text change", gameData.id, player.getUsername());
    }

    private void handleTextChangeToWordChosen(GameData gameData, Player player, String chosenWord, ColorChoiceContext.TextChangeToWord ctx) {
        if (ctx.isColor()) {
            if (!TEXT_CHANGE_COLOR_WORDS.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid color choice: " + chosenWord);
            }
        } else {
            if (!TEXT_CHANGE_LAND_TYPES.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid land type choice: " + chosenWord);
            }
        }

        gameData.awaitingInput = null;
        gameData.awaitingColorChoicePlayerId = null;
        gameData.colorChoiceContext = null;

        Permanent target = findPermanentById(gameData, ctx.targetPermanentId());
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
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} changes {} to {} on {}", gameData.id, player.getUsername(), fromText, toText, target.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        broadcastBattlefields(gameData);
        broadcastStackUpdate(gameData);
        broadcastPlayableCards(gameData);
        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
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

    private void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.awaitingInput = AwaitingInput.CARD_CHOICE;
        gameData.awaitingCardChoicePlayerId = playerId;
        gameData.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand", gameData.id, playerName);
    }

    private void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID targetPermanentId) {
        gameData.awaitingInput = AwaitingInput.TARGETED_CARD_CHOICE;
        gameData.awaitingCardChoicePlayerId = playerId;
        gameData.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        gameData.pendingCardChoiceTargetPermanentId = targetPermanentId;
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand (targeted)", gameData.id, playerName);
    }

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            if (gameData.awaitingInput == AwaitingInput.DISCARD_CHOICE) {
                handleDiscardCardChosen(gameData, player, cardIndex);
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
                logAndBroadcast(gameData, logEntry);
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

        sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
        broadcastGraveyards(gameData);

        String logEntry = player.getUsername() + " discards " + card.getName() + ".";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} discards {}", gameData.id, player.getUsername(), card.getName());

        gameData.awaitingDiscardRemainingCount--;

        if (gameData.awaitingDiscardRemainingCount > 0 && !hand.isEmpty()) {
            beginDiscardChoice(gameData, playerId);
        } else {
            gameData.awaitingInput = null;
            gameData.awaitingCardChoicePlayerId = null;
            gameData.awaitingCardChoiceValidIndices = null;
            gameData.awaitingDiscardRemainingCount = 0;
            resolveAutoPass(gameData);
        }
    }

    private void resolveTargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card, UUID targetPermanentId) {
        Permanent target = findPermanentById(gameData, targetPermanentId);
        if (target != null) {
            Permanent auraPerm = new Permanent(card);
            auraPerm.setAttachedTo(target.getId());
            gameData.playerBattlefields.get(playerId).add(auraPerm);

            sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
            broadcastBattlefields(gameData);

            String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield attached to " + target.getCard().getName() + ".";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} puts {} onto the battlefield attached to {}", gameData.id, player.getUsername(), card.getName(), target.getCard().getName());
        } else {
            hand.add(card);
            String logEntry = card.getName() + " can't be attached (target left the battlefield).";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Aura target gone, {} returned to hand", gameData.id, card.getName());
        }
    }

    private void resolveUntargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card) {
        gameData.playerBattlefields.get(playerId).add(new Permanent(card));

        sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
        broadcastBattlefields(gameData);

        String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield.";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts {} onto the battlefield", gameData.id, player.getUsername(), card.getName());

        handleCreatureEnteredBattlefield(gameData, playerId, card, null);
    }

    // ===== Instant effect methods =====

    private void resolveBoostSelf(GameData gameData, StackEntry entry, BoostSelfEffect boost) {
        Permanent self = findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.setPowerModifier(self.getPowerModifier() + boost.powerBoost());
        self.setToughnessModifier(self.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = self.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
        logAndBroadcast(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, self.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    private void resolveBoostTargetCreature(GameData gameData, StackEntry entry, BoostTargetCreatureEffect boost) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setPowerModifier(target.getPowerModifier() + boost.powerBoost());
        target.setToughnessModifier(target.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = target.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
        logAndBroadcast(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, target.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    private void resolveBoostAllOwnCreatures(GameData gameData, StackEntry entry, BoostAllOwnCreaturesEffect boost) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (isCreature(gameData, permanent)) {
                permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
                permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " gives +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " to " + count + " creature(s) until end of turn.";
        logAndBroadcast(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} boosts {} creatures +{}/+{}", gameData.id, entry.getCard().getName(), count, boost.powerBoost(), boost.toughnessBoost());
    }

    private void resolveGrantKeywordToTarget(GameData gameData, StackEntry entry, GrantKeywordToTargetEffect grant) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.getGrantedKeywords().add(grant.keyword());

        String keywordName = grant.keyword().name().charAt(0) + grant.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String logEntry = target.getCard().getName() + " gains " + keywordName + " until end of turn.";
        logAndBroadcast(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} gains {}", gameData.id, target.getCard().getName(), grant.keyword());
    }

    private void resolveMakeTargetUnblockable(GameData gameData, StackEntry entry) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setCantBeBlocked(true);

        String logEntry = target.getCard().getName() + " can't be blocked this turn.";
        logAndBroadcast(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} can't be blocked this turn", gameData.id, target.getCard().getName());
    }

    private void resolveDealXDamageToTargetCreature(GameData gameData, StackEntry entry) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (isDamageFromSourcePrevented(gameData, entry.getCard().getColor())
                || hasProtectionFrom(gameData, target, entry.getCard().getColor())) {
            String logEntry = entry.getCard().getName() + "'s damage is prevented.";
            logAndBroadcast(gameData, logEntry);
            return;
        }

        int damage = applyCreaturePreventionShield(gameData, target, entry.getXValue());
        String logEntry = entry.getCard().getName() + " deals " + damage + " damage to " + target.getCard().getName() + ".";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} deals {} damage to {}", gameData.id, entry.getCard().getName(), damage, target.getCard().getName());

        if (damage >= getEffectiveToughness(gameData, target)) {
            if (tryRegenerate(gameData, target)) {
                broadcastBattlefields(gameData);
            } else {
                removePermanentToGraveyard(gameData, target);
                String destroyLog = target.getCard().getName() + " is destroyed.";
                logAndBroadcast(gameData, destroyLog);
                log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
                removeOrphanedAuras(gameData);
                broadcastBattlefields(gameData);
                broadcastGraveyards(gameData);
            }
        }
    }

    private void resolveDealXDamageDividedAmongTargetAttackingCreatures(GameData gameData, StackEntry entry) {
        Map<UUID, Integer> assignments = entry.getDamageAssignments();
        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        if (isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = entry.getCard().getName() + "'s damage is prevented.";
            logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Permanent> destroyed = new ArrayList<>();

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            Permanent target = findPermanentById(gameData, assignment.getKey());
            if (target == null) {
                continue;
            }
            if (hasProtectionFrom(gameData, target, entry.getCard().getColor())) {
                continue;
            }

            int damage = applyCreaturePreventionShield(gameData, target, assignment.getValue());
            String logEntry = entry.getCard().getName() + " deals " + damage + " damage to " + target.getCard().getName() + ".";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} deals {} damage to {}", gameData.id, entry.getCard().getName(), damage, target.getCard().getName());

            if (damage >= target.getEffectiveToughness()) {
                if (!tryRegenerate(gameData, target)) {
                    destroyed.add(target);
                }
            }
        }

        for (Permanent target : destroyed) {
            removePermanentToGraveyard(gameData, target);
            String destroyLog = target.getCard().getName() + " is destroyed.";
            logAndBroadcast(gameData, destroyLog);
            log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
        }

        if (!destroyed.isEmpty()) {
            removeOrphanedAuras(gameData);
        }
        broadcastBattlefields(gameData);
        if (!destroyed.isEmpty()) {
            broadcastGraveyards(gameData);
        }
    }

    private void resolveDestroyAllCreatures(GameData gameData, boolean cannotBeRegenerated) {
        List<Permanent> toDestroy = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent perm : gameData.playerBattlefields.get(playerId)) {
                if (isCreature(gameData, perm)) {
                    toDestroy.add(perm);
                }
            }
        }

        for (Permanent perm : toDestroy) {
            if (!cannotBeRegenerated && tryRegenerate(gameData, perm)) {
                continue;
            }
            removePermanentToGraveyard(gameData, perm);
            String logEntry = perm.getCard().getName() + " is destroyed.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed", gameData.id, perm.getCard().getName());
        }

        if (!toDestroy.isEmpty()) {
            broadcastBattlefields(gameData);
            broadcastGraveyards(gameData);
        }
    }

    private void resolveDestroyAllEnchantments(GameData gameData) {
        List<Permanent> toDestroy = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent perm : gameData.playerBattlefields.get(playerId)) {
                if (perm.getCard().getType() == CardType.ENCHANTMENT) {
                    toDestroy.add(perm);
                }
            }
        }

        for (Permanent perm : toDestroy) {
            removePermanentToGraveyard(gameData, perm);
            String logEntry = perm.getCard().getName() + " is destroyed.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed", gameData.id, perm.getCard().getName());
        }

        if (!toDestroy.isEmpty()) {
            broadcastBattlefields(gameData);
            broadcastGraveyards(gameData);
        }
    }

    private void resolveDestroyTargetPermanent(GameData gameData, StackEntry entry, DestroyTargetPermanentEffect destroy) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (!destroy.targetTypes().contains(target.getCard().getType())) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (invalid target type).";
            logAndBroadcast(gameData, fizzleLog);
            log.info("Game {} - {}'s ability fizzles, target type mismatch", gameData.id, entry.getCard().getName());
            return;
        }

        // Try regeneration for creatures
        if (isCreature(gameData, target) && tryRegenerate(gameData, target)) {
            broadcastBattlefields(gameData);
            return;
        }

        removePermanentToGraveyard(gameData, target);
        String logEntry = target.getCard().getName() + " is destroyed.";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is destroyed by {}'s ability",
                gameData.id, target.getCard().getName(), entry.getCard().getName());

        removeOrphanedAuras(gameData);
        broadcastBattlefields(gameData);
        broadcastGraveyards(gameData);
    }

    private void resolveDestroyBlockedCreatureAndSelf(GameData gameData, StackEntry entry) {
        // Destroy the blocked creature (attacker) — referenced by targetPermanentId
        Permanent attacker = findPermanentById(gameData, entry.getTargetPermanentId());
        if (attacker != null && !tryRegenerate(gameData, attacker)) {
            removePermanentToGraveyard(gameData, attacker);
            String logEntry = attacker.getCard().getName() + " is destroyed by " + entry.getCard().getName() + ".";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} destroyed by {}'s block trigger", gameData.id, attacker.getCard().getName(), entry.getCard().getName());
        }

        // Destroy self (the blocker) — referenced by sourcePermanentId
        Permanent self = findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null && !tryRegenerate(gameData, self)) {
            removePermanentToGraveyard(gameData, self);
            String logEntry = entry.getCard().getName() + " is destroyed.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} destroyed (self-destruct from block trigger)", gameData.id, entry.getCard().getName());
        }

        broadcastBattlefields(gameData);
        broadcastGraveyards(gameData);
    }

    private void resolveSacrificeAtEndOfCombat(GameData gameData, StackEntry entry) {
        Permanent self = findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null) {
            gameData.permanentsToSacrificeAtEndOfCombat.add(self.getId());
            String logEntry = entry.getCard().getName() + " will be sacrificed at end of combat.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
        }
    }

    private void resolvePreventDamageToTarget(GameData gameData, StackEntry entry, PreventDamageToTargetEffect prevent) {
        UUID targetId = entry.getTargetPermanentId();

        // Check if target is a permanent
        Permanent target = findPermanentById(gameData, targetId);
        if (target != null) {
            target.setDamagePreventionShield(target.getDamagePreventionShield() + prevent.amount());

            String logEntry = "The next " + prevent.amount() + " damage that would be dealt to " + target.getCard().getName() + " is prevented.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Prevention shield {} added to permanent {}", gameData.id, prevent.amount(), target.getCard().getName());
            return;
        }

        // Check if target is a player
        if (gameData.playerIds.contains(targetId)) {
            int currentShield = gameData.playerDamagePreventionShields.getOrDefault(targetId, 0);
            gameData.playerDamagePreventionShields.put(targetId, currentShield + prevent.amount());

            String playerName = gameData.playerIdToName.get(targetId);
            String logEntry = "The next " + prevent.amount() + " damage that would be dealt to " + playerName + " is prevented.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Prevention shield {} added to player {}", gameData.id, prevent.amount(), playerName);
        }
    }

    private void resolvePreventNextDamage(GameData gameData, PreventNextDamageEffect prevent) {
        gameData.globalDamagePreventionShield += prevent.amount();

        String logEntry = "The next " + prevent.amount() + " damage that would be dealt to any permanent or player is prevented.";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Global prevention shield increased by {}", gameData.id, prevent.amount());
    }

    private int applyGlobalPreventionShield(GameData gameData, int damage) {
        int shield = gameData.globalDamagePreventionShield;
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.globalDamagePreventionShield = shield - prevented;
        return damage - prevented;
    }

    private void resolveDrawCard(GameData gameData, UUID playerId) {
        // Check for Plagiarize replacement effect
        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is replaced by Plagiarize — " + controllerName + " draws a card instead.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Plagiarize replaces {}'s draw, {} draws instead",
                    gameData.id, playerName, controllerName);
            // Draw for the replacement controller (no recursion risk: the controller
            // is a different player, and if the controller also has a replacement
            // that would be a separate entry)
            performDrawCard(gameData, replacementController);
            return;
        }

        performDrawCard(gameData, playerId);
    }

    private void performDrawCard(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);

        if (deck == null || deck.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to draw.";
            logAndBroadcast(gameData, logEntry);
            return;
        }

        Card drawn = deck.removeFirst();
        hand.add(drawn);

        sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
        broadcastDeckSizes(gameData);

        String logEntry = gameData.playerIdToName.get(playerId) + " draws a card.";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws a card from effect", gameData.id, gameData.playerIdToName.get(playerId));
    }

    private void resolveDrawCards(GameData gameData, UUID playerId, int amount) {
        for (int i = 0; i < amount; i++) {
            resolveDrawCard(gameData, playerId);
        }
    }

    private void resolveDiscardCards(GameData gameData, UUID playerId, int amount) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to discard.";
            logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.awaitingDiscardRemainingCount = amount;
        beginDiscardChoice(gameData, playerId);
    }

    private void beginDiscardChoice(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        gameData.awaitingInput = AwaitingInput.DISCARD_CHOICE;
        gameData.awaitingCardChoicePlayerId = playerId;
        gameData.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, "Choose a card to discard."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card to discard", gameData.id, playerName);
    }

    private void resolveReturnSelfToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<Card> hand = gameData.playerHands.get(controllerId);

        Permanent toReturn = null;
        for (Permanent p : battlefield) {
            if (p.getCard().getName().equals(entry.getCard().getName())) {
                toReturn = p;
                break;
            }
        }

        if (toReturn == null) {
            String logEntry = entry.getCard().getName() + " is no longer on the battlefield.";
            logAndBroadcast(gameData, logEntry);
            return;
        }

        battlefield.remove(toReturn);
        removeOrphanedAuras(gameData);
        hand.add(toReturn.getOriginalCard());

        String logEntry = entry.getCard().getName() + " is returned to its owner's hand.";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());

        broadcastBattlefields(gameData);
        sessionManager.sendToPlayer(controllerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(controllerId, 0)));
    }

    private void resolveReturnTargetPermanentToHand(GameData gameData, StackEntry entry) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                removeOrphanedAuras(gameData);
                // Stolen creatures return to their owner's hand, not the controller's
                UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameData.stolenCreatures.remove(target.getId());
                List<Card> hand = gameData.playerHands.get(ownerId);
                hand.add(target.getOriginalCard());

                String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());

                broadcastBattlefields(gameData);
                sessionManager.sendToPlayer(ownerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(ownerId, 0)));
                break;
            }
        }
    }

    private void resolveReturnCreaturesToOwnersHand(GameData gameData, StackEntry entry, ReturnCreaturesToOwnersHandEffect bounce) {
        UUID controllerId = entry.getControllerId();
        Set<UUID> affectedPlayers = new HashSet<>();

        boolean controllerOnly = bounce.filters().stream().anyMatch(f -> f instanceof ControllerOnlyTargetFilter);
        boolean excludeSelf = bounce.filters().stream().anyMatch(f -> f instanceof ExcludeSelfTargetFilter);

        List<UUID> playerIds = controllerOnly
                ? List.of(controllerId)
                : gameData.orderedPlayerIds;

        for (UUID playerId : playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }

            List<Permanent> creaturesToReturn = battlefield.stream()
                    .filter(p -> isCreature(gameData, p))
                    .filter(p -> !excludeSelf || !p.getOriginalCard().getId().equals(entry.getCard().getId()))
                    .toList();

            for (Permanent creature : creaturesToReturn) {
                battlefield.remove(creature);
                // Stolen creatures return to their owner's hand
                UUID ownerId = gameData.stolenCreatures.getOrDefault(creature.getId(), playerId);
                gameData.stolenCreatures.remove(creature.getId());
                List<Card> hand = gameData.playerHands.get(ownerId);
                hand.add(creature.getOriginalCard());
                affectedPlayers.add(ownerId);

                String logEntry = creature.getCard().getName() + " is returned to its owner's hand.";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id, creature.getCard().getName(), entry.getCard().getName());
            }
        }

        if (!affectedPlayers.isEmpty()) {
            removeOrphanedAuras(gameData);
            broadcastBattlefields(gameData);
            for (UUID playerId : affectedPlayers) {
                List<Card> hand = gameData.playerHands.get(playerId);
                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
            }
        }
    }

    private void resolveReturnArtifactsTargetPlayerOwnsToHand(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> artifactsToReturn = battlefield.stream()
                .filter(p -> p.getCard().getType() == CardType.ARTIFACT)
                .toList();

        if (artifactsToReturn.isEmpty()) {
            return;
        }

        for (Permanent artifact : artifactsToReturn) {
            battlefield.remove(artifact);
            List<Card> hand = gameData.playerHands.get(targetPlayerId);
            hand.add(artifact.getOriginalCard());

            String logEntry = artifact.getCard().getName() + " is returned to its owner's hand.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, artifact.getCard().getName(), entry.getCard().getName());
        }

        removeOrphanedAuras(gameData);
        broadcastBattlefields(gameData);
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        sessionManager.sendToPlayer(targetPlayerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(targetPlayerId, 0)));
    }

    private void resolveDoubleTargetPlayerLife(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();

        int currentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
        int newLife = currentLife * 2;
        gameData.playerLifeTotals.put(targetPlayerId, newLife);

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + "'s life total is doubled from " + currentLife + " to " + newLife + ".";
        logAndBroadcast(gameData, logEntry);
        broadcastLifeTotals(gameData);
        log.info("Game {} - {}'s life doubled from {} to {}", gameData.id, playerName, currentLife, newLife);
    }

    private void resolveMillByHandSize(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        int handSize = hand != null ? hand.size() : 0;
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (handSize == 0) {
            String logEntry = playerName + " has no cards in hand — mills nothing.";
            logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);

        int cardsToMill = Math.min(handSize, deck.size());
        for (int i = 0; i < cardsToMill; i++) {
            Card card = deck.removeFirst();
            graveyard.add(card);
        }

        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        logAndBroadcast(gameData, logEntry);
        broadcastDeckSizes(gameData);
        broadcastGraveyards(gameData);
        log.info("Game {} - {} mills {} cards (hand size)", gameData.id, playerName, cardsToMill);
    }

    private void resolveMillTargetPlayer(GameData gameData, StackEntry entry, MillTargetPlayerEffect mill) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        int cardsToMill = Math.min(mill.count(), deck.size());
        for (int i = 0; i < cardsToMill; i++) {
            Card card = deck.removeFirst();
            graveyard.add(card);
        }

        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        logAndBroadcast(gameData, logEntry);
        broadcastDeckSizes(gameData);
        broadcastGraveyards(gameData);
        log.info("Game {} - {} mills {} cards", gameData.id, playerName, cardsToMill);
    }

    private void resolveShuffleGraveyardIntoLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (graveyard.isEmpty()) {
            String logEntry = playerName + "'s graveyard is empty. Library is shuffled.";
            logAndBroadcast(gameData, logEntry);
            Collections.shuffle(deck);
            broadcastDeckSizes(gameData);
            return;
        }

        int count = graveyard.size();
        deck.addAll(graveyard);
        graveyard.clear();
        Collections.shuffle(deck);

        String logEntry = playerName + " shuffles their graveyard (" + count + " card" + (count != 1 ? "s" : "") + ") into their library.";
        logAndBroadcast(gameData, logEntry);
        broadcastDeckSizes(gameData);
        broadcastGraveyards(gameData);
        log.info("Game {} - {} shuffles graveyard ({} cards) into library", gameData.id, playerName, count);
    }

    private void resolveLookAtHand(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(entry.getControllerId());

        if (hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            logAndBroadcast(gameData, logEntry);
        } else {
            String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
            String logEntry = casterName + " looks at " + targetName + "'s hand: " + cardNames + ".";
            logAndBroadcast(gameData, logEntry);
        }

        List<CardView> cardViews = hand.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(entry.getControllerId(), new RevealHandMessage(cardViews, targetName));

        log.info("Game {} - {} looks at {}'s hand", gameData.id, casterName, targetName);
    }

    private void resolveRevealTopCardOfLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty.";
            logAndBroadcast(gameData, logEntry);
        } else {
            Card topCard = deck.getFirst();
            String logEntry = playerName + " reveals " + topCard.getName() + " from the top of their library.";
            logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - {} reveals top card of library", gameData.id, playerName);
    }

    private void resolveGainControlOfTargetAura(GameData gameData, StackEntry entry) {
        UUID casterId = entry.getControllerId();
        Permanent aura = findPermanentById(gameData, entry.getTargetPermanentId());
        if (aura == null) return;

        // Step 1: Gain control of the aura (move from current controller's battlefield to caster's)
        UUID currentControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(aura)) {
                currentControllerId = pid;
                break;
            }
        }
        if (currentControllerId != null && !currentControllerId.equals(casterId)) {
            gameData.playerBattlefields.get(currentControllerId).remove(aura);
            gameData.playerBattlefields.get(casterId).add(aura);
            String casterName = gameData.playerIdToName.get(casterId);
            String logEntry = casterName + " gains control of " + aura.getCard().getName() + ".";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} gains control of {}", gameData.id, casterName, aura.getCard().getName());
        }

        // Step 2: Attach it to another permanent it can enchant
        // Collect all creature permanents across ALL battlefields, excluding the one it's currently attached to
        List<UUID> validCreatureIds = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (isCreature(gameData, p) && !p.getId().equals(aura.getAttachedTo())) {
                    validCreatureIds.add(p.getId());
                }
            }
        }

        if (!validCreatureIds.isEmpty()) {
            gameData.permanentChoiceContext = new PermanentChoiceContext.AuraGraft(aura.getId());
            beginPermanentChoice(gameData, casterId, validCreatureIds,
                    "Attach " + aura.getCard().getName() + " to another permanent it can enchant.");
        } else {
            // No other valid creatures — aura stays attached as-is
            String logEntry = aura.getCard().getName() + " stays attached to its current target (no other valid permanents).";
            logAndBroadcast(gameData, logEntry);
        }

        broadcastBattlefields(gameData);
        broadcastPlayableCards(gameData);
    }

    private void stealCreature(GameData gameData, UUID newControllerId, Permanent creature) {
        UUID originalOwnerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(creature)) {
                originalOwnerId = pid;
                break;
            }
        }
        if (originalOwnerId == null || originalOwnerId.equals(newControllerId)) {
            return;
        }

        gameData.playerBattlefields.get(originalOwnerId).remove(creature);
        gameData.playerBattlefields.get(newControllerId).add(creature);
        creature.setSummoningSick(true);

        // Track the original owner so we can return the creature later
        if (!gameData.stolenCreatures.containsKey(creature.getId())) {
            gameData.stolenCreatures.put(creature.getId(), originalOwnerId);
        }

        String newControllerName = gameData.playerIdToName.get(newControllerId);
        String logEntry = newControllerName + " gains control of " + creature.getCard().getName() + ".";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains control of {}", gameData.id, newControllerName, creature.getCard().getName());
    }

    private void returnStolenCreatures(GameData gameData) {
        if (gameData.stolenCreatures.isEmpty()) return;

        boolean anyReturned = false;
        Iterator<Map.Entry<UUID, UUID>> it = gameData.stolenCreatures.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UUID> entry = it.next();
            UUID creatureId = entry.getKey();
            UUID ownerId = entry.getValue();

            Permanent creature = findPermanentById(gameData, creatureId);
            if (creature == null) {
                // Creature left the battlefield, clean up
                it.remove();
                continue;
            }

            // Check if any aura with ControlEnchantedCreatureEffect is still attached
            if (hasAuraWithEffect(gameData, creature, ControlEnchantedCreatureEffect.class)) {
                continue;
            }

            // No control aura attached — return creature to its owner
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(pid);
                if (bf != null && bf.remove(creature)) {
                    gameData.playerBattlefields.get(ownerId).add(creature);
                    creature.setSummoningSick(true);

                    String ownerName = gameData.playerIdToName.get(ownerId);
                    String logEntry = creature.getCard().getName() + " returns to " + ownerName + "'s control.";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returns to {}'s control", gameData.id, creature.getCard().getName(), ownerName);
                    anyReturned = true;
                    break;
                }
            }
            it.remove();
        }
        if (anyReturned) {
            broadcastBattlefields(gameData);
        }
    }

    private void resolveGainLifeEqualToTargetToughness(GameData gameData, StackEntry entry) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        int toughness = getEffectiveToughness(gameData, target);

        // Find the controller (owner of the battlefield the creature is on)
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(target)) {
                int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
                gameData.playerLifeTotals.put(playerId, currentLife + toughness);

                String logEntry = gameData.playerIdToName.get(playerId) + " gains " + toughness + " life.";
                logAndBroadcast(gameData, logEntry);
                broadcastLifeTotals(gameData);

                log.info("Game {} - {} gains {} life (equal to {}'s toughness)",
                        gameData.id, gameData.playerIdToName.get(playerId), toughness, target.getCard().getName());
                break;
            }
        }
    }

    private void resolvePutTargetOnBottomOfLibrary(GameData gameData, StackEntry entry) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                gameData.playerDecks.get(playerId).add(target.getOriginalCard());

                String logEntry = target.getCard().getName() + " is put on the bottom of "
                        + gameData.playerIdToName.get(playerId) + "'s library.";
                logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} put on bottom of {}'s library",
                        gameData.id, target.getCard().getName(), gameData.playerIdToName.get(playerId));
                break;
            }
        }

        removeOrphanedAuras(gameData);
        broadcastBattlefields(gameData);
        broadcastDeckSizes(gameData);
    }

    private void resolvePreventAllCombatDamage(GameData gameData) {
        gameData.preventAllCombatDamage = true;

        String logEntry = "All combat damage will be prevented this turn.";
        logAndBroadcast(gameData, logEntry);
    }

    private void resolvePreventDamageFromColors(GameData gameData, PreventDamageFromColorsEffect effect) {
        gameData.preventDamageFromColors.addAll(effect.colors());

        String colorNames = effect.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " and " + b)
                .orElse("");
        String logEntry = "All damage from " + colorNames + " sources will be prevented this turn.";
        logAndBroadcast(gameData, logEntry);
    }

    private boolean isDamageFromSourcePrevented(GameData gameData, CardColor sourceColor) {
        return sourceColor != null && gameData.preventDamageFromColors.contains(sourceColor);
    }

    private void resolvePreventNextColorDamageToController(GameData gameData, StackEntry entry, PreventNextColorDamageToControllerEffect effect) {
        CardColor chosenColor = effect.chosenColor();
        if (chosenColor == null) return;

        UUID controllerId = entry.getControllerId();
        gameData.playerColorDamagePreventionCount
                .computeIfAbsent(controllerId, k -> new ConcurrentHashMap<>())
                .merge(chosenColor, 1, Integer::sum);
    }

    private boolean applyColorDamagePreventionForPlayer(GameData gameData, UUID playerId, CardColor sourceColor) {
        if (sourceColor == null) return false;
        Map<CardColor, Integer> colorMap = gameData.playerColorDamagePreventionCount.get(playerId);
        if (colorMap == null) return false;
        Integer count = colorMap.get(sourceColor);
        if (count == null || count <= 0) return false;
        colorMap.put(sourceColor, count - 1);
        return true;
    }

    private boolean hasProtectionFrom(GameData gameData, Permanent target, CardColor sourceColor) {
        if (sourceColor == null) return false;
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionFromColorsEffect protection && protection.colors().contains(sourceColor)) {
                return true;
            }
        }
        if (target.getChosenColor() != null && target.getChosenColor() == sourceColor) {
            return true;
        }
        return false;
    }

    private void resolveRedirectUnblockedCombatDamageToSelf(GameData gameData, StackEntry entry) {
        // Find the source permanent (the creature whose tap ability was activated)
        List<Permanent> bf = gameData.playerBattlefields.get(entry.getControllerId());
        if (bf == null) return;
        for (Permanent p : bf) {
            if (p.getCard() == entry.getCard()) {
                gameData.combatDamageRedirectTarget = p.getId();

                String logEntry = p.getCard().getName() + "'s ability resolves — unblocked combat damage will be redirected to it this turn.";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - Combat damage redirect set to {}", gameData.id, p.getCard().getName());
                return;
            }
        }
    }

    private void resolveReturnAuraFromGraveyardToBattlefield(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        // Find the target Aura card in graveyards by its Card UUID
        Card auraCard = findCardInGraveyardById(gameData, entry.getTargetPermanentId());
        if (auraCard == null || !auraCard.isAura()) {
            String fizzleLog = entry.getDescription() + " fizzles (target Aura no longer in graveyard).";
            logAndBroadcast(gameData, fizzleLog);
            return;
        }

        // Check if controller has any creatures to attach to
        List<Permanent> controllerBf = gameData.playerBattlefields.get(controllerId);
        List<UUID> creatureIds = new ArrayList<>();
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (isCreature(gameData, p)) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String fizzleLog = entry.getDescription() + " fizzles (no creatures to attach Aura to).";
            logAndBroadcast(gameData, fizzleLog);
            return;
        }

        // Remove Aura from graveyard and store as pending
        removeCardFromGraveyardById(gameData, auraCard.getId());
        gameData.pendingAuraCard = auraCard;

        // Prompt controller to choose a creature
        beginPermanentChoice(gameData, controllerId, creatureIds, "Choose a creature you control to attach " + auraCard.getName() + " to.");
    }

    private boolean checkLegendRule(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;

        // Group legendary permanents by name
        Map<String, List<UUID>> legendaryByName = new HashMap<>();
        for (Permanent perm : battlefield) {
            if (perm.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)) {
                legendaryByName.computeIfAbsent(perm.getCard().getName(), k -> new ArrayList<>()).add(perm.getId());
            }
        }

        // Find the first name with duplicates
        for (Map.Entry<String, List<UUID>> entry : legendaryByName.entrySet()) {
            if (entry.getValue().size() >= 2) {
                gameData.permanentChoiceContext = new PermanentChoiceContext.LegendRule(entry.getKey());
                beginPermanentChoice(gameData, controllerId, entry.getValue(),
                        "You control multiple legendary permanents named " + entry.getKey() + ". Choose one to keep.");
                return true;
            }
        }
        return false;
    }

    private void beginPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, String prompt) {
        gameData.awaitingInput = AwaitingInput.PERMANENT_CHOICE;
        gameData.awaitingPermanentChoicePlayerId = playerId;
        gameData.awaitingPermanentChoiceValidIds = new HashSet<>(validIds);
        sessionManager.sendToPlayer(playerId, new ChoosePermanentMessage(validIds, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent", gameData.id, playerName);
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
                Permanent clonePerm = findPermanentById(gameData, cloneCopy.clonePermanentId());
                if (clonePerm == null) {
                    throw new IllegalStateException("Clone permanent no longer exists");
                }

                Permanent targetPerm = findPermanentById(gameData, permanentId);
                if (targetPerm == null) {
                    throw new IllegalStateException("Target creature no longer exists");
                }

                applyCloneCopy(clonePerm, targetPerm);

                String logEntry = "Clone enters as a copy of " + targetPerm.getCard().getName() + ".";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - Clone copies {}", gameData.id, targetPerm.getCard().getName());

                broadcastBattlefields(gameData);

                // Check legend rule (Clone may have copied a legendary creature)
                if (!checkLegendRule(gameData, playerId)) {
                    performStateBasedActions(gameData);
                    broadcastPlayableCards(gameData);
                    resolveAutoPass(gameData);
                }
            } else if (context instanceof PermanentChoiceContext.AuraGraft auraGraft) {
                Permanent aura = findPermanentById(gameData, auraGraft.auraPermanentId());
                if (aura == null) {
                    throw new IllegalStateException("Aura permanent no longer exists");
                }

                Permanent newTarget = findPermanentById(gameData, permanentId);
                if (newTarget == null) {
                    throw new IllegalStateException("Target permanent no longer exists");
                }

                aura.setAttachedTo(permanentId);

                String logEntry = aura.getCard().getName() + " is now attached to " + newTarget.getCard().getName() + ".";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(), newTarget.getCard().getName());

                broadcastBattlefields(gameData);
                broadcastPlayableCards(gameData);

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
                    collectDeathTrigger(gameData, perm.getCard(), playerId, wasCreature);
                    String logEntry = perm.getCard().getName() + " is put into the graveyard (legend rule).";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} sent to graveyard by legend rule", gameData.id, perm.getCard().getName());
                }

                removeOrphanedAuras(gameData);
                broadcastBattlefields(gameData);
                broadcastGraveyards(gameData);
                broadcastPlayableCards(gameData);

                resolveAutoPass(gameData);
            } else if (gameData.pendingAuraCard != null) {
                Card auraCard = gameData.pendingAuraCard;
                gameData.pendingAuraCard = null;

                Permanent creatureTarget = findPermanentById(gameData, permanentId);
                if (creatureTarget == null) {
                    throw new IllegalStateException("Target creature no longer exists");
                }

                // Create Aura permanent attached to the creature, under controller's control
                Permanent auraPerm = new Permanent(auraCard);
                auraPerm.setAttachedTo(creatureTarget.getId());
                gameData.playerBattlefields.get(playerId).add(auraPerm);

                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = auraCard.getName() + " enters the battlefield from graveyard attached to " + creatureTarget.getCard().getName() + " under " + playerName + "'s control.";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned {} from graveyard to battlefield attached to {}",
                        gameData.id, playerName, auraCard.getName(), creatureTarget.getCard().getName());

                broadcastBattlefields(gameData);
                broadcastGraveyards(gameData);
                broadcastPlayableCards(gameData);

                resolveAutoPass(gameData);
            } else {
                throw new IllegalStateException("No pending permanent choice context");
            }
        }
    }

    private void resolveReturnCardFromGraveyardToZone(GameData gameData, StackEntry entry,
            CardType cardType, GraveyardChoiceDestination destination, String prompt) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String typeName = cardType.name().toLowerCase();

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + typeName + " cards in graveyard.";
            logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getType() == cardType) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            String logEntry = entry.getDescription() + " — no " + typeName + " cards in graveyard.";
            logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.graveyardChoiceDestination = destination;
        beginGraveyardChoice(gameData, controllerId, matchingIndices, prompt);
    }

    private void resolveTapCreatures(GameData gameData, StackEntry entry, TapCreaturesEffect tap) {
        boolean controllerOnly = tap.filters().stream().anyMatch(f -> f instanceof ControllerOnlyTargetFilter);

        List<UUID> playerIds = controllerOnly
                ? List.of(entry.getControllerId())
                : gameData.orderedPlayerIds;

        for (UUID playerId : playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent p : battlefield) {
                if (!isCreature(gameData, p)) continue;
                if (!matchesFilters(gameData, p, tap.filters())) continue;

                p.tap();

                String logMsg = entry.getCard().getName() + " taps " + p.getCard().getName() + ".";
                logAndBroadcast(gameData, logMsg);
            }
        }

        broadcastBattlefields(gameData);
        log.info("Game {} - {} taps creatures matching filters", gameData.id, entry.getCard().getName());
    }

    private boolean matchesFilters(GameData gameData, Permanent permanent, Set<TargetFilter> filters) {
        for (TargetFilter filter : filters) {
            if (filter instanceof WithoutKeywordTargetFilter f) {
                if (hasKeyword(gameData, permanent, f.keyword())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void resolveTapTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.tap();

        String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
        logAndBroadcast(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    private void resolveUntapSelf(GameData gameData, StackEntry entry) {
        Permanent self = findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.untap();

        String logEntry = entry.getCard().getName() + " untaps.";
        logAndBroadcast(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} untaps", gameData.id, entry.getCard().getName());
    }

    private void beginGraveyardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.awaitingInput = AwaitingInput.GRAVEYARD_CHOICE;
        gameData.awaitingGraveyardChoicePlayerId = playerId;
        gameData.awaitingGraveyardChoiceValidIndices = new HashSet<>(validIndices);
        sessionManager.sendToPlayer(playerId, new ChooseCardFromGraveyardMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from graveyard", gameData.id, playerName);
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
                logAndBroadcast(gameData, logEntry);
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
                        logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} returns {} from graveyard to hand", gameData.id, player.getUsername(), card.getName());

                        broadcastGraveyards(gameData);
                        sessionManager.sendToPlayer(playerId, new HandDrawnMessage(gameData.playerHands.get(playerId).stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                    }
                    case BATTLEFIELD -> {
                        Permanent perm = new Permanent(card);
                        gameData.playerBattlefields.get(playerId).add(perm);

                        String logEntry = player.getUsername() + " returns " + card.getName() + " from graveyard to the battlefield.";
                        logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} returns {} from graveyard to battlefield", gameData.id, player.getUsername(), card.getName());

                        broadcastBattlefields(gameData);
                        broadcastGraveyards(gameData);

                        handleCreatureEnteredBattlefield(gameData, playerId, card, null);
                        if (gameData.awaitingInput == null) {
                            checkLegendRule(gameData, playerId);
                        }
                    }
                }
            }

            resolveAutoPass(gameData);
        }
    }

    private void resolveCreateCreatureToken(GameData gameData, UUID controllerId, CreateCreatureTokenEffect token) {
        Card tokenCard = new Card(token.tokenName(), CardType.CREATURE, "", token.color());
        tokenCard.setPower(token.power());
        tokenCard.setToughness(token.toughness());
        tokenCard.setSubtypes(token.subtypes());

        Permanent tokenPermanent = new Permanent(tokenCard);
        gameData.playerBattlefields.get(controllerId).add(tokenPermanent);

        String logEntry = "A " + token.power() + "/" + token.toughness() + " " + token.tokenName() + " creature token enters the battlefield.";
        logAndBroadcast(gameData, logEntry);
        broadcastBattlefields(gameData);

        handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null);
        if (gameData.awaitingInput == null) {
            checkLegendRule(gameData, controllerId);
        }

        log.info("Game {} - {} token created for player {}", gameData.id, token.tokenName(), controllerId);
    }

    private int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage) {
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(e -> e instanceof PreventAllDamageEffect)) return 0;
        if (hasAuraWithEffect(gameData, permanent, PreventAllDamageToAndByEnchantedCreatureEffect.class)) return 0;
        damage = applyGlobalPreventionShield(gameData, damage);
        int shield = permanent.getDamagePreventionShield();
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        permanent.setDamagePreventionShield(shield - prevented);
        return damage - prevented;
    }

    private boolean hasAuraWithEffect(GameData gameData, Permanent creature, Class<? extends CardEffect> effectClass) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getAttachedTo() != null && p.getAttachedTo().equals(creature.getId())) {
                    for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effectClass.isInstance(effect)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isPreventedFromDealingDamage(GameData gameData, Permanent creature) {
        return hasAuraWithEffect(gameData, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)
                || isDamageFromSourcePrevented(gameData, creature.getCard().getColor());
    }

    private int applyPlayerPreventionShield(GameData gameData, UUID playerId, int damage) {
        damage = applyGlobalPreventionShield(gameData, damage);
        int shield = gameData.playerDamagePreventionShields.getOrDefault(playerId, 0);
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.playerDamagePreventionShields.put(playerId, shield - prevented);
        return damage - prevented;
    }

    private Permanent findEnchantedCreatureByAuraEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectClass) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent p : bf) {
            if (p.getAttachedTo() != null) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effectClass.isInstance(effect)) {
                        return findPermanentById(gameData, p.getAttachedTo());
                    }
                }
            }
        }
        return null;
    }

    private int redirectPlayerDamageToEnchantedCreature(GameData gameData, UUID playerId, int damage, String sourceName) {
        if (damage <= 0) return damage;
        Permanent target = findEnchantedCreatureByAuraEffect(gameData, playerId, RedirectPlayerDamageToEnchantedCreatureEffect.class);
        if (target == null) return damage;

        int effectiveDamage = applyCreaturePreventionShield(gameData, target, damage);
        String logEntry = target.getCard().getName() + " absorbs " + effectiveDamage + " redirected " + sourceName + " damage.";
        logAndBroadcast(gameData, logEntry);

        if (effectiveDamage >= getEffectiveToughness(gameData, target)) {
            removePermanentToGraveyard(gameData, target);
            String deathLog = target.getCard().getName() + " is destroyed by redirected " + sourceName + " damage.";
            logAndBroadcast(gameData, deathLog);
            removeOrphanedAuras(gameData);
        }

        return 0;
    }

    private int getMaxSpellsPerTurn(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof LimitSpellsPerTurnEffect limit) {
                        return limit.maxSpells();
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private int getOpponentCostIncrease(GameData gameData, UUID playerId, CardType cardType) {
        UUID opponentId = getOpponentId(gameData, playerId);
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.get(opponentId);
        if (opponentBattlefield == null) return 0;

        int totalIncrease = 0;
        for (Permanent perm : opponentBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof IncreaseOpponentCastCostEffect increase) {
                    if (increase.affectedTypes().contains(cardType)) {
                        totalIncrease += increase.amount();
                    }
                }
            }
        }
        return totalIncrease;
    }

    private int getAttackPaymentPerCreature(GameData gameData, UUID attackingPlayerId) {
        UUID defenderId = getOpponentId(gameData, attackingPlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        if (defenderBattlefield == null) return 0;

        int totalTax = 0;
        for (Permanent perm : defenderBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RequirePaymentToAttackEffect tax) {
                    totalTax += tax.amountPerAttacker();
                }
            }
        }
        return totalTax;
    }

    private void payGenericMana(ManaPool pool, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            ManaColor highestColor = null;
            int highestAmount = 0;
            for (ManaColor color : ManaColor.values()) {
                int available = pool.get(color);
                if (available > highestAmount) {
                    highestAmount = available;
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

    private void removeOrphanedAuras(GameData gameData) {
        boolean anyRemoved = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (p.getAttachedTo() != null && findPermanentById(gameData, p.getAttachedTo()) == null) {
                    it.remove();
                    gameData.playerGraveyards.get(playerId).add(p.getOriginalCard());
                    String logEntry = p.getCard().getName() + " is put into the graveyard (enchanted creature left the battlefield).";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} removed (orphaned aura)", gameData.id, p.getCard().getName());
                    anyRemoved = true;
                }
            }
        }
        if (anyRemoved) {
            broadcastBattlefields(gameData);
            broadcastGraveyards(gameData);
        }
        returnStolenCreatures(gameData);
    }

    private void applyCloneCopy(Permanent clonePerm, Permanent targetPerm) {
        Card target = targetPerm.getCard();
        Card copy = new Card(target.getName(), target.getType(), target.getManaCost(), target.getColor());
        copy.setSupertypes(target.getSupertypes());
        copy.setSubtypes(target.getSubtypes());
        copy.setCardText(target.getCardText());
        copy.setPower(target.getPower());
        copy.setToughness(target.getToughness());
        copy.setKeywords(target.getKeywords());
        copy.setNeedsTarget(target.isNeedsTarget());
        copy.setSetCode(target.getSetCode());
        copy.setCollectorNumber(target.getCollectorNumber());
        copy.setArtist(target.getArtist());
        copy.setRarity(target.getRarity());
        for (EffectSlot slot : EffectSlot.values()) {
            for (CardEffect effect : target.getEffects(slot)) {
                copy.addEffect(slot, effect);
            }
        }
        for (ActivatedAbility ability : target.getActivatedAbilities()) {
            copy.addActivatedAbility(ability);
        }
        clonePerm.setCard(copy);
    }

    private void performStateBasedActions(GameData gameData) {
        boolean anyDied = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (isCreature(gameData, p) && getEffectiveToughness(gameData, p) <= 0) {
                    it.remove();
                    // Stolen creatures go to their owner's graveyard
                    UUID graveyardOwnerId = gameData.stolenCreatures.getOrDefault(p.getId(), playerId);
                    gameData.stolenCreatures.remove(p.getId());
                    gameData.playerGraveyards.get(graveyardOwnerId).add(p.getOriginalCard());
                    collectDeathTrigger(gameData, p.getCard(), playerId, true);
                    String logEntry = p.getCard().getName() + " is put into the graveyard (0 toughness).";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 toughness)", gameData.id, p.getCard().getName());
                    anyDied = true;
                }
            }
        }
        if (anyDied) {
            removeOrphanedAuras(gameData);
            broadcastBattlefields(gameData);
            broadcastGraveyards(gameData);
        }
    }

    private void validateTargetFilter(TargetFilter filter, Permanent target) {
        if (filter instanceof MaxPowerTargetFilter f) {
            if (target.getEffectivePower() > f.maxPower()) {
                throw new IllegalStateException("Target creature's power must be " + f.maxPower() + " or less");
            }
        } else if (filter instanceof AttackingOrBlockingTargetFilter) {
            if (!target.isAttacking() && !target.isBlocking()) {
                throw new IllegalStateException("Target must be an attacking or blocking creature");
            }
        } else if (filter instanceof AttackingTargetFilter) {
            if (!target.isAttacking()) {
                throw new IllegalStateException("Target must be an attacking creature");
            }
        }
    }

    private Permanent findPermanentById(GameData gameData, UUID permanentId) {
        if (permanentId == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(permanentId)) {
                    return p;
                }
            }
        }
        return null;
    }

    private boolean removePermanentToGraveyard(GameData gameData, Permanent target) {
        boolean wasCreature = isCreature(gameData, target);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                // Stolen creatures go to their owner's graveyard, not the controller's
                UUID graveyardOwnerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameData.playerGraveyards.get(graveyardOwnerId).add(target.getOriginalCard());
                gameData.stolenCreatures.remove(target.getId());
                collectDeathTrigger(gameData, target.getCard(), playerId, wasCreature);
                return true;
            }
        }
        return false;
    }

    private Card findCardInGraveyardById(GameData gameData, UUID cardId) {
        if (cardId == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card c : graveyard) {
                if (c.getId().equals(cardId)) {
                    return c;
                }
            }
        }
        return null;
    }

    private void removeCardFromGraveyardById(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            if (graveyard.removeIf(c -> c.getId().equals(cardId))) {
                return;
            }
        }
    }

    private void resetEndOfTurnModifiers(GameData gameData) {
        boolean anyReset = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getPowerModifier() != 0 || p.getToughnessModifier() != 0 || !p.getGrantedKeywords().isEmpty()
                        || p.getDamagePreventionShield() != 0 || p.getRegenerationShield() != 0 || p.isCantBeBlocked()) {
                    p.resetModifiers();
                    p.setDamagePreventionShield(0);
                    p.setRegenerationShield(0);
                    anyReset = true;
                }
            }
        }
        if (anyReset) {
            broadcastBattlefields(gameData);
        }

        // Clear player damage prevention shields
        gameData.playerDamagePreventionShields.clear();
        gameData.globalDamagePreventionShield = 0;
        gameData.preventAllCombatDamage = false;
        gameData.preventDamageFromColors.clear();
        gameData.combatDamageRedirectTarget = null;
        gameData.playerColorDamagePreventionCount.clear();
        gameData.drawReplacementTargetToController.clear();
    }

    // ===== Regeneration =====

    private void resolveRegenerate(GameData gameData, StackEntry entry) {
        Permanent perm = findPermanentById(gameData, entry.getTargetPermanentId());
        if (perm == null) {
            return;
        }
        perm.setRegenerationShield(perm.getRegenerationShield() + 1);

        String logEntry = perm.getCard().getName() + " gains a regeneration shield.";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains a regeneration shield", gameData.id, perm.getCard().getName());
        broadcastBattlefields(gameData);
    }

    private boolean tryRegenerate(GameData gameData, Permanent perm) {
        if (perm.getRegenerationShield() > 0) {
            perm.setRegenerationShield(perm.getRegenerationShield() - 1);
            perm.tap();
            perm.setAttacking(false);
            perm.setBlocking(false);
            perm.getBlockingTargets().clear();

            String logEntry = perm.getCard().getName() + " regenerates.";
            logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} regenerates", gameData.id, perm.getCard().getName());
            return true;
        }
        return false;
    }

    // ===== Static / continuous effect computation =====

    private record StaticBonus(int power, int toughness, Set<Keyword> keywords, boolean animatedCreature) {
        static final StaticBonus NONE = new StaticBonus(0, 0, Set.of(), false);
    }

    private StaticBonus computeStaticBonus(GameData gameData, Permanent target) {
        boolean isNaturalCreature = target.getCard().getType() == CardType.CREATURE;
        boolean isArtifact = target.getCard().getType() == CardType.ARTIFACT;
        boolean animatedCreature = false;
        int power = 0;
        int toughness = 0;
        Set<Keyword> keywords = new HashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                if (source == target) continue;
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AnimateNoncreatureArtifactsEffect && isArtifact) {
                        animatedCreature = true;
                    }
                    if (effect instanceof BoostCreaturesBySubtypeEffect boost
                            && target.getCard().getSubtypes().stream().anyMatch(boost.affectedSubtypes()::contains)) {
                        power += boost.powerBoost();
                        toughness += boost.toughnessBoost();
                        keywords.addAll(boost.grantedKeywords());
                    }
                    if (effect instanceof BoostEnchantedCreatureEffect boost
                            && source.getAttachedTo() != null
                            && source.getAttachedTo().equals(target.getId())) {
                        power += boost.powerBoost();
                        toughness += boost.toughnessBoost();
                    }
                    if (effect instanceof GrantKeywordToEnchantedCreatureEffect grant
                            && source.getAttachedTo() != null
                            && source.getAttachedTo().equals(target.getId())) {
                        keywords.add(grant.keyword());
                    }
                    if (effect instanceof BoostOwnCreaturesEffect boost
                            && bf.contains(target)) {
                        power += boost.powerBoost();
                        toughness += boost.toughnessBoost();
                    }
                }
            }
        }
        if (!isNaturalCreature && !animatedCreature) return StaticBonus.NONE;

        if (animatedCreature) {
            int manaValue = target.getCard().getManaValue();
            power += manaValue;
            toughness += manaValue;
        }

        return new StaticBonus(power, toughness, keywords, animatedCreature);
    }

    public boolean isCreature(GameData gameData, Permanent permanent) {
        if (permanent.getCard().getType() == CardType.CREATURE) return true;
        if (permanent.getCard().getType() != CardType.ARTIFACT) return false;
        return hasAnimateArtifactEffect(gameData);
    }

    private boolean hasAnimateArtifactEffect(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AnimateNoncreatureArtifactsEffect) return true;
                }
            }
        }
        return false;
    }

    public int getEffectivePower(GameData gameData, Permanent permanent) {
        return permanent.getEffectivePower() + computeStaticBonus(gameData, permanent).power();
    }

    public int getEffectiveToughness(GameData gameData, Permanent permanent) {
        return permanent.getEffectiveToughness() + computeStaticBonus(gameData, permanent).toughness();
    }

    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return permanent.hasKeyword(keyword) || computeStaticBonus(gameData, permanent).keywords().contains(keyword);
    }

    private boolean playerHasShroud(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent source : battlefield) {
            if (source.getCard().getKeywords().contains(Keyword.SHROUD)) {
                return true;
            }
        }
        return false;
    }

    // ===== Sorcery effect methods =====

    private void resolveDealDamageToFlyingAndPlayers(GameData gameData, StackEntry entry) {
        if (isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logMsg = entry.getCard().getName() + "'s damage is prevented.";
            logAndBroadcast(gameData, logMsg);
            return;
        }

        int damage = entry.getXValue();
        // Deal damage to creatures with flying
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            Set<Integer> deadIndices = new TreeSet<>(Collections.reverseOrder());
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent p = battlefield.get(i);
                if (hasKeyword(gameData, p, Keyword.FLYING)) {
                    if (hasProtectionFrom(gameData, p, entry.getCard().getColor())) {
                        continue;
                    }
                    int effectiveDamage = applyCreaturePreventionShield(gameData, p, damage);
                    int toughness = getEffectiveToughness(gameData, p);
                    if (effectiveDamage >= toughness && !tryRegenerate(gameData, p)) {
                        deadIndices.add(i);
                    }
                }
            }

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            for (int idx : deadIndices) {
                String playerName = gameData.playerIdToName.get(playerId);
                Permanent dead = battlefield.get(idx);
                String logEntry = playerName + "'s " + dead.getCard().getName() + " is destroyed by Hurricane.";
                logAndBroadcast(gameData, logEntry);
                graveyard.add(dead.getOriginalCard());
                collectDeathTrigger(gameData, dead.getCard(), playerId, true);
                battlefield.remove(idx);
            }
        }

        removeOrphanedAuras(gameData);
        broadcastBattlefields(gameData);
        broadcastGraveyards(gameData);

        // Deal damage to each player (with prevention shields and damage redirect)
        String cardName = entry.getCard().getName();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (applyColorDamagePreventionForPlayer(gameData, playerId, entry.getCard().getColor())) {
                continue;
            }
            int effectiveDamage = applyPlayerPreventionShield(gameData, playerId, damage);
            effectiveDamage = redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

            if (effectiveDamage > 0) {
                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                logAndBroadcast(gameData, logEntry);
            }
        }

        broadcastLifeTotals(gameData);
        checkWinCondition(gameData);
    }

    // ===== Combat methods =====

    private List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (isCreature(gameData, p) && !p.isTapped() && !p.isSummoningSick() && !hasKeyword(gameData, p, Keyword.DEFENDER) && !hasAuraWithEffect(gameData, p, EnchantedCreatureCantAttackOrBlockEffect.class)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<Integer> getBlockableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent p = battlefield.get(i);
            if (isCreature(gameData, p) && !p.isTapped() && !hasAuraWithEffect(gameData, p, EnchantedCreatureCantAttackOrBlockEffect.class)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<Integer> getAttackingCreatureIndices(GameData gameData, UUID playerId) {
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

    private UUID getOpponentId(GameData gameData, UUID playerId) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        return ids.get(0).equals(playerId) ? ids.get(1) : ids.get(0);
    }

    private List<Integer> getLifeTotals(GameData gameData) {
        List<Integer> totals = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            totals.add(gameData.playerLifeTotals.getOrDefault(pid, 20));
        }
        return totals;
    }

    private void broadcastLifeTotals(GameData gameData) {
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new LifeUpdatedMessage(getLifeTotals(gameData)));
    }

    private void handleDeclareAttackersStep(GameData gameData) {
        UUID activeId = gameData.activePlayerId;
        List<Integer> attackable = getAttackableCreatureIndices(gameData, activeId);

        if (attackable.isEmpty()) {
            String playerName = gameData.playerIdToName.get(activeId);
            log.info("Game {} - {} has no creatures that can attack, skipping combat", gameData.id, playerName);
            skipToEndOfCombat(gameData);
            return;
        }

        gameData.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        sessionManager.sendToPlayer(activeId, new AvailableAttackersMessage(attackable));
    }

    private void skipToEndOfCombat(GameData gameData) {
        gameData.currentStep = TurnStep.END_OF_COMBAT;
        clearCombatState(gameData);

        String logEntry = "Step: " + TurnStep.END_OF_COMBAT.getDisplayName();
        logAndBroadcast(gameData, logEntry);
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new StepAdvancedMessage(getPriorityPlayerId(gameData), TurnStep.END_OF_COMBAT));
    }

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
        synchronized (gameData) {
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
                resolveAutoPass(gameData);
                return;
            }

            // Check attack tax (e.g. Windborn Muse / Ghostly Prison)
            int taxPerCreature = getAttackPaymentPerCreature(gameData, playerId);
            if (taxPerCreature > 0) {
                int totalTax = taxPerCreature * attackerIndices.size();
                ManaPool pool = gameData.playerManaPools.get(playerId);
                if (pool.getTotal() < totalTax) {
                    throw new IllegalStateException("Not enough mana to pay attack tax (" + totalTax + " required)");
                }
                payGenericMana(pool, totalTax);
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));
            }

            // Mark creatures as attacking and tap them (vigilance skips tapping)
            for (int idx : attackerIndices) {
                Permanent attacker = battlefield.get(idx);
                attacker.setAttacking(true);
                if (!hasKeyword(gameData, attacker, Keyword.VIGILANCE)) {
                    attacker.tap();
                }
            }

            String logEntry = player.getUsername() + " declares " + attackerIndices.size() +
                    " attacker" + (attackerIndices.size() > 1 ? "s" : "") + ".";
            logAndBroadcast(gameData, logEntry);

            broadcastBattlefields(gameData);

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
                    broadcastLogEntry(gameData, triggerLog);
                    log.info("Game {} - {} attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                }
            }
            if (!gameData.stack.isEmpty()) {
                broadcastStackUpdate(gameData);
            }

            log.info("Game {} - {} declares {} attackers", gameData.id, player.getUsername(), attackerIndices.size());

            advanceStep(gameData);
            resolveAutoPass(gameData);
        }
    }

    private void handleDeclareBlockersStep(GameData gameData) {
        UUID activeId = gameData.activePlayerId;
        UUID defenderId = getOpponentId(gameData, activeId);
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
            advanceStep(gameData);
            return;
        }

        gameData.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        sessionManager.sendToPlayer(defenderId, new AvailableBlockersMessage(blockable, attackerIndices));
    }

    public void declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        synchronized (gameData) {
            if (gameData.awaitingInput != AwaitingInput.BLOCKER_DECLARATION) {
                throw new IllegalStateException("Not awaiting blocker declaration");
            }

            UUID activeId = gameData.activePlayerId;
            UUID defenderId = getOpponentId(gameData, activeId);

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
                if (hasKeyword(gameData, attacker, Keyword.FLYING)
                        && !hasKeyword(gameData, blocker, Keyword.FLYING)
                        && !hasKeyword(gameData, blocker, Keyword.REACH)) {
                    throw new IllegalStateException(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (flying)");
                }
                boolean blockOnlyFlyers = blocker.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof BlockOnlyFlyersEffect);
                if (blockOnlyFlyers && !hasKeyword(gameData, attacker, Keyword.FLYING)) {
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
                if (hasProtectionFrom(gameData, attacker, blocker.getCard().getColor())) {
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
                logAndBroadcast(gameData, logEntry);
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
                    logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} block trigger pushed onto stack", gameData.id, blocker.getCard().getName());
                }
            }

            broadcastBattlefields(gameData);
            if (!gameData.stack.isEmpty()) {
                broadcastStackUpdate(gameData);
            }

            log.info("Game {} - {} declares {} blockers", gameData.id, player.getUsername(), blockerAssignments.size());

            advanceStep(gameData);
            resolveAutoPass(gameData);
        }
    }

    private void resolveCombatDamage(GameData gameData) {
        if (gameData.preventAllCombatDamage) {
            String logEntry = "All combat damage is prevented.";
            logAndBroadcast(gameData, logEntry);

            advanceStep(gameData);
            resolveAutoPass(gameData);
            return;
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = getOpponentId(gameData, activeId);

        List<Permanent> atkBf = gameData.playerBattlefields.get(activeId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);

        // Check for combat damage redirect (e.g. Kjeldoran Royal Guard)
        Permanent redirectTarget = gameData.combatDamageRedirectTarget != null
                ? findPermanentById(gameData, gameData.combatDamageRedirectTarget) : null;
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
            if (hasKeyword(gameData, atkBf.get(atkIdx), Keyword.FIRST_STRIKE)
                    || hasKeyword(gameData, atkBf.get(atkIdx), Keyword.DOUBLE_STRIKE)) {
                anyFirstStrike = true;
                break;
            }
        }
        if (!anyFirstStrike) {
            for (List<Integer> blkIndices : blockerMap.values()) {
                for (int blkIdx : blkIndices) {
                    if (hasKeyword(gameData, defBf.get(blkIdx), Keyword.FIRST_STRIKE)
                            || hasKeyword(gameData, defBf.get(blkIdx), Keyword.DOUBLE_STRIKE)) {
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
                boolean atkHasFS = hasKeyword(gameData, atk, Keyword.FIRST_STRIKE)
                        || hasKeyword(gameData, atk, Keyword.DOUBLE_STRIKE);

                if (blkIndices.isEmpty()) {
                    // Unblocked first striker deals damage to player (or redirect target)
                    if (atkHasFS && !isPreventedFromDealingDamage(gameData, atk)) {
                        int power = getEffectivePower(gameData, atk);
                        if (redirectTarget != null) {
                            damageRedirectedToGuard += power;
                        } else if (!applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getCard().getColor())) {
                            damageToDefendingPlayer += power;
                        }
                        combatDamageDealt.merge(atk, power, Integer::sum);
                        combatDamageDealtToPlayer.merge(atk, power, Integer::sum);
                    }
                } else {
                    // First strike attacker deals damage to blockers
                    if (atkHasFS && !isPreventedFromDealingDamage(gameData, atk)) {
                        int remaining = getEffectivePower(gameData, atk);
                        for (int blkIdx : blkIndices) {
                            Permanent blk = defBf.get(blkIdx);
                            int dmg = Math.min(remaining, getEffectiveToughness(gameData, blk));
                            if (!hasProtectionFrom(gameData, blk, atk.getCard().getColor())) {
                                defDamageTaken.merge(blkIdx, dmg, Integer::sum);
                                combatDamageDealt.merge(atk, dmg, Integer::sum);
                            }
                            remaining -= dmg;
                        }
                    }
                    // First strike / double strike blockers deal damage to attacker
                    for (int blkIdx : blkIndices) {
                        Permanent blk = defBf.get(blkIdx);
                        if ((hasKeyword(gameData, blk, Keyword.FIRST_STRIKE) || hasKeyword(gameData, blk, Keyword.DOUBLE_STRIKE))
                                && !isPreventedFromDealingDamage(gameData, blk)
                                && !hasProtectionFrom(gameData, atk, blk.getCard().getColor())) {
                            atkDamageTaken.merge(atkIdx, getEffectivePower(gameData, blk), Integer::sum);
                            combatDamageDealt.merge(blk, getEffectivePower(gameData, blk), Integer::sum);
                        }
                    }
                }
            }

            // Determine phase 1 casualties (apply prevention shields)
            for (int atkIdx : attackingIndices) {
                int dmg = atkDamageTaken.getOrDefault(atkIdx, 0);
                dmg = applyCreaturePreventionShield(gameData, atkBf.get(atkIdx), dmg);
                atkDamageTaken.put(atkIdx, dmg);
                if (dmg >= getEffectiveToughness(gameData, atkBf.get(atkIdx))
                        && !tryRegenerate(gameData, atkBf.get(atkIdx))) {
                    deadAttackerIndices.add(atkIdx);
                }
            }
            for (List<Integer> blkIndices : blockerMap.values()) {
                for (int blkIdx : blkIndices) {
                    int dmg = defDamageTaken.getOrDefault(blkIdx, 0);
                    dmg = applyCreaturePreventionShield(gameData, defBf.get(blkIdx), dmg);
                    defDamageTaken.put(blkIdx, dmg);
                    if (dmg >= getEffectiveToughness(gameData, defBf.get(blkIdx))
                            && !tryRegenerate(gameData, defBf.get(blkIdx))) {
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
            boolean atkSkipPhase2 = hasKeyword(gameData, atk, Keyword.FIRST_STRIKE)
                    && !hasKeyword(gameData, atk, Keyword.DOUBLE_STRIKE);

            if (blkIndices.isEmpty()) {
                // Unblocked regular attacker deals damage to player (or redirect target)
                if (!atkSkipPhase2 && !isPreventedFromDealingDamage(gameData, atk)) {
                    int power = getEffectivePower(gameData, atk);
                    if (redirectTarget != null) {
                        damageRedirectedToGuard += power;
                    } else if (!applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getCard().getColor())) {
                        damageToDefendingPlayer += power;
                    }
                    combatDamageDealt.merge(atk, power, Integer::sum);
                    combatDamageDealtToPlayer.merge(atk, power, Integer::sum);
                }
            } else {
                // Attacker deals damage to surviving blockers (skip first-strike-only, allow double strike)
                if (!atkSkipPhase2 && !isPreventedFromDealingDamage(gameData, atk)) {
                    int remaining = getEffectivePower(gameData, atk);
                    for (int blkIdx : blkIndices) {
                        if (deadDefenderIndices.contains(blkIdx)) continue;
                        Permanent blk = defBf.get(blkIdx);
                        int remainingToughness = getEffectiveToughness(gameData, blk) - defDamageTaken.getOrDefault(blkIdx, 0);
                        int dmg = Math.min(remaining, remainingToughness);
                        if (!hasProtectionFrom(gameData, blk, atk.getCard().getColor())) {
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
                    boolean blkSkipPhase2 = hasKeyword(gameData, blk, Keyword.FIRST_STRIKE)
                            && !hasKeyword(gameData, blk, Keyword.DOUBLE_STRIKE);
                    if (!blkSkipPhase2 && !isPreventedFromDealingDamage(gameData, blk)
                            && !hasProtectionFrom(gameData, atk, blk.getCard().getColor())) {
                        atkDamageTaken.merge(atkIdx, getEffectivePower(gameData, blk), Integer::sum);
                        combatDamageDealt.merge(blk, getEffectivePower(gameData, blk), Integer::sum);
                    }
                }
            }
        }

        // Determine phase 2 casualties (apply prevention shields)
        for (int atkIdx : attackingIndices) {
            if (deadAttackerIndices.contains(atkIdx)) continue;
            int dmg = atkDamageTaken.getOrDefault(atkIdx, 0);
            dmg = applyCreaturePreventionShield(gameData, atkBf.get(atkIdx), dmg);
            atkDamageTaken.put(atkIdx, dmg);
            if (dmg >= getEffectiveToughness(gameData, atkBf.get(atkIdx))
                    && !tryRegenerate(gameData, atkBf.get(atkIdx))) {
                deadAttackerIndices.add(atkIdx);
            }
        }
        for (List<Integer> blkIndices : blockerMap.values()) {
            for (int blkIdx : blkIndices) {
                if (deadDefenderIndices.contains(blkIdx)) continue;
                int dmg = defDamageTaken.getOrDefault(blkIdx, 0);
                dmg = applyCreaturePreventionShield(gameData, defBf.get(blkIdx), dmg);
                defDamageTaken.put(blkIdx, dmg);
                if (dmg >= getEffectiveToughness(gameData, defBf.get(blkIdx))
                        && !tryRegenerate(gameData, defBf.get(blkIdx))) {
                    deadDefenderIndices.add(blkIdx);
                }
            }
        }

        // Apply redirected damage to guard creature (e.g. Kjeldoran Royal Guard)
        if (redirectTarget != null && damageRedirectedToGuard > 0) {
            damageRedirectedToGuard = applyCreaturePreventionShield(gameData, redirectTarget, damageRedirectedToGuard);
            String redirectLog = redirectTarget.getCard().getName() + " absorbs " + damageRedirectedToGuard + " redirected combat damage.";
            logAndBroadcast(gameData, redirectLog);

            if (damageRedirectedToGuard >= getEffectiveToughness(gameData, redirectTarget)
                    && !tryRegenerate(gameData, redirectTarget)) {
                removePermanentToGraveyard(gameData, redirectTarget);
                String deathLog = redirectTarget.getCard().getName() + " is destroyed by redirected combat damage.";
                logAndBroadcast(gameData, deathLog);
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
            collectDeathTrigger(gameData, dead.getCard(), activeId, true);
            atkBf.remove(idx);
        }
        for (int idx : deadDefenderIndices) {
            Permanent dead = defBf.get(idx);
            deadCreatureNames.add(gameData.playerIdToName.get(defenderId) + "'s " + dead.getCard().getName());
            // Stolen creatures go to their owner's graveyard
            UUID defGraveyardOwner = gameData.stolenCreatures.getOrDefault(dead.getId(), defenderId);
            gameData.stolenCreatures.remove(dead.getId());
            gameData.playerGraveyards.get(defGraveyardOwner).add(dead.getOriginalCard());
            collectDeathTrigger(gameData, dead.getCard(), defenderId, true);
            defBf.remove(idx);
        }
        if (!deadAttackerIndices.isEmpty() || !deadDefenderIndices.isEmpty()) {
            removeOrphanedAuras(gameData);
        }

        removeOrphanedAuras(gameData);

        // Apply life loss (with prevention shield and Pariah redirect)
        damageToDefendingPlayer = applyPlayerPreventionShield(gameData, defenderId, damageToDefendingPlayer);
        damageToDefendingPlayer = redirectPlayerDamageToEnchantedCreature(gameData, defenderId, damageToDefendingPlayer, "combat");
        if (damageToDefendingPlayer > 0) {
            int currentLife = gameData.playerLifeTotals.getOrDefault(defenderId, 20);
            gameData.playerLifeTotals.put(defenderId, currentLife - damageToDefendingPlayer);

            String logEntry = gameData.playerIdToName.get(defenderId) + " takes " + damageToDefendingPlayer + " combat damage.";
            logAndBroadcast(gameData, logEntry);
        }

        if (!deadCreatureNames.isEmpty()) {
            String logEntry = String.join(", ", deadCreatureNames) + " died in combat.";
            logAndBroadcast(gameData, logEntry);
        }

        broadcastBattlefields(gameData);
        broadcastLifeTotals(gameData);
        if (!deadAttackerIndices.isEmpty() || !deadDefenderIndices.isEmpty()) {
            broadcastGraveyards(gameData);
        }

        log.info("Game {} - Combat damage resolved: {} damage to defender, {} creatures died",
                gameData.id, damageToDefendingPlayer, deadAttackerIndices.size() + deadDefenderIndices.size());

        // Check win condition
        if (checkWinCondition(gameData)) {
            return;
        }

        // Process combat damage to player triggers (e.g. Cephalid Constable) after all combat is resolved
        processCombatDamageToPlayerTriggers(gameData, combatDamageDealtToPlayer, activeId, defenderId);
        if (gameData.awaitingInput != null) {
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            processNextMayAbility(gameData);
            return;
        }

        advanceStep(gameData);
        resolveAutoPass(gameData);
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
                                logAndBroadcast(gameData, logEntry);
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
                if (effect instanceof ReturnPermanentsOnCombatDamageToPlayerEffect) {
                    // Collect valid permanents the damaged player controls
                    List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
                    List<UUID> validIds = new ArrayList<>();
                    for (Permanent perm : defenderBattlefield) {
                        validIds.add(perm.getId());
                    }

                    if (validIds.isEmpty()) {
                        String logEntry = creature.getCard().getName() + "'s ability triggers, but " + gameData.playerIdToName.get(defenderId) + " has no permanents.";
                        logAndBroadcast(gameData, logEntry);
                        continue;
                    }

                    String logEntry = creature.getCard().getName() + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " may return up to " + damageDealt + " permanent" + (damageDealt > 1 ? "s" : "") + ".";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} combat damage trigger: {} damage, {} valid targets", gameData.id, creature.getCard().getName(), damageDealt, validIds.size());

                    gameData.pendingCombatDamageBounceTargetPlayerId = defenderId;
                    int maxCount = Math.min(damageDealt, validIds.size());
                    beginMultiPermanentChoice(gameData, attackerId, validIds, maxCount, "Return up to " + damageDealt + " permanent" + (damageDealt > 1 ? "s" : "") + " to their owner's hand.");
                    return;
                }
            }
        }
    }

    private void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount, String prompt) {
        gameData.awaitingInput = AwaitingInput.MULTI_PERMANENT_CHOICE;
        gameData.awaitingMultiPermanentChoicePlayerId = playerId;
        gameData.awaitingMultiPermanentChoiceValidIds = new HashSet<>(validIds);
        gameData.awaitingMultiPermanentChoiceMaxCount = maxCount;
        sessionManager.sendToPlayer(playerId, new ChooseMultiplePermanentsMessage(validIds, maxCount, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} permanents", gameData.id, playerName, maxCount);
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
                    logAndBroadcast(gameData, logEntry);
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
                        removeOrphanedAuras(gameData);
                        String logEntry = String.join(", ", bouncedNames) + (bouncedNames.size() == 1 ? " is" : " are") + " returned to " + gameData.playerIdToName.get(targetPlayerId) + "'s hand.";
                        logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} bounced {} permanents", gameData.id, gameData.playerIdToName.get(playerId), bouncedNames.size());

                        broadcastBattlefields(gameData);
                        sessionManager.sendToPlayer(targetPlayerId, new HandDrawnMessage(targetHand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(targetPlayerId, 0)));
                    }
                }

                if (!gameData.pendingMayAbilities.isEmpty()) {
                    processNextMayAbility(gameData);
                    return;
                }

                advanceStep(gameData);
                resolveAutoPass(gameData);
            } else {
                throw new IllegalStateException("No pending multi-permanent choice context");
            }
        }
    }

    private boolean checkWinCondition(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            if (life <= 0) {
                UUID winnerId = getOpponentId(gameData, playerId);
                String winnerName = gameData.playerIdToName.get(winnerId);

                gameData.status = GameStatus.FINISHED;

                String logEntry = gameData.playerIdToName.get(playerId) + " has been defeated! " + winnerName + " wins!";
                logAndBroadcast(gameData, logEntry);

                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new GameOverMessage(winnerId, winnerName));

                gameRegistry.remove(gameData.id);

                log.info("Game {} - {} wins! {} is at {} life", gameData.id, winnerName,
                        gameData.playerIdToName.get(playerId), life);
                return true;
            }
        }
        return false;
    }

    private void clearCombatState(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                battlefield.forEach(Permanent::clearCombatState);
            }
        }
        broadcastBattlefields(gameData);
    }

    private void processEndOfCombatSacrifices(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                List<Permanent> toSacrifice = battlefield.stream()
                        .filter(p -> gameData.permanentsToSacrificeAtEndOfCombat.contains(p.getId()))
                        .toList();
                for (Permanent perm : toSacrifice) {
                    boolean wasCreature = isCreature(gameData, perm);
                    battlefield.remove(perm);
                    gameData.playerGraveyards.get(playerId).add(perm.getOriginalCard());
                    collectDeathTrigger(gameData, perm.getCard(), playerId, wasCreature);
                    String logEntry = perm.getCard().getName() + " is sacrificed.";
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
                    log.info("Game {} - {} sacrificed at end of combat", gameData.id, perm.getCard().getName());
                }
            }
        }
        gameData.permanentsToSacrificeAtEndOfCombat.clear();
        removeOrphanedAuras(gameData);
        broadcastBattlefields(gameData);
        broadcastGraveyards(gameData);
    }

    // ===== End combat methods =====

    private void drainManaPools(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                manaPool.clear();
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(manaPool.toMap()));
            }
        }
    }

    private Map<String, Integer> getManaPool(GameData data, UUID playerId) {
        if (playerId == null) {
            return new ManaPool().toMap();
        }
        ManaPool pool = data.playerManaPools.get(playerId);
        return pool != null ? pool.toMap() : new ManaPool().toMap();
    }

    private List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId) {
        List<Integer> playable = new ArrayList<>();
        if (gameData.status != GameStatus.RUNNING) {
            return playable;
        }

        UUID priorityHolder = getPriorityPlayerId(gameData);
        if (!playerId.equals(priorityHolder)) {
            return playable;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return playable;
        }

        boolean isActivePlayer = playerId.equals(gameData.activePlayerId);
        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(playerId, 0);
        int spellsCast = gameData.spellsCastThisTurn.getOrDefault(playerId, 0);
        int maxSpells = getMaxSpellsPerTurn(gameData);
        boolean spellLimitReached = spellsCast >= maxSpells;

        boolean stackEmpty = gameData.stack.isEmpty();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getType() == CardType.BASIC_LAND && isActivePlayer && isMainPhase && landsPlayed < 1 && stackEmpty) {
                playable.add(i);
            }
            if (card.getType() == CardType.CREATURE && card.getManaCost() != null && !spellLimitReached) {
                boolean hasFlash = card.getKeywords().contains(Keyword.FLASH);
                if (hasFlash || (isActivePlayer && isMainPhase && stackEmpty)) {
                    ManaCost cost = new ManaCost(card.getManaCost());
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.CREATURE);
                    if (cost.canPay(pool, additionalCost)) {
                        playable.add(i);
                    }
                }
            }
            if (card.getType() == CardType.ENCHANTMENT && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.ENCHANTMENT);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.ARTIFACT && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.ARTIFACT);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.SORCERY && isActivePlayer && isMainPhase && stackEmpty && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                // For X-cost spells, playable if player can pay the colored portion (X=0 minimum)
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.SORCERY);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
            if (card.getType() == CardType.INSTANT && card.getManaCost() != null && !spellLimitReached) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.INSTANT);
                if (cost.canPay(pool, additionalCost)) {
                    playable.add(i);
                }
            }
        }

        return playable;
    }

    private void broadcastPlayableCards(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Integer> playable = getPlayableCardIndices(gameData, playerId);
            sessionManager.sendToPlayer(playerId, new PlayableCardsMessage(playable));
        }
    }

    private void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature) {
        if (!wasCreature) return;

        List<CardEffect> deathEffects = dyingCard.getEffects(EffectSlot.ON_DEATH);
        if (deathEffects.isEmpty()) return;

        for (CardEffect effect : deathEffects) {
            if (effect instanceof MayEffect may) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        dyingCard,
                        controllerId,
                        List.of(may.wrapped()),
                        dyingCard.getName() + " — " + may.prompt()
                ));
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        dyingCard,
                        controllerId,
                        dyingCard.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
            }
        }
    }

    private void checkSpellCastTriggers(GameData gameData, Card spellCard) {
        if (spellCard.getColor() == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)) {
                    CardEffect inner = effect instanceof MayEffect m ? m.wrapped() : effect;

                    if (inner instanceof GainLifeOnColorSpellCastEffect trigger
                            && spellCard.getColor() == trigger.triggerColor()) {
                        List<CardEffect> resolvedEffects = List.of(new GainLifeEffect(trigger.amount()));

                        if (effect instanceof MayEffect may) {
                            gameData.pendingMayAbilities.add(new PendingMayAbility(
                                    perm.getCard(),
                                    playerId,
                                    resolvedEffects,
                                    perm.getCard().getName() + " — " + may.prompt()
                            ));
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    playerId,
                                    perm.getCard().getName() + "'s ability",
                                    new ArrayList<>(resolvedEffects)
                            ));
                        }
                    }
                }
            }
        }

        processNextMayAbility(gameData);
    }

    private void processNextMayAbility(GameData gameData) {
        if (gameData.pendingMayAbilities.isEmpty()) {
            return;
        }

        PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
        gameData.awaitingInput = AwaitingInput.MAY_ABILITY_CHOICE;
        gameData.awaitingMayAbilityPlayerId = next.controllerId();
        sessionManager.sendToPlayer(next.controllerId(), new MayAbilityMessage(next.description()));

        String playerName = gameData.playerIdToName.get(next.controllerId());
        log.info("Game {} - Awaiting {} to decide on may ability: {}", gameData.id, playerName, next.description());
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
                    beginPermanentChoice(gameData, ability.controllerId(), creatureIds, "Choose a creature to copy.");

                    String logEntry = player.getUsername() + " accepts — choosing a creature to copy.";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} accepts clone copy", gameData.id, player.getUsername());
                } else {
                    gameData.permanentChoiceContext = null;
                    String logEntry = player.getUsername() + " declines to copy a creature. Clone enters as 0/0.";
                    logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} declines clone copy", gameData.id, player.getUsername());

                    performStateBasedActions(gameData);
                    broadcastBattlefields(gameData);
                    broadcastGraveyards(gameData);
                    broadcastPlayableCards(gameData);
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
                broadcastStackUpdate(gameData);

                String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName() + "'s triggered ability goes on the stack.";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} accepts may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
            } else {
                String logEntry = player.getUsername() + " declines " + ability.sourceCard().getName() + "'s triggered ability.";
                logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
            }

            processNextMayAbility(gameData);

            if (gameData.pendingMayAbilities.isEmpty() && gameData.awaitingInput == null) {
                gameData.priorityPassedBy.clear();
                broadcastBattlefields(gameData);
                broadcastStackUpdate(gameData);
                broadcastPlayableCards(gameData);
                sessionManager.sendToPlayers(gameData.orderedPlayerIds, new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
                resolveAutoPass(gameData);
            }
        }
    }

    private void resolveAutoPass(GameData gameData) {
        for (int safety = 0; safety < 100; safety++) {
            if (gameData.awaitingInput != null) {
                broadcastPlayableCards(gameData);
                return;
            }
            if (gameData.status == GameStatus.FINISHED) return;

            // When stack is non-empty, never auto-pass — players must explicitly pass
            if (!gameData.stack.isEmpty()) {
                broadcastPlayableCards(gameData);
                return;
            }

            UUID priorityHolder = getPriorityPlayerId(gameData);

            // If no one holds priority (both already passed), advance the step
            if (priorityHolder == null) {
                advanceStep(gameData);
                continue;
            }

            List<Integer> playable = getPlayableCardIndices(gameData, priorityHolder);
            if (!playable.isEmpty()) {
                // Priority holder can act — stop and let them decide
                broadcastPlayableCards(gameData);
                return;
            }

            // Check if current step is in the priority holder's auto-stop set
            Set<TurnStep> stopSteps = gameData.playerAutoStopSteps.get(priorityHolder);
            if (stopSteps != null && stopSteps.contains(gameData.currentStep)) {
                broadcastPlayableCards(gameData);
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
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));
            }
        }

        // Safety: if we somehow looped 100 times, broadcast current state and stop
        log.warn("Game {} - resolveAutoPass hit safety limit", gameData.id);
        broadcastPlayableCards(gameData);
    }

    private void resolveCounterSpell(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter target no longer on stack", gameData.id);
            return;
        }

        gameData.stack.remove(targetEntry);

        // Countered spells go to their owner's graveyard
        UUID ownerId = targetEntry.getControllerId();
        gameData.playerGraveyards.get(ownerId).add(targetEntry.getCard());
        broadcastGraveyards(gameData);
        broadcastStackUpdate(gameData);

        String logMsg = targetEntry.getCard().getName() + " is countered.";
        logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered {}", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());
    }

    private void resolvePlagiarize(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        UUID controllerId = entry.getControllerId();

        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            log.warn("Game {} - Plagiarize target player not found", gameData.id);
            return;
        }

        gameData.drawReplacementTargetToController.put(targetPlayerId, controllerId);

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "Plagiarize resolves targeting " + targetName
                + ". Until end of turn, " + targetName + "'s draws are replaced.";
        logAndBroadcast(gameData, logEntry);
        log.info("Game {} - Plagiarize: {}'s draws replaced by {} until end of turn",
                gameData.id, targetName, controllerName);
    }

    private void resolveReorderTopCardsOfLibrary(GameData gameData, StackEntry entry, ReorderTopCardsOfLibraryEffect reorder) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        int count = Math.min(reorder.count(), deck.size());
        if (count == 0) {
            String logMsg = entry.getCard().getName() + ": library is empty, nothing to reorder.";
            logAndBroadcast(gameData, logMsg);
            return;
        }

        if (count == 1) {
            // Only one card — no choice needed, it stays where it is
            String logMsg = gameData.playerIdToName.get(controllerId) + " looks at the top card of their library.";
            logAndBroadcast(gameData, logMsg);
            return;
        }

        // Take the top N cards (index 0 = top of library)
        List<Card> topCards = new ArrayList<>(deck.subList(0, count));

        gameData.awaitingLibraryReorderPlayerId = controllerId;
        gameData.awaitingLibraryReorderCards = topCards;
        gameData.awaitingInput = AwaitingInput.LIBRARY_REORDER;

        List<CardView> cardViews = topCards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(controllerId, new ReorderLibraryCardsMessage(
                cardViews,
                "Put these cards back on top of your library in any order (top to bottom)."
        ));

        String logMsg = gameData.playerIdToName.get(controllerId) + " looks at the top " + count + " cards of their library.";
        logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} reordering top {} cards of library", gameData.id, gameData.playerIdToName.get(controllerId), count);
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
            logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} reordered top {} cards", gameData.id, player.getUsername(), count);

            resolveAutoPass(gameData);
        }
    }

}

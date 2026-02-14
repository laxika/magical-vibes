package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.message.AutoStopsUpdatedMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
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
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetZone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
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
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
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
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
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
        gameData.priorityPassedBy.clear();
        TurnStep next = gameData.currentStep.next();

        drainManaPools(gameData);

        if (next != null) {
            gameData.currentStep = next;
            String logEntry = "Step: " + next.getDisplayName();
            gameData.gameLog.add(logEntry);
            log.info("Game {} - Step advanced to {}", gameData.id, next);

            broadcastLogEntry(gameData, logEntry);
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
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
                    log.info("Game {} - {} upkeep trigger pushed onto stack", gameData.id, perm.getCard().getName());
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} skips draw on turn 1", gameData.id, gameData.playerIdToName.get(activePlayerId));
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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);

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

        // Untap all permanents for the new active player
        List<Permanent> battlefield = gameData.playerBattlefields.get(nextActive);
        if (battlefield != null) {
            battlefield.forEach(Permanent::untap);
            battlefield.forEach(p -> p.setSummoningSick(false));
        }
        broadcastBattlefields(gameData);

        String untapLog = nextActiveName + " untaps their permanents.";
        gameData.gameLog.add(untapLog);
        broadcastLogEntry(gameData, untapLog);
        log.info("Game {} - {} untaps their permanents", gameData.id, nextActiveName);

        String logEntry = "Turn " + gameData.turnNumber + " begins. " + nextActiveName + "'s turn.";
        gameData.gameLog.add(logEntry);
        log.info("Game {} - Turn {} begins. Active player: {}", gameData.id, gameData.turnNumber, nextActiveName);

        broadcastLogEntry(gameData, logEntry);
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new TurnChangedMessage(
                getPriorityPlayerId(gameData), TurnStep.first(), nextActive, gameData.turnNumber
        ));
    }

    private void broadcastLogEntry(GameData gameData, String logEntry) {
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,new GameLogEntryMessage(logEntry));
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

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
                    gameData.gameLog.add(fizzleLog);
                    broadcastLogEntry(gameData, fizzleLog);
                    gameData.playerGraveyards.get(controllerId).add(card);
                    broadcastGraveyards(gameData);
                    log.info("Game {} - {} fizzles, target {} no longer exists", gameData.id, card.getName(), entry.getTargetPermanentId());
                } else {
                    Permanent perm = new Permanent(card);
                    perm.setAttachedTo(entry.getTargetPermanentId());
                    gameData.playerBattlefields.get(controllerId).add(perm);
                    broadcastBattlefields(gameData);

                    String playerName = gameData.playerIdToName.get(controllerId);
                    String logEntry = card.getName() + " enters the battlefield attached to " + target.getCard().getName() + " under " + playerName + "'s control.";
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
                    log.info("Game {} - {} resolves, attached to {} for {}", gameData.id, card.getName(), target.getCard().getName(), playerName);
                }
            } else {
                gameData.playerBattlefields.get(controllerId).add(new Permanent(card));
                broadcastBattlefields(gameData);

                String playerName = gameData.playerIdToName.get(controllerId);
                String logEntry = card.getName() + " enters the battlefield under " + playerName + "'s control.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

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
                } else {
                    targetFizzled = findPermanentById(gameData, entry.getTargetPermanentId()) == null
                            && !gameData.playerIds.contains(entry.getTargetPermanentId());
                }
            }
            if (targetFizzled) {
                String fizzleLog = entry.getDescription() + " fizzles (target no longer exists).";
                gameData.gameLog.add(fizzleLog);
                broadcastLogEntry(gameData, fizzleLog);
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
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
            } else if (effect instanceof PreventDamageToTargetEffect prevent) {
                resolvePreventDamageToTarget(gameData, entry, prevent);
            } else if (effect instanceof PreventNextDamageEffect prevent) {
                resolvePreventNextDamage(gameData, prevent);
            } else if (effect instanceof DrawCardEffect drawCard) {
                resolveDrawCards(gameData, entry.getControllerId(), drawCard.amount());
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
                gameData.gameLog.add(shuffleLog);
                broadcastLogEntry(gameData, shuffleLog);
            } else if (effect instanceof GainLifeEqualToTargetToughnessEffect) {
                resolveGainLifeEqualToTargetToughness(gameData, entry);
            } else if (effect instanceof PutTargetOnBottomOfLibraryEffect) {
                resolvePutTargetOnBottomOfLibrary(gameData, entry);
            } else if (effect instanceof DestroyBlockedCreatureAndSelfEffect) {
                resolveDestroyBlockedCreatureAndSelf(gameData, entry);
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
                resolveReturnCreatureFromGraveyardToBattlefield(gameData, entry);
            } else if (effect instanceof ReturnArtifactFromGraveyardToHandEffect) {
                resolveReturnArtifactFromGraveyardToHand(gameData, entry);
            } else if (effect instanceof RegenerateEffect) {
                resolveRegenerate(gameData, entry);
            } else if (effect instanceof TapTargetCreatureEffect) {
                resolveTapTargetCreature(gameData, entry);
            } else if (effect instanceof TapTargetPermanentEffect) {
                resolveTapTargetPermanent(gameData, entry);
            } else if (effect instanceof PreventNextColorDamageToControllerEffect prevent) {
                resolvePreventNextColorDamageToController(gameData, entry, prevent);
            } else if (effect instanceof PutAuraFromHandOntoSelfEffect) {
                resolvePutAuraFromHandOntoSelf(gameData, entry);
            } else if (effect instanceof MillTargetPlayerEffect mill) {
                resolveMillTargetPlayer(gameData, entry, mill);
            } else if (effect instanceof RevealTopCardOfLibraryEffect) {
                resolveRevealTopCardOfLibrary(gameData, entry);
            } else if (effect instanceof GainControlOfTargetAuraEffect) {
                resolveGainControlOfTargetAura(gameData, entry);
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} kept hand, needs to bottom {} cards (mulligan count: {})", gameData.id, player.getUsername(), cardsToBottom, mulliganCount);
            } else {
                String logEntry = player.getUsername() + " keeps their hand.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

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
        gameData.gameLog.add(logEntry1);
        gameData.gameLog.add(logEntry2);
        broadcastLogEntry(gameData, logEntry1);
        broadcastLogEntry(gameData, logEntry2);

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
                    views.add(permanentViewFactory.create(p, bonus.power(), bonus.toughness(), bonus.keywords()));
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

            // Validate target if specified (can be a permanent or a player)
            if (targetPermanentId != null) {
                Permanent target = findPermanentById(gameData, targetPermanentId);
                if (target == null && !gameData.playerIds.contains(targetPermanentId)) {
                    throw new IllegalStateException("Invalid target");
                }

                // Protection validation
                if (target != null && card.isNeedsTarget() && hasProtectionFrom(gameData, target, card.getColor())) {
                    throw new IllegalStateException(target.getCard().getName() + " has protection from " + card.getColor().name().toLowerCase());
                }

                // Player shroud validation
                if (target == null && card.isNeedsTarget() && gameData.playerIds.contains(targetPermanentId)
                        && playerHasShroud(gameData, targetPermanentId)) {
                    throw new IllegalStateException(gameData.playerIdToName.get(targetPermanentId) + " has shroud and can't be targeted");
                }

                // Effect-specific target validation
                for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
                    if (effect instanceof PutTargetOnBottomOfLibraryEffect) {
                        if (target == null || target.getCard().getType() != CardType.CREATURE || !target.isAttacking()) {
                            throw new IllegalStateException("Target must be an attacking creature");
                        }
                    }
                    if (effect instanceof BoostTargetBlockingCreatureEffect) {
                        if (target == null || target.getCard().getType() != CardType.CREATURE || !target.isBlocking()) {
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} plays {}", gameData.id, player.getUsername(), card.getName());

                resolveAutoPass(gameData);
            } else if (card.getType() == CardType.CREATURE) {
                // Creatures go on the stack
                if (card.getManaCost() != null) {
                    ManaCost cost = new ManaCost(card.getManaCost());
                    ManaPool pool = gameData.playerManaPools.get(playerId);
                    int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.CREATURE);
                    cost.pay(pool, additionalCost);
                    sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.CREATURE_SPELL, card, playerId, card.getName(),
                        List.of(), 0, targetPermanentId, null
                ));
                gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
                gameData.priorityPassedBy.clear();

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastStackUpdate(gameData);
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

                String logEntry = player.getUsername() + " casts " + card.getName() + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

                checkSpellCastTriggers(gameData, card);
                resolveAutoPass(gameData);
            } else if (card.getType() == CardType.ENCHANTMENT) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.ENCHANTMENT);
                cost.pay(pool, additionalCost);
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));

                gameData.stack.add(new StackEntry(
                        StackEntryType.ENCHANTMENT_SPELL, card, playerId, card.getName(),
                        List.of(), 0, targetPermanentId, null
                ));
                gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
                gameData.priorityPassedBy.clear();

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastStackUpdate(gameData);
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

                String logEntry = player.getUsername() + " casts " + card.getName() + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

                checkSpellCastTriggers(gameData, card);
                resolveAutoPass(gameData);
            } else if (card.getType() == CardType.ARTIFACT) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.ARTIFACT);
                cost.pay(pool, additionalCost);
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));

                gameData.stack.add(new StackEntry(
                        StackEntryType.ARTIFACT_SPELL, card, playerId, card.getName(),
                        List.of(), 0, null, null
                ));
                gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
                gameData.priorityPassedBy.clear();

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastStackUpdate(gameData);
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

                String logEntry = player.getUsername() + " casts " + card.getName() + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

                checkSpellCastTriggers(gameData, card);
                resolveAutoPass(gameData);
            } else if (card.getType() == CardType.SORCERY) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.SORCERY);
                cost.pay(pool, effectiveXValue + additionalCost);
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));

                gameData.stack.add(new StackEntry(
                        StackEntryType.SORCERY_SPELL, card, playerId, card.getName(),
                        new ArrayList<>(card.getEffects(EffectSlot.SPELL)), effectiveXValue, targetPermanentId, null
                ));
                gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
                gameData.priorityPassedBy.clear();

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastStackUpdate(gameData);
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

                String logEntry = player.getUsername() + " casts " + card.getName() + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

                checkSpellCastTriggers(gameData, card);
                resolveAutoPass(gameData);
            } else if (card.getType() == CardType.INSTANT) {
                ManaCost cost = new ManaCost(card.getManaCost());
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = getOpponentCostIncrease(gameData, playerId, CardType.INSTANT);
                if (cost.hasX()) {
                    cost.pay(pool, effectiveXValue + additionalCost);
                } else {
                    cost.pay(pool, additionalCost);
                }
                sessionManager.sendToPlayer(playerId, new ManaUpdatedMessage(pool.toMap()));

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
                        if (target == null || target.getCard().getType() != CardType.CREATURE || !target.isAttacking()) {
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
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.INSTANT_SPELL, card, playerId, card.getName(),
                            new ArrayList<>(card.getEffects(EffectSlot.SPELL)), effectiveXValue, targetPermanentId, null
                    ));
                }
                gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
                gameData.priorityPassedBy.clear();

                sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                broadcastStackUpdate(gameData);
                sessionManager.sendToPlayers(gameData.orderedPlayerIds,new PriorityUpdatedMessage(getPriorityPlayerId(gameData)));

                String logEntry = player.getUsername() + " casts " + card.getName() + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

                log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

                checkSpellCastTriggers(gameData, card);
                resolveAutoPass(gameData);
            }
        }
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
            if (permanent.isSummoningSick() && permanent.getCard().getType() == CardType.CREATURE) {
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

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
            battlefield.remove(permanentIndex);
            gameData.playerGraveyards.get(playerId).add(permanent.getCard());
            collectDeathTrigger(gameData, permanent.getCard(), playerId);
            removeOrphanedAuras(gameData);

            String logEntry = player.getUsername() + " sacrifices " + permanent.getCard().getName() + ".";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
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
                if (permanent.isSummoningSick() && permanent.getCard().getType() == CardType.CREATURE) {
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
                    if (target.getCard().getType() != CardType.CREATURE) {
                        throw new IllegalStateException("Target must be a creature");
                    }
                    if (!target.isAttacking() && !target.isBlocking()) {
                        throw new IllegalStateException("Target must be an attacking or blocking creature");
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

            // Player shroud validation for abilities
            if (targetPermanentId != null && gameData.playerIds.contains(targetPermanentId)
                    && playerHasShroud(gameData, targetPermanentId)) {
                throw new IllegalStateException(gameData.playerIdToName.get(targetPermanentId) + " has shroud and can't be targeted");
            }

            // Self-target if effects need the source permanent
            UUID effectiveTargetId = targetPermanentId;
            if (effectiveTargetId == null) {
                boolean needsSelfTarget = abilityEffects.stream().anyMatch(e ->
                        e instanceof RegenerateEffect || e instanceof BoostSelfEffect);
                if (needsSelfTarget) {
                    effectiveTargetId = permanent.getId();
                }
            }

            // Tap the permanent (only for tap abilities)
            if (isTapAbility) {
                permanent.tap();
            }

            String logEntry = player.getUsername() + " activates " + permanent.getCard().getName() + "'s ability.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
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
            gameData.gameLog.add(fizzleLog);
            broadcastLogEntry(gameData, fizzleLog);
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastLifeTotals(gameData);
        log.info("Game {} - {} gains {} life", gameData.id, playerName, amount);
    }

    private void resolveGainLifePerGraveyardCard(GameData gameData, UUID controllerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int amount = graveyard != null ? graveyard.size() : 0;
        if (amount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " has no cards in their graveyard.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} has no graveyard cards for life gain", gameData.id, playerName);
            return;
        }
        resolveGainLife(gameData, controllerId, amount);
    }

    private void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId) {
        // "As enters" effects — require player choice before any ETB triggers fire
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
                    gameData.gameLog.add(etbLog);
                    broadcastLogEntry(gameData, etbLog);
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
                    gameData.gameLog.add(triggerLog);
                    broadcastLogEntry(gameData, triggerLog);
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
                        gameData.gameLog.add(triggerLog);
                        broadcastLogEntry(gameData, triggerLog);
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
                log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), color, perm.getCard().getName());

                if (perm.getCard().getType() == CardType.CREATURE) {
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
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

    private void resolveTargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card, UUID targetPermanentId) {
        Permanent target = findPermanentById(gameData, targetPermanentId);
        if (target != null) {
            Permanent auraPerm = new Permanent(card);
            auraPerm.setAttachedTo(target.getId());
            gameData.playerBattlefields.get(playerId).add(auraPerm);

            sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
            broadcastBattlefields(gameData);

            String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield attached to " + target.getCard().getName() + ".";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} puts {} onto the battlefield attached to {}", gameData.id, player.getUsername(), card.getName(), target.getCard().getName());
        } else {
            hand.add(card);
            String logEntry = card.getName() + " can't be attached (target left the battlefield).";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - Aura target gone, {} returned to hand", gameData.id, card.getName());
        }
    }

    private void resolveUntargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card) {
        gameData.playerBattlefields.get(playerId).add(new Permanent(card));

        sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
        broadcastBattlefields(gameData);

        String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, target.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    private void resolveBoostAllOwnCreatures(GameData gameData, StackEntry entry, BoostAllOwnCreaturesEffect boost) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getType() == CardType.CREATURE) {
                permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
                permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " gives +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " to " + count + " creature(s) until end of turn.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} gains {}", gameData.id, target.getCard().getName(), grant.keyword());
    }

    private void resolveDealXDamageToTargetCreature(GameData gameData, StackEntry entry) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (isDamageFromSourcePrevented(gameData, entry.getCard().getColor())
                || hasProtectionFrom(gameData, target, entry.getCard().getColor())) {
            String logEntry = entry.getCard().getName() + "'s damage is prevented.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            return;
        }

        int damage = applyCreaturePreventionShield(gameData, target, entry.getXValue());
        String logEntry = entry.getCard().getName() + " deals " + damage + " damage to " + target.getCard().getName() + ".";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        log.info("Game {} - {} deals {} damage to {}", gameData.id, entry.getCard().getName(), damage, target.getCard().getName());

        if (damage >= getEffectiveToughness(gameData, target)) {
            if (tryRegenerate(gameData, target)) {
                broadcastBattlefields(gameData);
            } else {
                // Destroy the creature
                for (UUID playerId : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield != null && battlefield.remove(target)) {
                        gameData.playerGraveyards.get(playerId).add(target.getCard());
                        collectDeathTrigger(gameData, target.getCard(), playerId);

                        String destroyLog = target.getCard().getName() + " is destroyed.";
                        gameData.gameLog.add(destroyLog);
                        broadcastLogEntry(gameData, destroyLog);
                        log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
                        break;
                    }
                }
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} deals {} damage to {}", gameData.id, entry.getCard().getName(), damage, target.getCard().getName());

            if (damage >= target.getEffectiveToughness()) {
                if (!tryRegenerate(gameData, target)) {
                    destroyed.add(target);
                }
            }
        }

        for (Permanent target : destroyed) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield != null && battlefield.remove(target)) {
                    gameData.playerGraveyards.get(playerId).add(target.getCard());
                    collectDeathTrigger(gameData, target.getCard(), playerId);

                    String destroyLog = target.getCard().getName() + " is destroyed.";
                    gameData.gameLog.add(destroyLog);
                    broadcastLogEntry(gameData, destroyLog);
                    log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
                    break;
                }
            }
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
                if (perm.getCard().getType() == CardType.CREATURE) {
                    toDestroy.add(perm);
                }
            }
        }

        for (Permanent perm : toDestroy) {
            if (!cannotBeRegenerated && tryRegenerate(gameData, perm)) {
                continue;
            }
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield != null && battlefield.remove(perm)) {
                    gameData.playerGraveyards.get(playerId).add(perm.getCard());
                    collectDeathTrigger(gameData, perm.getCard(), playerId);

                    String logEntry = perm.getCard().getName() + " is destroyed.";
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
                    log.info("Game {} - {} is destroyed", gameData.id, perm.getCard().getName());
                    break;
                }
            }
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
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield != null && battlefield.remove(perm)) {
                    gameData.playerGraveyards.get(playerId).add(perm.getCard());

                    String logEntry = perm.getCard().getName() + " is destroyed.";
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
                    log.info("Game {} - {} is destroyed", gameData.id, perm.getCard().getName());
                    break;
                }
            }
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
            gameData.gameLog.add(fizzleLog);
            broadcastLogEntry(gameData, fizzleLog);
            log.info("Game {} - {}'s ability fizzles, target type mismatch", gameData.id, entry.getCard().getName());
            return;
        }

        // Try regeneration for creatures
        if (target.getCard().getType() == CardType.CREATURE && tryRegenerate(gameData, target)) {
            broadcastBattlefields(gameData);
            return;
        }

        // Find which player controls the target and remove it
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                gameData.playerGraveyards.get(playerId).add(target.getCard());
                collectDeathTrigger(gameData, target.getCard(), playerId);

                String logEntry = target.getCard().getName() + " is destroyed.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
                log.info("Game {} - {} is destroyed by {}'s ability",
                        gameData.id, target.getCard().getName(), entry.getCard().getName());
                break;
            }
        }

        removeOrphanedAuras(gameData);
        broadcastBattlefields(gameData);
        broadcastGraveyards(gameData);
    }

    private void resolveDestroyBlockedCreatureAndSelf(GameData gameData, StackEntry entry) {
        // Destroy the blocked creature (attacker) — referenced by targetPermanentId
        Permanent attacker = findPermanentById(gameData, entry.getTargetPermanentId());
        if (attacker != null && !tryRegenerate(gameData, attacker)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield != null && battlefield.remove(attacker)) {
                    gameData.playerGraveyards.get(playerId).add(attacker.getCard());
                    collectDeathTrigger(gameData, attacker.getCard(), playerId);
                    String logEntry = attacker.getCard().getName() + " is destroyed by " + entry.getCard().getName() + ".";
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
                    log.info("Game {} - {} destroyed by {}'s block trigger", gameData.id, attacker.getCard().getName(), entry.getCard().getName());
                    break;
                }
            }
        }

        // Destroy self (the blocker) — referenced by sourcePermanentId
        Permanent self = findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null && !tryRegenerate(gameData, self)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield != null && battlefield.remove(self)) {
                    gameData.playerGraveyards.get(playerId).add(self.getCard());
                    collectDeathTrigger(gameData, self.getCard(), playerId);
                    String logEntry = entry.getCard().getName() + " is destroyed.";
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
                    log.info("Game {} - {} destroyed (self-destruct from block trigger)", gameData.id, entry.getCard().getName());
                    break;
                }
            }
        }

        broadcastBattlefields(gameData);
        broadcastGraveyards(gameData);
    }

    private void resolvePreventDamageToTarget(GameData gameData, StackEntry entry, PreventDamageToTargetEffect prevent) {
        UUID targetId = entry.getTargetPermanentId();

        // Check if target is a permanent
        Permanent target = findPermanentById(gameData, targetId);
        if (target != null) {
            target.setDamagePreventionShield(target.getDamagePreventionShield() + prevent.amount());

            String logEntry = "The next " + prevent.amount() + " damage that would be dealt to " + target.getCard().getName() + " is prevented.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - Prevention shield {} added to permanent {}", gameData.id, prevent.amount(), target.getCard().getName());
            return;
        }

        // Check if target is a player
        if (gameData.playerIds.contains(targetId)) {
            int currentShield = gameData.playerDamagePreventionShields.getOrDefault(targetId, 0);
            gameData.playerDamagePreventionShields.put(targetId, currentShield + prevent.amount());

            String playerName = gameData.playerIdToName.get(targetId);
            String logEntry = "The next " + prevent.amount() + " damage that would be dealt to " + playerName + " is prevented.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - Prevention shield {} added to player {}", gameData.id, prevent.amount(), playerName);
        }
    }

    private void resolvePreventNextDamage(GameData gameData, PreventNextDamageEffect prevent) {
        gameData.globalDamagePreventionShield += prevent.amount();

        String logEntry = "The next " + prevent.amount() + " damage that would be dealt to any permanent or player is prevented.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
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
        List<Card> deck = gameData.playerDecks.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);

        if (deck == null || deck.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to draw.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            return;
        }

        Card drawn = deck.removeFirst();
        hand.add(drawn);

        sessionManager.sendToPlayer(playerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
        broadcastDeckSizes(gameData);

        String logEntry = gameData.playerIdToName.get(playerId) + " draws a card.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        log.info("Game {} - {} draws a card from effect", gameData.id, gameData.playerIdToName.get(playerId));
    }

    private void resolveDrawCards(GameData gameData, UUID playerId, int amount) {
        for (int i = 0; i < amount; i++) {
            resolveDrawCard(gameData, playerId);
        }
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            return;
        }

        battlefield.remove(toReturn);
        removeOrphanedAuras(gameData);
        hand.add(toReturn.getCard());

        String logEntry = entry.getCard().getName() + " is returned to its owner's hand.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());

        broadcastBattlefields(gameData);
        sessionManager.sendToPlayer(controllerId, new HandDrawnMessage(hand.stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(controllerId, 0)));
    }

    private void resolveDoubleTargetPlayerLife(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();

        int currentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
        int newLife = currentLife * 2;
        gameData.playerLifeTotals.put(targetPlayerId, newLife);

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + "'s life total is doubled from " + currentLife + " to " + newLife + ".";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastLifeTotals(gameData);
        log.info("Game {} - {}'s life doubled from {} to {}", gameData.id, playerName, currentLife, newLife);
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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastDeckSizes(gameData);
        broadcastGraveyards(gameData);
        log.info("Game {} - {} mills {} cards", gameData.id, playerName, cardsToMill);
    }

    private void resolveRevealTopCardOfLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
        } else {
            Card topCard = deck.getFirst();
            String logEntry = playerName + " reveals " + topCard.getName() + " from the top of their library.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} gains control of {}", gameData.id, casterName, aura.getCard().getName());
        }

        // Step 2: Attach it to another permanent it can enchant
        // Collect all creature permanents across ALL battlefields, excluding the one it's currently attached to
        List<UUID> validCreatureIds = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getCard().getType() == CardType.CREATURE && !p.getId().equals(aura.getAttachedTo())) {
                    validCreatureIds.add(p.getId());
                }
            }
        }

        if (!validCreatureIds.isEmpty()) {
            gameData.pendingAuraGraftPermanentId = aura.getId();
            beginPermanentChoice(gameData, casterId, validCreatureIds,
                    "Attach " + aura.getCard().getName() + " to another permanent it can enchant.");
        } else {
            // No other valid creatures — aura stays attached as-is
            String logEntry = aura.getCard().getName() + " stays attached to its current target (no other valid permanents).";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
        }

        broadcastBattlefields(gameData);
        broadcastPlayableCards(gameData);
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
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
                gameData.playerDecks.get(playerId).add(target.getCard());

                String logEntry = target.getCard().getName() + " is put on the bottom of "
                        + gameData.playerIdToName.get(playerId) + "'s library.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
    }

    private void resolvePreventDamageFromColors(GameData gameData, PreventDamageFromColorsEffect effect) {
        gameData.preventDamageFromColors.addAll(effect.colors());

        String colorNames = effect.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " and " + b)
                .orElse("");
        String logEntry = "All damage from " + colorNames + " sources will be prevented this turn.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
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
            gameData.gameLog.add(fizzleLog);
            broadcastLogEntry(gameData, fizzleLog);
            return;
        }

        // Check if controller has any creatures to attach to
        List<Permanent> controllerBf = gameData.playerBattlefields.get(controllerId);
        List<UUID> creatureIds = new ArrayList<>();
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (p.getCard().getType() == CardType.CREATURE) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String fizzleLog = entry.getDescription() + " fizzles (no creatures to attach Aura to).";
            gameData.gameLog.add(fizzleLog);
            broadcastLogEntry(gameData, fizzleLog);
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
                gameData.pendingLegendRuleCardName = entry.getKey();
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

            if (gameData.pendingAuraGraftPermanentId != null) {
                UUID auraId = gameData.pendingAuraGraftPermanentId;
                gameData.pendingAuraGraftPermanentId = null;

                Permanent aura = findPermanentById(gameData, auraId);
                if (aura == null) {
                    throw new IllegalStateException("Aura permanent no longer exists");
                }

                Permanent newTarget = findPermanentById(gameData, permanentId);
                if (newTarget == null) {
                    throw new IllegalStateException("Target permanent no longer exists");
                }

                aura.setAttachedTo(permanentId);

                String logEntry = aura.getCard().getName() + " is now attached to " + newTarget.getCard().getName() + ".";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
                log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(), newTarget.getCard().getName());

                broadcastBattlefields(gameData);
                broadcastPlayableCards(gameData);

                resolveAutoPass(gameData);
            } else if (gameData.pendingLegendRuleCardName != null) {
                // Legend rule: keep chosen permanent, move all others with the same name to graveyard
                String legendName = gameData.pendingLegendRuleCardName;
                gameData.pendingLegendRuleCardName = null;

                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                List<Permanent> toRemove = new ArrayList<>();
                for (Permanent perm : battlefield) {
                    if (perm.getCard().getName().equals(legendName) && !perm.getId().equals(permanentId)) {
                        toRemove.add(perm);
                    }
                }
                for (Permanent perm : toRemove) {
                    battlefield.remove(perm);
                    gameData.playerGraveyards.get(playerId).add(perm.getCard());
                    collectDeathTrigger(gameData, perm.getCard(), playerId);
                    String logEntry = perm.getCard().getName() + " is put into the graveyard (legend rule).";
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
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

    private void resolveReturnCreatureFromGraveyardToBattlefield(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no creature cards in graveyard.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            return;
        }

        List<Integer> creatureIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getType() == CardType.CREATURE) {
                creatureIndices.add(i);
            }
        }

        if (creatureIndices.isEmpty()) {
            String logEntry = entry.getDescription() + " — no creature cards in graveyard.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            return;
        }

        gameData.graveyardChoiceDestination = GraveyardChoiceDestination.BATTLEFIELD;
        beginGraveyardChoice(gameData, controllerId, creatureIndices,
                "You may return a creature card from your graveyard to the battlefield.");
    }

    private void resolveReturnArtifactFromGraveyardToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);

        if (graveyard == null || graveyard.isEmpty()) {
            String logEntry = entry.getDescription() + " — no artifact cards in graveyard.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            return;
        }

        List<Integer> artifactIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getType() == CardType.ARTIFACT) {
                artifactIndices.add(i);
            }
        }

        if (artifactIndices.isEmpty()) {
            String logEntry = entry.getDescription() + " — no artifact cards in graveyard.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            return;
        }

        gameData.graveyardChoiceDestination = GraveyardChoiceDestination.HAND;
        beginGraveyardChoice(gameData, controllerId, artifactIndices,
                "You may return an artifact card from your graveyard to your hand.");
    }

    private void resolveTapTargetCreature(GameData gameData, StackEntry entry) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.tap();

        String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    private void resolveTapTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.tap();

        String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastBattlefields(gameData);

        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
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
                        gameData.gameLog.add(logEntry);
                        broadcastLogEntry(gameData, logEntry);
                        log.info("Game {} - {} returns {} from graveyard to hand", gameData.id, player.getUsername(), card.getName());

                        broadcastGraveyards(gameData);
                        sessionManager.sendToPlayer(playerId, new HandDrawnMessage(gameData.playerHands.get(playerId).stream().map(cardViewFactory::create).toList(), gameData.mulliganCounts.getOrDefault(playerId, 0)));
                    }
                    case BATTLEFIELD -> {
                        Permanent perm = new Permanent(card);
                        gameData.playerBattlefields.get(playerId).add(perm);

                        String logEntry = player.getUsername() + " returns " + card.getName() + " from graveyard to the battlefield.";
                        gameData.gameLog.add(logEntry);
                        broadcastLogEntry(gameData, logEntry);
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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
        broadcastBattlefields(gameData);

        handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null);
        if (gameData.awaitingInput == null) {
            checkLegendRule(gameData, controllerId);
        }

        log.info("Game {} - {} token created for player {}", gameData.id, token.tokenName(), controllerId);
    }

    private int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage) {
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(e -> e instanceof PreventAllDamageEffect)) return 0;
        if (hasAuraPreventingAllDamage(gameData, permanent)) return 0;
        damage = applyGlobalPreventionShield(gameData, damage);
        int shield = permanent.getDamagePreventionShield();
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        permanent.setDamagePreventionShield(shield - prevented);
        return damage - prevented;
    }

    private boolean hasAuraPreventingAllDamage(GameData gameData, Permanent creature) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getAttachedTo() != null && p.getAttachedTo().equals(creature.getId())) {
                    for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof PreventAllDamageToAndByEnchantedCreatureEffect) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasAuraPreventingAttackOrBlock(GameData gameData, Permanent creature) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getAttachedTo() != null && p.getAttachedTo().equals(creature.getId())) {
                    for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof EnchantedCreatureCantAttackOrBlockEffect) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isPreventedFromDealingDamage(GameData gameData, Permanent creature) {
        return hasAuraPreventingAllDamage(gameData, creature)
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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);

        if (effectiveDamage >= getEffectiveToughness(gameData, target)) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(pid);
                if (bf != null && bf.remove(target)) {
                    gameData.playerGraveyards.get(pid).add(target.getCard());
                    collectDeathTrigger(gameData, target.getCard(), pid);
                    String deathLog = target.getCard().getName() + " is destroyed by redirected " + sourceName + " damage.";
                    gameData.gameLog.add(deathLog);
                    broadcastLogEntry(gameData, deathLog);
                    break;
                }
            }
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
        List<String> colors = List.of("W", "U", "B", "R", "G");
        int remaining = amount;
        while (remaining > 0) {
            String highestColor = null;
            int highestAmount = 0;
            for (String color : colors) {
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
                    gameData.playerGraveyards.get(playerId).add(p.getCard());
                    String logEntry = p.getCard().getName() + " is put into the graveyard (enchanted creature left the battlefield).";
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
                    log.info("Game {} - {} removed (orphaned aura)", gameData.id, p.getCard().getName());
                    anyRemoved = true;
                }
            }
        }
        if (anyRemoved) {
            broadcastBattlefields(gameData);
            broadcastGraveyards(gameData);
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
                        || p.getDamagePreventionShield() != 0 || p.getRegenerationShield() != 0) {
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
    }

    // ===== Regeneration =====

    private void resolveRegenerate(GameData gameData, StackEntry entry) {
        Permanent perm = findPermanentById(gameData, entry.getTargetPermanentId());
        if (perm == null) {
            return;
        }
        perm.setRegenerationShield(perm.getRegenerationShield() + 1);

        String logEntry = perm.getCard().getName() + " gains a regeneration shield.";
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
            log.info("Game {} - {} regenerates", gameData.id, perm.getCard().getName());
            return true;
        }
        return false;
    }

    // ===== Static / continuous effect computation =====

    private record StaticBonus(int power, int toughness, Set<Keyword> keywords) {
        static final StaticBonus NONE = new StaticBonus(0, 0, Set.of());
    }

    private StaticBonus computeStaticBonus(GameData gameData, Permanent target) {
        if (target.getCard().getType() != CardType.CREATURE) return StaticBonus.NONE;
        int power = 0;
        int toughness = 0;
        Set<Keyword> keywords = new HashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                if (source == target) continue;
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
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
        return new StaticBonus(power, toughness, keywords);
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
            gameData.gameLog.add(logMsg);
            broadcastLogEntry(gameData, logMsg);
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
                graveyard.add(dead.getCard());
                collectDeathTrigger(gameData, dead.getCard(), playerId);
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
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
            if (p.getCard().getType() == CardType.CREATURE && !p.isTapped() && !p.isSummoningSick() && !hasKeyword(gameData, p, Keyword.DEFENDER) && !hasAuraPreventingAttackOrBlock(gameData, p)) {
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
            if (p.getCard().getType() == CardType.CREATURE && !p.isTapped() && !hasAuraPreventingAttackOrBlock(gameData, p)) {
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
        gameData.gameLog.add(logEntry);
        broadcastLogEntry(gameData, logEntry);
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

            broadcastBattlefields(gameData);

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

        if (blockable.isEmpty()) {
            log.info("Game {} - Defending player has no creatures that can block", gameData.id);
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
                if (hasKeyword(gameData, attacker, Keyword.FLYING)
                        && !hasKeyword(gameData, blocker, Keyword.FLYING)
                        && !hasKeyword(gameData, blocker, Keyword.REACH)) {
                    throw new IllegalStateException(blocker.getCard().getName() + " cannot block " + attacker.getCard().getName() + " (flying)");
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
            }

            // Check for "when this creature blocks" triggers
            for (BlockerAssignment assignment : blockerAssignments) {
                Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
                if (!blocker.getCard().getEffects(EffectSlot.ON_BLOCK).isEmpty()) {
                    Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            blocker.getCard(),
                            defenderId,
                            blocker.getCard().getName() + "'s block trigger",
                            new ArrayList<>(blocker.getCard().getEffects(EffectSlot.ON_BLOCK)),
                            attacker.getId(),
                            blocker.getId()
                    ));
                    String triggerLog = blocker.getCard().getName() + "'s block ability triggers.";
                    gameData.gameLog.add(triggerLog);
                    broadcastLogEntry(gameData, triggerLog);
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);

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
            gameData.gameLog.add(redirectLog);
            broadcastLogEntry(gameData, redirectLog);

            if (damageRedirectedToGuard >= getEffectiveToughness(gameData, redirectTarget)
                    && !tryRegenerate(gameData, redirectTarget)) {
                // Guard dies — find and remove it
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> bf = gameData.playerBattlefields.get(pid);
                    if (bf != null && bf.remove(redirectTarget)) {
                        gameData.playerGraveyards.get(pid).add(redirectTarget.getCard());
                        collectDeathTrigger(gameData, redirectTarget.getCard(), pid);
                        String deathLog = redirectTarget.getCard().getName() + " is destroyed by redirected combat damage.";
                        gameData.gameLog.add(deathLog);
                        broadcastLogEntry(gameData, deathLog);
                        break;
                    }
                }
            }
        }

        // Process life gain from damage triggers (e.g. Spirit Link) before removing dead creatures
        processGainLifeEqualToDamageDealt(gameData, combatDamageDealt);

        // Remove dead creatures (descending order to preserve indices) and move to graveyard
        List<String> deadCreatureNames = new ArrayList<>();
        List<Card> attackerGraveyard = gameData.playerGraveyards.get(activeId);
        for (int idx : deadAttackerIndices) {
            Permanent dead = atkBf.get(idx);
            deadCreatureNames.add(gameData.playerIdToName.get(activeId) + "'s " + dead.getCard().getName());
            attackerGraveyard.add(dead.getCard());
            collectDeathTrigger(gameData, dead.getCard(), activeId);
            atkBf.remove(idx);
        }
        List<Card> defenderGraveyard = gameData.playerGraveyards.get(defenderId);
        for (int idx : deadDefenderIndices) {
            Permanent dead = defBf.get(idx);
            deadCreatureNames.add(gameData.playerIdToName.get(defenderId) + "'s " + dead.getCard().getName());
            defenderGraveyard.add(dead.getCard());
            collectDeathTrigger(gameData, dead.getCard(), defenderId);
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
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
        }

        if (!deadCreatureNames.isEmpty()) {
            String logEntry = String.join(", ", deadCreatureNames) + " died in combat.";
            gameData.gameLog.add(logEntry);
            broadcastLogEntry(gameData, logEntry);
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
                                gameData.gameLog.add(logEntry);
                                broadcastLogEntry(gameData, logEntry);
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
                        gameData.gameLog.add(logEntry);
                        broadcastLogEntry(gameData, logEntry);
                        continue;
                    }

                    String logEntry = creature.getCard().getName() + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " may return up to " + damageDealt + " permanent" + (damageDealt > 1 ? "s" : "") + ".";
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
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
                    gameData.gameLog.add(logEntry);
                    broadcastLogEntry(gameData, logEntry);
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
                        gameData.gameLog.add(logEntry);
                        broadcastLogEntry(gameData, logEntry);
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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);

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
            return Map.of("W", 0, "U", 0, "B", 0, "R", 0, "G", 0);
        }
        ManaPool pool = data.playerManaPools.get(playerId);
        return pool != null ? pool.toMap() : Map.of("W", 0, "U", 0, "B", 0, "R", 0, "G", 0);
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

    private void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId) {
        if (dyingCard.getType() != CardType.CREATURE) return;

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
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
                log.info("Game {} - {} accepts may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
            } else {
                String logEntry = player.getUsername() + " declines " + ability.sourceCard().getName() + "'s triggered ability.";
                gameData.gameLog.add(logEntry);
                broadcastLogEntry(gameData, logEntry);
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

}

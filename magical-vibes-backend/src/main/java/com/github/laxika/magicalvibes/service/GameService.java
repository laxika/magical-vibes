package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
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
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
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
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final CardViewFactory cardViewFactory;
    private final EffectResolutionService effectResolutionService;
    private final CombatService combatService;
    private final TurnProgressionService turnProgressionService;
    private final UserInputHandlerService userInputHandlerService;
    private final SpellCastingService spellCastingService;
    private final TargetValidationService targetValidationService;

    public void passPriority(GameData gameData, Player player) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            if (gameData.awaitingInput != null) {
                throw new IllegalStateException("Cannot pass priority while awaiting input");
            }

            UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
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
                    turnProgressionService.advanceStep(gameData);
                }
            } else {
                gameBroadcastService.broadcastGameState(gameData);
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void advanceStep(GameData gameData) {
        turnProgressionService.advanceStep(gameData);
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

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
                Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
                if (target == null) {
                    String fizzleLog = card.getName() + " fizzles (enchanted creature no longer exists).";
                    gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
                    gameData.playerGraveyards.get(controllerId).add(card);

                    log.info("Game {} - {} fizzles, target {} no longer exists", gameData.id, card.getName(), entry.getTargetPermanentId());
                } else {
                    Permanent perm = new Permanent(card);
                    perm.setAttachedTo(entry.getTargetPermanentId());
                    gameData.playerBattlefields.get(controllerId).add(perm);

                    String playerName = gameData.playerIdToName.get(controllerId);
                    String logEntry = card.getName() + " enters the battlefield attached to " + target.getCard().getName() + " under " + playerName + "'s control.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

                // Check if enchantment has "as enters" color choice
                boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .anyMatch(e -> e instanceof ChooseColorEffect);
                if (needsColorChoice) {
                    List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                    Permanent justEntered = bf.get(bf.size() - 1);
                    playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), null);
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);
            if (gameData.awaitingInput == null) {
                gameHelper.checkLegendRule(gameData, controllerId);
            }
        } else if (entry.getEntryType() == StackEntryType.PLANESWALKER_SPELL) {
            Card card = entry.getCard();
            UUID controllerId = entry.getControllerId();

            Permanent perm = new Permanent(card);
            perm.setLoyaltyCounters(card.getLoyalty() != null ? card.getLoyalty() : 0);
            perm.setSummoningSick(false);
            gameData.playerBattlefields.get(controllerId).add(perm);

            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = card.getName() + " enters the battlefield with " + perm.getLoyaltyCounters() + " loyalty under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

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
                    targetFizzled = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetPermanentId()) == null;
                } else if (entry.getTargetZone() == TargetZone.STACK) {
                    targetFizzled = gameData.stack.stream()
                            .noneMatch(se -> se.getCard().getId().equals(entry.getTargetPermanentId()));
                } else {
                    Permanent targetPerm = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
                    if (targetPerm == null && !gameData.playerIds.contains(entry.getTargetPermanentId())) {
                        targetFizzled = true;
                    } else if (targetPerm != null && entry.getCard() != null && entry.getCard().getTargetFilter() != null) {
                        try {
                            gameQueryService.validateTargetFilter(entry.getCard().getTargetFilter(), targetPerm);
                        } catch (IllegalStateException e) {
                            targetFizzled = true;
                        }
                    }
                }
            }
            // Check multi-target permanent fizzle: if ALL targeted permanents are gone, fizzle
            if (!targetFizzled && entry.getTargetPermanentIds() != null && !entry.getTargetPermanentIds().isEmpty()) {
                boolean allGone = true;
                for (UUID permId : entry.getTargetPermanentIds()) {
                    if (gameQueryService.findPermanentById(gameData, permId) != null) {
                        allGone = false;
                        break;
                    }
                }
                if (allGone) {
                    targetFizzled = true;
                }
            }
            // Check multi-target graveyard fizzle: if ALL targeted cards are gone, fizzle
            if (!targetFizzled && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
                boolean allGone = true;
                for (UUID cardId : entry.getTargetCardIds()) {
                    if (gameQueryService.findCardInGraveyardById(gameData, cardId) != null) {
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
                gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
                log.info("Game {} - {} fizzles, target {} is illegal",
                        gameData.id, entry.getDescription(), entry.getTargetPermanentId());

                // Fizzled spells still go to graveyard (copies cease to exist per rule 707.10a)
                if ((entry.getEntryType() == StackEntryType.SORCERY_SPELL
                        || entry.getEntryType() == StackEntryType.INSTANT_SPELL)
                        && !entry.isCopy()) {
                    gameData.playerGraveyards.get(entry.getControllerId()).add(entry.getCard());
                }
            } else {
                String logEntry = entry.getDescription() + " resolves.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} resolves", gameData.id, entry.getDescription());

                effectResolutionService.resolveEffects(gameData, entry);

                // Rule 723.1b: "End the turn" exiles the resolving spell itself (copies cease to exist per rule 707.10a)
                if (gameData.endTurnRequested) {
                    gameData.endTurnRequested = false;
                    if ((entry.getEntryType() == StackEntryType.SORCERY_SPELL
                            || entry.getEntryType() == StackEntryType.INSTANT_SPELL)
                            && !entry.isCopy()) {
                        gameData.playerExiledCards.get(entry.getControllerId()).add(entry.getCard());
                    }
                    gameBroadcastService.broadcastGameState(gameData);
                    return;
                }

                // Copies cease to exist per rule 707.10a — skip graveyard/shuffle
                if ((entry.getEntryType() == StackEntryType.SORCERY_SPELL
                        || entry.getEntryType() == StackEntryType.INSTANT_SPELL)
                        && !entry.isCopy()) {
                    boolean shuffled = entry.getEffectsToResolve().stream()
                            .anyMatch(e -> e instanceof ShuffleIntoLibraryEffect);
                    if (shuffled) {
                        // Ensure the card is shuffled into library even when an earlier effect
                        // required user input and broke the effect resolution loop before
                        // the ShuffleIntoLibraryEffect handler could run.
                        List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
                        if (!deck.contains(entry.getCard())) {
                            deck.add(entry.getCard());
                            Collections.shuffle(deck);
                            String shuffleLog = entry.getCard().getName() + " is shuffled into its owner's library.";
                            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
                        }
                    } else {
                        gameData.playerGraveyards.get(entry.getControllerId()).add(entry.getCard());
                    }
                }
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {

            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameBroadcastService.broadcastGameState(gameData);
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
                    UUID defenderId = gameQueryService.getOpponentId(gameData, gameData.activePlayerId);
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
                            options.addAll(GameQueryService.TEXT_CHANGE_COLOR_WORDS);
                            options.addAll(GameQueryService.TEXT_CHANGE_LAND_TYPES);
                            prompt = "Choose a color word or basic land type to replace.";
                        } else if (gameData.colorChoiceContext instanceof ColorChoiceContext.TextChangeToWord ctx) {
                            if (ctx.isColor()) {
                                options = GameQueryService.TEXT_CHANGE_COLOR_WORDS.stream().filter(c -> !c.equals(ctx.fromWord())).toList();
                                prompt = "Choose the replacement color word.";
                            } else {
                                options = GameQueryService.TEXT_CHANGE_LAND_TYPES.stream().filter(t -> !t.equals(ctx.fromWord())).toList();
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
                                    cardViews.add(cardViewFactory.create(card));
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
                        List<CardView> cardViews = gameData.awaitingLibraryReorderCards.stream().map(cardViewFactory::create).toList();
                        sessionManager.sendToPlayer(playerId, new ReorderLibraryCardsMessage(
                                cardViews, "Put these cards back on top of your library in any order (top to bottom)."));
                    }
                }
                case LIBRARY_SEARCH -> {
                    if (playerId.equals(gameData.awaitingLibrarySearchPlayerId) && gameData.awaitingLibrarySearchCards != null) {
                        List<CardView> cardViews = gameData.awaitingLibrarySearchCards.stream().map(cardViewFactory::create).toList();
                        String prompt = gameData.awaitingLibrarySearchCanFailToFind
                                ? "Search your library for a basic land card to put into your hand."
                                : "Search your library for a card to put into your hand.";
                        sessionManager.sendToPlayer(playerId, new ChooseCardFromLibraryMessage(
                                cardViews, prompt, gameData.awaitingLibrarySearchCanFailToFind));
                    }
                }
                case HAND_TOP_BOTTOM_CHOICE -> {
                    if (playerId.equals(gameData.awaitingHandTopBottomPlayerId) && gameData.awaitingHandTopBottomCards != null) {
                        List<CardView> cardViews = gameData.awaitingHandTopBottomCards.stream().map(cardViewFactory::create).toList();
                        int count = gameData.awaitingHandTopBottomCards.size();
                        sessionManager.sendToPlayer(playerId, new ChooseHandTopBottomMessage(
                                cardViews, "Look at the top " + count + " cards of your library. Choose one to put into your hand."));
                    }
                }
                case REVEALED_HAND_CHOICE -> {
                    if (playerId.equals(gameData.awaitingCardChoicePlayerId) && gameData.awaitingRevealedHandChoiceTargetPlayerId != null) {
                        UUID targetPlayerId = gameData.awaitingRevealedHandChoiceTargetPlayerId;
                        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
                        String targetName = gameData.playerIdToName.get(targetPlayerId);
                        List<CardView> cardViews = targetHand.stream().map(cardViewFactory::create).toList();
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
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} kept hand, needs to bottom {} cards (mulligan count: {})", gameData.id, player.getUsername(), cardsToBottom, mulliganCount);

                gameBroadcastService.broadcastGameState(gameData);
                sessionManager.sendToPlayer(player.getId(), new SelectCardsToBottomMessage(cardsToBottom));
            } else {
                String logEntry = player.getUsername() + " keeps their hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} bottomed {} cards, hand size now {}", gameData.id, player.getUsername(), bottomCards.size(), hand.size());

            gameBroadcastService.broadcastGameState(gameData);
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} mulliganed (count: {})", gameData.id, player.getUsername(), newMulliganCount);
            gameBroadcastService.broadcastGameState(gameData);
        }
    }

    private void startGame(GameData gameData) {
        gameData.status = GameStatus.RUNNING;
        gameData.activePlayerId = gameData.startingPlayerId;
        gameData.turnNumber = 1;
        gameData.currentStep = TurnStep.first();

        String logEntry1 = "Mulligan phase complete!";
        String logEntry2 = "Turn 1 begins. " + gameData.playerIdToName.get(gameData.activePlayerId) + "'s turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry1);
        gameBroadcastService.logAndBroadcast(gameData, logEntry2);

        gameBroadcastService.broadcastGameState(gameData);

        log.info("Game {} - Game started! Turn 1 begins. Active player: {}", gameData.id, gameData.playerIdToName.get(gameData.activePlayerId));

        turnProgressionService.resolveAutoPass(gameData);
    }

    private JoinGame toJoinGame(GameData data, UUID playerId) {
        List<CardView> hand = playerId != null
                ? data.playerHands.getOrDefault(playerId, List.of()).stream().map(cardViewFactory::create).toList()
                : List.of();
        int mulliganCount = playerId != null ? data.mulliganCounts.getOrDefault(playerId, 0) : 0;
        Map<String, Integer> manaPool = gameBroadcastService.getManaPool(data, playerId);
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
                gameQueryService.getPriorityPlayerId(data),
                hand,
                mulliganCount,
                gameBroadcastService.getDeckSizes(data),
                gameBroadcastService.getHandSizes(data),
                gameBroadcastService.getBattlefields(data),
                manaPool,
                autoStopSteps,
                gameBroadcastService.getLifeTotals(data),
                gameBroadcastService.getStackViews(data),
                gameBroadcastService.getGraveyardViews(data)
        );
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments) {
        synchronized (gameData) {
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, List.of(), List.of());
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds) {
        synchronized (gameData) {
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds);
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} taps {}", gameData.id, player.getUsername(), permanent.getCard().getName());

            gameBroadcastService.broadcastGameState(gameData);
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
                    Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
                    if (target == null) {
                        throw new IllegalStateException("Invalid target permanent");
                    }
                    if (!destroy.targetTypes().contains(target.getCard().getType())) {
                        throw new IllegalStateException("Invalid target type for sacrifice ability");
                    }
                    if (gameQueryService.hasProtectionFrom(gameData, target, permanent.getCard().getColor())) {
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
                playerInputService.processNextMayAbility(gameData);
            }
            gameBroadcastService.broadcastGameState(gameData);
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

            // Validate loyalty ability restrictions
            if (ability.getLoyaltyCost() != null) {
                // Sorcery-speed timing: must be active player, main phase, stack empty
                if (!playerId.equals(gameData.activePlayerId)) {
                    throw new IllegalStateException("Loyalty abilities can only be activated on your turn");
                }
                if (gameData.currentStep != TurnStep.PRECOMBAT_MAIN && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN) {
                    throw new IllegalStateException("Loyalty abilities can only be activated during a main phase");
                }
                if (!gameData.stack.isEmpty()) {
                    throw new IllegalStateException("Loyalty abilities can only be activated when the stack is empty");
                }
                // Once per turn
                if (permanent.isLoyaltyAbilityUsedThisTurn()) {
                    throw new IllegalStateException("Only one loyalty ability per planeswalker per turn");
                }
                // For negative loyalty costs, check sufficient loyalty
                int loyaltyCost = ability.getLoyaltyCost();
                if (loyaltyCost < 0 && permanent.getLoyaltyCounters() < Math.abs(loyaltyCost)) {
                    throw new IllegalStateException("Not enough loyalty counters");
                }
                // Pay loyalty cost
                permanent.setLoyaltyCounters(permanent.getLoyaltyCounters() + loyaltyCost);
                permanent.setLoyaltyAbilityUsedThisTurn(true);
            }

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
            targetValidationService.validateEffectTargets(abilityEffects,
                    new TargetValidationContext(gameData, targetPermanentId, targetZone, permanent.getCard()));

            // Generic target filter validation
            if (ability.getTargetFilter() != null && targetPermanentId != null) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
                if (target != null) {
                    gameQueryService.validateTargetFilter(ability.getTargetFilter(), target);
                }
            }

            // Creature shroud validation for abilities
            if (targetPermanentId != null) {
                Permanent shroudTarget = gameQueryService.findPermanentById(gameData, targetPermanentId);
                if (shroudTarget != null && hasKeyword(gameData, shroudTarget, Keyword.SHROUD)) {
                    throw new IllegalStateException(shroudTarget.getCard().getName() + " has shroud and can't be targeted");
                }
            }

            // Player shroud validation for abilities
            if (targetPermanentId != null && gameData.playerIds.contains(targetPermanentId)
                    && gameQueryService.playerHasShroud(gameData, targetPermanentId)) {
                throw new IllegalStateException(gameData.playerIdToName.get(targetPermanentId) + " has shroud and can't be targeted");
            }

            // Self-target if effects need the source permanent
            UUID effectiveTargetId = targetPermanentId;
            if (effectiveTargetId == null) {
                boolean needsSelfTarget = abilityEffects.stream().anyMatch(e ->
                        e instanceof RegenerateEffect || e instanceof BoostSelfEffect || e instanceof UntapSelfEffect
                                || e instanceof AnimateSelfEffect);
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
                boolean wasCreature = isCreature(gameData, permanent);
                battlefield.remove(permanent);
                gameData.playerGraveyards.get(playerId).add(permanent.getCard());
                gameHelper.collectDeathTrigger(gameData, permanent.getCard(), playerId, wasCreature);
            }

            String logEntry = player.getUsername() + " activates " + permanent.getCard().getName() + "'s ability.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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

            // Check if this is a mana ability (CR 605.1a: doesn't target, could produce mana, not loyalty)
            // Mana abilities resolve immediately without using the stack (CR 605.3a)
            boolean isManaAbility = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget()
                    && ability.getLoyaltyCost() == null
                    && !snapshotEffects.isEmpty()
                    && snapshotEffects.stream().allMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect);

            if (isManaAbility) {
                for (CardEffect effect : snapshotEffects) {
                    if (effect instanceof AwardManaEffect award) {
                        gameData.playerManaPools.get(playerId).add(award.color());
                    } else if (effect instanceof AwardAnyColorManaEffect) {
                        gameData.colorChoiceContext = new ColorChoiceContext.ManaColorChoice(playerId);
                        gameData.awaitingInput = AwaitingInput.COLOR_CHOICE;
                        gameData.awaitingColorChoicePlayerId = playerId;
                        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                        sessionManager.sendToPlayer(playerId, new ChooseColorMessage(colors, "Choose a color of mana to add."));
                        log.info("Game {} - Awaiting {} to choose a mana color", gameData.id, player.getUsername());
                    }
                }
                gameHelper.performStateBasedActions(gameData);
                gameData.priorityPassedBy.clear();
                if (gameData.awaitingInput == null && !gameData.pendingMayAbilities.isEmpty()) {
                    playerInputService.processNextMayAbility(gameData);
                }
                gameBroadcastService.broadcastGameState(gameData);
            } else {
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
                gameHelper.performStateBasedActions(gameData);
                gameData.priorityPassedBy.clear();
                gameBroadcastService.broadcastGameState(gameData);
            }
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
            gameBroadcastService.broadcastGameState(gameData);
        }
    }

    // ===== Delegated user input handlers =====

    public void handleColorChosen(GameData gameData, Player player, String colorName) {
        synchronized (gameData) {
            userInputHandlerService.handleColorChosen(gameData, player, colorName);
        }
    }

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            userInputHandlerService.handleCardChosen(gameData, player, cardIndex);
        }
    }

    public void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        synchronized (gameData) {
            userInputHandlerService.handlePermanentChosen(gameData, player, permanentId);
        }
    }

    public void handleGraveyardCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            userInputHandlerService.handleGraveyardCardChosen(gameData, player, cardIndex);
        }
    }

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        synchronized (gameData) {
            userInputHandlerService.handleMultiplePermanentsChosen(gameData, player, permanentIds);
        }
    }

    public void handleMultipleGraveyardCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        synchronized (gameData) {
            userInputHandlerService.handleMultipleGraveyardCardsChosen(gameData, player, cardIds);
        }
    }

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        synchronized (gameData) {
            userInputHandlerService.handleMayAbilityChosen(gameData, player, accepted);
        }
    }

    public void handleLibraryCardsReordered(GameData gameData, Player player, List<Integer> cardOrder) {
        synchronized (gameData) {
            userInputHandlerService.handleLibraryCardsReordered(gameData, player, cardOrder);
        }
    }

    public void handleHandTopBottomChosen(GameData gameData, Player player, int handCardIndex, int topCardIndex) {
        synchronized (gameData) {
            userInputHandlerService.handleHandTopBottomChosen(gameData, player, handCardIndex, topCardIndex);
        }
    }

    public void handleLibraryCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            userInputHandlerService.handleLibraryCardChosen(gameData, player, cardIndex);
        }
    }

    // ===== Combat wrapper methods =====

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
        synchronized (gameData) {
            turnProgressionService.handleCombatResult(combatService.declareAttackers(gameData, player, attackerIndices), gameData);
        }
    }

    public void declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        synchronized (gameData) {
            turnProgressionService.handleCombatResult(combatService.declareBlockers(gameData, player, blockerAssignments), gameData);
        }
    }

    // ===== Thin delegates for test API =====

    public boolean isCreature(GameData gameData, Permanent permanent) {
        return gameQueryService.isCreature(gameData, permanent);
    }

    public int getEffectivePower(GameData gameData, Permanent permanent) {
        return gameQueryService.getEffectivePower(gameData, permanent);
    }

    public int getEffectiveToughness(GameData gameData, Permanent permanent) {
        return gameQueryService.getEffectiveToughness(gameData, permanent);
    }

    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return gameQueryService.hasKeyword(gameData, permanent, keyword);
    }
}

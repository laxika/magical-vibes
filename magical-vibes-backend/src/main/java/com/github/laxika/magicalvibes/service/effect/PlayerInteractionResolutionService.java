package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaPerAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaWithInstantSorceryCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToExileEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndUntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardUnlessAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessExileCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndRandomDiscardWithSharedTypeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageLootEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.FlipTwoCoinsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawXCardsForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPermanentNoMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutAwakeningCountersOnTargetLandsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardXEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomHandCardAndPlayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDrawCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsReturnSelfIfCardTypeEffect;
import com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType;
import com.github.laxika.magicalvibes.model.PendingTransformOnCreatureDiscard;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardTransformIfCreatureDiscardedEffect;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerInteractionResolutionService {

    private final DrawService drawService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final TriggerCollectionService triggerCollectionService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @HandlesEffect(OpponentMayPlayCreatureEffect.class)
    void resolveOpponentMayPlayCreature(GameData gameData, StackEntry entry) {
        applyOpponentMayPlayCreature(gameData, entry.getControllerId());
    }

    @HandlesEffect(PutCardToBattlefieldEffect.class)
    void resolvePutCardToBattlefield(GameData gameData, StackEntry entry, PutCardToBattlefieldEffect effect) {
        applyPutCardToBattlefield(gameData, entry.getControllerId(), effect);
    }

    @HandlesEffect(DrawCardEffect.class)
    void resolveDrawCards(GameData gameData, StackEntry entry, DrawCardEffect effect) {
        applyDrawCards(gameData, entry.getControllerId(), effect.amount());
    }

    @HandlesEffect(EachPlayerDrawsCardEffect.class)
    void resolveEachPlayerDrawsCard(GameData gameData, StackEntry entry, EachPlayerDrawsCardEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            applyDrawCards(gameData, playerId, effect.amount());
        }
    }

    @HandlesEffect(SacrificeSelfAndDrawCardsEffect.class)
    void resolveSacrificeSelfAndDrawCards(GameData gameData, StackEntry entry,
                                                  SacrificeSelfAndDrawCardsEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + "'s ability fizzles — source no longer on the battlefield.");
            return;
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, source);
        gameBroadcastService.logAndBroadcast(gameData,
                entry.getCard().getName() + " is sacrificed.");

        applyDrawCards(gameData, entry.getControllerId(), effect.amount());
    }

    /**
     * Flips a coin for the source permanent's controller. On a win, the wrapped effect is
     * dispatched via the effect handler registry. On a loss, nothing happens.
     */
    @HandlesEffect(FlipCoinWinEffect.class)
    void resolveFlipCoinWinEffect(GameData gameData, StackEntry entry, FlipCoinWinEffect effect) {
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        boolean wonFlip = ThreadLocalRandom.current().nextBoolean();

        String flipLog = wonFlip
                ? gameData.playerIdToName.get(controllerId) + " wins the coin flip for " + sourceName + "."
                : gameData.playerIdToName.get(controllerId) + " loses the coin flip for " + sourceName + ".";
        gameBroadcastService.logAndBroadcast(gameData, flipLog);

        if (!wonFlip) {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(effect.wrapped());
        if (handler != null) {
            handler.resolve(gameData, entry, effect.wrapped());
        } else {
            log.warn("No handler for wrapped effect in FlipCoinWinEffect: {}",
                    effect.wrapped().getClass().getSimpleName());
        }
    }

    /**
     * Flips two coins for the source permanent's controller. If both come up heads, the bothHeads
     * effect is dispatched. If both come up tails, the bothTails effect is dispatched. On a split
     * result, nothing happens.
     */
    @HandlesEffect(FlipTwoCoinsEffect.class)
    void resolveFlipTwoCoinsEffect(GameData gameData, StackEntry entry, FlipTwoCoinsEffect effect) {
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        boolean firstFlip = ThreadLocalRandom.current().nextBoolean();
        boolean secondFlip = ThreadLocalRandom.current().nextBoolean();

        String firstResult = firstFlip ? "heads" : "tails";
        String secondResult = secondFlip ? "heads" : "tails";
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " flips two coins for " + sourceName + ": " + firstResult + " and " + secondResult + ".");

        CardEffect chosen;
        if (firstFlip && secondFlip) {
            chosen = effect.bothHeads();
        } else if (!firstFlip && !secondFlip) {
            chosen = effect.bothTails();
        } else {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(chosen);
        if (handler != null) {
            handler.resolve(gameData, entry, chosen);
        } else {
            log.warn("No handler for wrapped effect in FlipTwoCoinsEffect: {}",
                    chosen.getClass().getSimpleName());
        }
    }

    @HandlesEffect(SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect.class)
    void resolveSacrificeSelfAndTargetDiscardsPerPoisonCounter(GameData gameData, StackEntry entry,
                                                                       SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        if (targetPlayerId == null || sourcePermanentId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + "'s ability fizzles — source no longer on the battlefield.");
            return;
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, source);
        gameBroadcastService.logAndBroadcast(gameData,
                entry.getCard().getName() + " is sacrificed.");

        int poisonCounters = gameData.playerPoisonCounters.getOrDefault(targetPlayerId, 0);
        if (poisonCounters <= 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " has no poison counters — no cards to discard.");
            return;
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " must discard " + poisonCounters + " card" + (poisonCounters > 1 ? "s" : "")
                        + " (" + entry.getCard().getName() + ").");

        resolveDiscardCards(gameData, targetPlayerId, poisonCounters);
    }

    @HandlesEffect(DrawXCardsForTargetPlayerEffect.class)
    void resolveDrawXCardsForTargetPlayer(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        int amount = entry.getXValue();
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String cardName = entry.getCard().getName();

        for (int i = 0; i < amount; i++) {
            drawService.resolveDrawCard(gameData, targetPlayerId);
        }

        String logEntry = playerName + " draws " + amount + " card" + (amount != 1 ? "s" : "")
                + " (" + cardName + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws {} from {}", gameData.id, playerName, amount, cardName);
    }

    @HandlesEffect(DrawXCardsEffect.class)
    void resolveDrawXCards(GameData gameData, StackEntry entry) {
        int amount = entry.getXValue();
        UUID controllerId = entry.getControllerId();
        applyDrawCards(gameData, controllerId, amount);
    }

    @HandlesEffect(ShuffleHandIntoLibraryAndDrawEffect.class)
    void resolveShuffleHandIntoLibraryAndDraw(GameData gameData, StackEntry entry) {
        String cardName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);

            if (hand == null || hand.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " has no cards in hand to shuffle.");
                log.info("Game {} - {} has no cards in hand for {}", gameData.id, playerName, cardName);
                continue;
            }

            int handSize = hand.size();

            // Shuffle hand into library
            List<Card> deck = gameData.playerDecks.get(playerId);
            deck.addAll(hand);
            hand.clear();
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " shuffles " + handSize + " card" + (handSize != 1 ? "s" : "")
                            + " from hand into their library.");
            log.info("Game {} - {} shuffles {} cards from hand into library ({})",
                    gameData.id, playerName, handSize, cardName);

            // Draw that many cards
            for (int i = 0; i < handSize; i++) {
                drawService.resolveDrawCard(gameData, playerId);
            }

            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " draws " + handSize + " card" + (handSize != 1 ? "s" : "") + ".");
            log.info("Game {} - {} draws {} cards ({})", gameData.id, playerName, handSize, cardName);
        }
    }

    @HandlesEffect(DrawCardsEqualToChargeCountersOnSourceEffect.class)
    void resolveDrawCardsEqualToChargeCounters(GameData gameData, StackEntry entry) {
        int count = entry.getXValue();
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (count <= 0) {
            String logEntry = playerName + " draws 0 cards from " + entry.getCard().getName() + " (no charge counters).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} draws 0 from {} (no charge counters)", gameData.id, playerName, entry.getCard().getName());
            return;
        }

        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }

        String logEntry = playerName + " draws " + count + " card" + (count != 1 ? "s" : "") + " from " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws {} from {}", gameData.id, playerName, count, entry.getCard().getName());
    }

    @HandlesEffect(DrawCardsEqualToControlledCreatureCountEffect.class)
    void resolveDrawCardsEqualToControlledCreatureCount(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();

        int count = 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    count++;
                }
            }
        }

        if (count <= 0) {
            String logEntry = playerName + " draws 0 cards from " + cardName + " (no creatures).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} draws 0 from {} (no creatures)", gameData.id, playerName, cardName);
            return;
        }

        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }

        String logEntry = playerName + " draws " + count + " card" + (count != 1 ? "s" : "") + " from " + cardName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws {} from {}", gameData.id, playerName, count, cardName);
    }

    @HandlesEffect(DiscardCardEffect.class)
    void resolveDiscardCard(GameData gameData, StackEntry entry, DiscardCardEffect effect) {
        gameData.discardCausedByOpponent = false;
        resolveDiscardCards(gameData, entry.getControllerId(), effect.amount());
    }

    @HandlesEffect(DiscardCardUnlessAttackedThisTurnEffect.class)
    void resolveDiscardCardUnlessAttackedThisTurn(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();

        if (gameData.playersDeclaredAttackersThisTurn.contains(controllerId)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " attacked this turn — no discard required (" + cardName + ").");
            log.info("Game {} - {} attacked this turn, skipping discard for {}", gameData.id, playerName, cardName);
            return;
        }

        gameData.discardCausedByOpponent = false;
        resolveDiscardCards(gameData, controllerId, 1);
    }

    @HandlesEffect(DiscardOwnHandEffect.class)
    void resolveDiscardOwnHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        List<Card> hand = gameData.playerHands.get(controllerId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = playerName + " has no cards to discard (" + cardName + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            return;
        }

        List<Card> discarded = new ArrayList<>(hand);
        hand.clear();
        gameData.discardCausedByOpponent = false;

        for (Card card : discarded) {
            graveyardService.addCardToGraveyard(gameData, controllerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, controllerId, card);
        }

        String logEntry = playerName + " discards their hand (" + discarded.size()
                + " card" + (discarded.size() != 1 ? "s" : "") + ") (" + cardName + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} discards hand of {} cards for {}", gameData.id, playerName, discarded.size(), cardName);
    }

    @HandlesEffect(DrawDiscardTransformIfCreatureDiscardedEffect.class)
    void resolveDrawDiscardTransformIfCreatureDiscarded(GameData gameData, StackEntry entry,
                                                                DrawDiscardTransformIfCreatureDiscardedEffect effect) {
        UUID controllerId = entry.getControllerId();
        // Draw a card
        drawService.resolveDrawCard(gameData, controllerId);
        // Set up pending conditional transform before the discard
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            gameData.pendingTransformOnCreatureDiscard = new PendingTransformOnCreatureDiscard(sourcePermanentId);
        }
        // Then discard a card
        gameData.discardCausedByOpponent = false;
        resolveDiscardCards(gameData, controllerId, 1);
    }

    @HandlesEffect(DrawAndDiscardCardEffect.class)
    void resolveDrawAndDiscard(GameData gameData, StackEntry entry, DrawAndDiscardCardEffect effect) {
        UUID controllerId = entry.getControllerId();
        applyDrawCards(gameData, controllerId, effect.drawAmount());
        gameData.discardCausedByOpponent = false;
        resolveDiscardCards(gameData, controllerId, effect.discardAmount());
    }

    @HandlesEffect(RegisterDelayedCombatDamageLootEffect.class)
    void resolveRegisterDelayedCombatDamageLoot(GameData gameData, StackEntry entry,
                                                        RegisterDelayedCombatDamageLootEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.pendingDelayedCombatDamageLoots.add(
                new GameData.DelayedCombatDamageLoot(controllerId, effect.drawAmount(), effect.discardAmount(), entry.getCard()));
        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers delayed combat damage loot trigger (draw {}, discard {})",
                gameData.id, playerName, effect.drawAmount(), effect.discardAmount());
    }

    @HandlesEffect(DiscardUnlessExileCardFromGraveyardEffect.class)
    void resolveDiscardUnlessExileFromGraveyard(GameData gameData, StackEntry entry,
                                                        DiscardUnlessExileCardFromGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        boolean hasMatchingCards = graveyard != null && graveyard.stream()
                .anyMatch(card -> gameQueryService.matchesCardPredicate(card, effect.predicate(), null));

        if (!hasMatchingCards) {
            // No matching cards in graveyard — must discard
            gameData.discardCausedByOpponent = false;
            resolveDiscardCards(gameData, controllerId, 1);
            return;
        }

        // Has matching cards — ask via may ability
        String filterLabel = CardPredicateUtils.describeFilter(effect.predicate());
        String prompt = "Exile a " + filterLabel + " from your graveyard to avoid discarding? ("
                + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(effect), prompt
        ));
    }

    @HandlesEffect(DiscardUpToThenDrawThatManyEffect.class)
    void resolveDiscardUpToThenDraw(GameData gameData, StackEntry entry,
                                            DiscardUpToThenDrawThatManyEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();

        // Re-entry after player chose how many to discard
        if (gameData.chosenXValue != null) {
            int chosenCount = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenCount == 0) {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " chooses to discard 0 cards for " + cardName + ".");
                log.info("Game {} - {} chooses to discard 0 for {}", gameData.id, playerName, cardName);
                return;
            }

            // Store the draw count for after discards complete
            gameData.pendingRummageDrawCount = chosenCount;
            gameData.discardCausedByOpponent = false;
            resolveDiscardCards(gameData, controllerId, chosenCount);
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " has no cards to discard for " + cardName + ".");
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            return;
        }

        int maxDiscard = Math.min(effect.maxDiscard(), hand.size());
        String prompt = "Discard up to " + effect.maxDiscard() + " cards for " + cardName
                + ". You will draw that many cards.";
        playerInputService.beginXValueChoice(gameData, controllerId, maxDiscard, prompt, cardName);
    }

    @HandlesEffect(DiscardAndDrawCardEffect.class)
    void resolveDiscardAndDraw(GameData gameData, StackEntry entry, DiscardAndDrawCardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(controllerId) + " has no cards to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        gameData.pendingRummageDrawCount = effect.drawAmount();
        gameData.discardCausedByOpponent = false;
        resolveDiscardCards(gameData, controllerId, effect.discardAmount());
    }

    @HandlesEffect(DiscardCardAndUntapSelfEffect.class)
    void resolveDiscardAndUntapSelf(GameData gameData, StackEntry entry, DiscardCardAndUntapSelfEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(controllerId) + " has no cards to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        gameData.pendingUntapAfterDiscardPermanentId = entry.getSourcePermanentId();
        gameData.discardCausedByOpponent = false;
        resolveDiscardCards(gameData, controllerId, 1);
    }

    @HandlesEffect(EachPlayerDiscardsEffect.class)
    void resolveEachPlayerDiscards(GameData gameData, StackEntry entry, EachPlayerDiscardsEffect effect) {
        UUID controllerId = entry.getControllerId();
        // Build APNAP-ordered queue: active player first, then others in turn order
        gameData.pendingEachPlayerDiscardQueue.clear();
        gameData.pendingEachPlayerDiscardControllerId = controllerId;
        UUID activePlayerId = gameData.activePlayerId;
        gameData.pendingEachPlayerDiscardQueue.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                gameData.pendingEachPlayerDiscardQueue.add(playerId);
            }
        }
        // Store the amount for later queue processing
        gameData.pendingEachPlayerDiscardAmount = effect.amount();
        // Start the first player's discard
        startNextEachPlayerDiscard(gameData);
    }

    @HandlesEffect(EachOpponentDiscardsEffect.class)
    void resolveEachOpponentDiscards(GameData gameData, StackEntry entry, EachOpponentDiscardsEffect effect) {
        UUID controllerId = entry.getControllerId();
        // Build APNAP-ordered queue with only opponents (skip controller)
        gameData.pendingEachPlayerDiscardQueue.clear();
        gameData.pendingEachPlayerDiscardControllerId = controllerId;
        UUID activePlayerId = gameData.activePlayerId;
        if (!activePlayerId.equals(controllerId)) {
            gameData.pendingEachPlayerDiscardQueue.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                gameData.pendingEachPlayerDiscardQueue.add(playerId);
            }
        }
        gameData.pendingEachPlayerDiscardAmount = effect.amount();
        startNextEachPlayerDiscard(gameData);
    }

    public void startNextEachPlayerDiscard(GameData gameData) {
        int amount = gameData.pendingEachPlayerDiscardAmount;
        while (!gameData.pendingEachPlayerDiscardQueue.isEmpty()) {
            UUID nextPlayerId = gameData.pendingEachPlayerDiscardQueue.removeFirst();
            gameData.discardCausedByOpponent = !nextPlayerId.equals(gameData.pendingEachPlayerDiscardControllerId);
            List<Card> hand = gameData.playerHands.get(nextPlayerId);
            if (hand == null || hand.isEmpty()) {
                String logEntry = gameData.playerIdToName.get(nextPlayerId) + " has no cards to discard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                continue;
            }
            gameData.interaction.setDiscardRemainingCount(amount);
            playerInputService.beginDiscardChoice(gameData, nextPlayerId);
            return;
        }
        // All players done — clear controller tracking
        gameData.pendingEachPlayerDiscardControllerId = null;
    }

    @HandlesEffect(TargetPlayerExilesFromHandEffect.class)
    void resolveTargetPlayerExilesFromHand(GameData gameData, StackEntry entry, TargetPlayerExilesFromHandEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no cards to exile from hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            UUID controllerId = entry.getControllerId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard() == entry.getCard()) {
                        sourcePermanentId = p.getId();
                        break;
                    }
                }
            }
        }

        gameData.interaction.setDiscardRemainingCount(effect.amount());
        playerInputService.beginExileFromHandChoice(gameData, targetPlayerId, sourcePermanentId);
    }

    @HandlesEffect(TargetPlayerDiscardsEffect.class)
    void resolveTargetPlayerDiscards(GameData gameData, StackEntry entry, TargetPlayerDiscardsEffect effect) {
        gameData.discardCausedByOpponent = true;
        resolveDiscardCards(gameData, entry.getTargetId(), effect.amount());
    }

    @HandlesEffect(TargetSpellControllerDiscardsEffect.class)
    void resolveTargetSpellControllerDiscards(GameData gameData, StackEntry entry, TargetSpellControllerDiscardsEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                gameData.discardCausedByOpponent = true;
                resolveDiscardCards(gameData, se.getControllerId(), effect.amount());
                return;
            }
        }
        // Target spell already left the stack (e.g. was countered by earlier effect)
    }

    @HandlesEffect(TargetPlayerDiscardsByChargeCountersEffect.class)
    void resolveTargetPlayerDiscardsByChargeCounters(GameData gameData, StackEntry entry) {
        int chargeCounters = entry.getXValue();
        UUID targetPlayerId = entry.getTargetId();

        if (chargeCounters <= 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " discards 0 cards (no charge counters).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.discardCausedByOpponent = true;
        resolveDiscardCards(gameData, targetPlayerId, chargeCounters);
    }

    @HandlesEffect(TargetPlayerDiscardsReturnSelfIfCardTypeEffect.class)
    void resolveTargetPlayerDiscardsReturnSelfIfCardType(GameData gameData, StackEntry entry, TargetPlayerDiscardsReturnSelfIfCardTypeEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        if (targetHand != null && !targetHand.isEmpty()) {
            gameData.pendingReturnToHandOnDiscardType = new PendingReturnToHandOnDiscardType(
                    entry.getCard(), entry.getControllerId(), effect.returnIfType());
        }
        gameData.discardCausedByOpponent = true;
        resolveDiscardCards(gameData, targetPlayerId, effect.amount());
    }

    @HandlesEffect(ChooseCardFromTargetHandToDiscardEffect.class)
    void resolveChooseCardFromTargetHandToDiscardHandler(GameData gameData, StackEntry entry, ChooseCardFromTargetHandToDiscardEffect effect) {
        gameData.discardCausedByOpponent = true;
        resolveHandRevealAndChoose(gameData, entry, effect.count(), effect.excludedTypes(), effect.includedTypes(), true, false, null);
    }

    @HandlesEffect(ChooseCardFromTargetHandToExileEffect.class)
    void resolveChooseCardFromTargetHandToExileHandler(GameData gameData, StackEntry entry, ChooseCardFromTargetHandToExileEffect effect) {
        UUID sourcePermanentId = effect.returnOnSourceLeave() ? entry.getSourcePermanentId() : null;
        resolveHandRevealAndChoose(gameData, entry, effect.count(), effect.excludedTypes(), effect.includedTypes(), false, true, sourcePermanentId);
    }

    @HandlesEffect(ChooseCardNameAndExileFromZonesEffect.class)
    void resolveChooseCardNameAndExileFromZones(GameData gameData, StackEntry entry, ChooseCardNameAndExileFromZonesEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        playerInputService.beginSpellCardNameChoice(gameData, controllerId, targetPlayerId, effect.excludedTypes());
    }

    @HandlesEffect(ExileTargetGraveyardCardAndSameNameFromZonesEffect.class)
    void resolveExileTargetGraveyardCardAndSameNameFromZones(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        UUID targetCardId = entry.getTargetId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        Card targetedCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetedCard == null) {
            return; // Target removed — fizzle already handled by StackResolutionService
        }

        String cardName = targetedCard.getName();
        UUID targetPlayerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);

        String targetName = gameData.playerIdToName.get(targetPlayerId);

        // Collect all matching cards across hand, graveyard, and library of the card's owner
        List<Card> matchingCards = new ArrayList<>();

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand != null) {
            matchingCards.addAll(hand.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard != null) {
            matchingCards.addAll(graveyard.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library != null) {
            matchingCards.addAll(library.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        if (matchingCards.isEmpty()) {
            // No matching cards — just shuffle library and resolve
            if (library != null) {
                java.util.Collections.shuffle(library);
            }

            String exileLog = controllerName + " exiles 0 cards named \"" + cardName + "\" from " + targetName
                    + "'s hand, graveyard, and library. " + targetName + " shuffles their library.";
            gameBroadcastService.logAndBroadcast(gameData, exileLog);
            log.info("Game {} - {} found 0 cards named \"{}\" in {}'s zones", gameData.id, controllerName, cardName, targetName);
            return;
        }

        // Present matching cards for "any number" selection
        playerInputService.beginMultiZoneExileChoice(gameData, controllerId, matchingCards, targetPlayerId, cardName);
    }

    @HandlesEffect(TargetPlayerRandomDiscardEffect.class)
    void resolveTargetPlayerRandomDiscard(GameData gameData, StackEntry entry, TargetPlayerRandomDiscardEffect effect) {
        gameData.discardCausedByOpponent = effect.causedByOpponent();
        UUID playerId = effect.causedByOpponent() ? entry.getTargetId() : entry.getControllerId();
        resolveRandomDiscardCards(gameData, playerId, entry.getCard().getName(), effect.amount());
    }

    @HandlesEffect(DrawAndRandomDiscardWithSharedTypeCountersEffect.class)
    void resolveDrawAndRandomDiscardWithSharedTypeCounters(GameData gameData, StackEntry entry,
                                                                    DrawAndRandomDiscardWithSharedTypeCountersEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        // Step 1: Draw cards
        applyDrawCards(gameData, controllerId, effect.drawAmount());

        // Step 2: Discard cards at random, tracking what was discarded
        List<Card> hand = gameData.playerHands.get(controllerId);
        List<Card> discardedCards = new ArrayList<>();
        gameData.discardCausedByOpponent = false;

        for (int i = 0; i < effect.discardAmount(); i++) {
            if (hand == null || hand.isEmpty()) break;
            int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
            Card discarded = hand.remove(randomIndex);
            discardedCards.add(discarded);
            graveyardService.addCardToGraveyard(gameData, controllerId, discarded);
            String logEntry = playerName + " discards " + discarded.getName() + " at random.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(), sourceName);
            triggerCollectionService.checkDiscardTriggers(gameData, controllerId, discarded);
        }

        // Process any pending self-discard triggers
        if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }

        // Step 3: Check if discarded cards share at least one card type
        if (discardedCards.size() >= 2 && sharesCardType(discardedCards)) {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (source != null && !gameQueryService.cantHaveCounters(gameData, source)) {
                    source.setPlusOnePlusOneCounters(source.getPlusOnePlusOneCounters() + effect.counterAmount());
                    String logEntry = sourceName + " gets " + effect.counterAmount()
                            + " +1/+1 counter" + (effect.counterAmount() != 1 ? "s" : "")
                            + " (discarded cards share a card type).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} gets {} +1/+1 counters (shared card type)", gameData.id, sourceName, effect.counterAmount());
                }
            }
        } else if (discardedCards.size() >= 2) {
            String logEntry = sourceName + "'s discarded cards do not share a card type — no counters.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discarded cards do not share a card type", gameData.id, sourceName);
        }
    }

    private boolean sharesCardType(List<Card> cards) {
        if (cards.size() < 2) return false;
        Card first = cards.get(0);
        Card second = cards.get(1);
        // Check if any card type of the first card matches any card type of the second card
        if (first.getType() == second.getType()) return true;
        if (first.getAdditionalTypes().contains(second.getType())) return true;
        if (second.getAdditionalTypes().contains(first.getType())) return true;
        for (CardType additionalType : first.getAdditionalTypes()) {
            if (second.getAdditionalTypes().contains(additionalType)) return true;
        }
        return false;
    }

    @HandlesEffect(TargetPlayerRandomDiscardOrControllerDrawsEffect.class)
    void resolveTargetPlayerRandomDiscardOrControllerDraws(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = targetName + " has no cards to discard. " + gameData.playerIdToName.get(controllerId) + " draws a card.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            drawService.resolveDrawCard(gameData, controllerId);
        } else {
            gameData.discardCausedByOpponent = true;
            resolveRandomDiscardCards(gameData, targetPlayerId, entry.getCard().getName(), 1);
        }
    }

    @HandlesEffect(TargetPlayerRandomDiscardXEffect.class)
    void resolveTargetPlayerRandomDiscardX(GameData gameData, StackEntry entry) {
        int x = entry.getXValue();
        UUID targetPlayerId = entry.getTargetId();

        if (x <= 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " discards 0 cards (X is 0).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.discardCausedByOpponent = true;
        resolveRandomDiscardCards(gameData, targetPlayerId, entry.getCard().getName(), x);
    }

    @HandlesEffect(EachPlayerRandomDiscardEffect.class)
    void resolveEachPlayerRandomDiscard(GameData gameData, StackEntry entry, EachPlayerRandomDiscardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        // APNAP order: active player first, then others in turn order
        UUID activePlayerId = gameData.activePlayerId;
        gameData.discardCausedByOpponent = !activePlayerId.equals(controllerId);
        resolveRandomDiscardCards(gameData, activePlayerId, sourceName, effect.amount());
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                gameData.discardCausedByOpponent = !playerId.equals(controllerId);
                resolveRandomDiscardCards(gameData, playerId, sourceName, effect.amount());
            }
        }
    }

    @HandlesEffect(ReturnPermanentsOnCombatDamageToPlayerEffect.class)
    void resolveReturnPermanentsOnCombatDamage(GameData gameData, StackEntry entry, ReturnPermanentsOnCombatDamageToPlayerEffect effect) {
        UUID defenderId = entry.getTargetId();
        int damageDealt = entry.getXValue();
        UUID attackerId = entry.getControllerId();
        String creatureName = entry.getCard().getName();

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<UUID> validIds = new ArrayList<>();
        if (defenderBattlefield != null) {
            for (Permanent perm : defenderBattlefield) {
                if (effect.filter() == null || gameQueryService.matchesPermanentPredicate(gameData, perm, effect.filter())) {
                    validIds.add(perm.getId());
                }
            }
        }

        String targetLabel = effect.filter() != null ? "creature" : "permanent";
        String targetsLabel = effect.filter() != null ? "creatures" : "permanents";

        if (validIds.isEmpty()) {
            String logEntry = creatureName + "'s ability triggers, but " + gameData.playerIdToName.get(defenderId) + " has no " + targetsLabel + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        String logEntry = creatureName + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " may return up to " + damageDealt + " " + (damageDealt > 1 ? targetsLabel : targetLabel) + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} combat damage trigger: {} damage, {} valid targets", gameData.id, creatureName, damageDealt, validIds.size());

        gameData.pendingCombatDamageBounceTargetPlayerId = defenderId;
        int maxCount = Math.min(damageDealt, validIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, attackerId, validIds, maxCount, "Return up to " + damageDealt + " " + (damageDealt > 1 ? targetsLabel : targetLabel) + " to their owner's hand.");
    }

    @HandlesEffect(PutAwakeningCountersOnTargetLandsEffect.class)
    void resolvePutAwakeningCounters(GameData gameData, StackEntry entry) {
        UUID attackerId = entry.getControllerId();
        String creatureName = entry.getCard().getName();

        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
        List<UUID> validLandIds = new ArrayList<>();
        if (attackerBattlefield != null) {
            for (Permanent perm : attackerBattlefield) {
                if (perm.getCard().hasType(CardType.LAND)) {
                    validLandIds.add(perm.getId());
                }
            }
        }

        if (validLandIds.isEmpty()) {
            String logEntry = creatureName + "'s ability triggers, but " + gameData.playerIdToName.get(attackerId) + " controls no lands.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        String logEntry = creatureName + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " may put awakening counters on lands.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} combat damage trigger: {} valid lands", gameData.id, creatureName, validLandIds.size());

        gameData.pendingAwakeningCounterPlacement = true;
        playerInputService.beginMultiPermanentChoice(gameData, attackerId, validLandIds, validLandIds.size(), "Choose any number of lands to put awakening counters on.");
    }

    private void applyOpponentMayPlayCreature(GameData gameData, UUID controllerId) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        resolvePlayerMayPlayCreature(gameData, opponentId);
    }

    @HandlesEffect(MayEffect.class)
    void resolveMayEffect(GameData gameData, StackEntry entry, MayEffect mayEffect) {
        // CR 603.5 — "you may" choice happens at resolution time.
        // Set flag so the resolution loop re-runs this effect after the player responds.
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(mayEffect.wrapped()),
                entry.getCard().getName() + " - " + mayEffect.prompt(),
                entry.getTargetId(),
                null,
                entry.getSourcePermanentId()
        ));
    }

    @HandlesEffect(MayPayManaEffect.class)
    void resolveMayPayManaEffect(GameData gameData, StackEntry entry, MayPayManaEffect mayPayEffect) {
        // CR 603.5 — "you may pay" choice happens at resolution time.
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(mayPayEffect.wrapped()),
                entry.getCard().getName() + " - " + mayPayEffect.prompt(),
                entry.getTargetId(),
                mayPayEffect.manaCost(),
                entry.getSourcePermanentId()
        ));
    }

    @HandlesEffect(SacrificeArtifactThenDealDividedDamageEffect.class)
    void resolveSacrificeArtifactThenDealDividedDamage(GameData gameData, StackEntry entry,
                                                                SacrificeArtifactThenDealDividedDamageEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<UUID> validArtifactIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isArtifact(p)) {
                    validArtifactIds.add(p.getId());
                }
            }
        }

        if (validArtifactIds.isEmpty()) {
            String logEntry = playerName + " has no artifacts to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no artifacts to sacrifice for {}",
                    gameData.id, playerName, entry.getCard().getName());
            gameData.pendingETBDamageAssignments = Map.of();
            return;
        }

        Map<UUID, Integer> damageAssignments = gameData.pendingETBDamageAssignments;
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeArtifactForDividedDamage(
                        controllerId, entry.getCard(), damageAssignments));
        playerInputService.beginPermanentChoice(gameData, controllerId, validArtifactIds,
                entry.getCard().getName() + " — Choose an artifact to sacrifice.");

        String logEntry = playerName + " is choosing an artifact to sacrifice.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} choosing artifact to sacrifice for divided damage",
                gameData.id, playerName);
    }

    @HandlesEffect(SacrificePermanentThenEffect.class)
    void resolveSacrificePermanentThen(GameData gameData, StackEntry entry,
                                                SacrificePermanentThenEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.matchesPermanentPredicate(gameData, p, effect.filter())) {
                    validIds.add(p.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            String logEntry = playerName + " has no " + effect.permanentDescription() + " to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no {} to sacrifice for {}",
                    gameData.id, playerName, effect.permanentDescription(), entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificePermanentThen(
                        controllerId, entry.getCard(), effect.thenEffect()));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose " + effect.permanentDescription() + " to sacrifice.");

        String logEntry = playerName + " is choosing " + effect.permanentDescription() + " to sacrifice.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} choosing {} to sacrifice for {}",
                gameData.id, playerName, effect.permanentDescription(), entry.getCard().getName());
    }

    private void applyPutCardToBattlefield(GameData gameData, UUID playerId, PutCardToBattlefieldEffect effect) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card handCard = hand.get(i);
                if (gameQueryService.matchesCardPredicate(handCard, effect.predicate(), handCard.getId())) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + " has no " + effect.label() + " cards in hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no {} cards in hand for hand-to-battlefield effect", gameData.id, playerName, effect.label());
            return;
        }

        String prompt = "Choose a " + effect.label() + " card from your hand to put onto the battlefield.";
        playerInputService.beginCardChoice(gameData, playerId, validIndices, prompt);
    }

    private void resolvePlayerMayPlayCreature(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);

        List<Integer> creatureIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).hasType(CardType.CREATURE)) {
                    creatureIndices.add(i);
                }
            }
        }

        if (creatureIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + " has no creature cards in hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures in hand for creature-choice effect", gameData.id, playerName);
            return;
        }

        String prompt = "You may put a creature card from your hand onto the battlefield.";
        playerInputService.beginCardChoice(gameData, playerId, creatureIndices, prompt);
    }

    private void applyDrawCards(GameData gameData, UUID playerId, int amount) {
        for (int i = 0; i < amount; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
    }

    private void resolveDiscardCards(GameData gameData, UUID playerId, int amount) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.interaction.setDiscardRemainingCount(amount);
        playerInputService.beginDiscardChoice(gameData, playerId);
    }

    private void resolveRandomDiscardCards(GameData gameData, UUID playerId, String sourceName, int amount) {
        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = playerName + " has no cards to discard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        for (int i = 0; i < amount; i++) {
            List<Card> currentHand = gameData.playerHands.get(playerId);
            if (currentHand.isEmpty()) break;
            int randomIndex = ThreadLocalRandom.current().nextInt(currentHand.size());
            Card discarded = currentHand.remove(randomIndex);
            graveyardService.addCardToGraveyard(gameData, playerId, discarded);
            String logEntry = playerName + " discards " + discarded.getName() + " at random.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(), sourceName);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, discarded);
        }

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
        if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }

    @HandlesEffect(LookAtHandEffect.class)
    void resolveLookAtHand(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(entry.getControllerId());

        if (hand == null || hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
            String logEntry = casterName + " looks at " + targetName + "'s hand: " + cardNames + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        List<CardView> cardViews = hand.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(entry.getControllerId(), new RevealHandMessage(cardViews, targetName));

        log.info("Game {} - {} looks at {}'s hand", gameData.id, casterName, targetName);
    }

    @HandlesEffect(RevealRandomCardFromTargetPlayerHandEffect.class)
    void resolveRevealRandomCardFromTargetPlayerHand(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (hand == null || hand.isEmpty()) {
            String logEntry = targetName + " has no cards to reveal.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} trigger: {} has no cards to reveal", gameData.id, sourceName, targetName);
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card revealed = hand.get(randomIndex);
        String logEntry = targetName + " reveals " + revealed.getName() + " at random.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        List<CardView> cardViews = List.of(cardViewFactory.create(revealed));
        for (UUID playerId : gameData.orderedPlayerIds) {
            sessionManager.sendToPlayer(playerId, new RevealHandMessage(cardViews, targetName));
        }

        log.info("Game {} - {} trigger: {} reveals {} at random", gameData.id, sourceName, targetName, revealed.getName());
    }

    @HandlesEffect(ChooseCardsFromTargetHandToTopOfLibraryEffect.class)
    void resolveChooseCardsFromTargetHandToTopOfLibrary(GameData gameData, StackEntry entry, ChooseCardsFromTargetHandToTopOfLibraryEffect choose) {
        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            return;
        }

        // Log and reveal hand to caster
        String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
        String logEntry = casterName + " looks at " + targetName + "'s hand: " + cardNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        int cardsToChoose = Math.min(choose.count(), hand.size());

        // Build valid indices (all cards in hand)
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        gameData.interaction.beginRevealedHandChoice(casterId, targetPlayerId, Set.copyOf(validIndices),
                cardsToChoose, false, List.of());

        playerInputService.beginRevealedHandChoice(gameData, casterId, targetPlayerId, validIndices,
                "Choose a card to put on top of " + targetName + "'s library.");

        log.info("Game {} - {} choosing {} card(s) from {}'s hand to put on top of library",
                gameData.id, casterName, cardsToChoose, targetName);
    }

    private void resolveHandRevealAndChoose(GameData gameData, StackEntry entry,
                                             int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                             boolean discardMode, boolean exileMode, UUID sourcePermanentId) {
        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);
        String actionVerb = exileMode ? "exile" : "discard";

        if (hand == null || hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            return;
        }

        // Log and reveal hand to caster
        String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
        String logEntry = targetName + " reveals their hand: " + cardNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        // Build valid indices based on included or excluded types
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card handCard = hand.get(i);
            if (!includedTypes.isEmpty()) {
                // Included mode: card must match at least one included type (primary or additional)
                boolean matches = includedTypes.contains(handCard.getType())
                        || handCard.getAdditionalTypes().stream().anyMatch(includedTypes::contains);
                if (matches) {
                    validIndices.add(i);
                }
            } else if (!excludedTypes.contains(handCard.getType())) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String noValidEntry = casterName + " cannot choose a card (" + targetName + "'s hand contains no valid choices).";
            gameBroadcastService.logAndBroadcast(gameData, noValidEntry);
            log.info("Game {} - {}'s hand has no valid choices for {}", gameData.id, targetName, casterName);
            return;
        }

        int cardsToChoose = Math.min(count, validIndices.size());

        gameData.interaction.beginRevealedHandChoice(casterId, targetPlayerId, Set.copyOf(validIndices),
                cardsToChoose, discardMode, exileMode, List.of());

        String choicePrompt;
        if (!includedTypes.isEmpty()) {
            String typeNames = includedTypes.stream()
                    .map(CardType::getDisplayName)
                    .reduce((a, b) -> a + " or " + b)
                    .orElse("card");
            choicePrompt = "Choose a " + typeNames.toLowerCase() + " card to " + actionVerb + ".";
        } else {
            choicePrompt = "Choose a nonland card to " + actionVerb + ".";
        }
        playerInputService.beginRevealedHandChoice(gameData, casterId, targetPlayerId, validIndices,
                choicePrompt);

        // Track source permanent for exile-until-source-leaves effects (must be set after
        // playerInputService.beginRevealedHandChoice which recreates the state)
        if (sourcePermanentId != null) {
            gameData.interaction.revealedHandChoice().setSourcePermanentId(sourcePermanentId);
        }

        log.info("Game {} - {} choosing {} card(s) from {}'s hand to {}",
                gameData.id, casterName, cardsToChoose, targetName, actionVerb);
    }

    @HandlesEffect(ChangeColorTextEffect.class)
    void resolveChangeColorText(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        ChoiceContext.TextChangeFromWord choiceContext = new ChoiceContext.TextChangeFromWord(targetId);
        gameData.interaction.beginColorChoice(entry.getControllerId(), null, null, choiceContext);

        List<String> options = new ArrayList<>();
        options.addAll(GameQueryService.TEXT_CHANGE_COLOR_WORDS);
        options.addAll(GameQueryService.TEXT_CHANGE_LAND_TYPES);
        sessionManager.sendToPlayer(entry.getControllerId(), new ChooseFromListMessage(options, "Choose a color word or basic land type to replace."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a color word or basic land type for text change", gameData.id, playerName);
    }

    @HandlesEffect(AwardAnyColorManaWithInstantSorceryCopyEffect.class)
    void resolveAwardAnyColorManaWithCopy(GameData gameData, StackEntry entry, AwardAnyColorManaWithInstantSorceryCopyEffect effect) {
        ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(entry.getControllerId(), false, effect.amount());
        gameData.interaction.beginColorChoice(entry.getControllerId(), null, null, choiceContext);
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        sessionManager.sendToPlayer(entry.getControllerId(), new ChooseFromListMessage(colors, "Choose a color of mana to add."));
        // Register delayed trigger: copy next instant/sorcery spell cast with this mana
        gameData.pendingNextInstantSorceryCopyCount.merge(entry.getControllerId(), 1, Integer::sum);

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a mana color (with spell copy trigger)", gameData.id, playerName);
    }

    @HandlesEffect(AwardAnyColorManaEffect.class)
    void resolveAwardAnyColorMana(GameData gameData, StackEntry entry, AwardAnyColorManaEffect effect) {
        ChoiceContext.ManaColorChoice choiceContext = new ChoiceContext.ManaColorChoice(entry.getControllerId(), false, effect.amount());
        gameData.interaction.beginColorChoice(entry.getControllerId(), null, null, choiceContext);
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        sessionManager.sendToPlayer(entry.getControllerId(), new ChooseFromListMessage(colors, "Choose a color of mana to add."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a mana color", gameData.id, playerName);
    }

    @HandlesEffect(AddManaPerAttackingCreatureEffect.class)
    void resolveAddManaPerAttackingCreature(GameData gameData, StackEntry entry, AddManaPerAttackingCreatureEffect effect) {
        UUID controllerId = entry.getControllerId();

        // Attacker count is locked at trigger time via xValue (per MTG rules: creatures
        // removed before resolution still count, tokens entering attacking after don't)
        int attackerCount = entry.getXValue();
        if (attackerCount == 0) {
            return;
        }

        // Present color choice: player picks one of the two offered colors, all mana is added as that color
        ChoiceContext.AttackManaSplitChoice choiceContext =
                new ChoiceContext.AttackManaSplitChoice(controllerId, attackerCount);
        gameData.interaction.beginColorChoice(controllerId, null, null, choiceContext);
        List<String> colors = List.of(effect.color1().name(), effect.color2().name());
        sessionManager.sendToPlayer(controllerId,
                new ChooseFromListMessage(colors, "Choose a color of mana to add (" + attackerCount + " mana)."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose mana color for {} attacking creatures",
                gameData.id, playerName, attackerCount);
    }

    @HandlesEffect(DrawAndLoseLifePerSubtypeEffect.class)
    void resolveDrawAndLoseLifePerSubtype(GameData gameData, StackEntry entry, DrawAndLoseLifePerSubtypeEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        int count = 0;
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.getCard().getSubtypes().contains(effect.subtype())) {
                    count++;
                }
            }
        }

        if (count == 0) {
            String logEntry = playerName + " controls no " + effect.subtype().getDisplayName() + "s — draws nothing and loses no life.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} controls no {}s for draw/life loss", gameData.id, playerName, effect.subtype().getDisplayName());
            return;
        }

        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }

        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            String logEntry = playerName + " draws " + count + " card" + (count != 1 ? "s" : "")
                    + " (" + entry.getCard().getName() + "). " + playerName + "'s life total can't change.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            int currentLife = gameData.getLife(controllerId);
            gameData.playerLifeTotals.put(controllerId, currentLife - count);

            String logEntry = playerName + " draws " + count + " card" + (count != 1 ? "s" : "")
                    + " and loses " + count + " life (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} draws {} and loses {} life from {}", gameData.id, playerName, count, count, entry.getCard().getName());
        }
    }

    @HandlesEffect(RedirectDrawsEffect.class)
    void resolveRedirectDraws(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();

        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            log.warn("Game {} - RedirectDraws target player not found", gameData.id);
            return;
        }

        gameData.drawReplacementTargetToController.put(targetPlayerId, controllerId);

        String cardName = entry.getCard().getName();
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = cardName + " resolves targeting " + targetName
                + ". Until end of turn, " + targetName + "'s draws are replaced.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {}: {}'s draws replaced by {} until end of turn",
                gameData.id, cardName, targetName, controllerName);
    }

    @HandlesEffect(DrawCardForTargetPlayerEffect.class)
    void resolveDrawCardForTargetPlayer(GameData gameData, StackEntry entry, DrawCardForTargetPlayerEffect effect) {
        // Intervening-if re-check at resolution time (rule 603.4):
        // If the source is still on the battlefield but now tapped, the ability does nothing.
        // If the source left the battlefield, use last known information — it was untapped
        // when the trigger was created, so the ability still resolves.
        if (effect.requireSourceUntapped() && entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null && source.isTapped()) {
                log.info("Game {} - {}'s draw trigger does nothing (source is tapped)",
                        gameData.id, entry.getCard().getName());
                return;
            }
        }

        UUID targetPlayerId = entry.getTargetId();
        for (int i = 0; i < effect.amount(); i++) {
            drawService.resolveDrawCard(gameData, targetPlayerId);
        }
    }

    @HandlesEffect(SacrificeUnlessDiscardCardTypeEffect.class)
    void resolveSacrificeUnlessDiscardCardType(GameData gameData, StackEntry entry, SacrificeUnlessDiscardCardTypeEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Find the source permanent on the battlefield
        Permanent sourcePermanent = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(sourceCard.getId())) {
                    sourcePermanent = p;
                    break;
                }
            }
        }

        // Check if the controller has any cards of the required type in hand
        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean hasValidCard = false;
        if (hand != null) {
            for (Card card : hand) {
                if (effect.requiredType() == null || card.getType() == effect.requiredType()) {
                    hasValidCard = true;
                    break;
                }
            }
        }

        String typeName = effect.requiredType() == null ? "card" : effect.requiredType().name().toLowerCase() + " card";

        if (!hasValidCard) {
            if (sourcePermanent != null) {
                // No valid cards to discard — sacrifice immediately
                permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
                String logEntry = playerName + " has no " + typeName
                        + " to discard. " + sourceCard.getName() + " is sacrificed.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrificed (no {} to discard)", gameData.id, sourceCard.getName(), typeName);
            } else {
                // Permanent already gone and no valid cards — nothing to do
                log.info("Game {} - {} is no longer on the battlefield and no {} to discard", gameData.id, sourceCard.getName(), typeName);
            }
            return;
        }

        // Has valid cards — ask the controller via the may ability system
        // Per ruling 2008-04-01: even if the creature left the battlefield, the player
        // may still choose to discard if they want.
        String prompt;
        if (sourcePermanent != null) {
            prompt = "Discard a " + typeName + "? If you don't, " + sourceCard.getName() + " will be sacrificed.";
        } else {
            prompt = sourceCard.getName() + " is no longer on the battlefield. Discard a " + typeName + " anyway?";
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, controllerId, List.of(effect), prompt
        ));
    }

    @HandlesEffect(LoseLifeUnlessDiscardEffect.class)
    void resolveLoseLifeUnlessDiscard(GameData gameData, StackEntry entry, LoseLifeUnlessDiscardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        boolean hasCards = hand != null && !hand.isEmpty();

        if (!hasCards) {
            // No cards to discard — auto-apply life loss
            if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            } else {
                int currentLife = gameData.getLife(targetPlayerId);
                gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeLoss());
                String logEntry = playerName + " has no cards to discard. " + playerName + " loses " + effect.lifeLoss() + " life.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} loses {} life (no cards to discard, {})",
                        gameData.id, playerName, effect.lifeLoss(), entry.getCard().getName());
            }
            return;
        }

        // Has cards — ask the target player via the may ability system
        String prompt = "Discard a card? If you don't, you lose " + effect.lifeLoss() + " life. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(effect), prompt
        ));
    }

    @HandlesEffect(LoseLifeUnlessPaysEffect.class)
    void resolveLoseLifeUnlessPays(GameData gameData, StackEntry entry, LoseLifeUnlessPaysEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        com.github.laxika.magicalvibes.model.ManaCost cost = new com.github.laxika.magicalvibes.model.ManaCost("{" + effect.payAmount() + "}");
        com.github.laxika.magicalvibes.model.ManaPool pool = gameData.playerManaPools.get(targetPlayerId);
        boolean canPay = cost.canPay(pool);

        if (!canPay) {
            // Can't pay — auto-apply life loss
            if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            } else {
                int currentLife = gameData.getLife(targetPlayerId);
                gameData.playerLifeTotals.put(targetPlayerId, currentLife - effect.lifeLoss());
                String logEntry = playerName + " can't pay {" + effect.payAmount() + "}. " + playerName + " loses " + effect.lifeLoss() + " life.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} loses {} life (can't pay {}, {})",
                        gameData.id, playerName, effect.lifeLoss(), effect.payAmount(), entry.getCard().getName());
            }
            return;
        }

        // Can pay — ask the target player via the may ability system
        String prompt = "Pay {" + effect.payAmount() + "}? If you don't, you lose " + effect.lifeLoss() + " life. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(effect), prompt
        ));
    }

    @HandlesEffect(SacrificeUnlessReturnOwnPermanentTypeToHandEffect.class)
    void resolveSacrificeUnlessReturnOwnPermanentType(GameData gameData, StackEntry entry, SacrificeUnlessReturnOwnPermanentTypeToHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Find the source permanent on the battlefield
        Permanent sourcePermanent = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(sourceCard.getId())) {
                    sourcePermanent = p;
                    break;
                }
            }
        }

        // Check if the controller has any permanents of the required type on the battlefield
        boolean hasValidPermanent = false;
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().hasType(effect.permanentType())) {
                    hasValidPermanent = true;
                    break;
                }
            }
        }

        String typeName = effect.permanentType().name().toLowerCase();

        if (!hasValidPermanent) {
            if (sourcePermanent != null) {
                permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
                String logEntry = playerName + " controls no " + typeName
                        + "s. " + sourceCard.getName() + " is sacrificed.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrificed (no {}s to return)", gameData.id, sourceCard.getName(), typeName);
            }
            return;
        }

        // Has valid permanents — ask the controller via the may ability system
        String prompt;
        if (sourcePermanent != null) {
            prompt = "Return an " + typeName + " you control to hand? If you don't, " + sourceCard.getName() + " will be sacrificed.";
        } else {
            prompt = sourceCard.getName() + " is no longer on the battlefield. Return an " + typeName + " you control to hand anyway?";
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, controllerId, List.of(effect), prompt
        ));
    }

    @HandlesEffect(GrantPermanentNoMaxHandSizeEffect.class)
    void resolveGrantPermanentNoMaxHandSize(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        gameData.playersWithNoMaximumHandSize.add(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " has no maximum hand size for the rest of the game.");
        log.info("Game {} - {} granted permanent no maximum hand size", gameData.id, playerName);
    }

    @HandlesEffect(RevealRandomHandCardAndPlayEffect.class)
    void resolveRevealRandomHandCardAndPlay(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (hand == null || hand.isEmpty()) {
            String logEntry = playerName + " has no cards in hand (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} trigger: {} has no cards in hand", gameData.id, sourceName, playerName);
            return;
        }

        // Reveal a card at random from hand
        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card revealed = hand.get(randomIndex);

        String revealLog = playerName + " reveals " + revealed.getName() + " at random (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, revealLog);

        List<CardView> cardViews = List.of(cardViewFactory.create(revealed));
        for (UUID playerId : gameData.orderedPlayerIds) {
            sessionManager.sendToPlayer(playerId, new RevealHandMessage(cardViews, playerName));
        }

        log.info("Game {} - {} trigger: {} reveals {}", gameData.id, sourceName, playerName, revealed.getName());

        if (revealed.hasType(CardType.LAND)) {
            // Land — remove from hand and put onto the battlefield
            hand.remove(randomIndex);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, new Permanent(revealed));

            String landLog = playerName + " puts " + revealed.getName() + " onto the battlefield (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, landLog);
            log.info("Game {} - {} puts {} onto battlefield (Wild Evocation)", gameData.id, playerName, revealed.getName());

            battlefieldEntryService.processCreatureETBEffects(gameData, targetPlayerId, revealed, null, false);
        } else {
            // Non-land — cast without paying mana cost if able
            StackEntryType spellType = mapCardTypeToSpellType(revealed);
            List<CardEffect> spellEffects = new ArrayList<>(revealed.getEffects(EffectSlot.SPELL));

            if (EffectResolution.needsTarget(revealed)) {
                // Targeted spell — check for valid targets
                Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(revealed);
                List<UUID> validTargets = new ArrayList<>();
                if (allowedTargets.contains(TargetType.PERMANENT)) {
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) continue;
                        for (Permanent p : battlefield) {
                            if (revealed.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                                if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                                    validTargets.add(p.getId());
                                }
                            } else if (gameQueryService.isCreature(gameData, p)) {
                                validTargets.add(p.getId());
                            }
                        }
                    }
                }
                if (allowedTargets.contains(TargetType.PLAYER)) {
                    validTargets.addAll(gameData.orderedPlayerIds);
                }

                if (validTargets.isEmpty()) {
                    // Can't cast — card stays in hand
                    String noTargetLog = revealed.getName() + " has no valid targets and stays in " + playerName + "'s hand.";
                    gameBroadcastService.logAndBroadcast(gameData, noTargetLog);
                    log.info("Game {} - {} can't be cast (no targets), stays in hand", gameData.id, revealed.getName());
                    return;
                }

                // Remove from hand, then prompt for target
                hand.remove(randomIndex);
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.HandCastSpellTarget(revealed, targetPlayerId, spellEffects, spellType));
                playerInputService.beginPermanentChoice(gameData, targetPlayerId, validTargets,
                        "Choose a target for " + revealed.getName() + ".");

                String castLog = playerName + " casts " + revealed.getName() + " without paying its mana cost — choosing target (" + sourceName + ").";
                gameBroadcastService.logAndBroadcast(gameData, castLog);
                log.info("Game {} - {} casts {} (Wild Evocation), choosing target", gameData.id, playerName, revealed.getName());
            } else {
                // Non-targeted spell — remove from hand and put directly on stack
                hand.remove(randomIndex);
                gameData.stack.add(new StackEntry(
                        spellType, revealed, targetPlayerId, revealed.getName(),
                        spellEffects, 0, (UUID) null, null
                ));

                gameData.recordSpellCast(targetPlayerId, revealed);
                gameData.priorityPassedBy.clear();

                String castLog = playerName + " casts " + revealed.getName() + " without paying its mana cost (" + sourceName + ").";
                gameBroadcastService.logAndBroadcast(gameData, castLog);
                log.info("Game {} - {} casts {} (Wild Evocation) without paying mana", gameData.id, playerName, revealed.getName());

                triggerCollectionService.checkSpellCastTriggers(gameData, revealed, targetPlayerId, false);
            }
        }
    }

    private StackEntryType mapCardTypeToSpellType(Card card) {
        return switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    }
}

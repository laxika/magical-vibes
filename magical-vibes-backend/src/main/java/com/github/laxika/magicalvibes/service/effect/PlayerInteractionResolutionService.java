package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RandomDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerInteractionResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(OpponentMayPlayCreatureEffect.class,
                (gd, entry, effect) -> resolveOpponentMayPlayCreature(gd, entry.getControllerId()));
        registry.register(DrawCardEffect.class,
                (gd, entry, effect) -> resolveDrawCards(gd, entry.getControllerId(), ((DrawCardEffect) effect).amount()));
        registry.register(DiscardCardEffect.class,
                (gd, entry, effect) -> {
                    gd.discardCausedByOpponent = false;
                    resolveDiscardCards(gd, entry.getControllerId(), ((DiscardCardEffect) effect).amount());
                });
        registry.register(TargetPlayerDiscardsEffect.class,
                (gd, entry, effect) -> {
                    gd.discardCausedByOpponent = true;
                    resolveDiscardCards(gd, entry.getTargetPermanentId(), ((TargetPlayerDiscardsEffect) effect).amount());
                });
        registry.register(LookAtHandEffect.class,
                (gd, entry, effect) -> resolveLookAtHand(gd, entry));
        registry.register(ChooseCardsFromTargetHandToTopOfLibraryEffect.class,
                (gd, entry, effect) -> resolveChooseCardsFromTargetHandToTopOfLibrary(gd, entry, (ChooseCardsFromTargetHandToTopOfLibraryEffect) effect));
        registry.register(ChooseCardFromTargetHandToDiscardEffect.class,
                (gd, entry, effect) -> {
                    gd.discardCausedByOpponent = true;
                    resolveChooseCardFromTargetHandToDiscard(gd, entry, (ChooseCardFromTargetHandToDiscardEffect) effect);
                });
        registry.register(ChangeColorTextEffect.class,
                (gd, entry, effect) -> resolveChangeColorText(gd, entry));
        registry.register(RedirectDrawsEffect.class,
                (gd, entry, effect) -> resolveRedirectDraws(gd, entry));
        registry.register(AwardAnyColorManaEffect.class,
                (gd, entry, effect) -> resolveAwardAnyColorMana(gd, entry));
        registry.register(DrawAndLoseLifePerSubtypeEffect.class,
                (gd, entry, effect) -> resolveDrawAndLoseLifePerSubtype(gd, entry, (DrawAndLoseLifePerSubtypeEffect) effect));
        registry.register(SacrificeUnlessDiscardCardTypeEffect.class,
                (gd, entry, effect) -> resolveSacrificeUnlessDiscardCardType(gd, entry, (SacrificeUnlessDiscardCardTypeEffect) effect));
        registry.register(DrawCardForTargetPlayerEffect.class,
                (gd, entry, effect) -> resolveDrawCardForTargetPlayer(gd, entry, (DrawCardForTargetPlayerEffect) effect));
        registry.register(RandomDiscardEffect.class,
                (gd, entry, effect) -> {
                    gd.discardCausedByOpponent = false;
                    resolveRandomDiscardCards(gd, entry.getControllerId(), entry.getCard().getName(), ((RandomDiscardEffect) effect).amount());
                });
    }

    private void resolveOpponentMayPlayCreature(GameData gameData, UUID controllerId) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures in hand for ETB effect", gameData.id, opponentName);
            return;
        }

        String prompt = "You may put a creature card from your hand onto the battlefield.";
        playerInputService.beginCardChoice(gameData, opponentId, creatureIndices, prompt);
    }

    private void resolveDrawCards(GameData gameData, UUID playerId, int amount) {
        for (int i = 0; i < amount; i++) {
            gameHelper.resolveDrawCard(gameData, playerId);
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
            gameHelper.addCardToGraveyard(gameData, playerId, discarded);
            String logEntry = playerName + " discards " + discarded.getName() + " at random.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(), sourceName);
            gameHelper.checkDiscardTriggers(gameData, playerId, discarded);
        }

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
        if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            gameHelper.processNextDiscardSelfTrigger(gameData);
        }
    }

    private void resolveLookAtHand(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(entry.getControllerId());

        if (hand.isEmpty()) {
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

    private void resolveChooseCardsFromTargetHandToTopOfLibrary(GameData gameData, StackEntry entry, ChooseCardsFromTargetHandToTopOfLibraryEffect choose) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand.isEmpty()) {
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

    private void resolveChooseCardFromTargetHandToDiscard(GameData gameData, StackEntry entry, ChooseCardFromTargetHandToDiscardEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand. It is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} looks at {}'s empty hand", gameData.id, casterName, targetName);
            return;
        }

        // Log and reveal hand to caster
        String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
        String logEntry = targetName + " reveals their hand: " + cardNames + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        // Build valid indices (exclude cards matching excluded types)
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (!effect.excludedTypes().contains(hand.get(i).getType())) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String noValidEntry = casterName + " cannot choose a card (" + targetName + "'s hand contains no valid choices).";
            gameBroadcastService.logAndBroadcast(gameData, noValidEntry);
            log.info("Game {} - {}'s hand has no valid choices for {}", gameData.id, targetName, casterName);
            return;
        }

        int cardsToChoose = Math.min(effect.count(), validIndices.size());

        gameData.interaction.beginRevealedHandChoice(casterId, targetPlayerId, Set.copyOf(validIndices),
                cardsToChoose, true, List.of());

        playerInputService.beginRevealedHandChoice(gameData, casterId, targetPlayerId, validIndices,
                "Choose a nonland card to discard.");

        log.info("Game {} - {} choosing {} card(s) from {}'s hand to discard",
                gameData.id, casterName, cardsToChoose, targetName);
    }

    private void resolveChangeColorText(GameData gameData, StackEntry entry) {
        UUID targetPermanentId = entry.getTargetPermanentId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            return;
        }

        ColorChoiceContext.TextChangeFromWord choiceContext = new ColorChoiceContext.TextChangeFromWord(targetPermanentId);
        gameData.interaction.beginColorChoice(entry.getControllerId(), null, null, choiceContext);

        List<String> options = new ArrayList<>();
        options.addAll(GameQueryService.TEXT_CHANGE_COLOR_WORDS);
        options.addAll(GameQueryService.TEXT_CHANGE_LAND_TYPES);
        sessionManager.sendToPlayer(entry.getControllerId(), new ChooseColorMessage(options, "Choose a color word or basic land type to replace."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a color word or basic land type for text change", gameData.id, playerName);
    }

    private void resolveAwardAnyColorMana(GameData gameData, StackEntry entry) {
        ColorChoiceContext.ManaColorChoice choiceContext = new ColorChoiceContext.ManaColorChoice(entry.getControllerId());
        gameData.interaction.beginColorChoice(entry.getControllerId(), null, null, choiceContext);
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        sessionManager.sendToPlayer(entry.getControllerId(), new ChooseColorMessage(colors, "Choose a color of mana to add."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a mana color", gameData.id, playerName);
    }

    private void resolveDrawAndLoseLifePerSubtype(GameData gameData, StackEntry entry, DrawAndLoseLifePerSubtypeEffect effect) {
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
            gameHelper.resolveDrawCard(gameData, controllerId);
        }

        int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
        gameData.playerLifeTotals.put(controllerId, currentLife - count);

        String logEntry = playerName + " draws " + count + " card" + (count != 1 ? "s" : "")
                + " and loses " + count + " life (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws {} and loses {} life from {}", gameData.id, playerName, count, count, entry.getCard().getName());
    }

    private void resolveRedirectDraws(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
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

    private void resolveDrawCardForTargetPlayer(GameData gameData, StackEntry entry, DrawCardForTargetPlayerEffect effect) {
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

        UUID targetPlayerId = entry.getTargetPermanentId();
        for (int i = 0; i < effect.amount(); i++) {
            gameHelper.resolveDrawCard(gameData, targetPlayerId);
        }
    }

    private void resolveSacrificeUnlessDiscardCardType(GameData gameData, StackEntry entry, SacrificeUnlessDiscardCardTypeEffect effect) {
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
                if (card.getType() == effect.requiredType()) {
                    hasValidCard = true;
                    break;
                }
            }
        }

        String typeName = effect.requiredType().name().toLowerCase();

        if (!hasValidCard) {
            if (sourcePermanent != null) {
                // No valid cards to discard — sacrifice immediately
                gameHelper.removePermanentToGraveyard(gameData, sourcePermanent);
                String logEntry = playerName + " has no " + typeName
                        + " card to discard. " + sourceCard.getName() + " is sacrificed.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrificed (no {} card to discard)", gameData.id, sourceCard.getName(), effect.requiredType());
            } else {
                // Permanent already gone and no valid cards — nothing to do
                log.info("Game {} - {} is no longer on the battlefield and no {} card to discard", gameData.id, sourceCard.getName(), effect.requiredType());
            }
            return;
        }

        // Has valid cards — ask the controller via the may ability system
        // Per ruling 2008-04-01: even if the creature left the battlefield, the player
        // may still choose to discard if they want.
        String prompt;
        if (sourcePermanent != null) {
            prompt = "Discard a " + typeName + " card? If you don't, " + sourceCard.getName() + " will be sacrificed.";
        } else {
            prompt = sourceCard.getName() + " is no longer on the battlefield. Discard a " + typeName + " card anyway?";
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, controllerId, List.of(effect), prompt
        ));
    }
}


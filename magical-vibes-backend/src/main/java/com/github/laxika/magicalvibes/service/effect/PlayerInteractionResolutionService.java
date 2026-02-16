package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectDrawsEffect;
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
import java.util.UUID;

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
                (gd, entry, effect) -> resolveDiscardCards(gd, entry.getControllerId(), ((DiscardCardEffect) effect).amount()));
        registry.register(LookAtHandEffect.class,
                (gd, entry, effect) -> resolveLookAtHand(gd, entry));
        registry.register(ChooseCardsFromTargetHandToTopOfLibraryEffect.class,
                (gd, entry, effect) -> resolveChooseCardsFromTargetHandToTopOfLibrary(gd, entry, (ChooseCardsFromTargetHandToTopOfLibraryEffect) effect));
        registry.register(ChooseCardFromTargetHandToDiscardEffect.class,
                (gd, entry, effect) -> resolveChooseCardFromTargetHandToDiscard(gd, entry, (ChooseCardFromTargetHandToDiscardEffect) effect));
        registry.register(ChangeColorTextEffect.class,
                (gd, entry, effect) -> resolveChangeColorText(gd, entry));
        registry.register(RedirectDrawsEffect.class,
                (gd, entry, effect) -> resolveRedirectDraws(gd, entry));
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

        gameData.awaitingDiscardRemainingCount = amount;
        playerInputService.beginDiscardChoice(gameData, playerId);
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

        gameData.awaitingRevealedHandChoiceTargetPlayerId = targetPlayerId;
        gameData.awaitingRevealedHandChoiceRemainingCount = cardsToChoose;
        gameData.awaitingRevealedHandChoiceDiscardMode = false;
        gameData.awaitingRevealedHandChosenCards.clear();

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

        gameData.awaitingRevealedHandChoiceTargetPlayerId = targetPlayerId;
        gameData.awaitingRevealedHandChoiceRemainingCount = cardsToChoose;
        gameData.awaitingRevealedHandChoiceDiscardMode = true;
        gameData.awaitingRevealedHandChosenCards.clear();

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

        gameData.colorChoiceContext = new ColorChoiceContext.TextChangeFromWord(targetPermanentId);
        gameData.awaitingInput = AwaitingInput.COLOR_CHOICE;
        gameData.awaitingColorChoicePlayerId = entry.getControllerId();

        List<String> options = new ArrayList<>();
        options.addAll(GameQueryService.TEXT_CHANGE_COLOR_WORDS);
        options.addAll(GameQueryService.TEXT_CHANGE_LAND_TYPES);
        sessionManager.sendToPlayer(entry.getControllerId(), new ChooseColorMessage(options, "Choose a color word or basic land type to replace."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a color word or basic land type for text change", gameData.id, playerName);
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
}

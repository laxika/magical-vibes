package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TempestEfreetAnteExchangeEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TempestEfreetAnteExchangeEffect} (Tempest Efreet). The targeted opponent may pay
 * the life to avoid the exchange: a payable opponent is prompted via the may-ability system (the
 * accept/decline branch lives in {@code TempestEfreetAnteExchangeHandler}); an opponent who can't
 * pay exchanges immediately.
 *
 * <p>The ante "exchange ownership … permanent" is resolved as the single-game observable zone
 * movements — see {@link TempestEfreetAnteExchangeEffect} — never a runtime {@code ownerId} change.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempestEfreetAnteExchangeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final CardViewFactory cardViewFactory;
    private final SessionManager sessionManager;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TempestEfreetAnteExchangeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TempestEfreetAnteExchangeEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, opponentId)
                && gameData.getLife(opponentId) >= e.lifeCost();

        if (!canPay) {
            // Can't pay the life — exchange immediately, no prompt.
            performExchange(gameData, entry.getCard(), controllerId, opponentId);
            return;
        }

        // Payable — ask the targeted opponent. Carry the ability controller in the targetCardId slot
        // so the accept/decline branch knows who takes the revealed card.
        String prompt = "Pay " + e.lifeCost() + " life? If you don't, you reveal a card at random and "
                + "exchange it for " + entry.getCard().getName() + ". (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), opponentId, List.of(e), prompt, controllerId));
    }

    /**
     * Performs the ante exchange: the opponent reveals a card at random from their hand, that card is
     * put into {@code controllerId}'s hand, and {@code efreetCard} (Tempest Efreet, in the
     * controller's graveyard after being sacrificed as a cost) is put into the opponent's graveyard.
     * Does nothing if the opponent's hand is empty (no card to reveal, nothing to exchange).
     */
    public void performExchange(GameData gameData, Card efreetCard, UUID controllerId, UUID opponentId) {
        List<Card> opponentHand = gameData.playerHands.get(opponentId);
        String opponentName = gameData.playerIdToName.get(opponentId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (opponentHand == null || opponentHand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(opponentName + " has no cards to reveal — nothing is exchanged. (", efreetCard, ")"));
            log.info("Game {} - {} has empty hand, no {} exchange", gameData.id, opponentName, efreetCard.getName());
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(opponentHand.size());
        Card revealed = opponentHand.remove(randomIndex);

        // Reveal the card publicly.
        List<CardView> cardViews = List.of(cardViewFactory.create(revealed));
        for (UUID playerId : gameData.orderedPlayerIds) {
            sessionManager.sendToPlayer(playerId, new RevealHandMessage(cardViews, opponentName));
        }

        // Exchange the physical cards: revealed card to the controller's hand, Tempest Efreet from the
        // controller's graveyard to the opponent's graveyard.
        gameData.addCardToHand(controllerId, revealed);
        List<Card> controllerGraveyard = gameData.playerGraveyards.get(controllerId);
        if (controllerGraveyard != null) {
            controllerGraveyard.removeIf(c -> c.getId().equals(efreetCard.getId()));
        }
        List<Card> opponentGraveyard = gameData.playerGraveyards.get(opponentId);
        if (opponentGraveyard != null) {
            opponentGraveyard.add(efreetCard);
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(opponentName + " reveals ").card(revealed).text(" at random. " + controllerName + " takes it, and ").card(efreetCard).text(" goes to " + opponentName + "'s graveyard.").build());
        log.info("Game {} - {} takes {} from {}; {} to {}'s graveyard", gameData.id, controllerName,
                revealed.getName(), opponentName, efreetCard.getName(), opponentName);
    }
}

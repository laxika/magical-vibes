package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureCause;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TempestEfreetAnteExchangeEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
 * <p>The ante "exchange ownership" swaps the two cards' owners for the rest of the game through
 * {@link GameData#cardOwnerOverrides} (a frozen card's stamped owner is never rewritten); the cross-game
 * permanence of an ante is not modeled — see {@link TempestEfreetAnteExchangeEffect}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempestEfreetAnteExchangeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final CardRevealService cardRevealService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;
    private final DepartureService departureService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TempestEfreetAnteExchangeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TempestEfreetAnteExchangeEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();

        // CR 119.8: a cost that has the player pay life can't be paid by a player who can't lose life.
        boolean canPay = gameQueryService.canPlayerLoseLife(gameData, opponentId)
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
     * put into {@code controllerId}'s hand, and {@code efreetCard} moves from its current zone into
     * the opponent's graveyard.
     * Does nothing if the opponent's hand is empty (no card to reveal, nothing to exchange).
     *
     * <p>A Tempest Efreet on the battlefield leaves first, as a one-permanent departure batch, so its CR 616.1
     * questions are asked before anything else happens: the caller is stopped and run again with the answers
     * (this handler resolving as a marked stack step, or the declined payment prompt's replay). The only change
     * before it, recording the exchanged owner, is the same when it is run again.
     */
    public void performExchange(GameData gameData, Card efreetCard, UUID controllerId, UUID opponentId) {
        List<Card> opponentHand = gameData.playerHands.get(opponentId);
        String opponentName = gameData.playerIdToName.get(opponentId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (opponentHand == null || opponentHand.isEmpty()) {
            gameLogService.append(gameData, GameLog.textCardText(opponentName + " has no cards to reveal — nothing is exchanged. (", efreetCard, ")"));
            log.info("Game {} - {} has empty hand, no {} exchange", gameData.id, opponentName, efreetCard.getName());
            return;
        }

        gameData.cardOwnerOverrides.put(efreetCard.getId(), opponentId);
        Permanent efreetPermanent = findOnBattlefield(gameData, efreetCard);
        if (efreetPermanent != null) {
            departureService.departNow(gameData, DepartureBatch.of(List.of(
                    new DepartureIntent(efreetPermanent.getId(), DepartureCause.OTHER, opponentId))),
                    DepartureService.Hooks.NONE);
            permanentRemovalService.removeOrphanedAuras(gameData);
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(opponentHand.size());
        Card revealed = opponentHand.remove(randomIndex);

        // Reveal the card publicly.
        cardRevealService.revealToAllPlayers(
                gameData,
                opponentId,
                GameEventFact.RevealZone.HAND,
                List.of(revealed));

        gameData.cardOwnerOverrides.put(revealed.getId(), controllerId);
        gameData.addCardToHand(controllerId, revealed);
        if (efreetPermanent == null) {
            moveSourceToOpponentsGraveyard(gameData, efreetCard, opponentId);
        }

        gameLogService.append(gameData, GameLog.builder().text(opponentName + " reveals ").card(revealed).text(" at random. " + controllerName + " takes it, and ").card(efreetCard).text(" goes to " + opponentName + "'s graveyard.").build());
        log.info("Game {} - {} takes {} from {}; {} to {}'s graveyard", gameData.id, controllerName,
                revealed.getName(), opponentName, efreetCard.getName(), opponentName);
    }

    /** The permanent {@code card} is on the battlefield as, or {@code null}. */
    private static Permanent findOnBattlefield(GameData gameData, Card card) {
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (var permanent : battlefield) {
                if (permanent.getOriginalCard().getId().equals(card.getId())) {
                    return permanent;
                }
            }
        }
        return null;
    }

    /**
     * Follows the physical card's explicit "from anywhere" instruction for a card that is not on the battlefield,
     * without duplicating it.
     */
    private void moveSourceToOpponentsGraveyard(GameData gameData, Card card, UUID opponentId) {
        for (Zone zone : List.of(Zone.HAND, Zone.LIBRARY, Zone.GRAVEYARD, Zone.COMMAND)) {
            var containers = switch (zone) {
                case HAND -> gameData.playerHands;
                case LIBRARY -> gameData.playerDecks;
                case GRAVEYARD -> gameData.playerGraveyards;
                case COMMAND -> gameData.playerCommandZones;
                default -> throw new IllegalStateException("Unexpected card container");
            };
            for (var cards : containers.values()) {
                if (cards.removeIf(candidate -> candidate.getId().equals(card.getId()))) {
                    graveyardService.addCardToGraveyard(gameData, opponentId, card, zone);
                    return;
                }
            }
        }
        if (gameData.removeFromExile(card.getId())) {
            graveyardService.addCardToGraveyard(gameData, opponentId, card, Zone.EXILE);
            return;
        }
        boolean removedSpell = gameData.stack.removeIf(entry -> entry.getCard().getId().equals(card.getId())
                && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY);
        if (removedSpell) {
            graveyardService.addCardToGraveyard(gameData, opponentId, card, Zone.STACK);
        }
    }
}

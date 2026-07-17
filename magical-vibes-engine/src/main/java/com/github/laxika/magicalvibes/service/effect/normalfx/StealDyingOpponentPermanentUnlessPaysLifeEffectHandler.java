package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link StealDyingOpponentPermanentUnlessPaysLifeEffect} (Prince of Thralls). The
 * permanent's controller ("that opponent") may pay the life to keep it from being stolen: a payable
 * opponent is prompted via the may-ability system (the decline/can't-pay branch performs the steal
 * in {@code MayPenaltyChoiceHandlerService}); an opponent who can't pay is stolen from immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StealDyingOpponentPermanentUnlessPaysLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return StealDyingOpponentPermanentUnlessPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (StealDyingOpponentPermanentUnlessPaysLifeEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID payingPlayerId = e.payingPlayerId();

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, payingPlayerId)
                && gameData.getLife(payingPlayerId) >= e.lifeCost();

        if (!canPay) {
            // Can't pay — steal immediately.
            stealPermanent(gameData, controllerId, e.dyingCardId(), entry.getCard());
            return;
        }

        // Payable — ask "that opponent". Carry the ability controller (the thief) in the targetCardId
        // slot so the decline branch knows who the permanent is put onto the battlefield under.
        String prompt = "Pay " + e.lifeCost() + " life? If you don't, the permanent is put onto the "
                + "battlefield under an opponent's control. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payingPlayerId, List.of(e), prompt, controllerId));
    }

    /**
     * Removes the dying card from its owner's graveyard and puts it onto the battlefield under
     * {@code controllerId}'s control, tracked as a stolen permanent. Fizzles silently if the card is
     * no longer in a graveyard (e.g. it was a token, or moved).
     */
    public void stealPermanent(GameData gameData, UUID controllerId, UUID dyingCardId, Card sourceCard) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        Card card = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (card == null || ownerId == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    sourceCard.getName() + "'s ability fizzles (the permanent is no longer in a graveyard)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        Permanent permanent = new Permanent(card);
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);
        graveyardReturnSupport.trackStolenCreature(gameData, permanent.getId(), controllerId, ownerId);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                playerName + " puts " + card.getName() + " onto the battlefield under their control."));
        log.info("Game {} - {} steals {} via {}", gameData.id, playerName, card.getName(), sourceCard.getName());

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
    }
}

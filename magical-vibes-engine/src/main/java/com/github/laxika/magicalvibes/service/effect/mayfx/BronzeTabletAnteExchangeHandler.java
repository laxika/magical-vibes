package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BronzeTabletAnteExchangeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Bronze Tablet ante ability — the owner of the exiled permanent "may pay 10 life. If they do, put
 * this card into its owner's graveyard. Otherwise, that player owns this card and you own the other
 * exiled card." Accept-and-can-pay costs them the life and returns Bronze Tablet to its owner's
 * graveyard; otherwise the ante swap resolves and both cards remain exiled (ownership not modeled).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BronzeTabletAnteExchangeHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BronzeTabletAnteExchangeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (BronzeTabletAnteExchangeEffect) ability.effects().getFirst();
        UUID opponentId = ability.controllerId();   // the owner of the exiled permanent — the decision maker
        Card tabletCard = ability.sourceCard();

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, opponentId)
                && gameData.getLife(opponentId) >= effect.lifeCost();

        if (accepted && canPay) {
            gameData.playerLifeTotals.put(opponentId, gameData.getLife(opponentId) - effect.lifeCost());
            moveTabletFromExileToOwnerGraveyard(gameData, tabletCard);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(player.getUsername() + " pays "
                    + effect.lifeCost() + " life. " + tabletCard.getName() + " is put into its owner's graveyard."));
            log.info("Game {} - {} pays {} life to keep {}", gameData.id, player.getUsername(),
                    effect.lifeCost(), tabletCard.getName());
        } else {
            // Declined (or can no longer pay) — the ante swap resolves; both cards remain exiled.
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(player.getUsername()
                    + " declines to pay — ownership of the exiled cards is exchanged. (" + tabletCard.getName() + ")"));
            log.info("Game {} - {} declines the {} ante swap", gameData.id, player.getUsername(), tabletCard.getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Moves the exiled Bronze Tablet card from exile to its owner's graveyard. Its owner is read from
     * the exile entry (stamped when it was exiled). No-op if the Tablet was never exiled (it had left
     * the battlefield before the ability resolved).
     */
    private void moveTabletFromExileToOwnerGraveyard(GameData gameData, Card tabletCard) {
        ExiledCardEntry entry = gameData.findExiledCard(tabletCard.getId());
        if (entry == null) {
            return;
        }
        gameData.removeFromExile(tabletCard.getId());
        graveyardService.addCardToGraveyard(gameData, entry.ownerId(), tabletCard, Zone.EXILE);
    }
}

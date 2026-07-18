package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeUnlessDiscardCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeUnlessDiscardCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeUnlessDiscardCardTypeEffect) effect;

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
                if (e.requiredType() == null || card.getType() == e.requiredType()) {
                    hasValidCard = true;
                    break;
                }
            }
        }

        String typeName = e.requiredType() == null ? "card" : e.requiredType().name().toLowerCase() + " card";

        if (!hasValidCard) {
            if (sourcePermanent != null) {
                // No valid cards to discard — sacrifice immediately
                permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
                String logEntry = playerName + " has no " + typeName
                        + " to discard. " + sourceCard.getName() + " is sacrificed.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " has no " + typeName + " to discard. ").card(sourceCard).text(" is sacrificed.").build());
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
                sourceCard, controllerId, List.of(e), prompt
        ));
    
    }
}

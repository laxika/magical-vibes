package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeUnlessDiscardCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

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

        // Check if the controller has enough matching cards in hand
        List<Card> hand = gameData.playerHands.get(controllerId);
        int validCardCount = 0;
        if (hand != null) {
            for (Card card : hand) {
                if (predicateEvaluationService.matchesCardPredicate(card, e.discardPredicate(), sourceCard.getId(),
                        gameData, controllerId)) {
                    validCardCount++;
                }
            }
        }

        String typeName = e.discardDescription();

        if (validCardCount < e.discardCount()) {
            if (sourcePermanent != null) {
                // Not enough valid cards to discard — sacrifice immediately
                permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
                
                gameLogService.append(gameData, GameLog.builder().text(playerName + " does not have enough " + typeName + "s to discard. ").card(sourceCard).text(" is sacrificed.").build());
                log.info("Game {} - {} sacrificed (fewer than {} {}s to discard)", gameData.id, sourceCard.getName(), e.discardCount(), typeName);
            } else {
                // Permanent already gone and not enough valid cards — nothing to do
                log.info("Game {} - {} is no longer on the battlefield and has fewer than {} {}s to discard", gameData.id, sourceCard.getName(), e.discardCount(), typeName);
            }
            if (e.drawCardIfNotDiscarded()) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            return;
        }

        // Has valid cards — ask the controller via the may ability system
        // Per ruling 2008-04-01: even if the creature left the battlefield, the player
        // may still choose to discard if they want.
        String discardDescription = e.discardCount() == 1
                ? "a " + typeName
                : e.discardCount() + " " + typeName + "s";
        String prompt;
        if (sourcePermanent != null) {
            prompt = "Discard " + discardDescription + "? If you don't, " + sourceCard.getName() + " will be sacrificed.";
        } else {
            prompt = sourceCard.getName() + " is no longer on the battlefield. Discard " + discardDescription + " anyway?";
        }
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, controllerId, List.of(e), prompt
        ));
    
    }
}

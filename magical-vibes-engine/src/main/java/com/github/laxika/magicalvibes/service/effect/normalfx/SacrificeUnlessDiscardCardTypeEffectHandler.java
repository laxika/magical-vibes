package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
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
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeUnlessDiscardCardTypeEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final StepDepartureSupport stepDepartureSupport;
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

        // Find the source permanent on the battlefield. A permanent that left and returned is a new object
        // (CR 400.7), so the ability's own permanent id is matched when it is known, not just the card.
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                boolean sameObject = sourcePermanentId != null
                        ? p.getId().equals(sourcePermanentId)
                        : p.getCard().getId().equals(sourceCard.getId());
                if (sameObject) {
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
                // Not enough valid cards to discard — sacrifice immediately, then draw.
                gameLogService.append(gameData, GameLog.text(playerName + " does not have enough " + typeName + "s to discard."));
                log.info("Game {} - {} sacrificed (fewer than {} {}s to discard)", gameData.id, sourceCard.getName(), e.discardCount(), typeName);
                stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, sourcePermanent.getId(), this);
                return;
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
                sourceCard, controllerId, List.of(e), prompt, null, null, sourcePermanentId, null, 0, 0, null
        ));
    
    }

    /** "If you don't, sacrifice it and draw a card": the draw follows the sacrifice. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        if (((SacrificeUnlessDiscardCardTypeEffect) effect).drawCardIfNotDiscarded()) {
            drawService.resolveDrawCard(gameData, entry.getControllerId());
        }
    }
}

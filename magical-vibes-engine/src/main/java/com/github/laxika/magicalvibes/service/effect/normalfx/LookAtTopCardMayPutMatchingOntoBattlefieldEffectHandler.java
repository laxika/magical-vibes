package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldEffect.Stage;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Look at the top card of the controller's library (CR 701.20e, so the look stays private); if it matches the
 * effect's predicate, judged with the source permanent or its last-known snapshot (CR 608.2h), offer to put it onto
 * the battlefield. A card not offered stays on top, goes into the hand, or may go to the bottom, as the effect says.
 * A {@link StepKind#RETURN} step: nothing as it begins, the permanent once the put is accepted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPutMatchingOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutMatchingOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayPutMatchingOntoBattlefieldEffect typed =
                (LookAtTopCardMayPutMatchingOntoBattlefieldEffect) effect;
        if (typed.stage() != Stage.LOOK) {
            return;
        }
        entry.recordStep(StepKind.RETURN, StepOutcome.NONE);
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        // "Look at" is private, so the card's identity is not broadcast to opponents.
        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));

        Card topCard = deck.getFirst();
        cardRevealService.revealToPlayer(gameData, controllerId, GameEventFact.RevealZone.LIBRARY,
                List.of(topCard), controllerId);
        if (predicateEvaluationService.matchesCardPredicate(topCard, typed.predicate(), entry.getCard().getId(),
                gameData, controllerId, entry.getSourcePermanentId(), null, null,
                entry.getSourcePermanentSnapshot())) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    controllerId,
                    List.of(typed.withStage(Stage.MAY_PUT)),
                    sourceName + " — Put " + topCard.getName() + " onto the battlefield?"
            ));
            return;
        }

        switch (typed.otherwise()) {
            case HAND -> {
                deck.removeFirst();
                gameData.addCardToHand(controllerId, topCard);
                gameLogService.append(gameData, GameLog.text(
                        playerName + " puts the top card of their library into their hand (" + sourceName + ")."));
            }
            case MAY_BOTTOM -> gameData.pendingMayAbilities.addFirst(
                    mayBottom(entry.getCard(), controllerId, typed, topCard));
            case TOP -> gameLogService.append(gameData,
                    GameLog.text(playerName + " leaves the top card on their library (" + sourceName + ")."));
        }
    }

    /** "You may put it on the bottom of your library", asked of the looking player about the card they saw. */
    public static PendingMayAbility mayBottom(Card sourceCard, UUID controllerId,
                                              LookAtTopCardMayPutMatchingOntoBattlefieldEffect effect, Card topCard) {
        return new PendingMayAbility(sourceCard, controllerId, List.of(effect.withStage(Stage.MAY_BOTTOM)),
                sourceCard.getName() + " — Put " + topCard.getName() + " on the bottom of your library?");
    }
}

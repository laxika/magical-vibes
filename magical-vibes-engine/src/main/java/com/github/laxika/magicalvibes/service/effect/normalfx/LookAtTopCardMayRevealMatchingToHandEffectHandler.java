package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect.Stage;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Private peek at the top card (CR 701.20e), then may reveal a matching card to hand; a card that stays out of hand
 * stays on top, may be put into the graveyard, is put on the bottom, or may be put on the bottom, as the effect says.
 * The predicate's subjects ("shares a card type with that permanent") are bound to the entry as the card is looked at
 * (CR 608.2h). A {@link StepKind#REVEAL} step: nothing as it begins, the card as the may-reveal is accepted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SubjectResolutionService subjectResolutionService;
    @Autowired
    private CardRevealService cardRevealService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayRevealMatchingToHandEffect e =
                (LookAtTopCardMayRevealMatchingToHandEffect) effect;
        if (e.stage() != Stage.LOOK) {
            return;
        }
        entry.recordStep(StepKind.REVEAL, StepOutcome.NONE);

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        // "Look at" is private — do not broadcast the card's identity.
        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));

        Card topCard = deck.getFirst();
        cardRevealService.revealToPlayer(gameData, controllerId, GameEventFact.RevealZone.LIBRARY,
                List.of(topCard), controllerId);
        CardPredicate predicate = subjectResolutionService.bindCardSubjects(gameData, entry, e, e.predicate());
        // The source may have left the battlefield while its ability was on the stack; the entry's
        // snapshot keeps the chosen characteristic (Gathering Stone's chosen type) for the match.
        boolean matches = predicateEvaluationService.matchesCardPredicate(
                topCard, predicate, entry.getCard().getId(), gameData, controllerId,
                entry.getSourcePermanentId(), null, null, entry.getSourcePermanentSnapshot());

        if (matches) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    controllerId,
                    List.of(e.withStage(Stage.MAY_HAND)),
                    sourceName + " — Reveal " + topCard.getName() + " and put it into your hand?"
            ));
            log.info("Game {} - {} looks at matching {} — may put to hand ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
            return;
        }

        switch (e.otherwiseDestination()) {
            case BOTTOM -> {
                Card bottomCard = deck.removeFirst();
                deck.addLast(bottomCard);
                gameLogService.append(gameData, GameLog.text(
                        playerName + " puts the card on the bottom of their library (" + sourceName + ")."));
                log.info("Game {} - {} puts non-matching {} on the bottom of their library ({})",
                        gameData.id, playerName, bottomCard.getName(), sourceName);
            }
            case GRAVEYARD -> {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(),
                        controllerId,
                        List.of(e.withStage(Stage.MAY_GRAVEYARD)),
                        sourceName + " — Put " + topCard.getName() + " into your graveyard?"
                ));
                log.info("Game {} - {} looks at non-matching {} — may put to graveyard ({})",
                        gameData.id, playerName, topCard.getName(), sourceName);
            }
            case MAY_BOTTOM -> gameData.pendingMayAbilities.addFirst(mayBottom(entry.getCard(), controllerId,
                    e, topCard));
            case TOP -> log.info("Game {} - {} looks at non-matching top card — it stays on top ({})",
                    gameData.id, playerName, sourceName);
        }
    }

    /** "You may put it on the bottom of your library", asked of the looking player about the card they saw. */
    public static PendingMayAbility mayBottom(Card sourceCard, UUID controllerId,
                                              LookAtTopCardMayRevealMatchingToHandEffect effect, Card topCard) {
        return new PendingMayAbility(sourceCard, controllerId, List.of(effect.withStage(Stage.MAY_BOTTOM)),
                sourceCard.getName() + " — Put " + topCard.getName() + " on the bottom of your library?");
    }
}

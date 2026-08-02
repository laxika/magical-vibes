package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect.Stage;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Private peek at the top card, then may reveal a matching card to hand; when the effect opts in,
 * a card that stays out of hand may instead be put into the graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

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
        boolean matches = predicateEvaluationService.matchesCardPredicate(
                topCard, e.predicate(), entry.getCard().getId(), gameData, controllerId);

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

        if (!e.mayGraveyardOtherwise()) {
            log.info("Game {} - {} looks at non-matching top card — it stays on top ({})",
                    gameData.id, playerName, sourceName);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(e.withStage(Stage.MAY_GRAVEYARD)),
                sourceName + " — Put " + topCard.getName() + " into your graveyard?"
        ));
        log.info("Game {} - {} looks at non-matching {} — may put to graveyard ({})",
                gameData.id, playerName, topCard.getName(), sourceName);
    }
}

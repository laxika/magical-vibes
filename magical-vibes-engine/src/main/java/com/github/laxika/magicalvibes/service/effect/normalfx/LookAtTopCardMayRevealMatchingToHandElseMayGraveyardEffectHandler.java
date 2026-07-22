package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect.Stage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Archghoul of Thraben-style look: private peek, then may reveal matching card to hand, else may
 * put into graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect e =
                (LookAtTopCardMayRevealMatchingToHandElseMayGraveyardEffect) effect;
        if (e.stage() != Stage.LOOK) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        // "Look at" is private — do not broadcast the card's identity.
        gameBroadcastService.logAndBroadcast(gameData,
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
        } else {
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
}

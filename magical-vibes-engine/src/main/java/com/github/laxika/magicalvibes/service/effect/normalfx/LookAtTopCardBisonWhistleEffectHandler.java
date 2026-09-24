package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardBisonWhistleEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Bison Whistle's private top-card look and queues its first applicable choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardBisonWhistleEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardBisonWhistleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardBisonWhistleEffect typed = (LookAtTopCardBisonWhistleEffect) effect;
        if (typed.stage() != LookAtTopCardBisonWhistleEffect.Stage.LOOK) {
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

        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));

        Card topCard = deck.getFirst();
        if (predicateEvaluationService.matchesCardPredicate(
                topCard, new CardSubtypePredicate(CardSubtype.BISON), entry.getCard().getId(),
                gameData, controllerId)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    controllerId,
                    List.of(typed.withStage(LookAtTopCardBisonWhistleEffect.Stage.MAY_BATTLEFIELD)),
                    sourceName + " — Put " + topCard.getName() + " onto the battlefield?"
            ));
            return;
        }

        if (topCard.hasType(CardType.CREATURE)) {
            queueCreatureHandChoice(gameData, entry, topCard);
        } else {
            queueGraveyardChoice(gameData, entry, topCard);
        }
    }

    private void queueCreatureHandChoice(GameData gameData, StackEntry entry, Card topCard) {
        LookAtTopCardMayRevealMatchingToHandEffect handEffect =
                new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        LookAtTopCardMayRevealMatchingToHandEffect.OtherwiseDestination.TOP,
                        LookAtTopCardMayRevealMatchingToHandEffect.Stage.MAY_HAND);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(handEffect),
                entry.getCard().getName() + " — Reveal " + topCard.getName()
                        + " and put it into your hand?"
        ));
    }

    private void queueGraveyardChoice(GameData gameData, StackEntry entry, Card topCard) {
        LookAtTopCardMayRevealMatchingToHandEffect graveyardEffect =
                new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        LookAtTopCardMayRevealMatchingToHandEffect.OtherwiseDestination.GRAVEYARD,
                        LookAtTopCardMayRevealMatchingToHandEffect.Stage.MAY_GRAVEYARD);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(graveyardEffect),
                entry.getCard().getName() + " — Put " + topCard.getName() + " into your graveyard?"
        ));
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Rulik Mons's top-card land-or-Goblin attack trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typed = (LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            createFallbackToken(gameData, entry, typed);
            return;
        }

        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));

        Card topCard = deck.getFirst();
        if (!predicateEvaluationService.matchesCardPredicate(topCard, typed.predicate(), entry.getCard().getId())) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + " leaves the top card on their library (" + sourceName + ")."));
            createFallbackToken(gameData, entry, typed);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(typed.withStage(LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect.Stage.MAY_PUT)),
                sourceName + " — Put " + topCard.getName() + " onto the battlefield?",
                null,
                null,
                entry.getSourcePermanentId()));
    }

    void createFallbackToken(GameData gameData, StackEntry entry,
                             LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect effect) {
        createTokenEffectHandler.resolve(gameData, entry, effect.fallbackToken());
        log.info("Game {} - {} creates the fallback token", gameData.id, entry.getCard().getName());
    }
}

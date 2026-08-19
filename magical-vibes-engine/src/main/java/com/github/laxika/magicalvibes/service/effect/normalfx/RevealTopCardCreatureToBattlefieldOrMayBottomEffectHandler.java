package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardCreatureToBattlefieldOrMayBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardCreatureToBattlefieldOrMayBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardCreatureToBattlefieldOrMayBottomEffect typed =
                (RevealTopCardCreatureToBattlefieldOrMayBottomEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + ").";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        Card topCard = deck.getFirst();

        // Reveal the card to all players
        
        gameLogService.append(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard).text(" from the top of their library (" + sourceName + ").").build());

        boolean matchesPredicate = predicateEvaluationService.matchesCardPredicate(
                topCard, typed.predicate(), entry.getCard().getId());

        if (matchesPredicate && !typed.mayPutMatching()) {
            deck.removeFirst();
            Permanent perm = new Permanent(topCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

            gameLogService.append(gameData, GameLog.builder()
                    .card(topCard)
                    .text(" enters the battlefield under " + playerName + "'s control (" + sourceName + ").")
                    .build());

            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, topCard, null, false);

            log.info("Game {} - {} puts {} onto the battlefield ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
        } else {
            // A matching card may be put onto the battlefield, while a nonmatching card may be
            // put on the bottom. The may handler distinguishes the two choices from the live top card.
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), controllerId,
                    List.of(typed),
                    matchesPredicate
                            ? sourceName + " — Put " + topCard.getName() + " onto the battlefield?"
                            : sourceName + " — Put " + topCard.getName() + " on the bottom of your library?"
            ));
        }
    
    }
}

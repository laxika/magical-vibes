package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardRemoveTargetFromCombatIfMatchEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardRemoveTargetFromCombatIfMatchEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardRemoveTargetFromCombatIfMatchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardRemoveTargetFromCombatIfMatchEffect e = (RevealTopCardRemoveTargetFromCombatIfMatchEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        Card topCard = deck.removeFirst();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + topCard.getName() + " from the top of their library (" + sourceName + ")."));

        if (predicateEvaluationService.matchesCardPredicate(topCard, e.matchPredicate(), null, gameData, controllerId)) {
            Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (attacker != null && attacker.isAttacking()) {
                attacker.setAttacking(false);
                attacker.setAttackTarget(null);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName + " removes " + attacker.getCard().getName() + " from combat."));
                log.info("Game {} - {} removes {} from combat", gameData.id, sourceName, attacker.getCard().getName());
            }
        }

        deck.add(topCard);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + topCard.getName() + " on the bottom of their library."));
        log.info("Game {} - {} bottoms {} ({})", gameData.id, playerName, topCard.getName(), sourceName);
    
    }
}

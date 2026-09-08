package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the random hand reveal used by Friendly Fire and records the revealed card's mana
 * value for the following damage effects.
 */
@Component
@RequiredArgsConstructor
public class RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.setEventValue(0);

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + " has no cards to reveal."));
            return;
        }

        Card revealed = hand.get(ThreadLocalRandom.current().nextInt(hand.size()));
        gameLogService.append(gameData,
                GameLog.textCardText(controllerName + " reveals ", revealed, " at random."));
        cardRevealService.revealToAllPlayers(
                gameData, controllerId, GameEventFact.RevealZone.HAND, List.of(revealed));
        entry.setEventValue(revealed.getManaValue());
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandBoostTargetCreatureByManaValueEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealRandomCardFromTargetPlayerHandBoostTargetCreatureByManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealRandomCardFromTargetPlayerHandBoostTargetCreatureByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealRandomCardFromTargetPlayerHandBoostTargetCreatureByManaValueEffect) effect;
        List<UUID> playerGroup = entry.targetsForGroup(e.targetPlayerGroup());
        if (playerGroup.isEmpty()) {
            return;
        }

        UUID targetPlayerId = playerGroup.getFirst();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetName + " has no cards to reveal."));
            log.info("Game {} - {} ability: {} has no cards to reveal", gameData.id, sourceName, targetName);
            return;
        }

        Card revealed = hand.get(ThreadLocalRandom.current().nextInt(hand.size()));
        gameLogService.append(gameData,
                GameLog.textCardText(targetName + " reveals ", revealed, " at random."));
        cardRevealService.revealToAllPlayers(
                gameData,
                targetPlayerId,
                GameEventFact.RevealZone.HAND,
                List.of(revealed));

        int manaValue = revealed.getManaValue();
        List<UUID> creatureGroup = entry.targetsForGroup(e.targetCreatureGroup());
        if (creatureGroup.isEmpty() || manaValue == 0) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, creatureGroup.getFirst());
        if (target == null) {
            return;
        }

        int modifier = e.positive() ? manaValue : -manaValue;
        target.setPowerModifier(target.getPowerModifier() + modifier);
        target.setToughnessModifier(target.getToughnessModifier() + modifier);
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(String.format(" gets %+d/%+d until end of turn.", modifier, modifier))
                .build());
        log.info("Game {} - {} gets {}/{}", gameData.id, target.getCard().getName(), modifier, modifier);
    }
}

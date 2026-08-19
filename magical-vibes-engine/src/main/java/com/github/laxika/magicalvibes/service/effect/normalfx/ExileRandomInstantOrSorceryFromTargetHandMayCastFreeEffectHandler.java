package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromTargetHandMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Planeswalker's Mischief's random hand-card exile and free-cast permission. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileRandomInstantOrSorceryFromTargetHandMayCastFreeEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomInstantOrSorceryFromTargetHandMayCastFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " has no cards in hand — nothing is revealed."));
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card revealed = hand.get(randomIndex);
        cardRevealService.revealToAllPlayers(
                gameData, targetPlayerId, GameEventFact.RevealZone.HAND, List.of(revealed));

        if (!revealed.hasType(CardType.INSTANT) && !revealed.hasType(CardType.SORCERY)) {
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " reveals ").card(revealed)
                    .text(" at random from their hand; it is not an instant or sorcery.").build());
            return;
        }

        hand.remove(randomIndex);
        exileService.exileCard(gameData, targetPlayerId, revealed);
        gameData.exilePlayPermissions.put(revealed.getId(), entry.getControllerId());
        gameData.exilePlayWithoutPayingManaCost.add(revealed.getId());
        gameData.queueDelayedAction(new ReturnExiledCardToHandAtNextEndStep(
                revealed.getId(), targetPlayerId, entry.getCard()));

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ").card(revealed)
                .text(" at random from their hand and exiles it; its controller may cast it "
                        + "without paying its mana cost until the next end step.").build());
        log.info("Game {} - {} exiles {} at random from hand for free casting",
                gameData.id, playerName, revealed.getName());
    }
}

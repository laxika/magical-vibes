package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Privately looks at the top card and offers the reveal, hand, and life-loss sequence as one choice.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect typed =
                (LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect) effect;
        if (typed.stage() != LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect.Stage.LOOK) {
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
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(typed.withMayRevealStage()),
                sourceName + " - Reveal " + topCard.getName()
                        + " and put it into your hand? If you do, lose life equal to its mana value."
        ));
        log.info("Game {} - {} may reveal {} via {}", gameData.id, playerName, topCard.getName(), sourceName);
    }
}
